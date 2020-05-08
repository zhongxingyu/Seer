 package org.sgrp.singer.db;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpSession;
 
 import org.sgrp.singer.AccessionConstants;
 import org.sgrp.singer.AccessionServlet;
 import org.sgrp.singer.ResourceManager;
 import org.sgrp.singer.SearchResults;
 import org.sgrp.singer.form.AccessionForm;
 import org.sgrp.singer.form.MemberForm;
 import org.sgrp.singer.form.PIDMemberForm;
 import org.sgrp.singer.indexer.BaseIndexer;
 import org.sgrp.singer.utils.MailUtils;
 
 public class OrderEmailManager extends DataManager {
 	public static OrderEmailManager	mgr;
 	public static final String		tname		= "order";
 	public static final String		RECIPIENT	= "recipient";
 	public static final String		PROVIDER	= "provider";
 	public static final String		TXT			= "txt";
 	public static final String		HTML		= "html";
 	public static final String		ONEFORALL	= "OneForAll";
 
 /*
 	public synchronized void sendOrderEmail(int orderid,HttpSession session) throws SQLException {
 		if (orderid != 0) { throw new SQLException(ResourceManager.getString("save.user.null")); }
 		Connection conn = null;
 		try {
 			conn = AccessionServlet.getCP().newConnection(this.toString());
 			String userid = null;
 			PreparedStatement mpstmt = conn.prepareStatement("select userid from orders where orderid=?");
 			mpstmt.setInt(1, orderid);
 			ResultSet mrs = mpstmt.executeQuery();
 			while (mrs.next()) {
 				userid = mrs.getString("userid");
 			}
 
 			if (userid != null) {
 				PreparedStatement pstmt = conn.prepareStatement("select accenumb_, collcode_ from orderitems where orderid=?");
 				pstmt.setInt(1, orderid);
 				ResultSet rs = pstmt.executeQuery();
 				HashMap<String, String> map = new HashMap<String, String>();
 				while (rs.next()) {
 					String accenumb = rs.getString("accenumb_");
 					String collcode = rs.getString("collcode_");
 					map.put(accenumb, AccessionConstants.COLLECTION + collcode);
 				}
 				pstmt.close();
 				processMapToEmail(orderid, userid, map, null, null);
 			}
 			conn.commit();
 		} catch (SQLException se) {
 			se.printStackTrace();
 			throw new SQLException();
 		} finally {
 			AccessionServlet.getCP().freeConnection(conn);
 		}
 	}
 */
 	public void processMapToEmail(int orderid, HttpSession session, HashMap<String, String> map, String comments, String smta) {
 		HashMap<String, HashMap<String, ArrayList<String>>> eMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
 
 		String smtaInMail = "unknown";
 		if(smta.equalsIgnoreCase("no"))
 		{
 			smtaInMail = "I will not accept the SMTA";
 		}
 		else if(smta.equalsIgnoreCase("shrinkwrapped"))
 		{
 			smtaInMail = "  I hereby give advance notification that I have read the terms and conditions of the SMTA and that I intend to accept these terms and conditions on receipt of the seed with shrink-wrap SMTA.";
 		}
 		else if(smta.equalsIgnoreCase("signedcopy"))
 		{
 			smtaInMail = "I or my organization requires a signed version of the SMTA. I understand that a signed printed copy of the SMTA will be sent to me and must be returned to the Provider before the material can be shipped to me.";
 
 		}
 		
 		
 		for (Iterator<String> itr = map.keySet().iterator(); itr.hasNext();) {
 			String accenumb = itr.next();
 			String collcode = map.get(accenumb);
 			String emailid = null;
 			try {
 				String tcollcode = AccessionConstants.replaceString(collcode, AccessionConstants.COLLECTION, "", 0);
 				emailid = AccessionServlet.getKeywords().getName(AccessionConstants.COLLECTION_ORDER_EMAIL_ACC + tcollcode);
 			} catch (Exception e) {
 				e.printStackTrace(System.out);
 			}
 			
 			if (emailid != null) {
 				if (!eMap.containsKey(emailid)) {
 					eMap.put(emailid, new HashMap<String, ArrayList<String>>());
 				}
 				HashMap<String, ArrayList<String>> cMap = eMap.get(emailid);
 				if (!cMap.containsKey(collcode)) {
 					cMap.put(collcode, new ArrayList<String>());
 				}
 				ArrayList<String> aMap = cMap.get(collcode);
 
 				if (!aMap.contains(accenumb)) {
 					aMap.add(accenumb);
 				}
 
 				cMap.put(collcode, aMap);
 				eMap.put(emailid, cMap);
 			}
 		}
 		
 		if (eMap != null && eMap.size() > 0) {
 			generateEmail(orderid, session, eMap, comments, smtaInMail);
 		}
 	}
 
 	public void generateEmail(int orderid, HttpSession session, HashMap<String, HashMap<String, ArrayList<String>>> eMap, String comments, String smta) {
 		//System.out.println("User id to sent email to :" + userid);
 	
 		PIDMemberForm membForm = null;
 		try {
 			membForm = UserManager.getInstance().getUser(session);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (membForm != null) {
 			generateReceipientEmail(orderid, membForm, eMap);
 			generateProviderEmail(orderid, membForm, eMap, comments, smta);
 			/*Added by Gautier*/
 			generateSingerEmail(orderid,membForm,eMap, comments, smta);
 			/*End added by Gautier*/
 			//generateOneMailForAll(orderid,membForm,eMap);
 
 		}
 	}
 
 	//Method added by Gautier to send an email to singer@cgiar.org when accessions are ordered
 	public void generateSingerEmail(int orderid, PIDMemberForm membForm, HashMap<String,HashMap<String, ArrayList<String>>> eMap, String comments, String smta)
 	{
 		if(eMap!=null && eMap.size()>0)
 		{
 			for(Iterator<String> itr= eMap.keySet().iterator(); itr.hasNext();)
 			{
 				String pemail = itr.next();
 				HashMap<String, ArrayList<String>> cMap = eMap.get(pemail);
 				try {
 					String singerHTML = getSingerHTMLMessage(membForm, cMap, orderid, comments, smta);
 					boolean sent = MailUtils.sendOrderMail("singer@cgiar.org", "Germplasm request #"+orderid+"  through SINGER - PLEASE DO NOT RESPOND TO THIS EMAIL ", singerHTML);
 			        System.out.println("Email Sent " + sent +" to singer@cgiar.org");
 					
 				} catch (Exception e) {
 					e.printStackTrace(System.out);
 				}
 			}
 		}
 	}
 	
 	public void generateReceipientEmail(int orderid, PIDMemberForm membForm, HashMap<String, HashMap<String, ArrayList<String>>> eMap) {
 		String remail = membForm.getNemail();
 		try {
 			//System.out.println("Email Sent to :" + memail);
 			String recipientHTML = getRecipientHTMLMessage(eMap,orderid);
 			boolean sent = MailUtils.sendOrderMail(remail, "Your germplasm request #"+orderid+" through SINGER - PLEASE DO NOT RESPOND TO THIS EMAIL ", recipientHTML);
 			System.out.println("Email Sent " + sent +" to "+remail);
 		} catch (Exception e) {
 			e.printStackTrace(System.out);
 		}
 	}
 
 	public void generateProviderEmail(int orderid, PIDMemberForm membForm, HashMap<String, HashMap<String, ArrayList<String>>> eMap, String comments, String smta) 
 	{
 		if(eMap!=null && eMap.size()>0)
 		{
 			for(Iterator<String> itr= eMap.keySet().iterator(); itr.hasNext();)
 			{
 				String pemail = itr.next();
 				HashMap<String, ArrayList<String>> cMap = eMap.get(pemail);				
 				try {
 					String providerHTML = getProviderHTMLMessage(membForm, cMap, orderid, comments, smta);
 					boolean sent = MailUtils.sendOrderMail(pemail, "Germplasm request #"+orderid+"  through SINGER - PLEASE DO NOT RESPOND TO THIS EMAIL ", providerHTML);
 					System.out.println("Email Sent " + sent +" to "+pemail+"\n"+providerHTML);
 				} catch (Exception e) {
 					e.printStackTrace(System.out);
 				}
 			}
 		}
 	}
 	
 	public void generateOneMailForAll(int orderid, PIDMemberForm membForm, HashMap<String, HashMap<String, ArrayList<String>>> eMap)
 	{
 		if(eMap!=null && eMap.size()>0)
 		{
 			for(Iterator<String> itr= eMap.keySet().iterator(); itr.hasNext();)
 			{
 				String pemail = itr.next();
 				HashMap<String, ArrayList<String>> cMap = eMap.get(pemail);
 				String[] emails = new String[]{pemail,membForm.getNemail()};
 				try {
 					String oneForAllHTML = getOneForAllHTMLMessage(membForm, cMap);
 					boolean sent = MailUtils.sendOrderMail(emails, "Germplasm request #"+orderid+"  through SINGER", oneForAllHTML);
 					System.out.println("Email Sent " + sent +" to "+pemail+" and "+membForm.getNemail());
 				} catch (Exception e) {
 					e.printStackTrace(System.out);
 				}
 			}
 		}
 	}
 	
 	public String getProviderHTMLMessage(PIDMemberForm membForm, HashMap<String, ArrayList<String>> cMap, int orderid, String comments, String smta) throws Exception {
 		String msg = null;
 		StringBuffer sb = new StringBuffer();
 		StringBuffer sbReply = new StringBuffer();
 		if (cMap != null && cMap.size() > 0) {
 			sb.append("<table>");
             int accCount = 0;
             List<String> acceNumbs = new ArrayList<String>();
 				for (Iterator<String> itr1 = cMap.keySet().iterator(); itr1.hasNext();) {
 					String collcode = itr1.next();
 					sb.append("<tr>");
 					sb.append("<td colspan=1 class=dispValue1><b>Provider :</b></td><td colspan=4 class=dispValue1>" + AccessionServlet.getKeywords().getName(collcode) + "</td>");
 					sb.append("</tr>");
 					sb.append("<tr>");
 					sb.append("<td class=dispHead align=center>Accession Number</td>");
 					sb.append("<td class=dispHead align=center>Accession Name</td>");
 					sb.append("<td class=dispHead align=center>Institute Name</td>");
 					sb.append("<td class=dispHead align=center>Genus</td>");
 					sb.append("<td class=dispHead align=center>Species</td>");
 					sb.append("</tr>");
 
 					ArrayList<String> aList = cMap.get(collcode);
 					for (int i = 0; i < aList.size(); i++) {
                         accCount++;
 						String accid = aList.get(i);
 						accid = BaseIndexer.mangleKeywordValue(accid);
 						AccessionForm accForm = SearchResults.getInstance().getAccession(accid, new String[] { collcode });
                         acceNumbs.add(accForm.getAccenumb());
 						sb.append("<tr>");
 						sb.append("<td class=dispValue0>" + accForm.getAccenumb() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getAccename() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getInstname() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getGenusname() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getSpeciesname() + "</td>");
 						sb.append("</tr>");
 					}
 					sb.append("<tr><td colspan=5>&nbsp;</td></tr>");
 			}
 			sb.append("<tr><td colspan=5 class=dispValue0>Total number of accessions: <b>"+accCount+"</b>.</td></tr>");
 
             String query = "";
             int qcount = 0;
             for(String acceNumb : acceNumbs) { // build query to download CSV
                 if(qcount != 0)
                     query += "&";
                 query += "acceNumbs="+acceNumb;
                 qcount++;
             }
 			sb.append("<tr><td colspan=5 class=dispValue0>Download the accessions in Excel format <a href=\"http://singer.cgiar.org/csv-download.jsp?"+query+"\">here</a>.</td></tr>");
 			sb.append("<table>");
 		}
 		
 		String accdata = sb.toString();
 		if (accdata != null && accdata.trim().length() > 0) {
 			String data = getTextFromFile(PROVIDER, HTML);
 			msg = AccessionConstants.replaceString(data, "<<acc_data>>", accdata, 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_pid>>", membForm.getNuserpid() , 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_name>>", membForm.getNfname() + " " + membForm.getNlname(), 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_inst>>", membForm.getNiname(), 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_add>>", membForm.getNShippingAddress()+"<br/>"+membForm.getNZip()+"<br/>"+membForm.getNCity()+"<br/>"+membForm.getNCountry(), 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_email>>", membForm.getNemail(), 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_type>>", membForm.getNType(), 0);
 			sbReply.append("<a href=mailto:\"");
 			sbReply.append( membForm.getNemail());
 			sbReply.append("?subject='Germplasm request #"+orderid+"  through SINGER '\" >");
 			sbReply.append(membForm.getNemail());
 			sbReply.append("</a>");
 			String reply = sbReply.toString();
 			msg = AccessionConstants.replaceString(msg, "<<req_reply>>", reply, 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_comments>>", comments, 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_smta>>", smta, 0);
 		}
 		// System.out.println("Message is "+msg);
 		return msg;
 	}
 	
 	//Method added by Gautier to send an email to singer@cgiar.org when accessions are ordered
 	public String getSingerHTMLMessage( PIDMemberForm membForm, HashMap<String, ArrayList<String>> cMap, int orderid, String comments, String smta)throws Exception 
 	{
 		String msg = "<b> This mail is the exact copy of the mail sent to the provider</b><br />";
 		StringBuffer sb = new StringBuffer();
 		StringBuffer sbReply = new StringBuffer();
 		if (cMap != null && cMap.size() > 0) {
 			sb.append("<table>");
             int accCount = 0;
 				for (Iterator<String> itr1 = cMap.keySet().iterator(); itr1.hasNext();) {
 					String collcode = itr1.next();
 					sb.append("<tr>");
 					sb.append("<td colspan=1 class=dispValue1><b>Provider :</b></td><td colspan=4 class=dispValue1>" + AccessionServlet.getKeywords().getName(collcode) + "</td>");
 					sb.append("</tr>");
 					sb.append("<tr>");
 					sb.append("<td class=dispHead align=center>Accession Number</td>");
 					sb.append("<td class=dispHead align=center>Accession Name</td>");
 					sb.append("<td class=dispHead align=center>Institute Name</td>");
 					sb.append("<td class=dispHead align=center>Genus</td>");
 					sb.append("<td class=dispHead align=center>Species</td>");
 					sb.append("</tr>");
 
 					ArrayList<String> aList = cMap.get(collcode);
 					for (int i = 0; i < aList.size(); i++) {
                         accCount++;
 						String accid = aList.get(i);
 						accid = BaseIndexer.mangleKeywordValue(accid);
 						AccessionForm accForm = SearchResults.getInstance().getAccession(accid, new String[] { collcode });
 						sb.append("<tr>");
 						sb.append("<td class=dispValue0>" + accForm.getAccenumb() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getAccename() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getInstname() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getGenusname() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getSpeciesname() + "</td>");
 						sb.append("</tr>");
 					}
 					sb.append("<tr><td colspan=5>&nbsp;</td></tr>");
 			}
 			sb.append("<tr><td colspan=5 class=dispValue0>Total number of accessions: <b>"+accCount+"</b>.</td></tr>");
 			sb.append("<table>");
 		}
 		
 		String accdata = sb.toString();
 		if (accdata != null && accdata.trim().length() > 0) {
 			String data = msg+getTextFromFile(PROVIDER, HTML);
 			msg = AccessionConstants.replaceString(data, "<<acc_data>>", accdata, 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_name>>", membForm.getNfname() + " " + membForm.getNlname(), 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_inst>>", membForm.getNiname(), 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_add>>", membForm.getNShippingAddress()+"<br/>"+membForm.getNZip()+"<br/>"+membForm.getNCity()+"<br/>"+membForm.getNCountry(), 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_email>>", membForm.getNemail(), 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_type>>", membForm.getNType(), 0);
 			sbReply.append("<a href=\"mailto:");
 			sbReply.append( membForm.getNemail());
 			sbReply.append("?subject='Germplasm request #"+orderid+"  through SINGER '\" >");
 			sbReply.append(membForm.getNemail());
 			sbReply.append("</a>");
 			String reply = sbReply.toString();
 			msg = AccessionConstants.replaceString(msg, "<<req_reply>>", reply, 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_comments>>", comments, 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_smta>>", smta, 0);
 		}
 		//System.out.println("Message is "+msg);
 		return msg;
 	
 	}
 	
 	public String getRecipientHTMLMessage(HashMap<String, HashMap<String, ArrayList<String>>> eMap, int orderid) throws Exception {
 		String msg = null;
 		StringBuffer sb = new StringBuffer();
 		if (eMap != null && eMap.size() > 0) {
 			sb.append("<table>");
             int accCount = 0;
 			for (Iterator<String> itr = eMap.keySet().iterator(); itr.hasNext();) {
 				String pemail = itr.next();
 				HashMap<String, ArrayList<String>> cMap = eMap.get(pemail);
 				for (Iterator<String> itr1 = cMap.keySet().iterator(); itr1.hasNext();) {
 					String collcode = itr1.next();
 					sb.append("<tr>");
 					sb.append("<td colspan=1 class=dispValue1><b>Provider :</b></td><td colspan=2 class=dispValue1>" + AccessionServlet.getKeywords().getName(collcode) + "</td>");
 					sb.append("<td colspan=1 class=dispValue1><b>Contact :</b></td><td colspan=1 class=dispValue1><a href=\"mailto:"+pemail+"?subject="+"germplasm request #"+orderid+" through SINGER" +"\">"+pemail+"</a></td>");
 					sb.append("</tr>");
 					sb.append("<tr>");
 					sb.append("<td class=dispHead align=center>Accession Number</td>");
 					sb.append("<td class=dispHead align=center>Accession Name</td>");
 					sb.append("<td class=dispHead align=center>Institute Name</td>");
 					sb.append("<td class=dispHead align=center>Genus</td>");
 					sb.append("<td class=dispHead align=center>Species</td>");
 					sb.append("</tr>");
 
 					ArrayList<String> aList = cMap.get(collcode);
 					for (int i = 0; i < aList.size(); i++) {
                         accCount++;
 						String accid = aList.get(i);
 						accid = BaseIndexer.mangleKeywordValue(accid);
 						AccessionForm accForm = SearchResults.getInstance().getAccession(accid, new String[] { collcode });
 						sb.append("<tr>");
 						sb.append("<td class=dispValue0>" + accForm.getAccenumb() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getAccename() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getInstname() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getGenusname() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getSpeciesname() + "</td>");
 						sb.append("</tr>");
 					}
 					sb.append("<tr><td colspan=5>&nbsp;</td></tr>");
 				}
 			}
 			sb.append("<tr><td colspan=5 class=dispValue0>You have ordered a total of <b>"+accCount+"</b> accessions.</td></tr>");
 			sb.append("<table>");
 		}
 		String accdata = sb.toString();
 		if (accdata != null && accdata.trim().length() > 0) {
 			String data = getTextFromFile(RECIPIENT, HTML);
 			msg = AccessionConstants.replaceString(data, "<<acc_data>>", accdata, 0);
 		}
 		// System.out.println("Message is "+msg);
 		return msg;
 	}
 	
 	public String getOneForAllHTMLMessage(PIDMemberForm membForm, HashMap<String, ArrayList<String>> cMap) throws Exception
 	{
 		String msg = null;
 		StringBuffer sb = new StringBuffer();
 		if (cMap != null && cMap.size() > 0) {
 			sb.append("<table>");
 				for (Iterator<String> itr1 = cMap.keySet().iterator(); itr1.hasNext();) {
 					String collcode = itr1.next();
 					sb.append("<tr>");
 					sb.append("<td colspan=1 class=dispValue1><b>Provider :</b></td><td colspan=4 class=dispValue1>" + AccessionServlet.getKeywords().getName(collcode) + "</td>");
 					sb.append("</tr>");
 					sb.append("<tr>");
 					sb.append("<td class=dispHead align=center>Accession Number</td>");
 					sb.append("<td class=dispHead align=center>Accession Name</td>");
 					sb.append("<td class=dispHead align=center>Institute Name</td>");
 					sb.append("<td class=dispHead align=center>Genus</td>");
 					sb.append("<td class=dispHead align=center>Species</td>");
 					sb.append("</tr>");
 
 					ArrayList<String> aList = cMap.get(collcode);
 					for (int i = 0; i < aList.size(); i++) {
 						String accid = aList.get(i);
 						accid = BaseIndexer.mangleKeywordValue(accid);
 						AccessionForm accForm = SearchResults.getInstance().getAccession(accid, new String[] { collcode });
 						sb.append("<tr>");
 						sb.append("<td class=dispValue0>" + accForm.getAccenumb() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getAccename() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getInstname() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getGenusname() + "</td>");
 						sb.append("<td class=dispValue0>" + accForm.getSpeciesname() + "</td>");
 						sb.append("</tr>");
 					}
 					sb.append("<tr><td colspan=5>&nbsp;</td></tr>");
 			}
 			sb.append("<table>");
 		}
 		String accdata = sb.toString();
 		if (accdata != null && accdata.trim().length() > 0) {
 			String data = getTextFromFile(ONEFORALL, HTML);
 			msg = AccessionConstants.replaceString(data, "<<acc_data>>", accdata, 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_name>>", membForm.getNfname() + " " + membForm.getNlname(), 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_inst>>", membForm.getNiname(), 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_add>>", membForm.getNShippingAddress()+"<br/>"+membForm.getNZip()+"<br/>"+membForm.getNCity()+"<br/>"+membForm.getNCountry(), 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_email>>", membForm.getNemail(), 0);
 			msg = AccessionConstants.replaceString(msg, "<<req_type>>", membForm.getNType(), 0);
 		}
 		// System.out.println("Message is "+msg);
 		return msg;
 	}
 
 	public String getTextFromFile(String fileName, String type) {
 		String data = null;
 		StringBuffer sb = new StringBuffer();
 		String rootDir = AccessionConstants.getDefaultParameter(ResourceManager.getString(AccessionConstants.TEMPLATES), ".");
 		File file = new File(rootDir, fileName + "_" + type + ".txt");
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			String line = null;
 			while ((line = br.readLine()) != null) {
 				sb.append(line);
 			}
 
 			br.close();
 		} catch (Exception e) {
 			e.printStackTrace(System.out);
 		}
 		data = sb.toString();
 		if (data != null && data.trim().length() == 0) {
 			data = null;
 		}
 		return data;
 	}
 
 	public static OrderEmailManager getInstance() {
 		if (mgr == null) {
 			mgr = new OrderEmailManager();
 		}
 		return mgr;
 	}
 }
