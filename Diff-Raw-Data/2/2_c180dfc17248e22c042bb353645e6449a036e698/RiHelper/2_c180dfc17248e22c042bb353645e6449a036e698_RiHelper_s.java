 package org.eclipse.dltk.ruby.internal.ui.docs;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.dltk.launching.DLTKLaunchUtil;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.ruby.core.RubyNature;
 
 public class RiHelper {
 	private static RiHelper instance;
 
 	public static RiHelper getInstance() {
 		if (instance == null) {
 			instance = new RiHelper();
 		}
 
 		return instance;
 	}
 
 	protected RiHelper() {
 
 	}
 
 	public String getDocFor(String keyword) {
 		IInterpreterInstall install = DLTKLaunchUtil
				.getDefaultInterpreter(RubyNature.NATURE_ID);
 
 		File interpreterPath = install.getInstallLocation();
 
 		String[] cmdLine = new String[] { interpreterPath.toString(),
 				interpreterPath.getParent() + File.separator + "ri",
 				"--format", "html", keyword };
 
 		BufferedReader input = null;
 		OutputStreamWriter output = null;
 
 		try {
 			try {
 				Process process = DebugPlugin.exec(cmdLine, null);
 
 				input = new BufferedReader(new InputStreamReader(process
 						.getInputStream()));
 
 				StringBuffer sb = new StringBuffer();
 				String line = null;
 				while ((line = input.readLine()) != null) {
 					sb.append(line);
 					sb.append('\n');
 				}
 
 				return sb.toString();
 			} finally {
 				if (output != null) {
 					output.close();
 				}
 
 				if (input != null) {
 					input.close();
 				}
 			}
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 }
