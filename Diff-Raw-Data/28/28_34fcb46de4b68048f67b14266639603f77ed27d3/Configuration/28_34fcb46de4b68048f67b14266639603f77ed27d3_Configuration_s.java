 package org.protege.owl.server.deploy;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.net.URL;
 import java.util.EnumMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 public class Configuration {
 	public static final String ENCODING = "UTF-8";
 
 	public static final String UNIX_SCRIPTS = "unix-scripts.zip";
 	public static final String SERVER_DISTRIBUTION = "protege-owl-server.zip";
 
 	public static final String PREFIX_TO_REMOVE_FROM_DISTRO = "server";
 	
 	private Map<Parameter, String> parameterMap;
 	private OperatingSystem operatingSystem;
 
 	
 	public Configuration(String sandboxUser, String hostname, String javacmd, int memoryMb) {
 		setupParameterMap(sandboxUser, hostname, javacmd, memoryMb);
 	}
 	
 	private void setupParameterMap(String sandboxUser, String hostname, String javacmd, int memoryMb) {
 		parameterMap = new EnumMap<Parameter, String>(Parameter.class);
 		setupCommonParameterMappings(sandboxUser, hostname, javacmd, memoryMb);
 		operatingSystem=OperatingSystem.detectOperatingSystem();
 		switch (operatingSystem) {
 		case LINUX:
 			setupLinuxParameterMap();
 			break;
 		case WINDOWS_32_BIT:
 		case WINDOWS_64_BIT:
 			setupWindowsParameterMap();
 			break;
 		case OS_X:
 			setupOSXParameterMap();
 			break;
 		default:
 			throw new IllegalStateException("Programmer missed a case");
 		}
 	}
 	
 	private void setupCommonParameterMappings(String sandboxUser, String hostname, String javacmd, int memoryMb) {
 		parameterMap.put(Parameter.SANDBOX_USER, sandboxUser);
 		parameterMap.put(Parameter.HOSTNAME, hostname);
 		parameterMap.put(Parameter.JAVA_CMD, javacmd);
 		parameterMap.put(Parameter.MEMORY_IN_MB, "" + memoryMb + "M");
 	}
 	
 	private void setupLinuxParameterMap() {
 		parameterMap.put(Parameter.SERVER_PREFIX, "/usr/local/protege.server");
 		parameterMap.put(Parameter.DATA_PREFIX, "/var/protege.data");
 		parameterMap.put(Parameter.LOG_PREFIX, "/var/log/protege");
 	}
 	
 	private void setupWindowsParameterMap() {
 		parameterMap.put(Parameter.SERVER_PREFIX, "C:/Program Files/Protege OWL Server");
 		parameterMap.put(Parameter.DATA_PREFIX, "C:/ProgramData/Protege OWL Server");
 		parameterMap.put(Parameter.LOG_PREFIX, "C:/ProgramData/Protege OWL Server/logs");
 	}
 	
 	private void setupOSXParameterMap() {
 		parameterMap.put(Parameter.SERVER_PREFIX, "/usr/local/protege.server");
 		parameterMap.put(Parameter.DATA_PREFIX, "/var/protege.data");
 		parameterMap.put(Parameter.LOG_PREFIX, "/var/log/protege");
 	}
 	
 	public String getParameterValue(Parameter p) {
 		return parameterMap.get(p);
 	}
 	
 	public Map<Parameter, String> getParameterMap() {
 		return parameterMap;
 	}
 	
 	public OperatingSystem getOperatingSystem() {
 		return operatingSystem;
 	}
 	
 	public void copyWithReplacements(URL input, File output) throws IOException {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(input.openStream(), ENCODING));
 		Writer writer = new OutputStreamWriter(new FileOutputStream(output));
 		try {
 			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
 				for (Parameter p : Parameter.values()) {
					line = line.replaceAll(Pattern.quote("@" + p.getName() + "@"), parameterMap.get(p));
 				}
 				writer.write(line);
 				writer.write('\n');
 			}
 		}
 		finally {
 			reader.close();
 			writer.close();
 		}
 	}
 	
 	public void performReplacements(File f) throws IOException {
 		File tmpFile = File.createTempFile("Deployer", ".tmp");
 		copyWithReplacements(f.toURI().toURL(), tmpFile);
 		f.delete();
 		tmpFile.renameTo(f);
 	}
 	
 	public void performReplacementsInDir(File dir) throws IOException {
 		for (File f : dir.listFiles()) {
 			performReplacements(f);
 		}
 	}
 
 }
