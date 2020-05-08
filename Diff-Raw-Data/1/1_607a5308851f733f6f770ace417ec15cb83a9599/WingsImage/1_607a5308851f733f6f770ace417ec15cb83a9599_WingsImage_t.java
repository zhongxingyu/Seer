 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of the wingS demo (http://wings.mercatis.de).
  *
  * The wingS demo is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package wingset;
 
 import java.awt.Color;
 import javax.swing.Icon;
 
 import org.wings.*;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class WingsImage
     extends SPanel
 {
 
     public WingsImage() {
         add(createExample());
     }
 
     public SComponent createExample() {
         SPanel p = new SPanel();
         final SBorderLayout layout = new SBorderLayout();

         p.setLayout(layout);
         p.setBackground(new java.awt.Color(150, 168, 240));
         SLabel label = new SLabel(new ResourceImageIcon(WingSet.class, 
                                                         "wingS.jpg"));
         label.setHorizontalAlignment(CENTER);
         p.add(label, SBorderLayout.CENTER);
 
         label = new SLabel("Welcome to");
         label.setHorizontalAlignment(CENTER);
         p.add(label, SBorderLayout.NORTH);
 
 
         label = new SLabel("Have fun!");
         label.setHorizontalAlignment(CENTER);
         p.add(label, SBorderLayout.SOUTH);
         
         return p;
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
