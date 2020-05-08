 package edu.mit.cci.visualize.wiki.util;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.List;
 
 import edu.mit.cci.visualize.wiki.collector.ArticleContributions;
 import edu.mit.cci.visualize.wiki.collector.UsertalkEdge;
 
 public class Processing {
 
     public String processingCode(final List<ArticleContributions> nodes,
                                  final List<UsertalkEdge> edges,
                                  final String path,
                                  final String size) {
 		String code = "";
 		String eol = "\n";
 		String firstName = "";
 		String nodeCode = "";
 		for (ArticleContributions node : nodes) {
 			//engine.addParticle(new Particle("Remco", random(0, canvasSize), random(0, canvasSize), 30, 0, 0, 0x80FF0000));
 			String name = node.getUserID();
 			firstName = name;
 
 			String[] colors = {"0x800000FF", "0x80FF0000", "0x8000FF00"};
 
 			double nodeSize = Math.log10(node.getNumberOfChanges()) * 20;
 			if (nodeSize < 10) {
 				nodeSize = 10;
 			}
 			// TODO hardcoded
 			int numOfArticles = 1;
 			String color = "";
 			if (numOfArticles < 4) {
 				color = colors[numOfArticles-1];
 			} else {
 				color = "0x80000000";
 			}
 
 			nodeCode += "engine.addParticle(new Particle(\"" + name + "\", random(0, " + size + "), random(0, " + size + "), " + nodeSize + ", 0, 0, " + color + "));" + eol;
 		}
 
 		String edgeCode = "";
 		//String[] edgeArr = edges.split("\n");
 		for (UsertalkEdge edge : edges) {
 			//engine.connectParticles("Remco", "Joris", 2);
 			String name1 = edge.getFrom();
 			String name2 = edge.getTo();
 			double thick = edge.getNbrOfConversations();
 			thick = Math.log10(thick) * 2;
 			edgeCode += "engine.connectParticles(\"" + name1 + "\", \"" + name2 + "\", " + thick + ");" + eol;
 
 		}
 		try {
 			// Read skeleton code
 			BufferedReader br = new BufferedReader(new FileReader(path));
 			String line = "";
 			while ((line = br.readLine()) != null) {
 				if (line.startsWith("#SIZE")) {
 					code += "int canvasSize = " + size + ";" + eol;
 				} else if (line.startsWith("#NODES")) {
 					code += nodeCode;
 				} else if (line.startsWith("#EDGES")) {
 					code += edgeCode;
 				} else if (line.startsWith("#PINNED")) {
 					code += "engine.findParticle(\"" + firstName + "\").pin(" + size + "/2, " + size + "/2);" + eol;
 				} else {
 					code += line + eol;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return code;
 	}
 }
