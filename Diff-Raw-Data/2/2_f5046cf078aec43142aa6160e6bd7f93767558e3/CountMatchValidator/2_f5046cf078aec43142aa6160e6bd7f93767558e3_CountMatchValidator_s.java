 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dbtester;
 
 import com.bul7.exception.ValidationException;
 
 /**
  *
  * @author ivan
  */
 public class CountMatchValidator extends Validator {
 
     private String expect;
     private String actual;
 
     public String getActual() {
         return actual;
     }
 
     public void setActual(String actual) {
         this.actual = actual;
     }
 
     public String getExpect() {
         return expect;
     }
 
     public void setExpect(String expect) {
         this.expect = expect;
     }
 
     public void validate() throws ValidationException {
         if (actual == null || actual.trim().length() == 0) {
             throw new ValidationException("'actual' source not set or is empty.");
         }
         if (expect == null || expect.trim().length() == 0) {
             throw new ValidationException("'expect' source not set or is empty.");
         }
     }
 
     @Override
     protected String doGetSql() {
         TestCase tc = getTestCase();
         String expectSource = makeSource(expect);
         String actualSource = makeSource(actual);
         
         
         String sql = 
 "SELECT " + "\n" +
"   CAST('Expected ' || TRIM(e.num_rows) || ' rows, actual ' || TRIM(a.num_rows) AS VARCHAR(255)) AS message" + "\n" +
 "FROM " + "\n" +
 "   (SELECT COUNT(*) AS num_rows FROM " + expectSource + ") e" + "\n" +
 "JOIN" + "\n" +
 "   (SELECT COUNT(*) AS num_rows FROM " + actualSource + ") a ON (1=1) " + "\n" +
 "WHERE" + "\n" +
 "	e.num_rows <> a.num_rows\n";
         return sql;
     }
    
     @Override
     public String toString() {
         return "CountMatchValidator{" + "expect=" + expect + ", actual=" + actual + '}' + super.toString();
     }
 }
