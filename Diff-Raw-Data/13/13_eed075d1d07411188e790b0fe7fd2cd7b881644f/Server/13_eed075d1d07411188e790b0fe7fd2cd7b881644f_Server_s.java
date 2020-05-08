 package br.com.ahrpius.blank.model;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 @Entity
 public class Server {
 	
 	@Id
 	@GeneratedValue
 	private Long id;
 	private String description;
 	private String name;
 	private String ip;
 	private Calendar creation;
 	@ManyToOne
 	private User owner;
 	@ManyToMany(mappedBy="servers")
 	private List<Project> projects = new ArrayList<Project>();
 	@OneToMany(mappedBy="server")
 	private List<SystemUser> systemUsers = new ArrayList<SystemUser>();
 	@OneToMany(mappedBy="server")
 	private List<InstalledSoftware> installedSoftwares = new ArrayList<InstalledSoftware>();
 	@ManyToOne
 	private OperatingSystem operatingSystem;
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getIp() {
 		return ip;
 	}
 	public void setIp(String ip) {
 		this.ip = ip;
 	}
 	public Calendar getCreation() {
 		return creation;
 	}
 	public void setCreation(Calendar creation) {
 		this.creation = creation;
 	}
 	public User getOwner() {
 		return owner;
 	}
 	public void setOwner(User owner) {
 		this.owner = owner;
 	}
 	public Long getId() {
 		return id;
 	}
 	public OperatingSystem getOperatingSystem() {
 		return operatingSystem;
 	}
 	public void setOperatingSystem(OperatingSystem operatingSystem) {
 		this.operatingSystem = operatingSystem;
 	} 
 }
