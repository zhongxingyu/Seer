 // -*- mode: Java; c-basic-offset: 3; tab-width: 8; indent-tabs-mode: nil -*-
 // Copyright (C) 2008 Andreas Krey, Ulm, Germany <a.krey@gmx.de>
 
 package gloop;
 
 public class LocalScope extends Scope {
    final public static String rcsid = "$Header$";
 
    private int nextOffset = 0;
 
    public LocalScope (Scope p) {
       super (p);
    }
 
    abstract public class FrameEnt extends Ent {
       private int offset;
 
       public FrameEnt (String n) {
          super (n);
          offset = nextOffset ++;
       }
 
       public int getOffset () { return offset; }
       abstract public boolean isVar ();
    }
 
    public FrameEnt putDef (final String n) {
       return new FrameEnt (n) {
            public String desc () { return "<def " + n + ">"; }
            public boolean isVar () { return false; }
         };
    }
 
    public FrameEnt putVar (final String n) {
       return new FrameEnt (n) {
            public String desc () { return "<var " + n + ">"; }
            public boolean isVar () { return true; }
         };
    }
 }
 
