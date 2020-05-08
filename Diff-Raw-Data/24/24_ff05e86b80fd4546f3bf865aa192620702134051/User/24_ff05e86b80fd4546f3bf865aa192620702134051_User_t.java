 package it.univaq.mwt.j2ee.kmZero.business.model;
 
 import it.univaq.mwt.j2ee.kmZero.common.DateJsonSerializer;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.DiscriminatorType;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinTable;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 
 import com.fasterxml.jackson.databind.annotation.JsonSerialize;
 
 @Entity
 @Table(name="users")
 @Inheritance(strategy=InheritanceType.JOINED)
 @DiscriminatorColumn(name="user_type", discriminatorType=DiscriminatorType.STRING, length=1)
 @DiscriminatorValue(value="U")
 public class User implements java.io.Serializable{
 
 	@Id
 	@Column(name="user_id")
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	private long id;
 
 	@Column(name="name")
 	private String name;
 
 	@Column(name="surname")
 	private String surname;
 
 	@Column(name="email")
 	private String email;
 
 	@Column(name="password")
 	private Password password;
 
 	@Column(name="created",nullable=true) 
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date created;
 
 	@Column(name="date_of_birth",nullable=true)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date date_of_birth;
 
 	@Column(name="last_access",nullable=true)
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date last_access;
 
 	@Column(name="address")
 	private String address;
 
 	@ManyToMany(fetch=FetchType.EAGER,cascade = {CascadeType.PERSIST, CascadeType.REMOVE,CascadeType.MERGE})
 	@JoinTable(name="users_roles",joinColumns=@JoinColumn(name = "user_fk"),
 	inverseJoinColumns=@JoinColumn(name = "role_fk"))
 	private Set<Role> roles = new HashSet<Role>();
 	
 	@OneToMany(mappedBy="user", fetch=FetchType.LAZY,cascade=CascadeType.ALL,orphanRemoval=true)
 //	@JoinColumn(name = "user_fk")
 	private Collection<Cart> cart = new ArrayList<Cart>();
 
 	private static final long serialVersionUID = 1L;
 
 	public User() {
 
 	}
 
 	// Costruttore dello User con solo l'id
 	public User(long id){
 		this.id = id;
 	}
 
 	public User(String name, String surname, String email, Password password, Date created, 
 			Date date_of_birth, String address) {
 		super();
 		this.name = name;
 		this.surname = surname;
 		this.email = email;
 		this.password = password;
 		this.created = created;
 		this.date_of_birth = date_of_birth;
 		this.address = address;
 	}
 
 	public User(long id, String name, String surname, String email, Password password, 
 			Date created, Date date_of_birth, String address) {
 		this(name,surname,email,password,created,date_of_birth,address);
 		this.id = id;
 	}
 
 	/* Costruttore per visualizzare la lista degli utenti senza il campo password */
 	public User(long id, String name, String surname, String email,
 			Date created, Date date_of_birth, Date last_access, String address) {
 		this.id = id;
 		this.name = name;
 		this.surname = surname;
 		this.email = email;
 		this.created = created;
 		this.date_of_birth = date_of_birth;
 		this.last_access = last_access;
 		this.address = address;
 	}
 
 	/* Costruttore per aggiornare il profilo utente */
 	public User(long id, String name, String surname, String email,
 			Date date_of_birth, String address) {
 		super();
 		this.id = id;
 		this.name = name;
 		this.surname = surname;
 		this.email = email;
 		this.date_of_birth = date_of_birth;
 		this.address = address;
 	}
 	
 	/* Costruttore con il carrello (servira'?) */
 	public User(long id, String name, String surname, String email, Password password, 
 			Date created, Date date_of_birth, String address, Collection<Cart> cart) {
 		this(id, name,surname,email,password,created,date_of_birth,address);
 		this.cart = cart;
 	}
 
 
 
 	/* Costruttore che serve al Seller quando verr� visualizzata la lista tramite Datatables */
 
 	/* Costruttore che serve al Seller quando verr� visualizzata la lista tramite Datatables */
 
 	public User(long id, String name, String surname) {
 		super();
 		this.id = id;
 		this.name = name;
 		this.surname = surname;
 	}
 
 	// Costruttore per il cambio password
 	public User(long id, Password password) {
 		super();
 		this.id = id;
 		this.password = password;
 	}
 
 
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getSurname() {
 		return surname;
 	}
 	public void setSurname(String surname) {
 		this.surname = surname;
 	}
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	@Embedded
 	public Password getPassword() {
 		return password;
 	}
 	public void setPassword(Password password) {
 		this.password = password;
 	}
 
 	@JsonSerialize(using=DateJsonSerializer.class)
 	public Date getCreated() {
 		return created;
 	}
 
 	public void setCreated(Date created) {
 		this.created = created;
 	}
 
 	@JsonSerialize(using=DateJsonSerializer.class)
 	public Date getDate_of_birth() {
 		return date_of_birth;
 	}
 
 	public void setDate_of_birth(Date date_of_birth) {
 		this.date_of_birth = date_of_birth;
 	}
 
 	@JsonSerialize(using=DateJsonSerializer.class)
 	public Date getLast_access() {
 		return last_access;
 	}
 
 	public void setLast_access(Date last_access) {
 		this.last_access = last_access;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public Set<Role> getRoles() {
 		return roles;
 	}
 
 	public void setRoles(Set<Role> roles) {
 		this.roles = roles;
 	}
 
 	public Collection<Cart> getCart() {
 		return cart;
 	}
 
 	public void setCart(Collection<Cart> cart) {
 		this.cart = cart;
 	}
 	
 }
