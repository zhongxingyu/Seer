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
 package edu.rpi.cct.webdav.servlet.common;
 
 import edu.rpi.cct.webdav.servlet.common.PropFindMethod.PropRequest;
 import edu.rpi.cct.webdav.servlet.shared.PrincipalPropertySearch;
 import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
 import edu.rpi.cct.webdav.servlet.shared.WebdavException;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode;
 import edu.rpi.cct.webdav.servlet.shared.WebdavStatusCode;
 import edu.rpi.cct.webdav.servlet.shared.PrincipalPropertySearch.PropertySearch;
 import edu.rpi.sss.util.xml.XmlUtil;
 import edu.rpi.sss.util.xml.tagdefs.WebdavTags;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.util.Collection;
 
 /** Class called to handle POST
  *
  *   @author Mike Douglass   douglm@rpi.edu
  */
 public class ReportMethod extends MethodBase {
   private final static int reportTypeExpandProperty = 0;
   private final static int reportTypePrincipalPropertySearch = 1;
   private final static int reportTypePrincipalMatch = 2;
   private final static int reportTypeAclPrincipalPropSet = 3;
   private final static int reportTypePrincipalSearchPropertySet = 4;
 
   private int reportType;
 
   private PrincipalMatchReport pmatch;
 
   private PrincipalPropertySearch pps;
 
   protected PropFindMethod.PropRequest preq;
 
   protected PropFindMethod pm;
 
   private PropRequest aclPrincipalProps;
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.common.MethodBase#init()
    */
   public void init() {
   }
 
   public void doMethod(HttpServletRequest req,
                        HttpServletResponse resp) throws WebdavException {
     if (debug) {
       trace("ReportMethod: doMethod");
     }
 
     /* Get hold of the PROPFIND method instance - we need it to process
        possible prop requests.
      */
     pm = (PropFindMethod)getNsIntf().getMethod("PROPFIND");
 
     if (pm == null) {
       throw new WebdavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
 
     Document doc = parseContent(req, resp);
 
     if (doc == null) {
       return;
     }
 
     int depth = Headers.depth(req);
 
     if (debug) {
       trace("ReportMethod: depth=" + depth);
     }
 
     process(doc, depth, req, resp);
   }
 
   /* We process the parsed document and produce a response
    *
    * @param doc
    * @throws WebdavException
    */
   protected void process(Document doc,
                          int depth,
                          HttpServletRequest req,
                          HttpServletResponse resp) throws WebdavException {
     reportType = getReportType(doc);
 
     if (reportType < 0) {
       throw new WebdavBadRequest();
     }
 
     processDoc(doc, depth);
 
     processResp(req, resp, depth);
   }
 
   /* Apply a node to a parsed request - or the other way - whatever.
    */
   protected void doNodeProperties(WebdavNsNode node) throws WebdavException {
     int status = node.getStatus();
 
     openTag(WebdavTags.response);
 
     if (status != HttpServletResponse.SC_OK) {
       node.generateHref(xml);
 
       addStatus(status, null);
     } else {
       pm.doNodeProperties(node, preq);
     }
 
     closeTag(WebdavTags.response);
 
     flush();
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   /* We process the parsed document and produce a Collection of request
    * objects to process.
    *
    * @param doc
    * @throws WebdavException
    */
   private void processDoc(Document doc,
                           int depth) throws WebdavException {
     try {
       WebdavNsIntf intf = getNsIntf();
 
       Element root = doc.getDocumentElement();
 
       if (reportType == reportTypeAclPrincipalPropSet) {
         depth = defaultDepth(depth, 0);
         checkDepth(depth, 0);
         parseAclPrincipalProps(root, intf);
         return;
       }
 
       if (reportType == reportTypeExpandProperty) {
         return;
       }
 
       if (reportType == reportTypePrincipalSearchPropertySet) {
         return;
       }
 
       if (reportType == reportTypePrincipalMatch) {
         depth = defaultDepth(depth, 0);
         checkDepth(depth, 0);
         pmatch = new PrincipalMatchReport(this, intf, debug);
 
         pmatch.parse(root, depth);
         return;
       }
 
       if (reportType == reportTypePrincipalPropertySearch) {
         depth = defaultDepth(depth, 0);
         checkDepth(depth, 0);
         parsePrincipalPropertySearch(root, depth, intf);
         return;
       }
 
       throw new WebdavBadRequest();
     } catch (WebdavException wde) {
       throw wde;
     } catch (Throwable t) {
       System.err.println(t.getMessage());
       if (debug) {
         t.printStackTrace();
       }
 
       throw new WebdavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /*
    *  <!ELEMENT acl-principal-prop-set ANY>
    *  ANY value: a sequence of one or more elements, with at most one
    *             DAV:prop element.
    *  prop: see RFC 2518, Section 12.11
    *
    */
   private void parseAclPrincipalProps(Element root,
                                       WebdavNsIntf intf) throws WebdavException {
     try {
       Element[] children = getChildrenArray(root);
       boolean hadProp = false;
 
       for (int i = 0; i < children.length; i++) {
         Element curnode = children[i];
 
         if (XmlUtil.nodeMatches(curnode, WebdavTags.prop)) {
           if (hadProp) {
             throw new WebdavBadRequest("More than one DAV:prop element");
           }
           aclPrincipalProps = pm.parseProps(curnode);
 
           hadProp = true;
         }
       }
     } catch (WebdavException wde) {
       throw wde;
     } catch (Throwable t) {
       System.err.println(t.getMessage());
       if (debug) {
         t.printStackTrace();
       }
 
       throw new WebdavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /*
    *  <!ELEMENT principal-property-search
    *  ((property-search+), prop?, apply-to-principal-collection-set?) >
    *
    *  <!ELEMENT property-search (prop, match) >
    *  prop: see RFC 2518, Section 12.11
    *
    *  <!ELEMENT match #PCDATA >
    *
    *  e.g
    *  <principal-property-search>
    *    <property-search>
    *      <prop>
    *        <displayname/>
    *      </prop>
    *      <match>myname</match>
    *    </property-search>
    *    <prop>
    *      <displayname/>
    *    </prop>
    *    <apply-to-principal-collection-set/>
    *  </principal-property-search>
    */
   private void parsePrincipalPropertySearch(Element root,
                                             int depth,
                                             WebdavNsIntf intf) throws WebdavException {
     try {
       Element[] children = getChildrenArray(root);
 
       pps = new PrincipalPropertySearch();
 
       for (int i = 0; i < children.length; i++) {
         Element curnode = children[i];
 
         if (XmlUtil.nodeMatches(curnode, WebdavTags.propertySearch)) {
           PropertySearch ps = new PropertySearch();
 
           pps.propertySearches.add(ps);
 
           Element[] pschildren = getChildrenArray(curnode);
 
           if (pschildren.length != 2) {
             throw new WebdavBadRequest();
           }
 
           ps.props = intf.parseProp(pschildren[0]);
           ps.match = pschildren[1];
         } else if (XmlUtil.nodeMatches(curnode, WebdavTags.prop)) {
           pps.pr = pm.parseProps(curnode);
           preq = pps.pr;
           i++;
 
           if (i < children.length) {
             if (!XmlUtil.nodeMatches(children[i], WebdavTags.applyToPrincipalCollectionSet)) {
               throw new WebdavBadRequest();
             }
 
             pps.applyToPrincipalCollectionSet = true;
             i++;
           }
 
           if (i < children.length) {
             throw new WebdavBadRequest();
           }
 
           break;
         }
       }
     } catch (WebdavException wde) {
       throw wde;
     } catch (Throwable t) {
       System.err.println(t.getMessage());
       if (debug) {
         t.printStackTrace();
       }
 
       throw new WebdavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * @param req
    * @param resp
    * @param depth
    * @throws WebdavException
    */
   private void processResp(HttpServletRequest req,
                            HttpServletResponse resp,
                            int depth) throws WebdavException {
     WebdavNsIntf intf = getNsIntf();
 
     /* Build a collection of nodes for any user principals in the acl
      * associated with the resource.
      */
 
     if (reportType == reportTypeAclPrincipalPropSet) {
       processAclPrincipalPropSet(req, resp, intf);
       return;
     }
 
     if (reportType == reportTypePrincipalSearchPropertySet) {
       return;
     }
 
     if (reportType == reportTypeExpandProperty) {
       processExpandProperty(req, resp, depth, intf);
       return;
     }
 
     if (reportType == reportTypePrincipalMatch) {
       pmatch.process(req, resp, defaultDepth(depth, 0));
       return;
     }
 
     if (reportType == reportTypePrincipalPropertySearch) {
       processPrincipalPropertySearch(req, resp,
                                      defaultDepth(depth, 0), intf);
       return;
     }
 
     throw new WebdavBadRequest();
   }
 
   /**
    * @param req
    * @param resp
    * @param depth
    * @param intf
    * @throws WebdavException
    */
   private void processExpandProperty(HttpServletRequest req,
                                      HttpServletResponse resp,
                                      int depth,
                                      WebdavNsIntf intf) throws WebdavException {
     return;
   }
 
   private void processAclPrincipalPropSet(HttpServletRequest req,
                                           HttpServletResponse resp,
                                           WebdavNsIntf intf) throws WebdavException {
     String resourceUri = getResourceUri(req);
     WebdavNsNode node = getNsIntf().getNode(resourceUri,
                                             WebdavNsIntf.existanceMust,
                                             WebdavNsIntf.nodeTypeUnknown);
 
     Collection<String> hrefs = intf.getAclPrincipalInfo(node);
 
     resp.setStatus(WebdavStatusCode.SC_MULTI_STATUS);
     resp.setContentType("text/xml; charset=UTF-8");
 
     startEmit(resp);
 
     openTag(WebdavTags.multistatus);
     if (!hrefs.isEmpty()) {
       openTag(WebdavTags.response);
 
       for (String href: hrefs) {
        WebdavNsNode pnode = getNsIntf().getNode(href,
                                                  WebdavNsIntf.existanceMay,
                                                  WebdavNsIntf.nodeTypePrincipal);
         if (pnode != null) {
           pm.doNodeProperties(pnode, aclPrincipalProps);
         }
       }
 
       closeTag(WebdavTags.response);
     }
 
     closeTag(WebdavTags.multistatus);
 
     flush();
   }
 
   /**
    * @param req
    * @param resp
    * @param depth
    * @param intf
    * @throws WebdavException
    */
   private void processPrincipalPropertySearch(HttpServletRequest req,
                                               HttpServletResponse resp,
                                               int depth,
                                               WebdavNsIntf intf) throws WebdavException {
     resp.setStatus(WebdavStatusCode.SC_MULTI_STATUS);
     resp.setContentType("text/xml; charset=UTF-8");
 
     startEmit(resp);
 
     String resourceUri = getResourceUri(req);
 
     Collection<? extends WebdavNsNode> principals = intf.getPrincipals(resourceUri, pps);
 
     openTag(WebdavTags.multistatus);
 
     for (WebdavNsNode node: principals) {
       doNodeProperties(node);
     }
 
     closeTag(WebdavTags.multistatus);
 
     flush();
   }
 
   /** See if we recognize this report type and return an index.
    *
    * @param doc
    * @return index or <0 for unknown.
    * @throws WebdavException
    */
   private int getReportType(Document doc) throws WebdavException {
     try {
       Element root = doc.getDocumentElement();
 
       if (XmlUtil.nodeMatches(root, WebdavTags.expandProperty)) {
         return reportTypeExpandProperty;
       }
 
       if (XmlUtil.nodeMatches(root, WebdavTags.principalPropertySearch)) {
         return reportTypePrincipalPropertySearch;
       }
 
       if (XmlUtil.nodeMatches(root, WebdavTags.principalMatch)) {
         return reportTypePrincipalMatch;
       }
 
       if (XmlUtil.nodeMatches(root, WebdavTags.aclPrincipalPropSet)) {
         return reportTypeAclPrincipalPropSet;
       }
 
       if (XmlUtil.nodeMatches(root, WebdavTags.principalSearchPropertySet)) {
         return reportTypePrincipalSearchPropertySet;
       }
 
       return -1;
     } catch (Throwable t) {
       System.err.println(t.getMessage());
       if (debug) {
         t.printStackTrace();
       }
 
       throw new WebdavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 }
 
