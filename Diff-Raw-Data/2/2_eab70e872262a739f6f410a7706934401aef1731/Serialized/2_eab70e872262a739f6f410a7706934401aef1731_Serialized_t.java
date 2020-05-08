 /*
  * Copyright (c) 2008 Bradley W. Kimmel
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.eandb.util.rmi;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import ca.eandb.util.UnexpectedException;
 import ca.eandb.util.io.AlternateClassLoaderObjectInputStream;
 
 /**
  * Represents an object that is serialized.  When constructed using the
  * constructor, the object remains deserialized.  However, when constructed
  * by deserialization, it remains in a serialized state until explicitly
  * asked to deserialize (via the {@link #deserialize()} method).
  * @author Brad Kimmel
  */
 public final class Serialized<T> implements Serializable {
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = -6120896446795084977L;
 
 	/** The serialized object. */
 	private final byte[] data;
 
 	/** The deserialized object. */
 	private transient T object;
 
 	/**
 	 * Initializes the <code>Serialized</code> object.
 	 * @param obj The object to be serialized.
 	 */
 	public Serialized(T obj) {
 		this.object = obj;
 		try {
 			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
 			GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream);
 			ObjectOutputStream objectStream = new ObjectOutputStream(gzipStream);
 			objectStream.writeObject(obj);
 			objectStream.flush();
 			gzipStream.finish();
 			data = byteStream.toByteArray();
 			objectStream.close();
 		} catch (IOException e) {
 			throw new UnexpectedException(e);
 		}
 	}
 
 	/**
 	 * Determines if the object has been deserialized.
 	 * @return A value indicating if the object has been deserialized.
 	 */
 	public boolean isDeserialized() {
 		return (object != null);
 	}
 
 	/**
 	 * Returns the deserialized object.
 	 * @return The deserialized object.
 	 * @throws IllegalStateException If the object has not been deserialized.
 	 */
 	public T get() {
 		if (!isDeserialized()) {
 			throw new IllegalStateException("Object not deserialized.");
 		}
 		return object;
 	}
 
 	/**
 	 * Returns the deserialized object.
 	 * @param deserialize A value indicating whether the object should be
 	 * 		deserialized if it is not already.
 	 * @return The deserialized object.
 	 * @throws ClassNotFoundException If a required class could not be found
 	 * 		during deserialization.
 	 * @throws IllegalStateException If the object has not been deserialized
 	 * 		and <code>deserialize</code> is false.
 	 */
 	public T get(boolean deserialize) throws ClassNotFoundException {
 		if (!isDeserialized()) {
 			if (deserialize) {
 				deserialize();
 			} else {
 				throw new IllegalStateException("Object not deserialized.");
 			}
 		}
 		return object;
 	}
 
 	/**
 	 * Deserializes the serialized object.
 	 * @return The deserialized object.
 	 * @throws ClassNotFoundException If a required class could not be found
 	 * 		during deserialization.
 	 */
 	@SuppressWarnings("unchecked")
 	public T deserialize() throws ClassNotFoundException {
 		try {
 			ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
 			GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
 			try {
 				ObjectInputStream objectStream = new ObjectInputStream(gzipStream);
 				object = (T) objectStream.readObject();
 				return object;
 			} finally {
 				gzipStream.close();
 			}
 		} catch (IOException e) {
 			throw new UnexpectedException(e);
 		}
 	}
 
 	/**
 	 * Deserializes the serialized object, using the specified
 	 * <code>ClassLoader</code> to load classes as necessary.
 	 * @param loader The <code>ClassLoader</code> to use to load classes.
 	 * @return The deserialized object.
 	 * @throws ClassNotFoundException If a required class could not be found
 	 * 		during deserialization.
 	 */
 	@SuppressWarnings("unchecked")
 	public T deserialize(ClassLoader loader) throws ClassNotFoundException {
 		try {
 			ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
 			GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
 			try {
 				ObjectInputStream objectStream = new AlternateClassLoaderObjectInputStream(
						gzipStream, loader);
 				object = (T) objectStream.readObject();
 				return object;
 			} finally {
 				gzipStream.close();
 			}
 		} catch (IOException e) {
 			throw new UnexpectedException(e);
 		}
 	}
 
 }
