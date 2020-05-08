 package fit;
 
 //Copyright (c) 2008 Cunningham & Cunningham, Inc.
 //Released under the terms of the GNU General Public License version 2 or later.
 //Contributed by James Shore with inspiration from Martin Busik
 
 public class Parameters {
 
 	static final String HELP_TEXT = 
 		"usage: java fit.FileRunner [options] input-file output-file\n" +
 		"\n" +
 		"Options\n" +
 		" --encoding=CHARSET     read and write files using specified character set\n"+
 		" --version              print version number\n" + 
 		" --help                 show this help screen\n" +
 		"\n" +
 		"See http://fit.c2.com for further information and to report bugs\n"
 	;
 	
 	static final String VERSION_TEXT =
 		"Fit for Java v1.1+changes\n" +
 		"Conforms to Fit Specification v1.1+changes\n" +
 		"Copyright (C) 2008 Cunningham & Cunningham, Inc.\n" + 
 		"License GPLv2+: GNU GPL version 2 or later\n" +
 		"This is free software: you are free to change and redistribute it.\n" +
 		"There is NO WARRANTY, to the extent permitted by law.\n"
 	;
 	
 	private boolean shouldParseOptions = true;
 	private String input = null;
 	private String output = null;
 	private String encoding = null;
 
 	public String input() { return input; }
 	public String output() { return output; }
 	public String encoding() { return encodingSpecified() ? encoding : System.getProperty("file.encoding"); }
 	public boolean encodingSpecified() { return encoding != null; }
 	public String[] legacyArguments() { return new String[] {input(), output()}; }
 
 	public Parameters(String[] args) throws CommandLineException {
 		for (int i = 0; i < args.length; i++) {
 			String arg = args[i];
 			
 			if (arg.equals("--")) shouldParseOptions = false;
 			else if (arg.startsWith("-") && shouldParseOptions) parseOption(arg);
 			else if (input == null) input = arg;
 			else if (output == null) output = arg;
 			else throw new CommandLineParseException("too many file parameters");
 		}
 		
 		if (input == null) throw new CommandLineParseException("missing input-file and output-file");
 		if (output == null) throw new CommandLineParseException("missing output-file");
 	}
 	
 	private void parseOption(String encodingParm) throws CommandLineException {
 		String name = encodingParm;
 		String value = null;
 		int equalsAt = encodingParm.indexOf('=');
 		if (equalsAt >= 0) {
 			name = encodingParm.substring(0, equalsAt);
 			value = encodingParm.substring(equalsAt + 1);
 		}
 
 		if (name.equals("--encoding")) parseEncoding(value);
 		else if (name.equals("--version")) throw new CommandLineException(VERSION_TEXT);
 		else if (name.equals("--help")) throw new CommandLineException(HELP_TEXT);
 		else throw new CommandLineParseException(name + ": unknown option");
 	}
 	
 	private void parseEncoding(String value) throws CommandLineException {
 		if (value == null || "".equals(value)) throw new CommandLineParseException("--encoding: missing value");
 		if (encodingSpecified()) throw new CommandLineParseException("--encoding: duplicated parameter");
 
 		encoding = value;
 	}

 }
