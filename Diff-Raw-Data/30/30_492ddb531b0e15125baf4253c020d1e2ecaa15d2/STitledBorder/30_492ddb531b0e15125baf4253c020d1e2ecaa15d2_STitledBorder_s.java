 /*
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
 package org.wings.border;
 
 /**
  * Adds a border with a title to a component.
  *
  */
 public class STitledBorder extends SAbstractBorder {
    private SBorder border;

     private String title;
 
     /**
      * Constructor for STitledBorder.
      */
     public STitledBorder(SBorder border) {
         setBorder(border);
     }
 
     /**
      * Constructor for STitledBorder.
      *
      * @param border the border to use
      * @param title  the title to display
      */
     public STitledBorder(SBorder border, String title) {
         this(border);
         setTitle(title);
     }
 
     /**
      * Constructor for STitledBorder. Default border
      * type is {@link SEtchedBorder}, thickness 2
      */
     public STitledBorder(String title) {
        this(new SEmptyBorder(0,0,0,0));
         setTitle(title);
     }
 
     /**
      * Gets the border.
      *
      * @return Returns a SBorder
      */
     public SBorder getBorder() {
        return border;
     }
 
     /**
      * Sets the border.
      *
      * @param border The border to set
      */
     public void setBorder(SBorder border) {
        this.border = border;
        specs = ((SAbstractBorder)border).specs;
     }
 
     /**
      * Gets the title.
      *
      * @return Returns a String
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * Sets the title.
      *
      * @param title The title to set
      */
     public void setTitle(String title) {
         this.title = title;
     }
 }
