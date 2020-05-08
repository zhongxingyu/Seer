 package db;
 
public class User implements AbstractModel {
 	private Action action;
 	private static final long serialVersionUID = 1L;
 	private int id;
 	private String name;
 	private String email;
 	private String hashedPassword;
 	
 	public User() {
 		this.action = null;
 		this.id = 1337;
 		this.name = "leeroy jenkins";
 		this.hashedPassword = "hunter2";
 		this.email = "too.leqit@to.quit";
 	}
 	
 	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		User other = (User) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
 	/*public String toString() {
 		return "User [id=" + id + ", name=" + name + ", email=" + email
 				+ ", hashedPassword=" + hashedPassword + "]";
 	}*/
 	public String toString(){
 		return name + ", " + email;
 	}
 	
 
 	public User(int id, String name, String email, String hashedPassword){
 		this.action = null;
 		this.id = id;
 		this.name = name;
 		this.hashedPassword = hashedPassword;
 		this.email = email;
 	}
 	public User(String name, String email, String HashedPassword) {
 		this.action = null;
 		this.name = name;
 		this.hashedPassword = HashedPassword;
 		this.email = email;
 	}
 	public void setName(String name){
 		this.name = name;
 	}
 	public String getName(){
 		return name;
 	}
 	public void setHashedPassword(String hashedPassword){
 		this.hashedPassword = hashedPassword;
 	}
 	public String getPassword(){
 		return hashedPassword;
 	}
 	public void setEmail(String email){
 		this.email = email;
 	}
 	public String getEmail(){
 		return email;
 	}
 	public void setId(int id){
 		this.id = id;
 	}
 	public int getId(){
 		return id;
 	}
 
 	@Override
 	public void setAction(Action action) {
 		this.action = action;
 	}
 	@Override
 	public Action getAction() {
 		return this.action;
 	}
 	public <T> T getCopy() {
 		return (T) new User(this.id, this.name, this.email, this.hashedPassword);
 	}
 
 }
