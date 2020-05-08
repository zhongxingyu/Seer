 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.imageio.stream.FileImageInputStream;
 
 import org.w3c.dom.Node;
 
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.DomNode;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 import domted.DOMPostOrder;
 import domted.NodeDistance;
 import domted.TreeEditDistance;
 
 
 public class Tester {
 	private static BufferedReader reader;
 	private static PrintWriter writer;
 
 	public static void main(String[] args) {
 		if(args.length == 1) openFile(args[0]);
 		else System.out.println("NOOOO");
 		WebClient client = createClient();
 		TreeEditDistance ted = createTED();
 		String input;
 
 		DomNode prevBody=null;
 		HtmlPage page = null;
 		try {
 			while((input=reader.readLine())!=null) {
 				try {
 					System.out.println("Downloading "+input+"....");
 					page = client.getPage(input);
 					System.out.println("Done.");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				if(page!= null){
 					if(prevBody != null){
						if(page.getBody()== prevBody) continue;
						else {
 							writer.print(input);
 							writer.print("\t");
 							writer.print(ted.calculate(prevBody, page.getBody()));
							prevBody = page.getBody();
 						}
 					}
 				}
				writer.println();
 				writer.flush();
 			}
 		} catch (FailingHttpStatusCodeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		closeFiles();
 	}
 
 	private static WebClient createClient(){
 		return WebClientFactory.borrowClient();
 	}
 	private static TreeEditDistance createTED(){
 		NodeDistance nd = new NodeDistance() {
 			public int rename(Node n1, Node n2) {
 				if(n1.getNodeName().equals(n2.getNodeName())) return 0;
 				else return 1;
 			}
 			public int insert(Node n1, Node n2) {
 				return 2;
 			}
 			public int delete(Node n1, Node n2) {
 				return 2;
 			}
 		};
 		return new TreeEditDistance(nd);		
 	}
 
 	private static void openFile(String s) {
 		File f = new File(s);
 		File dir = new File(f.getParent());
 
 		try {
 			File newName = new File(dir.getPath()+File.separatorChar+f.getName()+".log");
 			reader = new BufferedReader(new FileReader(f));
 			writer = new PrintWriter(newName);
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 	}
 	private static void closeFiles(){
 		try {
 			reader.close();
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 }
