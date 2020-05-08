 package com.sun.identity.admin;
 
 import javax.el.ELResolver;
 import javax.faces.context.FacesContext;
 
 public class ManagedBeanResolver {
 
     public Object resolve(String name) {
         FacesContext fcontext = FacesContext.getCurrentInstance();
        ELResolver resolver = fcontext.getELContext().getELResolver();
         Object o = resolver.getValue(fcontext.getELContext(), null, name);
 
         return o;
     }

    /*
    public Object resolve(String name) {
        FacesContext fc = FacesContext.getCurrentInstance();
        final StringBuffer valueBinding = new StringBuffer();  
        valueBinding.append('#');  
        valueBinding.append('{');  
        valueBinding.append(name);  
        valueBinding.append('}');  
        
        final Object o = fc.getApplication().createValueBinding(valueBinding.toString()).getValue(fc);  

        return o;
    }
    */
 }
