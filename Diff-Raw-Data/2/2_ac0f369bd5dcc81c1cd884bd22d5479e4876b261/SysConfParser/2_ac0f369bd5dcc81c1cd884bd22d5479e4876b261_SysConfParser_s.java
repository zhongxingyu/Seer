 package com.eucalyptus.webui.server.config;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.dom4j.io.SAXReader;
 
 import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
 import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
 import com.eucalyptus.webui.server.QuickLinks;
 import com.eucalyptus.webui.shared.config.EnumLanguage;
 import com.eucalyptus.webui.shared.config.LanguageSelection;
 import com.eucalyptus.webui.shared.config.SysConfig;
import com.google.gwt.thirdparty.guava.common.base.Strings;
 
 public class SysConfParser {
 
 	public SysConfParser() {
 	}
 	
 	public void parse(String filePath) {
 		
 		SAXReader saxReader = new SAXReader();
 		
 		try {
 			Document document = saxReader.read(ClassLoader.getSystemResource(filePath));
 			
 			readElements(document);
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			SysConfParser.LOG.log(Level.ERROR, "SysConfParser parsing exception");
 			e.printStackTrace();
 		}
 	}
 	
 	public SysConfig getSysConfig() {
 		return this.sysConfig;
 	}
 	
 	private void readElements(Document document) {
 		Element root = document.getRootElement();
 		List<?> list = root.elements();
 		Iterator<?> iter = list.iterator();
 		while (iter.hasNext()) {
 			Element parent = (Element) iter.next();
 			String tag = parent.getName();
 			
 			if (tag.equalsIgnoreCase(SysConfParser.TAG_LAN))
 				parseLanguageConf(parent);
 			else if (tag.equalsIgnoreCase(TAG_LINKS))
 				parseLinks(parent);
 			else if (tag.equalsIgnoreCase(TAG_LINKCONFIG))
 				parseLinkConfig(parent);
 			else if (tag.equalsIgnoreCase(TAG_TABLECOL))
 				parseViewTableContentConfig(parent);
 			else if (tag.equalsIgnoreCase(SysConfParser.TAG_TABLESIZE))
 				parseViewTableSizeConfig(parent);			
 		}
 	}
 	
 	private void parseLanguageConf(Element node) {
 		List<?> childs = node.elements();
 		
 		Iterator<?> childIter = childs.iterator();
 		
 		while (childIter.hasNext()) {
 			Element child = (Element) childIter.next();
 			System.out.println(child.getName());
 			if (child.getName().equalsIgnoreCase(TAG_LAN_INDEX)) {
 				Object value = child.getData();
 				
 				if (value != null) {
 					EnumLanguage lan = EnumLanguage.valueOf(value.toString());
 					this.sysConfig.setLanguage(lan);
 					LanguageSelection.instance().setCurLanguage(lan);
 				}
 			}
 		}
 	}
 	
 	private void parseLinks(Element node) {
 		List<?> childs = node.elements();
 		
 		Iterator<?> childIter = childs.iterator();
 		
 		while (childIter.hasNext()) {
 			Element child = (Element) childIter.next();
 			
 			if (child.getName().equalsIgnoreCase(TAG_LINKS_LINK)) {
 				
 				List<?> eles = child.elements();
 				
 				Iterator<?> propIter = eles.iterator();
 				String linkName = null;
 				String linkDesc = null;
 				String image = null;
 				String queryType = null;
 				
 				while (propIter.hasNext()) {
 					Element ele = (Element) propIter.next();
 					String name = ele.getName();
 					String data = ele.getData().toString();
 					
 					if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(data))
 						continue;
 					
 					if (name.equalsIgnoreCase(TAG_LINKS_LINK_NAME)) {
 						linkName = data;
 					}
 					else if (name.equalsIgnoreCase(TAG_LINKS_LINK_DESC)) {
 						linkDesc = data;
 					}
 					else if (name.equalsIgnoreCase(TAG_LINKS_LINK_IMAGE)) {
 						image = data;
 					}
 					else if (name.equalsIgnoreCase(TAG_LINKS_LINK_QUERYTYPE)) {
 						queryType = data;
 					}
 				}
 				
 				if (!Strings.isNullOrEmpty(linkName) 
 						&& !Strings.isNullOrEmpty(linkDesc)
 						&& !Strings.isNullOrEmpty(image)
 						&& !Strings.isNullOrEmpty(queryType))
 					QuickLinks.addLink(linkName, linkDesc, image, queryType);
 			}
 		}
 	}
 	
 	private void parseLinkConfig(Element node) {
 		List<?> childs = node.elements();
 		
 		Iterator<?> childIter = childs.iterator();
 		
 		while (childIter.hasNext()) {
 			Element child = (Element) childIter.next();
 			
 			if (child.getName().equalsIgnoreCase(TAG_LINKCONFIG_GROUP)) {
 				
 				String userType = child.attributeValue(TAG_LINKCONFIG_GROUP_PROP_USERTYPE);
 				
 				List<?> grandchilds = child.elements();
 				
 				Iterator<?> grandchildIter = grandchilds.iterator();
 				
 				String tag = null;
 				ArrayList<String> links = null;
 				
 				while (grandchildIter.hasNext()) {
 					Element grandchild = (Element) grandchildIter.next();
 					
 					if (grandchild.getName().equalsIgnoreCase(TAG_LINKCONFIG_GROUP_TAG)) {
 						
 						tag = grandchild.attributeValue(TAG_LINKCONFIG_GROUP_TAG_PROP_NAME);
 						
 						List<?> eles = grandchild.elements();
 						
 						Iterator<?> propIter = eles.iterator();
 						
 						links = new ArrayList<String>();
 						
 						while (propIter.hasNext()) {
 							Element ele = (Element) propIter.next();
 							
 							String name = ele.getName();
 							String data = ele.getData().toString();
 							
 							if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(data))
 								continue;
 							
 							if (name.equalsIgnoreCase(TAG_LINKCONFIG_GROUP_TAG_LINK)) {
 								links.add(data);
 							}
 						}
 						
 						if (!Strings.isNullOrEmpty(userType) 
 								&& !Strings.isNullOrEmpty(tag)
 								&& links.size() > 0) {
 							if (userType.equalsIgnoreCase(TAG_LINKCONFIG_GROUP_PROP_USERTYPE_SYSADMIN))
 								QuickLinks.addSysAdminLinkGroup(tag, links);
 							else if (userType.equalsIgnoreCase(TAG_LINKCONFIG_GROUP_PROP_USERTYPE_ACCOUNTADMIN))
 								QuickLinks.addAccountAdminLinkGroup(tag, links);
 							else if (userType.equalsIgnoreCase(TAG_LINKCONFIG_GROUP_PROP_USERTYPE_USER))
 								QuickLinks.addUserLinkGroup(tag, links);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void parseViewTableContentConfig(Element node) {
 		
 		Iterator<?> childIter = node.elements().iterator();
 		
 		String srvName = null;
 		
 		while (childIter.hasNext()) {
 			Element grandchild = (Element) childIter.next();
 			
 			if (!grandchild.getName().equalsIgnoreCase(TAG_TABLECOL_SRV))
 				continue;
 			
 			ArrayList<SearchTableCol> tableCol = new ArrayList<SearchTableCol>();
 		
 			srvName = grandchild.attributeValue(TAG_TABLECOL_SRV_PROP_NAME.toLowerCase());
 			
 			Iterator<?> grandchildIter = grandchild.elements().iterator();
 			
 			while (grandchildIter.hasNext()) {
 				
 				Element grandgrandchild = (Element) grandchildIter.next();
 			
 				if (!grandgrandchild.getName().equalsIgnoreCase(TAG_TABLECOL_SRV_COL)) 
 					continue;
 				
 				Iterator<?> elesIter = grandgrandchild.elements().iterator();
 			
 				String[] title = new String[EnumLanguage.values().length];
 				String width = null;
 				String display = null;
 				String text = null;
 				String dbField = null;
 				
 				boolean sortable = false;
 				boolean editable = false;
 				boolean hidden = false;
 				boolean selected = false;
 				
 				while (elesIter.hasNext()) {
 					Element ele = (Element) elesIter.next();
 					String name = ele.getName();
 					String data = ele.getData().toString();
 				
 					if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(data))
 						continue;
 					
 					if (name.equalsIgnoreCase(TAG_TABLECOL_SRV_COL_TITLE)) {
 						String languageValue = ele.attributeValue(TAG_TABLECOL_SRV_COL_TITLE_PROP_LANGUAGE);
 						EnumLanguage lan = EnumLanguage.valueOf(languageValue);
 						title[lan.ordinal()] = data.toString();						
 					}
 					
 					else if (name.equalsIgnoreCase(TAG_TABLECOL_SRV_COL_WIDTH)) {
 						width = data;
 					}
 					else if (name.equalsIgnoreCase(TAG_TABLECOL_SRV_COL_DISPLAY)) {
 						display = data;
 					}
 					else if (name.equalsIgnoreCase(TAG_TABLECOL_SRV_COL_TEXT)) {
 						text = data;
 					}
 					else if (name.equalsIgnoreCase(TAG_TABLECOL_SRV_COL_SORTABLE)) {
 						sortable = Boolean.parseBoolean(data);
 					}
 					else if (name.equalsIgnoreCase(TAG_TABLECOL_SRV_COL_EDITABLE)) {
 						editable = Boolean.parseBoolean(data);
 					}
 					else if (name.equalsIgnoreCase(TAG_TABLECOL_SRV_COL_HIDDEN)) {
 						hidden = Boolean.parseBoolean(data);
 					}
 					else if (name.equalsIgnoreCase(TAG_TABLECOL_SRV_COL_SELECTED)) {
 						selected = Boolean.parseBoolean(data);
 					}
 					else if (name.equalsIgnoreCase(TAG_TABLECOL_SRV_COL_DBFIELD)) {
 						dbField = data;
 					}
 					
 				}
 
 				if (!Strings.isNullOrEmpty(srvName)
 						&& !Strings.isNullOrEmpty(width)
 						&& !Strings.isNullOrEmpty(display)
 						&& !Strings.isNullOrEmpty(text)
 						&& !Strings.isNullOrEmpty(dbField)) {
 					SearchTableCol col = new SearchTableCol(title, width, TableDisplay.valueOf(display), Type.valueOf(text), dbField, 
 															sortable, editable, hidden, selected);
 					tableCol.add(col);
 				}
 			}
 			
 			if (!Strings.isNullOrEmpty(srvName) && tableCol.size()>0)
 				ViewSearchTableColConfig.instance().setConfig(srvName, tableCol);
 		}
 	}
 	
 	private void parseViewTableSizeConfig(Element node) {
 		List<?> childs = node.elements();
 		
 		Iterator<?> childIter = childs.iterator();
 		
 		while (childIter.hasNext()) {
 			Element child = (Element) childIter.next();
 			
 			if (child.getName().equalsIgnoreCase(TAG_TABLESIZE_VIEW)) {
 				
 				List<?> eles = child.elements();
 				
 				Iterator<?> propIter = eles.iterator();
 				String viewName = null;
 				String tableSize = null;
 				
 				while (propIter.hasNext()) {
 					Element ele = (Element) propIter.next();
 					String name = ele.getName();
 					String data = ele.getData().toString();
 					
 					if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(data))
 						continue;
 					
 					if (name.equalsIgnoreCase(TAG_TABLESIZE_VIEW_ACTIVITYNAME)) {
 						viewName = data;
 					}
 					else if (name.equalsIgnoreCase(TAG_TABLESIZE_VIEW_SIZE)) {
 						tableSize = data;
 					}
 				}
 				
 				if (!Strings.isNullOrEmpty(viewName) && !Strings.isNullOrEmpty(tableSize))
 					this.sysConfig.getViewTableSizeConfig().put(viewName.toLowerCase(), tableSize);
 			}
 		}
 	}
 	
 	private static String TAG_LAN = "language";
 	private static String TAG_LAN_INDEX = "index";
 
 	private static String TAG_TABLECOL = "searchtable_content";
 	private static String TAG_TABLECOL_SRV = "service";
 	private static String TAG_TABLECOL_SRV_PROP_NAME = "name";
 	private static String TAG_TABLECOL_SRV_COL = "col";
 	private static String TAG_TABLECOL_SRV_COL_TITLE = "title";
 	private static String TAG_TABLECOL_SRV_COL_TITLE_PROP_LANGUAGE = "language";
 	private static String TAG_TABLECOL_SRV_COL_SORTABLE = "sortable";
 	private static String TAG_TABLECOL_SRV_COL_WIDTH = "width";
 	private static String TAG_TABLECOL_SRV_COL_DISPLAY = "display";
 	private static String TAG_TABLECOL_SRV_COL_TEXT = "text";
 	private static String TAG_TABLECOL_SRV_COL_EDITABLE = "editable";
 	private static String TAG_TABLECOL_SRV_COL_HIDDEN = "hidden";
 	private static String TAG_TABLECOL_SRV_COL_SELECTED = "selected";
 	private static String TAG_TABLECOL_SRV_COL_DBFIELD = "db_field";
 	
 	private static String TAG_TABLESIZE = "searchtable_size";
 	private static String TAG_TABLESIZE_VIEW = "view";
 	private static String TAG_TABLESIZE_VIEW_ACTIVITYNAME = "activity_name";
 	private static String TAG_TABLESIZE_VIEW_SIZE = "size";
 	
 	private static String TAG_LINKS = "links";
 	private static String TAG_LINKS_LINK = "link";
 	private static String TAG_LINKS_LINK_NAME = "name";
 	private static String TAG_LINKS_LINK_DESC = "desc";
 	private static String TAG_LINKS_LINK_IMAGE = "image";
 	private static String TAG_LINKS_LINK_QUERYTYPE = "query_type_enum";
 	
 	private static String TAG_LINKCONFIG = "link_config";
 	private static String TAG_LINKCONFIG_GROUP = "group";
 	private static String TAG_LINKCONFIG_GROUP_PROP_USERTYPE = "user_type";
 	private static String TAG_LINKCONFIG_GROUP_PROP_USERTYPE_SYSADMIN = "system_admin";
 	private static String TAG_LINKCONFIG_GROUP_PROP_USERTYPE_ACCOUNTADMIN = "account_admin";
 	private static String TAG_LINKCONFIG_GROUP_PROP_USERTYPE_USER = "user";
 	private static String TAG_LINKCONFIG_GROUP_TAG = "tag";
 	private static String TAG_LINKCONFIG_GROUP_TAG_PROP_NAME = "name";
 	private static String TAG_LINKCONFIG_GROUP_TAG_LINK = "link";
 	
 	private SysConfig sysConfig = new SysConfig();
 	
 	private static final Logger LOG = Logger.getLogger( SysConfParser.class.getName( ) );
 }
