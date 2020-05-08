 package org.fiteagle.core.userdatabase;
 
 import java.util.HashMap;
 
 public class InMemoryPersonDB implements PersonDB {

 	
 	private HashMap<String, Person> persons;
 	
 	public InMemoryPersonDB(){
 		persons = new HashMap<String, Person>();
 	}
 		
 	public int getSize(){
 		return persons.size();
 	}
 	
 	@Override
 	public void add(Person p) throws DuplicateUIDException {
 		if(persons.get(p.getUID()) != null){
 			throw new DuplicateUIDException();
 		}
 		else{
 			persons.put(p.getUID(), p);
 		}
 	}	
 
 	@Override
 	public void delete(String UID){
 		persons.remove(UID);			
 	}
 
 	@Override
 	public void delete(Person p){
 		delete(p.getUID());
 	}
 
 	@Override
 	public void update(Person p) throws RecordNotFoundException {
 		if(persons.get(p.getUID()) == null)
 			throw new RecordNotFoundException();
 		else
 			persons.put(p.getUID(), p);		
 	}
 
 	@Override
 	public Person get(String UID) throws RecordNotFoundException {
 		if(persons.get(UID) == null)
 			throw new RecordNotFoundException();
 		return persons.get(UID);		
 	}
 
 	@Override
 	public Person get(Person p) throws RecordNotFoundException {
 		return get(p.getUID());		
 	}
 
 	@Override
 	public void addKey(Person p, String key) throws RecordNotFoundException {
 		if((p=persons.get(p.getUID())) == null)
 			throw new RecordNotFoundException();
 		p.addPublicKey(key);
 		update(p);
 	}
 
 }
