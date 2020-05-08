 package de.bioinformatikmuenchen.pg4.alignment.io;
 
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.alignment.SequencePairAlignment;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 
 /**
  *
  * @author koehleru
  */
 public class HTMLAlignmentOutputFormatter extends AbstractAlignmentOutputFormatter {
 
     private static DecimalFormat numberFormat = getTwoDigitDecimalFormat();
 
     private static DecimalFormat getTwoDigitDecimalFormat() {
         DecimalFormat ret = new DecimalFormat();
         DecimalFormatSymbols dfs = new DecimalFormatSymbols();
         dfs.setDecimalSeparator('.');
         ret.setDecimalFormatSymbols(dfs);
        ret.setGroupingUsed(false);
         ret.setMinimumFractionDigits(2);
         ret.setMaximumFractionDigits(2);
         return ret;
     }
 
     public String format(AlignmentResult result) {
         check(result);
         StringBuilder builder = new StringBuilder();
         builder.append("<div>");
         for (SequencePairAlignment align : result.getAlignments()) {
             builder.append("<h3>Aligment of ").append(result.getQuerySequenceId()).append(" of ").append(result.getTargetSequenceId()).append("</h3>");
             builder.append("<h4>Score: ").append(numberFormat.format(result.getScore())).append("</h4>");
             //First line
             builder.append("<pre>");
             builder.append(align.queryAlignment);
             builder.append("</pre>");
             //Second line in red
             builder.append("<pre style=\"color: red;\">");
             align.calculateMatchLine();
             builder.append(align.matchLine);
             builder.append("</pre>");
             //Third line
             builder.append("<pre>");
             builder.append(align.targetAlignment);
             builder.append("</pre>");
 
             builder.append("</div>");
         }
         return builder.toString();
     }
 }
