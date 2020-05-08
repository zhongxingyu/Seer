 package org.jackie.jclassfile.constantpool;
 
 import org.jackie.jclassfile.model.Base;
 import org.jackie.utils.Log;
 import org.jackie.utils.Assert;
 import org.jackie.utils.Closeable;
 import org.jackie.utils.XDataInput;
 import org.jackie.utils.WriteOnlyList;
 import org.jackie.utils.XDataOutput;
 import static org.jackie.utils.Assert.typecast;
 import static org.jackie.utils.Assert.NOTNULL;
 import static org.jackie.utils.Assert.expected;
 import static org.jackie.utils.Assert.doAssert;
 import org.jackie.context.ContextObject;
import org.jackie.context.Context;
import org.jackie.context.ContextManager;
 import static org.jackie.context.ContextManager.context;
import static org.jackie.context.ContextManager.contextManager;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Formattable;
 import java.util.Formatter;
 
 /**
  * @author Patrik Beno
  */
 public class ConstantPool extends Base implements ContextObject, Closeable {
 
 	static public ConstantPool constantPool() {
 		ConstantPool pool = context().get(ConstantPool.class);
 		return NOTNULL(pool);
 	}
 
 	static public boolean available() {
		return contextManager().hasContext() && context().get(ConstantPool.class) != null;
 	}
 
 	static public ConstantPool create(boolean bindToContext) {
 		ConstantPool pool = new ConstantPool();
 		if (bindToContext) {
 			context().set(ConstantPool.class, pool);
 		}
 		return pool;
 	}
 
 	List<Constant> constants;
 	Map<Constant,Integer> indices;
 
 	{
 		init();
 	}
 
 	void init() {
 		constants = new ArrayList<Constant>();
 		constants.add(null); // index #0 is reserved
 		indices = new HashMap<Constant, Integer>();
 	}
 
 	public void load(XDataInput in, ConstantPool pool) {
 		init();
 
 		indices = null; // will rebuild index after all constants are read
 
 		int count = in.readUnsignedShort();
 
 		Log.debug("Loading constant pool (size: %s)", count);
 
 		List<Task> secondPassResolvers = new WriteOnlyList<Task>();
 
 		// round #1: register constants
 		for (int i = 1; i < count; i++) { // start from #1, #0 is reserved by JVM for NULL and it's not saved in pool
 			expected(constants.size(), i, "broken constant index?");
 
 			Constant c = loadConstant(in, secondPassResolvers);
 			constants.add(c);
 
 			if (c.isLongData()) {
 				i++;
 				constants.add(null);
 			}
 		}
 
 		// round #2: resolve/initialize
 		for (Task resolver : secondPassResolvers) {
 			resolver.execute();
 		}
 
 		// round #3: build index
 		buildIndex: {
 			indices = new HashMap<Constant, Integer>(constants.size());
 			int i = 0;
 			for (Constant c : constants) {
 				if (c != null) {
 					indices.put(c, i);
 				}
 				i++;
 			}
 		}
 
 		Log.debug("Loaded constant pool (%s non-null entries, total pool size: %s)", 
 					 countNotNull(constants), constants.size());
 		for (Constant c : constants) {
 			if (c == null) { continue; }
 			Log.debug("\t%s", c);
 		}
 
 	}
 
 	public void save(XDataOutput out) {
 		for (Constant c : new ArrayList<Constant>(constants)) {
 			if (c == null) { continue; }
 			c.register();
 		}
 
 		Log.debug("Saving constant pool (%s non-null entries, total pool size: %s)",
 					 countNotNull(constants), constants.size());
 
 		out.writeShort(constants.size());
 		for (Constant c : constants) {
 			if (c == null) { continue; }
 			Log.debug("\t%s", c);
 			c.save(out);
 		}
 	}
 
 	public <T extends Constant> T getConstant(int index, Class<T> type) {
 		if (index == 0) { return null; }
 
 		doAssert(index <= constants.size(),
 					"Invalid constant pool index %s, requested type %s (Pool size: %s)",
 					index, type.getSimpleName(), constants.size());
 
 
 		T c = typecast(constants.get(index), type);
 		return NOTNULL(c, "No constant found for index %s", index);
 	}
 
 	protected Constant loadConstant(XDataInput in, List<Task> secondPassResolvers) {
 		CPEntryType type = CPEntryType.forCode(in.readUnsignedByte());
 		Constant c = type.loader().load(in, this, secondPassResolvers);
 		return c;
 	}
 
 	public <T extends Constant> T intern(T constant) {
 		Integer idx = indexOf(constant, true);
 		Constant interned = constants.get(idx);
 		return Assert.typecast(interned, (Class<T>) constant.getClass());
 	}
 
 	public void close() {
 	}
 
 	public Integer indexOf(Constant constant) {
 		return indexOf(constant, false);
 	}
 
 	public Integer indexOf(Constant constant, boolean register) {
 		if (indices == null) { return null; } // index was not yet built
 
 		Integer idx = indices.get(constant);
 		if (idx == null && register) {
 			constants.add(constant);
 			idx = constants.size() - 1;
 			indices.put(constant, idx);
 			if (constant.isLongData()) { // long data (long, double) occupy two entries in the pool
 				constants.add(null);
 			}
 		}
 		return idx;
 	}
 
 	Formattable countNotNull(final List<?> list) {
 		return new Formattable() {
 			public void formatTo(Formatter formatter, int flags, int width, int precision) {
 				int count = 0;
 				for (Object o : list) {
 					if (o != null) { count++; }
 				}
 				formatter.format("%s", count);
 			}
 		};
 	}
 }
