 package ru.aifgi.recognizer.model.neural_network.stages;
 
 /*
  * Copyright 2012 Alexey Ivanov
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import com.google.common.base.Preconditions;
 import ru.aifgi.recognizer.api.neural_network.NeuralNetworkOutput;
 import ru.aifgi.recognizer.model.neural_network.layers.TwoDimensionalLayer;
 import ru.aifgi.recognizer.model.neural_network.layers.impl.ConvolutionalLayer;
 import ru.aifgi.recognizer.model.neural_network.layers.impl.SubsamplingLayer;
 
 import java.io.Serializable;
 
 /**
  * @author aifgi
  */
 
 public class ConvolutionalStage implements Stage {
     private static class LayerPair implements Serializable {
         ConvolutionalLayer convolutional;
         SubsamplingLayer subsampling;
     }
 
     private final LayerPair[] myLayers;
     private final int[][] myMask;
     private final int mySubsamplingLayerSize;
 
     public ConvolutionalStage(final int size, final int[][] mask,
                               final int convolutionalMask, final int subsamplingMask,
                               final int subsamplingLayerSize) {
         myLayers = new LayerPair[size];
         myMask = mask;
         final int length = myLayers.length;
         for (int i = 0; i < length; i++) {
             final LayerPair layerPair = new LayerPair();
             layerPair.convolutional = new ConvolutionalLayer(convolutionalMask);
             layerPair.subsampling = new SubsamplingLayer(subsamplingMask);
             myLayers[i] = layerPair;
         }
         mySubsamplingLayerSize = subsamplingLayerSize;
     }
 
     @Override
     public void forwardComputation(final NeuralNetworkOutput neuralNetworkOutput) {
         final int length = myLayers.length;
         final double[][][] conv = new double[length][][];
         final double[][][] sub = new double[length][][];
         final double[][][] input3d = neuralNetworkOutput.get3d();
         for (int i = 0; i < length; ++i) {
             final LayerPair layerPair = myLayers[i];
             conv[i] = layerPair.convolutional.computeOutput(computeInput(i, input3d));
             sub[i] = layerPair.subsampling.computeOutput(conv[i]);
         }
         neuralNetworkOutput.pushBack(conv);
         neuralNetworkOutput.pushBack(sub);
     }
 
     private double[][] computeInput(final int layerIndex, double[][][] input3d) {
         final int length = input3d[0].length;
         final double[][] res = new double[length][length];
         final int[] mask = myMask[layerIndex];
         for (int i = 0; i < length; ++i) {
             for (int j = 0; j < length; ++j) {
                 for (int k = 0; k < mask.length; ++k) {
                     res[i][j] += input3d[k][i][j];
                 }
             }
         }
         return res;
     }
 
     // TODO: rewrite
     @Override
     public void backwardComputation(final NeuralNetworkOutput networkOutput, final NeuralNetworkOutput errors) {
         double[][][] errors3d = errors.get3d(mySubsamplingLayerSize);
         double[][][] layerOutput3d = networkOutput.get3d();
         networkOutput.previous();
         double[][][] layerInput3d = networkOutput.get3d();
         final int length = myLayers.length;
         final double[][][] newErrors = new double[length][][];
         Preconditions.checkState(length == errors3d.length);
         for (int i = 0; i < length; ++i) {
             final TwoDimensionalLayer layer = myLayers[i].subsampling;
             final double[][] gradients = layer.computeGradients(layerOutput3d[i], errors3d[i]);
             newErrors[i] = layer.backPropagation(gradients);
             final double[][] deltas = layer.computeDeltas(layerInput3d[i], gradients, 0.1);
             layer.updateWeights(deltas);
         }
         errors.pushFront(newErrors);
 
 
         errors3d = newErrors;
         layerOutput3d = layerInput3d;
         networkOutput.previous();
         layerInput3d = networkOutput.get3d();
         for (int i = 0; i < length; ++i) {
             final TwoDimensionalLayer layer = myLayers[i].convolutional;
             final double[][] gradients = layer.computeGradients(layerOutput3d[i], errors3d[i]);
             newErrors[i] = layer.backPropagation(gradients);
             final double[][] deltas = layer.computeDeltas(computeInput(i, layerInput3d), gradients, 0.1);
             layer.updateWeights(deltas);
         }
         errors.pushFront(formErrors(newErrors));
     }
 
     private double[][][] formErrors(final double[][][] newErrors) {
        int maxLayer = Integer.MIN_VALUE;
        for (final int[] line : myMask) {
            for (final int value : line) {
                if (value > maxLayer) {
                    maxLayer = value;
                }
            }
        }

        final int length = newErrors[0].length;
        final double[][][] errors = new double[maxLayer + 1][length][length];
        final int maskLength = myMask.length;
        for (int i = 0; i < maskLength; ++i) {
            final int[] ints = myMask[i];
            for (final int layer : ints) {
                for (int x = 0; x < length; ++x) {
                    for (int y = 0; y < length; ++y) {
                        errors[layer][x][y] += newErrors[i][x][y];
                    }
                }
             }
         }
 
        return errors;
     }
 }
