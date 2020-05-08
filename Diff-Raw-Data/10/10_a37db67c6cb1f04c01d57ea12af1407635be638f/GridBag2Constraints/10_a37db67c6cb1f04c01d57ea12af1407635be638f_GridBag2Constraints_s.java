 package myjava.awt;
 
 import java.awt.*;
 
 public class GridBag2Constraints implements Cloneable, java.io.Serializable, polyglot.ext.esj.primitives.ESJObject {
     public GridBag2Constraints(polyglot.ext.esj.tologic.LogVar dontcare,
                                boolean isQuantifyVar) {
         super();
         if (isQuantifyVar) {
             this.var_log =
               dontcare;
             this.old =
               this.clone();
             this.old.var_log =
               dontcare;
         }
     }
     
     public GridBag2Constraints(polyglot.ext.esj.tologic.Log2Var dontcare,
                                boolean isQuantifyVar) {
         super();
         if (isQuantifyVar) {
             this.var_log2 =
               dontcare;
             this.old =
               this.clone();
             this.old.var_log2 =
               dontcare;
         }
     }
     
     GridBag2Constraints old;
     
     public GridBag2Constraints old() {
         return this.old;
     }
     
     boolean isOld;
     
     public boolean isOld() {
         return this.isOld;
     }
     
     public polyglot.ext.esj.tologic.LogVar var_log;
     
     public polyglot.ext.esj.tologic.LogVar var_log() {
         return this.var_log;
     }
     
     public polyglot.ext.esj.tologic.Log2Var var_log2;
     
     public polyglot.ext.esj.tologic.Log2Var var_log2() {
         return this.var_log2;
     }
     
     public boolean isQuantifyVar() {
         return this.var_log !=
           null;
     }
     
     public boolean isQuantifyVar2() {
         return this.var_log2 !=
           null;
     }
     
     int relationizerStep =
       0;
     
     boolean isRelationized() {
         return this.relationizerStep ==
           polyglot.ext.esj.tologic.LogMap.relationizerStep();
     }
     
     int clonerStep =
       0;
     
     public boolean isCloned() {
         return this.clonerStep ==
           polyglot.ext.esj.tologic.LogMap.clonerStep();
     }
     
     static polyglot.ext.esj.primitives.ESJList<GridBag2Constraints> allInstances =
       new polyglot.ext.esj.primitives.ESJList<GridBag2Constraints>();
     
     public static polyglot.ext.esj.primitives.ESJList<GridBag2Constraints> allInstances() {
         return GridBag2Constraints.allInstances;
     }
     
     public polyglot.ext.esj.primitives.ESJList<GridBag2Constraints> allInstances2() {
         return GridBag2Constraints.allInstances;
     }
     
     public static polyglot.ext.esj.tologic.LogSet allInstances_log() {
         return polyglot.ext.esj.tologic.LogMap.bounds_log(GridBag2Constraints.class,
                                                           false,
                                                           false);
     }
     
     public static polyglot.ext.esj.tologic.Log2Set allInstances_log2() {
         return new polyglot.ext.esj.tologic.Log2Set(polyglot.ext.esj.tologic.LogMap.bounds_log2(GridBag2Constraints.class,
                                                                                                 false));
     }
     
     public polyglot.ext.esj.tologic.LogFormula cmpOp(String kodkodiOp,
                                                      String kodkodOp,
                                                      polyglot.ext.esj.primitives.ESJObject o2) {
         return polyglot.ext.esj.tologic.LogFormula.binaryOp(this.var_log.string(),
                                                             kodkodiOp,
                                                             kodkodOp,
                                                             o2.var_log().string());
     }
     
     public polyglot.ext.esj.tologic.LogFormula cmpOp(String kodkodiOp,
                                                      String kodkodOp,
                                                      polyglot.ext.esj.tologic.LogObject o2) {
         return polyglot.ext.esj.tologic.LogFormula.binaryOp(this.var_log.string(),
                                                             kodkodiOp,
                                                             kodkodOp,
                                                             o2.string());
     }
     
     public kodkod.ast.Expression sum() {
        return this.var_log2.expression();
     }
     
     public void relationize() {
         if (!isRelationized()) {
             this.relationizerStep++;
             relationizeOld();
         }
     }
     
     public void relationizeOld() {
         
     }
     
     public GridBag2Constraints clone() {
         if (isCloned())
             return this;
         this.clonerStep =
           polyglot.ext.esj.tologic.LogMap.clonerStep();
         GridBag2Constraints res =
           new GridBag2Constraints(new polyglot.ext.esj.tologic.LogVar(null),
                                   false);
         res.isOld =
           true;
         this.old =
           res;
         return res;
     }
     
     boolean verifyInvariants() {
         return true;
     }
     
     long startMethodTime;
     
     Object result;
     
     public void result(Object r) {
         this.result =
           r;
     }
     
     void initEnsuredMethod() {
         this.startMethodTime =
           System.currentTimeMillis();
         polyglot.ext.esj.tologic.LogMap.initRelationize();
         clone();
     }
     
     polyglot.ext.esj.tologic.LogFormula verifyInvariants_log() {
         return new polyglot.ext.esj.tologic.LogFormula(true);
     }
     
     kodkod.ast.Formula verifyInvariants_log2() {
         return kodkod.ast.Formula.TRUE;
     }
     
     public static final int RELATIVE =
       -1;
     
     public static final int REMAINDER =
       0;
     
     public static final int NONE =
       0;
     
     public static final int BOTH =
       1;
     
     public static final int HORIZONTAL =
       2;
     
     public static final int VERTICAL =
       3;
     
     public static final int CENTER =
       10;
     
     public static final int NORTH =
       11;
     
     public static final int NORTHEAST =
       12;
     
     public static final int EAST =
       13;
     
     public static final int SOUTHEAST =
       14;
     
     public static final int SOUTH =
       15;
     
     public static final int SOUTHWEST =
       16;
     
     public static final int WEST =
       17;
     
     public static final int NORTHWEST =
       18;
     
     public static final int PAGE_START =
       19;
     
     public static final int PAGE_END =
       20;
     
     public static final int LINE_START =
       21;
     
     public static final int LINE_END =
       22;
     
     public static final int FIRST_LINE_START =
       23;
     
     public static final int FIRST_LINE_END =
       24;
     
     public static final int LAST_LINE_START =
       25;
     
     public static final int LAST_LINE_END =
       26;
     
     public int gridx;
     
     public int gridy;
     
     public int gridwidth;
     
     public int gridheight;
     
     public double weightx;
     
     public double weighty;
     
     public int anchor;
     
     public int fill;
     
     public Insets insets;
     
     public int ipadx;
     
     public int ipady;
     
     int tempX;
     
     int tempY;
     
     int tempWidth;
     
     int tempHeight;
     
     int minWidth;
     
     int minHeight;
     
     private static final long serialVersionUID =
       -1000070633030801713L;
     
     public GridBag2Constraints() {
         super();
         gridx =
           RELATIVE;
         gridy =
           RELATIVE;
         gridwidth =
           1;
         gridheight =
           1;
         weightx =
           0;
         weighty =
           0;
         anchor =
           CENTER;
         fill =
           NONE;
         insets =
           new Insets(0,
                      0,
                      0,
                      0);
         ipadx =
           0;
         ipady =
           0;
         this.allInstances.add(this);
     }
     
     public GridBag2Constraints(int gridx,
                                int gridy,
                                int gridwidth,
                                int gridheight,
                                double weightx,
                                double weighty,
                                int anchor,
                                int fill,
                                Insets insets,
                                int ipadx,
                                int ipady) {
         super();
         this.gridx =
           gridx;
         this.gridy =
           gridy;
         this.gridwidth =
           gridwidth;
         this.gridheight =
           gridheight;
         this.fill =
           fill;
         this.ipadx =
           ipadx;
         this.ipady =
           ipady;
         this.insets =
           insets;
         this.anchor =
           anchor;
         this.weightx =
           weightx;
         this.weighty =
           weighty;
         this.allInstances.add(this);
     }
     
     public Object clone2() {
         try {
             GridBag2Constraints c =
               (GridBag2Constraints)
                 super.clone();
             c.insets =
               (Insets)
                 insets.clone();
             return c;
         }
         catch (CloneNotSupportedException e) {
             throw new InternalError();
         }
     }
 }
