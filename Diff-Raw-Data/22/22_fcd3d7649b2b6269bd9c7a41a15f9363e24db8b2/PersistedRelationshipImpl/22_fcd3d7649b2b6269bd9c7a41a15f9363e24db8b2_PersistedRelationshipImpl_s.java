 package org.xbrlapi.impl;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.xbrlapi.Arc;
 import org.xbrlapi.ExtendedLink;
 import org.xbrlapi.Fragment;
 import org.xbrlapi.PersistedRelationship;
 import org.xbrlapi.Resource;
 import org.xbrlapi.builder.BuilderImpl;
 import org.xbrlapi.networks.Relationship;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 public class PersistedRelationshipImpl extends NonFragmentXMLImpl implements PersistedRelationship {
 
     /**
      * No argument constructor.
      * @throws XBRLException
      */
     public PersistedRelationshipImpl() throws XBRLException {
         super();
         setBuilder(new BuilderImpl());
     }    
     
     /**
      * @param relationship The relationship that is to be persisted.
      * within the scope of the containing data store.
      * @throws XBRLException
      */
     public PersistedRelationshipImpl(Relationship relationship) throws XBRLException {
         this(relationship.getArc(),relationship.getSource(),relationship.getTarget());
     }
     
     /**
      * @param arc The arc defining the relationship
      * @param source The source of the relationship
      * @param target The target of the relationship
      * @throws XBRLException
      */
     public PersistedRelationshipImpl(Arc arc, Fragment source, Fragment target) throws XBRLException {
         this();
         
         if (arc == null) throw new XBRLException("The arc must not be null");
         if (source == null) throw new XBRLException("The source must not be null");
         if (source == null) throw new XBRLException("The target must not be null");
         ExtendedLink link = arc.getExtendedLink();
 
         setIndex(arc.getIndex() + source.getIndex() + target.getIndex());
 
         setSourceIndex(source.getIndex());
         setSourceType(source.getType());
         setSourceName(source.getLocalname());
         setSourceNamespace(source.getNamespace());
         if (source.isa("org.xbrlapi.impl.ResourceImpl")) {
             setSourceLanguageCode(((Resource) source).getLanguage());
             setSourceRole(((Resource) source).getResourceRole());
         }
 
         setTargetIndex(target.getIndex());
         setTargetType(target.getType());
         setTargetName(target.getLocalname());
         setTargetNamespace(target.getNamespace());
         if (target.isa("org.xbrlapi.impl.ResourceImpl")) {
             setTargetLanguageCode(((Resource) target).getLanguage());
             setTargetRole(((Resource) target).getResourceRole());
         }
 
         setArcIndex(arc.getIndex());
         setArcName(arc.getLocalname());
         setArcNamespace(arc.getNamespace());
         setArcrole(arc.getArcrole());
         setArcOrder(arc.getOrder());
         setArcPriority(arc.getPriority());
         setArcUse(arc.getUse());
 
         setLinkName(link.getLocalname());
         setLinkNamespace(link.getNamespace());
         setLinkRole(link.getLinkRole());
 
         setLabelStatus();
         setReferenceStatus();
         setSignature(arc);
 
         arc.getStore().persist(this);
         
     }    
     
     
     /**
      * @param key The relationship to use in generating
      * the relationship signature that will be persisted.  THe 
      * signature is matched for relationship prohibition and overriding.
      * @throws XBRLException
      */
     private void setSignature(Arc arc) throws XBRLException {
         this.setMetaAttribute("signature",arc.getSemanticKey());
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getArc()
      */
     public Arc getArc() throws XBRLException {
         return (Arc) this.getStore().getXMLResource(getArcIndex());
     }
     
     
     /**
      * @see org.xbrlapi.PersistedRelationship#getArcIndex()
      */
     public String getArcIndex() {
         return getMetaAttribute("arcIndex");
     }
 
     /**
      * @param index the index of the arc defining this relationship.
      */
     private void setArcIndex(String index) throws XBRLException {
         this.setMetaAttribute("arcIndex",index);
     }
     
     /**
      * @see org.xbrlapi.PersistedRelationship#getArcName()
      */
     public String getArcName() {
         return getMetaAttribute("arcName");
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getArcNamespace()
      */
     public URI getArcNamespace() {
         try {
             return new URI(getMetaAttribute("arcNamespace"));
         } catch (URISyntaxException e) {
             return null;
         }
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getArcrole()
      */
     public URI getArcrole() {
         try {
             return new URI(getMetaAttribute("arcRole"));
         } catch (URISyntaxException e) {
             return null;
         }
     }
     
     /**
      * @see org.xbrlapi.PersistedRelationship#getArcOrder()
      */
     public Double getArcOrder() {
         return new Double(getMetaAttribute("arcOrder"));
     }
     
     /**
      * @see org.xbrlapi.PersistedRelationship#getArcPriority()
      */
     public Integer getArcPriority() {
         return new Integer(getMetaAttribute("arcPriority"));
     }
     
     /**
      * @see org.xbrlapi.PersistedRelationship#getArcUse()
      */
     public String getArcUse() {
         String use = getMetaAttribute("arcUse");
         if (use== null) return "optional";
         return "prohibited";
     }    
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getLinkName()
      */
     public String getLinkName() {
         return getMetaAttribute("linkName");
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getLinkNamespace()
      */
     public URI getLinkNamespace() {
         try {
             return new URI(getMetaAttribute("linkNamespace"));
         } catch (URISyntaxException e) {
             return null;
         }
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getLinkRole()
      */
     public URI getLinkRole() {
         try {
             return new URI(getMetaAttribute("linkRole"));
         } catch (URISyntaxException e) {
             return null;
         }
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getSource()
      */
     @SuppressWarnings("unchecked")
     public <F extends Fragment> F getSource() throws XBRLException {
         return (F) this.getStore().getXMLResource(getSourceIndex());
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getSourceIndex()
      */
     public String getSourceIndex() {
         return getMetaAttribute("sourceIndex");
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getSourceLanguageCode()
      */
     public String getSourceLanguageCode() {
         return getMetaAttribute("sourceLanguage");
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getSourceRole()
      */
     public URI getSourceRole() {
         try {
            return new URI(getMetaAttribute("sourceRole"));
         } catch (URISyntaxException e) {
             return null;
         }
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getSourceType()
      */
     public String getSourceType() {
         return getMetaAttribute("sourceType");
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getSourceName()
      */
     public String getSourceName() {
         return getMetaAttribute("sourceName");
     }    
     
     /**
      * @see org.xbrlapi.PersistedRelationship#getSourceNamespace()
      */
     public URI getSourceNamespace() {
         try {
             return new URI(getMetaAttribute("sourceNamespace"));
         } catch (URISyntaxException e) {
             return null;// Cannot be thrown.
         }
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getTarget()
      */
     @SuppressWarnings("unchecked")
     public <F extends Fragment> F getTarget() throws XBRLException {
         return (F) this.getStore().getXMLResource(getTargetIndex());
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getTargetIndex()
      */
     public String getTargetIndex() {
         return getMetaAttribute("targetIndex");
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getTargetLanguageCode()
      */
     public String getTargetLanguageCode() {
         return getMetaAttribute("targetLanguage");
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getTargetRole()
      */
     public URI getTargetRole() {
         try {
            return new URI(getMetaAttribute("targetRole"));
         } catch (URISyntaxException e) {
             return null;
         }
     }
     
     /**
      * @see org.xbrlapi.PersistedRelationship#getSignature()
      */
     public String getSignature() {
         return getMetaAttribute("signature");
     }    
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getTargetType()
      */
     public String getTargetType() {
         return getMetaAttribute("targetType");
     }
 
     /**
      * @see org.xbrlapi.PersistedRelationship#getTargetName()
      */
     public String getTargetName() {
         return getMetaAttribute("targetName");
     }    
     
     /**
      * @see org.xbrlapi.PersistedRelationship#getTargetNamespace()
      */
     public URI getTargetNamespace() {
         try {
             return new URI(getMetaAttribute("targetNamespace"));
         } catch (URISyntaxException e) {
             return null;// Cannot be thrown.
         }
     }
     
     /**
      * @param name The local name of the arc element.
      */
     private void setArcName(String name) throws XBRLException {
         this.setMetaAttribute("arcName",name);
     }
 
     /**
      * @param namespace The namespace of the arc element.
      */
     private void setArcNamespace(URI namespace) throws XBRLException {
         this.setMetaAttribute("arcNamespace",namespace.toString());
     }
 
     /**
      * @param role The XLink arcrole value
      * @throws XBRLException 
      */
     private void setArcrole(URI role) throws XBRLException {
         this.setMetaAttribute("arcRole",role.toString());
     }
     
     /**
      * @param order The arc order
      * @throws XBRLException if the order is null. 
      */
     private void setArcOrder(Double order) throws XBRLException {
         if (order == null) throw new XBRLException("The order must not be null."); 
         this.setMetaAttribute("arcOrder",order.toString());
     }
     
     /**
      * @param priority The arc priority
      * @throws XBRLException if the priority is null. 
      */
     private void setArcPriority(Integer priority) throws XBRLException {
         if (priority == null) throw new XBRLException("The priority must not be null."); 
         this.setMetaAttribute("arcPriority",priority.toString());
     }
     
     /**
      * @param use The arc use
      * @throws XBRLException if the use is null. 
      */
     private void setArcUse(String use) throws XBRLException {
         if (use == null) throw new XBRLException("The use must not be null."); 
         if (use.equals("prohibited")) 
             this.setMetaAttribute("arcUse","prohibited");
     }    
 
     /**
      * @param name The local name of the containing extended link element.
      * @throws XBRLException 
      */
     private void setLinkName(String name) throws XBRLException {
         this.setMetaAttribute("linkName",name);
     }
 
     /**
      * @param namespace The namespace of the containing extended link element.
      * @throws XBRLException 
      */
     private void setLinkNamespace(URI namespace) throws XBRLException {
         this.setMetaAttribute("linkNamespace",namespace.toString());
     }
 
     /**
      * @param role The containing extended link XLink role value
      * @throws XBRLException 
      */
     private void setLinkRole(URI role) throws XBRLException {
         this.setMetaAttribute("linkRole",role.toString());
     }
 
     /**
      * @param code the source fragment type.
      * @throws XBRLException 
      */
     private void setSourceType(String type) throws XBRLException {
         this.setMetaAttribute("sourceType",type);
     }
 
     /**
      * @param index The source fragment index.
      * @throws XBRLException 
      */
     private void setSourceIndex(String index) throws XBRLException {
         this.setMetaAttribute("sourceIndex",index);
     }
 
     /**
      * @param code the source XML language code.
      * @throws XBRLException 
      */
     private void setSourceLanguageCode(String code) throws XBRLException {
         this.setMetaAttribute("sourceLanguage",code);
     }
 
     /**
      * @param role the source XLink role.
      * @throws XBRLException 
      */
     private void setSourceRole(URI role) throws XBRLException {
        this.setMetaAttribute("sourceRole",role.toString());
     }
     
     /**
      * @param name The local name of the source element.
      * @throws XBRLException 
      */
     private void setSourceName(String name) throws XBRLException {
         this.setMetaAttribute("sourceName",name);
     }    
     
     /**
      * @param namespace The namespace of the source element.
      * @throws XBRLException 
      */
     private void setSourceNamespace(URI namespace) throws XBRLException {
         this.setMetaAttribute("sourceNamespace",namespace.toString());
     }    
 
     /**
      * @param code the target fragment type.
      * @throws XBRLException 
      */
     private void setTargetType(String type) throws XBRLException {
         this.setMetaAttribute("targetType",type);
     }
 
     /**
      * @param index The target fragment index.
      * @throws XBRLException 
      */
     private void setTargetIndex(String index) throws XBRLException {
         this.setMetaAttribute("targetIndex",index);
     }
 
     /**
      * @param code the target XML language code.
      * @throws XBRLException 
      */
     private void setTargetLanguageCode(String code) throws XBRLException {
         this.setMetaAttribute("targetLanguage",code);
     }
 
     /**
      * @param role the target XLink role.
      * @throws XBRLException 
      */
     private void setTargetRole(URI role) throws XBRLException {
        this.setMetaAttribute("targetRole",role.toString());
     }
 
     /**
      * @param name The local name of the target element.
      * @throws XBRLException 
      */
     private void setTargetName(String name) throws XBRLException {
         this.setMetaAttribute("targetName",name);
     }    
     
     /**
      * @param namespace The namespace of the target element.
      * @throws XBRLException 
      */
     public void setTargetNamespace(URI namespace) throws XBRLException {
         this.setMetaAttribute("targetNamespace",namespace.toString());
     }        
     
     /**
      * @see org.xbrlapi.PersistedRelationship#isToLabel()
      */
     public boolean isToLabel() {
         if (this.getMetaAttribute("label") != null) return true;
         return false;
     }
     
     /**
      * @see org.xbrlapi.PersistedRelationship#isToReference()
      */
     public boolean isToReference() {
         if (this.getMetaAttribute("reference") != null) return true;
         return false;
     }    
     
     
 
     /**
      * Stores in the XML resource a 'label' attribute of
      * the root element if the relationship is to an XBRL 2.1 
      * or a Generic label.
      * @throws XBRLException 
      */
     private void setLabelStatus() throws XBRLException {
         if (getTargetName().equals("label")) {
             URI namespace = getTargetNamespace();
             if (namespace.equals(Constants.XBRL21LinkNamespace()) || namespace.equals(Constants.GenericLabelNamespace())) {
                 URI arcrole = getArcrole();
                 if (arcrole.equals(Constants.LabelArcrole()) || arcrole.equals(Constants.GenericLabelArcrole())) {
                     this.setMetaAttribute("label","true");
                 }
             }
         }
     }
     
     /**
      * Updates the active status for this relationship and the equivalent
      * active relationship in the data store.
      * @param store The data store
      * @throws XBRLException 
      */
 /*    private void updateActiveStatuses(Store store) throws XBRLException {
 
         String query = "#roots#[@arcRole='"+ this.getArcrole() + "' and @linkRole='"+ this.getLinkRole() + "'  and @sourceIndex='"+ this.getSourceIndex() + "'  and @targetIndex='"+ this.getTargetIndex() + "'  and @signature='"+ this.getSignature() + "']";
         List<PersistedRelationship> equivalentRelationships = store.<PersistedRelationship>query(query);
         for (PersistedRelationship equivalentRelationship: equivalentRelationships) {
             if (equivalentRelationship.getArcPriority() > this.getArcPriority()) {
                 return;
             }
             if (equivalentRelationship.isActive()) {
                 equivalentRelationship.setActiveStatus(false);
                 store.persist(equivalentRelationship);
             }
         }
         if (getArcUse().equals("optional")) this.setActiveStatus(true);
         
     }
 */    
     /**
      * Stores in the XML resource a 'active' attribute of
      * the root element if the relationship not overridden or
      * prohibited by other relationships that have been persisted
      * in the data store.
      * @throws XBRLException 
      */
 /*    public void setActiveStatus(boolean active) throws XBRLException {
         if (active) {
             this.setMetaAttribute("active","");
         } else {
             this.removeMetaAttribute("active");
         }
     }*/
     /**
      * Stores in the XML resource a 'reference' attribute of
      * the root element if the relationship is to an XBRL 2.1 
      * or a Generic reference.
      * @throws XBRLException 
      */
     private void setReferenceStatus() throws XBRLException {
         if (getTargetName().equals("reference")) {
             URI namespace = getTargetNamespace();
             if (namespace.equals(Constants.XBRL21LinkNamespace()) || namespace.equals(Constants.GenericLabelNamespace())) {
                 URI arcrole = getArcrole();
                 if (arcrole.equals(Constants.ReferenceArcrole()) || arcrole.equals(Constants.GenericReferenceArcrole())) {
                     this.setMetaAttribute("reference","true");
                 }
             }
         }
     }    
     
     /**
      * @see PersistedRelationship#isFromRoot()
      */
     public boolean isFromRoot() throws XBRLException {
         String sourceIndex = this.getSourceIndex();
         String query = "#roots#[@active and @targetIndex='" + sourceIndex + "' and @arcRole='" + this.getArcrole() + "'  and @linkRole='"+this.getLinkRole()+"']";
         return (getStore().queryCount(query) == 0);
     }
     
     /**
      * @see PersistedRelationship#isActive()
      */
 /*    public boolean isActive() throws XBRLException {
         return (this.getMetaAttribute("active") != null);
     }    */
 
     
 }
