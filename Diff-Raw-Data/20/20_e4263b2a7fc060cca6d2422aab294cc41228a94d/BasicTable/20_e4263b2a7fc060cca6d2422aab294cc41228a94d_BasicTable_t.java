 /**
  * 
  */
 package ifs.datamodel;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author TDEWEERD
  *
  */
 public class BasicTable implements Table{
 	
 	private String name;
 	private Owner owner;
 	private String description;
 	private Set<String> crud = new HashSet<String>();
 
 	public BasicTable(String name, Owner owner, String description) {
 		this.name = name;
 		this.owner = owner;
 		this.description = description;
 	}
 	
 	public BasicTable(String name, String crud) {
 		this.name = name;
 		if(crud.length() == 1)
 			this.crud.add(crud);
 		else {
 			for (int i = 0; i < crud.length(); i++) {
 				this.crud.add(crud.substring(i, i+1));
 			}
 		}
 	}
 
 	@Override
 	public String getTableName() {
 		return name;
 	}
 
 	@Override
 	public Owner getTableOwner() {
 		return owner;
 	}
 
 	@Override
 	public String getTableDescription() {
 		return description;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Table) {
 			return name.equals(((Table)obj).getTableName());
 		}
 		return false;
 	}
 	
 	@Override
 	public int hashCode() {
 		return name.hashCode();
 	}
 
 	/**
 	 * @return the crud
 	 */
 	public String getCrudOperation() {
 		StringBuilder s = new StringBuilder();
 		
 		if (crud.contains(AUTO_ENTRY))
 			s.append('C');
 		if (crud.contains(DETERMINE))
 			s.append('R');
 		if (crud.contains(FLAG))
 			s.append('U');
 		if (crud.contains(PURGE))
 			s.append('D');
 		return s.toString();
 	}
 
 	@Override
 	public void pushCrudOperation(String crudOperation) {
 		this.crud.add(crudOperation);
 	}
 
 	@Override
 	public String getCrudOperation(char c) {
 		if (crud.contains(""+c)) {
			return ""+c;
 		}
 		return "";
 	}
 	
 	@Override
 	public String toString() {
 		return name + " (" + getCrudOperation() + ")";
 	}
 
 }
