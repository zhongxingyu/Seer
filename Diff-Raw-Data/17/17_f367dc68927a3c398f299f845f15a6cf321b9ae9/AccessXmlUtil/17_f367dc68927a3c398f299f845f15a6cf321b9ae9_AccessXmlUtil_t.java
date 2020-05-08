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
 
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.util.Collection;
 
 import edu.rpi.sss.util.xml.QName;
 import edu.rpi.sss.util.xml.XmlEmit;
 
 /** Class to generate xml from an access specification. The resulting xml follows
  * the webdav acl spec rfc3744
  *
  *  @author Mike Douglass   douglm @ rpi.edu
  *  @author Dave Brondsema
  */
 public class AccessXmlUtil implements Serializable {
   private XmlEmit xml;
 
   private QName[] privTags;
 
   /** Passed to new object
    */
   public static interface AccessTags {
     /** Get the (webdav) tag corresponding to the name
      *
      * @param name
      * @return QName
      */
     public QName getTag(String name);
   }
 
   private AccessTags accessTags;
 
   /** Acls use tags in the webdav and caldav namespace.
    *
    * @param privTags
    * @param accessTags
    * @param xml
    */
   public AccessXmlUtil(QName[] privTags, AccessTags accessTags, XmlEmit xml) {
     if (privTags.length != PrivilegeDefs.privEncoding.length) {
       throw new RuntimeException("edu.rpi.cmt.access.BadParameter");
     }
 
     this.privTags = privTags;
     this.accessTags = accessTags;
     this.xml = xml;
   }
 
   /** Represent the acl as an xml string
    *
    * @param acl
    * @param privTags
    * @param accessTags
    * @return String xml representation
    * @throws AccessException
    */
   public static String getXmlAclString(Acl acl,
                                        QName[] privTags,
                                        AccessTags accessTags) throws AccessException {
     try {
       XmlEmit xml = new XmlEmit(true);  // no headers
       StringWriter su = new StringWriter();
       xml.startEmit(su);
       AccessXmlUtil au = new AccessXmlUtil(privTags, accessTags, xml);
 
       au.emitAcl(acl);
 
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
 
   /** Override this to construct urls from the parameter
    *
    * @param who String
    * @return String href
    */
   public String makeUserHref(String who) {
     return escapeChars(who);
   }
 
   /** Override this to construct urls from the parameter
    *
    * @param who String
    * @return String href
    */
   public String makeGroupHref(String who) {
     return escapeChars(who);
   }
 
 /**
  * Emit an acl as an xml string the current xml writer
  *
  * @param acl
  * @throws AccessException
  */
   public void emitAcl(Acl acl) throws AccessException {
     try {
       emitAces(acl.getAces());
     } catch (AccessException ae) {
       throw ae;
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   /** Emit the Collection of aces as an xml using the current xml writer
    *
    * @param aces
    * @throws AccessException
    */
   public void emitAces(Collection<Ace> aces) throws AccessException {
     try {
       xml.openTag(accessTags.getTag("acl"));
 
       if (aces != null) {
         for (Ace ace: aces) {
           boolean aceOpen = emitAce(ace, true, false);
           if (emitAce(ace, false, aceOpen)) {
             aceOpen = true;
           }
 
           if (aceOpen) {
            if (ace.getInherited()) {
              QName tag = accessTags.getTag("inherited");
              xml.openTag(tag);
              xml.property(accessTags.getTag("href"), ace.getInheritedFrom());
              xml.closeTag(tag);
            }
             xml.closeTag(accessTags.getTag("ace"));
           }
         }
       }
 
       xml.closeTag(accessTags.getTag("acl"));
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
       xml.openTag(accessTags.getTag("supported-privilege-set"));
 
       emitSupportedPriv(Privileges.getPrivAll());
 
       xml.closeTag(accessTags.getTag("supported-privilege-set"));
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   /** Produce an xml representation of current user privileges from an array
    * of allowed/disallowed/unspecified flags indexed by a privilege index.
    *
    * @param xml
    * @param privTags
    * @param accessTags
    * @param privileges    char[] of allowed/disallowed
    * @throws AccessException
    */
   public static void emitCurrentPrivSet(XmlEmit xml,
                                         QName[] privTags,
                                         AccessTags accessTags,
                                         char[] privileges) throws AccessException {
     if (privTags.length != PrivilegeDefs.privEncoding.length) {
       throw new AccessException("edu.rpi.cmt.access.BadParameter");
     }
 
     try {
       xml.openTag(accessTags.getTag("current-user-privilege-set"));
 
       for (int pi = 0; pi < privileges.length; pi++) {
         if ((privileges[pi] == PrivilegeDefs.allowed) ||
             (privileges[pi] == PrivilegeDefs.allowedInherited)) {
           // XXX further work - don't emit abstract privs or contained privs.
           QName pr = privTags[pi];
 
           if (pr != null) {
             xml.propertyTagVal(accessTags.getTag("privilege"), pr);
           }
         }
       }
 
       xml.closeTag(accessTags.getTag("current-user-privilege-set"));
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   /** Produce an xml representation of current user privileges from an array
    * of allowed/disallowed/unspecified flags indexed by a privilege index,
    * returning the representation a a String
    *
    * @param privTags
    * @param accessTags
    * @param ps    PrivilegeSet allowed/disallowed
    * @return String xml
    * @throws AccessException
    */
   public static String getCurrentPrivSetString(QName[] privTags,
                                                AccessTags accessTags,
                                                PrivilegeSet ps)
           throws AccessException {
     try {
       char[] privileges = ps.getPrivileges();
 
       XmlEmit xml = new XmlEmit(true);  // no headers
       StringWriter su = new StringWriter();
       xml.startEmit(su);
       AccessXmlUtil.emitCurrentPrivSet(xml, privTags, accessTags, privileges);
 
       su.close();
 
       return su.toString();
     } catch (AccessException ae) {
       throw ae;
     } catch (Throwable t) {
       throw new AccessException(t);
     }
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   private void emitSupportedPriv(Privilege priv) throws Throwable {
     xml.openTag(accessTags.getTag("supported-privilege"));
 
     xml.openTagNoNewline(accessTags.getTag("privilege"));
     xml.emptyTagSameLine(privTags[priv.getIndex()]);
     xml.closeTagNoblanks(accessTags.getTag("privilege"));
 
     if (priv.getAbstractPriv()) {
       xml.emptyTag(accessTags.getTag("abstract"));
     }
 
     xml.property(accessTags.getTag("description"), priv.getDescription());
 
     for (Privilege p: priv.getContainedPrivileges()) {
       emitSupportedPriv(p);
     }
 
     xml.closeTag(accessTags.getTag("supported-privilege"));
   }
 
  /* This ges called twice, once to do denials, once to do grants
   *
   */
   private boolean emitAce(Ace ace, boolean denials, boolean aceOpen) throws Throwable {
     boolean tagOpen = false;
 
     QName tag;
     if (denials) {
       tag = accessTags.getTag("deny");
     } else {
       tag = accessTags.getTag("grant");
     }
 
     for (Privilege p: ace.getPrivs()) {
       if (denials == p.getDenial()) {
         if (!aceOpen) {
           xml.openTag(accessTags.getTag("ace"));
 
           emitAceWho(ace.getWho());
           aceOpen = true;
         }
 
         if (!tagOpen) {
           xml.openTag(tag);
           tagOpen = true;
         }
         xml.emptyTag(privTags[p.getIndex()]);
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
       xml.openTag(accessTags.getTag("invert"));
     }
 
     xml.openTag(accessTags.getTag("principal"));
 
     int whoType = who.getWhoType();
 
     /*
            <!ELEMENT principal (href)
                   | all | authenticated | unauthenticated
                   | property | self)>
     */
 
     if (whoType == Ace.whoTypeUser) {
       xml.property(accessTags.getTag("href"), makeUserHref(who.getWho()));
     } else if (whoType == Ace.whoTypeGroup) {
       xml.property(accessTags.getTag("href"), makeGroupHref(who.getWho()));
     } else if ((whoType == Ace.whoTypeOwner) ||
                (whoType == Ace.whoTypeOther)) {
       // Other is !owner
       xml.openTag(accessTags.getTag("property"));
       xml.emptyTag(accessTags.getTag("owner"));
       xml.closeTag(accessTags.getTag("property"));
     } else if (whoType == Ace.whoTypeUnauthenticated) {
       xml.emptyTag(accessTags.getTag("unauthenticated"));
     } else if (whoType == Ace.whoTypeAuthenticated) {
       xml.emptyTag(accessTags.getTag("authenticated"));
     } else if (whoType == Ace.whoTypeAll) {
       xml.emptyTag(accessTags.getTag("all"));
     } else  {
       throw new AccessException("access.unknown.who");
     }
 
     xml.closeTag(accessTags.getTag("principal"));
 
     if (invert) {
       xml.closeTag(accessTags.getTag("invert"));
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
 }
