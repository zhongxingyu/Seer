 /*
  * Fast Infoset ver. 0.1 software ("Software")
  * 
  * Copyright, 2004-2005 Sun Microsystems, Inc. All Rights Reserved. 
  * 
  * Software is licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License. You may
  * obtain a copy of the License at:
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations.
  * 
  *    Sun supports and benefits from the global community of open source
  * developers, and thanks the community for its important contributions and
  * open standards-based technology, which Sun has adopted into many of its
  * products.
  * 
  *    Please note that portions of Software may be provided with notices and
  * open source licenses from such communities and third parties that govern the
  * use of those portions, and any licenses granted hereunder do not alter any
  * rights and obligations you may have under such open source licenses,
  * however, the disclaimer of warranty and limitation of liability provisions
  * in this License will apply to all Software in this distribution.
  * 
  *    You acknowledge that the Software is not designed, licensed or intended
  * for use in the design, construction, operation or maintenance of any nuclear
  * facility.
  *
  * Apache License
  * Version 2.0, January 2004
  * http://www.apache.org/licenses/
  *
  */
 
 package org.jvnet.fastinfoset.sax;
 
 import org.xml.sax.Attributes;
 
 
 /**
  * Interface for a list of XML attributes that may contain encoding algorithm
  * data.
  * <p>
 * Implementations shall ensure that the {@link Attributes#getValue{int)} method
  * correctly returns a String object even if the attribute is represented by
  * as algorithm data.
  * <p>
  * If an attribute has algorithm data then the {@link #getAlgorithmData} method
  * shall return a non <code>null</code> value.
  *
  * @see org.jvnet.fastinfoset.sax.FastInfosetReader
  * @see org.xml.sax.XMLReader
  */
 public interface EncodingAlgorithmAttributes extends Attributes {
     
     /**
      * Return the URI of the encoding algorithm.
      *
      * <p>If the algorithm data corresponds to a built-in encoding algorithm
      *    then the null is returned.</p>
      *
      * <p>If the algorithm data corresponds to an application-defined encoding 
      *    algorithm then the URI of the algorithm is returned.</p>
      *
      * <p>If {@link #getAlgorithmData(int)} returns null then the result of 
      *    this method is undefined.<p>
      *
      * @param index The attribute index (zero-based).
      * @return The URI.
      */
     public String getAlgorithmURI(int index);
  
     /**
      * Return the index of the encoding algorithm.
      *
      * <p>If {@link #getAlgorithmData(int)} returns null then the result of 
      *    this method is undefined.<p>
      *
      * @param index The attribute index (zero-based).
      * @return The index
      * @see org.jvnet.fastinfoset.EncodingAlgorithmIndexes       
      */
     public int getAlgorithmIndex(int index);
     
     /**
      * Return the data of the encoding algorithm.
      *
      * <p>If the algorithm data corresponds to a built-in encoding algorithm
      *    then an Object corresponding to the Java primitive type is returned.</p>
      *
      * <p>If the algorithm data corresponds to an application-defined encoding 
      *    algorithm then an Object that is an instance of <code>byte[]</code>
      *    is returned if there is no EncodingAlgorithm registered for the 
      *    application-defined encoding algorithm URI. Otherwise, an Object produced 
      *    from the registeredEncodingAlgorithm is returned.</p>
      *
      * <p>If there no encoding algorithm data associated an attribute then 
      *    <code>null</code> is returned.<p>
      *
      * @param index The attribute index (zero-based).
      * @return The data
      */
     public Object getAlgorithmData(int index);    
 }
