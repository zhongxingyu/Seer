 /**
  *
  */
 package org.selfip.bkimmel.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.DigestOutputStream;
 import java.security.MessageDigest;
 
 import org.selfip.bkimmel.io.NullOutputStream;
 import org.selfip.bkimmel.io.StreamUtil;
 
 /**
  * Utility methods for working with classes.
  * @author brad
  */
 public final class ClassUtil {
 
 	/**
 	 * Gets an <code>InputStream</code> from which the specified class'
 	 * bytecode definition may be read.
 	 * @param cl The <code>Class</code> for which to get the stream.
 	 * @return An <code>InputStream</code> from which the specified class'
 	 * 		bytecode definition may be read.
 	 */
 	public static InputStream getClassAsStream(Class<?> cl) {
		String resourceName = cl.getSimpleName() + ".class";
 		return cl.getResourceAsStream(resourceName);
 	}
 
 	/**
 	 * Computes a digest from a class' bytecode.
 	 * @param cl The <code>Class</code> for which to compute the digest.
 	 * @param digest The <code>MessageDigest</code> to update.
 	 */
 	public static void getClassDigest(Class<?> cl, MessageDigest digest) {
 		DigestOutputStream out = new DigestOutputStream(NullOutputStream.getInstance(), digest);
 		try {
 			writeClassToStream(cl, out);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new UnexpectedException(e);
 		}
 	}
 
 	/**
 	 * Writes a class' bytecode to an <code>OutputStream</code>.
 	 * @param cl The <code>Class</code> to write.
 	 * @param out The <code>OutputStream</code> to write to.
 	 * @throws IOException If unable to write to <code>out</code>.
 	 */
 	public static void writeClassToStream(Class<?> cl, OutputStream out) throws IOException {
 		InputStream in = getClassAsStream(cl);
 		StreamUtil.writeStream(in, out);
 	}
 
 	/**
 	 * Gets the outer class (a class with no enclosing class) that contains the
 	 * given class.
 	 * @param cl The <code>Class</code> for which to find the outer class.  If
 	 * 		<code>class_</code> is an outer class, then <code>class_</code> is
 	 * 		returned.
 	 * @return The outer <code>Outer</code> class containing
 	 * 		<code>class_</code>.
 	 */
 	public static Class<?> getOuterClass(Class<?> cl) {
 		Class<?> enclosingClass;
 		while ((enclosingClass = cl.getEnclosingClass()) != null) {
 			cl = enclosingClass;
 		}
 		return cl;
 	}
 
 	/** Declared private to prevent this class from being instantiated. */
 	private ClassUtil() {}
 
 }
