 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
 import java.io.*;
 import java.util.*;
 import java.util.jar.*;
 import org.apache.bcel.*;
 import org.apache.bcel.Repository;
 import org.apache.bcel.classfile.*;
 import org.apache.bcel.generic.*;
 import org.apache.bcel.verifier.*;
 import org.apache.bcel.util.*;
 
 class Main{
 	static boolean loud = false;
 
 	public static void main(String[] args) throws Exception{
 		ArrayList<String> classes = new ArrayList<String>();
 		ArrayList<String> jars = new ArrayList<String>();
 		SyntheticRepository synthRepo = SyntheticRepository.getInstance();
 
 		for(String arg : args){
 			if(arg.equals("-loud")){
 				loud = true;
 				continue;
 			}
 			if(arg.endsWith(".class")){
 				classes.add(arg);
 				preload(arg);
 			}else if(arg.endsWith(".jar")){
 				jars.add(arg);
 				synthRepo = SyntheticRepository.getInstance(
 					new ClassPath(synthRepo.getClassPath(), arg));
 				Repository.setRepository(synthRepo);
 			}else{
 				System.err.printf("I don't know what \"%s\" is. It should be a class or jar file.\n", arg);
 				System.exit(1);
 			}
 		}
 
 		for(String j : jars){
 			JarFile jar = new JarFile(j);
 			inject(jar);
			overwrite(j);
 		}
 
		for(String cf : classes){
 			inject(cf);
			overwrite(cf);
		}
 	}
 
 	private static void inject(JarFile jar) throws Exception{
 		InputStream in = null;
 		JarOutputStream out = null;
 		try{
 			out = new JarOutputStream(
 				new FileOutputStream(jar.getName()+".new"),
 				jar.getManifest());
 			for(JarEntry entry : Collections.list(jar.entries())){
 				String name = entry.getName();
 				if(entry.isDirectory() || name.startsWith("META-INF/MANIFEST"))
 					continue;
 
 				try{
 					in = jar.getInputStream(entry);
 					out.putNextEntry(new JarEntry(name));
 
 					if(name.endsWith(".class"))
 						inject(in, out, entry.getName());
 					else
 						copy(in, out);
 				}finally{
 					if(in != null) in.close();
 				}
 			}
 		}finally{
 			if(out != null) out.close();
 		}
 	}
 
 	private static void preload(String fname) throws Exception{
 		Repository.addClass(load(fname));
 	}
 
 	private static void inject(String name) throws Exception{
 		InputStream in = null;
 		OutputStream out = null;
 		try{
 			in = new BufferedInputStream(new FileInputStream(name));
 			out = new BufferedOutputStream(new FileOutputStream(name+".new"));
 			inject(in, out, name);
 		}finally{
 			if(in != null) in.close();
 			if(out != null) out.close();
 		}
 	}
 
 	private static void inject(InputStream in, OutputStream out, String name) throws Exception{
 		JavaClass jc = load(in, name);
 		if(jc.isInterface())
 			return;
 
 		Method[] methods = jc.getMethods();
 		ConstantPoolGen cp = new ConstantPoolGen(jc.getConstantPool());
 		InstructionFactory insf = new InstructionFactory(cp);
 
 		for(int i = 0; i < methods.length; i++){
 			if(methods[i].isNative() || methods[i].isAbstract())
 				continue;
 
 			if(loud)
 				System.err.printf("Injecting %s.%s\n", jc.getClassName(), methods[i]);
 
 			MethodGen mg = new MethodGen(methods[i], jc.getClassName(), cp);
 			boolean changed = new Injector(cp, insf, mg).inject();
 			methods[i] = mg.getMethod();
 
 			if(loud && changed){
 				System.out.println(methods[i]);
 				System.out.println(methods[i].getCode());
 			}
 		}
 
 		jc.setConstantPool(cp.getFinalConstantPool());
 		jc.dump(out);
 	}
 
 	private static JavaClass load(InputStream in, String name) throws Exception{
 		return new ClassParser(in, name).parse();
 	}
 
 	private static JavaClass load(String fname) throws Exception{
 		InputStream file = null;
 		try{
 			file = new BufferedInputStream(new FileInputStream(fname));
 			return load(file, fname);
 		}catch(FileNotFoundException e){
 			System.err.printf("I failed to open \"%s\": %s\n",
 				fname, e.getLocalizedMessage());
 			System.exit(1);
 		}finally{
 			if(file != null) file.close();
 		}
 		throw new AssertionError("Shouldn't get here");
 	}
 
 	private static void copy(InputStream in, OutputStream out) throws Exception{
 		byte[] buf = new byte[1024];
 		while(true){
 			int n = in.read(buf);
 			if(n == -1)
 				break;
 			out.write(buf, 0, n);
 		}
 	}

	private static void overwrite(String fname) throws Exception{
		File orig = new File(fname);
		File mod = new File(fname + ".new");
		orig.delete();
		mod.renameTo(orig);
	}
 }
