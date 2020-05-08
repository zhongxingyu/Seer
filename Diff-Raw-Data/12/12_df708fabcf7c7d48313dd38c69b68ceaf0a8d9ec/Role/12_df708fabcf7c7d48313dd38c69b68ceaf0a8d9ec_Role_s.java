 package models;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import org.openrdf.annotations.Iri;
 import org.openrdf.model.Resource;
 import org.openrdf.repository.object.ObjectConnection;
 import org.openrdf.repository.object.RDFObject;
 
 @Iri(NS.GNGM + "Role")
 public class Role implements RDFObject {
 
 	public static final String URI = "http://geonigme.fr/role/";
 
 	private String name;
 	private Integer rights;
 
 	public Role() {
 	}
 
 	public boolean canDo(Right r) {
 		return canDo(r.getValue());
 	}
 
 	public boolean canDo(int r) {
 		//return true;// Everybody is admin
 		//System.out.println("Role "+name+" canDo() => "+r+"/"+getRights());
		return (getRights() & r) == r;
 	}
 
 	public boolean hasRights() {
 		//return true;// Everybody is admin
		return getRights() > 0;
 	}
 
 	public Role grantRight(Right r) {
		setRights(getRights() | r.getValue());
 		return this;
 	}
 
 	public Role revokeRight(Right r) {
		setRights(getRights() & ~r.getValue());
 		return this;
 	}
 
 	public boolean equals(Role other) {
 		return getName().equals(other.getName());
 	}
 
 	public boolean equals(Object other) {
 		return equals((Role) other);
 	}
 
 	// Getters
 
 	@Iri(NS.GNGM + "name")
 	public String getName() {
 		return name;
 	}
 
 	@Iri(NS.GNGM + "rights")
 	public int getRights() {
 		return rights;
 	}
 
 	// Setters
 
 	@Iri(NS.GNGM + "name")
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Iri(NS.GNGM + "rights")
 	public void setRights(Integer rights) {
 		this.rights = rights;
 	}
 
 	public String urify() {
 		try {
 			return URI + URLEncoder.encode(getName(), "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public ObjectConnection getObjectConnection() {
 		return null;
 	}
 
 	@Override
 	public Resource getResource() {
 		return null;
 	}
 }
