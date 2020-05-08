 /*
  * Created on Nov 30, 2005
  */
 package uk.org.ponder.springutil;
 
import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.FactoryBean;

/** A utility factory, useful for being the target of a TLAB list, where this might
 * need to cross scopes, for example.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */

 public class ListFactory implements FactoryBean {
   private List list;
 
   public Object getObject() throws Exception {
    return list == null? new ArrayList() : list;
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
