 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.codehaus.jackson.annotate.JsonBackReference;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 
 @Entity
 public class Utilisateur extends Model {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	@Id
 	private long id;
 	@Required
 	@Column(length = 30, nullable = false)
 	private String nom;
 	@Required
 	@Column(length = 30, nullable = false)
 	private String pnom;
 	@Required
 	@Column(length = 30, nullable = false, unique = true)
 	private String pseudo;
 	@Required
 	@Column(nullable = false, unique = true)
 	public String mail;
 	@Required
 	@JsonIgnore
 	@Column(length = 30)
 	private String mdp;
 	@Temporal(TemporalType.DATE)
 	private Date dateInscription = new Date();
 
 	@JsonBackReference
 	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
 	private List<Message> msgs = new ArrayList<Message>();
 	@JsonBackReference
 	@OneToMany(mappedBy = "userAdmin", cascade = CascadeType.ALL)
 	private List<Groupe> adminGrps = new ArrayList<Groupe>();
 	@JsonBackReference
	@ManyToMany(mappedBy = "members")
 	private List<Groupe> grps = new ArrayList<Groupe>();
 
 	private List<Utilisateur> usersFollow = new ArrayList<Utilisateur>();
 
 	public Utilisateur() {
 
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getNom() {
 		return nom;
 	}
 
 	public void setNom(String nom) {
 		this.nom = nom;
 	}
 
 	public String getPnom() {
 		return pnom;
 	}
 
 	public void setPnom(String pnom) {
 		this.pnom = pnom;
 	}
 
 	public String getPseudo() {
 		return pseudo;
 	}
 
 	public void setPseudo(String pseudo) {
 		this.pseudo = pseudo;
 	}
 
 	public String getMail() {
 		return mail;
 	}
 
 	public void setMail(String mail) {
 		this.mail = mail;
 	}
 
 	public String getMdp() {
 		return mdp;
 	}
 
 	public void setMdp(String mdp) {
 		this.mdp = mdp;
 	}
 
 	public String getDateInscription() {
 		return dateInscription.toString();
 	}
 
 	public void setDateInscription(Date dateInscription) {
 		this.dateInscription = dateInscription;
 	}
 
 	public List<Message> getMsgs() {
 		return msgs;
 	}
 
 	public void setMsgs(List<Message> msgs) {
 		this.msgs = msgs;
 	}
 
 	public List<Groupe> getGrps() {
 		return grps;
 	}
 
 	public void setGrps(List<Groupe> grps) {
 		this.grps = grps;
 	}
 
 	public List<Groupe> getAdminGrps() {
 		return this.adminGrps;
 	}
 
 	public void setMyGrps(List<Groupe> adminGrps) {
 		this.adminGrps = adminGrps;
 	}
 
 	public List<Utilisateur> getUsersFollow() {
 		return usersFollow;
 	}
 
 	public void setUsersFollow(Utilisateur userToFollow) {
 		this.usersFollow.add(userToFollow);
 	}
 
 	// Méthodes CRUD
 
 	public static Finder<Long, Utilisateur> find = new Finder(Long.class,
 			Utilisateur.class);
 
 	// Create
 	public static void createUser(Utilisateur user) {
 		user.save();
 	}
 
 	// Read
 	public static List<Utilisateur> findAllUsers() {
 		return find.all();
 	}
 
 	public static Utilisateur findUserById(long id) {
 		return find.byId(id);
 	}
 
 
 	// Update
 	public static void updateUser(Utilisateur user) {
 		user.update();
 	}
 
 	// Delete
 	public static void deleteUser(Long id) {
 		find.ref(id).delete();
 	}
 
 	// find by email
 	public static Utilisateur findUserByEmail(String email) {
 		return find.where().eq("mail", email).findUnique();
 	}
 
 	// find by pseudo
 	public static Utilisateur findByPseudo(String pseudo) {
 		return find.where().eq("pseudo", pseudo).findUnique();
 	}
 
 	// find by Login
 	public static Utilisateur findByLogin(String email, String password) {
 		return find.where().eq("mail", email).eq("mdp", password).findUnique();
 	}
 
 	@Override
 	public String toString() {
 		return "Utilisateur [id=" + id + ", nom=" + nom + ", pnom=" + pnom
 				+ ", pseudo=" + pseudo + ", login=" + mail
 				+ ", dateInscription=" + dateInscription + ", msgs=" + msgs
 				+ ", grps=" + grps + "]";
 	}
 
 
 }
