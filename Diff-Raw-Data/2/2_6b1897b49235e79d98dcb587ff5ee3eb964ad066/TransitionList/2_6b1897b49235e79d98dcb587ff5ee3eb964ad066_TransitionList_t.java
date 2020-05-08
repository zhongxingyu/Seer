 package gr.uoi.cs.daintiness.hecate.transitions;
 
 import java.util.ArrayList;
 
 import javax.xml.bind.annotation.XmlAnyElement;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlRootElement(name="transition")
 public class TransitionList {
 	
 	@XmlAttribute(name="oldVersion")
 	private String oldVersion;
 	@XmlAttribute(name="newVersion")
 	private String newVersion;
	@XmlAnyElement(lax=true)
 	private ArrayList<Transition> list;
 	
 	public TransitionList() {
 		this.oldVersion = null;
 		this.newVersion = null;
 	}
 	
 	public TransitionList(String oldVersion, String newVersion) {
 		this.oldVersion = oldVersion;
 		this.newVersion = newVersion;
 		list = new ArrayList<Transition>();
 	}
 	
 	public void add(Transition in) {
 		this.list.add(in);
 	}
 	
 	public Transition get(int index) {
 		return list.get(index);
 	}
 
 	/**
 	 * @return the oldVersion
 	 */
 	public String getOldVersion() {
 		return oldVersion;
 	}
 
 	/**
 	 * @return the newVersion
 	 */
 	public String getNewVersion() {
 		return newVersion;
 	}
 }
