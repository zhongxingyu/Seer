 /*
  Copyright (c) 2000-2005 University of Washington.  All rights reserved.
 
  Redistribution and use of this distribution in source and binary forms,
  with or without modification, are permitted provided that:
 
    The above copyright notice and this permission notice appear in
    all copies and supporting documentation;
 
    The name, identifiers, and trademarks of the University of Washington
    are not used in advertising or publicity without the express prior
    written permission of the University of Washington;
 
    Recipients acknowledge that this distribution is made available as a
    research courtesy, "as is", potentially with defects, without
    any obligation on the part of the University of Washington to
    provide support, services, or repair;
 
    THE UNIVERSITY OF WASHINGTON DISCLAIMS ALL WARRANTIES, EXPRESS OR
    IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION
    ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
    WASHINGTON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
    PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING
    NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH
    THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 
 package edu.rpi.cct.webdav.servlet.shared;
 
 import edu.rpi.cmt.access.AccessXmlUtil;
 import edu.rpi.cmt.access.PrivilegeSet;
 import edu.rpi.cmt.access.Acl.CurrentAccess;
 import edu.rpi.sss.util.xml.QName;
 import edu.rpi.sss.util.xml.XmlEmit;
 
 import java.io.Serializable;
 import javax.servlet.http.HttpServletResponse;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.log4j.Logger;
 import org.bedework.davdefs.WebdavTags;
 import org.w3c.dom.Element;
 
 /** Represents a node in the underlying namespace for which this
  * servlet is acting as a gateway. This could be a file system, a set of
  * dynamically created objects or some sort of CMS for example.
  *
  *   @author Mike Douglass   douglm@rpi.edu
  */
 public abstract class WebdavNsNode implements Serializable {
   protected boolean debug;
 
   /** Does the resource exist? */
   protected boolean exists = true;
 
   private transient Logger log;
 
   /** Uri of the node. These are relative to the root of the namespace this
    * interface represents and should start with a "/". For example, if this
    * namespace is part of a file system starting at uabc in /var/local/uabc
    * and we are referring to a directory x at /var/local/uabc/a/x then the
    * uri should be /a/x/
    */
   protected String uri;
 
   /** Suitable for display
    */
   protected String name;
 
   /** ISO format creation date
    */
   protected String creDate;
 
   protected String lastmodDate;
 
   //  ????? locks??????
 
   /** True if this node is a collection
    */
   protected boolean collection;
 
   /** True if this node is a user
    */
   protected boolean userPrincipal;
 
   /** True if this node is a group
    */
   protected boolean groupPrincipal;
 
   /** True if GET is allowed
    */
   protected boolean allowsGet;
 
   /** Can be set to indicate some sort of abnormal condition on this node,
    * e.g. no access.
    */
   protected int status = HttpServletResponse.SC_OK;
 
  private final static Collection<PropertyTagEntry> propertyNames =
    new ArrayList<PropertyTagEntry>();
 
   /** */
   public static final class PropertyTagEntry {
     /** */
     public QName tag;
     /** */
     public boolean inPropAll = true;
 
     /**
      * @param tag
      */
     public PropertyTagEntry(QName tag) {
       this.tag = tag;
     }
 
     /**
      * @param tag
      * @param inPropAll
      */
     public PropertyTagEntry(QName tag, boolean inPropAll) {
       this.tag = tag;
       this.inPropAll = inPropAll;
     }
   }
 
   static {
     addPropEntry(WebdavTags.acl);
     addPropEntry(WebdavTags.creationdate);
     addPropEntry(WebdavTags.currentUserPrivilegeSet);
     addPropEntry(WebdavTags.displayname);
     addPropEntry(WebdavTags.getcontentlanguage);
     addPropEntry(WebdavTags.getcontentlength);
     addPropEntry(WebdavTags.getcontenttype);
     addPropEntry(WebdavTags.getlastmodified);
     addPropEntry(WebdavTags.owner);
     addPropEntry(WebdavTags.resourcetype);
     addPropEntry(WebdavTags.supportedPrivilegeSet);
   }
 
   /* ....................................................................
    *                   Content fields
    * We assume these get filled in when we get the content.
    * .................................................................... */
 
   protected String contentLang;
 
   protected int contentLen;
 
   protected String contentType;
 
   /* ....................................................................
    *                   Alias fields
    * .................................................................... */
 
   /** true if this is an alias
    */
   protected boolean alias;
 
   protected String targetUri;
 
   /** Constructor
    *
    * @param debug
    */
   public WebdavNsNode(boolean debug) {
     this.debug = debug;
   }
 
   /* ====================================================================
    *                   Abstract methods
    * ==================================================================== */
 
   /** Set the owner
    *
    * @param val
    * @throws WebdavIntfException
    */
   public abstract void setOwner(String val) throws WebdavIntfException;
 
   /** Should return a value suitable for WebdavNsIntf.makeUserHref
    *
    * @return String owner
    * @throws WebdavIntfException
    */
   public abstract String getOwner() throws WebdavIntfException;
 
   /** Get the current access granted to this principal for this node.
    *
    * @return CurrentAccess
    * @throws WebdavIntfException
    */
   public abstract CurrentAccess getCurrentAccess() throws WebdavIntfException;
 
   /** Remove the given property for this node.
    *
    * @param val   Element defining property to remove
    * @return boolean   true for removed, false for already absent
    * @throws WebdavIntfException
    */
   public abstract boolean removeProperty(Element val) throws WebdavIntfException;
 
   /** Set the given property for this node.
    *
    * @param val   Element defining property to set
    * @return boolean   true for created
    * @throws WebdavIntfException
    */
   public abstract boolean setProperty(Element val) throws WebdavIntfException;
 
   /** Entity tags are defined in RFC2068 - they are supposed to provide some
    * sort of indication the data has changed - e.g. a checksum.
    * <p>There are weak and strong tags
    *
    * <p>This methods should return a suitable value for that tag.
    *
    * @return String
    * @throws WebdavIntfException
    */
   public abstract String getEtagValue() throws WebdavIntfException;
 
   /* ====================================================================
    *                   Property methods
    * ==================================================================== */
 
   /**
    */
   public static class PropVal {
     /**
      */
     public boolean notFound;
 
     /**
      */
     public String val;
   }
 
   /** Emit the property indicated by the tag.
    *
    * @param tag  QName defining property
    * @param intf WebdavNsIntf
    * @return boolean   true if emitted
    * @throws WebdavIntfException
    */
   public boolean generatePropertyValue(QName tag,
                                        WebdavNsIntf intf) throws WebdavIntfException {
     String ns = tag.getNamespaceURI();
     XmlEmit xml = intf.getXmlEmit();
 
     if (!ns.equals(WebdavTags.namespace)) {
       // Not ours
 
       return false;
     }
 
     try {
       if (tag.equals(WebdavTags.acl)) {
         // access 5.4
         intf.emitAcl(this);
         return true;
       }
 
       if (tag.equals(WebdavTags.creationdate)) {
         // dav 13.1
 
         String val = getCreDate();
         if (val == null) {
           return false;
         }
 
         xml.property(tag, val);
         return true;
       }
 
       if (tag.equals(WebdavTags.currentUserPrivilegeSet)) {
         // access 5.3
         CurrentAccess ca = getCurrentAccess();
         if (ca != null) {
           PrivilegeSet ps = ca.privileges;
           char[] privileges = ps.getPrivileges();
 
           AccessXmlUtil.emitCurrentPrivSet(xml, intf.getPrivTags(),
                                            new WebdavTags(), privileges);
         }
 
         return true;
       }
 
       if (tag.equals(WebdavTags.displayname)) {
         // dav 13.2
         xml.property(tag, getName());
 
         return true;
       }
 
       if (tag.equals(WebdavTags.getcontentlanguage)) {
         // dav 13.3
         return false;
       }
 
       if (tag.equals(WebdavTags.getcontentlength)) {
         // dav 13.4
         if (!getAllowsGet()) {
           return false;
         }
         xml.property(tag, String.valueOf(getContentLen()));
         return true;
       }
 
       if (tag.equals(WebdavTags.getcontenttype)) {
         // dav 13.5
         if (!getAllowsGet()) {
           return false;
         }
 
         String val = getContentType();
         if (val == null) {
           return false;
         }
 
         xml.property(tag, val);
         return true;
       }
 
       if (tag.equals(WebdavTags.getlastmodified)) {
         // dav 13.7
         String val = getLastmodDate();
         if (val == null) {
           return false;
         }
 
         xml.property(tag, val);
         return true;
       }
 
       if (tag.equals(WebdavTags.owner)) {
         // access 5.1
         xml.openTag(tag);
         xml.property(WebdavTags.href, intf.makeUserHref(getOwner()));
         xml.closeTag(tag);
 
         return true;
       }
 
       if (tag.equals(WebdavTags.resourcetype)) {
         // dav 13.9
         if (getCollection()) {
           xml.propertyTagVal(WebdavTags.resourcetype,
                              WebdavTags.collection);
         }
 
         return true;
       }
 
       if (tag.equals(WebdavTags.supportedPrivilegeSet)) {
         // access 5.2
         intf.emitSupportedPrivSet(this);
         return true;
       }
 
       // Not known
       return false;
     } catch (WebdavIntfException wde) {
       throw wde;
     } catch (Throwable t) {
       throw new WebdavIntfException(t);
     }
   }
 
   /** This method is called before each setter/getter takes any action.
    * It allows the concrete implementation to defer some expensive
    * operation to just before the first call.
    *
    * @param content     boolean flag indicating if this is a content related
    *                    property - that is a property which requires fetching
    *                    and/or rendering the content
    * @throws WebdavIntfException
    */
   public void init(boolean content) throws WebdavIntfException {
   }
 
   /** Return true if this represents a principal
    *
    * @return boolean
    * @throws WebdavIntfException
    */
   public boolean isPrincipal() throws WebdavIntfException {
     return userPrincipal || groupPrincipal;
   }
 
   /** Return a set of QName defining properties this node supports.
    *
    * @return
    * @throws WebdavIntfException
    */
  public Collection<PropertyTagEntry> getPropertyNames()throws WebdavIntfException {
     if (!isPrincipal()) {
       return propertyNames;
     }
 
    Collection<PropertyTagEntry> res = new ArrayList<PropertyTagEntry>();
 
     res.addAll(getPropertyNames());
    res.add(new PropertyTagEntry(WebdavTags.principalURL));
 
     return res;
   }
 
   /**
    * @param val  boolean true if node exists
    * @throws WebdavIntfException
    */
   public void setExists(boolean val) throws WebdavIntfException {
     exists = val;
   }
 
   /**
    * @return boolean true if node exists
    * @throws WebdavIntfException
    */
   public boolean getExists() throws WebdavIntfException {
     return exists;
   }
 
   /** Set uri
    *
    * @param val
    * @throws WebdavIntfException
    */
   public void setUri(String val) throws WebdavIntfException {
     init(false);
     uri = val;
   }
 
   /** Get uri
    *
    * @return String uri
    * @throws WebdavIntfException
    */
   public String getUri() throws WebdavIntfException {
     init(false);
     return uri;
   }
 
   /**
    * @return String encoded uri
    * @throws WebdavIntfException
    */
   public String getEncodedUri() throws WebdavIntfException {
     try {
       return new URI(null, null, getUri(), null).toString();
     } catch (Throwable t) {
       if (debug) {
         error(t);
       }
       throw WebdavIntfException.badRequest();
     }
   }
 
   /**
    * @param val String credate
    * @throws WebdavIntfException
    */
   public void setCreDate(String val) throws WebdavIntfException {
     init(false);
     creDate = val;
   }
 
   /**
    * @return String credate
    * @throws WebdavIntfException
    */
   public String getCreDate() throws WebdavIntfException {
     init(false);
     return creDate;
   }
 
   /**
    * @param val String last mod date
    * @throws WebdavIntfException
    */
   public void setLastmodDate(String val) throws WebdavIntfException {
     init(false);
     lastmodDate = val;
   }
 
   /**
    * @return String last mod date
    * @throws WebdavIntfException
    */
   public String getLastmodDate() throws WebdavIntfException {
     init(false);
     return lastmodDate;
   }
 
   /**
    * @param val String name
    * @throws WebdavIntfException
    */
   public void setName(String val) throws WebdavIntfException {
     init(false);
     name = val;
   }
 
   /**
    * @return String name
    * @throws WebdavIntfException
    */
   public String getName() throws WebdavIntfException {
     init(false);
     return name;
   }
 
   /**
    * @param val boolean true for a collection
    * @throws WebdavIntfException
    */
   public void setCollection(boolean val) throws WebdavIntfException {
     collection = val;
   }
 
   /**
    * @return boolean true for a collection
    * @throws WebdavIntfException
    */
   public boolean getCollection() throws WebdavIntfException {
     return collection;
   }
 
   /**
    * @param val boolean true if node allows get
    * @throws WebdavIntfException
    */
   public void setAllowsGet(boolean val) throws WebdavIntfException {
     allowsGet = val;
   }
 
   /**
    * @return true if node allows get
    * @throws WebdavIntfException
    */
   public boolean getAllowsGet() throws WebdavIntfException {
     return allowsGet;
   }
 
   /**
    * @param val in status
    */
   public void setStatus(int val) {
     status = val;
   }
 
   /**
    * @return int sttaus
    */
   public int getStatus() {
     return status;
   }
 
   /**
    * @param val String lang
    * @throws WebdavIntfException
    */
   public void setContentLang(String val) throws WebdavIntfException {
     init(false);
     contentLang = val;
   }
 
   /**
    * @return String lang
    * @throws WebdavIntfException
    */
   public String getContentLang() throws WebdavIntfException {
     init(false);
     return contentLang;
   }
 
   /**
    * @param val int content length
    * @throws WebdavIntfException
    */
   public void setContentLen(int val) throws WebdavIntfException {
     init(true);
     contentLen = val;
   }
 
   /**
    * @return int content length
    * @throws WebdavIntfException
    */
   public int getContentLen() throws WebdavIntfException {
     init(true);
     return contentLen;
   }
 
   /**
    * @param val String content type
    * @throws WebdavIntfException
    */
   public void setContentType(String val) throws WebdavIntfException {
     init(false);
     contentType = val;
   }
 
   /** A content type of null implies no content (or we don't know)
    *
    * @return String content type
    * @throws WebdavIntfException
    */
   public String getContentType() throws WebdavIntfException {
     init(false);
     return contentType;
   }
 
   /**
    * @param val
    * @throws WebdavIntfException
    */
   public void setAlias(boolean val) throws WebdavIntfException {
     init(false);
     alias = val;
   }
 
   /**
    * @return boolean true if an alias
    * @throws WebdavIntfException
    */
   public boolean getAlias() throws WebdavIntfException {
     init(false);
     return alias;
   }
 
   /**
    * @param val
    * @throws WebdavIntfException
    */
   public void setTargetUri(String val) throws WebdavIntfException {
     init(false);
     targetUri = val;
   }
 
   /**
    * @return String uri
    * @throws WebdavIntfException
    */
   public String getTargetUri() throws WebdavIntfException {
     init(false);
     return targetUri;
   }
 
   /* ********************************************************************
    *                        Protected methods
    * ******************************************************************** */
 
   protected Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   protected void error(Throwable t) {
     getLogger().error(this, t);
   }
 
   protected void warn(String msg) {
     getLogger().warn(msg);
   }
 
   protected void debugMsg(String msg) {
     getLogger().debug(msg);
   }
 
   protected void logIt(String msg) {
     getLogger().info(msg);
   }
 
   protected static void addPropEntry(QName tag) {
     propertyNames.add(new PropertyTagEntry(tag));
   }
 
   protected static void addPropEntry(QName tag, boolean inAllProp) {
     propertyNames.add(new PropertyTagEntry(tag, inAllProp));
   }
 
   /* ********************************************************************
    *                        Object methods
    * ******************************************************************** */
 
   public int hashCode() {
     return uri.hashCode();
   }
 
   public boolean equals(Object o) {
     if (o == this) {
       return true;
     }
 
     if (!(o instanceof WebdavNsNode)) {
       return false;
     }
 
     WebdavNsNode that = (WebdavNsNode)o;
 
     return uri.equals(that.uri);
   }
 }
