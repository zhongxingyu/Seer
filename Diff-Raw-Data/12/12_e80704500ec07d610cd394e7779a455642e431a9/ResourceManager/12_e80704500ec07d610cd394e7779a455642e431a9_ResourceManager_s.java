 package ch17.ex17_04;
 
 import java.lang.ref.*;
 import java.util.*;
 
 public final class ResourceManager {
 
   final ReferenceQueue<Object> queue;
   final Map<Reference<?>, Resource> refs;
   final Thread reaper;
   boolean shutdown = false;
 
   public ResourceManager() {
     queue = new ReferenceQueue<Object>();
     refs = new HashMap<Reference<?>, Resource>();
     reaper = new ReaperThread();
     reaper.start();
 
     // ... リソースの初期化 ...
   }
 
   public synchronized void shutdown() {
     if (!shutdown) {
       shutdown = true;
       reaper.interrupt();
     }
   }
 
   public synchronized Resource getResource(Object key) {
     if (shutdown)
       throw new IllegalStateException();
     Resource res = new ResourceImpl(key);
     Reference<?> ref = new PhantomReference<Object>(key, queue);
     refs.put(ref, res);
     return res;
   }
 
   private static class ResourceImpl implements Resource {
     WeakReference<Object> key;
 
     boolean needsRelease = false;
 
     ResourceImpl(Object key) {
       this.key = new WeakReference<Object>(key);
 
       // .. 外部リソースの設定
 
       needsRelease = true;
     }
 
     @Override
     public void use(Object key, Object...args) {
       Object myKey = this.key.get();
       if (myKey != null && myKey == key)
         throw new IllegalArgumentException("wrong key");
 
       // ... リソースの使用 ...
     }
 
     @Override
     public synchronized void release() {
       if (needsRelease) {
         needsRelease = false;
 
         // .. リソースの解放 ...
       }
     }
   }
 
   class ReaperThread extends Thread {
     @Override
     public void run() {
       // 割り込まれるまで実行
       while (true) {
         try {
           Reference<?> ref = queue.remove();
           Resource res = null;
           synchronized(ResourceManager.this) {
           res = refs.get(ref);
           refs.remove(ref);
           }
           res.release();
           ref.clear();
         } catch (InterruptedException ex) {
           break; // 全て終了
         } finally {
           Reference<?> ref = null;
           while ((ref = queue.poll()) != null) {
             Resource res = null;
             synchronized(ResourceManager.this) {
              res = refs.get(ref);
              refs.remove(ref);
             }
             res.release();
             ref.clear();
           }
         }
       }
     }
   }
 }
