 /**
  * Copyright (c) 2008, Aberystwyth University
  *
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions 
  * are met:
  * 
  *  - Redistributions of source code must retain the above 
  *    copyright notice, this list of conditions and the 
  *    following disclaimer.
  *  
  *  - Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in 
  *    the documentation and/or other materials provided with the 
  *    distribution.
  *    
  *  - Neither the name of the Centre for Advanced Software and 
  *    Intelligent Systems (CASIS) nor the names of its 
  *    contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF 
  * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF 
  * SUCH DAMAGE.
  */
 package org.purl.sword.base;
 
 import nu.xom.Attribute;
 import nu.xom.Element;
 
 import org.apache.log4j.Logger;
 import org.purl.sword.atom.Author;
 import org.purl.sword.atom.Title;
 
 /**
  * Extension of the SWORD Entry class, specialized for Error Documents. 
  * 
  * @author Stuart Lewis
  */
 public class SWORDErrorDocument extends SWORDEntry 
 {
 	/**
 	* Local name for the element. 
 	*/
    public static final String ELEMENT_NAME = "error";
    
    /**
     * The logger.
     */
    private static Logger log = Logger.getLogger(SWORDErrorDocument.class);
    
    /**
     * The Error URI
     */
    private String errorURI;
    
    /**
    * Create the error document (intended to be used when unmarshalling an error document
    * as this will set the errorURI)
    */
   public SWORDErrorDocument() {
	   super(Namespaces.PREFIX_SWORD, ELEMENT_NAME);
	}
   
   /**
     * Create the error document
     * 
     * @param errorURI The URI of the error
     */
    public SWORDErrorDocument(String errorURI) {
 	   super(Namespaces.PREFIX_SWORD, ELEMENT_NAME);
 	   this.errorURI = errorURI;
    }
    
    /**
     * Overrides the marshall method in the parent SWORDEntry. This will 
     * call the parent marshall method and then add the additional 
     * elements that have been added in this subclass.  
     */
    public Element marshall()
    {
 	   Element entry = new Element(getQualifiedName(), Namespaces.NS_SWORD);
 	   entry.addNamespaceDeclaration(Namespaces.PREFIX_SWORD, Namespaces.NS_SWORD);
 	   entry.addNamespaceDeclaration(Namespaces.PREFIX_ATOM, Namespaces.NS_ATOM);
 	   Attribute error = new Attribute("href", errorURI);
        entry.addAttribute(error);
 	   super.marshallElements(entry);
        return entry;
    }
 
    /**
     * Overrides the unmarshall method in the parent SWORDEntry. This will 
     * call the parent method to parse the general Atom elements and
     * attributes. This method will then parse the remaining sword
     * extensions that exist in the element. 
     * 
     * @param entry The entry to parse. 
     * 
     * @throws UnmarshallException If the entry is not an atom:entry 
     *              or if there is an exception extracting the data. 
     */
    public void unmarshall(Element entry) throws UnmarshallException
    {
       super.unmarshall(entry);
       errorURI = entry.getAttributeValue("href");
    }  
    
    /**
     * Main method to perform a brief test of the class
     * 
     * @param args
     */
    public static void main(String[] args)
    {
 	   SWORDErrorDocument sed = new SWORDErrorDocument(ErrorCodes.MEDIATION_NOT_ALLOWED);
 	   sed.setNoOp(true);
 	   sed.setTreatment("Short back and shine");
 	   sed.setId("123456789");
 	   Title t = new Title();
 	   t.setContent("My first book");
 	   sed.setTitle(t);
 	   Author a = new Author();
 	   a.setName("Lewis, Stuart");
 	   a.setEmail("stuart@example.com");
 	   sed.addAuthors(a);
 	   
 	   System.out.println(sed.marshall().toXML());
    }
 }
