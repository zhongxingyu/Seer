 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.language.factory;
 
 import java.io.Serializable;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.modelcc.language.syntax.RuleElement;
 import org.modelcc.language.syntax.RuleElementPosition;
 import org.modelcc.language.syntax.SymbolBuilder;
 import org.modelcc.metamodel.BasicModelElement;
 import org.modelcc.metamodel.ElementMember;
 import org.modelcc.metamodel.ModelElement;
 import org.modelcc.metamodel.MultipleElementMember;
 import org.modelcc.metamodel.ComplexModelElement;
 import org.modelcc.metamodel.Model;
 import org.modelcc.parser.fence.Symbol;
 
 /**
  * Symbol content builder
  * @author elezeta
  * @serial
  */
 public final class CompositeSymbolBuilder extends SymbolBuilder implements Serializable {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
     /**
      * The model.
      */
     private Model m;
        
     /**
      * Constructor
      * @param m the model.
      */
     public CompositeSymbolBuilder(Model m) {
         this.m = m;
     }
     
     /**
      * Builds a symbol, filling its listData, and validates it.
      * @param t symbol to be built.
      * @param data the parser listData.
      * @return true if the symbol is valid, false if not
      */
     @Override
     public boolean build(Symbol t,Object data) {
         Map<Class,Map<KeyWrapper,Object>> ids = ((ModelCCParserData)data).getIds();
         Map<Object,ObjectWrapper> map = ((ModelCCParserData)data).getMap();
         ElementId eid = (ElementId)t.getType();
         ComplexModelElement ce = (ComplexModelElement) eid.getElement();
         Class c = ce.getElementClass();
         Object o;
         boolean valid = true;
 
         try {
             o = c.newInstance();
             Set<RuleElement> proc = new HashSet<RuleElement>();
             Set<Field> filled = new HashSet<Field>();
             for (int i = 0;i < t.getElements().size();i++) {
                 Symbol s = t.getContents().get(i);
                 RuleElement re = t.getElements().get(i);
                 proc.add(re);
                 if (re.getClass().equals(RuleElementPosition.class)) {
                     ElementMember ct = (ElementMember)((RuleElementPosition)re).getPositionId();
                     Field fld = c.getDeclaredField(ct.getField());
                     filled.add(fld);
                     Object list;
                     Method addm;
                     int j;
                     Object[] listData;
                     if (fld != null) {
                         fld.setAccessible(true);
                         if (!ct.getClass().equals(MultipleElementMember.class)) {
                             fld.set(o,s.getUserData());
                         }
                         else {
                             MultipleElementMember mc = (MultipleElementMember)ct;
                             listData = (Object[]) s.getUserData();
                             if (mc.getMinimumMultiplicity() != -1) {
                                 if (listData.length<mc.getMinimumMultiplicity())
                                     valid = false;
                             }
                             if (mc.getMaximumMultiplicity() != -1) {
                                 if (listData.length>mc.getMaximumMultiplicity())
                                     valid = false;
                             }
                             switch (mc.getCollection()) {
                                 case LIST:
                                     if (fld.getType().isInterface())
                                             list = ArrayList.class.newInstance();
                                         else
                                             list = fld.getType().newInstance();
                                         addm = list.getClass().getDeclaredMethod("add",Object.class);
                                         if (listData != null)
                                             for (j = 0;j < listData.length;j++)
                                                 addm.invoke(list,listData[j]);
                                         fld.set(o,list);
                                         break;
                                     case LANGARRAY:
                                         list = Array.newInstance(ct.getElementClass(),listData.length);
                                         if (listData != null)
                                             for (j = 0;j < listData.length;j++)
                                                 Array.set(list, j, listData[j]);
                                         fld.set(o,list);
                                         break;
                                     case SET:
                                         if (fld.getType().isInterface())
                                             list = HashSet.class.newInstance();
                                         else
                                             list = fld.getType().newInstance();
                                         addm = list.getClass().getDeclaredMethod("add",Object.class);
                                         if (listData != null)
                                             for (j = 0;j < listData.length;j++)
                                                 addm.invoke(list,listData[j]);
                                         fld.set(o,list);
                                         break;
                                 }
                         }
                     }
                 }
             }
             fixOptionals(o,m,filled);
             if (ce.getAutorunMethod() != null) {
                 Method mtd = c.getDeclaredMethod(ce.getAutorunMethod(),new Class[]{});
                 if (mtd != null) {
                     mtd.setAccessible(true);
                     if (valid) {
                         if (mtd.getReturnType().equals(boolean.class) || mtd.getReturnType().equals(Boolean.class)) {
                             valid = (Boolean)mtd.invoke(o);
                         }
                         else {
                             mtd.invoke(o);
                             valid = true;
                         }
                     }
                 }
             }
             t.setUserData(o);
 
         } catch (Exception ex) {
             Logger.getLogger(CompositeSymbolBuilder.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         }
 
         if (valid && !ce.getIds().isEmpty()) {
             Map<KeyWrapper,Object> idmap = ids.get(c);
             if (idmap == null) {
                 idmap = new HashMap<KeyWrapper,Object>();
                 ids.put(c,idmap);
             }
             KeyWrapper kw = KeyWrapper.getKeyWrapper(o, m,map);
             if (idmap.containsKey(kw)) {
                 Logger.getLogger(CompositeSymbolBuilder.class.getName()).log(Level.SEVERE, "Class \"{0}\": Duplicate ID.", new Object[]{c.getCanonicalName()});
             }
             else {
                 idmap.put(kw,o);
             }
         }
         return valid;
     }
 
     private boolean fixOptionals(Object o, Model m,Set<Field> filled) throws InstantiationException, IllegalAccessException, NoSuchFieldException,SecurityException {
     	if (m.getClassToElement().get(o.getClass()).getClass().equals(BasicModelElement.class)) {
     		Class c = o.getClass();
             BasicModelElement be = (BasicModelElement) m.getClassToElement().get(c);
             try {
                 if (be.getValueField() != null) {
                     Field fld = c.getDeclaredField(be.getValueField());
                     if (fld != null) {
                         fld.setAccessible(true);
                         if (fld.getType().equals(String.class))
                         	fld.set(o, ObjectCaster.castObject(fld.getType(), ""));
                     }
                 }
             } catch (Exception ex) {
                 Logger.getLogger(BasicTokenBuilder.class.getName()).log(Level.SEVERE, null, ex);
                 return false;
             }
 
         }
         else {
 	        ComplexModelElement ce = (ComplexModelElement)m.getClassToElement().get(o.getClass());
 	        Field[] fields = o.getClass().getDeclaredFields();
 	        for (int i = 0;i < fields.length;i++) {
 	            ElementMember ct = ce.getFieldToContent().get(fields[i].getName());
 	            if (ct != null) {
 	                Field fld = o.getClass().getDeclaredField(ct.getField());
 	                if (filled == null || !filled.contains(fields[i])) {
 	                    if (ct != null) {
 	                        if (!ct.isOptional()) {
 	                            if (!ct.getClass().equals(MultipleElementMember.class)) {
 	                                Class c = fields[i].getType();
 	                                Object o2;
                                	System.out.println("TODO CompositeSymbolBuilder fix this CLASS IS "+c);
 	                                while (Modifier.isAbstract( c.getModifiers())) {
	                                	System.out.println("IS ABSTRACT");
	                                	System.out.println("DEF "+m.getDefaultElement().get(m.getClassToElement().get(c)));
 	                                	c = m.getDefaultElement().get(m.getClassToElement().get(c)).getElementClass();
	                                	System.out.println("NEW CLASS IS "+c);
 	                                }
                                 	o2 = c.newInstance();
 	                                fixOptionals(o2,m,null);
 	                                fields[i].setAccessible(true);
 	                                fields[i].set(o,o2);
 	                            }
 	                            else {
 	                                MultipleElementMember mc = (MultipleElementMember)ct;
 	                                Object list;
 	                                if (mc.getMinimumMultiplicity() == 0) {
 	                                    switch (mc.getCollection()) {
 	                                        case LIST:
 	                                            if (fld.getType().isInterface())
 	                                                    list = ArrayList.class.newInstance();
 	                                                else
 	                                                    list = fld.getType().newInstance();
 	                                                fields[i].setAccessible(true);
 	                                                fields[i].set(o,list);
 	                                                break;
 	                                            case LANGARRAY:
 	                                                list = Array.newInstance(ct.getElementClass(),0);
 	                                                fields[i].setAccessible(true);
 	                                                fields[i].set(o,list);
 	                                                break;
 	                                            case SET:
 	                                                if (fld.getType().isInterface())
 	                                                    list = HashSet.class.newInstance();
 	                                                else
 	                                                    list = fld.getType().newInstance();
 	                                                fields[i].setAccessible(true);
 	                                                fields[i].set(o,list);
 	                                                break;
 	                                    }
 	                                }
 	                            }
 	                        }
 	                    }
 	                }
 	            }
 	        }
         }
     	return true;
     }
 }
