 package com.sun.tools.xjc.reader.xmlschema;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.activation.MimeType;
 import javax.xml.namespace.QName;
 
 import com.sun.tools.xjc.model.CAdapter;
 import com.sun.tools.xjc.model.CClassInfo;
 import com.sun.tools.xjc.model.CElement;
 import com.sun.tools.xjc.model.CElementInfo;
 import com.sun.tools.xjc.model.CElementPropertyInfo;
 import com.sun.tools.xjc.model.CReferencePropertyInfo;
 import com.sun.tools.xjc.model.CTypeRef;
 import com.sun.tools.xjc.model.Multiplicity;
 import com.sun.tools.xjc.reader.RawTypeSet;
 import com.sun.tools.xjc.reader.Ring;
 import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDom;
 import com.sun.xml.bind.v2.model.core.ID;
 import com.sun.xml.bind.v2.model.core.WildcardMode;
 import com.sun.xml.xsom.XSElementDecl;
 import com.sun.xml.xsom.XSModelGroup;
 import com.sun.xml.xsom.XSModelGroupDecl;
 import com.sun.xml.xsom.XSParticle;
 import com.sun.xml.xsom.XSWildcard;
 import com.sun.xml.xsom.visitor.XSTermVisitor;
 
 /**
  * Builds {@link RawTypeSet} for XML Schema.
  *
  * @author Kohsuke Kawaguchi
  */
 public class RawTypeSetBuilder implements XSTermVisitor {
     /**
      * @param optional
      *      if this whole property is optional due to the
      *      occurence constraints on ancestors, set this to true.
      *      this will prevent the primitive types to be generated.
      */
     public static RawTypeSet build( XSParticle p, boolean optional ) {
         RawTypeSetBuilder rtsb = new RawTypeSetBuilder();
        rtsb.particle(p);
         Multiplicity mul = MultiplicityCounter.theInstance.particle(p);
 
         if(optional)
             mul = mul.makeOptional();
 
         return new RawTypeSet(rtsb.refs,mul);
     }
 
 
     /**
      * To avoid declaring the same element twice for a content model like
      * (A,A), we keep track of element names here while we are building up
      * this instance.
      */
     private final Set<QName> elementNames = new HashSet<QName>();
 
     private final Set<RawTypeSet.Ref> refs = new HashSet<RawTypeSet.Ref>();
 
     protected final BGMBuilder builder = Ring.get(BGMBuilder.class);
 
     private RawTypeSetBuilder() {}
 
     /**
      * Build up {@link #refs} and compute the total multiplicity of this {@link RawTypeSet.Ref} set.
      */
     private void particle( XSParticle p ) {
         // if the DOM customization is present, bind it like a wildcard
         BIDom dom = builder.getLocalDomCustomization(p);
         if(dom!=null) {
             dom.markAsAcknowledged();
             refs.add(new WildcardRef(WildcardMode.SKIP));
         } else {
             p.getTerm().visit(this);
         }
     }
 
     public void wildcard(XSWildcard wc) {
         refs.add(new WildcardRef(wc));
     }
 
     public void modelGroupDecl(XSModelGroupDecl decl) {
         modelGroup(decl.getModelGroup());
     }
 
     public void modelGroup(XSModelGroup group) {
         for( XSParticle p : group.getChildren())
             particle(p);
     }
 
     public void elementDecl(XSElementDecl decl) {
 
         QName n = new QName(decl.getTargetNamespace(),decl.getName());
         if(elementNames.add(n)) {
             CElement elementBean = Ring.get(ClassSelector.class).bindToType(decl);
             if(elementBean==null)
                 refs.add(new RawTypeSet.XmlTypeRef(decl));
             else {
                 if(elementBean instanceof CClassInfo)
                     refs.add(new CClassInfoRef(decl,(CClassInfo)elementBean));
                 else
                     refs.add(new CElementInfoRef(decl,(CElementInfo)elementBean));
             }
         }
     }
 
     /**
      * Reference to a wildcard.
      */
     public static final class WildcardRef extends RawTypeSet.Ref {
         private final WildcardMode mode;
 
         WildcardRef(XSWildcard wildcard) {
             this.mode = getMode(wildcard);
         }
         WildcardRef(WildcardMode mode) {
             this.mode = mode;
         }
 
         private static WildcardMode getMode(XSWildcard wildcard) {
             switch(wildcard.getMode()) {
             case XSWildcard.LAX:
                 return WildcardMode.LAX;
             case XSWildcard.STRTICT:
                 return WildcardMode.STRICT;
             case XSWildcard.SKIP:
                 return WildcardMode.SKIP;
             default:
                 throw new IllegalStateException();
             }
         }
 
         protected CTypeRef toTypeRef(CElementPropertyInfo ep) {
             // we don't allow a mapping to typeRef if the wildcard is present
             throw new IllegalStateException();
         }
 
         protected void toElementRef(CReferencePropertyInfo prop) {
             prop.setWildcard(mode);
         }
 
         protected boolean canBeType(RawTypeSet parent) {
             return false;
         }
 
         protected boolean isListOfValues() {
             return false;
         }
 
         protected ID id() {
             return ID.NONE;
         }
     }
 
 
     /**
      * Reference to a class that maps from an element.
      */
     public static final class CClassInfoRef extends RawTypeSet.Ref {
         public final CClassInfo target;
         public final XSElementDecl decl;
 
         CClassInfoRef(XSElementDecl decl, CClassInfo target) {
             this.decl = decl;
             this.target = target;
         }
 
         protected CTypeRef toTypeRef(CElementPropertyInfo ep) {
             return new CTypeRef(target,target.getElementName(),decl.isNillable(),decl.getDefaultValue());
         }
 
         protected void toElementRef(CReferencePropertyInfo prop) {
             prop.getElements().add(target);
         }
 
         protected boolean canBeType(RawTypeSet parent) {
             // if element substitution can occur, no way it can be mapped to a list of types
             if(decl.getSubstitutables().size()>1)
                 return false;
 
             return true;
         }
 
         protected boolean isListOfValues() {
             return false;
         }
 
         protected ID id() {
             return ID.NONE;
         }
     }
 
     /**
      * Reference to a class that maps from an element.
      */
     public static final class CElementInfoRef extends RawTypeSet.Ref {
         public final CElementInfo target;
         public final XSElementDecl decl;
 
         CElementInfoRef(XSElementDecl decl, CElementInfo target) {
             this.decl = decl;
             this.target = target;
         }
 
         protected CTypeRef toTypeRef(CElementPropertyInfo ep) {
             assert !target.isCollection();
             CAdapter a = target.getProperty().getAdapter();
             if(a!=null && ep!=null) ep.setAdapter(a);
 
             return new CTypeRef(target.getContentType(),target.getElementName(),decl.isNillable(),decl.getDefaultValue());
         }
 
         protected void toElementRef(CReferencePropertyInfo prop) {
             prop.getElements().add(target);
         }
 
         protected boolean canBeType(RawTypeSet parent) {
             // if element substitution can occur, no way it can be mapped to a list of types
             if(decl.getSubstitutables().size()>1)
                 return false;
 
             // we have no place to put an adater if this thing maps to a type
             CElementPropertyInfo p = target.getProperty();
             // if we have an adapter or IDness, which requires special
             // annotation, and there's more than one element,
             // we have no place to put the special annotation, so we need JAXBElement.
             if(parent.refs.size()>1 || !parent.mul.isAtMostOnce()) {
                 if(p.getAdapter()!=null || p.id()!=ID.NONE)
                     return false;
             }
 
             return true;
         }
 
         protected boolean isListOfValues() {
             return target.getProperty().isValueList();
         }
 
         protected ID id() {
             return target.getProperty().id();
         }
 
         protected MimeType getExpectedMimeType() {
             return target.getProperty().getExpectedMimeType();
         }
     }
 }
