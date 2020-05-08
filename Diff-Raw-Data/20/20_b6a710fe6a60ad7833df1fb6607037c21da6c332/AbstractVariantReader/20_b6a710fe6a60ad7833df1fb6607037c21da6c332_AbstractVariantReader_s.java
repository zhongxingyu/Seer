 package varviewer.server.variant;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import varviewer.server.VVProps;
 import varviewer.shared.variant.Annotation;
 import varviewer.shared.variant.AnnotationIndex;
 import varviewer.shared.variant.SimpleAnnotationIndex;
 import varviewer.shared.variant.Variant;
 
 /**
  * Base class for things that read (create) Variants from files. This tends to be something of
  * a performance bottleneck so has undergone a bit of optimization that can make the code confusing.
  * 
  * @author brendan
  *
  */
 public abstract class AbstractVariantReader {
 
 	public static final int COLS_TO_IGNORE = 5; //Skip first X columns when parsing variants
 	protected File varFile = null;
 	protected String[] headerToks = null;
 	protected boolean[] numericFlags = null; //Indicates which annotations are numeric, is initialized with header
 	protected AnnotationIndex annoIndex = null; //Initialized in call to initializeHeader
 	
 	//Stores which annotations should be treated as numeric. This is read in from
 	//the properties file every time an AbstractVariantReader is created. 
 	//protected Set<String> numericAnnotations = new HashSet<String>();
 	
 	public AbstractVariantReader(File source) throws IOException {
 		this.varFile = source;
 		if (!varFile.exists()) {
 			Logger.getLogger(getClass()).error("Cannot read variants from file " + varFile.getAbsolutePath() + ", it does not exist");
 			throw new IllegalArgumentException("File " + varFile.getAbsolutePath() + " does not exist");
 		}
 		if (!varFile.canRead()) {
 			Logger.getLogger(getClass()).error("Cannot read variants from file " + varFile.getAbsolutePath() + ", it is not readable");
 			throw new IllegalArgumentException("File " + varFile.getAbsolutePath() + " exists but is not readable");
 		}
 		
 	}
 	
 	
 
 	/**
 	 * Returns all variants in a VariantCollection
 	 * @return
 	 * @throws IOException 
 	 */
 	public abstract VariantCollection toVariantCollection() throws IOException;
 	
 	protected void initializeHeader(String header) {
 		header = header.substring(1); //Trim off leading #
 		Set<String> numericAnnotations = new HashSet<String>();
 		headerToks = header.split("\t");
 		numericFlags = new boolean[headerToks.length];
 		
 		String numAnnosStr = VVProps.getProperty("numeric.annotations");
 		if (numAnnosStr != null) {
 			String[] annos = numAnnosStr.split(":");
 			for(int i=0; i<annos.length; i++) {
 				numericAnnotations.add(annos[i].trim());
 			}
 		}
 		
 		for(int i=0; i<headerToks.length; i++) {
 			headerToks[i] = headerToks[i].trim();
 			if (numericAnnotations.contains(headerToks[i])) {
 				numericFlags[i] = true;
 			}
 			else {
 				numericFlags[i]  = false;
 			}
 		}
 		
 		
 		//Create annotationIndex. First five columns (chrom, start, end, ref, alt) are ignored
 		String[] annoKeys = Arrays.copyOfRange(headerToks, COLS_TO_IGNORE, headerToks.length);
 		annoIndex = new SimpleAnnotationIndex(annoKeys, numericAnnotations);
		
 	}
 	
 	/**
 	 * Obtain the index that associates annotation keys with numbers. This is null until initializeHeader is called.
 	 * @return
 	 */
 	public AnnotationIndex getAnnotationIndex() {
 		return annoIndex;
 	}
 	
 	/**
 	 * Parse, create and return a variant from the given string. All items in columns five
 	 * and greater are treated as annotations with key determined by the header 
 	 * @param str
 	 * @return
 	 */
 	protected static Variant variantFromString(String[] toks, AnnotationIndex index, boolean[] numericFlags) {
 		String contig = toks[0];
 		Integer start = Integer.parseInt(toks[1]);
 		String ref = toks[3];
 		String alt = toks[4];
 		Variant var = new Variant(contig, start, ref, alt);
 		Annotation[] annotations = new Annotation[toks.length-COLS_TO_IGNORE];
 		for(int i=COLS_TO_IGNORE; i<toks.length; i++) {
 			
 			if (numericFlags[i]) {
 				try {
 					Double val = Double.parseDouble( toks[i]);
 					annotations[i-COLS_TO_IGNORE] = new Annotation(val);
 				}
 				catch (NumberFormatException nfe) {
 					//Don't sweat it, no annotation added
 				}
 			}
 			else {
 				annotations[i-COLS_TO_IGNORE] = new Annotation(toks[i].trim());
 			}
 		}
 		var.setAnnotations(Arrays.asList(annotations));
 		var.setAnnotationIndex(index);
 		return var;
 	}
 }
