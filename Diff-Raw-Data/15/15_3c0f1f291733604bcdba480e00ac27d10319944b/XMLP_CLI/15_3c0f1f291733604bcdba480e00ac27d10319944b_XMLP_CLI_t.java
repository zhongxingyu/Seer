 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mlparch;
 
 import java.text.ParseException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  *
  * @author john
  */
 public class XMLP_CLI {
 	public static void showHelp() {
 		System.out.println("XMLPatch XML patching utility");
 		System.out.println("Public domain code, released 2012");
 		System.out.println("Options:");
		System.out.println("    -p - apply a patch file (default)");
 		System.out.println("    -q <arg> - do an XPath query");
		System.out.println("    -t <arg> - set target file for patch or query (default \"xmlpatch.xml\")");
		System.out.println("    -p <arg> - set root directory for patch files (default \"extract\")");
 		System.out.println("    -v - show this help");
 		System.out.println("    -? - show this help");
 		System.out.println("    --help - show this help");
 	}
 	public static void main(String[] args) throws Exception {
 		String targName = "xmlpatch.xml";
 		String pdirName = "extract";
 		String query = null;
		int mode = 0; //0 == patch, 1 == query
 		
 		for (int i = 0; i < args.length; i++) {
 			String arg0 = args[i];
 			if (arg0.startsWith("--")) {
 				//long option
 				arg0 = arg0.substring(2);
 				
 				if (arg0.equals("help")) {
 					showHelp(); System.exit(0);
 				} else {
 					throw new IllegalArgumentException("Unrecognized long option: '"+arg0+"'.");
 				}
 			} else if (arg0.charAt(0) == '-') {
 				//short option
 				arg0 = arg0.substring(1);
 				
 				for (int j = 0; j < arg0.length(); j++) {
 					//short optiona may be stacked
 					char opt = arg0.charAt(j);
 					
 					switch (opt) {
 						case 'v':
 						case '?':
 							showHelp(); System.exit(0);
						case 'p':
							mode = 0;
							break;
 						case 'q':
 							if (j!=arg0.length()-1)
 								throw new IllegalArgumentException("'q' short option must be last in a stack!");
 							if (++i >= args.length)
 								throw new IllegalArgumentException("Expected another bare argument after 'q'!");
							mode = 1;
 							query = args[i];
 							break;
 						case 't':
 							if (j!=arg0.length()-1)
 								throw new IllegalArgumentException("'t' short option must be last in a stack!");
 							if (++i >= args.length)
 								throw new IllegalArgumentException("Expected another bare argument after 't'!");
 							targName = args[i];
 							break;
 						case 'f':
 							if (j!=arg0.length()-1)
 								throw new IllegalArgumentException("'f' short option must be last in a stack!");
 							if (++i >= args.length)
 								throw new IllegalArgumentException("Expected another bare argument after 'f'!");
 							pdirName = args[i];
 							break;
 						default:
 							throw new IllegalArgumentException("Unrecognized short option: '"+opt+"'.");
 					}
 				}
 			} else {
 				//bare argument
 				throw new IllegalArgumentException("Unrecognized bare argument: '"+arg0+"'.");
 			}
 		}
 		
 		if (mode == 0) {
 			//patch mode
 			System.out.println("Reading patch file \""+targName+"\"...");
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			Document patchDoc = builder.parse(targName);
 			Node n_xmlp = patchDoc.getFirstChild();
 			if (!n_xmlp.getNodeName().equals("xmlp"))
 				throw new RuntimeException("Root patch node must be named \"xmlp\""); 
 			
 			for (Node n_xmlp_patch = n_xmlp.getFirstChild(); n_xmlp_patch != null; n_xmlp_patch = n_xmlp_patch.getNextSibling()) {
 				NamedNodeMap attr = n_xmlp_patch.getAttributes();
 				
 				//get target...
 				Node n = attr.getNamedItem("target");
 				if (n == null) { System.err.println("Patch has no target!"); continue; }
 				String p_target = n.getNodeValue();
 				
 				//get query...
 				n = attr.getNamedItem("query");
 				if (n == null) { System.err.println("Patch has no query!"); continue; }
 				String p_query = n.getNodeValue();
 				
 				//get op...
 				n = attr.getNamedItem("op");
 				if (n == null) { System.err.println("Patch has no op!"); continue; }
 				String p_op = n.getNodeValue();
 				
 				System.err.println("Performing \""+p_op+"\" on \""+p_target+"\":\""+p_query+"\"");
 				
 			}
 		} else {
 			//query mode	
 			System.out.println("Querying \""+query+"\" from \""+targName+"\"...");
 				//String query = "/GameObjects/GameObject[@Category=\"Pony\"]/@ID";
 				//String query = "/GameObjects/GameObject[@Category=\"Pony_House\"]/Construction/@ConstructionTime";
 				XMLPatch patcher = new XMLPatch(targName);
 				patcher.applyPatch(query, new XMLPatch.PrintXMLActor());
 		}
 	}
 }
