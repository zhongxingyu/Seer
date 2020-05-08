 package chameleon.core.method;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.rejuse.association.OrderedReferenceSet;
 import org.rejuse.predicate.PrimitiveTotalPredicate;
 
 import chameleon.core.MetamodelException;
 import chameleon.core.declaration.Declaration;
 import chameleon.core.element.Element;
 import chameleon.core.element.ElementImpl;
 import chameleon.core.lookup.LexicalLookupStrategy;
 import chameleon.core.lookup.LocalLookupStrategy;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.type.Type;
 import chameleon.core.variable.FormalParameter;
 import chameleon.core.variable.VariableContainer;
 
 public abstract class MethodHeader<E extends MethodHeader, P extends Method, S extends MethodSignature> extends ElementImpl <E,P> implements VariableContainer<E, P> { //extends Signature<E, P> 
   
   public E clone() {
     E result = cloneThis();
     for(FormalParameter param:getParameters()) {
       result.addParameter(param.clone());
     }
     return result;
   }
 
   public abstract S signature();
   
   public abstract E cloneThis();
   
   public List<Element> children() {
   	List<Element> result = new ArrayList<Element>();
   	result.add(signature());
   	result.addAll(getParameters());
   	return result;
   }
   
   /*********************
    * FORMAL PARAMETERS *
    *********************/
 
   public List<FormalParameter> getParameters() {
     return _parameters.getOtherEnds();
   }
 
 
   public void addParameter(FormalParameter arg) {
     _parameters.add(arg.parentLink());
   }
 
   public int getNbParameters() {
     return _parameters.size();
   }
 
   private OrderedReferenceSet<MethodHeader,FormalParameter> _parameters = new OrderedReferenceSet<MethodHeader,FormalParameter>(this);
   
   /**
    * Return the type of the formal parameters of this signature.
    * 
    * @return
    * @throws MetamodelException
    */
   public List<Type> getParameterTypes() throws LookupException {
     List<Type> result = new ArrayList<Type>();
     for(FormalParameter param:getParameters()) {
       result.add(param.getType());
     }
     return result;
   }
 
   /**
    * Check whether or not this method contains a formal parameter with the given name.
    *
    * @param name
    *        The name that has to be checked.
    */
   /*@
    @ public behavior
    @
    @ post \result == (\exists FormalParameter fp; getFormalParameters.contains(fp);
    @                   fp.getName().equals(name);
    @*/
   public boolean containsParameterWithName(final String name) {
     return new PrimitiveTotalPredicate() {
       public boolean eval(Object o) {
         return ((FormalParameter)o).getName().equals(name);
       }
     }.exists(getParameters());
   }
 
   public Type getNearestType() {
   	return parent().getNearestType();
   }
   
   public Set<Declaration> declarations() {
     Set<Declaration> result = new HashSet<Declaration>();
     result.addAll(getParameters());
     return result;
   }
   
   public boolean sameParameterTypesAs(MethodHeader other) throws MetamodelException {
   	boolean result = false;
   	if (other != null) {
 			List<FormalParameter> mine = getParameters();
 			List<FormalParameter> others = other.getParameters();
 			result = mine.size() == others.size();
 			Iterator<FormalParameter> iter1 = mine.iterator();
 			Iterator<FormalParameter> iter2 = others.iterator();
 			while (result && iter1.hasNext()) {
         result = result && iter1.next().getType().equals(iter2.next().getType());
 			}
 		}
   	return result;
   }
 
   public Method variableScopeElement() {
   	return parent();
   }
   
   public LookupStrategy lexicalContext(Element element) {
   	return new LexicalLookupStrategy(new LocalLookupStrategy<MethodHeader>(this),this);
   }
 }
