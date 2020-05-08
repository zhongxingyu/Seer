 package com.eucalyptus.webui.server;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.List;
 
 import com.eucalyptus.webui.client.service.QuickLink;
 import com.eucalyptus.webui.client.service.QuickLinkTag;
 import com.eucalyptus.webui.client.service.EucalyptusServiceException;
 import com.eucalyptus.webui.shared.query.QueryType;
 import com.eucalyptus.webui.shared.user.EnumUserType;
 
 public class QuickLinks {
 	
 	public static ArrayList<QuickLinkTag> getTags(boolean isSysAdmin, EnumUserType userType) throws EucalyptusServiceException {
 		if (isSysAdmin)
 			return getSystemAdminTags();
 		else if (userType == EnumUserType.ADMIN) {
 			return getAccountAdminTags();
 		}
 		else {
 			return getUserTags();
 		}
 	}
 	
 	public static void addLink(String linkName, String linkDesc, String image, String queryType) {
 		QueryType type = QueryType.valueOf(queryType);
 		
 		QuickLink link = new QuickLink(linkName, linkDesc, image, QueryBuilder.get().start(type).query());
 		links.put(linkName, link);
 	}
 	
 	public static void addSysAdminLinkGroup(String tag, ArrayList<String> links) {
 		LinkGroup linkGroup = new LinkGroup(tag, links);
 		sysAdminLinkGroups.add(linkGroup);
 	}
 	
 	public static void addAccountAdminLinkGroup(String tag, ArrayList<String> links) {
 		LinkGroup linkGroup = new LinkGroup(tag, links);
 		accountAdminLinkGroups.add(linkGroup);
 	}
 	
 	public static void addUserLinkGroup(String tag, ArrayList<String> links) {
 		LinkGroup linkGroup = new LinkGroup(tag, links);
 		userLinkGroups.add(linkGroup);
 	}
 	
 	private static ArrayList<QuickLinkTag> getSystemAdminTags() throws EucalyptusServiceException {
 		ArrayList<QuickLinkTag> quickLinkTag = new ArrayList<QuickLinkTag>();
 		for (LinkGroup linkGroup : sysAdminLinkGroups) {
 			quickLinkTag.add(getQuickLinkTag(linkGroup.getTag(), linkGroup.getLinks()));
 		}
 		return quickLinkTag;
 	}
 	
 	private static ArrayList<QuickLinkTag> getAccountAdminTags() throws EucalyptusServiceException {
 		ArrayList<QuickLinkTag> quickLinkTag = new ArrayList<QuickLinkTag>();
 		
 		for (LinkGroup linkGroup : accountAdminLinkGroups) {
 			quickLinkTag.add(getQuickLinkTag(linkGroup.getTag(), linkGroup.getLinks()));
 		}
 		
 		return quickLinkTag;
 	}
 	
 	private static ArrayList<QuickLinkTag> getUserTags() throws EucalyptusServiceException {
 		ArrayList<QuickLinkTag> quickLinkTag = new ArrayList<QuickLinkTag>();
 		
 		for (LinkGroup linkGroup : userLinkGroups) {
 			quickLinkTag.add(getQuickLinkTag(linkGroup.getTag(), linkGroup.getLinks()));
 		}
 		
 		return quickLinkTag;
 	}
 	
 	private static QuickLinkTag getQuickLinkTag(String tagName, List<String> linkNames) {
 		ArrayList<QuickLink> linkList = new ArrayList<QuickLink>();
 		for (String name : linkNames) {
 			QuickLink link = links.get(name);
 			if (link != null)
 				linkList.add(link);
 		}
 		return new QuickLinkTag(tagName, linkList);
 	}
 	
 	private static Hashtable<String, QuickLink> links = new Hashtable<String, QuickLink>();
 	
 	private static ArrayList<LinkGroup> sysAdminLinkGroups = new ArrayList<LinkGroup>();
 	private static ArrayList<LinkGroup> accountAdminLinkGroups = new ArrayList<LinkGroup>();
 	private static ArrayList<LinkGroup> userLinkGroups = new ArrayList<LinkGroup>();
 }
 
 class LinkGroup {
 	String tag;
 	ArrayList<String> links;
 	
 	LinkGroup(String tag, ArrayList<String> links) {
 		this.tag = tag;
 		this.links = links;
 	}
 	
 	public void setTag(String tag) {
 		this.tag = tag;
 	}
 	public String getTag() {
 		return this.tag;
 	}
 	
 	public void setLinks(ArrayList<String> links) {
 		this.links = links;
 	}
 	public ArrayList<String> getLinks() {
 		return this.links;
 	}
 }
