 package org.alt60m.fsk2.model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.*;
 
 import org.hibernate.CallbackException;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.classic.Lifecycle;
 import org.hibernate.classic.Validatable;
 import org.hibernate.classic.ValidationFailure;
 
 @MappedSuperclass
 public abstract class Persistent<T extends Persistent> implements Lifecycle, Validatable, Serializable {
 
 	
 	private static SessionFactory sessionFactory;
 			
 	private Class<T> persistentClass;
 	
 	private Integer id;
 	private Integer version;
 	private Date createdAt;
 	private Date updatedAt;
 
 	public Persistent(Class<T> persistentClass)
 	{
 		this.persistentClass = persistentClass; 
 	}
 	
 
 	@SuppressWarnings("unchecked")
 	public T findById(Integer id)
 	{
		return (T) sessionFactory.getCurrentSession().load(getPersistentClass(), id);
 	}
 	
 	
 	
 
 	public Integer persist() throws HibernateException {
 		sessionFactory.getCurrentSession().saveOrUpdate(this);
 		return id;
 	}
 
 	public void delete() throws HibernateException {
 		sessionFactory.getCurrentSession().delete(this);
 	}
 
 	public void load() throws HibernateException {
 		Session session = sessionFactory.getCurrentSession();
 		if (!session.contains(this)) {
 			session.load(this, id);
 		}
 		else {
 			T persisted = findById(id);
 			session.evict(persisted);
 			session.save(this);
 			session.merge(persisted);
 		}
 	}
 
 	
 	public boolean onSave(Session s) throws CallbackException {
 		return NO_VETO;
 	}
 
 	public boolean onDelete(Session s) throws CallbackException {
 		return NO_VETO;
 	}
 
 	public boolean onUpdate(Session s) throws CallbackException {
 		return NO_VETO;
 	}
 
 	public void onLoad(Session s, Serializable id) {
 	}
 	
 	public void validate() throws ValidationFailure {
 	}
 
 	public static SessionFactory getSessionFactory() {
 		return sessionFactory;
 	}
 
 	public static void setSessionFactory(SessionFactory sessionFactory) {
 		Persistent.sessionFactory = sessionFactory;
 	}
 	
 	protected void copy(Persistent p)
 	{
 		setId(p.getId());
 		setVersion(p.getVersion());
 	}
 
	@Transient
	public Class<T> getPersistentClass() {
		return persistentClass;
	}

	public void setPersistentClass(Class<T> persistentClass) {
		this.persistentClass = persistentClass;
	}
	

 	@Id
 	@GeneratedValue
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	@Version
 	public Integer getVersion() {
 		return version;
 	}
 
 	public void setVersion(Integer version) {
 		this.version = version;
 	}
 
 	@Column(name="created_at")
 	public Date getCreatedAt() {
 		return createdAt;
 	}
 
 	public void setCreatedAt(Date createdAt) {
 		this.createdAt = createdAt;
 	}
 
 	@Column(name="updated_at")
 	public Date getUpdatedAt() {
 		return updatedAt;
 	}
 
 	public void setUpdatedAt(Date updatedAt) {
 		this.updatedAt = updatedAt;
 	}
 	
 	
 	
 }
