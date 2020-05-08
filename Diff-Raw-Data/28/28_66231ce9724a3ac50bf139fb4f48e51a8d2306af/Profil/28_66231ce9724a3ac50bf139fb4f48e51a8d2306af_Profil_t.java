 package com.ossia.test.domain;//
 
 import java.text.Normalizer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 
 import org.hibernate.annotations.OrderBy;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 
 @Entity
 @Table(name = "T_PROFILS")
 public class Profil implements UserDetails {
 
 	private static final long serialVersionUID = 1L;
 
 	public static final SimpleGrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
 	private static final Collection<GrantedAuthority> ADMIN = wrapAuthority(ROLE_ADMIN);
 	private static final Collection<GrantedAuthority> USER = wrapAuthority(new SimpleGrantedAuthority("ROLE_USER"));
 	
 	public static final String MODE_ADD = "add";
 	public static final String MODE_EDIT = "edit";
 	public static final String MODE_ACTIVATION = "activate";
 	public static final String MODE_DESACTIVATION = "desactivate";
 	public static final String MODE_DELETE = "delete";
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Integer id;
 
 	@NotEmpty
 	private String nom;
 
 	@NotEmpty
 	private String prenom;
 
 	@NotEmpty @Pattern(regexp = "^\\s*$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(?:[a-zA-Z]{2}|com|org|net|edu|gov|mil|biz|info|mobi|name|aero|asia|jobs|museum)$", message = "{form.error.email.invalid}")
 	private String email;
 
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY , mappedBy = "profil") @OrderBy(clause = "startTime DESC")
 	private Set<Evaluation> evaluations;
 
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY , mappedBy = "profil") @OrderBy(clause = "timestamp DESC")
 	private List<ProfilHisto> historique;
 	
 	@NotEmpty
 	private String login;
 
 	@NotEmpty
 	private String pass;
 
 	@NotNull
 	private boolean admin;
 	
 	/**
 	 * Flag used in the forms to precise if the user is active or not
 	 */
 	@Transient
 	private boolean active = true;
 
 	private Date dateActivation;
 	
 	/**
 	 * Form action: ADD or EDIT
 	 */
 	@Transient
 	private String mode;
 
 	public Profil() {
 		super();
 		this.id = 0;
 		this.mode = MODE_ADD;
 		this.active = true;
 	}
 
 	public Profil(String nom, String prenom) {
 		super();
 		this.nom = nom;
 		this.prenom = prenom;
 		
 		this.dateActivation = new Date(); 
 		this.active = true;
 		this.login = generateLoginFromNom() ; 
 		this.pass = generatePassFromNom() ; 
 	}
 	
 	public void copyPropertiesFrom(Profil profilFrom) {
 		this.nom = profilFrom.getNom();
 		this.prenom = profilFrom.getPrenom();
 		this.dateActivation = profilFrom.getDateActivation();
 		this.pass = profilFrom.getPass();
 		this.login = profilFrom.getLogin();
 		this.email = profilFrom.getEmail();		
 	}
 	
 	public boolean hasTestsToPass() {
 		return getAssignedTests().size() > 0;
 	}
 	
 	public List<Evaluation> getAssignedTests() {
 		List<Evaluation> assignedTests = new ArrayList<Evaluation>();
 		for(Evaluation evaluation : this.evaluations) {
 			if (!evaluation.isTestTaken()) {
 				assignedTests.add(evaluation);
 			}
 		}
 		return assignedTests;
 	}
 	
 	private String generateLoginFromNom () {
 		if (this.prenom != null && !this.prenom.isEmpty() && this.nom != null && !this.nom.isEmpty()) {			
 			return Normalizer.normalize(this.prenom.substring(0, 1).toLowerCase().concat(this.nom.toLowerCase().trim()), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
 		}
 		return "";
 	}
 	
 	private String generatePassFromNom () {
 		return Normalizer.normalize(getNom().toLowerCase().trim(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
 	}
 	
 	public String getNomComplet() {
 		return prenom + " " + nom;
 	}
 
 	/* Authorization fields */
 	public String getPass() {
 		return pass;
 	}
 
 	public void setPass(String pass) {
 		this.pass = pass;	
 	}
 
 	public Set<Evaluation> getEvaluations() {
 		return evaluations;
 	}
 
 	public void setEvaluations(Set<Evaluation> evaluations) {
 		this.evaluations = evaluations;
 	}
 
 	public List<ProfilHisto> getHistorique() {
 		if (historique == null) {
 			historique = new ArrayList<ProfilHisto>();
 		}
 		return historique;
 	}
 
 	public void setHistorique(List<ProfilHisto> historique) {
 		this.historique = historique;
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public void setLogin(String login) {
 		this.login = login;
 	}
 
 	public Date getDateActivation() {
 		return dateActivation;
 	}
 
 	public void setDateActivation(Date dateActivation) {
 		this.dateActivation = dateActivation;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getNom() {
 		return nom;
 	}
 
 	public void setNom(String nom) {
 		this.nom = nom;
 	}
 
 	public String getPrenom() {
 		return prenom;
 	}
 
 	public void setPrenom(String prenom) {
 		this.prenom = prenom;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getPassword() {
 		return pass;
 	}
 
 	@Override
 	public String getUsername() {
 		return getLogin();
 	}
 
 	public boolean isEnabled() {
 		// User is enabled only if dateActivation is not null
 		return getDateActivation() != null;
 	}
 
 	@Override
 	public Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
 		if (admin) {
 			return ADMIN;
 		}
 		return USER;
 	}
 
 	@Override
 	public boolean isAccountNonExpired() {
 		return true;
 	}
 
 	@Override
 	public boolean isAccountNonLocked() {
 		return true;
 	}
 
 	@Override
 	public boolean isCredentialsNonExpired() {
 		return true;
 	}
 
 	private static Set<GrantedAuthority> wrapAuthority(GrantedAuthority authority) {
 		return Collections.<GrantedAuthority> singleton(authority);
 	}
 
 	public boolean isAdmin() {
 		return admin;
 	}
 
 	public void setAdmin(boolean admin) {
 		this.admin = admin;
 	}
 
 	public String getMode() {
 		return mode;
 	}
 
 	public void setMode(String mode) {
 		this.mode = mode;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 	
 	public boolean getActive() {
 		return active;
 	}
 
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 }
