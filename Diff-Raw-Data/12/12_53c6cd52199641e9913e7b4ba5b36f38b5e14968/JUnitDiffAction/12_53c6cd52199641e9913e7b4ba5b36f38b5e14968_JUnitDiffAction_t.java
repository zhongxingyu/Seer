 package hudson.plugins.junit_diff;
 
 import hudson.FilePath;
 import hudson.matrix.MatrixRun;
 import hudson.matrix.MatrixProject;
 import hudson.model.Action;
 import hudson.model.AbstractBuild;
 import hudson.model.Hudson;
 import hudson.model.Job;
import hudson.model.Run;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 public class JUnitDiffAction implements Action {
 
 	public static final String PATH_MAP = "junitDiffMap";
 	public static final String URL_MAP = "junitUrlMap";
 	public static final String RES_FILE_PATH = "resFilePath";
 
 	public final AbstractBuild<?, ?> build;
 	public final String buildId;
 	public final String junitPath;
 	public String errorMsg;
 
 	public JUnitDiffAction(AbstractBuild<?, ?> build) {
 		this.build = build;
 		this.buildId = build.getParent().getDisplayName() + " " + build.getDisplayName();
 		this.junitPath = build.getRootDir().getAbsolutePath() + "/" + JUnitDiff.DATA_DIR;
 		this.errorMsg = "";
 	}
 
 	public void doAddBuild(StaplerRequest req, StaplerResponse rsp) throws IOException {
 		errorMsg = "";
 		Map<String, String> diffMap = (HashMap<String, String>) req.getSession().getAttribute(PATH_MAP);
 		Map<String, String> urlMap = (HashMap<String, String>) req.getSession().getAttribute(URL_MAP);
 		if (diffMap == null) {
 			diffMap = new HashMap<String, String>();
 			urlMap = new HashMap<String, String>();
 		}
 		addBuild(diffMap, urlMap);
 		req.getSession().setAttribute(PATH_MAP, diffMap);
 		req.getSession().setAttribute(URL_MAP, urlMap);
 		// junit list has changed
 		req.getSession().setAttribute(RES_FILE_PATH, null);
 		rsp.sendRedirect2("showList");
 	}
 
 	public void doAddBuildMatrix(StaplerRequest req, StaplerResponse rsp) throws IOException {
 		errorMsg = "";
 		Map<String, String> diffMap = (HashMap<String, String>) req.getSession().getAttribute(PATH_MAP);
 		Map<String, String> urlMap = (HashMap<String, String>) req.getSession().getAttribute(URL_MAP);
 		if (diffMap == null) {
 			diffMap = new HashMap<String, String>();
 			urlMap = new HashMap<String, String>();
 		}
 		MatrixProject mProj = (MatrixProject) build.getParent().getParent();
 		int buildNumber = build.getNumber(); // number of current build
 		for (Job comb : mProj.getAllJobs()) {
 			System.out.println("combination: " + comb.getDisplayName());
			Run<?, ?> run = comb.getBuildByNumber(buildNumber);
			if (run == null) continue; // Combination was not run in this build
			JUnitDiffAction action = run.getAction(JUnitDiffAction.class);
 			if (action != null) {
 				action.addBuild(diffMap, urlMap);
 			}
 		}
 		req.getSession().setAttribute(PATH_MAP, diffMap);
 		req.getSession().setAttribute(URL_MAP, urlMap);
 		// junit list has changed
 		req.getSession().setAttribute(RES_FILE_PATH, null);
 		rsp.sendRedirect2("showList");
 	}
 
 	public void doRemoveBuild(StaplerRequest req, StaplerResponse rsp) throws IOException {
 		errorMsg = "";
 		System.out.println("Odebiram build " + buildId);
 		Map<String, String> diffMap = (HashMap<String, String>) req.getSession().getAttribute(PATH_MAP);
 		Map<String, String> urlMap = (HashMap<String, String>) req.getSession().getAttribute(URL_MAP);
 		removeBuild(diffMap, urlMap);
 		req.getSession().setAttribute(PATH_MAP, diffMap);
 		req.getSession().setAttribute(URL_MAP, urlMap);
 		// junit list has changed
 		req.getSession().setAttribute(RES_FILE_PATH, null);
 		rsp.sendRedirect2("showList");
 	}
 
 	public void doJunitDiff(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
 		errorMsg = "";
 		Map<String, String> diffMap = (HashMap<String, String>) req.getSession().getAttribute(PATH_MAP);
 		if (diffMap != null) {
 			if (diffMap.size() < 2) {
 				errorMsg = "To run JUnit Diff properly, you have to select at least two different builds, currecntly you have selecte only "
 						+ diffMap.size();
 			} else {
 				req.getSession().setAttribute(RES_FILE_PATH, runJunitDiff(diffMap));
 			}
 		} else {
 			errorMsg = "There are no selected build in your session! Please select at least two.";
 		}
 		// rsp.forward(this, "showDiff", req);
 		rsp.sendRedirect2("showDiff");
 	}

 	public void doDownload(StaplerRequest req, StaplerResponse rsp) throws IOException, InterruptedException,ServletException{
 		errorMsg = "";
 		String file = (String)req.getSession().getAttribute(RES_FILE_PATH);
 		if(file==null){
 			Map<String, String> diffMap = (HashMap<String, String>) req.getSession().getAttribute(PATH_MAP);
 			if(diffMap==null){
 				errorMsg = "There are no selected build in your session! Please select at least two.";
 				rsp.forwardToPreviousPage(req);
 				return;
 			} else {
 				if (diffMap.size() < 2) {
 					errorMsg = "To run JUnit Diff properly, you have to select at least two different builds, currecntly you have selecte only "
 							+ diffMap.size();
 					rsp.forwardToPreviousPage(req);
 					return;
 				}
 			}
 			file = runJunitDiff(diffMap);
 		}
 		FilePath path = new FilePath(new File(file));
 		rsp.setContentType("application/zip");
 		path.zip(rsp.getOutputStream());
 	}
 
 	public String readJunitDiffResult(String filePath) throws IOException {
 		// attribute on session is not set up - will show nothing, error msg should be setup doJunitDiff
 		if (filePath == null) {
 			return "";
 		}
 		StringBuilder text = new StringBuilder();
 		Scanner scanner = new Scanner(new FileInputStream(filePath));
 		try {
 			while (scanner.hasNextLine()) {
 				text.append(scanner.nextLine());
 				text.append("\n");
 			}
 		} finally {
 			scanner.close();
 		}
 		return text.toString();
 	}
 
 	public boolean isMatrixBuild() {
		return (build instanceof MatrixRun);
 	}
 
 	private void addBuild(Map<String, String> diffMap, Map<String, String> urlMap) {
 		// diffMap and urlMap never null
 		if (!diffMap.containsKey(buildId)) {
 			diffMap.put(buildId, junitPath);
 			urlMap.put(buildId, build.getUrl());
 		}
 	}
 
 	private void removeBuild(Map<String, String> diffMap, Map<String, String> urlMap) {
 		if (diffMap != null) {
 			if (diffMap.containsKey(buildId)) {
 				diffMap.remove(buildId);
 				urlMap.remove(buildId);
 			}
 		}
 	}
 
 	private String runJunitDiff(Map<String, String> diffMap) throws IOException {
 		System.out.println("Provadim JUnit diff...");
 		JUnitDiff.DescriptorImpl desc = (JUnitDiff.DescriptorImpl) Hudson.getInstance().getDescriptor(
 				JUnitDiff.class.getName());
 		String jdPath = desc.getJunitDiffPath();
 		File tmpFile = File.createTempFile("junitDiffRes_", ".html", null);
 		// tmpFile.deleteOnExit();
 		String tmpFilePath = tmpFile.getAbsolutePath();
 		System.out.println("Tmp file je: " + tmpFilePath);
 		System.out.println("Desc je: " + desc);
 		String command = "java -jar " + jdPath + " -o " + tmpFilePath;
 		for (String path : diffMap.values()) {
 			command += " " + path;
 		}
 		try {
 			System.out.println("Command is " + command);
 			Process p = Runtime.getRuntime().exec(command);
 			p.waitFor();
 
 			System.out.println("=== output begin ===");
 			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			String line = reader.readLine();
 			while (line != null) {
 				System.out.println(line);
 				line = reader.readLine();
 			}
 			System.out.println("=== output end ===");
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return tmpFilePath;
 	}
 
 	public String getIconFileName() {
 		return "/plugin/junit-diff/img/mail-send-receive.png";
 	}
 
 	public String getDisplayName() {
 		return "JUnit Diff";
 	}
 
 	public String getUrlName() {
 		return "junit-diff";
 	}
 
 }
