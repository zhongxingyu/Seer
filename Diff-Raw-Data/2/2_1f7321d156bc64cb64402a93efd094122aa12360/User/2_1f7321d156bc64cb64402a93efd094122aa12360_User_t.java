 package name.mikkoostlund.spring_mvc_experiment;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class User {
	static Log log = LogFactory.getLog(User.class.getName());
 	private String _name;
 	private String password;
 	private final String _origName;
 
 	public User() {
 		_origName = super.toString();
 		_name = super.toString();
 		new Integer(-1);
 		log.info("#### new "+ toString());
 	}
 
 	public String getName() {
 		log.info("#### "+toString() +".getName() ==> "+ _name);
 		return _name;
 	}
 	public void setName(String name) {
 		log.info("#### "+toString() +".setName(\""+ name +"\")");
 		this._name = name;
 	}
 
 	public String getPassword() {
 		log.info("#### "+toString() +".getPassword() ==> "+ password);
 		return password;
 	}
 	public void setPassword(String password) {
 		log.info("#### "+ toString() +".setPassword(\""+ password +"\")");
 		this.password = password;
 	}
 
 	public String toString() {
 		String logName;
 		if (_name.startsWith("name"))
 			logName = _name.substring(44);
 		else	
 			logName = _name;
 		return "User(" + _origName.substring(44) + ":\""+ logName +"\")";
 	}
 }
