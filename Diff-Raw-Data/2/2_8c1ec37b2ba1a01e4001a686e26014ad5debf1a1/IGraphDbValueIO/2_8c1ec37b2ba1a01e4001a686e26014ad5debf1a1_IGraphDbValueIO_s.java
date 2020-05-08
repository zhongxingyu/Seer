 package lapd.databases.neo4j;
 
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.type.TypeStore;
 
 public interface IGraphDbValueIO {
 	
 	public void write(String id, IValue value) throws GraphDbMappingException;
	void write(String id, IValue value, boolean deleteOld) throws GraphDbMappingException;
 	public IValue read(String id) throws GraphDbMappingException;
 	public IValue read(String id, TypeStore typeStore) throws GraphDbMappingException;
 	public IValue read(String id, Type type) throws GraphDbMappingException;
 	public IValue read(String id, Type type, TypeStore typeStore) throws GraphDbMappingException;
 	public IValue executeQuery(String query) throws GraphDbMappingException;
 	public IValue executeQuery(String query, TypeStore typeStore) throws GraphDbMappingException;
 	public IValue executeQuery(String query, Type type) throws GraphDbMappingException;
 	public IValue executeQuery(String query, Type type, TypeStore typeStore) throws GraphDbMappingException;
 	public boolean idExists(String id);	
 
 }
