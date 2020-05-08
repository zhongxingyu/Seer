 package cache;
 
 public interface MessageRepository {
 
    public abstract Message getMessage(String key);
 
    public abstract void save(Message message);
 
 }
