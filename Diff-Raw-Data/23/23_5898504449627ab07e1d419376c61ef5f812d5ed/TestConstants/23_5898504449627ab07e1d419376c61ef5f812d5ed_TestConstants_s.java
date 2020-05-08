 package com.github.podd.api.test;
 
 /**
  * Interface containing constant values for use in test verification.
  */
 public interface TestConstants
 {
 
     /* Expected number of triples in schema ontologies */
     
     public static final int EXPECTED_TRIPLE_COUNT_DC_TERMS_CONCRETE = 39;
     public static final int EXPECTED_TRIPLE_COUNT_DC_TERMS_INFERRED = 7;
     
     public static final int EXPECTED_TRIPLE_COUNT_FOAF_CONCRETE = 38;
     public static final int EXPECTED_TRIPLE_COUNT_FOAF_INFERRED = 18;
     
     public static final int EXPECTED_TRIPLE_COUNT_PODD_USER_CONCRETE = 217;
     public static final int EXPECTED_TRIPLE_COUNT_PODD_USER_INFERRED = 34;
     
     public static final int EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE = 321;
     public static final int EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED = 72;
     
     public static final int EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE = 1266;
     public static final int EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED = 195;
     
     public static final int EXPECTED_TRIPLE_COUNT_PODD_PLANT_CONCRETE = 83;
     public static final int EXPECTED_TRIPLE_COUNT_PODD_PLANT_INFERRED = 278;
  
     /** Test resource: artifact with 1 internal object */
     public static final String TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT = "/test/artifacts/basic-1-internal-object.rdf";
     public static final int TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES = 28;
     public static final int TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES = 300;
 
     /** Test resource: artifact with 1 internal object, slightly different */
     public static final String TEST_ARTIFACT_BASIC_PROJECT_2 = "/test/artifacts/basicProject-2.rdf";
     public static final int TEST_ARTIFACT_BASIC_PROJECT_2_CONCRETE_TRIPLES = 29;
     public static final int TEST_ARTIFACT_BASIC_PROJECT_2_INFERRED_TRIPLES = 303;
     
     public static final String TEST_ARTIFACT_PURLS_2_FILE_REFS = "/test/artifacts/project-purls-file-ref.rdf"; 
     
     
     /** Test resource (in Turtle): artifact with temporary URIs and 1 internal object having a PURL */
     public static final String TEST_ARTIFACT_TTL_1_INTERNAL_OBJECT =  "/test/artifacts/connected-1-object.ttl";
 
     /** Test resource: artifact with PURLs and multiple internal objects in Turtle format */
     public static final String TEST_ARTIFACT_20130206 = "/test/artifacts/basic-20130206.ttl";
     public static final int TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES = 396;
 
     /** Test resource (in Turtle): inferred artifact with PURLs and multiple internal objects */
     public static final String TEST_ARTIFACT_20130206_INFERRED = "/test/artifacts/basic-20130206-inferred.ttl";
     
     
     /** Test resource: fragment containing a Publication object  in Turtle format */
     public static final String TEST_ARTIFACT_FRAGMENT_NEW_PUBLICATION_OBJECT = "/test/artifacts/fragment-new-publication.ttl";
 
     /** Test resource: fragment containing a Publication object  in Turtle format */
     public static final String TEST_ARTIFACT_FRAGMENT_INCONSISTENT_OBJECT = "/test/artifacts/fragment-inconsistent-object.ttl";
 
     /** Test resource: fragment containing a Publication object  in Turtle format */
     public static final String TEST_ARTIFACT_FRAGMENT_MODIFIED_PUBLICATION_OBJECT = "/test/artifacts/fragment-modified-publication.ttl";
     
     /** Test resource: fragment containing a new File Reference object  in RDF/XML format */
     public static final String TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT = "/test/artifacts/fragment-new-file-ref.rdf";
 
     /** Test resource: fragment modifying Demo_Investigation object to no longer contain SqueekeeMaterial. In Turtle format */
     public static final String TEST_ARTIFACT_FRAGMENT_MODIFY_DEMO_INVESTIGATION = "/test/artifacts/fragment-modify-demo-investigation.ttl";
     
     /** Test resource: fragment moves SqueekeeMaterial to under My_Treatment1. In Turtle format */
     public static final String TEST_ARTIFACT_FRAGMENT_MOVE_DEMO_INVESTIGATION = "/test/artifacts/fragment-move-demo-investigation.ttl";
 
     /** Test resource: any file which exists and can be used as a file reference */
    public static final String TEST_FILE = "/test/artifacts/basicProject-2.rdf";
 
 }
