 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeSet;
 
 
 /**
  * ϐ錾̏ۗLNX
  * 
  * @author t-miyake
  *
  */
 @SuppressWarnings("serial")
 public class VariableDeclarationStatementInfo extends SingleStatementInfo implements ConditionInfo {
 
     /**
      * 錾ĂϐCCʒu^ď
      * 錾ĂϐĂꍇC̃RXgN^gp
      * 
      * @param variableDeclaration 錾Ă郍[Jϐ
      * @param initializationExpression 
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public VariableDeclarationStatementInfo(final LocalVariableUsageInfo variableDeclaration,
             final ExpressionInfo initializationExpression, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
         super(variableDeclaration.getUsedVariable().getDefinitionUnit(), fromLine, fromColumn,
                 toLine, toColumn);
 
         if (null == variableDeclaration) {
             throw new IllegalArgumentException("declaredVariable is null");
         }
 
         this.variableDeclaration = variableDeclaration;
         this.variableDeclaration.setOwnerExecutableElement(this);
         this.variableDeclaration.getUsedVariable().setDeclarationStatement(this);
 
         if (null != initializationExpression) {
             this.initializationExpression = initializationExpression;
         } else {
 
             final LocalSpaceInfo ownerSpace = variableDeclaration.getUsedVariable()
                     .getDefinitionUnit();
 
             // ownerSpaceInfo\bh܂̓RXgN^̎
             if (ownerSpace instanceof CallableUnitInfo) {
                 this.initializationExpression = new EmptyExpressionInfo(
                         (CallableUnitInfo) ownerSpace, toLine, toColumn - 1, toLine, toColumn - 1);
             }
 
             // ownerSpaceInfoubN̎
             else if (ownerSpace instanceof BlockInfo) {
                 final CallableUnitInfo ownerMethod = ((BlockInfo) ownerSpace).getOwnerMethod();
                this.initializationExpression = new EmptyExpressionInfo(null, toLine, toColumn - 1,
                         toLine, toColumn - 1);
             }
             
             // ȊO̎̓G[
             else{
                 throw new IllegalStateException();
             }
         }
 
         this.initializationExpression.setOwnerExecutableElement(this);
 
     }
 
     /**
      * ̐錾Ő錾ĂϐԂ
      * 
      * @return ̐錾Ő錾Ăϐ
      */
     public final LocalVariableInfo getDeclaredLocalVariable() {
         return this.variableDeclaration.getUsedVariable();
     }
 
     /**
      * 錾̕ϐgpԂ
      * @return 錾̕ϐgp
      */
     public final LocalVariableUsageInfo getDeclaration() {
         return this.variableDeclaration;
     }
 
     /**
      * 錾Ăϐ̏Ԃ
      * 
      * @return 錾Ăϐ̏DĂꍇnull
      */
     public final ExpressionInfo getInitializationExpression() {
         return this.initializationExpression;
     }
 
     /**
      * 錾ĂϐĂ邩ǂԂ
      * 
      * @return 錾ĂϐĂtrue
      */
     public boolean isInitialized() {
         return !(this.initializationExpression instanceof EmptyExpressionInfo);
     }
 
     @Override
     public Set<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> getVariableUsages() {
         final Set<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> usages = new TreeSet<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>>();
 
         usages.add(this.variableDeclaration);
         if (this.isInitialized()) {
             usages.addAll(this.getInitializationExpression().getVariableUsages());
         }
 
         return Collections.unmodifiableSet(usages);
     }
 
     /**
      * `ꂽϐSetԂ
      * 
      * @return `ꂽϐSet
      */
     @Override
     public Set<VariableInfo<? extends UnitInfo>> getDefinedVariables() {
         final Set<VariableInfo<? extends UnitInfo>> definedVariables = new HashSet<VariableInfo<? extends UnitInfo>>();
         definedVariables.add(this.getDeclaredLocalVariable());
         return Collections.unmodifiableSet(definedVariables);
     }
 
     /**
      * ĂяoSetԂ
      * 
      * @return ĂяoSet
      */
     @Override
     public Set<CallInfo<?>> getCalls() {
         return this.isInitialized() ? this.getInitializationExpression().getCalls()
                 : CallInfo.EmptySet;
     }
 
     /**
      * ̕ϐ錾̃eLXg\iString^jԂ
      * 
      * @return ̕ϐ錾̃eLXg\iString^j
      */
     @Override
     public String getText() {
 
         final StringBuilder sb = new StringBuilder();
 
         final LocalVariableInfo variable = this.getDeclaredLocalVariable();
         final TypeInfo type = variable.getType();
         sb.append(type.getTypeName());
 
         sb.append(" ");
 
         sb.append(variable.getName());
 
         if (this.isInitialized()) {
 
             sb.append(" = ");
             final ExpressionInfo expression = this.getInitializationExpression();
             sb.append(expression.getText());
         }
 
         sb.append(";");
 
         return sb.toString();
     }
 
     /**
      * 錾Ăϐ̌^Ԃ
      * @return 錾Ăϐ̌^
      */
     public TypeInfo getType() {
         return this.variableDeclaration.getType();
     }
 
     /**
      * 錾Ăϐ\tB[h
      */
     private final LocalVariableUsageInfo variableDeclaration;
 
     /**
      * 錾Ăϐ̏\tB[h
      */
     private final ExpressionInfo initializationExpression;
 
 }
