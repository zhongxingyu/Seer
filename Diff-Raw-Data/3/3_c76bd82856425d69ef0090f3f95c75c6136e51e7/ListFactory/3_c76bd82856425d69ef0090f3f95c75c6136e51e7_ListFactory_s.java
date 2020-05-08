 /*
  * Created on Nov 30, 2005
  */
 package uk.org.ponder.springutil;
 
 import java.util.List;
 
 import org.springframework.beans.factory.FactoryBean;

 public class ListFactory implements FactoryBean {
   private List list;
 
   public Object getObject() throws Exception {
     return list;
   }
 
   public Class getObjectType() {
     return List.class;
   }
 
   public boolean isSingleton() {
     return true;
   }
 
   public void setList(List list) {
     this.list = list;
   }
 }
