 package topicmodels;
 
 import util.*;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.logging.Logger;
 
 
 public class ATM {
 
     public static Logger logger = Logger.getLogger(DDLLDA.class.getName());
 
     public ATM.LearnSampler learnSampler;
 
     public int numTopics;
     public int numTypes;
     public int numWords;
 
     // hyper-parameters
     public double alpha;
     public double beta;
     public double gamma;
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
 
     /**
      * Initialize an instance of ATM.
      *
      * @param numTopics the number of topics per author;
      * @param alpha smoothing sum over the topic distribution;
      * @param beta smoothing over the unigram distribution;
      * @param gamma smoothing over the type distribution;
      * @param corpus the corpus from which to learn the distributions;
      */
     public ATM(int numTopics, double alpha, double beta, double gamma, Corpus corpus) {
         this.numTopics = numTopics;
         this.numTypes = corpus.getNumTypes();
         this.numWords = corpus.getNumWords();
 
         this.beta = beta;
         this.gamma = gamma;
         this.alpha = alpha;
         this.betaSum = beta * numWords;
         this.gammaSum = gamma * numTypes;
 
         topicCounts = new int[numTopics];
         wordTopicCounts = new int[numWords][numTopics];
         typeCounts = new int[numTypes];
         typeTopicCounts = new int[numTypes][numTopics];
 
         topicIndex = new Index();
         for (int topic = 0; topic < numTopics; topic++) {
             topicIndex.put("Topic: " + topic);
         }
         typeIndex = corpus.getTypeIndex();
         wordIndex = corpus.getWordIndex();
 
         random = new Randoms(20);
     }
 
     /**
      * Given a corpus, where each document has been assigned to a category and a number
      * of labels or topics have been assigned to the document, learn the type-topic
      * distributions, the distributions of types over documents, the distributions
      * of topics over documents and the word distributions of topics.
      *
      * @param iterations how many iterations to run the sampler;
      * @param corpus the corpus to run the sampler on;
      */
     public void train (int iterations, Corpus corpus) {
         learnSampler = new ATM.LearnSampler();
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
 
     /**
      * Write the either learned or inferred topic distributions to a file.
      *
      * @param file the name of the file to write the results;
      * @param corpus the corpus containing the topic and type assignments;
      * @param smooth parameter to use for smoothing the topic distributions on output;
      * @throws java.io.IOException
      */
     public void writeTopicDistributions (File file, Corpus corpus, double smooth) throws IOException {
         PrintWriter printer = new PrintWriter(file);
         printer.print("source\ttopic:proportion...\n");
         for (Document document : corpus) {
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
                 printer.print(topicIndex.getItem(sortedTopics[index].getIndex()) + " " + score + " ");
             }
             printer.print("\n");
         }
         printer.close();
     }
 
     public void printAuthorTopicDistribution (File file) throws IOException {
         PrintStream output = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
         output.print(authorTopicDistributions());
         output.close();
     }
 
     public String authorTopicDistributions () {
         StringBuilder output = new StringBuilder();
         output.append("#person\ttopic:proportion...\n");
         IDSorter[] sortedTopics = new IDSorter[numTopics];
         for (int author = 0; author < numTypes; author++) {
             for (int topic = 0; topic < numTopics; topic++) {
                 sortedTopics[topic] = new IDSorter(topic, ((double) typeTopicCounts[author][topic]) / typeCounts[author]);
             }
             Arrays.sort(sortedTopics);
             output.append(typeIndex.getItem(author)).append("\t");
             for (int topic = 0; topic < numTopics; topic++) {
                output.append(topic).append(":").append(sortedTopics[topic].getValue()).append(" ");
             }
             output.append("\n");
         }
         return output.toString();
     }
 
     public void printTopicDistribution (File file) throws IOException {
         PrintStream output = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
         output.print(getTopicDistribution());
         output.close();
     }
 
     private String getTopicDistribution () {
         StringBuilder output = new StringBuilder();
         IDSorter[] sortedWords = new IDSorter[numWords];
         for (int topic = 0; topic < numTopics; topic++) {
             for (int word = 0; word < numWords; word++) {
                 sortedWords[word] = new IDSorter(word, (double) wordTopicCounts[word][topic]);
             }
             Arrays.sort(sortedWords);
             output.append(topicIndex.getItem(topic))
                     .append(" ")
                     .append("count: ")
                     .append(topicCounts[topic])
                     .append(" ");
             for (int word = 0; word < numWords; word++) {
                output.append(wordIndex.getItem(sortedWords[word].getIndex()))
                        .append(":")
                        .append(sortedWords[word].getValue())
                        .append(" ");
             }
             output.append("\n");
         }
         return output.toString();
     }
 
     /**
      * Read an existing serialized model from disk.
      *
      * @param file the filename of the model to read
      * @return the model
      * @throws IOException
      * @throws ClassNotFoundException
      */
     public static ATM read (File file) throws IOException, ClassNotFoundException {
         ATM atm;
         ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
         atm = (ATM) inputStream.readObject();
         inputStream.close();
         return atm;
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
 
         trained = inputStream.readBoolean();
     }
 
     /**
      * Write a model serialized to disk.
      *
      * @param file the name of the file to write the model to;
      * @throws IOException
      */
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
 
         outputStream.writeBoolean(trained);
     }
 
     /**
      * Base sampler, sub-classed by LearnSampler and InferSampler.
      */
     public class Sampler {
 
         /**
          * Sample the topics and types for all tokens of a document.
          *
          * @param document an instance of Document for which we sample the topics and types;
          * @param types the set of possible types to sample from fro this document;
          */
         public void sampleForOneDocument (Document document, ArrayList<Integer> types) {
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
                 assignment = sample(word, types, docTypeCounts);
                 topic = assignment[1]; type = assignment[2];
                 increment(topic, word, type);
                 docTypeCounts[type]++;
                 document.setTopic(position, topic);
                 document.setType(position, type);
             }
         }
 
         /**
          * Sample the topics and types for all tokens of a document.
          *
          * @param document an instance of Document for which we sample the topics and types;
          */
         public void sampleForOneDocument (Document document) {
             sampleForOneDocument(document, document.getTypes());
         }
 
         public void increment (int topic, int word, int type) {}
         public void decrement (int topic, int word, int type) {}
 
         /**
          * Sample a type and a topic for the current word. This method is computationally
          * quite heavy and should and could probably be optimized further.
          *
          * @param word the word for which we sample a topic and a type;
          * @param types the set of types to sample a type from;
          * @param docTypeCounts for each type, how often does it occur in the document under investigation?
          * @return an array consisting of a word, a topic and a type;
          */
         public int[] sample (int word, ArrayList<Integer> types, int[] docTypeCounts) {
             double[][] topicTermScores = new double[numTopics][types.size()];
             double sum = 0.0;
             for (int i = 0; i < types.size(); i++) {
                 int type = types.get(i);
                 double P_T = (gammaSum + typeCounts[type]);
                 double P_Dt = (alpha + docTypeCounts[type]);
                 for (int topic = 0; topic < numTopics; topic++) {
                     double score = P_Dt * // P(T|D)
                             (beta + wordTopicCounts[word][topic]) / (betaSum + topicCounts[topic]) * // P(w|t)
                             (gamma + typeTopicCounts[type][topic]) / P_T;  // P(t|T)
                     sum += score;
                     topicTermScores[topic][i] = score;
                 }
             }
             double sample = random.nextUniform() * sum;
             int topic = -1; int type = 0;
             while (sample > 0.0) {
                 topic++;
                 if (topic == numTopics) {
                     type++;
                     topic = 0;
                 }
                 sample -= topicTermScores[topic][type];
             }
             if (topic == -1) {
                 throw new IllegalStateException("No topic sampled.");
             }
             return new int[]{word, topic, types.get(type)};
         }
     }
 
     /**
      * Sampler for training a model.
      */
     public class LearnSampler extends ATM.Sampler {
 
         /**
          * Add a document to the sampler, which means that we randomly assign to each token
          * a type (sampled from the set of types associated with this document) and
          * a topic (again sampled from the set of topics associated with this document).
          * Increment the new assigned topic and type in the count matrices.
          *
          * @param document an instance of Document for which to do the random assignments;
          */
         public void addDocument (Document document) {
             ArrayList<Integer> types = document.getTypes();
             for (int position = 0; position < document.size(); position++) {
                 int topic = random.nextInt(numTopics);
                 int type = random.choice(types);
                 document.setTopic(position, topic);
                 document.setType(position, type);
                 increment(topic, document.getToken(position), type);
             }
         }
 
         /**
          * Update the count matrices by decrementing the appropriate counts.
          *
          * @param topic the topic to update;
          * @param word the word to update;
          * @param type the type to update;
          */
         public void decrement (int topic, int word, int type) {
             typeCounts[type]--;
             typeTopicCounts[type][topic]--;
             topicCounts[topic]--;
             wordTopicCounts[word][topic]--;
         }
 
         /**
          * Update the count matrices by incrementing the appropriate counts.
          *
          * @param topic the topic to update.
          * @param word the word to update.
          * @param type the type to update.
          */
         public void increment (int topic, int word, int type) {
             typeCounts[type]++;
             typeTopicCounts[type][topic]++;
             topicCounts[topic]++;
             wordTopicCounts[word][topic]++;
         }
     }
 }
