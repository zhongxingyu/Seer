 /*
 
    Copyright 2011 Monits
  
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
 */
 
 
 package com.monits.commons.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 /**
  * Object serialization utils.
  */
 public class SerializationUtils {
 
 	/**
 	 * Utility classes should not have a public or default constructor.
 	 */
 	private SerializationUtils() {
		throw new AssertionError();
 	}
 
 	/**
 	 * Serializes an object into a byte array.
 	 *
 	 * The object is assumed to implement Serializable
 	 * @param obj The object to serialize
 	 * @return
 	 */
 	public static byte[] serializeObject(final Serializable obj) {
 		final ObjectOutputStream out;
 		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 
 		try {
 			out = new ObjectOutputStream(outputStream);
 
 			out.writeObject(obj);
 		} catch (final IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		return outputStream.toByteArray();
 	}
 
 	/**
 	 * Deserializes an object from a byte array.
 	 * @param str The stream from which to read the object.
 	 * @return The deserialized object.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T extends Serializable> T deserializeObject(final byte[] str) {
 		final ObjectInputStream in;
 		final ByteArrayInputStream inputStream = new ByteArrayInputStream(str);
 		final T ret;
 
 		try {
 			in = new ObjectInputStream(inputStream);
 
 			ret = (T) in.readObject();
 		} catch (final Exception e) {
 			throw new RuntimeException(e);
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Clones the given serializable object by serializing and deserializing it into a copy.
 	 * @param object The object being cloned.
 	 * @return A copy of the object.
 	 */
 	public static <T extends Serializable> T clone(final T object) {
 		return SerializationUtils.deserializeObject(SerializationUtils.serializeObject(object));
 	}
 }
