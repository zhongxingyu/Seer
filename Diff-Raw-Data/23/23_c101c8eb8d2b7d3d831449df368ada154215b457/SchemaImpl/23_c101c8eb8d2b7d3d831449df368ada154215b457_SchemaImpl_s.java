 package org.xbrlapi.impl;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 import java.util.Vector;
 
 import org.xbrlapi.Concept;
 import org.xbrlapi.ExtendedLink;
 import org.xbrlapi.Linkbase;
 import org.xbrlapi.ReferencePartDeclaration;
 import org.xbrlapi.Schema;
 import org.xbrlapi.SimpleLink;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 public class SchemaImpl extends SchemaContentImpl implements Schema {
 		
     /**
      * @see org.xbrlapi.Schema#getTargetNamespace()
      */
     public URI getTargetNamespace() throws XBRLException {
     	if (getDataRootElement().hasAttribute("targetNamespace")) {
     		try {
     		    return new URI(getDataRootElement().getAttribute("targetNamespace"));
     		} catch (URISyntaxException e) {
     		    throw new XBRLException("The target namespace is not a valid URI.",e);
     		}
     	}
     	return null;
     }
     
     /**
      * @see org.xbrlapi.Schema#isElementFormQualified()
      */
     public boolean isElementFormQualified() throws XBRLException {
     	if (getDataRootElement().getAttribute("elementFormDefault").equals("qualified")) {
     		return true;
     	}
     	return false;
     }
 
     /**
      * @see org.xbrlapi.Schema#getImports()
      */
     public List<SimpleLink> getImports() throws XBRLException {
    	String query = "/"+ Constants.XBRLAPIPrefix+ ":" + "fragment[@parentIndex='" + this.getIndex() + "' and @type='org.xbrlapi.impl.SimpleLinkImpl']";
    	List<SimpleLink> links = getStore().<SimpleLink>queryForXMLResources(query);
    	List<SimpleLink> imports = new Vector<SimpleLink>();
    	for (SimpleLink link: links) {
        	if (link.getLocalname().equals("import") && link.getNamespace().equals(Constants.XMLSchemaNamespace)) {
        		imports.add(link);
        	}
    	}
    	return imports;
     }
     
     /**
      * @see org.xbrlapi.Schema#getIncludes()
      */
     public List<SimpleLink> getIncludes() throws XBRLException {
    	String query = "#roots#[@parentIndex='" + this.getIndex() + "' and @type='org.xbrlapi.impl.SimpleLinkImpl']";
    	List<SimpleLink> links = getStore().<SimpleLink>queryForXMLResources(query);
    	List<SimpleLink> includes = new Vector<SimpleLink>();
    	for (SimpleLink link: links) {
        	if (link.getLocalname().equals("include") && link.getNamespace().equals(Constants.XMLSchemaNamespace)) {
        		includes.add(link);
        	}
    	}
    	return includes;
     }
     
     /**
      * @see org.xbrlapi.Schema#getExtendedLinks()
      */
     public List<ExtendedLink> getExtendedLinks() throws XBRLException {
     	List<Linkbase> linkbases = getStore().<Linkbase>getChildFragments("Linkbase",getIndex());
     	logger.debug("The schema contains " + linkbases.size() + " linkbases.");
     	List<ExtendedLink> links = new Vector<ExtendedLink>();
     	for (Linkbase linkbase: linkbases) {
         	links.addAll(getStore().<ExtendedLink>getChildFragments("ExtendedLink",linkbase.getIndex()));
     	}
     	return links;
     }
     
     /**
      * @see org.xbrlapi.Schema#getConcepts()
      */
     public List<Concept> getConcepts() throws XBRLException {
     	return this.<Concept>getChildren("org.xbrlapi.impl.ConceptImpl");
     }
     
     /**
      * @see org.xbrlapi.Schema#getOtherElementDeclarations()
      */
     public List<Concept> getOtherElementDeclarations() throws XBRLException {
         return this.<Concept>getChildren("org.xbrlapi.impl.ElementDeclarationImpl");
     }
     
 
     /**
      * @see org.xbrlapi.Schema#getConceptByName(String)
      */
     public Concept getConceptByName(String name) throws XBRLException {
     	String query = "/"+ Constants.XBRLAPIPrefix+ ":" + "fragment[@type='org.xbrlapi.impl.ConceptImpl' and @parentIndex='" + getIndex() + "' and "+ Constants.XBRLAPIPrefix+ ":" + "data/*/@name='" + name + "']";
     	List<Concept> concepts = getStore().<Concept>queryForXMLResources(query);
     	if (concepts.size() == 0) return null;
     	if (concepts.size() > 1) throw new XBRLException("The concept name is not unique to the schema.");
     	return concepts.get(0);
     }
 
     /**
      * @see org.xbrlapi.Schema#getConceptsByType(URI, String)
      */
     public List<Concept> getConceptsByType(URI namespace, String localName) throws XBRLException {
     	List<Concept> matches = new Vector<Concept>();
     	List<Concept> concepts = getConcepts();
     	for (Concept concept: concepts) {
 			if (concept.getTypeNamespace().equals(namespace) && 
 				concept.getTypeLocalname().equals(localName)) {
 				matches.add(concept);
 			}
 		}
 		return matches;
 	}
     
     /**
      * @see org.xbrlapi.Schema#getConceptsBySubstitutionGroup(URI, String)
      */
     public List<Concept> getConceptsBySubstitutionGroup(URI namespace, String localname) throws XBRLException {
     	Vector<Concept> matches = new Vector<Concept>();
     	List<Concept> concepts = getConcepts();
     	for (Concept concept: concepts) {
 			if (concept.getSubstitutionGroupNamespace().equals(namespace) && 
 					concept.getSubstitutionGroupLocalname().equals(localname)) {
 					matches.add(concept);
 			}
     	}
 		return matches;
     }
 
 
 
 	
 
     /**
      * @see org.xbrlapi.Schema#getReferencePartDeclaration(String)
      */
     public ReferencePartDeclaration getReferencePartDeclaration(String name) throws XBRLException {
     	for (int i=0; i<getReferencePartDeclarations().size(); i++) {
     		if (getReferencePartDeclarations().get(i).getLocalname().equals(name))
 				return getReferencePartDeclarations().get(i);
     	}
     	return null;
     }
     
     /**
      * @see org.xbrlapi.Schema#getReferencePartDeclarations()
      */
     public List<ReferencePartDeclaration> getReferencePartDeclarations() throws XBRLException {
     	return this.getChildren("org.xbrlapi.impl.ReferencePartDeclarationImpl");
     }
     
     
 
 
 
 
 
     
 
 
 
     
 
 
 
 
     
 
 
 }
