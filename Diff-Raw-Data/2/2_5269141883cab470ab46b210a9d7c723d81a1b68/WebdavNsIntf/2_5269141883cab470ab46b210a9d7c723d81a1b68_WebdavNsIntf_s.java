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
 
 import org.bedework.davdefs.WebdavTags;
 
 import edu.rpi.sss.util.xml.QName;
 import edu.rpi.sss.util.xml.XmlEmit;
 import edu.rpi.sss.util.xml.XmlUtil;
 import edu.rpi.cct.webdav.servlet.common.MethodBase;
 import edu.rpi.cct.webdav.servlet.common.PrincipalMatchReport;
 import edu.rpi.cct.webdav.servlet.common.WebdavServlet;
 import edu.rpi.cct.webdav.servlet.common.WebdavUtils;
 import edu.rpi.cct.webdav.servlet.common.MethodBase.MethodInfo;
 
 import java.io.Reader;
 import java.io.Serializable;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /** This acts as an interface to the underlying namespace for which this
  * servlet is acting as a gateway. This could be a file system, a set of
  * dynamically created objects or some sort of CMS for example.
  *
  * A namespace consists of a number of nodes which may be containers for
  * other nodes or leaf nodes.
  *
  * All nodes are considered identical in their capabilities, that is, a
  * non-terminal node might contain content.
  *
  * Some nodes are aliases of other nodes (e.g. symlinks in a unix file
  * system). By default these aliases will be followed.
  *
  *   @author Mike Douglass   douglm@rpi.edu
  */
 public abstract class WebdavNsIntf implements Serializable {
   protected boolean debug;
 
   protected transient Logger log;
 
   protected WebdavServlet servlet;
 
   protected Properties props;
 
   private HttpServletRequest req;
 
   /* User associated with session */
   protected String account;
   protected boolean anonymous;
 
   protected boolean dumpContent;
 
   protected XmlEmit xml;
 
   /** Table of methods - set by servlet
    */
   protected HashMap<String, MethodInfo> methods;
 
   /** Table of created methods
    */
   private HashMap<String, MethodBase> createdMethods = new HashMap<String, MethodBase>();
 
   /** Should we return ok status in multistatus?
    */
   boolean returnMultistatusOk = true;
 
   private String urlPrefix;
 
   /** Called before any other method is called to allow initialisation to
    * take place at the first or subsequent requests
    *
    * @param servlet
    * @param req
    * @param props
    * @param debug
    * @param methods    HashMap   table of method info
    * @param dumpContent
    * @throws WebdavIntfException
    */
   public void init(WebdavServlet servlet,
                    HttpServletRequest req,
                    Properties props,
                    boolean debug,
                    HashMap<String, MethodInfo> methods,
                    boolean dumpContent) throws WebdavIntfException {
     this.servlet = servlet;
     this.req = req;
     this.props = props;
    this.xml = new XmlEmit(false, true /* try with no default namespace */);
     this.debug = debug;
     this.methods = methods;
     this.dumpContent = dumpContent;
 
     account = req.getRemoteUser();
     anonymous = (account == null) || (account.length() == 0);
     urlPrefix = WebdavUtils.getUrlPrefix(req);
 
     addNamespace();
   }
 
   /**
    * @return String
    */
   public String getAccount() {
     return account;
   }
 
   /**
    * @return XmlEmit xmlemitter
    */
   public XmlEmit getXmlEmit() {
     return xml;
   }
 
   /**
    * @return HttpServletRequest
    */
   public HttpServletRequest getRequest() {
     return req;
   }
 
   /** Return DAV header
    *
    * @return  String
    */
   public String getDavHeader() {
     return "1";
   }
 
   /**
    * @return Collection of method names.
    */
   public Collection<String> getMethodNames() {
     return methods.keySet();
   }
 
   /** Return the named initialised method or null if no such method or the
    * method requires authentication and we are anonymous
    *
    * @param name  name
    * @return MethodBase object or null
    * @throws WebdavIntfException
    */
   public MethodBase getMethod(String name) throws WebdavIntfException {
     name = name.toUpperCase();
     MethodBase mb = createdMethods.get(name);
     if (mb != null) {
       return mb;
     }
 
     MethodInfo mi = methods.get(name);
 
     if ((mi == null) || getAnonymous() && mi.getRequiresAuth()) {
       return null;
     }
 
     try {
       mb = (MethodBase)mi.getMethodClass().newInstance();
 
       mb.init(this, debug, dumpContent);
 
       createdMethods.put(name, mb);
 
       return mb;
     } catch (Throwable t) {
       if (debug) {
         error(t);
       }
       throw new WebdavIntfException(t);
     }
   }
 
   /**
    * @return boolean true for anon access
    */
   public boolean getAnonymous() {
     return anonymous;
   }
 
   /** Return the part of the href referring to the actual entity, e.g. <br/>
    * for http://localhost/ucaldav/user/caluser/calendar/2656-uwcal-demouwcalendar@mysite.edu.ics
    *
    * <br/>user/caluser/calendar/2656-uwcal-demouwcalendar@mysite.edu.ics
    *
    * @param href
    * @return String
    * @throws WebdavIntfException
    */
   public String getUri(String href) throws WebdavIntfException {
     try {
       if (href == null) {
         throw new WebdavIntfException("bad URI " + href);
       }
 
       String context = req.getContextPath();
 
       if (href.startsWith(context)) {
         return href.substring(context.length());
       }
 
       URL url = new URL(href);
 
       String path = url.getPath();
 
       if ((path == null) || (path.length() <= 1)) {
         return path;
       }
 
       if (context == null) {
         return path;
       }
 
       if (path.indexOf(context) != 0){
         return path;
       }
 
       int pos = context.length();
 
       if (path.length() == pos) {
         return "";
       }
 
       if (path.charAt(pos) != '/') {
         throw new WebdavIntfException("bad URI " + href);
       }
 
       return path.substring(pos + 1);
     } catch (Throwable t) {
       if (debug) {
         error(t);
       }
       throw new WebdavIntfException(t);
     }
   }
 
   /**
    * @return WebdavServlet
    */
   public WebdavServlet getServlet() {
     return servlet;
   }
 
   /**
    * @return boolean
    */
   public boolean getReturnMultistatusOk() {
     return returnMultistatusOk;
   }
 
   /** Add any namespaces for xml tag names in requests and responses.
    * An abbreviation will be supplied by the servlet.
 
    * The name should be globally unique in a global sense so don't return
    * something like "RPI:"
    *
    * <p>Something more like "http://ahost.rpi.edu/webdav/"
    *
    * @throws WebdavIntfException
    */
   public void addNamespace() throws WebdavIntfException {
     try {
       xml.addNs(WebdavTags.namespace);
     } catch (Throwable t) {
       throw new WebdavIntfException(t);
     }
   }
 
   /** Return true if the system disallows directory browsing.
    *
    * @return boolean
    * @throws WebdavIntfException
    */
   public abstract boolean getDirectoryBrowsingDisallowed() throws WebdavIntfException;
 
   /** Called on the way out to allow resources to be freed.
    *
    * @throws WebdavIntfException
    */
   public abstract void close() throws WebdavIntfException;
 
   /** Returns the supported locks for the supportedlock property.
    *
    * <p>To ensure these will work always provide the full namespace "DAV:"
    * for example, the result for supported exclusive and shared write locks
    * would be the string
    *
    *  "&lt;DAV:lockentry&gt;" +
    *  "  &lt;DAV:lockscope&gt;&lt;DAV:exclusive/&gt;&lt;DAV:/lockscope&gt;" +
    *  "  &lt;DAV:locktype&gt;&lt;DAV:write/&gt;&lt;DAV:/locktype&gt;" +
    *  "&lt;DAV:/lockentry&gt;" +
    *  "&lt;DAV:lockentry&gt;" +
    *  "  &lt;DAV:lockscope&gt;&lt;DAV:shared/&gt;&lt;DAV:/lockscope&gt;" +
    *  "&lt;DAV:/lockentry&gt;"
    *
    * @return String response
    */
   public abstract String getSupportedLocks();
 
   /** Returns true if the namespace supports access control
    *
    * @return boolean
    */
   public abstract boolean getAccessControl();
 
   /** Return the complete URL describing the location of the object
    * represented by the node
    *
    * @param node             node in question
    * @return String      url
    * @throws WebdavIntfException
    */
   public abstract String getLocation(WebdavNsNode node)
       throws WebdavIntfException;
 
   //ENUM
 
   /** Must not exist */
   public static final int existanceNot = 0;
 
   /** Must exist. */
   public static final int existanceMust = 1;
 
   /** We know it exists. */
   public static final int existanceDoesExist = 2;
 
   /** May exist */
   public static final int existanceMay = 3;
 
   //ENUM
 
   /** Must be collection */
   public static final int nodeTypeCollection = 0;
 
   /** Must be entity. */
   public static final int nodeTypeEntity = 1;
 
   /** Unknown. */
   public static final int nodeTypeUnknown = 2;
 
   /** Retrieves a node by uri, following any links.
    *
    * @param uri              String decoded uri of the node to retrieve
    * @param existance        Say's something about the state of existance
    * @param nodeType         Say's something about the type of node
    * @return WebdavNsNode    node specified by the URI or the node aliased by
    *                         the node at the URI.
    * @throws WebdavIntfException
    */
   public abstract WebdavNsNode getNode(String uri,
                                        int existance,
                                        int nodeType)
       throws WebdavIntfException;
 
   /** Retrieves a node by encoded uri, following any links. That si the uri
    * will have the usual encoding applied.
    *
    * @param uri              String encoded uri of the node to retrieve
    * @param existance        Say's something about the state of existance
    * @param nodeType         Say's something about the type of node
    * @return WebdavNsNode    node specified by the URI or the node aliased by
    *                         the node at the URI.
    * @throws WebdavIntfException
    */
   public abstract WebdavNsNode getNodeEncoded(String uri,
                                               int existance,
                                               int nodeType)
       throws WebdavIntfException;
 
   /** Stores/updates an object.
    *
    * @param node             node in question
    * @throws WebdavIntfException
    */
   public abstract void putNode(WebdavNsNode node)
       throws WebdavIntfException;
 
   /** Deletes a node from the namespace.
    *
    * @param node             node in question
    * @throws WebdavIntfException
    */
   public abstract void delete(WebdavNsNode node)
       throws WebdavIntfException;
 
   /** Returns the immediate children of a node.
    *
    * @param node             node in question
    * @return Iterator        over WebdavNsNode children
    * @throws WebdavIntfException
    */
   public abstract Iterator getChildren(WebdavNsNode node)
       throws WebdavIntfException;
 
   /** Returns the parent of a node.
    *
    * @param node             node in question
    * @return WebdavNsNode    node's parent, or null if the specified node
    *                         is the root
    * @throws WebdavIntfException
    */
   public abstract WebdavNsNode getParent(WebdavNsNode node)
       throws WebdavIntfException;
 
   /** Returns an InputStream for the content.
    *
    * @param node             node in question
    * @return Reader          A reader for the content.
    * @throws WebdavIntfException
    */
   public abstract Reader getContent(WebdavNsNode node)
       throws WebdavIntfException;
 
   /** Result for putContent
    */
   public static class PutContentResult {
     /** Same node or new node for creation */
     public WebdavNsNode node;
 
     /** True if created */
     public boolean created;
   }
 
   /** Set the content from a Reader
    *
    * @param node              node in question.
    * @param contentRdr        Reader for content
    * @param create            true if this is a probably creation
    * @return PutContentResult result of creating
    * @throws WebdavIntfException
    */
   public abstract PutContentResult putContent(WebdavNsNode node,
                                               Reader contentRdr,
                                               boolean create)
       throws WebdavIntfException;
 
   /** Create a new node.
    *
    * @param node             node to create with new uri set
    * @throws WebdavIntfException
    */
   public abstract void create(WebdavNsNode node)
       throws WebdavIntfException;
 
   /** Creates an alias to another node.
    *
    * @param alias       alias node that should be created with uri and
    *                    targetUri set
    * @throws WebdavIntfException
    */
   public abstract void createAlias(WebdavNsNode alias)
       throws WebdavIntfException;
 
   /** Throw an exception if we don't want the content for mkcol.
    *
    * @param req       HttpServletRequest
    * @throws WebdavIntfException
    */
   public abstract void acceptMkcolContent(HttpServletRequest req)
       throws WebdavIntfException;
 
   /** Create an empty collection at the given location..
    *
    * @param req       HttpServletRequest
    * @param node             node to create
    * @throws WebdavIntfException
    */
   public abstract void makeCollection(HttpServletRequest req,
                                       WebdavNsNode node)
       throws WebdavIntfException;
 
   /* ====================================================================
    *                  Access methods
    * ==================================================================== */
 
   /** Return the prefix - starting with "/" - which identifies principal urls
    *
    * @return String prefix
    * @throws WebdavIntfException
    */
   public abstract String getPrincipalPrefix() throws WebdavIntfException;
 
   /** Given a PrincipalMatchReport returns a Collection of matching nodes.
    *
    * @param resourceUri
    * @param pmatch PrincipalMatchReport object
    * @return Collection of WebdavNsNode
    * @throws WebdavIntfException
    */
   public abstract Collection<WebdavNsNode> principalMatch(String resourceUri,
                                                           PrincipalMatchReport pmatch)
           throws WebdavIntfException;
 
   /** Given a uri returns a Collection of uris that allow search operations on
    * principals for that resource.
    *
    * @param req
    * @param resourceUri
    * @return Collection of String
    * @throws WebdavIntfException
    */
   public abstract Collection<String> getPrincipalCollectionSet(String resourceUri)
          throws WebdavIntfException;
 
   /** Given a PrincipalPropertySearch returns a Collection of matching nodes.
    *
    * @param resourceUri
    * @param pps Collection of PrincipalPropertySearch
    * @return Collection of WebdavNsNode
    * @throws WebdavIntfException
    */
   public abstract Collection getPrincipals(String resourceUri,
                                            PrincipalPropertySearch pps)
           throws WebdavIntfException;
 
   /**
    * @param id
    * @return String href
    * @throws WebdavIntfException
    */
   public abstract String makeUserHref(String id) throws WebdavIntfException;
 
   /** Object class passed around as we parse access.
    */
   public static class AclInfo {
   }
 
   /** Get an AclInfo object .
    *
    * @param uri       String uri of entity
    * @return AclInfo  The object
    * @throws WebdavIntfException
    */
   public abstract AclInfo startAcl(String uri) throws WebdavIntfException;
 
   /** Parse the webdav acl principal element.which webdav-acl defines as
    * <pre>
    *    <!ELEMENT principal (href) | all | authenticated | unauthenticated |
    *                        property | self)>
    *
    *    <!ELEMENT all EMPTY>
    *    <!ELEMENT authenticated EMPTY>
    *    <!ELEMENT unauthenticated EMPTY>
    *    <!ELEMENT property ANY>
    *    <!ELEMENT self EMPTY>
    * </pre>
    * Other protocols/implemenmtations may wish to extend this.
    *
    * @param ainfo
    * @param nd       The principal element
    * @param inverted boolean true if principal element was containjed in invert
    * @throws WebdavIntfException
    */
   public abstract void parseAcePrincipal(AclInfo ainfo, Node nd,
                                          boolean inverted) throws WebdavIntfException;
 
   /** Parse the webdav privilege element.
    * The supplied node is the privilege webdav element
      <!ELEMENT read EMPTY>
      <!ELEMENT write EMPTY>
      <!ELEMENT write-properties EMPTY>
      <!ELEMENT write-content EMPTY>
      <!ELEMENT unlock EMPTY>
      <!ELEMENT read-acl EMPTY>
      <!ELEMENT read-current-user-privilege-set EMPTY>
      <!ELEMENT write-acl EMPTY>
      <!ELEMENT bind EMPTY>
      <!ELEMENT unbind EMPTY>
      <!ELEMENT all EMPTY>
    *
    * @param ainfo
    * @param nd       The privilege element
    * @param grant    boolean true for granting priv, false for deny
    * @throws WebdavIntfException
    */
   public abstract void parsePrivilege(AclInfo ainfo, Node nd,
                                       boolean grant) throws WebdavIntfException;
 
   /**
    * @param ainfo
    * @throws WebdavIntfException
    */
   public abstract void updateAccess(AclInfo ainfo) throws WebdavIntfException;
 
   /**
    * @param node
    * @throws WebdavIntfException
    */
   public abstract void emitAcl(WebdavNsNode node) throws WebdavIntfException;
 
   /**
    * @param node
    * @throws WebdavIntfException
    */
   public abstract void emitSupportedPrivSet(WebdavNsNode node) throws WebdavIntfException;
 
   /* ====================================================================
    *                Property value methods
    * ==================================================================== */
 
   /** Open a propstat response.
    *
    * @throws WebdavIntfException
    */
   public void openPropstat() throws WebdavIntfException {
     try {
       xml.openTag(WebdavTags.propstat);
       xml.openTag(WebdavTags.prop);
     } catch (Throwable t) {
       throw new WebdavIntfException(t);
     }
   }
 
   /** Close a propstat response with given result.
    *
    * @param status
    * @throws WebdavIntfException
    */
   public void closePropstat(int status) throws WebdavIntfException {
     try {
       xml.closeTag(WebdavTags.prop);
 
       if ((status != HttpServletResponse.SC_OK) ||
           getReturnMultistatusOk()) {
         xml.property(WebdavTags.status, "HTTP/1.1 " + status + " " +
                      WebdavStatusCode.getMessage(status));
       }
 
       xml.closeTag(WebdavTags.propstat);
     } catch (Throwable t) {
       throw new WebdavIntfException(t);
     }
   }
 
   /** Close a propstat response with an ok result.
    *
    * @throws WebdavIntfException
    */
   public void closePropstat() throws WebdavIntfException {
     closePropstat(HttpServletResponse.SC_OK);
   }
 
   /** Parse a <prop> list of property names in any namespace.
    *
    * @param nd
    * @return Collection
    * @throws WebdavException
    */
   public Collection<WebdavProperty> parseProp(Node nd) throws WebdavException {
     Collection<WebdavProperty> props = new ArrayList<WebdavProperty>();
 
     Element[] children = getChildren(nd);
 
     for (int i = 0; i < children.length; i++) {
       Element propnode = children[i];
 
       WebdavProperty prop = makeProp(propnode);
 
       if (debug) {
         trace("prop: " + prop.getTag());
       }
 
       props.add(prop);
     }
 
     return props;
   }
 
   /** Override this to create namespace specific property objects.
    *
    * @param propnode
    * @return WebdavProperty
    * @throws WebdavException
    */
   public WebdavProperty makeProp(Element propnode) throws WebdavException {
     return new WebdavProperty(new QName(propnode.getNamespaceURI(),
                                         propnode.getLocalName()),
                                         null);
   }
 
   /** Generate a response for a single webdav property. This should be overrriden
    * to handle other namespaces.
    *
    * @param node
    * @param pr
    * @param allProp   true if we're doing allprop
    * @return int status
    * @throws WebdavIntfException
    */
   public int generatePropValue(WebdavNsNode node,
                                WebdavProperty pr,
                                boolean allProp) throws WebdavIntfException {
     QName tag = pr.getTag();
     String ns = tag.getNamespaceURI();
     int status = HttpServletResponse.SC_OK;
 
     try {
       /* Deal with webdav properties */
       if (!ns.equals(WebdavTags.namespace)) {
         // Not ours
         xml.emptyTag(tag);
         return HttpServletResponse.SC_NOT_FOUND;
       }
 
       if (tag.equals(WebdavTags.lockdiscovery)) {
         // dav 13.8
         xml.emptyTag(tag);
         return HttpServletResponse.SC_NOT_FOUND;
       }
 
       if (tag.equals(WebdavTags.source)) {
         // dav 13.10
         xml.emptyTag(tag);
         return HttpServletResponse.SC_NOT_FOUND;
       }
 
       if (tag.equals(WebdavTags.supportedlock)) {
         // dav 13.11
         xml.emptyTag(tag);
         return HttpServletResponse.SC_NOT_FOUND;
       }
 
       if (tag.equals(WebdavTags.aclRestrictions)) {
         // access 5.5
         return HttpServletResponse.SC_NOT_FOUND;
       }
 
       if (tag.equals(WebdavTags.inheritedAclSet)) {
         // access 5.6
         return HttpServletResponse.SC_NOT_FOUND;
       }
 
       if (tag.equals(WebdavTags.principalCollectionSet)) {
         // access 5.7
         xml.openTag(WebdavTags.prop);
         xml.openTag(WebdavTags.principalCollectionSet);
 
         for (String s: getPrincipalCollectionSet(node.getUri())) {
           xml.property(tag, s);
         }
 
         xml.closeTag(WebdavTags.principalCollectionSet);
         xml.closeTag(WebdavTags.prop);
         return status;
       }
 
       /* Try the node for a value */
 
       if (node.generatePropertyValue(tag, this, allProp)) {
         // Generated by node
         return status;
       }
 
       // Not known
       xml.emptyTag(tag);
       return HttpServletResponse.SC_NOT_FOUND;
     } catch (WebdavIntfException wie) {
       throw wie;
     } catch (Throwable t) {
       throw new WebdavIntfException(t);
     }
   }
 
   /** xml rpivilege tags */
   private static final QName[] privTags = {
     WebdavTags.all,              // privAll = 0;
     WebdavTags.read,             // privRead = 1;
     WebdavTags.readAcl,          // privReadAcl = 2;
     WebdavTags.readCurrentUserPrivilegeSet,  // privReadCurrentUserPrivilegeSet = 3;
     null,                        // privReadFreeBusy = 4;
     WebdavTags.write,            // privWrite = 5;
     WebdavTags.writeAcl,         // privWriteAcl = 6;
     WebdavTags.writeProperties,  // privWriteProperties = 7;
     WebdavTags.writeContent,     // privWriteContent = 8;
     WebdavTags.bind,             // privBind = 9;
     WebdavTags.unbind,           // privUnbind = 10;
     WebdavTags.unlock,           // privUnlock = 11;
     null                         // privNone = 12;
   };
 
   /**
    * @return QName[]
    */
   public QName[] getPrivTags() {
     return privTags;
   }
 
   /**
    * @param node
    * @throws WebdavException
    */
   public void addHref(WebdavNsNode node) throws WebdavException {
     try {
       if (debug) {
         trace("Adding href " + urlPrefix + node.getEncodedUri());
       }
 
       String url = urlPrefix + new URI(node.getEncodedUri()).toASCIIString();
       xml.property(WebdavTags.href, url);
     } catch (WebdavException wde) {
       throw wde;
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /**
    * @param status
    * @throws WebdavException
    */
   public void addStatus(int status) throws WebdavException {
     try {
       xml.property(WebdavTags.status, "HTTP/1.1 " + status + " " +
                WebdavStatusCode.getMessage(status));
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /* ====================================================================
    *                   XmlUtil wrappers
    * ==================================================================== */
 
   /** Get all the children if any
    *
    * @param nd
    * @return
    * @throws WebdavIntfException
    */
   public Element[] getChildren(Node nd) throws WebdavIntfException {
     try {
       return XmlUtil.getElementsArray(nd);
     } catch (Throwable t) {
       if (debug) {
         getLogger().error(this, t);
       }
 
       throw WebdavIntfException.badRequest();
     }
   }
 
   /** We expect a single child
    *
    * @param nd
    * @return Element
    * @throws WebdavIntfException
    */
   public Element getOnlyChild(Node nd) throws WebdavIntfException {
     try {
       return XmlUtil.getOnlyElement(nd);
     } catch (Throwable t) {
       if (debug) {
         getLogger().error(this, t);
       }
 
       throw WebdavIntfException.badRequest();
     }
   }
 
   /**
    * @param el
    * @return String
    * @throws WebdavIntfException
    */
   public String getElementContent(Element el) throws WebdavIntfException {
     try {
       return XmlUtil.getElementContent(el);
     } catch (Throwable t) {
       if (debug) {
         getLogger().error(this, t);
       }
 
       throw WebdavIntfException.badRequest();
     }
   }
 
   /* ====================================================================
    *                        Protected methods
    * ==================================================================== */
 
   protected Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   protected void trace(String msg) {
     getLogger().debug(msg);
   }
 
   protected void debugMsg(String msg) {
     getLogger().debug(msg);
   }
 
   protected void warn(String msg) {
     getLogger().warn(msg);
   }
 
   protected void error(Throwable t) {
     getLogger().error(this, t);
   }
 
   protected void logIt(String msg) {
     getLogger().info(msg);
   }
 }
