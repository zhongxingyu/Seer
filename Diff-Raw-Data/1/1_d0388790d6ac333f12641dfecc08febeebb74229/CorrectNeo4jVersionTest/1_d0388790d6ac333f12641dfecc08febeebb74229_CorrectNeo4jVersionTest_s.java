 package com.neotechnology.build.test;
 
 import static org.junit.Assert.*;
 
 import org.neo4j.kernel.Neo4jKernelVersion;
 
 public class CorrectNeo4jVersionTest {
     public @Test void hasSnapshotVersionOfNeo4j() {
 	assertTrue(Neo4jKernelVersion.getVersionString().startsWith("1.7-SNAPSHOT"));
     }
 }
