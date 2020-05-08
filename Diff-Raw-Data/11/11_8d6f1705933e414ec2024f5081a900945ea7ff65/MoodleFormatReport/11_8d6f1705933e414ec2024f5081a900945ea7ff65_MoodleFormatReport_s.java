 /**
  *
  */
 package om.tnavigator.reports.std;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import om.OmException;
 import om.OmUnexpectedException;
 import om.tnavigator.*;
 import om.tnavigator.db.DatabaseAccess;
 import om.tnavigator.reports.*;
 import om.tnavigator.scores.CombinedScore;
 import om.tnavigator.scores.AttemptForPI;
 
 /**
  * This report exports test scores in the format expected by the Moodle &gt;=1.9 gradebook.
  */
 public class MoodleFormatReport implements OmTestReport, OmReport {
 	private NavigatorServlet ns;
 
 	/**
 	 * Create an instance of this report.
 	 * @param ns the navigator servlet we belong to.
 	 */
 	public MoodleFormatReport(NavigatorServlet ns) {
 		this.ns = ns;
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmTestReport#getUrlTestReportName()
 	 */
 	public String getUrlTestReportName() {
 		return "moodle";
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmTestReport#getReadableReportName()
 	 */
 	public String getReadableReportName() {
 		return "Results for import into the Moodle gradebook";
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmTestReport#isApplicable(om.tnavigator.TestDeployment)
 	 */
 	public boolean isApplicable(TestDeployment td) {
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmReport#getUrlReportName()
 	 */
 	public String getUrlReportName() {
 		return "moodle";
 	}
 
 	private class MoodleTabularReport extends TabularReportBase {
 		private String testId;
 		private TestDefinition def;
 
 		MoodleTabularReport(String testId, TestDeployment deploy) throws OmException {
 			this.testId = testId;
 			this.def = deploy.getTestDefinition();
 			this.ns = MoodleFormatReport.this.ns;
 			batchid = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
 			title = testId + " results for export to Moodle";
 		}
 		
 	private void outputScoresForPI(AttemptForPI bestAttempt,TabularReportWriter reportWriter)
 		{
 
 		
 		String assignmentid=bestAttempt.getassignmentid();
 		CombinedScore score=bestAttempt.getScore();
 		String pi=bestAttempt.getPI();
 		try
 		{
 			// Output a row of the report for each axis.
			for (String axis : score.getAxes()) {
				Map<String, String> row = new HashMap<String, String>();
				if (axis != null)
				{
					assignmentid = bestAttempt.getassignmentid() + "." + axis;
				}
 				row.put("student", pi);
				row.put("assignment", assignmentid);
 				row.put("score", score.getScore(axis) + "");
 				reportWriter.printRow(row);
 			}	
 		} catch (Exception e) {
 			throw new OmUnexpectedException("Error outputting report.", e);
 		}
 		
 		}
 		/* (non-Javadoc)
 		 * @see om.tnavigator.reports.TabularReportBase#generateReport(om.tnavigator.reports.TabularReportWriter)
 		 */
 		@Override
 		public void generateReport(TabularReportWriter reportWriter) {
 			// Query from database for PIs and questions
 			DatabaseAccess.Transaction dat;
 			try {
 				dat = ns.getDatabaseAccess().newTransaction();
 			} catch (SQLException e1) {
 				throw new OmUnexpectedException("Cannot connect to the database");
 			}
 			try
 			{
 				// Get list of people who did the test.
 				// Show:
 				// * Each person only once
 				// * Only the most recent finished attempt, or most recent unfinished attempt
 				//   if there aren't any finished
 				// * Within categories (finished/unfinished), sorting by PI
 				// I achieve this by setting the sort order and dropping all but the first
 				// result for each PI.
 				ResultSet rs = ns.getOmQueries().queryTestAttemptersByPIandFinishedASC(dat, testId);
 				//create empty lists
 				boolean startflag=true;
 				String lastpi="";
 				int isAdmin=0;
 				AttemptForPI BestAttempt = new AttemptForPI("",0,null,"",false);
 
 				while(rs.next())
 				{
 					isAdmin=rs.getInt(5);
 					String pi = rs.getString(2);
 					boolean isDummy=pi.toLowerCase().startsWith("q");
 
 					//ns.getLog().logDebug("*************** pi " + pi);
 					//we dont do admins, and we dont do dummy students which start with a Q
 					if (isAdmin != 1 && pi != "" && !isDummy) //need the test on pi just to be on safe side
 					{
 						
 						//first we deal with the previous student and output it if necesary
 						if (!pi.equals(lastpi) && !(startflag) && !pi.equals(""))
 						{
 							// ok, we need to process and output last student if they finished
 							// startflag is there so we dont process the first student yet
 			
 							if(BestAttempt.gethasFinished())
 							{
 									outputScoresForPI(BestAttempt,reportWriter);	
 									ns.getLog().logDebug("Output score for  " +BestAttempt.getPI() +
 											" score " + BestAttempt.gettestScore() );
 							}
 							//reset BestAttempt
 							BestAttempt = new AttemptForPI("",0,null,"",false);
 
 						}
 						//we checked wether to output this one, so reset the startflag 
 					    startflag=false;
 						
 						// now we get on with checking this student
 						int ti = rs.getInt(9);
 						int isFinished=rs.getInt(4);
 						long randomSeed = rs.getLong(7);
 						int fixedVariant = rs.getInt(8);
 						
 						//not an admin so go with it
 						//is this pi the same as the last? if it isnt we need to process it
 						//ns.getLog().logDebug("pi *" + pi +
 						//		"* lastpi *" + lastpi + "* ti " + ti );
 
 						// Create TestRealisation
 						TestRealisation testRealisation = TestRealisation.realiseTest(
 								def, randomSeed, fixedVariant, testId, ti);	
 						// Use it to get the score.
 						String assignmentid = testRealisation.getTestId();
 						
 						CombinedScore score = testRealisation.getScore(new NavigatorServlet.RequestTimings(), ns, ns.getDatabaseAccess(), ns.getOmQueries());
 						//set up out test attempt ready for the list
 						// despite the comments in the getscore declaration., using null causes a java exception
 						// so we use the get axis ordered and pick of the first one.
 						
 						String defaultAxis=null;
 						for (String axis : score.getAxesOrdered()) {
 							//  get the score for the first one, which is the default
 							ns.getLog().logDebug("axis " + axis );
 							defaultAxis=axis;
 							break;
 							}
 
 						double thisscore=score.getScore(defaultAxis);					
 						ns.getLog().logDebug("this attempt " + pi +
 								" score " + thisscore );	
 						//set values for the current attempt then compare with th best attempt so far
 						AttemptForPI ThisAttempt = new AttemptForPI(pi,thisscore,score,assignmentid,isFinished==1);
 		
 						BestAttempt.SetIfGreater(ThisAttempt);
 						lastpi=pi;
 					}
 				}
 				// now output the last student as long as they aren't an admin
 				if (BestAttempt.gethasFinished())
 				{	
 					outputScoresForPI(BestAttempt,reportWriter);
 					ns.getLog().logDebug("Output score for  " +BestAttempt.getPI() +
 							" score " + BestAttempt.gettestScore() );
 				}
 
 				//
 				
 			} catch (Exception e) {
 				throw new OmUnexpectedException("Error generating report.", e);
 			}
 			finally
 			{
 				dat.finish();
 			}
 
 		}
 
 		/* (non-Javadoc)
 		 * @see om.tnavigator.reports.TabularReportBase#init(javax.servlet.http.HttpServletRequest)
 		 */
 		@Override
 		public List<ColumnDefinition> init(HttpServletRequest request) {
 			List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
 			columns.add(new ColumnDefinition("student", "Student"));
 			columns.add(new ColumnDefinition("assignment", "Assignment"));
 			columns.add(new ColumnDefinition("score", "Score"));
 			return columns;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmTestReport#handleTestReport(om.tnavigator.UserSession, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public void handleTestReport(UserSession us, String suffix,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		MoodleTabularReport report = new MoodleTabularReport(us.getTestId(), us.getTestDeployment());
 		report.handleReport(request, response);
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmReport#handleReport(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public void handleReport(String suffix, HttpServletRequest request, HttpServletResponse response) throws Exception {
 		String testId = request.getParameter("test");
 		TestDeployment deploy = new TestDeployment(ns.pathForTestDeployment(testId));
 
 		MoodleTabularReport report = new MoodleTabularReport(testId, deploy);
 		report.handleReport(request, response);
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmReport#isSecurityRestricted()
 	 */
 	@Override
 	public boolean isSecurityRestricted() {
 		return true;
 	}
 
 }
