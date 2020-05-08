 package beans;
 
 import java.util.Date;
 
 import interfaces.Model;
 import dao.Users;
 
 
 public class User extends Model{
 	protected int _id;
 	protected String _login;
 	protected String _pwd;
 	protected String _fName;
 	protected String _lName;
 	protected int _age;
 	protected String _email;
 	protected Boolean _isAdmin;
 	protected Date _lastConnection;
 	protected Integer _lastDC;
 	protected String _role;
 
  	
 	public User(int id, String login, String pwd, String fName, String lName, int age, String email) {
 		super();
 		setInfos(id, login, pwd, fName, lName, age, email, false);
 		this.DAO = new Users();
 	}
 
 	public User(int id, String login, String pwd, String fName, String lName, int age, String email, Boolean isAdmin) {
 		super();
 		setInfos(id, login, pwd, fName, lName, age, email, isAdmin);
 		this.DAO = new Users();
 	}
 	
 	public User() {
		super();
		setInfos(0, null, null, null, null, 0, null, false);
 		this.DAO = new Users();
 	}
 
 	public int getId() { return _id;}
 	public String getLogin() {	return _login;}
 	public String getPwd() {return _pwd;}
 	public String getFName() {	return _fName;}
 	public String getLName() {return _lName;}
 	public int getAge() {return _age;}
 	public String getEmail() {	return _email;}
 	public Boolean getIsAdmin(){ return _isAdmin;}
 	public String getRole() { return _role;}
 	public Date getLastconnection() {return _lastConnection;}
 	public Integer getLastDC() {return _lastDC;}
 	public String getFormatedLastCo() { return this.getLastconnection().toString();}
 
 
 	public void setId(int _id) {this._id = _id;}
 	public void setLogin(String _login) {this._login = _login;}
 	public void setPwd(String _pwd) {this._pwd = _pwd;}
 	public void setFName(String _fName) {this._fName = _fName;}
 	public void setLName(String _lName) {this._lName = _lName;}
 	public void setAge(int _age) {this._age = _age;}
 	public void setEmail(String _email) {this._email = _email;}
 	public void setIsAdmin(Boolean _isAdmin){
 		this._isAdmin = _isAdmin; 
 		this.setRole(this._isAdmin? "admin" : "");
 	}
 	public void setRole(String role) {this._role = role;}
 	public void setLastConnection(Date _lastConnection) {this._lastConnection = _lastConnection;}
 	public void setLastDC(Integer _lastDC){this._lastDC = _lastDC;}
 	
 	private void setInfos(int id, String login, String pwd, String fName, String lName, int age, String email, Boolean isAdmin) {
 		this.setId(id);
 		this.setLogin(login);
 		this.setPwd(pwd);
 		this.setFName(fName);
 		this.setLName(lName);
 		this.setAge(age);
 		this.setEmail(email);
 		this.setIsAdmin(isAdmin);
 	}
 	
 	public String toString() {
 		String toS = "User id: " + this._id + "\n";
 		toS += "login: " + this._login + "::" + this._pwd + "\n";
 		toS += "Name " + this._fName + " " + this._lName + "\n"; 
 		toS += "age: " + this._age + "\n";
 		toS += "email: " + this._email + "\n";
 		toS += "isAdmin: " + this._isAdmin + "\n";
 		toS += "LastDc: " + this._lastConnection + "\n";
 		toS += "LastDur: " + this._lastDC + "\n";
 		return toS;
 	}
 	
 	public void clone(Model item) {
 		User u = (User) item;
 		this.setInfos(u.getId(), u.getLogin(), u.getPwd(), u.getFName(), u.getLName(), u.getAge(), u.getEmail(), u.getIsAdmin());
 	}
 	
 	
 	
 	
 	
 }
