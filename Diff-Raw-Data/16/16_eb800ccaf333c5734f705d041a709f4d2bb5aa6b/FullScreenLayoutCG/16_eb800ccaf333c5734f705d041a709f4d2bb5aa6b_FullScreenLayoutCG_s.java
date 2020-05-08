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
 
 import java.io.IOException;
 import java.util.*;
 
 import org.wings.*;
 import org.wings.io.*;
 import org.wings.plaf.*;
 
 /**
  * CG for <a href="../../SFullScreenLayout.html">SFullScreenLayout</a>.
  * @author <a href="mailto:andre.lison@crosstec.de">Andre Lison</a>
  * @version $Revision$
  */
 public class FullScreenLayoutCG
     extends BorderLayoutCG
 {
     /**
      * TODO: documentation
      *
      * @param d the device to write the code to
      * @param l the layout manager
      * @throws IOException
      */
     public void write(Device d, SLayoutManager l)
         throws IOException
     {
		d.append("\n<div style=\"position:absolute; z-index:1; left: 0; top: 0; width: 100%; height: 100%\">"); 
         super.write( d, l );
         d.append("</div>");
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
