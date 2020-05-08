 package ch17.ex03;
 
import java.io.File;
 import java.lang.ref.PhantomReference;
 import java.lang.ref.Reference;
 import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Map;
 
 
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
 	
 	class ReaperThread extends Thread {
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
 				}
 				catch (InterruptedException ex) {
 					break; // すべて終了
 				}
 			}
 		}
 	}
 	
 	private static class ResourceImpl implements Resource {
		WeakReference<Object> storedKey; // keyを直接格納するように変更。
 		boolean needsRelease = false;
 		
 		ResourceImpl(Object key) {
			storedKey = new WeakReference<Object>(key);
 			
 			// .. 外部リソースの設定
 			
 			needsRelease = true;
 		}
 		
 		public void use(Object key, Object... args) {
 			if (storedKey != key)
 				throw new IllegalArgumentException("wrong key");
 			
 			// ... リソースの使用 ...
 		}
 		
 		public synchronized void release() {
 			if (needsRelease) {
 				needsRelease = false;
 				
 				// ..リソースの解放 ...
 			}		
 		}
 	}
 }
