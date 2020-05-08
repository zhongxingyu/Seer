 package net.tuschhcm.routercontrol;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * Class to represent a router preset.
  */
 public class Preset {
     private static final String UNNAMED = "<Unnamed Preset>";
 
     /**
      * Name of the preset.
      */
     private String mName;
 
     /**
      * List of inputs for outputs.
      * List index corresponds with the output.
      */
     private final ArrayList<Integer> mInputs;
 
     /**
      * Load a preset from the given file.
      *
      * @param file The file to load.
      * @return the Preset object
      * @throws IllegalArgumentException if the preset can't be loaded.
      */
     public static Preset loadPresetFile(File file) throws IllegalArgumentException {
         try {
             Preset preset = new Preset();
 
             BufferedReader in = new BufferedReader(new FileReader(file));
             int lineno = 1;
 
             String line = in.readLine();
             if (line == null) {
                 throw new IllegalArgumentException("Empty preset file: " + file);
             }
 
             // The name is optional and starts with a #
             if (line.charAt(0) == '#') {
                 preset.mName = line.substring(1).trim();
                 line = in.readLine();
                 lineno++;
             }
 
             // Read the inputs.
             int output = 1;
             while (line != null && !line.equals("")) {
                 try {
                     final Integer input = Integer.valueOf(line.trim());
                     preset.mInputs.add(input);
                     output++;
 
                 } catch (final NumberFormatException nfe) {
                     throw new IllegalArgumentException(
                         String.format("%s: Input set for output %d on line %d is not a number.",
                                 file, output, lineno));
                 }
 
                 line = in.readLine();
                 lineno++;
             }
 
             in.close();
 
             return preset;
 
         } catch (final IOException e) {
             throw new IllegalArgumentException("Could not read " + file, e);
         }
     }
 
     /**
      * Constructor for Preset.
      */
     public Preset() {
         mName = UNNAMED;
         mInputs = new ArrayList<Integer>();
     }
 
     /**
      * Get the name of the preset.
      *
      * @return The preset name
      */
     public String getName() {
         return mName;
     }
 
     /**
      * Get an input for the given output.
      *
      * @param output
      * @return Input number, or 0 if no change.
      * @throws IllegalArgumentException if the output isn't defined.
      */
     public int getInputForOutput(final int output) {
         final int index = output - 1;
         if (index < 0 || index > mInputs.size()) {
             throw new IllegalArgumentException("Output out of range.");
         }
 
        return mInputs.get(index);
     }
 
     /**
      * @return The number of outputs defined in this preset.
      */
     public int getNumberOfOutputs() {
         return mInputs.size();
     }
 
     /**
      * Return a string representation of this preset.
      */
     public String toString() {
         String str = "";
         if (!mName.equals(UNNAMED)) {
             str = "# " + mName + "\n";
         }
 
         for (Integer in : mInputs) {
             str += in + "\n";
         }
 
         return str;
     }
 }
