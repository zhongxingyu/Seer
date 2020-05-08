 package be.occam.colloseum.action;
 
 import java.util.Date;
 
import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import be.occam.colloseum.person.Person;
 
@XmlTransient
public class Action<T> {
 	
 	protected String id;
 	
 	protected Person actor;
 	
 	protected T subject;
 	
 	protected Date date;
 
 	public String getId() {
 		return id;
 	}
 
 	public Action<T> setId(String id) {
 		this.id = id;
 		return this;
 	}
 
 	public Person getActor() {
 		return actor;
 	}
 
 	public Action<T> setActor(Person actor) {
 		this.actor = actor;
 		return this;
 	}
 
 	public T getSubject() {
 		return subject;
 	}
 
 	public Action<T> setSubject(T subject) {
 		this.subject = subject;
 		return this;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public Action<T> setDate(Date date) {
 		this.date = date;
 		return this;
 	}
 	
 }
