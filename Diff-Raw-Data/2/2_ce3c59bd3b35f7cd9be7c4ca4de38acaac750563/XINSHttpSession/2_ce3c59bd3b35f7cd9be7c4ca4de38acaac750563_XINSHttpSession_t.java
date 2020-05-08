 /*
  * $Id: XINSHttpSession.java,v 1.11 2007/09/18 08:45:08 agoubard Exp $
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.servlet.container;
 
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.collections.iterators.IteratorEnumeration;
 
 /**
  * A user session.
  *
  * @version $Revision: 1.11 $ $Date: 2007/09/18 08:45:08 $
  * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
  *
  * @since XINS 1.4.0
  */
 public class XINSHttpSession implements HttpSession {
 
    /**
     * The random generator.
     */
    private final static Random RANDOM = new Random();
 
    /**
     * The session attributes.
     */
    private Map<String,Object> _attributes = new HashMap<String,Object>();
 
    /**
     * The creation time of the session.
     */
    private long _creationTime = System.currentTimeMillis();
 
    /**
     * The ID of the session.
     */
    private int _sessionID = RANDOM.nextInt();
 
    /**
     * Creates a new instance of XINSHttpSession.
     */
    XINSHttpSession() {
       // empty
    }
 
    @Deprecated
    public void removeValue(String value) {
       throw new UnsupportedOperationException();
    }
 
    public void removeAttribute(String name) {
       _attributes.remove(name);
    }
 
    public Object getAttribute(String name) {
       return _attributes.get(name);
    }
 
    @Deprecated
    public Object getValue(String name) {
       return getAttribute(name);
    }
 
    public void setMaxInactiveInterval(int i) {
       // empty
    }
 
    public void setAttribute(String name, Object value) {
       _attributes.put(name, value);
    }
 
    @Deprecated
    public void putValue(String name, Object value) {
       setAttribute(name, value);
    }
 
    /**
    * @deprecated
     *    Since XINS 3.0, use {@link #getAttributes()} instead.
     */
    @Deprecated
    public Enumeration getAttributeNames() {
       return new IteratorEnumeration(getAttributes().keySet().iterator());
    }
 
    /** @since XINS 3.0 */
    public Map<String,Object> getAttributes() {
       return Collections.unmodifiableMap(_attributes);
    }
 
    public long getCreationTime() {
       return _creationTime;
    }
 
    public String getId() {
       return "" + _sessionID;
    }
 
    public long getLastAccessedTime() {
       throw new UnsupportedOperationException();
    }
 
    public int getMaxInactiveInterval() {
       throw new UnsupportedOperationException();
    }
 
    public ServletContext getServletContext() {
       throw new UnsupportedOperationException();
    }
 
    @Deprecated
    public javax.servlet.http.HttpSessionContext getSessionContext() {
       throw new UnsupportedOperationException();
    }
 
    @Deprecated
    public String[] getValueNames() {
       throw new UnsupportedOperationException();
    }
 
    public void invalidate() {
       throw new UnsupportedOperationException();
    }
 
    public boolean isNew() {
       throw new UnsupportedOperationException();
    }
 }
