 /* Copyright (c) 2010, Jacob Godserv
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *     * Redistributions of source code must retain the above
  *       copyright notice, this list of conditions and the following
  *       disclaimer.
  *     * Redistributions in binary form must reproduce the above
  *       copyright notice, this list of conditions and the following
  *       disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *     * Neither the name of the author nor the names of its
  *       contributors may be used to endorse or promote products
  *       derived from this software without specific prior written
  *       permission.
  *
  * THIS SOFTWARE IS PROVIDED BY Jacob Godserv "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL Jacob Godserv BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.neuvoo.profileinspector;
 
 import java.io.*;
 import org.jargp.*;
 import java.util.Vector;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 public class ProfileInspector {
 
 	private String action = "";
 	private String profile = "";
 	private String search = "";
 	private boolean verbose = false;
 	private boolean minus = false;
 
 	public Profile processProfile () throws ArgumentErrorException, IOException {
 		if (this.action.equals("")) {
 			this.action = "i";
 		}
 		
 		if (this.action.equals("i")) {
 			if (this.profile.equals("")) {
 				throw new ArgumentErrorException ("action i requires profile path");
 			} else {
 				if (this.verbose) System.out.println("Starting profile investigation at " + this.profile);
 			}
 		} else {
 			throw new ArgumentErrorException ("invalid action " + this.action);
 		}
 		return new Profile(this.profile, new ProfileEnvironment(), this.verbose, this.minus, this.search);
 	}
 
 	private static final ParameterDef[] ARG_DEFS = {
 		new StringDef('a', "action", "the action to take with a/the profile"),
 		new StringDef('p', "profile", "the path to the profile to investigate"),
 		new StringDef('s', "search", "search for a string in the profile"),
 		new BoolDef('v', "verbose", "Supress extra information normally printed prior to results"),
 		new BoolDef('m', "minus", "Do not let the minus prefix vanish")
 	};
 
 	public static void main (String args[]) {
 		ProfileInspector profileInspector = new ProfileInspector();
 		Profile resultingProfile = null;
 
 		if (args.length < 1 || args[0].indexOf("help") >= 0) {
 			usage();
 			System.exit(0);
 		} else {
 			try {
 				ArgumentProcessor.processArgs(args, ARG_DEFS, profileInspector);
 				resultingProfile = profileInspector.processProfile();
 			} catch (ArgumentErrorException e) {
 				System.err.println("Error processing command-line arguments: " + e.getMessage());
 				usage();
 				System.exit(255);
 			} catch (Exception e) {
 				System.err.println("Error proccessing:");
 				e.printStackTrace();
 				System.exit(1);
 			}
 			
 			System.out.println(resultingProfile.getHumanOutput());
 		}
 	}
 
 	public static void usage () {
 		System.out.println("Usage: java ProfileInspector [-a <action>] [options]");
 		System.out.println("");
 		System.out.println("Actions:");
 		System.out.println("    i           Default. Investigate the profile, and print out\n" +
 		                   "                accumulated profile information (requires -p)");
 		System.out.println("");
 		System.out.println("Options:");
 		System.out.println("    -p <path>   The path to the profile to investigate");
 		System.out.println("");
 		System.out.println("    -s <string> Search for any mention of string and report to\n" +
 		                   "                stderr.");
 	}
 
 }
 
 class ProfileEnvironment {
 
 	private HashMap<String,HashMap<String,Vector<String>>> variables = new HashMap<String,HashMap<String,Vector<String>>>();
 
 	public void setVars (String category, String key, Vector<String> value) {
 		HashMap<String,Vector<String>> currCategory = this.variables.get(category);
 
 		if (currCategory == null) { currCategory = new HashMap<String,Vector<String>>(); }
 		currCategory.put(key, value);
 
 		this.variables.put(category, currCategory);
 	}
 
 	public Vector<String> getVars (String category, String key) {
 		HashMap<String,Vector<String>> currCategory = this.variables.get(category);
 		if (currCategory == null) { currCategory = new HashMap<String,Vector<String>>(); }
 
 		Vector<String> values = currCategory.get(key);
 		if (values == null) { values = new Vector<String>(); }
 		return values;
 	}
 
 	public HashMap<String,Vector<String>> getCategoryKeys (String category) {
 		HashMap<String,Vector<String>> currCategory = this.variables.get(category);
 		if (currCategory == null) { currCategory = new HashMap<String,Vector<String>>(); }
 		return currCategory;
 	}
 	
 	public HashMap<String,HashMap<String,Vector<String>>> getCategories () {
 		return this.variables;
 	}
 
 	public void setCategoryKeys (String category, HashMap<String,Vector<String>> currCategory) {
 		this.variables.put(category, currCategory);
 	}
 
 }
 
 class ProfileFile {
 	
 	public static final int TYPE_IGNORE = 1;
 	public static final int TYPE_2D = 2; // means there is only one lineRule
 	public static final int TYPE_3D = 3; // means there is two lineRules
 	public static final int TYPE_KEYVAL_BASH = 4;
 	public static final int MAX_TYPE = TYPE_KEYVAL_BASH;
 	private int type = TYPE_2D;
 	
 	public static final int NOEXISTS_CAN_BLANK = 1;
 	public static final int NOEXISTS_CAN_IGNORE = 2;
 	public static final int MAX_NOEXISTS = (NOEXISTS_CAN_IGNORE*2)-1;
 	private boolean noExistsCanBlank = false;
 	private boolean noExistsCanIgnore = false;
 	
 	public static final int EXCEPT_NO_BLANK = 1;
 	public static final int EXCEPT_NO_COMMENT = 2;
 	public static final int EXCEPT_NO_LINE_CONT = 4;
 	public static final int MAX_EXCEPT = (EXCEPT_NO_LINE_CONT*2)-1;
 	private boolean exceptNoBlank = false;
 	private boolean exceptNoComment = false;
 	private boolean exceptNoLineCont = false;
 	
 	// lineRule[][0]
 	public static final int LINEPIECE_PREFIX_NONE = 1;
 	public static final int LINEPIECE_PREFIX_MINUS = 2; // It's important MINUS is here so it doesn't mess up regexp with ranges (which use hyphens)
 	public static final int LINEPIECE_PREFIX_PLUS = 4;
 	public static final int LINEPIECE_PREFIX_ASTERIK = 8;
 	public static final int LINEPIECE_PREFIX_SPECIAL_ONLY = 16;
 	public static final String LINEPIECE_PREFIX_ALL = "-+*";
 	public static final int MAX_LINE_PREFIX = (LINEPIECE_PREFIX_SPECIAL_ONLY*2)-1;
 	// lineRule[][1]
 	public static final int LINEPIECE_TYPE_NUMBER = 1;
 	public static final int LINEPIECE_TYPE_STRING = 2;
 	public static final int LINEPIECE_TYPE_PACKAGE = 3;
 	public static final int MAX_LINE_KEY = LINEPIECE_TYPE_PACKAGE;
 	private int[][] lineRules = new int[][]{{LINEPIECE_PREFIX_NONE, LINEPIECE_TYPE_STRING}, {LINEPIECE_PREFIX_NONE, LINEPIECE_TYPE_STRING}};
 	
 	public static final int INHERIT_NONE = 1;
 	public static final int INHERIT_PARENT = 2;
 	public static final int INHERIT_NO_APPEND = 4;
 	public static final int INHERIT_APPEND_SPECIAL_ONLY = 8;
 	public static final int MAX_INHERIT = (INHERIT_APPEND_SPECIAL_ONLY*2)-1;
 	private boolean inherits = false;
 	private boolean inheritsCanAppend = true;
 	private boolean inheritsCanAppendSpecialOnly = false;
 	
 	public static final int DEPRECATED_NO = 1;
 	public static final int DEPRECATED_YES = 2;
 	public static final int MAX_DEPRECATED = DEPRECATED_YES;
 	private int deprecation = DEPRECATED_NO;
 	
 	private String path = "";
 	private String fileName = "";
 	private int eapi = 0;
 	
 	private boolean verbose = false;
 	private boolean showMinus = false;
 	private String search = "";
 	
 	public ProfileFile (String path, int type, int fileNonExistanceRules, int disallowedExceptions, int[][] lineRules, int inheritanceRules, int pmsDeprecationRules, boolean verbose, boolean showMinus, String search) {
 		if (path != null && !path.equals("")) {
 			this.path = path;
 			this.fileName = new File(path).getName();
 		} else {
 			throw new IllegalArgumentException ("Empty path");
 		}
 		this.verbose = verbose;
 		this.showMinus = showMinus;
 		this.search = search;
 		if (type > 0 && type <= MAX_TYPE) {
 			this.type = type;
 		} else {
 			throw new IllegalArgumentException ("For path " + path + ", bad file type: " + type);
 		}
 		while (fileNonExistanceRules > 0) {
 			if (fileNonExistanceRules >= NOEXISTS_CAN_IGNORE) {
 					this.noExistsCanIgnore = true;
 					fileNonExistanceRules -= NOEXISTS_CAN_IGNORE;
 			} else if (fileNonExistanceRules >= NOEXISTS_CAN_BLANK) {
 					this.noExistsCanBlank = true;
 					fileNonExistanceRules -= NOEXISTS_CAN_BLANK;
 			} else {
 					throw new IllegalArgumentException ("For path " + path + ", bad rule number for non-existance rules: " + fileNonExistanceRules);
 			}
 		}
 		while (disallowedExceptions > 0) {
 			if (disallowedExceptions >= EXCEPT_NO_LINE_CONT) {
 				this.exceptNoLineCont = true;
 				disallowedExceptions -= EXCEPT_NO_LINE_CONT;
 			} else if (disallowedExceptions >= EXCEPT_NO_COMMENT) {
 				this.exceptNoComment = true;
 				disallowedExceptions -= EXCEPT_NO_COMMENT;
 			} else if (disallowedExceptions >= EXCEPT_NO_BLANK) {
 				this.exceptNoBlank = true;
 				disallowedExceptions -= EXCEPT_NO_BLANK;
 			} else {
 				throw new IllegalArgumentException ("For path " + path + ", bad rule number for disallowed exceptions: " + disallowedExceptions);
 			}
 		}
 		if (lineRules.length > 0) {
 			for (int i = 0; i < lineRules.length; i++) {
 				if (lineRules[i].length < 2) {
 					throw new IllegalArgumentException ("For path " + path + ", line part #" + i + " does not have the proper number of line rules: " + lineRules[i].length);
 				}
 			}
 			this.lineRules = lineRules;
 		} else {
 			throw new IllegalArgumentException ("For path " + path + ", there are no line rules");
 		}
 		while (inheritanceRules > 0) {
 			if (inheritanceRules >= INHERIT_APPEND_SPECIAL_ONLY) {
 				this.inheritsCanAppendSpecialOnly = true;
 				inheritanceRules -= INHERIT_APPEND_SPECIAL_ONLY;
 			} else if (inheritanceRules >= INHERIT_NO_APPEND) {
 				this.inheritsCanAppend = false;
 				inheritanceRules -= INHERIT_NO_APPEND;
 			} else if (inheritanceRules >= INHERIT_PARENT) {
 				this.inherits = true;
 				inheritanceRules -= INHERIT_PARENT;
 			} else if (inheritanceRules >= INHERIT_NONE) {
 				this.inherits = false;
 				inheritanceRules -= INHERIT_NONE;
 			} else {
 				throw new IllegalArgumentException ("For path " + path + ", bad rule number for inheritance: " + inheritanceRules);
 			}
 		}
 		if (deprecation > 0 && deprecation <= MAX_DEPRECATED) {
 			this.deprecation = deprecation;
 		} else {
 			throw new IllegalArgumentException ("For path " + path + ", bad rule number for deprecation: " + deprecation);
 		}
 	}
 	
 	public void mergeToEnvironment (ProfileEnvironment environment) throws FileNotFoundException, IOException {
 		// Discern EAPI
 		Vector<String> eapiData = environment.getVars("eapi", "list");
 		if (eapiData.size() > 0) {
 			try {
 				this.eapi = Integer.valueOf(eapiData.firstElement());
 			} catch (NumberFormatException e) {
 				// ignore, just use default // PMS 5.2.2
 			}
 		}
 		
 		// No inheritance? Then reset it.
 		if (this.inherits == false) {
 			environment.setCategoryKeys(this.fileName, new HashMap<String,Vector<String>>());
 		}
 		
 		// Open file
 		BufferedReader br = null;
 		boolean blankFile = false;
 		try {
 			br = new BufferedReader(new FileReader(path));
 		} catch (FileNotFoundException e) {
 			if (verbose) System.err.println("File " + path + " does not exist.");
 			if (this.noExistsCanBlank) {
 				blankFile = true;
 			} else if (this.noExistsCanIgnore) {
 				return; // we don't exist
 			} else {
 				throw new FileNotFoundException ("Path not found, and does not appear optional: " + this.path);
 			}
 		}
 		int lineNum = 0;
 
 		// These variables sometimes need to exist beyond a line
 		HashMap<String,Vector<String>> fileData = environment.getCategoryKeys(this.fileName);
 		String key = "";
 		Vector<String> keyValues = null;
 		
 		boolean fileHasEnded = false;
 		while (true) {
 			
 			// Read a line
 			String line = "";
 			while (true) { // we allow for line continuations. Loop until full line is gathered
 				if (!blankFile) { // we're dealing with an empty/non-existant file, so skip reading
 					try {
 						String newlyRead = br.readLine();
 						lineNum++;
 						// Handle EOF gracefully
 						if (newlyRead == null) {
 							fileHasEnded = true;
 							newlyRead = "";
 						}
 						line += newlyRead;
 						line.trim();
 					} catch (IOException ioe) {
 						throw ioe;
 					}
 				}
 				
 				// Only continue if we have more data (blank files get to loop once; see end of loop)
 				if (fileHasEnded && line.equals("")) {
 					try {
 						br.close();
 					} catch (Exception e) {
 						// meh, we're done here anyway
 					} finally {
 						return;
 					}
 				} else {
 					fileHasEnded = false; // file has not really ended. Proceed.
 				}
 				
 				if (line.indexOf('#') > -1) { // comment cancels even line continuation so it comes first
 					if (this.exceptNoComment) {
 						System.err.println("Notice: file " + path + " line " + lineNum + ": possible comment where comment not allowed. Allowing it to be parsed by profile.");
 					} else {
						line = line.substring(0, line.indexOf('#')).trim(); // Cut out comment // PMS 5.2.5  and we're assuming all files with line continuation use these rules)
 						if (line.equals("")) {
 							continue; // allowable comment line with no data but a comment; no need to parse it
 						}
 					}
 				}
				if (line.endsWith("\\") || (this.type == this.TYPE_KEYVAL_BASH && !line.endsWith("\""))) { // line continuation // PMS 5.2.5 (and we're assuming all files with line continuation use these rules)
 					if (this.exceptNoLineCont) {
 						System.err.println("Notice: file " + path + " line " + lineNum + ": possible line continuation where line continuation not allowed. Allowing it to be parsed by profile.");
 					} else {
 						if (line.endsWith("\\")) {
 							line = line.substring(0, line.lastIndexOf('\\')).trim();
 						}
 						continue; // we need another line first!
 					}
 				}
 				
 				if (line.equals("") && !blankFile) { // illegal blank lines caught here
 					if (this.exceptNoBlank) { // blank lines aren't supposed to even exist
 						System.err.println("Warning: file " + path + " line " + lineNum + ": blank line where blank is not allowed.");
 					}
 					continue; // skip the blanks
 				}
 				break; // done with line continuations
 			}
 			
 			if (line.contains(this.search) && !this.search.equals("")) {
 				System.err.println("Notice: search string found in file " + path + " line " + lineNum + ": " + line);
 			}
 			
 			String[] pieces;
 			if (this.type == TYPE_KEYVAL_BASH) { // strip the bash to a simple 3D format
 				
 				// This entire block uses PMS 5.2.4
 				
 				// If there's no room for a variable name, there is no variable name, and that's bad.
 				int equalsIndex = line.indexOf('=');
 				if (equalsIndex < 1) {
 					System.err.println ("Error: in file " + path + " line " + lineNum + ": equals sign in bad position or non-existant, which means the variable name couldn't be found. SKIPPING this line.");
 					continue;
 				}
 				
 				// Quotes are a big deal. Syntax is a big deal.
 				String value = line.substring(equalsIndex+1, line.length()).trim();
 				if (!value.startsWith("\"") || !value.endsWith("\"")) {
 					System.err.println ("Error: in file " + path + " line " + lineNum + ": variable data should begin and end with double quotes. SKIPPING this line.");
 					continue;
 				}
 				
 				value = value.substring(1, value.length()-1).trim(); // cut out the quotes.
 
 				String variableName = line.substring(0, equalsIndex).trim();
 				// Some key validation
 				if (!variableName.replaceAll("[a-zA-Z0-9_]","").equals("")) { // PMS 5.2.4
 					System.err.println ("Warning: in file " + path + " line " + lineNum + ": variable name " + variableName + " contains invalid characters.");
 				}
 				if (!Pattern.matches("[a-zA-Z]", Character.toString(variableName.charAt(0)))) { // PMS 5.2.4
 					System.err.println ("Warning: in file " + path + " line " + lineNum + ": variable name " + variableName + " begins with an invalid character.");
 				}
 				value = variableName + " " + value; // prepend the key so it becomes part of the pieces variable, just like the regular 3D format
 				
				pieces = value.split("[ \t]+"); // TODO: PMS ASSUMPTION in bash-like format
 			}
 			else { // everyone else is easy to parse
				pieces = line.split("[ \t]+"); // PMS 5.2.5 (and we're assuming all white-space delimited files use this format)
 			}
 			
 			if (pieces.length < 1) {
 				pieces = new String[]{""}; // there needs to be at least one loop
 			}
 			
 			if (this.type == TYPE_2D) { // TYPE_2D only wants one piece.
 				if (pieces.length > 1) {
 					System.err.println("Notice: file " + path + " line " + lineNum + ": possible syntax issue: there are spaces, indicating 3D data, in a 2D file. Allowing it to be parsed by profile as 2D anyway.");
 				}
 				pieces = new String[]{line};
 			} else if (pieces.length < 2) { // Everyone else wants at least two.
 				if (pieces.length > 0 && this.type == TYPE_KEYVAL_BASH) {
 					// there's a "variable=", but then inside the quotes immediately following there are no spaces. This is fine.
 				} else {
 					System.err.println ("Notice: in file " + path + " line " + lineNum + ": possible syntax issue: there are no spaces, indicating 2D data, in a 3D file. Allowing it to be parsed by profile as 3D anyway.");
 				}
 			}
 			
 			for (int i = 0; i < pieces.length; i++) {
 				String piece = pieces[i];
 				
 				// Key == 0, value == 1
 				int linePiece = 0;
 				if (i > 0) { linePiece = 1; }
 				
 				// OK, time to gather linePiece rules
 				// reasonable defaults
 				boolean prefixCanIgnore = false;
 				boolean prefixSpecialOnly = false;
 				String prefixes = "";
 				int lineType = LINEPIECE_TYPE_STRING;
 				for (int lri = 0; lri < 2; lri++) {
 					int currLineRule = this.lineRules[linePiece][lri];
 					if (lri == 0) { // part one of the rule: what can we strip as a prefix?
 						prefixCanIgnore = false;
 						while (currLineRule > 0) {
 							if (currLineRule >= LINEPIECE_PREFIX_SPECIAL_ONLY) {
 								prefixSpecialOnly = true;
 								currLineRule -= LINEPIECE_PREFIX_SPECIAL_ONLY;
 							} else if (currLineRule >= LINEPIECE_PREFIX_ASTERIK) {
 								prefixes += "*";
 								currLineRule -= LINEPIECE_PREFIX_ASTERIK;
 							} else if (currLineRule >= LINEPIECE_PREFIX_PLUS) {
 								prefixes += "+";
 								currLineRule -= LINEPIECE_PREFIX_PLUS;
 							} else if (currLineRule >= LINEPIECE_PREFIX_MINUS) {
 								prefixes += "-";
 								currLineRule -= LINEPIECE_PREFIX_MINUS;
 							} else if (currLineRule >= LINEPIECE_PREFIX_NONE) {
 								prefixCanIgnore = true;
 								currLineRule -= LINEPIECE_PREFIX_NONE;
 							} else {
 								throw new IllegalArgumentException ("In file " + path + " line " + lineNum + ": bad rule number for allowed prefixes: " + currLineRule);
 							}
 						}
 					} else if (lri == 1) {
 						switch (currLineRule) { // part two: what type is this part stored as?
 							case LINEPIECE_TYPE_NUMBER:
 								if (this.type == TYPE_KEYVAL_BASH && linePiece == 0) {
 									throw new IllegalArgumentException ("In file " + path + " line " + lineNum + ": when using TYPE_KEYVAL_BASH, cannot use LINEPIECE_TYPE_NUMBER for key type");
 								}
 								lineType = LINEPIECE_TYPE_NUMBER;
 								break;
 							case LINEPIECE_TYPE_STRING:
 								lineType = LINEPIECE_TYPE_STRING;
 								break;
 							case LINEPIECE_TYPE_PACKAGE:
 								if (this.type == TYPE_KEYVAL_BASH) {
 									System.err.println ("Warning: in file " + path + " line " + lineNum + ": while this utility can handle package atoms in bash-like files, PMS has neither allowed nor disallowed this.");
 								}
 								lineType = LINEPIECE_TYPE_PACKAGE;
 								break;
 							default:
 								throw new IllegalArgumentException ("In file " + path + " line " + lineNum + ": bad rule number for piece type: " + currLineRule);
 						}
 					}
 				}
 				
 				// We can't have conflicting rules. :)
 				if (prefixCanIgnore == false && prefixes.equals("")) {
 					throw new IllegalArgumentException ("In file " + path + " line " + lineNum + ": we were requested to not ignore prefixes but weren't told what prefixes were acceptable.");
 				}
 				
 				if (prefixSpecialOnly && !this.isSpecialKey(key)) { // If only special variables are allowed to have prefixes, delete the prefixes variable so no prefix is recognized // PMS 5.3.1
 					prefixes = "";
 				}
 				
 				// Strip the prefix, and store it. Also store the line piece without its prefix.
 				String pieceWithoutPrefix = piece;
 				String piecePrefix = "";
 				if (!prefixes.equals("")) {
 					piecePrefix = piece.replaceAll("^(["+prefixes+"]+).*", "$1"); // snag the prefix at the beginning
 					if (Pattern.matches("^["+LINEPIECE_PREFIX_ALL+"]+", piece)) { // if there is a prefix
 						if (prefixes.equals("")) { // should never happen (and we don't really need this warning: CFLAGS for example)
 							System.err.println("Warning: in file " + path + " line " + lineNum + ": we did not desire a prefix, but there appears to be one in this piece: " + piece);
 						} else {
 							if (!Pattern.matches("^["+prefixes+"]+", piecePrefix)) {
 								System.err.println("Warning: in file " + path + " line " + lineNum + ": there is an invalid prefix in this piece (which we will ignore): " + piece);
 							} else {
 								pieceWithoutPrefix = piece.substring(piecePrefix.length(), piece.length());
 								if (Pattern.matches("^["+LINEPIECE_PREFIX_ALL+"]+", Character.toString(pieceWithoutPrefix.charAt(0)))) { // still!?
 									System.err.println("Warning: in file " + path + " line " + lineNum + ": we desired a prefix, and found one, but now there appears to be yet another prefix (which will not be processed) in this piece: " + piece);
 								}
 							}
 						}
 					} else if (!prefixes.equals("") && !prefixCanIgnore) { // if there is no prefix but we want one
 						System.err.println("Warning: in file " + path + " line " + lineNum + ": it seems we desired a prefix, but there is none in this piece: " + piece);
 					}
 				}
 				
 				// Some special handling of number formats.
 				if (lineType == LINEPIECE_TYPE_NUMBER) {
 					if (this.exceptNoBlank && pieceWithoutPrefix.equals("")) { // Special scenario: allow number zero where blank is not allowed in a numeric field
 						pieceWithoutPrefix = "0";
 						piece += "0";
 					} else {
 						try {
 							Double.valueOf(pieceWithoutPrefix);
 						} catch (NumberFormatException e) {
 							System.err.println ("Warning: in file " + path + " line " + lineNum + ": this piece is supposed to be numeric, but it has failed checks: " + piece);
 						}
 					}
 				}
 				
 				// TODO: Some special handling of package formats
 				
 				if (linePiece == 0 && this.type != TYPE_2D) { // first piece, the key. We reset values for each line, but only in a 3D array.
 					// This means a change in key, so...
 					// Save data using current key before we update key to new one and reset data
 					if (keyValues != null) {
 						// Append previously collected data (if any exists and we're allowed)
 						if (this.inheritsCanAppend && (!this.inheritsCanAppendSpecialOnly || this.isSpecialKey(key))) {
 							Vector<String> existingValues = new Vector<String>();
 							if (fileData.containsKey(key)) {
 								existingValues = fileData.get(key);
 							} // otherwise we'll start a new one
 							keyValues = this.mergeValues(existingValues, keyValues, prefixes);
 						}
 					
 						fileData.put(key, keyValues);
 					}
 					keyValues = new Vector<String>(); // reset our data
 
 					// We located our key! Save it for later!
 					key = pieceWithoutPrefix;
 					
 					if (!piecePrefix.equals("")) {
 						System.err.println ("Warning: in file " + path + " line " + lineNum + ": this piece has a prefix, but it is a key. Behavior for keys with prefixes is undefined in PMS, so we will ignore: " + piece);
 					}
 				} else {
 					// First run for TYPE_2D.
 					// This type is always on linePiece 1, functionally, so values needs only to be set once for the duration of the file
 					if (keyValues == null) {
 						keyValues = new Vector<String>();
 						key = "list"; // Key is always the same for 2D formats, making it store 2D in a 3D array.
 					}
 					
 					// Find values that need to append to our growing list
 					Vector<String> appendToKeyValues = new Vector<String>();
 					
 					if (pieceWithoutPrefix.startsWith("$") && this.type == TYPE_KEYVAL_BASH) {  // it's actually a variable, so expand it. // PMS 5.2.4
 						String variableName = pieceWithoutPrefix.substring(1, pieceWithoutPrefix.length()).replace("{","").replace("}",""); // cut off the $, and remove any curly brackets, since it means the same thing with or without
 
 						// Expand variable to that variable's value(s)
 						Vector<String> existingValues = new Vector<String>();
 						if (fileData.containsKey(variableName)) {
 							existingValues = fileData.get(variableName);
 						}
 						appendToKeyValues.addAll(existingValues);
 					} else {
 						// Someone trying to be fancy?
 						if (pieceWithoutPrefix.startsWith("$")) {
 							System.err.println ("Warning: in file " + path + " line " + lineNum + ": this piece looks like a variable, but this file does not allow variables: " + piece);
 						}
 						appendToKeyValues.add(piece);
 					}
 					keyValues = this.mergeValues(keyValues, appendToKeyValues, ""); // just use for dup-checking. Prefixes will be applied later. (Doing prefixes twice means the prefixes are ineffective.)
 					
 					// Append if we're allowed to, otherwise replace.
 					Vector<String> values;
 					if (this.inheritsCanAppend && (!this.inheritsCanAppendSpecialOnly || this.isSpecialKey(key))) {
 						// Get the existing values that we'll append to
 						Vector<String> existingValues = new Vector<String>();
 						if (fileData.containsKey(key)) {
 							existingValues = fileData.get(key);
 						} // otherwise we'll start a new one
 						values = this.mergeValues(existingValues, keyValues, prefixes);
 					} else {
 						values = keyValues;
 					}
 					fileData.put(key, values);
 				}
 			}
 			
 			environment.setCategoryKeys(this.fileName, fileData);
 			
 			if (br == null && line.equals("")) {
 				// single loop execution is done
 				return;
 			}
 		}
 	}
 	
 	private Vector<String> mergeValues (Vector<String> existingValues, Vector<String> keyValues, String prefixesToIgnore) {
 		// Locate duplicates and remove them before merging.
 		String[] wasKeyValues = new String[0];
 		wasKeyValues = keyValues.toArray(wasKeyValues); // make a light-weight copy of old values so we can modify existingValues
 		for (String newValue: wasKeyValues) {
 			String newValueWithoutPrefix = newValue;
 			if (!prefixesToIgnore.equals("")) {
 				newValueWithoutPrefix = newValue.replaceAll("^["+prefixesToIgnore+"]+", "");
 			}
 
 			if (!this.showMinus) {
 				int minusPrefixPos = newValue.indexOf('-');
 				if (prefixesToIgnore.indexOf('-') > -1 && minusPrefixPos > -1 && minusPrefixPos < newValue.length() - newValueWithoutPrefix.length()) {
 					// This has a - prefix, so it negates but does not appear in the list
 					keyValues.remove(newValue);
 				}
 			}
 			
 			String[] wasExistingValues = new String[0];
 			wasExistingValues = existingValues.toArray(wasExistingValues); // make a light-weight copy of old values so we can modify existingValues
 
 			for (String existingValue: wasExistingValues) {
 				String existingValueWithoutPrefix = existingValue;
 				if (!prefixesToIgnore.equals("")) {
 					existingValueWithoutPrefix = existingValue.replaceAll("^["+prefixesToIgnore+"]+", "");
 				}
 				if (newValueWithoutPrefix.equals(existingValueWithoutPrefix)) { // existingValue is overridden.
 					existingValues.remove(existingValue);
 				}
 			}
 
 		}
 		
 		Vector<String> values = new Vector<String>();
 		values.addAll(existingValues);
 		values.addAll(keyValues);
 		return values;
 	}
 	
 	private boolean isSpecialKey (String key) {
 		// This entire block is PMS 5.3.1
 		if (
 			key.equals("USE")
 			|| key.equals("USE_EXPAND")
 			|| key.equals("USE_EXPAND_HIDDEN")
 			|| key.equals("CONFIG_PROTECT")
 			|| key.equals("CONFIG_PROTECT_MASK")
 			) {
 			return true;
 		} else if (eapi == 4 && (
 			key.equals("IUSE_IMPLICIT")
 			|| key.equals("USE_EXPAND_IMPLICIT")
 			|| key.equals("USE_EXPAND_UNPREFIXED")
 			) ) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public String getPath () {
 		return this.path;
 	}
 	public int getEAPI () {
 		return this.eapi;
 	}
 	public String getFileName () {
 		return this.fileName;
 	}
 	public int getType () {
 		return this.type;
 	}
 	
 }
 
 class Profile {
 
 	private static HashMap<String, Vector<String>> allRelations = new HashMap<String, Vector<String>>(); // parent -> child
 
 	private String path = "";
 	private ProfileEnvironment environment = null;
 	private ProfileFile files[][];
 
 	private boolean verbose = false;
 	private boolean showMinus = false;
 	private String search = "";
 	private Vector<Profile> parents = new Vector<Profile>();
 
 	public Profile (String path, ProfileEnvironment environment, boolean verbose, boolean showMinus, String search) throws IOException, IllegalArgumentException {
 		this.path = path;
 		this.environment = environment;
 		this.verbose = verbose;
 		this.showMinus = showMinus;
 		this.search = search;
 		
 		File pathFile = new File(this.path);
 		String absolutePath = pathFile.getCanonicalPath(); // resolve symlinks, get absolute path. Used for cycle checking.
 		
 		if (!pathFile.exists()) {
 			System.err.println("Warning: profile at " + this.path + " doesn't exist!");
 		}
 		
 		this.files = new ProfileFile[][]{ // files are listed in rounds: the ones that need to load before one are listed in a round before that one
 			{ // round one: it is special in that it gets processed before we announce we are processing this profile
 				new ProfileFile( // PMS 5.2.1
 								this.path+"/parent",
 								ProfileFile.TYPE_2D,
 								ProfileFile.NOEXISTS_CAN_IGNORE,
 								ProfileFile.EXCEPT_NO_BLANK
 								+ ProfileFile.EXCEPT_NO_COMMENT
 								+ ProfileFile.EXCEPT_NO_LINE_CONT,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE, ProfileFile.LINEPIECE_TYPE_STRING
 									}
 								},
 								ProfileFile.INHERIT_NONE,
 								ProfileFile.DEPRECATED_NO,
 								this.verbose, this.showMinus, this.search
 				),
 				new ProfileFile( // PMS 5.2.2
 								this.path+"/eapi",
 								ProfileFile.TYPE_2D,
 								ProfileFile.NOEXISTS_CAN_BLANK,
 								ProfileFile.EXCEPT_NO_BLANK
 								+ ProfileFile.EXCEPT_NO_COMMENT
 								+ ProfileFile.EXCEPT_NO_LINE_CONT,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE, ProfileFile.LINEPIECE_TYPE_NUMBER
 									}
 								},
 								ProfileFile.INHERIT_NONE,
 								ProfileFile.DEPRECATED_NO,
 								this.verbose, this.showMinus, this.search
 				),
 			},
 			{ // round two
 				new ProfileFile( // PMS 5.2.3
 								this.path+"/deprecated",
 								ProfileFile.TYPE_2D,
 								ProfileFile.NOEXISTS_CAN_IGNORE,
 								ProfileFile.EXCEPT_NO_BLANK
 								+ ProfileFile.EXCEPT_NO_COMMENT
 								+ ProfileFile.EXCEPT_NO_LINE_CONT,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE, ProfileFile.LINEPIECE_TYPE_STRING
 									}
 								},
 								ProfileFile.INHERIT_NONE,
 								ProfileFile.DEPRECATED_NO,
 								this.verbose, this.showMinus, this.search
 				),
 				new ProfileFile( // PMS 5.2.4
 								this.path+"/make.defaults",
 								ProfileFile.TYPE_KEYVAL_BASH,
 								ProfileFile.NOEXISTS_CAN_IGNORE,
 								0,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE, ProfileFile.LINEPIECE_TYPE_STRING
 									},
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE+ProfileFile.LINEPIECE_PREFIX_SPECIAL_ONLY+ProfileFile.LINEPIECE_PREFIX_MINUS, ProfileFile.LINEPIECE_TYPE_STRING
 									}
 								},
 								ProfileFile.INHERIT_PARENT+ProfileFile.INHERIT_APPEND_SPECIAL_ONLY,
 								ProfileFile.DEPRECATED_NO,
 								this.verbose, this.showMinus, this.search
 				),
 				new ProfileFile( // PMS 5.2.5
 								this.path+"/virtuals",
 								ProfileFile.TYPE_3D,
 								ProfileFile.NOEXISTS_CAN_IGNORE,
 								ProfileFile.EXCEPT_NO_LINE_CONT,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE, ProfileFile.LINEPIECE_TYPE_PACKAGE
 									},
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE, ProfileFile.LINEPIECE_TYPE_PACKAGE
 									}
 								},
 								ProfileFile.INHERIT_PARENT+ProfileFile.INHERIT_NO_APPEND,
 								ProfileFile.DEPRECATED_YES,
 								this.verbose, this.showMinus, this.search
 				),
 				new ProfileFile( // PMS 5.2.7
 								this.path+"/packages",
 								ProfileFile.TYPE_2D,
 								ProfileFile.NOEXISTS_CAN_IGNORE,
 								ProfileFile.EXCEPT_NO_LINE_CONT,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE+ProfileFile.LINEPIECE_PREFIX_MINUS+ProfileFile.LINEPIECE_PREFIX_ASTERIK, ProfileFile.LINEPIECE_TYPE_PACKAGE
 									}
 								},
 								ProfileFile.INHERIT_PARENT,
 								ProfileFile.DEPRECATED_NO,
 								this.verbose, this.showMinus, this.search
 				),
 				new ProfileFile( // PMS 5.2.9
 								this.path+"/package.mask",
 								ProfileFile.TYPE_2D,
 								ProfileFile.NOEXISTS_CAN_IGNORE,
 								ProfileFile.EXCEPT_NO_LINE_CONT,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE+ProfileFile.LINEPIECE_PREFIX_MINUS, ProfileFile.LINEPIECE_TYPE_PACKAGE
 									}
 								},
 								ProfileFile.INHERIT_PARENT,
 								ProfileFile.DEPRECATED_NO,
 								this.verbose, this.showMinus, this.search
 				),
 				new ProfileFile( // PMS 5.2.10
 								this.path+"/package.provided",
 								ProfileFile.TYPE_2D,
 								ProfileFile.NOEXISTS_CAN_IGNORE,
 								ProfileFile.EXCEPT_NO_LINE_CONT,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE, ProfileFile.LINEPIECE_TYPE_PACKAGE
 									}
 								},
 								ProfileFile.INHERIT_PARENT,
 								ProfileFile.DEPRECATED_NO,
 								this.verbose, this.showMinus, this.search
 				),
 				new ProfileFile( // PMS 5.2.11
 								this.path+"/package.use",
 								ProfileFile.TYPE_3D,
 								ProfileFile.NOEXISTS_CAN_IGNORE,
 								ProfileFile.EXCEPT_NO_LINE_CONT,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE, ProfileFile.LINEPIECE_TYPE_PACKAGE
 									},
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE+ProfileFile.LINEPIECE_PREFIX_MINUS, ProfileFile.LINEPIECE_TYPE_STRING
 									}
 								},
 								ProfileFile.INHERIT_PARENT,
 								ProfileFile.DEPRECATED_NO,
 								this.verbose, this.showMinus, this.search
 				),
 				new ProfileFile( // PMS 5.2.12
 								this.path+"/package.use.force",
 								ProfileFile.TYPE_3D,
 								ProfileFile.NOEXISTS_CAN_IGNORE,
 								ProfileFile.EXCEPT_NO_LINE_CONT,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE, ProfileFile.LINEPIECE_TYPE_PACKAGE
 									},
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE+ProfileFile.LINEPIECE_PREFIX_MINUS, ProfileFile.LINEPIECE_TYPE_STRING
 									},
 								},
 								ProfileFile.INHERIT_PARENT,
 								ProfileFile.DEPRECATED_NO,
 								this.verbose, this.showMinus, this.search
 				),
 				new ProfileFile( // PMS 5.2.12
 								this.path+"/package.use.mask",
 								ProfileFile.TYPE_3D,
 								ProfileFile.NOEXISTS_CAN_IGNORE,
 								ProfileFile.EXCEPT_NO_LINE_CONT,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE, ProfileFile.LINEPIECE_TYPE_PACKAGE
 									},
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE+ProfileFile.LINEPIECE_PREFIX_MINUS, ProfileFile.LINEPIECE_TYPE_STRING
 									},
 								},
 								ProfileFile.INHERIT_PARENT,
 								ProfileFile.DEPRECATED_NO,
 								this.verbose, this.showMinus, this.search
 				),
 				new ProfileFile( // PMS 5.2.12
 								this.path+"/use.force",
 								ProfileFile.TYPE_2D,
 								ProfileFile.NOEXISTS_CAN_IGNORE,
 								ProfileFile.EXCEPT_NO_LINE_CONT,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE+ProfileFile.LINEPIECE_PREFIX_MINUS, ProfileFile.LINEPIECE_TYPE_STRING
 									}
 								},
 								ProfileFile.INHERIT_PARENT,
 								ProfileFile.DEPRECATED_NO,
 								this.verbose, this.showMinus, this.search
 				),
 				new ProfileFile( // PMS 5.2.12
 								this.path+"/use.mask",
 								ProfileFile.TYPE_2D,
 								ProfileFile.NOEXISTS_CAN_IGNORE,
 								ProfileFile.EXCEPT_NO_LINE_CONT,
 								new int[][] {
 									{
 										ProfileFile.LINEPIECE_PREFIX_NONE+ProfileFile.LINEPIECE_PREFIX_MINUS, ProfileFile.LINEPIECE_TYPE_STRING
 									}
 								},
 								ProfileFile.INHERIT_PARENT,
 								ProfileFile.DEPRECATED_NO,
 								this.verbose, this.showMinus, this.search
 				)
 			},
 		};
 
 		this.mergeFilesToEnvironment(this.files[0], this.environment);
 		
 		Vector<String> existingParents = new Vector<String>(); // by default blank, unless the key exists
 		if (this.allRelations.containsKey(absolutePath)) {
 			existingParents = this.allRelations.get(absolutePath);
 		}
 		Vector<String> parents = this.environment.getVars("parent", "list");
 		for (String parent: parents) {
 			String absoluteParent = new File(this.path+"/"+parent).getCanonicalPath();
 			if (existingParents.contains(absoluteParent)) { // PMS 5.2.1
 				throw new IllegalArgumentException("Found a cycle, which results in a broken profile: child path " + this.path + " is trying to include parent path " + parent + " more than once.");
 			}
 			
 			existingParents.add(absoluteParent);
 			this.parents.add(new Profile(this.path+"/"+parent, this.environment, this.verbose, this.showMinus, this.search));
 		}
 		this.allRelations.put(absolutePath, existingParents);
 
 		if (verbose) System.out.println("Investigating profile at " + path); // we do this after because parents do investigation first // PMS 5.2.1
 		this.mergeFilesToEnvironment(this.files[1], this.environment);
 	}
 	
 	public void mergeFilesToEnvironment(ProfileFile[] files, ProfileEnvironment environment) throws FileNotFoundException, IOException {
 		for (ProfileFile currFile: files) {
 			currFile.mergeToEnvironment(environment);
 		}
 	}
 	
 	public String getHumanOutput () {
 		String results = "";
 		Set<String> leftOverCategoryNames = this.environment.getCategories().keySet();
 		
 		for (ProfileFile[] round: this.files) {
 			for (ProfileFile file: round) {
 				String fileName = file.getFileName();
 				results += "###### " + fileName + "\n";
 				if (file.getType() == ProfileFile.TYPE_2D) {
 					Vector<String> values = this.environment.getVars(fileName, "list");
 					results += Profile.array2DToString(values, "", "\n", "\n") + "\n";
 					leftOverCategoryNames.remove(fileName);
 				} else if (file.getType() == ProfileFile.TYPE_3D) {
 					HashMap<String,Vector<String>> category = this.environment.getCategoryKeys(fileName);
 					results += Profile.array3DToString(category, " ", " ", "") + "\n";
 					leftOverCategoryNames.remove(fileName);
 				} else if (file.getType() == ProfileFile.TYPE_KEYVAL_BASH) {
 					HashMap<String,Vector<String>> category = this.environment.getCategoryKeys(fileName);
 					results += Profile.array3DToString(category, "=\"", " ", "\"") + "\n";
 					leftOverCategoryNames.remove(fileName);
 				}
 			}
 		}
 		
 		if (verbose) {
 			// Warn about unhandled data
 			for (String categoryName: leftOverCategoryNames) {
 				HashMap<String,Vector<String>> keys = this.environment.getCategoryKeys(categoryName);
 				Set<String> keyNames = keys.keySet();
 				for (String keyName: keyNames) {
 					System.err.println("Warning: unhandled data: " + keyName);
 				}
 			}
 		}
 		return results;
 	}
 	
 	public static String array2DToString (Vector<String> array, String begin, String valueSeperator, String end) {
 		String results = "";
 		results += begin;
 		for (int v = 0; v < array.size(); v++) {
 			results += array.get(v);
 			if (v+1 < array.size()) { results += valueSeperator; }
 		}
 		results += end;
 		return results;
 	}
 	
 	public static String array3DToString (HashMap<String,Vector<String>> array, String beginSeperator, String valueSeperator, String endSeperator) {
 		String results = "";
 		Iterator arrays = array.keySet().iterator();
 		while (arrays.hasNext()) {
 			String arrayName = (String)arrays.next();
 			Vector<String> currAarrray = array.get(arrayName);
 			if (currAarrray.size() > 0) {
 				results += array2DToString(array.get(arrayName), arrayName+beginSeperator, valueSeperator, endSeperator+"\n");
 			}
 		}
 		return results;
 	}
 }
