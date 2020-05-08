 /*
  * (C) Copyright 2009-2010 Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * Contributors:
  *   Bogdan Stefanescu (bs@nuxeo.com), Nuxeo
  *   Stefane Fermigier (sf@nuxeo.com), Nuxeo
  *   Florent Guillaume (fg@nuxeo.com), Nuxeo
  */
 
 package org.apache.chemistry.shell.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.chemistry.opencmis.client.api.CmisObject;
 import org.apache.chemistry.opencmis.client.api.Document;
 import org.apache.chemistry.opencmis.client.api.Property;
 import org.apache.chemistry.opencmis.commons.data.ContentStream;
 import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
 import org.apache.chemistry.shell.app.Console;
 import org.apache.chemistry.shell.command.CommandException;
 
 public class SimplePropertyManager {
 
 	private final class PropertyComparator implements Comparator<Property<?>> {
 
 		public int compare(Property<?> p1, Property<?> p2) {
 			return p1.getId().compareTo(p2.getId());
 		}
 	}
 
 	protected final CmisObject item;
 
 	public SimplePropertyManager(CmisObject item) {
 		this.item = item;
 	}
 
 	public String getPropertyAsString(String name) {
 		Property<Object> p = item.getProperty(name);
 		if (p == null) {
 			return "[null]";
 		}
 		Serializable val = p.getValue();
 		return val != null ? val.toString() : "[null]";
 	}
 
 	public void setProperty(String name, String value) throws Exception {
 		Map<String, String> properties = new HashMap<String, String>();
 		properties.put(name, value);
 		item.updateProperties(properties);
 	}
 
 	public void dumpProperties() {
		List<Property<?>> props = new LinkedList<Property<?>>(
				item.getProperties());
 		Collections.sort(props, new PropertyComparator());
 
 		for (Property<?> prop : props) {
 			Object value = prop.getValue();
 			Console.getDefault().println(
 					prop.getId() + " = " + (value != null ? value : "[null]"));
 		}
 	}
 
 	public ContentStream getStream() throws IOException, CommandException {
 		if (item instanceof Document) {
 			Document doc = (Document) item;
 			return doc.getContentStream();
 		} else {
 			throw new CommandException(
 					"Target object is not a Document, can not get stream");
 		}
 	}
 
 	public void setStream(InputStream in, long filesize, String name)
 			throws Exception {
 		if (item instanceof Document) {
 			Document doc = (Document) item;
 			String mimeType = MimeTypeHelper.getMimeType(name);
 			ContentStream stream = new ContentStreamImpl(name,
 					BigInteger.valueOf(filesize), mimeType, in);
 			doc.setContentStream(stream, true);
 		} else {
 			throw new CommandException(
 					"Target object is not a Document, can not set stream");
 		}
 	}
 
 }
