 /**
  * Copyright 2012 Wim Rijnders <wrijnders@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *=========================================================================
  */
 package nl.axizo.EBNF;
 
 import nl.axizo.parser.*;
 import java.io.IOException;
 import java.io.File;
 import java.util.Vector;
 import java.util.Map;
 import java.util.Hashtable;
 
 /**
  *  Generates output from a parse tree.
  *
  * The parse tree must have been created with an EBNF parser.
  */
 public class EBNFGenerator extends Generator {
 
 	/**
  	 * Generate output from the current node tree.
  	 */
 	public void generate( State state, String outfile ) throws ParseException {
 		String output = "";
 
 		Node root = state.getCurNode();
 
 		String packageName     = root.get("language").get("package_name").getValue();
 		String className       = root.get("language").get("label").getValue();
 		Node   member_patterns = root.get("temp").get("members");
 		Node   init_patterns   = root.get("temp").get("ctor");
 
 
 		if ( packageName != "" ) {
 			// Remove trailing dot
 			packageName = packageName.substring( 0, packageName.length() -1 );
 		} else {
 			// Use default value
 			packageName = "nl.axizo.EBNF";
 		}
 
 		// parameter value is ignored, we derive the filename from the
 		// class name in the parsed data.
 	    outfile = className + ".java";
 
 		Map actionMap = getActions(root);
 
 		///////////////////////////////////
 		// Perform the output generation
 		///////////////////////////////////
 	
 
 		// Create the file header.	
 		output += 
 				  "/* THIS FILE IS GENERATED!\n"
 				+ " *\n"
 				+ " * Editing it is a bad idea.\n"
 				+ " */\n"
 				+ "package " + packageName + ";\n"
 				+ "\n"
 				+ "import nl.axizo.parser.*;\n"
 				+ "import java.util.regex.*;\n"
 				+ "\n"
 				+ "\n"
 				+ "public class " + className + " extends BasicParser {\n\n";
 
 		output += generateState(root);
 
 		int num = member_patterns.numChildren();
 		for( int i = 0;  i < num; ++i ) {
 			output += member_patterns.get(i).getValue();
 		}
 
 		//
 		// Generate the constructors
 		// 
 		output += "\n\n\t" + "public " + className + "(String buffer, boolean loadFromFile) {\n"
 				+ "\t\tsuper(buffer, loadFromFile);\n\n";
 
 		num = init_patterns.numChildren();
 		for( int i = 0;  i < num; ++i ) {
 			output += init_patterns.get(i).getValue();
 		}
 
 		output += "\t}\n\n";
 		output += "\n\t" + "public " + className + "(byte [] buffer, boolean loadFromFile) {\n"
 				+ "\t\tthis( new String( buffer), loadFromFile);\n";
 		output += "\t}\n\n";
 		output += "\n\t" + "public " + className + "(String filename) {\n"
 				+ "\t\tthis(filename, true);\n";
 		output += "\t}\n\n";
 
 		//
 		// If WS_intern not present, add default implementation
 		//
 		// This is actually WS (if present), renamed to WS_intern
 		// in the Translator step.
 		//
 		
 		//Check if WS is present
 		Vector ws_intern = root.findNodesByValue("WS_intern");
 		if ( ws_intern.size() == 0 ) {
 			Util.info("No internal WS present. Creating default implementation.");
 
 			output += "\tpublic boolean WS_intern(State state ) throws ParseException {\n"
 					+ "\t\treturn super.WS_intern(state);\n"
 					+ "\t}\n\n";
 		}
 
 
 		//
 		// Generate methods
 		//
 		Vector rules = root.findNodes("rule");
 		for( int i = 0;  i < rules.size(); ++i ) {
 			Node rule = (Node) rules.get(i);
 			output += generateMethod( rule, actionMap );
 		}
 
 
 		//
 		// Add native code, if present
 		// 
 		Node native_code = root.get("language").get("native_code");
 		if ( !native_code.isNull() ) {
 			output += "\n\t// Start native code\n"
 					+ native_code.getValue()
 					+ "\n// End native code\n\n";
 		}
 		
 		output += generateEntryPoint(root ); 
 
 		output += "}\n";
 
 		// Save what we got
 		try {
 			String packageName2 = root.get("language").get("package_name").getValue();
 			if ( packageName2 != "" ) {
 				// Package specified. Create the output path
 
 				// Remove trailing dot
 				packageName2 = packageName2.substring( 0, packageName.length() );
 				Util.info( "packageName2: " + packageName2 );
 				String path = packageName2.replace( '.', '/' );
 
 				boolean success = (new File(path)).mkdirs();
 
 				if ( !success ) {
 					Util.warning( "Error during creation of path '" + path + 
 						"', continuing anyway.");
 				} else {
 					Util.info( "Creating path '" + path + "' succeeded." );
 				}
 
 				// Save the file
 				Util.saveFile( path + "/" + outfile, output );
 			} else {
 				// No path specified. Do a regular save
 				Util.saveFile( outfile, output );
 			}
 		} catch( IOException e) {
 			String errMsg = "error while saving '" + outfile + "': " + e.toString();
 			Util.error( errMsg );
 			throw new ParseException( errMsg);
 		}
 	}
 
 
 	protected String generateAlternative( Node n, boolean doWS, boolean isFirst ) {
 		String out = "";
 
 		if ( doWS && !isFirst ) out += "\t\tWS(state);\n";
 
 		if ( n.numChildren() == 1 ) {
 			String value =  n.get("call").getValue();
 			int whileIndex =  value.indexOf( "do; while" );
 			boolean isWhileLoop = ( whileIndex != -1 );
 
 			// Special case for loop, need to take the endbrace into
 			// account. This is sort of dirty. TODO: examine better solution
 			if ( isWhileLoop ) {
 				// While-loops at start of statement need to do WS regardless
 				// isFirst therefore ignored here.
 				if ( doWS ) {
 					// Dirty: insert WS handling in while loop
 					int len = "do; while".length();
 					int endIndex = whileIndex + len;
 					value = value.substring(0, whileIndex ) 
 							+ "do WS(state); while" 
 							+ value.substring(endIndex);
 				}
 
 				// while-loop 
 				out += "\t\t" + value + " ));\n";
 			} else {
 
 				// normal statement
 				boolean mustReturn = true;
 
 				String throwParams = "";
 
 				// If parameters were passed, use those instead
 				Node param1 = n.get("call").get("param1");
 				Node param2 = n.get("call").get("param2");
 				Node raise = n.get("call").get("raise");
 
 				boolean isNot = !n.get("call").get("not").isNull();
 
 				if ( isNot ) {
 					param2 = new Node( "param2", "true");
 				}
 
 				// Param1 overrides default value
 				if ( !param1.isNull() ) {
 					mustReturn = Boolean.getBoolean( param1.getValue() );
 				}
 
 				if ( !raise.isNull() ) throwParams += "," + raise.getValue();
 				else throwParams += ", false";
 
 				//Param2 gets appended
 				if ( !param2.isNull() ) throwParams += "," + param2.getValue();
 
 				if ( !( param1.isNull() || param2.isNull() ) ) {
 					Util.info("Detected parameters '" + throwParams 
 							+ "' for call to '" + value + "'");
 				}
 
 
 				if ( isNot ) {
 					Util.info("Not-operator detected");
 
 					// Not-operator inverts the return value of the call
 					// result should not be added to the parser tree
 					if ( !mustReturn ) {
 						value = "!" + value;
 					}
 				} else {
 					if ( mustReturn ) {
 						value = "!" + value;
 					}
 				}
 
 				if ( mustReturn ) {
 					out += "\t\tif( " + value + throwParams + " ) ) return false;\n";
 				} else {
 					out += "\t\t" + value + throwParams + " );\n";
 				}
 			}
 		} else {
 			// more than one child
 			out += "\t\tif( !(\n";
 
 			for( int i = 0; i < n.numChildren(); ++ i ) {
 				Node c = n.get( i );
 
 				if ( !"call".equals( c.getKey() ) ) {
 					// TODO: enable alternatives for non-calls.
 					//       this would be while-loops
 					Util.warning( "non-call found while generating alternative" );
 					continue;
 				}
 
 				if ( i != 0 ) {
 					out += "\t\t || ";
 				} else {
 					out += "\t\t    ";
 				}
 
 				out += c.getValue();
 
 /*
 				// Only final statement in if-block gets to throw
 				boolean doThrow = ( i == n.numChildren() -1 );
 
 				if ( doThrow ) {
 					out += ", true";
 				}
 */
 				out += " )\n";
 			}
 
 
 			//out += "\t\t);\n";
 			out += "\t\t) ) return false;\n";
 		}
 
 		return out;
 	}
 
 	protected String generateStatements( Node n ) {
 		Node body = n.get("statements");
 		String out = "";
 		boolean isFirst = true;
 
 		// Special case, WS_intern should not handle whitespace 
 		// internally
 		boolean isWS_intern = "WS_intern".equals( n.get("label").getValue());
 		boolean warnedAboutWS = false;
 
 		out += "\t\t// Generated parsing code\n";
 		for( int i = 0; i < body.numChildren(); ++ i ) {
 			Node c = body.get( i );
 
 			if ( !"alternative".equals( c.getKey() ) ) {
 				//WS modifier is allowed; skip warning in this case
 				if ( !"modifier_WS".equals( c.getKey() ) ) {
 					Util.warning( "non-alternative found while generating statements"
 							+ " for rule " + n.get("label").getValue() );
 				}
 				continue;
 			}
 
 			// Flag for determining if whitespace should be handled automatically
 			boolean doWS = "skipWS".equals( c.getValue() );
 
 			if ( doWS && isWS_intern && !warnedAboutWS ) {
 				Util.warning( "Disabling WS handling within WS function.");
 				warnedAboutWS = true;
 			}
 
 			if ( isWS_intern) doWS = false;
 
 			out += generateAlternative( c, doWS, isFirst );
 			isFirst = false;
 		}
 
 		return out;
 	}
 
 	private static boolean modifierPresent( Node rule, String name ) {
 		Vector<Node> modifiers =rule.get("rule_modifier").findNodes("string");
 		for ( Node n: modifiers ) {
 			if ( name.equals( n.getValue() ) ) return true;
 		}
 		return false;
 	}
 
 	/**
  	 * Determine is passed rule node has a token modifier.
  	 *
  	 * @param rule node to check for token modifier.
  	 * @return true if token modifier present, false otherwise.
  	 */
 	protected static boolean isTokenRule( Node rule ) {
 		return modifierPresent( rule, "token" );
 	}
 
 
 	/**
  	 * Determine if passed rule node has a skip modifier.
  	 *
  	 * A rule with a skip modifier gets hidden in the output,
  	 * while the generated children of the rule are retained.
  	 *
  	 * @param rule node to check for skip modifier.
  	 * @return true if skip modifier present, false otherwise.
  	 */
 	protected static boolean skipThisRule( Node rule ) {
 		return modifierPresent( rule, "skip" );
 	}
 
 
 	/**
  	 * Determine if passed rule node has an ignore modifier.
  	 *
  	 * A rule with an ignore modifier is not outputted,
  	 * including any child nodes.
  	 * 
  	 * This modifier can be used for e.g. comments in input.
  	 *
  	 * @param rule node to check for ignore modifier.
  	 * @return true if ignore modifier present, false otherwise.
  	 */
 	protected static boolean ignoreThisRule( Node rule ) {
 		return modifierPresent( rule, "ignore" );
 	}
 
 	protected static boolean raiseThisRule( Node rule ) {
 		return modifierPresent( rule, "raise" );
 	}
 
 
 	/**
  	 * Generate method code from given Node.
  	 */
 	protected String generateMethod( Node rule, Map actionMap ) {
 		String name   = rule.get("label").getValue();
 		String output = "";
 
 		output += "\tpublic boolean " + name + "(State state ) throws ParseException {\n" +
 				"\t\ttrace(" + (Util.TRACE -5) + ",\"Called method '" + name + "'.\");\n\n";
 
 		Node pre_node  = null;
 		Node post_node = null;
 		if ( actionMap.containsKey( name ) ) {
 			// Actions present, add a handy reference for the current token.
 			output += "\t\tNode current = state.getCurNode();\n\n";
 
 			Node action = (Node) actionMap.get( name );
 
 			pre_node  = action.get("pre_block" ).get("code_block");
 			if ( pre_node.isNull() ) pre_node = null; 
 
 			post_node = action.get("post_block").get("code_block");
 			if ( post_node.isNull() ) post_node = null; 
 		}
 
 		// Ignore rule flag needs to be set before statements are generated
 		if ( ignoreThisRule( rule ) ) {
 			output += "\t\t// Block output of current node and its children\n" +
 				"\t\tstate.setIgnoreCurrent( true );\n\n";
 		}
 
 		if ( pre_node != null ) {
 			output += "\t\t// Pre actions\n"
 					+ pre_node.getValue()
 					+ "\n\t\t// End Pre actions\n\n";
 		}
 
 		output += generateStatements( rule );
 
 		if ( isTokenRule( rule) ) {
 			output += "\n\t\tstate.getCurNode().collect();\n";
 		}
 
 		if ( post_node != null ) {
 			output += "\n\t\t// Post actions\n"
 					+ post_node.getValue()
 					+ "\n\t\t// End Post actions\n";
 		}
 
 
 		if ( skipThisRule( rule ) ) {
 			output += "\n\t\t// replace this node with its children\n" +
 						"\t\tstate.setSkipCurrent( true );\n";
 		}
 
 		output += "\n\t\ttrace(\"Completed method '" + name + "'; value: \" +" + 
 			"state.getCurNode().getValue() + \".\");\n";
 
 		output += "\n\t\treturn true;\n\t}\n\n";
 
 		return output;
 	}
 
 	private String generateState( Node root ) throws ParseException {
 		String out = "";
 
 		Node stateNode = root.get("language").get("stateblock");
 
 		if ( stateNode.isNull() ) {
 			Util.info("No stateblock found, skipping generation.");
 			return out;
 		} else {
 			Util.info("Stateblock found.");
 		}
 
 		out += "\t// Start State variables\n";
 
 		Vector vars = stateNode.findNodes( "statevar" );
 		for( int i = 0; i < vars.size(); ++ i ) {
 			Node n = (Node) vars.get(i);
 
 			out += "\tprivate " + n.get("type_state").getValue()
 				+  " " + n.get("member_state").getValue();
 
 			if ( !n.get("param_init").isNull() ) {
 				out += " = " +  n.get("param_init").getValue();
 			}
 
 			out += ";\n";
 		}
 
 		out += "\t// End State variables\n\n";
 
 		return out;
 	}
 
 
 	/**
  	 * Detect entry point for the parse and generate code for calling it.
  	 * 
  	 * @throws ParseException if no entry point found.
  	 */
 	protected String generateEntryPoint( Node root ) throws ParseException {
 
 		// Find the entry  point
 		Vector<Node> rule_modifiers = root.findNodes( "rule_modifier" );
 		Node entry_node = null;
 		int	 entry_count = 0;
 		for( int i = 0;  i < rule_modifiers.size(); ++i ) {
 			Node mod = rule_modifiers.get(i);
 
 			if ( "entry".equals( mod.get("string").getValue() ) ) {
 				entry_node = mod.getParent();
 				entry_count++;
 			} 
 		}
 
 		if ( entry_node == null ) {
 			throw new ParseException( "No entry point found.");
 		}
 
 		if ( entry_count > 1 ) {
 			throw new ParseException( "too many entry points.");
 		}
 
 		String entry_label = entry_node.get("label").getValue();
 		Util.info( "Entry point found; label: '" + entry_label + "'");
 
 		// Generate the corresponding code.
 		String out =
 		"	public State parse() {\n" +
 //		"		throws NoSuchMethodException, IllegalAccessException {\n" +
 		"\n" +
 		"		State state = new State();\n" +
 		"\n" +
 		"		// Parse according to rules\n" +
 		"		try {\n" +
 		"			while ( !eol(state.getCurpos() ) ) {\n" +
 		"				WS(state);\n" +
 		"\n" +
 		"				if ( !s( \"" + entry_label + "\", state ) ) {\n" +
//		"					state.setError( \"end of parsing\" );\n" +
 		"					break;\n" +
 		"				}\n" +
 		"			}\n" +
 		"		} catch ( ParseException e ) {\n" +
 		"			error( \"Exception: \" + e.toString() );\n" +
 		"		}\n" +
 		"\n" +
 		"		return state;\n" +
 		"	}\n\n";
 
 		return out;
 	} 
 
 
 	/**
  	 * Create a Map of all actions.
  	 *
  	 * Actions need to be associated with the corresponding rules.
  	 * Since the rules are iterated over, a map for the actions 
  	 * is sensible. This cuts down the search time for actions.
  	 */
 	protected Map getActions(Node root) {
 		Map ret = new Hashtable();
 
 		Vector res =  root.findNodes( "action" );
 		for( int i = 0;  i < res.size(); ++i ) {
 			Node n = (Node) res.get(i);
 
 			// Action name used  as key
 			String ruleName = n.get("label").getValue();
 
 			ret.put( ruleName, n);
 		}
 
 		return ret;
 	}
 }
 
