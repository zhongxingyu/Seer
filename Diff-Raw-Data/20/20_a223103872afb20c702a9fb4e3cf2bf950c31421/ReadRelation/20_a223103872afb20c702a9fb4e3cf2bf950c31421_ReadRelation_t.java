 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package hashJoin;
 
 import hashJoin.basicConnector.Connector;
 import hashJoin.basicConnector.ReadEnd;
 import hashJoin.basicConnector.WriteEnd;
 import hashJoin.gammaSupport.Relation;
 import hashJoin.gammaSupport.Tuple;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import parallelsort.ReportError;
 
 /**
  *
  * @author bansal
  */
 public class ReadRelation extends Thread {
     
     BufferedReader in;
     WriteEnd out;
   
     ReadRelation(String fileName, Connector con){
       try {
             in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
         } catch (Exception e) {
             ReportError.msg(e.getMessage());
         }
         this.out = con.getWriteEnd();
       
   }
     
         public void run() {
         try {
             String input;
            int linesRead=0;            
             while (true) {
                 input = in.readLine();
                linesRead++;
                if(linesRead <= 2) continue;
                 if (input == null) {
                     break;
                 }
                String[] input_arr=input.split("\\s+");
                 Tuple t = new Tuple(input_arr.length);
                 for (int i=0; i<input_arr.length;i++){
                     t.set(i, input_arr[i]);
                 }
                 System.out.println(input);
                 out.putNextTuple(t);
             }
             out.close();
         } catch (Exception e) {
             ReportError.msg(this.getClass().getName() + e);
         }
     }
 
     
 }
