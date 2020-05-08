 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package hashJoin;
 
 import hashJoin.basicConnector.Connector;
 
 /**
  *
  * @author bansal
  */
 public class MainTest {
     public static void main(String[] args) {
        readRelation_PrintTupleTest();
     }
 
    public static void readRelation_PrintTupleTest() {
         // read --> sort --> print
         System.out.println("Starting ReadRelation _ PrintTuple");
         Connector read_A = new Connector("input1");
         ReadRelation r = new ReadRelation("client.txt", read_A);
         PrintTuple p = new PrintTuple(read_A);
         r.start();
         p.start();
     }
     
 }
