 package org.xbrlapi.impl;
 
 import java.net.URI;
 import java.util.List;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xbrlapi.AttributeGroupDeclaration;
 import org.xbrlapi.TypeDeclaration;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 public class TypeDeclarationImpl extends SchemaDeclarationImpl implements TypeDeclaration {	
 
     /**
      * 
      */
     private static final long serialVersionUID = 1297992906402945120L;
 
     /**
      * @see org.xbrlapi.TypeDeclaration#getFinal()
      */
     public String getFinal() throws XBRLException {
         return getDataRootElement().getAttributeNS(Constants.XMLSchemaNamespace.toString(),"final");
     }
 
     /**
      * @see org.xbrlapi.TypeDeclaration#isFinal()
      */
     public boolean isFinal() throws XBRLException {
         return (isFinalForExtension() && isFinalForRestriction());
     }
 
     /**
      * @see org.xbrlapi.TypeDeclaration#isFinalForExtension()
      */
     public boolean isFinalForExtension() throws XBRLException {
         String f = getFinal();
         if (f.contains("#all")) return true;
         if (f.contains("extension")) return true;
         return false;
     }
 
     /**
      * @see org.xbrlapi.TypeDeclaration#isFinalForRestriction()
      */
     public boolean isFinalForRestriction() throws XBRLException {
         String f = getFinal();
         if (f.contains("#all")) return true;
         if (f.contains("restriction")) return true;
         return false;
     }
 
     /**
      * @see org.xbrlapi.TypeDeclaration#isDerivedFrom(org.xbrlapi.TypeDeclaration)
      */
     public boolean isDerivedFrom(TypeDeclaration candidate) throws XBRLException {
         if (this.equals(candidate)) return true;
         URI parentNamespace = getParentTypeNamespace();
         if (parentNamespace.equals(Constants.XMLSchemaNamespace)) return false;
         TypeDeclaration parentType = this.getParentType();
         if (parentType == null) return false;
         if (parentType.equals(candidate)) return true;
         return parentType.isDerivedFrom(candidate);
     }
 
     /**
      * @see org.xbrlapi.TypeDeclaration#isDerivedFrom(java.net.URI, java.lang.String)
      */
     public boolean isDerivedFrom(URI namespace, String name) throws XBRLException {
         URI parentNamespace = getParentTypeNamespace();
         String parentName = this.getParentTypeLocalname();
         if (parentNamespace.equals(namespace) && parentName.equals(name)) return true;
         TypeDeclaration parentType = this.getParentType();
         if (parentType == null) return false;
         return parentType.isDerivedFrom(namespace,name);
     }
 
     /**
      * @return the type derivation element specifying the QName of the type that is being extended or restricted.
      * @throws XBRLException
      */
     private Element getDerivation() throws XBRLException {
         Element data = this.getDataRootElement();
         NodeList contents = data.getElementsByTagNameNS(Constants.XMLSchemaNamespace.toString(),"restriction");
         if (contents.getLength() != 0) return (Element) contents.item(0);
         contents = data.getElementsByTagNameNS(Constants.XMLSchemaNamespace.toString(),"extension");
         if (contents.getLength() != 0) return (Element) contents.item(0);
         contents = data.getElementsByTagNameNS(Constants.XMLSchemaNamespace.toString(),"complexContent");
         if (contents.getLength() == 0) 
             contents = data.getElementsByTagNameNS(Constants.XMLSchemaNamespace.toString(),"simpleContent");
         if (contents.getLength() == 0) {
             return null;
         }
         Element content = (Element) contents.item(0);
         NodeList derivations = content.getElementsByTagNameNS(Constants.XMLSchemaNamespace.toString(),"extension");
         if (derivations.getLength() == 0) derivations = data.getElementsByTagNameNS(Constants.XMLSchemaNamespace.toString(),"restriction");
         if (derivations.getLength() == 0) {
             return null;
         }
         
         return (Element) derivations.item(0);
         
     }
     
     /**
      * @see org.xbrlapi.TypeDeclaration#getParentType()
      */
     public TypeDeclaration getParentType() throws XBRLException {
         URI parentNamespace = this.getParentTypeNamespace();
         if (parentNamespace.equals(Constants.XMLSchemaNamespace)) return null;
         String parentName = this.getParentTypeLocalname();
         TypeDeclaration parent = getStore().getGlobalDeclaration(parentNamespace,parentName);
         if (parent != null) return parent;
         throw new XBRLException("There is no parent type ( "+parentNamespace + ":" + parentName +" ) in the data store.");
     }
 
     /**
      * @see org.xbrlapi.TypeDeclaration#getParentTypeLocalname()
      */
     public String getParentTypeLocalname() throws XBRLException {
         Element derivation = this.getDerivation();
         if (derivation.hasAttribute("base")) {
             String qname = derivation.getAttribute("base");
             return this.getLocalnameFromQName(qname);
         }
         throw new XBRLException("There is no parent type in the data store.");
     }
 
     /**
      * @see org.xbrlapi.TypeDeclaration#getParentTypeNamespace()
      */
     public URI getParentTypeNamespace() throws XBRLException {
         Element derivation = this.getDerivation();
        if (derivation == null) throw new XBRLException("This type is not derived from another.");
         if (derivation.hasAttribute("base")) {
             String qname = derivation.getAttribute("base");
             URI namespace = this.getNamespaceFromQName(qname,derivation);
             return namespace;
         }
         throw new XBRLException("This type is not derived from another.");
     }
 
     /**
      * @see TypeDeclaration#isNumericItemType()
      */
     public boolean isNumericItemType() throws XBRLException {
         
         TypeDeclaration parentType = this.getParentType();
         if (parentType != null) 
             if (parentType.isa(ComplexTypeDeclarationImpl.class))
                 return parentType.isNumericItemType();
         
         URI namespace = this.getTargetNamespace();
         if (namespace == null) return false;
         if (namespace.equals(Constants.XBRL21Namespace)) {
             List<AttributeGroupDeclaration> agds = this.<AttributeGroupDeclaration>getChildren(AttributeGroupDeclarationImpl.class);
             for (AttributeGroupDeclaration agd: agds) {
                 if (agd.hasReference()) {
                     if (agd.getReferenceLocalname().equals("numericItemAttrs")) return true;
                     if (agd.getReferenceLocalname().equals("essentialNumericItemAttrs")) return true;
                 }
             }
         }
         return false;        
     }
 }
