 package ch22.ex22_07;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.regex.*;
 
 class ReadCSV {
   public static List<String[]> readCSVTable(Readable source, int cellNum)
     throws IOException {
     Scanner in = new Scanner(source);
     List<String[]> vals = new ArrayList<String[]>();
     String exp = createCSVExp(cellNum);
     Pattern pat = Pattern.compile(exp, Pattern.MULTILINE);
    while (in.hasNext()) {
       String line = in.findInLine(pat);
       if (line != null) {
         String[] cells = new String[cellNum];
         MatchResult match = in.match();
         for (int i = 0; i < cells.length; ++i)
           cells[i] = match.group(i+1);
         vals.add(cells);
         in.nextLine(); // 改行を読み飛ばし
       } else {
         throw new IOException("input format error");
       }
     }
 
     IOException ex = in.ioException();
     if (ex != null)
       throw ex;
 
     return vals;
   }
 
   private static String createCSVExp(int cellNum) {
     StringBuilder exp = new StringBuilder("^");
     for (int i = 0; i < cellNum; ++i)
       exp.append("(.*),");
     exp.deleteCharAt(exp.length() - 1);
     return exp.toString();
   }
 
   public static void main(String[] args)
       throws IOException {
     if (args.length == 0) {
       System.out.println("needs csv files");
       System.exit(1);
     }
 
     for (String fpath : args) {
       FileReader in = null;
       List<String[]> vals = null;
       try {
         in = new FileReader(fpath);
         vals = readCSVTable(in, 5);
       } catch (IOException e) {
         e.printStackTrace();
       } finally {
         if (in != null)
           in.close();
       }
 
       for (String[] strs : vals) {
         for (int i = 0; i < strs.length; ++i)
           System.out.print(strs[i] + " ");
         System.out.println();
       }
     }
   }
 }
