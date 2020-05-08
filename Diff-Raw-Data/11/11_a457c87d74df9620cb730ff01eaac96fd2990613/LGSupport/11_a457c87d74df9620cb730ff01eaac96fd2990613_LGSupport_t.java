 package com.soartech.bolt;
 
 import java.io.IOException;
 
 import net.sf.jlinkgrammar.Linkage;
 import net.sf.jlinkgrammar.Sentence;
 import sml.Agent;
 import sml.Agent.OutputEventInterface;
 import sml.Identifier;
 import sml.WMElement;
 
 public class LGSupport implements OutputEventInterface {
 	private static Agent agent;
 	public static String dictionaryPath = ""; 
 	private static Identifier lgInputRoot;
 	private static int sentenceCount = -1;
 	
 	public LGSupport(Agent _agent, String dictionary) {
 		agent = _agent;
 		dictionaryPath = dictionary;
 		
 		// make a root lg-input WME
 		if (agent != null) {
 			lgInputRoot = agent.CreateIdWME(agent.GetInputLink(), "lg");
 		}
 		agent.AddOutputHandler("preprocessed-sentence", this, null);
 	}
 	
 	public void handleSentence(String sentence) {
 		
 		// load the sentence into WM
 		originalSentenceToWM(sentence);
 		
 		// Soar rules may modify it
 		
 		// wait for preprocessed sentence on output
 	}
 	public void outputEventHandler(Object data, String agentName,
 			String attributeName, WMElement pWmeAdded) {
 		String sentence = preprocessedSentenceFromWM(pWmeAdded);
		sentenceCount++;

 		// call LG Parser
 		try {
 			net.sf.jlinkgrammar.parser.doIt(new String[]{sentence});
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	// called from parser.java
 	// doIt (called above) will call this once for each linkage (parse)
 	public static void loadLinkage(Linkage thisLinkage, int idx, Sentence sent) {
 		int     rWordIndex;
         int     lWordIndex;
         String  linkLabel;
         
         // combine all sublinkages to one
         thisLinkage.linkage_compute_union();
         
         String outstr = thisLinkage.linkage_print_diagram();
 		System.out.println(outstr);
 		
 		if (agent == null) {
 			// valid if run to print the parse alone
 			return;
 		}
 		
         int numLinks = thisLinkage.linkage_get_num_links();
         
        // make a root for this sentence
         Identifier sentenceRoot = agent.CreateIdWME(lgInputRoot, "sentence");
         
         // make a wme for the count
         agent.CreateIntWME(sentenceRoot, "count", sentenceCount);
        
         agent.CreateIntWME(sentenceRoot, "parse-count", idx);
         	
         // make a wme for the words
         Identifier wordsWME = agent.CreateIdWME(sentenceRoot, "words");
         
         int numWords = sent.sentence_length();
         // now load the words
         for (int wordx = 0; wordx < numWords; wordx++) {
             // add ^word information for this link
             String wordval = sent.sentence_get_word(wordx);
  
             Identifier wordWME = agent.CreateIdWME(wordsWME, "word");
             agent.CreateIntWME(wordWME, "wcount", wordx);
             agent.CreateStringWME(wordWME, "original-wvalue", wordval);
         }
             
         // make a wme for the links
         Identifier linksWME = agent.CreateIdWME(sentenceRoot, "links");
         
         String noStarsPattern = "\\*";
         String idiomPattern = "ID.*";
         String pattern = "([A-Z]+)([a-z]*)";
         
         // now load the links
         for (int linkIndex = 0; linkIndex < numLinks; linkIndex++) {
             rWordIndex = thisLinkage.linkage_get_link_rword(linkIndex);
             lWordIndex = thisLinkage.linkage_get_link_lword(linkIndex);
             linkLabel = thisLinkage.linkage_get_link_label(linkIndex);
            
             // SBW 3/8/12
             // remove all *'s from the link names
             // these indicate "any subtype in this position"
             // not sure what to do with them, but they definitely shouldn't be stuck to the main type
             linkLabel = linkLabel.replaceAll(noStarsPattern, "");
             
             String ltype = linkLabel;
             ltype = ltype.replaceAll(pattern, "$1");
             ltype = ltype.replaceAll(idiomPattern, "ID");
             String lsubtype = linkLabel;
             lsubtype = lsubtype.replaceAll(pattern, "$2");
             
             // add ^link information for this link
             Identifier linkWME = agent.CreateIdWME(linksWME, "link");
                         
             agent.CreateStringWME(linkWME, "lvalue", linkLabel);
             agent.CreateIntWME(linkWME, "lwleft", lWordIndex);
             agent.CreateIntWME(linkWME, "lwright", rWordIndex);
             agent.CreateStringWME(linkWME, "ltype", ltype);
            
             // make a separate WME for each subtype
             // assumption: subtype ordering doesn't matter
         	for (int i=0; i< lsubtype.length(); i++) {
         		agent.CreateStringWME(linkWME, "ltypesub", lsubtype.substring(i, i+1));
         	}
           
         }		
 	}
 
 	private void originalSentenceToWM(String sentence) {
 		Identifier root = agent.CreateIdWME(lgInputRoot, "original-sentence");
         agent.CreateIntWME(root, "count", sentenceCount);
         Identifier wordsWME = agent.CreateIdWME(root, "words");
         sentence = sentence.replaceAll("(\\W)", " $1");
         //System.out.println("padded: " + sentence);
         String[] words = sentence.split("\\s+");
         
         for (int i=0; i<words.length; i++) {
         	Identifier wordWME = agent.CreateIdWME(wordsWME, "word");
         	agent.CreateStringWME(wordWME, "wvalue", words[i]);
         	agent.CreateIntWME(wordWME, "wcount", i);
         	//System.out.println("wd " + words[i]);
         }
 
 	}
 	
 	private String preprocessedSentenceFromWM(WMElement pWmeAdded) {
 		String result = "";
 		
 		WMElement currentWME = pWmeAdded.ConvertToIdentifier().FindByAttribute("start", 0);
 		
 		while (currentWME != null) {
 			String word = currentWME.ConvertToIdentifier().GetParameterValue("word");
 			if (word != null) {
 				result += " " + word;
 			}
 			currentWME = currentWME.ConvertToIdentifier().FindByAttribute("next", 0);
 		}
 		
 		//System.out.println("got sentence: " + result);
 		return result;
 	}
 
 }
