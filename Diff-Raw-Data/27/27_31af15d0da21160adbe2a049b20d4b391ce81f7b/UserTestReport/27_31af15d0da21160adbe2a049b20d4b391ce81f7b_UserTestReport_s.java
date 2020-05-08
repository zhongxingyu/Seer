 /**
  *
  */
 package om.tnavigator.reports.std;
 
 import java.sql.ResultSet;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import om.tnavigator.*;
 import om.tnavigator.db.DatabaseAccess;
 import om.tnavigator.reports.OmTestReport;
 import util.xml.XHTML;
 import util.xml.XML;
 
 /**
  * @author tjh238
  *
  */
 public class UserTestReport implements OmTestReport {
 	NavigatorServlet ns;
 
 	/**
 	 * Create an instance of this report.
 	 * @param ns the navigator servlet we belong to.
 	 */
 	public UserTestReport(NavigatorServlet ns) {
 		this.ns = ns;
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmTestReport#getUrlTestReportName()
 	 */
 	public String getUrlTestReportName() {
 		return "user";
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmTestReport#getReadableReportName()
 	 */
 	public String getReadableReportName() {
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmTestReport#isApplicable(om.tnavigator.TestDeployment)
 	 */
 	public boolean isApplicable(TestDeployment td) {
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmTestReport#handleTestReport(om.tnavigator.UserSession, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public void handleTestReport(UserSession us, String sUser,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		// Build result
 		StringBuffer sb=new StringBuffer("<div class='basicpage userreport report'>");
 
		SimpleDateFormat sdf=new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
 
 		// Query from database for PIs and questions
 		DatabaseAccess.Transaction dat=ns.getDatabaseAccess().newTransaction();
 		try
 		{
 			// Get session info
 			Map<Integer, String> mSessionInfo=new HashMap<Integer, String>();
 			ResultSet rs=ns.getOmQueries().queryUserReportSessions(dat,us.getTestId(),sUser);
 			while(rs.next())
 			{
 				int iTAttempt=rs.getInt(1);
 				Timestamp tsDate=rs.getTimestamp(2);
 				String sIP=rs.getString(3);
 				String sUserAgent=rs.getString(4);
 
 				String sOutput=mSessionInfo.get(iTAttempt);
 				if(sOutput==null) sOutput=
 					"<div class='sessions'>" +
 					"<table class='topheaders'>"+
 					"<tr><th class='time'>Time</th><th class='ip'>IP address</th><th class='useragent'>User agent</th></tr>"+
 					"</table></div>";
 				sOutput=sOutput.substring(0,sOutput.length()-"</table></div>".length());
				sOutput+="<tr><td class='time'>"+sdf.format(tsDate)+
 					"</td><td class='ip'>"+sIP+"</td><td class='useragent'>"+
 					XHTML.escape(sUserAgent, XHTML.ESCAPE_TEXT)+"</td></tr></table></div>";
 				mSessionInfo.put(iTAttempt,sOutput);
 			}
 
 			rs=ns.getOmQueries().queryUserReportTest(dat,us.getTestId(),sUser);
 
 			int iCurrentTAttempt=0;
 			String sCurrentQuestion=null;
 			int iCurrentQAttempt=0;
 
 			boolean bInTest=false,bInQuestion=false;
 			while(rs.next())
 			{
 				int iTAttempt=rs.getInt(1);
 				int iFinished=rs.getInt(2);
 				String sQuestion=rs.getString(3);
 				int iQAttempt=rs.getInt(4);
 				Timestamp tsDate=rs.getTimestamp(5);
 				String questionSummary=rs.getString(6);
 				if (questionSummary == null) questionSummary = "The question did not return this information.";
 				String answerSummary=rs.getString(7);
 				if (answerSummary == null) answerSummary = "The question did not return this information.";
 				String actionSummary=rs.getString(8);
 				if (actionSummary == null) actionSummary = "The question did not return this information.";
 				String attemptString = NavigatorServlet.getAttemptsString(rs.getInt(9));
 				if (rs.wasNull()) attemptString = "The question did not return this information.";
 				String sAxis=rs.getString(10);
 				String sAxisScore=rs.getString(11);
 				int iQNumber=rs.getInt(12);
 				Timestamp tsFinished=rs.getTimestamp(13);
 				Timestamp tsMinAction=rs.getTimestamp(14);
 				Timestamp tsMaxAction=rs.getTimestamp(15);
 				int qFinished=rs.getInt(16);
 
 				if(iTAttempt!=iCurrentTAttempt)
 				{
 					iCurrentTAttempt=iTAttempt;
 					if(bInQuestion)
 					{
 						sb.append("</div></div>");
 						bInQuestion=false;
 					}
 					if(bInTest) sb.append("</div>");
 					sCurrentQuestion = null;
 
 					sb.append("<div class='tattempt'><h3>Test attempt "+iCurrentTAttempt+
						" ("+(iFinished==0?"Unfinished":"Finished on "+(tsFinished==null ? "[date not available] " : sdf.format(tsFinished))
 								)+")</h3>");
 					bInTest=true;
 
 					String sSessionInfo=mSessionInfo.get(iTAttempt);
 					if(sSessionInfo!=null) sb.append(sSessionInfo);
 				}
 
 				if(sQuestion != null && qFinished > 0 && !sQuestion.equals(sCurrentQuestion))
 				{
 					iCurrentQAttempt=iQAttempt;
 					if(bInQuestion) sb.append("</div></div>");
 
 					sb.append("<div class='qattempt'><h4>#"+iQNumber+" ("+sQuestion+") attempt "+
 						iCurrentQAttempt+"</h4>"+
						"<div class='started'>Access time: <em>"+sdf.format(tsDate)+"</em></div>"+
						"<div class='started'>Action times: <em>"+sdf.format(tsMinAction)+"</em> - <em>"+sdf.format(tsMaxAction)+"</em></div>"+
 						"<div class='question'>Question: <em>"+XML.escape(questionSummary)+"</em></div>"+
 						"<div class='answer'>User's answer: <em>"+XML.escape(answerSummary)+"</em></div>"+
 						"<pre>"+XML.escape(actionSummary)+"</pre>"+
 						"<div class='attempts'>Result: <em>" + attemptString + "</em></div>" +
 						"<div class='scores'><span class='t'>Score:</span>");
 					bInQuestion=true;
 				}
 
 				if(sAxis!=null)
 				{
 					sb.append("<div><span class='axis'>"+
 						(sAxis.equals("") ? "Default" : sAxis)+
 						": </span><span class='val'>"+sAxisScore+"</span></div>");
 				}
 			}
 			if(bInQuestion) sb.append("</div></div>");
 			if(bInTest) sb.append("</div>");
 		}
 		finally
 		{
 			dat.finish();
 		}
 
 		sb.append("<p><a href='reports!'>Back to reports home</a></p>");
 		sb.append("</div>");
 		ns.serveTestContent(us,"Reports: "+sUser,"",null,null,sb.toString(),false, request, response, true);
 	}
 }
