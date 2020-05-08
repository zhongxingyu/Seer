 package com.aha.aheui.interpreter;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import com.aha.aheui.ast.Instruction;
 import com.aha.aheui.ast.Program;
 import com.aha.aheui.parser.AheuiParser;
 import com.aha.util.Matrix;
 import com.aha.util.Tuple;
 import com.aha.util.streams.StreamHelper;
 
 public class Main {
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         if (args.length != 1) {
             printUsage();
            System.exit(-1);
         }
         
         String source = null;
         try {
             FileInputStream fIn = new FileInputStream(args[0]);
             source = StreamHelper.readAll(fIn, "UTF-8");
             fIn.close();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             System.exit(-1);
         }
         
         AheuiParser parser = new AheuiParser();
         Tuple<Matrix<Instruction>, Program> result = parser.parse(source);
         
         Program program = result.getSecond();
         AheuiInterpreter interpreter = new AheuiInterpreter();
         interpreter.run(program);
     }
 
     private static void printUsage() {
         // TODO Auto-generated method stub
         
     }
 
 }
