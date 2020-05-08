 package org.tsaikd.java.utils;
 
 import java.lang.reflect.Field;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class ArgParser {
 
 	static Log log = LogFactory.getLog(ArgParser.class);
 	String version = "1.0";
 	Collection<Option> opts = new LinkedList<ArgParser.Option>();
 	Collection<Class<?>> optcSet = new HashSet<Class<?>>();
 	CommandLine cmd = null;
 	int helpWidth = 80;
 
 	static public class Option {
 		String opt = null;
 		String longOpt = null;
 		boolean hasArg = false;
 		String description = null;
 
 		public Option(String opt, String longOpt, boolean hasArg, Object defaultArg, String description) {
 			this.opt = opt;
 			this.longOpt = longOpt;
 			this.hasArg = hasArg;
 
 			String desc;
 			if (description != null) {
 				desc = description;
 			} else {
 				desc = "";
 			}
 
 			if (hasArg) {
 				String confArg = null;
 				try {
 					confArg = ConfigUtils.get(longOpt, defaultArg.toString());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				if (confArg == null) {
 					if (defaultArg == null) {
 						confArg = "";
 					} else {
 						confArg = defaultArg.toString();
 					}
 				}
 
 				if (desc.length() > 0) {
 					desc += "\n";
 				}
 				if (confArg.isEmpty()) {
 					desc += "Default: \"\"";
 				} else {
 					desc += "Default: " + confArg;
 				}
 			}
 			if (desc.length() < 1) {
 				desc = null;
 			}
 			this.description = desc;
 		}
 
 		public Option(String opt, String longOpt, String description) {
 			this.opt = opt;
 			this.longOpt = longOpt;
 			this.description = description;
 		}
 
 	}
 
 	public ArgParser() throws Exception {
 		addOpt(new Option("h", "help", "Show help message"));
 	}
 
 	public ArgParser(String version) throws Exception {
 		addOpt(new Option("h", "help", "Show help message"));
 		this.version = version;
 	}
 
 	public ArgParser(String version, Option[] opts) throws Exception {
 		addOpt(new Option("h", "help", "Show help message"));
 		this.version = version;
 		addOpt(opts);
 	}
 
 	public ArgParser(String version, Class<?> optc) throws Exception {
 		addOpt(new Option("h", "help", "Show help message"));
 		this.version = version;
 		addOpt(optc);
 	}
 
 	public ArgParser(String version, Class<?>[] optDep) throws Exception {
 		addOpt(new Option("h", "help", "Show help message"));
 		this.version = version;
 		addOpt(optDep);
 	}
 
 	public ArgParser addOpt(Option opt) throws Exception {
 		this.opts.add(opt);
 		return this;
 	}
 
 	public ArgParser addOpt(Option[] opts) throws Exception {
 		for (Option opt : opts) {
 			addOpt(opt);
 		}
 		return this;
 	}
 
 	public ArgParser addOpt(Class<?> optc) throws Exception {
 		if (optcSet.contains(optc)) {
 			return this;
 		}
 		optcSet.add(optc);
 
 		Field fopts;
 		ArgParser.Option[] opts;
 
 		try {
 			fopts = optc.getDeclaredField("opts");
 			opts = (ArgParser.Option[]) fopts.get(optc);
 		} catch(Exception e) {
 			e.printStackTrace();
 			opts = null;
 		}
 		if (opts != null) {
 			addOpt(opts);
 		}
 
 		Field foptDep;
 		Class<?>[] optDep2;
 
 		try {
 			foptDep = optc.getDeclaredField("optDep");
 			optDep2 = (Class<?>[]) foptDep.get(optc);
 		} catch(Exception e) {
 			e.printStackTrace();
 			optDep2 = null;
 		}
 		if (optDep2 != null) {
 			addOpt(optDep2);
 		}
 
 		return this;
 	}
 
 	public ArgParser addOpt(Class<?>[] optDep) throws Exception {
 		for (Class<?> optc : optDep) {
 			addOpt(optc);
 		}
 		return this;
 	}
 
 	public ArgParser parse(String[] args) throws Exception {
 		Options options = new Options();
 
 		for (Option opt : opts) {
 			options.addOption(opt.opt, opt.longOpt, opt.hasArg, opt.description);
 		}
 
 		cmd = new PosixParser().parse(options, args);
 
 		if (getCmd().hasOption("h")) {
 			printHelp(options, null);
 			System.exit(0);
 			return this;
 		}
 
 		for (Option opt : opts) {
 			if (!opt.hasArg) {
 				continue;
 			}
 			String key = opt.opt;
 			if (key != null && getCmd().hasOption(key)) {
 				String value = getCmd().getOptionValue(key);
 				ConfigUtils.set(key, value);
 			}
 			key = opt.longOpt;
 			if (key != null && getCmd().hasOption(key)) {
 				String value = getCmd().getOptionValue(key);
 				ConfigUtils.set(key, value);
 			}
 		}
 
 		return this;
 	}
 
 	public void printHelp(Options options, String footer) {
 		HelpFormatter helpFmt = new HelpFormatter();
 		helpFmt.setWidth(getHelpWidth());
		helpFmt.printHelp(ClassUtils.getClassName(false, 1), "Version: "
 			+ version, options, footer, true);
 	}
 
 	public void printHelp(String footer) {
 		Options options = new Options();
 
 		for (Option opt : opts) {
 			options.addOption(opt.opt, opt.longOpt, opt.hasArg, opt.description);
 		}
 
 		printHelp(options, footer);
 	}
 
 	public void printHelp() {
 		printHelp(null);
 	}
 
 	public CommandLine getCmd() {
 		return cmd;
 	}
 
 	public int getHelpWidth() {
 		return helpWidth;
 	}
 
 	public void setHelpWidth(int helpWidth) {
 		this.helpWidth = helpWidth;
 	}
 
 }
