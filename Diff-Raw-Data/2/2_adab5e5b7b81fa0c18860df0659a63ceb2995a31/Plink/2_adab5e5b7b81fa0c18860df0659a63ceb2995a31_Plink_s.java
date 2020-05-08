 package edu.mit.wi.plink;
 
 import edu.mit.wi.haploview.StatFunctions;
 import edu.mit.wi.haploview.Util;
 
 import java.io.*;
 import java.util.Vector;
 import java.util.StringTokenizer;
 import java.util.Hashtable;
 import java.net.URL;
 import java.net.MalformedURLException;
 
 
 /** @noinspection RedundantStringConstructorCall*/
 public class Plink {
 
     private Vector results = null;
     private Vector columns = null;
     private Vector ignoredMarkers;
     private boolean dupMarkers = false;
 
     public void parseWGA(String wga, String map, boolean embed, String chromFilter, Vector columnFilter) throws PlinkException {
         results = new Vector();
         columns = new Vector();
         columns.add("CHROM");
         columns.add("MARKER");
         columns.add("POSITION");
 
         Hashtable markerHash = new Hashtable(1,1);
         ignoredMarkers = new Vector();
         short chrFilter = 0;
         BufferedReader mapReader;
         String mapName = null;
         String wgaName = null;
 
 
         if (chromFilter != null){
             if (!chromFilter.equals("")){
                 chrFilter = Short.parseShort(chromFilter);
             }
         }
 
         if (!embed){
             try{
                 URL mapURL = new URL(map);
                 mapName = map;
                 mapReader = new BufferedReader(new InputStreamReader(mapURL.openStream()));
             }catch(MalformedURLException mfe){
                 File mapFile = new File(map);
                 mapName = mapFile.getName();
                 try{
                     if (mapFile.length() < 1){
                         throw new PlinkException("Map file is empty or nonexistent: " + mapName);
                     }
                     mapReader = new BufferedReader(new FileReader(mapFile));
                 }catch(IOException ioe){
                     throw new PlinkException("Error reading the map file: " + mapName);
                 }
             }catch(IOException ioe){
                 throw new PlinkException("Could not connect to " + mapName);
             }
 
             String mapLine;
             int line = 0;
             int numColumns = -1;
 
             try{
                 while((mapLine = mapReader.readLine())!=null) {
                     if (mapLine.length() == 0){
                         //skip blank lines
                         line++;
                         continue;
                     }
 
                     StringTokenizer st = new StringTokenizer(mapLine,"\t ");
 
                     if (numColumns == -1){
                         numColumns = st.countTokens();
                     }else{
                         if (numColumns != st.countTokens()){
                             throw new PlinkException("Inconsistent number of map file columns on line " + (line+1));
                         }
                     }
 
                     if (numColumns != 3 && numColumns != 4 && numColumns != 6){
                         throw new PlinkException("Improper map file formatting on line " + (line+1));
                     }
 
                     String chrom = st.nextToken();
                     short chr;
                     if (chrom.equalsIgnoreCase("X")){
                         chr = 23;
                     }else if (chrom.equalsIgnoreCase("Y")){
                         chr = 24;
                     }else if (chrom.equalsIgnoreCase("XY")){
                         chr = 25;
                     }else if (chrom.equalsIgnoreCase("MT")){
                         chr = 26;
                     }else if (chrom.equals("-9")){
                         chr = 0;
                     }else{
                         try{
                             chr = Short.parseShort(chrom);
                         }catch(NumberFormatException nfe){
                             throw new PlinkException("Invalid chromosome specification on line " + (line +1) + ": " + chrom);
                         }
                         if (chr < 0 || chr > 26){
                             throw new PlinkException("Invalid chromosome specification on line " + (line +1) + ": " + chrom);
                         }
                     }
                     if (chrFilter > 0){
                         if (chr != chrFilter){
                             line++;
                             continue;
                         }
                     }
                     String marker = new String(st.nextToken());
                     if (numColumns == 4 || numColumns == 6){
                         //useless morgan distance
                         st.nextToken();
                     }
                     String pos = st.nextToken();
                     if (pos.startsWith("-")){
                         line++;
                         continue;
                     }
                     long position;
                     try{
                         position = Long.parseLong(pos);
                     }catch(NumberFormatException nfe){
                         throw new PlinkException("Invalid position specification on line " + (line +1) + ": " + pos);
                     }
 
                     Marker mark = new Marker(chr, marker, position);
                     markerHash.put(mark.getMarkerID(), mark);
                     line++;
                 }
             }catch(IOException ioe){
                 throw new PlinkException("Error reading the map file: " + mapName);
             }
         }
 
         BufferedReader wgaReader;
 
         try{
             URL wgaURL = new URL(wga);
             wgaName = wga;
             wgaReader = new BufferedReader(new InputStreamReader(wgaURL.openStream()));
         }catch(MalformedURLException mfe){
             File wgaFile = new File(wga);
             wgaName = wgaFile.getName();
             try{
                 if (wgaFile.length() < 1){
                     throw new PlinkException("Results file is empty or nonexistent: " + wgaName);
                 }
                 wgaReader = new BufferedReader(new FileReader(wgaFile));
             }catch(IOException ioe){
                 throw new PlinkException("Error reading the results file: " + wgaName);
             }
         }catch(IOException ioe){
             throw new PlinkException("Could not connect to " + wgaName);
         }
 
         int colIndex = 0;
         int markerColumn = -1;
         int chromColumn = -1;
         int positionColumn = -1;
         String headerLine;
         try{
             headerLine = wgaReader.readLine();
         }catch(IOException ioe){
             throw new PlinkException("Error reading the results file: " + wgaName);
         }
         StringTokenizer headerSt = new StringTokenizer(headerLine);
         boolean[] filteredColIndex = new boolean[headerSt.countTokens()];
         int counter;
         while (headerSt.hasMoreTokens()){
             String column = headerSt.nextToken();
             if (column.equalsIgnoreCase("SNP")){
                 markerColumn = colIndex;
             }else if (column.equalsIgnoreCase("CHR")){
                 chromColumn = colIndex;
             }else if (column.equalsIgnoreCase("POS")||column.equalsIgnoreCase("POSITION")){
                 positionColumn = colIndex;
             }else{
                 if (columnFilter != null){
                     if (columnFilter.contains(column)){
                         filteredColIndex[colIndex] = true;
                     }else{
                         if(columns.contains(column)){
                             counter = 1;
                             String dupColumn = column + "-" + counter;
                             while (columns.contains(dupColumn)){
                                 counter++;
                                 dupColumn = column + "-" + counter;
                             }
                             columns.add(dupColumn);
                         }else{
                             columns.add(column);
                         }
                     }
                 }else{
                     if(columns.contains(column)){
                         counter = 1;
                         String dupColumn = column + "-" + counter;
                         while (columns.contains(dupColumn)){
                             counter++;
                             dupColumn = column + "-" + counter;
                         }
                         columns.add(dupColumn);
                     }else{
                         columns.add(column);
                     }
                 }
             }
             colIndex++;
         }
 
         if (markerColumn == -1){
             throw new PlinkException("Results file must contain a SNP column.");
         }
 
         if (embed){
             if (chromColumn == -1 || positionColumn == -1){
                 throw new PlinkException("Results files with embedded map files must contain CHR and POS columns.");
             }
         }
 
         String wgaLine;
         int lineNumber = 0;
         Hashtable markerDups = new Hashtable(1,1);
 
         try{
             while((wgaLine = wgaReader.readLine())!=null){
                 if (wgaLine.length() == 0){
                     //skip blank lines
                     continue;
                 }
                 int tokenNumber = 0;
                 StringTokenizer tokenizer = new StringTokenizer(wgaLine);
                 if (tokenizer.countTokens() != colIndex){
                     throw new PlinkException("Inconsistent column number on line " + (lineNumber+1));
                 }
                 String marker = null;
                 String chromosome, position;
                 short chr = 0;
                 long pos = 0;
                 Vector values = new Vector();
                 while(tokenizer.hasMoreTokens()){
                     if (tokenNumber == markerColumn){
                         marker = new String(tokenizer.nextToken());
                         if (markerDups.containsKey(marker)){
                             dupMarkers = true;
                         }else{
                             markerDups.put(marker,"");
                         }
                     }else if (tokenNumber == chromColumn){
                         //new String() stops StringTokenizer from wasting memory
                         chromosome = new String(tokenizer.nextToken());
                         if(chromosome.equalsIgnoreCase("X")){
                             chr = 23;
                         }else if(chromosome.equalsIgnoreCase("Y")){
                             chr = 24;
                         }else if(chromosome.equalsIgnoreCase("XY")){
                             chr = 25;
                         }else if(chromosome.equalsIgnoreCase("MT")){
                             chr = 26;
                         }else if (chromosome.equals("-9")){
                             chr = 0;
                         }else{
                             try{
                                 chr = Short.parseShort(chromosome);
                             }catch(NumberFormatException nfe){
                                 throw new PlinkException("Invalid chromosome specification on line " + (lineNumber +1) + ": " + chromosome);
                             }
                             if (chr < 0 || chr > 26){
                                 throw new PlinkException("Invalid chromosome specification on line " + (lineNumber +1) + ": " + chromosome);
                             }
                         }
                     }else if (tokenNumber == positionColumn){
                         if (embed){
                             position = new String(tokenizer.nextToken());
                             try{
                                 pos = Long.parseLong(position);
                             }catch(NumberFormatException nfe){
                                 throw new PlinkException("Invalid position specification on line " + (lineNumber +1) + ": " + position);
                             }
                         }else{
                             tokenizer.nextToken();
                         }
                     }else{
                         if (filteredColIndex[tokenNumber]){
                             tokenizer.nextToken();
                         }else{
                             String val = tokenizer.nextToken();
                             if (val.equalsIgnoreCase("NA")){
                                 values.add(new Double(Double.NaN));
                             }else{
                                 try{
                                     values.add(new Double(val));
                                 }catch (NumberFormatException n){
                                     values.add(new String(val));
                                 }
                             }
                         }
                     }
                     tokenNumber++;
                 }
 
                 if (chrFilter > 0){
                     if (chr != chrFilter){
                         lineNumber++;
                         continue;
                     }
                 }
 
                 Marker assocMarker;
                 if (!embed){
                     assocMarker = (Marker)markerHash.get(marker);
 
                     if (assocMarker == null){
                         ignoredMarkers.add(marker);
                         lineNumber++;
                         continue;
                     }else if (assocMarker.getChromosomeIndex() != chr && chromColumn != -1){
                         throw new PlinkException("Incompatible chromosomes for marker " + marker +
                                "\non line " + lineNumber + " " + assocMarker.getChromosomeIndex() + " " + chr);
                     }
                 }else{
                     assocMarker = new Marker(chr,marker,pos);
                 }
 
                 AssociationResult result = new AssociationResult(lineNumber,assocMarker,values);
                 results.add(result);
                 lineNumber++;
             }
         }catch(IOException ioe){
             throw new PlinkException("Error reading the results file: " + wgaName);
         }
     }
 
     public void parseNonSNP(String name, Vector columnFilter) throws PlinkException{
         results = new Vector();
         columns = new Vector();
 
         String wgaName = null;
         BufferedReader wgaReader;
         try{
             URL wgaURL = new URL(name);
             wgaName = name;
             wgaReader = new BufferedReader(new InputStreamReader(wgaURL.openStream()));
         }catch(MalformedURLException mfe){
             File wgaFile = new File(name);
             wgaName = wgaFile.getName();
             try{
                 if (wgaFile.length() < 1){
                     throw new PlinkException("Results file is empty or nonexistent: " + wgaName);
                 }
                 wgaReader = new BufferedReader(new FileReader(wgaFile));
             }catch(IOException ioe){
                 throw new PlinkException("Error reading the results file: " + wgaName);
             }
         }catch(IOException ioe){
             throw new PlinkException("Could not connect to " + wgaName);
         }
 
         int numColumns = 0;
         String headerLine;
         try{
             headerLine = wgaReader.readLine();
         }catch(IOException ioe){
             throw new PlinkException("Error reading the results file: " + wgaName);
         }
         StringTokenizer headerSt = new StringTokenizer(headerLine);
         boolean[] filteredColIndex = new boolean[headerSt.countTokens()];
         while (headerSt.hasMoreTokens()){
             String column = headerSt.nextToken();
             if (columnFilter != null){
                 if (columnFilter.contains(column)){
                     filteredColIndex[numColumns] = true;
                     numColumns++;
                 }else{
                     columns.add(column);
                     numColumns++;
                 }
             }else{
                 columns.add(column);
                 numColumns++;
             }
         }
 
         String wgaLine;
         int lineNumber = 0;
         try{
             while((wgaLine = wgaReader.readLine())!=null){
                 if (wgaLine.length() == 0){
                     //skip blank lines
                     continue;
                 }
                 int tokenNumber = 0;
                 StringTokenizer tokenizer = new StringTokenizer(wgaLine);
                 Vector values = new Vector();
                 while(tokenizer.hasMoreTokens()){
                     if (filteredColIndex[tokenNumber]){
                         tokenizer.nextToken();
                     } else{
                         String val = tokenizer.nextToken();
                         if (val.equalsIgnoreCase("NA")){
                             values.add(new Double(Double.NaN));
                         }else{
                             try{
                                 values.add(new Double(val));
                             }catch (NumberFormatException n){
                                 values.add(new String(val));
                             }
                         }
                     }
                     tokenNumber++;
                 }
 
                 if (tokenNumber != numColumns){
                     throw new PlinkException("Inconsistent column number on line " + (lineNumber+1));
                 }
 
                 AssociationResult result = new AssociationResult(lineNumber,values);
                 results.add(result);
                 lineNumber++;
             }
         }catch(IOException ioe){
             throw new PlinkException("Error reading the results file: " + wgaName);
         }
     }
 
     public Vector getIgnoredMarkers() {
         return ignoredMarkers;
     }
 
     public Vector parseMoreResults(String wga, Vector columnFilter) throws PlinkException {
         File moreResultsFile = new File(wga);
         Vector newColumns = new Vector();
         ignoredMarkers = new Vector();
         Vector duplicateColumns = new Vector();
         boolean addColumns = false;
         int numValues = columns.size()-3;
 
 
         try{
             if (moreResultsFile.length() < 1){
                 throw new PlinkException("Results file is empty or nonexistent: " + moreResultsFile.getName());
             }
 
             BufferedReader moreResultsReader = new BufferedReader(new FileReader(moreResultsFile));
             int numColumns = 0;
             int markerColumn = -1;
             int chromColumn = -1;
             int posColumn = -1;
             String headerLine = moreResultsReader.readLine();
             StringTokenizer headerSt = new StringTokenizer(headerLine);
             boolean[] filteredColIndex = new boolean[headerSt.countTokens()];
 
             while (headerSt.hasMoreTokens()){
                 String column = new String(headerSt.nextToken());
 
                 if (column.equalsIgnoreCase("SNP")){
                     if (markerColumn != -1){
                         throw new PlinkException("Results file contains more then one SNP column.");
                     }
                     markerColumn = numColumns;
                     numColumns++;
                 }else if (column.equalsIgnoreCase("CHR")){
                     chromColumn = numColumns;
                     numColumns++;
                 }else if (column.equalsIgnoreCase("POS") || column.equalsIgnoreCase("POSITION")){
                     posColumn = numColumns;
                     numColumns++;
                 }else{
                     if (columnFilter != null){
                         if (columnFilter.contains(column)){
                             filteredColIndex[numColumns] = true;
                             numColumns++;
                         }else{
                             if(columns.contains(column)){
                                 int counter = 1;
                                 String dupColumn = column + "-" + counter;
                                 while (columns.contains(dupColumn)){
                                     counter++;
                                     dupColumn = column + "-" + counter;
                                 }
                                 duplicateColumns.add(dupColumn);
                                 newColumns.add(dupColumn);
                             }else{
                                 newColumns.add(column);
                             }
                             numColumns++;
                         }
                     }else{
                         if(columns.contains(column)){
                             int counter = 1;
                             String dupColumn = column + "-" + counter;
                             while (columns.contains(dupColumn)){
                                 counter++;
                                 dupColumn = column + "-" + counter;
                             }
                             duplicateColumns.add(dupColumn);
                             newColumns.add(dupColumn);
                         }else{
                             newColumns.add(column);
                         }
                         numColumns++;
                     }
                 }
             }
 
             if (markerColumn == -1){
                 throw new PlinkException("Results file must contain a SNP column.");
             }
 
             String wgaLine;
             int lineNumber = 0;
             Hashtable markerDups = new Hashtable(1,1);
 
             Hashtable resultsHash = new Hashtable(1,1);
             for (int i = 0; i < results.size(); i++){
                 String markerID = ((AssociationResult)results.get(i)).getMarker().getMarkerID();
                 resultsHash.put(markerID,new Integer(i));
             }
 
             while((wgaLine = moreResultsReader.readLine())!=null){
                 if (wgaLine.length() == 0){
                     //skip blank lines
                     continue;
                 }
                 int tokenNumber = 0;
                 StringTokenizer tokenizer = new StringTokenizer(wgaLine);
                 String marker = null;
                 Vector values = new Vector();
                 while(tokenizer.hasMoreTokens()){
                     if (tokenNumber == markerColumn){
                         marker = new String(tokenizer.nextToken());
                         if (markerDups.containsKey(marker)){
                             throw new PlinkException("Marker: " + marker +
                                     " appears more than once.");
                         }else{
                             markerDups.put(marker,"");
                         }
                     }else if(tokenNumber == chromColumn || tokenNumber == posColumn){
                         //we don't give a toss for the chromosome or position...
                         tokenizer.nextToken();
                     }else{
                         if (filteredColIndex[tokenNumber]){
                             tokenizer.nextToken();
                         }else{
                             String val = tokenizer.nextToken();
                             if (val.equalsIgnoreCase("NA")){
                                 values.add(new Double(Double.NaN));
                             }else{
                                 try{
                                     values.add(new Double(val));
                                 }catch (NumberFormatException n){
                                     values.add(new String(val));
                                 }
                             }
                         }
                     }
                     tokenNumber++;
                 }
 
                 if (tokenNumber != numColumns){
                     throw new PlinkException("Inconsistent column number on line " + (lineNumber+1));
                 }
 
                 AssociationResult currentResult;
 
                 if (resultsHash.containsKey(marker)){
                     addColumns = true;
                     currentResult = (AssociationResult)results.get(((Integer)resultsHash.get(marker)).intValue());
                     if (currentResult.getValues().size() < numValues){
                         int nullsToAdd = numValues - currentResult.getValues().size();
                         Vector nullPads = new Vector();
                         for (int i = 0; i < nullsToAdd; i++){
                             nullPads.add(null);
                         }
                         currentResult.addValues(nullPads);
                     }
                     currentResult.addValues(values);
                 }else{
                     ignoredMarkers.add(marker);
                 }
 
                 lineNumber++;
             }
         }catch(IOException ioe){
             throw new PlinkException("Error reading the results file: " + moreResultsFile.getName());
         }
 
         if (addColumns){
             for (int i = 0; i < newColumns.size(); i++){
                 columns.add(newColumns.get(i));
             }
         }else{
             duplicateColumns = new Vector();
         }
         return duplicateColumns;
     }
 
     public void doFisherCombined(Vector cols) throws PlinkException{
         int numValues = columns.size()-3;
 
         for (int i = 0; i < results.size(); i ++){
             AssociationResult currentResult = (AssociationResult)results.get(i);
             Vector values = currentResult.getValues();
             Vector pValues = new Vector();
             Vector valuesToAdd = new Vector();
             for (int j = 0; j < cols.size(); j++){
                 int value = ((Integer)cols.get(j)).intValue();
                 Double pv;
                 try{
                     if (values.size() > value){
                         if (values.get(value) != null){
                             if (values.get(value) instanceof Double){
                                 if (!((values.get(value)).equals(new Double(Double.NaN)))){
                                     pv = (Double)values.get(value);
                                     pValues.add(pv);
                                 }
                             }
                         }
                     }
                 }catch(NumberFormatException nfe){
                     throw new PlinkException("One or more of the selected columns does not contain\n" +
                             "properly formatted P-values.");
                 }
             }
             int numPvals = pValues.size();
             double sumLns = 0;
             for (int j = 0; j < numPvals; j++){
                 if (pValues.get(j) != null){
                     sumLns += Math.log(((Double)pValues.get(j)).doubleValue());
                 }
             }
             double chisq = -2*sumLns;
             if (chisq == 0){
                 valuesToAdd.add(new Double(1));
             }else if (chisq > Double.MAX_VALUE){ //in case of infinite chisq due to pvalue of 0
                 valuesToAdd.add(new Double("1.0E-16"));
             }else{
                 double df = 2*numPvals;
                 try{
                     Double p;
                     if (1-StatFunctions.pchisq(chisq,df) == 0){
                         p = new Double("1.0E-16");
                     }else{
                         p = new Double(Util.formatPValue(1-StatFunctions.pchisq(chisq,df)));
                     }
                     valuesToAdd.add(p);
                 }catch(IllegalArgumentException iae){
                     throw new PlinkException("One or more of the selected columns does not contain\n" +
                             "properly formatted P-values.");
                 }
             }
             if (currentResult.getValues().size() < numValues){
                 int nullsToAdd = numValues - currentResult.getValues().size();
                 Vector nullPads = new Vector();
                 for (int j = 0; j < nullsToAdd; j++){
                     nullPads.add(null);
                 }
                 currentResult.addValues(nullPads);
             }
             currentResult.addValues(valuesToAdd);
         }
         String column = "P_COMBINED";
         if(columns.contains(column)){
             int counter = 1;
             String dupColumn = column + "-" + counter;
             while (columns.contains(dupColumn)){
                 counter++;
                 dupColumn = column + "-" + counter;
             }
             columns.add(dupColumn);
         }else{
             columns.add(column);
         }
     }
 
     public Vector getResults(){
         return results;
     }
 
     public Vector getColumnNames(){
         return columns;
     }
 
     public boolean getPlinkDups(){
         return dupMarkers;
     }
 
 }
