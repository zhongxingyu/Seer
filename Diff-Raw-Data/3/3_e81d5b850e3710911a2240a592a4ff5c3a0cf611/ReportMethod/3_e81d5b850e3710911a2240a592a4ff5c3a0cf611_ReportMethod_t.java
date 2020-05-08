 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package org.bedework.webdav.servlet.common;
 
 import org.bedework.util.misc.Util;
 import org.bedework.util.xml.XmlUtil;
 import org.bedework.util.xml.tagdefs.WebdavTags;
 import org.bedework.webdav.servlet.common.PropFindMethod.PropRequest;
 import org.bedework.webdav.servlet.shared.PrincipalPropertySearch;
 import org.bedework.webdav.servlet.shared.WdSynchReport;
 import org.bedework.webdav.servlet.shared.WdSynchReport.WdSynchReportItem;
 import org.bedework.webdav.servlet.shared.WebdavBadRequest;
 import org.bedework.webdav.servlet.shared.WebdavException;
 import org.bedework.webdav.servlet.shared.WebdavNsIntf;
 import org.bedework.webdav.servlet.shared.WebdavNsNode;
import org.bedework.webdav.servlet.shared.WebdavProperty;
 import org.bedework.webdav.servlet.shared.WebdavStatusCode;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.util.Collection;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /** Class called to handle POST
  *
  *   @author Mike Douglass   douglm   rpi.edu
  */
 public class ReportMethod extends MethodBase {
   private final static int reportTypeExpandProperty = 0;
   private final static int reportTypePrincipalPropertySearch = 1;
   private final static int reportTypePrincipalMatch = 2;
   private final static int reportTypeAclPrincipalPropSet = 3;
   private final static int reportTypePrincipalSearchPropertySet = 4;
   private final static int reportTypeSync = 5;
 
   private int reportType;
 
   private PrincipalMatchReport pmatch;
 
   private PrincipalPropertySearch pps;
 
   protected PropFindMethod.PropRequest preq;
 
   protected PropFindMethod pm;
 
   private PropRequest propReq;
 
   private String syncToken;
 
   private int syncLevel;
 
   private int syncLimit; // -1 for no limit
 
   private boolean syncRecurse;
 
   @Override
   public void init() {
   }
 
   @Override
   public void doMethod(final HttpServletRequest req,
                        final HttpServletResponse resp) throws WebdavException {
     if (debug) {
       trace("ReportMethod: doMethod");
     }
 
     /* Get hold of the PROPFIND method instance - we need it to process
        possible prop requests.
      */
     /*
     pm = (PropFindMethod)getNsIntf().getMethod("PROPFIND");
 
     if (pm == null) {
       throw new WebdavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
     */
     pm = new PropFindMethod();
     pm.init(getNsIntf(), true);
 
     Document doc = parseContent(req, resp);
 
     if (doc == null) {
       return;
     }
 
     int depth = Headers.depth(req, 0);
 
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
   protected void process(final Document doc,
                          final int depth,
                          final HttpServletRequest req,
                          final HttpServletResponse resp) throws WebdavException {
     reportType = getReportType(doc);
 
     if (reportType < 0) {
       throw new WebdavBadRequest();
     }
 
     processDoc(doc, depth);
 
     processResp(req, resp, depth);
   }
 
   /* Apply a node to a parsed request - or the other way - whatever.
    */
   protected void doNodeProperties(final WebdavNsNode node) throws WebdavException {
     final int status = node.getStatus();
 
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
   private void processDoc(final Document doc,
                           int depth) throws WebdavException {
     try {
       WebdavNsIntf intf = getNsIntf();
 
       Element root = doc.getDocumentElement();
 
       if (reportType == reportTypeSync) {
         parseSyncReport(root, depth, intf);
 
         return;
       }
 
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
         pmatch = new PrincipalMatchReport(this, intf);
 
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
   private void parseAclPrincipalProps(final Element root,
                                       final WebdavNsIntf intf) throws WebdavException {
     try {
       Element[] children = getChildrenArray(root);
       boolean hadProp = false;
 
       for (int i = 0; i < children.length; i++) {
         Element curnode = children[i];
 
         if (XmlUtil.nodeMatches(curnode, WebdavTags.prop)) {
           if (hadProp) {
             throw new WebdavBadRequest("More than one DAV:prop element");
           }
           propReq = pm.parseProps(curnode);
 
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
 
   private void parseSyncReport(final Element root,
                                final int depth,
                                final WebdavNsIntf intf) throws WebdavException {
     try {
       Element[] children = getChildrenArray(root);
 
       if ((children.length < 2) || (children.length > 4)) {
         throw new WebdavBadRequest("Expect 2 - 4 child elements");
       }
 
       if (!XmlUtil.nodeMatches(children[0], WebdavTags.syncToken)) {
         throw new WebdavBadRequest("Expect " + WebdavTags.syncToken);
       }
 
       syncToken = XmlUtil.getOneNodeVal(children[0]);
 
       int childI = 1;
       syncLimit = -1;
 
       if (XmlUtil.nodeMatches(children[1], WebdavTags.synclevel)) {
         String lvl = XmlUtil.getElementContent(children[1]);
 
         if (lvl != "1") {
           syncLevel = 1;
         } else if (lvl.equals("infinity")) {
           syncLevel = Headers.depthInfinity;
         } else {
           throw new WebdavBadRequest("Bad sync-level " + lvl);
         }
 
         childI++;
       } else {
         // Cope with back-level clients
         if ((depth != Headers.depthInfinity) && (depth != 1)) {
           throw new WebdavBadRequest("Bad depth");
         }
 
         syncLevel = depth;
       }
 
       syncRecurse = syncLevel == Headers.depthInfinity;
 
       if (XmlUtil.nodeMatches(children[childI], WebdavTags.limit)) {
         syncLimit = Integer.valueOf(XmlUtil.getElementContent(children[childI]));
         childI++;
       }
 
       if (!XmlUtil.nodeMatches(children[childI], WebdavTags.prop)) {
         throw new WebdavBadRequest("Expect " + WebdavTags.prop);
       }
 
       propReq = pm.parseProps(children[childI]);
 
     } catch (NumberFormatException nfe) {
       throw new WebdavBadRequest("Invalid value");
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
   private void parsePrincipalPropertySearch(final Element root,
                                             final int depth,
                                             final WebdavNsIntf intf) throws WebdavException {
     try {
       final Element[] children = getChildrenArray(root);
 
       pps = new PrincipalPropertySearch();
 
       for (int i = 0; i < children.length; i++) {
         final Element curnode = children[i];
 
         if (XmlUtil.nodeMatches(curnode, WebdavTags.propertySearch)) {
           final Element[] pschildren = getChildrenArray(curnode);
 
           if (pschildren.length != 2) {
             throw new WebdavBadRequest();
           }
 
           final String match = XmlUtil.getElementContent(pschildren[1]);
           final Collection<WebdavProperty> props = intf.parseProp(pschildren[0]);
 
           if (!Util.isEmpty(props)) {
             for (final WebdavProperty wd: props) {
               wd.setPval(match);
               pps.props.add(wd);
             }
           }
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
     } catch (final WebdavException wde) {
       throw wde;
     } catch (final Throwable t) {
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
   private void processResp(final HttpServletRequest req,
                            final HttpServletResponse resp,
                            final int depth) throws WebdavException {
     WebdavNsIntf intf = getNsIntf();
 
     if (reportType == reportTypeSync) {
       processSyncReport(req, resp, intf);
       return;
     }
 
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
   private void processExpandProperty(final HttpServletRequest req,
                                      final HttpServletResponse resp,
                                      final int depth,
                                      final WebdavNsIntf intf) throws WebdavException {
     resp.setStatus(WebdavStatusCode.SC_MULTI_STATUS);
     resp.setContentType("text/xml; charset=UTF-8");
 
     startEmit(resp);
 
     openTag(WebdavTags.multistatus);
 
     closeTag(WebdavTags.multistatus);
 
     flush();
   }
 
   private void processSyncReport(final HttpServletRequest req,
                                  final HttpServletResponse resp,
                                  final WebdavNsIntf intf) throws WebdavException {
     WdSynchReport wsr = intf.getSynchReport(getResourceUri(req),
                                             syncToken,
                                             syncLimit,
                                             syncRecurse);
     resp.setStatus(WebdavStatusCode.SC_MULTI_STATUS);
     resp.setContentType("text/xml; charset=UTF-8");
 
     startEmit(resp);
 
     openTag(WebdavTags.multistatus);
 
     if (wsr != null) {
       if (!Util.isEmpty(wsr.items)) {
         for (WdSynchReportItem wsri: wsr.items) {
           openTag(WebdavTags.response);
 
           if (wsri.getCanSync()) {
             pm.doNodeProperties(wsri.getNode(), propReq);
 
             /* No status for changed element - 404 for deleted */
 
             if (wsri.getNode().getDeleted()) {
               addStatus(HttpServletResponse.SC_NOT_FOUND, null);
             }
           } else {
             wsri.getNode().generateHref(xml);
             addStatus(HttpServletResponse.SC_FORBIDDEN, null);
             propertyTagVal(WebdavTags.error, WebdavTags.syncTraversalSupported);
           }
 
           closeTag(WebdavTags.response);
         }
       }
 
       property(WebdavTags.syncToken, wsr.token);
     }
 
     closeTag(WebdavTags.multistatus);
 
     flush();
   }
 
   private void processAclPrincipalPropSet(final HttpServletRequest req,
                                           final HttpServletResponse resp,
                                           final WebdavNsIntf intf) throws WebdavException {
     String resourceUri = getResourceUri(req);
     WebdavNsNode node = intf.getNode(resourceUri,
                                      WebdavNsIntf.existanceMust,
                                      WebdavNsIntf.nodeTypeUnknown,
                                      false);
 
     Collection<String> hrefs = intf.getAclPrincipalInfo(node);
 
     resp.setStatus(WebdavStatusCode.SC_MULTI_STATUS);
     resp.setContentType("text/xml; charset=UTF-8");
 
     startEmit(resp);
 
     openTag(WebdavTags.multistatus);
     if (!hrefs.isEmpty()) {
       openTag(WebdavTags.response);
 
       for (String href: hrefs) {
         WebdavNsNode pnode = getNsIntf().getNode(getNsIntf().getUri(href),
                                                  WebdavNsIntf.existanceMay,
                                                  WebdavNsIntf.nodeTypePrincipal,
                                                  false);
         if (pnode != null) {
           pm.doNodeProperties(pnode, propReq);
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
   private void processPrincipalPropertySearch(final HttpServletRequest req,
                                               final HttpServletResponse resp,
                                               final int depth,
                                               final WebdavNsIntf intf) throws WebdavException {
     resp.setStatus(WebdavStatusCode.SC_MULTI_STATUS);
     resp.setContentType("text/xml; charset=UTF-8");
 
     startEmit(resp);
 
     final String resourceUri = getResourceUri(req);
 
     final Collection<? extends WebdavNsNode> principals =
             intf.getPrincipals(resourceUri, pps);
 
     openTag(WebdavTags.multistatus);
 
     for (final WebdavNsNode node: principals) {
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
   private int getReportType(final Document doc) throws WebdavException {
     try {
       Element root = doc.getDocumentElement();
 
       if (XmlUtil.nodeMatches(root, WebdavTags.expandProperty)) {
         return reportTypeExpandProperty;
       }
 
       if (XmlUtil.nodeMatches(root, WebdavTags.syncCollection)) {
         return reportTypeSync;
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
 
