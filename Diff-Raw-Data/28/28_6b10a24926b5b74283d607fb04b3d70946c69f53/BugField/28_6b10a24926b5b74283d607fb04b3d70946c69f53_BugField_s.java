 package com.j2bugzilla.base;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 public class BugField {
     /**
      * HashMap representing fields for each BugField. The Value for each Key is a
      * <code>String</code>.
      */
     private Map<String, Object> internalState;
 
     /**
      * Constructor for creating a new {@link com.j2bugzilla.base.BugField}
      */
     public BugField() {
 
     }
 
     /**
      * Constructor for creating a new {@link com.j2bugzilla.base.BugField} to submit to an installation.
      *
      * @param state A <code>Map</code> pairing required keys to values
      */
     public BugField(final Map<String, Object> state) {
         setInternalState(state);
     }
 
     /**
      * Returns the internal Bugzilla ID number for this field. If it is not in the
      * Bugzilla database, this will return null.
      *
      * @return The field ID
      */
     public Integer getID() {
         return (Integer) getInternalState().get("id");
     }
 
     /**
      * Returns the internal Bugzilla Name for this field. If it is not in the
      * Bugzilla database, this will return null.
      *
      * @return The field name
      */
     public String getName() {
         return (String) getInternalState().get("name");
     }
 
     /**
      * Sets the name of this {@link BugField}.
      *
      * @param name A {@code String} representing a unique name for this field.
      */
     public void setName(final String name) {
         getInternalState().put("name", name);
     }
 
     /**
      * Returns the internal Bugzilla Display name for this field. If it is not in the
      * Bugzilla database, this will return null.
      *
      * @return The field display name
      */
     public String getDisplayName() {
         return (String) getInternalState().get("display_name");
     }
 
     /**
      * Sets the description of this {@link BugField}.
      *
      * @param displayName A {@code String} representing a display name for this field.
      */
     public void setDisplayName(final String displayName) {
         getInternalState().put("display_name", displayName);
     }
 
     public Boolean getIsMandatory() {
         final Object isMandatory = getInternalState().get("is_mandatory");
         return isMandatory == null ? null : (Boolean) isMandatory;
     }
 
     public void setIsMandatory(Boolean isMandatory) {
         getInternalState().put("is_mandatory", isMandatory);
     }
 
     public Boolean getIsCustom() {
         final Object isCustom = getInternalState().get("is_custom");
         return isCustom == null ? null : (Boolean) isCustom;
     }
 
     public void setIsCustom(Boolean isCustom) {
         getInternalState().put("is_custom", isCustom);
     }
 
     public Integer getType() {
         return (Integer) getInternalState().get("type");
     }
 
     public void setType(Integer type) {
         getInternalState().put("type", type);
     }
 
     /**
      * Used when a representation of this {@link BugField Bug Fields} internals must be
      * passed via XML-RPC for a remote method. Regular users of this API should
      * prefer the normal {@code getXxx()} methods.
      *
      * @return A read-only {@code Map} of key-value pairs corresponding to this
      *         Bug Fields properties.
      */
     public Map<Object, Object> getParameterMap() {
         Map<Object, Object> params = new HashMap<Object, Object>();
         for (final String key : getInternalState().keySet()) {
             params.put(key, getInternalState().get(key));
         }
         return Collections.unmodifiableMap(params);
     }
 
     public Map<String, Object> getInternalState() {
         return internalState;
     }
 
     public void setInternalState(final Map<String, Object> internalState) {
         this.internalState = internalState;
     }
 }
