 /* ====================================================================
  * The Apache Software License, Version 1.1
  *
  * Copyright (c) 2000 The Apache Software Foundation.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution,
  *    if any, must include the following acknowledgment:
  *       "This product includes software developed by the
  *        Apache Software Foundation (http://www.apache.org )."
  *    Alternately, this acknowledgment may appear in the software itself,
  *    if and wherever such third-party acknowledgments normally appear.
  *
  * 4. The names "Apache" and "Apache Software Foundation" must
  *    not be used to endorse or promote products derived from this
  *    software without prior written permission. For written
  *    permission, please contact apache@apache.org.
  *
  * 5. Products derived from this software may not be called "Apache",
  *    nor may "Apache" appear in their name, without prior written
  *    permission of the Apache Software Foundation.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Apache Software Foundation.  For more
  * information on the Apache Software Foundation, please see
  * <http://www.apache.org >.
  *
  * Portions of this software are based upon public domain software
  * originally written at the National Center for Supercomputing Applications,
  * University of Illinois, Urbana-Champaign.
  */
 package net.sf.commonclipse;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.jdt.core.Flags;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.JavaModelException;
 
 /**
  * Generator for toString() methods
  * @author fgiust
  * @version $Revision$ ($Author$)
  */
 public final class ToStringGenerator extends Generator
 {
 
     /**
      * class name for the ToString buider
      */
     private static final String BUILDER_CLASS = "org.apache.commons.lang.builder.ToStringBuilder";
 
     /**
      * singleton for ToStringGenerator
      */
     private static Generator instance = new ToStringGenerator();
 
     /**
      * use getInstance() to obtain an instance of ToStringGenerator
      */
     private ToStringGenerator()
     {
     }
 
     /**
      * returns the ToStringGenerator instance
      * @return instance of ToStringGenerator
      */
     public static Generator getInstance()
     {
         return instance;
     }
 
     /**
      * @see net.sf.commonclipse.Generator#getMethodName()
      */
     protected String getMethodName()
     {
         return "toString";
     }
 
     /**
      * @see net.sf.commonclipse.Generator#addImports(org.eclipse.jdt.core.IType)
      */
     protected void addImports(IType type) throws JavaModelException
     {
         type.getCompilationUnit().createImport(BUILDER_CLASS, null, null);
 
         if (CCPluginPreferences.getPreferences().useCustomToStringStyle())
         {
             type.getCompilationUnit().createImport(
                 CCPluginPreferences.getPreferences().getToStringStyleQualifiedClass(),
                 null,
                 null);
         }
     }
 
     /**
      * @see net.sf.commonclipse.Generator#createMethod(org.eclipse.jdt.core.IType)
      */
     protected String createMethod(IType type) throws JavaModelException
     {
 
         StringBuffer buffer = new StringBuffer();
 
         buffer.append(getJavadoc());
 
         buffer.append("    public String toString()\n" + "    {\n" + "        return new ToStringBuilder(this");
 
         if (CCPluginPreferences.getPreferences().useCustomToStringStyle())
         {
             buffer.append(", " + CCPluginPreferences.getPreferences().getToStringStyleClassAndConstant());
         }
 
         buffer.append(")\n");
 
         if (CCPluginPreferences.getPreferences().appendSuperToToString())
         {
             buffer.append(".appendSuper(super.toString())\n");
         }
 
         if (CCPluginPreferences.getPreferences().useJavabeanToString())
         {
             buffer.append(buildAppenderListFromBean(type));
         }
         else
         {
             buffer.append(buildAppenderList(type));
         }
 
         buffer.append(".toString();\n}");
 
         return buffer.toString();
     }
 
     /**
      * iterates on javabean properties and calls getFieldAppender for all of them
      * @param type IType
      * @return String
      * @throws JavaModelException exception in analyzing properties
      */
     private String buildAppenderListFromBean(IType type) throws JavaModelException
     {
         // temporary map of methods to avoid duplicated entry
         Map getterMethods = buildMethodsMap(type);
 
         // fields to match method names
         Map fields = buildFieldMap(type);
 
         // now iterates on generated method list and create the toString method body
         Iterator iterator = getterMethods.entrySet().iterator();
 
         StringBuffer buffer = new StringBuffer();
 
         while (iterator.hasNext())
         {
             IMethod method = (IMethod) ((Map.Entry) iterator.next()).getValue();
             String methodName = method.getElementName();
             String propertyName = getJavabeanProperyName(methodName);
 
             // check if propertyName is excluded
             if (!isExcluded(propertyName))
             {
                 // does a field with the same name exist?
                 IField matchingField = (IField) fields.get(propertyName);
 
                 if (matchingField != null)
                 {
                     if (propertyName.equals(matchingField.getElementName()))
                     {
                         // if we have a fields with the same name and type of the property, use it instead
                         // of the getter method for the toString()
                         buffer.append(getFieldAppender(propertyName, propertyName));
                         continue;
                     }
                 }
 
                 // else add the getter method to the toString
                 buffer.append(getFieldAppender(propertyName, methodName + "()"));
             }
         }
         return buffer.toString();
     }
 
     /**
      * returns a Map containing all the javabean getter methods in this type and its supertypes
      * @param type IType
      * @return Map containg method names - IMethod objects
      * @throws JavaModelException exception in analyzing type
      */
     private Map buildMethodsMap(IType type) throws JavaModelException
     {
         Map getterMethods = new HashMap();
 
         // iterates on hierarchy, looking for properties also if defined in superclasses
         ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
         IType[] types = hierarchy.getAllClasses();
 
         for (int j = 0; j < types.length; j++)
         {
             IMethod[] superMethods = types[j].getMethods();
 
             for (int x = 0; x < superMethods.length; x++)
             {
                 IMethod method = superMethods[x];
 
                 if (isJavabeanGetter(method))
                 {
                     getterMethods.put(method.getElementName(), method);
                 }
             }
         }
 
         return getterMethods;
     }
 
     /**
      * return the javabean property name from the getter method name
      * @param methodName getter method name
      * @return javabean property name (ex. "test" for "getTest()")
      */
     private String getJavabeanProperyName(String methodName)
     {

        String propertyName;
        if (methodName.startsWith("get"))
        {
            propertyName = methodName.substring(3, methodName.length());
        }
        else
        {
            // should be a boolean property isXXX
            propertyName = methodName.substring(2, methodName.length());
        }
 
         if (propertyName.length() > 1 && Character.isLowerCase(propertyName.charAt(1)))
         {
             propertyName =
                 Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1, propertyName.length());
         }
         else if (propertyName.length() == 1)
         {
             propertyName = propertyName.toLowerCase();
         }
         return propertyName;
     }
 
     /**
      * check if the given method is a javabean getter (start with get or is for boolean properties, no param)
      * @param method IMethod
      * @return <code>true</code> if the method is a javabean property accessor
      * @throws JavaModelException exception thrown in analyzing method
      */
     private boolean isJavabeanGetter(IMethod method) throws JavaModelException
     {

         if (method.getNumberOfParameters() == 0 && Flags.isPublic(method.getFlags()))
         {
             String methodName = method.getElementName();

             if (methodName.length() > 3 && methodName.startsWith("get"))
             {
                 return true;
             }
             else if (
                 methodName.length() > 2
                     && methodName.startsWith("is")
                    && ("Z".equals(method.getReturnType()) || "QBoolean;".equals(method.getReturnType())))
             {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * generate the method javadoc
      * @return String javadoc
      */
     private String getJavadoc()
     {
         return "/**\n * @see java.lang.Object#toString()\n */\n";
     }
 
     /**
      * @see net.sf.commonclipse.Generator#getExistingMethod(org.eclipse.jdt.core.IType)
      */
     protected IMethod getExistingMethod(IType type)
     {
         return type.getMethod(getMethodName(), new String[0]);
     }
 
     /**
      * @see net.sf.commonclipse.Generator#getFieldAppender(java.lang.String, java.lang.String)
      */
     protected String getFieldAppender(String fieldName, String accessor)
     {
         return ".append(\"" + fieldName + "\", this." + accessor + ")\n";
     }
 
 }
