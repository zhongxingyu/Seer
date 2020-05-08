 package ch17.ex04;
 
 import java.lang.ref.PhantomReference;
 import java.lang.ref.Reference;
 import java.lang.ref.ReferenceQueue;
 import java.lang.ref.SoftReference;
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Map;
 
 
 public final class ResourceManager {
 	final ReferenceQueue<Object> queue;
 	public final Map<Reference<?>, Resource> refs;
 	final Thread reaper;
 	boolean shutdown = false;
 	
 	public ResourceManager() {
 		queue = new ReferenceQueue<Object>();
 		refs = new HashMap<Reference<?>, Resource>();
 		reaper = new ReaperThread();
 		reaper.start();
 	}
 	
 	public synchronized void shutdown() {
 		if (!shutdown) {
 			System.out.println("ShutDown...");
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
 	
 	class ReaperThread extends Thread {
 		public void run() {
 			// refsが空になるまで実行
 			while (!shutdown || !refs.isEmpty()) {
 				Reference<?> ref;
 				try {
					System.out.println("enqueue." + queue.poll());
					System.out.println("remove");
 					ref = queue.remove();
 					Resource res = null;
 					synchronized(ResourceManager.this) {
 						res = refs.remove(ref);
 					}
 					res.release();
 					ref.clear();
 				} catch (InterruptedException e) {
 					// 処理を中断しない
 					System.out.println("interrupted. size:" + refs.size());
 				} 
 				System.out.println("... size:" + refs.size());
 			}
 			System.out.println("end. size: " + refs.size());
 		}
 	}
 	
 	private static class ResourceImpl implements Resource {
 		WeakReference<Object> ref;
 		boolean needsRelease = false;
 		
 		ResourceImpl(Object key) {
 			ref = new WeakReference<Object>(key);
 			needsRelease = true;
 		}
 		
 		public void use(Object key, Object... args) {
 			System.out.println("use.");
 		}
 		
 		public synchronized void release() {
 			if (needsRelease) {
 				needsRelease = false;
 				ref.clear();
 				Runtime.getRuntime().gc();
 				System.out.println("release. key:" + ref.get());
 			}			
 		}
 	}
 }
