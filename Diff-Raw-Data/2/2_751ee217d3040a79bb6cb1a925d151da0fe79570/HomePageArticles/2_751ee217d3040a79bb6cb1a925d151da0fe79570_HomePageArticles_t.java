 package org.alt60m.html;
 
 import java.util.*;
 
 import org.alt60m.cms.model.File;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class HomePageArticles implements java.io.Serializable {
 	private static Log log = LogFactory.getLog(HomePageArticles.class);
 	
 	
 	String titleFont = "<FONT FACE=\"Arial\" SIZE=\"3\" COLOR=\"#336699\">";
 	String dateFont = "<FONT FACE=\"Arial\" SIZE=\"1\" COLOR=\"#00000\">";
 	String authorFont = "<FONT FACE=\"Arial\" SIZE=\"2\" COLOR=\"#336699\">";
 	String bodyFont = "<FONT FACE=\"Arial\" SIZE=\"2\" COLOR=\"#000000\">";
 	String hr = "<hr color=#336699 size=1>";
 	String numOfArticles = "3";
 	String categoryId = "12";
 	String region = "";
 	String regionName = ""; //added by David to show user which region they are
 
     public HomePageArticles() {}
 
 	public void setNumOfArticles(String num) {
 		if(num != null && !"".equals(num))
 			numOfArticles = num;
 	}
 	public void setCategoryId(String num) {
 		categoryId = num;
 	}
 
 	public void setRegion(String reg) {
 		region = reg;
 		if (reg==null) {
 			categoryId = "";
 			regionName = "";
 		} else if (reg.equals("GL")) {
 			categoryId = "1000122";
 			regionName = "Great Lakes Regional";
 		} else if (reg.equals("GP")) {
 			categoryId = "1000123";
 			regionName = "Great Plains International Regional";
 		} else if (reg.equals("NW")) {
 			categoryId = "1000124";
 			regionName ="Greater Northwest Regional";
 		} else if (reg.equals("MA")) {
 			categoryId = "1000107";
 			regionName = "Mid-Atlantic Regional";
 		} else if (reg.equals("MS")) {
 			categoryId = "1000031";
 			regionName = "Mid-South Regional";
 		} else if (reg.equals("NC")) {
 			categoryId = "1000121";
 			regionName = "National Campus Office";
 		} else if (reg.equals("NE")) {
 			categoryId = "1000125";
 			regionName = "Northeast Regional";
 		} else if (reg.equals("SW")) {
 			categoryId = "1000126";
 			regionName = "Pacific Southwest Regional";
 		} else if (reg.equals("RR")) {
 			categoryId = "1000127";
 			regionName = "Red River Regional";
 		} else if (reg.equals("SE")) {
 			categoryId = "1000128";
 			regionName = "Southeast Regional";
 		} else if (reg.equals("UM")) {
 			categoryId = "1000129";
 			regionName = "Upper Midwest Regional";
 		} else {
 			region = "";
 			categoryId = "";
 			regionName = "";
 		}
 	}
 
 	public String print(String num) {
 		setNumOfArticles(num);
 		return print();
 	}
 
 	public String print() {
 
 		String type = "square";
 
 
 		String stringBuffer = "<!--  --------------------Recent Articles MODULE ------------------- --> ";
 		
 		int numToDisplay = new Integer(numOfArticles).intValue();
 		int numHeadersToDisplay = 2;    // specify the Number of Headlines to display in full.
 
 		//grab the files
 		try {
 			if (region.equals("")) {
 				File file = new File();
 				file.changeTargetTable("cms_viewcategoryidfiles");
 				Collection catFiles = file.selectList("CmsCategoryID='"+categoryId+"' AND quality='B' ORDER BY dateAdded DESC");
 
 				Iterator iter = catFiles.iterator();
 				if (iter.hasNext()) {
 					stringBuffer = stringBuffer + "<br><b><i><FONT FACE=\"Arial\" SIZE=\"3\" COLOR=\"#000000\">National News</a></font></b></i><br>&nbsp;<br>";
 				}
 				for(int i=0; i<numToDisplay && iter.hasNext(); i++) {
 					File recent = (File) iter.next();
 					
 					//print out the rest of the headlines without the summary
 					if(i > (numHeadersToDisplay - 1)) {
 						
 						if(i == numHeadersToDisplay) {
 							stringBuffer = stringBuffer + "				<br>"+ authorFont + "More Headlines:" + "</font>";
 							stringBuffer = stringBuffer + "<UL TYPE= " + type + ">";
 						}
 						
 						stringBuffer = stringBuffer + "				<b><li><A HREF='/lx_frame.jsp?id=" + recent.getFileId() + "' target='_blank'><font size=2 face=arial color='#336699'>" + recent.getTitle() + "</font></a></b></li>";	
 						
 						if(i == (numToDisplay-1) | !iter.hasNext()) {	// if max. number of articles to display reached or if last article
 							stringBuffer = stringBuffer + "</UL>";
 						}
 					
 					
 					}
 					
 					else {
 						stringBuffer = stringBuffer + "			<b>" + titleFont + recent.getTitle() + "</font></b>";
 						stringBuffer = stringBuffer + "				<br>\n"+ authorFont + "by " + recent.getAuthor() + "</font>";
 					//	stringBuffer = stringBuffer + "				<br>\n<i>" + dateFont + recent.getDateAdded() + "</font></i>"; //
						stringBuffer = stringBuffer + "				<br>\n" + bodyFont + recent.getSummary() + "</font>";
 					}
 				
 				}
 			} else {
 				File file = new File();
 				file.changeTargetTable("cms_viewcategoryidfiles");
 				Collection catFiles = file.selectList("CmsCategoryID='"+categoryId+"' ORDER BY dateAdded DESC");
 
 				Iterator iter = catFiles.iterator();
 				if (iter.hasNext()) {
 					stringBuffer = stringBuffer + "<b><i><FONT FACE=\"Arial\" SIZE=\"3\" COLOR=\"#000000\">" + regionName + " News</a></font></b></i><br>&nbsp;<br>";
 				}
 				for(int i=0; i<numToDisplay && iter.hasNext(); i++) {
 					File recent = (File) iter.next();
 					
 					//print out the rest of the headlines without the summary
 					if(i != 0) {
 						
 						if(i == 1) {
 							stringBuffer = stringBuffer + "				<br>"+ authorFont + "More Headlines:" + "</font>";
 							stringBuffer = stringBuffer + "<UL TYPE= " + type + ">";
 						}
 					
 						stringBuffer = stringBuffer + "				<b><li><A HREF='/lx_frame.jsp?id=" + recent.getFileId() + "' target='_blank'><font size=2 face=arial color='#336699'>" + recent.getTitle() + "</font></a></b></li>";	
 						
 						if(i == (numToDisplay-1) | !iter.hasNext()) {	// if max. number of articles to display reached or if last article
 							stringBuffer = stringBuffer + "</UL>";
 						}	
 					}
 					
 					else {
 						stringBuffer = stringBuffer + "			<b>" + titleFont + recent.getTitle() + "</font></b>";
 						stringBuffer = stringBuffer + "				<br>\n"+ authorFont + "by " + recent.getAuthor() + "</font>";
 						stringBuffer = stringBuffer + "				<br>\n<i>" + dateFont + recent.getDateAdded() + "</font></i>";
 						stringBuffer = stringBuffer + "				<br>\n" + bodyFont + recent.getSummary() + "</font>";
 						
 					}				
 				}
 				
 			}
 		} catch (Exception e) {
 			// should handle better!
 			log.error(e, e);
 		}
 
 		
 
 		return stringBuffer;
 	}
 }
