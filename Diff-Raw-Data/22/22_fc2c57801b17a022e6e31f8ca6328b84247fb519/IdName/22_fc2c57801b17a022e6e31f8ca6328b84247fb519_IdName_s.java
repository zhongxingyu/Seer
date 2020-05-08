 package elw.vo;
 
 public class IdName {
 	protected String name;
 	protected String id;
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String toString() {
 		return name == null || name.trim().length() == 0 ? id : name;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (this == o) return true;
 		if (!(o instanceof IdName)) return false;
 
 		IdName idName = (IdName) o;
 
 		if (id != null ? !id.equals(idName.id) : idName.id != null) return false;
 		if (name != null ? !name.equals(idName.name) : idName.name != null) return false;
 
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		int result = name != null ? name.hashCode() : 0;
 		result = 31 * result + (id != null ? id.hashCode() : 0);
 		return result;
 	}
 
 	public static <E extends IdName> E findById(final E[] elems, String id) {
 		E found = null;
 		for (E e : elems) {
 			if (id.equals(e.getId())) {
 				found = e;
 				break;
 			}
 		}
 
 		return found;
 	}
 
	public static <E extends IdName> E findByName(final E[] elems, String name) {
 		E found = null;
 		for (E e : elems) {
			if (name.equals(e.getName())) {
 				found = e;
 				break;
 			}
 		}
 
 		return found;
 	}
 }
