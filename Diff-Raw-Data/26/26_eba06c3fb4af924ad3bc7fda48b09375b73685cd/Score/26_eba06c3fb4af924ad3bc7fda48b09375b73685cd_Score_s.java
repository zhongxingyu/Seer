 /*******************************************************************************
  * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the new BSD license
  * which accompanies this distribution, and is available at
  * http://www.opensource.org/licenses/bsd-license.html
  * 
  * Contributors:
  *     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
  *     Christopher Barnes, Narayan Raum - scoring ideas and algorithim
  *     Yang Li - pairwise scoring algorithm
  *     Christopher Barnes - regex scoring algorithim
  ******************************************************************************/
 package org.vivoweb.ingest.score;
 
 import static java.util.Arrays.asList;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import javax.xml.parsers.ParserConfigurationException;
 import joptsimple.OptionParser;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.vivoweb.ingest.util.ArgList;
 import org.vivoweb.ingest.util.JenaConnect;
 import org.vivoweb.ingest.util.Record;
 import org.vivoweb.ingest.util.RecordHandler;
 import org.xml.sax.SAXException;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 
 /***
  *  VIVO Score
  *  @author Nicholas Skaggs nskaggs@ctrip.ufl.edu
  */
 public class Score {
 		/**
 		 * Log4J Logger
 		 */
 		private static Log log = LogFactory.getLog(Score.class);
 		/**
 		 * Model for VIVO instance
 		 */
 		private Model vivo;
 		/**
 		 * Model where input is stored
 		 */
 		private Model scoreInput;
 		/**
 		 * Model where output is stored
 		 */
 		private Model scoreOutput;
 		/**
 		 * Attribute to match for exactMatch algorithim
 		 */
 		private String matchAttribute;
 		
 		
 		/**
 		 * Main method
 		 * @param args command line arguments
 		 */
 		public static void main(String... args) {
 			
 			log.info("Scoring: Start");
 			try {
				new Score(new ArgList(getParser(), args, "r","v"));
 			} catch(IllegalArgumentException e) {
 				try {
 					log.fatal(e);
 					getParser().printHelpOn(System.out);
 				} catch(IOException e1) {
 					log.fatal(e1.getMessage(),e1);
 				}
 			} catch(Exception e) {
 				log.fatal(e.getMessage(),e);
 			}
 			log.info("Scoring: End");
 		}
 		
 		/**
 		 * Get the OptionParser
 		 * @return the OptionParser
 		 */
 		private static OptionParser getParser() {
 			OptionParser parser = new OptionParser();
 			//parser.acceptsAll(asList("e", "exactmatch")).withRequiredArg().describedAs("exact matching algorithim");
 			//parser.acceptsAll(asList("p", "pairwise")).withRequiredArg().describedAs("pairwise algorithim");
 			//parser.acceptsAll(asList("u", "username")).withRequiredArg().describedAs("database username");
 			//parser.acceptsAll(asList("p", "password")).withRequiredArg().describedAs("database password");
			parser.acceptsAll(asList("r", "rdfRecordHandler")).withRequiredArg().describedAs("rdfRecordHandler config file path");
 			parser.acceptsAll(asList("v", "vivoJenaConfig")).withRequiredArg().describedAs("vivoJenaConfig config file path");
 			parser.acceptsAll(asList("e", "exactMatch")).withRequiredArg().describedAs("exact match fieldname").defaultsTo("workEmail");
 			parser.acceptsAll(asList("t", "tempModel")).withRequiredArg().describedAs("temporary working model name").defaultsTo("tempModel");
 			parser.acceptsAll(asList("o", "outputModel")).withRequiredArg().describedAs("output model name").defaultsTo("outputModel");
 			parser.acceptsAll(asList("n","allow-non-empty-working-model"),"flag to allow a non-empty working model");
 			return parser;
 		}
 		
 		
 		/**
 		 * Constructor	
 		 * @param jenaVivo model containing vivo statements
 		 * @param jenaScoreInput model containing statements to be scored
 		 * @param jenaScoreOutput output model
 		 * @param exactMatchArg exact match attribute
 		 */
 		public Score(Model jenaVivo, Model jenaScoreInput, Model jenaScoreOutput, String exactMatchArg) {
 			this.vivo = jenaVivo;
 			this.scoreInput = jenaScoreInput;
 			this.scoreOutput = jenaScoreOutput;
 			this.matchAttribute = exactMatchArg;
 		}
 		
 		/**
 		 * Constructor
 		 * @param opts option set of parsed args
 		 */
 		public Score(ArgList opts) {
 			//TODO Nicholas: verify args; ensure required args are all present
 //			String jdbcDriverClass = (String)opts.valueOf("d ");
 //			try {
 //				Class.forName(jdbcDriverClass);
 //			} catch(ClassNotFoundException e) {
 //				throw new IOException(e.getMessage(),e);
 //			}
 //			String connLine = (String)opts.valueOf("c");
 //			String username = (String)opts.valueOf("u");
 //			String password = (String)opts.valueOf("p");
 //			Connection dbConn;
 //			try {
 //				dbConn = DriverManager.getConnection(connLine, username, password);
 //			} catch(SQLException e) {
 //				throw new IOException(e.getMessage(),e);
 //			}
 			
 			//if (args.length != 4) {
 			//	log.error("Usage requires 4 arguments rdfRecordHandler tempJenaConfig vivoJenaConfig outputJenaConfig");
 			//	return;
 			//}
 			
 			//Get optional inputs / set defaults
 			String workingModel = opts.get("t");
 			String outputModel = opts.get("o");
			Boolean allowNonEmptyWorkingModel= new Boolean(opts.has("n"));
 			String exactMatchArg = opts.get("e");
 
 			try {
 				log.info("Loading configuration and models");
				RecordHandler rh = RecordHandler.parseConfig(opts.get("r"));	
 				
 				//Connect to vivo
 				JenaConnect jenaVivoDB = JenaConnect.parseConfig(opts.get("v"));
 				
 				//Create working model
 				JenaConnect jenaTempDB = new JenaConnect(jenaVivoDB,workingModel);
 				
 				//Create output model
 				JenaConnect jenaOutputDB = new JenaConnect(jenaVivoDB,outputModel);
 				
 				//Load up rdf data from translate into temp model
 				Model jenaInputDB = jenaTempDB.getJenaModel();
 				
				if (!jenaInputDB.isEmpty() && !allowNonEmptyWorkingModel.booleanValue()) {
 					log.warn("Working model was not empty!");
 					jenaInputDB.removeAll();
 				}
 				
 				for (Record r: rh) {
 					jenaInputDB.read(new ByteArrayInputStream(r.getData().getBytes()), null);
 				}
 				
 				new Score(jenaVivoDB.getJenaModel(), jenaInputDB, jenaOutputDB.getJenaModel(), exactMatchArg).execute();
 			} catch(ParserConfigurationException e) {
 				log.fatal(e.getMessage(),e);
 			} catch(SAXException e) {
 				log.fatal(e.getMessage(),e);
 			} catch(IOException e) {
 				log.fatal(e.getMessage(),e);
 			}
 		}
 
 		/**
 		 * Executes scoring algorithms
 		 */
 		public void execute() {		
 			 	ResultSet scoreInputResult;
 			 	
 			 	//DEBUG
 				 	//TODO Nicholas: howto pass this in via config
 			 		log.info("Executing matchResult");
 				 	String matchQuery = "PREFIX score: <http://vivoweb.org/ontology/score#> " +
 			    						"SELECT ?x ?" + this.matchAttribute + " " + 
 			    						"WHERE { ?x score:" + this.matchAttribute + " ?" + this.matchAttribute + "}";
 				 	String coreAttribute = "core:" + this.matchAttribute;
 				 	
 			 	//DEBUG
 			 	
 				//Attempt Matching
 
 			 	//Exact Matches
 			 	//TODO Nicholas: finish implementation of exact matching loop
 			 	//for each matchAttribute
 			 		scoreInputResult = executeQuery(this.scoreInput, matchQuery);
 			 		exactMatch(this.vivo,this.scoreOutput,this.matchAttribute,coreAttribute,scoreInputResult);
 			    //end for
 			 		
 		 		//DEBUG
 				 	//TODO Nicholas: howto pass this in via config
 				 	//matchAttribute = "author";
 				 	//matchQuery = "PREFIX score: <http://vivoweb.org/ontology/score#> " +
 			    	//       		 "SELECT ?x ?author " +
 			    	//			 "WHERE { ?x score:author ?author}";
 				 	//coreAttribute = "core:author";
 			 	//DEBUG
 				
 				//Pairwise Matches
 				//TODO Nicholas: finish implementation of pairwise matching loop
 			 	//for each matchAttribute
 			 		//scoreInputResult = executeQuery(scoreInput, matchQuery);
 			 		//pairwiseScore(vivo,scoreInput,matchAttribute,coreAttribute,scoreInputResult);	
 			 	//end for
 			 		
 				//Close and done
 				this.scoreInput.close();
 		    	this.scoreOutput.close();
 		    	this.vivo.close();
 		}
 		
 		/**
 		 * Executes a sparql query against a JENA model and returns a result set
 		 * @param  model a model containing statements
 		 * @param  queryString the query to execute against the model
 		 * @return queryExec the executed query result set
 		 */
 		 private static ResultSet executeQuery(Model model, String queryString) {
 		    	Query query = QueryFactory.create(queryString);
 		    	QueryExecution queryExec = QueryExecutionFactory.create(query, model);
 		    	
 		    	return queryExec.execSelect();
 			}
 		 
 		 
 		/**
 		 * Commits resultset to a matched model
 		 * @param  result a model containing vivo statements
 		 * @param  storeResult the result to be stored
 		 * @param  paperResource the paper of the resource
 		 * @param  matchNode the node to match
 		 * @param  paperNode the node of the paper
 		 */
 		 private static void commitResultSet(Model result, ResultSet storeResult, Resource paperResource, RDFNode matchNode, RDFNode paperNode) {
 				RDFNode authorNode;
 				QuerySolution vivoSolution;
 				
 				//loop thru resultset
 	 	    	while (storeResult.hasNext()) {
 	 	    		vivoSolution = storeResult.nextSolution();
 	 	    		
 	 	    		//Grab person URI
 	                authorNode = vivoSolution.get("x");
 	                log.info("Found " + matchNode.toString() + " for person " + authorNode.toString());
 	                log.info("Adding paper " + paperNode.toString());
 	
 	                result.add(recursiveSanitizeBuild(paperResource,null));
 	                
 	                replaceResource(authorNode,paperNode, result);
 	                
 					//take results and store in matched model
 	                result.commit();
 	 	    	} 
 		 }
 		 
 		/**
 		 * Traverses paperNode and adds to toReplace model 
 		 * @param  mainNode primary node
 		 * @param  paperNode node of paper
 		 * @param  toReplace model to replace
 		 */
 		private static void replaceResource(RDFNode mainNode, RDFNode paperNode, Model toReplace){
 			 Resource authorship;
 			 Property linkedAuthorOf = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#linkedAuthor");
              Property authorshipForPerson = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#authorInAuthorship");
              
              Property authorshipForPaper = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#informationResourceInAuthorship");
              Property paperOf = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#linkedInformationResource");
              Property rankOf = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#authorRank");
              
              Resource flag1 = ResourceFactory.createResource("http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing");
              Resource authorshipClass = ResourceFactory.createResource("http://vivoweb.org/ontology/core#Authorship");
              
              Property rdfType = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
              Property rdfLabel = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
 			 int authorRank = 1;	
 			 
              log.info("Link paper " + paperNode.toString() + " to person " + mainNode.toString() + " in VIVO");
              authorship = ResourceFactory.createResource(paperNode.toString() + "/vivoAuthorship/l1");
              
              //string that finds the last name of the person in VIVO
              Statement authorLName = ((Resource)mainNode).getProperty(ResourceFactory.createProperty("http://xmlns.com/foaf/0.1/lastName"));
              
              String authorQuery = "PREFIX core: <http://vivoweb.org/ontology/core#> " +
          							"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
 									"SELECT ?badNode " +
 									"WHERE {?badNode foaf:lastName \"" + authorLName.getObject().toString() + "\" . " +
 											"?badNode core:authorInAuthorship ?authorship . " +
 											"?authorship core:linkedInformationResource <" + paperNode.toString() + "> }";
              
              log.debug(authorQuery);
              
              ResultSet killList = executeQuery(toReplace,authorQuery);
              
              while(killList.hasNext()) {
             	 QuerySolution killSolution = killList.nextSolution();
 	 	    		
 	 	    	 //Grab person URI
             	 Resource removeAuthor = killSolution.getResource("badNode");
 	             
             	 //query the paper for the first author node (assumption that affiliation matches first author)
             	 log.debug("Delete Resource " + removeAuthor.toString());
                  
 	             //return a statement iterator with all the statements for the Author that matches, then remove those statements
             	 //model.remove is broken so we are using statement.remove
 	             StmtIterator deleteStmts = toReplace.listStatements(null, null, removeAuthor);
 	             while (deleteStmts.hasNext()) {
 	            	 Statement dStmt = deleteStmts.next();
 	            	 log.debug("Delete Statement " + dStmt.toString());
 	            		            	 
                  	if (!dStmt.getSubject().equals(removeAuthor)) {
                  		Statement authorRankStmt = dStmt.getSubject().getProperty(rankOf);
                  		authorRank = authorRankStmt.getObject().asLiteral().getInt();
        	                              		
                  		StmtIterator authorshipStmts = dStmt.getSubject().listProperties();
 	       	            while (authorshipStmts.hasNext()) {
 	       	            	 log.debug("Delete Statement " + authorshipStmts.next().toString());
 	       	            }
 	       	            dStmt.getSubject().removeProperties();
 	       	            
 	       	            StmtIterator deleteAuthorshipStmts = toReplace.listStatements(null, null, dStmt.getSubject());
 	       	            while (deleteAuthorshipStmts.hasNext()) {
 	       	            	Statement dASStmt = deleteAuthorshipStmts.next();
 	       	            	log.debug("Delete Statement " + dASStmt.toString());
 	       	            	dASStmt.remove();
 	       	            }	       	            
 	       	            	       	            
                  	}                 	
                  	
 	             }	             
 	             
 	             StmtIterator authorStmts = removeAuthor.listProperties();
 	             while (authorStmts.hasNext()) {
 	            	 log.debug("Delete Statement " + authorStmts.next().toString());
 	             }
 	             removeAuthor.removeProperties();
              }
                          
              
              toReplace.add(authorship,linkedAuthorOf,mainNode);
              log.trace("Link Statement [" + authorship.toString() + ", " + linkedAuthorOf.toString() + ", " + mainNode.toString() + "]");
              toReplace.add((Resource)mainNode,authorshipForPerson,authorship);
              log.trace("Link Statement [" + mainNode.toString() + ", " + authorshipForPerson.toString() + ", " + authorship.toString() + "]");
              toReplace.add(authorship,paperOf,paperNode);
              log.trace("Link Statement [" + authorship.toString() + ", " + paperOf.toString() + ", " + paperNode.toString() + "]");
              toReplace.add((Resource)paperNode,authorshipForPaper,authorship);
              log.trace("Link Statement [" + paperNode.toString() + ", " + authorshipForPaper.toString() + ", " + authorship.toString() + "]");
              toReplace.add(authorship,rdfType,flag1);
              log.trace("Link Statement [" + authorship.toString() + ", " + rdfType.toString() + ", " + flag1.toString() + "]");
              toReplace.add(authorship,rdfType,authorshipClass);
              log.trace("Link Statement [" + authorship.toString() + ", " + rdfType.toString() + ", " + authorshipClass.toString() + "]");
              toReplace.add(authorship,rdfLabel,"Authorship for Paper");
              log.trace("Link Statement [" + authorship.toString() + ", " + rdfLabel.toString() + ", " + "Authorship for Paper]");
              toReplace.addLiteral(authorship,rankOf,authorRank);
              log.trace("Link Statement [" + authorship.toString() + ", " + rankOf.toString() + ", " + String.valueOf(authorRank) + "]");
 
              toReplace.commit();
 		 }
 		 
 		/**
 		 * Traverses paperNode and adds to toReplace model 
 		 * @param mainRes the main resource
 		 * @param linkRes the resource to link it to
 		 * @return the model containing the sanitized info so far 
 		 */
 		 private static Model recursiveSanitizeBuild(Resource mainRes, Resource linkRes){
 			 Model returnModel = ModelFactory.createDefaultModel();
 			 Statement stmt;
 			 
 			 StmtIterator mainStmts = mainRes.listProperties();
 			 
 			 while (mainStmts.hasNext()) {
              	stmt = mainStmts.nextStatement();
               	log.trace("Statement " + stmt.toString());
 			 
               	//Don't add any scoring statements
 				 if (!stmt.getPredicate().toString().contains("/score")) {
 	          		returnModel.add(stmt);
 	          		                    	
 	                 	if ((stmt.getObject().isResource() && !((Resource)stmt.getObject()).equals(linkRes)) && !((Resource)stmt.getObject()).equals(mainRes)) {
 	                 		returnModel.add(recursiveSanitizeBuild((Resource)stmt.getObject(), mainRes));
 	                 	}
 	                 	if (!stmt.getSubject().equals(linkRes) && !stmt.getSubject().equals(mainRes)) {
 	                 		returnModel.add(recursiveSanitizeBuild(stmt.getSubject(), mainRes));
 	                 	}
 	          		}
 			 }
 			 
 			 return returnModel;
 		 }
 		 
 		 
 		/**
 		* Executes a pair scoring method, utilizing the matchAttribute. This attribute is expected to 
 		* return 2 to n results from the given query. This "pair" will then be utilized as a matching scheme 
 		* to construct a sub dataset. This dataset can be scored and stored as a match 
 		* @param  matched a model containing statements describing known authors
 		* @param  score a model containing statements to be disambiguated
 		* @param  matchAttribute an attribute to perform the exact match
 		* @param  coreAttribute an attribute to perform the exact match against from core ontology
 		* @param  matchResult contains a resultset of the matchAttribute
 		* @return score model
 		*/
 		@SuppressWarnings("unused")
 		private static Model pairwiseScore(Model matched, Model score, String matchAttribute, String coreAttribute, ResultSet matchResult) {			
 		 	//iterate thru scoringInput pairs against matched pairs
 		 	//TODO Nicholas: support partial scoring, multiples matches against several pairs
 		 	//if pairs match, store publication to matched author in Model
 			//TODO Nicholas: return scoreInput minus the scored statements
 			
 			String scoreMatch;
 			RDFNode matchNode;
 			QuerySolution scoreSolution;
 
 		 	//create pairs of *attribute* from matched
 	    	log.info("Creating pairs of " + matchAttribute + " from input");
 	    	
 	    	//look for exact match in vivo
 	    	while (matchResult.hasNext()) {
 	    		scoreSolution = matchResult.nextSolution();
                 matchNode = scoreSolution.get(matchAttribute);
                 
                 scoreMatch = matchNode.toString();
                 
                 log.info("\nChecking for " + scoreMatch + " in VIVO");
             }	    			 
 	    	
 	    	//TODO Nicholas: return scoreInput minus the scored statements			
 			return score;
 		 }
 		 
 		 
 		 /**
 		 * Executes an exact matching algorithm for author disambiguation
 		 * @param  matched a model containing statements describing authors
 		 * @param  output a model containing statements to be disambiguated
 		 * @param  matchAttribute an attribute to perform the exact match
 		 * @param  coreAttribute an attribute to perform the exact match against from core ontology
 		 * @param  matchResult contains a resultset of the matchAttribute
 		 * @return model of matched statements
 		 */
 		 private static Model exactMatch(Model matched, Model output, String matchAttribute, String coreAttribute, ResultSet matchResult) {
 				String scoreMatch;
 				String queryString;
 				Resource paperResource;
 				RDFNode matchNode;
 				RDFNode paperNode;
 				ResultSet vivoResult;
 				QuerySolution scoreSolution;
 
                 
 		    	log.info("Looping thru " + matchAttribute + " from input");
 		    	
 		    	//look for exact match in vivo
 		    	while (matchResult.hasNext()) {
 		    		scoreSolution = matchResult.nextSolution();
 	                matchNode = scoreSolution.get(matchAttribute);
 	                //TODO Nicholas: paperNode must currently be 'x'; howto abstract?
 	                paperNode = scoreSolution.get("x");
 	                //TODO Nicholas: paperResource must currently be 'x'; howto abstract?
 	                paperResource = scoreSolution.getResource("x");
 	                
 	                scoreMatch = matchNode.toString();
 	                
 	                log.info("\nChecking for " + scoreMatch + " from " + paperNode.toString() + " in VIVO");
 	    			
 	                //Select all matching attributes from vivo store
 	    			queryString =
 						"PREFIX core: <http://vivoweb.org/ontology/core#> " +
 						"SELECT ?x " +
 						"WHERE { ?x " + coreAttribute + " \"" +  scoreMatch + "\" }";
 	    			
 	    			log.debug(queryString);
 	    			
 	    			//TODO Nicholas: how to combine result sets? not possible in JENA
 	    			vivoResult = executeQuery(matched, queryString);
 	    			//while (vivoResult.hasNext()) {
 	    			//	System.out.println(vivoResult.toString());
 	    			//}
 	    			
 	    			commitResultSet(output,vivoResult,paperResource,matchNode,paperNode);
 	            }	    			 
 		    	
 		    	//TODO Nicholas: return scoreInput minus the scored statements
 		    	return output;
 		 }
 	}
