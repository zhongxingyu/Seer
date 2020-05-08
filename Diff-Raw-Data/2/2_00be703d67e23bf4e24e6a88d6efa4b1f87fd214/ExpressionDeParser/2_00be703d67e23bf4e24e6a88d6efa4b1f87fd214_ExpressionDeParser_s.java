 package net.sf.jsqlparser.util.deparser;
 
 import java.util.Iterator;
 import java.util.List;
 
 import net.sf.jsqlparser.expression.AllComparisonExpression;
 import net.sf.jsqlparser.expression.AnyComparisonExpression;
 import net.sf.jsqlparser.expression.BinaryExpression;
 import net.sf.jsqlparser.expression.CaseExpression;
 import net.sf.jsqlparser.expression.DateValue;
 import net.sf.jsqlparser.expression.DoubleValue;
 import net.sf.jsqlparser.expression.Expression;
 import net.sf.jsqlparser.expression.ExpressionVisitor;
 import net.sf.jsqlparser.expression.Function;
 import net.sf.jsqlparser.expression.InverseExpression;
 import net.sf.jsqlparser.expression.JdbcParameter;
 import net.sf.jsqlparser.expression.LongValue;
 import net.sf.jsqlparser.expression.NullValue;
 import net.sf.jsqlparser.expression.Parenthesis;
 import net.sf.jsqlparser.expression.Relation;
 import net.sf.jsqlparser.expression.SimilarColumn;
 import net.sf.jsqlparser.expression.Similarity;
 import net.sf.jsqlparser.expression.StringValue;
 import net.sf.jsqlparser.expression.TimeValue;
 import net.sf.jsqlparser.expression.TimestampValue;
 import net.sf.jsqlparser.expression.WhenClause;
 import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
 import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
 import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
 import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
 import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
 import net.sf.jsqlparser.expression.operators.arithmetic.Division;
 import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
 import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
 import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
 import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
 import net.sf.jsqlparser.expression.operators.relational.Between;
 import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
 import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
 import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
 import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
 import net.sf.jsqlparser.expression.operators.relational.InExpression;
 import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
 import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
 import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
 import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
 import net.sf.jsqlparser.expression.operators.relational.Matches;
 import net.sf.jsqlparser.expression.operators.relational.MinorThan;
 import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
 import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
 import net.sf.jsqlparser.schema.Column;
 import net.sf.jsqlparser.statement.select.SelectVisitor;
 import net.sf.jsqlparser.statement.select.SubSelect;
 
 /**
  * A class to de-parse (that is, tranform from JSqlParser hierarchy into a string)
  * an {@link net.sf.jsqlparser.expression.Expression}
  */
 public class ExpressionDeParser implements ExpressionVisitor, ItemsListVisitor {
 
     protected StringBuffer buffer;
     protected SelectVisitor selectVisitor;
     protected boolean useBracketsInExprList = true;
 
     public ExpressionDeParser() {
     }
 
     public void setUseBracketsInExprList(boolean useBracketsInExprList) {
         this.useBracketsInExprList = useBracketsInExprList;
     }
 
     public boolean getUseBracketsInExprList() {
         return this.useBracketsInExprList;
     }
 
     /**
      * @param selectVisitor a SelectVisitor to de-parse SubSelects. It has to share the same<br>
      * StringBuffer as this object in order to work, as:
      * <pre>
      * <code>
      * StringBuffer myBuf = new StringBuffer();
      * MySelectDeparser selectDeparser = new  MySelectDeparser();
      * selectDeparser.setBuffer(myBuf);
      * ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeparser, myBuf);
      * </code>
      * </pre>
      * @param buffer the buffer that will be filled with the expression
      */
     public ExpressionDeParser(SelectVisitor selectVisitor, StringBuffer buffer) {
         this.selectVisitor = selectVisitor;
         this.buffer = buffer;
     }
 
     public StringBuffer getBuffer() {
         return buffer;
     }
 
     public void setBuffer(StringBuffer buffer) {
         this.buffer = buffer;
     }
 
     public void visit(Addition addition) {
         visitBinaryExpression(addition, " + ");
     }
 
     public void visit(AndExpression andExpression) {
         visitBinaryExpression(andExpression, " AND ");
     }
 
     public void visit(Between between) {
         try {
             between.getLeftExpression().accept(this);
         } catch (Exception e) {
         }
         if (between.isNot()) {
             buffer.append(" NOT");
         }
 
         buffer.append(" BETWEEN ");
         try {
             between.getBetweenExpressionStart().accept(this);
         } catch (Exception e) {
         }
         buffer.append(" AND ");
         try {
             between.getBetweenExpressionEnd().accept(this);
         } catch (Exception e) {
         }
 
     }
 
     public void visit(Division division) {
         visitBinaryExpression(division, " / ");
 
     }
 
     public void visit(DoubleValue doubleValue) {
         buffer.append(doubleValue.getValue());
 
     }
 
     public void visit(EqualsTo equalsTo) {
         visitBinaryExpression(equalsTo, " = ");
     }
 
     public void visit(GreaterThan greaterThan) {
         visitBinaryExpression(greaterThan, " > ");
     }
 
     public void visit(GreaterThanEquals greaterThanEquals) {
         visitBinaryExpression(greaterThanEquals, " >= ");
 
     }
 
     public void visit(InExpression inExpression) {
 
         try {
             inExpression.getLeftExpression().accept(this);
         } catch (Exception e) {
         }
         if (inExpression.isNot()) {
             buffer.append(" NOT");
         }
         buffer.append(" IN ");
 
         try {
             inExpression.getItemsList().accept(this);
         } catch (Exception e) {
         }
     }
 
     public void visit(InverseExpression inverseExpression) {
         buffer.append("-");
         try {
             inverseExpression.getExpression().accept(this);
         } catch (Exception e) {
         }
     }
 
     public void visit(IsNullExpression isNullExpression) {
         try {
             isNullExpression.getLeftExpression().accept(this);
         } catch (Exception e) {
         }
         if (isNullExpression.isNot()) {
             buffer.append(" IS NOT NULL");
         } else {
             buffer.append(" IS NULL");
         }
     }
 
     public void visit(JdbcParameter jdbcParameter) {
         buffer.append("?");
 
     }
 
     public void visit(LikeExpression likeExpression) {
         visitBinaryExpression(likeExpression, " LIKE ");
 
     }
 
     public void visit(ExistsExpression existsExpression) {
         if (existsExpression.isNot()) {
             buffer.append(" NOT EXISTS ");
         } else {
             buffer.append(" EXISTS ");
         }
         try {
             existsExpression.getRightExpression().accept(this);
         } catch (Exception e) {
         }
     }
 
     public void visit(LongValue longValue) {
         buffer.append(longValue.getStringValue());
 
     }
 
     public void visit(MinorThan minorThan) {
         visitBinaryExpression(minorThan, " < ");
 
     }
 
     public void visit(MinorThanEquals minorThanEquals) {
         visitBinaryExpression(minorThanEquals, " <= ");
 
     }
 
     public void visit(Multiplication multiplication) {
         visitBinaryExpression(multiplication, " * ");
 
     }
 
     public void visit(NotEqualsTo notEqualsTo) {
         visitBinaryExpression(notEqualsTo, " <> ");
 
     }
 
     public void visit(NullValue nullValue) {
         buffer.append("NULL");
 
     }
 
     public void visit(OrExpression orExpression) {
         visitBinaryExpression(orExpression, " OR ");
 
     }
 
     public void visit(Parenthesis parenthesis) {
         if (parenthesis.isNot()) {
             buffer.append(" NOT ");
         }
 
         buffer.append("(");
         try {
             parenthesis.getExpression().accept(this);
         } catch (Exception e) {
         }
         buffer.append(")");
 
     }
 
     public void visit(StringValue stringValue) {
         buffer.append("'" + stringValue.getValue() + "'");
 
     }
 
     public void visit(Subtraction subtraction) {
         visitBinaryExpression(subtraction, "-");
 
     }
 
     private void visitBinaryExpression(BinaryExpression binaryExpression, String operator) {
         if (binaryExpression.isNot()) {
             buffer.append(" NOT ");
         }
         try {
             binaryExpression.getLeftExpression().accept(this);
         } catch (Exception e) {
         }
         buffer.append(operator);
         try {
             binaryExpression.getRightExpression().accept(this);
         } catch (Exception e) {
         }
 
     }
 
     public void visit(SubSelect subSelect) {
         buffer.append("(");
         try {
             subSelect.getSelectBody().accept(selectVisitor);
         } catch (Exception e) {
         }
         buffer.append(")");
     }
 
     public void visit(Column tableColumn) {
         String tableName = tableColumn.getTable().getWholeTableName();
         if (tableName != null) {
             buffer.append(tableName + ".");
         }
 
         buffer.append(tableColumn.getColumnName());
     }
 
     public void visit(Function function) {
         if (function.isEscaped()) {
             buffer.append("{fn ");
         }
 
         buffer.append(function.getName());
         if (function.isAllColumns()) {
             buffer.append("(*)");
         } else if (function.getParameters() == null) {
             buffer.append("()");
         } else {
             boolean oldUseBracketsInExprList = useBracketsInExprList;
             if (function.isDistinct()) {
                 useBracketsInExprList = false;
                 buffer.append("(DISTINCT ");
             }
             visit(function.getParameters());
             useBracketsInExprList = oldUseBracketsInExprList;
             if (function.isDistinct()) {
                 buffer.append(")");
             }
         }
 
         if (function.isEscaped()) {
             buffer.append("}");
         }
 
     }
 
     public void visit(ExpressionList expressionList) {
         if (useBracketsInExprList) {
             buffer.append("(");
         }
         for (Iterator iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
             Expression expression = (Expression) iter.next();
             try {
                 expression.accept(this);
             } catch (Exception e) {
             }
             if (iter.hasNext()) {
                 buffer.append(", ");
             }
         }
         if (useBracketsInExprList) {
             buffer.append(")");
         }
     }
 
     public SelectVisitor getSelectVisitor() {
         return selectVisitor;
     }
 
     public void setSelectVisitor(SelectVisitor visitor) {
         selectVisitor = visitor;
     }
 
     public void visit(DateValue dateValue) {
         buffer.append("{d '" + dateValue.getValue().toString() + "'}");
     }
 
     public void visit(TimestampValue timestampValue) {
         buffer.append("{ts '" + timestampValue.getValue().toString() + "'}");
     }
 
     public void visit(TimeValue timeValue) {
         buffer.append("{t '" + timeValue.getValue().toString() + "'}");
     }
 
     public void visit(CaseExpression caseExpression) {
         buffer.append("CASE ");
         Expression switchExp = caseExpression.getSwitchExpression();
         if (switchExp != null) {
             try {
                 switchExp.accept(this);
             } catch (Exception e) {
             }
         }
 
         List clauses = caseExpression.getWhenClauses();
         for (Iterator iter = clauses.iterator(); iter.hasNext();) {
             Expression exp = (Expression) iter.next();
             try {
                 exp.accept(this);
             } catch (Exception e) {
             }
         }
 
         Expression elseExp = caseExpression.getElseExpression();
         if (elseExp != null) {
             try {
                 elseExp.accept(this);
             } catch (Exception e) {
             }
         }
 
         buffer.append(" END");
     }
 
     public void visit(WhenClause whenClause) {
         buffer.append(" WHEN ");
         try {
             whenClause.getWhenExpression().accept(this);
         } catch (Exception e) {
         }
         buffer.append(" THEN ");
         try {
             whenClause.getThenExpression().accept(this);
         } catch (Exception e) {
         }
     }
 
     public void visit(AllComparisonExpression allComparisonExpression) {
         buffer.append(" ALL ");
         try {
             allComparisonExpression.GetSubSelect().accept((ExpressionVisitor) this);
         } catch (Exception e) {
         }
     }
 
     public void visit(AnyComparisonExpression anyComparisonExpression) {
         buffer.append(" ANY ");
         try {
             anyComparisonExpression.GetSubSelect().accept((ExpressionVisitor) this);
         } catch (Exception e) {
         }
     }
 
     public void visit(Concat concat) {
         visitBinaryExpression(concat, " || ");
     }
 
     public void visit(Matches matches) {
         visitBinaryExpression(matches, " @@ ");
     }
 
     public void visit(BitwiseAnd bitwiseAnd) {
         visitBinaryExpression(bitwiseAnd, " & ");
     }
 
     public void visit(BitwiseOr bitwiseOr) {
         visitBinaryExpression(bitwiseOr, " | ");
     }
 
     public void visit(BitwiseXor bitwiseXor) {
         visitBinaryExpression(bitwiseXor, " ^ ");
     }
 
     public void visit(SimilarColumn similarColumn) {
         //sql doesn't accept FUZZY syntax
         //buffer.append(" SIMILAR ");
         try{
             similarColumn.getColumn().accept(this);
         }catch(Exception e) {}
     }
 
     public void visit(Relation relation) {
         buffer.append("(");
         try{
             relation.getLabel1().accept(this);
         }catch(Exception e) {}
         buffer.append(",");
         try{
             relation.getLabel2().accept(this);
         }catch(Exception e) {}
         buffer.append(")");
     }
 
     public void visit(Similarity similarity) {
         buffer.append("(");
         try{
             similarity.getLabel1().accept(this);
         }catch(Exception e) {}
         buffer.append(",");
         try{
             similarity.getLabel2().accept(this);
         }catch(Exception e) {}
         buffer.append(") / ");
         try{
             similarity.getValue().accept(this);
         }catch(Exception e) {}
     }
 }
