 package com.cloudbees.cloud_resource.types;
 
 import java.util.List;
 
 /**
  * A CR that manages bindings of another CR.
  *
  * @author Vivek Pandey
  */
 @CloudResourceType("https://types.cloudbees.com/binding/collection")
 public interface BindingCollection extends CloudResource{
    List<Edge> edges();
 }
