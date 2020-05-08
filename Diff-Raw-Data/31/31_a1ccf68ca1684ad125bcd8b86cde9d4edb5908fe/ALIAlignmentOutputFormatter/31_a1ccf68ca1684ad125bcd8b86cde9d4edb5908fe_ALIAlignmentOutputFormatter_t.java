 package de.bioinformatikmuenchen.pg4.alignment.recursive.io;
 
 import de.bioinformaikmuenchen.pg4.common.alignment.AlignmentResult;
import de.bioinformaikmuenchen.pg4.common.alignment.SequencePairAlignment;
 
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
        builder.append("<br/>");
         //Alignments
         
        builder.append("<b>Alignments:</b><br/>");
        builder.append("<br/>");
         
        for(SequencePairAlignment alignment : result.getAlignments()) {
            
        }
         builder.append("</div>");
         return "Alignment score: " + Double.toString(result.getScore());
     }
 }
