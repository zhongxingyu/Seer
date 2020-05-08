 package ru.spbau.bioinf.tagfinder;
 
 import java.io.File;
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 public class TexTableGenerator {
 
 
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
 
     public static void createSixRowsTable(double[][] data, String caption, String label, String header) {
         int maxLen = 0;
         for (int i = 0; i < data.length; i++) {
             maxLen = Math.max(maxLen, data[i].length);
         }
 
         int width = 20;
         if (maxLen > width) {
             width = (maxLen + 1)/2;
         }
         for (int start = 1; start <= maxLen; start += width) {
             int end = Math.min(start + width - 1, maxLen);
 
             System.out.print("\\begin{table}[h]\\tiny\n" +
                     "\\vspace{3mm}\n" +
                     "{\\centering\n" +
                     "\\begin{center}\n" +
                     "\\begin{tabular}{|c|l|");
             for (int i = start; i <= end; i++) {
                 System.out.print("c|");
             }
             System.out.print("}\n" +
                     "  \\hline\n" +
                     "  \\multicolumn{2}{|c|}{ } & \\multicolumn{ " + (end - start + 1) + " }{|c|}{ " + header + " (\\%)} \\\\\n" +
                     "  \\cline{3- " + (end - start + 3) + "}\n" +
                     "  \\multicolumn{2}{|c|}{ } ");
             //"& 1 & 2 & 3 & 4 & 5 & 6 & 7 & 8 & 9 & 10 & 11 & 12 & 13 & 14 & 15 & 16 & 17 & 18 & 19 & 20 & 21 & 22 & 23 & 24
             for (int i = start; i <= end; i++) {
                 System.out.print(" & " + i);
             }
             System.out.print("\\\\\n" +
                     "  \\hline\n" +
                     "  \\multirow{3}{*}{exp}\n");
 
 
             for (int row = 0; row < 3; row++) {
                 printRows(data, row, start, end, true);
             }
 
             System.out.println(" \\hline\n  \\multirow{3}{*}{virt} ");
 
             for (int row = 3; row < 6; row++) {
                 printRows(data, row, start, end, true);
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
 
 
     private static void createTexTable(String fileFirst, String caption, String label, String header) throws Exception {
         System.out.println("% 3-rows table for " + fileFirst);
         double[][] data = new double[3][];
         for (int gap = 1; gap <= 3; gap++) {
             data[gap - 1] = ReaderUtil.getLastString(new File("res", "share_" + fileFirst + "_" + gap + ".txt"));
         }
 
         createThreeRowsTable(data, caption, label, header);
     }
 
     public static void createThreeRowsTable(double[][] data, String caption, String label, String header) {
         int maxLen = 0;
         for (int i = 0; i < data.length; i++) {
             maxLen = Math.max(maxLen, data[i].length);
         }
 
         int width = 20;
         if (maxLen > width) {
             width = (maxLen + 1)/2;
         }
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
                    "\\centering\n" +
                    "\\caption{ " + caption + "}\n" +
                    "\\vspace{3mm}\n" +
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
 
 }
