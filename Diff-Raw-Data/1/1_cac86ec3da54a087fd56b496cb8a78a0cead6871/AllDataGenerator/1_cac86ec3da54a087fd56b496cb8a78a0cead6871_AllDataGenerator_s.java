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
         TexTableGenerator.main(args);
         CalculateRelation.main(args);
         IntencityTableGenerator.main(args);
         UnmatchedStatistics.main(args);
         GenerateMatchesTable.main(args);
         System.out.println("\n" +
                 "\\end{document}");
     }
 }
