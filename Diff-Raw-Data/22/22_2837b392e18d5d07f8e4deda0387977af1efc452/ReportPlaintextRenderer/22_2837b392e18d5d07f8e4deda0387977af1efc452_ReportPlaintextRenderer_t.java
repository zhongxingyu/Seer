 package com.github.arnostv.xquery.coverage.saxon;
 
 import java.util.Set;
 
 public class ReportPlaintextRenderer {
     public String renderToPlaintext(CoverageReport report) {
         StringBuilder sb = new StringBuilder();
 
         final Set<Integer> coveredLines = report.coveredLines();
         final Set<Integer> ignoredLines = report.ignoredLines();
 
         for (int i = 1, l = report.numberOfLines(); i <= l; i++) {
             sb.append(String.format("%4d ", i));
             if (coveredLines.contains(i)) {
                 sb.append("+ ");
            } else if (ignoredLines.contains(i)) {
                 sb.append(". ");
             } else {
                 sb.append("  ");
             }
 
             sb.append(report.lineText(i));
             sb.append('\n');
         }
 
        sb.append('\n');

        //TODO should be calculated by report
        int covered = coveredLines.size();
        int toCover = report.numberOfLines() - ignoredLines.size();
        int coverage = (covered * 100) / toCover;

        sb.append(String.format("Covered %d of %d lines (%2d%%)", covered, toCover, coverage));

         return sb.toString();
     }
 }
