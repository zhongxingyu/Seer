 package models;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.persistence.CascadeType;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToOne;
 
 import play.data.validation.Required;
 import siena.Column;
 import siena.Generator;
 import siena.Id;
 import siena.Max;
 import siena.Model;
 import siena.NotNull;
 import siena.Query;
 import siena.Table;
 
 @Table("user")
 public class User extends Model {
 
 	@Id(Generator.AUTO_INCREMENT)
 	public Long id;
 
 	@Column("account")
 	@Required
 	@Max(100)
 	@NotNull
 	public String account;
 
 	@Column("member")
 	@Required
 	@NotNull
 	public Member member;
 
 	public User(String account, Member member) {
 		this.account = account;
 		this.member = member;
 	}
 
 	public static Query<User> all() {
 		return Model.all(User.class);
 	}
 
 	@Override
 	public String toString() {
 		return account + " (" + Member.all().getByKey(member.id) + ')';
 	}
 
 	public static Collection<Member> findByAccount(String account) {
 		Collection<User> users = all().filter("account", account).fetch();
 		Collection<Member> members = new ArrayList<Member>(users.size());
 		for (User user : users) {
			members.add(user.member); // TODO should I use getByKey() here too ?!
 		}
 		return members;
 	}
 }
