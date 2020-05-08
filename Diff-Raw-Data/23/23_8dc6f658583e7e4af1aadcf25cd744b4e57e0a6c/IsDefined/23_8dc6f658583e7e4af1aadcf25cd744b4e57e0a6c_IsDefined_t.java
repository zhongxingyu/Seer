 package see.functions.service;
 
 
 import com.google.common.base.Preconditions;
 import see.evaluation.Context;
 import see.functions.ContextCurriedFunction;
 import see.functions.VarArgFunction;
 
 import javax.annotation.Nonnull;
 import java.util.List;
 
 public class IsDefined implements ContextCurriedFunction<VarArgFunction<String, Boolean>> {
     @Override
     public VarArgFunction<String, Boolean> apply(@Nonnull final Context context) {
         return new VarArgFunction<String, Boolean>() {
             @Override
             public Boolean apply(@Nonnull List<String> strings) {
                 Preconditions.checkArgument(strings.size() == 1, "isDefined takes variable name");
 
                 String variable = strings.get(0);
                 
                return context.getScope().contains(variable);
             }
         };
     }
 
     @Override
     public String toString() {
         return "isDefined";
     }
 }
