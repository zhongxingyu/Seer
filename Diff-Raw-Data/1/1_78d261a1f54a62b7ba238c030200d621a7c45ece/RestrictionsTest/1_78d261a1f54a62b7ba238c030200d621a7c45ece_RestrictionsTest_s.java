 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lightframework.data.criterion;
 
 import org.hamcrest.core.*;
 import org.junit.*;
 import org.junit.Assert;
 
 import lightframework.data.criterion.operands.*;
 
 /**
  *
  * @author Tom Deng <xianrendzw@hotmail.com>
  */
 public class RestrictionsTest {
     
     public RestrictionsTest(){
     }
 
     @Test
     public void equalOperand() {
         Assert.assertThat(new EqualOperand("columnName", "'columnValue'").toString(),
                 IsEqual.equalTo("columnName = 'columnValue' "));
     }
 
     @Test
     public void betweenOperand() {
         String expr = "columnName BETWEEN columnValue1 AND columnValue2 ";
         Assert.assertThat(new BetweenOperand("columnName", "columnValue1", "columnValue2").toString(), IsEqual.equalTo(expr));
     }
 
     @Test
     public void greaterThanOperand() {
         String expr = "columnName > columnValue ";
         Assert.assertThat(new GreaterThanOperand("columnName", "columnValue").toString(), IsEqual.equalTo(expr));
     }
 
     @Test
     public void greaterThanOrEqualOperand() {
         String expr = "columnName >= columnValue ";
         Assert.assertThat(new GreaterThanOrEqualOperand("columnName", "columnValue").toString(), IsEqual.equalTo(expr));
     }
 
     @Test
     public void inOperand() {
         String expr = "columnName IN ('v1','v2','v3') ";
         Assert.assertThat(new InOperand("columnName", "'v1','v2','v3'").toString(), IsEqual.equalTo(expr));
     }
 
     @Test
     public void lessThanOperand() {
         String expr = "columnName < columnValue ";
         Assert.assertThat(new LessThanOperand("columnName", "columnValue").toString(), IsEqual.equalTo(expr));
     }
 
     @Test
     public void lessThanOrEqualOperand() {
         String expr = "columnName <= columnValue ";
         Assert.assertThat(new LessThanOrEqualOperand("columnName", "columnValue").toString(), IsEqual.equalTo(expr));
     }
 
     @Test
     public void likeOperand() {
         String expr = "columnName like '%columnValue%' ";
         Assert.assertThat(new LikeOperand("columnName", "%columnValue%").toString(), IsEqual.equalTo(expr));
     }
 
     @Test
     public void notEqualOperand() {
         String expr = "columnName <> columnValue ";
         Assert.assertThat(new NotEqualOperand("columnName", "columnValue").toString(), IsEqual.equalTo(expr));
     }
 
     @Test
     public void notInOperand() {
         String expr = "columnName NOT IN ('v1','v2','v3') ";
         Assert.assertThat(new NotInOperand("columnName", "'v1','v2','v3'").toString(), IsEqual.equalTo(expr));
     }
 
     @Test
     public void notLikeOperand() {
         String expr = "columnName NOT LIKE '%columnValue%' ";
         Assert.assertThat(new NotLikeOperand("columnName", "%columnValue%").toString(), IsEqual.equalTo(expr));
     }
 
     @Test
     public void compositeOperand() {
         String sqlCondition = "Where Name = TomDeng  AND Age = 29  AND (Weight BETWEEN 100 AND 180  AND Salary < 20w )";
         Operand operand = Restrictions.clause(SqlClause.Where)
                 .append(Restrictions.equal("Name", "TomDeng"))
                 .append(Restrictions.And)
                 .append(Restrictions.equal("Age", 29))
                 .append(Restrictions.And)
                 .append(Restrictions.bracket(Bracket.Left))
                 .append(Restrictions.between("Weight", 100, 180))
                 .append(Restrictions.And)
                 .append(Restrictions.lessThan("Salary", "20w"))
                 .append(Restrictions.bracket(Bracket.Rgiht));
 
         Assert.assertThat(operand.toString(), IsEqual.equalTo(sqlCondition));
     }
 
     @Test
     public void nestedCompositeOperand() {
         String sqlCondition = "Where Name = TomDeng  AND Age = 29  AND (Weight BETWEEN 100 AND 180  AND Salary < 20w )";
         Operand operand = Restrictions.clause(SqlClause.Where)
                 .append(Restrictions.equal("Name", "TomDeng"))
                 .append(Restrictions.And)
                 .append(Restrictions.equal("Age", 29)
                     .append(Restrictions.And)
                     .append(Restrictions.bracket(Bracket.Left))
                     .append(Restrictions.between("Weight", 100, 180))
                     .append(Restrictions.And)
                     .append(Restrictions.lessThan("Salary", "20w"))
                     .append(Restrictions.bracket(Bracket.Rgiht)));
 
         Assert.assertThat(operand.toString(), IsEqual.equalTo(sqlCondition));
     }
 }
