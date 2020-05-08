 /**
  * 
  */
 package nz.ac.vuw.ecs.rprofs.server.domain;
 
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.Version;
 
 import nz.ac.vuw.ecs.rprofs.server.domain.id.AttributeId;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.ObjectId;
 
 /**
  * @author Stephen Nelson (stephen@sfnelson.org)
  *
  */
 @Entity
 @Table( name = "instances" )
 @NamedQueries({
 	@NamedQuery(name="numInstances", query="select count(I) from Instance I"),
 	@NamedQuery(name="allInstances", query="select count(I) from Instance I"),
 	@NamedQuery(name="numInstancesForType", query="select count(I) from Instance I where I.type = :type"),
	@NamedQuery(name="instancesForType", query="select I from Instance I where I.type = :type"),
 })
 public class Instance implements DataObject<Instance> {
 
 	public static final java.lang.Class<Instance> TYPE = Instance.class;
 
 	@EmbeddedId
 	private ObjectId id;
 
 	@Version
 	private int version;
 
 	@ManyToOne
 	private Class type;
 
 	@ManyToOne
 	protected Method constructor;
 
 	public Instance() {}
 
 	public Instance(ObjectId id, Class type, Method constructor) {
 		this.id = id;
 		this.type = type;
 		this.constructor = constructor;
 	}
 
 	public ObjectId getId() {
 		return id;
 	}
 
 	public Integer getVersion() {
 		return version;
 	}
 
 	public Class getType() {
 		return type;
 	}
 
 	public ClassId getTypeId() {
 		return type.getId();
 	}
 
 	public void setType(Class type) {
 		this.type = type;
 	}
 
 	public Method getConstructor() {
 		return constructor;
 	}
 
 	public AttributeId<Method> getConstructorId() {
 		return constructor.getId();
 	}
 
 	public void setConstructor(Method m) {
 		this.constructor = m;
 	}
 
 	public long getIndex() {
 		return id.getId();
 	}
 
 	public short getThreadIndex() {
 		return id.getThread();
 	}
 
 	public int getInstanceIndex() {
 		return id.getIndex();
 	}
 }
