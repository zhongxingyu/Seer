 package org.omo.core;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public abstract class AbstractModel<K, V> implements Model<K, V> {
 
 	protected final Log log = LogFactory.getLog(getClass());
 
 	protected final String name;
 	
 	protected final Collection<View<K, V>> views = new ArrayList<View<K, V>>();
 
 	protected final Metadata<K, V> metadata;
 	
 	protected abstract Collection<V> getData();
 	
 	public AbstractModel(String name, Metadata<K, V> metadata) {
 		this.name = name;
 		this.metadata = metadata;
 	}
 	
 	@Override
 	public String getName() {
 		return name;
 	}
 	
 	@Override
 	public Registration<K, V> registerView(View<K, V> view) {
 		log.debug("registering view: " + view);
 		synchronized (views) {
 			views.add(view);
 		}
		return new Registration<K, V>(getMetadata(), getData());
 	}
 	
 	private Metadata<K, V> getMetadata() {
 		return metadata;
 	}
 
 	@Override
 	public boolean deregisterView(View<K, V> view) {
 		boolean success;
 		synchronized (views) {
 			success = views.remove(view);
 		}
 		if (success)
 			log.debug("deregistered view: " + view);
 		else
 			log.warn("failed to deregister view: " + view);
 		
 		return success;
 	}	
 	
 	protected void notifyInsertion(V update) {
 		synchronized (views) {
 			for (View<K, V> view : views)
 				try {
 					view.insert(update);
 				} catch (RuntimeException e) {
 					log.error("view insertion failed: " + view + " <- " + update, e);
 				}
 		}
 	}
 
 	protected void notifyUpdate(V value) {
 		synchronized (views) {
 			for (View<K, V> view : views)
 				try {
 					view.update(null, value);
 				} catch (RuntimeException e) {
 					log.error("view update failed: " + view + " <- " + value, e);
 				}
 		}
 	}
 
 	// TODO: notifications for update/delete
 
 	protected void notifyBatch(Collection<V> insertions, Collection<Update<V>> updates, Collection<K> deletions) {
 		for (View<K, V> view : views)
 			try {
 				view.batch(insertions, updates, deletions);
 			} catch (RuntimeException e) {
 				log.error("view notification failed: " + view + " <- " + insertions, e);
 			}
 	}
 }
