 /*
  * Copyright (c) 2008, Carman Consulting, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.domdrides.maven.plugin;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.velocity.VelocityContext;
 
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @goal generate-repository
  */
 public class GenerateRepositoryMojo extends AbstractGeneratorMojo
 {
 //**********************************************************************************************************************
 // Fields
 //**********************************************************************************************************************
 
     private static Map<String, String> repoImplMap = new HashMap<String, String>();
 
     /**
      * @parameter expression="${entityName}"
      * @required
      */
     private String entityName;
 
     /**
      * @parameter expression = "${repositoryType}" default-value="hibernate"
      * @required
      */
     private String repositoryType;
 
 //**********************************************************************************************************************
 // Static Methods
 //**********************************************************************************************************************
 
     static
     {
         repoImplMap.put("hibernate", "org.domdrides.hibernate.repository.HibernateRepository");
     }
 
 //**********************************************************************************************************************
 // Mojo Implementation
 //**********************************************************************************************************************
 
     public void execute() throws MojoExecutionException, MojoFailureException
     {
         final String interfacePackageName = getRepositoryPackage();
         final String interfaceName = entityName + "Repository";
         final String implPackageName = interfacePackageName + "." + repositoryType;
         final String implClassName = interfaceName + "Impl";
         Class entityClass = getClass(getEntityPackage() + "." + entityName);
         Class implSuperclass = getClass(repoImplMap.get(repositoryType));
         final Class idType = getPropertyType(entityClass, "id");
         final VelocityContext ctx = new VelocityContext();
         ctx.put("entityClass", entityClass);
         ctx.put("idType", idType);
         ctx.put("interfacePackageName", interfacePackageName);
         ctx.put("interfaceName", interfaceName);
         ctx.put("projectVersion", getProjectVersion());
         generateSourceFile("RepositoryInterfaceTemplate.vm", ctx, interfacePackageName + "." + interfaceName);
 
         ctx.put("implSuperclass", implSuperclass);
         ctx.put("implPackageName", implPackageName);
         ctx.put("implClassName", implClassName);
         generateSourceFile("RepositoryImplTemplate.vm", ctx, implPackageName + "." + implClassName);
     }
 
 //**********************************************************************************************************************
 // Getter/Setter Methods
 //**********************************************************************************************************************
 
     public String getEntityName()
     {
         return entityName;
     }
 
     public void setEntityName(String entityName)
     {
         this.entityName = entityName;
     }
 
 //**********************************************************************************************************************
 // Other Methods
 //**********************************************************************************************************************
 
    private Class findTypeBoundToVariable(Class c, TypeVariable idTypeVariable)
     {
        while (c != null)
         {
            Type genericSupertype = c.getGenericSuperclass();
             if (genericSupertype instanceof ParameterizedType)
             {
                 ParameterizedType pt = (ParameterizedType) genericSupertype;
                 final Class rawType = (Class) pt.getRawType();
                 TypeVariable[] typeParameters = rawType.getTypeParameters();
                 for (int i = 0; i < typeParameters.length; i++)
                 {
                     TypeVariable typeParameter = typeParameters[i];
                     if (typeParameter.equals(idTypeVariable))
                     {
                         return (Class) pt.getActualTypeArguments()[i];
                     }
                 }
             }
            c = c.getSuperclass();
         }
         return null;
     }
 
    private Class getPropertyType(Class c, String property) throws MojoExecutionException
     {
         Method readMethod = getReadMethod(c, property);
         Type type = readMethod.getGenericReturnType();
         if (type instanceof TypeVariable)
         {
             TypeVariable typeVariable = (TypeVariable) type;
             return findTypeBoundToVariable(c, typeVariable);
         } else if (type instanceof Class)
         {
             return (Class) type;
         }
         return null;
     }
 
     private Method getReadMethod(Class c, String property) throws MojoExecutionException
     {
         try
         {
             Method readMethod = null;
             PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(c).getPropertyDescriptors();
             for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
             {
                 if (property.equals(propertyDescriptor.getName()))
                 {
                     readMethod = propertyDescriptor.getReadMethod();
                     break;
                 }
             }
             return readMethod;
         }
         catch (IntrospectionException e)
         {
             throw new MojoExecutionException("Unable to determine id type.", e);
         }
     }
 }
