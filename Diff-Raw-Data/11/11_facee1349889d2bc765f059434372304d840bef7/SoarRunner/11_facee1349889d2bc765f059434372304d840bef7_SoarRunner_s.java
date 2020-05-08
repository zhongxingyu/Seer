 package com.soartech.bolt;
 
 import java.io.IOException;
 
 import net.sf.jlinkgrammar.Linkage;
 import net.sf.jlinkgrammar.Sentence;
 
 import sml.Agent;
 import sml.Identifier;
 import sml.smlPrintEventId;
 import sml.Agent.PrintEventInterface;
 import sml.Kernel;
 
 import edu.umich.soar.debugger.SWTApplication;
 
 
 public class SoarRunner implements PrintEventInterface {
 	public static String dictionaryPath = "/opt/bolt/stbolt/lgsoar/data/link";
 	private static String lgSoarLoaderPath = "/opt/bolt/stbolt/lgsoar/soarcode/simple-init.soar";
 	
 	private static Agent agent = null;
 	private static Kernel kernel = null;
 	
 	private static String sentence = "";
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// two args: first a sentence (quoted) to parse
 		// second is a (optional) --debug or --silent flag
 		if (args.length < 1) {
 			System.out.println("SoarRunner needs a sentence to parse!");
 			return;
 		}
 		
 		boolean debug = false;
 		boolean silent = false;
 		if (args.length >= 2 && args[1].equals("--debug")) {
 			debug = true;
 		}
 		else if (args.length >= 2 && args[1].equals("--silent")) {
 			silent = true;
 		}
 		SoarRunner sr = new SoarRunner(args[0], debug, silent);
 	}
 	
 	public SoarRunner(String theSentence, boolean debug, boolean silent) {
 		sentence = theSentence;
 		if (debug) {
 			kernel = Kernel.CreateKernelInNewThread();
 		}
 		else {
 			kernel = Kernel.CreateKernelInCurrentThread();
 		}
 		agent = kernel.CreateAgent("LGSoar");
 
 		if (silent) {
 			agent.ExecuteCommandLine("watch 0");
 		}
 		
 		
 		agent.LoadProductions(lgSoarLoaderPath, debug);
 		agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, this);
 		
 		loadSentenceOnInput();
 			
 		if (debug) {
 			try {
 				SWTApplication swtApp = new SWTApplication();
 				swtApp.startApp(new String[]{"-remote"});
 				System.exit(0); // is there a better way to get the Soar thread to stop? 
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		else {
 			agent.RunSelf(1000);
 			if (agent.GetDecisionCycleCounter() > 990) {
 				System.out.println("Agent reached dc " + agent.GetDecisionCycleCounter() + ", terminating.");
 			}
 		}
 	}
 
 	
 	@Override
 	public void printEventHandler(int eventID, Object data, Agent agent,
 			String message) {
 		// remove extraneous newline
 		message = message.substring(1, message.length());
 	
 		String[] lines = message.split("\n");
 		for (int i=0; i<lines.length; i++) {
 			// filter out "the agent halted." messages
 			if (!lines[i].endsWith("halted.")) {
 				System.out.println(lines[i]);
 			}
 		}
 	}
 	
 	private void loadSentenceOnInput() {
 		try {
 			net.sf.jlinkgrammar.parser.doIt(new String[]{sentence});
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	// called from parser.java
 	public static void loadLinkage(Linkage thisLinkage, Sentence sent) {
 		int     rWordIndex;
         int     lWordIndex;
         String  linkLabel;
         
         String outstr = thisLinkage.linkage_print_diagram();
 		System.out.println(outstr);
 		
         // Normally you loop through sublinkages
         // int n = thisLinkage.linkage_get_num_sublinkages();
         // For now only choose the first sublinkage
         thisLinkage.linkage_set_current_sublinkage(0);
         int numLinks = thisLinkage.linkage_get_num_links();
         
         // make a root lg-input WME
         Identifier lgInputRoot = agent.CreateIdWME(agent.GetInputLink(), "lg");
         
         // make a wme for the count
         agent.CreateIntWME(lgInputRoot, "count", 0);
         	
         // make a wme for the words
         Identifier wordsWME = agent.CreateIdWME(lgInputRoot, "words");
         
         int numWords = sent.sentence_length();
         // now load the words
         for (int wordx = 0; wordx < numWords; wordx++) {
             // add ^word information for this link
             String wordval = sent.sentence_get_word(wordx);
             // there's some kind of bug in the add-wme code...
             if (wordval.equals(".")) {
             	wordval = "|.|";
             }
             Identifier wordWME = agent.CreateIdWME(wordsWME, "word");
             agent.CreateIntWME(wordWME, "wcount", wordx);
             agent.CreateStringWME(wordWME, "wvalue", wordval);
         }
             
         // make a wme for the links
         Identifier linksWME = agent.CreateIdWME(lgInputRoot, "links");
         
         // now load the links
         for (int linkIndex = 0; linkIndex < numLinks; linkIndex++) {
             rWordIndex = thisLinkage.linkage_get_link_rword(linkIndex);
             lWordIndex = thisLinkage.linkage_get_link_lword(linkIndex);
             linkLabel = thisLinkage.linkage_get_link_label(linkIndex);
            
            String pattern = "([A-Z]+)([a-z]*)";
             String ltype = linkLabel;
             ltype = ltype.replaceAll(pattern, "$1");
             String lsubtype = linkLabel;
             lsubtype = lsubtype.replaceAll(pattern, "$2");
             
             // add ^link information for this link
             Identifier linkWME = agent.CreateIdWME(linksWME, "link");
                         
             agent.CreateStringWME(linkWME, "lvalue", linkLabel);
             agent.CreateIntWME(linkWME, "lwleft", lWordIndex);
             agent.CreateIntWME(linkWME, "lwright", rWordIndex);
             agent.CreateStringWME(linkWME, "ltype", ltype);
            
             if (lsubtype.length() > 0) {
                 agent.CreateStringWME(linkWME, "ltypesub", lsubtype);
             }
         }		
 	}
 }
