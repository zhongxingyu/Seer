 package org.dada.core;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 public class RemoteModel<K, V> implements Model<K, V>, Serializable {
 
 	private final Metadata<K, V> metadata;
 	private final String name;
 	private final String endPoint;
 	
 	private transient Session session;
 	
 	public RemoteModel(String name, Metadata<K, V> metadata) throws Exception {
 		this.metadata = metadata;
 		this.name = name;
 		this.endPoint = name;// TODO pull from localSessionManager - name of tmp Topic
 		this.session = null; // peer should never be needed this side of the wire
 		// TODO: we need to start a server for our host Model
 	}
 	
 	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
 		out.defaultWriteObject();
 	}
 
 	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
 		in.defaultReadObject();
 		session = SessionManagerHelper.getCurrentSession();
 		//if (session == null) new Exception("SessionManagerHelper has no current Session").printStackTrace();
 	}
 	 
 	@Override
 	public void start() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("NYI");
 	}
 
 	@Override
 	public void stop() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("NYI");
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	// TODO - SessionManager should be a template as well
 	@Override
 	public Data<V> registerView(View<V> view) {
 		return (Data<V>) session.registerView((Model<Object, Object>)this, (View<Object>)view);
 	}
 
 	@Override
 	public Data<V> deregisterView(View<V> view) {
 		return (Data<V>) session.deregisterView((Model<Object, Object>)this, (View<Object>) view);
 	}
 
 	@Override
 	public Metadata<K, V> getMetadata() {
 		return metadata;
 	}
 
 	@Override
 	public Data<V> getData() {
	    throw new UnsupportedOperationException("NYI");
	    //return (Data<V>) sessionManager.getData(name);
 	}
 
 	public String toString() {
 		return "<0x" + Integer.toHexString(System.identityHashCode(this)) + ":" + getClass().getSimpleName() + " " + name + " " + endPoint + ">";
 	}
 
 	@Override
 	public V find(K key) {
 		return (V)session.find((Model<Object, Object>)this, key);
 	}
 	
 	// RemoteModel
 	public String getEndPoint() {
 		return endPoint;
 	}
 }
