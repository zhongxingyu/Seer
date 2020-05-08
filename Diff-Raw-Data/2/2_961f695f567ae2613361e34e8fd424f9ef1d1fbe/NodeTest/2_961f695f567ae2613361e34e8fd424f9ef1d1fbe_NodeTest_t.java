 package de.uni_leipzig.simba.saim.core.metric;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 
 import org.junit.Test;
 
 import de.uni_leipzig.simba.saim.core.metric.Property.Origin;
 
 public class NodeTest
 {
 	
 	String[] testMetrics = {"jaccard(x.dc:title,y.dc:title)",
 			"trigrams(x.dc:title,y.dc:title)",
 
 			"levenshtein(x.dc:title,y.dc:title)",
 			"cosine(x.dc:title,y.dc:title)",
 			"euclidean(x.dc:title,y.dc:title)",
			"jaccard(src.dailymed:name,dest.rdfs:label)",
 			"ADD(0.6*jaccard(x.dc:title,y.dc:title),0.6*cosine(x.authors,y.authors))",
 			"MAX(jaccard(x.dc:title,y.dc:title)|0.3,0.6*cosine(x.authors,y.authors)|0.7)"
 			};
 	@Test
 	public void testMetricParsing() {
 		for(String s : testMetrics) {
 			System.out.println("testing: "+s);
 			Output o = MetricParser.parse(s,"x");
 			System.out.println("parsed : "+o);	
 			assertTrue("metric strings not equal, s="+s+", o="+o,o.toString().equalsIgnoreCase(s));
 			assertTrue(o.isComplete());
 		}
 		String s = "ADD(0.6*jaccard(x.dc:title,y.dc:title)|0.5,0.6*cosine(x.authors,y.authors)|0.5)|0.5";	
 	}
 	
 	@Test
 	public void testSplitFunc()
 	{
 		//System.out.println(Arrays.toString(MetricParser.splitFunc("ADD(0.6*jaccard(x.title,y.title)|0.5,0.6*cosine(x.authors,y.authors)|0.5)")));
 		assertTrue(Arrays.equals(
 				MetricParser.splitFunc("ADD(0.6*jaccard(x.title,y.title)|0.5,0.6*cosine(x.authors,y.authors)|0.5)"),
 				new String[]{"ADD","0.6*jaccard(x.title,y.title)|0.5","0.6*cosine(x.authors,y.authors)|0.5"}));
 	}
 
 	@Test
 	public void testParse()
 	{
 		Output o = new Output();
 		Measure m = new Measure("trigrams");
 		o.addChild(m);
 		Property p = new Property("x.rdf:type",Origin.SOURCE);
 		Property q = new Property("x.rdf:type",Origin.SOURCE);
 		Property r = new Property("y.rdf:type",Origin.TARGET);
 		Node[] nodes = {m,p,q,r};
 
 		for(int i=0;i<nodes.length;i++)
 			for(int j=i+1;j<nodes.length;j++)
 			{assertTrue(nodes[i].color!=nodes[j].color);}
 		//Property s = new Property("rdf:type",Origin.SOURCE);
 		assertTrue(m.validParentOf(p));
 		assertTrue(m.acceptsChild(p));
 		m.addChild(p);
 		assertFalse(p.validParentOf(m));
 		assertFalse(p.acceptsChild(m));
 
 		assertTrue(m.validParentOf(q));
 		assertFalse(m.acceptsChild(q));
 
 		assertTrue(m.validParentOf(r));
 		assertTrue(m.acceptsChild(r));		
 		assertFalse(m.isComplete());
 		m.addChild(r);
 		assertTrue(m.isComplete());
 
 		assertTrue(m.toString().equals(MetricParser.parse(m.toString(),"x").toString()));
 
 		Operator o1 = new Operator("min");
 		Operator o2 = new Operator("min");
 		Operator o3 = new Operator("min");
 		assertTrue(o1.addChild(o2));
 		assertTrue(o2.addChild(o3));
 		// would be a cycle
 		assertFalse(o3.addChild(o1));
 		o2.removeChild(o3);
 		assertTrue(o3.addChild(o1));
 		assertFalse(o1.isComplete());
 	}
 
 }
