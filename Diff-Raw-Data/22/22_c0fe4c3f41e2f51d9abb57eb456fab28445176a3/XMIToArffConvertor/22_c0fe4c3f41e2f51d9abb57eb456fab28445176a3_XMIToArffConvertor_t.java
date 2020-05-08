 package org.dbmi.uima.tools.components.idash;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.uima.cas.CAS;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.cas.FeatureStructure;
 import org.apache.uima.cas.TypeSystem;
 import org.apache.uima.cas.impl.XmiCasDeserializer;
 import org.apache.uima.cas.text.AnnotationIndex;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceConfigurationException;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.resource.ResourceProcessException;
 import org.apache.uima.util.CasCreationUtils;
 import org.dbmi.uima.tools.components.idash.type.ClassLabel;
 import org.xml.sax.SAXException;
 
 import edu.mayo.bmi.uima.core.type.refsem.OntologyConcept;
 import edu.mayo.bmi.uima.core.type.refsem.UmlsConcept;
 import edu.mayo.bmi.uima.core.type.syntax.WordToken;
 import edu.mayo.bmi.uima.core.type.textsem.EntityMention;
 
 import weka.core.Attribute;
 import weka.core.FastVector;
 import weka.core.Instances;
 import weka.core.SparseInstance;
 import weka.core.converters.ArffSaver;
 
 /**
  * Convert all XMI files in a given directory into one single
  * encoded ARFF file.
  * Not thread-safe.
  * 
  * @author abhishek
  *
  */
 public class XMIToArffConvertor {
 	
 	private Logger logger = Logger.getLogger(getClass().getName());
 	
 	/* Dictionary of all features, and their statistics */
 	Map<String, FeatureStats> stats = null;
 	
 	/* Directory where all the XMI files to be read, are located */
 	File mOutputDir = null;
 	
 	/* Feature Groups to encode. Shouldn't include LABELS */
 	List<String> featureGroupsToEncode = null;
 	
 	/* Labels and their class values, if we are encoding nominal labels */
 	Map<String, Set<String>> labelClasses = null;
 	Map<String, FastVector> labelClassesWeka = null;
 	
 	/* Document frequencies of terms, as read from training set when we are encoding test set */
 	Map<String, Double> trainingSetDocumentFrequency;
 	
 	/**
 	 *  Map to store regex pattern to feature name mappings.
 	 *  Key = Annotation type (from type system)
 	 *  Value = Name of the feature being encoded
 	 */
 	Map<Integer, String> patternToFeatureMapping = null;
 	
 	TypeSystem typeSystem = null;
 	Integer encodingMethod = null;
 	boolean labelsAreNominal;
 	
 	String outputFileName;
 	String trainingSetFileName;
 	
 	/**
 	 * The only ctor.
 	 * @param referenceStats
 	 * @param outputDirectory
 	 * @param orderedFeatureGroupsToEncode
 	 * @param labelClassesToEncode can be null, if labels are not nominal (i.e. they are numeric)
 	 * @param regexAnnotationTypeToFeatureName Mapping between regex annotation data type and its feature group name
 	 * @param typeSystemOfXMIFiles type system of the XMI files to encode. All files must adhere to this.
 	 */
 	public XMIToArffConvertor(Map<String, FeatureStats> referenceStats, 
 			File outputDirectory, List<String> orderedFeatureGroupsToEncode,
 			Map<String, Set<String>> labelClassesToEncode,
 			Map<Integer, String> regexAnnotationTypeToFeatureName,
 			TypeSystem typeSystemOfXMIFiles,
 			int encodingChoice,
 			boolean labelEncodingChoice,
 			String outFileName,
 			String trainingSetFile) {
 		stats = referenceStats;
 		mOutputDir = outputDirectory;
 		featureGroupsToEncode = orderedFeatureGroupsToEncode;
 		labelClasses = labelClassesToEncode;
 		patternToFeatureMapping = regexAnnotationTypeToFeatureName;
 		typeSystem = typeSystemOfXMIFiles;
 		encodingMethod = encodingChoice;
 		labelsAreNominal = labelEncodingChoice;
 		outputFileName = outFileName;
 		trainingSetFileName = trainingSetFile;
 	}
 	
 	public void encodeXMIFiles() throws IOException, ResourceProcessException {
 
 		logger.info("Going to encode XMI files into ARFF now");
 		
 		/* Read CAS from XMI files, and encode features to csv format */
 	    if (!mOutputDir.exists() || !mOutputDir.isDirectory())
 	    	throw new IOException("Directory not found: ".concat(mOutputDir.getAbsolutePath()));
 	    
   	  	File[] files = mOutputDir.listFiles();
   	  	FastVector attributes = new FastVector();
   	  	HashMap<String, Integer> attributePositions = new HashMap<String, Integer>();
   	  	int position = 0;
   	  	trainingSetDocumentFrequency = null;
 	  	
   	  	// If this is a test set, we must base our attributes on the training set
   	  	if ( (trainingSetFileName != null) && !trainingSetFileName.isEmpty()) {
   	  		BufferedReader reader = new BufferedReader(new FileReader(trainingSetFileName));
   	  		Instances trainingSet = new Instances(reader);
   	  		labelClassesWeka = new HashMap<String, FastVector>();
   	  		for (int i=0; i<trainingSet.numAttributes(); ++i) {
   	  			Attribute a = trainingSet.attribute(i);
   	  			
   	  			if (a.isNominal()) {
   	  				List<String> labelValues = Collections.list(a.enumerateValues());
   	  				FastVector attributeValues = new FastVector(labelValues.size());
   	  				for (String value : labelValues)
   	  					attributeValues.addElement(value);
   	  				labelClassesWeka.put(a.name(), attributeValues);
   	  				attributes.addElement( new Attribute(a.name(), attributeValues) );
   	  			} else if(a.isString())
   	  				attributes.addElement( new Attribute(a.name(), (FastVector) null) );
   	  			else
   	  				attributes.addElement( new Attribute(a.name()) );
   	  			attributePositions.put(a.name(), i);
   	  			
   	  		}
   	  		
   	  		// get the df values from training set
   	  		getDocumentFrequencies(trainingSet);
   	  	} else {
   	  		// This is a fresh training set.
   	  		
   	  		// Add a string feature to hold doc name
   	  	  	attributes.addElement(new Attribute("DocumentName", (FastVector) null));
   	  	  	attributePositions.put("DocumentName", position++);
   	  	  	
   	  	  	// Map each feature (or term) to a unique position in the attribute list. 
   	  	  	// Also assign a unique feature name to each term.
   	  	  	for(String featureGroupName : featureGroupsToEncode) {
   		  	  	for(String term : stats.get(featureGroupName).getTerms()) {
   		  	  		String attributeName = featureGroupName.concat("_").concat(term);
   			  		attributes.addElement(new Attribute(attributeName));
   			  		attributePositions.put(attributeName, position++);
   			  	}
   	  	  	}
   	  	  	
   	  	  	// Add labels
   	  	  	logger.info("Adding label data to dataset:" + labelClasses.toString());
   	  	  	labelClassesWeka = new HashMap<String, FastVector>();
   	  	  	for (String labelName : labelClasses.keySet()) {
   	  	  		Set<String> labelValues = labelClasses.get(labelName);
   	  	  		
   	  	  		String attributeName = new StringBuilder(Encoder.LABELS).append("_").append(labelName).toString();
   	  	  		FastVector attributeValues = new FastVector(labelValues.size());
   	  	  		for (String value : labelValues)
   	  	  			attributeValues.addElement(value);
   	  	  		
   	  	  		attributes.addElement(new Attribute(attributeName, attributeValues));
   	  	  		attributePositions.put(attributeName, position++);
   	  	  		labelClassesWeka.put(attributeName, attributeValues);
   	  	  	}
   	  	}
   	  	
 	    
   	  	// Create the dataset
 	  	Instances dataset = new Instances("Dataset", attributes, files.length);
 	  	for (int i = 0; i < files.length; i++) {
 	      if (!files[i].isDirectory() && files[i].getName().endsWith(".xmi")) {
 	    	  logger.debug("Encoding file ".concat(files[i].getName()));
 	    	  
 	    	  // Deal with this XMI file
 	    	  double values[] = encodeXmiFile(files[i], attributePositions, dataset);
 		    	
 	    	  // Add to dataset
 	    	  dataset.add(new SparseInstance(1.0, values));
 	      }
 	    }
 	  	
 	  	// Write dataset to file.
 	  	ArffSaver arffSaverInstance = new ArffSaver();
 	  	arffSaverInstance.setInstances(dataset);
 	  	String filePath = mOutputDir.getPath().concat(File.separator).concat(outputFileName);
 	  	arffSaverInstance.setFile(new File(filePath));
 	  	arffSaverInstance.writeBatch();
 	  	logger.info(String.format("Encoded file written to '%s'", filePath));	  	
 	}
 	
 	/**
 	 * Given a CAS in an XMI file, read the file and write out a feature vector.
 	 * @param inFile XMI file to encode. Cannot be null
 	 * @param typeSystemCAS Type system of the XMI files to be read. Cannot be null
 	 * @param attributePositions mapping between attribute name and its position in the feature vector
 	 * @param dataset the dataset to write to (output)
 	 * @throws ResourceProcessException
 	 */
 	private double[] encodeXmiFile(File inFile, 
 			HashMap<String, Integer> attributePositions, Instances dataset) 
 			throws ResourceProcessException {
 	    try {
 	    	logger.info("Encoding file ".concat(inFile.getName()));
 	    	
 	    	FileInputStream inputStream = new FileInputStream(inFile);
 	    	CAS aCAS = CasCreationUtils.createCas(typeSystem, null, null, null);
 	        XmiCasDeserializer.deserialize(inputStream, aCAS, true);
 	        JCas aJCas = aCAS.getJCas();
 	        
 	    	double[] values = new double[attributePositions.size()];
 	    	
 	    	// Add document name
 	    	values[0] = dataset.attribute(0).addStringValue(inFile.getName());
 	    	
 	    	// Iterate through all ENTITIES
 			encodeEntityMentions(aJCas, attributePositions, values);
 
 			// Iterate through all REGEX Patterns
 			encodeRegularExpressions(aJCas, attributePositions, values);
 			
 			// Iterate through all Words
 			encodeWordTokens(aJCas, true, attributePositions, values);
 			
 			// Finally encode the labels
 			encodeLabels(aJCas, attributePositions, values, dataset);
 			
			
			// DEBUG: check for a -1 value which causes errors
			for (Integer i=0; i<values.length; ++i)
				if (values[i] < 0)
					logger.error("Value -1 found for attribute ".concat(i.toString()).concat(" file ").concat(inFile.getName()));
 	    	return values;
 	    } catch (ResourceInitializationException | SAXException | IOException | CASException e) {
 	    	throw new ResourceProcessException(e);
 		}
 	}
 	
 	private void encodeEntityMentions(JCas aJCas, HashMap<String, Integer> attributePositions, double[] encodedValues) {
 		AnnotationIndex<Annotation> idx = aJCas.getAnnotationIndex(EntityMention.type);
     	FSIterator<Annotation> entities = idx.iterator();
     	Map<String, String> attributeNameToLocalName = new HashMap<>();
     	Map<String, Integer> attributeNameCounts = new HashMap<>();
     	int numEntities = idx.size();
     	while(entities.hasNext()) {
 			EntityMention entity = (EntityMention) entities.next();
 			for(FeatureStructure fs : entity.getOntologyConceptArr().toArray()) {
 				OntologyConcept concept = (OntologyConcept) fs;
 				String attributeName = "", localName = "";
 				
 				// Find the attribute name (CUI or OUI)
 				if(concept.getCodingScheme().equals(Encoder.SNOMED_CODING_SCHEME)) {
 					// Encode cui
 					localName = ((UmlsConcept) concept).getCui();
 					attributeName = Encoder.ENTITIES.concat("_").concat(localName);
 				} else if(concept.getCodingScheme().equals(Encoder.RXNORM_CODING_SCHEME)) {
 					// Encode Oui
 					localName = concept.getOui();
 					attributeName = Encoder.ENTITIES.concat("_").concat(localName);
 				}
 				
 				
 				// record this entity's occurrence
 				int prev = 0;
 				if (attributeNameCounts.containsKey(attributeName))
 					prev = attributeNameCounts.get(attributeName);
 				attributeNameCounts.put(attributeName, prev + 1);
 				attributeNameToLocalName.put(attributeName, localName);
 			}
 		}
     	
     	// Encode the entities
     	for (String attributeName : attributeNameCounts.keySet())
     		encodeTerm(attributeName, attributeNameToLocalName.get(attributeName), attributePositions, 
     				encodedValues, stats.get(Encoder.ENTITIES), numEntities, attributeNameCounts.get(attributeName));
 	}
 	
 	private void encodeWordTokens(JCas aJCas, boolean encodeCanonicalForms, 
 			HashMap<String, Integer> attributePositions, double[] encodedValues) {
 		
 		// Get all word tokens
 		AnnotationIndex<Annotation> idx = aJCas.getAnnotationIndex(WordToken.type);
 		FSIterator<Annotation> words = idx.iterator();
 		Map<String, String> attributeNameToLocalName = new HashMap<>();
     	Map<String, Integer> attributeNameCounts = new HashMap<>();
     	
     	Set<String> uniqueWords = new HashSet<>();
     	Set<String> uniqueCanonicalWords = new HashSet<>();
     	int canonicalCount = 0;
 		int numWords = idx.size();
 		
 		while(words.hasNext()){
 			WordToken word = (WordToken) words.next();
 			String localName = word.getCoveredText();
 			String attributeName = Encoder.WORDS.concat("_").concat(localName);
 			
 			// record this word's occurrence
 			int prev = 0;
 			if (attributeNameCounts.containsKey(attributeName))
 				prev = attributeNameCounts.get(attributeName);
 			attributeNameCounts.put(attributeName, prev + 1);
 			attributeNameToLocalName.put(attributeName, localName);
 			uniqueWords.add(attributeName);
 			
 			
 			localName = word.getCanonicalForm();
 			if(encodeCanonicalForms && (localName != null) && (!localName.isEmpty())) {
 				canonicalCount++;
 				attributeName = Encoder.CANONICALWORDS.concat("_").concat(localName);
 				
 				prev = 0;
 				if (attributeNameCounts.containsKey(attributeName))
 					prev = attributeNameCounts.get(attributeName);
 				attributeNameCounts.put(attributeName, prev + 1);
 				attributeNameToLocalName.put(attributeName, localName);
 				uniqueCanonicalWords.add(attributeName);
 			}
 		}
 		
 		// Encode words
 		for (String attributeName : uniqueWords)
 			encodeTerm(attributeName, attributeNameToLocalName.get(attributeName), attributePositions, encodedValues, 
 					stats.get(Encoder.WORDS), numWords, attributeNameCounts.get(attributeName));
 		
 		// Encode canonical words
 		for (String attributeName : uniqueCanonicalWords)
 			encodeTerm(attributeName, attributeNameToLocalName.get(attributeName), attributePositions, encodedValues, 
 					stats.get(Encoder.CANONICALWORDS), canonicalCount, attributeNameCounts.get(attributeName));
 	}
 	
 	private void encodeRegularExpressions(JCas aJCas, 
 			HashMap<String, Integer> attributePositions, double[] encodedValues) {
 		
 		// Collect stats of each regex pattern and process them
 		int totalCount = 0;
 		for(Integer annotationType : patternToFeatureMapping.keySet())
 			totalCount += aJCas.getAnnotationIndex(annotationType).size();
 
 		for(Integer annotationType : patternToFeatureMapping.keySet()) {
 			String localName = patternToFeatureMapping.get(annotationType);
 			String attributeName = Encoder.REGEX.concat("_").concat(localName);
 			int count = aJCas.getAnnotationIndex(annotationType).size();
 			encodeTerm(attributeName, localName, attributePositions, encodedValues, 
 					stats.get(Encoder.REGEX), totalCount, count);
 		}
 	}
 	
 	private void encodeLabels(JCas aJCas, 
 			HashMap<String, Integer> attributePositions, double[] encodedValues, Instances dataset) {
 		FeatureStats fs = stats.get(Encoder.LABELS);
 		
 		// set of labels that we've encoded
 		Set<String> encodedLabels = new HashSet<String>(fs.size());
 		
 		// Start encoding
 		StringBuilder logMessage = new StringBuilder("Encoded labels.");
 		FSIterator<Annotation> labels = aJCas.getAnnotationIndex(ClassLabel.type).iterator();
     	while(labels.hasNext()) {
     		ClassLabel label = (ClassLabel) labels.next();
     		String lblName = label.getLabelName();
     		String attributeName = Encoder.LABELS.concat("_").concat(label.getLabelName());
     		if (labelsAreNominal) {
     			String labelValue = Encoder.getLabelValue(label, logger);
     			logMessage.append(lblName).append("=").append(labelValue).append(";");
     			encodedValues[attributePositions.get(attributeName)] =
     					labelClassesWeka.get(attributeName).indexOf(labelValue);
     		} else {
     			Double labelValue = Encoder.getLabelDoubleValue(label, logger);
     			logMessage.append(lblName).append("=").append(labelValue).append(";");
     			encodedValues[attributePositions.get(attributeName)] = labelValue;
     		}
     		encodedLabels.add(lblName);
     	}
     	
     	// If there was any label that we didn't encode, we must print to log
     	for (String labelName : fs.getTerms())
     		if(!encodedLabels.contains(labelName))
     			logger.error(String.format("label %s Missing. Will default to 0.", labelName));
     	logger.debug(logMessage.toString());
 	}
 
 	/**
 	 * 
 	 * @param term The term to be encoded
 	 * @param localTermName The name by which the term is known locally within FeatureStats.
 	 * @param attributePositions mapping between term and its position in the feature vector.
 	 * @param featureVector the vector encoding of features for a document
 	 * @param fs {@code FeatureStats} representing the feature class of {@code term}
 	 * @param numTermsInDocument Number of terms of this feature class in this doc
 	 * @param numTerms number of these terms found in the document (count to be encoded)
 	 */
 	private void encodeTerm(String term, String localTermName, Map<String, Integer> attributePositions, 
 			double[] featureVector, FeatureStats fs, int numTermsInDocument, int numTerms) {
 		if(!attributePositions.containsKey(term))
 			return;
 		int position = attributePositions.get(term);
 		if (trainingSetDocumentFrequency == null ) { // we are encoding a training set 
 			featureVector[position] = Encoder.encode(encodingMethod, 
 					numTerms, numTermsInDocument, 
 					fs.getDocumentFrequency(localTermName));
 		} else { // we are encoding a test set, read df from training set
 			featureVector[position] = Encoder.encode(encodingMethod, 
 					numTerms, numTermsInDocument, 
 					trainingSetDocumentFrequency.get(term));
 		}
 	}
 	
 	private void getDocumentFrequencies(Instances trainingSet) {
 		trainingSetDocumentFrequency = new HashMap<>();
 		int D = trainingSet.numAttributes(); int m = trainingSet.numInstances();
 		StringBuilder sb = new StringBuilder("Read df values from training set.");
 		for (int j=0; j<D; ++j) {
 			double count = 0;
 			for (int i=0; i<m; ++i)
 				if (trainingSet.instance(i).value(j) > 0.0)
 					++count;
 			double df = count / ((double) m);
 			trainingSetDocumentFrequency.put(trainingSet.attribute(j).name(), df);
 			sb.append("\n\tAttribute ").append(j).append(". df=").append(df);
 		}
 		logger.debug(sb.toString());
 	}
 	
 }
