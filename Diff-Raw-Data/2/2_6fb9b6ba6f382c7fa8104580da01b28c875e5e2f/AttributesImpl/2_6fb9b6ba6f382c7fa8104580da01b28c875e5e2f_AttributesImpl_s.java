 package org.jackie.compiler_impl.jmodelimpl.attribute;
 
 import org.jackie.compiler.event.AttributeListener;
 import org.jackie.compiler.spi.Compilable;
 import org.jackie.compiler.spi.CompilableHelper;
 import static org.jackie.event.Events.events;
 import org.jackie.jvm.attribute.Attributes;
 import org.jackie.jvm.attribute.JAttribute;
 import org.jackie.jvm.JNode;
 import org.jackie.utils.Assert;
 import static org.jackie.utils.CollectionsHelper.iterable;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import static java.util.Collections.unmodifiableList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author Patrik Beno
  */
 public class AttributesImpl implements Attributes, Compilable {
 
 	JNode jnode;
 	List<JAttribute> attributes;
 
 	public AttributesImpl(JNode jnode) {
 		this.jnode = jnode;
 	}
 
 	public JNode jnode() {
 		return jnode; 
 	}
 
 	public Set<String> getAttributeNames() {
 		Set<String> names = new HashSet<String>();
 		for (JAttribute a : iterable(attributes)) {
 			names.add(a.getName());
 		}
 		return names;
 	}
 
 	public List<JAttribute> getAttributes() {
 		return attributes != null ? unmodifiableList(attributes) : Collections.<JAttribute>emptyList();
 	}
 
 	public JAttribute getAttribute(String name) {
 		for (JAttribute a : iterable(attributes)) {
 			if (a.getName().equals(name)) {
 				return a;
 			}
 		}
 		return null;
 	}
 
 	public boolean isEditable() {
 		throw Assert.notYetImplemented(); // todo implement this
 	}
 
 	public Editor edit() {
 		return new Editor() {
 			public Editor addAttribute(JAttribute attribute) {
 				JAttribute<?> a = getAttribute(attribute.getName());
 				if (a != null) {
 					a.edit().setNext(attribute);
 				} else {
 					if (attributes == null) {
 						attributes = new ArrayList<JAttribute>();
 					}
 					attributes.add(attribute);
 				}
 				events(AttributeListener.class).attributeAdded(attribute);
 				return this;
 			}
 
 			public Attributes editable() {
 				return AttributesImpl.this;
 			}
 		};
 	}
 
 	public void compile() {
		for (JAttribute a : attributes) {
 			CompilableHelper.compile(a);
 		}
 	}
 }
