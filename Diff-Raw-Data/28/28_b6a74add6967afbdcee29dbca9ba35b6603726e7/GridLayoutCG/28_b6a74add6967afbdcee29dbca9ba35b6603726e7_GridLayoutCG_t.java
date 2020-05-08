 /*
  * $Id$
  * Copyright 2000,2005 j-wingS development team.
  *
  * This file is part of j-wingS (http://www.j-wings.org).
  *
  * j-wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.xhtml;
 
 import org.wings.*;
 import org.wings.io.Device;
 import org.wings.plaf.LayoutCG;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 public class GridLayoutCG
         implements LayoutCG {
     /**
      * @param d the device to write the code to
      * @param l the layout manager
      * @throws IOException
      */
     public void write(Device d, SLayoutManager l)
             throws IOException {
         SGridLayout layout = (SGridLayout) l;
         SContainer container = layout.getContainer();
         List components = layout.getComponents();
         SDimension dim = layout.getPreferredSize();
 
         boolean header = layout.getHeader();
         int cellSpacing = layout.getCellSpacing();
         int cellPadding = layout.getCellPadding();
         int border = layout.getBorder();
 
         int cols = layout.getColumns();
         int rows = layout.getRows();
 
         d.print("\n<table ");
        org.wings.plaf.css.Utils.innerPreferredSize(d, container.getPreferredSize());
 
         if (cellSpacing >= 0)
             d.print(" cellspacing=\"").print(cellSpacing).print("\"");
         else
             d.print(" cellspacing=\"0\"");
 
         if (cellPadding >= 0)
             d.print(" cellpadding=\"").print(cellPadding).print("\"");
         else
             d.print(" cellpadding=\"0\"");
 
         if (border > 0)
             d.print(" border=\"").print(border).print("\"");
         else
             d.print(" border=\"0\"");
 
         d.print(">\n");
 
         if (cols <= 0)
             cols = components.size() / rows;
 
         boolean firstRow = true;
 
         int col = 0;
         for (Iterator iter = components.iterator(); iter.hasNext();) {
             if (col == 0)
                 d.print("<tr>");
             else if (col % cols == 0 && iter.hasNext()) {
                 d.print("</tr>\n<tr>");
                 firstRow = false;
             }
 
             SComponent c = (SComponent) iter.next();
 
             if (firstRow && header)
                 d.print("<th");
             else
                 d.print("<td");
 
             Utils.printTableCellAlignment(d, c);
             if (c instanceof SContainer && c.isVisible() && Utils.hasSpanAttributes(c)) {
                 // Adapt inner styles (esp. width of containers)
                 // maybe better restrict to dimension styles only?
                 d.print(" style=\"");
                 Utils.writeAttributes(d, c);
                 d.print("\"");
             }
             d.print(">");
 
             c.write(d);
 
             if (firstRow && header)
                 d.print("</th>");
             else
                 d.print("</td>");
 
             col++;
 
             if (!iter.hasNext())
                 d.print("</tr>\n");
         }
 
         d.print("</table>");
     }
 }
