 package code;
 
 import org.junit.*;
 
 import static org.junit.Assert.*;
 
 import java.util.*;
import code.TopKRank;
 
 public class TopKRankTest {
 
 	// TODO:
 	// - add a table SUBGRAPHS_2 so you can start testing
 	// - write calcIsomorphScore()
 	// - pray to got all of this works
 
 	String[] args = {""};
 	TopKRank topK = new TopKRank(args, "");
 
 	@Test
 	public void testCalcUpScore() {
 		LinkedList<String> ll = new LinkedList<String>();
 		ll.add("cites");
 		ll.add("160158");
 		ll.add("592013");
 		TopKRank.Subgraph sg = topK.new Subgraph(ll, 1);
 	}
 
 	public static void main(String[] args)
 	{
 		org.junit.runner.JUnitCore.main("graphs.TopKRankTest");
 	}
 }
