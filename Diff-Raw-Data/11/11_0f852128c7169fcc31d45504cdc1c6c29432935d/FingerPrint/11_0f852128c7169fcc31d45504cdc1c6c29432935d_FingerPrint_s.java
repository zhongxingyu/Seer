 package fingerprint;
 
 
 import spectrogram.Spectrogram;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Osibisaad
  * Date: 7/16/13
  * Time: 2:14 PM
  * To change this template use File | Settings | File Templates.
  */
 public class FingerPrint {
 
    private static final double AMP_THRESHHOLD = 0.80;
    private static final double NUMBER_OF_SECONDS = 1;
     private List<Phrase> phrases;
     private double[][] data;
     private int framesPerSecond;
     private int freqRange;
 
     public FingerPrint(Spectrogram spectrogram){
         data = spectrogram.getSpectrogram();
         framesPerSecond = spectrogram.getFramesPerSecond();
         freqRange = data[0].length;
         List<Point> wordPoints = findPoints();
         phrases = getPhrases(getWords(wordPoints));
        System.out.println(phrases.size());


     }
 
     private List<Word> getWords(List<Point> points){
         List<Word> words = new ArrayList<Word>();
         for(Point p : points){
             double[][] wordData = new double[p.getEnd() - p.getStart()+1][freqRange];
             for(int i =p.getStart(); i < p.getEnd(); i++){
                 for(int j = 0; j < data[i].length; j++){
                     wordData[(i-p.getStart())][j] = data[i][j];
                 }
             }
             words.add(new Word(wordData, p));
         }
         return words;
     }
     private List<Phrase> getPhrases(List<Word> words){
         List<Phrase> phrases = new ArrayList<Phrase>();
         int start = 0;
         int lastLoc  =0;
         for(int i = 0; i < words.size(); i++){
             int startLoc = words.get(i).getPoint().getStart();
            if(startLoc - lastLoc > NUMBER_OF_SECONDS*framesPerSecond){
                 int end = i;
                 phrases.add(new Phrase(words.subList(start,end)));
                 start = i+1;
             }
             lastLoc = words.get(i).getPoint().getEnd();
         }
         return phrases;
     }
 
     private List<Point> findPoints(){
         List<Point> wordPoints = new ArrayList<Point>();
         for(int i =0; i < data.length; i++){
             double maxAmp = 0.0;
             for(int j = 0; j < data[i].length; j++){
                 if(data[i][j] > maxAmp)
                     maxAmp = data[i][j];
             }
             if(maxAmp > AMP_THRESHHOLD){
                 int end = findEndPoint(i+1);
                 if(i+2 < end)
                     wordPoints.add(new Point(i, end));
                 i = end + 1;
             }
 
         }
         return wordPoints;
     }
 
     private int findEndPoint(int start){
         int end =0;
         for(int i = start; i < data.length && end == 0; i++){
             double maxAmp = 0.0;
             for(int j = 0; j < data[i].length; j++){
                 if(data[i][j] > maxAmp)
                     maxAmp = data[i][j];
             }
             if(maxAmp < AMP_THRESHHOLD)
                 end = i-1;
         }
         return end;
     }
 
     public List<Phrase> getPhrases() {
         return phrases;
     }
 }
