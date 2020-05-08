 package ac.kr.ssu.snav.parser;
 
 import java.io.InputStream;
 
 import com.hp.hpl.jena.n3.turtle.TurtleReader;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFReader;
 import com.hp.hpl.jena.util.FileManager;
 
 public class SNavReadingRDF {
 
 	//private String inputFileName = "c:\\exampleRDF/LAW_GENSEARCH.owl";
 	//private String inputFileName = "c:\\exampleRDF/camera.owl";
 	//private String inputFileName = "c:\\exampleRDF/electionLaw2.rdf";
 	//private String inputFileName = "c:\\exampleRDF/vcard.rdf";
 	//private String inputFileName = "c:\\exampleRDF/electionLaw3.rdf";
	private String inputFileName = "c:\\exampleRDF/law5.rdf";
	//private String inputFileName = "c:\\exampleRDF/law4.rdf";
 	
 	private InputStream in;
 	
 	// create an empty model
 	private Model model;
 	
 	static public Model sModel;
 	
 	public SNavReadingRDF(){
 		
 		this.model = ModelFactory.createDefaultModel();
 		
 		// use the FileManager to find the input file
 		this.in = FileManager.get().open( inputFileName );
 		if (this.in == null) {
 		    throw new IllegalArgumentException(
 		       "File: " + inputFileName + " not found");
 		}	
 		
 	}
 	
 	static public Model createModel(){
 		sModel =  ModelFactory.createDefaultModel();
 
 		return sModel;
 	}
 
 	// read the RDF/XML file
 	public void modelRead(){
 		this.model.read(in, null);
 	}
 	
 	// write it to standard out
 	public void modelWrite(){
 		//this.model.write(System.out);
 		//write finish
 		System.out.println("\n -- model write finished --\n");
 		
 	}
 	
 	public Model getModel(){
 		return model;
 	}
 	
 	//set rdf file name
 	public void setInputFileName(String inputFileName){
 		this.inputFileName = inputFileName;
 	}	
 	
 	public InputStream getInputFileStream(){
 		
 		this.in = FileManager.get().open( inputFileName );
 		if (this.in == null) {
 		    throw new IllegalArgumentException(
 		       "File: " + inputFileName + " not found");
 		}
 		return this.in;
 	}
 }
