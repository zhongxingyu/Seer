 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mlparch;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.regex.Pattern;
 import mlparch.MLPArch.MLPFileEntry;
 
 /**
  *
  * @author John Petska
  */
 public class MLPA_CLI {
 	public static void showHelp() {
 		System.out.println("MLPArch packing/unpacking utility");
 		System.out.println("Public domain code, released 2012");
 		System.out.println("Options:");
 		System.out.println("    -u - unpack mode (default)");
 		System.out.println("    -p - pack mode");
 		System.out.println("    -l - list mode ");
 		System.out.println("    -a <arg> - specify archive location (default \"main.1050.com.gameloft.android.ANMP.GloftPOHM.obb\")");
 		System.out.println("    -f <arg> - specify pack/unpack location (default \"extract\")");
 		System.out.println("    -s <arg> - pack/unpack only a single file");
 		System.out.println("    -r <arg> - pack/unpack only files matching this regex");
 		//System.out.println("    -m <arg> - pack/unpack only files matching this wildcard pattern (*, ?)");
 		System.out.println("    -v - show this help");
 		System.out.println("    -? - show this help");
 		System.out.println("    --help - show this help");
 	}
 	public static void main(String[] args) throws Exception {
 		String archName = "main.1050.com.gameloft.android.ANMP.GloftPOHM.obb";
 		String tmplName = archName;
 		String packName = "extract";
 		int mode = 0; //0 == unpack, 1 == pack, 2 == list
 		int matchMode = 0; //0 == all, 1 == single, 2 == regex, 3 == wildcard
 		String matchPat = null;
 		
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
 						case 'u':
 							mode = 0; //unpack
 							break;
 						case 'p':
 							mode = 1; //pack
 							break;
 						case 'l':
 							mode = 2; //list
 							break;
 						case 'a':
							if (j!=arg0.length()-1 || ++i >= args.length)
 								throw new IllegalArgumentException("Expected another bare argument after 'a'!");
 							archName = args[i];
 							break;
 						case 'f':
							if (j!=arg0.length()-1 || ++i >= args.length)
 								throw new IllegalArgumentException("Expected another bare argument after 'f'!");
 							packName = args[i];
 							break;
 						case 's':
							if (j!=arg0.length()-1 || ++i >= args.length)
 								throw new IllegalArgumentException("Expected another bare argument after 's'!");
 							matchMode = 1;
 							matchPat = args[i];
 							break;
 						case 'r':
							if (j!=arg0.length()-1 || ++i >= args.length)
 								throw new IllegalArgumentException("Expected another bare argument after 'r'!");
 							matchMode = 2;
 							matchPat = args[i];
 							break;
 						case 'm':
							if (j!=arg0.length()-1 || ++i >= args.length)
 								throw new IllegalArgumentException("Expected another bare argument after 'm'!");
 							System.err.println("Attempting to use wildcard matching: This doesn't work very well, have fun!");
 							matchMode = 3;
 							matchPat = args[i];
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
 		
 		File archFile = new File(archName);
 		File packFile = new File(packName);
 		MLPArch arch = new MLPArch(archFile);
 		switch (matchMode) {
 			default:
 			case 0: //all
 				matchPat = null; break;
 			case 1: //single
 				matchPat = Pattern.quote(matchPat); break;
 			case 2: //regex
 				/* done. */ break;
 			case 3: //wildcard
 				matchPat = Pattern.quote(matchPat);
 				matchPat = matchPat.replace("\\*", ".*");
 				matchPat = matchPat.replace("\\?", ".");
 				break;
 		}
 		Pattern pat = matchPat==null ? null : Pattern.compile(matchPat);
 			
 		if (mode == 0 || mode == 2) {
 			//unpack or list
 			System.out.println((mode==0?"Unpacking":"Listing")+" MLPArch at \""+archFile.getPath()+"\" to \""+packFile.getPath()+"\".");
 			
 			System.out.print("Reading header...");
 				arch.loadHeaderFromArchive();
 			System.out.println("done.");
 			System.out.println("\tIndex Start: "+arch.indexOffset);
 			
 			System.out.print("Reading index...");
 				arch.loadIndexFromArchive();
 			System.out.println("done.");
 			System.out.println("\tFile Count: "+arch.index.size());
 			
 			if (mode == 0) {
 				System.out.println("Unpacking archive...");
 				packFile.mkdir();
 				NumberFormat format = NumberFormat.getPercentInstance(); format.setMinimumFractionDigits(1); format.setMaximumFractionDigits(1);
 				for (int i = 0; i < arch.index.size(); i++) {
 					MLPFileEntry entry = arch.index.get(i);
 					if (pat != null && !pat.matcher(entry.path).matches())
 						continue; //skipping
 					System.out.print("Unpacking "+(i+1)+"/"+arch.index.size()+" ("+format.format((float)(i+1)/arch.index.size())+"): \""+entry.path+"\" ("+entry.size()+" bytes)...");
 						arch.unpackFile(entry, packFile);
 					System.out.println("done.");
 				}
 			} else {
 				System.out.println("Listing Index...");
 				for (int i = 0; i < arch.index.size(); i++)
 					if (pat == null || pat.matcher(arch.index.get(i).path).matches())
 						System.out.println((i+1)+": "+arch.index.get(i).toString());
 			}
 		} else if (mode == 1) {
 			//pack
 			System.out.println("Packing MLPArch at \""+archFile.getPath()+"\" from \""+packFile.getPath()+"\".");
 			
 			System.out.print("Building index...");
 				arch.loadIndexFromFolder(packFile);
 			System.out.println("done.");
 			System.out.println("\tFile Count: "+arch.index.size());
 			
 			System.out.println("Writing header...");
 				arch.writeHeaderToArchive();
 			
 			System.out.println("Packing files...");
 				arch.writeFilesToArchive(packFile, pat);
 			
 			System.out.println("Writing index...");
 				arch.writeIndexToArchive();
 		}
 		
 	}
 }
