 package com.j2bugzilla.rpc;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.j2bugzilla.base.BugField;
 import com.j2bugzilla.base.BugzillaMethod;
 
 public class GetBugField implements BugzillaMethod {
     /**
      * The XML-RPC method Bugzilla will use
      */
     private static final String METHOD_NAME = "Bug.fields";
 
     /**
      * A {@code Map} of parameter objects required by the XML-RPC method call.
      */
     private final Map<Object, Object> params = new HashMap<Object, Object>();
 
     /**
      * A {@code Map} of objects returned by the XML-RPC method call.
      */
     private Map<Object, Object> hash = new HashMap<Object, Object>();
 
     /**
      * Creates a new {@link GetBugField} object for the specified {@link com.j2bugzilla.base.BugField}
      * ID.
      *
      * @param id An integer specifying which {@link com.j2bugzilla.base.BugField} to retrieve details for.
      */
     public GetBugField(final int id) {
         params.put("ids", id);
     }
 
     /**
      * Creates a new {@link GetBugField} object for the specified {@link com.j2bugzilla.base.BugField}
      * ID.
      *
      * @param name A String specifying which {@link com.j2bugzilla.base.BugField} to retrieve details for.
      */
     public GetBugField(final String name) {
         params.put("names", name);
     }
 
     /**
      * Returns a <code>List</code> of all {@link com.j2bugzilla.base.BugField Bug Fields} on the
      *  requested from the installation
      *
      * @return A List of {@link com.j2bugzilla.base.BugField} objects
      */
    public BugField getBugFields() {
         BugField result = null;
         if (hash.containsKey("fields")) {
             final Object[] bugFields = (Object[]) hash.get("fields");
 
             if (bugFields.length == 0) {
                 return result;
             } // Early return to prevent ClassCast
 
             for (final Object o : bugFields) {
                 @SuppressWarnings("unchecked")
                 final Map<String, Object> bugFieldMap = (Map<String, Object>) o;
                 result = new BugField(bugFieldMap);
             }
         }
 
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setResultMap(final Map<Object, Object> hash) {
         this.hash = hash;
     }
 
     /**
      * {@inheritDoc}
      */
     public Map<Object, Object> getParameterMap() {
         return params;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getMethodName() {
         return METHOD_NAME;
     }
 
 }
