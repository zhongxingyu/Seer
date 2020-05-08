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
 
 package org.apache.chemistry.shell.app;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.chemistry.opencmis.client.api.CmisObject;
 import org.apache.chemistry.opencmis.client.api.Folder;
 import org.apache.chemistry.opencmis.client.api.ItemIterable;
 import org.apache.chemistry.opencmis.client.api.Session;
 import org.apache.chemistry.shell.util.ColorHelper;
 import org.apache.chemistry.shell.util.Path;
 
 public class ChemistryContext extends AbstractContext {
 
 	// public static final String CONN_KEY = "chemistry.connection";
 
 	protected final Session session;
 	protected final CmisObject entry;
 
 	protected List<String> keys;
 	protected List<String> ls;
 	protected Map<String, CmisObject> children;
 
 	public ChemistryContext(ChemistryApp app, Path path, Session session,
 			CmisObject entry) {
 		super(app, path);
 		this.session = session;
 		this.entry = entry;
 	}
 
 	@Override
 	public ChemistryApp getApplication() {
 		return (ChemistryApp) app;
 	}
 	
 	public Session getSession() {
 		return session;
 	}
 
 	public Context getContext(String name) {
 		load();
 		CmisObject e = children.get(name);
 		if (e != null) {
 			return new ChemistryContext((ChemistryApp) app, path.append(name),
 					session, e);
 		}
 		return null;
 	}
 
 	public String[] ls() {
 		load();
 		return ls.toArray(new String[ls.size()]);
 	}
 
 	public String[] entries() {
 		load();
 		return keys.toArray(new String[keys.size()]);
 	}
 
 	public void reset() {
 		children = null;
 		keys = null;
 		ls = null;
 	}
 
 	public boolean isFolder() {
 		return entry instanceof Folder;
 	}
 
 	protected void load() {
 		if (children == null) {
 			if (!isFolder()) {
 				return;
 			}
 			Folder folder = (Folder) entry;
 			ItemIterable<CmisObject> feed = folder.getChildren();
 			children = new LinkedHashMap<String, CmisObject>();
			if (keys != null) {
 				keys.clear();
			}
 			keys = new LinkedList<String>();
 			ls = new LinkedList<String>();
 			for (CmisObject entry : feed) {
 				children.put(entry.getName(), entry);
 				keys.add(entry.getName());
 				String entryTypeId = entry.getType().getId();
 				if (entryTypeId.startsWith("cmis:")) {
 					entryTypeId = entryTypeId.substring("cmis:".length());
 				}
 				ls.add(ColorHelper.decorateNameByType(entry.getName(),
 						entryTypeId));
 			}
 		}
 	}
 
 	public <T> T as(Class<T> type) {
 		if (type.isAssignableFrom(entry.getClass())) {
 			return type.cast(entry);
 		}
 		return null;
 	}
 
 	public String id() {
 		return "Object " + entry.getId() + " of type " + entry.getType().getId();
 	}
 
 }
