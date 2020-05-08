 package phonemeScorers.trainers;
 
 import graphemesToPhonemesConverters.IWordToPhonemesConverter;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 
 import phonemeAligner.singleGaussianBased.GaussianAligner;
 import phonemeScorers.IPhonemeScorer;
 
 import common.AudioLabel;
 import common.GenericListContainer;
 import common.algorithms.DataByTimesExtractor;
 import common.exceptions.ImplementationError;
 
 public class IterativePhonemeScorerTraining
 {
     private AudioLabel[] chunks;
     private ArrayList<double[]> data;
     private double totalTime;
     private GaussianAligner aligner;
     private ArrayList<AudioLabel> lastResults;
 
     public IterativePhonemeScorerTraining(
             double maxTimeOfChunk,
             AudioLabel[] chunks,
             ArrayList<double[]> allData,
             IWordToPhonemesConverter converter,
             double totalTime)
     {
         this.aligner = new GaussianAligner(maxTimeOfChunk, chunks, allData, converter, totalTime);
         Arrays.sort(chunks, new Comparator<AudioLabel>() {
             @Override
             public int compare(AudioLabel o1, AudioLabel o2)
             {
                 double t1 = o1.getEnd() - o1.getStart();
                 double t2 = o2.getEnd() - o2.getStart();
                 if (t1 < t2) return -1;
                 if (t1 > t2) return 1;
                 return 0;
             }
         });
         ArrayList<AudioLabel> auxChunks = new ArrayList<AudioLabel>();
        for (int i = 0; i < this.chunks.length; ++i) {
             if (chunks[i].getEnd() - chunks[i].getStart() > maxTimeOfChunk)
                 break;
            this.chunks[i] = chunks[i];
         }
         System.err.println("took " + auxChunks.size() + " chunks out of " + chunks.length);
         
         this.chunks = auxChunks.toArray(new AudioLabel[0]);
         this.totalTime = totalTime;
         this.data = allData;
     }
     
     public IPhonemeScorer[] train(int iterations) throws ImplementationError
     {
         this.lastResults = new ArrayList<AudioLabel>();
         for (AudioLabel word : chunks)
             this.lastResults.addAll(initialSplit(word));
         
         IPhonemeScorer[] phonemeScorers = new IPhonemeScorer[0];
         for (int i = 0; i < iterations; ++i) {
             System.err.println("align iteration " + i);
             phonemeScorers =
                     new PhonemeSingleGaussianTrainer().trainPhonemes(this.lastResults, data, totalTime);
             
             this.lastResults = this.aligner.align(phonemeScorers);
             System.err.println("~align iteration " + i);
         }
         
         return phonemeScorers;
     }
     
     private ArrayList<AudioLabel> initialSplit(AudioLabel chunk)
     {
         String[] phonemes = this.aligner.splitChunk(chunk.getLabel());
         
         double splitTime = (chunk.getEnd() - chunk.getStart()) / phonemes.length;
         ArrayList<AudioLabel> split = new ArrayList<AudioLabel>();
         for (int i = 0; i < phonemes.length; ++i) {
             double start = (i) * splitTime + chunk.getStart();
             double end = (i + 1) * splitTime + chunk.getStart();
             split.add(new AudioLabel(phonemes[i], start, end));
         }
         return split;
     }
 
     public ArrayList<AudioLabel> getLastResults()
     {
         return this.lastResults;
     }
 }
