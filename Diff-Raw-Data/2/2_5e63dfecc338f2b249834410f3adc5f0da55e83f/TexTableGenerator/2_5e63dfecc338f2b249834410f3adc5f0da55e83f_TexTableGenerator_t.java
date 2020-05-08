 package ru.spbau.bioinf.tagfinder;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 public class TexTableGenerator {
 
     public static int tableId = 0;
 
     public static void main(String[] args) throws Exception {
         createTexTable("bar_exp_annotated_correct_none", "bar_virt_annotated_correct_zero", "Average percentage of correct $d$-tags.", "correct-d-tags", "correct $d$-tags");
         createTexTable("bar_exp_annotated_proper_none", "bar_virt_annotated_proper_zero", "Average percentage of proper $d$-tags.", "proper-d-tags", "proper $d$-tags");
         createTexTable("bar_exp_annotated_correct_none_add", "Average percentage of correct $d$-tags.", "correct-d-tags", "correct $d$-tags");
 
         createTexTable("bar_exp_annotated_correct_more", "Average percentage of correct $d$-tags.", "correct-d-tags", "correct $d$-tags");
 
         createTexTable("bar_virt_annotated_correct_none", "Average percentage of correct $d$-mono-tags w.r.t. all $d$-mono-tags.","correct-d-mono-tags", "correct $d$-mono-tags");
 
         ///createTexTable("bar_virt_annotated_correct_none", "Average percentage of correct $d$-tags.", "correct-d-tags");
     }
 
     private static void createTexTable(String fileFirst, String fileSecond, String caption, String label, String header) throws Exception {
         System.out.println("% 6-rows table for " + fileFirst + " and " + fileSecond);
         double[][] data = new double[6][];
         for (int gap = 1; gap <= 3; gap++) {
             data[gap - 1] = ReaderUtil.getLastString(new File("res", "share_" + fileFirst + "_" + gap + ".txt"));
             data[gap + 2] = ReaderUtil.getLastString(new File("res", "share_" + fileSecond + "_" + gap + ".txt"));
         }
 
         createSixRowsTable(data, caption, label, header);
     }
 
     public static void createSixRowsTable(double[][] data, String caption, String label, String header) throws Exception {
         int maxLen = 0;
         for (int i = 0; i < data.length; i++) {
             maxLen = Math.max(maxLen, data[i].length);
         }
 
         prepareEps(data, caption, 6);
 
         int width = 20;
         if (maxLen > width) {
             width = (maxLen + 1)/2;
         }
         StringBuilder table = new StringBuilder();
 
         table.append("\\includegraphics{plots/" + tableId + ".eps}");
 
         for (int start = 1; start <= maxLen; start += width) {
             int end = Math.min(start + width - 1, maxLen);
 
             table.append("\\begin{table}[h]\\tiny\n" +
                     "\\vspace{3mm}\n" +
                     "{\\centering\n" +
                     "\\begin{center}\n" +
                     "\\begin{tabular}{|c|l|");
             for (int i = start; i <= end; i++) {
                 table.append("c|");
             }
             table.append("}\n" +
                     "  \\hline\n" +
                     "  \\multicolumn{2}{|c|}{ } & \\multicolumn{ " + (end - start + 1) + " }{|c|}{ " + header + " (\\%)} \\\\\n" +
                     "  \\cline{3- " + (end - start + 3) + "}\n" +
                     "  \\multicolumn{2}{|c|}{ } ");
             //"& 1 & 2 & 3 & 4 & 5 & 6 & 7 & 8 & 9 & 10 & 11 & 12 & 13 & 14 & 15 & 16 & 17 & 18 & 19 & 20 & 21 & 22 & 23 & 24
             for (int i = start; i <= end; i++) {
                 table.append(" & " + i);
             }
             table.append("\\\\\n" +
                     "  \\hline\n" +
                     "  \\multirow{3}{*}{exp}\n");
 
 
             for (int row = 0; row < 3; row++) {
                 printRows(table, data, row, start, end, true);
             }
 
             table.append(" \\hline\n  \\multirow{3}{*}{virt} \n");
 
             for (int row = 3; row < 6; row++) {
                 printRows(table, data, row, start, end, true);
             }
 
             table.append(" \\hline\n" +
                     "\\end{tabular}\n" +
                     "\\end{center}\n" +
                     "\\par}\n" +
                     "\\centering\n");
             if (end == maxLen) {
                 table.append("\\caption{ " + caption + "}\n");
             }
 
             table.append("\\vspace{3mm}\n" +
                     "\\label{table:" + label + "}\n" +
                     "\\end{table}\n\n");
         }
 
         System.out.println(table.toString());
     }
 
     private static void prepareEps(double[][] data, String caption, int rows) throws IOException {
         tableId++;
 
         PrintWriter dataFile = ReaderUtil.createOutputFile(new File("plots", tableId + ".dat"));
         for (int i = 0; i < 14; i++) {
             dataFile.print((i + 1) + " ");
             for (int col = 0; col < rows; col++) {
                 dataFile.print(data[col][i] + " ");
             }
             dataFile.println();
         }
         dataFile.close();
 
         PrintWriter gplFile = ReaderUtil.createOutputFile(new File("plots", tableId + ".gpl"));
         gplFile.print("set terminal postscript eps\n" +
                 "set out \"plots/" + tableId + ".eps\"\n" +
                 "set ylabel \"" + caption.replaceAll("\\$", "") + "\"\n" +
                 "set xlabel \"Tag length\"\n" +
                 "plot");
         for (int i = 1; i <= rows; i++) {
             int gap = i;
             if (gap > 3) {
                 gap = gap - 3;
             }
            gplFile.print("\"plots/" + tableId + ".dat\" using 1:" + (i + 1) + " title '" + (i > 3 ? "virt" : " exp") + " " + gap + "-aa' with linespoints");
             if ( i < rows) {
                 gplFile.println(", \\");
             }
         }
 
         gplFile.close();
     }
 
 
     private static void createTexTable(String fileFirst, String caption, String label, String header) throws Exception {
         System.out.println("% 3-rows table for " + fileFirst);
         double[][] data = new double[3][];
         for (int gap = 1; gap <= 3; gap++) {
             data[gap - 1] = ReaderUtil.getLastString(new File("res", "share_" + fileFirst + "_" + gap + ".txt"));
         }
 
         createThreeRowsTable(data, caption, label, header);
     }
 
     public static void createThreeRowsTable(double[][] data, String caption, String label, String header) throws Exception {
         int maxLen = 0;
         for (int i = 0; i < data.length; i++) {
             maxLen = Math.max(maxLen, data[i].length);
         }
 
 
         prepareEps(data, caption, 3);
         int width = 20;
         if (maxLen > width) {
             width = (maxLen + 1)/2;
             if (width > 20) {
                 width = (maxLen + 1)/3;
                 if (width  < 18) {
                     width = 18;
                 }
             }
         }
         System.out.println("\\includegraphics{plots/" + tableId+ ".eps}\n");
 
         for (int start = 1; start <= maxLen; start += width) {
             int end = Math.min(start + width - 1, maxLen);
 
             System.out.print("\\begin{table}[h]\\tiny\n" +
                     "\\vspace{3mm}\n" +
                     "{\\centering\n" +
                     "\\begin{center}\n" +
                     "\\begin{tabular}{|l|c|");
             for (int i = start; i <= end; i++) {
                 System.out.print("c|");
             }
             System.out.print("}\n" +
                     "  \\hline\n" +
                     "  & \\multicolumn{ " + (end - start + 1) + " }{|c|}{" + header + "(\\%)} \\\\\n" +
                     "  \\cline{2- " + (end - start + 2) + "}\n" +
                     "   ");
             //"& 1 & 2 & 3 & 4 & 5 & 6 & 7 & 8 & 9 & 10 & 11 & 12 & 13 & 14 & 15 & 16 & 17 & 18 & 19 & 20 & 21 & 22 & 23 & 24
             for (int i = start; i <= end; i++) {
                 System.out.print(" & " + i);
             }
             System.out.print("\\\\\n" +
                     "  \\hline\n");
 
 
             for (int row = 0; row < 3; row++) {
                 printRows(data, row, start, end, false);
             }
 
             System.out.println(" \\hline\n" +
                     "\\end{tabular}\n" +
                     "\\end{center}\n" +
                     "\\par}\n" +
                     "\\centering\n");
             if (end == maxLen) {
                 System.out.println("\\caption{ " + caption + "}\n");
             }
 
             System.out.println("\\vspace{3mm}\n" +
 
                     "\\label{table:" + label + "}\n" +
                     "\\end{table}");
         }
     }
 
     private static void printRows(double[][] data, int row, int start, int end, boolean needAmp) {
         if (needAmp) {
             System.out.print("&  ");
         }
         System.out.print((row % 3  +1) +  "-aa ");
         for (int i = start; i <=end; i++) {
             System.out.print(" & ");
             if (data[row].length >= i) {
                 System.out.print(ValidTags.df.format(data[row][i - 1]));
             }
         }
         System.out.println("\\\\");
     }
 
     private static void printRows(StringBuilder sb, double[][] data, int row, int start, int end, boolean needAmp) {
         if (needAmp) {
             sb.append("&  ");
         }
         sb.append((row % 3 + 1) + "-aa ");
         for (int i = start; i <=end; i++) {
             sb.append(" & ");
             if (data[row].length >= i) {
                 sb.append(ValidTags.df.format(data[row][i - 1]));
             }
         }
         sb.append("\\\\");
     }
 
 }
