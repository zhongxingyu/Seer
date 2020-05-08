 package org.jackie.event.impl.proxygen;
 
 import org.jackie.asmtools.CodeBlock;
 import org.jackie.asmtools.Variable;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.Type;
 
 import java.lang.reflect.Method;
 import java.util.Iterator;
 
 /**
  * @author Patrik Beno
  */
 public class InvokeEvent extends CodeBlock {
 
 
 	protected Class eventClass;
 	protected Method eventMethod;
 
 	public InvokeEvent(CodeBlock parent, Class eventClass, Method eventMethod) {
 		super(parent);
 		this.eventClass = eventClass;
 		this.eventMethod = eventMethod;
 	}
 
 	protected void body() {
 		Variable iterator = declareVariable("iterator", Iterator.class);
 		Variable next = declareVariable("next", eventClass);
 
		// code:: EventManager.eventManager().getListeners(type).iterator()
 		{
 			push(eventClass);
 			invoke(ClassProxyHelper.ClassProxyHelper$eventListeners);
 			store(iterator);
 		}
 
 		// code:: loop
 		{
 			Label lcontinue = new Label();
 			Label lbreak = new Label();
 
 			label(lcontinue);
 			load(iterator);
 			invoke(ClassProxyHelper.Iterator$hasNext);
 			jumpif(false, lbreak);
 
 			load(iterator);
 			invoke(ClassProxyHelper.Iterator$next);
 			cast(next.type);
 			store(next);
 
 			tryinvoke(next);
 
 			jump(lcontinue);
 
 			label(lbreak);
 		}
 	}
 
 	private void tryinvoke(Variable next) {
 		Label ltry = new Label();
 		Label ltryend = new Label();
 		Label lcatch = new Label();
 		Label lcatchend = new Label();
 
 		mv.visitTryCatchBlock(ltry, ltryend, lcatch, Type.getInternalName(Throwable.class));
 
 		label(ltry);
 		dispatch(next); // guarded methodInfo invocation
 		label(ltryend);
 		jump(lcatchend); // try bock success
 
 		label(lcatch);
 		invokeVoid(ClassProxyHelper.ClassProxyHelper$onException);
 		label(lcatchend);
 	}
 
 	void dispatch(Variable next) {		
 		load(next);
 		for (Variable v : methodInfo.variables.methodParameters(true)) {
 			load(v);
 		}
 		invokeVoid(eventMethod);
 	}
 
 }
