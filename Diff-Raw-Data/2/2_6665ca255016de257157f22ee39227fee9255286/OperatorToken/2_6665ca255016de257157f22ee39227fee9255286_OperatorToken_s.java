 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.token;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.OPERATOR;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 
 
 /**
  * Zq\g[NNX
  * 
  * @author kou-tngt
  *
  */
 public class OperatorToken extends AstTokenAdapter {
 
     /**
      * LXg͉Zq\萔CX^X
      */
     public static final OperatorToken CAST = new OperatorToken(null,"CAST", 2, false, false, null,new int[]{0});
 
     /**
      * CNgZqƃfNgZq\萔CX^X
      */
     public static final OperatorToken INCL_AND_DECL = new OperatorToken(null,"INCLEMENT", 1, true, true,
             null,new int[]{0});
 
     /**
      * Zq\萔CX^X
      */
     public static final OperatorToken ASSIGNMENT = new OperatorToken(OPERATOR.ASSIGNMENT,"ASSIMENT", 2, true, false, null,
             new int[]{0});
 
     /**
      * Zq\萔CX^X
      */
     public static final OperatorToken COMPOUND_ASSIGNMENT = new OperatorToken(OPERATOR.ASSIGNMENT,"COMPOUND_ASSIGNMENT",2,true,true,null,
             new int[]{0});
     
     /**
      * PZpZq\萔CX^X
      */
     public static final OperatorToken ARITHMETHIC_UNARY = new OperatorToken(OPERATOR.ARITHMETIC,"ARITHMETIC_UNARY", 1, false,
             true, null,new int[]{0});
     
     /**
      * 񍀎ZpZq\萔CX^X
      */
     public static final OperatorToken ARITHMETICH_BINOMIAL = new OperatorToken(OPERATOR.ARITHMETIC,"ARITHMETIC_BINOMIAL", 2, false, true, null,
             new int[]{0,1});
     
     /**
      * P_Zq\萔CX^X
      */
     public static final OperatorToken LOGICAL_UNARY = new OperatorToken(OPERATOR.LOGICAL,"NOT_UNARY", 1, false, true,
             PrimitiveTypeInfo.BOOLEAN,new int[]{});
     
     /**
      * 񍀘_Zq\萔CX^X
      */
     public static final OperatorToken LOGICAL_BINOMIAL = new OperatorToken(OPERATOR.LOGICAL,"LOGICAL_BINOMIAL", 2, false, true,
             PrimitiveTypeInfo.BOOLEAN,new int[]{0,1});
     
     /**
      * PrbgZq\萔CX^X
      */
     public static final OperatorToken BIT_UNARY = new OperatorToken(OPERATOR.BITS,"BIT_UNARY", 1, false, true,
            null,new int[]{});
     
     /**
      * 񍀃rbgZq\萔CX^X
      */
     public static final OperatorToken BIT_BINOMIAL = new OperatorToken(OPERATOR.BITS,"BIT_BINOMIAL", 2, false, true, null,
             new int[]{0,1});
     
     /**
      * 񍀃VtgZq\萔CX^X
      */
     public static final OperatorToken SHIFT = new OperatorToken(OPERATOR.SHIFT,"SHIFT", 2, false, true, null,
             new int[]{0,1});
 
     /**
      * 񍀔rZq\萔CX^X
      */
     public static final OperatorToken COMPARATIVE = new OperatorToken(OPERATOR.COMPARATIVE,"COMPARATIVE", 2, false, true,
             PrimitiveTypeInfo.BOOLEAN,new int[]{});
     
     /**
      * OZq\萔CX^X
      */
     public static final OperatorToken TERNARY= new OperatorToken(null,"TERNARY", 3, false, true,
             null,new int[]{1,2});
 
     /**
      * zLqq\萔CX^X
      */
     public static final OperatorToken ARRAY = new OperatorToken(null,"ARRAY", 2, false, true, null,new int[]{});
 
     /**
      * Zq̕C̐CӒlւ̎QƂƑsǂCZʂ̌^w肷RXgN^.
      * 
      * @param text Zq̕
      * @param termCount ̐
      * @param leftIsAssignmentee Ӓlւ̑ꍇtrue
      * @param leftIsReferencee Ӓlւ̂ꍇtrue
      * @param specifiedResultType Zʂ̌^܂Ăꍇ͂̌^C܂ĂȂꍇnullw肷
      * @throws IllegalArgumentException termCount0ȉ̏ꍇ
      */
     public OperatorToken(final OPERATOR operator,final String text, final int termCount, final boolean leftIsAssignmentee,
             final boolean leftIsReferencee, final UnresolvedTypeInfo specifiedResultType,
             final int[] typeSpecifiedTermIndexes) {
         super(text);
 
         if (termCount <= 0) {
             throw new IllegalArgumentException("Operator must treat one or more terms.");
         }
 
         this.operator = operator;
         this.leftIsAssignmentee = leftIsAssignmentee;
         this.leftIsReferencee = leftIsReferencee;
         this.termCount = termCount;
         this.specifiedResultType = specifiedResultType;
         this.typeSpecifiedTermIndexes = typeSpecifiedTermIndexes;
     }
 
     /**
      * ̉Zq舵̐Ԃ.
      * @return ̉Zq舵̐
      */
     public int getTermCount() {
         return this.termCount;
     }
 
     /**
      * Ӓlւ̑邩ǂԂ.
      * @return@Ӓlւ̑ꍇtrue
      */
     @Override
     public boolean isAssignmentOperator() {
         return this.leftIsAssignmentee;
     }
 
     /**
      * Zq\g[NǂԂ.
      * 
      * @return@true
      */
     @Override
     public boolean isOperator() {
         return true;
     }
 
     /**
      * ӒlQƂƂėp邩ǂԂ.
      * @return@ӒlQƂƂėpꍇtrue
      */
     public boolean isLeftTermIsReferencee() {
         return this.leftIsReferencee;
     }
     
     /**
      * OpEnum OPERATOR̗vfԂD
      * OɌ^ϏȂނ̉Zq̏ꍇnullԂD
      * @return OpEnum OPERATOR̗vfCOɌ^ϏȂނ̉Zq̏ꍇnull
      */
     public OPERATOR getOperator(){
         return this.operator;
     }
 
     /**
      * Zʂ̌^܂Ăꍇ͂̌^Ԃ.
      * ܂ĂȂꍇnullԂ.
      * @return Zʂ̌^܂Ăꍇ͂̌^C܂ĂȂꍇnull
      */
     public UnresolvedTypeInfo getSpecifiedResultType() {
         return this.specifiedResultType;
     }
     
     /**
      * Zʂ̌^肳ۂɍl鍀̃CfbNX̔zԂ.
      * ̌^Ƃ͊֌WȂ^肳ꍇ͋̔zԂ.
      * @return Zʂ̌^肳ۂɍl鍀̃CfbNX̔z
      */
     public int[] getTypeSpecifiedTermIndexes(){
         return this.typeSpecifiedTermIndexes;
     }
 
     /**
      * Ӓlւ̑邩ǂ\
      */
     private final boolean leftIsAssignmentee;
 
     /**
      * ӒlQƂƂėp邩ǂ\
      */
     private final boolean leftIsReferencee;
 
     /**
      * ̉Zq舵̐
      */
     private final int termCount;
 
     /**
      * Zq
      */
     private final OPERATOR operator;
     
     /**
      * Zʂ̌^\.
      * ܂ĂȂꍇnull.
      */
     private final UnresolvedTypeInfo specifiedResultType;
     
     /**
      * Zʂ̌^肳ۂɍl鍀̃CfbNX̔z
      */
     private final int[] typeSpecifiedTermIndexes;
 }
