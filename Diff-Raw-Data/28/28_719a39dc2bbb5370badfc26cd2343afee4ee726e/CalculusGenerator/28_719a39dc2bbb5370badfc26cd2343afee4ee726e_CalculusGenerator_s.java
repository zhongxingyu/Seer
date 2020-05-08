 package no.anderska.wta.questions;
 
 import java.util.Random;
 
 public class CalculusGenerator {
     private final static int stopWhenNodes = 12;
     private final static int maxNum = 43;
 
 
     public void setRandom(Random random) {
         this.random = random;
     }
 
     private Random random = new Random();
     private int numberOfNumbers=0;
     private int xpick;
 
     private int leftpicks = stopWhenNodes;
 
 
     private static enum Operator {
         ADD("+"),SUBSTRACT("-"),MULTIPLY("*");
 
         private Operator(String sign) {
             this.sign = sign;
         }
         public final String sign;
     }
 
 
     private static class Part {
         Operator operator;
         Integer value;
         Part left;
         Part right;
         boolean isX = false;
 
         private String equasion() {
             if (operator == null) {
                 return "" + value;
             }
             StringBuilder res = new StringBuilder();
             res.append(left.equasion());
             res.append(operator.sign);
             res.append(right.equasion());
             return res.toString();
         }
 
         @Override
         public String toString() {
             StringBuilder res = new StringBuilder();
             res.append(equasion());
             res.append("->");
             res.append(value);
             return res.toString();
         }
     }
 
 
     public static final int TYPE_NUMBER = 0;
     public static final int TYPE_ADD = 1;
     public static final int TYPE_SUBSTR = 2;
     public static final int TYPE_MULT = 3;
 
     private int pickType(boolean forcenum,int leftpicks) {
         if (forcenum || leftpicks <= 0) {
             return TYPE_NUMBER;
         }
        return random.nextInt(3)+1;
     }
 
     private Part generate(boolean forcenum) {
         Part part = new Part();
         switch (pickType(forcenum,leftpicks)) {
             case TYPE_NUMBER:
                 this.numberOfNumbers=this.numberOfNumbers+1;
                 part.value = random.nextInt(maxNum)+1;
                 return part;
             case TYPE_ADD:
                 part.operator = Operator.ADD;
                 part.left = generate(false);
                 part.right = generate(false);
                 part.value = part.left.value+part.right.value;
                 return part;
             case TYPE_SUBSTR:
                 part.operator = Operator.SUBSTRACT;
                 part.left = generate(true);
                 part.right = generate(true);
                 part.value = part.left.value-part.right.value;
                 return part;
             case TYPE_MULT:
                 part.operator = Operator.MULTIPLY;
                 part.left = generate(true);
                 part.right = generate(true);
                 part.value = part.left.value*part.right.value;
                 return part;
         }
         throw new RuntimeException("Should not be here");
     }
 
     private void partToStr(StringBuilder res,Part part) {
         if (part.operator == null) {
             res.append(part.isX ? "X" : part.value);
             return;
         }
         partToStr(res, part.left);
         res.append(part.operator.sign);
         partToStr(res, part.right);
     }
 
     public String[] generateComputation() {
         Part part = generate(false);
         StringBuilder res = new StringBuilder();
         partToStr(res,part);
 
         String [] resarr = new String[2];
         resarr[0] = res.toString();
         resarr[1] = "" + part.value;
         return resarr;
     }
 
 
     public String[] generateEquation() {
         Part part = generate(false);
 
         xpick = random.nextInt(numberOfNumbers);
 
         Part xp = setX(part);
 
         StringBuilder res = new StringBuilder();
         partToStr(res,part);
 
         String [] resarr = new String[2];
         resarr[0] = res.toString() + " eq " + part.value;
         resarr[1] = "" + xp.value;
         return resarr;
     }
 
     private Part setX(Part part) {
         if (part.operator == null) {
             xpick--;
             if (xpick >= 0) {
                 return null;
             }
             part.isX = true;
             return part;
         }
         Part res = setX(part.left);
         if (res == null) {
             res=setX(part.right);
         }
         return res;
     }
 
 
 }
