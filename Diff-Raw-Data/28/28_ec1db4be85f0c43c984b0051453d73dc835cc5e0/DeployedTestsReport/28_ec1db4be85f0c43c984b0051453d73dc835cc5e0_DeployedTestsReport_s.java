 /**
  * 
  */
 package om.tnavigator.reports.std;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import om.OmException;
 import om.OmUnexpectedException;
 import om.tnavigator.NavigatorServlet;
 import om.tnavigator.TestDeployment;
 import om.tnavigator.reports.*;
 
 import org.w3c.dom.*;
 
 import util.xml.XML;
 import util.xml.XMLException;
 
 /**
  * This report lists the questions that have been deployed on this server, with the
  * available versions in reverse date order.
  */
 public class DeployedTestsReport implements OmReport {
 	private final static Pattern filenamePattern = Pattern.compile("^(.*)\\.deploy\\.xml$");
 	private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 
 	private NavigatorServlet ns;
 
 	/**
 	 * Create an instance of this report.
 	 * @param ns the navigator servlet we belong to.
 	 */
 	public DeployedTestsReport(NavigatorServlet ns) {
 		this.ns = ns;
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmReport#getUrlReportName()
 	 */
 	@Override
 	public String getUrlReportName() {
 		return "alltests";
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmReport#handleReport(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	public void handleReport(String suffix, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		String prefix = request.getParameter("prefix");
 
 		DeployedTestsTabularReport report = new DeployedTestsTabularReport(prefix);
 		report.handleReport(request, response);
 	}
 
 	private static class Test {
 		String deploy;
 		String deployModified;
 		String test;
 		String testModified;
 		boolean open;
 		boolean world;
 		String openDate;
 		String closeDate;
 		String forbidDate;
 		String feedbackDate;
 
 		private Test(String deploy, File deployFile, File testBank) throws OmException {
 			this.deploy = deploy;
 			deployModified = dateFormat.format(deployFile.lastModified());
 			TestDeployment def = new TestDeployment(deployFile);
 			if (def.isSingleQuestion()) {
 				test = "[single question]";
 				testModified = "";
 			} else {
 				test = def.getDefinition();
 				File testFile = new File(testBank, test + ".test.xml");
 				if (testFile.exists()) {
 					testModified = dateFormat.format(testFile.lastModified());
 				} else {
 					testModified = "File missing";
 				}
 			}
 			open = def.isAfterOpen() && !def.isAfterForbid();
 			world = def.isWorldAccess();
 			openDate = def.displayOpenDate();
 			closeDate = def.displayCloseDate();
 			forbidDate = def.displayForbidDate();
 			feedbackDate = def.displayFeedbackDate();
 		}
 
 		private Map<String, String> toRow() {
 			Map<String, String> row = new HashMap<String, String>();
 			row.put("deploy", deploy);
 			row.put("deploy" + HtmlReportWriter.LINK_SUFFIX, "../" + deploy + "/");
 			row.put("lastupdated", deployModified);
 			row.put("test", test);
 			row.put("testmodified", testModified);
 			row.put("open", open ? "Yes" : "No");
 			row.put("world", world ? "Yes" : "No");
 			row.put("opendate", openDate);
 			row.put("closedate", closeDate);
 			row.put("forbiddate", forbidDate);
 			row.put("feedbackdate", feedbackDate);
 			
 			return row;
 		}
 	}
 
 	private class DeployedTestsTabularReport extends TabularReportBase {
 		private final String prefix;
 
 		/**
 		 * Constructor.
 		 * @param prefix optional prefix. If specified, only return questions whose id starts with this prefix.
 		 */
 		public DeployedTestsTabularReport(String prefix) {
 			this.prefix = prefix;
 			batchid = null;
 			if (prefix == null ) {
 				title = "All deployed tests";
 			} else {
 				title = "Deployed test with id " + prefix + "*";
 			}
 			this.ns = DeployedTestsReport.this.ns;
 		}
 
 		/* (non-Javadoc)
 		 * @see om.tnavigator.reports.TabularReportBase#init(javax.servlet.http.HttpServletRequest)
 		 */
 		@Override
 		public List<ColumnDefinition> init(HttpServletRequest request) {
 			List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
 			columns.add(new ColumnDefinition("deploy", "Deploy file"));
 			columns.add(new ColumnDefinition("lastupdated", "Last updated"));
 			columns.add(new ColumnDefinition("test", "Test file"));
 			columns.add(new ColumnDefinition("testmodified", "Test updated"));
 			columns.add(new ColumnDefinition("open", "Open now?"));
 			columns.add(new ColumnDefinition("world", "World accessible?"));
 			columns.add(new ColumnDefinition("opendate", "Open date"));
 			columns.add(new ColumnDefinition("closedate", "Close date"));
 			columns.add(new ColumnDefinition("forbiddate", "Forbid date"));
 			columns.add(new ColumnDefinition("feedbackdate", "Feedback date"));
 			return columns;
 		}
 
 		/* (non-Javadoc)
 		 * @see om.tnavigator.reports.TabularReportBase#generateReport(om.tnavigator.reports.TabularReportWriter)
 		 */
 		@Override
 		public void generateReport(TabularReportWriter reportWriter) {
 			File testBank = ns.getTestbankFolder();
 			File[] deployFiles = testBank.listFiles(new FilenameFilter() {
 				@Override
 				public boolean accept(File dir, String name) {
 					return filenamePattern.matcher(name).matches() && (prefix == null || name.startsWith(prefix));
 				}
 			});
 
 			SortedSet<Test> tests = new TreeSet<Test>(new Comparator<Test>() {
 				@Override
 				public int compare(Test test1, Test test2) {
 					if (test1.open && !test2.open) {
 						return -1;
 					} else if (!test1.open && test2.open) {
 						return 1;
 					} else {
 						return test1.deploy.compareTo(test2.deploy);
 					}
 				}
 			});
 			for (int i = 0; i < deployFiles.length; i++) {
 				File deployFile = deployFiles[i];
 				Matcher m = filenamePattern.matcher(deployFile.getName());
 				m.matches();
 				String deployId = m.group(1);
 				try {
 					tests.add(new Test(deployId, deployFile, testBank));
 				} catch (OmException e) {
 					throw new OmUnexpectedException("Error parsing test " + deployId, e);
 				}
 			}
 
 			for (Test test : tests) {
 				reportWriter.printRow(test.toRow());
 			}
 		}
 		
 		/* (non-Javadoc)
 		 * @see om.tnavigator.reports.TabularReportBase#extraHtmlContent(org.w3c.dom.Element)
 		 */
 		@Override
 		public void extraHtmlContent(Element mainElement) {
 			super.extraHtmlContent(mainElement);
 			try {
 				Document d = mainElement.getOwnerDocument();
 				Element form = XML.getChild(XML.getChild(mainElement, "form"), "p");
 
 				Element label = d.createElement("label");
 				label.setAttribute("for", "prefix-input");
 				XML.setText(label, "Prefix ");
 
 				Element input = d.createElement("input");
 				input.setAttribute("type", "text");
 				input.setAttribute("size", "10");
 				input.setAttribute("name", "prefix");
 				input.setAttribute("id", "prefix-input");
 				if (prefix != null) {
 					input.setAttribute("value", prefix);
 				}
 
 				Node oldFirstChild = form.getFirstChild();
 				form.insertBefore(label, oldFirstChild);
 				form.insertBefore(input, oldFirstChild);
 				form.insertBefore(d.createTextNode(" "), oldFirstChild);
 			} catch (XMLException e) {
 				throw new OmUnexpectedException("Cannot find form element.", e);
 			}
 		}
 	}
 }
