 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package utilidades;
 
 import com.sun.media.sound.MidiUtils;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import javax.sound.midi.*;
 
 /**
  *
  * @author mota
  */
 public class MIDIReader {
     private String filename;
     private Sequence sequencia;
     private Sequencer player;
     private Receiver recebedor;
     private MidiUtils.TempoCache tempoProcessor;
     private float duration;
     private float[][] notes;
     private float[][] realNotes;
     private float interval;
     private boolean canPlay;
     public MIDIReader(String filename){
         this.filename = filename;
         try {
             this.recebedor = MidiSystem.getReceiver();
             this.sequencia = MidiSystem.getSequence(new File(filename));
             this.tempoProcessor = new MidiUtils.TempoCache(sequencia);
             this.player = MidiSystem.getSequencer(true);
             this.player.setSequence(sequencia);
             this.player.open();
             this.interval = 0.5f;
             this.loadNotes();
             this.duration = this.getRealDuration();
         } catch (Exception ex) {
             Utilidades.alertar(ex.getMessage());
         }
     }
     private void loadNotes(){
         int program = 0;
         ArrayList< ArrayList<Number> > notas = new ArrayList< ArrayList<Number>>(); // Cria um ArrayList com as notas, que devem vir a ser a matriz com as notas por si so
          ArrayList< ArrayList<Number> > realNotas = new ArrayList< ArrayList<Number>>();
         //int[] chords = new int[]{64, 69, 74, 79, 83, 88};
         int[] chords = new int[]{21, 43, 63, 84, 106, 128};        
         int maxNote = 0;
         HashMap<Integer, Float> lastTimeNote = new HashMap<Integer, Float>();
         for (Track track:  sequencia.getTracks()) {
             for(int c=0;c<track.size();++c){
                 MidiEvent event = track.get(c);
                 MidiMessage msg = event.getMessage();
                 if(msg instanceof ShortMessage){
                     ShortMessage shortmsg = (ShortMessage) msg;
                     if(shortmsg.getCommand() == ShortMessage.PROGRAM_CHANGE){
                         program = shortmsg.getData1();
                     }
                     else if(program>=0 && program <= 128){
                     //else if(program>=25 && program <= 40){
                     //else if(program== 30){
                         if(shortmsg.getCommand() == ShortMessage.NOTE_ON){
                             int note = shortmsg.getData1();
                             int noteChord = 1;
                             for (int chord: chords){
                                 if (note < chord){
                                     break;
                                 }
                                 noteChord++; 
                             }
                             //tocador.start();
                             float noteSecond = MidiUtils.tick2microsecond(sequencia, event.getTick(), this.tempoProcessor)/1000000.0f;
                             if(!lastTimeNote.containsKey(noteChord)){
                                 lastTimeNote.put(noteChord, 0.0f);
                             }
                             if(noteSecond-lastTimeNote.get(noteChord).floatValue()<=this.interval){
                                 continue;
                             }
                             lastTimeNote.put(noteChord,noteSecond);
                             //System.out.println("Play chord "+noteChord+" in "+noteSecond+" seconds");
                             ArrayList<Number> lastNote = new ArrayList<Number>();
                             if(notas.size() > 0){
                                 lastNote = notas.get(notas.size()-1);
                                 int theIndex = 0;
                                 float lastSecond = (float) 0.0;
                                 boolean exists = false;
                                 for(ArrayList<Number> aNota: notas){
                                     if(aNota.get(0).floatValue() == noteSecond){
                                         exists = true;
                                         lastNote = aNota;
                                     }
                                     else if(lastSecond<noteSecond && aNota.get(0).floatValue()>noteSecond){
                                         exists = false;
                                         break;
                                     }
                                     lastSecond = aNota.get(0).floatValue();
                                     theIndex++;
                                 }
                                 if(exists){
                                     if(!lastNote.contains(noteChord)){
                                         lastNote.add(noteChord);
                                     }
                                 }
                                 else{
                                     lastNote = new ArrayList<Number>();
                                     lastNote.add(noteSecond);
                                     lastNote.add(noteChord);
                                     notas.add(theIndex,lastNote);
                                 }
                                 
                             }
                             else{
                                 lastNote = new ArrayList<Number>();
                                 lastNote.add(noteSecond);
                                 lastNote.add(noteChord);
                                 notas.add(lastNote);
                             }
                             if(realNotas.size() > 0){
                                lastNote = realNotas.get(realNotas.size()-1);
                                 int theIndex = 0;
                                 float lastSecond = (float) 0.0;
                                 boolean exists = false;
                                 for(ArrayList<Number> aNota: realNotas){
                                     if(aNota.get(0).floatValue() == noteSecond){
                                         exists = true;
                                         lastNote = aNota;
                                     }
                                     else if(lastSecond<noteSecond && aNota.get(0).floatValue()>noteSecond){
                                         exists = false;
                                         break;
                                     }
                                     lastSecond = aNota.get(0).floatValue();
                                     theIndex++;
                                 }
                                 if(exists){
                                     if(!lastNote.contains(note)){
                                         lastNote.add(c);
                                         lastNote.add(note);
                                     }
                                 }
                                 else{
                                     lastNote = new ArrayList<Number>();
                                     lastNote.add(noteSecond);
                                     lastNote.add(c);
                                     lastNote.add(note);
                                     realNotas.add(theIndex,lastNote);
                                 }
                             }
                             else{
                                 lastNote = new ArrayList<Number>();
                                 lastNote.add(noteSecond);
                                 lastNote.add(c);
                                 lastNote.add(note);
                                 realNotas.add(lastNote);
                             }
                             if(maxNote < lastNote.size()){
                                 maxNote = lastNote.size();
                             }
                         }
                         
                     }
                     
                 }
             }
         }
         //System.out.println("tamanho da pista "+notas.size()+" e track "+maxNote);
         this.notes = new float[notas.size()][maxNote];
         this.realNotes = new float[notas.size()][maxNote];
         for(int c=0;c < notas.size(); ++c){
             ArrayList<Number> notasTrack = notas.get(c);
             ArrayList<Number> realNotasTrack = realNotas.get(c);
             for(int c2=0; c2<notasTrack.size(); ++c2){
                 this.notes[c][c2] = (float)notasTrack.get(c2).floatValue();
                 this.realNotes[c][c2] = (float)realNotasTrack.get(c2).floatValue();
             }
         }
        // GameEngine.getInstance().setFramesPerSecond((int)(((tocador.getMicrosecondLength()/1000000)/(notas.size()*1.0))*4000));
        // System.out.println("(int)(("+sequencia.getMicrosecondLength()+"/1000000)/"+notas.size()+"="+(int)((sequencia.getMicrosecondLength()/1000000)/notas.size()))
     }
     
     public void setInterval(float interval){
         this.interval = interval;
     }
     public float getInterval(){
         return this.interval;
     }
     public void refresh(){
         this.loadNotes();
         this.duration = this.getRealDuration();
     }
     public float[][] getNotes(){
         float audioDuration  = this.getRealDuration();
         float ratio = audioDuration/this.duration;
         float[][] novaNotas = new float[notes.length][notes[0].length];
         int c = 0;
         for(float[] nota: notes){
             novaNotas[c] = nota;
             novaNotas[c][0] = novaNotas[c][0]/ratio;
             ++c;
         }
         return novaNotas;
     }
     public float[][] getTheNotes(){
         float audioDuration  = this.getRealDuration();
         float ratio = audioDuration/this.duration;
         float[][] novaNotas = new float[notes.length][realNotes[0].length];
         int c = 0;
         for(float[] nota: realNotes){
             novaNotas[c] = nota;
             novaNotas[c][0] = novaNotas[c][0]/ratio;
             ++c;
         }
         return novaNotas;
     }
     public float[][] getRealNotes(){
         return this.notes;
     }
     public float[][] getTrueNotes(){
         return this.realNotes;
     }
     public float getDuration(){
         return this.duration;
     }
     public float getRealDuration(){
         if(this.notes.length==0){
             return 0;
         }
         return this.notes[this.notes.length-1][0];
     }
     public void setDuration(float duration){
         this.duration = duration;
     }
     public void setDuration(){
         this.duration = this.getRealDuration();
     }
     public void play(){
         this.canPlay = true; 
     }
     public void stop(){
         this.canPlay = false; 
     }
     //Permite a execucao de uma nota do video
     public void tocar(float seconds, int corda){
         if(!this.canPlay){
             return;
         }
         int track = 0;
         int note;
         corda *= 2;
         try{
             for(float[] nota: this.realNotes){
                 if(nota[0] == seconds){
                     for(int c=1;c<nota.length;++c){
                         if(c%2 != 0){
                             track = (int)nota[c];
                             continue;
                         }
                         else if(c==corda*2){
                             note = (int)nota[c];
                             ShortMessage msg = new ShortMessage();
                             msg.setMessage(ShortMessage.NOTE_ON, track , note, 100);
                             this.recebedor.send(msg,(long)(nota[0]*1000000));
                         }   
                     }                
                 }
             }
         }
         catch(Exception ex){
             Utilidades.alertar("Erro na execucao do MIDI: "+ex.getMessage());
         }
     }
 }
