 
 package org.gridlab.gridsphere.portlet.jsrimpl;
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.Preference;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.Value;
 
 import javax.portlet.ReadOnlyException;
 import javax.portlet.ValidatorException;
 import javax.portlet.PreferencesValidator;
 import javax.portlet.PortletPreferences;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.io.IOException;
 
 
 /**
  * The <CODE>PortletPreferences</CODE> interface allows the portlet to store
  * configuration data. It is not the
  * purpose of this interface to replace general purpose databases.
  * <p>
  * There are two different types of preferences:
  * <ul>
  * <li>modifiable preferences - these preferences can be changed by the
  *     portlet in any standard portlet mode (<code>EDIT, HELP, VIEW</code>).
  *     Per default every preference is modifiable.
  * <li>read-only preferences - these preferences cannot be changed by the
  *     portlet in any standard portlet mode, but may be changed by administrative modes.
  *     Preferences are read-only, if the are defined in the
  *     deployment descriptor with <code>read-only</code> set to <code>true</code>,
  *     or if the portlet container restricts write access.
  * </ul>
  * <p>
  * Changes are persisted when the <code>store</code> method is called. The <code>store</code> method 
  * can only be invoked within the scope of a <code>processAction</code> call.
  * Changes that are not persisted are discarded when the
  * <code>processAction</code> or <code>render</code> method ends.
  */
 public class PortletPreferencesImpl implements PortletPreferences
 {
 
     //private Map prefsMap = new HashMap();
     private transient Map defaultPrefsMap = new HashMap();
     private transient PreferencesValidator validator = null;
     private transient PersistenceManagerRdbms pm = null;
 
     private String oid = null;
 
     /**
      * The PortletId this data belongs to
      */
     private String portletId = new String();
 
      /**
      * The unique userid this data belongs to
      */
     private String userId = new String();
     // key/value pairs
 
     private Map attributes = new HashMap();
 
     public PortletPreferencesImpl() {}
 
     public PortletPreferencesImpl(org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletPreferences portletPrefs, ClassLoader loader) {
         Map prefsMap = new HashMap();
        if (portletPrefs != null) {
         Preference[] prefs = portletPrefs.getPreference();
         for (int i = 0; i < prefs.length; i++) {
             PersistencePreferenceAttribute ppa = new PersistencePreferenceAttribute();
             prefsMap.put(prefs[i].getName().getContent(), prefs[i]);
             defaultPrefsMap.put(prefs[i].getName().getContent(), prefs[i]);
 
             ppa.setName(prefs[i].getName().getContent());
             String[] vals = new String[prefs[i].getValueCount()];
             Value[] prefVals = prefs[i].getValue();
             for (int j = 0; j < vals.length; j++) {
                 vals[j] = prefVals[j].getContent();
             }
             ppa.setAValues(vals);
             ppa.setReadOnly(Boolean.valueOf(prefs[i].getReadOnly().getContent()).booleanValue());
             attributes.put(ppa.getName(), ppa);
         }
 
         org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PreferencesValidator  v = portletPrefs.getPreferencesValidator();
         if (v != null) {
             String validatorClass = v.getContent();
             if (validatorClass != null) {
                 try {
                     validator = (PreferencesValidator)Class.forName(validatorClass, true, loader).newInstance();
                 } catch (Exception e) {
                     System.err.println("Unable to create validoator: " + validatorClass + "! " + e.getMessage());
                 }
             }
         }
        }
     }
 
     public void setPersistenceManager(PersistenceManagerRdbms pm) {
         this.pm = pm;
     }
 
     public String getOid() {
         return oid;
     }
 
     public void setOid(String oid) {
         this.oid = oid;
     }
 
     /**
      * Returns true, if the value of this key cannot be modified by the user.
      * <p>
      * Modifiable preferences can be changed by the
      * portlet in any standard portlet mode (<code>EDIT, HELP, VIEW</code>).
      * Per default every preference is modifiable.
      * <p>
      * Read-only preferences cannot be changed by the
      * portlet in any standard portlet mode, but inside of custom modes
      * it may be allowed changing them.
      * Preferences are read-only, if they are defined in the
      * deployment descriptor with <code>read-only</code> set to <code>true</code>,
      * or if the portlet container restricts write access.
      *
      * @return  false, if the value of this key can be changed, or
      *          if the key is not known
      *
      * @exception java.lang.IllegalArgumentException
      *         if <code>key</code> is <code>null</code>.
      */
     public boolean isReadOnly(String key) {
         if (key == null) throw new IllegalArgumentException("key is NULL");
         PersistencePreferenceAttribute ppa = (PersistencePreferenceAttribute)attributes.get(key);
         if (ppa == null) return false;
         return ppa.isReadOnly();
     }
 
     /**
      * Returns the first String value associated with the specified key of this preference.
      * If there is one or more preference values associated with the given key
      * it returns the first associated value.
      * If there are no preference values associated with the given key, or the
      * backing preference database is unavailable, it returns the given
      * default value.
      *
      * @param key key for which the associated value is to be returned
      * @param def the value to be returned in the event that there is no
      *            value available associated with this <code>key</code>.
      *
      * @return the value associated with <code>key</code>, or <code>def</code>
      *         if no value is associated with <code>key</code>, or the backing
      *         store is inaccessible.
      *
      * @exception java.lang.IllegalArgumentException
      *         if <code>key</code> is <code>null</code>. (A
      *         <code>null</code> value for <code>def</code> <i>is</i> permitted.)
      *
      * @see #getValues(java.lang.String, java.lang.String[])
      */
     public String getValue(String key, String def) {
         if (key == null) throw new IllegalArgumentException("key is NULL");
         PersistencePreferenceAttribute ppa = (PersistencePreferenceAttribute)attributes.get(key);
         if (ppa == null) return def;
         return ppa.getValue();
     }
 
 
     /**
      * Returns the String array value associated with the specified key in this preference.
      *
      * <p>Returns the specified default if there is no value
      * associated with the key, or if the backing store is inaccessible.
      *
      * <p>If the implementation supports <i>stored defaults</i> and such a
      * default exists and is accessible, it is used in favor of the
      * specified default.
      *
      *
      * @param key key for which associated value is to be returned.
      * @param def the value to be returned in the event that this
      *        preference node has no value associated with <code>key</code>
      *        or the associated value cannot be interpreted as a String array,
      *        or the backing store is inaccessible.
      *
      * @return the String array value associated with
      *         <code>key</code>, or <code>def</code> if the
      *         associated value does not exist.
      *
      * @exception java.lang.IllegalArgumentException if <code>key</code> is <code>null</code>.  (A
      *         <code>null</code> value for <code>def</code> <i>is</i> permitted.)
      *
      * @see #getValue(java.lang.String,java.lang.String)
      */
 
     public String[] getValues(String key, String[] def) {
         if (key == null) throw new IllegalArgumentException("key is NULL");
         PersistencePreferenceAttribute ppa = (PersistencePreferenceAttribute)attributes.get(key);
         if (ppa == null) return def;
         return ppa.getAValues();
     }
 
     /**
      * Associates the specified String value with the specified key in this
      * preference.
      * <p>
      * The key cannot be <code>null</code>, but <code>null</code> values
      * for the value parameter are allowed.
      *
      * @param key key with which the specified value is to be associated.
      * @param value value to be associated with the specified key.
      *
      * @exception  javax.portlet.ReadOnlyException
      *                 if this preference cannot be modified for this request
      * @exception java.lang.IllegalArgumentException if key is <code>null</code>,
      *                 or <code>key.length()</code>
      *                 or <code>value.length</code> are to long. The maximum length
      *                 for key and value are implementation specific.
      *
      * @see #setValues(java.lang.String, java.lang.String[])
      */
     public void setValue(String key, String value)  throws ReadOnlyException {
         if (key == null) throw new IllegalArgumentException("key is NULL");
 
         // TODO use request value to determin if this value can be set
 
         PersistencePreferenceAttribute ppa = (PersistencePreferenceAttribute)attributes.get(key);
         if (ppa == null) {
             ppa = new PersistencePreferenceAttribute();
             ppa.setName(key);
             ppa.setReadOnly(false);
             ppa.setValue(value);
             attributes.put(key, ppa);
         } else {
             ppa.setValue(value);
         }
     }
 
 
     /**
      * Associates the specified String array value with the specified key in this
      * preference.
      * <p>
      * The key cannot be <code>null</code>, but <code>null</code> values
      * in the values parameter are allowed.
      *
      * @param key key with which the  value is to be associated
      * @param values values to be associated with key
      *
      * @exception  java.lang.IllegalArgumentException if key is <code>null</code>, or
      *                 <code>key.length()</code>
      *                 is to long or <code>value.size</code> is to large.  The maximum
      *                 length for key and maximum size for value are implementation specific.
      * @exception  javax.portlet.ReadOnlyException
      *                 if this preference cannot be modified for this request
      *
      * @see #setValue(java.lang.String,java.lang.String)
      */
 
     public void setValues(String key, String[] values) throws ReadOnlyException {
         if (key == null) throw new IllegalArgumentException("key is NULL");
 
         // TODO use request value to determine if this value can be set
         PersistencePreferenceAttribute ppa = (PersistencePreferenceAttribute)attributes.get(key);
         if (ppa == null) {
             ppa = new PersistencePreferenceAttribute();
             ppa.setName(key);
             ppa.setReadOnly(false);
             ppa.setAValues(values);
             attributes.put(key, ppa);
         } else {
             ppa.setAValues(values);
         }
     }
 
     /**
      * Returns all of the keys that have an associated value,
      * or an empty <code>Enumeration</code> if no keys are
      * available.
      *
      * @return an Enumeration of the keys that have an associated value,
      *         or an empty <code>Enumeration</code> if no keys are
      *         available.
      */
     public java.util.Enumeration getNames() {
         return new Enumerator(attributes.keySet().iterator());
     }
 
     /**
      * Returns a <code>Map</code> of the preferences.
      * <p>
      * The values in the returned <code>Map</code> are from type
      * String array (<code>String[]</code>).
      * <p>
      * If no preferences exist this method returns an empty <code>Map</code>.
      *
      * @return     an immutable <code>Map</code> containing preference names as
      *             keys and preference values as map values, or an empty <code>Map</code>
      *             if no preference exist. The keys in the preference
      *             map are of type String. The values in the preference map are of type
      *             String array (<code>String[]</code>).
      */
     public java.util.Map getMap() {
         Map map = new HashMap();
         Iterator it = attributes.keySet().iterator();
         while (it.hasNext()) {
             String key = (String)it.next();
             String[] vals = this.getValues(key, null);
             map.put(key, vals);
         }
         return map;
     }
 
     /**
      * Resets or removes the value associated with the specified key.
      * <p>
      * If this implementation supports stored defaults, and there is such
      * a default for the specified preference, the given key will be
      * reset to the stored default.
      * <p>
      * If there is no default available the key will be removed.
      *
      * @param  key to reset
      *
      * @exception  java.lang.IllegalArgumentException if key is <code>null</code>.
      * @exception  javax.portlet.ReadOnlyException
      *                 if this preference cannot be modified for this request
      */
     public void reset(String key) throws ReadOnlyException {
         if (key == null) throw new IllegalArgumentException("key is NULL");
         PersistencePreferenceAttribute ppa = (PersistencePreferenceAttribute)attributes.get(key);
         if (ppa != null) {
             Preference defaultPref = (Preference)defaultPrefsMap.get(key);
             if (defaultPref != null) {
                 Value[] defvals = defaultPref.getValue();
                 String[] vals = new String[defvals.length];
                 for (int i = 0; i < defvals.length; i++) {
                     vals[i] = defvals[i].getContent();
                 }
                 ppa.setAValues(vals);
             }  else {
                 attributes.remove(key);
             }
         }
     }
 
     /**
      * Commits all changes made to the preferences via the
      * <code>set</code> methods in the persistent store.
      * <P>
      * If this call returns succesfull, all changes are made
      * persistent. If this call fails, no changes are made
      * in the persistent store. This call is an atomic operation
      * regardless of how many preference attributes have been modified.
      * <P>
      * All changes made to preferences not followed by a call
      * to the <code>store</code> method are discarded when the
      * portlet finishes the <code>processAction</code> method.
      * <P>
      * If a validator is defined for this preferences in the
      * deployment descriptor, this validator is called before
      * the actual store is performed to check wether the given
      * preferences are vaild. If this check fails a
      * <code>ValidatorException</code> is thrown.
      *
      * @exception  java.io.IOException
      *                 if changes cannot be written into
      *                 the backend store
      * @exception  javax.portlet.ValidatorException
      *                 if the validation performed by the
      *                 associated validator fails
      * @exception  java.lang.IllegalStateException
      *                 if this method is called inside a render call
      *
      * @see  javax.portlet.PreferencesValidator
      */
     public void store() throws java.io.IOException, ValidatorException {
         if (validator != null) validator.validate(this);
         try {
             if (this.getOid() != null) {
                 pm.update(this);
             } else {
                 pm.create(this);
             }
         } catch (PersistenceManagerException e) {
             throw new IOException(e.getMessage());
         }
     }
 
     /**
      * Returns the user id of this portlet data
      *
      * @return the user id
      */
     public String getUserId() {
         return userId;
     }
 
     /**
      * Sets the user id of this portlet data
      *
      * @param userId the concrete portlet id
      */
     public void setUserId(String userId) {
         this.userId = userId;
     }
 
     /**
      * Returns the concrete portlet id of this portlet data
      *
      * @return the concrete portlet id
      */
     public String getPortletId() {
         return portletId;
     }
 
     /**
      * Sets the concrete portlet id of this portlet data
      *
      * @param portletId the concrete portlet id
      */
     public void setPortletId(String portletId) {
         this.portletId = portletId;
     }
 
     public Map getAttributes() {
         return attributes;
     }
 
     public void setAttributes(Map attributes) {
         this.attributes = attributes;
     }
 
 }
