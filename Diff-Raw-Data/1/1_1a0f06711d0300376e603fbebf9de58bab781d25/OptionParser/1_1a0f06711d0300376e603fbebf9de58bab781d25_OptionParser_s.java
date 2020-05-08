 package crono;
 
 public class OptionParser {
     public enum OptionType {
 	SHORT_OPTION,
 	LONG_OPTION,
 	NOT_AN_OPTION,
     }
     
     public String optopt;
     public String optchar;
     
     private String[] args;
     private int current, subarg;
     private boolean done;
     
     public OptionParser(String[] args) {
 	this.args = args;
 	this.current = 0;
 	this.subarg = -1;
 	this.optopt = null;
 	this.optchar = null;
 	this.done = false;
     }
     
     private Option match(Option[] opts, String longopt) {
 	for(int i = 0; i < opts.length; ++i) {
 	    if(longopt.equals(opts[i].longopt)) {
 		return opts[i];
 	    }
 	}
 	return null;
     }
     
     private Option match(Option[] opts, char shortopt) {
 	for(int i = 0; i < opts.length; ++i) {
 	    if(shortopt == opts[i].shortopt) {
 		return opts[i];
 	    }
 	}
 	return null;
     }
     
     private boolean optstart(char ch) {
 	return (ch == '-' || ch == '/');
     }
     
     private OptionType opttype(String option) {
 	int optlen = option.length();
 	char initial = (optlen > 1) ? option.charAt(0) : '\0';
 	if(optstart(initial)) {
 	    if(optlen > 2 && option.charAt(1) == initial) {
 		return OptionType.LONG_OPTION;
 	    }
 	    return OptionType.SHORT_OPTION;
 	}
 	return OptionType.NOT_AN_OPTION;
     }
     
     public int getopt(Option[] options) {
 	if(done || current >= args.length) {
 	    return -1;
 	}
 	
 	this.optopt = null;
 	this.optchar = null;
 	
 	int arglen = args[current].length();
 	if(subarg == -1) {
 	    if("--".equals(args[current]) || "//".equals(args[current])) {
 		System.err.println("Found stream stop symbol");
 		optchar = args[current];
 		current++;
 		return -1;
 	    }
 	    
 	    switch(opttype(args[current])) {
 	    case SHORT_OPTION:
 		System.err.println("Found short option stream");
 		subarg = 1;
 		break;
 	    case LONG_OPTION:
 		System.err.println("Found long option");
 		String optstr = args[current].substring(2);
 		Option opt = match(options, optstr);
 		if(opt != null) {
 		    if(opt.arg) {
 			if(current < args.length - 1) {
 			    current++;
 			    optopt = args[current];
 			}
 		    }
 		    
 		    current++;
 		    optchar = optstr;
 		    return ((int)(opt.shortopt));
 		}
 		
 		optchar = optstr;
 		current++;
 		done = true; /*< End parsing */
 		return ((int)'?');
 	    default:
 		optchar = args[current].substring(1);
		current++;
 		done = true;
 		return -1;
 	    }
 	}
 	
 	char optionchar = args[current].charAt(subarg);
 	System.err.printf("Matching option %c ... ", optionchar);
 	Option opt = match(options, optionchar);
 	if(opt != null) {
 	    System.err.println("Matched");
 	    if(opt.arg) {
 		/* End of option stream, take whole next argument */
 		if(subarg == arglen - 1) {
 		    current++;
 		    if(current < args.length) {
 			optopt = args[current];
 		    }
 		}else {
 		    optopt = args[current].substring(subarg + 1);
 		}
 		subarg = -1;
 		current++;
 	    }else if(subarg == arglen - 1) {
 		subarg = -1;
 		current++;
 	    }else {
 		subarg++;
 	    }
 	    
 	    System.err.printf("Returning %d(%c)\n", ((int)(opt.shortopt)),
 			      opt.shortopt);
 	    return ((int)(opt.shortopt));
 	}
 	System.err.printf("Failed");
 	
 	optchar = "" + optionchar;
 	current++;
 	done = true;
 	return ((int)'?');
     }
     
     public int optind() {
 	return current;
     }
 }
