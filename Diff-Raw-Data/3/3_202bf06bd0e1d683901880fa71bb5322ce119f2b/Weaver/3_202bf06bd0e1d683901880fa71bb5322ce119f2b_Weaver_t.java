 package nz.ac.vuw.ecs.rprofs.server.weaving;
 
 import nz.ac.vuw.ecs.rprof.HeapTracker;
 import nz.ac.vuw.ecs.rprofs.server.domain.Class;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
 
 import org.objectweb.asm.ClassAdapter;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Type;
 
 public class Weaver {
 
 	private ClassRecord cr;
 	private short lastMethodId = 0;
 	private short lastFieldId = 0;
 
 	public Weaver(ClassId classId) {
 		this.cr = new ClassRecord(this, classId);
 	}
 
 	public byte[] weave(byte[] classfile) {
 
 		ClassReader reader = new ClassReader(classfile);
 		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
 
 		reader.accept(new FieldReader(cr), ClassReader.SKIP_CODE);
 		reader.accept(new Dispatcher(writer, cr), 0); // ClassReader.EXPAND_FRAMES);
 
 		return writer.toByteArray();
 	}
 
 	public ClassRecord getClassRecord() {
 		return cr;
 	}
 
 	MethodRecord createMethodRecord(String name) {
 		MethodRecord mr = new MethodRecord(cr, ++lastMethodId, name);
 		cr.methods.put(mr.id, mr);
 		return mr;
 	}
 
 	FieldRecord createFieldRecord(String name) {
 		FieldRecord fr = new FieldRecord(cr, ++lastFieldId, name);
 		cr.fields.put(fr.id, fr);
 		return fr;
 	}
 
 	private static class Dispatcher extends ClassVisitorDispatcher {
 
 		private ClassVisitor cv;
 		private ClassRecord cr;
 
 		public Dispatcher(ClassVisitor cv, ClassRecord cr) {
 			this.cv = cv;
 			this.cr = cr;
 		}
 
 		@Override
 		public void visit(int version, int access, String name,
 				String signature, String superName, String[] interfaces) {
 
 			ClassVisitor cv = null;
 
 
 			String[] filters = new String[] {
 					"sun/reflect/.*",	// jhotdraw crashes in this package
 					"java/awt/.*",		// jhotdraw has font problems if this packages is included
 					"com/sun/.*",
 					"sun/.*",
 					"apple/.*",
 					"com/apple/.*",		// might help jhotdraw?
 					"java/lang/IncompatibleClassChangeError",	// gc blows up if this is woven
 					"java/lang/LinkageError",					// gc blows up if this is woven
					"java/lang/NullPointerException",			// something blows up - null pointers appear as runtime exceptions with this change
					"java/util/concurrent/.*"					// SIGSEGV/SIGBUS in pmd
 			};
 
 			int flags = cr.properties;
 
 			if (Type.getInternalName(HeapTracker.class).equals(name)) {
 				flags |= Class.SPECIAL_CLASS_WEAVER;
 				cv = new TrackingClassWeaver(this.cv, cr);
 			}
 			else if (Type.getInternalName(Thread.class).equals(name)) {
 				flags |= Class.SPECIAL_CLASS_WEAVER;
 				cv = new ThreadClassWeaver(this.cv, cr);
 			}
 			else if (Type.getInternalName(Throwable.class).equals(name)) {
 				flags |= Class.SPECIAL_CLASS_WEAVER;
 				cv = new ThreadClassWeaver(this.cv, cr);
 			}
 			else if (Type.getInternalName(Object.class).equals(name)) {
 				flags |= Class.SPECIAL_CLASS_WEAVER;
 				cv = new ObjectClassWeaver(this.cv, cr);
 			}
 			else {
 				for (String filter: filters) {
 					if (name.matches(filter)) {
 						flags |= Class.CLASS_IGNORED_PACKAGE_FILTER;
 						cv = new ClassAdapter(this.cv);
 					}
 				}
 			}
 
 			cr.setProperties(flags);
 
 			if (cv == null) {
 				cv = new GenericClassWeaver(this.cv, cr);
 			}
 
 			setClassVisitor(cv);
 			cv.visit(version, access, name, signature, superName, interfaces);
 		}
 	}
 }
