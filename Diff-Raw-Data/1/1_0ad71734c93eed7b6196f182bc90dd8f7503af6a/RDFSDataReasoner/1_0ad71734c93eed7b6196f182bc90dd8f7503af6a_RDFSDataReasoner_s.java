 import java.io.*;
 //import java.net.*;
 //import java.sql.*;
 import java.util.*;
 //import com.hp.hpl.jena.query.*;
 import com.hp.hpl.jena.util.*;
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.ontology.*;
 import com.hp.hpl.jena.reasoner.*;
 import com.hp.hpl.jena.vocabulary.*;
 
 //import de.fuberlin.wiwiss.ng4j.*;
 //import de.fuberlin.wiwiss.ng4j.db.*;
 //import de.fuberlin.wiwiss.ng4j.sparql.*;
 
 public class RDFSDataReasoner{
 public static void addStatements(Model om, Model m, Resource s, Property p, Resource o) {
 	for (StmtIterator i = m.listStatements(s,p,o); i.hasNext(); ) {
             	Statement stmt = i.nextStatement();
 		if (stmt.getObject().isAnon() == false){
 			om.add(stmt);
 		}
         }
 	
 }
 public static void main (String args[]) {
 	String[] onts = new String[100];
 	String[] datas = new String[100];
 	BufferedReader br;
 	try{
                 Vector<String> vec = new Vector<String>(100);
                 br = new BufferedReader(new FileReader(args[0]));
                 while (br.ready()){
                         vec.add(br.readLine());
                 }
                 onts = (String[])vec.toArray(new String[vec.size()]);
 		br.close();
 		vec = new Vector<String>(100);
 		br = new BufferedReader(new FileReader(args[1]));
 		while (br.ready()){
                         vec.add(br.readLine());
                 }
 		datas =  (String[])vec.toArray(new String[vec.size()]);
 		br.close();
         }
         catch (IOException ioe){
                 System.out.println(SQSTimestamp.getTimestamp("time")+" Failed to read ontologies or data list file");
        //      ioe.printStackTrace();
                 System.exit(1);
         }
 	System.out.print(SQSTimestamp.getTimestamp("time")+" Processing Ontologies from "+args[0]);
 	Model schema = FileManager.get().loadModel(onts[0]);
 	for (int i=1; i<onts.length; i++){
 		schema = FileManager.get().readModel(schema,onts[i]);
 		System.out.print(".");
 	}
 	System.out.println("Done");
 	System.out.print(SQSTimestamp.getTimestamp("time")+" Setting up Reasoner");
  	Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
         reasoner = reasoner.bindSchema(schema);
 	System.out.println(" Done");
 	int r=1;
 	for (int i=0; i<datas.length; i++){
 		System.gc();
 	        System.out.print(SQSTimestamp.getTimestamp("time")+" RDFS Inferencing "+datas[i]);
 		Model data = FileManager.get().loadModel(datas[i]);	
 		InfModel infmodel = ModelFactory.createInfModel(reasoner, data);
 		System.out.println(" Done");
 		System.out.println(SQSTimestamp.getTimestamp("time")+" Model has "+infmodel.size()+" triples");
 		System.out.print(SQSTimestamp.getTimestamp("time")+" Rationalising Model");
 		Model outmodel = ModelFactory.createDefaultModel();
 		for ( ResIterator ri = data.listSubjects(); ri.hasNext(); ) {
 	                Resource res = ri.nextResource();	
 		//	System.err.print(r+" "+res.toString());
 			addStatements(outmodel,infmodel,res,null,null);
 			if (r % 100 == 0){
 				System.out.print(".");
 				if (r % 5000 == 0){
 					infmodel = ModelFactory.createInfModel(reasoner, data);
 					System.gc();	
                 			
 				}
 				
 			}
 			r++;
 	//		System.err.println(" Done");
 		}
 		String[] dsplit = datas[i].split("/");
 		String filename=new String(args[2]+dsplit[dsplit.length-1]);
 		System.out.println("Done");
 		System.out.print(SQSTimestamp.getTimestamp("time")+" Printing RDF/XML file to "+filename);
 		try{
 			FileOutputStream RDFFile = new FileOutputStream(new File(filename));
 		        outmodel.write(RDFFile,"RDF/XML",null);
         		System.out.println(" Done");
 		}
 		catch (IOException ioe){
 			System.out.println(" Failed");
 		}
 	}
 }
 }
