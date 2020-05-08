 package ch17.ex17_04;
 
 import java.awt.RenderingHints.Key;
 import java.lang.ref.PhantomReference;
 import java.lang.ref.Reference;
 import java.lang.ref.ReferenceQueue;
 import java.util.HashMap;
 import java.util.Map;
 
 public final class ResourceManager {
 	final ReferenceQueue<Object> queue;
 	final Map<Reference<?>, Resource> refs;
 	final Thread reaper;
 	boolean shutdown = false;
 	
 	public ResourceManager(){
 		queue = new ReferenceQueue<Object>();
 		refs = new HashMap<Reference<?>, Resource>();
 		reaper = new ReaperThread();
 		reaper.start();
 	}
 	
 	public synchronized void shutdown(){
 		if(!shutdown){
 			shutdown = true;
 		}
 	}
 	
 	public synchronized Resource getResource(Object key){
 		if(shutdown)
 			throw new IllegalStateException();
 		Resource res = new ResourceImple(key);
 		Reference<?> ref = new PhantomReference<Object>(key, queue);
 		refs.put(ref, res);
 		return res;
 	}
 	
 	class ReaperThread extends Thread {
 		public void run(){
 			while(true){
 //				try{					
					Reference<?> ref = queue.poll();
					if(ref==null && shutdown==true){
						break;
					}else if(ref==null){
 						continue;
 					}
 					Resource res = null;
					synchronized(ResourceManager.this){
						res = refs.get(ref);
						refs.remove(ref);
 					}
					res.release();
					ref.clear();
					System.out.println("##remove ref::"+ref.toString()+" res::"+res.toString());
 //				}
 			}
 		}
 	}
 	
 	public static void main(String[] args) {
 		ResourceManager manager = new ResourceManager();
 		Object key = new Object();
 		Resource res = manager.getResource(key);
 		res.use(key, new Object());
 		
 		Resource res2 = manager.getResource(key);
 		res2.use(key, new Object());
 		
 		//stop repear thread
 		System.out.println("shutdown");
 		manager.shutdown();
 		
 		//add key's ref to queue
 		key = null;
 		Runtime.getRuntime().gc();
 	}
 }
