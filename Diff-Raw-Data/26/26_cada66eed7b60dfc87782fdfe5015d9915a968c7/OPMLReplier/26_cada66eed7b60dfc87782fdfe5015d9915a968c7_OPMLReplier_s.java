 /*
  *
  */
 
 package com.luntsys.luntbuild.repliers;
 
 import com.luntsys.luntbuild.builders.Builder;
 import com.luntsys.luntbuild.db.*;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.utility.NotifierProperty;
 import com.luntsys.luntbuild.vcs.Vcs;
 
 import java.util.Iterator;
 
 /**
  * OPML API replier implementation.
  *
  * @author Jason Archer
  */
 public class OPMLReplier extends Replier {
 
 	private String apiURL = "";
 
     /**
      * Constructs a new OPML replier.
      */
     public OPMLReplier() {
     }
 
 	/**
      * Gets the display name for this replier.
      *
      * @return the display name for this replier
 	 */
 	public String getDisplayName() {
 		return "OPML";
 	}
 
 	/**
 	 * Gets the full reply based on the chosen method.
 	 * 
 	 * @return the full reply message
 	 */
 	public String getReply() {
 		String reply = "";
 		String header = "";
 		String body = "";
 		String footer = "";
 
		String servletURL = (String) Luntbuild.getProperties().get(Constants.SERVLET_URL);
		if (servletURL == null) {
			return "";
		}
 		apiURL = servletURL.replaceAll("app.do","api/rss");
 
 		// Construct feed
 		header += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
 		header += "<?xml-stylesheet type=\"text/xsl\" href=\"/luntbuild/web/xslt/opml.xsl\" version=\"1.0\"?>";
 		header += "<opml>";
 		header += "<generator>" + servletURL + "</generator>";
 		header += "<head><title>RSS Feeds from Luntbuild@" + Luntbuild.getHostName() + "</title></head>";
 		body += "<body>";
 		body += "<outline type=\"rss\" title=\"All Schedules\" description=\"Build Listing - one stop shopping\" ";
 		body += "xmlUrl=\"" + escape(apiURL + "/schedules") + "\" htmlUrl=\"" + escape(servletURL + "?service=direct/1/Home/tabs.$DirectLink&sp=Sbuilds") + "\" />";
 		body += "<outline type=\"rss\" title=\"All Users\" description=\"Build Listing - one stop shopping\" ";
 		body += "xmlUrl=\"" + escape(apiURL + "/users") + "\" htmlUrl=\"" + escape(servletURL + "?service=direct/1/Home/tabs.$DirectLink&sp=Susers") + "\" />";
 
 		// Get all schedules
 		Iterator schedules = Luntbuild.getDao().loadSchedules().iterator();
 		while (schedules.hasNext()) {
 			Schedule schedule = (Schedule) schedules.next();
 			body += getSchedule(schedule);
 		}
 		body += "</body>";
 		footer += "</opml>";
 
 		reply = header + body + footer;
 
 		return reply;
 	}
 
 	/**
 	 * Escapes special characters in the XML.
 	 * 
 	 * @param text the XML
 	 * @return the properly escaped XML
 	 */
 	protected String escape(String text) {
 		text = text.replaceAll("&","&amp;");
 		text = text.replaceAll("<","&lt;");
 		text = text.replaceAll(">","&gt;");
 
 		// Return escaped text
 		return text;
 	}
 
 	/**
 	 * Gets the "system" object containing system wide settings and users.
 	 * 
 	 * @return the system object
 	 */
 	public String getSystem() {
 		String OutlineItem = "";
 		// TODO Auto-generated method stub
 
 		// Return Outline Item
 		return OutlineItem;
 	}
 
 	/**
 	 * Gets the property object of the specified system level property.
 	 * 
 	 * @param property the property
 	 * @return the property object
 	 */
 	public String getNotifierProperty(NotifierProperty property) {
 		String OutlineItem = "";
 		// TODO Auto-generated method stub
 
 		// Return Outline Item
 		return OutlineItem;
 	}
 
 	/**
 	 * Gets the property object of the specified user level property.
 	 * 
 	 * @param property the property
 	 * @param user the user
 	 * @return the property object
 	 */
 	public String getNotifierProperty(NotifierProperty property, User user) {
 		String OutlineItem = "";
 		// TODO Auto-generated method stub
 
 		// Return Outline Item
 		return OutlineItem;
 	}
 
 	/**
 	 * Gets the user object of the specified user.
 	 * 
 	 * @param user the user
 	 * @return the user object
 	 */
 	public String getUser(User user) {
 		String OutlineItem = "";
 		// TODO Auto-generated method stub
 
 		// Return Outline Item
 		return OutlineItem;
 	}
 
 	/**
 	 * Gets the project object of the specified project.
 	 * 
 	 * @param project the project
 	 * @return the project object
 	 */
 	public String getProject(Project project) {
 		String OutlineItem = "";
 		// TODO Auto-generated method stub
 
 		// Return Outline Item
 		return OutlineItem;
 	}
 
 	/**
 	 * Gets the vcs object of the specified vcs.
 	 * 
 	 * @param vcs the vcs
 	 * @return the vcs object
 	 */
 	public String getVcs(Vcs vcs) {
 		String OutlineItem = "";
 		// TODO Auto-generated method stub
 
 		// Return Outline Item
 		return OutlineItem;
 	}
 
 	/**
 	 * Gets the builder object of the specified builder.
 	 * 
 	 * @param builder the builder
 	 * @return the builder object
 	 */
 	public String getBuilder(Builder builder) {
 		String OutlineItem = "";
 		// TODO Auto-generated method stub
 
 		// Return Outline Item
 		return OutlineItem;
 	}
 
 	/**
 	 * Gets the schedule object of the specified schedule.
 	 * 
 	 * @param schedule the schedule
 	 * @return the schedule object
 	 */
 	public String getSchedule(Schedule schedule) {
 		String OutlineItem = "";
 		OutlineItem += "<outline type=\"rss\" title=\"" + schedule.getProject().getName() + "/" + schedule.getName() + " Builds\" ";
 		OutlineItem += "description=\"Build Listing - one stop shopping\" ";
 		OutlineItem += "xmlUrl=\"" + escape(apiURL + "/builds/" + schedule.getProject().getName() + "/" + schedule.getName()) + "\" ";
 		OutlineItem += "htmlUrl=\"" + escape(schedule.getUrl()) + "\" />";
 
 		// Return Outline Item
 		return OutlineItem;
 	}
 
 	/**
 	 * Gets the build object of the specified build.
 	 * 
 	 * @param build the build
 	 * @return the build object
 	 */
 	public String getBuild(Build build) {
 		String OutlineItem = "";
 		// TODO Auto-generated method stub
 
 		// Return Outline Item
 		return OutlineItem;
 	}
 }
