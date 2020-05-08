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
 
 /**
  * Entry point for generated EBNF class.
  *
  * This file is not included in the final jar-package.
  * It is intended for testing the generated EBNF-class, and
  * will be compiled and called only during testing.
  *
  * Derived from main() in class EBNFInitial.
  */
 public class EBNFMain {
 
 	public static void main(String[] argv) 
 		throws NoSuchMethodException, IllegalAccessException {
 
 		String nodesFile = "nodes.txt";
 		boolean parseOnly = false;
 		String inFile;
 
 		// Handle parameters, if any:
 		//
 		//	-p file	- parse only, quit after doing parse stage
 		//			- parse tree is outputted to given file
 		//	file	- input (ebnf) file to parse
 		int curarg = 0;
 		while ( curarg < argv.length -1 ) {
 			if ( "-p".equals(argv[curarg]) ) {
 				parseOnly = true;
 				nodesFile = argv[curarg + 1];
 				curarg += 2;
 			} else {
 				Util.error ("Unknown option '" + argv[curarg] + "'" );
 				curarg += 1;
 			}
 		}
 
 		inFile   = argv[curarg];
 
 		BasicParser parser = new EBNF( inFile );
 		//NOTE: Following is the ruby parser!!!!!
 		//BasicParser parser = new EBNFruby( inFile );
		parser.setTraceLevel( 5); //Util.TRACE );
 		parser.setFirstTwoLines(true);
 		State state = parser.parse();
 
 		if( parseOnly) {
 			Util.info("Doing parse only.");
 			parser.saveNodes( state, nodesFile );
 			parser.showFinalResult(state);
 			return;
 		}
 		parser.saveNodes( state, "nodes_parse.txt" );
 
 		// Skip rest of steps if  error occured during parsing stage
 		if ( state.hasErrors() ) {
 			Util.info( "Errors occured during parsing; skipping translation and generation.");
 		} else {
 			try {
 				Validator  validator  = new EBNFValidator();
 				Translator translator = new EBNFTranslator();
 				Generator  generator  = new EBNFGenerator();
 
 				// Validate parse tree
 				validator.validate( state );
 
 				// Do node translations
 				Util.info("Doing translate.");
 				translator.translate( state );
 				parser.saveNodes( state, "after_translate.txt" );
 	
 				// Create output
 				generator.generate( state, null );
 	
 			} catch( ParseException e ) {
 				Util.error("Error during validation/translation/generation: " + e.getMessage() );
 
 				// Nodes output may be handy for debugging
 				parser.saveNodes( state, nodesFile );
 				System.exit(1);
 			}
 		}
 
 		parser.saveNodes( state, nodesFile );
 		parser.showFinalResult(state);
 		if ( state.hasErrors() ) {
 			System.exit(1);
 		}
 	}
 }
