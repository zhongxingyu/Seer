 package org.jackie.compiler_impl.jmodelimpl;
 
 import org.jackie.compiler.event.ExtensionEvents;
 import org.jackie.compiler.extension.ExtensionManager;
 import org.jackie.compiler.extension.ExtensionProvider;
 import org.jackie.compiler.spi.Compilable;
 import static org.jackie.context.ContextManager.context;
 import static org.jackie.event.Events.events;
 import org.jackie.jvm.JNode;
 import org.jackie.jvm.extension.Extension;
 import org.jackie.jvm.extension.Extensions;
import org.jackie.jvm.extension.builtin.PrimitiveType;
import org.jackie.jvm.extension.builtin.ArrayType;
 import org.jackie.jvm.spi.JModelHelper;
 import static org.jackie.utils.Assert.*;
 import org.jackie.utils.Stack;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
import java.util.Arrays;
 
 /**
  * @author Patrik Beno
  */
 public class ExtensionsImpl implements Extensions, Compilable {
 
 	JNode jnode;
 
 	Map<Class<? extends Extension>, Extension> extensions;
 
 	public ExtensionsImpl(JNode jnode) {
 		this.jnode = jnode;
 	}
 
 	{
 		extensions = new HashMap<Class<? extends Extension>, Extension>();
 	}
 
 	public Set<Class<? extends Extension>> supported() {
 		return Collections.unmodifiableSet(extensions.keySet());
 	}
 
 	public <T extends Extension> boolean supports(Class<T> type) {
 		NOTNULL(type);
 		return extensions.containsKey(type) || get(type) != null;
 	}
 
 	public <T extends Extension> T get(Class<T> type) {
 		NOTNULL(type);
 
 		Extension ext = extensions.get(type);
 		if (ext != null) {
 			return typecast(ext, type);
 		}
 
		// todo handle primitives and arrays
		if (Arrays.asList(PrimitiveType.class, ArrayType.class).contains(type)) {
			return null; 
		}

 		Info info = new Info(type, jnode);
 
 		doAssert(!processing().contains(info),
 					"Recursive extension resolution (%s). Current stack: %s", type.getName(), info);
 
 		processing().push(info);
 		try {
 			// todo optimize extension lookup: positive lookups fall here only once; false lookups (nonexistent or unsupported extensions) will repeat this on every lookup
 
 			ExtensionManager xm = NOTNULL(context(ExtensionManager.class));
 			ExtensionProvider provider = xm.getProvider(type);
 
 			doAssert(provider != null, "No extension provider for extension type: %s", type.getName());
 
 			ext = provider.getExtension(jnode);
 
 			if (ext == null) {
 				return null;
 			}
 
 			assert type.equals(ext.type());
 
 			extensions.put(type, ext);
 			events(ExtensionEvents.class).added(ext);
 
 			return typecast(ext, type);
 
 		} finally {
 			processing().pop();
 		}
 	}
 
 	public boolean isEditable() {
 		return JModelHelper.isEditable(jnode);
 	}
 
 	public Editor edit() {
 		return new Editor() {
 			public void add(Extension extension) {
 				NOTNULL(extension);
 				doAssert(!extensions.containsKey(extension.type()),
 							"Duplicate extension (%s) in %s",
 							extension.type().getName(), jnode);
 
 				extensions.put(extension.type(), extension);
 				events(ExtensionEvents.class).added(extension);
 			}
 
 			public Extensions editable() {
 				return ExtensionsImpl.this;
 			}
 		};
 	}
 
 	public Iterator<Extension> iterator() {
 		return new Iterator<Extension>() {
 
 			Iterator<Map.Entry<Class<? extends Extension>, Extension>> it
 					= extensions.entrySet().iterator();
 
 			public boolean hasNext() {
 				return it.hasNext();
 			}
 
 			public Extension next() {
 				return it.next().getValue();
 			}
 
 			public void remove() {
 				it.remove();
 			}
 		};
 	}
 
 	public void compile() {
 		for (Extension ext : this) {
 			((Compilable) ext).compile();
 		}
 	}
 
 	static ThreadLocal<Stack<Info>> tlInProgress = new ThreadLocal<Stack<Info>>();
 
 	static class Info {
 		Class<? extends Extension> type;
 		JNode jnode;
 
 		Info(Class<? extends Extension> type, JNode jnode) {
 			this.type = NOTNULL(type);
 			this.jnode = NOTNULL(jnode);
 		}
 
 		public boolean equals(Object o) {
 			if (this == o) return true;
 			if (o == null || getClass() != o.getClass()) return false;
 
 			Info info = (Info) o;
 
 			if (jnode != info.jnode) { return false; }
 			if (type != info.type) { return false; }
 
 			return true;
 		}
 
 		public int hashCode() {
 			int result;
 			result = System.identityHashCode(type.hashCode());
 			result = 31 * result + System.identityHashCode(jnode);
 			return result;
 		}
 
 		public String toString() {
 			return String.format("%s for %s@%s", type.getName(), jnode.getClass().getName(), System.identityHashCode(jnode));
 		}
 	}
 
 	protected Stack<Info> processing() {
 		Stack<Info> stack = tlInProgress.get();
 		if (stack == null) {
 			stack = new Stack<Info>();
 			tlInProgress.set(stack);
 		}
 		return stack;
 	}
 
 
 }
