 package ru.spbau.bioinf.tagfinder;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.PrintWriter;
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 public class AllDataGenerator {
     public static void main(String[] args) throws Exception {
         ValidTags2.main(args);
         System.out.println("\\documentclass{article}[12pt]\n" +
                 "\n" +
                 "\\usepackage{amsmath}\n" +
                 "\\usepackage{amsthm}\n" +
                 "\\usepackage{booktabs}\n" +
                 "\\usepackage{epsfig}\n" +
                 "\\usepackage{graphicx}\n" +
                 "\\usepackage{lscape}\n" +
                 "\\usepackage{multirow}\n" +
                 "\\usepackage{natbib}\n" +
                 "\\usepackage{setspace}\n" +
                 "\\usepackage{epstopdf}\n" +
                 "\n" +
                 "\\def\\STbar{{\\overline{\\mathrm{ST}}}}\n" +
                 "\\def\\STtilde{{\\widetilde{\\mathrm{ST}}}}\n" +
                 "\n" +
                 "\\setlength{\\topmargin}{0in}\n" +
                 "\\setlength{\\headheight}{12pt}\n" +
                 "\\setlength{\\headsep}{0.3in}\n" +
                 "\\setlength{\\textheight}{8.7in}\n" +
                 "\\setlength{\\oddsidemargin}{0in}\n" +
                 "\\setlength{\\evensidemargin}{0in}\n" +
                 "\\setlength{\\textwidth}{6.5in}\n" +
                 "\n" +
                 "\\title{Peptide Sequence Tags for Top-Down Spectra}\n" +
                 "\\author{}\n" +
                 "%\\date{}\n" +
                 "\n" +
                 "\\begin{document}\n" +
                 "\n" +
                 "\\maketitle\n" +
                 "\n" +
                 "\\doublespacing\n" +
                 "\\begin{abstract}\n" +
                 "\n" +
                 "\n" +
                 "\\end{abstract}" +
                 "\n" +
                 "%TEXT" +
                 "\n\n" +
                 "% MONO-TAGS");
         CalculateRelation.tableMono();
         TexTableGenerator.tableMonoCorrect();
 
         System.out.println("\n" +
                 "% SPECTRUM GRAPHS\n");
 
         TexTableGenerator.tableCorrectAndProper();
 
         System.out.println("\n");
 
         CalculateRelation.tableCorrectVsProper();
        CalculateRelation.tableMonoCorrect();
 
         System.out.println();
 
         CalculateRelation.tableLongestCorrect();
 
         System.out.println();
 
 
         KdTableGenerator.printTablesProper(1);
         KdTableGenerator.printTablesCorrect(1);
 
         System.out.println();
 
 
 
         TexTableGenerator.tablesCorrectErrAndAdv();
 
         System.out.println("\n\\begin{landscape}\n");
         System.out.println("\\begin{table}[t]\\footnotesize\n" +
                 "\\vspace{3mm}\\\n" +
                 "{\\centering\n" +
                 "\\begin{center}\n" +
                 "\\begin{tabular}{|c|c|c|c|c|}\n" +
                 "\\hline\n" +
                 "scan & protein & tag score & E-value \\\\\n" +
                 "\\hline\n" +
                 "\\hline\n" +
                 "898 & 3299 & 1 & 5.97E-5\\\\\n" +
                 "\\hline\n" +
                 "904 & 3299 & 1 & 2.92E-4\\\\\n" +
                 "\\hline\n" +
                 "1059 & 3296 & 2 & 1.18E-5\\\\\n" +
                 "\\hline\n" +
                 "1127 & 3949 & 3 & 5.09E-4\\\\\n" +
                 "\\hline\n" +
                 "1214 & 3949 & 1 & 2.05E-3\\\\\n" +
                 "\\hline\n" +
                 "1219 & 3949 & 3 & 3.11E-5\\\\\n" +
                 "\\hline\n" +
                 "1220 & 3949 & 3 & 2.08E-3\\\\\n" +
                 "\\hline\n" +
                 "1243 & 3302 & 2 & 2.59E-5\\\\\n" +
                 "\\hline\n" +
                 "1250 & 3302 & 2 & 9.12E-6\\\\\n" +
                 "\\hline\n" +
                 "1252 & 3302 & 3 & 3.95E-4\\\\\n" +
                 "\\hline\n" +
                 "1342 & 1453 & 2 & 1.44E-4\\\\\n" +
                 "\\hline\n" +
                 "\\multirow{2}{*}{1675} & 2535 & 4 & 2.76E-6\\\\\n" +
                 "& 983 & 4 & 2.87E-6\\\\\n" +
                 "\\hline\n" +
                 "\\end{tabular}\n" +
                 "\\end{center}\n" +
                 "\\par}\n" +
                 "\\centering\n" +
                 "\\caption{Tag-based matches for spectra unidentified by MS-Align+, along with the respective tag scores and E-values.}\n" +
                 "\\vspace{3mm}\n" +
                 "\\label{table:unident-spectra}\n" +
                 "\\end{table}");
         System.out.println("\\end{landscape}");
 
         System.out.println("\n\n% TOP-SCORING TAGS\n");
 
 
         IntencityTableGenerator.tableTopscoreAllAndAvg();
 
         System.out.println("\n\n\n% UNIDENTIFIED SPECTRA\n");
 
         System.out.println("\\begin{landscape}\n");
 
         UnmatchedStatistics.tableUnident();
         System.out.println();
         System.out.println();
         GenerateMatchesTable.tableMatches();
 
 
 
         System.out.println("\n\n\n%NEIGHBORS TABLE\n\n\n");
         System.out.println("\\end{landscape}");
 
         ///CalculateRelation.main(args);
         //IntencityTableGenerator.main(args);
         //UnmatchedStatistics.main(args);
 
         //KdTableGenerator.main(args);
         System.out.println("\n" +
                 "\\end{document}");
 
         generateAaDat();
     }
 
     private static void generateAaDat() throws Exception {
         String dir = "plots";
         BufferedReader in1 = ReaderUtil.createInputReader(new File(dir, "correct-l-tags.dat"));
         BufferedReader in2 = ReaderUtil.createInputReader(new File(dir, "correct-l-tags-err.dat"));
         BufferedReader in3 = ReaderUtil.createInputReader(new File(dir, "correct-l-tags-adv.dat"));
         PrintWriter out1 = ReaderUtil.createOutputFile(new File(dir, "1aa.dat"));
         PrintWriter out2 = ReaderUtil.createOutputFile(new File(dir, "2aa.dat"));
         PrintWriter out3 = ReaderUtil.createOutputFile(new File(dir, "3aa.dat"));
         for (int i = 1; i <=14; i++) {
             String[] d1 = in1.readLine().split(" ");
             String[] d2 = in2.readLine().split(" ");
             String[] d3 = in3.readLine().split(" ");
             out1.println(i + " " + d1[1] + " " + d1[4] + " " + d2[1] + " " + d3[1]);
             out2.println(i + " " + d1[2] + " " + d1[5] + " " + d2[2] + " " + d3[2]);
             out3.println(i + " " + d1[3] + " " + d1[6] + " " + d2[3] + " " + d3[3]);
         }
         out1.close();
         out2.close();
         out3.close();
     }
 }
