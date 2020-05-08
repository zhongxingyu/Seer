 import org.w3c.dom.Node;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 import domted.DOMPostOrder;
 import domted.NodeDistance;
 import domted.TreeEditDistance;
 
 
 public class Tester {
 	public static void main(String[] args) {
 		WebClient client = new WebClient();
 		client.setJavaScriptEnabled(false);
 		try {
 			HtmlPage p1 = client.getPage(
					"http://slashdot.org/"
 			);
 			HtmlPage p2 = client.getPage(
					"http://books.slashdot.org/"
 			);
 			TreeEditDistance ted = new TreeEditDistance(new NodeDistance() {
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
 			});
 			int val = ted.calculate(p1.getBody(), p2.getBody());
 			System.out.println(val);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
