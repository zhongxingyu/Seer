 package org.geworkbench.components.parsers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.swing.ProgressMonitorInputStream;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
 import org.geworkbench.bison.parsers.resources.Resource;
 import org.geworkbench.components.parsers.microarray.DataSetFileFormat;
 
 /**
  * <p>Title: Sequence and Pattern Plugin</p>
  * <p>Description: </p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: </p>
  *
  * @version 1.0
  * @author xuegong wang
  */
 
 public class RMAExpressFileFormat extends DataSetFileFormat {
 
     static Log log = LogFactory.getLog(RMAExpressFileFormat.class);
 
     String[] maExtensions = {"txt", "tsv", "TSV"};
     ExpressionResource resource = new ExpressionResource();
     RMAExpressFilter maFilter = null;
 
     public RMAExpressFileFormat() {
        formatName = "RMA Express, Tab-Delimited";
         maFilter = new RMAExpressFilter();
         Arrays.sort(maExtensions);
     }
 
     public Resource getResource(File file) {
         try {
             resource.setReader(new BufferedReader(new FileReader(file)));
             resource.setInputFileName(file.getName());
         } catch (IOException ioe) {
             ioe.printStackTrace(System.err);
         }
         return resource;
     }
 
     public String[] getFileExtensions() {
         return maExtensions;
     }
 
     /**
      * This method is not used in getMArraySet(), since we don't need to spend time parse file twice.
      * In this method, we check that:
      * number of columns in header line is same as number of columns in data.
      * There are no duplicate markers (ie., no 2 markers have the same name).
      */
     // FIXME
     // In here we should also check (among other things) that:
     // * The values of the data points respect their expected type.    
     public boolean checkFormat(File file) {
         boolean columnsMatch = true;
         boolean noDuplicateMarkers = true;
         boolean valuesAreExpectedType = true;
     	try{
 	        FileInputStream fileIn = new FileInputStream(file);
 	        ProgressMonitorInputStream progressIn = new ProgressMonitorInputStream(null, "Checking File Format", fileIn);
 	        BufferedReader reader = new BufferedReader(new InputStreamReader(progressIn));
 	
 	        String line = null;
 	        int totalColumns = 0;
 	        List<String> markers = new ArrayList<String>();
 	        int lineIndex=0;
 	        int headerLineIndex=0;
 	        while ((line = reader.readLine()) != null) { //for each line
 	        	if ((line.indexOf("#") < 0)){//we'll skip comments and anything before header
 	        		if (headerLineIndex==0)//no header detected yet, then this is the header.
 	        			headerLineIndex=lineIndex;
 		            String token=null;
 			        int columnIndex = 0;
 			        int accessionIndex= 0;
 		            StringTokenizer st = new StringTokenizer(line, "\t\n");	            
 		            while (st.hasMoreTokens()) {	//for each column
 		                token = st.nextToken().trim();
 		                if (token.equals("")) {//header
 		                    accessionIndex = columnIndex;
 		                }else if ((headerLineIndex>0) && (columnIndex==0)){ // if this line is after header, then first column should be our marker name
 		                	if (markers.contains(token)){//duplicate markers
 		                		noDuplicateMarkers=false;
 		                	}else{
 		                		markers.add(token);
 		                	}
 		                }
 		                columnIndex++;
 		            }
 		            //check if column match or not
 		            if (headerLineIndex>0){ //if this line is real data, we assume lines after header are real data. (we might have bug here)
 		            	if (totalColumns==0)//not been set yet
 		            		totalColumns=columnIndex-accessionIndex;
 		            	else if (columnIndex!=totalColumns)//if not equal 
 		            		columnsMatch=false;
 		            }
 	        	}
 	        	lineIndex++;
 	        }
 	        fileIn.close();
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	    if (columnsMatch && noDuplicateMarkers)
 	    	return true;
 	    else
 	    	return false;
     }
 
     public DSDataSet getDataFile(File file) throws InputFileFormatException{
         return (DSDataSet) getMArraySet(file);
     }
 
     public DSMicroarraySet getMArraySet(File file) throws InputFileFormatException {
 
 //        if (!checkFormat(file))
 //            throw new InputFileFormatException("AffyFileFormat::getMArraySet - " + "Attempting to open a file that does not comply with the " + "Affy format.");
     	
         CSExprMicroarraySet maSet = new CSExprMicroarraySet();
         String fileName = file.getName();
         int dotIndex = fileName.lastIndexOf('.');
         if (dotIndex != -1) {
             fileName = fileName.substring(0, dotIndex);
         }
         maSet.setLabel(fileName);
         try {
             BufferedReader in = new BufferedReader(new FileReader(file));
             if (in != null) {
                 String header = in.readLine();
                 if (header == null) {
                     throw new InputFileFormatException("File is empty.");
                 }
                 while (header != null &&                 		
                 		(header.startsWith("#")
                 		|| header.startsWith("!"))		// for mantis issue: 1349
                 		|| StringUtils.isEmpty(header) 	// for mantis issue: 1349
                 		) {
                     header = in.readLine();                    
                 }
                 if (header == null) {
                     throw new InputFileFormatException("File is empty or consists of only comments.\n"+"RMAExpressFileFormat expected");
                 }
                 header = StringUtils.replace(header, "\"", ""); // for mantis issue:1349
                 StringTokenizer headerTokenizer = new StringTokenizer(header, "\t", false);
                 int n = headerTokenizer.countTokens();
                 if (n <= 1) {
                     throw new InputFileFormatException("Attempting to open a file that does not comply with the RMA Express format.\n"+"Invalid header: " + header);
                 }
                 n -= 1;
                 ArrayList<Float>[] values = new ArrayList[n];
                 for (int i = 0; i < n; i++) {
                     values[i] = new ArrayList<Float>();
                 }
                 String line = in.readLine();
                 line = StringUtils.replace(line, "\"", "");
                 int m = 0;
                 //while (line != null) {		// original code
                 while((line != null) 			// modified for mantis issue: 1349
                 		&& (!StringUtils.isEmpty(line))
                 		&& (!line.trim().startsWith("!"))
                 		){	
                     String[] tokens = line.split("\t");
                     int length = tokens.length;
                     if (length != (n + 1)) {
                         System.out.println("Warning: Could not parse line #" + (m + 1) + ". Line should have " + (n+1) + " lines, has " + length + ".");
                         if ((m==0)&&(length==n + 2)) //this file probably comes from R's RMA, without first column in header
                         	throw new InputFileFormatException("Attempting to open a file that does not comply with the " + "RMA Express format." + "\n" + "Warning: Could not parse line #" + (m + 1) + ". Line should have " + (n+1) + " columns, but it has " + length + ".\n" + "This file looks like R's RMA format, which needs manually add a tab in the beginning of the header to make it a valid RMA format.");
                         else 
                         	throw new InputFileFormatException("Attempting to open a file that does not comply with the " + "RMA Express format." + "\n" + "Warning: Could not parse line #" + (m + 1) + ". Line should have " + (n+1) + " columns, but it has " + length + ".");
                     }
                     String markerName = new String(tokens[0].trim());
                     CSExpressionMarker marker = new CSExpressionMarker(m);
                     marker.setLabel(markerName);
                     maSet.getMarkerVector().add(m, marker);
                     for (int i = 0; i < n; i++) {
                         String valString = "";
                         if ((i + 1) < tokens.length) {
                             valString = tokens[i + 1];
                         }
                         if (valString.trim().length() == 0) {
                             values[i].add(Float.NaN);
                         } else {
                             float value = Float.NaN;
                             try {
                                 value = Float.parseFloat(valString);
                             } catch (NumberFormatException nfe) {
 
                             }
                             values[i].add(value);
                         }
                     }
                     m++;
                     line = in.readLine();
                     line = StringUtils.replace(line, "\"", "");
                 }
                 // Skip first token
                 headerTokenizer.nextToken();
                 for (int i = 0; i < n; i++) {
                     String arrayName = headerTokenizer.nextToken();
                     CSMicroarray array = new CSMicroarray(i, m, arrayName, null, null, false, DSMicroarraySet.affyTxtType);
                     maSet.add(array);
                     for (int j = 0; j < m; j++) {
                         Float v = values[i].get(j);
                         CSExpressionMarkerValue markerValue = new CSExpressionMarkerValue(v);
                         if (v.isNaN()) {
                             markerValue.setMissing(true);
                         } else {
                             markerValue.setPresent();
                         }
                         array.setMarkerValue(j, markerValue);
                     }
                 }
                 // Set chip-type
                 String result = null;
                 for (int i = 0; i < m; i++) {
                     result = AnnotationParser.matchChipType(maSet, maSet.getMarkerVector().get(i).getLabel(), false);
                     if (result != null) {
                         break;
                     }
                 }
                 if (result == null) {
                     AnnotationParser.matchChipType(maSet, "Unknown", true);
                 } else {
                     maSet.setCompatibilityLabel(result);
                 }
                 for (DSGeneMarker marker : maSet.getMarkerVector()) {
                     String token = marker.getLabel();
                     String[] locusResult = AnnotationParser.getInfo(token, AnnotationParser.LOCUSLINK);
                     String locus = "";
                     if ((locusResult != null) && (!locusResult[0].trim().equals(""))) {
                         locus = locusResult[0].trim();
                     }
                     if (locus.compareTo("") != 0) {
                         try {
                             marker.setGeneId(Integer.parseInt(locus));
                         } catch (NumberFormatException e) {
                             log.info("Couldn't parse locus id: "+locus);
                         }
                     }
                     String[] geneNames = AnnotationParser.getInfo(token, AnnotationParser.ABREV);
                     if (geneNames != null) {
                         marker.setGeneName(geneNames[0]);
                     }
 
                     marker.getUnigene().set(token);
 
                 }
             }
         } catch (InputFileFormatException e){
         	throw e;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return maSet;
     }
 
     public List getOptions() {
         // todo Implement this org.geworkbench.components.parsers.FileFormat abstract method
         throw new UnsupportedOperationException("Method getOptions() not yet implemented.");
     }
 
     public FileFilter getFileFilter() {
         return maFilter;
     }
 
     /**
      * getDataFile
      *
      * @param files File[]
      * @return DataSet
      */
     public DSDataSet getDataFile(File[] files) {
         return null;
     }
 
     /**
      * Defines a <code>FileFilter</code> to be used when the user is prompted
      * to select Affymetrix input files. The filter will only display files
      * whose extension belongs to the list of file extension defined in {@link
      * #affyExtensions}.
      */
     class RMAExpressFilter extends FileFilter {
 
         public String getDescription() {
             return getFormatName();
         }
 
         public boolean accept(File f) {
             boolean returnVal = false;
             for (int i = 0; i < maExtensions.length; ++i)
                 if (f.isDirectory() || f.getName().endsWith(maExtensions[i])) {
                     return true;
                 }
             return returnVal;
         }
     }
 }
