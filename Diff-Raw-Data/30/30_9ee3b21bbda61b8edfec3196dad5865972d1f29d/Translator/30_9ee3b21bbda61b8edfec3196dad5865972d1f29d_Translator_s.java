 import java.util.Hashtable;
 import java.util.regex.*;
 
 public class Translator {
 
     static boolean debug = false;   
 	protected Hashtable<String, String> trans = new Hashtable<String, String>();		
 
 	public Translator() {
 
 	    trans.put("0",  "NAR,1,=NAR.");
	    trans.put("1",  "[FT]............,2,=NAR.");
 	    trans.put("2",  ".[PFX][PFX][PFX][PFX][PF][APF][APF][APF][APF][APF][APF].,=TIMEOUT.,3");
 	    trans.put("3",  ".[PF][PF][PF][PF]........,=ANOMALOUS.,4");
 	    trans.put("4",  "PP...P[AP][AP].[AP]...,=NOTDNSSEC.,5");
 	    trans.put("5",  "......PP.P...,7,8");
 	    trans.put("7",  ".............,7a,=Validator");
 	    trans.put("7a", "....P...A....,DNAME,7b");
 	    trans.put("7b", "...F.......A.,7c,TCP");
 	    trans.put("7c", "............F,7d,Permissive");
 	    trans.put("7d", "......[AF][AF][AF][AF][AF][AF],6a,Mixed");
 	    trans.put("8",  ".............,8a,=DNSSEC Aware");
 	    trans.put("8a", "....P...[AP]....,DNAME,8b");
 	    trans.put("8b", "...F.......[AP].,6a,TCP)");
 	    trans.put("6a", "..F..........,6b,Unknown");
 	    trans.put("6b", "..........F..,6c,NSEC3");
 	    trans.put("6c", "...P.......F.,6d,SlowBig");
 	    trans.put("6d", "...F.......F.,.,NoBig");
 	    trans.put(".",  ".............,.,.");
 	}
 
 	public static void set_debug(boolean val) {
         debug = val;
     }
     
 	public String translate(String pfa) {
 		
 	    String g = "";
 	    String gmod = "";
 	    
 	    String next_state = "0";
 	    while (!next_state.equals("."))
 	    {
 	        String p = trans.get(next_state);
 	        String[] pieces = p.split(",");
 	        if (pieces.length==3)
 	        {
 	            // pattern, fail, succeed
 	            // fail/succeed: transition | [=]output[.]
 	            // = means main ("g"), no = means within ()'s ("gmod")
 	            // . is terminal
 	        	
 	        	Pattern pattern = Pattern.compile(pieces[0]);
 
 	            String state = next_state;
 	            next_state = "NOT_A_STATE";
 
 	            Matcher matcher = pattern.matcher(pfa);
 	            int m;
 	            String match_action, alt_action;
 	            if (matcher.matches())            
 	            { 
 	                m = 1;
 	                match_action = pieces[2];
 	                alt_action = pieces[1];
 	            }
 	            else
 	            {
 	                m = 0; 
 	                match_action = pieces[1];
 	                alt_action = pieces[2];
 	            }
 	            
                 if (debug) {
                     System.out.printf(
                         "state=%-2s, p=%s, pfa=%s,match=%s, match_action=%s, alt_action=%s, g=%s, gmod=%s\n", 
                         state, p, pfa, m, match_action, alt_action, g, gmod);
 	            }
                 
 	            if (trans.containsKey(match_action))
 	            {
 	                next_state = match_action;
 	                continue;
 	            }
 	            else 
 	            {
 	                int have_dot = match_action.indexOf(".");
 	                if (have_dot >= 0)
 	                {
 	                    match_action = match_action.substring(0,have_dot);
 	                }
 
 	                if (match_action.indexOf("=")==0)
 	                {
 	                    g = match_action.substring(1);
 	                }
 	                else
 	                {
 	                    gmod = gmod + match_action + ",";
 	                }
 	                
 	                if (have_dot > 0)
 	                    next_state = ".";
 	                else
 	                    next_state = alt_action;
 	            }
 	        }
 	        else
 	        {
 	            System.out.printf("ERROR: p=%s, next_state=%s\n", p, next_state);
 	        }
 	    }
 	    if (g.length() == 0)
 	    {
 	        g = "NAR";
 	        System.out.printf("ERROR: pfa=%s, g=%s\n", pfa, g);
 	    }
 	    else
 	    {
 	        int l = gmod.length();
 	        if (l > 0)
 	        {
 	            g = "Partial " + g + "(" + gmod.substring(0, l-1) + ")";
 	        }
 	    }
 	    return g;	
 	}
 }
