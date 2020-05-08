 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is Gmodel.
  *
  * The Initial Developer of the Original Code is
  * Sofismo AG (Sofismo).
  * Portions created by the Initial Developer are
  * Copyright (C) 2009-2011 Sofismo AG.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Chul Kim
 * Andrew Shewring
 * 
  * ***** END LICENSE BLOCK ***** */
 
 package org.s23m.cell.serialization.serializer;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
/**
 * See
 * 
 * http://cmaki.blogspot.com/2007/09/annotated-jaxb-classes.html?showComment=1230640440000#c8002831406668901813
 * 
 * for comments on correct JAXBContext initialisation
 */
 public class SerializationMarshaller {
 
 	private SerializationMarshaller() {
 	}
 
 	private static class MarshallerHolder {
 		public static final Map<String,Marshaller> MARSHALLER_MAP = new HashMap<String, Marshaller>();
 		public static final Map<String,Unmarshaller> UN_MARSHALLER_MAP = new HashMap<String, Unmarshaller>();
 	}
 
 	protected static Marshaller getMarshaller(final String packageName) throws JAXBException {
 		if (MarshallerHolder.MARSHALLER_MAP.containsKey(packageName)) {
 			return MarshallerHolder.MARSHALLER_MAP.get(packageName);
 		} else {
			final JAXBContext jaxbContext = JAXBContext.newInstance(packageName, SerializationMarshaller.class.getClassLoader());
			final Marshaller marshaller = jaxbContext.createMarshaller();
 			marshaller.setProperty(Marshaller.JAXB_ENCODING, Serializer.CONTENT_ENCODING);
 			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 			MarshallerHolder.MARSHALLER_MAP.put(packageName, marshaller);
 			return marshaller;
 		}
 	}
 
 	protected static Unmarshaller getUnmarshaller(final String packageName) throws JAXBException {
 		if (MarshallerHolder.UN_MARSHALLER_MAP.containsKey(packageName)) {
 			return MarshallerHolder.UN_MARSHALLER_MAP.get(packageName);
 		} else {
			final JAXBContext jcx = JAXBContext.newInstance(packageName, SerializationMarshaller.class.getClassLoader());
 			final Unmarshaller unMarsahller = jcx.createUnmarshaller();
 			MarshallerHolder.UN_MARSHALLER_MAP.put(packageName, unMarsahller);
 			return unMarsahller;
 		}
 	}
 
 }
