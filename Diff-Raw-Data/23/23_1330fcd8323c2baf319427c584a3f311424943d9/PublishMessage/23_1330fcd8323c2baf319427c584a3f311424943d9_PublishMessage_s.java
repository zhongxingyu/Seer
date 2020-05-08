 package de.innoaccel.wamp.server.message;
 
 import java.util.List;
 
 public class PublishMessage implements Message
 {
     private String topicURI;
 
     private Object event;
 
     private boolean excludeMe;
 
     private List<String> exclude;
 
    private List<String> eligable;
 
     public PublishMessage(String topicURI, Object event)
     {
         this(topicURI, event, false);
     }
 
     public PublishMessage(String topicURI, Object event, boolean excludeMe)
     {
         this(topicURI, event, excludeMe, null, null);
     }
 
    public PublishMessage(String topicURI, Object event, List<String> exclude, List<String> eligable)
     {
        this(topicURI, event, false, exclude, eligable);
     }
 
    private PublishMessage(String topicURI, Object event, boolean excludeMe, List<String> exclude, List<String> eligable)
     {
         this.topicURI = topicURI;
         this.event = event;
         this.excludeMe = excludeMe;
         this.exclude = exclude;
        this.eligable = eligable;
     }
 
     @Override
     public int getMessageCode()
     {
         return Message.PUBLISH;
     }
 
     public String getTopicURI()
     {
         return this.topicURI;
     }
 
     public Object getEvent()
     {
         return event;
     }
 
     public boolean excludeMe()
     {
         return excludeMe;
     }
 
     public List<String> getExclude()
     {
         return exclude;
     }
 
    public List<String> getEligable()
     {
        return eligable;
     }
 
     public boolean isTargeted()
     {
        return (null != this.exclude || null != this.eligable);
     }
 }
