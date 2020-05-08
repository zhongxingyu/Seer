 /*
  *  You may not change or alter any portion of this comment or credits
  * of supporting developers from this source code or any supporting source code
  * which is considered copyrighted (c) material of the original comment or credit authors.
  * This program is distributed WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  */
 
 package cmdGA;
 
 import java.util.List;
 import java.util.Vector;
 
 import cmdGA.exceptions.IncorrectParameterTypeException;
 
 public class Parser {
 	// Instance Variables
 	protected List<Option> options = new Vector<Option>();
 
 	
 	// Public Methods
 	/**
 	 * parseEx method. 
 	 *
 	 * This methods analyzes the commandline arguments and assign them to different options.
 	 * @throws IncorrectParameterTypeException 
 	 */
 	public boolean parseEx(String[] arg) throws IncorrectParameterTypeException {
 
 		String cm = this.arrayToStr(arg);     
 			// cm contains all the arguments as a single String
 		Option currentOption = null;          
 			// currentoption references to the last option found. 
 		Match next = null;
 			// This match contains the info of the lasts arguments processed.
 
 		do {
 		
 			next = this.consume(cm);
 				// Look for the first option found in the command line.
 			if (next==null) {
 				// next is null when no option is found in the command line.
 				// when next is null, means that no more options remains in the command line.
 				
				if (!cm.equals(""))	currentOption.setValue(cm);
 					// cm contains the last, non-processed part of the command line.
        				// if cm is "" may indicate an empty command line
 			} else {
 				if (currentOption!=null) {
 					// currentOption is null when the first option is found in the command line.
 					// otherwise, an option was found previously.
 					currentOption.setValue(cm.substring(0,Math.max(next.getPosition()-1,0)));
 						// the first characters of cm contains the parameters of the previous option. 
 					    // (i.e. cm = " previousArgumentParameter -currentArgument currentArgumentParameters")
 				}
 				currentOption = next.getOption();
 					// sets the currentOption to the next option.
 				currentOption.setPresent(true);
 					// sets that the currentOption is found.
 				cm = cm.substring(Math.min(next.getEnding()+1,cm.length()));	
 					// modifies cm, eliminating the processed characters.
 			}
 		} while(next!=null);
 			// next is null after the last option is found.
 		
 		return true;
 			// returns true, indicating that the parse was successful
 	}
 	
 	/**
 	 * Consume method. 
 	 * 
 	 * Given a command line, this method search for the first option and returns a Match object if one is found.
 	 * 
 	 *  @return a Match if an option is found, or null if not. returning null indicates the end of the command line, there is no more option to look for.
 	 *  	
 	 */
 	protected Match consume(String string) {
 		List<Match> positives = new Vector<Match>();
 			// positives stores references to all the Matches that were found.
 		int toffset =0;
 			// toffset, is the offset character value to search options. 
 		int size=0;
 			// size will store the number of positive matches. (size = positives.size())
 		do  {
 			// this loop will search options in different positions.
 			for (int i=0;i<this.options.size();i++) {
 				//loop to search every option in the command line. 
 				// All the matches in the current position are stores (try to minimizes ambiguity)
 				Option opt = this.options.get(i);
 					// opt is the current option
 				String optName = opt.getName();
 					// optName is the current option name
 				String optAName = opt.getAlias();
 					// optAName is the current option alias, if it exists. Is null otherwise.
 				int optSize = optName.length();
 					// optsize is the length of current option name
 				int optASize = 0;
 				if (optAName!=null) {optASize = optAName.length();}
 					// optAsize is the length current option alias, if it exists. Is zero otherwise.
 					
 				if (string.regionMatches(true, toffset, optName, 0, optSize)) {
 					// if the option name is the first found in the command line  
 					positives.add(new Match(toffset,toffset + optSize,opt,false));
 						// Add a positive match to positives list.
 				}
 			
 				if (optAName !=null && string.regionMatches(true, toffset, optAName, 0, optASize)) {
 					// if the option alias is the first found in the command line					
 					positives.add(new Match(toffset,toffset + optASize,opt,true));
 						// Add a positive match to positives list.
 				}
 			}
 			
 			size = positives.size(); 
 				// size will store the number of positive matches. (size = positives.size())
 			if(size==0){
 				// Case 1 : There is no option found
 				toffset = Math.min(string.indexOf(" ", toffset)+ 1, string.length());
 					// toffset is displaced to the next space plus one.
 				if(toffset ==0 ) toffset = string.length();
 					// if none space is found, toffset is displaced to the end.
 			}
 			
 			if (size==1) {
 				// Case 2: There is one option found
 				return positives.get(0);
 					// return this Match!
 			}
 
 			if (size>1) {
 				// Case 3: There is more than one option found
 				// Look for the one with the the larger name/alias match
 				// This is in order to reduce ambiguity
 				// (i.e. option1.name = "-min" and option2.name = "-minSize", if both match, option2 is preferred)
 				int maxIndex = 0;
 					// maxIndex will store the index of the larger match
 				int maxSize=positives.get(maxIndex).getEnding();
 					// maxSize is the lenght of the larger match
 				for(int i=1; i <positives.size();i++) {
 					// look in every match
 					if (positives.get(i).getEnding()>maxSize) {
 						// is current match is larger than previous
 						maxIndex = i;
 						maxSize = positives.get(maxIndex).getEnding();
 							// update maxindex and maxsize values.
 					}
 				}
 				return positives.get(maxIndex);
 					// return the larger Match
 			}
 		}
 		while (size==0&& toffset<string.length());
 			// if no option is found in the current position (recorded in toffset) continue looking for matches 
 			// until the end of the command line is reached.
 		return null;
 		// returning null means that no more option remains in the command line. 
 	}
 	
 	/**
 	 * This method joins an array of String to a single string, interleaving each one with a space.
 	 * 
 	 * @param arg an array of string.
 	 * @return a string with array joined.
 	 */
 	protected String arrayToStr(String[] arg) {
 		if (arg.length>0) {
 		String args=arg[0];
 			// The first element is treated specially
 		for (int i=1;i<arg.length;i++) {args = args + " "+ arg[i] ;}
 			// add the next elements of the array 
 		return args;
 			// return the final value
 		} else {
 			return "";
 		}
 	}
 }
