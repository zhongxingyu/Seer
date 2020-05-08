 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import org.antlr.stringtemplate.*;
 
 import java.io.*;
 import java.util.*;
 
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
       ArrayList<FuncBlock> blist = new ArrayList<FuncBlock>();
       HashMap<String, Type> vartable = null;
       HashMap<String, StructType> structtable = null;
       try
       {
          CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
          nodes.setTokenStream(tokens);
          TypeCheck tparser = new TypeCheck(nodes);
          structtable = new HashMap<String, StructType>();
          vartable = new HashMap<String, Type>();
          tparser.verify(new HashMap<String, FuncType>(), structtable, vartable);
          
          nodes = new CommonTreeNodeStream(t);
          nodes.setTokenStream(tokens);
 
          ILOC iloc = new ILOC(nodes);
          iloc.generate(blist,structtable,vartable);
       }
       catch (org.antlr.runtime.RecognitionException e)
       {
          error(e.toString());
       }
          
         
          String ilocstr = "";
          
         if(!_dumpSPARC)
         {
           for(FuncBlock b : blist){
              if(b.name.equals("main")){
                ilocstr += "@function " + b.name + "\n";
              }
            }
            for(FuncBlock b : blist){
              if(!b.name.equals("main")){
                ilocstr += "@function " + b.name + "\n";
              }
            }
            for(FuncBlock b : blist){
              ilocstr += b.getHeader(false) + "\n";
            }
              
            for(FuncBlock b : blist){
              if(b.name.equals("main")){
                ilocstr += b.getInstructions(false) + "\n";
              }
            }
            for(FuncBlock b : blist){
              if(!b.name.equals("main")){
                ilocstr += b.getInstructions(false) + "\n";
              }
            }
            if(!_dumpIL){
              System.out.println(ilocstr);
            }
            else{
              try{
                FileOutputStream f = new FileOutputStream(_inputFile.replace(".ev", "") + ".il");
                f.write(ilocstr.getBytes());
                f.close();
              }
              catch(IOException e){
                e.printStackTrace();
              }
            }
       }
       else 
       {
         RegisterAllocator ra = new RegisterAllocator(blist);
         ra.color();
         System.out.println(".section\t\".text\"");
         System.out.println(".align 4");
         for(String s : vartable.keySet())
         {
           System.out.println(".common\t" + s + ", 4, 4");
         }
         for(FuncBlock b : blist)
         {
           System.out.println(".global " + b.name);
           System.out.println(".type\t" + b.name + ", #function");
         }
         for(FuncBlock b : blist)
         {
           if(b.name.equals("main"))
           {
             System.out.println(b.getHeader(true));
             System.out.println(b.getInstructions(true));
           }
         }
         for(FuncBlock b : blist)
         {
           if(!b.name.equals("main"))
           {
             System.out.println(b.getHeader(true));
             System.out.println(b.getInstructions(true));
           }
         }
         System.out.println();
         System.out.println(".section\t\".rodata\"");
         System.out.println(".align 8");
         System.out.println(".LLC0:");
         System.out.println(".asciz\t\"%d \"");
         System.out.println(".align 8");
         System.out.println(".LLC1:");
         System.out.println(".asciz\t\"%d\\n\"");
         System.out.println(".align 8");
         System.out.println(".LLC2:");
         System.out.println(".asciz\t\"%d\"");
         System.out.println();
         System.out.println(".common\t_read, 4, 4");
       }
       //RegisterAllocator ra = new RegisterAllocator(blist);
       //ra.color();
    }
 
    private static final String DISPLAYAST = "-displayAST";
    private static final String DUMPIL = "-dumpIL";
    private static final String DUMPSPARC = "-dumpSPARC";
 
    private static String _inputFile = null;
    private static boolean _displayAST = false;
    private static boolean _dumpIL = false;
    private static boolean _dumpSPARC = false;
 
    private static void parseParameters(String [] args)
    {
       for (int i = 0; i < args.length; i++)
       {
          if (args[i].equals(DISPLAYAST))
          {
             _displayAST = true;
          }
          else if(args[i].equals(DUMPIL))
          {
             _dumpIL = true;
          }
          else if(args[i].equals(DUMPSPARC))
          {
             _dumpSPARC = true;
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
