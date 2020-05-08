 /*****************************************************************************
  * File:    Init.java
  * Project: linux-mag-article
  * Created: 21 Mar 2011
  * By:      ian
  *
  * Copyright (c) 2011 Epimorphics Ltd. All rights reserved.
  *****************************************************************************/
 
 // Package
 ///////////////
 
 package com.epimorphics.plb;
 
 
 // Imports
 ///////////////
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.cli.*;
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.epimorphics.plb.vocabs.DOAP;
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.tdb.TDBFactory;
 import com.hp.hpl.jena.tdb.sys.SetupTDB;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 /**
  * <p>Initialize a project log book</p>
  *
  * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
  */
 public class Init
     extends PLB
 {
     /***********************************/
     /* Constants                       */
     /***********************************/
 
     /***********************************/
     /* Static variables                */
     /***********************************/
 
     private static final Logger log = LoggerFactory.getLogger( Init.class );
 
     public static Options options;
     static {
         options = new Options();
        PLB.addCommonOptions( options );
         options.addOption( "f", "force", false, "Force the persistent model to reset to empty" );
     }
 
     /***********************************/
     /* Instance variables              */
     /***********************************/
 
     /***********************************/
     /* Constructors                    */
     /***********************************/
 
     /***********************************/
     /* External signature methods      */
     /***********************************/
 
     public static void main( String[] args ) {
         Init init = new Init();
         init.setCommandLine( options, args );
         init.run();
     }
 
     public void run() {
         try {
             if (getArgList().isEmpty()) {
                 usage();
             }
 
             if (noTDB() || hasOption( "f" )) {
                 // (re-)create the TDB image
                 FileUtils.deleteQuietly( getTDBFile() );
                 FileUtils.forceMkdir( getTDBFile() );
                 SetupTDB.setOptimizerWarningFlag( false );
                 Dataset dataset = TDBFactory.createDataset( getTdbLocation() );
 
                 // TODO: ideally, we should check that this makes a legal URI
                 String projectName = getArgs()[0];
                 Resource project = dataset.getDefaultModel().createResource( projectNamespace() + projectName );
 
                 project.addProperty( RDF.type, DOAP.Project );
 
                 // add descriptions from command line options
                 addOptionalProperty( project, DOAP.shortdesc, "s" );
                 addOptionalProperty( project, DOAP.description, "d" );
 
                System.out.println( String.format( "Created new DOAP description for %s with %d triples",
                                                    projectName, dataset.getDefaultModel().size() ) );
             }
             else {
                 System.out.println( "Logbook already exists, doing nothing" );
             }
         }
         catch (IOException e) {
             log.error( e.getMessage(), e );
         }
     }
 
     /***********************************/
     /* Internal implementation methods */
     /***********************************/
 
     /** No TDB is true if either the TDB directory doesn't exist, or it's empty */
     protected boolean noTDB() {
         File tdbDir = getTDBFile();
         return !tdbDir.exists() || tdbDir.list().length == 0;
     }
 
     /** Return the namespace for the project itself */
     protected String projectNamespace() {
         if (hasOption( "n" )) {
             return getOptionValue( "n" );
         }
         else {
             return DEFAULT_PROJECT_NAMESPACE;
         }
     }
 
     /** Add a property if the corresponding option is set */
     protected void addOptionalProperty( Resource project, Property p, String opt ) {
         if (hasOption( opt )) {
             project.addProperty( p, getOptionValue( opt ) );
         }
     }
 
     private void usage() {
         new HelpFormatter().printHelp( "plb init [options] project-name", options );
         System.exit( 1 );
     }
 
     /***********************************/
     /* Inner class definitions         */
     /***********************************/
 
 }
 
