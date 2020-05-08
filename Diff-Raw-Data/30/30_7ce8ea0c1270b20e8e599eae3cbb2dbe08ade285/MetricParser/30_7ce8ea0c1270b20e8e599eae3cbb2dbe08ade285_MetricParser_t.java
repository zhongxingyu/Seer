 package de.uni_leipzig.simba.saim.core.metric;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.uni_leipzig.simba.saim.core.metric.Property.Origin;
 
 public class MetricParser
 {
 	protected static String[] splitFunc(String s)
 	{
 		if(!s.contains("(")) {return new String[] {s};}
 		List<String> tokenList = new LinkedList<String>();
 		int i = s.indexOf("(");
 		tokenList.add(s.substring(0,i));
 		s = s.substring(i,s.length()); // remove name of the function
 		int depth = 0;
 		StringBuilder sb = new StringBuilder();
 		for(char c : s.toCharArray())
 		{
 			switch(c)
 			{
 				case('('): if(depth>0) {sb.append(c);} depth++; break;
 				case(')'): if(depth>1) {sb.append(c);} depth--; break;
 				case(','):
 					if(depth>1) {sb.append(c);} else {tokenList.add(sb.toString());sb = new StringBuilder();}
 				break;
 				default: sb.append(c);
 			}		
 		}
 		tokenList.add(sb.toString());
 		return tokenList.toArray(new String[0]);
 	}
 
 	protected static String setParametersFromString(Node node, String s, int pos)
 	{				
 		// example: 0.6*trigrams(x.rdfs:label,y.rdfs:label)
 		if(node.hasFrontParameters())
 		{
 			int i = s.indexOf("*");
 			if(i==-1) return s;
 			Double d = Double.valueOf(s.substring(0,i));
 			s = s.substring(i+1);
 			switch(pos)
 			{
 				case(0):node.param1=d;break;
 				case(1):node.param2=d;
 			}			
 		}
 		// example: trigrams(x.rdfs:label,y.rdfs:label)|0.5
 		else
 		{ 
 			int i = s.lastIndexOf("|");
 			if(i==-1||i<s.lastIndexOf(")")) {return s;}
 //			if(s.lastIndexOf("|")>s.lastIndexOf(")"))
 //			{
 //				s = s.substring(0,s.lastIndexOf("|"));
 //				return setParametersFromString(node,s,pos);
 //			}
 			Double d = Double.valueOf(s.substring(i+1));
 			s = s.substring(0,i);
 			switch(pos)
 			{
 				case(0):node.param1=d;break;
 				case(1):node.param2=d;
 			}
 		}
 		return s;
 	}
 
 	protected static Node parsePart(Node parent, String s, String sourceVar, int pos) throws MetricFormatException
 	{
 		//ADD(0.6*jaccard(x.title,y.title)|0.5,0.6*cosine(x.authors,y.authors)|0.5)|0.5
 		//		if(!s.contains("(")) {return output;}
 		String[] tokens = splitFunc(s);		
 		String id = tokens[0].trim();
 		if(id.contains("*")) // e.g. 0.6*jaccard, parameter of parent component
 		{
 			String[] parts = id.split("\\*");
 			double d = Double.parseDouble(parts[0]);
 			if(parent.param1==null) {parent.param1=d;} else {parent.param2=d;}
 			id = parts[1]; 
 		}
 		if(tokens.length==1) return new Property(id,id.startsWith(sourceVar)?Origin.SOURCE:Origin.TARGET);
 
 		Node node = Node.createNode(id);
 		s=setParametersFromString(parent,s,pos);
		tokens = splitFunc(s); // update tokens in case s was updated
 		int i=0;
 		for(String argument: Arrays.copyOfRange(tokens,1,tokens.length))
 		{
 			if(!node.addChild(parsePart(node,argument,sourceVar,i))) {throw new MetricFormatException("Could not add child \""+argument+'"');}
 			i++;
 		}				
 		return node;
 	}
 
 	/** Parses the string and returns the Output root node of the metric tree represented by the argument in a metric expression.
 	 * @param s the metric expression as required by LIMES, e.g. <code>trigrams(x.rdfs.label,y.rdfs.label)</code>,
 	 * see <a href="http://aksw.org/Projects/LIMES/files?get=limes_manual.pdf">the LIMES manual.</a>
 	 * @param sourceVar the source variable without question mark, e.g. "x"
 	 * @return the Output root node of the metric tree represented by the argument in a metric expression.
 	 * @throws MetricFormatException  if the string does not contain a parsable metric. */
 	public static Output parse(String s,String sourceVar) throws MetricFormatException
 	{
 		if(s.isEmpty()) throw new MetricFormatException();
 		Output output = new Output();
 		s = setParametersFromString(output,s,0);
 		// at the moment the very last parameter is ignored because the global threshold is set elsewhere
 		output.param1=null;
 		output.param2=null;
 		try{output.addChild(parsePart(output, s,sourceVar,0));}
 		catch(MetricFormatException e) {throw new MetricFormatException("Error parsing metric expression \""+s+"\".",e);}		
 		return output;
 	}
 }
