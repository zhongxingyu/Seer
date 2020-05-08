 package org.codehaus.xfire.jaxws.type;
 
 import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
 import org.codehaus.xfire.aegis.type.TypeCreator;
 
 public class JAXWSTypeRegistry extends DefaultTypeMappingRegistry
 {
     public JAXWSTypeRegistry()
     {
         super(true);
     }
 
     protected TypeCreator createTypeCreator()
     {
        return new JAXWSTypeCreator();
     }
 }
