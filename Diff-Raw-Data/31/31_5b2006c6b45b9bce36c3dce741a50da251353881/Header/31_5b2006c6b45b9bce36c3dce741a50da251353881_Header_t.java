 package datatypes;
 
 public class Header {
     
     private String composer = "Unknown";
     private final String title;
     private final int indexNumber;
     private int tempo = 100;
     private Fraction defaultNoteLengthFraction = new Fraction(1, 8);
     private double defaultNoteLength = defaultNoteLengthFraction.getValue();
     private final KeySignature keySignature;
     private Fraction meter = new Fraction(4,4);
     private String[] voiceNames;
     
     /**
      * Represents the header of an abc file. The three mandatory values must be passed upon instantiation.
      * @param indexNumber int index number of the file ("X")
      * @param title String title of the file ("T")
      * @param keySignature KeySignature object representing the key signature of this file ("K")
      */
     public Header(int indexNumber, String title, KeySignature keySignature) {
         this.indexNumber = indexNumber;
         this.title = title;
         this.keySignature = keySignature;
     }
     
     public String getComposer() {
         return this.composer;
     }
 
     /**
      * Sets the value of this.composer
      * @param composer String value for the composer field
      */
     public void setComposer(String composer) {
         this.composer = composer;
     }
 
     public int getTempo() {
         return this.tempo;
     }
 
     /**
      * Sets the value of this.tempo
      * @param tempo int tempo of the file
      */
     public void setTempo(int tempo) {
         this.tempo = tempo;
     }
 
     public double getDefaultNoteLength() {
         return this.defaultNoteLength;
     }
 
     /**
      * Sets the value for the default note length.
      * @param defaultNoteLengthFraction Fraction representing the default note length
      */
     public void setDefaultNoteLengthFraction(Fraction defaultNoteLengthFraction) {
         this.defaultNoteLengthFraction = defaultNoteLengthFraction;
         this.defaultNoteLength = this.defaultNoteLengthFraction.getValue();
     }
     
     public Fraction getDefaultNoteLengthFraction() {
         return this.defaultNoteLengthFraction;
     }
 
     public Fraction getMeter() {
         return this.meter;
     }
 
     /**
      * Sets the meter of the file
      * @param meter Fraction representing the meter of the file
      */
     public void setMeter(Fraction meter) {
         this.meter = meter;
     }
 
     public String[] getVoiceNames() {
         return this.voiceNames;
     }
 
     /**
      * Adds an array of Voice names to the header
      * @param voiceNames String array containing the names of the different voices present in the file
      */
     public void setVoiceNames(String[] voiceNames) {
         this.voiceNames = voiceNames;
     }
 
     public String getTitle() {
         return this.title;
     }
 
     public int getIndexNumber() {
         return this.indexNumber;
     }
 
     public KeySignature getKeySignature() {
         return this.keySignature;
     }
     
     @Override
     public String toString() {
     	String voices = "";
     	for (String voice : voiceNames) {
    		voices += "V:" + voice.trim() + "\n";
     	}
        return String.format("X:%s\n" +
                "T:%s\n" +
                "C:%s\n" +
                "M:%s\n" +
                "L:%s\n" +
                "Q:%s\n" +
                 "%s" + 
                "K:%s\n", this.getIndexNumber(), this.getTitle(), this.getComposer(),
                 this.getMeter().toString(), this.getDefaultNoteLengthFraction().toString(),
                 this.getTempo(), voices, this.getKeySignature().getStringRep());
     }
 }
