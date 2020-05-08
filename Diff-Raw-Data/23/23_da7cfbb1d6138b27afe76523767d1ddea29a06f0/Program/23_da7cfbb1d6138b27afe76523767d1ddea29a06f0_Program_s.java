 package yapl;
 /**** YAPL Compiler
 *
 *	Copyright 2010 Daniel Hlbling, Klagenfurt University
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *       
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 ***/
 
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import yapl.interfaces.CompilerError;
 
 public class Program {
 	public static void main(String[] args) {
 		if (args.length != 1) {
 			writeUsage();
 			return;
 		}
 		try {
			System.out.print("YAPL compilaton: " + args[0]);
		    FileInputStream fis = new FileInputStream(args[0]);
 		    yapl parser = new yapl(fis);
 		    parser.Start();
		    System.out.println(" OK");
 		} catch (ParseException e) {
			writeError(e);
			e.printStackTrace();
 		} catch (TokenMgrError e){
			writeError(e);
 		}
 		catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 	
	private static void writeError(CompilerError error){
		System.out.println(" ERROR " + error.errorNumber() + 
				" (line " + error.line() + ", column " + error.column() + ")");
 	}
 
 	private static void writeUsage() {
 		System.out.println("YAPL-Compiler: No Input file specified.\n" +
 		  "Usage: compilerbau <inputfile.yapl >");
 	}
 }
