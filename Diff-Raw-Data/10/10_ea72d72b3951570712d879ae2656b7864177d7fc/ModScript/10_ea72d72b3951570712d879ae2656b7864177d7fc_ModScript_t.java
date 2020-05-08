 package org.rsbot.loader.script;
 
 import org.rsbot.loader.asm.ClassAdapter;
 import org.rsbot.loader.asm.ClassReader;
 import org.rsbot.loader.asm.ClassVisitor;
 import org.rsbot.loader.asm.ClassWriter;
 import org.rsbot.loader.script.adapter.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Jacmob
  */
 public class ModScript {
 
 	public static interface Opcodes {
 		int ATTRIBUTE = 1;
 		int GET_STATIC = 2;
 		int GET_FIELD = 3;
 		int ADD_FIELD = 4;
 		int ADD_METHOD = 5;
 		int ADD_INTERFACE = 6;
 		int SET_SUPER = 7;
 		int SET_SIGNATURE = 8;
 		int INSERT_CODE = 9;
 		int OVERRIDE_CLASS = 10;
 	}
 
 	public static final int MAGIC = 0xFADFAD;
 
 	private String name;
 	private int version;
 	private Map<String, String> attributes;
 	private Map<String, ClassAdapter> adapters;
 	private Map<String, ClassWriter> writers;
 
 	public ModScript(final byte[] data) throws ParseException {
 		load(new Buffer(data));
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public int getVersion() {
 		return version;
 	}
 
 	public String getAttribute(final String key) {
 		return attributes.get(key);
 	}
 
 	public byte[] process(final String key, final byte[] data) {
 		final ClassAdapter adapter = adapters.get(key);
 		if (adapter != null) {
 			final ClassReader reader = new ClassReader(data);
 			reader.accept(adapter, ClassReader.SKIP_FRAMES);
 			return writers.get(key).toByteArray();
 		}
 		return data;
 	}
 
 	public byte[] process(final String key, final InputStream is) throws IOException {
 		final ClassAdapter adapter = adapters.get(key);
 		if (adapter != null) {
 			final ClassReader reader = new ClassReader(is);
 			reader.accept(adapter, ClassReader.SKIP_FRAMES);
 			return writers.get(key).toByteArray();
 		}
 		final ByteArrayOutputStream os = new ByteArrayOutputStream();
 		final byte[] buffer = new byte[4096];
 		int n;
 		while ((n = is.read(buffer)) != -1) {
 			os.write(buffer, 0, n);
 		}
 		return os.toByteArray();
 	}
 
 	private void load(final Buffer buff) throws ParseException {
 		if (buff.g4() != ModScript.MAGIC) {
 			throw new ParseException("Bad magic!");
 		}
 		attributes = new HashMap<String, String>();
 		adapters = new HashMap<String, ClassAdapter>();
 		writers = new HashMap<String, ClassWriter>();
 		name = buff.gstr();
 		version = buff.g2();
 		int num = buff.g2();
 		while (num-- > 0) {
 			final int op = buff.g1();
 			if (op == Opcodes.ATTRIBUTE) {
 				final String key = buff.gstr();
 				final String value = buff.gstr();
 				attributes.put(key, new StringBuilder(value).reverse().toString());
 			} else if (op == Opcodes.GET_STATIC || op == Opcodes.GET_FIELD) {
 				final String clazz = buff.gstr();
 				final int count = buff.g2();
 				int ptr = 0;
 				final AddGetterAdapter.Field[] fields = new AddGetterAdapter.Field[count];
 				while (ptr < count) {
 					final AddGetterAdapter.Field f = new AddGetterAdapter.Field();
 					f.getter_access = buff.g4();
 					f.getter_name = buff.gstr();
 					f.getter_desc = buff.gstr();
 					f.owner = buff.gstr();
 					f.name = buff.gstr();
 					f.desc = buff.gstr();
 
 					fields[ptr++] = f;
 				}
 				adapters.put(clazz, new AddGetterAdapter(delegate(clazz), op == Opcodes.GET_FIELD, fields));
 			} else if (op == Opcodes.ADD_FIELD) {
 				final String clazz = buff.gstr();
 				final int count = buff.g2();
 				int ptr = 0;
 				final AddFieldAdapter.Field[] fields = new AddFieldAdapter.Field[count];
 				while (ptr < count) {
 					final AddFieldAdapter.Field f = new AddFieldAdapter.Field();
 					f.access = buff.g4();
 					f.name = buff.gstr();
 					f.desc = buff.gstr();
 					fields[ptr++] = f;
 				}
 				adapters.put(clazz, new AddFieldAdapter(delegate(clazz), fields));
 			} else if (op == Opcodes.ADD_METHOD) {
 				final String clazz = buff.gstr();
 				final int count = buff.g2();
 				int ptr = 0;
 				final AddMethodAdapter.Method[] methods = new AddMethodAdapter.Method[count];
 				while (ptr < count) {
 					final AddMethodAdapter.Method m = new AddMethodAdapter.Method();
 					m.access = buff.g4();
 					m.name = buff.gstr();
 					m.desc = buff.gstr();
 					final byte[] code = new byte[buff.g4()];
 					buff.gdata(code, code.length, 0);
 					m.code = code;
 					m.max_locals = buff.g1();
 					m.max_stack = buff.g1();
 					methods[ptr++] = m;
 				}
 				adapters.put(clazz, new AddMethodAdapter(delegate(clazz), methods));
 			} else if (op == Opcodes.ADD_INTERFACE) {
 				final String clazz = buff.gstr();
 				final String inter = buff.gstr();
 				adapters.put(clazz, new AddInterfaceAdapter(delegate(clazz), inter));
 			} else if (op == Opcodes.SET_SUPER) {
 				final String clazz = buff.gstr();
 				final String superName = buff.gstr();
 				adapters.put(clazz, new SetSuperAdapter(delegate(clazz), superName));
 			} else if (op == Opcodes.SET_SIGNATURE) {
 				final String clazz = buff.gstr();
 				final int count = buff.g2();
 				int ptr = 0;
 				final SetSignatureAdapter.Signature[] signatures = new SetSignatureAdapter.Signature[count];
 				while (ptr < count) {
 					final SetSignatureAdapter.Signature s = new SetSignatureAdapter.Signature();
 					s.name = buff.gstr();
 					s.desc = buff.gstr();
 					s.new_access = buff.g4();
 					s.new_name = buff.gstr();
 					s.new_desc = buff.gstr();
 					signatures[ptr++] = s;
 				}
 				adapters.put(clazz, new SetSignatureAdapter(delegate(clazz), signatures));
 			} else if (op == Opcodes.INSERT_CODE) {
 				final String clazz = buff.gstr();
 				final String name = buff.gstr();
 				final String desc = buff.gstr();
 				int count = buff.g1();
 				final Map<Integer, byte[]> fragments = new HashMap<Integer, byte[]>();
 				while (count-- > 0) {
 					final int off = buff.g2();
 					final byte[] code = new byte[buff.g4()];
 					buff.gdata(code, code.length, 0);
 					fragments.put(off, code);
 				}
 				adapters.put(clazz, new InsertCodeAdapter(delegate(clazz),
 						name, desc, fragments, buff.g1(), buff.g1()));
 			} else if (op == Opcodes.OVERRIDE_CLASS) {
 				final String old_clazz = buff.gstr();
 				final String new_clazz = buff.gstr();
 				int count = buff.g1();
 				while (count-- > 0) {
 					final String clazz = buff.gstr();
 					adapters.put(clazz, new OverrideClassAdapter(delegate(clazz), old_clazz, new_clazz));
 				}
 			}
 		}
 	}
 
 	private ClassVisitor delegate(final String clazz) {
 		final ClassAdapter delegate = adapters.get(clazz);
 		if (delegate == null) {
 			final ClassWriter writer = new ClassWriter(0);
 			writers.put(clazz, writer);
 			return writer;
 		} else {
 			return delegate;
 		}
 	}
 
}
