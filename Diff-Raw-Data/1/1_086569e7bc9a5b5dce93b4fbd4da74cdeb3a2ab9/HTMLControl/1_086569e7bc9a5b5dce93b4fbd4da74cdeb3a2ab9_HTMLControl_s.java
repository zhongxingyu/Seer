 package cmg.org.monitor.util.shared;
 
 import cmg.org.monitor.entity.shared.SystemMonitor;
 
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.ui.HTML;
 
 public class HTMLControl {
 
 	public static final String NOTIFY_OPTION = "Notify Options";
 
 	public static final String LOGIN_SERVLET_NAME = "login";
 
 	public static final String HTML_INDEX_NAME = "Index.html";
 
 	public static final String HTML_DASHBOARD_NAME = "#dashboard";
 	public static final String HTML_SYSTEM_DETAIL_NAME = "#dashboard/system/detail";
 	public static final String HTML_SYSTEM_STATISTIC_NAME = "#dashboard/system/statistic";
 	public static final String HTML_ADD_NEW_SYSTEM_NAME = "#management/system/add";
 	public static final String HTML_SYSTEM_MANAGEMENT_NAME = "#management/system";
 	public static final String HTML_USER_MANAGEMENT_NAME = "#management/user";
 	public static final String HTML_ABOUT_NAME = "#about";
 	public static final String HTML_HELP_NAME = "#help";
 	public static final String HTML_EDIT_NAME = "#management/system/edit";
 	public static final String HTML_DELETE_SYSTEM_NAME = "#management/system/delete";
 
 	public static final String ID_STEP_HOLDER = "step-holder";
 	public static final String ID_PAGE_HEADING = "page-heading";
 	public static final String ID_BODY_CONTENT = "body-content";
 	public static final String ID_STATUS_MESSAGE = "statusMes";
 	public static final String ID_LOGIN_FORM = "nav-right";
 	public static final String ID_MENU = "menuContent";
 	public static final String ID_MESSAGE_RED = "message-red";
 	public static final String ID_MESSAGE_BLUE = "message-blue";
 	public static final String ID_MESSAGE_GREEN = "message-green";
 	public static final String ID_MESSAGE_YELLOW = "message-yellow";
 	public static final String ID_VERSION = "version";
 
 	public static final int VIEW_DETAILS = 0x001;
 	public static final int VIEW_STATISTIC = 0x002;
 
 	public static final int GREEN_MESSAGE = 0x001;
 	public static final int BLUE_MESSAGE = 0x002;
 	public static final int RED_MESSAGE = 0x003;
 	public static final int YELLOW_MESSAGE = 0x004;
 
 	public static final int PAGE_DASHBOARD = 0x001;
 	public static final int PAGE_SYSTEM_MANAGEMENT = 0x002;
 	public static final int PAGE_USER_MANAGEMENT = 0x003;
 	public static final int PAGE_HELP = 0x004;
 	public static final int PAGE_ABOUT = 0x005;
 	public static final int PAGE_SYSTEM_STATISTIC = 0x006;
 	public static final int PAGE_SYSTEM_DETAIL = 0x007;
 	public static final int PAGE_ADD_SYSTEM = 0x008;
 	public static final int PAGE_DELETE_SYSTEM = 0x009;
 	public static final int PAGE_EDIT_SYSTEM = 0x010;
 
 	public static final int ERROR_NORMAL = 0x000;
 	public static final int ERROR_SYSTEM_ID = 0x001;
 
 	public static final String HTML_ACTIVE_IMAGE = "<img src=\"images/icon/active.gif\" width=\"42\" height=\"20\" "
 			+ " style=\"display: block; margin-left: auto; margin-right: auto\"/>";
 
 	public static final String HTML_ARROW_IMAGE = "<img src=\"images/icon/right_arrow.png\" />";
 
 	public static String getButtonHtml(String sid, boolean type) {
 		StringBuffer tmp = new StringBuffer();
 		tmp.append("<a href=\""
 				+ (type ? HTML_SYSTEM_DETAIL_NAME : HTML_SYSTEM_STATISTIC_NAME)
 				+ "/" + sid + "\">");
 		tmp.append("<input type=\"button\" class=\""
 				+ (type ? "form-details" : "form-statistic") + "\">");
 		tmp.append("</a>");
 		return tmp.toString();
 	}
 
 	public static String getAboutContent() {
 		StringBuffer tmp = new StringBuffer();
 		// tmp.append("<img src=\"images/logo/c-mg_logo.png\" width=\"210\" height=\"80\" style='margin:10px'>");
 		tmp.append("<h3 style='font-size:24px'>" + MonitorConstant.PROJECT_NAME
 				+ "</h3>");
 		tmp.append("<h3>Version " + MonitorConstant.VERSION + "</h3>");
 		tmp.append("<h3>Released on: " + MonitorConstant.RELEASED_ON + "</h3>");
 		tmp.append("<h3>Support contact: ");
 		tmp.append("<a href='mailto:monitor@c-mg.com'>monitor@c-mg.com</a>");
 		tmp.append(" / <a href='mailto:monitor@c-mg.vn'>monitor@c-mg.vn</a></h3>");
 		tmp.append("<h3>Find out more about us: <a href=\"http://www.c-mg.com\">www.c-mg.com</a></h3>");
 		return tmp.toString();
 	}
 
 	public static String getHelpContent() {
 		StringBuffer tmp = new StringBuffer();
 		tmp.append("<h3>Help-page is in progress</h3>");
 		return tmp.toString();
 	}
 
 	public static String getSystemId(String hash) {
 		return hash.substring(hash.lastIndexOf("/") + 1, hash.length());
 	}
 
 	public static int getPageIndex(String hash) {
 		hash = "#" + hash;
 		int index = PAGE_DASHBOARD;
 		if (hash.equals(HTML_DASHBOARD_NAME)) {
 			index = PAGE_DASHBOARD;
 		} else if (hash.equals(HTML_ABOUT_NAME)) {
 			index = PAGE_ABOUT;
 		} else if (hash.equals(HTML_HELP_NAME)) {
 			index = PAGE_HELP;
 		} else if (hash.equals(HTML_ADD_NEW_SYSTEM_NAME)) {
 			index = PAGE_ADD_SYSTEM;
 		} else if (hash.startsWith(HTML_SYSTEM_DETAIL_NAME)) {
 			index = PAGE_SYSTEM_DETAIL;
 		} else if (hash.equals(HTML_SYSTEM_MANAGEMENT_NAME)) {
 			index = PAGE_SYSTEM_MANAGEMENT;
 		} else if (hash.equals(HTML_USER_MANAGEMENT_NAME)) {
 			index = PAGE_USER_MANAGEMENT;
 		} else if (hash.startsWith(HTML_SYSTEM_STATISTIC_NAME)) {
 			index = PAGE_SYSTEM_STATISTIC;
 		} else if (hash.startsWith(HTML_EDIT_NAME)) {
 			index = PAGE_EDIT_SYSTEM;
 		} else if (hash.startsWith(HTML_DELETE_SYSTEM_NAME)) {
 			index = PAGE_DELETE_SYSTEM;
 		}
 		return index;
 	}
 
 	public static boolean validIndex(String hash) {
 		hash = "#" + hash;
 		return (hash.equals(HTML_DASHBOARD_NAME)
 				|| hash.equals(HTML_ABOUT_NAME) || hash.equals(HTML_HELP_NAME)
 				|| hash.equals(HTML_SYSTEM_MANAGEMENT_NAME)
 				|| hash.equals(HTML_USER_MANAGEMENT_NAME)
 				|| hash.startsWith(HTML_SYSTEM_DETAIL_NAME)
 				|| hash.startsWith(HTML_SYSTEM_STATISTIC_NAME)
 				|| hash.startsWith(HTML_DELETE_SYSTEM_NAME)
 				|| hash.equals(HTML_ADD_NEW_SYSTEM_NAME) || hash
 					.startsWith(HTML_EDIT_NAME));
 	}
 
 	public static HTML getSystemInfo(SystemMonitor sys) {
 		StringBuffer temp = new StringBuffer();
 		temp.append("<h3>SID: " + sys.getCode() + "</h3>");
 		temp.append("<h3>Name: " + sys.getName() + "</h3>");
 		temp.append("<h3>IP: " + sys.getIp() + "</h3>");
 		temp.append("<h3>Health Status: ");
 		temp.append("<img src=\"images/icon/" + sys.getHealthStatus());
 		temp.append("_status_icon.png\" width=\"24\" height=\"24\" /></h3>");
 		return new HTML(temp.toString());
 	}
 
 	public static HTML getLogoutHTML(String url, String username) {
 		StringBuffer temp = new StringBuffer();
 
 		temp.append("<a href='");
 		temp.append(url);
 		temp.append("' id='logout'><img src='images/shared/nav/nav_logout.gif' width='64' height='14' /></a>");
 		temp.append("<div class='showhide-account'><span>" + username
 				+ "</span>");
 		temp.append("</div>");
 		return new HTML(temp.toString());
 	}
 
 	public static HTML getLoginHTML(String url) {
 		StringBuffer temp = new StringBuffer();
 		temp.append("<a href='");
 		temp.append(url);
 		temp.append("' id='logout'><img src='images/shared/nav/nav_login.gif' width='64' height='14' /></a>");
 		return new HTML(temp.toString());
 	}
 
 	public static HTML getMenuHTML(int page, int role) {
 		StringBuffer temp = new StringBuffer();
 		// Dashboard Menu
 		temp.append("<ul class='");
 		temp.append((page == PAGE_DASHBOARD || page == PAGE_SYSTEM_DETAIL || page == PAGE_SYSTEM_STATISTIC) ? "current"
 				: "select");
 		temp.append("'><li><a href='" + HTML_DASHBOARD_NAME + "'><b>");
 		temp.append("Dashboard");
 		temp.append("</b></a><div class='select_sub");
 		temp.append((page == PAGE_DASHBOARD || page == PAGE_SYSTEM_DETAIL || page == PAGE_SYSTEM_STATISTIC) ? " show"
 				: "");
 		temp.append("'><ul class='sub'>");
 		temp.append("<li><a href=''></a></li></ul></div></li></ul>");
 
 		// Administration Menu
 		if (role == MonitorConstant.ROLE_ADMIN) {
 			temp.append("<div class='nav-divider'>&nbsp;</div>");
 			temp.append("<ul class='");
 			temp.append((page == PAGE_USER_MANAGEMENT
 					|| page == PAGE_SYSTEM_MANAGEMENT
 					|| page == PAGE_ADD_SYSTEM || page == PAGE_EDIT_SYSTEM) ? "current"
 					: "select");
 			temp.append("'><li><a href='" + HTML_SYSTEM_MANAGEMENT_NAME
 					+ "'><b>");
 			temp.append("Administration");
 			temp.append("</b></a><div class='select_sub");
 			temp.append((page == PAGE_SYSTEM_MANAGEMENT
 					|| page == PAGE_USER_MANAGEMENT || page == PAGE_ADD_SYSTEM || page == PAGE_EDIT_SYSTEM) ? " show"
 					: "");
 			temp.append("'><ul class='sub'>");
 			temp.append("<li");
 			temp.append((page == PAGE_SYSTEM_MANAGEMENT
 					|| page == PAGE_ADD_SYSTEM || page == PAGE_EDIT_SYSTEM) ? " class='sub_show'"
 					: "");
 			temp.append("><a href='" + HTML_SYSTEM_MANAGEMENT_NAME
 					+ "'>System Management</a></li>");
 			temp.append("<li");
 			temp.append((page == PAGE_USER_MANAGEMENT) ? " class='sub_show'"
 					: "");
 			temp.append("><a href='" + HTML_USER_MANAGEMENT_NAME
 					+ "'>User List</a></li></ul></div></li></ul>");
 
 		}
 		// About Menu
 		temp.append("<div class='nav-divider'>&nbsp;</div>");
 		temp.append("<ul class='");
 		temp.append((page == PAGE_ABOUT) ? "current" : "select");
 		temp.append("'><li><a href='" + HTML_ABOUT_NAME + "'><b>");
 		temp.append("About");
 		temp.append("</b></a><div class='select_sub");
 		temp.append((page == PAGE_ABOUT) ? " show" : "");
 		temp.append("'><ul class='sub'>");
 		temp.append("<li><a href=''></a></li></ul></div></li></ul>");
 		// Help Menu
 		temp.append("<div class='nav-divider'>&nbsp;</div>");
 		temp.append("<ul class='");
 		temp.append((page == PAGE_HELP) ? "current" : "select");
 		temp.append("'><li><a href='" + HTML_HELP_NAME + "'><b>");
 		temp.append("Help");
 		temp.append("</b></a><div class='select_sub");
 		temp.append((page == PAGE_HELP) ? " show" : "");
 		temp.append("'><ul class='sub'>");
 		temp.append("<li><a href=''></a></li></ul></div></li></ul>");
 		return new HTML(temp.toString());
 	}
 
 	public static String getColor(int type) {
 		String color = "";
 		switch (type) {
 		case HTMLControl.BLUE_MESSAGE:
 			color = "blue";
 			break;
 		case HTMLControl.RED_MESSAGE:
 			color = "red";
 			break;
 		case HTMLControl.YELLOW_MESSAGE:
 			color = "yellow";
 			break;
 		case HTMLControl.GREEN_MESSAGE:
 		default:
 			color = "green";
 			break;
 		}
 		return color;
 	}
 
 	public static HTML getColorTitle(String title, boolean isOpen) {
 		return new HTML(
 				"<div id='message-blue'>"
 						+ "<table border='0' width='100%' cellpadding='0' cellspacing='0'>"
 						+ "<tr><td class='blue-left'>" + title + "</td>"
 						+ "<td class='blue-right'>" + "<a class='close-blue'>"
 						+ "<img src='images/icon/icon_"
 						+ (isOpen ? "close" : "open")
 						+ "_blue.gif' /></a></td></tr></table></div>");
 	}
 
 	public static String getHTMLStatusImage(String sid, String healthStatus) {
 		String mes = "";
 		if (healthStatus.equals("dead")) {
 			mes = "System is not working.\nClick the Icon to see more information!";
 		} else if (healthStatus.equals("bored")) {
 			mes = "Insufficient data.\nClick the Icon to see more information!";
 		} else if (healthStatus.equals("smile")) {
 			mes = "All is working correctly.\nClick the Icon to see more information!";
 		} else {
 			mes = "Click the Icon to see more information!";
 		}
 		return "<img src=\"images/icon/"
 				+ healthStatus
 				+ "_status_icon.png\" width=\"24\" height=\"24\" "
 				+ "style=\"display: block; margin-left: auto; margin-right: auto\""
 				+ " onClick=\"javascript:showStatusDialogBox('" + sid + "','"
 				+ healthStatus + "');\"" + " title='" + mes + "'" + " alt='"
 				+ mes + "'/>";
 	}
 
 	public static String getHTMLStatusImage(boolean b) {
 		return "<img src=\"images/icon/"
 				+ Boolean.toString(b)
 				+ "_icon.png\" width=\"24\" height=\"24\" "
 				+ "style=\"display: block; margin-left: auto; margin-right: autso\" />";
 
 	}
 
 	public static String getHTMLActiveImage(boolean b) {
 		return "<img src=\"images/icon/p_"
 				+ (b ? "online" : "offline")
 				+ ".gif\" "
 				+ " style=\"display: block; margin-left: auto; margin-right: auto\"/>";
 	}
 
 	public static String getLinkSystemDetail(String id, String code) {
 		return "<a href=\"" + HTML_SYSTEM_DETAIL_NAME + "/" + id
 				+ "\"  class='system-id' ><span>" + code + "</span></a>";
 
 	}
 
 	public static String getLinkSystemStatistic(SystemMonitor sys) {
 		return "<a href=\"" + MonitorConstant.PROJECT_HOST_NAME + "/Index.html"
 				+ HTML_SYSTEM_STATISTIC_NAME + "/" + sys.getId() + "\" ><span>"
 				+ sys + "</span></a>";
 
 	}
 
 	public static String getLinkEditSystem(String id, String code) {
 		return "<a href=\"" + HTML_EDIT_NAME + "/" + id + "\">" + code + "</a>";
 
 	}
 
 	public static String getStringTime(int secsIn) {
 		int hours = secsIn / 3600, remainder = secsIn % 3600, minutes = remainder / 60, seconds = remainder % 60;
 
 		String time = ((hours < 10 ? "0" : "") + hours + ":"
 				+ (minutes < 10 ? "0" : "") + minutes + ":"
 				+ (seconds < 10 ? "0" : "") + seconds);
 		return time;
 	}
 
 	public static HTML getPageHeading(SystemMonitor sys) {
 		StringBuffer temp = new StringBuffer();
 		temp.append("<h1>");
 		temp.append("<a href=\"" + HTML_DASHBOARD_NAME + "\">Dashboard</a> ");
 		temp.append(HTML_ARROW_IMAGE);
 		temp.append(" <a ");
 		temp.append("\">");
 		temp.append(sys.getCode() + " - " + sys.getName());
 		temp.append("</a> ");
 		return new HTML(temp.toString());
 	}
 
 	public static HTML getPageHeading(int page) {
 		StringBuffer temp = new StringBuffer();
 		temp.append("<h1>");
 
 		if (page == PAGE_DASHBOARD || page == PAGE_SYSTEM_STATISTIC
 				|| page == PAGE_SYSTEM_DETAIL) {
 			temp.append("<a href=\"" + HTML_DASHBOARD_NAME
 					+ "\">Dashboard</a> ");
 		}
 		if (page == PAGE_SYSTEM_STATISTIC || page == PAGE_SYSTEM_DETAIL) {
 			temp.append(HTML_ARROW_IMAGE);
 			temp.append(" <a ");
 			temp.append("\">");
 			temp.append(page == PAGE_SYSTEM_STATISTIC ? "Statistic System" : "");
 			temp.append(page == PAGE_SYSTEM_DETAIL ? "System Information" : "");
 			temp.append("</a> ");
 		}
 		if (page == PAGE_SYSTEM_MANAGEMENT || page == PAGE_ADD_SYSTEM
 				|| page == PAGE_EDIT_SYSTEM) {
 			temp.append("<a href=\"" + HTML_SYSTEM_MANAGEMENT_NAME
 					+ "\">System Management</a> ");
 		}
 		if (page == PAGE_ADD_SYSTEM || page == PAGE_EDIT_SYSTEM) {
 			temp.append(HTML_ARROW_IMAGE);
 			temp.append(" <a ");
 			temp.append("\">");
 			temp.append(page == PAGE_EDIT_SYSTEM ? "Edit System" : "");
 			temp.append(page == PAGE_ADD_SYSTEM ? "Add New System" : "");
 			temp.append("</a> ");
 		}
 		if (page == PAGE_USER_MANAGEMENT) {
 			temp.append("<a href=\"" + HTML_USER_MANAGEMENT_NAME
 					+ "\">User List</a> ");
 		}
 		if (page == PAGE_ABOUT) {
 			temp.append("<a href=\"" + HTML_ABOUT_NAME + "\">About Us</a> ");
 		}
 		if (page == PAGE_HELP) {
 			temp.append("<a href=\"" + HTML_HELP_NAME + "\">Help Content</a> ");
 		}
 		temp.append("</h1>");
 		return new HTML(temp.toString());
 	}
 
 	public static String getPercentBar(int percent, int redRangeValue) {
 		StringBuffer sb = new StringBuffer();
 		String startImg = "<img style=\"padding: 0\" src=\"http://ajax.googleapis.com/ajax/static/modules/gviz/1.0/util/";
 		sb.append("<span style=\"padding: 0; float: left; white-space: nowrap;\">");
 		sb.append(startImg + "bar_s.png\" height=\"12\" width=\"1\">");
 		if (percent > 0) {
 			sb.append(startImg + "bar_"
 					+ (percent >= redRangeValue ? "r" : "b")
 					+ ".png\" height=\"12\" width=\"" + 2 * percent + "\">");
 		}
 		sb.append(startImg
 				+ "bar_w.png\" "
 				+ (percent == -1 ? "title=\"Has no data from the system\" alt=\"Has no data from the system\""
 						: "") + " height=\"12\" width=\"" + 2 * (100 - percent)
 				+ "\">");
 		sb.append(startImg + "bar_s.png\" height=\"12\" width=\"1\">"
 				+ (percent == -1 ? "" : ("&nbsp;" + percent + "%")) + "</span>");
 		return sb.toString();
 	}
 
 	public static HTML getStepHolder(int page, String sid) {
 		StringBuffer temp = new StringBuffer();
 		if (page == PAGE_SYSTEM_DETAIL || page == PAGE_SYSTEM_STATISTIC) {
 			temp.append("<div class=\"step-no"
 					+ ((page == PAGE_SYSTEM_DETAIL) ? "" : "-off")
 					+ "\">1</div>");
 			temp.append("<div class=\"step-"
 					+ ((page == PAGE_SYSTEM_DETAIL) ? "dark" : "light")
 					+ "-left\">");
 			temp.append("<a href=\"" + HTML_SYSTEM_DETAIL_NAME + "/" + sid
 					+ "\">System Infomation</a>");
 			temp.append("</div>");
 			temp.append("<div class=\"step-"
 					+ ((page == PAGE_SYSTEM_DETAIL) ? "dark" : "light")
 					+ "-right\">&nbsp;</div>");
 			temp.append("<div class=\"step-no"
 					+ ((page == PAGE_SYSTEM_STATISTIC) ? "" : "-off")
 					+ "\">2</div>");
 			temp.append("<div class=\"step-"
 					+ ((page == PAGE_SYSTEM_STATISTIC) ? "dark" : "light")
 					+ "-left\">");
 			temp.append("<a href=\"" + HTML_SYSTEM_STATISTIC_NAME + "/" + sid
 					+ "\">System Statistic</a>");
 			temp.append("</div>");
 			temp.append("<div class=\"step-"
 					+ ((page == PAGE_SYSTEM_STATISTIC) ? "dark" : "light")
 					+ "-round\">&nbsp;</div>");
 			temp.append("<div class=\"clear\"></div>");
 		} else if (page == PAGE_SYSTEM_MANAGEMENT || page == PAGE_ADD_SYSTEM) {
 			temp.append("<div class=\"step-no"
 					+ ((page == PAGE_SYSTEM_MANAGEMENT) ? "" : "-off")
 					+ "\">1</div>");
 			temp.append("<div class=\"step-"
 					+ ((page == PAGE_SYSTEM_MANAGEMENT) ? "dark" : "light")
 					+ "-left\">");
 			temp.append("<a href=\"" + HTML_SYSTEM_MANAGEMENT_NAME
 					+ "\">System List</a>");
 			temp.append("</div>");
 			temp.append("<div class=\"step-"
 					+ ((page == PAGE_SYSTEM_MANAGEMENT) ? "dark" : "light")
 					+ "-right\">&nbsp;</div>");
 			temp.append("<div class=\"step-no"
 					+ ((page == PAGE_ADD_SYSTEM) ? "" : "-off") + "\">2</div>");
 			temp.append("<div class=\"step-"
 					+ ((page == PAGE_ADD_SYSTEM) ? "dark" : "light")
 					+ "-left\">");
 			temp.append("<a href=\"" + HTML_ADD_NEW_SYSTEM_NAME
 					+ "\">Add New System</a>");
 			temp.append("</div>");
 			temp.append("<div class=\"step-"
 					+ ((page == PAGE_ADD_SYSTEM) ? "dark" : "light")
 					+ "-round\">&nbsp;</div>");
 			temp.append("<div class=\"clear\"></div>");
 		}
 		return new HTML(temp.toString());
 	}
 
 	public static String trimHashPart(String url) {
 		String temp = url;
 		if (url.contains("#")) {
 			temp = temp.substring(0, url.indexOf("#"));
 		}
 		return temp;
 	}
 
 	public static HTML getHtmlForm(int view) {
 		String temp = "";
 		if (view == VIEW_STATISTIC) {
 			temp += "<div id=\"test1\"></div>";
 		} else {
 			temp += "<div id=\"test2\"></div>";
 		}
 		return new HTML(temp);
 	}
 
 	public static String convertMemoryToString(double value) {
 		value = value / 1024;
 		String temp = null;
 		if (value >= 1024 * 1024) {
 			temp = NumberFormat.getFormat("#.0").format(value / (1024 * 1024))
 					+ " GB";
 		} else if (value >= 1024) {
 			temp = NumberFormat.getFormat("#.0").format(value / 1024) + " MB";
 		} else {
 			temp = NumberFormat.getFormat("#.0").format(value) + " KB";
 		}
 		return temp;
 	}
 
 	public static String format(final String format, final Object... args) {
 		StringBuilder sb = new StringBuilder();
 		int cur = 0;
 		int len = format.length();
 		while (cur < len) {
 			int fi = format.indexOf('{', cur);
 			if (fi != -1) {
 				sb.append(format.substring(cur, fi));
 				int si = format.indexOf('}', fi);
 				if (si != -1) {
 					String nStr = format.substring(fi + 1, si);
 					int i = Integer.parseInt(nStr);
 					sb.append(args[i]);
 					cur = si + 1;
 				} else {
 					sb.append(format.substring(fi));
 					break;
 				}
 			} else {
 				sb.append(format.substring(cur, len));
 				break;
 			}
 		}
 		return sb.toString();
 	}
 }
