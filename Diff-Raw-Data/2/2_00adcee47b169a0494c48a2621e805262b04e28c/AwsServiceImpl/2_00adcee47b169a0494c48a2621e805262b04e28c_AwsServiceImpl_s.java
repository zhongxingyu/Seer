 package com.eucalyptus.webui.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.amazonaws.ClientConfiguration;
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.AmazonEC2Client;
 import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
 import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
 import com.amazonaws.services.ec2.model.CreateKeyPairResult;
 import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
 import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
 import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;
 import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
 import com.amazonaws.services.ec2.model.DescribeImagesResult;
 import com.amazonaws.services.ec2.model.DescribeInstancesResult;
 import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
 import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
 import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
 import com.amazonaws.services.ec2.model.Image;
 import com.amazonaws.services.ec2.model.Instance;
 import com.amazonaws.services.ec2.model.InstanceType;
 import com.amazonaws.services.ec2.model.IpPermission;
 import com.amazonaws.services.ec2.model.KeyPairInfo;
 import com.amazonaws.services.ec2.model.Reservation;
 import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
 import com.amazonaws.services.ec2.model.RunInstancesRequest;
 import com.amazonaws.services.ec2.model.RunInstancesResult;
 import com.amazonaws.services.ec2.model.SecurityGroup;
 import com.amazonaws.services.ec2.model.StartInstancesRequest;
 import com.amazonaws.services.ec2.model.StopInstancesRequest;
 import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
 import com.eucalyptus.webui.client.service.AwsService;
 import com.eucalyptus.webui.client.service.EucalyptusServiceException;
 import com.eucalyptus.webui.client.service.SearchRange;
 import com.eucalyptus.webui.client.service.SearchResult;
 import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
 import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
 import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
 import com.eucalyptus.webui.client.service.SearchResultRow;
 import com.eucalyptus.webui.client.session.Session;
 import com.eucalyptus.webui.server.db.DBProcWrapper;
 import com.eucalyptus.webui.server.dictionary.DBTableColName;
 import com.eucalyptus.webui.server.dictionary.DBTableName;
 import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
 import com.eucalyptus.webui.shared.aws.ImageType;
 import com.eucalyptus.webui.shared.query.QueryParser;
 import com.eucalyptus.webui.shared.query.QueryParsingException;
 import com.eucalyptus.webui.shared.query.QueryType;
 import com.eucalyptus.webui.shared.query.SearchQuery;
 import com.eucalyptus.webui.shared.user.LoginUserProfile;
 import com.google.common.collect.Lists;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class AwsServiceImpl extends RemoteServiceServlet implements AwsService {
 	/**
    * 
    */
   private static final long serialVersionUID = 1L;
   static final String FALLBACK_ACCESS_KEY="6CQT6BWUN4CDB9HTTHZOX";
 	static final String FALLBACK_SECRET_KEY="cboRkSRFMd2hhokkIcVAxDbwTO6828wIB1mD2x5H";
 	static final String FALLBACK_ENDPOINT="http://166.111.134.30:8773/services/Eucalyptus";
 	static final String FALLBACK_SSH_HOST="root@166.111.134.30";
 	static String ENDPOINT = null;
 	static boolean CAN_FALLBACK = true;
 	
 	static final String TYPE_KERNEL = "kernel";
 	static final String TYPE_RAMDISK = "ramdisk";
 	static final String TYPE_ROOTFS = "machine";
 	static final Set<String> TYPES = new HashSet<String>();
 	static {
 	  TYPES.add(TYPE_KERNEL);
 	  TYPES.add(TYPE_RAMDISK);
 	  TYPES.add(TYPE_ROOTFS);
 	}
 	
 	private static final Logger LOG = Logger.getLogger(AwsServiceImpl.class);
 	DBProcWrapper wrapper = DBProcWrapper.Instance();
 	
 	public static final ArrayList<SearchResultFieldDesc> INSTANCE_COMMON_FIELD_DESCS = Lists.newArrayList();
 	static {
 		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "虚拟机ID", true, "10%") );
 		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "镜像ID", true, "10%") );
 		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "公共IP", true, "10%") );
 		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "私有IP", true, "10%") );
 		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "密钥", true, "10%") );
 		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "所属安全组", true, "10%") );
 		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "使用用户", true, "10%") );
 		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "开始时间", true, "10%") );
 		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "服务期限", true, "10%") );
 		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "剩余时间", true, "10%") );
 		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "运行状态", true, "10%") );		
 	}
 	public static final ArrayList<SearchResultFieldDesc> IMAGE_COMMON_FIELD_DESCS = Lists.newArrayList();
 	static {
 	  IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "全选", "5%", false));
 	  IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("系统名称", true, "10%"));
 	  IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("系统版本", true, "10%"));
 		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("镜像ID", true, "10%"));
 		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("类型", true, "10%"));
 		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("架构", true, "10%"));
 		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("位置", true, "20%"));
 		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("内核", true, "10%"));
 		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("Ramdisk", true, "10%"));
 		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("状态", true, "10%"));
 	}
 
   public static final ArrayList<SearchResultFieldDesc> KEYPAIR_COMMON_FIELD_DESCS = Lists.newArrayList();
   static {
     KEYPAIR_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("名称", true, "10%"));
     KEYPAIR_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("指纹", true, "10%"));
   }
   
   public static final ArrayList<SearchResultFieldDesc> SECURITY_GROUP_COMMON_FIELD_DESCS = Lists.newArrayList();
   public static final String IP_PERMISSION = "ip_permission";
   public static final String SEGROUP = "segroup";
   static {
     //SECURITY_GROUP_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("所属用户", true, "10%"));
     //SECURITY_GROUP_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("安全组ID", true, "10%"));
     SECURITY_GROUP_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("安全组名称", true, "10%"));
     SECURITY_GROUP_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("安全组描述", true, "10%"));
     SECURITY_GROUP_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( IP_PERMISSION, "规则列表", false, "0px", TableDisplay.NONE, Type.LINK, false, false ));
   }
   
   public static final ArrayList<SearchResultFieldDesc> SECURITY_RULE_COMMON_FIELD_DESCS = Lists.newArrayList();
   static {
     SECURITY_RULE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("ID", true, "10%"));
     SECURITY_RULE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("安全组名称", true, "10%"));
     SECURITY_RULE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("源端口", true, "10%"));
     SECURITY_RULE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("目的端口", true, "10%"));
     SECURITY_RULE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("协议", true, "10%"));
     SECURITY_RULE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("IP段", true, "10%"));
   }
   
   public static final ArrayList<SearchResultFieldDesc> CTRL_COMMON_FIELD_DESCS = Lists.newArrayList();
   static {
     CTRL_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "名称", true, "10%") );
     CTRL_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "分区", true, "10%") );
     CTRL_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "地址", true, "10%") );
     CTRL_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "状态", true, "10%") );
   }
 
 	
   private static SearchQuery parseQuery( QueryType type, String query ) {
     SearchQuery sq = null;
     try {
       sq = QueryParser.get( ).parse( type.name( ), query );
     } catch ( QueryParsingException e ) {
     }
     return sq;
   }
 
   private static String getEndPoint() {
     if (CAN_FALLBACK)
       return FALLBACK_ENDPOINT;
     if (ENDPOINT != null)
       return ENDPOINT;
     else {
       // real code here
       return null;
     }
 
   }
   private void verifySession(Session session) throws EucalyptusServiceException {
     WebSession ws = WebSessionManager.getInstance().getSession(session.getId());
     if (ws == null) {
       throw new EucalyptusServiceException(EucalyptusServiceException.INVALID_SESSION);
     }
   }
 
   private LoginUserProfile getLoginUserProfile(Session session) throws EucalyptusServiceException {
     verifySession(session);
     return LoginUserProfileStorer.instance().get(session.getId());
   }
   
   private AmazonEC2 verify(Session session, int userID) throws EucalyptusServiceException {
     return getEC2(getKeys(session, userID));
   }
   
   private int getUserID(Session session) throws EucalyptusServiceException {
     return getLoginUserProfile(session).getUserId();
   }
   
   private String[] _getKeys(int userID) throws EucalyptusServiceException {
     String[] keys = null;
     //real code here
     
     if (keys == null) {
       if (CAN_FALLBACK) {
         keys = new String[2];
         keys[0] = FALLBACK_ACCESS_KEY;
         keys[1] = FALLBACK_SECRET_KEY;
       } else {
         throw new EucalyptusServiceException("key auth with eucalyptus failed");
       }
     }
     return keys;
   }
   
   private String[] getKeys(Session session, int userID) throws EucalyptusServiceException {
     verifySession(session);
     //TODO: check if session is admin, otherwise should not be able to use others' userID
     if (userID == 0)
       userID = getUserID(session);
     return _getKeys(userID);
   }
 
 	private AmazonEC2 getEC2(String[] keys) throws EucalyptusServiceException {
 		AWSCredentials credentials = new BasicAWSCredentials(keys[0], keys[1]);
 		ClientConfiguration cc = new ClientConfiguration();
 		AmazonEC2 ec2 = new AmazonEC2Client(credentials, cc);
 		ec2.setEndpoint(getEndPoint());
 		//System.out.println(ec2.describeAvailabilityZones());
 		return ec2;
 	}
 
 	@Override
 	public SearchResult lookupInstance(Session session, int userID, String search, SearchRange range)  throws EucalyptusServiceException{		
 		AmazonEC2 ec2 = verify(session, userID);
 		DescribeInstancesResult r = ec2.describeInstances();
 		//DescribeInstanceStatusResult rr = ec2.describeInstanceStatus();
 		List<SearchResultRow> data = new ArrayList<SearchResultRow>();
 		for (Reservation res : r.getReservations()) {
 			Instance ins = res.getInstances().get(0);
 			String owner = res.getOwnerId();
 			String id = ins.getInstanceId();
 			String image = ins.getImageId();
 			String priIp = ins.getPrivateIpAddress();
 			String pubIp = ins.getPublicIpAddress();
 			String state = ins.getState().getName();
 			String seGroup = res.getGroups().size() > 0 ? res.getGroups().get(0).getGroupName() : "";
 			String date = ins.getLaunchTime().toString();
 			String key = ins.getKeyName();
 			data.add(new SearchResultRow(Arrays.asList(id, image, pubIp, priIp, key, seGroup, owner, date, "", "", state)));
 		}
 		
 		int resultLength = data.size();
 		SearchResult result = new SearchResult(data.size(), range);
 		result.setDescs(INSTANCE_COMMON_FIELD_DESCS);
 		result.setRows(data.subList(range.getStart(), range.getStart() + resultLength));
 		return result;
 	}
 
 	@Override
 	public ArrayList<String> stopInstances(Session session, int userID, List<String> ids) throws EucalyptusServiceException {
 		AmazonEC2 ec2 = verify(session, userID);
 		ec2.stopInstances(new StopInstancesRequest(ids));
 		return null;
 	}
 
 	@Override
 	public ArrayList<String> startInstances(Session session, int userID, List<String> ids) throws EucalyptusServiceException {
 		AmazonEC2 ec2 = verify(session, userID);
 		ec2.startInstances(new StartInstancesRequest(ids));
 		return null;
 	}
 
 	@Override
 	public ArrayList<String> terminateInstances(Session session, int userID, List<String> ids) throws EucalyptusServiceException {
 		AmazonEC2 ec2 = verify(session, userID);
 		ec2.terminateInstances(new TerminateInstancesRequest(ids));
 		return null;
 	}
 
 	@Override
 	public SearchResult lookupImage(Session session, int userID, String search, SearchRange range) throws EucalyptusServiceException {
 		AmazonEC2 ec2 = verify(session, userID);
 		DescribeImagesResult r = ec2.describeImages();
 		List<SearchResultRow> data = new ArrayList<SearchResultRow>();
 		int total = 0;
 		for (Image i : r.getImages()) {
 			String id = i.getImageId();
 			String type = i.getImageType();
 			String arch = i.getArchitecture();
 			String loc = i.getImageLocation();
 			String kern = i.getKernelId();
 			String rd = i.getRamdiskId();
 			String state = i.getState();
 			String sysName = "";
 			String sysVer = "";
 			if (!TYPES.contains(search) || type.equals(search))
 		     if (type.equals(TYPE_ROOTFS)) {
 		        StringBuilder sb = new StringBuilder();
 		        sb.append("SELECT * FROM ");
 		        sb.append(DBTableName.VM_IMAGE_TYPE);
 		        sb.append(" WHERE ").append(DBTableColName.VM_IMAGE_TYPE.EUCA_VIT_ID);
 		        sb.append(" = '").append(id).append("'").append(" LIMIT 1");
 		          ResultSet rs;
               try {
                 rs = wrapper.query(sb.toString()).getResultSet();
                 rs.next();
                 if (!rs.getString(DBTableColName.VM_IMAGE_TYPE.DEL).equals("1")) {
                   sysName = rs.getString(DBTableColName.VM_IMAGE_TYPE.OS);
                   sysVer = rs.getString(DBTableColName.VM_IMAGE_TYPE.VER);
                 }
               } catch (SQLException e) {
                 //e.printStackTrace();
               }
 		      }
 		    data.add(new SearchResultRow(Arrays.asList(String.valueOf(++ total), sysName, sysVer, id, type, arch, loc, kern, rd, state)));
 		}
 		int resultLength = data.size();
 		SearchResult result = new SearchResult(data.size(), range);
 		result.setDescs(IMAGE_COMMON_FIELD_DESCS);
 		result.setRows(data.subList(range.getStart(), range.getStart() + resultLength));
 		return result;
 	}
 
   @Override
   public String runInstance(Session session, int userID, String image, String key) throws EucalyptusServiceException {
     //FIXME !@!@#*!#(!#*!@()* failed silently
     AmazonEC2 ec2 = verify(session, userID);
     RunInstancesRequest r = new RunInstancesRequest();
     System.out.println("!!image: " + image + " key:" + key);
     r.setImageId(image);
     r.setKeyName(key);
     r.setInstanceType(InstanceType.M1Small);
     r.setMinCount(1);
     r.setMaxCount(1);
     RunInstancesResult ret = ec2.runInstances(r);
     System.out.println("??" + ret.toString());
     return ret.getReservation().getInstances().get(0).getInstanceId();
   }
 
   List<SearchResultRow> lookupKeypair(Session session, int userID) throws EucalyptusServiceException {
     AmazonEC2 ec2 = verify(session, userID);
     DescribeKeyPairsResult r = ec2.describeKeyPairs();
     List<SearchResultRow> data = new ArrayList<SearchResultRow>();
     for (KeyPairInfo k : r.getKeyPairs()) {
       String name = k.getKeyName();
       String footprint = k.getKeyFingerprint();
       data.add(new SearchResultRow(Arrays.asList(name, footprint)));
     }
     return data;
   }
   
   @Override
   public SearchResult lookupKeypair(Session session, int userID, String search, SearchRange range) throws EucalyptusServiceException {
     List<SearchResultRow> data = lookupKeypair(session, userID);
     int resultLength = data.size();
     SearchResult result = new SearchResult(data.size(), range);
     result.setDescs(KEYPAIR_COMMON_FIELD_DESCS);
     result.setRows(data.subList(range.getStart(), range.getStart() + resultLength));
     return result;
   }
 
   @Override
   public String addKeypair(Session session, int userID, String name) throws EucalyptusServiceException {
     AmazonEC2 ec2 = verify(session, userID);
     CreateKeyPairRequest req = new CreateKeyPairRequest();
     req.setKeyName(name);
     CreateKeyPairResult ret = ec2.createKeyPair(req);
     return ret.getKeyPair().getKeyMaterial();
   }
 
   @Override
   public void importKeypair(Session session, int userID, String name, String key) throws EucalyptusServiceException{
     throw new EucalyptusServiceException("Not implemented yet");
   }
 
   @Override
   public void deleteKeypairs(Session session, int userID, List<String> keys) throws EucalyptusServiceException {
     AmazonEC2 ec2 = verify(session, userID);
     for (String k : keys) {
       DeleteKeyPairRequest req = new DeleteKeyPairRequest();
       req.setKeyName(k);
       ec2.deleteKeyPair(req);
     }
   }
 
   @Override
   public String createSecurityGroup(Session session, int userID, String name, String desc) throws EucalyptusServiceException {
     AmazonEC2 ec2 = verify(session, userID);
     CreateSecurityGroupRequest req = new CreateSecurityGroupRequest();
     req.setGroupName(name);
     req.setDescription(desc);
     CreateSecurityGroupResult ret = ec2.createSecurityGroup(req);
     //AuthorizeSecurityGroupEgressRequest req_ = new AuthorizeSecurityGroupEgressRequest();
     return ret.getGroupId();
   }
   
   List<SearchResultRow> lookupSecurityGroup(Session session, int userID) throws EucalyptusServiceException {
     AmazonEC2 ec2 = verify(session, userID);
     DescribeSecurityGroupsResult r = ec2.describeSecurityGroups();
     List<SearchResultRow> data = new ArrayList<SearchResultRow>();
     for (SecurityGroup g : r.getSecurityGroups()) {
       //String owner = g.getOwnerId();
       //String id = g.getGroupId();
       String name = g.getGroupName();
       String desc = g.getDescription();
       String url = QueryBuilder.get().start(QueryType.ipPermission).add(SEGROUP, name).url();
       //data.add(new SearchResultRow(Arrays.asList(owner, id, name, desc, url)));
       data.add(new SearchResultRow(Arrays.asList(name, desc, url)));      
     }
     return data;
   }
 
   @Override
   public SearchResult lookupSecurityGroup(Session session, int userID, String search, SearchRange range) throws EucalyptusServiceException {
     List<SearchResultRow> data = lookupSecurityGroup(session, userID);
     int resultLength = data.size();
     SearchResult result = new SearchResult(data.size(), range);
     result.setDescs(SECURITY_GROUP_COMMON_FIELD_DESCS);
     result.setRows(data.subList(range.getStart(), range.getStart() + resultLength));
     return result;
   }
 
   @Override
   public void deleteSecurityGroups(Session session, int userID, List<String> names) throws EucalyptusServiceException {
     AmazonEC2 ec2 = verify(session, userID);
     DeleteSecurityGroupRequest req = new DeleteSecurityGroupRequest();
     for (String s : names) {
       req.setGroupName(s);
       ec2.deleteSecurityGroup(req);
     }
   }
 
   @Override
   public SearchResult lookupSecurityRule(Session session, int userID, String search, SearchRange range) throws EucalyptusServiceException {
     SearchQuery q = parseQuery(QueryType.ipPermission, search);
     AmazonEC2 ec2 = verify(session, userID);
     DescribeSecurityGroupsRequest req = new DescribeSecurityGroupsRequest();
     if (q.hasOnlySingle(SEGROUP)) {
       String group = q.getSingle(SEGROUP).getValue();
       req.setGroupNames(Arrays.asList(group));     
     }
     DescribeSecurityGroupsResult r = ec2.describeSecurityGroups(req);
     List<SearchResultRow> data = new ArrayList<SearchResultRow>();
     int total = 0;
     for (SecurityGroup g : r.getSecurityGroups()) {
       String name = g.getGroupName();
       for (IpPermission i : g.getIpPermissions()) {
         String id = String.valueOf(++ total);
         String fromPort = i.getFromPort().toString();
         String proto = i.getIpProtocol();
         String toPort = i.getToPort().toString();
         List<String> _ipRange = i.getIpRanges();
         //FIXME should it be joined like this?
         String ipRange = StringUtils.join(_ipRange, ",");
         data.add(new SearchResultRow(Arrays.asList(id, name, fromPort, toPort, proto, ipRange)));
       }
     }
     int resultLength = data.size();
     SearchResult result = new SearchResult(data.size(), range);
     result.setDescs(SECURITY_RULE_COMMON_FIELD_DESCS);
     result.setRows(data.subList(range.getStart(), range.getStart() + resultLength));
     return result;
   }
 
   @Override
   public void addSecurityRule(Session session, int userID, String group, String fromPort, String toPort, String proto, String ipRange) throws EucalyptusServiceException {
     AmazonEC2 ec2 = verify(session, userID);
     AuthorizeSecurityGroupIngressRequest req = new AuthorizeSecurityGroupIngressRequest();
     req.setGroupName(group);
     req.setFromPort(Integer.parseInt(fromPort));
     req.setToPort(Integer.parseInt(toPort));
     req.setIpProtocol(proto);    
     req.setCidrIp(ipRange);
     ec2.authorizeSecurityGroupIngress(req);
   }
 
   @Override
   public void delSecurityRules(Session session, int userID, List<String> groups, List<String> fromPorts, List<String> toPorts, List<String> protos, List<String> ipRanges) throws EucalyptusServiceException {
     AmazonEC2 ec2 = verify(session, userID);
     RevokeSecurityGroupIngressRequest req = new RevokeSecurityGroupIngressRequest();
     int len = groups.size();
     for (int i = 0; i < len; ++ i) {
       req.setGroupName(groups.get(i));
       req.setFromPort(Integer.parseInt(fromPorts.get(i)));
       req.setToPort(Integer.parseInt(toPorts.get(i)));
       req.setIpProtocol(protos.get(i));    
       req.setCidrIp(ipRanges.get(i));
       ec2.revokeSecurityGroupIngress(req);
     }
   
   }
 
   @Override
   public void bindImage(Session session, int userID, String id, String sysName, String sysVer) throws EucalyptusServiceException {
     verify(session, userID);
     StringBuilder sb = new StringBuilder();
     String OS = DBTableColName.VM_IMAGE_TYPE.OS;
     String VER = DBTableColName.VM_IMAGE_TYPE.VER;
     String DEL = DBTableColName.VM_IMAGE_TYPE.DEL;
     String EUCA_ID = DBTableColName.VM_IMAGE_TYPE.EUCA_VIT_ID;
     sb.append("INSERT INTO ").append(DBTableName.VM_IMAGE_TYPE);
     sb.append("(").append(OS).append(",");
     sb.append(VER).append(",");
     sb.append(DEL).append(",");
     sb.append(EUCA_ID).append(")");
     sb.append(" VALUES (");
     sb.append("'").append(sysName).append("',");
     sb.append("'").append(sysVer).append("',");
     sb.append("'").append(0).append("',");
     sb.append("'").append(id).append("')");
     sb.append("ON DUPLICATE KEY UPDATE ");
     sb.append(OS).append("=VALUES(").append(OS).append("),");
     sb.append(OS).append("=VALUES(").append(DEL).append("),");
     sb.append(VER).append("=VALUES(").append(VER).append(")");
     try {
       wrapper.update(sb.toString());
     } catch (SQLException e) {
       LOG.error("bind image failed: " + e.toString());
       throw new EucalyptusServiceException(e.toString());
     }
   }
 
   @Override
   public void unbindImages(Session session, int userID, List<String> ids) throws EucalyptusServiceException {
     verify(session, userID);
     StringBuilder sb = new StringBuilder();
     /*
     sb.append("DELETE FROM ").append(DBTableName.VM_IMAGE_TYPE);
     */
     sb.append("UPDATE ").append(DBTableName.VM_IMAGE_TYPE);
     sb.append(" SET ").append(DBTableColName.VM_IMAGE_TYPE.DEL);
     sb.append("= '1'");
     sb.append(" WHERE ").append(DBTableColName.VM_IMAGE_TYPE.EUCA_VIT_ID);
     sb.append(" IN (");
     for (String id : ids) {
       sb.append("'").append(id).append("',");
     }
     sb.replace(sb.length() - 1, sb.length(), ")");
     try {
       wrapper.update(sb.toString());
     } catch (SQLException e) {
       LOG.error("unbind image failed: " + e.toString());
       throw new EucalyptusServiceException(e.toString());
     }
   }
   
   private String runWithKey(String[] keys, Collection<? extends String> cmd) {
     List<String> _cmd = new ArrayList<String>(cmd);
    _cmd.addAll(Arrays.asList("-A", keys[0], "-S", keys[1], "-U", getEndPoint()));
     return sshRun(_cmd);
   }
   
   private String run(List<String> cmd) {
     String ret = "";
     try {
       ProcessBuilder b = new ProcessBuilder(cmd);
       Process p = b.start();
       BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
       String s = reader.readLine();
       while (s != null) {
         ret = ret.concat(s + "\n");
         s = reader.readLine();
       }
       p.waitFor(); 
     } catch (IOException e) {
       e.printStackTrace();
     } catch (InterruptedException e) {
       e.printStackTrace();
     }
     return ret;
   }
 
   private String sshRun(Collection<? extends String> cmd) {
     List<String> _cmd = new ArrayList<String>();
     _cmd.addAll(Arrays.asList("ssh", FALLBACK_SSH_HOST, "-p", "22220", StringUtils.join(cmd, " ")));
     return run(_cmd);
   }
 
   @Override
   public SearchResult lookupNodeCtrl(Session session, String search, SearchRange range) throws EucalyptusServiceException {
     final List<String> cmd = Arrays.asList("euca-describe-nodes");
     String[] keys = getKeys(session, 0);
     String ret = runWithKey(keys, cmd);
     List<SearchResultRow> data = new ArrayList<SearchResultRow>();
     for (String s : ret.split("\n")) {
       if (s != null) {
         String[] i = s.split("\\s+");
         if (i.length < 3)
           break;
         data.add(new SearchResultRow(Arrays.asList("", i[2], i[1], "")));
       }
     }
     SearchResult result = new SearchResult(data.size(), range);
     result.setDescs(CTRL_COMMON_FIELD_DESCS);
     result.setRows(data.subList(range.getStart(), range.getStart() + data.size()));
     return result;
   }
   
   @Override
   public SearchResult lookupStorageCtrl(Session session, String search, SearchRange range) throws EucalyptusServiceException {
     final List<String> cmd = Arrays.asList("euca-describe-storage-controllers");
     String[] keys = getKeys(session, 0);
     String ret = runWithKey(keys, cmd);
     List<SearchResultRow> data = new ArrayList<SearchResultRow>();
     for (String s : ret.split("\n")) {
       if (s != null) {
         String[] i = s.split("\\s+");
         if (i.length < 5)
           break;
         data.add(new SearchResultRow(Arrays.asList(i[2], i[1], i[3], i[4])));
       }
     }
     SearchResult result = new SearchResult(data.size(), range);
     result.setDescs(CTRL_COMMON_FIELD_DESCS);
     result.setRows(data.subList(range.getStart(), range.getStart() + data.size()));
     return result;
   }
   
   @Override
   public SearchResult lookupClusterCtrl(Session session, String search, SearchRange range) throws EucalyptusServiceException {
     final List<String> cmd = Arrays.asList("euca-describe-clusters");
     String[] keys = getKeys(session, 0);
     String ret = runWithKey(keys, cmd);
     List<SearchResultRow> data = new ArrayList<SearchResultRow>();
     for (String s : ret.split("\n")) {
       if (s != null) {
         String[] i = s.split("\\s+");
         if (i.length < 5)
           break;
         data.add(new SearchResultRow(Arrays.asList(i[2], i[1], i[3], i[4])));
       }
         
     }
     SearchResult result = new SearchResult(data.size(), range);
     result.setDescs(CTRL_COMMON_FIELD_DESCS);
     result.setRows(data.subList(range.getStart(), range.getStart() + data.size()));
     return result;
   }
   
   @Override
   public SearchResult lookupWalrusCtrl(Session session, String search, SearchRange range) throws EucalyptusServiceException {
     final List<String> cmd = Arrays.asList("euca-describe-walruses");
     String[] keys = getKeys(session, 0);
     String ret = runWithKey(keys, cmd);
     List<SearchResultRow> data = new ArrayList<SearchResultRow>();
     for (String s : ret.split("\n")) {
       if (s != null) {
         String[] i = s.split("\\s+");
         if (i.length < 5)
           break;
         data.add(new SearchResultRow(Arrays.asList(i[2], i[1], i[3], i[4])));
       }
     }
     SearchResult result = new SearchResult(data.size(), range);
     result.setDescs(CTRL_COMMON_FIELD_DESCS);
     result.setRows(data.subList(range.getStart(), range.getStart() + data.size()));
     return result;
   }
 
   @Override
   public String uploadImage(Session session, int userID, String file, ImageType type, String bucket,
       String name, String kernel, String ramdisk) throws EucalyptusServiceException {
     throw new EucalyptusServiceException("not implemented yet");
     /*
     //won't need if run on server    
     final String[] cmd0 = {"scp", file, SSH_HOST + ":" + IMAGE_PATH};
     String ret = run(session, cmd0);
     System.out.println("ret: " + ret);
     
     file = file.replace("/tmp/", IMAGE_PATH);
     //rename
     String _file = IMAGE_PATH + name;
     String s = "mv " + file + " " + _file;
     final String[] cmd = {s};
     ret = sshRun(session, cmd);
     System.out.println("ret: " + ret);
     
     //gunzip if needed
     if (name.endsWith(".gz")) {
       final String[] _cmd = {"gunzip", _file};
       ret = sshRun(session, _cmd);
       System.out.println("ret: " + ret);
       _file = _file.substring(0, _file.length() - 3);
     }
     
     //bundle
     String[] cmd1 = null;
     switch (type) {
     case KERNEL:
       String[] k = {"euca-bundle-image", "-i", _file, "--kernel", "true"};
       cmd1 = k;
       break;
     case RAMDISK:
       String[] ra = {"euca-bundle-image", "-i", _file, "--ramdisk", "true"};
       cmd1 = ra;
       break;
     case ROOTFS:
       String[] ro = {"euca-bundle-image", "-i", _file, "--kernel", kernel, "--ramdisk", ramdisk};
       cmd1 = ro;
       break;
     }
     ret = sshRun(session, cmd1);
     System.out.println("ret: " + ret);
     String manifest = "";
     {     
       String[] tmp  = ret.split("\n");
       for (String t : tmp) {
         if (t.contains("manifest")) {
           manifest = t.split("\\s")[2];
         }
       }
     }
     System.out.println("manifest: " + manifest);
     
     //upload
     final String[] cmd2 = {"euca-upload-bundle", "-b", bucket, "-m", manifest};
     ret = sshRun(session, cmd2);
     System.out.println("ret: " + ret);
     
     //register
     File m = new File(manifest);
     final String[] cmd3 = {"euca-register", bucket + "/" + m.getName()};
     ret = sshRun(session, cmd3);
     System.out.println("ret: " + ret);
     {
       String[] tmp  = ret.split("\n");
       for (String t : tmp) {
         if (t.contains("IMAGE")) {
           ret = t.split("\\s")[1];
         }
       }    
     }
     return ret;
     */ 
   }
 
   @Override
   public String runInstance(Session session, int userID, String image, String keypair, String vmtype, String group) throws EucalyptusServiceException {
     String[] keys = getKeys(session, userID);
     List<String> cmd = new ArrayList<String>();
     cmd.add("euca-run-instances");
     if (keypair!= null && !keypair.equals("")) {
       cmd.add("-k");
       cmd.add(keypair);
     }
     if (vmtype != null && !vmtype.equals("")) {
       cmd.add("-t");
       cmd.add(vmtype);
     }
     if (group != null && !group.equals("")) {
       cmd.add("-g");
       cmd.add(group);
     }
     cmd.add(image);
     String ret = runWithKey(keys, cmd);
     System.out.println("ret: " + ret);
     {
       String[] tmp  = ret.split("\n");
       for (String t : tmp) {
         if (t.contains("INSTANCE")) {
           ret = t.split("\\s")[1];
         }
       }    
     }
     return ret;
   }
 
   @Override
   public void registerCluster(Session session, String part, String host, String name) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca-register-cluster", "--partition", part, "--host", host, "--component", name);
     String[] keys = getKeys(session, 0);
     String ret = runWithKey(keys, cmd);
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
     
   }
 
   @Override
   public void deregisterCluster(Session session, String part, String name) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca-deregister-cluster", "--partition", part, "--component", name);
     String[] keys = getKeys(session, 0);
     String ret = runWithKey(keys, cmd);
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
   }
 
 
   @Override
   public void registerStorage(Session session, String part, String host, String name) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca-register-storage-controller", "--partition", part, "--host", host, "--component", name);
     String[] keys = getKeys(session, 0);
     String ret = runWithKey(keys, cmd);
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
   }
 
   @Override
   public void deregisterStorage(Session session, String part, String name) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca-deregister-storage-controller", "--partition", part, "--component", name);
     String[] keys = getKeys(session, 0);
     String ret = runWithKey(keys, cmd);
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
     
   }
 
   @Override
   public void registerWalrus(Session session, String host, String name) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca-register-walrus", "--partition", "walrus", "--host", host, "--component", name);
     String[] keys = getKeys(session, 0);
     String ret = runWithKey(keys, cmd);
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
   }
 
   @Override
   public void deregisterWalrus(Session session, String name) throws EucalyptusServiceException {
     List<String> cmd = Arrays.asList("euca-deregister-walrus", "--partition", "walrus", "--component", name);
     String[] keys = getKeys(session, 0);
     String ret = runWithKey(keys, cmd);
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
     
   }
 
   @Override
   public void registerNode(Session session, String host) throws EucalyptusServiceException {
     throw new EucalyptusServiceException("not implemented yet");
     /*
     List<String> cmd = Arrays.asList("euca_conf", "--register-nodes", host);
     String ret = sshRun(session, cmd.toArray(new String[0]));
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
     */
   }
 
   @Override
   public void deregisterNode(Session session, String host) throws EucalyptusServiceException {
     throw new EucalyptusServiceException("not implemented yet");
     /*
     List<String> cmd = Arrays.asList("euca_conf", "--deregister-nodes", host);
     String ret = sshRun(session, cmd.toArray(new String[0]));
     if (ret.contains("error:")) {
       throw new EucalyptusServiceException();
     }
     */
   }
 }
