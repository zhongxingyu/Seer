 package TestCases;
 
import static org.junit.Assert.*;

 import java.util.ArrayList;
 
import org.junit.Test;
 
 import Source.ASNode;
 import Source.ASTree;
 import Source.Token;
 
 public class TreeTest {
 
	@Test
	public void test() {
 		ASTree at = new ASTree();
 		ASNode head = new ASNode();
 		ASNode term = new ASNode();
 		ASNode tail = new ASNode();
 		
 		at.setHead(head);
 		
 		Token hToken = new Token("expr", null);
 		head.setToken(hToken);
 		head.setParent(head);
 		
 		//term.setName("term");
 		Token termToken = new Token("term", null);
 		term.setParent(head);
 		term.setToken(termToken);
 		
 		//tail.setName("tail");
 		Token tailToken = new Token("tail", null);
 		tail.setParent(head);
 		tail.setToken(tailToken);
 		
 		ArrayList<ASNode> childrenList = new ArrayList<ASNode>();
 		childrenList.add(term);
 		childrenList.add(tail);
 		head.setChildren(childrenList);
 		
 		System.out.println(at.toString());
 		
 		
 		
 	}
 
 }
