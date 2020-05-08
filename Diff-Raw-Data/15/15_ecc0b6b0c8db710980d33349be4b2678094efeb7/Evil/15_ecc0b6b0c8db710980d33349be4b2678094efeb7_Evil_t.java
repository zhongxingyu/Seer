 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import org.antlr.stringtemplate.*;
 
 import java.io.*;
 import java.util.Vector;
 import java.util.HashMap;
 
 public class Evil
 {
    public static void main(String[] args)
    {
       parseParameters(args);
 
       CommonTokenStream tokens = new CommonTokenStream(createLexer());
       EvilParser parser = new EvilParser(tokens);
       EvilParser.program_return ret = null;
       
       try
       {
          ret = parser.program();
       }
       catch (org.antlr.runtime.RecognitionException e)
       {
          error(e.toString());
       }
 
       CommonTree t = (CommonTree)ret.getTree();
       if (_displayAST && t != null)
       {
          DOTTreeGenerator gen = new DOTTreeGenerator();
          StringTemplate st = gen.toDOT(t);
          System.out.println(st);
       }
 
       /*
          To create and invoke a tree parser.  Modify with the appropriate
          name of the tree parser and the appropriate start rule.
       */
       try
       {
          CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
          nodes.setTokenStream(tokens);
          TypeCheck tparser = new TypeCheck(nodes);
 
          tparser.verify(new HashMap<String, FuncType>(), new HashMap<String, StructType>(), new HashMap<String, Type>());
          
	 nodes = new CommonTreeNodeStream(t);
         nodes.setTokenStream(tokens);

          ILOC iloc = new ILOC(nodes);
          
          iloc.generate();
       }
       catch (org.antlr.runtime.RecognitionException e)
       {
          error(e.toString());
       }
    }
 
    private static final String DISPLAYAST = "-displayAST";
 
    private static String _inputFile = null;
    private static boolean _displayAST = false;
 
    private static void parseParameters(String [] args)
    {
       for (int i = 0; i < args.length; i++)
       {
          if (args[i].equals(DISPLAYAST))
          {
             _displayAST = true;
          }
          else if (args[i].charAt(0) == '-')
          {
             System.err.println("unexpected option: " + args[i]);
             System.exit(1);
          }
          else if (_inputFile != null)
         {
             System.err.println("too many files specified");
             System.exit(1);
          }
          else
          {
             _inputFile = args[i];
          }
       }
    }
 
 
    private static void error(String msg)
    {
       System.err.println(msg);
       System.exit(1);
    }
 
    private static EvilLexer createLexer()
    {
       try
       {
          ANTLRInputStream input;
          if (_inputFile == null)
          {
             input = new ANTLRInputStream(System.in);
          }
          else
          {
             input = new ANTLRInputStream(
                new BufferedInputStream(new FileInputStream(_inputFile)));
          }
          return new EvilLexer(input);
       }
       catch (java.io.IOException e)
       {
          System.err.println("file not found: " + _inputFile);
          System.exit(1);
          return null;
       }
    }
 }
