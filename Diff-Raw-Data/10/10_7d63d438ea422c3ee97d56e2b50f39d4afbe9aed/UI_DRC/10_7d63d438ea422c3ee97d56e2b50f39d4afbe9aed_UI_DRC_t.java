 import java.io.*;
 
 /*
  * This module/library asks questions of given resolver candidates and 
  * reports to texamines answers from a recursive resolver to 
     determine how compliant it is with DNSSEC by asking it a series of 
     question. . 
        Validator
        Partial Validator
        DNSSEC Aware
        Partially DNSSEC Aware 
        Old 
        Not a Resolver 
 
     The partial systems will have a descriptors reflecting their 
     deficiencies: 
         DNAME:  DNAME processing is not working 
 	Permissive: Returns answers that fail validation 
 	Mixed: Answers are inconsistent i.e. not all validatable answers are
 	       reported as validated. 
 	TCP:  TCP queries are not available 
 	SlowBig: Big UDP answers fail but TCP fallback is supported
 	NoBig:   Both big UDP answers and TCP fail
 
  */
 public class UI_DRC {
     static boolean long_report = false;
     static void do_eval(String resolv) {
 	String gr = new DNSSEC_resolver_check().evaluate_resolver(resolv); 
 	//print("testing: " + gr);
 	String tr = new Translator().translate(gr);
 	if (long_report)
 	    System.out.println("Eval: " + resolv + " Tests=" + gr + " Result=" + tr);
 	else 
 	    System.out.println("Result: " + resolv + " " + tr);
     }
 
     public static void 
     main(String [] args) throws Exception {
 	String usage = 
	    "Usage: java UI_DRC.java -[adhlrST] [-m <msg>] [<resolvers>] \n" 
 	    + "     :  -a # Aborts on first error for each resolver\n"  
 	    + "     :  -d # prints lots of debug info\n"
 	    + "     :  -h # prints help and exits\n"  
 	    + "     :  -l # Lists the locally configured resolvers and exits\n" 
 	    + "     :  -r # detailed report on screen\n"
	    + "     :  -S # DO not SUBMIT results\n" 
 	    + "     :  -T # Show compact from of test results\n"
 	    + "     :  -m # A string that gets added to the report an identifier " 
	    + "\n\tExample: starbucks\n" 
	    + "     :    # No resolvers listed, use the configured resolvers\n" 
 	    + "   resolvers can be addresses or names";
 	
 	int num_resolvers = 0; 
 	boolean resolver_evaluated = false;
 	//	String [] list = ResolverConfig.getCurrentConfig().servers(); 
 	String [] list = DNSSEC_resolver_check.get_local_resolvers();
 	int abort = 999999999; 
 	
 	DNSSEC_resolver_check.set_abort(false);
 	DNSSEC_resolver_check.set_submit_report(true);
 	for (num_resolvers = 0; args.length > num_resolvers; num_resolvers++) {
 	    if (args[num_resolvers].equals("-a") )
 		DNSSEC_resolver_check.set_abort(true);
 	    else if (args[num_resolvers].equals("-d"))
 		DNSSEC_resolver_check.set_debug(true);
 	    else if (args[num_resolvers].equals("-r")) {
 		DNSSEC_resolver_check.set_verbose_report(true);
 		long_report = true;
 	    }
 	    else if (args[num_resolvers].equals("-S"))
 		DNSSEC_resolver_check.set_submit_report(false);
 	    else if (args[num_resolvers].equals("-T"))
 		DNSSEC_resolver_check.set_show_test_results(true); 
 	    else if (args[num_resolvers].equals("-m")){ 
 		if( num_resolvers + 1 < args.length ) 
 		    DNSSEC_resolver_check.set_message( args[++num_resolvers]);
 		else 
 		    System.out.println( "-m must be followed by a message");
 	    } else if (args[num_resolvers].equals("-l")) {
 		String msg = "Configured resolvers ";
 		for (int i =0 ; i < list.length; i++)
 		    msg = msg + " " + list[i]; 
 		System.out.println (msg);
 		num_resolvers = abort;
 	    } else if (args[num_resolvers].equals("-h")){
 		System.out.println(usage);
 		num_resolvers = abort; // abort 
 	    } else { 
 		resolver_evaluated = true;
 		do_eval(args[num_resolvers]);
 	    }
 	}
 	if (resolver_evaluated == false && num_resolvers < abort) 
 	    for (int cnt  = 0; cnt < list.length; cnt++) {
 		do_eval(list[cnt]);
 	    }
 	
     }
 }
