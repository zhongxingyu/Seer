 package com.knaptus.domainiser.example;
 
 import com.knaptus.domainiser.core.DomainResolver;
 
 /**
  * Example of a domain resolver.
  *
  * @author Aditya Bhardwaj
  */
 public class ExampleDomainResolver implements DomainResolver {
 
     @Override
     public boolean isDomainModel(Class domain) {
        return domain.getCanonicalName().contains("intelladept");
 
     }
 }
