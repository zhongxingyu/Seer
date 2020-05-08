 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
 import org.wings.plaf.AnchorCG;
 
 import java.net.URL;
 
 /**
  * Creates a 'normal'
  * &lt;a href=&quothttp://whatever/&quot&gt;...&lt;/a&gt;
  * HTML link around some components that are stored in the container.
  *
  * @author <a href="mailto:H.Zeller@acm.org">Henner Zeller</a>
  * @version $Revision$
  */
 public class SAnchor extends SContainer {
 
     /**
      * the URL to link to.
      */
     protected SimpleURL url;
 
     /**
      * the target frame/window.
      */
     protected String target;
 
     /**
      * creates an anchor with emtpy URL and target.
      */
     public SAnchor() {
         this((SimpleURL) null, null);
     }
 
     /**
      * create an anchor that points to the URL url.
      *
      * @param url the url to point to.
      */
     public SAnchor(String url) {
         this(url, null);
     }
 
     /**
      * creates an anchor that points to the URL and is openend
      * in the frame or window named target.
      *
      * @param url    the url to link to.
      * @param target the target window or frame.
      */
     public SAnchor(String url, String target) {
         setURL(url);
         setTarget(target);
     }
 
     /**
      * creates an anchor that points to the URL and is openend
      * in the frame or window named target.
      *
      * @param url    the url to link to.
      * @param target the target window or frame.
      */
     public SAnchor(SimpleURL url, String target) {
         setURL(url);
         setTarget(target);
     }
 
     /**
      * set the url this anchor points to.
      *
      * @param ref the url.
      */
     public void setURL(URL ref) {
         if (ref != null) {
             setURL(ref.toString());
         } else {
             setURL((SimpleURL) null);
         }
     }
 
     /**
      * set the url this anchor points to.
      *
      * @param r the url.
      */
     public void setURL(SimpleURL r) {
         SimpleURL oldURL = url;
         url = r;
         if (url == null && oldURL != null
                 || (url != null && !url.equals(oldURL))) {
             reload();
         }
     }
 
     /**
      * set the url this anchor points to.
      *
      * @param url the url.
      */
     public void setURL(String url) {
         setURL(new SimpleURL(url));
     }
 
     /**
      * set the name of the target frame/window.
      */
     public void setTarget(String t) {
         target = t;
     }
 
     /**
      * get the name of the target frame/window.
      */
     public String getTarget() {
         return target;
     }
 
     public SimpleURL getURL() {
         return url;
     }
 
     public void setCG(AnchorCG cg) {
         super.setCG(cg);
     }
 }


