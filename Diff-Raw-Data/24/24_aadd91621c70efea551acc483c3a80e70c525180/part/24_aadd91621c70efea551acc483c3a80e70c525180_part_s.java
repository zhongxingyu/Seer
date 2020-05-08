 public class Part {
   private String measure; 
   private String pitch;
   private String step;
   private String alter;
   private String octave;
   private String chord;
   private String tie;
   private String backup;
   private String duration;
   
   public String getMeasure() {
     return measure;
   }
   public void setMeasure(String measure) {
     this.measure = measure;
   }
   public String getPitch() {
     return pitch;
   }
   public void setPitch(String pitch) {
     this.pitch = pitch;
   }
   public String getStep() {
     return step;
   }
   public void setStep(String step) {
     this.step = step;
   }
   public String getAlter() {
     return alter;
   }
   public void setAlter(String alter) {
     this.alter = alter;
   }
   public String getOctave() {
     return octave;
   }
   public void setOctave(String octave) {
     this.octave = octave;
   }
     public String getChord() {
     return chord;
   }
   public void setChord(String chord) {
     this.chord = chord;
   }
   public String getTie() {
     return tie;
   }
   public void setTie(String tie) {
     this.tie = tie;
   }
   public String getBackup() {
     return backup;
   }
   public void setBackup(String backup) {
     this.backup = backup;
   }
   public String getDuration() {
     return duration;
   }
   public void setDuration(String duration) {
     this.duration = duration;
   }

 
   @Override
   public String toString() {
     return "Part [alter=" + alter + ", measure=" + measure + ", octave="
         + octave + ", note=" + note + ", pitch=" + pitch + "]";
   }
 } 
