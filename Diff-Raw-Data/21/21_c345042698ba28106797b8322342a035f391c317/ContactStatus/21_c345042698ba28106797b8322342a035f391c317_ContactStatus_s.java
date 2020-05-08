 package ch9k.chat;
 
 import ch9k.configuration.Persistable;
 import ch9k.configuration.PersistentDataObject;
 import org.jdom.Element;
 
 public class ContactStatus implements Persistable {
     public enum Status {
         ONLINE,
         OFFLINE,
         REQUESTED,
         IGNORED,
         BLOCKED;
     }
     
     private Status status;
     private String text;
     
     public ContactStatus() {
         status = Status.OFFLINE;
         text = "";
     }
     
     public ContactStatus(Status status,String text) {
         this.status = status;
         this.text = text;
     }
     
     public ContactStatus(PersistentDataObject dataObject) {
        this();
        if(dataObject != null) {
            load(dataObject);
        }
     }
 
     @Override
     public PersistentDataObject persist() {
         Element el = new Element("status");
        el.setAttribute("type", status.name());
         el.setText(text);
         return new PersistentDataObject(el);
     }
 
     @Override
     public void load(PersistentDataObject object) {
         Element el = object.getElement();
         status = Status.valueOf(el.getAttributeValue("status"));
         text = el.getValue();
     }
     
     public String getText() {
         return text;
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     public boolean isBlocked() {
         return status == Status.BLOCKED;
     }
 
     public boolean isRequested() {
         return status == Status.REQUESTED;
     }
 
     public boolean isOnline() {
         return status == Status.ONLINE;
     }
     
     public boolean isOffline() {
         return status == Status.OFFLINE;
     }
     
     public boolean isIgnored() {
         return status == Status.IGNORED;
     }
     
     public Status getStatus() {
         return status;
     }
 
     public void setStatus(Status status) {
         this.status = status;
     }
 }
