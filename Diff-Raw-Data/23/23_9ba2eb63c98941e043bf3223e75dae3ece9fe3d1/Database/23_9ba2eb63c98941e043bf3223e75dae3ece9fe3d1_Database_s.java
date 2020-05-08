 package fr.arpinum.siteTester.tools;
 
 import java.io.File;
 import java.util.List;
 
 import com.db4o.Db4oEmbedded;
 import com.db4o.ObjectContainer;
 
 public enum Database {
 
 	INSTANCE;
 
 	private Database() {
 	}
 
 	public void store(final Object object) {
 		container.store(object);
 	}
 
 	public <T> List<T> getAll(final Class<T> clazz) {
 		return container.queryByExample(clazz);
 	}
 
	public void flush() {
		container.close();
	}

 	public void close() {
 		container.close();
 
 	}
 
 	public void commit() {
 		container.commit();
 	}
 
 	public void rollback() {
 		container.rollback();
 	}
 
 	public void open(File file) {
 		container = Db4oEmbedded.openFile(file.getAbsolutePath());
 	}
 
 	private ObjectContainer container;
 
 }
