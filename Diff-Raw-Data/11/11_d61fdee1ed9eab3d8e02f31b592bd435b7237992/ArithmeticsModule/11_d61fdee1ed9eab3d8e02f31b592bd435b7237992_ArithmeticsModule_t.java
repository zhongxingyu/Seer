 package xhl.modules;
 
 import java.util.List;
 
 import xhl.core.Builder;
 import xhl.core.GenericModule;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 public class ArithmeticsModule extends GenericModule {
 
     @Function(name = "+")
    public Builder<Double> plus(Builder<Double> arg1, Builder<Double> arg2) {
         return new ArithmeticsBuilder(ArithmOperation.ADD, arg1, arg2);
     }
 
     @Function(name = "-")
    public Builder<Double> minus(Builder<Double> arg1, Builder<Double> arg2) {
         return new ArithmeticsBuilder(ArithmOperation.SUB, arg1, arg2);
     }
 
     @Function(name = "*")
    public Builder<Double> multiply(Builder<Double> arg1, Builder<Double> arg2) {
         return new ArithmeticsBuilder(ArithmOperation.MUL, arg1, arg2);
     }
 
     @Function(name = "/")
    public Builder<Double> divide(Builder<Double> arg1, Builder<Double> arg2) {
         return new ArithmeticsBuilder(ArithmOperation.DIV, arg1, arg2);
     }
 
     public enum ArithmOperation {
         ADD, SUB, MUL, DIV
     }
 
     static class ArithmeticsBuilder implements Builder<Double> {
         public List<Builder<Double>> operands = newArrayList();
         public ArithmOperation operation;
 
 
         public ArithmeticsBuilder(ArithmOperation operation,
                 Builder<Double> op1, Builder<Double> op2) {
             this.operation = operation;
             operands.add(op1);
             operands.add(op2);
         }
 
         @Override
         public Double toValue() {
             double[] args = new double[operands.size()];
             for (int i = 0; i < operands.size(); i++)
                 args[i] = operands.get(i).toValue();
             double result;
             switch (operation) {
             case ADD:
                 result = 0;
                 for (double val : args) {
                     result += val;
                 }
                 return result;
             case SUB:
                 result = args.length > 1 ? args[0] : - args[0];
                 for (int i = 1; i < args.length; i++) {
                     result -= args[i];
                 }
                 return result;
             case MUL:
                 result = 1;
                 for (double val : args) {
                     result *= val;
                 }
                 return result;
             case DIV:
                 result = args.length > 1 ? args[0] : 1 / args[0];
                 for (int i = 1; i < args.length; i++) {
                     result /= args[i];
                 }
                 return result;
             default:
                 return null;
             }
         }
 
         @Override
         public String toCode() {
             // TODO Auto-generated method stub
             return null;
         }
     }
 }
