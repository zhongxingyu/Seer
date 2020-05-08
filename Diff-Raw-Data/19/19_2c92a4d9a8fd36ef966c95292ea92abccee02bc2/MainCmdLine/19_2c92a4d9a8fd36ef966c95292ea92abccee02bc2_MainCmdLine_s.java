 package indeece;
 
 import indeece.Model.Result;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Set;
 
 import antlr.ANTLRException;
 import antlr.MismatchedTokenException;
 
 
 
 
 
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 
 public class MainCmdLine
 {
 	public static void main(String args[])
 	throws IOException
 	{
 		String pathName = null;
 		// parse command line arguments.
 		OptionSet opts = (new OptionParser("rb:v:os")).parse(args);
 		if(!(opts.has("v") ^ opts.has("b")))
 			usage();
 		if(opts.has("r") && opts.has("w"))
 			usage();
 		
 		if(opts.has("v")) 
 			pathName = (String)opts.valueOf("v");
 		else if(opts.has("b")) 
 			pathName = (String)opts.valueOf("b");
 		
 		if(!opts.has("r")) {
 			// create inverted index from corpusDir
 			try {
 				Indeece.createIndex(new CorpusBuilder(pathName), opts.has("s"), opts.has("p"));
 				if(opts.has("w"))
 					Indeece.storeIndeece(pathName);
 			} catch(IOException e) {
 				System.err.println("Error indexing "+pathName);
 				e.printStackTrace();
 				System.exit(1);
 			}
 		} else {
 			// reload inverted index from pathName
 			Indeece.loadIndeece(pathName);
 		}
 
 		Indeece.initModels(opts.has("o"));
 		if(opts.has("v"))
 			Indeece.setActive("Vector");
 		else if(opts.has("b"))
 			Indeece.setActive("Boolean");
 		
 		
 		// start user interaction.
 		BufferedReader 		input 	= new BufferedReader(new InputStreamReader(System.in));
 		Collection<Model.Result>	results	= null;
 		String query = new String();
 		while(true)
 		{
 			System.out.print("Enter query: ");
 			
 			// perform search
 			try {
 				query=input.readLine();
 				results = Indeece.activeModel().search(query);
			} catch(Exception e) {	
 				if( Indeece.isActiveModel("Boolean")) {
 					results=switchToVectorModel(query);
 				}
 			} 
 			
 			// print results
 			Iterator<Model.Result> resultsIter = results.iterator();
 			for(int i=1; resultsIter.hasNext(); i++)
 			{
 				System.out.println(i+"."+resultsIter.next());
 			}
 		}
 	}
 	
 	//When a boolean query is invalid, direct the query towards the Vector Model
 	private static Collection<Result> switchToVectorModel(String query) {
 		Collection<Result> results = null;
 		System.out.println("Invalid boolean query. Querying vector model instead");
 		Indeece.setActive("Vector");
 		try {
 			results = Indeece.activeModel().search(query);
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		Indeece.setActive("Boolean");
 		return results;
 	}
 
 	private static void usage()
 	{
 		System.err.println("[-s] [-r|-w] <-b | -v [-o]> corpusDir");
 		System.exit(1);
 	}
 }
