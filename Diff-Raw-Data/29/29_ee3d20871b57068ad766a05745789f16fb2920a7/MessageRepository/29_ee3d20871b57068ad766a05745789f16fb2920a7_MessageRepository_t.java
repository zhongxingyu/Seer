 package cache;
 
 public interface MessageRepository {
 
    public String getTranslation(String key);
 
    public Message getMessage(String key);

    public void save(Message message);
 
 }
