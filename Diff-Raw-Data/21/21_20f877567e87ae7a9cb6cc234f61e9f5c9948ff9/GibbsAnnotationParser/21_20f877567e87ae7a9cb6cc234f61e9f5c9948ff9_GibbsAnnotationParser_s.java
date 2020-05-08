 package uk.ac.ebi.chemet.tools.annotation.parse;
 
 import uk.ac.ebi.mdk.domain.annotation.GibbsEnergy;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * An annotation parser for Gibbs energy annotations.
  *
  * @author John May
  */
 final class GibbsAnnotationParser extends AnnotationParser<GibbsEnergy> {
 
     private final static Pattern DOUBLE_PATTERN = Pattern
             .compile("([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)");
 
     /** @inheritDoc */
     @Override public GibbsEnergy parse(String str) {
         if (str == null || str.isEmpty())
             return null;
         Matcher matcher = DOUBLE_PATTERN.matcher(str);
         if (matcher.find()) {
             String energy = matcher.group(1);
            if (matcher.find(matcher.end())) {
                String error = matcher.group(1);
                 return new GibbsEnergy(Double.parseDouble(energy),
                                        Double.parseDouble(error));
             }
             return new GibbsEnergy(Double.parseDouble(energy),
                                    0d);
         }
         return null;
     }
 }
