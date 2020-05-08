 package org.xbrlapi.impl;
 
 import java.util.List;
 
 import org.xbrlapi.Concept;
 import org.xbrlapi.Fact;
 import org.xbrlapi.Fragment;
 import org.xbrlapi.Instance;
 import org.xbrlapi.Tuple;
 import org.xbrlapi.TypeDeclaration;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 public class FactImpl extends FragmentImpl implements Fact {
 
     /**
      * 
      */
     private static final long serialVersionUID = 5557580425035320023L;
 
     /**
      * Return true if the fact is a tuple and false otherwise
      */
     public boolean isTuple() throws XBRLException {
         return (! this.getDataRootElement().hasAttribute("contextRef"));
     }
    
    
 	/**
 	 * @see org.xbrlapi.Fact#getInstance()
 	 */
 	public Instance getInstance() throws XBRLException {
 		return (Instance) this.getAncestorOrSelf("org.xbrlapi.impl.InstanceImpl");
 	}
 
 	/**
 	 * @see org.xbrlapi.Fact#getTuple()
 	 */
 	public Tuple getTuple() throws XBRLException {
 		Fragment parent = this.getParent();
 		if (parent.getType().equals("org.xbrlapi.impl.TupleImpl")) return (Tuple) parent;
 		return null;
 	}
 
 	/**
 	 * @see org.xbrlapi.Fact#getConcept()
 	 */
 	public Concept getConcept() throws XBRLException {
 		
 		List<Concept> candidates = getStore().<Concept>queryForXMLResources("#roots#[@type='org.xbrlapi.impl.ConceptImpl' and */*/@name='" + this.getLocalname() + "']");
 		for (Concept concept: candidates) {
 			if (this.getNamespace().equals(concept.getTargetNamespace())) 
 				return concept;
 		}
 		logger.error(getStore().serializeToString(this.getMetadataRootElement()));
 		throw new XBRLException("No concept could be found for the fact.");
 	}
 	
 	/**
 	 * @see org.xbrlapi.Fact#isNumeric()
 	 */
 	public boolean isNumeric() throws XBRLException {
 		
 		String unitRef = this.getDataRootElement().getAttribute("unitRef");
 		return (unitRef != "");
 	}
 	
     /**
      * @see Fact#isFraction()
      */
     public boolean isFraction() throws XBRLException {
        Concept concept = this.getConcept();
        TypeDeclaration type = concept.getTypeDeclaration();
        return (type.isDerivedFrom(Constants.XBRL21Namespace,"fractionItemType"));
     }
 
 	/** 
      * @see org.xbrlapi.Fact#isNil()
      */
     public boolean isNil() throws XBRLException {
         if (this.getDataRootElement().getAttributeNS(Constants.XMLSchemaInstanceNamespace.toString(),"nil").equals("true")) return true;
         return false;
     }
 }
