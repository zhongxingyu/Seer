 package ru.aifgi.recognizer.model;
 
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
 
 import ru.aifgi.recognizer.api.Rectangle;
 import ru.aifgi.recognizer.api.graph.Graph;
 import ru.aifgi.recognizer.api.neural_network.NeuralNetwork;
 import ru.aifgi.recognizer.model.viterbi.GraphImpl;
 import ru.aifgi.recognizer.model.viterbi.ViterbiAlgorithm;
 
 /**
  * @author aifgi
  */
 
 // TODO: delete hardcoded numbers
 public class WordRecognizer {
     private static final int STEP = 5;
 
     private final double[][] myInputImage;
     private final Labels myLabels;
     private final NeuralNetwork myNeuralNetwork;
 
     public WordRecognizer(final double[][] inputImage, final Labels labels, final NeuralNetwork neuralNetwork) {
         myInputImage = inputImage;
         myLabels = labels;
         myNeuralNetwork = neuralNetwork;
     }
 
     public String recognize(final Rectangle wordRectangle) {
         final double[][] word = getWord(wordRectangle);
         final Graph graph = buildGraph(word);
         final int[] labels = ViterbiAlgorithm.getLabels(graph);
         return buildRecognitionResult(labels);
     }
 
     private double[][] getWord(final Rectangle wordRectangle) {
         final int x = wordRectangle.getX();
         final int y = wordRectangle.getY();
         final int width = wordRectangle.getWidth();
         final int height = wordRectangle.getHeight();
         final int annInputHeight = 32;
         final double[][] res;
         if (height <= annInputHeight) {
             res = new double[width][annInputHeight];
             final int topMargin = (annInputHeight - height) / 2;
             for (int i = 0; i < width; ++i) {
                 final int lineNumber = i + x;
                 System.arraycopy(myInputImage[lineNumber], y, res[i], topMargin, height);
             }
         }
         else {
             final double scale = ((double) annInputHeight) / height;
             final double newWidth = width * scale;
             res = new double[(int) newWidth + 1][annInputHeight];
             for (int i = 0; i < newWidth; ++i) {
                 for (int j = 0; j < annInputHeight; ++j) {
                     res[i][j] = myInputImage[((int) (i * scale))][((int) (j * scale))];
                 }
             }
         }
         return res;
     }
 
     private Graph buildGraph(final double[][] word) {
         final int width = word.length;
        final Graph graph = new GraphImpl(width / STEP + 1);
         for (int size = STEP; size <= 30; size += STEP) {
             for (int pos = 0; pos < width; pos += STEP) {
                 final double[] outputVector = myNeuralNetwork.computeOutput(getNeuralNetworkInput(word, pos, size));
                 final int from = pos / STEP;
                graph.setWeights(from, getToVertexNumber(graph, from, size), outputVector);
             }
         }
         return graph;
     }
 
    private int getToVertexNumber(final Graph graph, final int from, final int size) {
        final int to = from + size / STEP;
        if (to >= graph.getVertexesNumber()) {
            return graph.getVertexesNumber() - 1;
        }
        return to;
    }

     private double[][] getNeuralNetworkInput(final double[][] word, final int pos, final int size) {
         final double[][] res = new double[32][32];
         final int leftMargin = (32 - size) / 2;
         for (int i = 0; i < size; ++i) {
             for (int j = 0; j < 32; ++j) {
                 res[leftMargin + i][j] = getElement(word, pos + i, j);
             }
         }
         return res;
     }
 
     private double getElement(double[][] word, int i, int j) {
         if (i >= word.length) {
             return 0;
         }
         return word[i][j];
     }
 
     private String buildRecognitionResult(final int[] labels) {
         final StringBuilder builder = new StringBuilder(labels.length);
         for (final int label : labels) {
             builder.append(myLabels.getCharForLabel(label));
         }
         return builder.toString();
     }
 }
