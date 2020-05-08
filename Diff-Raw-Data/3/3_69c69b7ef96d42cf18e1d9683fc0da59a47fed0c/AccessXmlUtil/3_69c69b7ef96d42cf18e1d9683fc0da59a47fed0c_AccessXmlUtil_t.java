 /* **********************************************************************
     Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
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
 package edu.rpi.cmt.access;
 
 import edu.rpi.sss.util.xml.XmlEmit;
 import edu.rpi.sss.util.xml.XmlUtil;
 import edu.rpi.sss.util.xml.tagdefs.CaldavTags;
 import edu.rpi.sss.util.xml.tagdefs.WebdavTags;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 
 import java.io.Serializable;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.Collection;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 /** Class to generate xml from an access specification. The resulting xml follows
  * the webdav acl spec rfc3744
  *
  *  @author Mike Douglass   douglm @ rpi.edu
  *  @author Dave Brondsema
  */
 public class AccessXmlUtil implements Serializable {
   private transient Logger log;
 
   private boolean debug;
 
   private XmlEmit xml;
 
   private QName[] privTags;
 
   /* Following used when parsing an xml accl spec */
   private Acl curAcl;
 
   private Ace curAce;
 
   private AceWho awho;
 
   /** xml privilege tags */
   public static final QName[] caldavPrivTags = {
     WebdavTags.all,              // privAll = 0;
     WebdavTags.read,             // privRead = 1;
     WebdavTags.readAcl,          // privReadAcl = 2;
     WebdavTags.readCurrentUserPrivilegeSet,  // privReadCurrentUserPrivilegeSet = 3;
     CaldavTags.readFreeBusy,     // privReadFreeBusy = 4;
     WebdavTags.write,            // privWrite = 5;
     WebdavTags.writeAcl,         // privWriteAcl = 6;
     WebdavTags.writeProperties,  // privWriteProperties = 7;
     WebdavTags.writeContent,     // privWriteContent = 8;
     WebdavTags.bind,             // privBind = 9;
 
     CaldavTags.schedule,         // privSchedule = 10;
     CaldavTags.scheduleRequest,  // privScheduleRequest = 11;
     CaldavTags.scheduleReply,    // privScheduleReply = 12;
     CaldavTags.scheduleFreeBusy, // privScheduleFreeBusy = 13;
 
     WebdavTags.unbind,           // privUnbind = 14;
     WebdavTags.unlock,           // privUnlock = 15;
     null                         // privNone = 16;
   };
 
   /** Callback for xml utility
    *
    * @author douglm - rpi.edu
    */
   public interface AccessXmlCb {
     /**
      * @param id
      * @param whoType - from WhoDefs
      * @return String href
      * @throws AccessException
      */
     public String makeHref(String id, int whoType) throws AccessException;
 
     /** For parsing of acls - return the current account.
      *
      * @return String
      * @throws AccessException
      */
     public String getAccount() throws AccessException;
 
     /** Return PrincipalInfo for the given href
      *
      * @param href
      * @return PrincipalInfo or null for unknown.
      * @throws AccessException
      */
     public PrincipalInfo getPrincipalInfo(String href) throws AccessException;
 
     /** Called during processing to indicate an error
      *
      * @param tag
      * @throws AccessException
      */
     public void setErrorTag(QName tag) throws AccessException;
 
     /** Return any error tag
      *
      * @return QName
      * @throws AccessException
      */
     public QName getErrorTag() throws AccessException;
   }
 
   private AccessXmlCb cb;
 
   /** Acls use tags in the webdav and caldav namespace.
    *
    * @param privTags
    * @param xml
    * @param cb
    * @param debug
    */
   public AccessXmlUtil(QName[] privTags, XmlEmit xml,
                        AccessXmlCb cb, boolean debug) {
     if (privTags.length != PrivilegeDefs.privEncoding.length) {
       throw new RuntimeException("edu.rpi.cmt.access.BadParameter");
     }
 
     this.privTags = privTags;
     this.xml = xml;
     this.cb = cb;
     this.debug = debug;
   }
 
   /** Represent the acl as an xml string
    *
    * @param acl
    * @param forWebDAV  - true if we should split deny from grant.
    * @param privTags
    * @param cb
    * @param debug
    * @return String xml representation
    * @throws AccessException
    */
   public static String getXmlAclString(Acl acl, boolean forWebDAV,
                                        QName[] privTags,
                                        AccessXmlCb cb,
                                        boolean debug) throws AccessException {
     try {
       XmlEmit xml = new XmlEmit(true, false);  // no headers
       StringWriter su = new StringWriter();
       xml.startEmit(su);
       AccessXmlUtil au = new AccessXmlUtil(privTags, xml, cb, debug);
 
       au.emitAcl(acl, forWebDAV);
 
       su.close();
 
       return su.toString();
     } catch (AccessException ae) {
       throw ae;
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   /** (Re)set the xml writer
    *
    * @param val      xml Writer
    */
   public void setXml(XmlEmit val) {
     xml = val;
   }
 
   /** Return any error tag
    *
    * @return QName
    * @throws AccessException
    */
   public QName getErrorTag() throws AccessException {
     return cb.getErrorTag();
   }
 
   /** Given a webdav like xml acl return the internalized form as an Acl.
    *
    * @param xmlStr
    * @return Acl
    * @throws AccessException
    */
   public Acl getAcl(String xmlStr) throws AccessException {
     try {
       DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       factory.setNamespaceAware(true);
 
       DocumentBuilder builder = factory.newDocumentBuilder();
 
       Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
 
       return getAcl(doc.getDocumentElement());
     } catch (AccessException ae) {
       throw ae;
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   /**
    * @param root
    * @return Acl
    * @throws AccessException
    */
   public Acl getAcl(Element root) throws AccessException {
     try {
       /* We expect an acl root element containing 0 or more ace elements
        <!ELEMENT acl (ace)* >
        */
       if (!XmlUtil.nodeMatches(root, WebdavTags.acl)) {
         throw exc("Expected ACL");
       }
 
       Element[] aceEls = XmlUtil.getElementsArray(root);
 
       curAcl = new Acl();
 
       for (Element curnode: aceEls) {
         if (!XmlUtil.nodeMatches(curnode, WebdavTags.ace)) {
           throw exc("Expected ACE");
         }
 
         if (!processAce(curnode)) {
           break;
         }
       }
 
       return curAcl;
     } catch (AccessException ae) {
       throw ae;
     } catch (Throwable t) {
       t.printStackTrace();
       throw new AccessException(t);
     }
   }
 
   /**
    * Emit an acl as an xml string using the current xml writer
    *
    * @param acl
    * @param forWebDAV  - true if we should split deny from grant.
    * @throws AccessException
    */
   public void emitAcl(Acl acl, boolean forWebDAV) throws AccessException {
     try {
       emitAces(acl.getAces(), forWebDAV);
     } catch (AccessException ae) {
       throw ae;
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   /** Produce an xml representation of supported privileges. This is the same
    * at all points in the system and is identical to the webdav/caldav
    * requirements.
    *
    * @throws AccessException
    */
   public void emitSupportedPrivSet() throws AccessException {
     try {
       xml.openTag(WebdavTags.supportedPrivilegeSet);
 
       emitSupportedPriv(Privileges.getPrivAll());
 
       xml.closeTag(WebdavTags.supportedPrivilegeSet);
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   /** Produce an xml representation of current user privileges from an array
    * of allowed/disallowed/unspecified flags indexed by a privilege index.
    *
    * <p>Each position i in privileges corrsponds to a privilege defined by
    * privTags[i].
    *
    * @param xml
    * @param privTags
    * @param privileges    char[] of allowed/disallowed
    * @throws AccessException
    */
   public static void emitCurrentPrivSet(XmlEmit xml,
                                         QName[] privTags,
                                         char[] privileges) throws AccessException {
     if (privTags.length != PrivilegeDefs.privEncoding.length) {
       throw new AccessException("edu.rpi.cmt.access.BadParameter");
     }
 
     try {
       xml.openTag(WebdavTags.currentUserPrivilegeSet);
 
       for (int pi = 0; pi < privileges.length; pi++) {
         if ((privileges[pi] == PrivilegeDefs.allowed) ||
             (privileges[pi] == PrivilegeDefs.allowedInherited)) {
           // XXX further work - don't emit abstract privs or contained privs.
           QName pr = privTags[pi];
 
           if (pr != null) {
             xml.propertyTagVal(WebdavTags.privilege, pr);
           }
         }
       }
 
       xml.closeTag(WebdavTags.currentUserPrivilegeSet);
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   /** Produce an xml representation of current user privileges from an array
    * of allowed/disallowed/unspecified flags indexed by a privilege index,
    * returning the representation a a String
    *
    * @param privTags
    * @param ps    PrivilegeSet allowed/disallowed
    * @return String xml
    * @throws AccessException
    */
   public static String getCurrentPrivSetString(QName[] privTags,
                                                PrivilegeSet ps)
           throws AccessException {
     try {
       char[] privileges = ps.getPrivileges();
 
       XmlEmit xml = new XmlEmit(true, false);  // no headers
       StringWriter su = new StringWriter();
       xml.startEmit(su);
       AccessXmlUtil.emitCurrentPrivSet(xml, privTags, privileges);
 
       su.close();
 
       return su.toString();
     } catch (AccessException ae) {
       throw ae;
     } catch (Throwable t) {
       throw new AccessException(t);
     }
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
 
   protected void debugMsg(String msg) {
     getLogger().debug(msg);
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   /* Process an acl<br/>
          <!ELEMENT ace ((principal | invert), (grant|deny), protected?,
                          inherited?)>
          <!ELEMENT grant (privilege+)>
          <!ELEMENT deny (privilege+)>
 
          protected and inherited are for acl display
    */
   private boolean processAce(Node nd) throws Throwable {
     Element[] children = XmlUtil.getElementsArray(nd);
 
     if (children.length < 2) {
       throw exc("Bad ACE");
     }
 
     Element curnode = children[0];
     boolean inverted = false;
 
     /* Require principal or invert */
 
     if (XmlUtil.nodeMatches(curnode, WebdavTags.principal)) {
     } else if (XmlUtil.nodeMatches(curnode, WebdavTags.invert)) {
       /*  <!ELEMENT invert principal>       */
 
       inverted = true;
       curnode = XmlUtil.getOnlyElement(curnode);
     } else {
       throw exc("Bad ACE - expect principal | invert");
     }
 
     if (!parseAcePrincipal(curnode, inverted)) {
       return false;
     }
 
     /* Recognize grant or deny */
     for (int i = 1; i < children.length; i++) {
       curnode = children[i];
 
       boolean denial = false;
 
       if (XmlUtil.nodeMatches(curnode, WebdavTags.deny)) {
         denial = true;
       } else if (!XmlUtil.nodeMatches(curnode, WebdavTags.grant)) {
         if (debug) {
           debugMsg("Expected grant | deny");
         }
         cb.setErrorTag(WebdavTags.noAceConflict);
         return false;
       }
 
       Element[] pchildren = XmlUtil.getElementsArray(curnode);
 
       for (int pi = 0; pi < pchildren.length; pi++) {
         Element pnode = pchildren[pi];
 
         if (!XmlUtil.nodeMatches(pnode, WebdavTags.privilege)) {
           throw exc("Bad ACE - expect privilege");
         }
 
         parsePrivilege(pnode, denial);
       }
     }
 
     return true;
   }
 
   private boolean parseAcePrincipal(Node nd,
                                    boolean inverted) throws Throwable {
     Element el = XmlUtil.getOnlyElement(nd);
 
     int whoType = -1;
     String who = null;
 
     if (XmlUtil.nodeMatches(el, WebdavTags.href)) {
       String href = XmlUtil.getElementContent(el);
 
       if ((href == null) || (href.length() == 0)) {
         throw exc("Missing href");
       }
       PrincipalInfo pi = cb.getPrincipalInfo(href);
       if (pi == null) {
         cb.setErrorTag(WebdavTags.recognizedPrincipal);
         return false;
       }
       whoType = pi.whoType;
       who = pi.who;
     } else if (XmlUtil.nodeMatches(el, WebdavTags.all)) {
       whoType = Ace.whoTypeAll;
     } else if (XmlUtil.nodeMatches(el, WebdavTags.authenticated)) {
       whoType = Ace.whoTypeAuthenticated;
     } else if (XmlUtil.nodeMatches(el, WebdavTags.unauthenticated)) {
       whoType = Ace.whoTypeUnauthenticated;
     } else if (XmlUtil.nodeMatches(el, WebdavTags.property)) {
       el = XmlUtil.getOnlyElement(el);
       if (XmlUtil.nodeMatches(el, WebdavTags.owner)) {
         whoType = Ace.whoTypeOwner;
       } else {
         throw exc("Bad WHO property");
       }
     } else if (XmlUtil.nodeMatches(el, WebdavTags.self)) {
       whoType = Ace.whoTypeUser;
       who = cb.getAccount();
     } else {
       throw exc("Bad WHO");
     }
 
     curAce = null;
     awho = AceWho.getAceWho(who, whoType, inverted);
 
     if (debug) {
       debugMsg("Parsed ace/principal =" + awho);
     }
 
     return true;
   }
 
   private void parsePrivilege(Node nd,
                              boolean denial) throws Throwable {
     Element el = XmlUtil.getOnlyElement(nd);
 
     int priv;
 
     if (curAce == null) {
       /* Look for this 'who' in the list */
 
       Collection<Ace> aces = curAcl.getAces();
       if (aces != null) {
         for (Ace ace: curAcl.getAces()) {
           if (ace.getWho().equals(awho)) {
             curAce = ace;
             break;
           }
         }
       }
 
       if (curAce == null) {
         curAce = new Ace();
         curAce.setWho(awho);
 
         curAcl.addAce(curAce);
       }
     }
 
     findPriv: {
       // ENUM
       for (priv = 0; priv < privTags.length; priv++) {
         if (XmlUtil.nodeMatches(el, privTags[priv])) {
           break findPriv;
         }
       }
       throw exc("Bad privilege");
     }
 
     if (debug) {
       debugMsg("Add priv " + priv + " denied=" + denial);
     }
     curAce.addPriv(Privileges.makePriv(priv, denial));
 
   }
 
   /* Emit the Collection of aces as an xml using the current xml writer
    *
    * @param aces
    * @throws AccessException
    */
   private void emitAces(Collection<Ace> aces,
                         boolean forWebDAV) throws AccessException {
     try {
       xml.openTag(WebdavTags.acl);
 
       if (aces != null) {
         for (Ace ace: aces) {
           boolean aceOpen = emitAce(ace, false, false);
 
           if (aceOpen && forWebDAV) {
             closeAce(ace);
             aceOpen = false;
           }
 
           if (emitAce(ace, true, aceOpen)) {
             aceOpen = true;
           }
 
           if (aceOpen) {
             closeAce(ace);
           }
         }
       }
 
       xml.closeTag(WebdavTags.acl);
     } catch (AccessException ae) {
       throw ae;
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   private void closeAce(Ace ace) throws Throwable {
     if (ace.getInherited()) {
       QName tag = WebdavTags.inherited;
       xml.openTag(tag);
       xml.property(WebdavTags.href, ace.getInheritedFrom());
       xml.closeTag(tag);
     }
     xml.closeTag(WebdavTags.ace);
   }
 
   private void emitSupportedPriv(Privilege priv) throws Throwable {
     xml.openTag(WebdavTags.supportedPrivilege);
 
     xml.openTagNoNewline(WebdavTags.privilege);
     xml.emptyTagSameLine(privTags[priv.getIndex()]);
     xml.closeTagNoblanks(WebdavTags.privilege);
 
     if (priv.getAbstractPriv()) {
       xml.emptyTag(WebdavTags._abstract);
     }
 
     xml.property(WebdavTags.description, priv.getDescription());
 
     for (Privilege p: priv.getContainedPrivileges()) {
       emitSupportedPriv(p);
     }
 
     xml.closeTag(WebdavTags.supportedPrivilege);
   }
 
   /* This gets called twice, once to do denials, once to do grants
    *
    */
   private boolean emitAce(Ace ace, boolean denials, boolean aceOpen) throws Throwable {
     boolean tagOpen = false;
 
     QName tag;
     if (denials) {
       tag = WebdavTags.deny;
     } else {
       tag = WebdavTags.grant;
     }
 
     for (Privilege p: ace.getPrivs()) {
       if (denials == p.getDenial()) {
         if (!aceOpen) {
           xml.openTag(WebdavTags.ace);
 
           emitAceWho(ace.getWho());
           aceOpen = true;
         }
 
         if (!tagOpen) {
           xml.openTag(tag);
           tagOpen = true;
         }

        xml.propertyTagVal(WebdavTags.privilege, privTags[p.getIndex()]);
       }
     }
 
     if (tagOpen) {
       xml.closeTag(tag);
     }
 
     return aceOpen;
   }
 
   private void emitAceWho(AceWho who) throws Throwable {
     boolean invert = who.getNotWho();
 
     if (who.getWhoType() == Ace.whoTypeOther) {
       invert = !invert;
     }
 
     if (invert) {
       xml.openTag(WebdavTags.invert);
     }
 
     xml.openTag(WebdavTags.principal);
 
     int whoType = who.getWhoType();
 
     /*
            <!ELEMENT principal (href)
                   | all | authenticated | unauthenticated
                   | property | self)>
     */
 
     if ((whoType == Ace.whoTypeOwner) ||
         (whoType == Ace.whoTypeOther)) {
       // Other is !owner
       xml.openTag(WebdavTags.property);
       xml.emptyTag(WebdavTags.owner);
       xml.closeTag(WebdavTags.property);
     } else if (whoType == Ace.whoTypeUnauthenticated) {
       xml.emptyTag(WebdavTags.unauthenticated);
     } else if (whoType == Ace.whoTypeAuthenticated) {
       xml.emptyTag(WebdavTags.authenticated);
     } else if (whoType == Ace.whoTypeAll) {
       xml.emptyTag(WebdavTags.all);
     } else  {
       /* Just emit href */
       String href = escapeChars(cb.makeHref(who.getWho(), whoType));
       xml.property(WebdavTags.href, href);
     }
 
     xml.closeTag(WebdavTags.principal);
 
     if (invert) {
       xml.closeTag(WebdavTags.invert);
     }
   }
 
   /**
    * Lifted from org.apache.struts.util.ResponseUtils#filter
    *
    * Filter the specified string for characters that are senstive to HTML
    * interpreters, returning the string with these characters replaced by the
    * corresponding character entities.
    *
    * @param value      The string to be filtered and returned
    * @return String   escaped value
    */
   public static String escapeChars(String value) {
     if ((value == null) || (value.length() == 0)) {
       return value;
     }
 
     StringBuffer result = null;
     String filtered = null;
 
     for (int i = 0; i < value.length(); i++) {
       filtered = null;
 
       switch (value.charAt(i)) {
       case '<':
         filtered = "&lt;";
 
         break;
 
       case '>':
         filtered = "&gt;";
 
         break;
 
       case '&':
         filtered = "&amp;";
 
         break;
 
       case '"':
         filtered = "&quot;";
 
         break;
 
       case '\'':
         filtered = "&#39;";
 
         break;
       }
 
       if (result == null) {
         if (filtered != null) {
           result = new StringBuffer(value.length() + 50);
 
           if (i > 0) {
             result.append(value.substring(0, i));
           }
 
           result.append(filtered);
         }
       } else {
         if (filtered == null) {
           result.append(value.charAt(i));
         } else {
           result.append(filtered);
         }
       }
     }
 
     if (result == null) {
       return value;
     }
 
     return result.toString();
   }
 
   private AccessException exc(String msg) {
     if (debug) {
       debugMsg(msg);
     }
     return AccessException.badXmlACL(msg);
   }
 }
