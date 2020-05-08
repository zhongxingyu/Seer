 package ru.xrm.app.domain;
 
 import java.lang.reflect.Field;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 @Entity
 public class Section {
 
 	@Id
 	@Column(name="section_id")
 	private Long id;
 
 	@Column(name="name")
 	private String name;
 
 	@Column(name="href")
 	private String href;
 
 	@OneToMany(mappedBy = "section")
 	private Set<Vacancy> vacancies = new HashSet<Vacancy>();
 
 	public Section() {
 		this("","");
 	}
 
 	public Section(String name) {
 		this(name, "");
 	}
 
 	public Section(String name, String href) {
 		this.name = name;
 		this.href = href;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getHref() {
 		return href;
 	}
 
 	public void setHref(String href) {
 		this.href = href;
 	}
 
 	public Set<Vacancy> getVacancies() {
 		return vacancies;
 	}
 
 	public void setVacancies(Set<Vacancy> vacancies) {
 		this.vacancies = vacancies;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
 		Section other = (Section) obj;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 
 	public void setProperty(String property, Object value)
 			throws SecurityException, NoSuchFieldException,
 			IllegalArgumentException, IllegalAccessException {
 		@SuppressWarnings("rawtypes")
 		Class aClass = getClass();
 		Field field = aClass.getDeclaredField(property);
 		field.set(this, value);
 	}
	
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(getClass().getName());
 		sb.append("{\n");
 		try {
 			for (Field f : getClass().getDeclaredFields()) {
 				sb.append("\t");
 				sb.append(f.getName());
 				sb.append(" : ");
 
 				sb.append(f.get(this));
 
 				sb.append("\n");
 			}
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		sb.append("}\n");
 		return sb.toString();
	}
 }
