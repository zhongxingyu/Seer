 package org.jboss.tools.jst.web.kb.internal.taglib;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.jboss.tools.common.model.project.ext.event.Change;
 import org.jboss.tools.common.xml.XMLUtilities;
 import org.jboss.tools.jst.web.kb.internal.KbObject;
 import org.jboss.tools.jst.web.kb.internal.KbXMLStoreConstants;
 import org.jboss.tools.jst.web.kb.taglib.IELFunction;
 import org.jboss.tools.jst.web.kb.taglib.IFunctionLibrary;
 import org.w3c.dom.Element;
 
 public abstract class FunctionTagLib extends AbstractTagLib implements IFunctionLibrary {
 	protected List<ELFunction> functions = new ArrayList<ELFunction>();
 	protected IELFunction[] functionArray = null;
 
 	public FunctionTagLib() {}
 
 	public IELFunction[] getFunctions() {
 		if(functionArray == null) {
 			functionArray = functions.toArray(new ELFunction[0]);
 		}
 		return functionArray;
 	}
 
 	public FunctionTagLib clone() throws CloneNotSupportedException {
 		FunctionTagLib copy = (FunctionTagLib)super.clone();
 		copy.functions = new ArrayList<ELFunction>();
 		copy.functionArray = null;
 		for (IELFunction f: getFunctions()) {
 			copy.addFunction(((ELFunction)f).clone());
 		}
 		return copy;
 	}
 
 	public void addFunction(ELFunction f) {
 		functions.add(f);
 		functionArray = null;
 	}
 
 	public List<Change> merge(KbObject s) {
 		List<Change> changes = super.merge(s);
 		FunctionTagLib t = (FunctionTagLib)s;
 		Change children = new Change(this, null, null, null);
 		mergeFunctions(t, children);
 		changes = Change.addChange(changes, children);
 		return changes;
 	}
 
 	public void mergeFunctions(FunctionTagLib c, Change children) {
 		Map<Object,ELFunction> functionMap = new HashMap<Object, ELFunction>();
 		for (IELFunction f: getFunctions()) functionMap.put(((KbObject)f).getId(), (ELFunction)f);
 		for (IELFunction f: c.getFunctions()) {
 			ELFunction loaded = (ELFunction)f;
 			ELFunction current = functionMap.get(loaded.getId());
 			if(current == null) {
 				addFunction(loaded);
 				Change change = new Change(this, null, null, loaded);
 				children.addChildren(Change.addChange(null, change));
 			} else {
 				List<Change> rc = current.merge(loaded);
 				if(rc != null) children.addChildren(rc);
 			}
 		}
 		for (ELFunction f: functionMap.values()) {
 			ELFunction removed = f;
 			synchronized (functions) {
 				if(functions.contains(removed)) {
 					continue;
 				}
 				functions.remove(removed.getName());
 				functionArray = null;
 			}
 			Change change = new Change(this, null, removed, null);
 			children.addChildren(Change.addChange(null, change));
 		}
 	}
 
 	public Element toXML(Element parent, Properties context) {
 		Element element = super.toXML(parent, context);
 
 		for (IELFunction f: getFunctions()) {
 			((KbObject)f).toXML(element, context);
 		}
 
 		return element;
 	}
 
 	public void loadXML(Element element, Properties context) {
 		super.loadXML(element, context);
 		
 		Element[] cs = XMLUtilities.getChildren(element, KbXMLStoreConstants.TAG_FUNCTION);
 		for (Element e: cs) {
 			ELFunction f = new ELFunction();
 			f.loadXML(e, context);
 			addFunction(f);
 		}
 	}
 
 }
