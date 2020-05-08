 package cz.cvut.fel.kinclma1;
 
 /**
  * Created with IntelliJ IDEA.
  * User: void
  * Date: 24.9.12
  * Time: 19:50
  * To change this template use File | Settings | File Templates.
  */
 public class MusicStringTone extends NoteContent {
 
     enum RelativeTone {
         C,
         C_SHARP,
         D,
         D_SHARP,
         E,
         F,
         F_SHARP,
         G,
         G_SHARP,
         A,
         A_SHARP,
         B;
 
         @Override
         public String toString() {
             switch (this) {
                 case C:
                     return "C";
                 case C_SHARP:
                     return "C#";
                 case D:
                     return "D";
                 case D_SHARP:
                     return "D#";
                 case E:
                     return "E";
                 case F:
                     return "F";
                 case F_SHARP:
                     return "F#";
                 case G:
                     return "G";
                 case G_SHARP:
                     return "G#";
                 case A:
                     return "A";
                 case A_SHARP:
                     return "A#";
                 case B:
                     return "B";
                 default:
                     return null;
             }
         }
 
         public static RelativeTone fromInt(int tone) {
             return RelativeTone.values()[tone];
         }
 
         public int toInt() {
             return ordinal();
         }
     }
 
     private RelativeTone tone;
     private int octave;
 
     public MusicStringTone(int value) {
         tone = RelativeTone.fromInt(value % 12);
         octave = value / 12;
     }
 
     public MusicStringTone(String mStrTone) {
         int len;
         if (mStrTone.length() > 1 && mStrTone.charAt(1) == '#') {
             len = 2;
             for (RelativeTone t : RelativeTone.values()) {
                 if (t.toString().equals(mStrTone.substring(0,2))) {
                     tone = t;
                     break;
                 }
             }
         } else {
             len = 1;
             for (RelativeTone t : RelativeTone.values()) {
                 if (t.toString().equals(mStrTone.substring(0,1))) {
                     tone = t;
                     break;
                 }
             }
         }
         try {
            String[] newMStrTone = mStrTone.split("([a-z])");
            octave = Integer.parseInt(newMStrTone[0].substring(len,newMStrTone[0].length()));
         } catch (NumberFormatException ex) {
             octave = 5;
         }
 
     }
 
     @Override
     public String toString() {
         return tone.toString() + octave;
     }
 
     @Override
     public int toInt() {
         return octave * 12 + tone.toInt();
     }
 
     @Override
     protected String relativeTone() {
         return tone.toString();
     }
 }
