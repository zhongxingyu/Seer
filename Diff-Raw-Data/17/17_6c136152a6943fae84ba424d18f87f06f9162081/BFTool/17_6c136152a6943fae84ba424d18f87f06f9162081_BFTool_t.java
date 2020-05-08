 package mx.itam.metodos.bf;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.util.BitSet;
 
 import com.google.common.io.LineReader;
 
 public class BFTool {
   
   public static BitSet load(String path) throws Exception {
     InputStream in = new FileInputStream(path);
     try {
       ObjectInputStream is = new ObjectInputStream(in);
       return (BitSet) is.readObject();
     } finally {
       in.close();
     }
   }
   
   public static void main(String[] args) throws Exception {
     BitSet bs = load(args[0]);
     int n = Integer.parseInt(args[1]);
     float p = Float.parseFloat(args[2]);
     int mqc = Integer.parseInt(args[3]);
     HashingHelper functions = new HashingHelper(n, p);
     LineReader lr = new LineReader(new InputStreamReader(System.in));
     String line = null;
     while ((line = lr.readLine()) != null) {
       int count = test(bs, mqc, functions, line);
       int e = getE(count);
       System.out.printf("%s: %s\n", line, e);
     }
   }
 
   private static int test(BitSet bs, int mqc, HashingHelper functions, String line) {
     for (int j = 0; j < mqc; j++) {
       String rep = String.format("%s_%s", line, j);
       int[] pos = functions.getHashes(rep);
       for (int p : pos) {
         if (!bs.get(p)) {
          return j;
         }
       }
     }
     return mqc;
   }
   
   private static int getE(int j) {
    return (int) ((Math.pow(2, j - 1) + Math.pow(2, j)  - 1)  / 2);
   }
 }
