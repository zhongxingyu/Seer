 package directi.androidteam.training.StanzaStore;
 
 import directi.androidteam.training.TagStore.IQTag;
 import directi.androidteam.training.TagStore.Tag;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 9/3/12
  * Time: 2:02 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RosterResult extends TagWrapper {
     public RosterResult(Tag tag) {
         this.tag = new IQTag(tag);
     }
     public boolean isError() {
         String attr = tag.getAttribute("type");
         if(attr==null || attr.equals("error"))
             return true;
         else return false;
     }
     public ArrayList<Tag> getListOfRosters(){
         if (isError() || !tag.getAttribute("type").equals("result"))
             return null;
        return ((IQTag)tag).getRosterItems();
     }
 }
