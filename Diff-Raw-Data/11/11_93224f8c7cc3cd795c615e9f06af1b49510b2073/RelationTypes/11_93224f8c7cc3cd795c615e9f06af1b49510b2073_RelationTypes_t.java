 package com.core.db;
 
 import org.neo4j.graphdb.RelationshipType;
 
 public class RelationTypes {
	private static enum types implements RelationshipType{
 		KNOWS,IS,CANBE,BEFORE,AFTER;
 	}
 	
	public types type;
 }
