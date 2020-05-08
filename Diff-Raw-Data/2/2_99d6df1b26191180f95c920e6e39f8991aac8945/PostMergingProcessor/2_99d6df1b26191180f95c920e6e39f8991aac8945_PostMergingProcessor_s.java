 /*
  * Copyright (C) 2013 Brockmann Consult GmbH (info@brockmann-consult.de)
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option)
  * any later version.
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see http://www.gnu.org/licenses/
  */
 
 package org.esa.beam.occci.merging;
 
 
 import org.esa.beam.binning.CellProcessor;
 import org.esa.beam.binning.CellProcessorConfig;
 import org.esa.beam.binning.CellProcessorDescriptor;
 import org.esa.beam.binning.VariableContext;
 import org.esa.beam.binning.Vector;
 import org.esa.beam.binning.WritableVector;
 import org.esa.beam.occci.qaa.ImaginaryNumberException;
 import org.esa.beam.occci.qaa.QaaAlgorithm;
 import org.esa.beam.occci.qaa.QaaConstants;
 import org.esa.beam.occci.qaa.QaaResult;
 import org.esa.beam.occci.qaa.SensorConfig;
 import org.esa.beam.occci.qaa.SensorConfigFactory;
 import org.esa.beam.occci.qaa.binning.QaaConfig;
 import org.esa.beam.occci.qaa.binning.QaaDescriptor;
 import org.esa.beam.occci.qaa.binning.ResultMapper;
 import org.esa.beam.occci.util.binning.BinningUtils;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class PostMergingProcessor extends CellProcessor {
 
     private final QaaAlgorithm qaaAlgorithm;
     private final int[] rrsBandIndices;
     private final int[] sensorBandIndices;
     private final float[] rrs;
     private final float[] sensor;
     private final int rrsOffset;
     private final int sensorOffset;
     QaaResult qaaResult;
     private final ResultMapper resultMapper;
 
     public PostMergingProcessor(VariableContext varCtx, QaaConfig qaaConfig) {
         super(createOutputFeatureNames(varCtx, qaaConfig));
 
         final SensorConfig sensorConfig = SensorConfigFactory.get(qaaConfig.getSensorName());
         qaaAlgorithm = new QaaAlgorithm(sensorConfig);
         rrsOffset = 4 * 6;
         sensorOffset = 4 * 6 + 6;
 
         final String[] bandNames = qaaConfig.getBandNames();
         rrsBandIndices = BinningUtils.getBandIndices(varCtx, bandNames);
         sensorBandIndices = BinningUtils.getBandIndices(varCtx, new String[]{"sensor_0", "sensor_1", "sensor_2"});
 
         rrs = new float[rrsBandIndices.length];
         sensor = new float[sensorBandIndices.length];
         qaaResult = new QaaResult();
         resultMapper = new ResultMapper(qaaConfig);
     }
 
     @Override
     public void compute(Vector inputVector, WritableVector outputVector) {
         for (int i = 0; i < rrs.length; i++) {
             rrs[i] = inputVector.get(rrsBandIndices[i]);
             if (Float.isNaN(rrs[i])) {
                 BinningUtils.setToInvalid(outputVector);
                 return;
             }
         }
        for (int i = 0; i < rrs.length; i++) {
             sensor[i] = inputVector.get(sensorBandIndices[i]);
         }
         try {
             qaaResult = qaaAlgorithm.process(rrs, qaaResult);
         } catch (ImaginaryNumberException e) {
             BinningUtils.setToInvalid(outputVector);
             copyRRS(rrs, outputVector);
             copySensorContribution(sensor, outputVector);
             return;
         }
         if (containsInvalid(qaaResult.getA_PIG()) ||
                 containsInvalid(qaaResult.getA_Total()) ||
                 containsInvalid(qaaResult.getA_YS()) ||
                 containsInvalid(qaaResult.getBB_SPM())) {
             BinningUtils.setToInvalid(outputVector);
             copyRRS(rrs, outputVector);
             copySensorContribution(sensor, outputVector);
             return;
         }
         resultMapper.assign(qaaResult, rrs, outputVector);
         copyRRS(rrs, outputVector);
         copySensorContribution(sensor, outputVector);
     }
 
     private void copyRRS(float[] rrs, WritableVector outputVector) {
         for (int i = 0; i < rrs.length; i++) {
             outputVector.set(rrsOffset + i, rrs[i]);
         }
     }
 
     private void copySensorContribution(float[] sensors, WritableVector outputVector) {
         for (int i = 0; i < sensors.length; i++) {
             outputVector.set(sensorOffset + i, sensors[i]);
         }
     }
 
     private static boolean containsInvalid(float[] values) {
         for (float value : values) {
             if (Float.isNaN(value) || Float.isInfinite(value)) {
                 return true;
             }
         }
         return false;
     }
 
     public static class Config extends CellProcessorConfig {
 
         public Config() {
             super(Descriptor.NAME);
         }
 
     }
 
     public static class Descriptor implements CellProcessorDescriptor {
 
         public static final String NAME = "PostMerging";
         private static final int[] ALL_IOPS = new int[]{0, 1, 2, 3, 4, 5};
 
         @Override
         public String getName() {
             return NAME;
         }
 
         @Override
         public CellProcessor createCellProcessor(VariableContext varCtx, CellProcessorConfig cellProcessorConfig) {
             QaaConfig qaaConfig = new QaaConfig();
             qaaConfig.setSensorName(QaaConstants.SEAWIFS);
             qaaConfig.setBandNames(new String[]{"Rrs_412", "Rrs_443", "Rrs_490", "Rrs_510", "Rrs_555", "Rrs_670"});
             qaaConfig.setATotalOutIndices(ALL_IOPS);
             qaaConfig.setBbSpmOutIndices(ALL_IOPS);
             qaaConfig.setAPigOutIndices(ALL_IOPS);
             qaaConfig.setAYsOutIndices(ALL_IOPS);
             qaaConfig.setRrsOut(false);
             return new PostMergingProcessor(varCtx, qaaConfig);
         }
 
         @Override
         public CellProcessorConfig createConfig() {
             return new Config();
         }
     }
 
     private static String[] createOutputFeatureNames(VariableContext varCtx, QaaConfig qaaConfig) {
         String[] outputFeatureNames = QaaDescriptor.createOutputFeatureNames(qaaConfig);
         final ArrayList<String> featureNameList = new ArrayList<String>();
         Collections.addAll(featureNameList, outputFeatureNames);
         Collections.addAll(featureNameList, qaaConfig.getBandNames());
 
         for (int i = 0; i < SensorMerging.SENSORS.length; i++) {
             featureNameList.add("sensor_" + i);
         }
         return featureNameList.toArray(new String[featureNameList.size()]);
     }
 }
