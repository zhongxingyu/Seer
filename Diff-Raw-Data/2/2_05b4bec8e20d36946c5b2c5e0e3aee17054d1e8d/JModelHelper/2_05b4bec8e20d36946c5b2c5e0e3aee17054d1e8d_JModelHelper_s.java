 package org.jackie.jvm.spi;
 
 import org.jackie.jvm.JClass;
 import org.jackie.jvm.JNode;
 import org.jackie.jvm.attribute.Attributed;
 import org.jackie.jvm.extension.Extensible;
 import static org.jackie.utils.Assert.NOTNULL;
 import static org.jackie.utils.JavaHelper.FALSE;
 
 /**
  * @author Patrik Beno
  */
 public class JModelHelper {
 
 	static public <T> T findOwner(JNode jnode, Class<T> type) {
 		NOTNULL(jnode);
 		NOTNULL(type);
 		
 		JNode candidate = jnode;
		while (candidate != null && type.isInstance(candidate)) {
 			candidate = candidate.owner();
 		}
 		return type.cast(candidate);
 	}
 
 	static public JClass findOwningJClass(JNode jnode) {
 		return findOwner(jnode, JClass.class);
 	}
 
 	static public boolean isEditable(JNode jnode) {
 		NOTNULL(jnode, "Missing jnode");
 		JClass jclass = findOwningJClass(jnode);
 		boolean editable = (jclass != null && jclass.isEditable());
 		return editable;
 	}
 
 }
