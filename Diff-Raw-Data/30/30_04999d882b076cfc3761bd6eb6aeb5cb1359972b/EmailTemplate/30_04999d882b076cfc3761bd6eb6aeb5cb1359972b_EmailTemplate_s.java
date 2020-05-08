 package models;
 
 public class EmailTemplate {
 	
 	public String getMsg() {
 		return msg;
 	}
 
 	public void setMsg(String msg) {
 		this.msg = msg;
 	}
 
	String msg;
 	
 public String getTemp() {
		return Temp;
 	}
 
	public void setTemp(String temp) {
		Temp = temp;
	}
 
String Temp="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
 "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><title>*|MC:SUBJECT|*</title>"+
 "<style type=\"text/css\">#outlook a{padding:0;} /* Force Outlook to provide a \"view in browser\" button. */"+
 "body{width:100% !important;} .ReadMsgBody{width:100%;} .ExternalClass{width:100%;} /* Force Hotmail to display emails at full width */"+
 "body{-webkit-text-size-adjust:none;} /* Prevent Webkit platforms from changing default text sizes. */"+
 "body{margin:0; padding:0;} img{border:0; height:auto; line-height:100%; outline:none; text-decoration:none;}"+
 "table td{border-collapse:collapse;}#backgroundTable{height:100% !important; margin:0; padding:0; width:100% !important;}"+
 "body, #backgroundTable{ background-color:#FAFAFA;}#templateContainer{ border:0;}h1, .h1{color:#202020;"+
 "display:block;font-family:Arial;font-size:40px;font-weight:bold;line-height:100%;margin-top:2%;margin-right:0;"+
 "margin-bottom:1%;margin-left:0;text-align:left;}h2, .h2{color:#404040;display:block;font-family:Arial;font-size:18px;"+
 "font-weight:bold;line-height:100%;margin-top:2%;margin-right:0;margin-bottom:1%;margin-left:0;text-align:left;"+
 "}h3, .h3{color:#606060;display:block; font-family:Arial; font-size:16px;font-weight:bold;line-height:100%;"+
 "margin-top:2%;margin-right:0;margin-bottom:1%;margin-left:0;text-align:left;}h4, .h4{color:#808080;display:block;"+
 "font-family:Arial;font-size:14px;font-weight:bold;/*@editable*/ line-height:100%;margin-top:2%;margin-right:0;margin-bottom:1%;"+
 "margin-left:0;text-align:left;}#templatePreheader{background-color:#FAFAFA;}.preheaderContent div{"+
 "/*@editable*/ color:#707070;/*@editable*/ font-family:Arial;/*@editable*/ font-size:10px;/*@editable*/ line-height:100%;"+
 "/*@editable*/ text-align:left;}.preheaderContent div a:link, .preheaderContent div a:visited, /* Yahoo! Mail Override */ .preheaderContent div a .yshortcuts /* Yahoo! Mail Override */{"+
 "/*@editable*/ color:#336699;/*@editable*/ font-weight:normal;/*@editable*/ text-decoration:underline;}"+
 "#social div{text-align:right;}#templateHeader{/*@editable*/ background-color:#FFFFFF;/*@editable*/ border-bottom:5px solid #505050;"+
 "}.headerContent{/*@editable*/ color:#202020;/*@editable*/ font-family:Arial;/*@editable*/ font-size:34px;"+
 "/*@editable*/ font-weight:bold;/*@editable*/ line-height:100%;/*@editable*/ padding:10px;/*@editable*/ text-align:right;"+
 "/*@editable*/ vertical-align:middle;}.headerContent a:link, .headerContent a:visited, /* Yahoo! Mail Override */ .headerContent a .yshortcuts /* Yahoo! Mail Override */{"+
 "/*@editable*/ color:#336699;/*@editable*/ font-weight:normal;/*@editable*/ text-decoration:underline;"+
 "}#headerImage{height:auto;max-width:600px !important;}#templateContainer, .bodyContent{/*@editable*/ background-color:#FDFDFD;"+
 "}.bodyContent div{color:#505050;/*@editable*/ font-family:Arial;/*@editable*/ font-size:14px;/*@editable*/ line-height:150%;/*@editable*/ text-align:justify;"+
 "}.bodyContent div a:link, .bodyContent div a:visited, /* Yahoo! Mail Override */ .bodyContent div a .yshortcuts /* Yahoo! Mail Override */{"+
 "color:#336699; font-weight:normal; text-decoration:underline;}.bodyContent img{display:inline;height:auto;}"+
 "#templateSidebar{ background-color:#FDFDFD;}.sidebarContent{/*@editable*/ border-right:1px solid #DDDDDD;}"+
 ".sidebarContent div{/*@editable*/ color:#505050;/*@editable*/ font-family:Arial;/*@editable*/ font-size:10px;/*@editable*/ line-height:150%;"+
 "/*@editable*/ text-align:left;}.sidebarContent div a:link, .sidebarContent div a:visited, /* Yahoo! Mail Override */ .sidebarContent div a .yshortcuts /* Yahoo! Mail Override */{"+
 "/*@editable*/ color:#336699;/*@editable*/ font-weight:normal;/*@editable*/ text-decoration:underline;}"+
 ".sidebarContent img{display:inline;height:auto;}#templateFooter{/*@editable*/ background-color:#FAFAFA;/*@editable*/ border-top:3px solid #909090;"+
 "}.footerContent div{/*@editable*/ color:#707070;/*@editable*/ font-family:Arial;/*@editable*/ font-size:11px;/*@editable*/ line-height:125%;"+
 "/*@editable*/ text-align:left;}.footerContent div a:link, .footerContent div a:visited, /* Yahoo! Mail Override */ .footerContent div a .yshortcuts /* Yahoo! Mail Override */{"+
 "/*@editable*/ color:#336699\";/*@editable*/ font-weight:normal;/*@editable*/ text-decoration:underline;}.footerContent img{display:inline;"+
 "}#social{ background-color:#FFFFFF; border:0;}#social div{/*@editable*/ text-align:left;}#utility{ background-color:#FAFAFA; border-top:0;"+
 "}#utility div{ text-align:left;}#monkeyRewards img{max-width:170px !important;}</style></head><body leftmargin=\"0\" marginwidth=\"0\" topmargin=\"0\" marginheight=\"0\" offset=\"0\">"+
 "<center><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" height=\"100%\" width=\"100%\" id=\"backgroundTable\"><tr><td align=\"center\" valign=\"top\">"+
 "<table border=\"0\" cellpadding=\"10\" cellspacing=\"0\" width=\"600\" id=\"templatePreheader\"><tr><td valign=\"top\" class=\"preheaderContent\">"+
 " <table border=\"0\" cellpadding=\"10\" cellspacing=\"0\" width=\"100%\"><tr><td valign=\"top\"><div mc:edit=\"std_preheader_content\">"+
 "Use this area to offer a short teaser of your email's content. Text here will show in the preview area of some email clients."+
 "</div></td><td valign=\"top\" width=\"170\"><div mc:edit=\"std_preheader_links\">Email not displaying correctly?<br /><a href=\"*|ARCHIVE|*\" target=\"_blank\">View it in your browser</a>."+
 "</div></td></tr></table></td></tr></table><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" id=\"templateContainer\">"+
 "<tr><td align=\"center\" valign=\"top\"> <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" id=\"templateHeader\">"+
 "<tr><td class=\"headerContent\"><img src=\"http://www.trtrip.com/images/Bingo.jpg\" style=\"max-width:180px;\" id=\"headerImage campaign-icon\" mc:label=\"header_image\" mc:edit=\"header_image\" mc:allowtext />"+
 "</td><td class=\"headerContent\" width=\"100%\" style=\"padding-left:10px; padding-right:20px;\"><div mc:edit=\"Header_content\">"+
 "<h1>BINGO</h1></div></td></tr></table></td></tr><tr><td align=\"center\" valign=\"top\"><table border=\"0\" cellpadding=\"10\" cellspacing=\"0\" width=\"600\" id=\"templateBody\">"+
 "<tr> <td valign=\"top\" width=\"180\" id=\"templateSidebar\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">"+
 "<tr><td valign=\"top\"><table border=\"0\" cellpadding=\"20\" cellspacing=\"0\" width=\"100%\" class=\"sidebarContent\">"+
 " <tr><td valign=\"top\" style=\"padding-left:10px;\"> <div mc:edit=\"std_content01\"><strong>Don't Reply on This Email</strong>"+
 "<br /><strong>Bingo is free service </strong><br />"+
 "SWE 574 Group1 2012 Spring semester</div> </td></tr> </table> </td></tr>"+
  "</table></td> <td valign=\"top\" class=\"bodyContent\"><table border=\"0\" cellpadding=\"10\" cellspacing=\"0\" width=\"100%\">"+
"<tr> <td valign=\"top\" style=\"padding-left:0;\"><div mc:edit=\"std_content00\"><h2 class=\"h2\">MESSAGE:</h2>"+msg+
"</div></td> </tr></table> </td></tr></table></td></tr> <tr> <td align=\"center\" valign=\"top\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" id=\"templateFooter\">"+
"<tr> <td valign=\"top\" class=\"footerContent\"> <table border=\"0\" cellpadding=\"10\" cellspacing=\"0\" width=\"100%\">"+
"<tr> <td colspan=\"2\" valign=\"middle\" id=\"social\"><div mc:edit=\"std_social\">&nbsp;<a href=\"*|TWITTER:PROFILEURL|*\">follow on Twitter</a> | <a href=\"*|FACEBOOK:PROFILEURL|*\">friend on Facebook</a> | <a href=\"*|FORWARD|*\">forward to a friend</a>&nbsp;"+
"</div></td></tr> <tr><td valign=\"top\" width=\"350\"><div mc:edit=\"std_footer\"><b>Copyright &copy; 2012 *Bogazici University*, All rights reserved.</b>"+
"</div></td><td valign=\"top\" width=\"190\" id=\"monkeyRewards\"><div mc:edit=\"monkeyrewards\"></div>"+
"</td></tr><tr> <td colspan=\"2\" valign=\"middle\" id=\"utility\"> <div mc:edit=\"std_utility\"> &nbsp;<a href=\"*|UNSUB|*\">unsubscribe from this list</a> | <a href=\"*|UPDATE_PROFILE|*\">update subscription preferences</a>&nbsp;"+
"</div></td></tr></table> </td></tr></table></td></tr> </table> <br /></td> </tr></table></center></body></html>";
 
 }
