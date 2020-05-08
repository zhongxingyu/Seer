 package gr.di.uoa.a8.bvplus.boldi;
 
import java.io.File;
import java.io.FileInputStream;
 import java.io.IOException;
 
 import gr.di.uoa.a8.bvplus.utils.BVPlusUtils;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
 import it.unimi.dsi.webgraph.BVGraph;
 import it.unimi.dsi.webgraph.ImmutableGraph;
 import it.unimi.dsi.webgraph.LazyIntIterator;
 import it.unimi.dsi.webgraph.NodeIterator;
 
 
 public class Test {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		
 //		ImmutableGraph ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-csconf-mult5-PaperJSON.graph-no-duplicates.txt")));
 ////		System.out.println(ig.numNodes()+" "+ig.numArcs());
 //		ImmutableGraph.store(BVGraph.class, ig, "citation-csconf-mult5-PaperJSON");
 //		
 //		ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-PaperJSON_199star.graph-no-duplicates.txt")));
 //		ImmutableGraph.store(BVGraph.class, ig, "citation-PaperJSON_199star");
 //		
 //		ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-csconf-even-PaperJSON.graph-no-duplicates.txt")));
 //		ImmutableGraph.store(BVGraph.class, ig, "citation-csconf-even-PaperJSON");
 //		
 //		ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-csconf-mult10-PaperJSON.graph-no-duplicates.txt")));
 //		ImmutableGraph.store(BVGraph.class, ig, "citation-csconf-mult10-PaperJSON");
 //		
 //		ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-csconf-mult15-PaperJSON.graph-no-duplicates.txt")));
 //		ImmutableGraph.store(BVGraph.class, ig, "citation-csconf-mult15-PaperJSON");
 //		
 //		ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-csconf-PaperJSON.graph-no-duplicates.txt")));
 //		ImmutableGraph.store(BVGraph.class, ig, "citation-csconf-PaperJSON");
 //		
 //		ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-PaperJSON_19star.graph-no-duplicates.txt")));
 //		ImmutableGraph.store(BVGraph.class, ig, "citation-PaperJSON_19star.graph");
 		
 //		ImmutableGraph ig  = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("eu2005.txt.noD1")));
 //		ImmutableGraph.store(BVGraph.class, ig, "eu.noD1.graph");
 		
 //		ImmutableGraph ig  = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("dblp2010.txt.noD1")));
 //		ImmutableGraph.store(BVGraph.class, ig, "dblp2010.noD1.graph");
 		
 //		ImmutableGraph ig  = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("/var/www/graphs/eu2005/eu2005-zero.txt")));
 //		ImmutableGraph.store(BVGraph.class, ig, "/tmp/example");
 		
 		ImmutableGraph ig = BVGraph.load(args[0]);
 		
 		NodeIterator it = ig.nodeIterator();
 		int D = 0,total=0,in=0;
 		if(args.length>1)
 			D = Integer.parseInt(args[1]);
 		while(it.hasNext())
 		{
 			Integer node = it.next();
 			LazyIntIterator its = ig.successors(node);
 			int suc;
 			while((suc = its.nextInt()) !=-1)
 			{
 				if(args.length==1)
 					System.out.println(node+"\t"+suc);
 				else
 				{
 					if(BVPlusUtils.isDiagonal(node, suc, D))
 					{
 						in++;
 					}
 					total++;
 				}
 			}
 		}
 		if(args.length>1)
 			System.out.println(D+": In Diagonal: "+in+" Total: "+total+" Percentage: "+(float)100*in/total);
 	}
 
 }
