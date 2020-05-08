 package model;
 
 import hash.HashCalculator;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Transient;
 
 import org.hibernate.validator.NotEmpty;
 import org.hibernate.validator.NotNull;
 
 @Entity
 public class User implements Serializable {
 	
 	@Id
 	@GeneratedValue
 	private Long id;
 	
 	@Column(unique=true)
 	@NotEmpty
 	private String login;
 
 	@NotEmpty
 	private String password;
 
 	@NotEmpty
 	private String email;
 	
 	@NotNull
 	@Enumerated(EnumType.STRING)
 	private Role role;
 
 	private boolean active;
 
 	private String name;
 	private String institution;
 
 	@NotNull
 	private boolean certified;
 	
 	//@OneToMany(mappedBy="user")
 	//private List<Answer> answer = new ArrayList<Answer>();
 	
 	@OneToMany(mappedBy="user")
 	private List<Specialist> specialists = new ArrayList<Specialist>();
 
 	public long getId() {
 		return this.id;
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public String getPassword() {
 		return this.password;
 	}
 	
 	public String getEmail() {
 		return email;
 	}
 
 	public Role getRole() {
 		return role;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 	
 	public String getInstitution() {
 		return institution;
 	}
 
 	public boolean isCertified() {
 		return certified;
 	}
 
 	public void setPasswordFromRawString(String password) {
 		HashCalculator encryption = new HashCalculator(password);
 		this.password = encryption.getValue();
 	}
 
 	@Transient
 	public boolean isAdmin() {
 		return role == Role.ADMIN;
 	}
 
 	public List<Specialist> getSpecialists() {
 		return specialists;
 	}
 
 	public boolean isSpecialistIn(Specialty specialty) {
 		for (Specialist specialist : getSpecialists()) {
 			if (specialist.getSpecialty().equals(specialty))
 				return true;
 		}
 		return false;
 	}
 
 	public List<Specialty> getSpecialties() {
 		List<Specialty> specialties = new ArrayList<Specialty>();
 		for (Specialist specialist : this.specialists) {
 			specialties.add(specialist.getSpecialty());
 		}
 		return specialties;
 	}
 	
 	public Specialist getSpecialistAt(Specialty specialty) {
 		for (Specialist specialist : this.specialists) {
 			if (specialist.getSpecialty().equals(specialty))
 				return specialist;
 		}
 		return null;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public void setLogin(String login) {
 		this.login = login;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public void setRole(Role role) {
 		this.role = role;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public void setInstitution(String institution) {
 		this.institution = institution;
 	}
 
 	public void setCertified(boolean certified) {
 		this.certified = certified;
 	}
 	
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 	
 	public void setSpecialists(List<Specialist> specialists) {
 		this.specialists = specialists;
 	}
 	
 	public boolean equals(User u) {
		if (u == null)
			return false;
 		return u.getId() == this.id;
 	}
 
 	public boolean isSpecialist() {
 		return !this.getSpecialists().isEmpty();
 	}
 
 }
