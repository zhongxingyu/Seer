 /*-
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.io;
 
 import java.io.ByteArrayOutputStream;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.SerializationUtils;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 /**
  * Basic implementation of metadata
  */
 public class Metadata implements IMetaData {
 	private static final long serialVersionUID = IMetaData.serialVersionUID;
 
 	private Map<String, ? extends Serializable> metadata;
	private Map<String,int[]> shapes = new HashMap<String,int[]>(1);
 	private Collection<Serializable> userObjects;
 
 	public Metadata() {
 	}
 
 	public Metadata(Map<String, ? extends Serializable> metadata) {
 		this.metadata = metadata;
 	}
 
 	public Metadata(Collection<String> names) {
 		if (names != null) {
 			for (String n : names) {
 				shapes.put(n, null);
 			}
 		}
 	}
 
 	/**
 	 * Set metadata map
 	 * @param metadata
 	 */
 	void setMetadata(Map<String, ? extends Serializable> metadata) {
 		this.metadata = metadata;
 	}
 
 	/**
 	 * Internal use only
 	 * @return metadata map
 	 */
 	Map<String, ? extends Serializable> getInternalMetadata() {
 		return metadata;
 	}
 
 	/**
 	 * Set user objects
 	 * @param objects
 	 */
 	void setUserObjects(Collection<Serializable> objects) {
 		userObjects = objects;
 	}
 
 	/**
 	 * Add name and shape of a dataset to metadata
 	 * @param name
 	 * @param shape (can be null or zero-length)
 	 */
 	void addDataInfo(String name, int... shape) {
 		shapes.put(name, shape == null || shape.length == 0 ? null : shape);
 	}
 
 	@Override
 	public Collection<String> getDataNames() {
 		return Collections.unmodifiableCollection(shapes.keySet());
 	}
 
 	@Override
 	public Map<String, int[]> getDataShapes() {
 		return Collections.unmodifiableMap(shapes);
 	}
 
 	@Override
 	public Map<String, Integer> getDataSizes() {
 		Map<String, Integer> sizes = new HashMap<String, Integer>(1);
 		for (Entry<String, int[]> e : shapes.entrySet()) {
 			int[] shape = e.getValue();
 			if (shape != null && shape.length > 1)
 				sizes.put(e.getKey(), AbstractDataset.calcSize(shape));
 			else
 				sizes.put(e.getKey(), null);
 		}
 		if (sizes.size() > 0) {
 			return Collections.unmodifiableMap(sizes);
 		}
 		return null;
 	}
 
 	@Override
 	public Serializable getMetaValue(String key) throws Exception {
 		return metadata == null ? null : metadata.get(key);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Collection<String> getMetaNames() throws Exception {
 		return metadata == null ? (Collection<String>) Collections.EMPTY_SET : Collections.unmodifiableCollection(metadata.keySet());
 	}
 
 	@Override
 	public Collection<Serializable> getUserObjects() {
 		return userObjects;
 	}
 
 	@Override
 	public IMetaData clone() {
 		Metadata c = null;
 		try {
 			c = (Metadata) super.clone();
 			if (metadata != null) {
 				HashMap<String, Serializable> md = new HashMap<String, Serializable>();
 				c.metadata = md;
 				ByteArrayOutputStream os = new ByteArrayOutputStream(512);
 				for (String k : metadata.keySet()) {
 					Serializable v = metadata.get(k);
 					if (v != null) {
 						SerializationUtils.serialize(v, os);
 						Serializable nv = (Serializable) SerializationUtils.deserialize(os.toByteArray());
 						os.reset();
 						md.put(k, nv);
 					} else {
 						md.put(k, null);
 					}
 				}
 			}
 			c.shapes = new HashMap<String, int[]>(1);
 			for (Entry<String, int[]> e : shapes.entrySet()) {
 				int[] s = e.getValue();
 				c.shapes.put(e.getKey(), s == null ? null : s.clone());
 			}
 		} catch (CloneNotSupportedException e) {
 		}
 		return c;
 	}
 }
