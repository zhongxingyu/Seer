 package org.jackie.jclassfile;
 
 import org.jackie.jclassfile.model.ClassFile;
 import org.testng.annotations.Test;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ByteArrayOutputStream;
 import java.io.ByteArrayInputStream;
 import java.util.List;
 import java.net.URL;
 import java.text.MessageFormat;
 
 /**
  * @author Patrik Beno
  */
 public class ClassFileTest {
 
 	@Test
 	public void generateSimpleClass() throws IOException {
 		final ClassFile cf = new ClassFile();
		cf.classname("test.Simple").superclass("java.lang.Object");
 
 		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		cf.save(new DataOutputStream(baos));
 		final byte[] bytes = baos.toByteArray();
 
 		FileOutputStream out = new FileOutputStream("h:/var/out.class");
 		out.write(bytes);
 		out.close();
 
 		new ClassLoader(null) {{
			Class<?> cls = defineClass(cf.classname(), bytes, 0, bytes.length);
 			System.out.println(cls);
 		}};
 
 	}
 
 
 	static public void main(String[] args) throws IOException {
 //		final Class cls = MessageFormat.class;
 		final Class cls = Sample.class;
 
 		URL url = cls.getResource(cls.getSimpleName() + ".class");
 		byte[] original = new byte[url.openConnection().getContentLength()];
 
 		{
 			DataInputStream in = new DataInputStream(url.openStream());
 			in.readFully(original);
 		}
 
 		{
 			FileOutputStream out = new FileOutputStream("h:/var/original.class");
 			out.write(original);
 			out.close();
 		}
 
 		ClassFile classfile = new ClassFile();
 		classfile.load(new DataInputStream(new ByteArrayInputStream(original)));
 
 		final byte[] bytes;
 		{
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(baos);
 			classfile.save(out);
 			out.close();
 			bytes = baos.toByteArray();
 		}
 
 		{
 			FileOutputStream out = new FileOutputStream("h:/var/out.class");
 			out.write(bytes);
 			out.close();
 		}
 
 		new ClassLoader(null) {{
 				defineClass(cls.getName(), bytes, 0, bytes.length);
 		}}; 
 	}
 }
 
 class Sample<T> {
 	List<T> items;
 	<T> void method(T t) {}
 }
