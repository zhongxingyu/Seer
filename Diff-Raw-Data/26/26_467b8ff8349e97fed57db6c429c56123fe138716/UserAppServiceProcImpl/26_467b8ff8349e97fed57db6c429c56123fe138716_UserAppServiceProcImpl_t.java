 package com.eucalyptus.webui.server;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import com.eucalyptus.webui.client.service.EucalyptusServiceException;
 import com.eucalyptus.webui.client.service.SearchRange;
 import com.eucalyptus.webui.client.service.SearchResult;
 import com.eucalyptus.webui.client.service.SearchResultRow;
 import com.eucalyptus.webui.client.session.Session;
 import com.eucalyptus.webui.server.config.SearchTableCol;
 import com.eucalyptus.webui.server.config.ViewSearchTableColConfig;
 import com.eucalyptus.webui.server.db.ResultSetWrapper;
 import com.eucalyptus.webui.server.device.DeviceTemplateServiceProcImpl;
 import com.eucalyptus.webui.server.dictionary.DBTableColName;
 import com.eucalyptus.webui.server.user.UserAppDBProcWrapper;
 import com.eucalyptus.webui.server.user.UserAppSyncException;
 import com.eucalyptus.webui.server.vm.VITDBProcWrapper;
 import com.eucalyptus.webui.server.vm.VITSyncException;
 import com.eucalyptus.webui.server.vm.VmImageType;
 import com.eucalyptus.webui.shared.config.LanguageSelection;
