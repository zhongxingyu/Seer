 package models;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import play.Logger;
 import util.FileIterator;
 import util.Utils;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 
 import exception.QuickException;
 
 @XStreamAlias("tcoffee")
 public class TCoffeeCommand extends AbstractShellCommand {
 
 	static final Pattern RESULT_PATTERN = Pattern .compile("^\\s*\\#{4} File Type=(.+)Format=(.+)Name=(.+)$");
 
 	/** The t-coffee arguments */
 	public CmdArgs args;
 
 	private String fInputFileName;
 	
 	/** The default constructor */
 	public TCoffeeCommand() {
 	}
 
 	/** The copy constrcutor */
 	public TCoffeeCommand(TCoffeeCommand that) {
 		super(that);
 		this.args = Utils.copy(that.args);
 	}
 	
 	@Override
 	protected void onInitEnv(Map<String, String> map) {
 		AppProps props = AppProps.instance();
 		
 		File binFolder = props.getBinFolder();
 		File dataFolder = props.getDataFolder();
 		String matrixPath = props.getMatrixPath();
 		
 		/* check that the t-coffee home exists */
 		File tcoffeeFolder = props.getTCoffeeFolder();
 		if( !tcoffeeFolder.exists() ) {
 			if( !tcoffeeFolder.mkdirs() ) {
 				throw new QuickException("Unable to create T-Coffee home folder: '%s'", tcoffeeFolder);
 			};
 		}
 		
 		/* redefine the home folder */
 		map.put("HOME", Utils.getCanonicalPath(dataFolder));
 		
 		/* MAFFT BINARIES location */
 		
 		map.put("MAFFT_BINARIES", Utils.getCanonicalPath(binFolder)); 
 		
 		/* preprend the t-coffee binaries to teh PATH */
 		String path = Utils.getCanonicalPath(binFolder) + File.pathSeparator + System.getenv("PATH");
 		map.put("PATH", path);
 		
 
 		/* t-coffee email */
 		map.put("EMAIL_4_TCOFFEE", "paolo.ditommaso@crg.es");
 
 		/* t-coffee home folder */
 		map.put("DIR_4_TCOFFEE", Utils.getCanonicalPath(tcoffeeFolder));
 		
 		/* t-coffee temp folder */
 		map.put("TMP_4_TCOFFEE", Utils.getCanonicalPath(new File(ctxfolder,"_tmp")));
 		//map.put("DEBUG_TMP_FILE", "1");
 		
 		/* lock dir */
 		File lock = new File(ctxfolder,"_lck");
 		lock.mkdirs();
 		map.put("LOCKDIR_4_TCOFFEE", Utils.getCanonicalPath(lock));
 		
		/* t-coffee cache folder */
		map.put("CACHE_4_TCOFFEE", Utils.getCanonicalPath(new File(ctxfolder,"_cache")));
 		
 		/* m-coffee folder is contained in the main bin path */ 
 		map.put("MCOFFEE_4_TCOFFEE", matrixPath );
 
 		/*
 		 * put the super invocation after to make the command local environment to override 
 		 * this command "global" environment initialization
 		 */
 		super.onInitEnv(map);
 
 		
 	}
 
 	@Override
 	protected void init(CommandCtx ctx) {
 
 		/* an outfile is mandatory required */
 		if( Utils.isEmpty(logfile) ) logfile = "_tcoffee.out.log";
 		if( Utils.isEmpty(errfile) ) errfile = "_tcoffee.err.log";
 		if( Utils.isEmpty(cmdfile) ) cmdfile = "_tcoffee.cmd.txt";
 		if( Utils.isEmpty(envfile) ) envfile = "_tcoffee.env.txt";
 		
 		super.init(ctx);
 
 	}
 
 	@Override
 	protected String onInitCommandLine(String cmdLine) {
 		/*
 		 * get t_coffee absolute path 
 		 */
 		String result = "t_coffee";
 		
 		/* 
 		 * add the specified arguments for t-coffee 
 		 */
 		if (args == null) {
 			return result;
 		}
 		
 		/* 
 		 * detect input file name 
 		 */
 		String inFile = args.get("infile");
 		// fallback on alternate file input argument 
 		if( Utils.isEmpty(inFile)) {
 			inFile = args.get("in");
 			// fallback on alternate file input argument 
 			if( Utils.isEmpty(inFile)) {
 				inFile = args.get("aln");
 			}
 		}
 		
 		if( Utils.isNotEmpty(inFile)) {
 			int p = inFile.indexOf(' ');
 			fInputFileName = (p==-1) ? inFile : inFile.substring(0,p);
 			Logger.debug("t-coffee detected input file name: %s", fInputFileName);
 		}
 		
 		/*
 		 * check if database has been specified for loca blast 
 		 */
 		if( "LOCAL".equals(args.get("blast")) ) {
 			String db = args.get("pdb_db");
 			if( Utils.isEmpty(db) ) {
 				args.put("pdb_db", "${PDB_DB}");
 			} 
 				
 			db = args.get("protein_db");
 			if( Utils.isEmpty(db) ) {
 				args.put("protein_db", "${PROTEIN_DB}");
 			} 
 		}
 		
 		/* 
 		 * check any method has been specified otheriwse remove the parameter 
 		 */
 		String method = args.get("method");
 		if( Utils.isEmpty(method)) {
 			args.remove("method");
 		}
 		else if( method.contains("clustalw")) {
 			Logger.warn("Note: multicore is not supported using 'clustal_xxx' method(s)");
 			args.put("multi_core","no");
 		}
 		
 		/* 
 		 * Always override 'quit' attribute. This is required becase it influence as the stdout stderr are generated 
 		 */
 		String quiet = args.get("quiet");
 		if( quiet != null && !quiet.equals("stdout") ) {
 			Logger.warn("T-Coffee -quiet attribute is not supported. Setting to 'stdout'");
 		}
 		args.put("quiet", "stdout");
 		
 		/* return the command line */
 		return result += " " + args.toCmdLine();
 	}
 
 	/**
 	 * on job termination parse the result file to discover tcoffee output
 	 */
 	@Override
 	protected boolean done(boolean success) {
 
 		/* the result file MUST exists */
 		/* check the result file does not contain an error */
 		if( success && existsLogFile()) {
 			String firstLine = new FileIterator(getLogFile()).iterator().next();
 			success = firstLine != null && !firstLine.contains("ERROR:");
 		}
 		
 		/* add the input file to the result object */
 		if( fInputFileName != null ) {		
 
 			OutItem out = new OutItem(fInputFileName,"input_file");
 			//TODO tis label should be parametrized
 			out.label = "Input sequences";
 			result.add(out);
 		}
 		
 		/* add the command line file to the result object */
 		if( getCmdFile() != null ) {
 			OutItem out = new OutItem(getCmdFile(),"system_file");
 			//TODO this label should be parametrized
 			out.label = "Command line";
 			out.format = "cmdline";
 			result.add(out);
 		}
 		
 		if( success || existsLogFile()) { // <-- note: it is OR condition to force an exception if the job has been processed BUT the out file does not exists
 			
 			/* add at least the tcoffee log file */
 			OutItem out = new OutItem(logfile, "system_file");
 			//TODO tis label should be parametrized
 			out.label = "Log file";
 			result.add(out); 
 			
 			/* add all t-coffee result */
 			result.addAll(parseResultFile(getLogFile()));
 
 		}
 
 		if( !success &&  existsErrFile()) { 
 			
 			/* add at least the tcoffee log file */
 			OutItem out = new OutItem(getErrFile(), "error_file");
 			//TODO tis label should be parametrized
 			out.label = "Error file";
 			result.add(out); 
 			
 		}
 		
 		OutItem html = result.getAlignmentHtml();
 		return success && html != null && html.exists();
 	}
 
 	List<OutItem> parseResultFile(File file) {
 		List<OutItem> result = new ArrayList<OutItem>();
 
 		try {
 			String line;
 			BufferedReader reader = new BufferedReader(new FileReader(file));
 			/* consume until output section is found */
 			while ((line = reader.readLine()) != null && 
 					!"OUTPUT RESULTS".equals(line) && 
 					!"Looking For Sequence Templates:".equals(line)
 			) { /* empty */ }
 
 			/* parse the output items */
 			while ((line = reader.readLine()) != null) {
 				OutItem item = parseForResultItem(line);
 				if (item != null) {
 					result.add(item);
 				}
 			}
 			/* .. and return the list */
 			return result;
 
 		} catch (IOException e) {
 			throw new QuickException(e, "Unable to parse result file: %s", file);
 		}
 	}
 
 	OutItem parseForResultItem(String line) {
 		Matcher matcher = RESULT_PATTERN.matcher(line);
 		if (!matcher.matches()) {
 			return null;
 		}
 
 		String name = matcher.group(3).trim();
 		String type = matcher.group(1).trim();
 		
 		/* handle special exception */
 		if( name == null || name.contains("NOT PRODUCED")) {
 			return null;
 		}
 		
 		OutItem item = new OutItem(name,type);
 		item.format = matcher.group(2).trim();
 
 		return item;
 	}
 	
 }
