 package edu.kit.iti.algo2.textindexing.alexdomge.index;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import edu.kit.iti.algo2.textindexing.alexdomge.indexer.Pickle;
 
 public class InvertedIndexPickle {
 
     public static final String SFX_XML_GZIP = ".xml.gzip";
     public static final String SFX_XML = ".xml";
     public static final String SFX_ZIP = ".zip";
     public static final String SFX_DUMP = ".dump";
     public static final String SFX_DUMP_GZIP = ".dump.gzip";
 
     public static void storeToXml(String filepath, InvertedIndex ii)
 	    throws FileNotFoundException {
 	FileOutputStream fos = new FileOutputStream(filepath + SFX_XML);
 	IndexXMLProcessor.writeFile(fos, ii.getInternal());
     }
 
     public static InvertedIndex loadFromXml(String filepath)
 	    throws FileNotFoundException {
 	InvertedIndex ii = new InvertedIndex();
 	InputStream fis = new FileInputStream(filepath + SFX_XML);
 	Map<String, List<Occurrence>> a = IndexXMLProcessor.read(fis);
 	ii.setInternal(a);
 	return ii;
     }
 
     public static void storeToGZipXml(String filepath, InvertedIndex ii)
 	    throws FileNotFoundException, IOException {
 	GZIPOutputStream fos = new GZIPOutputStream(new FileOutputStream(
 		filepath + SFX_XML_GZIP));
 	IndexXMLProcessor.writeFile(fos, ii.getInternal());
 	fos.close();
     }
 
     public static InvertedIndex loadFromGZipXml(String filepath)
 	    throws IOException {
 	InvertedIndex ii = new InvertedIndex();
 	InputStream fis = new GZIPInputStream(new FileInputStream(filepath
 		+ SFX_XML_GZIP));
 	Map<String, List<Occurrence>> a = IndexXMLProcessor.read(fis);
 	ii.setInternal(a);
 	return ii;
     }
 
     public static InvertedIndex loadFromDump(String file)
 	    throws FileNotFoundException {
 	return (InvertedIndex) Pickle.readObject(new File(file + SFX_DUMP));
     }
 
     public static void storeToDump(String file, InvertedIndex ii)
 	    throws IOException {
 	Pickle.saveObject(new File(file + SFX_DUMP), ii);
     }
 
     public static InvertedIndex loadFromDumpGZip(String file)
 	    throws FileNotFoundException, IOException {
 	return (InvertedIndex) Pickle.readObject(new GZIPInputStream(
 		new FileInputStream(file + SFX_DUMP_GZIP)));
     }
 
     public static void storeToDumpGZip(String file, InvertedIndex ii)
 	    throws IOException {
 	Pickle.saveObject(new GZIPOutputStream(new FileOutputStream(file
 		+ SFX_DUMP_GZIP)), ii);
     }
 
     public static InvertedIndex loadFrom(File indexFile) throws IOException {
 	String end = indexFile.getAbsoluteFile().getName();
 	String sfx = end.substring(end.indexOf('.'));
 	String name = end.substring(0,end.indexOf('.'));
 	String path = name;
	if(indexFile.getParent() != null)
		path = indexFile.getParent() + File.separator + name;
 
 	switch (sfx) {
 	case SFX_DUMP:
 	    return loadFromDump(path);
 	case SFX_DUMP_GZIP:
 	    return loadFromDumpGZip(path);
 	case SFX_XML:
 	    return loadFromXml(path);
 	case SFX_XML_GZIP:// default
 	    return loadFromGZipXml(path);
 	}
 	return loadFromGZipXml(path);
     }
 
     public static void storeTo(File indexFile, InvertedIndex ii) throws FileNotFoundException, IOException {
 	String end = indexFile.getAbsoluteFile().getName();
 	String sfx = end.substring(end.indexOf('.'));
 	String name = end.substring(0,end.indexOf('.'));
 	String path = name;
	if(indexFile.getParent() != null)
		path = indexFile.getParent() + File.separator + name;
	
 	switch (sfx) {
 	case SFX_DUMP:
 	    storeToDump(path, ii);
 	    break;
 	case SFX_DUMP_GZIP:
 	    storeToDumpGZip(path, ii);
 	    break;
 	case SFX_XML:
 	    storeToXml(path, ii);
 	    break;
 	case SFX_XML_GZIP:// default
 	    storeToGZipXml(path, ii);
 	}
 	storeToGZipXml(path, ii);
     }
 
 }
