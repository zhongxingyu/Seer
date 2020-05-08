 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.core.dependencyScan;
 
 import java.util.Collection;
 import java.util.Set;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  *          Utils which store the shared code
  */
 class ClassLogUtils {
     public static final String BINARY_PACKAGE = "\\/";
 
 
     private static final String DOMAIN_JAVA = "java.";
     private static final String DOMAIN_JAVAX = "javax.";
     private static final String DOMAIN_COM_SUN = "com.sun";
     private static final String DOMAIN_APACHE = "org.apache.";
     private static final String DOMAIN_MYFACES = "org.apache.myfaces";
 
 
     /**
      * checks if a given package or class
      * belongs to a standard namespaces which is
      * untouchable by an implementor
      *
      * @param in the page or fully qualified classname
      * @return true if it belongs to one of the standard namespaces, false if not
      */
     public static final boolean isStandard(String in) {
         //We dont use a regexp here, because an test has shown that direct startsWith is 5 times as fast as applying
         //a precompiled regexp with match
 
         return in.startsWith(DOMAIN_JAVA) || in.startsWith(DOMAIN_JAVAX) || in.startsWith(DOMAIN_COM_SUN) ||
                 //apache domain has to be treated specially myfaces can be referenced due to our tests and demos, otherwise this one
                 //is also treated as taboo zone
                 ((in.startsWith(DOMAIN_APACHE) && !in.startsWith(DOMAIN_MYFACES)));
     }
 
     /**
      * checks for an allowed namespaces from a given namespace list
      * if the class or package is in the list of allowed namespaces
      * a true is returned otherwise a false
      *
      * @param classOrPackage
      * @param nameSpaces
      * @return
      */
     public static final boolean allowedNamespaces(String classOrPackage, String[] nameSpaces) {
 
         //ok this is probably the fastest way to iterate hence we use this old construct
         //a direct or would be faster but we cannot do it here since we are not dynamic here
         int len = nameSpaces.length;
         for (int cnt = 0; cnt < len; cnt++) {
             if (classOrPackage.startsWith(nameSpaces[cnt])) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * renames the internal member class descriptors of L<qualified classnamewith />; to its source name
      *
      * @param parm the internal class name
      * @return the changed classname in its sourceform
      */
     public static final String internalClassDescriptorToSource(String parm) {
         //we strip the meta information which is not needed
         //aka start mit ( alles strippen bis )
 
         //([A-Za-u]{0,1})
         if (parm.startsWith("(")) {
             parm = parm.substring(parm.lastIndexOf(')') + 1);
         }
 
         //()I for single datatypes
         if (parm.equals("") || parm.length() == 1) {
             return null;
         }
 
         //fully qualified name with meta information
         if (parm.endsWith(";")) {
             //we can skip all the other meta information, a class identifier on sub class level
             //has to start with L the format is <META ATTRIBUTES>L<CLASS QUALIFIED NAME>;
             //The meta attributes can be for instance [ for array
             parm = parm.substring(parm.indexOf('L') + 1, parm.length() - 1);
         }
 
 
         //normal fully qualified name with no meta info attached
         parm = parm.replaceAll(BINARY_PACKAGE, ".");
         return parm;
     }
 
     /**
      * logs a dependency if it does not belong to the standard namespaces
      *
      * @param dependencies the target which has to recieve the dependency in source format
      * @param parms        the list of dependencies which have to be added
      */
    public static final void logParmList(Collection<String> dependencies, Set<String> whiteList, String... parms) {
         for (String parm : parms) {
             if (parm == null) continue;
             if (parm.equals("")) continue;
             parm = internalClassDescriptorToSource(parm);
             if (parm == null || isStandard(parm)) continue;
 
             String[] packages = parm.split("\\.");
 
             StringBuilder fullPackage = null;
             for (String currPackage : packages) {
                 if (fullPackage != null) {
                     fullPackage.append(".");
                     fullPackage.append(currPackage);
                 } else {
                     fullPackage = new StringBuilder(parm.length());
                     fullPackage.append(currPackage);
                 }
 
                 String tempPackage = fullPackage.toString();
                 if (whiteList.contains(tempPackage)) {
                     dependencies.add(parm);
                     break;
                 }
             }
         }
     }
 
 }
