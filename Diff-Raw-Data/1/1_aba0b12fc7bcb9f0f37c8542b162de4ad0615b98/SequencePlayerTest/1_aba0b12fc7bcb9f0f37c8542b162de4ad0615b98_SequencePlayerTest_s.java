 package sound;
 
 import static org.junit.Assert.*;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiUnavailableException;
 
 import org.junit.Test;
 
 /**
 * 
  * @author ChristopherFord
  * @category no_didt
  */
 public class SequencePlayerTest{
 
     @Test
     public static void main(String[] args) {
         SequencePlayer player;
         SequencePlayer player1;
         SequencePlayer player2;
         SequencePlayer player3;
         try {
             
             // Create a LyricListener that prints each lyric that it sees.
             LyricListener listener = new LyricListener() {
                 public void processLyricEvent(String text) {
                     System.out.println(text);
                 }
             };
             
             //Creates a new player for Piece 3
             player3 = new SequencePlayer(100, 2, listener);
             
             //Adds all the necessary notes and lyrics to the Piece 3 player
             player3.addLyricEvent("A", 4);
             player3.addNote(new Pitch('D').toMidiNote(), 4, 2);
             player3.addLyricEvent("ma", 6);
             player3.addNote(new Pitch('G').toMidiNote(), 6, 4);
             player3.addLyricEvent("zing", 10);
             player3.addNote(new Pitch('B').toMidiNote(), 10, 1);
             player3.addNote(new Pitch('G').toMidiNote(), 11, 1);
             player3.addLyricEvent("grace!", 12);
             player3.addNote(new Pitch('B').toMidiNote(), 12, 4);
             player3.addLyricEvent("How", 16);
             player3.addNote(new Pitch('A').toMidiNote(), 16, 2);
             player3.addLyricEvent("sweet", 18);
             player3.addNote(new Pitch('G').toMidiNote(), 18, 4);
             player3.addLyricEvent("the", 22);
             player3.addNote(new Pitch('E').toMidiNote(), 22, 2);
             player3.addLyricEvent("sound", 24);
             player3.addNote(new Pitch('D').toMidiNote(), 24, 4);
             player3.addLyricEvent("That", 28);
             player3.addNote(new Pitch('D').toMidiNote(), 28, 2);
             player3.addLyricEvent("saved", 31);
             player3.addNote( new Pitch('G').toMidiNote(), 30, 4);
             player3.addLyricEvent("a", 34);
             player3.addNote( new Pitch('B').toMidiNote(), 34, 1);
             player3.addNote(new Pitch('G').toMidiNote(), 35, 1);
             player3.addLyricEvent("wretch", 36);
             player3.addNote(new Pitch('B').toMidiNote(), 36, 4);
             player3.addLyricEvent("like", 40);
             player3.addNote(new Pitch('A').toMidiNote(), 40, 2);
             player3.addLyricEvent("me", 42);
             player3.addNote(new Pitch('D').transpose(Pitch.OCTAVE).toMidiNote(), 42, 8);
             
             //creates a new player for piece 2
             player2 = new SequencePlayer(200, 12, listener);
             
             //adds the necessary notes for the player for piece 2
             
             player2.addNote(new Pitch('F').transpose(1).toMidiNote(), 0, 6);
             player2.addNote(new Pitch('E').transpose(Pitch.OCTAVE).toMidiNote(), 0, 6);
             player2.addNote(new Pitch('F').transpose(1).toMidiNote(), 6, 6);
             player2.addNote(new Pitch('E').transpose(Pitch.OCTAVE).toMidiNote(), 6, 6);
             player2.addNote(new Pitch('F').transpose(1).toMidiNote(), 18, 6);
             player2.addNote(new Pitch('E').transpose(Pitch.OCTAVE).toMidiNote(), 18, 6);
             player2.addNote(new Pitch('F').transpose(1).toMidiNote(), 30, 6);
             player2.addNote(new Pitch('C').transpose(Pitch.OCTAVE).toMidiNote(), 30, 6);
             player2.addNote(new Pitch('F').transpose(1).toMidiNote(), 36, 12);
             player2.addNote(new Pitch('E').transpose(Pitch.OCTAVE).toMidiNote(), 36, 12);
             player2.addNote(new Pitch('G').toMidiNote(), 48, 12);
             player2.addNote(new Pitch('B').toMidiNote(), 48, 12);
             player2.addNote(new Pitch('G').transpose(Pitch.OCTAVE).toMidiNote(), 48, 12);
             player2.addNote(new Pitch('G').toMidiNote(), 72, 12);
             player2.addNote(new Pitch('C').transpose(Pitch.OCTAVE).toMidiNote(), 96, 18);
             player2.addNote(new Pitch('G').toMidiNote(), 114, 6);
             player2.addNote(new Pitch('E').toMidiNote(), 132, 12);
             player2.addNote(new Pitch('E').toMidiNote(), 144, 6);
             player2.addNote(new Pitch('A').toMidiNote(), 150, 12);
             player2.addNote(new Pitch('B').toMidiNote(), 162, 12);
             player2.addNote(new Pitch('B').transpose(-1).toMidiNote(), 174, 6);
             player2.addNote(new Pitch('A').toMidiNote(), 180, 12);
             player2.addNote(new Pitch('G').toMidiNote(), 192, 8);
             player2.addNote(new Pitch('E').transpose(Pitch.OCTAVE).toMidiNote(), 200, 8);
             player2.addNote(new Pitch('G').transpose(Pitch.OCTAVE).toMidiNote(), 208, 8);
             player2.addNote(new Pitch('A').transpose(Pitch.OCTAVE).toMidiNote(), 216, 12);
             player2.addNote(new Pitch('F').transpose(Pitch.OCTAVE).toMidiNote(), 228, 6);
             player2.addNote(new Pitch('G').transpose(Pitch.OCTAVE).toMidiNote(), 234, 6);
             player2.addNote(new Pitch('E').transpose(Pitch.OCTAVE).toMidiNote(), 246, 12);
             player2.addNote(new Pitch('C').transpose(Pitch.OCTAVE).toMidiNote(), 258, 6);
             player2.addNote(new Pitch('D').transpose(Pitch.OCTAVE).toMidiNote(), 264, 6);
             player2.addNote(new Pitch('B').toMidiNote(), 270, 9);
             
             //creates a new SequencePlayer for the first piece
             player1 = new SequencePlayer(140, 12, listener);
             
             //adds all the necessary notes to the SequencePlayr for the first piece
             player1.addNote(new Pitch('C').toMidiNote(), 0, 12);
             player1.addNote(new Pitch('C').toMidiNote(), 12, 12);
             player1.addNote(new Pitch('C').toMidiNote(), 24, 9);
             player1.addNote(new Pitch('D').toMidiNote(), 33, 3);
             player1.addNote(new Pitch('E').toMidiNote(), 36, 12);
             player1.addNote(new Pitch('E').toMidiNote(), 48, 9);
             player1.addNote(new Pitch('D').toMidiNote(), 57, 3);
             player1.addNote(new Pitch('E').toMidiNote(), 60, 9);
             player1.addNote(new Pitch('F').toMidiNote(), 69, 3);
             player1.addNote(new Pitch('G').toMidiNote(), 72, 24);
             player1.addNote(new Pitch('C').transpose(Pitch.OCTAVE).toMidiNote(), 96, 4);
             player1.addNote(new Pitch('C').transpose(Pitch.OCTAVE).toMidiNote(), 100, 4);
             player1.addNote(new Pitch('C').transpose(Pitch.OCTAVE).toMidiNote(), 104, 4);
             player1.addNote(new Pitch('G').toMidiNote(), 108, 4);
             player1.addNote(new Pitch('G').toMidiNote(), 112, 4);
             player1.addNote(new Pitch('G').toMidiNote(), 116, 4);
             player1.addNote(new Pitch('E').toMidiNote(), 120, 4);
             player1.addNote(new Pitch('E').toMidiNote(), 124, 4);
             player1.addNote(new Pitch('E').toMidiNote(), 128, 4);
             player1.addNote(new Pitch('C').toMidiNote(), 132, 4);
             player1.addNote(new Pitch('C').toMidiNote(), 136, 4);
             player1.addNote(new Pitch('C').toMidiNote(), 140, 4);
             player1.addNote(new Pitch('G').toMidiNote(), 144, 9);
             player1.addNote(new Pitch('F').toMidiNote(), 153, 3);
             player1.addNote(new Pitch('E').toMidiNote(), 156, 9);
             player1.addNote(new Pitch('D').toMidiNote(), 165, 3);
             player1.addNote(new Pitch('C').toMidiNote(), 168, 24);
             
             player = new SequencePlayer(120, 2, listener);
 
             player.addLyricEvent("Up!", 0);
             player.addNote(new Pitch('C').toMidiNote(), 0, 1);
             player.addNote(new Pitch('D').toMidiNote(), 1, 1);
             player.addNote(new Pitch('E').toMidiNote(), 2, 1);
             player.addNote(new Pitch('F').toMidiNote(), 3, 1);
             player.addNote(new Pitch('G').toMidiNote(), 4, 1);
             player.addNote(new Pitch('A').toMidiNote(), 5, 1);
             player.addNote(new Pitch('B').toMidiNote(), 6, 1);
             player.addNote(new Pitch('C').transpose(Pitch.OCTAVE).toMidiNote(), 7, 1);
             player.addLyricEvent("Down!", 8);
             player.addNote(new Pitch('B').toMidiNote(), 8, 1);
             player.addNote(new Pitch('A').toMidiNote(), 9, 1);
             player.addNote(new Pitch('G').toMidiNote(), 10, 1);
             player.addNote(new Pitch('F').toMidiNote(), 11, 1);
             player.addNote(new Pitch('E').toMidiNote(), 12, 1);
             player.addNote(new Pitch('D').toMidiNote(), 13, 1);
             player.addNote(new Pitch('C').toMidiNote(), 14, 1);
 
             System.out.println(player);
 
             // play specifid player (player1, player2, player3)
             player2.play();
 
             /*
              * Note: A possible weird behavior of the Java sequencer: Even if the
              * sequencer has finished playing all of the scheduled notes and is
              * manually closed, the program may not terminate. This is likely
              * due to daemon threads that are spawned when the sequencer is
              * opened but keep on running even after the sequencer is killed. In
              * this case, you need to explicitly exit the program with
              * System.exit(0).
              */
             // System.exit(0);
 
         } catch (MidiUnavailableException e) {
             e.printStackTrace();
         } catch (InvalidMidiDataException e) {
             e.printStackTrace();
         }
     }
 }
