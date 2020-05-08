 package jason.infra.centralised;
 
 import jason.asSemantics.TransitionSystem;
 import jason.jeditplugin.Config;
 import jason.jeditplugin.MASLauncherInfraTier;
 import jason.jeditplugin.RunProjectListener;
 import jason.mas2j.MAS2JProject;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 
 public class CentralisedMASLauncherAnt implements MASLauncherInfraTier {
 
 	protected MAS2JProject project;
 	protected RunProjectListener listener;
 
 	protected boolean stop = false;
 
 	protected Process masProcess = null;
 	protected OutputStream processOut;
 	
 	public void setProject(MAS2JProject project) {
 		this.project = project;
 	}
 	
 	public void setListener(RunProjectListener listener) {
 		this.listener = listener;		
 	}
 
 	public void run() {
 		try {
 			String[] command = getStartCommandArray();
 			
 			String cmdstr = command[0];
 			for (int i=1; i<command.length; i++) {
 				cmdstr += " "+command[i];
 			}
 			System.out.println("Executing MAS with " + cmdstr);
 			
			masProcess = Runtime.getRuntime().exec(command, null,	new File(project.getDirectory()));
 
 			BufferedReader in = new BufferedReader(new InputStreamReader(masProcess.getInputStream()));
 			BufferedReader err = new BufferedReader(new InputStreamReader(	masProcess.getErrorStream()));
 			processOut = masProcess.getOutputStream();
 
 			Thread.sleep(300);
 			stop = false;
 			while (!stop) {// || saciProcess!=null) {
 				while (!stop && in.ready()) {
 					System.out.println(in.readLine());
 				}
 				while (!stop && err.ready()) {
 					System.out.println(err.readLine());
 				}
 				Thread.sleep(250); // to not consume cpu
 				
 				try {
 					masProcess.exitValue();
 					// no exception when the process has finished
 					stop = true;
 				} catch (Exception e) {}
 			}
 		} catch (Exception e) {
 			System.err.println("Execution error: " + e);
 			e.printStackTrace();
 		} finally {
 			if (listener != null) {
 				listener.masFinished();
 			}
 		}
 	}
 
 	public void stopMAS() {
 		try {
 			if (processOut != null) {
 				processOut.write(1);//"quit"+System.getProperty("line.separator"));
 			}
 
 			if (masProcess != null) {
 				masProcess.destroy();
 			}
 		} catch (Exception e) {
 			System.err.println("Execution error: " + e);
 			e.printStackTrace();
 		} finally {
 			stop = true;			
 		}
 	}
 
 	/** returns the operating system command that runs the MAS */
 	public String[] getStartCommandArray() {
 		String build = "build.xml";
 		if (hasCBuild()) build = "c-build.xml";
 		return  new String[] {
 					Config.get().getJavaHome()+"bin"+File.separator+"java",
 					"-classpath", 
 					//Config.get().getAntLib()+ "ant.jar"  + File.pathSeparator + 
 					Config.get().getAntLib()+ "ant-launcher.jar",
 				    "org.apache.tools.ant.launch.Launcher",
 				    "-e",
 				    "-f", build 
 				 };
 	}
 
 	/** write the scripts necessary to run the project */	
 	public void writeScripts(boolean debug) {
 		if (hasCBuild()) {
 			System.out.println("The build.xml file is not being created, the user c-build.xml file is used instead.");
 			return; // if the user has a c-build.xml file, use his build
 		}
 		
 		try {
 			String nl = System.getProperty("line.separator");
 			// get template
 			BufferedReader in;
 			
 			// if there is jason/src/xml/build-template.xml, use it; otherwise use the file in jason.jar
 			File bt = new File("src/xml/build-template.xml");
 			if (bt.exists()) {
 				in = new BufferedReader(new FileReader(bt));				
 			} else {
 				bt = new File("../src/xml/build-template.xml");
 				if (bt.exists()) {
 					in = new BufferedReader(new FileReader(bt));
 				} else {
 					in = new BufferedReader(new InputStreamReader(TransitionSystem.class.getResource("/xml/build-template.xml").openStream()));
 				}
 			}
 			
 			StringBuffer scriptBuf = new StringBuffer();
 			String line = in.readLine();
 			while (line != null) {
 				scriptBuf.append(line + nl);
 				line = in.readLine();
 			}
 			String script = scriptBuf.toString();
 			
 			// replace <....> 
 			script = replaceMarks(script, debug);
 
 			// write the script
 			FileWriter out = new FileWriter(project.getDirectory() + "build.xml");
 			out.write(script);
 			out.close();
 
 		} catch (Exception e) {
 			System.err.println("Could not write start script for project " + project.getSocName());
 			e.printStackTrace();
 		}
 	}
 
 	protected String replaceMarks(String script, boolean debug) {
 			script = replace(script, "<PROJECT-ID>", project.getSocName());
 			
 			String dDir = project.getDirectory();
 			if (dDir.endsWith(File.separator+".")) {
 				dDir = dDir.substring(0, dDir.length() - 2);
 			}
 			if (dDir.endsWith(File.separator)) {
 				dDir = dDir.substring(0, dDir.length() - 1);
 			}
 			script = replace(script,"<PROJECT-DIR>", dDir);
 			script = replace(script,"<PROJECT-FILE>", project.getProjectFile().getName());
 			
 			script = replace(script,"<JASON-JAR>", Config.get().getJasonJar());
 
 			// add lib/*.jar
 			String lib = "";
 			if (new File(dDir + File.separator + "lib").exists()) {
 				lib = "<fileset dir=\"${basedir}/lib\" >  <include name=\"*.jar\" /> </fileset>";
 			}
 			script = replace(script,"<PATH-LIB>", lib);
 
 			script = replace(script,"<PROJECT-RUNNER-CLASS>", jason.infra.centralised.RunCentralisedMAS.class.getName());
 			String sDebug = "";
 			if (debug) {
 				sDebug = " -debug";
 			}
 			script = replace(script,"<DEBUG>", sDebug);
 			script = replace(script,"<OTHER-TASK>", "");
 			return script;
 	}
 
 	protected String replace(String s, String p, String n) {
 		int i = s.indexOf(p);
 		if (i >= 0) {
 			s = s.substring(0,i) + n + s.substring(i+p.length()); 
 		}
 		return s;
 	}
 	
 	protected boolean hasCBuild() {
 		return new File(project.getDirectory()+ "c-build.xml").exists();
 	}
 	
 }
