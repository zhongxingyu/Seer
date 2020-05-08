 package see.functions.service;
 
 import com.google.common.base.Function;
 
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkArgument;
import static see.functions.bool.BooleanCastHelper.toBoolean;
 
 public class If<T> implements Function<List<T>, T> {
     @Override
     public T apply(List<T> args) {
         checkArgument(args.size() == 2 || args.size() == 3, "If takes two or three arguments");
 
         T condition = args.get(0);
 
         if (isTrue(condition)) {
             return args.get(1);
         } else {
             return args.size() == 3 ? args.get(2) : null;
         }
     }
 
     private boolean isTrue(T condition) {
         if (condition instanceof Boolean) {
             return (Boolean) condition;
         } else if (condition instanceof Number) {
            return toBoolean((Number) condition);
         } else {
             throw new IllegalArgumentException("Cannot evaluate condition " + condition);
         }
     }
 }
