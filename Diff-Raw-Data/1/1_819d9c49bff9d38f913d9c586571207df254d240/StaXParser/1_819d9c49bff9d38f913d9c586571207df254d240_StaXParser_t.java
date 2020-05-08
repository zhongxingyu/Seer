 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.XMLEvent;
 
 
 public class StaXParser {
     static final String PART = "part";
     static final String MEASURE = "measure";
     static final String NOTE = "note";
     static final String PITCH = "pitch";
     static final String STEP = "step";
     static final String ALTER = "alter";
     static final String OCTAVE = "octave";
     static final String CHORD = "chord";
     static final String TIE = "tie";
     static final String BACKUP =  "backup";
     static final String DURATION = "duration";
     static final String VOICE = "voice";
     static final String KEY = "key";
     static final String ATTRIBUTES = "attributes";
     static final String MODE = "mode";
     static final String FIFTHS = "fifths";
 
     public void readConfig(String configFile) {
         try {
 
             Converter c = new Converter();
 
             //input a note and string key and measure
             //method "what_key" given (fifths and mode) outputs string which is the key
 
             // First create a new XMLInputFactory
             XMLInputFactory inputFactory = XMLInputFactory.newInstance();
             // Setup a new eventReader
             System.out.println(configFile);
             InputStream in = new FileInputStream(configFile);
             XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
 
             FileWriter fw = new FileWriter("newXML.xml");
 
             BufferedWriter bw = new BufferedWriter(fw);
 
             // Read the XML document
             String mode = "";
             int fifths = 0;
             while (eventReader.hasNext()) {
                 XMLEvent event = eventReader.nextEvent();
 
                 if (event.isStartElement() && 
                         event.asStartElement().getName().toString().equals(PART)){
 
                     while(eventReader.hasNext()){
                         event = eventReader.nextEvent();
                         //System.out.println("GOES");
                         boolean endMeasure = false;
                         if(event.isStartElement() && 
                                 event.asStartElement().getName().toString().equals(MEASURE)){
                             //System.out.println("GOES INTO MEASURE");
                             bw.write(event.toString());
                             ArrayList<Note> measure = new ArrayList<Note>();
                             int counter = 0;
                             boolean endAttribute = false;
 
                             while(eventReader.hasNext() && !endMeasure){
                                 event = eventReader.nextEvent();
 
                                 if(event.isStartElement() && 
                                         event.asStartElement().getName().toString().equals(ATTRIBUTES)){
                                     while(eventReader.hasNext() && !endAttribute){
                                         if(event.isEndElement() &&
                                                 event.asEndElement().getName().toString().equals(ATTRIBUTES)){
                                             bw.write(event.toString());
                                             endAttribute = true;
                                         }
                                         else if(event.isStartElement()){
                                             String currentTag = event.asStartElement().getName().toString();
                                             bw.write(event.toString());
                                             event = eventReader.nextEvent();
                                             if(currentTag.equals(KEY)){
 
                                                 boolean endKey = false;
 
                                                 while(eventReader.hasNext() && !endKey){
                                                     if(event.isEndElement() &&
                                                             event.asEndElement().getName().toString().equals(KEY)){
                                                         endKey = true;
                                                     }
                                                     else if(event.isStartElement()){
                                                         currentTag = event.asStartElement().getName().toString();
 
                                                         bw.write(event.toString());
                                                         if(event.isStartElement() &&
                                                                 event.asStartElement().getName().toString().equals(FIFTHS)){
 
                                                             event = eventReader.nextEvent();
                                                             bw.write(event.toString());
                                                             if(event.isCharacters()){
                                                                 String character = event.asCharacters().getData().toString();
                                                                 int num = Integer.parseInt(character);
                                                                 fifths = num;
                                                             }
                                                         }
 
                                                         else if(event.isStartElement() && 
                                                                 event.asStartElement().getName().toString().equals(MODE)){
 
                                                             event = eventReader.nextEvent();
                                                             bw.write(event.toString());
                                                             if(event.isCharacters()){
                                                                 String character = event.asCharacters().getData().toString();
                                                                 mode = character;
                                                             }
                                                         }
 
                                                     }
                                                     else
                                                         bw.write(event.toString());
                                                     if(eventReader.hasNext() && !endKey)
                                                         event = eventReader.nextEvent();
                                                 }
                                             }
                                         }
                                         else{
                                             bw.write(event.toString());
                                             if(eventReader.hasNext() && !endAttribute)
                                                 event = eventReader.nextEvent();
                                         }
                                     }
                                 }
                                 else if(event.isEndElement() &&
                                         event.asEndElement().getName().toString().equals(MEASURE)){
                                     bw.write(event.toString());
                                     endMeasure = true;
                                 }
 
                                 //New Note
                                 else if(event.isStartElement() && 
                                         event.asStartElement().getName().toString().equals(NOTE)){
                                     //System.out.println("Goes in here and mode = " + mode + "\n fifth = " + fifth);
                                     Note current = new Note();
                                     current.setMode(mode);
                                     current.setFifth(fifths);
                                     String key = c.whatKey(mode, fifths);
                                     boolean endNote = false;
                                     String currentTag = "";
                                     Note newNote = new Note();
 
                                     while(eventReader.hasNext() && !endNote){
 
                                         if(event.isEndElement() &&
                                                 event.asEndElement().getName().toString().equals(NOTE)){
                                             bw.write(event.toString());
                                             endNote = true;
                                         }
 
                                         if(event.isStartElement()){
                                             currentTag = event.asStartElement().getName().toString();
 
                                             bw.write(event.toString());
                                             if(currentTag.equals(CHORD))
                                                 current.setChord(true);
                                         }
 
                                         if(event.isEndElement() && !endNote){
                                             String eventName = event.asEndElement().getName().toString();
                                             if(eventName.equals(STEP))
                                                 bw.write(newNote.getStep() + event.toString());
                                             else if(eventName.equals(ALTER))
                                                 bw.write(newNote.getAlter() + event.toString());
                                             else if(eventName.equals(OCTAVE))
                                                 bw.write(newNote.getOctave() + event.toString());
                                             else
                                             	bw.write(event.toString());
                                             event = eventReader.nextEvent();
                                         }
 
                                         if(event.isCharacters()){
 
 
                                             String character = event.asCharacters().getData().toString();
 
                                             boolean pitch = false;
 
                                             //System.out.println(currentTag + " : " + character);
                                             if(currentTag.equals(STEP) && current.getStep().length()!=1){
                                                 current.setStep(character);
                                                 //putting hash but here is where you change it
                                                 pitch = true;
                                             }
 
                                             if(currentTag.equals(ALTER) && current.getAlter() == 0){
                                                 current.setAlter(Integer.parseInt(character));
                                                 //where you change alter
 
                                             }
 
                                             if(currentTag.equals(OCTAVE) && current.getOctave() == 0){
                                                 current.setOctave(Integer.parseInt(character));
                                                 //change octave code
 
                                             }
 
 
                                             if(pitch){
                                                 newNote = c.convert(current, key, "toMin", measure);
                                             }
 
                                             if(currentTag.equals(DURATION) && current.getDuration() == 0){
                                                 current.setPosition(counter);
                                                 current.setDuration(Integer.parseInt(character));
                                                 counter = counter + current.getDuration();
                                                 bw.write(event.toString());
                                             }
 
                                             if(currentTag.equals(VOICE) && current.getVoice() == 0){
                                                 current.setVoice(Integer.parseInt(character));
                                                 System.out.println(event.asCharacters().getData());
                                                 bw.write(character);
                                             }
                                         }
                                         //System.out.println(event.asCharacters().getData());
                                         event = eventReader.nextEvent();
                                     }
                                     System.out.println(current);
                                     System.out.println("********");
                                     measure.add(current);
 
                                     //System.out.println(measure.size());
                                 }
                                 else if(event.isStartElement() && 
                                         event.asStartElement().getName().toString().equals(BACKUP)){
                                	bw.write(event.toString());
                                     event = eventReader.nextEvent();
                                     if(event.isStartElement() && event.asStartElement().getName().toString().equals(DURATION)){
                                         event = eventReader.nextEvent();
                                         if(event.isCharacters()){
                                             String character = event.asCharacters().getData().toString();
                                             int num = Integer.parseInt(character);
                                             counter = counter - num;
                                         }
                                     }
                                 }
                                 else
                                     bw.write(event.toString());    
 
                             }
 
                         }
                         else
                             bw.write(event.toString());
                     }
                 }
                 else
                     bw.write(event.toString());
             }
             bw.close();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (XMLStreamException e) {
             e.printStackTrace();
         }catch (IOException e) {
             e.printStackTrace();
         }
     }
 } 
 
