 package de.uni_koblenz.ist.utilities.revision_task;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 import org.tmatesoft.svn.core.wc.SVNInfo;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.core.wc.SVNWCClient;
 
 public class RetrieveRevision extends Task {
 
 	private enum VersionControl {
 		SVN, HG, NONE;
 	}
 
 	@Override
 	public void execute() {
 		File baseDir = getProject().getBaseDir();
 		File[] ls = baseDir.listFiles();
 		VersionControl control = VersionControl.NONE;
 		for (File currentFile : ls) {
 			if (currentFile.isDirectory()) {
 				if (currentFile.getName().startsWith(".hg")) {
 					control = VersionControl.HG;
 					break;
 				}
 				if (currentFile.getName().startsWith(".svn")) {
 					control = VersionControl.SVN;
 					break;
 				}
 			}
 		}
 		String revisionString = null;
 		switch (control) {
 		case SVN:
 			try {
 				SVNClientManager svnClientManager = SVNClientManager
 						.newInstance();
 				SVNWCClient wcClient = svnClientManager.getWCClient();
 				SVNInfo info = wcClient.doInfo(baseDir, SVNRevision.WORKING);
 				long revision = info.getRevision().getNumber();
 				revisionString = Long.toString(revision);
 			} catch (SVNException e) {
 				throw new BuildException(e);
 			}
 			break;
 		case HG:
 			Runtime rt = Runtime.getRuntime();
 			try {
				Process pr = rt.exec("hg summary", null, new File(
						"../../../mercurial_test/jgralab"));
 				pr.waitFor();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(pr.getInputStream()));
 				String line = "";
 				while (line != null) {
 					line = reader.readLine();
 					if (line != null) {
 						String regex = "^[^:]*:\\p{Space}+([0-9]+:[a-f0-9]+)\\p{Space}*.*$";
 						Pattern p = Pattern.compile(regex);
 						Matcher m = p.matcher(line);
 						if (m.matches()) {
 							revisionString = m.group(1);
 							break;
 						}
 					}
 				}
 			} catch (Exception e) {
 				throw new BuildException(e);
 			}
 			break;
 		case NONE:
 			revisionString = "none";
 		}
 		revisionString = revisionString == null ? "unknown" : revisionString;
 		System.out.println("Revision: " + revisionString);
 		getProject().setNewProperty("revision", revisionString);
 
 	}
 }
