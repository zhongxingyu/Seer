 package org.jackie.compiler_impl;
 
 import org.jackie.compiler.Compiler;
 import org.jackie.compiler.context.CompilerContext;
 import org.jackie.compiler.extension.ExtensionManager;
 import org.jackie.compiler.filemanager.FileManager;
 import org.jackie.compiler.filemanager.FileObject;
 import org.jackie.compiler.typeregistry.TypeRegistry;
 import org.jackie.compiler_impl.filemanager.MultiFileManager;
 import org.jackie.compiler_impl.javacintegration.JavacCompiler;
 import org.jackie.compiler_impl.jmodelimpl.JClassImpl;
 import org.jackie.compiler_impl.typeregistry.CompilerWorkspaceRegistry;
 import org.jackie.compiler_impl.typeregistry.JClassRegistry;
 import org.jackie.compiler_impl.typeregistry.MultiRegistry;
 import static org.jackie.context.ContextManager.*;
 import org.jackie.jvm.JClass;
 import org.jackie.utils.ClassName;
 import org.jackie.utils.IOHelper;
 
 import java.nio.ByteBuffer;
 import java.nio.channels.WritableByteChannel;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Patrik Beno
  */
 public class CompilerImpl implements Compiler {
 
 	protected FileManager sources;
 
 	protected FileManager workspace;
 
 	protected List<FileManager> dependencies;
 
 	public void compile() {
 		newContext();
 		try {
 			context().set(CompilerContext.class, new CompilerContext());
 			context().set(ExtensionManager.class, new ExtensionManagerImpl());
 
 			compileJavaSources();
 
 			context().set(TypeRegistry.class, new CompilerWorkspaceRegistry(
 					new JClassRegistry(workspace),
 					new MultiRegistry(toRegistryList(dependencies))
 			));
 
 			compileByteCode();
 
 			save();
 
 		} finally {
 			closeContext();
 		}
 	}
 
 	protected void save() {
 	}
 
 	protected List<TypeRegistry> toRegistryList(List<FileManager> dependencies) {
 		List<TypeRegistry> regs = new ArrayList<TypeRegistry>();
 		for (FileManager d : dependencies) {
 			regs.add(new JClassRegistry(d));
 		}
 		return regs;
 	}
 
 	void compileJavaSources() {
 		List<String> options = Arrays.asList("-g"/*, "-source", "1.5", "-target", "1.5"*/);
 		JavacCompiler javac = new JavacCompiler(options,
 				sources, new MultiFileManager(dependencies), workspace);
 		javac.compile();
 	}
 
 	void compileByteCode() {
 		for (JClass jcls : context(TypeRegistry.class).jclasses()) {
 			compile(jcls);
 		}
 	}
 
 	void compile(JClass jcls) {
 		newContext();
 		try {
 			byte[] bytes = ((JClassImpl) jcls).compile();
 
 			ClassName clsname = new ClassName(jcls.getFQName());
 			workspace.remove(clsname.getPathName());
 			FileObject fo = workspace.create(clsname.getPathName());
 
 			WritableByteChannel out = fo.getOutputChannel();
 			IOHelper.write(
 					ByteBuffer.wrap(bytes),
 					out);
 			IOHelper.close(out);
 
 
 		} finally {
 			closeContext();
 		}
 	}
 }
