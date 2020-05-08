 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.peterlavalle.util;
 
 import com.google.common.collect.Lists;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Enumeration;
 import java.util.Iterator;
 
 /**
  *
  * @author Peter LaValle
  */
 public final class Util {
 
 	public static <T extends OutputStream> T copyStream(InputStream inputStream, T outputStream) throws IOException {
 
 		final byte[] buffer = new byte[128];
 		int read;
 
 		while ((read = inputStream.read(buffer)) != -1) {
 			outputStream.write(buffer, 0, read);
 		}
 
 		inputStream.close();
 
 		return outputStream;
 	}
 
 	private Util() {
 	}
 
 	public static <T> Iterable<T> toIterable(final Enumeration<T> enumeration) {
		return Lists.newArrayList(toIterable(enumeration));
 	}
 
 	public static <T> Iterator<T> toIterator(final Enumeration<T> enumeration) {
 		return new Iterator<T>() {
 			@Override
 			public boolean hasNext() {
 				return enumeration.hasMoreElements();
 			}
 
 			@Override
 			public T next() {
 				return enumeration.nextElement();
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		};
 	}
 }
