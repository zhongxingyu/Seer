 import com.google.common.base.CharMatcher;
 import com.google.common.base.Splitter;
 import com.google.common.collect.*;
 import org.apache.tools.ant.DirectoryScanner;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import weka.core.Attribute;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.converters.ConverterUtils;
 import weka.filters.Filter;
 import weka.filters.unsupervised.attribute.NumericToBinary;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.*;
 
 public class Retrieval {
     @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
     @Option(name = "-i", aliases = {"--index"}, multiValued = true, required = false, usage = "the indices to be used")
     private List<String> indicesNames;
     private List<File> indices;
     @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
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
     @SuppressWarnings({"FieldCanBeLocal"})
     @Option(name = "-q", aliases = {"--query"}, required = false, usage = "if a query should be used")
     private boolean queryWords = false;
 
     public void query() throws Exception {
         setupIndices();
 
         // index -> document -> similarity
         Map<String, Multimap<String, DocumentSimilarity>> similaritiesByIndex = Maps.newHashMap();
         // document -> similarity
         Multimap<String, DocumentSimilarity> similaritiesByDocument = HashMultimap.create();
 
         // build a table: query / index -> {similarity}
         Table<String, String, List<DocumentSimilarity>> table = HashBasedTable.create();
 
         for (File indexFile : indices) {
             ConverterUtils.DataSource source = new ConverterUtils.DataSource(indexFile.getAbsolutePath());
             Instances indexInstances = source.getDataSet();
 
 
             Instance queryVector = new Instance(indexInstances.numAttributes());
 
             Enumeration attributes = indexInstances.enumerateAttributes();
             while (attributes.hasMoreElements()) {
                 Attribute attribute = (Attribute) attributes.nextElement();
 
                 if (classAttribute == null && attribute.name().matches(".*[Cc]lass.*")) {
                     classAttribute = attribute;
                     //queryVektor.setValue(attribute, "QUERY");
                 }
 
                 if (documentAttribute == null && attribute.name().matches(".*[Dd]ocument.*")) {
                     documentAttribute = attribute;
                     queryVector.setValue(attribute, "QUERY");
                 }
 
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
 
 
             //Instance queryVector = new Instance(indexInstances.numAttributes());
 
             attributes = indexInstances.enumerateAttributes();
             while (attributes.hasMoreElements()) {
                 Attribute attribute = (Attribute) attributes.nextElement();
 
                 for (String queryWord : queryDocuments) {
                     if (attribute.name().matches(queryWord)) {
                         queryVector.setValue(attribute, 1);
                     } else {
                         queryVector.setValue(attribute, 0);
                     }
                 }
             }
 
             NumericToBinary numericToBinary = new NumericToBinary();
             numericToBinary.setInputFormat(indexInstances);
             Instances newData = Filter.useFilter(indexInstances, numericToBinary);
 
             System.out.println("Used Filter NumericToBinary");
 
             List<Instance> documentVectors = Lists.newLinkedList();
 
 
             documentVectors.add(queryVector);
 
 
             similarityMeasure.getDistanceFunction().setInstances(indexInstances);
 
             // document -> similarity
             Multimap<String, DocumentSimilarity> similaritiesForIndex = HashMultimap.create();
 
             // calculate distance to all other documents in the index file
             Enumeration instances = indexInstances.enumerateInstances();
             while (instances.hasMoreElements()) {
                 Instance instance = (Instance) instances.nextElement();
                 String instanceName = getInstanceName(instance);
 
                 for (Instance queryInstance : documentVectors) {
                     String queryInstanceName = getInstanceName(queryInstance);
                     // skip same document
                     if (instanceName.equals(queryInstanceName))
                         continue;
 
                     double distance = similarityMeasure.getDistanceFunction().distance(queryInstance, instance);
 
                     List<DocumentSimilarity> similarities = table.get(queryInstanceName, indexFile.getName());
 
                     if (similarities == null) {
                         similarities = Lists.newArrayList();
                         table.put(queryInstanceName, indexFile.getName(), similarities);
                     }
 
                     DocumentSimilarity documentSimilarity =
                             new DocumentSimilarity(distance, instanceName, indexFile.getName());
 
                     similarities.add(documentSimilarity);
 
                     //similaritiesForIndex.put(queryInstanceName, documentSimilarity);
                 }
 
                 //trimMatchesToSizeK(similaritiesForIndex);
             }
 
             for (Map.Entry<String, List<DocumentSimilarity>> stringListEntry : table.column(indexFile.getName())
                     .entrySet()) {
                 List<DocumentSimilarity> documentSimilarities = stringListEntry.getValue();
                 Collections.sort(documentSimilarities);
 
                 for (int i = 0, documentSimilaritiesSize = documentSimilarities.size(); i < documentSimilaritiesSize;
                      i++) {
                     DocumentSimilarity documentSimilarity = documentSimilarities.get(i);
                     documentSimilarity.setRank(i + 1);
                 }
             }
 
             //trimMatchesToSizeK(similaritiesForIndex);
 
             //similaritiesByIndex.put(indexFile.getName(), similaritiesForIndex);
             //similaritiesByDocument.putAll(similaritiesForIndex);
         }
 
         for (Map.Entry<String, Map<String, List<DocumentSimilarity>>> queryIndexMap : table.rowMap().entrySet()) {
             String query = queryIndexMap.getKey();
             Map<String, List<DocumentSimilarity>> indexResults = queryIndexMap.getValue();
 
             System.out.println("\n\nquery: " + query);
 
             System.out.print("rank ");
             for (File index : indices) {
                 System.out.print(String.format("%-41.41s ", index.getName()));
             }
 
             System.out.println();
 
             System.out.print("-----+");
             for (int i = 0; i < indices.size(); i++) {
                 for (int j = 0; j < 41; j++)
                     System.out.print('-');
                 System.out.print('+');
             }
             System.out.println();
 
             // document -> similarity
             Multimap<String, DocumentSimilarity> documentToSimilarity = HashMultimap.create();
 
             for (int i = 0; i < k; i++) {
                 System.out.print(String.format("#%3d ", i));
 
                 for (File index : indices) {
                     List<DocumentSimilarity> documentSimilarities = indexResults.get(index.getName());
 
                     for (DocumentSimilarity documentSimilarity : documentSimilarities) {
                         documentToSimilarity.put(documentSimilarity.getTargetDocument(), documentSimilarity);
                     }
 
                     DocumentSimilarity similarity = documentSimilarities.get(i);
                     System.out.print(String.format("%-30.30s %10.3f ", similarity.getTargetDocument(),
                             similarity.getDistance()));
                 }
 
                 System.out.println();
             }
 
             System.out.println();
             System.out.println();
             System.out.println();
 
             System.out.println(String.format("\n%-40.40s %-7.7s %-15.15s %-15.15s", "document", "#occur", "avg rank",
                     "avg dist"));
             List<DocumentStatistics> statistics = Lists.newArrayList();
 
             for (String document : documentToSimilarity.keySet()) {
                 Collection<DocumentSimilarity> documentSimilarities = documentToSimilarity.get(document);
                 DocumentStatistics stats = new DocumentStatistics(document);
 
                 for (DocumentSimilarity documentSimilarity : documentSimilarities)
                     stats.addState(documentSimilarity.getRank(), documentSimilarity.getDistance());
 
                 statistics.add(stats);
             }
 
             Collections.sort(statistics, new Comparator<DocumentStatistics>() {
                 @Override
                 public int compare(DocumentStatistics o1, DocumentStatistics o2) {
                     int result = Double.compare(o1.getAverageRank(), o2.getAverageRank());
 
                     if (result != 0.0) return result;
 
                     result = Double.compare(o1.getAverageDistance(), o2.getAverageDistance());
 
                     if (result != 0.0) return result;
 
                     return o2.getNumberOfOccurrences() - o1.getNumberOfOccurrences();
                 }
             });
 
             for (DocumentStatistics stats : statistics.subList(0, Math.min(k * 5, statistics.size()))) {
                 System.out.println(String.format("%-40.40s %7d %15.3f %15.3f", stats.getDocument(),
                         stats.getNumberOfOccurrences(), stats.getAverageRank(), stats.getAverageDistance()));
             }
         }
     }
 
     public void run() throws Exception {
         if (queryWords) {
             query();
             return;
         }
 
         setupIndices();
         printProgramStatus();
 
         // build a table: query / index -> {similarity}
         Table<String, String, List<DocumentSimilarity>> table = HashBasedTable.create();
 
         for (File indexFile : indices) {
             ConverterUtils.DataSource source = new ConverterUtils.DataSource(indexFile.getAbsolutePath());
             Instances indexInstances = source.getDataSet();
 
            classAttribute = null;
            documentAttribute = null;

             Enumeration attributes = indexInstances.enumerateAttributes();
             while (attributes.hasMoreElements()) {
                 Attribute attribute = (Attribute) attributes.nextElement();
 
                 if (classAttribute == null && attribute.name().matches(".*[Cc]lass.*") &&
                         (attribute.type() == Attribute.STRING || attribute.type() == Attribute.NOMINAL))
                     classAttribute = attribute;
 
                 if (documentAttribute == null && attribute.name().matches(".*[Dd]ocument.*") &&
                         attribute.type() == Attribute.STRING)
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
 
             System.err.println("index " + indexFile.getName());
             System.err.println("    class: " + classAttribute.name());
             System.err.println("    document: " + documentAttribute.name());
 
             List<Instance> documentVectors = Lists.newLinkedList();
 
             Enumeration instances = indexInstances.enumerateInstances();
             while (instances.hasMoreElements()) {
                 Instance instance = (Instance) instances.nextElement();
 
                 String document = getInstanceName(instance);
 
                 if (!queryDocuments.contains(document)) continue;
 
                 documentVectors.add(instance);
             }
 
             similarityMeasure.getDistanceFunction().setInstances(indexInstances);
 
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
 
                     List<DocumentSimilarity> similarities = table.get(queryInstanceName, indexFile.getName());
 
                     if (similarities == null) {
                         similarities = Lists.newArrayList();
                         table.put(queryInstanceName, indexFile.getName(), similarities);
                     }
 
                     DocumentSimilarity documentSimilarity =
                             new DocumentSimilarity(distance, instanceName, indexFile.getName());
 
                     similarities.add(documentSimilarity);
                 }
             }
 
             for (Map.Entry<String, List<DocumentSimilarity>> stringListEntry : table.column(indexFile.getName())
                     .entrySet()) {
                 List<DocumentSimilarity> documentSimilarities = stringListEntry.getValue();
                 Collections.sort(documentSimilarities);
 
                 for (int i = 0, documentSimilaritiesSize = documentSimilarities.size(); i < documentSimilaritiesSize;
                      i++) {
                     DocumentSimilarity documentSimilarity = documentSimilarities.get(i);
                     documentSimilarity.setRank(i + 1);
                 }
             }
         }
 
         for (Map.Entry<String, Map<String, List<DocumentSimilarity>>> queryIndexMap : table.rowMap().entrySet()) {
             String query = queryIndexMap.getKey();
             Map<String, List<DocumentSimilarity>> indexResults = queryIndexMap.getValue();
 
             System.out.println("\n\nquery: " + query);
 
             System.out.print("rank ");
             for (File index : indices) {
                 System.out.print(String.format("%-41.41s ", index.getName()));
             }
 
             System.out.println();
 
             System.out.print("-----+");
             //noinspection UnusedDeclaration
             for (File index : indices) {
                 for (int j = 0; j < 41; j++)
                     System.out.print('-');
                 System.out.print('+');
             }
             System.out.println();
 
             // document -> similarity
             Multimap<String, DocumentSimilarity> documentToSimilarity = HashMultimap.create();
 
             for (int i = 0; i < k; i++) {
                 System.out.print(String.format("#%3d ", i));
 
                 for (File index : indices) {
                     List<DocumentSimilarity> documentSimilarities = indexResults.get(index.getName());
 
                     if (documentSimilarities == null) {
                         System.err.println("No similarities for index " + index.getName());
                         for (int j = 0; j < 42; j++) System.out.print(' ');
                         continue;
                     }
 
                     for (DocumentSimilarity documentSimilarity : documentSimilarities) {
                         documentToSimilarity.put(documentSimilarity.getTargetDocument(), documentSimilarity);
                     }
 
                     DocumentSimilarity similarity = documentSimilarities.get(i);
                     System.out.print(String.format("%-30.30s %10.6f ", similarity.getTargetDocument(),
                             similarity.getDistance()));
                 }
 
                 System.out.println();
             }
 
             System.out.println(String.format("\n%-40.40s %-7.7s %-15.15s %-15.15s", "document", "#occur", "avg rank",
                     "avg dist"));
             List<DocumentStatistics> statistics = Lists.newArrayList();
 
             for (String document : documentToSimilarity.keySet()) {
                 Collection<DocumentSimilarity> documentSimilarities = documentToSimilarity.get(document);
                 DocumentStatistics stats = new DocumentStatistics(document);
 
                 for (DocumentSimilarity documentSimilarity : documentSimilarities)
                     stats.addState(documentSimilarity.getRank(), documentSimilarity.getDistance());
 
                 statistics.add(stats);
             }
 
             Collections.sort(statistics, new Comparator<DocumentStatistics>() {
                 @Override
                 public int compare(DocumentStatistics o1, DocumentStatistics o2) {
                     int result = Double.compare(o1.getAverageRank(), o2.getAverageRank());
 
                     if (result != 0.0) return result;
 
                     result = Double.compare(o1.getAverageDistance(), o2.getAverageDistance());
 
                     if (result != 0.0) return result;
 
                     return o2.getNumberOfOccurrences() - o1.getNumberOfOccurrences();
                 }
             });
 
             for (DocumentStatistics stats : statistics.subList(0, Math.min(k * 5, statistics.size()))) {
                 System.out.println(String.format("%-40.40s %7d %15.3f %15.3f", stats.getDocument(),
                         stats.getNumberOfOccurrences(), stats.getAverageRank(), stats.getAverageDistance()));
             }
 
             for (DocumentStatistics stats : statistics.subList(Math.min(k * 5, statistics.size()), statistics.size())) {
                 if (stats.getNumberOfOccurrences() < indices.size()) {
                     System.out.println(String.format("%-40.40s %7d %15.3f %15.3f", stats.getDocument(),
                             stats.getNumberOfOccurrences(), stats.getAverageRank(), stats.getAverageDistance()));
                 }
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
