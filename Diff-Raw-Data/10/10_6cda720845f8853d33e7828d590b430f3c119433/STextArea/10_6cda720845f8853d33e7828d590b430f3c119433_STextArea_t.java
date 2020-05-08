 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings;
 
 import org.wings.plaf.*;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:armin.haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class STextArea
     extends STextComponent
 {
     private static final String cgClassID = "TextAreaCG";
 
    private int rows = 0;
 
    private int columns = 0;
 
     /**
      * allowed parameters
      * are off(0), virtual(1), physical(2)
      * default value is off
      */
    private int lineWrap = SConstants.NO_WRAP;
 
 
     /**
      * TODO: documentation
      *
      * @param text
      */
     public STextArea(String text) {
         super(text);
     }
 
     /**
      * TODO: documentation
      *
      */
     public STextArea() {
         super();
     }
 
 
     /**
      * TODO: documentation
      *
      * @param rows
      */
     public void setRows(int r) {
         int oldRows = rows;
         rows = r;
         if (oldRows != rows)
             reload(ReloadManager.RELOAD_CODE);
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public int getRows() {
         return rows;
     }
 
 
     /**
      * TODO: documentation
      *
      * @param c
      */
     public void setColumns(int c) {
         int oldColumns = columns;
         columns = c;
         if (columns != oldColumns)
             reload(ReloadManager.RELOAD_CODE);
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public int getColumns() {
         return columns;
     }
 
 
     /**
      * TODO: documentation
      *
      * @param lw
      */
     public void setLineWrap(int lw) {
         if ( lw >= 0 && lw < 3 )
             lineWrap = lw;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public int getLineWrap() {
         return lineWrap;
     }
 
     public String getCGClassID() {
         return cgClassID;
     }
 
     public void setCG(TextAreaCG cg) {
         super.setCG(cg);
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
