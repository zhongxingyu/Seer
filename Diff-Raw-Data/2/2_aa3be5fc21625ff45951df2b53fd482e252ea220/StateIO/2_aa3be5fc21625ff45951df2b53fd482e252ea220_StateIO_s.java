 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.exercise.persistence;
 
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.Exercise;
 import edu.gatech.statics.exercise.state.ExerciseState;
 import edu.gatech.statics.ui.InterfaceRoot;
 import edu.gatech.statics.ui.windows.knownforces.KnownsContainer;
 import edu.gatech.statics.ui.windows.knownforces.KnownsSidebarWindow;
 import edu.gatech.statics.util.Base64;
 import edu.gatech.statics.util.SolveListener;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.DataFormatException;
 import java.util.zip.Deflater;
 import java.util.zip.Inflater;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class StateIO {
 
     private StateIO() {
     }
 
     public static void saveToFile(String filename) {
         try {
             FileOutputStream output = new FileOutputStream(filename);
             String saveState = StateIO.saveState();
             output.write(saveState.getBytes());
             //System.out.println("State Data:");
             //System.out.println(saveState);
             System.out.println("State saved...");
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     public static void loadFromFile(String filename) {
         try {
             FileInputStream fileInput = new FileInputStream(filename);
            byte data[] = new byte[10 * 1024];
             int read = fileInput.read(data);
             String s = new String(data, 0, read);
 
             loadState(s);
         } catch (Exception ex) {
             Logger.getLogger("Statics").log(Level.SEVERE, "Exception in loading state", ex);
         }
     }
 
     /**
      * Transforms the existing state into a string representation.
      * @return
      */
     public static String saveState() {
 
         // 1) Encode the state to XML
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         StaticsXMLEncoder encoder = new StaticsXMLEncoder(bout);
 //        System.out.println("************ "+System.nanoTime());
         encoder.writeObject(Exercise.getExercise().getState());
 //        System.out.println("************ "+System.nanoTime());
         encoder.close();
         byte[] xmlData = bout.toByteArray();
 //System.out.println("OUTPUT: "+new String(xmlData));
 
         // 2) Zip it
         Deflater deflater = new Deflater();
         deflater.setInput(xmlData);
         deflater.finish();
 
         ByteArrayOutputStream zout = new ByteArrayOutputStream();
         byte[] partialData = new byte[1024];
         int dataCompressed;
         while ((dataCompressed = deflater.deflate(partialData)) != 0) {
             zout.write(partialData, 0, dataCompressed);
         }
 
         byte[] compressedData = zout.toByteArray();
         deflater.deflate(compressedData);
 
         // 3) Encode it with Base64 for convenience
         String encodedData = Base64.encodeBytes(compressedData);
         return encodedData;
     }
 
     /**
      * Loads and activates a state from the given string representation.
      * @param stateData
      */
     public static void loadState(String stateData) {
 
         // 1) Decode the data with Base64
         byte[] compressedData = Base64.decode(stateData);
 
         // 2) Unzip it
         Inflater inflater = new Inflater();
         inflater.setInput(compressedData);
         ByteArrayOutputStream zout = new ByteArrayOutputStream();
         try {
             int dataDecompressed;
             byte[] partialData = new byte[1024];
             while ((dataDecompressed = inflater.inflate(partialData)) != 0) {
                 zout.write(partialData, 0, dataDecompressed);
             }
         } catch (DataFormatException ex) {
             throw new IllegalArgumentException("State data did not include a valid state ");
         }
         byte[] xmlData = zout.toByteArray();
 
         // 3) Decode and activate the state
         ByteArrayInputStream bin = new ByteArrayInputStream(xmlData);
         StaticsXMLDecoder decoder = new StaticsXMLDecoder(bin);
         ExerciseState state = (ExerciseState) decoder.readObject();
 
         // init the exercise so that everything is kosher.
         Exercise.getExercise().initExercise();
 
         // now update UI elements
         // TODO: Make sure that UI is upadated?
 
         for (SolveListener solveListener : StaticsApplication.getApp().getSolveListeners()) {
             // attempt to catch the knowns container
             if (solveListener instanceof KnownsContainer) {
                 ((KnownsContainer) solveListener).update();
             }
         }
 //        for (TitledDraggablePopupWindow popup : InterfaceRoot.getInstance().getAllPopupWindows()) {
 //            if (popup instanceof DescriptionWindow) {
 //                ((DescriptionWindow) popup).update();
 //            }
 //            if (popup instanceof KnownLoadsWindow) {
 //                ((KnownLoadsWindow) popup).update();
 //            }
 //        }
 
     }
 }
