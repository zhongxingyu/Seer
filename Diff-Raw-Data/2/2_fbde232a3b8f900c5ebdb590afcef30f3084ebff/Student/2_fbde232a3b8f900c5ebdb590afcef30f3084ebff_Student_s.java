 package no.niths.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.constraints.Max;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Past;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import no.niths.common.AppConstants;
 import no.niths.domain.constraints.StudentGender;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.hibernate.validator.constraints.Email;
 
 @Entity
 @Table(name = AppConstants.STUDENTS)
 @XmlRootElement
 public class Student implements Serializable {
 
 	@Transient
 	private static final long serialVersionUID = 8441269238845961513L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 
 	@Column(name = "first_name")
 	@NotNull
 	@Size(min = 1, max = 55, message = "Must be minimun 1 char and max 55 chars")
 	private String firstName;
 
 	@Column(name = "last_name")
 	@NotNull
 	@Size(min = 1, max = 55, message = "Must be minimun 1 char and max 55 chars")
 	private String lastName;
 
 	@Column
 	@StudentGender
 	private Character gender;
 
 	@Column
 	private String password;
 
 	@Column
 	@Past
 	private Date birthday;
 
 	@Column
 	@Max(value = 3, message = "Can not be larger then 3")
 	@Min(value = 1, message = "Can not be smaller then 1")
 	private Integer grade;
 
 	@Column
 	@Email(message = "Not a valid email")
 	private String email;
 
 	@Column(name = "phone_number")
 	@Pattern(regexp = "(^$)|([0-9]{8})", message = "Not a valid number")
 	private String telephoneNumber;
 
 	@Column
 	@Size(min = 0, max = 255, message = "Can not be more then 255 chars")
 	private String description;
 
 	@ManyToMany(fetch = FetchType.LAZY)
 	private List<Course> courses = new ArrayList<Course>();
 
 	// , mappedBy = "members"
 	@ManyToMany(fetch = FetchType.LAZY, targetEntity = Committee.class)
 	private List<Committee> committees = new ArrayList<Committee>();;
 
 	public Student() {
 		this("", "");
 	}
 
 	public Student(Long id, String firstName, String lastName) {
 		this(firstName, lastName);
 		setId(id);
 	}
 
 	public Student(String firstName, String lastName) {
 		setFirstName(firstName);
 		setLastName(lastName);
 	}
 
 	public Student(String firstName, String lastName, Character gender,
 			Integer grade, String email, String telephoneNumber,
 			String description) {
 		setFirstName(firstName);
 		setLastName(lastName);
 		setEmail(email);
 		setTelephoneNumber(telephoneNumber);
 		setDescription(description);
 		setCourses(new ArrayList<Course>());
 		setCommittees(new ArrayList<Committee>());
 		setGender(gender);
 		setGrade(grade);
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public Date getBirthday() {
 		return birthday;
 	}
 
 	public void setBirthday(Date birthday) {
 		this.birthday = birthday;
 	}
 
 	public Character getGender() {
 		return gender;
 	}
 
 	public void setGender(Character gender) {
 		this.gender = gender;
 	}
 
 	public Integer getGrade() {
 		return grade;
 	}
 
 	public void setGrade(Integer grade) {
 		this.grade = grade;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getTelephoneNumber() {
 		return telephoneNumber;
 	}
 
 	public void setTelephoneNumber(String telephoneNumber) {
 		this.telephoneNumber = telephoneNumber;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public List<Course> getCourses() {
 		return courses;
 	}
 
 	public void setCourses(List<Course> courses) {
 		this.courses = courses;
 	}
 
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Student)) {
 			return false;
 		}
 		Student s = (Student) obj;
 		return s == this ? true : s.getId() == id ? true : false;
 	}
 
 	public List<Committee> getCommittees() {
 		return committees;
 	}
 
 	public void setCommittees(List<Committee> committees) {
 		this.committees = committees;
 	}
 
 	@JsonIgnore
 	public boolean isEmpty() {
 		//Do we need to check for firstName and lastName? They can not be null
		return (id == null && firstName == null && lastName == null && 
 				gender == null && password == null && email == null && 
 				description == null && birthday == null && birthday == null);
 	}
 
 	@Override
 	public String toString() {
 		String s = "{\"name\":\"" + firstName + "\",\"id\"";
 
 		return s;
 	}
 }
