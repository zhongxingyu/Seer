 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.codehaus.jackson.annotate.JsonBackReference;
 import org.codehaus.jackson.annotate.JsonManagedReference;
 
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 
 @Entity
 public class Groupe extends Model {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	@Id
 	private long id;
 	@Required
 	@Column(length = 30, nullable = false)
 	private String nomGroupe;
 	@Temporal(TemporalType.DATE)
 	private Date dateCreation = new Date();
 	
 	@JsonBackReference
 	@OneToMany(mappedBy = "grp", cascade = CascadeType.ALL)
 	private List<Message> msgs = new ArrayList<Message>();
 	
 	@JsonManagedReference
	@ManyToMany(mappedBy = "grps",cascade = {CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
 	private List<Utilisateur> members = new ArrayList<Utilisateur>();
 	
 	@JsonManagedReference
 	@ManyToOne
 	private Utilisateur userAdmin;
 
 	public Groupe() {
 
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getNomGroupe() {
 		return nomGroupe;
 	}
 
 	public void setNomGroupe(String nomGroupe) {
 		this.nomGroupe = nomGroupe;
 	}
 
 	public String getDateCreation() {
 		return dateCreation.toString();
 	}
 
 	public void setDateCreation(Date dateCreation) {
 		this.dateCreation = dateCreation;
 	}
 
 	public List<Message> getMsgs() {
 		return msgs;
 	}
 
 	public void setMsgs(List<Message> msgs) {
 		this.msgs = msgs;
 	}
 
 	public List<Utilisateur> getMembers() {
 		return this.members;
 	}
 
 	public void setMembers(List<Utilisateur> members) {
 		this.members = members;
 	}
 
 	public Utilisateur getUserAdmin() {
 		return userAdmin;
 	}
 
 	public void setUserAdmin(Utilisateur userAdmin) {
 		this.userAdmin = userAdmin;
 	}
 
 	// Méthodes CRUD
 
 	public static Finder<Long, Groupe> find = new Finder(Long.class,Groupe.class);
 
 	// Create
 	public static void createGroupe(Groupe grp) {
 		grp.save();
 	}
 
 	// Read
 	public static List<Groupe> findAllGroupes() {
 		return find.all();
 	}
 
 	public static Groupe findGroupeById(long id) {
 		return find.byId(id);
 	}
 
 	// Update
 	public static void updateGroupe(Groupe grp) {
 		grp.update();
 	}
 
 	// Delete
 	public static void deleteGroupe(Long id) {
 		find.ref(id).delete();
 	}
 
 	@Override
 	public String toString() {
 		return "Groupe [id=" + id + ", nomGroupe=" + nomGroupe + ", dateCreation="
 				+ dateCreation + ", msgs=" + msgs + ", members=" + members
 				+ ", userAdmin=" + userAdmin + "]";
 	}
 	
 
 	
 
 
 }
