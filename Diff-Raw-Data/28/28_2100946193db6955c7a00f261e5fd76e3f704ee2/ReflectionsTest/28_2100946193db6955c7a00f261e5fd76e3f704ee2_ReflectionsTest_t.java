 package net.sparkmuse.common;
 
 import play.test.UnitTest;
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 import static org.hamcrest.Matchers.*;
 import org.hamcrest.MatcherAssert;
 
 import java.util.Date;
import java.util.Map;
 import java.lang.reflect.Field;
 
 /**
  * @author neteller
  * @created: Jan 16, 2011
  */
 public class ReflectionsTest extends UnitTest {
 
   @Test
   public void shouldCopyAllValues() {
     final A existing = new A();
     final A newObject = new A();
     newObject.setStrProperty("Hello");
     newObject.setDateProperty(new Date());
     newObject.setIntProperty(10);
 
     final A a = Reflections.overlay(existing, newObject);
     
     MatcherAssert.assertThat(a.getStrProperty(), equalTo("Hello"));
     MatcherAssert.assertThat(a.getIntProperty(), equalTo(10));
     MatcherAssert.assertThat(a.getDateProperty(), notNullValue());
   }
 
   @Test
   public void shouldCopySelectedValues() {
     final A existing = new A();
     final A newObject = new A();
     newObject.setStrProperty("Hello");
     newObject.setDateProperty(new Date());
     newObject.setIntProperty(10);
 
     final A a = Reflections.overlay(existing, newObject, "intProperty");
 
     MatcherAssert.assertThat(a.getStrProperty(), nullValue());
     MatcherAssert.assertThat(a.getIntProperty(), equalTo(10));
     MatcherAssert.assertThat(a.getDateProperty(), nullValue());
   }
 
   private static class A {
     private String strProperty;
     private Integer intProperty;
     private Date dateProperty;
 
     public String getStrProperty() {
       return strProperty;
     }
 
     public void setStrProperty(String strProperty) {
       this.strProperty = strProperty;
     }
 
     public Integer getIntProperty() {
       return intProperty;
     }
 
     public void setIntProperty(Integer intProperty) {
       this.intProperty = intProperty;
     }
 
     public Date getDateProperty() {
       return dateProperty;
     }
 
     public void setDateProperty(Date dateProperty) {
       this.dateProperty = dateProperty;
     }
   }
 
 }
