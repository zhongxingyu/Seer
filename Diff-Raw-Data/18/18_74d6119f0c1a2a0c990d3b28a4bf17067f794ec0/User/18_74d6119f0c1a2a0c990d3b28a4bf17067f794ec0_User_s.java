 package eu.margiel.domain;
 
 import javax.persistence.Lob;
 import javax.persistence.MappedSuperclass;
 
 import org.jasypt.util.password.StrongPasswordEncryptor;
 
 @SuppressWarnings("serial")
 @MappedSuperclass
 public class User extends AbstractEntity {
 	private transient StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
 	private String firstName;
 	private String lastName;
 	protected String mail;
 	private String password;
 	@Lob
 	private String bio;
 
 	public User() {
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public User password(String password) {
 		this.password = password;
 		return this;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends User> T firstName(String firstName) {
 		this.firstName = firstName;
 		return (T) this;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends User> T lastName(String lastName) {
 		this.lastName = lastName;
 		return (T) this;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public String getMail() {
 		return mail;
 	}
 
 	public boolean passwordIsCorrect(String plainPassword) {
 		return encryptor.checkPassword(plainPassword, this.password);
 	}
 
 	public void encryptPassword() {
 		this.password = encryptor.encryptPassword(this.password);
 	}
 
 	public void passwordWithEncryption(String password) {
 		this.password = password;
 		encryptPassword();
 	}
 
 	public String getFullName() {
 		return getFirstName() + " " + getLastName();
 	}
 
 	public String getBio() {
 		return bio;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends User> T mail(String mail) {
 		this.mail = mail;
 		return (T) this;
 	}
 
 }
