 package com.feelthebeats.js.ftb2om;
 
 import java.io.*;
 import java.util.*;
 
 public class ConverterMain {
 
     public static final String ERROR_ARGUMENTS = "Required arguments: [input file] [output file] [BPM]";
     public static final String ERROR_IO = "ERROR: File not found, or error opening file.";
 
     public static final String MSG_GREET = "ftb->osumania converter by JS. js.nexen@gmail.com";
     public static final String MSG_SCANNING = "Scanning input file...";
     public static final String MSG_SCANNING_DONE = "Done scanning. Generating converted text...";
     public static final String MSG_DONE = "Done! Now make a new file in osu if you haven't already, and paste the text in.";
 
     private static File inFile;
     private static File outFile;
     private static BufferedReader reader;
     private static PrintWriter writer;
 
     private static double mainBpm;
 
     private static LinkedList<BPM> bpms;
     private static LinkedList<Note> notes;
 
     public static void main(String[] args) {
         System.out.println(MSG_GREET);
         bpms = new LinkedList<BPM>();
         notes = new LinkedList<Note>();
         if (args.length != 3) {
             System.out.println(ERROR_ARGUMENTS);
         } else {
             inFile = new File(args[0]);
             outFile = new File(args[1]);
             mainBpm = Double.parseDouble(args[2]);
             try {
                 reader = new BufferedReader(new FileReader(inFile));
                 writer = new PrintWriter(outFile);
                 System.out.println(MSG_SCANNING);
                 String readLine;
                 while ((readLine = reader.readLine()) != null) {
                     if (readLine.startsWith("###"))
                         continue;
                     else if (readLine.startsWith("BPM")) {
                         String[] bpmData = readLine.split(" ");
                         bpms.add(new BPM(
                                 (int) Math.round(Double.parseDouble(bpmData[1])),
                                 mainBpm * Double.parseDouble(bpmData[2]) / 120
                         ));
                     } else {
                         String[] noteData = readLine.split(" ");
                         String[] noteTimeData = noteData[0].split("-");
                         double beatLength = 0;
                         int noteTimeStart = (int) Math.round(Double.parseDouble(noteTimeData[0]));
                         if (noteTimeData.length > 1) {
                             int noteTimeEnd = (int) Math.round(Double.parseDouble(noteTimeData[1]));
                             boolean bpmFound = false;
                             for (ListIterator<BPM> bpmIterator = bpms.listIterator(); bpmIterator.hasNext(); ) {
                                 BPM bpm = bpmIterator.next();
                                if (bpm.getTime() > noteTimeStart) {
                                     bpm = bpmIterator.previous();
                                     bpm = bpmIterator.previous();
                                    double noteTimeLength = (noteTimeEnd - noteTimeStart);
                                    beatLength = noteTimeLength * bpm.getValue() / 60000;
                                     bpmFound = true;
                                 }
                                 if (bpmFound) break;
                             }
                         }
                         notes.add(new Note(
                                 noteTimeStart,
                                 Integer.parseInt(noteData[2]),
                                 beatLength
                         ));
                     }
                 }
                 System.out.println(MSG_SCANNING_DONE);
                 writer.println("[TimingPoints]");
                 while (!bpms.isEmpty()) {
                     BPM bpm = bpms.remove();
                     writer.println(bpm.getTime() + ","
                             + 60000 / ((bpm.getValue() != 0) ? bpm.getValue() : 0.01)
                             + ",4,1,0,100,1,0"
                     );
                 }
                 writer.println();
                 writer.println("[HitObjects]");
                 while (!notes.isEmpty()) {
                     Note note = notes.remove();
                     int noteX = (512 / 14) * ((note.getColumn() - 1) * 2 + 1);
                     int noteY = 192;
                     writer.println(noteX + ","
                             + noteY + ","
                             + note.getTime()
                             + ((note.getBeatLength() > 0)
                            ? (",2,0,B|" + noteX + ":32,8," + note.getBeatLength() * 70/4) : ",1,0")
                     );
                 }
                 writer.close();
                 reader.close();
                 System.out.println(MSG_DONE);
             } catch (IOException e) {
                 System.out.println(ERROR_IO);
             }
         }
     }
 }
