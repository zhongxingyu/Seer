 package es.amplia.research.maven.protodocbook.cmd;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 
 import lombok.Getter;
 import lombok.Setter;
 
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.plugin.logging.SystemStreamLog;
 
 public abstract class AbstractCmd implements Cmd{
 
 	Log log = new SystemStreamLog();
 	
 	@Getter
 	protected String command;
 	
 	protected LinkedList<String> args;
 	
 	@Setter
 	protected File converterExec;	
 	
 	@Setter
 	protected File baseDir;	
 	
 	@Setter
 	protected File inputDir;
 	
 	@Setter
 	protected File inputFile;
 	
 	@Setter
 	protected File outputDir;
 	
 	@Setter
 	protected File outputFile;
 	
 	protected Map<String, String> env;
 	
 	public AbstractCmd(String cmd) {
 		this.command 	= cmd;
 		this.args 		= new LinkedList<>();
 	}
 	
 	public void addArg(String arg) {
 		this.args.add(arg);
 	}
 	
 	public void newProcess() throws IOException, InterruptedException {
		if (!outputDir.exists()) outputDir.mkdirs();
 		List<String> sentence = new ArrayList<>();
 		sentence.add(command);
 		if ((args != null) && !args.isEmpty()) sentence.addAll(args);
 		log.info(sentence.toString());
 		ProcessBuilder pb = new ProcessBuilder(sentence);
 		pb.directory(baseDir);
 		pb.redirectErrorStream(true);		
 		Process p = pb.start();
 		p.waitFor();
 		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
 		String line = null;
 		while ( (line = br.readLine()) != null) {
 		   if (this.log.isInfoEnabled()) this.log.info(line); 	 
 		}
 	}
 	
 }
