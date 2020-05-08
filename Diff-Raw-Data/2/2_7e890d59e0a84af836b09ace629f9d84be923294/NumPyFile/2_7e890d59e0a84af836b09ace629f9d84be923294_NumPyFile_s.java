 /*
  * Copyright 2011 Diamond Light Source Ltd.
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
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 public class NumPyFile {
 	/**
 	 * Magic number at the head of the file, includes version number as all numpy files are always version 1.0.
 	 */
 	/*package*/ static byte[] magic = { (byte) 0x93, 'N', 'U', 'M', 'P', 'Y', 0x1, 0x0 };
 
 	/*package*/ static class DataTypeInfo {
 		static DataTypeInfo create() {
 			return new DataTypeInfo();
 		}
 
 		DataTypeInfo setDType(int dType) {
 			this.dType = dType;
 			return this;
 		}
 
 		DataTypeInfo setNumPyType(String numPyType) {
 			this.numPyType = numPyType;
 			return this;
 		}
 
 		DataTypeInfo setISize(int iSize) {
 			this.iSize = iSize;
 			return this;
 		}
 
 		DataTypeInfo setUnsigned(boolean isUnsigned) {
 			unsigned = isUnsigned;
 			return this;
 		}
 
 		String numPyType;
 		int dType;
 		int iSize;
 		boolean unsigned;
 	}
 
 	/*package*/ static Map<String, DataTypeInfo> dataTypeMap = new HashMap<String, DataTypeInfo>();
 	/*package*/ static Map<Integer, DataTypeInfo> numPyTypeMap = new HashMap<Integer, DataTypeInfo>();
 	/*package*/ static Map<Integer, DataTypeInfo> unsignedNumPyTypeMap = new HashMap<Integer, DataTypeInfo>();
 	static {
 		Set<DataTypeInfo> infos = new HashSet<DataTypeInfo>();
 		infos.add(DataTypeInfo.create().setNumPyType("|b1").setDType(AbstractDataset.BOOL).setISize(1));
 		infos.add(DataTypeInfo.create().setNumPyType("|i1").setDType(AbstractDataset.INT8).setISize(1));
 		infos.add(DataTypeInfo.create().setNumPyType("|u1").setDType(AbstractDataset.INT8).setISize(1).setUnsigned(true));
 		infos.add(DataTypeInfo.create().setNumPyType("<i2").setDType(AbstractDataset.INT16).setISize(1));
 		infos.add(DataTypeInfo.create().setNumPyType("<u2").setDType(AbstractDataset.INT16).setISize(1).setUnsigned(true));
 		infos.add(DataTypeInfo.create().setNumPyType("<i4").setDType(AbstractDataset.INT32).setISize(1));
 		infos.add(DataTypeInfo.create().setNumPyType("<u4").setDType(AbstractDataset.INT32).setISize(1).setUnsigned(true));
 		infos.add(DataTypeInfo.create().setNumPyType("<i8").setDType(AbstractDataset.INT64).setISize(1));
 		infos.add(DataTypeInfo.create().setNumPyType("<f4").setDType(AbstractDataset.FLOAT32).setISize(1));
 		infos.add(DataTypeInfo.create().setNumPyType("<f8").setDType(AbstractDataset.FLOAT64).setISize(1));
 		infos.add(DataTypeInfo.create().setNumPyType("<c8").setDType(AbstractDataset.COMPLEX64).setISize(2));
 		infos.add(DataTypeInfo.create().setNumPyType("<c16").setDType(AbstractDataset.COMPLEX128).setISize(2));
 		
 		for (DataTypeInfo dataTypeInfo : infos) {
 			dataTypeMap.put(dataTypeInfo.numPyType, dataTypeInfo);
 			if (dataTypeInfo.unsigned)
 				unsignedNumPyTypeMap.put(dataTypeInfo.dType, dataTypeInfo);
 			else
 				numPyTypeMap.put(dataTypeInfo.dType, dataTypeInfo);
 		}
 	}
 
 }
