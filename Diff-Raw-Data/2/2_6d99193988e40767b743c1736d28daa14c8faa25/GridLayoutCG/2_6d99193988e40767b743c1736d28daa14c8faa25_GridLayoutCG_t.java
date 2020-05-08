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
 import org.wings.util.CGUtil;
 
 public class GridLayoutCG
     implements LayoutCG
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
         SGridLayout layout = (SGridLayout)l;
         SContainer container = layout.getContainer();
         List components = layout.getComponents();
 
         boolean header = layout.getHeader();
         boolean relative = layout.isRelative();
         int width = layout.getWidth();
         int cellSpacing = layout.getCellSpacing();
         int cellPadding = layout.getCellPadding();
         int border = layout.getBorder();
 
         int cols = layout.getColumns();
         int rows = layout.getRows();
 
         d.append("\n<table");
         if (cellSpacing >= 0)
             d.append(" cellspacing=\"").append(cellSpacing).append("\"");
         else
             d.append(" cellspacing=\"0\"");
 
         if (cellPadding >= 0)
             d.append(" cellpadding=\"").append(cellPadding).append("\"");
 
 		CGUtil.writeSize( d, container );
 
         if (border > 0)
             d.append(" border=\"").append(border).append("\"");
         else
            d.append(" border=\"0\"");
 
         if (container != null && container.getBackground() != null)
             d.append(" bgcolor=\"#").
                 append(Utils.toColorString(container.getBackground())).append("\"");
 
         d.append(">\n");
 
         if (cols <= 0)
             cols = components.size() / rows;
 
         boolean firstRow = true;
 
         int col = 0;
         for (Iterator iter = components.iterator(); iter.hasNext();) {
             if (col == 0)
                 d.append("<tr>");
             else if (col%cols == 0 && iter.hasNext()) {
                 d.append("</tr>\n<tr>");
                 firstRow = false;
             }
 
             SComponent c = (SComponent)iter.next();
 
             if (firstRow && header) {
                 d.append("<th>");
             }
             else {
                 d.append("<td");
                 Utils.appendTableCellAlignment(d, c);
                 d.append(">\n");
             }
 
             c.write(d);
 
             if (firstRow && header)
                 d.append("</th>");
             else
                 d.append("</td>");
 
             col++;
 
             if (!iter.hasNext())
                 d.append("</tr>\n");
         }
 
         d.append("</table>");
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
