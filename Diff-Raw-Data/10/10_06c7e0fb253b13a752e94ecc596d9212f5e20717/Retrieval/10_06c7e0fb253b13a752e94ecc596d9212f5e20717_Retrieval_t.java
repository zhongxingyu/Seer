 import com.google.common.base.CharMatcher;
 import com.google.common.base.Objects;
 import com.google.common.base.Splitter;
 import com.google.common.collect.*;
 import org.apache.tools.ant.DirectoryScanner;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import weka.core.*;
 import weka.core.converters.ConverterUtils;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.*;
 
 
 public class Retrieval {
     public enum SimilarityMeasure {
         L1(new ManhattanDistance()),
         L2(new EuclideanDistance());
 
         private DistanceFunction distanceFunction;
 
         SimilarityMeasure(DistanceFunction distanceFunction) {
             this.distanceFunction = distanceFunction;
         }
 
         public DistanceFunction getDistanceFunction() {
             return distanceFunction;
         }
     }
 
     public static class DocumentSimilarity implements Comparable<DocumentSimilarity> {
         private double distance;
         private String sourceDocument;
         private String targetDocument;
         private String index;
 
         public DocumentSimilarity(double distance, String sourceDocument, String targetDocument, String index) {
             this.distance = distance;
             this.sourceDocument = sourceDocument;
             this.targetDocument = targetDocument;
             this.index = index;
         }
 
         public double getDistance() {
             return distance;
         }
 
         public String getSourceDocument() {
             return sourceDocument;
         }
 
         public String getTargetDocument() {
             return targetDocument;
         }
 
         public String getIndex() {
             return index;
         }
 
         @Override
         public int compareTo(DocumentSimilarity o) {
             return Double.compare(distance, o.distance);
         }
 
         @Override
         public int hashCode() {
             return Objects.hashCode(distance, sourceDocument, targetDocument, index);
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) return false;
             if (!obj.getClass().equals(getClass())) return false;
             DocumentSimilarity other = (DocumentSimilarity) obj;
 
             return Objects.equal(distance, other.distance) &&
                     Objects.equal(sourceDocument, other.sourceDocument) &&
                     Objects.equal(targetDocument, other.targetDocument) &&
                     Objects.equal(index, other.index);
         }
     }
 
     public static class DocumentStatistics implements Comparable<DocumentStatistics> {
         private int numberOfOccurrences;
         private List<Integer> ranks;
         private List<Double> distances;
         private double averageRank = -1.0;
         private double averageDistance = -1.0;
 
         public DocumentStatistics() {
             numberOfOccurrences = 0;
             ranks = Lists.newArrayList();
             distances = Lists.newArrayList();
         }
 
         public void addState(int rank, double distance) {
             numberOfOccurrences++;
             ranks.add(rank);
             distances.add(distance);
         }
 
         public double getAverageRank() {
             if (averageRank > -1) return averageRank;
 
             averageRank = 0;
 
             for (Integer rank : ranks)
                 averageRank += rank;
 
             averageRank /= ranks.size();
             ranks = null;
 
             return averageRank;
         }
 
         public double getAverageDistance() {
             if (averageDistance > -1) return averageDistance;
 
             averageDistance = 0;
 
             for (Double distance : distances)
                 averageDistance += distance;
 
             averageDistance /= distances.size();
             distances = null;
 
             return averageDistance;
         }
 
         public int getNumberOfOccurrences() {
             return numberOfOccurrences;
         }
 
         @Override
         public int compareTo(DocumentStatistics o) {
             int result;
 
             result = o.getNumberOfOccurrences() - getNumberOfOccurrences();
             if(result != 0.0) return result;
 
             result = Double.compare(getAverageRank(), o.getAverageRank());
             if(result != 0.0) return result;
 
             result = Double.compare(getAverageDistance(), o.getAverageDistance());
             return result;
         }
     }
 
     @Option(name = "-i", aliases = {"--index"}, multiValued = true, required = false, usage = "the indices to be used")
     private List<String> indicesNames;
     private List<File> indices;
     @Argument(multiValued = true, required = true, index = 0, usage = "the names of the query documents",
             metaVar = "QUERY")
     private List<String> queryDocuments;
     @Option(name = "-k", required = false, usage = "the number k of to-be-retrieved documents")
     private int k = 5;
     @Option(name = "-m", aliases = {"--measure"}, required = false,
             usage = "the similarity function to be used for similarity retrieval")
     private SimilarityMeasure similarityMeasure = SimilarityMeasure.L1;
     private Attribute classAttribute = null;
     private Attribute documentAttribute = null;
 
     @Option(name = "-q", aliases = {"--query"}, multiValued = true, required = false, usage = "the query to be used")
     private List<String> queryWords;
     
     public void query() throws Exception
     {
     	setupIndices();
     	
         // index -> document -> similarity
         Multimap<String, DocumentSimilarity> similarities = HashMultimap.create();
         
         for (File indexFile : indices) 
         {
             ConverterUtils.DataSource source = new ConverterUtils.DataSource(indexFile.getAbsolutePath());
             Instances indexInstances = source.getDataSet();
         	
             Enumeration attributes = indexInstances.enumerateAttributes();
             while (attributes.hasMoreElements()) {
                 Attribute attribute = (Attribute) attributes.nextElement();
 
                 if (classAttribute == null && attribute.name().matches(".*[Cc]lass.*"))
                     classAttribute = attribute;
 
                 if (documentAttribute == null && attribute.name().matches(".*[Dd]ocument.*"))
                     documentAttribute = attribute;
 
                 if (documentAttribute != null && classAttribute != null) break;
             }
 
             if (classAttribute == null) {
                 System.err.println("No class attribute found for index " + indexFile);
                 System.err.println("Aborting");
                 System.exit(1);
             }
 
             if (documentAttribute == null) {
                 System.err.println("No document attribute found for index " + indexFile);
                 System.err.println("Aborting");
                 System.exit(1);
             }
             
            Instance queryVector = new Instance(indexInstances.numAttributes());
             attributes = indexInstances.enumerateAttributes();
             while (attributes.hasMoreElements()) {
                 Attribute attribute = (Attribute) attributes.nextElement();
 
                 for(String queryWord : queryWords)
                 {
                     if (attribute.name().matches(queryWord))
                     {
                    	queryVector.setValue(attribute, 1);
                     }
                     else
                     {
                    	queryVector.setValue(attribute, 0);
                     }
                 }             
             }
         }
     }
     
     public void run() throws Exception {
         if(queryWords != null) {
             query();
             return;
         }
 
         setupIndices();
         printProgramStatus();
 
         // index -> document -> similarity
         Map<String, Multimap<String, DocumentSimilarity>> similaritiesByIndex = Maps.newHashMap();
         // document -> similarity
         Multimap<String, DocumentSimilarity> similaritiesByDocument = HashMultimap.create();
 
         for (File indexFile : indices) {
             ConverterUtils.DataSource source = new ConverterUtils.DataSource(indexFile.getAbsolutePath());
             Instances indexInstances = source.getDataSet();
 
             Enumeration attributes = indexInstances.enumerateAttributes();
             while (attributes.hasMoreElements()) {
                 Attribute attribute = (Attribute) attributes.nextElement();
 
                 if (classAttribute == null && attribute.name().matches(".*[Cc]lass.*"))
                     classAttribute = attribute;
 
                 if (documentAttribute == null && attribute.name().matches(".*[Dd]ocument.*"))
                     documentAttribute = attribute;
 
                 if (documentAttribute != null && classAttribute != null) break;
             }
 
             if (classAttribute == null) {
                 System.err.println("No class attribute found for index " + indexFile);
                 System.err.println("Aborting");
                 System.exit(1);
             }
 
             if (documentAttribute == null) {
                 System.err.println("No document attribute found for index " + indexFile);
                 System.err.println("Aborting");
                 System.exit(1);
             }
 
             List<Instance> documentVectors = Lists.newLinkedList();
 
             Enumeration instances = indexInstances.enumerateInstances();
             while (instances.hasMoreElements()) {
                 Instance instance = (Instance) instances.nextElement();
 
                 String document = getInstanceName(instance);
 
                 if (!queryDocuments.contains(document)) continue;
 
                 documentVectors.add(instance);
             }
 
             similarityMeasure.getDistanceFunction().setInstances(indexInstances);
 
             // document -> similarity
             Multimap<String, DocumentSimilarity> similaritiesForIndex = HashMultimap.create();
 
             // calculate distance to all other documents in the index file
             instances = indexInstances.enumerateInstances();
             while (instances.hasMoreElements()) {
                 Instance instance = (Instance) instances.nextElement();
                 String instanceName = getInstanceName(instance);
 
                 for (Instance queryInstance : documentVectors) {
                     String queryInstanceName = getInstanceName(queryInstance);
                     // skip same document
                     if (instanceName.equals(queryInstanceName))
                         continue;
 
                     double distance = similarityMeasure.getDistanceFunction().distance(queryInstance, instance);
 
                     similaritiesForIndex.put(queryInstanceName, new DocumentSimilarity(distance, queryInstanceName,
                             instanceName, indexFile.getName()));
                 }
 
                 trimMatchesToSizeK(similaritiesForIndex);
             }
 
             trimMatchesToSizeK(similaritiesForIndex);
 
             similaritiesByIndex.put(indexFile.getName(), similaritiesForIndex);
             similaritiesByDocument.putAll(similaritiesForIndex);
         }
 
         // build a table: query / index -> {similarity}
         Table<String, String, List<DocumentSimilarity>> table = HashBasedTable.create();
         for (Map.Entry<String, Multimap<String, DocumentSimilarity>> indexToSimilarities : similaritiesByIndex.entrySet()) {
             String column = indexToSimilarities.getKey();
 
             for (Map.Entry<String, DocumentSimilarity> queryToSimilarity : indexToSimilarities.getValue().entries()) {
                 String row = queryToSimilarity.getKey();
 
                 List<DocumentSimilarity> similarities = table.get(row, column);
 
                 if (similarities == null) {
                     similarities = Lists.newArrayList();
                     table.put(row, column, similarities);
                 }
 
                 similarities.add(queryToSimilarity.getValue());
             }
         }
 
         // for each query
         for (String query : table.rowKeySet()) {
             BiMap<String, DocumentStatistics> statistics = HashBiMap.create();
 
             List<DocumentSimilarity> allSimilarities = Lists.newArrayList();
 
             // for each index
             for (List<DocumentSimilarity> documentSimilarities : table.row(query).values()) {
                 Collections.sort(documentSimilarities);
 
                 allSimilarities.addAll(documentSimilarities);
                 int documentSimilaritiesSize = documentSimilarities.size();
 
                 for (int i = 0; i < documentSimilaritiesSize; i++) {
                     DocumentSimilarity documentSimilarity = documentSimilarities.get(i);
                     String name = documentSimilarity.getTargetDocument();
                     DocumentStatistics documentStatistics = statistics.get(name);
 
                     if (documentStatistics == null) {
                         documentStatistics = new DocumentStatistics();
                         statistics.put(name, documentStatistics);
                     }
 
                     documentStatistics.addState(i + 1, documentSimilarity.getDistance());
                 }
             }
 
             Collections.sort(allSimilarities);
 
             System.out.println(
                     String.format("\n\n========================= Query: %20.20s ==========================", query));
 
             System.out.println(String.format("%-37.37s %-8.8s %-33.33s", "document", "distance", "index"));
             System.out.println(
                     "-------------------------------------+--------+---------------------------------");
             for (DocumentSimilarity documentSimilarity : allSimilarities) {
                 System.out.println(String.format("%-37.37s % 8.3f %-33.33s", documentSimilarity.getTargetDocument(),
                         documentSimilarity.getDistance(), documentSimilarity.getIndex()));
             }
 
             List<DocumentStatistics> statsSorted = Lists.newArrayList(statistics.values());
             Collections.sort(statsSorted);
 
             System.out.println(String.format("\n%-40.40s %-7.7s %-15.15s %-15.15s", "document", "#occur", "avg rank",
                     "avg dist"));
             System.out.println("----------------------------------------+-------+---------------+---------------");
             for (DocumentStatistics stats : statsSorted) {
                 System.out.println(String.format("%-40.40s %7d %15.3f %15.3f", statistics.inverse().get(stats),
                         stats.getNumberOfOccurrences(), stats.getAverageRank(),
                         stats.getAverageDistance()));
             }
         }
     }
 
     private void trimMatchesToSizeK(Multimap<String, DocumentSimilarity> similarities) {
         for (String queryDocument : queryDocuments) {
             Collection<DocumentSimilarity> similarityCollection = similarities.get(queryDocument);
 
             if (similarityCollection.size() <= k) return;
 
             List<DocumentSimilarity> similarityList = Lists.newArrayList(similarityCollection);
 
             Collections.sort(similarityList, new Comparator<DocumentSimilarity>() {
                 public int compare(DocumentSimilarity s1, DocumentSimilarity s2) {
                     return Double.compare(s1.getDistance(), s2.getDistance());
                 }
             });
 
             similarities.replaceValues(queryDocument, similarityList.subList(0, Math.min(k, similarityList.size())));
         }
     }
 
     private String getInstanceName(Instance instance) {
         return instance.toString(classAttribute) + "/" + instance.toString(documentAttribute);
     }
 
     private void setupIndices() {
         String workingDirectory = System.getProperty("user.dir");
         File file = new File(workingDirectory);
 
         if (indicesNames == null || indicesNames.size() == 0) {
             indices = Arrays.asList(file.listFiles(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.endsWith(".arff") || name.endsWith(".arff.gz");
                 }
             }));
         } else {
             indices = Lists.newLinkedList();
 
             DirectoryScanner directoryScanner = new DirectoryScanner();
             directoryScanner.setBasedir(file);
             directoryScanner.setCaseSensitive(true);
 
             for (String indexName : indicesNames) {
                 Iterable<String> subIndicesNames =
                         Splitter.on(CharMatcher.is(',')).omitEmptyStrings().trimResults().split(
                                 indexName);
 
                 List<String> subIndices = Lists.newArrayList(subIndicesNames);
 
                 for (String subIndice : subIndices) {
                     System.out.println("subindex " + subIndice);
                 }
 
                 directoryScanner.setIncludes(subIndices.toArray(new String[subIndices.size()]));
                 directoryScanner.scan();
                 String[] fileNames = directoryScanner.getIncludedFiles();
 
                 for (String fileName : fileNames) {
                     if (!fileName.endsWith(".arff") && !fileName.endsWith(".arrf.gz")) continue;
 
                     indices.add(new File(fileName));
                 }
             }
         }
 
         if (indices.size() == 0) {
             System.err.println("No .arff files found in current directory, or no .arff files specified");
             System.exit(1);
         }
     }
 
     private void printProgramStatus() {
         System.out.println(String.format("k                 : %d", k));
         System.out.println(String.format("Similarity Measure: %s", similarityMeasure));
         System.out.println("Used inidices:");
         for (File indexFile : indices) {
             System.out.println("\t" + indexFile.getName());
         }
         System.out.println("Document query:");
         for (String queryDocument : queryDocuments) {
             System.out.println("\t" + queryDocument);
         }
     }
 
     public static void main(String[] args) throws Exception {
         Retrieval retrieval = new Retrieval();
         CmdLineParser parser = new CmdLineParser(retrieval);
         parser.setUsageWidth(80); // width of the error display area
 
         try {
             parser.parseArgument(args);
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             System.err.println("java Retrieval [options...] arguments...");
             // print the list of available options
             parser.printUsage(System.err);
             System.err.println();
             System.exit(1);
         }
 
         retrieval.run();
     }
 
 }
