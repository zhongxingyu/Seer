 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is Colin J. Fuller's code.
  *
  * The Initial Developer of the Original Code is
  * Colin J. Fuller.
  * Portions created by the Initial Developer are Copyright (C) 2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s): Colin J. Fuller
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.frontend;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.Hashtable;
 
 import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
 
 /**
  * Summarizes the output from analysis routines, combining all output from a directory into a single unified file.
  * If objects have been clustered into groups, summarizes only over those groups, omitting data on individual objects.
  *
  * @author Colin J. Fuller
  *
  */
 
 
 public class DataSummary {
 
     private static double[] newAverages(int numChannels) {
 
         double[] averages = new double[numChannels];
         for (int i = 0; i < numChannels; i++) {
             averages[i] = 0.0;
         }
         return averages;
 
     }
 
 
     /**
      * Creates a summary of output files created by the analysis program.
      *
      * @param directory     The full path to the directory containing the output files to be summarized.
      * @param parameterDirectory       The directory that stores the parameters for the analysis.
      * @throws java.io.IOException      If any problems reading the analysis output files or writing the summary to disk ar encountered.
      */
     public static void SummarizeData(String directory, String parameterDirectory) throws java.io.IOException {
 
 
         final String outputFileExtension = ".out.txt";
 
         File dir = new File(directory);
 
         if (! dir.exists()) {
 
             dir.mkdir();
 
         }
 
         File outputFile = new File(directory + File.separator + "summary.txt");
 
 
 
         PrintWriter output = new PrintWriter(new FileOutputStream(outputFile));
 
 
         int numChannels = 0;
 
         boolean headerRowWritten = false;
 
         for (File f : dir.listFiles()) {
 
 
 
             if (! f.getName().matches(".*" + outputFileExtension)) {continue;}
 
             if (! headerRowWritten) {
 
                 File parameterFile = new File(parameterDirectory + File.separator + f.getName().replace(outputFileExtension, AnalysisController.PARAMETER_EXTENSION));
 
                 ParameterDictionary params = ParameterDictionary.readParametersFromFile(parameterFile.getAbsolutePath());
 
                 numChannels = Integer.parseInt(params.getValueForKey("number_of_channels"));
 
                 // column headers
 
                 output.print("cell_number" + " ");
                 for (int i = 0; i < numChannels; i++) {
 
                     output.print(params.getValueForKey("channel_name").split(" ")[i]);
                     output.print(" ");
                 }
 
                 for (int i = 0; i < numChannels; i++) {
                     output.print(params.getValueForKey("channel_name").split(" ")[i] + "_background ");
 
                 }
 
                 output.print("number_of_centromeres_in_cell ");
 
                 output.println("average_centromere_size");
 
 
 
                 headerRowWritten = true;
 
             }
 
 
 
 
             System.out.println(f.getName());
 
             int ch1Pos = 0;
             int ch2Pos = 0;
             int bothPos = 0;
             Hashtable<Integer, double[]> regions = new Hashtable<Integer, double[]>();
             Hashtable<Integer, int[]> counts = new Hashtable<Integer, int[]>();
             //Hashtable<Integer, Double> ch0BG = new Hashtable<Integer, Double>();
             //Hashtable<Integer, Double> ch2BG = new Hashtable<Integer, Double>();
             Hashtable<Integer, Double[]> allBG = new Hashtable<Integer, Double[]>();
             Hashtable<Integer, Double> average_sizes = new Hashtable<Integer, Double>();
 
 
             BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
 
             String line;
 
             while ((line = b.readLine()) != null) {
 
 
                 String[] splitline = line.split(" ");
 
                 int regionID = (int) Double.parseDouble(splitline[splitline.length - 1]);
                 int regionType = 0;
 
                 if (! regions.containsKey(regionID)){
                     regions.put(regionID, newAverages(numChannels));
                     int[] countsArr = new int[4];
                     countsArr[0] = 0; countsArr[1] = 0; countsArr[2] = 0; countsArr[3] = 0;
                     counts.put(regionID, countsArr);
                     average_sizes.put(regionID, 0.0);
                 }
 
                 double[] averages = regions.get(regionID);
                 int[] averageCounts = counts.get(regionID);
 
                 for (int i = 0; i < numChannels; i++) {
 
                     //averages[i] += Double.parseDouble(splitline[i])/Double.parseDouble(splitline[numChannels]);
                     averages[i] += Double.parseDouble(splitline[i]);
                 }
 
                 averageCounts[regionType] += 1;
                 Double[] tempBg = new Double[numChannels];
                 for (int i = 0; i < numChannels; i++) {
                     tempBg[i] = Double.parseDouble(splitline[numChannels+i+1]);
 
                 }
 
                 average_sizes.put(regionID, average_sizes.get(regionID)+ Double.parseDouble(splitline[numChannels]));
 
                 //ch0BG.put(regionID, Double.parseDouble(splitline[numChannels+1]));
                 //ch2BG.put(regionID, Double.parseDouble(splitline[numChannels+2]));
 
                 allBG.put(regionID, tempBg);
 
             }
             output.println(f.getName());
 
             for (int key : regions.keySet()) {
 
                 if (key == 0) continue; //things found as centromeres but excluded by clustering have region 0 -- exclude them
 
                 output.print("" + key + " ");
 
                 int totalCounts = counts.get(key)[0] + counts.get(key)[1] + counts.get(key)[2] + counts.get(key)[3];
 
                 for (int i = 0; i < numChannels; i++) {
                     output.print("" + regions.get(key)[i]/totalCounts + " ");
                 }
                 for (int i = 0; i < numChannels; i++) {
                     output.print("" + allBG.get(key)[i] + " ");
                 }
                 //output.println("" + counts.get(key)[0]*1.0/totalCounts + " " + counts.get(key)[1]*1.0/totalCounts + " " + counts.get(key)[2]*1.0/totalCounts + " " + counts.get(key)[3]*1.0/totalCounts);
 
                 output.print("" + totalCounts + " ");
 
                 output.println("" + average_sizes.get(key)/totalCounts);
 
             }
 
         }
 
         output.close();
 
 
     }
 
 
 
 }
