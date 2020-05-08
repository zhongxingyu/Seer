 /**
 * xml(docbook)2markdown parser
 * Qijia (Michael) Jin
 * @version 1.0
 */
 
 import java.io.*;
 
 public class xml2md
 	{
 		static String inputfile[], edited[];
 		static int a, nl = 0;
 		public static void main(String[] args) throws IOException
 			{
 				BufferedReader in = new BufferedReader(new FileReader(args[0]));
 				while (in.readLine() != null) nl++;
 				in.close();
 				inputfile = new String [nl];
 				edited = new String [nl];
 				BufferedReader read = new BufferedReader (new FileReader(args[0]));
 				Writer write;
 				write = new BufferedWriter(new FileWriter("markdown-" + args[0] + ".txt", true));
 				for (a = 0; a < nl; a++)
 					{
 						inputfile[a] = read.readLine();
 						edited[a] = inputfile[a].replaceAll("<?/?title>", "##");
 						edited[a] = edited[a].replaceAll("<?/?literal>", "");
 						edited[a] = edited[a].replaceAll("&mdash;", "--");
 						edited[a] = edited[a].replaceAll("&amp;", "&");
 						edited[a] = edited[a].replaceAll("&man.", "");
 						edited[a] = edited[a].replaceAll(".0;", "(0)");
 						edited[a] = edited[a].replaceAll(".1;", "(1)");
 						edited[a] = edited[a].replaceAll(".2;", "(2)");
 						edited[a] = edited[a].replaceAll(".3;", "(3)");
 						edited[a] = edited[a].replaceAll(".4;", "(4)");
 						edited[a] = edited[a].replaceAll(".5;", "(5)");
 						edited[a] = edited[a].replaceAll(".6;", "(6)");
 						edited[a] = edited[a].replaceAll(".7;", "(7)");
 						edited[a] = edited[a].replaceAll(".8;", "(8)");
 						edited[a] = edited[a].replaceAll(".9;", "(9)");
 						edited[a] = edited[a].replaceAll("<entry>", "|");
 						edited[a] = edited[a].replaceAll("<email>", "[");
 						edited[a] = edited[a].replaceAll("</email>", "]");
 						edited[a] = edited[a].replaceAll("<command>", "**");
 						edited[a] = edited[a].replaceAll("</command>", "**");
 						edited[a] = edited[a].replaceAll("<emphasis>", "*");
 						edited[a] = edited[a].replaceAll("</emphasis>", "*");
 						edited[a] = edited[a].replaceAll("<function>", "**");
 						edited[a] = edited[a].replaceAll("</function>", "**");
 						edited[a] = edited[a].replaceAll("<parameter>", "*");
 						edited[a] = edited[a].replaceAll("</parameter>", "*");
 						edited[a] = edited[a].replaceAll("<[^>]*>", "");
 						edited[a] = edited[a].replaceAll("&lt;", "<");
						edited[a] = edited[a].replaceAll("&gt;", ">");
 						write.append(edited[a] + System.getProperty("line.separator"));
 					}
 				read.close();
 				write.close();
 			}
 	}
