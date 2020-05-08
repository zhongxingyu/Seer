 /**
  * Copyright (c) 2007, Aberystwyth University
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
 package org.purl.sword.atom;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import nu.xom.Element;
 import nu.xom.Elements;
 
 import org.apache.log4j.Logger;
 import org.purl.sword.base.Namespaces;
 import org.purl.sword.base.SwordElementInterface;
 import org.purl.sword.base.UnmarshallException;
 import org.purl.sword.base.XmlElement;
 
 /**
  * Represents an ATOM entry. 
  * 
  * @author Neil Taylor
  *
  */
 public class Entry extends XmlElement implements SwordElementInterface
 {
 	/**
 	* Local name for the element. 
 	*/
    public static final String ELEMENT_NAME = "entry";
    
    /**
     * Local name for the atom id element. 
     */
    public static final String ELEMENT_ID = "id";
    
    /**
     * Local name for the atom published element. 
     */
    public static final String ELEMENT_PUBLISHED = "published";
    
    /**
     * Local name for the atom updated element. 
     */
    public static final String ELEMENT_UPDATED = "updated";
    
    /**
     * Local name for the atom category element. 
     */
    public static final String ELEMENT_CATEGORY = "category";
    
    /**
     * A list of authors associated with this entry. There can be 0 
     * or more of these elements.   
     */
    private List<Author> authors; 
 
    /**
     * The atom:category data. There can be 0 or more of these elements.
     */
    private List<String> categories; 
 
    /**
     * A single content element for the Entry. 
     */
    private Content content; 
 
    /**
     * A list of contributors associated with this entry. There can be 0 or
     * more of these elements. 
     */
    private List<Contributor> contributors; 
 
    /**
     * This is a simplified version. The ID can also have atomCommonAttributes, 
     * but these have not been modelled in this version. The content of 
     * ID is an unconstrained string, which is intended to represent a URI. 
     */
    private String id; 
 
    /**
     * A list of link elements. This can contain 0 or more entries. 
     */
    private List<Link> links; 
 
    /**
     * Simplified version of the atom:published element. This implementation 
     * does not record the general atomCommonAttributes. The date is 
     * taken from an xsd:dateTime value. 
     * 
     * This item is optional. 
     */
    private String published; 
 
    /**
     * A single, optional, content element for the Entry. 
     */
    private Rights rights; 
 
    /**
     * A single, optional, content element for the Entry. 
     */
    private Source source; 
 
    /**
     * A single, optional, summary element for the Entry. 
     */
    private Summary summary; 
 
    /**
     * A required title element for the entry. 
     */
    private Title title; 
 
    /**
     * The date on which the entry was last updated.  
     */
    private String updated; 
 
    /**
     * The log. 
     */
    private static Logger log = Logger.getLogger(Entry.class);
 
    /**
     * Create a new instance of the class and initialise it. 
     * Also, set the prefix to 'atom' and the local name to 'entry'. 
     */
    public Entry() 
    {
       this(Namespaces.PREFIX_ATOM, ELEMENT_NAME);
    }
    
    /**
     * Create a new instance of the class an initalise it, setting the
     * element namespace and name.
     * 
      * @param namespace The namespace of the element
     * @param element The element name
     */
    public Entry(String namespace, String element)
    {
 	   super(namespace, element);
 	   
 	   authors = new ArrayList<Author>();
 	   categories = new ArrayList<String>();
 	   contributors = new ArrayList<Contributor>();
 	   links = new ArrayList<Link>();
    }
 
    /**
     * Mashall the data stored in this object into Element objects. 
     * 
     * @return An element that holds the data associated with this object. 
     */
    public Element marshall()
    {
       Element entry = new Element(getQualifiedName(), Namespaces.NS_ATOM);
       entry.addNamespaceDeclaration(Namespaces.PREFIX_SWORD, Namespaces.NS_SWORD);
       entry.addNamespaceDeclaration(Namespaces.PREFIX_ATOM, Namespaces.NS_ATOM);
       this.marshallElements(entry);
       return entry;  
    }
    
    protected void marshallElements(Element entry)
    {
 	      if (id != null)
 	      {
 	         Element idElement = new Element(getQualifiedNameWithPrefix(Namespaces.PREFIX_ATOM, ELEMENT_ID), Namespaces.NS_ATOM);
 	         idElement.appendChild(id);
 	         entry.appendChild(idElement);
 	      }
 
 	      for (Author author : authors)
 	      {
 	         entry.appendChild(author.marshall());
 	      }
 
 	      if (content != null)
 	      {
 	         entry.appendChild(content.marshall());
 	      }
 
 	      for (Author contributor : contributors)
 	      {
 	         entry.appendChild(contributor.marshall());
 	      }
 
 	      for (Link link : links)
 	      {
 	         entry.appendChild(link.marshall());
 	      }
 
 	      if (published != null)
 	      {
 	         Element publishedElement = new Element(getQualifiedNameWithPrefix(Namespaces.PREFIX_ATOM, ELEMENT_PUBLISHED), Namespaces.NS_ATOM);
 	         publishedElement.appendChild(published);
 	         entry.appendChild(publishedElement);
 	      }
 
 	      if (rights != null)
 	      {
 	         entry.appendChild(rights.marshall());
 	      }
 
 	      if (summary != null)
 	      {
 	         entry.appendChild(summary.marshall());
 	      }
 
 	      if (title != null)
 	      {
 	         entry.appendChild(title.marshall());
 	      }
 
 	      if (source != null)
 	      {
 	         entry.appendChild(source.marshall());
 	      }
 
 	      if (updated != null)
 	      {
 	         Element updatedElement = new Element(getQualifiedNameWithPrefix(Namespaces.PREFIX_ATOM, ELEMENT_UPDATED), Namespaces.NS_ATOM);
 	         updatedElement.appendChild(updated);
 	         entry.appendChild(updatedElement);
 	      }
 
 	      Element categoryElement = null; 
 	      for (String category : categories)
 	      {
 	         categoryElement = new Element(getQualifiedNameWithPrefix(Namespaces.PREFIX_ATOM, ELEMENT_CATEGORY), Namespaces.NS_ATOM );
 	         categoryElement.appendChild(category);
 	         entry.appendChild(categoryElement);
 	      }
    }
 
    /**
     * Unmarshall the contents of the Entry element into the internal data objects
     * in this object. 
     * 
     * @param entry The Entry element to process. 
     *
     * @throws UnmarshallException If the element does not contain an ATOM entry
     *         element, or if there is a problem processing the element or any 
     *         subelements. 
     */
    public void unmarshall(Element entry)
    throws UnmarshallException
    {
	  if (!((isInstanceOf(entry, localName, Namespaces.NS_ATOM)) || 
		    (isInstanceOf(entry, "error", Namespaces.NS_SWORD))))
       {
          log.error("Unexpected element. Expected atom:entry. Got " + 
                ((entry != null ) ? entry.getQualifiedName() : "null"));
          throw new UnmarshallException( "Not a " + getQualifiedName() + " element" );
       }
 
       try
       {
          authors.clear();
          categories.clear();
          contributors.clear();
          links.clear(); 
 
          // retrieve all of the sub-elements
          Elements elements = entry.getChildElements();
          Element element = null; 
          int length = elements.size();
 
          for (int i = 0; i < length; i++)
          {
             element = elements.get(i);
 
             if (isInstanceOf(element, Author.ELEMENT_NAME, Namespaces.NS_ATOM))
             {
                Author author = new Author(); 
                author.unmarshall(element);
                authors.add(author);
             }
             else if (isInstanceOf(element, ELEMENT_CATEGORY, Namespaces.NS_ATOM))
             {
                try
                {
                   categories.add(unmarshallString(element));
                }
                catch( UnmarshallException ume ) 
                {
                   log.error("Error accessing the content for the categories element");
                }
             }
             else if (isInstanceOf(element, Content.ELEMENT_NAME, Namespaces.NS_ATOM))
             {
                content = new Content();
                content.unmarshall(element); 
             }
             else if (isInstanceOf(element, Contributor.ELEMENT_NAME, Namespaces.NS_ATOM))
             {
                Contributor contributor = new Contributor(); 
                contributor.unmarshall(element);
                contributors.add(contributor);
             }
             else if (isInstanceOf(element, ELEMENT_ID, Namespaces.NS_ATOM))
             {
                try
                {
                   id = unmarshallString(element);
                }
                catch( UnmarshallException ume )
                {
                   log.error("Error accessing the content for the id element.");
                }
             }
             else if (isInstanceOf(element, Link.ELEMENT_NAME, Namespaces.NS_ATOM))
             {
                Link link = new Link(); 
                link.unmarshall(element);
                links.add(link);
             }
             else if (isInstanceOf(element, ELEMENT_PUBLISHED, Namespaces.NS_ATOM))
             {
                try
                {
                   published = unmarshallString(element);
                }
                catch( UnmarshallException ume ) 
                {
                   log.error("Error accessing the published date");
                }
             }
             else if (isInstanceOf(element, Rights.ELEMENT_NAME, Namespaces.NS_ATOM))
             {
                rights = new Rights(); 
                rights.unmarshall(element);
             }
             else if (isInstanceOf(element, Summary.ELEMENT_NAME, Namespaces.NS_ATOM))
             {
                summary = new Summary(); 
                summary.unmarshall(element);
             }
             else if (isInstanceOf(element, Title.ELEMENT_NAME, Namespaces.NS_ATOM))
             {
                title = new Title(); 
                title.unmarshall(element);
             }
             else if (isInstanceOf(element, ELEMENT_UPDATED, Namespaces.NS_ATOM))
             {
                try
                {
                   updated = unmarshallString(element);
                }
                catch( UnmarshallException ume) 
                {
                   log.error("Unable to access the updated date.");
                }
             }
             else if (isInstanceOf(element, Source.ELEMENT_NAME, Namespaces.NS_ATOM))
             {
                source = new Source(); 
                source.unmarshall(element);
             }
 
          } // for 
       }
       catch (Exception ex)
       {
          log.error("Unable to parse an element in Entry: " + ex.getMessage());
          throw new UnmarshallException("Unable to parse an element in " + getQualifiedName(), ex);
       }
    }
 
    /**
     * Get an iterator for the authors in the Entry. 
     * 
     * @return An iterator. 
     */
    public Iterator<Author> getAuthors()
    {
       return authors.iterator();
    }
 
    /**
     * Add an author to the Entry. 
     * 
     * @param author The author to add. 
     */
    public void addAuthors(Author author)
    {
       this.authors.add(author);
    }
 
    /**
     * Clear the list of authors. 
     */
    public void clearAuthors()
    {
       this.authors.clear();
    }
 
    /**
     * Get an iterator for the categories in this Entry. 
     * 
     * @return An iterator. 
     */
    public Iterator<String> getCategories() {
       return categories.iterator();
    }
 
    /**
     * Add a category. 
     * 
     * @param category the category to add. 
     */
    public void addCategory(String category) {
       this.categories.add(category);
    }
 
    /**
     * Clear the list of categories. 
     */
    public void clearCategories()
    {
       this.categories.clear();
    }
 
    /**
     * Get the content element for this Entry. 
     * 
     * @return The content element. 
     */
    public Content getContent() 
    {
       return content;
    }
 
    /**
     * Set the content element for this Entry. 
     * @param content
     */
    public void setContent(Content content) 
    {
       this.content = content;
    } 
 
    /**
     * Get a list of contributors. 
     * 
     * @return An iterator. 
     */
    public Iterator<Contributor> getContributors() {
       return contributors.iterator();
    }
 
    /**
     * Add a contributor. 
     * 
     * @param contributor The contributor. 
     */
    public void addContributor(Contributor contributor) 
    {
       this.contributors.add(contributor);
    }
 
    /**
     * Clear the list of contributors. 
     */
    public void clearContributors()
    {
       this.contributors.clear();
    }
 
    /**
     * Get the ID for this Entry. 
     * 
     * @return The ID. 
     */
    public String getId() 
    {
       return id;
    }
 
    /**
     * Set the ID for this Entry. 
     * 
     * @param id The ID. 
     */
    public void setId(String id) 
    {
       this.id = id;
    }
 
    /**
     * Get the list of links for this Entry. 
     * 
     * @return An iterator. 
     */
    public Iterator<Link> getLinks() 
    {
       return links.iterator();
    }
 
    /**
     * Get the link for this Entry. 
     * 
     * @param link The link. 
     */
    public void addLink(Link link) 
    {
       this.links.add(link);
    }
 
    /**
     * Clear the list of links. 
     */
    public void clearLinks()
    {
       this.links.clear();
    }
 
    /**
     * Get the published date, expressed as a String. 
     * 
     * @return The date. 
     */
    public String getPublished() 
    {
       return published;
    }
    
    /**
     * Set the published date. The date should be in one of the 
     * supported formats. This method will not check that the string 
     * is in the correct format. 
     * 
     * @param published The string. 
     * @see org.purl.sword.base.XmlElement#stringToDate(String) stringToDate
     * @see Entry#setPublished(Date) setPublished
     */
    public void setPublished(String published) 
    {
       this.published = published;
    }
 
    /**
     * Get the rights for this Entry. 
     * @return The rights. 
     */
    public Rights getRights() {
       return rights;
    }
 
    /**
     * Set the rights for this Entry. 
     * 
     * @param rights The rights. 
     */
    public void setRights(Rights rights) {
       this.rights = rights;
    }
 
    /**
     * Get the source for this Entry. 
     * @return The source. 
     */
    public Source getSource() {
       return source;
    }
 
    /**
     * Set the source for this entry. 
     * 
     * @param source The source. 
     */
    public void setSource(Source source) 
    {
       this.source = source;
    }
 
    /** 
     * Get the summary. 
     * 
     * @return The summary. 
     */
    public Summary getSummary() 
    {
       return summary;
    }
 
    /** 
     * Set the summary. 
     * 
     * @param summary The summary. 
     */
    public void setSummary(Summary summary) 
    {
       this.summary = summary;
    }
 
    /**
     * Get the title. 
     * 
     * @return The title. 
     */
    public Title getTitle() 
    {
       return title;
    }
 
    /**
     * Set the title. 
     * 
     * @param title The title. 
     */
    public void setTitle(Title title) 
    {
       this.title = title;
    }
 
    /**
     * Get the updated date, expressed as a String. See
     * org.purl.sword.XmlElement.stringToDate for the 
     * list of supported formats. This particular method 
     * will not check if the date is formatted correctly. 
     * 
     * @return The date. 
     */
    public String getUpdated() 
    {
       return updated;
    }
 
    /**
     * Set the updated date. The date should match one of the 
     * supported formats. This method will not check the format of the 
     * string. 
     * 
     * @param updated The string. 
     * @see org.purl.sword.base.XmlElement#stringToDate(String) stringToDate
     * @see Entry#setPublished(Date) setPublished
     */ 
    public void setUpdated(String updated) 
    {
       this.updated = updated;
    }     
 }
