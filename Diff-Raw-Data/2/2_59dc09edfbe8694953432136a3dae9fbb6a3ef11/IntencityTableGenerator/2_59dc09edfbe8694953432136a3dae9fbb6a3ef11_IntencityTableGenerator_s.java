 package ru.spbau.bioinf.tagfinder;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 public class IntencityTableGenerator {
 
     private static String file1 = "bar_exp_annotated_proper_none_intencity";
     private static String file2 = "bar_exp_annotated_correct_none_intencity";
 
     public static void main(String[] args) throws Exception {
 
         double[][] res = new double[100][3];
         printTable(res, file1, "Percentage of spectra in the $\\STbar$ data set, such that all their top-scoring tags of length $\\ell$ are proper (+) or improper (-).", "label16");
         TexTableGenerator.createThreeRowsTable(res, "Average percentage of proper top-scoring tags of a given length", "", "label10");
 
         tableTopscoreAllAndAvg();
     }
 
     public static void tableTopscoreAllAndAvg() throws Exception {
         double[][] res;
         res = new double[100][3];
         printTable(res, file2, "Percentage of spectra in the $\\STbar$ data set, such that all their top-scoring tags of length $\\ell$ are correct (+) or incorrect (-).", "all-top-scoring");
         TexTableGenerator.createThreeRowsTable(res, "Average percentage of correct top-scoring tags of a given length", "correct top-scoring tags", "avg-top-scoring");
     }
 
     private static void printTable(double[][] res, String file, String caption, String label) throws Exception {
 
 
         double[][] res2 = new double[6][];
 
         int max = 0;
 
         for (int gap = 1; gap <= 3; gap++) {
             BufferedReader in = ReaderUtil.createInputReader(new File("res", "share_" + file + "_" + gap + ".txt"));
             double[] good = new double[100];
             double[] bad = new double[100];
             double[] both = new double[100];
 
             List<Double>[] percentage = new List[100];
 
             do {
                 String s = in.readLine();
                 if (s.contains(",")) {
                     break;
                 }
 
                 if (s.indexOf(" ") == 0) {
                     break;
                 }
                 long[] data = CalculateRelation.getData(s);
                 int pos = 2;
                 int d = 1;
 
                 while (pos < data.length) {
                     max = Math.max(max, d);
                     if (data[pos] == 0) {
                         bad[d]++;
                     } else if (data[pos + 1] <= data[pos]) {
                         good[d]++;
                     } else {
                         both[d]++;
                     }
                     if (percentage[d] == null) {
                         percentage[d] = new ArrayList<Double>();
                     }
                     percentage[d].add(data[pos] * 100.0d / (data[pos] + data[pos + 1]));
 
                     d++;
                     pos += 2;
                 }
             } while (true);
             double[] ans = new double[max];
             for (int i = 1; i <= max; i++) {
                 double total = 0;
                 for (double v : percentage[i]) {
                     total += v;
                 }
                 ans[i - 1] = total / percentage[i].size();
             }
             res[gap - 1] = ans;
             res2[2 * gap -  2 ] = new double[max];
             res2[2 * gap - 1] = new double[max];
             for (int i = 1; i <= max; i++) {
                 double total = good[i] + bad[i] + both[i];
                 res2[2 * gap - 2][i - 1] = 100 * good[i] / total;
                 res2[2 * gap - 1][i - 1] = 100 * bad[i] / total;
             }
         }
 
         int width = 19;
         System.out.println("\\begin{landscape}\n");
         for (int start = 1; start <= max; start += width) {
             int end = Math.min(start + width - 1, max);
             System.out.print("\\begin{table}[ht]\\tiny\n" +
                     "\\vspace{3mm}\n" +
                     "{\\centering\n" +
                     "\\begin{center}\n" +
                     "\\begin{tabular}{|c|cc|c|");
             for (int i = start; i <= end; i++) {
                 System.out.print("c|");
             }
             int cols = end - start + 1;
             System.out.println("}\n" +
                     "  \\hline\n" +
                     "  \\multicolumn{3}{|c|}{ } & \\multicolumn{ " + cols + "}{|c|}{$k$} \\\\\n" +
                     "  \\cline{4-" + (cols + 3) + " }\n" +
                     "  \\multicolumn{3}{|c|}{ } ");
             for (int i = start; i <= end; i++) {
                 System.out.print(" & " + i);
             }
 
             for (int row = 0; row < 6; row++) {
                 System.out.print("\\\\\n");
                 if (row % 2 == 0) {
                     System.out.print("\\hline\n" +
                                 "  \\multirow{2}{*}{" + (row / 2  + 1) + "-aa}& \\multirow{2}{*}{spectra (\\%)} ");
                 } else {
                     System.out.println(" & ");
 
                 }
                 System.out.println(" &  " + (row % 2 == 0 ? "+" : "--"));
                 for (int i = start; i <= end; i++) {
                     System.out.print(" & ");
                     if (res2[row].length > i - 1) {
                         System.out.print(ValidTags.df.format(res2[row][i-1]));
                     }
                 }
             }
 
             System.out.println(" \\\\\n" +
                     "  \\hline\n" +
                     "\\end{tabular}\n" +
                     "\\end{center}\n" +
                     "\\par}\n" +
                     "\\centering\n");
 
             if (end == max) {
                 System.out.println("\\caption{" + caption + "}\n" +
                         "\\label{table:all-top-scoring}\n");
             }
             System.out.println("\\vspace{3mm}\n" +
                     "\\end{table}");
         }
         System.out.println("\\end{landscape}");
 
 
         System.out.println("\n\\begin{figure}\n" +
                 "  \\begin{center}");
         System.out.println("\\includegraphics{" + label + "}");
         System.out.println("\\end{center}\n" +
                 "\\caption{" + caption + "}\n" +
                 "  \\label{fig:" + label + "}\n" +
                 "\\end{figure}\n");
 
         PrintWriter dataFile = ReaderUtil.createOutputFile(new File("plots", label + ".dat"));
         PrintWriter gplFile = ReaderUtil.createOutputFile(new File("plots", label + ".gpl"));
        gplFile.print("set terminal postscript eps color\n" +
                 "set out \"" + label + ".eps\"\n" +
                 "set ylabel \"spectra (%)\"\n" +
                 "set xlabel \"tag length (l)\"\n" +
                 "plot");
         gplFile.println("\"plots/" + label + ".dat\" using 1:2 title 't=1 +' with linespoints,\\");
         gplFile.println("\"plots/" + label + ".dat\" using 1:3 title 't=1 -' with linespoints,\\");
         gplFile.println("\"plots/" + label + ".dat\" using 1:4 title 't=2 +' with linespoints,\\");
         gplFile.println("\"plots/" + label + ".dat\" using 1:5 title 't=2 -' with linespoints,\\");
         gplFile.println("\"plots/" + label + ".dat\" using 1:6 title 't=3 +' with linespoints,\\");
         gplFile.println("\"plots/" + label + ".dat\" using 1:7 title 't=3 -' with linespoints");
 
 
         gplFile.close();
 
         for (int i = 1; i <=14; i++) {
                 dataFile.print(i + " ");
                 for (int j = 0; j < 6; j++) {
                     dataFile.print(res2[j].length > i - 1 ? (res2[j][i-1]): " ");
                     dataFile.print(" ");
                 }
                 dataFile.println();
         }
         dataFile.close();
     }
 }
