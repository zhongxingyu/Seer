 /*
  * ComponentSegment.java
  *
  * Copyright (c) 2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package quill.textbase;
 
 import quill.docbook.Block;
 import quill.docbook.Title;
 
 public final class ComponentSegment extends Segment
 {
    public ComponentSegment() {
         super();
     }
 
     /*
      * We assume that a Chapter always has a title.
      */
     public Block createBlock() {
         return new Title();
     }
 
     Segment createSimilar() {
         final Segment result;
 
         result = new ComponentSegment();
         result.setParent(this.getParent());
 
         return result;
     }
 }
