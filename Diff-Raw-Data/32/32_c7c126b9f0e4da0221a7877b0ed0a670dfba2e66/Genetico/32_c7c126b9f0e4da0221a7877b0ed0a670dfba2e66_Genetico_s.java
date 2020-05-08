 package genetico;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 
 public class Genetico {
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
 //        try {
             Parser p = new Parser(args[0]);
             ArrayList<int[]> trainingData = p.go();
 
            GABIL g = new GABIL(trainingData, 500, 3.0/5.0, 0.005, 0.6);
 //            Random gen = new Random();
 //            for(int i = 0; i < 5000; i++) {
 //                System.out.println( (int)  (1 + gen.nextDouble()*(3)));
 //            }
 //            ArrayList<int[]> ipop = g.initPop();
 //            Iterator<int[]> t = ipop.iterator();
 //            while (t.hasNext()) {
 //                int[] k = t.next();
 //                int[] k1 = new int[Parser.REP_SIZE];
 //                int[] k2 = new int[Parser.REP_SIZE];
 //                System.arraycopy(k, 0, k1, 0, Parser.REP_SIZE);
 ////                for(int i = Parser.REP_SIZE; i < Parser.REP_SIZE*2; i++) 
 ////                    k2[i % Parser.REP_SIZE] = k[i];
 //                System.arraycopy(k, Parser.REP_SIZE, k2, 0, Parser.REP_SIZE);
 //                p.test(k1);
 //                System.out.println("Segunda regla");
 //                System.out.println("===================================");
 //                p.test(k2);
 //                System.out.println("-----------------------------");
 //            }
            int[] bestHyp = g.go();
            System.out.println("Best hypothesis");
            System.out.println(bestHyp);
 //        }
 //        catch(ArrayIndexOutOfBoundsException e) {
 //            System.out.println("Especificar archivo de entrada."+e);
 //        }
         
     }
 }
