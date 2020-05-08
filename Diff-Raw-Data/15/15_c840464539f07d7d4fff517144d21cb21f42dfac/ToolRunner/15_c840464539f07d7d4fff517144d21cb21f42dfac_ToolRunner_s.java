 package fmv;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import javax.swing.SwingWorker;
 
 import fmv.tools.BasicTool;
 import fmv.tools.Compiler;
 import fmv.tools.Interpreter;
 import fmv.tools.Tools;
 
 public class ToolRunner extends SwingWorker<Boolean, String> {
 
 	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
 			"HH:mm:ss.SSS");
 
 	private Source root;
 
 	private String tempDir;
 
 	private String sourceDir;
 
 	private String toolName;
 
 	private ToolRunnerDialog dialog;
 
 	private String[] tests = new String[] { "EasyTests", "AllTests" };
 
 	private String testZip;
 
 	public ToolRunner(Source root, ToolRunnerDialog dialog, String toolName) {
 		this.root = root;
 		this.dialog = dialog;
 		this.toolName = toolName;
 		tempDir = System.getProperty("java.io.tmpdir");
 		if (!tempDir.endsWith(File.separator)) {
 			tempDir = tempDir + File.separator;
 		}
 		sourceDir = tempDir + "src";
 	}
 	public ToolRunner(Source root, ToolRunnerDialog dialog, String toolName, String testZip) {
 		this(root, dialog, toolName);
 		removeDirs(new File(sourceDir));
 		this.testZip = testZip;
 	}
 
 	private void removeDirs(File file) {
 		if (file.isDirectory()) {
 			for (File f : file.listFiles()) {
 				removeDirs(f);
 			}
 		} else if (!file.getName().equals("tpgen.jar")) {
 			file.delete();
 		}
 	}
 
 	@SuppressWarnings("resource")
 	private void prepareTestdir() {
 		if (FMV.prefs.getRemoveSrc()) {
 			System.out.println("deleting dirs");
 			removeDirs(new File(sourceDir));
 		}
 		try {
 			ZipFile z = new ZipFile(testZip);
 			Enumeration<? extends ZipEntry> zz = z.entries();
 			while (zz.hasMoreElements()) {
 				ZipEntry e = zz.nextElement();
 				if (e.isDirectory()) {
 					continue;
 				}
 				String toName = sourceDir + File.separator + e.getName();
 				File toFile = new File(toName);
 				toFile.getParentFile().mkdirs();
 				toFile.createNewFile();
 				InputStream from = z.getInputStream(e);
 				FileOutputStream to = new FileOutputStream(toFile);
 				byte[] buffer = new byte[65536];
 				int bytesRead;
 				while ((bytesRead = from.read(buffer)) != -1) {
 					to.write(buffer, 0, bytesRead);
 				}
 				to.close();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected Boolean doInBackground() throws Exception {
 		publish("Initializing");
 		boolean ret;
 		if (toolName.equals("Tests")) {
 			ret = executeTests();
 		} else {
 			ret = executeOther();
 		}
 		return ret;
 	}
 
 	private boolean executeOther() {
 		Set<Date> dates = root.getKeys();
 		int i = 1, n = dates.size();
 		BasicTool tool = Tools.getTool(toolName);
 		for (Date d : dates) {
 			if (isCancelled()) {
 				return false;
 			}
 			String df = "(" + i + "/" + n + ") " + dateFormat.format(d);
 			File file = root.unpack(d, sourceDir);
 			if(file == null){
 				return false;
 			}
 			File workDir = new File(tempDir);
 			publish(df + ": running " + tool);
 			Pair p = tool.run(workDir, file.getParentFile().getAbsolutePath());
 			System.out.println(p.outPut());
 			root.setReport(d, toolName, p.outPut());
 			setProgress((i++ * 100) / n);
 		}
 		return true;
 	}
 
 	private boolean executeTests() {
 		prepareTestdir();
 		Set<Date> dates = root.getKeys();
 		int i = 1, n = dates.size();
 		Compiler compiler = new Compiler(FMV.prefs.getCompilerCmd());
		compiler.configure(new String[] { "-cp", sourceDir });
 		Interpreter interpreter = new Interpreter(FMV.prefs.getInterpreterCmd());
		interpreter.configure(new String[] { "-cp", sourceDir,
				"org.junit.runner.JUnitCore" });
 		for (Date d : dates) {
 			if (isCancelled()) {
 				return false;
 			}
 			String df = "(" + i + "/" + n + ") " + dateFormat.format(d);
 			root.unpack(d, sourceDir);
 			System.out.println(tempDir);
 			File workDir = new File(sourceDir);
 			runTests(d, df, compiler, interpreter, workDir);
 			setProgress((i++ * 100) / n);
 		}
 		return true;
 	}
 
 	private void runTests(Date date, String df, Compiler compiler,
 			Interpreter interpreter, File workDir) {
 		Pair p;
 		for (String test : tests) {
 			publish(df + ": compiling " + test);
 			p = compiler.run(workDir, sourceDir + File.separator + "testing"
 					+ File.separator + test + ".java");
 			if (p.hasError()) {
 				root.setStatus(date, Status.NOCOMPILE, p.outPut());
 				FMV.log(ToolRunner.class.getName(), p.outPut());
 				return;
 			}
 		}
 		for (String test : tests) {
 			publish(df + ": running " + test);
 			p = interpreter.run(workDir, "testing." + test);
 			if (p.hasError()) {
 				if (p.outPut().contains("Out of time")) {
 					root.setStatus(date, Status.TIMEOUT, p.outPut());
 				} else if (p.outPut().contains("Errors: 0")) {
 					root.setStatus(date, Status.E_FAIL, p.outPut());
 				} else {
 					root.setStatus(date, Status.E_ERRS, p.outPut());
 				}
 				return;
 			}
 			FMV.log(ToolRunner.class.getName(), p.outPut());
 			root.setStatus(date, Status.OK, p.outPut());
 		}
 	}
 
 	public void process(List<String> messages) {
 		for (String m : messages) {
 			dialog.setMessage(m);
 		}
 	}
 
 }
