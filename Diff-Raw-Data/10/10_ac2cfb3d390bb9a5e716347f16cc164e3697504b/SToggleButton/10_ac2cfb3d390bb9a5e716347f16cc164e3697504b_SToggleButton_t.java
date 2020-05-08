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
 
 import java.awt.Color;
 
 import org.wings.plaf.*;
 import org.wings.io.Device;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SToggleButton
     extends SAbstractButton
 {
     private static final String cgClassID = "ToggleButtonCG";
 
     /**
      * TODO: documentation
      *
      * @param text
      */
     public SToggleButton(String text) {
         super(text);
     }
 
     /**
      * TODO: documentation
      *
      */
     public SToggleButton() {
     }
 
     public String getCGClassID() {
         return cgClassID;
     }
 
     public void setCG(ToggleButtonCG cg) {
         super.setCG(cg);
     }

    /**
     * in form components the parameter value of an button is the button
     * text. So just toggle selection, in process request, if it is a request
     * for me.
     */
    protected boolean parseSelectionToggle(String toggleParameter) {
        return true;
    }

 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
