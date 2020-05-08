 package ru.finam.bustard;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Multimap;
 
 import java.util.Map;
 
 public abstract class AbstractBustard implements Bustard {
 
     private final Config config;
     private final Executor defaultExecutor;
     private final Multimap<String, Object> subscribers;
     private final Map<String, Object> savedEvents;
 
    private ErrorListener errorListener;

     public AbstractBustard(Executor defaultExecutor,
                            Multimap<String, Object> subscribersMap,
                            Map<String, Object> eventsMap) {
         Preconditions.checkNotNull(defaultExecutor, "defaultExecutor");
         Preconditions.checkNotNull(subscribersMap, "subscribersMap");
         Preconditions.checkNotNull(eventsMap, "eventsMap");
 
         this.defaultExecutor = defaultExecutor;
         this.subscribers = subscribersMap;
         this.savedEvents = eventsMap;
         this.config = new Config();
     }
 
     @Override
     public void attachExecutors(Executor... executors) {
         config.attachExecutors(executors);
     }
 
    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

     @Override
     public <T> Channel<T> getChannelFor(final String key) {
         return new Channel<T>() {
             @Override
             public void post(T event) {
                 AbstractBustard.this.post(key, event);
             }
         };
     }
 
     protected abstract void initialize(Config config);
 
     protected abstract void post(Object subscriber, Object event, String key) throws Throwable;
 
     @Override
     public void initialize() {
         initialize(config);
     }
 
     @Override
     public void subscribe(Object subscriber) {
         for (String key : config.findEventTypesFor(subscriber.getClass())) {
             subscribers.put(key, subscriber);
             if (config.isEventOnBinding(subscriber.getClass(), key)) {
                 Object event = savedEvents.get(key);
                 if (event != null) {
                     postToSubscriber(subscriber, key, event);
                 }
             }
         }
     }
 
     @Override
     public void unsubscribe(Object subscriber) {
         for (String key : config.findEventTypesFor(subscriber.getClass())) {
             subscribers.remove(key, subscriber);
         }
     }
 
     protected Executor getExecutorFor(String key, Class<?> subscriber) {
         Executor executor = config.findExecutorFor(subscriber, key);
         return executor != null ? executor : defaultExecutor;
     }
 
     private <T> void post(String key, T event) {
         if (config.needToSave(key)) {
             savedEvents.put(key, event);
         }
 
         for (Object subscriber : subscribers.get(key)) {
             postToSubscriber(subscriber, key, event);
         }
     }
 
     @Override
     public void post(Object event) {
         String key = ChannelKey.get(event.getClass());
         post(key, event);
     }
 
     private void postToSubscriber(Object subscriber, String key, Object event) {
         Executor executor = getExecutorFor(key, subscriber.getClass());
         executor.execute(new PostEvent(subscriber, event, key));
     }
 
 
     private class PostEvent implements Runnable {
 
         private final Object subscriber;
         private final Object event;
         private final String key;
 
         private PostEvent(Object subscriber, Object event, String key) {
             this.subscriber = subscriber;
             this.event = event;
             this.key = key;
         }
 
         @Override
         public void run() {
             try {
                 post(subscriber, event, key);
            } catch (Throwable error) {
                if (errorListener != null) {
                    errorListener.handle(error);
                 }
             }
         }
     }
 }
