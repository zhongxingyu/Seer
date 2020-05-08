 package nl.topicus.lan.dashboard.entities.person;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 
 import nl.topicus.lan.dashboard.entities.BaseLanEntity;
 
 import org.hibernate.annotations.AccessType;
 
import com.ibm.icu.util.GenderInfo.Gender;

 @Entity
 @AccessType("field")
 public class Profile extends BaseLanEntity
 {
 	private static final long serialVersionUID = 1L;
 
 	@JoinColumn(nullable = false)
 	private Account account;
 
 	/**
 	 * Game Info
 	 */
 	@Column(nullable = false)
 	private String nickname;
 
 	/**
 	 * Personal info
 	 */
 	@Column(nullable = false)
 	private String name;
 
 	@Column(nullable = true)
 	private String surname;
 
 	@Column(nullable = false)
 	private String lastname;
 
 	@Column(nullable = false)
	private Gender gender;
 
 	@Column(nullable = false)
 	private int tableNumber;
 
 	public Account getAccount()
 	{
 		return account;
 	}
 
 	public void setAccount(Account account)
 	{
 		this.account = account;
 	}
 
 	public String getNickname()
 	{
 		return nickname;
 	}
 
 	public void setNickname(String nickname)
 	{
 		this.nickname = nickname;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	public String getSurname()
 	{
 		return surname;
 	}
 
 	public void setSurname(String surname)
 	{
 		this.surname = surname;
 	}
 
 	public String getLastname()
 	{
 		return lastname;
 	}
 
 	public void setLastname(String lastname)
 	{
 		this.lastname = lastname;
 	}
 
	public Gender getGender()
 	{
 		return gender;
 	}
 
	public void setGender(Gender gender)
 	{
 		this.gender = gender;
 	}
 
 	public int getTableNumber()
 	{
 		return tableNumber;
 	}
 
 	public void setTableNumber(int tableNumber)
 	{
 		this.tableNumber = tableNumber;
 	}
 
 }
