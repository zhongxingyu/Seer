 /*
  * Copyright (C) 2012 brweber2
  */
 package com.brweber2.repl;
 
 import com.brweber2.kb.KnowledgeBase;
 import com.brweber2.kb.ProofSearch;
 import com.brweber2.parser.CompileGrammar;
 import com.brweber2.term.Atom;
 import com.brweber2.term.ComplexTerm;
 import com.brweber2.term.Term;
 import com.brweber2.term.rule.Rule;
 import com.brweber2.unification.Unification;
 import com.brweber2.unification.Unify;
 import com.creativewidgetworks.goldparser.parser.GOLDParser;
 import com.sun.xml.internal.bind.v2.model.runtime.RuntimeElementPropertyInfo;
 import jline.ConsoleReader;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Repl
 {
     private static Logger log = Logger.getLogger(Repl.class.getName());
     
     private static final KnowledgeBase kb = new KnowledgeBase();
     private static final Unify unifier = new Unification();
     private static final ProofSearch proofSearch = new ProofSearch( unifier, kb );
     private static final CompileGrammar compiler = new CompileGrammar();
     private static MODE mode = MODE.ASK;
     
     private enum MODE { ADD, ASK }
     
     public static void main( String[] args ) throws IOException
     {
         // todo load any knowledge base files passed in with -f flag
         ConsoleReader in = new ConsoleReader();
         boolean done = false;
         while ( !done )
         {
             try
             {
                 String read = read(in);
                 if ( done(read) )
                 {
                     done = true;
                     continue;
                 }
                 Object result = eval(read);
                 print(result);
             }
             catch ( Exception e )
             {
                 System.err.println( "Error: " + e.getMessage() );
                 System.err.flush();
                System.out.flush();
             }
         }
     }
     
     private static Object parseString( String read )
     {
         GOLDParser parser = compiler.parser();
         if ( !parser.parseSourceStatements( new StringReader( read ) ) )
         {
             throw new RuntimeException( "Unable to parse: [" + read + "]." );
         }
         parser.getCurrentReduction().execute();
         return parser.getCurrentReduction().getValue().asObject();
     }
 
     private static boolean load( Term question )
     {
         if ( question instanceof ComplexTerm )
         {
             ComplexTerm ct = (ComplexTerm) question;
             if ( ct.getFunctor().equals( "load" ) && ct.getArity() == 1 && ct.getTerms().get( 0 ) instanceof Atom )
             {
                 return true;
             }
         }
         return false;
     }
     
     private static List loadFile( Term question )
     {
         try
         {
             ComplexTerm ct = (ComplexTerm) question;
             Atom fileName = (Atom) ct.getTerms().get( 0 );
             String file = fileName.getRawString();
             File f = new File( file );
             if ( !f.exists() )
             {
                 throw new RuntimeException( "No such file: " + file );
             }
             GOLDParser parser = compiler.parser();
             FileReader reader = new FileReader( f );
             if ( !parser.parseSourceStatements( reader ) )
             {
                 throw new RuntimeException( "Unable to parse file " + file );
             }
             // todo figure out how to parse the file and get the terms and rules...
             return null;
         }
         catch ( FileNotFoundException e )
         {
             throw new RuntimeException( "Unable to parse file " + question, e );
         }
     }
     
     private static boolean switchMode( Term question )
     {
         if ( question instanceof ComplexTerm )
         {
             ComplexTerm ct = (ComplexTerm) question;
             return ct.getFunctor().equals( "mode" ) && ct.getArity() == 0;
         }
         return false;
     }
     
     private static Object eval( String read )
     {
         // parse
         Object s = parseString( read );
         if ( s instanceof Term )
         {
             Term question = (Term) s;
             log.fine("we have term " + question);
             // eval
             // todo add concept of repl functions and built in functions...
             if ( switchMode( question ) )
             {
                 switch ( mode )
                 {
                     case ADD:
                         mode = MODE.ASK;
                         break;
                     case ASK:
                         mode = MODE.ADD;
                         break;
                 }
             }
             else if ( load(question) )
             {
                 log.fine("time to load a file...");
                 List toAdds = loadFile(question);
                 for ( Object o : toAdds )
                 {
                     if ( o instanceof Term )
                     {
                         kb.fact( (Term) o );
                     }
                     else if ( o instanceof Rule )
                     {
                         kb.rule( (Rule) o );
                     }
                     else
                     {
                         throw new RuntimeException( "Unsupported item in file [" + o + "]." );
                     }
                 }
                 return "loaded " + question;
             }
             else
             {
                 switch ( mode )
                 {
                     case ADD:
                         kb.fact( question );
                         log.info("added fact " + question);
                         break;
                     case ASK:
                         return proofSearch.ask( question );
                 }
             }
         }
         else if ( s instanceof Rule )
         {
             log.fine("we have rule " + s);
             switch ( mode )
             {
                 case ADD:
                     kb.rule( (Rule) s );
                     log.info("added rule: " + s);
                     break;
                 case ASK:
                     return "You cannot specify rules in ASK mode.";
             }
         }
         else
         {
             throw new RuntimeException( "meh" );
         }
 
         // fall through cases...
         return "";
     }
     
     private static String getPrompt()
     {
         switch ( mode )
         {
             case ADD:
                 return "!- ";
             case ASK:
                 return "?- ";
         }
         throw new RuntimeException( "Invalid mode " + mode );
     }
 
     private static String read(ConsoleReader in) throws IOException
     {
         return in.readLine(getPrompt());
     }
 
     private static void print( Object result )
     {
         System.out.println(result);
     }
 
     private static boolean done( String read )
     {
         return "exit".equalsIgnoreCase( read ) || "quit".equalsIgnoreCase( read ) || "halt".equalsIgnoreCase( read );
     }
 
 }
