 package models;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 import org.apache.commons.lang.StringUtils;
 
 import play.modules.ebean.Model;
 import util.CryptoUtil;
 import util.StringUtil;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.Query;
 @Entity
 @Table(name="user")
 public class User extends Model{
 	private String name;
 	private String realname;
 	private String website;
 	private String location;
 	private Date birthday;
 	private String about;
 	private String email;
 	
 	@Column(name="createDate")
	private Date	createDate;
 	@ManyToMany
 		@JoinTable(name="user_cprole"
 		,inverseJoinColumns = @JoinColumn(name="roles_id",referencedColumnName="id",table="cprole")
 	)
 	private Set<CPRole> roles;
 	
 	
 	public static User createOrGet(String email) {
 		Query<User> find = User.find("email = ?",email);
 		User user = find.findUnique();
 		if(user!=null)
 			return user;
 		user = new User();
 		if(User.count()==0) {
 			user.roles.add(CPRole.createOrGet("admin"));
 		}
 		user.roles.add(CPRole.createOrGet("user"));
 		user.createDate = new Date();
 		user.email = email;
 		user.save();
 		user.name = "用户"+CryptoUtil.longToString(user.id);
 		user.save();
 		return user;
 	}
 	public static User createOrGetByOpenid(String openid, String provider) {
 		Query<OpenIdInfo> q = OpenIdInfo.find("openid = ? and provider = ?", openid, provider);
 		OpenIdInfo findByOpenid = q.findUnique();
 		if(findByOpenid==null && StringUtils.isNotEmpty(openid)) {
 			User user = new User();
			user.roles = new HashSet<CPRole>();
 			user.roles.add(CPRole.createOrGet("user"));
 			user.createDate = new Date();
 			user.save();
 			Ebean.saveManyToManyAssociations(user, "roles");
 			OpenIdInfo openIdInfo = new OpenIdInfo();
 			openIdInfo.openid = openid;
 			openIdInfo.provider = provider;
 			openIdInfo.user = user;
 			openIdInfo.save();
 			findByOpenid = openIdInfo;
 		}
 		return findByOpenid.user;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getRealname() {
 		return realname;
 	}
 	public void setRealname(String realname) {
 		this.realname = realname;
 	}
 	public String getWebsite() {
 		return website;
 	}
 	public void setWebsite(String website) {
 		this.website = website;
 	}
 	public String getLocation() {
 		return location;
 	}
 	public void setLocation(String location) {
 		this.location = location;
 	}
 	public Date getBirthday() {
 		return birthday;
 	}
 	public void setBirthday(Date birthday) {
 		this.birthday = birthday;
 	}
 	public String getAbout() {
 		return about;
 	}
 	public void setAbout(String about) {
 		this.about = about;
 	}
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	public Date getCreateDate() {
 		return createDate;
 	}
 	public void setCreateDate(Date createDate) {
 		this.createDate = createDate;
 	}
 	public Set<CPRole> getRoles() {
 		return roles;
 	}
 	public void setRoles(Set<CPRole> roles) {
 		this.roles = roles;
 	}
 	
 }
