 package nz.ac.vuw.ecs.rprofs.server.weaving;
 
 import java.util.regex.Pattern;
 
 import nz.ac.vuw.ecs.rprof.HeapTracker;
 import nz.ac.vuw.ecs.rprofs.server.domain.Clazz;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Type;
 import org.objectweb.asm.commons.SerialVersionUIDAdder;
 import org.objectweb.asm.util.CheckClassAdapter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Weaver {
 
 	private static final Logger log = LoggerFactory.getLogger(Weaver.class);
 
 	public static final Pattern includes = new PatternBuilder()
 			.add(".*") //.add("[^/]*")
 			.get();
 
 	public static final Pattern collections = new PatternBuilder()
 			.add("java/util/.*Set")
 			.add("java/util/.*Map")
 			.add("java/util/.*List")
 			.add("java/util/.*table")
 			.add("java/util/.*Queue")
 			.add("java/util/.*Deque")
 			.add("java/util/Vector")
 			.get();
 
 	public static final Pattern generated = new PatternBuilder()
 			.add("sun/reflect/Generated.*")
 			.add(".*ByCGLIB$$.*")
 			.get();
 
 	public static final Pattern excludes = new PatternBuilder()
 //			.add("java/awt/.*")        // jhotdraw has font problems if this packages is included
 //			.add("com/sun/.*")
 //			.add("sun/.*")
 //			.add("apple/.*")
 //			.add("com/apple/.*")        // might help jhotdraw?
 //			.add("java/lang/IncompatibleClassChangeError")    // gc blows up if this is woven
 //			.add("java/lang/LinkageError")                    // gc blows up if this is woven
 //			.add("java/lang/NullPointerException")            // something blows up - null pointers appear as runtime exceptions with this change
 //			.add("java/util/concurrent/.*")                    // SIGSEGV/SIGBUS in pmd
 //			.add("java/lang/reflect/.*")
 //			.add("sun/reflect/.*")
 			.add("java/nio/charset/CharsetDecoder")
 			.add("java/nio/charset/CharsetEncoder")
 			.add("java/util/zip/ZipFile")
 					// throwable has a transient field, backtrace, which causes off-by-1 errors
 					// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4934380
 			.add("java/lang/Throwable")
 			.add("java/lang/String") // strings are automatically created, breaking final field tracking
 					// this won't cause errors, but looks weird in output data

			.add("org/apache/geronimo/security/keystore/FileKeystoreManager")
 			.get();
 
 	public byte[] weave(ClassRecord record, byte[] classfile) {
 
 		ClassReader reader = new ClassReader(classfile);
 		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
 
 		ClassVisitor visitor;
 		if (Type.getInternalName(HeapTracker.class).equals(record.getName())) {
 			record.addProperty(Clazz.SPECIAL_CLASS_WEAVER);
 			visitor = new TrackingClassWeaver(writer, record);
 		} else if (Type.getInternalName(Thread.class).equals(record.getName())) {
 			record.addProperty(Clazz.SPECIAL_CLASS_WEAVER);
 			visitor = new ThreadClassWeaver(writer, record);
 		} else if (Type.getInternalName(Object.class).equals(record.getName())) {
 			record.addProperty(Clazz.SPECIAL_CLASS_WEAVER);
 			visitor = new ObjectClassWeaver(writer, record);
 		} else {
 			if (collections.matcher(record.getName()).find()) {
 				record.addProperty(Clazz.COLLECTION_MATCHED);
 				log.trace("{} is a collection", record.getName());
 			} else if (generated.matcher(record.getName()).find()) {
 				record.addProperty(Clazz.GENERATED_MATCHED);
 				log.trace("{} is generated", record.getName());
 			}
 			if (includes.matcher(record.getName()).find()) {
 				record.addProperty(Clazz.CLASS_INCLUDE_MATCHED);
 				if (excludes.matcher(record.getName()).find()) {
 					record.addProperty(Clazz.CLASS_EXCLUDE_MATCHED);
 					return new byte[0];
 				} else {
 					visitor = new GenericClassWeaver(writer, record);
 					visitor = new SerialVersionUIDAdder(visitor);
 				}
 			} else {
 				visitor = writer;
 			}
 		}
 
 		visitor = new CheckClassAdapter(visitor);
 
 		reader.accept(visitor, 0);
 
 		return writer.toByteArray();
 	}
 
 	private static class PatternBuilder {
 		StringBuilder pattern = null;
 
 		public PatternBuilder add(String pattern) {
 			if (this.pattern == null) {
 				this.pattern = new StringBuilder("^");
 			} else {
 				this.pattern.append('|');
 			}
 			this.pattern.append('(');
 			this.pattern.append(pattern);
 			this.pattern.append(')');
 			return this;
 		}
 
 		public Pattern get() {
 			String pattern = this.pattern.append("$").toString();
 			log.trace("using pattern: {}", pattern);
 			return Pattern.compile(pattern);
 		}
 	}
 }
