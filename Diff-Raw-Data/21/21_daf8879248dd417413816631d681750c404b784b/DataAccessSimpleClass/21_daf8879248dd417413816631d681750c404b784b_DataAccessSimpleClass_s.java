 package jpaoletti.jpm.test;
 
 import java.util.ArrayList;
import jpaoletti.jpm.test.SimpleClass;
 
 /**
  *
  * @author jpaoletti
  */
 public class DataAccessSimpleClass extends DataAccessTest {
 
     @Override
     protected void fill() {
         list = new ArrayList<Object>();
         int top = random(5, 30);
         for (int i = 0; i < top; i++) {
             SimpleClass o = new SimpleClass();
             o.setId(new Long(i));
             o.setDescription(String.format("Simple Class %d", i));
             list.add(o);
         }
     }
 }
