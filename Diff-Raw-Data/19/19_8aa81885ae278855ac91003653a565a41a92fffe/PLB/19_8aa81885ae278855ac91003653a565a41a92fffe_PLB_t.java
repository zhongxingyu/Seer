 /*****************************************************************************
  * File:    PLB.java
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
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.cli.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.epimorphics.plb.vocabs.DOAP;
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.tdb.TDBFactory;
 import com.hp.hpl.jena.tdb.sys.SetupTDB;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 /**
  * <p>Shared implementation code for PLB commands</p>
  *
  * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
  */
 public abstract class PLB
     implements Runnable
 {
     /***********************************/
     /* Constants                       */
     /***********************************/
 
     /** Default location for project log book */
     public static final String DEFAULT_LOGBOOK_LOCATION = ".plb/tdb";
 
     /** Default namespace for projects */
     public static final String DEFAULT_PROJECT_NAMESPACE = "http://www.epimorphics.com/tutorial/plb#";
 
     /***********************************/
     /* Static variables                */
     /***********************************/
 
     private static final Logger log = LoggerFactory.getLogger( PLB.class );
 
     protected static Options common_options;
     static {
         common_options = new Options();
        common_options.addOption( "n", "namespace", true, "The HTTP namespace for the project" );
     }
 
     /***********************************/
     /* Instance variables              */
     /***********************************/
 
     /** Parsed command line */
     private CommandLine commandLine;
 
     /** Location of the TDB data store */
     private String tdbLocation = DEFAULT_LOGBOOK_LOCATION;
 
     /***********************************/
     /* Constructors                    */
     /***********************************/
 
     public PLB() {
         SetupTDB.setOptimizerWarningFlag( false );
     }
 
     /***********************************/
     /* External signature methods      */
     /***********************************/
 
     /** Extend the given options by adding the common options from this class */
     protected static void addCommonOptions( Options options ) {
         for (Iterator<?> i = common_options.getOptions().iterator(); i.hasNext(); ) {
             options.addOption( (Option) i.next() );
         }
     }
 
     public void setCommandLine( CommandLine commandLine ) {
         this.commandLine = commandLine;
     }
 
     /** Set the command line from the command's option list and command line args */
     public void setCommandLine( Options options, String[] args ) {
         try {
             setCommandLine( new PosixParser().parse( options, args ) );
         }
         catch (ParseException e) {
             log.error( e.getMessage(), e );
         }
     }
 
     public CommandLine getCommandLine() {
         return commandLine;
     }
 
     public void setTdbLocation( String tdbLocation ) {
         this.tdbLocation = tdbLocation;
     }
 
     public String getTdbLocation() {
         return tdbLocation;
     }
 
     public List<?> getArgList() {
         return commandLine.getArgList();
     }
 
     public boolean hasOption( String opt ) {
         return getCommandLine().hasOption( opt );
     }
 
     public File getTDBFile() {
         return new File( getTdbLocation() );
     }
 
     public String[] getArgs() {
         return getCommandLine().getArgs();
     }
 
     public String getOptionValue( String opt ) {
         return getCommandLine().getOptionValue( opt );
     }
 
     /** Return the model for the default TDB graph */
     public Model getTDBModel() {
         return TDBFactory.createDataset( getTdbLocation() ).getDefaultModel();
     }
 
     /** Get the DOAP project resource */
     public Resource getProjectResource() {
         ResIterator i = getTDBModel().listSubjectsWithProperty( RDF.type, DOAP.Project );
         if (i.hasNext()) {
             return i.next();
         }
         else {
             System.err.println( "No doap:Project resource in this log book: has the project been initialized?" );
             throw new RuntimeException( "Missing project root resource" );
         }
     }
 
 
     /***********************************/
     /* Internal implementation methods */
     /***********************************/
 
     /***********************************/
     /* Inner class definitions         */
     /***********************************/
 
 }
 
