 package net.bodz.bas.potato.element;
 
 import java.util.Map;
 
 import net.bodz.bas.i18n.dom.XiString;
 import net.bodz.bas.i18n.dom.iString;
 import net.bodz.bas.i18n.dom1.IElement;
 import net.bodz.bas.i18n.dom1.IMutableElement;
 
 public class MutablePotatoElement
         extends MutableAnnotations
         implements IPotatoElement, IMutableElement {
 
    private static final long serialVersionUID = 1L;

     private Class<?> declaringClass;
     private Map<Class<?>, Object> typerMap;
 
     private String name;
     private iString label = new XiString();
     private iString description = new XiString();
     private iString helpDoc = new XiString();
     private int verboseLevel = PUBLIC_LEVEL;
     private int modifiers;
 
     /** ⇱ Implementation Of {@link IPotatoElement}. */
     /* _____________________________ */static section.iface __POTATO__;
 
     @Override
     public Class<?> getDeclaringClass() {
         return declaringClass;
     }
 
     public void setDeclaringClass(Class<?> declaringClass) {
         this.declaringClass = declaringClass;
     }
 
     @Override
     public <T> T getTyper(Class<T> typerClass) {
         T typer = (T) typerMap.get(typerClass);
         return typer;
     }
 
     public <T> boolean addTyper(Class<T> typerClass, T typer) {
         if (typerClass == null)
             throw new NullPointerException("typerClass");
         if (typer == null)
             throw new NullPointerException("typer");
 
         if (typerMap.containsKey(typerClass))
             return false;
 
         typerMap.put(typerClass, typer);
         return true;
     }
 
     public boolean removeTyper(Class<?> typerClass) {
         Object removed = typerMap.remove(typerClass);
         return removed != null;
     }
 
     /** ⇱ Implementation Of {@link IElement}. */
     /* _____________________________ */static section.iface __ELEMENT__;
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public iString getLabel() {
         return label;
     }
 
     @Override
     public iString getDescription() {
         return description;
     }
 
     @Override
     public int getVerboseLevel() {
         return verboseLevel;
     }
 
     @Override
     public int getModifiers() {
         return modifiers;
     }
 
     /** ⇱ Implementation Of {@link IMutableElement}. */
     /* _____________________________ */static section.iface __MUTABLE__;
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public void setLabel(iString label) {
         this.label = label;
     }
 
     @Override
     public void setDescription(iString description) {
         this.description = description;
     }
 
     @Override
     public void setHelpDoc(iString helpDoc) {
         this.helpDoc = helpDoc;
     }
 
     @Override
     public iString getHelpDoc() {
         return helpDoc;
     }
 
     @Override
     public void setVerboseLevel(int userLevel) {
         this.verboseLevel = userLevel;
     }
 
     @Override
     public void setModifiers(int modifiers) {
         this.modifiers = modifiers;
     }
 
 }
