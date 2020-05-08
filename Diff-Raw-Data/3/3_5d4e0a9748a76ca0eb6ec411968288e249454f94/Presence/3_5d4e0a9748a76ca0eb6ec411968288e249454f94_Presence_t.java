 package directi.androidteam.training.TagStore;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 8/31/12
  * Time: 2:09 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Presence extends Tag {
     public Presence() {
         super("presence", null,null,null);
     }
 
     public Presence(Tag tag) {
         super("presence",tag.attributes,tag.childTags,tag.content);
     }
 
     public Presence(String id, String from, String show, String status) {
        this.tagname = "presence";
         this.addAttribute("id", id);
         this.addAttribute("from", from);
         this.addChildTag(new Show(show));
         this.addChildTag(new Status(status));
     }

     public String getShow() {
         Tag show = this.getChildTag("show");
         if (show != null) {
             return show.getContent();
         } else {
             return null;
         }
     }
 
     public void setShow(String show) {
         this.addChildTag(new Show(show));
     }
 
     public String getStatus() {
         Tag status = this.getChildTag("status");
         if (status != null) {
             return status.getContent();
         } else {
             return null;
         }
     }
 
     public void setStatus(String status) {
         this.addChildTag(new Status(status));
     }
 
     public String getType() {
         return this.getAttribute("type");
     }
 
     public String getFrom() {
         return this.getAttribute("from");
     }
 
     public void setTo(String receiver) {
         this.addAttribute("to", receiver);
     }
 
     public void setType(String type) {
         this.addAttribute("type", type);
     }
 }
 
