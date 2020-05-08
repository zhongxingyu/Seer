 package edu.stanford.cs224u.disentanglement.features;
 
 import com.google.common.collect.*;
 import edu.stanford.cs224u.disentanglement.structures.Message;
 import edu.stanford.cs224u.disentanglement.structures.MessagePair;
 import edu.stanford.cs224u.disentanglement.util.WordTokenizer;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class TFIDFFeatureFactory extends AbstractFeatureFactory {
 
     @Override
     public Feature createFeature(List<Message> messages) {
         final ImmutableMap.Builder<String, Multiset<String>> tfCounterBuilder = ImmutableMap.builder();
         final ImmutableMultiset.Builder<String> dfCounterBuilder = ImmutableMultiset.builder();
         final int numMessages = messages.size();
 
         for (Message s : messages) {
             Iterable<String> tokens = WordTokenizer.tokenizeWhitespace(s.getBody());
             Multiset<String> counter = ImmutableMultiset.copyOf(tokens);
             tfCounterBuilder.put(s.getId(), counter);
             dfCounterBuilder.addAll(tokens);
         }
 
         return new Feature() {
 
             private final ImmutableMap<String, Multiset<String>> tfCounters = tfCounterBuilder.build();
             private final ImmutableMultiset<String> dfCounter = dfCounterBuilder.build();
 
             @Override
             public Map<Integer, Double> processExample(MessagePair example) {
                 double totalTfIdf = 0.0;
                 double tfIdf1Norm = 0.0;
                 double tfIdf2Norm = 0.0;
                 Set<String> commonWords = example.getWordIntersection();
                 for (String word : commonWords) {
                     // Message 1
                     Multiset<String> counter = tfCounters.get(example.getFirst().getId());
                     Iterator<String> it = Multisets.copyHighestCountFirst(counter).iterator();
                     double maxFreq = counter.count(it.next());
                     double freq = counter.count(word);
                    double tfIdf1 = (freq / maxFreq) * Math.log((numMessages + 1) / dfCounter.count(word));
 
                     // Message 2
                     counter = tfCounters.get(example.getSecond().getId());
                     it = Multisets.copyHighestCountFirst(counter).iterator();
                     maxFreq = counter.count(it.next());
                     freq = counter.count(word);
                    double tfIdf2 = (freq / maxFreq) * Math.log((numMessages + 1) / dfCounter.count(word));
 
                     totalTfIdf += tfIdf1 * tfIdf2;
                 }
                 for (String word : WordTokenizer.tokenizeWhitespace(example.getFirst().getBody())) {
                     Multiset<String> counter = tfCounters.get(example.getFirst().getId());
                     Iterator<String> it = Multisets.copyHighestCountFirst(counter).iterator();
                     double maxFreq = counter.count(it.next());
                     double freq = counter.count(word);
                    double tfIdf = (freq / maxFreq) * Math.log((numMessages + 1) / dfCounter.count(word));
                     tfIdf1Norm += tfIdf * tfIdf;
                 }
                 for (String word : WordTokenizer.tokenizeWhitespace(example.getSecond().getBody())) {
                     Multiset<String> counter = tfCounters.get(example.getSecond().getId());
                     Iterator<String> it = Multisets.copyHighestCountFirst(counter).iterator();
                     double maxFreq = counter.count(it.next());
                     double freq = counter.count(word);
                    double tfIdf = (freq / maxFreq) * Math.log((numMessages + 1) / dfCounter.count(word));
                     tfIdf2Norm += tfIdf * tfIdf;
                 }
                 return ImmutableMap.of(0, totalTfIdf / (Math.sqrt(tfIdf1Norm) * Math.sqrt(tfIdf2Norm)));
             }
 
             @Override
             public int getMaxLength() {
                 return 1;
             }
         };
     }
 }
