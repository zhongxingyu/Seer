 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 import java.util.Scanner;
 
 import net.sf.okapi.common.filterwriter.GenericContent;
 import net.sf.okapi.lib.translation.ITMQuery;
 import net.sf.okapi.lib.translation.QueryResult;
 import net.sf.okapi.tm.globalsight.GlobalSightTMConnector;
 import net.sf.okapi.tm.globalsight.Parameters;
 import net.sf.okapi.tm.opentran.OpenTranTMConnector;
 
 public class Main {
 	public static void main (String[] args) {
 		try {
 			/* OpenTran (http://en.fr.open-tran.eu/) is a repository of translations
 			 * mostly from open-source software projects.
 			 * Okapi provide a connector to easily query the server.
 			 */
 			ITMQuery connector = new OpenTranTMConnector();
 			connector.setLanguages("en", "fr");
 			connector.open();
 			String query = "Open the file";
 			connector.query(query);
 			QueryResult res;
 			System.out.println("------------------------------------------------------------");
 			System.out.println(String.format("OpenTran-TM results for \"%s\":", query));
 			while ( connector.hasNext() ) {
 				res = connector.next();
 				System.out.println("- Source: " + res.source.toString());
 				System.out.println("  Target: " + res.target.toString());
 			}
 
 			/* Another implementation of the connector interface is the one for
 			 * GlobalSight. GlobalSight is an open-source GMS project that offers
 			 * a TM server and a Web service to access it.
 			 * If you have access to a GlobalSight TM server, the following calls
 			 * will allow you to query it.
 			 */
 			connector = new GlobalSightTMConnector();
 			Parameters params = new Parameters();
 			Scanner scanner = new Scanner(System.in);
 			
 			System.out.println("------------------------------------------------------------");
 			System.out.println("Example of querying a GlobalSight TM server");
 			System.out.println("You need to have access to a GlobalSight TM server for this.");
 			System.out.print("Do you want to continue (y or n + Enter)? ");
 			String tmp = scanner.nextLine();
 			if (( tmp.length() == 0 ) || (( tmp.charAt(0) != 'y') && ( tmp.charAt(0) != 'Y' ))) {
 				// Stop demo here
 				return;
 			}
 			
 			System.out.print("Service URL (e.g. http://myserver:8080/globalsight/services/AmbassadorWebService?wsdl) = ");
			params.setServerURL(scanner.nextLine());
 			System.out.print("Username = ");
			params.setUsername(scanner.nextLine());
 			System.out.print("Password = ");
			params.setPassword(scanner.nextLine());
 			System.out.print("TM Profile name = ");
			params.setTmProfile(scanner.nextLine());
 			System.out.print("Source language (e.g. en) = ");
 			String srcLang = scanner.nextLine();
 			System.out.print("Target language (e.g. fr) = ");
 			String trgLang = scanner.nextLine();
 			
 			connector.setLanguages(srcLang, trgLang);
 			connector.setParameters(params);
 			connector.open();
 			
 			GenericContent fmt = new GenericContent();
 			System.out.print("Text to query = ");
 			query = scanner.nextLine();
 			connector.query(query);
 			System.out.println(String.format("--- GlobalSight-TM results for \"%s\":", query));
 			while ( connector.hasNext() ) {
 				res = connector.next();
 				System.out.println(String.format("- Score: %d%%", res.score));
 				System.out.println(" Source: " + fmt.setContent(res.source).toString());
 				System.out.println(" Target: " + fmt.setContent(res.target).toString());
 			}
 			
 		}
 		catch ( Throwable e ) {
 			e.printStackTrace();
 		}
 	}
 
 }
