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
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 
 public class DiffractionMetadata extends ExtendedMetadataAdapter implements IDiffractionMetadata {
 	private static final long serialVersionUID = IMetaData.serialVersionUID;
 
 	private final DetectorProperties props, oProps;
 	private final DiffractionCrystalEnvironment env, oEnv;
 	private Map<String, ? extends Serializable> metadata;
 	private Date date;
 	private static List<String> dataNames = new ArrayList<String>(1);
 	private final Map<String,int[]> shapes = new HashMap<String,int[]>(1);
 
 	public DiffractionMetadata(String filename, DetectorProperties props, DiffractionCrystalEnvironment env) {
 		super(new File(filename));
 		oProps = props;
 		this.props = oProps == null ? null : oProps.clone();
 		oEnv = env;
 		this.env = oEnv == null ? null : oEnv.clone();
 	}
 
 	void setCreationDate(Date date) {
 		this.date = date;
 	}
 
 	void setMetadata(Map<String, ? extends Serializable> metadata) {
 		this.metadata = metadata;
 	}
 
 	void setImageInfo(String imageName, int s1, int s2) {
 		dataNames.add(0, imageName);
 		shapes.put(imageName, new int[] {s2, s1});
 	}
 
 	@Override
 	public Date getCreation() {
 		return date;
 	}
 
 	@Override
 	public Collection<String> getDataNames() {
 		return Collections.unmodifiableCollection(dataNames);
 	}
 
 	@Override
 	public Map<String, int[]> getDataShapes() {
 		return Collections.unmodifiableMap(shapes);
 	}
 
 	@Override
 	public Map<String, Integer> getDataSizes() {
 		Map<String, Integer> sizes = new HashMap<String, Integer>(1);
 		if (dataNames.size() > 0) {
 			String name = dataNames.get(0);
 			int[] shape = shapes.get(name);
 			sizes.put(name, shape[0] * shape[1]);
 			return Collections.unmodifiableMap(sizes);
 		}
 		return null;
 	}
 
 	@Override
 	public String getMetaValue(String key) throws Exception {
 		return metadata.get(key).toString();
 	}
 
 	@Override
 	public Collection<String> getMetaNames() throws Exception {
 		return Collections.unmodifiableCollection(metadata.keySet());
 	}
 
 	@Override
 	public DetectorProperties getDetector2DProperties() {
 		return props;
 	}
 
 	@Override
 	public DiffractionCrystalEnvironment getDiffractionCrystalEnvironment() {
 		return env;
 	}
 
 	@Override
 	public DetectorProperties getOriginalDetector2DProperties() {
 		return oProps;
 	}
 
 	@Override
 	public DiffractionCrystalEnvironment getOriginalDiffractionCrystalEnvironment() {
 		return oEnv;
 	}
 
 	@Override
 	public IDiffractionMetadata clone() {
 		DiffractionMetadata c = new DiffractionMetadata(getFileName(), oProps, env);
 		c.setCreationDate(date);
 		c.setMetadata(metadata);
 		if (dataNames.size() > 0) {
 			String name = dataNames.get(0);
 			int[] shape = shapes.get(name);
			if(shape.length>0)
 				c.setImageInfo(name, shape[0], shape[1]);
 		}
 		return c;
 	}
 }
