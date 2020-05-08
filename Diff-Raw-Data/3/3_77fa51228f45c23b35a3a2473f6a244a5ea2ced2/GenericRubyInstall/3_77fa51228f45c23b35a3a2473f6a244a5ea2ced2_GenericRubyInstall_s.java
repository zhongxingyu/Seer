 package org.eclipse.dltk.ruby.internal.launching;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.launching.AbstractInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.IInterpreterRunner;
 import org.eclipse.dltk.ruby.launching.RubyLaunchingPlugin;
 import org.osgi.framework.Bundle;
 
 public class GenericRubyInstall extends AbstractInterpreterInstall {
 	
 	public class BuiltinsHelper {
 		public BuiltinsHelper() {
 
 		}
 		
 		protected void storeFile(File dest, URL url) throws IOException {
 			InputStream input = null;
 			OutputStream output = null;
 			try {
 				input = new BufferedInputStream(url.openStream());
 
 				output = new BufferedOutputStream(new FileOutputStream(dest));
 
 				// Simple copy
 				int ch = -1;
 				while ((ch = input.read()) != -1) {
 					output.write(ch);
 				}
 			} finally {
 				if (input != null) {
 					input.close();
 				}
 
 				if (output != null) {
 					output.close();
 				}
 			}
 		}
 		
 		protected File storeToMetadata(Bundle bundle, String name, String path)
 		throws IOException {
 			File pathFile = DLTKCore.getDefault().getStateLocation().append(name)
 					.toFile();
 			storeFile(pathFile, FileLocator.resolve(bundle.getEntry(path)));
 			return pathFile;
 		}
 
 
 		public String execute(String command) {
 			Bundle bundle = RubyLaunchingPlugin.getDefault().getBundle();
 			File builder;
 			try {
 				builder = storeToMetadata(bundle, "builtin.rb", "scripts/builtin.rb");
 			} catch (IOException e1) {
 				e1.printStackTrace();
 				return null;
 			}
 
 			String[] cmdLine = new String[] { GenericRubyInstall.this.getInstallLocation().getAbsolutePath(),
 					builder.getAbsolutePath(), command};
 
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
 						sb.append(line + "\n");						
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
 
 	public GenericRubyInstall(IInterpreterInstallType type, String id) {
 		super(type, id);
 	}
 
 	public IInterpreterRunner getInterpreterRunner(String mode) {
 		return new RubyInterpreterRunner(this);
 	}
 	
 	
 	private static final String prefix = "#### DLTK RUBY BUILTINS ####";
 	private static final int prefixLength = prefix.length();
 	private HashMap sources = null;
 	
 	private void initialize () {
 		sources = new HashMap();
 		String content = new BuiltinsHelper().execute("");
 		int nl;
 		int start = 0;
 		int pos = content.indexOf(prefix, start);
 		while (pos >= 0) {
 			nl = content.indexOf('\n', pos);
 			String filename = content.substring(pos + prefixLength, nl).trim();
 			String data = "";
 			pos = content.indexOf(prefix, nl + 1);
 			if (pos != -1)
 				data = content.substring(nl + 1, pos);
 			else 
 				data = content.substring(nl + 1);
 			sources.put(filename, data);
 		}
 	}
 
 	public String getBuiltinModuleContent(String name) {
 		if (sources == null)
 			initialize();
 		return (String) sources.get(name);
 	}
 
 	public String[] getBuiltinModules() {
 		if (sources == null)
 			initialize();
 		return (String[]) sources.keySet().toArray(new String[sources.size()]);
 	}
 	
 }
