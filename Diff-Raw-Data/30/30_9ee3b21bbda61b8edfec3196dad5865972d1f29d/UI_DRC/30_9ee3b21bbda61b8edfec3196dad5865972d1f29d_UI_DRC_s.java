 import java.io.*;
 import java.net.*;
 import org.xbill.DNS.*;
 //import Translator.*;
 /*
 * This module/library asks questions of a given resolver candiates and 
  * reports to texamines answers from a recursive resolver to 
     determine how compliant it is with DNSSEC by asking it a series of 
     question. . 
        Validator
        Partial Validator
        DNSSEC Aware
       Parially DNSSEC Aware 
        Old 
        Not a Resolver 
 
     The partial systems will have a descriptors reflecting their 
     deficiencies: 
         DNAME:  DNAME processing is not working 
 	Permissive: Returns answers that fail validation 
	Mixed: Answers are inconsitant i.e. not all validatable answers are
 	       reported as validated. 
 	TCP:  TCP queries are not available 
	SlowBig: Big UDP answers fail but TCP fallback is suppored
 	NoBig:   Both big UDP answers and TCP fail
 
  */
 public class UI_DRC extends DNSSEC_resolver_check {
 
     static void do_eval(String resolv) {
 	String gr = evaluate_resolver(resolv); 
	//	print("testing: " + gr);
 	String tr = new Translator().translate(gr);
 	print("Eval: " + resolv + " " + gr + " " + tr);
     }
 
     public static void 
     main(String [] args) throws Exception {
 	String usage = 
 	    "Usage: java -jar DNSSEC-resolver-check.jar -[ahdrS] <resolvers> \n" 
 	    + "     :  -a # Aborts on first error for each resolver\n"  
 	    + "     :  -d # prints lots of debug info\n"
 	    + "     :  -h # prints help and exits\n"  
 	    + "     :  -l # Lists the locally configured resolvers and exits\n" 
 	    + "     :  -r # detailed report on screen\n"
 	    + "     :  -T # Show compact from of test results\n"
 	    + "     :  -m # A string that gets added to the report an identifier " 
 	    + "or something like that\n\tExample: starbucks\n" 
 	    + "     :  -S # DO not SUBMIT results\n" 
 	    + "     :    # No resolvers listed, use configured resolvers\n" 
 	    + "   resolvers can be addresses or names";
 	
 	int num_resolvers = 0; 
 	boolean resolver_evaluated = false;
 	String [] list = ResolverConfig.getCurrentConfig().servers(); 
 	int abort = 999999999; 
 	
 	set_abort(false);
 	set_submit_report(true);
 	for (num_resolvers = 0; args.length > num_resolvers; num_resolvers++) {
 	    if (args[num_resolvers].equals("-a") )
 		set_abort(true);
 	    else if (args[num_resolvers].equals("-d"))
 		set_debug(true);
  	else if (args[num_resolvers].equals("-r"))
 	    set_verbose_report(true);
 	else if (args[num_resolvers].equals("-S"))
 	    set_submit_report(false);
 	else if (args[num_resolvers].equals("-T"))
 	    set_show_test_results(true); 
 	else if (args[num_resolvers].equals("-m")){ 
 	    if( num_resolvers + 1 < args.length ) 
 		set_message( args[++num_resolvers]);
 	    else 
 		print( "-m must be followed by a message");
 	} else if (args[num_resolvers].equals("-l")) {
 	    String msg = "Configured resolvers ";
 	    for (int i =0 ; i < list.length; i++)
 		msg = msg + " " + list[i]; 
 	    print (msg);
 	    num_resolvers = abort;
  	} else if (args[num_resolvers].equals("-h")){
 	    print(usage);
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
 	
 	System.exit(reports_failed);
     }
 }
