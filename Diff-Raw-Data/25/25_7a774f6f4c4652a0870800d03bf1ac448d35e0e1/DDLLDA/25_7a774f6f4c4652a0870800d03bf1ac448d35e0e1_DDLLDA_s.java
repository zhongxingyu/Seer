 package topicmodels;
 
 
 import util.*;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.logging.Logger;
 
 
 public class DDLLDA implements Serializable {
 
     public Logger logger = Logger.getLogger(DDLLDA.class.getName());
 
     public DDLLDA.LearnSampler learnSampler;
     public DDLLDA.InferSampler inferSampler;
 
     public int numTopics;
     public int numTypes;
     public int numWords;
 
     // hyper-parameters
     public double alpha;
     public double beta;
     public double gamma;
     public double alphaSum;
     public double betaSum;
     public double gammaSum;
 
     // count matrices
     protected int[] topicCounts;
     protected int[][] wordTopicCounts;
     protected int[] typeCounts;
     protected int[][] typeTopicCounts;
 
     // indexes
     public Index topicIndex;
     public Index typeIndex;
     public Index wordIndex;
 
     protected Randoms random;
     protected Boolean trained = false;
 
     public DDLLDA(double alphaSum, double beta, double gamma, Corpus corpus) {
         this.numTopics = corpus.getNumTopics();
         this.numTypes = corpus.getNumTypes();
         this.numWords = corpus.getNumWords();
 
         this.alphaSum = alphaSum;
         this.beta = beta;
         this.gamma = gamma;
         this.alpha = alphaSum / numTopics;
         this.betaSum = beta * numWords;
         this.gammaSum = gamma * numTypes;
 
         topicCounts = new int[numTopics];
         wordTopicCounts = new int[numWords][numTopics];
         typeCounts = new int[numTypes];
         typeTopicCounts = new int[numTypes][numTopics];
 
         topicIndex = corpus.getLabelIndex();
         typeIndex = corpus.getTypeIndex();
         wordIndex = corpus.getWordIndex();
 
         random = new Randoms(20);
     }
 
     public void train (int iterations, Corpus corpus) {
         learnSampler = new DDLLDA.LearnSampler();
         for (Document document : corpus) {
             learnSampler.addDocument(document);
         }
         logger.info("Sampler initialized. " + numTopics + " topics and " + corpus.size() + " documents.");
         for (int iteration = 1; iteration <= iterations; iteration++) {
             logger.info("Sampling iteration " + iteration + " started.");
             for (Document document: corpus) {
                 learnSampler.sampleForOneDocument(document);
             }
         }
         trained = true;
     }
 
     public void infer (int iterations, Corpus corpus) {
         if (!trained) {
             throw new IllegalStateException("The model is not trained yet!");
         }
 
         inferSampler = new InferSampler();
         for (Document document : corpus) {
             inferSampler.addDocument(document);
         }
         logger.info("Sampler initialized. " + numTopics + " topics and " + corpus.size() + " documents.");
         for (int iteration = 1; iteration <= iterations; iteration++) {
             logger.info("Sampling iteration " + iteration + " started.");
             for (Document document : corpus) {
                 inferSampler.sampleForOneDocument(document);
             }
         }
     }
 
     public void writeTopicDistributions (File file, Corpus corpus, double smooth) throws IOException {
         PrintWriter printer = new PrintWriter(file);
         printer.print("type\tsource\ttopic:proportion...\n");
         for (Document document : corpus) {
             //printer.print(corpus.getTypeIndex().getItem(document.getType()) + "\t");
             printer.print(document.getSource() + "\t");
             IDSorter[] sortedTopics = new IDSorter[numTopics];
             int[] topicCounts = new int[numTopics];
             int docLen = 0;
             for (int position = 0; position < document.size(); position++) {
                 int word = document.getToken(position);
                 if (word >= numWords) { continue; }
                 docLen++;
                 topicCounts[document.getTopic(position)]++;
             }
             for (int topic = 0; topic < numTopics; topic++) {
                 sortedTopics[topic] = new IDSorter(topic, (smooth + topicCounts[topic]) / (docLen));
             }
             Arrays.sort(sortedTopics);
             for (int index = 0; index < numTopics; index++) {
                 double score = sortedTopics[index].getValue();
                 if (score == 0.0) { break; }
                 printer.print(corpus.getLabelIndex().getItem(sortedTopics[index].getIndex()) + " " + score + " ");
             }
             printer.print("\n");
         }
         printer.close();
     }
 
     public static DDLLDA read (File file) throws IOException, ClassNotFoundException {
         DDLLDA ddllda;
         ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
         ddllda = (DDLLDA) inputStream.readObject();
         inputStream.close();
         return ddllda;
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
 
         random = (Randoms) inputStream.readObject();
 
         topicIndex = (Index) inputStream.readObject();
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
 
         outputStream.writeObject(topicIndex);
         outputStream.writeObject(typeIndex);
         outputStream.writeObject(wordIndex);
     }
 
     /* Base sampler, sub-classed by LearnSampler and InferSampler.*/
     public class Sampler {
 
         public void sampleForOneDocument (Document document, ArrayList<Integer> labels, ArrayList<Integer> types) {
             int[] assignment;
             int[] docTypeCounts = new int[numTypes];
             for (Integer type : document.getTypeAssignments()) {
                 docTypeCounts[type]++;
             }
             for (int position = 0; position < document.size(); position++) {
                 int word = document.getToken(position);
                 if (word >= numWords) {
                     continue;
                 }
                 int topic = document.getTopic(position);
                 int type = document.getType(position);
                 decrement(topic, word, type);
                 docTypeCounts[type]--;
                 assignment = sample(word, labels, types, docTypeCounts);
                 topic = assignment[1]; type = assignment[2];
                 increment(topic, word, type);
                 docTypeCounts[type]++;
                 document.setTopic(position, topic);
                 document.setType(position, type);
             }
         }
 
         public void sampleForOneDocument (Document document) {
             sampleForOneDocument(document, document.getLabels(), document.getTypes());
         }
 
         public void increment (int topic, int word, int type) {}
         public void decrement (int topic, int word, int type) {}
 
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
             double sample = random.nextUniform() * sum;
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
             return new int[]{word, labels.get(topic), types.get(type)};
         }
 
     }
 
     // Sampler for training a model.
     public class LearnSampler extends DDLLDA.Sampler {
 
         public void addDocument (Document document) {
             ArrayList<Integer> types = document.getTypes();
             for (int position = 0; position < document.size(); position++) {
                 ArrayList<Integer> labels = document.getLabels();
                 int topic = random.choice(labels);
                 int type = random.choice(types);
                 document.setTopic(position, topic);
                 document.setType(position, type);
                 increment(topic, document.getToken(position), type);
             }
         }
 
         public void decrement (int topic, int word, int type) {
             typeCounts[type]--;
             typeTopicCounts[type][topic]--;
             topicCounts[topic]--;
             wordTopicCounts[word][topic]--;
         }
 
         public void increment (int topic, int word, int type) {
             typeCounts[type]++;
             typeTopicCounts[type][topic]++;
             topicCounts[topic]++;
             wordTopicCounts[word][topic]++;
         }
     }
 
     // Sampler for inference on unseen documents
     public class InferSampler extends DDLLDA.Sampler {
 
         private ArrayList<Integer> documentTypes;
         private ArrayList<Integer> documentTopics;
 
         public InferSampler () {
             documentTypes = new ArrayList<Integer>();
             for (int type = 0; type < numTypes; type++) {
                 documentTypes.add(type);
             }
             documentTopics = new ArrayList<Integer>();
             for (int topic = 0; topic < numTopics; topic++) {
                 documentTopics.add(topic);
             }
         }
 
         public void addDocument (Document document) {
             for (int position = 0; position < document.size(); position++) {
                 int topic = random.choice(documentTopics);
                 int type = random.choice(documentTypes);
                 document.setTopic(position, topic);
                 document.setType(position, type);
             }
         }
 
         public void sampleForOneDocument (Document document) {
             sampleForOneDocument(document, documentTopics, documentTypes);
         }
     }
 }
