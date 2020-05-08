 package com.sun.identity.admin.model;
 
 import com.sun.identity.admin.DeepCloneable;
import java.util.List;
 
 public interface Action extends DeepCloneable {
     public String getName();
    public Object getValue();
 }
