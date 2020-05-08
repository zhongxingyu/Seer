 package sampleapp.entities;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import org.apache.commons.lang.builder.ReflectionToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 import edu.upenn.bbl.common.enums.NameSuffix;
 import edu.upenn.bbl.common.enums.Sex;
 import edu.upenn.bbl.common.jpa.Identifiable;
 import edu.upenn.bbl.common.util.DataFormats;
 import edu.upenn.bbl.common.util.Name;
import edu.upenn.bbl.common.web.struts.DateTypeConverter;
 
 @Entity
 @Table(name="PERSON")
 @NamedQueries({
 	@NamedQuery(name=Person.GET_ALL, query="select p from Person p order by p.id asc")
 })
 public class Person implements Serializable, Identifiable, Name {
 
 	private static final long serialVersionUID = 20100503L;
 	
 	public static final String GET_ALL = "getAllPeople";
 
 	private Long _id;
 
 	private String _firstName;
 	private String _lastName;
 	private NameSuffix _suffix;
 	private Date _birthDate;
 	private Sex _sex;
 
 	public Person() { }
 
 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	@Column(name="ID")
 	@Override
 	public Long getId() {
 		return _id;
 	}
 	@Override
 	public void setId(Long id) {
 		_id = id;
 	}
 	
 	@Column(name="FIRST_NAME")
 	@Override
 	public String getFirstName() {
 		return _firstName;
 	}
 	public void setFirstName(String firstName) {
 		_firstName = firstName;
 	}
 	
 	@Transient
 	@Override
 	public String getMiddleName() {
 		return null;
 	}
 
 	@Column(name="LAST_NAME")
 	@Override
 	public String getLastName() {
 		return _lastName;
 	}
 	public void setLastName(String lastName) {
 		_lastName = lastName;
 	}
 
 	@Column(name="SUFFIX")
 	@Enumerated(EnumType.STRING)
 	@Override
 	public NameSuffix getSuffix() {
 		return _suffix;
 	}
 	public void setSuffix(NameSuffix suffix) {
 		_suffix = suffix;
 	}
 
 	@Column(name="BIRTH_DATE")
 	@Temporal(TemporalType.DATE)
 	public Date getBirthDate() {
 		return _birthDate;
 	}
 	public void setBirthDate(Date birthDate) {
 		_birthDate = birthDate;
 	}
 	public void setBirthDate(String birthDate) {
		_birthDate = (Date)new DateTypeConverter().convertFromString(null, new String[]{ birthDate }, Date.class);
 	}
 
 	@Column(name="SEX")
 	@Enumerated(EnumType.STRING)
 	public Sex getSex() {
 		return _sex;
 	}
 	public void setSex(Sex sex) {
 		_sex = sex;
 	}
 	
 	@Override
 	public String toString() {
 		return ReflectionToStringBuilder.toString(this,ToStringStyle.MULTI_LINE_STYLE);
 	}
 
 	@Transient
 	public Person getPerson() {
 		return this;
 	}
 	
 	@Transient
 	@Override
 	public String getFullName() {
 		return DataFormats.getFullName(this);
 	}
 }
