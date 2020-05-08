 package net.citizensnpcs.adventures.dialog.evaluators;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class StringEvaluator {
     private static class InterpolatedStringEvaluator implements Evaluator {
         private final Matcher matcher;
         private final VariableSource source;
         private final String value;
 
         private InterpolatedStringEvaluator(VariableSource source, String value, Matcher matcher) {
             this.value = value;
             this.matcher = matcher;
             this.source = source;
         }
 
         @Override
         public Object get() {
             matcher.reset();
             boolean result = matcher.find();
             if (result) {
                 StringBuffer sb = new StringBuffer();
                 do {
                     Object variable = source.getVariable(matcher.group(1));
                     matcher.appendReplacement(sb, variable == null ? "null" : variable.toString());
                     result = matcher.find();
                 } while (result);
                 matcher.appendTail(sb);
                 return sb.toString();
             }
             return value;
         }
 
         @Override
         public boolean isConstant() {
             return false;
         }
 
         @Override
         public String toString() {
             return "InterpolatedStringEvaluator [" + value + "]";
         }
     }
 
     private static final Pattern INTERPOLATION = Pattern.compile("[$][{]([a-zA-Z.]+?)[}]");
 
     public static Evaluator create(String raw, VariableSource source) {
         Matcher matcher = INTERPOLATION.matcher(raw);
        if (matcher.matches())
             return new InterpolatedStringEvaluator(source, raw, matcher);
         return ConstantEvaluator.create(raw);
     }
 }
