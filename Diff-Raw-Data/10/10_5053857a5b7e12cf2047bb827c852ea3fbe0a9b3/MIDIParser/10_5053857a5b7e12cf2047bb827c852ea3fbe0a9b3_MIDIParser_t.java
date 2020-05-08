 package com.flexymind.alpha.parsers;
 
 import com.flexymind.alpha.player.Note;
 import com.flexymind.alpha.player.Tone;
 import com.leff.midi.MidiFile;
 import com.leff.midi.event.MidiEvent;
 import com.leff.midi.event.NoteOn;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.SortedSet;
 
 public class MIDIParser {
 //    protected final static int DEFAULT_DURATION = 480;
 
     protected List<Note> listNotes;

     public MIDIParser(File file) throws IOException{
         listNotes = new ArrayList<Note>();
         parseFile(new MidiFile(file));
     }
 
     public MIDIParser(InputStream file) throws IOException{
         listNotes = new ArrayList<Note>();
         parseFile(new MidiFile(file));
     }
 
     public List<Note> getNotes(){
         return listNotes;
     }
 
     private void parseFile(MidiFile mFile){
        SortedSet<MidiEvent> eventTreeSet = mFile.getTracks().get(1).getEvents();
         for (MidiEvent event : eventTreeSet) {
            if ((event instanceof NoteOn) && (Tone.getToneById(((NoteOn) event).getNoteValue()) != Tone.UNKNOW)) {
                Note note = new Note(Tone.getToneById(((NoteOn) event).getNoteValue()));
                 listNotes.add(note);
             }
         }
 
     }
 
 }
