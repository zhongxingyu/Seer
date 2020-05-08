 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
 import java.io.Serializable;
 import java.net.URL;
 
 /**
  * A small fixed size picture, typically used to decorate components. 
  * This icon can be accessed via an URL; this URL is passed to a browser
  * that fetches it from there.
  * 
  * @author <a href="mailto:H.Zeller@acm.org">Henner Zeller</a>
  */
 public interface SIcon extends URLResource
 {
     /**
      * @return the width of the icon, or -1 if unknown.
      */
     int getIconWidth();
 
     /**
      * @return the height of the icon, or -1 if unknown.
      */
     int getIconHeight();
 
     /**
      * sets the width of the icon, -1 if unknown.
      */
     void setIconWidth(int width);
 
     /**
     * sets the height of the icon, -1 if unknown.
      */
     void setIconHeight(int height);
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
