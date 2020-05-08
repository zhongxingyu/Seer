 package com.gentics.cr;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.datasource.Datasource;
 import com.gentics.api.lib.exception.UnknownPropertyException;
 import com.gentics.api.lib.expressionparser.filtergenerator.DatasourceFilter;
 import com.gentics.api.lib.resolving.PropertyResolver;
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.cr.portalnode.expressions.ExpressionParserHelper;
 import com.gentics.cr.util.AccessibleBean;
 import com.gentics.lib.content.GenticsContentObjectImpl;
 
 /**
  * Rosolveable Proxy Class. As Resolvsables are not serializable this class gets
  * a resolvable and a list of attributes and stores these for further usage as
  * serializable bean.
  * 
  * This class also provides various methods to access attributes
  * 
  * Last changed: $Date: 2010-04-01 15:24:02 +0200 (Do, 01 Apr 2010) $
  * 
  * @version $Revision: 541 $
  * @author $Author: supnig@constantinopel.at $
  * 
  */
 public class CRResolvableBean extends AccessibleBean implements Serializable, Resolvable {
 
 	/**
 	 * object type of gentics content repository for file objects.
 	 */
 	public static final String DEFAULT_FILE_TYPE = "10008";
 
 	/**
 	 * object type of gentics content repository for page objects.
 	 */
 	public static final String DEFAULT_PAGE_TYPE = "10007";
 
 	/**
 	 * object type of gentics content repository for folder objects.
 	 */
 	public static final String DEFAULT_DIR_TYPE = "10002";
 
 	/**
 	 * generated unique serial version id.
 	 */
 	private static final long serialVersionUID = -8743515908056719834L;
 
 	/**
 	 * Log4j logger.
 	 */
 	private final static Logger LOGGER = Logger.getLogger(CRResolvableBean.class);
 
 	private Collection<CRResolvableBean> childRepository;
 
 	private ConcurrentHashMap<String, Object> attrMap;
 
 	private String contentid;
 
 	private String obj_id;
 
 	private String obj_type;
 
 	private String mother_id;
 
 	private String mother_type;
 
 	/**
 	 * Resolvable wrapped in this CRResolvableBean.
 	 */
 	private Resolvable resolvable;
 
 	/**
 	 * Populate the child elements with the given collection of CRResolvableBeans.
 	 * 
 	 * @param childRep - TODO javadoc
 	 */
 	public void fillChildRepository(final Collection<CRResolvableBean> childRep) {
 		this.childRepository.addAll(childRep);
 	}
 
 	/**
 	 * Get the Child elements.
 	 * 
 	 * @return collection of child elements.
 	 */
 	public Collection<CRResolvableBean> getChildRepository() {
 		return (this.childRepository);
 	}
 
 	/**
 	 * Returns if this CRResolvableBean has a filled children list.
 	 * @return default:false
 	 */
 	public boolean hasChildren() {
 		boolean children = false;
 		if (this.childRepository != null && this.childRepository.size() > 0) {
 			children = true;
 		}
 		return children;
 	}
 
 	/**
 	 * Set the child elements to the given collection of CRResolvableBeans.
 	 * 
 	 * @param children
 	 */
 	public void setChildRepository(Collection<CRResolvableBean> children) {
 		this.childRepository = children;
 	}
 
 	/**
 	 * Create new instance of CRResolvableBean.
 	 */
 	public CRResolvableBean() {
 		this.contentid = "10001";
 	}
 
 	/**
 	 * Create new instance of CRResolvableBean. This will generate an empty
 	 * CRResolvableBean with only the contentid set.
 	 * 
 	 * @param id initialize the bean with the given contentid
 	 */
 	public CRResolvableBean(final String id) {
 		this.contentid = id;
 	}
 
 	/**
 	 * Create new instance of CRResolvableBean.
 	 * 
 	 * @param resolvable
 	 *            Sets the given resolvable as basis for the CRResolvableBean If
 	 *            obj_type of resolvable is 10008 it sets the attribute array to {
 	 *            "binarycontent", "mimetype" }, otherwise to { "binarycontent",
 	 *            "mimetype" } If you want to be more specific about the
 	 *            attribute array, use public CRResolvableBean(Resolvable
 	 *            resolvable, String[] attributeNames) instead
 	 */
 	public CRResolvableBean(final Resolvable resolvable) {
 		// TODO This is ugly => make more beautiful
 		Object objTypeObject = resolvable.get("obj_type");
 		String objType = null;
 		if (objTypeObject instanceof String) {
 			objType = (String) objTypeObject;
 		} else if (objTypeObject != null) {
 			objType = objTypeObject.toString();
 		}
 		if (DEFAULT_FILE_TYPE.equals(objType)) {
 			init(resolvable, new String[] { "binarycontent", "mimetype" });
 		} else {
 			init(resolvable, new String[] { "content", "mimetype" });
 		}
 	}
 
 	/**
 	 * make a CRResolvableBean out of a Resolvable.
 	 * 
 	 * @param resolvable The Resolvable to be converted to a CRResolveableBean
 	 * @param attributeNames The attributenames as an array of strings that should be fetched from the Resolveable
 	 */
 	public CRResolvableBean(final Resolvable resolvable, final String[] attributeNames) {
 		init(resolvable, attributeNames);
 	}
 
 	/**
 	 * Initialize the CRResolvableBean with the Resolvable and populate elements /
 	 * sets the Resolvable as member.
 	 * 
 	 * @param givenResolvable
 	 * @param attributeNames
 	 */
 	private void init(final Resolvable givenResolvable, final String[] attributeNames) {
 		if (givenResolvable != null) {
 			this.resolvable = givenResolvable;
 			this.childRepository = new Vector<CRResolvableBean>();
 			this.contentid = (String) givenResolvable.get("contentid");
 
 			if (givenResolvable.get("obj_id") != null) {
 				this.obj_id = givenResolvable.get("obj_id").toString();
 			}
 			if (givenResolvable.get("obj_type") != null) {
 				this.obj_type = givenResolvable.get("obj_type").toString();
 			}
 			if (givenResolvable.get("mother_obj_id") != null) {
 				this.mother_id = givenResolvable.get("mother_obj_id").toString();
 			}
 			if (givenResolvable.get("mother_obj_type") != null) {
 				this.mother_type = givenResolvable.get("mother_obj_type").toString();
 			}
 
 			this.attrMap = new ConcurrentHashMap<String, Object>();
 			if (attributeNames != null) {
 				ArrayList<String> attributeList = new ArrayList<String>(Arrays.asList(attributeNames));
 				String[] cleanedAttributeNames = attributeList.toArray(attributeNames);
 				if (attributeList.contains("binarycontenturl")) {
 					this.attrMap.put("binarycontenturl", "ccr_bin?contentid=" + this.contentid);
 					attributeList.remove("binarycontenturl");
 					cleanedAttributeNames = attributeList.toArray(attributeNames);
 				}
 
 				for (int i = 0; i < cleanedAttributeNames.length; i++) {
 					// we have to inspect returned attribute for containing not
 					// serializable objects (Resolvables) and convert them into
 					// CRResolvableBeans
 					try {
 						// THE FOLLOWING CALL DOES NOT THROW AN EXCEPTION
 						// WHEN THE DB CONNECTION IS LOST
 						Object o = inspectResolvableAttribute(PropertyResolver.resolve(
 							givenResolvable,
 							cleanedAttributeNames[i]));
 						if (o != null) {
 							this.attrMap.put(cleanedAttributeNames[i], o);
 						}
 					} catch (UnknownPropertyException e) {
 						Object o = inspectResolvableAttribute(givenResolvable.get(cleanedAttributeNames[i]));
 						if (o != null) {
 							this.attrMap.put(cleanedAttributeNames[i], o);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Helper Method to inspect Attributes given from PropertyResolver or
 	 * Resolvables theirself for containing not serializable Resolvables.
 	 * 
 	 * @param resolvableAttribute
 	 *            The attribute should be inspected
 	 * @return the cleaned up attribute. All Resolvables are converted to
 	 *         CRResolvableBeans. The attribute should be serializable
 	 *         afterwards.
 	 */
 	@SuppressWarnings("unchecked")
 	private Object inspectResolvableAttribute(Object resolvableAttribute) {
 		if (resolvableAttribute instanceof Collection) {
 			// in Collections we must inspect all elements. We assume it is a
 			// parameterized Collection
 			// and therefore we quit if the first Object in the Collection is
 			// not a Resolvable
 			ArrayList<CRResolvableBean> newAttributeObject = new ArrayList<CRResolvableBean>();
 			for (Iterator<Object> it = ((Collection<Object>) resolvableAttribute).iterator(); it.hasNext();) {
 				Object object = it.next();
 				if (object instanceof Resolvable) {
 					newAttributeObject.add(new CRResolvableBean((Resolvable) object, new String[] {}));
 				} else {
 					return resolvableAttribute;
 				}
 			}
 			return newAttributeObject;
 		} else if (resolvableAttribute instanceof Resolvable) {
 			return new CRResolvableBean((Resolvable) resolvableAttribute, new String[] {});
 		} else {
 			return resolvableAttribute;
 		}
 	}
 
 	/**
 	 * Gets the fetched attributes as Map.
 	 * 
 	 * @return attribute map
 	 */
 	public Map<String, Object> getAttrMap() {
 		return attrMap;
 	}
 
 	/**
 	 * Sets the attributes of the CRResolvableBean to the given map of
 	 * attributes.
 	 * 
 	 * @param attr
 	 *            Checks if attr is instance of ConcurrentHashMap. If true, it sets attr
 	 *            as the new attribute map. If false, a new ConcurrentHashMap with the
 	 *            given map as basis is being generated.
 	 */
 	public void setAttrMap(Map<String, Object> attr) {
 		if (attr instanceof ConcurrentHashMap<?, ?>) {
 			this.attrMap = (ConcurrentHashMap<String, Object>) attr;
 		} else {
 			this.attrMap = new ConcurrentHashMap<String, Object>(attr);
 		}
 	}
 
 	/**
 	 * @return the contentid of the bean, this is usually used as unique
 	 *         identifier within the gentics frameworks.
 	 */
 	public String getContentid() {
 		if (contentid != null || resolvable == null) {
 			return contentid;
 		} else {
 			Object resContentid = resolvable.get("contentid");
 			if (resContentid != null) {
 				return resContentid.toString();
 			} else {
 				return null;
 			}
 		}
 	}
 
 	/**
 	 * Sets the contentid of the CRResolvableBean.
 	 * @param id - contentid
 	 */
 	public void setContentid(final String id) {
 		this.contentid = id;
 	}
 
 	/**
 	 * Get the parent folder of the current bean.
 	 * This method uses non api objects and should not be used!
 	 * @return GenticsContentObjectImpl representing the parent folder.
 	 */
 	@Deprecated
 	public GenticsContentObjectImpl getMother() {
 		try {
 			GenticsContentObjectImpl r = (GenticsContentObjectImpl) resolvable;
 			Datasource datasource = r.getDatasource();
 			DatasourceFilter filter = ExpressionParserHelper.createDatasourceFilter("object.contentid == \"10002."
 					+ mother_id + "\"" + " && object.obj_type == 10002", datasource);
 			Collection<?> parentFolder = datasource.getResult(filter, new String[] { "contentid" });
 			return (GenticsContentObjectImpl) (parentFolder.iterator().next());
 		} catch (Exception e) {
 			LOGGER.error("Could not retreive mother folder (" + mother_type + "." + mother_id + ") of current bean: "
 					+ contentid);
 			return null;
 		}
 	}
 
 	/**
 	 * Gets the mother contentid of the CRResolvableBean.
 	 * @return motherid
 	 */
 	public String getMother_id() {
 		return mother_id;
 	}
 
 	/**
 	 * Seths the mother contentid of the CRResolvableBean.
 	 * @param id
 	 */
 	public void setMother_id(String id) {
 		this.mother_id = id;
 	}
 
 	/**
 	 * Gets the type of the mother object.
 	 * @return mothertype
 	 */
 	public String getMother_type() {
 		return mother_type;
 	}
 
 	/**
 	 * Sets the type of the mother object.
 	 * @param type
 	 */
 	public void setMother_type(String type) {
 		this.mother_type = type;
 	}
 
 	/**
 	 * Gets the id of the object.
 	 * @return objectid
 	 */
 	public String getObj_id() {
 		return obj_id;
 	}
 
 	/**
 	 * Sets the id of the object.
 	 * @param id
 	 */
 	public void setObj_id(String id) {
 		this.obj_id = id;
 	}
 
 	/**
 	 * Gets the type of the Object.
 	 * @return objecttype
 	 */
 	public String getObj_type() {
 		return obj_type;
 	}
 
 	/**
 	 * Sets the type of the object.
 	 * @param type
 	 */
 	public void setObj_type(String type) {
 		this.obj_type = type;
 	}
 
 	/**
 	 * Returns true if this CRResolvableBean holds binary content.
 	 * @return boolean
 	 */
 	public boolean isBinary() {
 		// TODO this is ugly => make more beautiful
 		if (this.attrMap.containsKey("binarycontent") && this.attrMap.get("binarycontent") != null) {
 			return (true);
 		} else if (DEFAULT_FILE_TYPE.equals(this.getObj_type())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Gets the mimetype of the CRResolvableBean.
 	 * @return mimetype set in the attrMaps of the resolvable as string.
 	 */
 	public String getMimetype() {
 		return (String) this.attrMap.get("mimetype");
 	}
 
 	/**
 	 * Returns the content attribute as string.
 	 * @return content
 	 */
 	public String getContent() {
 		Object o = this.get("content");
 		try {
 			return (String) o;
 		} catch (ClassCastException ex) {
 			// If type is not String then assume that byte[] would do the trick
 			// Not very clean
 			return new String((byte[]) o);
 		}
 	}
 
 	/**
 	 * Gets the Content as String using the given encoding.
 	 * @param encoding
 	 *            Has to be a supported charset US-ASCII Seven-bit ASCII, a.k.a.
 	 *            ISO646-US, a.k.a. the Basic Latin block of the Unicode
 	 *            character set ISO-8859-1 ISO Latin Alphabet No. 1, a.k.a.
 	 *            ISO-LATIN-1 UTF-8 Eight-bit UCS Transformation Format UTF-16BE
 	 *            Sixteen-bit UCS Transformation Format, big-endian byte order
 	 *            UTF-16LE Sixteen-bit UCS Transformation Format, little-endian
 	 *            byte order UTF-16 Sixteen-bit UCS Transformation Format, byte
 	 *            order identified by an optional byte-order mark
 	 * @return content
 	 */
 	public String getContent(String encoding) {
 		Object bValue = this.get("content");
 		String value = "";
 		if (bValue != null && bValue.getClass() == String.class) {
 			value = (String) bValue;
 		} else {
 			try {
 				value = new String(getBytes(bValue), encoding);
 
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return value;
 	}
 
 	/**
 	 * Gets the binary content if it is set otherwise returns null.
 	 * @return binary content as array of bytes
 	 */
 	public byte[] getBinaryContent() {
 		Object o = this.get("binarycontent");
 		if (o instanceof String) {
 			return ((String) o).getBytes();
 		} else {
 			return (byte[]) o;
 		}
 	}
 
 	/**
 	 * Gets the binary content as InputStream if it is set otherwise returns null.
 	 * @return returns a ByteArrayInputStream by default containing the binaryContent
 	 */
 	public InputStream getBinaryContentAsStream() {
 		byte[] buf = getBinaryContent();
 		InputStream os = null;
 		if (buf != null) {
 			os = new ByteArrayInputStream(getBinaryContent());
 		}
 		return os;
 	}
 
 	/**
 	 * Gets the value of the requested attribute Will first try to fetch the
 	 * attribute from the Beans attribute array. If attribute can not be fetched
 	 * and a base resolvable is set, then it tries to fetch the attribute over
 	 * the resolvable
 	 * 
 	 * @param attribute requested attribute name
 	 * @return value of attribute or null if value is not set
 	 */
 	public Object get(final String attribute) {
 		if ("contentid".equalsIgnoreCase(attribute)) {
 			return this.getContentid();
 		} else if ("obj_type".equals(attribute) && !attrMap.containsKey("obj_type") && resolvable == null) {
 			return this.getObj_type();
 		} else if ("obj_id".equals(attribute) && !attrMap.containsKey("obj_id") && resolvable == null) {
 			return this.getObj_type();
 		} else if (this.attrMap != null && this.attrMap.containsKey(attribute)) {
 			return this.attrMap.get(attribute);
 		} else if (this.resolvable != null) {
 			// if we are returning an attribute from an resolvable we must
 			// inspect it
 			// for containing not serializable Objects
 			return inspectResolvableAttribute(this.resolvable.get(attribute));
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * @return contained Resolvable if there is any.
 	 */
 	public final Resolvable getResolvable() {
 		return resolvable;
 	}
 
 	/**
 	 * Sets the value of the requested attribute.
 	 * 
 	 * @param attribute - requested attribute name
 	 * @param obj - value of attribute
 	 */
 	public void set(final String attribute, final Object obj) {
 		if ("contentid".equals(attribute)) {
 			if (obj != null) {
 				setContentid(obj.toString());
 			} else {
 				setContentid(null);
 			}
 		} else if (obj != null && attribute != null) {
 			if (this.attrMap == null) {
 				this.attrMap = new ConcurrentHashMap<String, Object>();
 			}
 			this.attrMap.put(attribute, obj);
 		}
 	}
 
 	/**
 	 * remove/unset the attribute with the specified name.
 	 * 
 	 * @param attributeName - name of the attribute to remove
 	 */
 	public final void remove(final String attributeName) {
		if (this.attrMap != null && this.attrMap.contains(attributeName)) {
 			this.attrMap.remove(attributeName);
 		}
 	}
 
 	/**
 	 * Converts an Object to an array of bytes.
 	 * 
 	 * @param obj Object to convert
 	 * @return byte[] - converted object
 	 */
 	private byte[] getBytes(final Object obj) throws java.io.IOException {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		ObjectOutputStream oos = new ObjectOutputStream(bos);
 		oos.writeObject(obj);
 		oos.flush();
 		oos.close();
 		bos.close();
 		byte[] data = bos.toByteArray();
 		return data;
 	}
 
 	/**
 	 * CRResolvableBean is always able to resolve properties.
 	 * 
 	 * @return <code>true</code>
 	 */
 	public final boolean canResolve() {
 		return true;
 	}
 
 	/**
 	 * Gets the value of the requested attribute. Alias for get(String key)
 	 * 
 	 * @param key requested attribute name
 	 * @return value of attribute
 	 */
 	public Object getProperty(String key) {
 		return get(key);
 	}
 
 	/**
 	 * A String representation of this CRResolvableBean instance.
 	 * 
 	 * @return String contentid
 	 */
 	public String toString() {
 		return this.getContentid();
 	}
 }
