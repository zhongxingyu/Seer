 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 import org.jsoup.Jsoup;
 import org.jsoup.Connection.Response;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.sun.corba.se.impl.orbutil.graph.Graph;
 
 
 public class ModuleConnector {
 
 	ArrayList<String> nodes;
 
 	/**
 	 * Finds connections between Modules.  reads them in from text files (?)
 	 * @param args
 	 * @throws IOException 
 	 */
 	public ModuleConnector(String subject) throws IOException
 	{
		System.out.println(subject);
 		nodes = new ArrayList<String>();
 		File file = new File(subject+".txt");
 		Scanner sc = new Scanner(file);
 		String[] phrases=null;
 		while(sc.hasNextLine())
 		{
 			String line = sc.nextLine();
 			if(line.contains("{}"))
 			{
 				continue;
 			}
 			String phrase=line.split("\\{|\\}")[1];
 			phrases=phrase.split("( *=[0-9]+, )|( *=[0-9]+)"); //now we have each string separately
 			System.out.println(Arrays.toString(phrases));
 			for(String s:phrases)
 				nodes.add(s.trim());
 		}
 		//we have strings in array phrases
 
 		int[][] result=new int[nodes.size()][nodes.size()];
 		System.out.println("Size of nodes: "+nodes.size());
 		for(int i=0;i<nodes.size();++i){
 			String wiki=lookUpOnWiki(nodes.get(i));
 			for(int j=0;j<nodes.size();++j){
 				if(j==i)
 					continue;
 				if(wiki.indexOf(nodes.get(j))!=-1)
 				{	
 					int count = wiki.split(nodes.get(j)).length;
 					if(count < 2) //should never happen
 					{
 						System.err.println("something went wrong");
 					}
 					result[j][i] += (count-1);
 				}
 			}
 		}
 		//for(int[] i:result)
 			//System.out.println(Arrays.toString(i));
 
 		/* finished looking up on wikipedia */
 
 		LinkedList<Pair> list = new LinkedList<Pair>();
 		int lines=0;
 		for(int i = 0; i < result[0].length; i++)
 		{
 			for(int j = i+1; j < result[0].length; j++)
 			{
 				if(result[i][j] > result[j][i])
 				{
 					Pair p = new Pair(nodes.get(i),nodes.get(j));
 					list.add(p);
 					++lines;
 				}
 				else if((result[i][j] < result[j][i]))
 				{
 					Pair p = new Pair(nodes.get(j),nodes.get(i));
 					list.add(p);
 					++lines;
 				}
 				else if((result[i][j] == result[j][i]) && (result[i][j] != 0))
 				{//choose arbitrarily if both are equal and non-zero
 					Pair p = new Pair(nodes.get(i),nodes.get(j));
 					list.add(p);
 					++lines;
 				}
 				//else if both are 0, do nothing
 			}
 		}
 		
 		int nonzerocount=0;
 		for(int[] i:result){
 			for(int j:i)
 				if(j!=0)
 					++nonzerocount;
 		}
 		System.out.println("Non-zero count: "+nonzerocount);
 		System.out.println("lines: "+lines);
 		System.out.println("triangle: "+(((result[0].length*result[0].length)-result[0].length)/2));
 		/* we have list of Pairs */
 		ModGraph modgraph = new ModGraph(list, nodes, result);
		
		ArrayList<String> cycle;
		cycle = (ArrayList<String>) graph.TestCycles.getCycle(modgraph.getAdjMatrix(), modgraph.getNodesForLookup());
		while(cycle != null)
		{
			SortCycle mySC = new SortCycle();
			Edge e = mySC.sort(cycle, nodes, result);
			System.out.println(e);
			//remove e: TODO
			
			
			cycle = (ArrayList<String>) graph.TestCycles.getCycle(modgraph.getAdjMatrix(), modgraph.getNodesForLookup());
		}
		
 		printDotFile(subject, list);
 
 	}//end constructor
 
 
 	private void printDotFile(String subject, LinkedList<Pair> list) throws IOException
 	{
 		/*
 		digraph graphname {
      	a -> b -> c;
      	b -> d;
  		}
 		 */
 		String subj = subject.replace(' ', '_');
 		File dotfile = new File(subj+".dot");
 		PrintWriter pw = new PrintWriter(new FileWriter(dotfile));
 		pw.println("digraph "+subj+" {");
 		for(Pair p : list)
 		{
 			pw.println("\""+p.getPre().replace(' ', '_')+"\" -> \""+p.getPost().replace(' ', '_')+"\";");
 			pw.flush();
 		}
 		pw.print("}");
 		pw.flush();
 		pw.close();
 	}
 
 	private String lookUpOnWiki(String searchTerm)
 	{
 		String temp = searchTerm.replace(' ', '_');
 		Document document;
 		StringBuilder sb = new StringBuilder();
 		try {
 			document = Jsoup.connect("http://en.wikipedia.org/wiki/"+temp).get();
 			//String doc_text = document.text();
 			//Elements content = document.select("html body table tbody tr td span p");
 			String content = document.body().text();
 			sb.append(content.toString());
 			/*for(Element e : content)
 			{
 				sb.append(e.toString());
 			}*/
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		String tmp = sb.toString();
 		return tmp;
 	}
 
 	public static void main(String args[]) throws IOException
 	{
		ModuleConnector mc = new ModuleConnector(args[0]);
 	}
 
 }//end class
 
 class Pair
 {
 	private String pre;
 	private String post;
 
 	public Pair(String before, String after)
 	{
 		pre = before;
 		post = after;
 	}
 
 	public String getPre()
 	{
 		return pre;
 	}
 
 	public String getPost()
 	{
 		return post;
 	}
 }
