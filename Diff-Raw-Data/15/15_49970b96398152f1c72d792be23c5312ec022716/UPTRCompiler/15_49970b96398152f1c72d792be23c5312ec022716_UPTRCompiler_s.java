 package clojure.lang;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.net.URI;
 
 import lang.clojure.syntax.ClojureParser;
 
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.eclipse.imp.pdb.facts.IList;
 import org.eclipse.imp.pdb.facts.IListWriter;
 import org.eclipse.imp.pdb.facts.ISourceLocation;
 import org.eclipse.imp.pdb.facts.IString;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.rascalmpl.parser.gtd.IGTD;
 import org.rascalmpl.parser.gtd.util.ArrayList;
 import org.rascalmpl.parser.uptr.NodeToUPTR;
 import org.rascalmpl.values.ValueFactoryFactory;
 import org.rascalmpl.values.uptr.TreeAdapter;
 
 import clojure.asm.Type;
 
 public class UPTRCompiler extends Compiler {
 	private static final String START_SORT = "start__File";
 
 	// have to copy, because private in Compiler.
 	private static final Type VAR_TYPE = Type.getType(Var.class);
 	
 	private final IValueFactory vf;
 	
 	public UPTRCompiler(IValueFactory vf) {
 		this.vf = vf;
 	}
 	
 	// TODO: this class needs some refactoring... Too much duplication.
 	
 	public static IConstructor loadForRascal(IValueFactory vf, IString src, ISourceLocation loc) {
 		IGTD gtd = new ClojureParser();
 		IConstructor file = (IConstructor) gtd.parse(START_SORT, loc.getURI(), 
 				src.getValue().toCharArray(), new NodeToUPTR());
 		
 		String path = loc.getURI().getPath();
 		IConstructor[] fileRef = new IConstructor[] {file};
 		
 		// ignore the clojure result
 		new UPTRCompiler(vf).loadPT(fileRef, path, new File(path).getName());
 		
 		// return the updated parse tree.
 		// TODO: collect error messages.
 		return fileRef[0];
 	}
 
 	
 	public static Object loadWithRascal(Reader rdr, String sourcePath, String sourceName) {
 		IValueFactory vf = ValueFactoryFactory.getValueFactory();
 		UPTRCompiler c = new UPTRCompiler(vf);
 		return c.load2(rdr, sourcePath, sourceName);
 	}
 	
 	public Object load2(Reader rdr, String sourcePath, String sourceName) {
 		char[] input = convertReader(rdr);
 		return loadFromChars(input, sourcePath, sourceName);
 	}
 
 	private Object loadFromChars(char[] input, String sourcePath,
 			String sourceName) {
 		IGTD gtd = new ClojureParser();
 		IConstructor file = (IConstructor) gtd.parse(START_SORT, URI.create(sourcePath), input, new NodeToUPTR());
 		
 		IConstructor filePt = (IConstructor)file;
 		return loadPT(new IConstructor[] {filePt}, sourcePath, sourceName);
 	}
 	
 	public Object loadPT(IConstructor[] fileRef, String sourcePath, String sourceName) {
 		IConstructor file = fileRef[0];
 		ISourceLocation loc = TreeAdapter.getLocation(file);
 		int lineNumber = loc.getBeginLine();
 		
 		Object ret = null;
 		
 		Var.pushThreadBindings(
 				RT.map(LOADER, RT.makeClassLoader(),
 				       SOURCE_PATH, sourcePath,
 				       SOURCE, sourceName,
 				       METHOD, null,
 				       LOCAL_ENV, null,
 						LOOP_LOCALS, null,
 						NEXT_LOCAL_NUM, 0,
 				       RT.CURRENT_NS, RT.CURRENT_NS.deref(),
 				       LINE_BEFORE, lineNumber,
 				       LINE_AFTER, lineNumber
 				       ,RT.UNCHECKED_MATH, RT.UNCHECKED_MATH.deref()
 						,RT.WARN_ON_REFLECTION, RT.WARN_ON_REFLECTION.deref()
 				       ,RT.DATA_READERS, RT.DATA_READERS.deref()
 	                        ));
 
 		UPTRLispReader reader = new UPTRLispReader(vf);
 		
 		try
 			{
 			
 			if (TreeAdapter.isAmb(file)) {
 				System.err.println("Amb");
 			}
 			
 			// File is start[File], so
 			IConstructor file2 = (IConstructor) TreeAdapter.getArgs(file).get(1);
 			IList args = TreeAdapter.getArgs(file2);
 			// Probably only this is the list of forms; don't forget to fix below.
 			IList forms = TreeAdapter.getArgs((IConstructor) args.get(0));
//			IList forms3 = TreeAdapter.getArgs((IConstructor)forms.get(0));
 
 
 			IListWriter newArgs = vf.listWriter();
 			for (int i = 0; i < forms.length(); i++) {
 				IConstructor form = (IConstructor) forms.get(i);
 				// only forms, no literals at this level.
 				UPTRLispReader.Pair p = reader.read(form);
 				newArgs.append(p.tree);
 				LINE_AFTER.set(TreeAdapter.getLocation(form).getEndLine());
 				ret = eval(p.obj, false);
 				LINE_BEFORE.set(TreeAdapter.getLocation(form).getBeginLine());
 				if (i < forms.length() - 2) {
 					i++;
 					newArgs.append(forms.get(i)); // layout
 				}
 			}
 			// Fix tree
 			file2 = file2.set("args", newArgs.done());
			file = file.set(1, file2);
 
 			}
 		catch(LispReader.ReaderException e)
 			{
 			throw new CompilerException(sourcePath, e.line, e.getCause());
 			}
 		finally
 			{
 			Var.popThreadBindings();
 			}
 		fileRef[0] = file;
 		return ret;
 	}
 	
 //	public static void compileWithRascal(Reader rdr, String sourcePath, String sourceName) throws IOException {
 //		IValueFactory vf = ValueFactoryFactory.getValueFactory();
 //		UPTRCompiler c = new UPTRCompiler(vf);
 //		c.compile2(rdr, sourcePath, sourceName);
 //	}
 //
 //	public void compile2(Reader rdr, String sourcePath, String sourceName) throws IOException {
 //		IGTD gtd = new ClojureParser();
 //		char[] input = convertReader(rdr);
 //		IConstructor file = (IConstructor) gtd.parse("start__File", URI.create(sourcePath), input, new NodeToUPTR());
 //		
 //		IConstructor filePt = (IConstructor)file;
 //		compilePT(filePt, sourcePath, sourceName);
 //	}
 //	
 //	public IConstructor compilePT(IConstructor file, String sourcePath, String sourceName) throws IOException {
 //		ISourceLocation loc = TreeAdapter.getLocation(file);
 //		int lineNumber = loc.getBeginLine();
 //		
 //		UPTRLispReader reader = new UPTRLispReader(vf);
 //
 //		
 //		if (COMPILE_PATH.deref() == null)
 //			throw Util.runtimeException("*compile-path* not set");
 //
 //		Var.pushThreadBindings(RT.map(SOURCE_PATH, sourcePath, SOURCE,
 //				sourceName, METHOD, null, LOCAL_ENV, null, LOOP_LOCALS, null,
 //				NEXT_LOCAL_NUM, 0, RT.CURRENT_NS, RT.CURRENT_NS.deref(),
 //				LINE_BEFORE, lineNumber, LINE_AFTER, lineNumber, CONSTANTS,
 //				PersistentVector.EMPTY, CONSTANT_IDS, new IdentityHashMap(),
 //				KEYWORDS, PersistentHashMap.EMPTY, VARS,
 //				PersistentHashMap.EMPTY, RT.UNCHECKED_MATH,
 //				RT.UNCHECKED_MATH.deref(), RT.WARN_ON_REFLECTION,
 //				RT.WARN_ON_REFLECTION.deref(), RT.DATA_READERS,
 //				RT.DATA_READERS.deref()
 //		// ,LOADER, RT.makeClassLoader()
 //				));
 //
 //		try {
 //			// generate loader class
 //			ObjExpr objx = new ObjExpr(null);
 //			objx.internalName = sourcePath.replace(File.separator, "/")
 //					.substring(0, sourcePath.lastIndexOf('.'))
 //					+ RT.LOADER_SUFFIX;
 //
 //			objx.objtype = Type.getObjectType(objx.internalName);
 //			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
 //			ClassVisitor cv = cw;
 //			cv.visit(V1_5, ACC_PUBLIC + ACC_SUPER, objx.internalName, null,
 //					"java/lang/Object", null);
 //
 //			// static load method
 //			GeneratorAdapter gen = new GeneratorAdapter(
 //					ACC_PUBLIC + ACC_STATIC, Method.getMethod("void load ()"),
 //					null, null, cv);
 //			gen.visitCode();
 //
 //			
 //			// File is start[File], so
 //			IConstructor file2 = (IConstructor) TreeAdapter.getArgs(file).get(1);
 //			IList args = TreeAdapter.getArgs(file2);
 //			
 //			
 //			IListWriter newArgs = vf.listWriter();
 //			for (int i = 0; i < args.length(); i++) {
 //				IConstructor form = (IConstructor) args.get(i);
 //				// only forms, no literals at this level.
 //				UPTRLispReader.Pair p = reader.read(form);
 //				newArgs.append(p.tree);
 //				LINE_AFTER.set(TreeAdapter.getLocation(form).getEndLine());
 //				compile1(gen, objx, p.obj);
 //				LINE_BEFORE.set(TreeAdapter.getLocation(form).getBeginLine());
 //				i++;
 //				newArgs.append(args.get(i)); // layout
 //			}
 //			// Fix tree
 //			file2 = file2.set("args", newArgs.done());
 //			file = file.set(1, file2);
 //
 //			// end of load
 //			gen.returnValue();
 //			gen.endMethod();
 //
 //			// static fields for constants
 //			for (int i = 0; i < objx.constants.count(); i++) {
 //				cv.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, objx
 //						.constantName(i), objx.constantType(i).getDescriptor(),
 //						null, null);
 //			}
 //
 //			final int INITS_PER = 100;
 //			int numInits = objx.constants.count() / INITS_PER;
 //			if (objx.constants.count() % INITS_PER != 0)
 //				++numInits;
 //
 //			for (int n = 0; n < numInits; n++) {
 //				GeneratorAdapter clinitgen = new GeneratorAdapter(ACC_PUBLIC
 //						+ ACC_STATIC,
 //						Method.getMethod("void __init" + n + "()"), null, null,
 //						cv);
 //				clinitgen.visitCode();
 //				try {
 //					Var.pushThreadBindings(RT.map(RT.PRINT_DUP, RT.T));
 //
 //					for (int i = n * INITS_PER; i < objx.constants.count()
 //							&& i < (n + 1) * INITS_PER; i++) {
 //						objx.emitValue(objx.constants.nth(i), clinitgen);
 //						clinitgen.checkCast(objx.constantType(i));
 //						clinitgen.putStatic(objx.objtype, objx.constantName(i),
 //								objx.constantType(i));
 //					}
 //				} finally {
 //					Var.popThreadBindings();
 //				}
 //				clinitgen.returnValue();
 //				clinitgen.endMethod();
 //			}
 //
 //			// static init for constants, keywords and vars
 //			GeneratorAdapter clinitgen = new GeneratorAdapter(ACC_PUBLIC
 //					+ ACC_STATIC, Method.getMethod("void <clinit> ()"), null,
 //					null, cv);
 //			clinitgen.visitCode();
 //			Label startTry = clinitgen.newLabel();
 //			Label endTry = clinitgen.newLabel();
 //			Label end = clinitgen.newLabel();
 //			Label finallyLabel = clinitgen.newLabel();
 //
 //			// if(objx.constants.count() > 0)
 //			// {
 //			// objx.emitConstants(clinitgen);
 //			// }
 //			for (int n = 0; n < numInits; n++)
 //				clinitgen.invokeStatic(objx.objtype,
 //						Method.getMethod("void __init" + n + "()"));
 //
 //			clinitgen.push(objx.internalName.replace('/', '.'));
 //			clinitgen.invokeStatic(CLASS_TYPE,
 //					Method.getMethod("Class forName(String)"));
 //			clinitgen.invokeVirtual(CLASS_TYPE,
 //					Method.getMethod("ClassLoader getClassLoader()"));
 //			clinitgen.invokeStatic(Type.getType(Compiler.class),
 //					Method.getMethod("void pushNSandLoader(ClassLoader)"));
 //			clinitgen.mark(startTry);
 //			clinitgen.invokeStatic(objx.objtype,
 //					Method.getMethod("void load()"));
 //			clinitgen.mark(endTry);
 //			clinitgen.invokeStatic(VAR_TYPE,
 //					Method.getMethod("void popThreadBindings()"));
 //			clinitgen.goTo(end);
 //
 //			clinitgen.mark(finallyLabel);
 //			// exception should be on stack
 //			clinitgen.invokeStatic(VAR_TYPE,
 //					Method.getMethod("void popThreadBindings()"));
 //			clinitgen.throwException();
 //			clinitgen.mark(end);
 //			clinitgen.visitTryCatchBlock(startTry, endTry, finallyLabel, null);
 //
 //			// end of static init
 //			clinitgen.returnValue();
 //			clinitgen.endMethod();
 //
 //			// end of class
 //			cv.visitEnd();
 //
 //			writeClassFile(objx.internalName, cw.toByteArray());
 //		} catch (UPTRLispReader.ReaderException e) {
 //			CompilerException ex = new CompilerException(sourcePath, e.line, e.getCause());
 //			throw RuntimeExceptionFactory.javaException(ex.getMessage(), null, Arrays.toString(ex.getStackTrace()));
 //		} catch (IOException e) {
 //			throw RuntimeExceptionFactory.javaException(e.getMessage(), null, Arrays.toString(e.getStackTrace()));
 //		} finally {
 //			Var.popThreadBindings();
 //		}
 //		return file;
 //	}
 
 	// Copied from Rascal's InputConverter
 	private final static int STREAM_READ_SEGMENT_SIZE = 8192;
 	private char[] convertReader(Reader reader) {
 		ArrayList<char[]> segments = new ArrayList<char[]>();
 		
 		// Gather segments.
 		int nrOfWholeSegments = -1;
 		int bytesRead;
 		do{
 			char[] segment = new char[STREAM_READ_SEGMENT_SIZE];
 			try {
 			bytesRead = reader.read(segment, 0, STREAM_READ_SEGMENT_SIZE);
 			}
 			catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 			
 			segments.add(segment);
 			++nrOfWholeSegments;
 		}while(bytesRead == STREAM_READ_SEGMENT_SIZE);
 		
 		// Glue the segments together.
 		char[] segment = segments.get(nrOfWholeSegments);
 		char[] input;
 		if(bytesRead != -1){
 			input = new char[(nrOfWholeSegments * STREAM_READ_SEGMENT_SIZE) + bytesRead];
 			System.arraycopy(segment, 0, input, (nrOfWholeSegments * STREAM_READ_SEGMENT_SIZE), bytesRead);
 		}else{
 			input = new char[(nrOfWholeSegments * STREAM_READ_SEGMENT_SIZE)];
 		}
 		for(int i = nrOfWholeSegments - 1; i >= 0; --i){
 			segment = segments.get(i);
 			System.arraycopy(segment, 0, input, (i * STREAM_READ_SEGMENT_SIZE), STREAM_READ_SEGMENT_SIZE);
 		}
 		
 		return input;
 	}
 
 }
