 package ac.kr.ssu.snav.query;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import javax.annotation.Resource;
 
 import ac.kr.ssu.snav.parser.SNavReadingRDF;
 import ac.kr.ssu.snav.parser.SNavStatements;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFactory;
 import com.hp.hpl.jena.query.ResultSetFormatter;
 import com.hp.hpl.jena.query.ResultSetRewindable;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.util.FileManager;
 
 public class SNavQuery {
 	
 	private Model model;
 	
 	private String PREFIXsubject = "http://imc.ssu.ac.kr/law/election#";
 	private String PREFIXpredicate = "http://imc.ssu.ac.kr/2012/08/gensol/election#";
 	private String PREFIXobject = "http://imc.ssu.ac.kr/2012/08/gensol/election#";
 	
 	private String varSubject = "";
 	private String varPredicate = "<"+PREFIXpredicate+"구성인원>";	
 	private String varObject = "";
 	
 	private SNavStatements statement;
 	
 	private Vector<String> vSubject;
 	private Vector<String> vPredicate;
 	private Vector<String> vObject;
 	
 	private Vector<String> mLawNum;
 	private Vector<String> mLawID;
 	private Vector<String> mLawSID;
 	
 	private int tripleNum = 0;
 	
 	public SNavQuery(String var){
 		
 		this.vSubject = new Vector<String>();
 		this.vPredicate = new Vector<String>();
 		this.vObject = new Vector<String>();
 		
 		this.mLawName = new Vector<String>();
 		this.mLawNum = new Vector<String>();
 		this.mLawID = new Vector<String>();
 		this.mLawSID = new Vector<String>();
 				
 		System.out.println("=== in SNavQuery ===");
 		System.out.println("var: " + var);
 
 		
 		//this.varPredicate = "<"+ PREFIXelection + var + ">";
 		
 		//System.out.println(this.varPredicate);
 		
 		
 		SNavStatements stmt = new SNavStatements();
 		
 		PREFIXsubject = stmt.getNameSpace();
 		PREFIXpredicate = stmt.getNameSpaceP();
 		PREFIXobject = stmt.getNameSpaceO();
 				
 		this.model = stmt.getModel();		
 		
 		//String queryString = "SELECT * { <http://imc.ssu.ac.kr/law/election#읍·면·동선거관리위원회> ?predicate ?object }";
 		//String queryString = getQueryPredicate(this.varPredicate);
 		
 		String queryStringSubject = this.getQuerySubject(var); 
 		String queryStringPredicate = this.getQueryPredicate(var); 
 		String queryStringObject = this.getQueryObject(var);
 //		String queryStringLawName = this.getQueryMetaLawName();
 		
 //		this.queryExecuteMetaLawNamePredicate(queryStringLawName);
 		this.queryExecuteSubject(var, queryStringSubject);
 		this.queryExecutePredicate(queryStringPredicate);		
 		this.queryExecuteObject(queryStringObject);
 
 	}
 	
 	private void queryExecuteSubject(String subject, String queryString){
 		
 		Query query = QueryFactory.create(queryString);
 		QueryExecution qexec = QueryExecutionFactory.create(query, this.model);
 		
 		
 		  try {
 		    ResultSet results = qexec.execSelect();		
 		    
 		    List<String> resultVarNames = results.getResultVars();
 		    Iterator iterator = resultVarNames.iterator();
 		    
 		    String var1 = resultVarNames.get(0);
 		    String var2 = resultVarNames.get(1);	 
 		  	
 			if(results.hasNext()){
 			  	//query results navigation
 			    while(results.hasNext()){
 			    	
 			    	QuerySolution soln = results.nextSolution();
 			   		    	
 			    	RDFNode r = soln.get(var1);
 			    	Literal l = soln.getLiteral(var2);		    	
 			    	
 			    	//query results added vector
 			    	int nameSpaceLenth = r.asResource().getNameSpace().length();
 			    	String nonURIValue = r.asResource().getURI().substring(nameSpaceLenth);
 			    	
 			    	//get literal
 			        String convertLiteral = String.valueOf(l.getValue());
 			        
 			        System.out.println(subject + " : " + nonURIValue + " : " + convertLiteral);
 			        
 			        this.vSubject.add(subject);
 			    	this.vPredicate.add(nonURIValue);
 			    	this.vObject.add(convertLiteral);
 			    	
 //			    	for(index=0; index<=this.mLawName.size(); index++){
 //				    	if(convertLiteral.equals(this.mLawName.get(index))){
 //				    		
 //				    	}else{
 //				    		this.vObject.add(convertLiteral);
 //				    	} 
 //			    	}	
 				    		    	
 			    }			    
 			    System.out.println("Triple Size: " + this.vSubject.size());
 			    
 			}else{System.out.println("This keyword is not Subject.");}		   
 		    		    
 		  } finally { qexec.close(); }		
 	}
 
 	private void queryExecutePredicate(String queryString){
 		
 		Query query = QueryFactory.create(queryString);
 		QueryExecution qexec = QueryExecutionFactory.create(query, this.model);
 		
 		Vector<String> compareVector = new Vector<String>();
 		int index = 0;
 		int first = 0;
 		
 		  try {
 		    ResultSet results = qexec.execSelect();			 
 		    List<String> resultVarNames = results.getResultVars();
 		    Iterator iterator = resultVarNames.iterator();
 		    
 		    String var1 = resultVarNames.get(0);
 		    String var2 = resultVarNames.get(1);
 		 	
 		  	if(results.hasNext()){
 		  		
 			  	//query results navigation
 			    while(results.hasNext()){
 			    
 			    	QuerySolution soln = results.nextSolution(); 
 			    	
 			    	RDFNode r = soln.get(var1);
 			    	Literal l = soln.getLiteral(var2);		    	
 			    	
 			    	//query results added vector
 			    	int nameSpaceLenth = r.asResource().getNameSpace().length();
 			    	String nonURIValue = r.asResource().getURI().substring(nameSpaceLenth);		    	
 			    	compareVector.add(nonURIValue);
			
 			    	//same subject check
 			    	if(index == first){
 			    		String querySubject = getQuerySubject(nonURIValue);
 				    	queryExecuteESubject(nonURIValue, querySubject);	
 			    	}else if(index >= 1){
 			    		int bool = compareVector.get(index-1).compareTo(compareVector.get(index));
 			    		if(bool != 0){
 			    			String querySubject = getQuerySubject(nonURIValue);
 					    	queryExecuteESubject(nonURIValue, querySubject);
 			    		}else{}
 			    	}
 			    	index++;
 			    				    	
 			    	//String querySubject = getQuerySubject(this.vSubject.get(index));
 //			    	String querySubject = getQuerySubject(nonURIValue);
 //			    	queryExecuteESubject(nonURIValue, querySubject);
 			    		    	
 			    }
 		  	}
 			else{System.out.println("This keyword is not Predicate.");}
 				    		    
 		 } finally { qexec.close(); }		
 		  
 	}
 	
 	private void queryExecuteObject(String queryString){
 		
 		Query query = QueryFactory.create(queryString);
 		QueryExecution qexec = QueryExecutionFactory.create(query, this.model);
 			
 		  try {
 		    ResultSet results = qexec.execSelect();			 
 		    List<String> resultVarNames = results.getResultVars();
 		    Iterator iterator = resultVarNames.iterator();
 		    
 		    String var1 = resultVarNames.get(0);
 
 		  	int index = 0;
 		 	
 		  	//ResultSetFormatter.out(System.out, results);
 		  	
 		  	if(results.hasNext()){
 		  		
 			  	//query results navigation
 			    while(results.hasNext()){
 			    
 			    	QuerySolution soln = results.nextSolution();
  			    	
 			    	RDFNode r = soln.get(var1);
 			    				    	
 			    	//query results added vector
 			    	int nameSpaceLenth = r.asResource().getNameSpace().length();
 			    	String nonURIValue = r.asResource().getURI().substring(nameSpaceLenth);
 			    	//this.vSubject.add(nonURIValue);		    	
 				    
 			    	//String querySubject = getQuerySubject(this.vSubject.get(index));
 			    	String querySubject = getQuerySubject(nonURIValue);
 			    	queryExecuteESubject(nonURIValue, querySubject);
 			    		    	
 			    	//System.out.println(this.vSubject.get(index++));
 			    }
 		  	}
 			else{System.out.println("This keyword is not Object.");}
 				    		    
 		 } finally { qexec.close(); }		
 		  
 	}
 
 	private void queryExecuteESubject(String nonURIValue, String queryString){
 		
 		String nonURISubjectValue = nonURIValue;
 		
 		Query query = QueryFactory.create(queryString);
 		QueryExecution qexec = QueryExecutionFactory.create(query, this.model);
 		
 		try {
 		    ResultSet results = qexec.execSelect();			 
 		    List<String> resultVarNames = results.getResultVars();
 		    
 		    String predicate = resultVarNames.get(0);
 		    String object = resultVarNames.get(1);
 		  			    
 		  	//query results navigation from subject
 		    while(results.hasNext()){
 		    	
 		    	QuerySolution soln = results.nextSolution();
 		   		    	
 		    	RDFNode r = soln.get(predicate);
 		    	Literal l = soln.getLiteral(object);		    	
 		    			    	
 		    	//get predicate.
 		    	int nameSpaceLenth = r.asResource().getNameSpace().length();
 		    	String nonURIPredicateValue = r.asResource().getURI().substring(nameSpaceLenth);	 
 		    	
 		    	//get literal
 		        String convertLiteral = String.valueOf(l.getValue());
 		        
 		        System.out.println(nonURISubjectValue + " : " + nonURIPredicateValue + " : " + convertLiteral);
 		        
 			    this.vSubject.add(nonURISubjectValue);
 		    	this.vPredicate.add(nonURIPredicateValue);		    	
 		    	this.vObject.add(convertLiteral);
 		    }
 		    
 		    //ResultSetFormatter.out(System.out, results);
 		  
 		    System.out.println("Triple Size: " + this.vSubject.size());
 		   } finally { qexec.close(); }	
 		
 		//ResultSet resultset = qexec.execSelect();
 		//ResultSetFormatter.out(System.out, resultset);
 //		this.tripleNum = resultset.getRowNumber();
 //		
 //		for(int i=0; i<this.tripleNum; i++)
 //			this.vSubject.add(nonURIValue);
 //		
 //		System.out.println("Triple count per subject: " + this.tripleNum + "\n");
 		
 	}
 	
 	private void queryExecuteMetaLawNamePredicate(String queryString){
 		
 		Query query = QueryFactory.create(queryString);
 		QueryExecution qexec = QueryExecutionFactory.create(query, this.model);
 		
 		  try {
 		    ResultSet results = qexec.execSelect();		
 		    
 		    List<String> resultVarNames = results.getResultVars();
 		    
 		    String var1 = resultVarNames.get(0);
 		    String var2 = resultVarNames.get(1);	 
 		  	
 			if(results.hasNext()){
 			  	//query results navigation
 			    while(results.hasNext()){
 			    
 			    	QuerySolution soln = results.nextSolution();
 			   		    	
 			    	Literal l = soln.getLiteral(var2);		    	
 			    				    	
 			    	//get literal
 			        String convertLiteral = String.valueOf(l.getValue());
 			        	    	
 				    this.mLawName.add(convertLiteral);
 				    		    	
 			    }
 			}else{}		   
 		    		    
 		  } finally { qexec.close(); }		
 		  
 	}
 
 	private String getQuerySubject(String varSubject){
 		
 		varSubject = "<"+ this.PREFIXsubject + varSubject + ">";
 		//System.out.println(varSubject);
 		String queryString = "SELECT * WHERE{ "+ varSubject + " ?predicate ?object } ORDER BY ?predicate";
 		//String queryString = "SELECT * WHERE{ "+ varSubject + " ?predicate ?object }";
 		System.out.println(queryString);
 		return queryString;
 		
 	}
 	
 	private String getQueryPredicate(String varPredicate){
 		
 		varPredicate = "<"+ this.PREFIXpredicate + varPredicate + ">";
 		//this.varPredicate = varPredicate;
		String queryString = "SELECT * WHERE{ ?subject  " + varPredicate + " ?object } ORDER BY ?subject";
 		System.out.println(queryString);
 		return queryString;
 	}
 	
 	private String getQueryObject(String varObject){
 		
		String queryString = "SELECT * WHERE{ ?subject ?predicate \"" + varObject + "\" } ORDER BY ?subject";
 		System.out.println(queryString);
 		return queryString;
 		  
 	}
 	
 	private String getQueryMetaLawName(){
 		
 		String varPredicate = "<"+ this.PREFIXpredicate + "법명" + ">";
 		String queryString = "SELECT * WHERE{ ?subject  " + varPredicate + " ?object }";
 		System.out.println(queryString);
 		return queryString;		  
 	}
 	
 	private String getQuerySPO(){
 		
 		String queryString = "SELECT * WHERE{ ?subject ?predicate ?object }";
 		return queryString;
 		  
 	}
 	
 	private void queryProcess(ResultSet results){
 		
 		for ( ; results.hasNext() ; )
 	    {
 	      QuerySolution soln = results.nextSolution() ;
 	      
 	      // Get a result variable by name.
 	      RDFNode x = soln.get("http://imc.ssu.ac.kr/law/election#선거관리위원회") ;    
 	      
 	      // Get a result variable - must be a resource
 	      Resource r = (Resource) soln.getResource("http://imc.ssu.ac.kr/law/election#선거관리위원회"); 
 	      
 	      // Get a result variable - must be a literal
 	      Literal l = soln.getLiteral("VarL");  
 	      
 //	      System.out.println(x.toString());  
 //	      System.out.println(r.toString());
 	    } 
 	}
 	
 	public static void generalQueryProcess(){
 		
 		FileManager.get().addLocatorClassLoader(SNavQuery.class.getClassLoader());
        Model model = FileManager.get().loadModel("exampleRDF/law5.rdf");
         
        // String queryString = "SELECT * { ?s ?p ?o }";
       // String queryString = "SELECT (COUNT(*) AS ?no) { <http://imc.ssu.ac.kr/law/legislation#공적자금관리위원회> <http://imc.ssu.ac.kr/2013/04/gensol/legislation#법명> ?o }";

        String queryString = "SELECT * WHERE{ ?subject  <http://imc.ssu.ac.kr/2013/04/gensol/legislation#기재사항> ?object} ORDER BY ?subject";
         Query query = QueryFactory.create(queryString);
         QueryExecution qexec = QueryExecutionFactory.create(query, model);
         try {
             ResultSetRewindable results = ResultSetFactory.makeRewindable(qexec.execSelect());
 
 //            System.out.println("---- XML ----");
 //            ResultSetFormatter.outputAsXML(System.out, results);
 //            results.reset();
 
             System.out.println("---- Text ----");
             ResultSetFormatter.out(System.out, results);
             results.reset();
 
 //            System.out.println("\n---- CSV ----");
 //            ResultSetFormatter.outputAsCSV(System.out, results);
 //            results.reset();
 //
 //            System.out.println("\n---- TSV ----");
 //            ResultSetFormatter.outputAsTSV(System.out, results);
 //            results.reset();
 //            
 //            System.out.println("\n---- JSON ----");
 //            ResultSetFormatter.outputAsJSON(System.out, results);
 //            results.reset();
             
         } finally {
             qexec.close();
         }      
 	}
 	
 	public SNavStatements getSnavStatements(){
 		return this.statement;
 	}
 	
 	/**
 	 * @return the vSubject
 	 */
 	public Vector<String> getvSubject() {
 		return this.vSubject;
 	}
 
 	/**
 	 * @param vSubject the vSubject to set
 	 */
 	public void setvSubject(Vector<String> vSubject) {
 		this.vSubject = vSubject;
 	}
 
 	/**
 	 * @return the vPredicate
 	 */
 	public Vector<String> getvPredicate() {
 		return vPredicate;
 	}
 
 	/**
 	 * @param vPredicate the vPredicate to set
 	 */
 	public void setvPredicate(Vector<String> vPredicate) {
 		this.vPredicate = vPredicate;
 	}
 
 	/**
 	 * @return the vObject
 	 */
 	public Vector<String> getvObject() {
 		return vObject;
 	}
 
 	/**
 	 * @param vObject the vObject to set
 	 */
 	public void setvObject(Vector<String> vObject) {
 		this.vObject = vObject;
 	}
 	private Vector<String> mLawName;
 	/**
 	 * @return the mLawName
 	 */
 	public Vector<String> getmLawName() {
 		return mLawName;
 	}
 
 	/**
 	 * @param mLawName the mLawName to set
 	 */
 	public void setmLawName(Vector<String> mLawName) {
 		this.mLawName = mLawName;
 	}
 
 	/**
 	 * @return the mLawNum
 	 */
 	public Vector<String> getmLawNum() {
 		return mLawNum;
 	}
 
 	/**
 	 * @param mLawNum the mLawNum to set
 	 */
 	public void setmLawNum(Vector<String> mLawNum) {
 		this.mLawNum = mLawNum;
 	}
 
 	/**
 	 * @return the mLawID
 	 */
 	public Vector<String> getmLawID() {
 		return mLawID;
 	}
 
 	/**
 	 * @param mLawID the mLawID to set
 	 */
 	public void setmLawID(Vector<String> mLawID) {
 		this.mLawID = mLawID;
 	}
 
 	/**
 	 * @return the mLawSID
 	 */
 	public Vector<String> getmLawSID() {
 		return mLawSID;
 	}
 
 	/**
 	 * @param mLawSID the mLawSID to set
 	 */
 	public void setmLawSID(Vector<String> mLawSID) {
 		this.mLawSID = mLawSID;
 	}
 	
 	public static void main(String args[]){
 		
		new SNavQuery("기재사항");
 		//generalQueryProcess();
 	}
 }
