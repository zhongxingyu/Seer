 package com.core.db;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.factory.GraphDatabaseFactory;
 
 /**
  * 
  * @author Josh Wretlind
  * Wraps the Neo4j database
  *
  */
 public class DBWrapper {
 	
 	GraphDatabaseService graphDb;
 	
 	public DBWrapper(){
 		runDB();
 	}
 	
 	public void runDB(){
 		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("");
 		registerShutdownHook( graphDb );
 	}
 	
 	private static void registerShutdownHook( final GraphDatabaseService graphDb )
 	{
 	    // Registers a shutdown hook for the Neo4j instance so that it
 	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
 	    // running example before it's completed)
 	    Runtime.getRuntime().addShutdownHook( new Thread()
 	    {
 	        @Override
 	        public void run()
 	        {
 	            graphDb.shutdown();
 	        }
 	    } );
 	}
 	
 	public void shutdownDB(){
 		graphDb.shutdown();
 	}
 	
 	public Node writeNode(String key, Object value){
 		Transaction tx = graphDb.beginTx();
 		Node n = graphDb.createNode();
 
 		try{
 			n.setProperty(key, value);
 		    tx.success();
 		}
 		finally{
 		    tx.finish();
 		}
 		
 		return n;
 	}
 	
 	public Relationship createRelationship(Node from, Node to, RelationTypes type, String key, Object value){
 		Transaction tx = graphDb.beginTx();
 		Relationship rel = from.createRelationshipTo( to, type.type );
 
 		try{
 			rel.setProperty(key, value);
 		    tx.success();
 		}
 		finally{
 		    tx.finish();
 		}
 		return rel;
 	}
 	
 	public void deleteNode(Node n){
 		Transaction tx = graphDb.beginTx();
 		try{
 			n.delete();
 		    tx.success();
 		}
 		finally{
 		    tx.finish();
 		}
 	}
 	
 	public void deleteRelationship(Relationship rel){
 		Transaction tx = graphDb.beginTx();
 		try{
 			rel.delete();
 		    tx.success();
 		}
 		finally{
 		    tx.finish();
 		}
 	}
 	
 	
	
 }
