 package de.bioinformatikmuenchen.pg4.alignment.recursive.io;
 
 import de.bioinformaikmuenchen.pg4.common.alignment.AlignmentResult;
 
 /**
  *
  * @author koehleru
  */
 public class ALIAlignmentOutputFormatter extends AbstractAlignmentOutputFormatter {
 
     public String format(AlignmentResult result) {
         StringBuilder builder = new StringBuilder();
         builder.append("<div>");
         //Score
         builder.append("<b>Score</b>:");
         builder.append(result.getScore());
         //Alignments
         
         
        result.getAlignment().
         builder.append("</div>");
         return "Alignment score: " + Double.toString(result.getScore());
     }
 }
