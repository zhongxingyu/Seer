 package com.trabajo.jpa;
 
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 
 @Entity(name = "ClassLoaderDef")
 @Table(name = "CLASSLOADERDEF")
 public class ClassLoaderDefJPA implements JPAProcessComponent<ClassLoaderDefJPA> {
 
 	private static final long serialVersionUID = 1L;
 
 	public ClassLoaderDefJPA() {
 		super();
 	}
 
 	private int id;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	private VersionJPA definitionVersion;
 
 	@Embedded
 	public VersionJPA getDefinitionVersion() {
 		return definitionVersion;
 	}
 
 	public void setDefinitionVersion(VersionJPA definitionVersion) {
 		this.definitionVersion = definitionVersion;
 	}
 	
 	private boolean deprecated;
 	@Column(name = "DEPRECATED", columnDefinition="tinyint default 0")
 	public boolean isDeprecated() {
 		return deprecated;
 	}
 
 	public void setDeprecated(boolean deprecated) {
 		this.deprecated = deprecated;
 	}
 
 	private String category;
 
 	@Column(name = "CATEGORY", nullable = false)
 	public String getCategory() {
 		return category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 
 	private String description;
 
 	@Column(name = "DESCRP", nullable = false)
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	@PrePersist
 	@PreUpdate
 	public void prePersist() {
 
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((getDefinitionVersion() == null) ? 0 : getDefinitionVersion().hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ClassLoaderDefJPA other = (ClassLoaderDefJPA) obj;
 		if (getDefinitionVersion() == null) {
 			if (other.getDefinitionVersion() != null)
 				return false;
 		} else if (!getDefinitionVersion().equals(other.getDefinitionVersion()))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String getName() {
		return getDefinitionVersion().toString();
 	}
 }
