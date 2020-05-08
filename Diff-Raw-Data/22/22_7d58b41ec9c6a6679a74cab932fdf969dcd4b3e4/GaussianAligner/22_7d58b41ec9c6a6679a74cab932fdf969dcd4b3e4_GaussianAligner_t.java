 package phonemeAligner.singleGaussianBased;
 
 import graphemesToPhonemesConverters.IWordToPhonemesConverter;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import phonemeScorers.IPhonemeScorer;
 
 import common.AudioLabel;
 import common.GenericListContainer;
 import common.algorithms.DataByTimesExtractor;
 import common.exceptions.ImplementationError;
 
 public class GaussianAligner
 {
     private AudioLabel[] chunks;
     ArrayList<double[]> data;
     IWordToPhonemesConverter converter;
     double totalTime;
     double frameTime;
     DataByTimesExtractor<double[]> dataExtractor;
 
     public GaussianAligner(
             double maxTimeOfChunk,
             AudioLabel[] chunks,
             ArrayList<double[]> allData,
             IWordToPhonemesConverter converter,
             double totalTime)
     {
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
        for (int i = 0; i < chunks.length; ++i) {
             if (chunks[i].getEnd() - chunks[i].getStart() > maxTimeOfChunk)
                 break;
            auxChunks.add(chunks[i]);
         }
         System.err.println("took " + auxChunks.size() + " chunks out of " + chunks.length);
         
         this.chunks = auxChunks.toArray(new AudioLabel[0]);
         this.converter = converter;
         this.totalTime = totalTime;
         this.frameTime = totalTime / allData.size();
         this.data = allData;
         this.dataExtractor = new DataByTimesExtractor<double[]>(
                 new GenericListContainer<double[]>(this.data), totalTime, 0);
     }
     
     public ArrayList<AudioLabel> align(IPhonemeScorer[] phonemeScorers) throws ImplementationError
     {
         ArrayList<AudioLabel> phonemeLabels = new ArrayList<AudioLabel>();
         for (AudioLabel word : chunks) {
             ArrayList<AudioLabel> wordPhonemes = findPhonemes(word, phonemeScorers);
             phonemeLabels.addAll(wordPhonemes);
         }
         
         return phonemeLabels;
     }
     
     private class PhonemeSequenceScorer
     {
         String phoneme;
         IPhonemeScorer dataScorer;
         PhonemeSequenceScorer previous = null;
         double bestScore;
         double bestStartTime;
 
         public PhonemeSequenceScorer(
             String phoneme,
             IPhonemeScorer gaussianMixturePhonemeScorer,
             double initialScore,
             double initialTime)
         {
             this.phoneme = phoneme;
             this.dataScorer = gaussianMixturePhonemeScorer;
             this.bestScore = initialScore;
             this.bestStartTime = initialTime;
         }
 
         public PhonemeSequenceScorer(PhonemeSequenceScorer previous)
         {
             this.phoneme = previous.phoneme;
             this.dataScorer = previous.dataScorer;
             this.previous = previous.previous;
             this.bestScore = previous.bestScore;
             this.bestStartTime = previous.bestStartTime;
         }
 
         public ArrayList<AudioLabel> getBestAlignment(double endTime)
         {
             ArrayList<AudioLabel> ret =
                 (previous == null) ? new ArrayList<AudioLabel>() :
                     previous.getBestAlignment(bestStartTime);
             ret.add(new AudioLabel(phoneme, bestStartTime, endTime));
             return ret;
         }
 
         public void score(
             double[] audio, double currentFrameTime, PhonemeSequenceScorer previous, double previousScore) throws ImplementationError
         {
             double frameScore = this.dataScorer.score(audio);
             double noChangeScore = this.bestScore + frameScore;
             double changeScore = ((previous != null) ? previous.getScore() : Double.NEGATIVE_INFINITY) + frameScore;
             
             if (noChangeScore > changeScore) {
                 this.bestScore = noChangeScore;
             } else {
                 this.bestScore = changeScore;
                 this.bestStartTime = currentFrameTime;
                 this.previous = previous;
             }
         }
 
         public double getScore()
         {
             return bestScore;
         }
     }
 
     private ArrayList<AudioLabel> findPhonemes(
             AudioLabel chunk, IPhonemeScorer[] phonemeScorers) throws ImplementationError
     {
         if (chunk.getEnd() <= chunk.getStart()) return new ArrayList<AudioLabel>();
         
         String[] phonemes = splitChunk(chunk.getLabel());
         ArrayList<double[]> audio = this.dataExtractor.extract(chunk.getStart(), chunk.getEnd());
         
         PhonemeSequenceScorer[] scorers = new PhonemeSequenceScorer[phonemes.length];
         for (int i = 0; i < scorers.length; ++i) {
             for (int j = 0; j < phonemeScorers.length; ++j) {
                 if (!phonemeScorers[j].getPhoneme().equals(phonemes[i])) continue; 
                 scorers[i] = new PhonemeSequenceScorer(
                         phonemes[i],
                         phonemeScorers[j],
                         (i == 0) ? 0 : Double.NEGATIVE_INFINITY,
                         chunk.getStart());
                 break;
             }
             if (scorers[i] == null) {
                 System.err.println("null: " + phonemes[i]);
             }
         }
         
         for (int i = 0; i < audio.size(); ++i) {
             PhonemeSequenceScorer[] newScorers = new PhonemeSequenceScorer[phonemes.length];
             for (int j = 0; j < newScorers.length; ++j) {
                 newScorers[j] = new PhonemeSequenceScorer(scorers[j]);
             }
             for (int j = 0; j < scorers.length; ++j) {
                 PhonemeSequenceScorer previous = (j > 0) ? scorers[j - 1] : null;
                 double previousScore = (j > 0) ? previous.getScore() : Double.NEGATIVE_INFINITY;
                 double currentTime = frameTime * i + chunk.getStart();
                 newScorers[j].score(audio.get(i), currentTime, previous, previousScore);
             }
             scorers = newScorers;
         }
         
         return scorers[scorers.length - 1].getBestAlignment(chunk.getEnd());
     }
 
     public String[] splitChunk(String chunk)
     {
         String[] words = chunk.split("[. ]+");
         ArrayList<String> phonemes = new ArrayList<String>();
         for (String word : words) {
             phonemes.add("sil");
             String[] wordPhonemes = splitWord(word);
             for (String phoneme : wordPhonemes)
                 phonemes.add(phoneme);
         }
         phonemes.add("sil");
         return phonemes.toArray(new String[0]);
     }
     private String[] splitWord(String word)
     {
         return this.converter.convert(word).get(0).split(" ");
     }
 }