import com.eucalyptus.webui.shared.dictionary.Enum2String;
 import com.eucalyptus.webui.shared.resource.Template;
 import com.eucalyptus.webui.shared.user.EnumUserAppStatus;
 import com.eucalyptus.webui.shared.user.LoginUserProfile;
 import com.eucalyptus.webui.shared.user.UserApp;
 import com.eucalyptus.webui.shared.user.UserAppStateCount;
 
 public class UserAppServiceProcImpl {
 	  
 	  public void addUserApp(Session session, UserApp userApp) throws EucalyptusServiceException {
 		  
 		  userApp.setStatus(EnumUserAppStatus.APPLYING);
 		  
 		  Calendar cal = Calendar.getInstance();
 		  Date date = cal.getTime();
 		  userApp.setAppTime(date);
 		  
 		  long srvDuration = userApp.getSrvEndingTime().getTime() - userApp.getSrvStartingTime().getTime();
 		  try {
 			  //update device state by user application
 			  deviceTemDBProc.actionTemplate(session, userApp.getUserId(), userApp.getTemplateId(), (int)srvDuration);
 			  
 			  userAppDBProc.addUserApp(userApp);
 		  }
 		  catch (UserAppSyncException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				throw new EucalyptusServiceException("Failed to add user app");
 		  }
 	  }
 	  
 	  /**
 	   * Search keys.
 	   * 
 	   * @param session
 	   * @param search
 	   * @param range
 	   * @return
 	   * @throws EucalyptusServiceException
 	 * @throws UserAppSyncException 
 	   */
 	  public SearchResult lookupUserApp( LoginUserProfile curUser, String search, SearchRange range, EnumUserAppStatus state ) throws EucalyptusServiceException {
 		  boolean isRootAdmin = curUser.isSystemAdmin();
 		  
 		  ResultSetWrapper rs;
 		  try {
 			  if (isRootAdmin) {
 				  rs = userAppDBProc.queryUserApp(0, 0, state);
 			  }
 			  else {		  
 				  rs = userAppDBProc.queryUserApp(curUser.getAccountId(), curUser.getUserId(), state);
 			  }
 		  } catch (UserAppSyncException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				throw new EucalyptusServiceException("Fail to query user apps");
 			}
 		  if (rs == null)
 			  return null;
 		  
 		  return getSearchResult(isRootAdmin, rs, range);
 	  }
 	  
 	  public void deleteUserApps(ArrayList<String> ids ) throws EucalyptusServiceException {
 		  try {
 			  userAppDBProc.delUserApps(ids);
 		} catch (UserAppSyncException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new EucalyptusServiceException("Failed to delete user apps");
 		}
 	  }
 	  
 	  public void updateUserApp(UserApp userApp) throws EucalyptusServiceException {
 		  try {
 			  userAppDBProc.updateUserApp(userApp);
 		  } catch (UserAppSyncException e) {
 			  // TODO Auto-generated catch block
 			  e.printStackTrace();
 			  throw new EucalyptusServiceException("Failed to update user apps");
 		  }
 	  }
 	  /**
 	   * @param session
 	   * @param userAppId
 	   * @return eucalyptus vm instance key
 	   * @throws EucalyptusServiceException
 	   */
 	  public String runVMInstance(Session session, int userAppId) throws EucalyptusServiceException {
 		  try {
 			  UserApp userApp = this.userAppDBProc.lookupUserApp(userAppId);
 			  
 			  int templateId = userApp.getTemplateId();
 			  
 			  String keyPair = userApp.getKeyPair();
 			  String securityGroup = userApp.getSecurityGroup();
 			  
 			  String euca_vit_id = null;	  
 			  VmImageType vit = this.vitDBProc.lookupVIT(userApp.getVmIdImageTypeId());
 			  if (vit != null)
 				  euca_vit_id = vit.getEucaVITId();
 			  
 			  Template template = deviceTemDBProc.lookupTemplateByID(session, templateId);
 			  
 			  if (keyPair != null && securityGroup != null && euca_vit_id != null) {
 			    //FIXME userID
 				  String euca_vi_key = EucaServiceWrapper.getInstance().runVM(session, 0, template, keyPair, securityGroup, euca_vit_id);
 				  
 				  if (euca_vi_key != null) {
 					  return euca_vi_key;
 				  }
 				  else
 					  throw new EucalyptusServiceException("Failed to get eucalyptus vm instance key");
 			  }
 			  else
 				  throw new EucalyptusServiceException("User's key_pair or security group para error");
 			  
 		  } catch (UserAppSyncException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new EucalyptusServiceException("Failed to query user app");
 		  } catch (VITSyncException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new EucalyptusServiceException("Failed to query eucalyptus vm image type id");
 		}
 	  }
 	  
 	  public ArrayList<UserAppStateCount> countUserApp(LoginUserProfile curUser) throws EucalyptusServiceException {
 		  try {
 			  if (curUser.isSystemAdmin())
 				  return userAppDBProc.countUserAppByState(0,0);
 			  else if (curUser.isAccountAdmin())
 				  return userAppDBProc.countUserAppByState(curUser.getAccountId(), 0);
 			  else
 				  return userAppDBProc.countUserAppByState(curUser.getAccountId(), curUser.getUserId());
 			  
 		} catch (UserAppSyncException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new EucalyptusServiceException("Failed to update user apps");
 		}
 	  }
 	  
 	  private SearchResult getSearchResult(boolean isRootAdmin, ResultSetWrapper rs, SearchRange range) {
 		  
 		  assert (range != null);
 		  
 		  final int sortField = range.getSortField( );
 		  
 		  DATA = resultSet2List(rs);
 		  
 		  int resultLength = Math.min( range.getLength( ), DATA.size( ) - range.getStart( ) );
 		  SearchResult result = new SearchResult( DATA.size( ), range );
 		  result.setDescs( ViewSearchTableColConfig.instance().getConfig(UserAppServiceProcImpl.class.getName(), LanguageSelection.instance().getCurLanguage())  );
 		  result.setRows( DATA.subList( range.getStart( ), range.getStart( ) + resultLength ) );
 			
 		  return result;
 	  }
 	  
 	  private List<SearchResultRow> resultSet2List(ResultSetWrapper rsWrapper) {
 		  ResultSet rs = rsWrapper.getResultSet();
 		  int index = 1;
 		  List<SearchResultRow> result = null;
 		  
 		  try {
 			  if (rs != null) {
 				  result = new ArrayList<SearchResultRow>();
 				  
 				  ArrayList<SearchTableCol> tableCols = ViewSearchTableColConfig.instance().getConfig(UserAppServiceProcImpl.class.getName());
 				  String[] dbFields = new String[tableCols.size()];
 				  
 				  for (int i=0; i<dbFields.length; i++)
 					  dbFields[i] = tableCols.get(i).getDbField();
 				  
 				  while (rs.next()) {
 					  ArrayList<String> rowValue = new ArrayList<String>();
 					  
 					  String cpu=null, cpuCount=null, mem=null, disk=null;
 					  String os=null, ver=null;
 					  
 					  for (int i=0; i<dbFields.length; i++) {
 						  String value = null;
 						  if (!dbFields[i].equalsIgnoreCase("null")) {
 							  if (dbFields[i].equalsIgnoreCase("TEMPLATE_DETAILS")) {
 								  value = "...";
 							  }
 							  else if (dbFields[i].equalsIgnoreCase("VM_IMAGE_DETAILS")) {
 								  value = "...";
 							  }
							  else if (dbFields[i].equalsIgnoreCase(DBTableColName.USER_APP.STATUS))
								  value = Enum2String.getInstance().getUserAppStateName(rs.getString(dbFields[i]));
 							  else
 								  value = rs.getString(dbFields[i]);
 							  
 							  if (dbFields[i].equalsIgnoreCase(DBTableColName.TEMPLATE.CPU))
 								  cpu = value;
 							  else if (dbFields[i].equalsIgnoreCase(DBTableColName.TEMPLATE.NCPUS))
 								  cpuCount = value;
 							  else if (dbFields[i].equalsIgnoreCase(DBTableColName.TEMPLATE.MEM))
 								  mem = value;
 							  else if (dbFields[i].equalsIgnoreCase(DBTableColName.TEMPLATE.DISK))
 								  disk = value;
 							  else if (dbFields[i].equalsIgnoreCase(DBTableColName.VM_IMAGE_TYPE.OS))
 								  os = value;
 							  else if (dbFields[i].equalsIgnoreCase(DBTableColName.VM_IMAGE_TYPE.VER))
 								  ver = value;
 						  }
 						  else
 							  value = Integer.toString(index++);
 					  			 
 						  rowValue.add(value);
 					  }
 					  
 					  String template_details = formatTemplateInfo(cpu, cpuCount, mem, disk);
 					  String vm_details = formatVMImageTypeInfo(os, ver);

 					  List<String> links = Arrays.asList(null, null, null, null, template_details, vm_details,	null, null, null, null, null, null);
 					  
 					  SearchResultRow row = new SearchResultRow(rowValue, links);
 					  
 					  result.add(row);
 				  }
 			  }
 			rsWrapper.close();			
 		  } catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 		  }
 		  
 		  return result;
 	  }
 	  
 	  private String formatTemplateInfo(String cpu, String cpuCount, String mem, String disk) {
 		  StringBuilder str = new StringBuilder();
 		  
 		  str.append("模板详细信息：").append("\n");
 		  str.append("    CPU： ").append(cpu).append("\n");
 		  str.append("    CPU数量： ").append(cpuCount).append("\n");
 		  str.append("    内存： ").append(mem).append("GB").append("\n");
 		  str.append("    磁盘： ").append(disk).append("GB");
 		  
 		  return str.toString();
 	  }
 	  
 	  private String formatVMImageTypeInfo(String os, String version) {
 		  StringBuilder str = new StringBuilder();
 		  
 		  str.append("镜像详细信息：").append("\n");
 		  str.append("    操作系统： ").append(os).append("\n");
 		  str.append("    版本： ").append(version);
 		  
 		  return str.toString();
 	  }
 	  
 	  
 	  private UserAppDBProcWrapper userAppDBProc = new UserAppDBProcWrapper();
 	  private DeviceTemplateServiceProcImpl deviceTemDBProc = new DeviceTemplateServiceProcImpl();
 	  private VITDBProcWrapper vitDBProc = new VITDBProcWrapper();
 	  
 	  private static List<SearchResultRow> DATA = null;
 }
