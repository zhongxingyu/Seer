 /*
  * Copyright (C) 2012 brweber2
  */
 package com.brweber2.parser;
 
 import com.creativewidgetworks.goldparser.parser.GOLDParser;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.net.URISyntaxException;
 import java.util.List;
 import java.util.logging.Logger;
 
 public class CompileGrammar
 {
     private static final Logger log = Logger.getLogger(CompileGrammar.class.getName());
     
     public static void main( String[] args ) throws IOException
     {
         File grammarFile = new File("/Users/bweber/brweber2/naive-java-unification/src/main/resources/naive_java_unification.egt");
         File sourceFile = new File( "/Users/bweber/brweber2/naive-java-unification/src/main/resources/grammar/delete_me.txt" );
 
         log.finer("" + grammarFile.exists());
         log.finer("" + sourceFile.exists());
 
         Reader sourceReader = new FileReader( sourceFile );
         GOLDParser parser = new GOLDParser();
         if ( !parser.setup( grammarFile ) )
         {
             throw new RuntimeException( "Unable to parse the grammar file " + grammarFile.getAbsolutePath() );
         }
         parser.loadRuleHandlers( "com.brweber2.parser.rulehandler" );
         parser.setGenerateTree( true );
         log.warning("errors:" + parser.validateHandlersExist());
         if ( !parser.parseSourceStatements( sourceReader ) )
         {
             throw new RuntimeException( "Unable to parse the source file " + sourceFile.getAbsolutePath() );
         }
         parser.getCurrentReduction().execute();
         log.info(parser.getParseTree());
     }
 
     public GOLDParser parser()
     {
             InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("naive_java_unification.egt");
             if ( inputStream == null )
             {
                 throw new RuntimeException( "No such egt file." );
             }
             return parser( inputStream );
     }
     
     public GOLDParser parser( InputStream grammarFile )
     {
         GOLDParser parser = new GOLDParser(grammarFile,"com.brweber2.parser.rulehandler",false);
         List<String> errors = parser.validateHandlersExist();
         if ( !errors.isEmpty() )
         {
//            throw new RuntimeException( "Missing handlers!" + errors );
            throw new RuntimeException( "Missing handlers!"  );
         }
         return parser;
     }
 }
