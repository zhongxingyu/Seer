 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.application.arraydata;
 
 import gov.nih.nci.caintegrator.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator.file.FileManager;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import ucar.ma2.Array;
 import ucar.ma2.DataType;
 import ucar.ma2.Index;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.NetcdfFileWriteable;
 
 /**
  * Provides NetCDF formatting and storage capabilities.
  */
 class NetCDFWriter extends AbstractNetCdfFileHandler {
 
     private final ArrayDataValues values;
     private NetcdfFileWriteable writer;
 
     NetCDFWriter(ArrayDataValues values, FileManager fileManager) {
         super(fileManager);
         this.values = values;
     }
 
     void storeValues() {
         openNetCdfFileWriteable();
         writeValues();
         closeNetCdfFileWriteable();
     }
 
     private void openNetCdfFileWriteable() {
         File file = getFile(values);
         if (file.exists()) {
             openNetCdfFileWriteable(file);
         } else {
             createNetCdfFile(file);
         }
     }
 
     private void openNetCdfFileWriteable(File file) {
         try {
             writer = NetcdfFileWriteable.openExisting(file.getAbsolutePath(), false);
         } catch (IOException e) {
             throw new ArrayDataStorageException("Couldn't open the NetCDF file for writing", e);
         }
     }
 
     private void createNetCdfFile(File file) {
         try {
            file.getParentFile().mkdirs();
             writer = NetcdfFileWriteable.createNew(file.getAbsolutePath(), true);
             Dimension reporterDimension = writer.addDimension(REPORTER_DIMENSION_NAME, values.getReporters().size());
             Dimension arrayDataDimension = writer.addUnlimitedDimension(ARRAY_DATA_DIMENSION_NAME);
             Dimension[] dimensions = new Dimension[] {arrayDataDimension, reporterDimension};
             for (ArrayDataValueType valueType : values.getTypes()) {
                 writer.addVariable(valueType.name(), getDataType(valueType), dimensions);
             }
             writer.addVariable(ARRAY_DATA_IDS_VARIABLE, DataType.INT, new Dimension[] {arrayDataDimension});
             writer.create();
         } catch (IOException e) {
             throw new ArrayDataStorageException("Couldn't create the NetCDF file", e);
         }
     }
 
     private void closeNetCdfFileWriteable() {
         try {
             writer.close();
         } catch (IOException e) {
             throw new ArrayDataStorageException("Couldn't close the NetCDF file.", e);
         }
     }
 
     private void writeValues() {
         try {
             int[] shape = new int[] {1, values.getReporters().size()};
             Array arrayDataIdArray = Array.factory(DataType.INT, new int[] {1});
             int[] valuesOrigin = new int[] {0, 0};
             int[] arrayIdOrigin = new int[] {0};
             List<ArrayData> arrayDatas = values.getOrderedArrayDatas();
             for (ArrayData arrayData : arrayDatas) {
                 writeArrayDataValues(shape, arrayDataIdArray, valuesOrigin, arrayIdOrigin, arrayData);
             }
         } catch (IOException e) {
             throw new ArrayDataStorageException("Couldn't writes values. IOException.", e);
         } catch (InvalidRangeException e) {
             throw new ArrayDataStorageException("Couldn't writes values. InvalidRangeException", e);
         }
     }
 
     private void writeArrayDataValues(int[] shape, Array arrayDataIdArray, int[] valuesOrigin, int[] arrayIdOrigin,
             ArrayData arrayData) throws IOException, InvalidRangeException {
         valuesOrigin[0] = getArrayDataOffset(arrayData);
         if (!getArrayDataOffsets().containsKey(arrayData.getId())) {
             arrayIdOrigin[0] = getArrayDataOffset(arrayData);
             arrayDataIdArray.setLong(arrayDataIdArray.getIndex(), arrayData.getId());
             writer.write(ARRAY_DATA_IDS_VARIABLE, arrayIdOrigin, arrayDataIdArray);
         }
         for (ArrayDataValueType valueType : values.getTypes()) {
             Array valuesArray = Array.factory(valueType.getTypeClass(), shape);
             Index valuesIndex = valuesArray.getIndex();
             for (AbstractReporter reporter : values.getReporters()) {
                 valuesIndex.set(0, reporter.getDataStorageIndex());
                 setValue(valuesArray, valuesIndex, arrayData, reporter, valueType);
             }
             writer.write(valueType.name(), valuesOrigin, valuesArray);
         }
     }
 
     private void setValue(Array valuesArray, Index valuesIndex, ArrayData arrayData, AbstractReporter reporter, 
             ArrayDataValueType type) {
         if (Float.class.equals(type.getTypeClass())) {
             valuesArray.setFloat(valuesIndex, values.getFloatValue(arrayData, reporter, type));
         } else {
             throw new ArrayDataStorageException("Unsupported data type " + type.getTypeClass().getName());
         }
 
     }
 
     private int getArrayDataOffset(ArrayData arrayData) throws IOException {
         if (getArrayDataOffsets().containsKey(arrayData.getId())) {
             return getArrayDataOffsets().get(arrayData.getId());
         } else {
             return writer.findDimension(ARRAY_DATA_DIMENSION_NAME).getLength();
         }
     }
 
     @Override
     NetcdfFile getNetCdfFile() {
         return writer;
     }
 
 }
