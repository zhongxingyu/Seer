 package topicmodels;
 
 import util.Index;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Random;
 
 abstract public class Sampler implements Serializable {
 
     // initialize some arrays for storing counts
     protected int[] typeCounts;
     protected int[][] typeTopicCounts;
     protected int[] topicCounts;
     protected int[][] wordTopicCounts;
 
     // hyper-parameters
     public double alpha;
     public double beta;
     public double gamma;
     public double betaSum;
     public double gammaSum;
 
     // statistics
     public int numTopics;
     public int numTypes;
     public int numWords;
 
     public Random random;
 
     // Indexes
     public Index wordIndex;
     public Index labelIndex;
     public Index typeIndex;
 
     public int[] sample (int word, ArrayList<Integer> labels, ArrayList<Integer> types, int[] docTypeCounts) {
         double[][] topicTermScores = new double[labels.size()][types.size()];
         double sum = 0.0;
         for (int i = 0; i < types.size(); i++) {
             int type = types.get(i);
             for (int j = 0; j < labels.size(); j++) {
                 int topic = labels.get(j);
                 double score = (gamma + docTypeCounts[type]) * // P(T|D)
                                (beta + wordTopicCounts[word][topic]) / (betaSum + topicCounts[topic]) * // P(w|t)
                                (gamma + typeTopicCounts[type][topic]) / (gammaSum + typeCounts[type]);  // P(t|T)
                 sum += score;
                 topicTermScores[j][i] = score;
             }
         }
         double sample = Math.random() * sum;
         int topic = -1; int type = 0;
         while (sample > 0.0) {
             topic++;
             if (topic == labels.size()) {
                 type++;
                 topic = 0;
             }
             sample -= topicTermScores[topic][type];
         }
         if (topic == -1) {
             throw new IllegalStateException("No topic sampled.");
         }
        if (type == -1) {
            throw new IllegalStateException("No type sampled");
        }
         return new int[]{word, labels.get(topic), types.get(type)};
     }
 
     private void readObject (ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
         typeCounts = (int[]) inputStream.readObject();
         typeTopicCounts = (int[][]) inputStream.readObject();
         topicCounts = (int[]) inputStream.readObject();
         wordTopicCounts = (int[][]) inputStream.readObject();
 
         alpha = inputStream.readDouble();
         beta = inputStream.readDouble();
         betaSum = inputStream.readDouble();
         gamma = inputStream.readDouble();
         gammaSum = inputStream.readDouble();
 
         numTopics = inputStream.readInt();
         numTypes = inputStream.readInt();
         numWords = inputStream.readInt();
 
         random = (Random) inputStream.readObject();
 
         labelIndex = (Index) inputStream.readObject();
         typeIndex = (Index) inputStream.readObject();
         wordIndex = (Index) inputStream.readObject();
     }
 
     public void write (File file) throws IOException {
         ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
         outputStream.writeObject(this);
         outputStream.close();
     }
 
     private void writeObject (ObjectOutputStream outputStream) throws IOException {
         outputStream.writeObject(typeCounts);
         outputStream.writeObject(typeTopicCounts);
         outputStream.writeObject(topicCounts);
         outputStream.writeObject(wordTopicCounts);
 
         outputStream.writeDouble(alpha);
         outputStream.writeDouble(beta);
         outputStream.writeDouble(betaSum);
         outputStream.writeDouble(gamma);
         outputStream.writeDouble(gammaSum);
 
         outputStream.writeInt(numTopics);
         outputStream.writeInt(numTypes);
         outputStream.writeInt(numWords);
 
         outputStream.writeObject(random);
 
         outputStream.writeObject(labelIndex);
         outputStream.writeObject(typeIndex);
         outputStream.writeObject(wordIndex);
 
     }
 }
