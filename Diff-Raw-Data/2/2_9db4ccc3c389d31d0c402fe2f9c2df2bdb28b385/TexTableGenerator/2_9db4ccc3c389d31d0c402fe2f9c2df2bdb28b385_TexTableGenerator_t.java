 package ru.spbau.bioinf.tagfinder;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 public class TexTableGenerator {
 
     public static int tableId = 0;
 
     public static void main(String[] args) throws Exception {
         tableCorrectAndProper();
         tablesCorrectErrAndAdv();
         tableMonoCorrect();
     }
 
     public static void tableCorrectAndProper() throws Exception {
         createTexTable("bar_exp_annotated_correct_none", "bar_virt_annotated_correct_zero", "Average percentage of correct $\\ell$-tags (basic spectrum graphs)", "correct $\\ell$-tags", "correct-l-tags");
         System.out.println();
         createTexTable("bar_exp_annotated_proper_none", "bar_virt_annotated_proper_zero", "Average percentage of proper $\\ell$-tags (basic spectrum graphs)", "proper $\\ell$-tags", "proper-l-tags");
     }
 
     public static void tablesCorrectErrAndAdv() throws Exception {
         createTexTableForSingleFile("bar_exp_annotated_correct_none_add", "Average percentage of correct $\\ell$-tags (error-correcting spectrum graphs)", "correct $\\ell$-tags", "correct-l-tags-err");
         System.out.println();
         createTexTableForSingleFile("bar_exp_annotated_correct_more", "Average percentage of correct $\\ell$-tags (combined spectrum graphs)", "correct $\\ell$-tags", "correct-l-tags-adv");
     }
 
     public static void tableMonoCorrect() throws Exception {
         createTexTableForSingleFile("bar_virt_annotated_correct_none", "Average percentage of correct mono-$\\ell$-tags w.r.t. all mono-$\\ell$-tags", "correct mono-$\\ell$-tags", "l-mono-correct");
     }
 
     private static void createTexTable(String fileFirst, String fileSecond, String caption, String header, String label) throws Exception {
         //System.out.println("% 6-rows table for " + fileFirst + " and " + fileSecond);
         double[][] data = new double[6][];
         for (int gap = 1; gap <= 3; gap++) {
             data[gap - 1] = ReaderUtil.getLastString(new File("res", "share_" + fileFirst + "_" + gap + ".txt"));
             data[gap + 2] = ReaderUtil.getLastString(new File("res", "share_" + fileSecond + "_" + gap + ".txt"));
         }
 
         createSixRowsTable(data, caption, header, 0, label);
     }
 
     private static void createTexTableForSingleFile(String fileFirst, String caption, String header, String label) throws Exception {
         //System.out.println("% 3-rows table for " + fileFirst);
         double[][] data = new double[3][];
         for (int gap = 1; gap <= 3; gap++) {
             data[gap - 1] = ReaderUtil.getLastString(new File("res", "share_" + fileFirst + "_" + gap + ".txt"));
         }
 
         createThreeRowsTable(data, caption, header, label);
     }
 
 
     public static void createSixRowsTable(double[][] data, String caption, String header, int columnHeaderDelta, String label) throws Exception {
         int maxLen = 0;
         for (int i = 0; i < data.length; i++) {
             maxLen = Math.max(maxLen, data[i].length);
         }
 
         prepareEps(label, data, header + " (%)", 6, columnHeaderDelta);
 
         int width = 20;
         if (maxLen > width) {
             width = (maxLen + 1) / 2;
         }
 
         System.out.print("\\begin{landscape}\n");
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
             for (int i = start; i <= end; i++) {
                 System.out.print(" & " + (i + columnHeaderDelta));
             }
             System.out.print("\\\\\n" +
                     "  \\hline\n" +
                     "  \\multirow{3}{*}{exp}\n");
 
 
             for (int row = 0; row < 3; row++) {
                 printRows(data, row, start, end, true);
             }
 
             System.out.print(" \\hline\n  \\multirow{3}{*}{virt} \n");
 
             for (int row = 3; row < 6; row++) {
                 printRows(data, row, start, end, true);
             }
 
             System.out.print(" \\hline\n" +
                     "\\end{tabular}\n" +
                     "\\end{center}\n" +
                     "\\par}\n" +
                     "\\centering\n");
             if (end == maxLen) {
                 System.out.print("\\caption{ " + caption + ".}\n" +
                 "\\label{table:" + label + "}\n");
             }
 
             System.out.print("\\vspace{3mm}\n" +
                     "\\end{table}\n\n");
         }
         System.out.println("\\end{landscape}");
 
         System.out.println("\n\\begin{figure}\n" +
                 "  \\begin{center}");
         System.out.println("\\includegraphics{" + label + "}");
         System.out.println("\\end{center}\n" +
                 "\\caption{" + caption + "}\n" +
                 "  \\label{fig:" + label + "}\n" +
                 "\\end{figure}\n");
     }
 
     private static void prepareEps(String label, double[][] data, String header, int rows, int columnHeaderDelta) throws IOException {
         tableId++;
 
         PrintWriter dataFile = ReaderUtil.createOutputFile(new File("plots", label + ".dat"));
         for (int i = columnHeaderDelta; i < 14; i++) {
             dataFile.print((i + 1) + " ");
             for (int col = 0; col < rows; col++) {
                 dataFile.print(data[col][i - columnHeaderDelta] + " ");
             }
             dataFile.println();
         }
         dataFile.close();
 
         PrintWriter gplFile = ReaderUtil.createOutputFile(new File("plots", label + ".gpl"));
        gplFile.print("set terminal postscript eps color lw 4 \"Helvetica\" 22\n" +
                 "set out \"" + label + ".eps\"\n" +
                 "set ylabel \"" + header.replaceAll("\\$\\\\ell\\$", "l") + "\"\n" +
                 "set xlabel \"tag length (l)\"\n" +
                 "plot");
         for (int i = 1; i <= rows; i++) {
             int gap = i;
             if (gap > 3) {
                 gap = gap - 3;
             }
             String titlePrefix = "";
             if (rows > 3) {
                 titlePrefix = i > 3 ? "virt, " : " exp, ";
             }
             gplFile.print("\"plots/" + label + ".dat\" using 1:" + (i + 1) + " title '" + titlePrefix + "t=" + gap + "' with linespoints");
             if (i < rows) {
                 gplFile.println(", \\");
             }
         }
 
         gplFile.close();
     }
 
 
     public static void createThreeRowsTable(double[][] data, String caption, String header, String label) throws Exception {
         int maxLen = 0;
         for (int i = 0; i < data.length; i++) {
             maxLen = Math.max(maxLen, data[i].length);
         }
 
 
         prepareEps(label, data, header + " (%)", 3, 0);
         int width = 20;
         if (maxLen > width) {
             width = (maxLen + 1) / 2;
             if (width > 20) {
                 width = (maxLen + 1) / 3;
                 if (width < 18) {
                     width = 18;
                 }
             }
         }
 
         System.out.println("\n\\begin{landscape}\n");
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
                 System.out.println("\\caption{ " + caption + ".}\n" +
                 "\\label{table:" + label + "}\n");
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
                 "  \\label{fig:" + label +"}\n" +
                 "\\end{figure}\n");
     }
 
     private static void printRows(double[][] data, int row, int start, int end, boolean needAmp) {
         if (needAmp) {
             System.out.print("&  ");
         }
         System.out.print((row % 3 + 1) + "-aa ");
         for (int i = start; i <= end; i++) {
             System.out.print(" & ");
             if (data[row].length >= i) {
                 System.out.print(ValidTags.df.format(data[row][i - 1]));
             }
         }
         System.out.println("\\\\");
     }
 }
