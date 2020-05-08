 package com.sun.facelets.compiler;
 
 import com.sun.facelets.tag.Tag;
 import com.sun.facelets.tag.TagLibrary;
 
 final class TrimmedTagUnit extends TagUnit {
 
    public TrimmedTagUnit(TagLibrary library, String namespace, String name, Tag tag, String id) {
        super(library, namespace, name, tag, id);
     }
 
 }
