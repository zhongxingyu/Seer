 package ru.spbau.bioinf.tagfinder;
 
 
 public class AllDataGenerator {
     public static void main(String[] args) throws Exception {
         //ValidTags.main(args);
         System.out.println("\\documentclass{article}\n" +
                 "\\usepackage{multirow}\n" +
                 "\\usepackage{lscape}\n" +
                 "\\usepackage{morefloats}\n" +
                 "\\usepackage{graphicx}\n" +
                 "\\usepackage{epstopdf}\n" +
                 "\n" +
                 "\\def\\STbar{{\\overline{\\mathrm{ST}}}}\n" +
                 "\n" +
                 "\\begin{document}");
         TexTableGenerator.tableOneTwo();
         CalculateRelation.tableThree();
         CalculateRelation.tableFour();
         KdTableGenerator.printTablesProper(1);
         KdTableGenerator.printTablesCorrect(1);
         TexTableGenerator.tablesNineTen();
 
         System.out.println("\\begin{landscape}");
         System.out.println("\\begin{table}[ht]\\footnotesize\n" +
                 "\\vspace{3mm}\n" +
                 "{\\centering\n" +
                 "\\begin{center}\n" +
                 "\\begin{tabular}{|c|c|}\n" +
                 "  \\hline\n" +
                 "  a pair of correct aa-s & an incorrect aa \\\\\n" +
                 "  \\hline\n" +
                 "  AG or GA & K \\\\\n" +
                 "  \\hline\n" +
                 "  AG or GA & Q \\\\\n" +
                 "  \\hline\n" +
                 "  AD or DA & \\multirow{3}{*}{W} \\\\\n" +
                 "  EG or GE & \\\\\n" +
                 "  SV or VS & \\\\\n" +
                 "  \\hline\n" +
                 "  GV or VG & R \\\\\n" +
                 "  \\hline\n" +
                 "  GG & N \\\\\n" +
                 "  \\hline\n" +
                 "\\end{tabular}\n" +
                 "\\end{center}\n" +
                 "\\par}\n" +
                 "\\centering\n" +
                 "\\caption{Pairs of consecutive amino acids that can be mistaken for a single one.}\n" +
                 "\\vspace{3mm}\n" +
                 "\\label{table:errors-vs}\n" +
                 "\\end{table}");
         System.out.println("\\end{landscape}");
 
         CalculateRelation.tableTwelve();
         TexTableGenerator.tableThirteen();
         IntencityTableGenerator.tableFourteenFifteen();
         UnmatchedStatistics.main(new String[]{});//16-17
 
        GenerateMatchesTable.main(args);
 
 
         ///CalculateRelation.main(args);
         //IntencityTableGenerator.main(args);
         //UnmatchedStatistics.main(args);
 
         //KdTableGenerator.main(args);
         System.out.println("\n" +
                 "\\end{document}");
     }
 }
