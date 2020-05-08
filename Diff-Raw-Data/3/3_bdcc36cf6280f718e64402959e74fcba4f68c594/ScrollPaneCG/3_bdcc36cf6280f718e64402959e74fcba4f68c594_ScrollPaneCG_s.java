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
 
 package org.wings.plaf.xhtml;
 
 import java.awt.*;
 import java.io.IOException;
 
import org.wings.*; import org.wings.border.*;
 import org.wings.io.*;
 import org.wings.plaf.*;
 
 public class ScrollPaneCG
     extends org.wings.plaf.AbstractComponentCG
     implements org.wings.plaf.ScrollPaneCG
 {
     public void installCG(SComponent component) {
     }
 
     public void uninstallCG(SComponent component) {
     }
     
     public void write(Device d, SComponent c)
         throws IOException
     {
         SScrollPane scrollPane = (SScrollPane)c;
         SComponent scrollable = (SComponent)scrollPane.getScrollable();
         SBorder border = c.getBorder();
 
         if ( scrollPane.getPreferredSize() == null && 
              scrollable.getPreferredSize() != null ) {
             scrollPane.setPreferredSize(scrollable.getPreferredSize());
         }
 
         scrollPane.synchronizeAdjustables();
 
         
         Utils.writeBorderPrefix(d, border);
         Utils.writeContainerContents(d, scrollPane);
         Utils.writeBorderPostfix(d, border);
     }
    
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
