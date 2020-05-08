 /*
  New BSD license: http://opensource.org/licenses/bsd-license.php
 
  Copyright (c) 2003, 2004, 2005 Sun Microsystems, Inc.
  901 San Antonio Road, Palo Alto, CA 94303 USA. 
  All rights reserved.
 
 
  Redistribution and use in source and binary forms, with or without 
  modification, are permitted provided that the following conditions are met:
 
  - Redistributions of source code must retain the above copyright notice, 
   this list of conditions and the following disclaimer.
  - Redistributions in binary form must reproduce the above copyright notice, 
   this list of conditions and the following disclaimer in the documentation 
   and/or other materials provided with the distribution.
  - Neither the name of Sun Microsystems, Inc. nor the names of its contributors
   may be used to endorse or promote products derived from this software 
   without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  POSSIBILITY OF SUCH DAMAGE.
 */
 package net.java.rdf.sommer;
 
 import net.java.rdf.annotations.rdf;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Classes that implement a Mapper need to implement this interface
  *
  * @author Henry Story
  */
 public abstract class Mapping implements Mapper, RewriteMapper, RDFFactory {
 
     static final HashMap<String, List<Class>> classmap = new HashMap<String, List<Class>>();
 
 
     public void preload(Class... classes) {
           for (Class c: classes) {
               rdf ann = (rdf) c.getAnnotation(rdf.class);
               String type;
               if (ann == null) { //create url
                  type = "urn:java:"+c.getCanonicalName();
                  System.out.println("url of "+c+" is "+type);
               } else {
                  type = ann.value();
               }
               List<Class> list = classmap.get(type);
               if (list == null) {
                   list = new ArrayList<Class>();
                   classmap.put(type,list);
               }
               list.add(c);
           }
     }
 
 
     /**
      * return the most specific subclass of clazz that is mapped to one of the given rdf types
      *
      * we assume that we don't have two subclasses of <code>clazz</code> that are associated with the same
      * rdf type. If this is not the case then the returned class will be randomly selected from one of the available
      * ones. We also have to assume that the types given in rdf map to exclusive java classes. So the collection of typeUris
      * should not be associated with classes in distinct classes in the java class hierarchy.  Because there is no clear way to
      * decide which one should be returned.
      *
      * see the thread on sommer.dev.java.net written over the weekend of  10 Dec 2006
      * 
      *
      * mhh: problem: if an object
      * @param clazz
      * @param typeUris
      * @return a subclass of clazz or clazz itself
      */
     Class mostSpecificSubClass(Class clazz, ArrayList<String> typeUris) {
         ArrayList<Class> mappedClasses = new ArrayList<Class>();
         for(String uri: typeUris) {
             List<Class> classes = classmap.get(uri);
             if (classes != null) mappedClasses.addAll(classes);
         }
         Class solution =clazz;
        boolean foundOne = false;
         //now we have to find the most specific class
         for (Class candidate: mappedClasses) {
              if (solution.isAssignableFrom(candidate)) {
                 solution = candidate;
                foundOne = true;
             }
         }
        if (foundOne) {
        	return solution;
        } else {
        	return null;
        }
     }
 
     public void cogitate(List<Inferencer> inferenceList) {
           for (Inferencer inf: inferenceList) {
               inf.infer(this);
           }
     }
 }
