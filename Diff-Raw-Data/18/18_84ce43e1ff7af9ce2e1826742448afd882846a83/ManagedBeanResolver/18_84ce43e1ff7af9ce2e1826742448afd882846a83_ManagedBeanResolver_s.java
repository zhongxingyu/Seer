 package com.sun.identity.admin;
 
 import javax.el.ELResolver;
 import javax.faces.context.FacesContext;
 
 public class ManagedBeanResolver {
 
     public Object resolve(String name) {
         FacesContext fcontext = FacesContext.getCurrentInstance();
        ELResolver resolver = fcontext.getApplication().getELResolver();
         Object o = resolver.getValue(fcontext.getELContext(), null, name);
 
         return o;
     }
 }
