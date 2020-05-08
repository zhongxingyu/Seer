 package org.dbmi.uima.tools.components.idash;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.uima.UIMAFramework;
 import org.apache.uima.cas.CAS;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.cas.FeatureStructure;
 import org.apache.uima.cas.TypeSystem;
 import org.apache.uima.cas.impl.XmiCasDeserializer;
 import org.apache.uima.cas.text.AnnotationIndex;
 import org.apache.uima.collection.CollectionException;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceConfigurationException;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.resource.ResourceProcessException;
 import org.apache.uima.resource.metadata.TypeSystemDescription;
 import org.apache.uima.tools.components.XmiWriterCasConsumer;
 import org.apache.uima.util.CasCreationUtils;
 import org.apache.uima.util.InvalidXMLException;
 import org.apache.uima.util.ProcessTrace;
 import org.apache.uima.util.XMLInputSource;
 import org.dbmi.uima.tools.components.idash.type.ClassLabel;
 import org.xml.sax.SAXException;
 
 import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;
 import com.sun.xml.internal.ws.resources.EncodingMessages;
 
 import weka.core.Attribute;
 import weka.core.FastVector;
 import weka.core.Instances;
 import weka.core.SparseInstance;
 import weka.core.converters.ArffSaver;
 import edu.mayo.bmi.uima.core.type.refsem.OntologyConcept;
 import edu.mayo.bmi.uima.core.type.refsem.UmlsConcept;
 import edu.mayo.bmi.uima.core.type.syntax.NumToken;
 import edu.mayo.bmi.uima.core.type.syntax.WordToken;
 import edu.mayo.bmi.uima.core.type.textsem.DateAnnotation;
 import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
 import edu.mayo.bmi.uima.core.type.textsem.FractionAnnotation;
 import edu.mayo.bmi.uima.core.type.textsem.TimeAnnotation;
 
 /**
  * Analysis engine that encodes annotated features into a feature vector.
  * Not thread-safe.
  * @author abhishek
  *
  */
 public class Encoder extends XmiWriterCasConsumer {
 
 	/* Type system related constants */
 	public static final String UMLS_TAG_TYPENAME = "";
 	public static final String RXNORM_CODING_SCHEME = "RXNORM";
 	public static final String SNOMED_CODING_SCHEME = "SNOMED";
 	
 	/* Feature dictionary related constants */
 	public static final String ENTITIES = "Entities";
 	public static final String WORDS = "Words";
 	public static final String REGEX = "Pattern";
 	public static final String CANONICALWORDS = "CanonicalWords";
 	public static final String LABELS = "Labels";
 	
 	public static final String NULL = "NULL";
 	
 	/* Parameters */
 	public static final String PARAM_ENCODING_CHOICE = "EncodingMethod";
 	public static final String PARAM_LABELSARENOMINAL = "LabelsAreNominal";
 	public static final String PARAM_TERMCOUNTCUTOFF = "TermCountCutoff";
 	public static final String PARAM_DOCUMENTCOUNTCUTOFF = "DocumentPercentCutoff";
 	public static final String PARAM_TRAINING_FILE = "TrainingFile";
 	
 	/* Encoding choices */
 	public static final int ENCODINGMETHOD_BINARY = 0;
 	public static final int ENCODINGMETHOD_TF = 1;
 	public static final int ENCODINGMETHOD_TFxIDF = 2;
 	public static final int ENCODINGMETHOD_COUNT = 3;
 	public static final int ENCODINGMETHOD_LOG_TF = 4;
 	public static final int ENCODINGMETHOD_LOG_TFxIDF = 5;
 	
 	public static final int ENCODINGMETHOD_ALL = 100;
 	
 	Integer encodingChoice = -1; // Invalid
 	boolean labelsAreNominal = false; // Numeric by default
 	int numCASesProcessed = 0;
 	int termCountCutoff; // remove terms that occur less times than this.
 	int documentCountCutoff; // remove terms that occur in less than these many % of docs.
 	
 	
 	/* Our dictionary of all features */
 	Map<String, FeatureStats> stats = new HashMap<String, FeatureStats>();
 	
 	/* If we multi-class labels, then map between label and the set of its class values */
 	Map<String, Set<String>> labelClasses = new HashMap<String, Set<String>>();
 	
 	/* Where to output resulting feature encoded file */
 	private File mOutputDir;
 	
 	// LOG4J logger based on class name
 	private Logger logger = Logger.getLogger(getClass().getName());
 	
 	// Type system used by our CASes
 	TypeSystem typeSystemCAS = null;
 	
 	// If we are encoding a test set, base it on the training set file.
 	String trainingSetFile;
 	
 	
 	/**
 	 *  Map to store regex pattern to feature name mappings.
 	 *  Key = Annotation type (from type system)
 	 *  Value = Name of the feature being encoded
 	 */
 	Map<Integer, String> patternToFeatureMapping = new HashMap<Integer, String>();
 	
 	/**
 	 * Set up the Data Structures to be populated for
 	 * the entire corpus, one CAS document at a time.
 	 * These will be used to calculate TF / TF*IDF / Binary 
 	 * feature representation cutoffs and representations.
 	 * @throws ResourceInitializationException 
 	 */
 	public void initialize() throws ResourceInitializationException {
 		super.initialize();
 		
 		// Directory to output our encoded feature files
 		mOutputDir = new File((String) getConfigParameterValue(PARAM_OUTPUTDIR));
 		encodingChoice = (Integer) getConfigParameterValue(PARAM_ENCODING_CHOICE);
 		labelsAreNominal = (Boolean) getConfigParameterValue(PARAM_LABELSARENOMINAL);
 		termCountCutoff = (Integer) getConfigParameterValue(PARAM_TERMCOUNTCUTOFF);
 		documentCountCutoff = (Integer) getConfigParameterValue(PARAM_DOCUMENTCOUNTCUTOFF);
 		trainingSetFile = (String) getConfigParameterValue(PARAM_TRAINING_FILE);
 		
 		// Initialize feature stats datastructures
 		stats.put(ENTITIES, new FeatureStats());
 		stats.put(WORDS, new FeatureStats());
 		stats.put(CANONICALWORDS, new FeatureStats());
 		stats.put(REGEX, new FeatureStats());
 		stats.put(LABELS, new FeatureStats());
 		
 		// Initialize Regex pattern to Feature name mappings
 		patternToFeatureMapping.put(DateAnnotation.type,     "Date");
 		patternToFeatureMapping.put(TimeAnnotation.type,     "Time");
 		patternToFeatureMapping.put(NumToken.type,           "Number");
 		patternToFeatureMapping.put(FractionAnnotation.type, "Fraction");
 		//TODO: Add other regex annotations from config
 		
 		logger.info("Initialized FeatureEncoder");
 		logger.debug("keys in stats: ".concat(stats.keySet().toString()));
 	}
 	
 	/**
 	 * Process a CAS for a single document.
 	 * We add feature information to the global datastructures
 	 * and process them finally in 
 	 * {@link #collectionProcessComplete() collectionProcessComplete()}.
 	 */
 	@Override
 	public void processCas(CAS aCAS) throws ResourceProcessException {
 		// Iterate through the CAS and update feature counts
 		//AnnotationIndex<AnnotationFS> idx = (AnnotationIndex<AnnotationFS>) aCAS.getAnnotationIndex();
 		//FSIterator<AnnotationFS> itr = idx.iterator(true);
 		
 		JCas aJCas;
 		try {
 			logger.info("process(JCas)");
 			
 			aJCas = aCAS.getJCas();
 			
 			// Prepare the feature dictionaries for a new document
 			for(FeatureStats fs : stats.values()) {
 				fs.newDocument();
 			}
 			++numCASesProcessed;
 			
 			// Process all the named entities
 			processEntityMentions(aJCas);
 			
 			// Process all the Words (Unigrams) and their canonical forms
 			processWordTokens(aJCas, true);
 						
 			// Process all Regex Annotations
 			processRegularExpressions(aJCas);
 			
 			// Finally, process all the labels in the document
 			processLabels(aJCas);
 			
 		} catch (CASException e) {
 			throw new ResourceProcessException(e);
 		}
 		
 		// Write CAS to XMI file.
 		super.processCas(aCAS);
 		
 		// Save the type system if we haven't done so yet
 		if (typeSystemCAS == null) 
 			typeSystemCAS = aCAS.getTypeSystem();
 	}
 	
 	@Override
 	public void collectionProcessComplete(ProcessTrace aTrace) 
 		throws ResourceProcessException, IOException {
 		try {
 			/* Eliminate unnecessary features */
 			int docCountCutoff = (documentCountCutoff * numCASesProcessed) / 100;
 			logger.debug("Going to remove rare that occur in less than " + 
 					docCountCutoff + " documents, or occur less than " + termCountCutoff + " times.");
 			Set<String> featureGroups = new HashSet<String>(stats.keySet());
 			featureGroups.remove(LABELS); // we don't want to remove rare labels
 			for(String FeatureType : featureGroups) {
 				FeatureStats fs = stats.get(FeatureType);
 				
 				logger.debug("Feature type " + FeatureType + 
 						" has\n\t" + fs.size() + " Features \n" + 
 						fs.getDocumentsSeenSoFar() + " Documents seen. ");
 				
 				// determine and use corpus-level cutoffs for terms
 				int numTermsRemoved = fs.removeRareTerms(termCountCutoff, docCountCutoff);
 				logger.debug(String.format(
 						"\t%d terms were removed because they were rare.", 
 						numTermsRemoved));
 			}
 			
 			/* Encode all XMI files into ARFF */
 			XMIToArffConvertor convertor;
 			if (encodingChoice == ENCODINGMETHOD_ALL) {
 				// Binary
 				convertor = new XMIToArffConvertor(stats, mOutputDir, 
 						Arrays.asList(ENTITIES, REGEX, WORDS, CANONICALWORDS),labelClasses,
 						patternToFeatureMapping, typeSystemCAS, ENCODINGMETHOD_BINARY, labelsAreNominal, 
 						"encodedDataset.binary.arff", trainingSetFile);
 				convertor.encodeXMIFiles();
 				
 				// Count
 				convertor = new XMIToArffConvertor(stats, mOutputDir, 
 						Arrays.asList(ENTITIES, REGEX, WORDS, CANONICALWORDS),labelClasses,
 						patternToFeatureMapping, typeSystemCAS, ENCODINGMETHOD_COUNT, labelsAreNominal, 
 						"encodedDataset.count.arff", trainingSetFile);
 				convertor.encodeXMIFiles();
 				
 				// tf
 				convertor = new XMIToArffConvertor(stats, mOutputDir, 
 						Arrays.asList(ENTITIES, REGEX, WORDS, CANONICALWORDS),labelClasses,
 						patternToFeatureMapping, typeSystemCAS, ENCODINGMETHOD_TF, labelsAreNominal, 
 						"encodedDataset.tf.arff", trainingSetFile);
 				convertor.encodeXMIFiles();
 				
 				// tf.idf
 				convertor = new XMIToArffConvertor(stats, mOutputDir, 
 						Arrays.asList(ENTITIES, REGEX, WORDS, CANONICALWORDS),labelClasses,
 						patternToFeatureMapping, typeSystemCAS, ENCODINGMETHOD_TFxIDF, labelsAreNominal, 
 						"encodedDataset.tf.idf.arff", trainingSetFile);
 				convertor.encodeXMIFiles();
 				
 				// logtf.idf
 				convertor = new XMIToArffConvertor(stats, mOutputDir, 
 						Arrays.asList(ENTITIES, REGEX, WORDS, CANONICALWORDS),labelClasses,
 						patternToFeatureMapping, typeSystemCAS, ENCODINGMETHOD_LOG_TFxIDF, labelsAreNominal, 
 						"encodedDataset.logtf.idf.arff", trainingSetFile);
 				convertor.encodeXMIFiles();
 				
 				
 			} else {
 				convertor = new XMIToArffConvertor(stats, mOutputDir, 
 						Arrays.asList(ENTITIES, REGEX, WORDS, CANONICALWORDS),labelClasses,
 						patternToFeatureMapping, typeSystemCAS,encodingChoice, labelsAreNominal, 
 						"encodedDataset.arff", trainingSetFile);
 				convertor.encodeXMIFiles();
 			}
 			
 		} catch (Throwable e) {
 			e.printStackTrace();
 			logger.error(e.getMessage());
 		}
 	}
 	
 	private void processEntityMentions(JCas aJCas) {
 		FeatureStats entityFeatureStats = stats.get(ENTITIES);
 		
 		// Get all the UMLS Concepts in this document
 		FSIterator<Annotation> entities = aJCas.getAnnotationIndex(EntityMention.type).iterator();
 		
 		while(entities.hasNext()) {
 			EntityMention entity = (EntityMention) entities.next();
 			for(FeatureStructure fs : entity.getOntologyConceptArr().toArray()) {
 				OntologyConcept concept = (OntologyConcept) fs;
 				
				// Get Negated status
				String negated = "";
				if (entity.getPolarity() == -1)
					negated = "_NEGATED";
 				// Store the unique identifier for concept
 				// Note: We can store tui or code instead, if needed (could be made configurable)
 				if(concept.getCodingScheme().equals(SNOMED_CODING_SCHEME)) {
 					// Add cui
					entityFeatureStats.process(((UmlsConcept) concept).getCui().concat(negated), 1);
 				} else if(concept.getCodingScheme().equals(RXNORM_CODING_SCHEME)) {
 					// Add Oui
					entityFeatureStats.process(concept.getOui().concat(negated), 1);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Given a JCAS, build a dictionary of words and their canonical forms.
 	 * @param aJCas
 	 * @param processCanonicalForms If true, also build a dictionary of canonical forms of words.
 	 */
 	private void processWordTokens(JCas aJCas, boolean processCanonicalForms) {
 		FeatureStats wordFeatureStats = stats.get(WORDS);
 		FeatureStats canonicalWordFeatureStats = stats.get(CANONICALWORDS);
 		
 		// Get all word tokens
 		FSIterator<Annotation> words = aJCas.getAnnotationIndex(WordToken.type).iterator();
 		
 		while(words.hasNext()){
 			WordToken word = (WordToken) words.next();
 			wordFeatureStats.process(word.getCoveredText(), 1);
 			if(processCanonicalForms)
 				canonicalWordFeatureStats.process(word.getCanonicalForm(), 1);
 		}
 	}
 	
 	/**
 	 * Given a JCAS, build a dictionary of each pattern and counts of its' occurrences in the dataset.
 	 * @param aJCas
 	 */
 	private void processRegularExpressions(JCas aJCas) {
 		FeatureStats regexFeatureStats = stats.get(REGEX);
 		
 		// Collect stats of each regex pattern and process them
 		for(Integer annotationType : patternToFeatureMapping.keySet()) {
 			int count = aJCas.getAnnotationIndex(annotationType).size();
 			regexFeatureStats.process(patternToFeatureMapping.get(annotationType), count);
 		}
 	}
 	
 	private void processLabels(JCas aJCas) {
 		FeatureStats labelFeatureStats = stats.get(LABELS);
 		
     	FSIterator<Annotation> labels = aJCas.getAnnotationIndex(ClassLabel.type).iterator();
     	while(labels.hasNext()) {
     		ClassLabel label = (ClassLabel) labels.next();
     		String labelName = label.getLabelName();
     		labelFeatureStats.process(labelName, 1);
     		
     		// We need to store distinct label value if labels are nominal
     		if (labelsAreNominal) {
     			String labelValue = label.getLabelValue();
     			if (labelValue == null) {
     				labelValue = NULL;
     				logger.error("Label " + labelName + " is missing for a document. Setting class value of " + NULL);
     			}
     			if (labelClasses.containsKey(labelName))
     				labelClasses.get(labelName).add(labelValue.trim());
     			else {
     				Set<String> classValues = new HashSet<>();
     				classValues.add(labelValue.trim());
     				labelClasses.put(labelName, classValues);
     			}
     		}
     	}
 	}
 
 	/**
 	 * Calculates encoding of a term in a feature vector using one of the many encoding choices, 
 	 * such as TF, TF*IDF, etc.
 	 * 
 	 * @param encodingMethod Binary, TF, TFIDF, COUNT, LOG(TF+1)
 	 * @param currentValue Currently encoded value for this term
 	 * @param numTerms
 	 * @param numTermsInDocument
 	 * @param documentFrequency
 	 * @return
 	 */
 	public static double encode(int encodingMethod,
 			int numTerms, int numTermsInDocument, double documentFrequency) {
 		double tf;
 		double encodedValue = 0.0;
 		switch(encodingMethod) {
 		case Encoder.ENCODINGMETHOD_BINARY:
 			encodedValue = 1.0;
 			break;
 		case Encoder.ENCODINGMETHOD_TF:
 			tf = ((double) numTerms) / ((double) numTermsInDocument);
 			encodedValue =  tf;
 			break;
 		case Encoder.ENCODINGMETHOD_TFxIDF:
 			tf = ((double) numTerms) / ((double) numTermsInDocument);
 			if (tf > 0)
 				encodedValue =  tf * Math.log(1/documentFrequency);
 			break;
 		case Encoder.ENCODINGMETHOD_COUNT:
 			encodedValue = numTerms;
 			break;
 		case Encoder.ENCODINGMETHOD_LOG_TF:
 			tf = ((double) numTerms) / ((double) numTermsInDocument);
 			encodedValue =  Math.log(1.0 + tf);
 			break;
 		case Encoder.ENCODINGMETHOD_LOG_TFxIDF:
 			tf = ((double) numTerms) / ((double) numTermsInDocument);
 			if (tf > 0)
 				encodedValue =  Math.log(1.0 + tf) * Math.log(1/documentFrequency);
 			break;
 		}
 		return encodedValue;
 	}
 	
 	/**
 	 * Given a label annotation, get its value.
 	 * This getter takes care of null values, and uses the special class
 	 * NULL to return appropriately
 	 * @param label
 	 * @return
 	 */
 	public static String getLabelValue(ClassLabel label, Logger messageLogger) {
 		String retVal = label.getLabelValue();
 		if (retVal == null) {
 			messageLogger.debug("Label " + label.getLabelName() + "has a null value.");
 			retVal = NULL;
 		}
 		return retVal.trim();
 	}
 	
 	public static Double getLabelDoubleValue(ClassLabel label, Logger messageLogger) {
 		String labelValue = label.getLabelValue();
 		Double retVal = null;
 		if (labelValue != null) {
 			retVal = Double.parseDouble(labelValue.trim());
 		} else {
 			messageLogger.debug("Label " + label.getLabelName() + "has a null value. Setting default of 0.");
 			retVal = 0.0;
 		}
 		return retVal;
 	}
 }
