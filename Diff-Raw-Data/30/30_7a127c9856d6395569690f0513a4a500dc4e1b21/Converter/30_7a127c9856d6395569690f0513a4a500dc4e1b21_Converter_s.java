 package mcmapconverter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import mcmaplib.InvalidMapException;
 import mcmaplib.MapFormatException;
 import mcmaplib.MinecraftMap;
 import mcmaplib.NotImplementedException;
 
 public class Converter {
     private final Map<String, Set<MapFormat>> extensionMapFormats;
     private final Map<String, MapFormat> namedMapFormats;
     private File inputFile, outputFile;
     private MapFormat inputFormat = null, outputFormat = null;
 
     public Converter() {
         extensionMapFormats = new HashMap<String, Set<MapFormat>>();
         namedMapFormats = new HashMap<String, MapFormat>();
     }
     
     public void setInputFile(File inputFile) {
         this.inputFile = inputFile;
     }
 
     public void setOutputFile(File outputFile) {
         this.outputFile = outputFile;
     }
 
     public void setInputFormat(MapFormat inputFormat) {
         this.inputFormat = inputFormat;
     }
 
     public void setOutputFormat(MapFormat outputFormat) {
         this.outputFormat = outputFormat;
     }
 
     private MapFormat detectFileMapFormat(File file) {
         Set<MapFormat> mapFormats;
 
         mapFormats = getFileMapFormats(file);
         if(mapFormats.size() != 1)
             return null;
         else
             return mapFormats.iterator().next();
     }
 
     public MapFormat detectInputFileMapFormat() {
         return detectFileMapFormat(inputFile);
     }
 
     public MapFormat detectOutputFileMapFormat() {
         return detectFileMapFormat(outputFile);
     }
 
     public void convert(ConverterUI ui) {
         MinecraftMap inMap, outMap;
         MapFormat conversionInputFormat, conversionOutputFormat;
 
         if(inputFile == null) {
             ui.displayInputFileError("No input file specified");
         }
 
         if(outputFile == null) {
             ui.displayOutputFileError("No output file specified");
         }
 
         if(inputFile == null || outputFile == null)
             return;
 
         if(inputFormat == null) {
             conversionInputFormat = detectInputFileMapFormat();
             if(conversionInputFormat == null) {
                 ui.displayInputFormatError("Could not detect input format, please specify one manually" );
             }
         } else
             conversionInputFormat = inputFormat;
 
         if(outputFormat == null) {
             conversionOutputFormat = detectOutputFileMapFormat();
             if(conversionOutputFormat == null) {
                ui.displayInputFormatError("Could not detect output format, please specify one manually" );
             }
         } else
             conversionOutputFormat = outputFormat;
 
         if(conversionInputFormat == null || conversionOutputFormat == null)
             return;
         
         try {
             inMap = conversionInputFormat.load(inputFile);
         } catch(FileNotFoundException e) {
             ui.displayInputFileError("Input file not found.");
             return;
         } catch(NotImplementedException e) {
             ui.displayInputFormatError("Converting from that map format is not supported: " + e.getMessage());
             return;
         } catch(MapFormatException e) {
             ui.displayInputFileError("Input map file corrupt: " + e.getMessage());
             return;
         } catch(IOException e) {
             ui.displayInputFileError("Error loading input map file: " + e.getMessage());
             return;
         }
 
         try {
             outMap = conversionOutputFormat.convert(inMap);
         } catch(InvalidMapException e) {
             ui.displayError("Error converting map: " + e.getMessage());
             return;
         }
 
         try {
             outMap.save(outputFile);
         } catch(NotImplementedException e) {
             ui.displayOutputFormatError("Converting to that map format is not supported: " + e.getMessage());
         } catch(IOException e) {
             ui.displayOutputFileError("Error saving map file: " + e.getMessage());
         }
         ui.displaySuccess();
     }
 
     public MapFormat getMapFormat(String name) {
         return namedMapFormats.get(name.toLowerCase());
     }
 
     public Set<MapFormat> getMapFormats() {
         return new HashSet<MapFormat>(namedMapFormats.values());
     }
 
     public Set<String> getMapFormatNames() {
         return new HashSet<String>(namedMapFormats.keySet());
     }
 
     public Set<MapFormat> getExtensionMapFormats(String extension) {
         Set<MapFormat> mapFormats;
 
         mapFormats = extensionMapFormats.get(extension.toLowerCase());
         if(mapFormats == null)
             return new HashSet<MapFormat>();
         return new HashSet<MapFormat>(mapFormats);
     }
 
     private String getExtension(File file) {
         String filename, extension;
         int extensionIndex;
 
         filename = file.getName();
         extensionIndex = filename.lastIndexOf('.');
         if(extensionIndex == -1)
             return null;
         extension = filename.substring(extensionIndex + 1);
         return extension;
     }
 
     public Set<MapFormat> getFileMapFormats(File file) {
         String filename, extension;
 
         filename = file.getName();
         extension = getExtension(file);
         if(extension == null)
             return new HashSet<MapFormat>();
         return getExtensionMapFormats(extension);
     }
 
     public void addMapFormat(MapFormat format) {
         Set<String> extensions;
 
         namedMapFormats.put(format.getName().toLowerCase(), format);
         extensions = format.getExtensions();
         synchronized(extensionMapFormats) {
             synchronized(extensions) {
                 for(String extension : extensions) {
                     Set<MapFormat> formats;
 
                     extension = extension.toLowerCase();
                     if(extensionMapFormats.containsKey(extension)) {
                         formats = extensionMapFormats.get(extension);
                     } else {
                         formats = new HashSet<MapFormat>();
                         extensionMapFormats.put(extension, formats);
                     }
                     synchronized(formats) {
                         formats.add(format);
                     }
                 }
             }
         }
     }
 
     public void removeMapFormat(MapFormat format) {
         Set<String> extensions;
 
         extensions = format.getExtensions();
         synchronized(extensionMapFormats) {
             synchronized(extensions) {
                 for(String extension : extensions) {
                     Set<MapFormat> formats;
 
                     if(extensionMapFormats.containsKey(extension)) {
                         formats = extensionMapFormats.get(extension);
                         synchronized(formats) {
                             formats.remove(format);
                         }
                     }
                 }
             }
         }
     }
 }
