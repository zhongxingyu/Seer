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
 package edu.rpi.cct.webdav.servlet.common;
 
 import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
 import edu.rpi.cct.webdav.servlet.shared.WebdavException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.ws.Holder;
 
 /** Retrieve and process Webdav header values
  *
  *   @author Mike Douglass   douglm@rpi.edu
  */
 public class Headers {
   /** */
   public final static int depthInfinity = Integer.MAX_VALUE;
   /** */
   public final static int depthNone = Integer.MIN_VALUE;
 
   /** Get the depth header
    *
    * @param req    HttpServletRequest
    * @return int   depth - depthInfinity if absent
    * @throws WebdavException
    */
   public static int depth(final HttpServletRequest req) throws WebdavException {
     return depth(req, depthNone);
   }
 
   /** Get the depth header
    *
    * @param req    HttpServletRequest
    * @param def    int default if no header
    * @return int   depth -
    * @throws WebdavException
    */
   public static int depth(final HttpServletRequest req,
                           final int def) throws WebdavException {
     String depthStr = req.getHeader("Depth");
 
     if (depthStr == null) {
       return def;
     }
 
     if (depthStr.equals("infinity")) {
       return depthInfinity;
     }
 
     if (depthStr.equals("0")) {
       return 0;
     }
 
     if (depthStr.equals("1")) {
       return 1;
     }
 
     throw new WebdavBadRequest();
   }
 
   /**
    * @param req
   * @return true if we have a (MS) "brief" header
    */
   public static boolean brief(final HttpServletRequest req) {
    String b = req.getHeader("Depth");
 
    return (b != null) && b.equalsIgnoreCase("T");
   }
 
   /** Create a location header
    *
    * @param resp
    * @param url
    */
   public static void makeLocation(final HttpServletResponse resp,
                                   final String url) {
     resp.setHeader("Location", url);
   }
 
   /**
    */
   public static class IfHeader {
     /** Null if no resource tag */
     public String resourceTag;
 
     /** An entity tag is surrounded by "[" and "]", whereas a token is
      * surrounded by "<" and ">"
      */
     public static class TagOrToken {
       /** True if value is an entity tag */
       public boolean entityTag;
       /** The tag or token value */
       public String value;
 
       /** Constructor
        * @param entityTag
        * @param value
        */
       public TagOrToken(final boolean entityTag,
                         final String value) {
         this.entityTag = entityTag;
         this.value = value;
       }
     }
 
     /** The list - never null */
     public List<TagOrToken> tagsAndTokens = new ArrayList<TagOrToken>();
 
     /**
      * @param tagOrToken
      * @throws WebdavException
      */
     public void addTagOrToken(final String tagOrToken) throws WebdavException {
       boolean entityTag;
 
       if (tagOrToken.length() < 3) {
         throw new WebdavException("Invalid tag or token for If header: " +
             tagOrToken);
       }
 
       if (tagOrToken.startsWith("[")) {
         entityTag = true;
       } else if (tagOrToken.startsWith("<")) {
         entityTag = false;
       } else {
         throw new WebdavException("Invalid tag or token for If header: " +
                                   tagOrToken);
       }
 
       tagsAndTokens.add(new TagOrToken(entityTag,
                                        tagOrToken.substring(1,
                                                             tagOrToken.length() - 1)));
     }
   }
 
   /** From Webdav RFC4918 Section 10.4
    * <pre>
    *  If = "If" ":" ( 1*No-tag-list | 1*Tagged-list )
    *
    *  No-tag-list = List
    *  Tagged-list = Resource-Tag 1*List
    *
    *  List = "(" 1*Condition ")"
    *  Condition = ["Not"] (State-token | "[" entity-tag "]")
    *  ; entity-tag: see Section 3.11 of [RFC2616]
    *  ; No LWS allowed between "[", entity-tag and "]"
    *
    *  State-token = Coded-URL
    *
    *  Resource-Tag = "<" Simple-ref ">"
    *  ; Simple-ref: see Section 8.3
    *  ; No LWS allowed in Resource-Tag
    * </pre>
    *
    * @param req
    * @return populated IfHeader or null
    * @throws WebdavException
    */
   public static IfHeader testIfHeader(final HttpServletRequest req)
           throws WebdavException {
     final String hdrStr = req.getHeader("If");
 
     if (hdrStr == null) {
       return null;
     }
 
     String h = hdrStr.trim();
 
     IfHeader ih = new IfHeader();
 
     if (h.startsWith("<")) {
       int pos = h.indexOf(">");
 
       if (pos < 0) {
         throw new WebdavException("Invalid If header: " + hdrStr);
       }
 
       ih.resourceTag = h.substring(1, pos);
       h = h.substring(pos + 1).trim();
     }
 
     if (!h.startsWith("(") || !h.endsWith(")")) {
       throw new WebdavException("Invalid If header: " + hdrStr);
     }
 
     h = h.substring(1, h.length() - 1);
 
     Holder<Integer> hpos = new Holder<Integer>();
 
     hpos.value = new Integer(0);
 
     for (;;) {
       String ntt = nextTagOrToken(hdrStr, h, hpos);
 
       if (ntt == null) {
         break;
       }
 
       ih.addTagOrToken(ntt);
     }
 
     return ih;
   }
 
   private static String nextTagOrToken(final String hdrStr, // Original header
                                        final String h,      // What we're parsing
                                        final Holder<Integer> hpos) throws WebdavException {
     int pos = hpos.value;
 
     if (pos >= h.length()) {
       return null; // done
     }
 
     String res = null;
     String endDelim;
 
     char delim = h.charAt(pos);
 
     if (delim == '<') {
       endDelim = ">";
     } else if (delim == '[') {
       endDelim = "]";
     } else {
       throw new WebdavException("Invalid If header: " + hdrStr);
     }
 
     pos = h.indexOf(endDelim, pos);
 
     if (pos < 0) {
       throw new WebdavException("Invalid If header: " + hdrStr);
     }
 
     res = h.substring(0, pos + 1);
     pos++;
 
     while ((pos < h.length()) && Character.isSpaceChar(h.charAt(pos))) {
       pos++;
     }
 
     hpos.value = new Integer(pos);
 
     return res;
   }
 
   /** Look for the If-None-Match * header
    *
    * @param req    HttpServletRequest
    * @return boolean true if present
    * @throws WebdavException
    */
   public static boolean ifNoneMatchAny(final HttpServletRequest req)
           throws WebdavException {
     String hdrStr = req.getHeader("If-None-Match");
 
     return "*".equals(hdrStr);
   }
 
   /** Look for the If-None-Match header
    *
    * @param req    HttpServletRequest
    * @return String null if not present
    * @throws WebdavException
    */
   public static String ifNoneMatch(final HttpServletRequest req)
           throws WebdavException {
     return req.getHeader("If-None-Match");
   }
 
   /** Look for the If-Match header
    *
    * @param req    HttpServletRequest
    * @return String null if not present
    * @throws WebdavException
    */
   public static String ifMatch(final HttpServletRequest req)
           throws WebdavException {
     return req.getHeader("If-Match");
   }
 
   /** Look for the If-Schedule-Tag-Match header
    *
    * @param req    HttpServletRequest
    * @return String null if not present
    * @throws WebdavException
    */
   public static String ifScheduleTagMatch(final HttpServletRequest req)
           throws WebdavException {
     return req.getHeader("If-Schedule-Tag-Match");
   }
 
   /** The following is instantiated for If headers
    */
   public static class IfHeaders {
     /** True if we had ifNoneMatchAny */
     public boolean create;
 
     /** Non null if we got if-match
      */
     public String ifEtag;
 
     /** Non null if we got an if header
      */
     public Headers.IfHeader ifHeader;
   }
 
   /**
    * @param req
    * @return populated Ifheaders object
    * @throws WebdavException
    */
   public static IfHeaders processIfHeaders(final HttpServletRequest req)
       throws WebdavException {
     IfHeaders ih = new IfHeaders();
 
     ih.create = ifNoneMatchAny(req);
     ih.ifEtag = ifMatch(req);
     ih.ifHeader = testIfHeader(req);
 
     return ih;
   }
 }
 
