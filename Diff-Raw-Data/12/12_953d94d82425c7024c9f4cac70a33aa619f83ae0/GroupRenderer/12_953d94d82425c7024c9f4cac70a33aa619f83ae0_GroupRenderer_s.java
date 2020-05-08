 package amu.licence.edt.view.renderers;
 
 import amu.licence.edt.model.beans.Group;
 
 public class GroupRenderer implements StrRenderer {
 
     @Override
     public String getStrRender(Object obj) {
         try {
             Group group = (Group) obj;
            return "G"+group.getId();   // maby find a better display
         } catch (ClassCastException cce) {
             System.err.println("error: trying to render a non-Group in GroupRenderer");
             return null;
         }
     }
 
 }
