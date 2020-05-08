 package org.kisst.gft.action;
 
 import java.lang.reflect.Constructor;
 
 import org.kisst.cfg4j.Props;
 import org.kisst.gft.GftContainer;
 import org.kisst.util.ReflectionUtil;
 
 public class ActionFactory {
 
 	public static Action createAction(GftContainer gft, Props props) {
		String classname=props.getString("class");
 		if (classname.indexOf('.')<0)
 			classname="org.kisst.gft.action."+classname;
 		if (classname.startsWith(".")) // Prefix a class in the default package with a .
 			classname=classname.substring(1);
 		Constructor<?> c=ReflectionUtil.getConstructor(classname, new Class<?>[] {GftContainer.class, Props.class} );
 		if (c==null)
 			return (Action) ReflectionUtil.createObject(classname);
 		else
 			return (Action) ReflectionUtil.createObject(c, new Object[] {gft, props} );
 		
 	}
 }
