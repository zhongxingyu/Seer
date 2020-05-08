 package org.rulez.magwas.worldmodel;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 
 @Entity
 @Table(name="object")
 public class BaseObject implements Serializable {
 	
 	public BaseObject() {
 		super();
 	}
 	
 	public BaseObject(Element node, Session session) throws InputParseException {
 		if (node == null) {
 			throw new InputParseException("got a null");
 		}
 		String cidstring = node.getAttribute("id");
 		if (cidstring.equals("")) {
 			throw new InputParseException("object should have an ID");
 		}
 		BaseObject obj = getBaseObjectByCompositeId(cidstring, session);
 		if (obj != null) {
 			throw new InputParseException("there is already an object with this id");
 		}
 		List<String> r = parseCompositeId(cidstring);
 		this.setId(Value.getValueByValue(r.get(0), session));
 		String versionstring = r.get(1);
 		if ((versionstring != null) && !versionstring.equals("")) {
 			this.setVersion(Value.getValueByValue(versionstring, session));			
 		}
 		String typestring = node.getAttribute("type");
 		if (!typestring.equals("")) {
 			BaseObject tobj = getBaseObjectByCompositeId(typestring,session);
 			this.setType(tobj);
 		}
 		String sourcestring = node.getAttribute("source");
 		if (!sourcestring.equals("")) {
 			BaseObject tobj = getBaseObjectByCompositeId(sourcestring,session);
 			this.setSource(tobj);
 		}
 		String deststring = node.getAttribute("dest");
 		if (!deststring.equals("")) {
 			BaseObject tobj = getBaseObjectByCompositeId(deststring,session);
 			this.setDest(tobj);
 		}
 		String valuestring = node.getFirstChild().getNodeValue();
 		if (!valuestring.equals("")) {
 			Value tobj = Value.getValueByValue(valuestring,session);
 			this.setValue(tobj);
 		}
 		session.save(this);
 	}
 	
 	private static List<String> parseCompositeId(String cidstring) throws InputParseException {
 		String[] parts = cidstring.split(":");
 		ArrayList<String> r = new ArrayList<String>();
 		if ( ( parts.length < 1 ) || (parts.length > 2)) {
 			throw new InputParseException("object should have a valid ID");
 		}
 		r.add(parts[0]);
 		String versionstring = null;
 		if ( parts.length == 2) {
 			versionstring = parts[1];
 		}
 		r.add(versionstring);
 		return r;
 		
 	}
 	public static BaseObject getBaseObjectByCompositeId(String cidstring, Session session) throws InputParseException {
 		List<String> r = parseCompositeId(cidstring);
 
 		Value idvalue = Value.getValueByValue(r.get(0), session);
 		Value versionvalue;
 		Query query;
 		if (r.get(1) == null) {
 			versionvalue = null;
 			query = session.createQuery("from BaseObject where theid = :id and version is null");
 		} else {
 			query = session.createQuery("from BaseObject where theid = :id and version = :version");
 			versionvalue = Value.getValueByValue(r.get(1), session);
 			query.setParameter("version", versionvalue);
 		}
 		query.setParameter("id", idvalue);
 		List<?> l  = query.list();
 		BaseObject obj;
 		if (l.isEmpty()){
 			obj = null;
 		} else {
 			obj = (BaseObject) query.list().get(0);
 		}
 		return obj;
 	}
 	
 	private static final long serialVersionUID = 1L;
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	@Column
 	private Integer physid;
 
 	@ManyToOne
 	@JoinColumn
 	private Value theid;
 
 	@ManyToOne
 	@JoinColumn
 	private Value version;
 
 	@ManyToOne
 	@JoinColumn
 	private BaseObject type;
 
 	@ManyToOne
 	@JoinColumn
 	private BaseObject source;
 
 	@ManyToOne
 	@JoinColumn
 	private BaseObject dest;
 
 	@ManyToOne
 	@JoinColumn
 	private Value value;
 
 	/**
 	 * @return the physid
 	 */
 	public Integer getPhysid() {
 		if (this.physid == null) {
 			throw new UnsupportedOperationException("You should persist before getting physid");
 		}
 		return physid;
 	}
 
 	/**
 	 * @param physid the physid to set
 	 */
 	public void setPhysid(int physid) {
 
 		this.physid = physid;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public Value getId() {
 		if (this.theid == null) {
 			throw new UnsupportedOperationException("Object without ID");
 		}
 		return theid;
 	}
 
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(Value id) {
 		this.theid = id;
 	}
 
 	/**
 	 * @return the version
 	 */
 	public Value getVersion() {
 		return version;
 	}
 
 	/**
 	 * @param version the version to set
 	 */
 	public void setVersion(Value version) {
 		this.version = version;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public BaseObject getType() {
 		return type;
 	}
 
 	/**
 	 * @param type the type to set
 	 */
 	public void setType(BaseObject type) {
 		this.type = type;
 	}
 
 	/**
 	 * @return the source
 	 */
 	public BaseObject getSource() {
 		return source;
 	}
 
 	/**
 	 * @param source the source to set
 	 */
 	public void setSource(BaseObject source) {
 		this.source = source;
 	}
 
 	/**
 	 * @return the dest
 	 */
 	public BaseObject getDest() {
 		return dest;
 	}
 
 	/**
 	 * @param dest the dest to set
 	 */
 	public void setDest(BaseObject dest) {
 		this.dest = dest;
 	}
 
 	/**
 	 * @return the value
 	 */
 	public Value getValue() {
 		return value;
 	}
 
 	/**
 	 * @param value the value to set
 	 */
 	public void setValue(Value value) {
 		this.value = value;
 	}
 
 	public String getCompositeId(){
 		String idstring = this.getId().getValue();
 		String versionstring;
 		if (this.version == null ) {
 			versionstring = "";
 		} else {
 			versionstring = ":"+this.getVersion().getValue();
 		}
 		return idstring + versionstring;
 	}
 	
 	private static void setAttrIf(Element e, String name, BaseObject value) {
 		if(value != null) {
 			e.setAttribute(name, value.getCompositeId());
 		}
 	}
 
 	public Element toXML(Document doc) {
 		Element e = doc.createElement("BaseObject");
 		e.setAttribute("id", this.getCompositeId());
 		setAttrIf(e,"type",type);
 		setAttrIf(e,"source",source);
 		setAttrIf(e,"dest",dest);
 		if(value != null) {
 			Text tn = doc.createTextNode(value.getValue());
 			e.appendChild(tn);
 		}
 		return e;
 	}
 	
 	
 
 }
