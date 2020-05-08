 package rte.util;
 
 import static org.junit.Assert.assertEquals;
 
import java.util.ArrayList;

 import org.junit.Test;
 
 import rte.pairs.Relation;
 import rte.pairs.Sentence;
 import rte.pairs.SentenceNode;
 
 public class TreeDistCalculatorTest {
 
 	@Test
 	public void test() {
 		Sentence t1 = new Sentence(0);
 		SentenceNode f = new SentenceNode("f");
 		SentenceNode e = new SentenceNode("e");
 		SentenceNode d = new SentenceNode("d");
 		SentenceNode c = new SentenceNode("c");
 		SentenceNode b = new SentenceNode("b");
 		SentenceNode a = new SentenceNode("a");
 		f.children.add(d);
 		f.children.add(e);
 		
 		d.children.add(a);
 		d.children.add(c);
 		d.relation = new Relation(d, f, null);
 		
 		e.relation = new Relation(e, f, null);
 		
 		a.relation = new Relation(a, d, null);
 		c.relation = new Relation(c, d, null);
 		c.children.add(b);
 		
 		b.relation = new Relation(b, c, null);
 		t1.nodes.add(f);
 		t1.nodes.add(e);
 		t1.nodes.add(d);
 		t1.nodes.add(c);
 		t1.nodes.add(b);
 		t1.nodes.add(a);
 		
 		// 2nd Sentence!
 		
 		Sentence t2 = new Sentence(0);
 		
 		SentenceNode f2 = new SentenceNode("f");
 		SentenceNode e2 = new SentenceNode("e");
 		SentenceNode d2 = new SentenceNode("d");
 		SentenceNode c2 = new SentenceNode("c");
 		SentenceNode b2 = new SentenceNode("b");
 		SentenceNode a2 = new SentenceNode("a");
 		f2.children.add(c2);
 		f2.children.add(e2);
 		
 		e2.relation = new Relation(e2, f2, null);
 		
 		c2.relation = new Relation(c2, f2, null);
 		c2.children.add(d2);
 		
 		d2.relation = new Relation(d2, c2, null);
 		d2.children.add(a2);
 		d2.children.add(b2);
 		
 		a2.relation = new Relation(a2, d2, null);
 		b2.relation = new Relation(b2, d2, null);
 
		t2.nodes.add(f2);
		t2.nodes.add(e2);
		t2.nodes.add(d2);
		t2.nodes.add(c2);
		t2.nodes.add(b2);
		t2.nodes.add(a2);
		
		
		ArrayList<SentenceNode> toName = new ArrayList<SentenceNode>();
		toName.addAll(t1.getAllSentenceNodes());
		toName.addAll(t2.getAllSentenceNodes());
		
		for(SentenceNode n : toName) {
			n.word = n.id;
			n.lemma = n.id;
		}
 		
 		TreeDistCalculator calculator = new TreeDistCalculator(f, f2);
 		assertEquals(2, calculator.calculate());
 		
 	}
 
 }
