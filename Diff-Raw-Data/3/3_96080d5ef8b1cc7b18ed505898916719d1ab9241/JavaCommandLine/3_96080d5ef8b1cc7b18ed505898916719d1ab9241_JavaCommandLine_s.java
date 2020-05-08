 package de.berlios.statcvs.xml.maven;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import org.apache.maven.plugin.logging.Log;
import org.apache.maven.reporting.MavenReportException;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.CommandLineUtils;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.codehaus.plexus.util.cli.StreamConsumer;
 import org.codehaus.plexus.util.cli.WriterStreamConsumer;
 
 
 public class JavaCommandLine {
 
 	private Commandline cli;
 	private List classPath;
 	private String mainClass;
 	private int returnCode;
 
 	public JavaCommandLine() {
 		cli = new Commandline();
 		classPath = new ArrayList();
 	}
 	
 	public void setWorkingDirectory(File directory) {
 		cli.setWorkingDirectory(directory.getAbsolutePath());
 	}
 	
 	public void setExecutable(String jvm) {
 		cli.setExecutable(jvm);
 	}
 	
 	public void setMainClass(String mainClass) {
 		this.mainClass = mainClass;
 	}
 	
 	public void addClassPath(File file) {
 		classPath.add(file);
 	}
 	
 	public String[] getClassPathArgs()
 	{
 		return new String[] { "-classpath", getClassPathAsString() }; 
 	}
 	
 	public String getClassPathAsString() {
 		StringBuffer sb = new StringBuffer();
 		for (Iterator it = classPath.iterator(); it.hasNext();) {
 			File file = (File)it.next();
 			sb.append(file.getAbsolutePath());
 			if (it.hasNext()) {
 				sb.append(File.pathSeparator);
 			}
 		}
 		return sb.toString();
 	}
 	
 	public Writer run(String[] args) throws CommandLineException {
 		cli.clearArgs();
 		cli.addArguments(getClassPathArgs());
 		cli.addArguments(new String[] { getMainClass() } );
 		cli.addArguments(args);
 		
 		String[] foo = cli.getArguments();
 		for (int i = 0; i < foo.length; i++) {
 			System.out.println(i + ": " + foo[i]);
 		}
 		       
 		Writer stringWriter = new StringWriter();
 		StreamConsumer out = new WriterStreamConsumer(stringWriter);
 		StreamConsumer err = new WriterStreamConsumer(stringWriter);
 
 		returnCode = CommandLineUtils.executeCommandLine(cli, out, err);
 		
 		return stringWriter;
 	}
 	
 	public int getReturnCode() {
 		return returnCode;
 	}
 	
 	public void print(Log logger, Writer writer) {
 		String string = writer.toString();
 		if (string != null && string.length() > 0) {
 			StringReader sr = new StringReader(string);
 			BufferedReader br = new BufferedReader(sr);
 			try {
 				while ((string = br.readLine()) != null) {
 					logger.info(string);
 				}
 			}
 			catch (IOException e) {
 				logger.debug(e);
 			}
 		}
 
 	}
 
 	public String getMainClass() {
 		return mainClass;
 	}
 
     public String[] getCommandline() {
         return cli.getCommandline();
     }
 }
