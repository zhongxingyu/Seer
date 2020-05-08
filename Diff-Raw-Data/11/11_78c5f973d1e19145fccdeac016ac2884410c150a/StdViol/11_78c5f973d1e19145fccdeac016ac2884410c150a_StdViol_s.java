 package hudson.plugins.cpptest.parser;
 
 /**
  * Java Bean class for a file of the Cpptest format.
  *
  * @author Ulli Hafner
  * 
  * NQH: adapt for Cpptest
  */
 public class StdViol {
     /** Name of the violation. */
     private String rule;
     /** language */
     private String lang;
     /** Severity of the violation. */
     private String sev;
     /** Message of the violation. */
     private String msg;
     /** Line of the violation. */
     private Integer ln;
     /** File location */
     private String locFile;
     /** Start Line of the violation. */
     private Integer locStartln;
     /** End Line of the violation. */
     private Integer locEndLn;
     /** Category of the violation. */
     private String cat;
     
     
 	public void setRule(String rule) {
 		this.rule = rule;
 	}
 
 	public String getRule() {
 		return rule;
 	}
 
 	public void setLang(String lang) {
 		this.lang = lang;
 	}
 
 	public String getLang() {
 		return lang;
 	}
 
 	public void setSev(String sev) {
 		this.sev = sev;
 	}
 
 	public String getSev() {
 		return sev;
 	}
 
 	public void setMsg(String msg) {
 		this.msg = msg;
 	}
 
 	public String getMsg() {
 		return msg;
 	}
 
 	public void setLn(Integer ln) {
 		this.ln = ln;
 	}
 
 	public Integer getLn() {
 		return ln;
 	}
 
 	public void setLocFile(String locFile) {
 		this.locFile = locFile;
 	}
 
 	public String getLocFile() {
 		return locFile;
 	}
 
 	public void setLocStartln(Integer locStartln) {
 		this.locStartln = locStartln;
 	}
 
 	public Integer getLocStartln() {
 		return locStartln;
 	}
 
 	public void setLocEndLn(Integer locEndLn) {
 		this.locEndLn = locEndLn;
 	}
 
 	public Integer getLocEndLn() {
 		return locEndLn;
 	}
 
 	public String getCat() {
 		return cat;
 	}
 
 	public void setCat(String cat) {
 		this.cat = cat;
 	}
     
 }
 
