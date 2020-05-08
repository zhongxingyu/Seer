 package prefwork.rating.method;
 
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.XMLConfiguration;
 
 import prefwork.core.DataSource;
 import prefwork.core.Method;
 import prefwork.core.UserEval;
 import prefwork.core.Utils;
 import prefwork.rating.Rating;
 import prefwork.rating.datasource.ContentDataSource;
 import weka.classifiers.Classifier;
 import weka.core.Attribute;
 import weka.core.DenseInstance;
 import weka.core.Instance;
 import weka.core.Instances;
 
 public class WekaBridge implements Method {
 
 	// Create a naive bayes classifier
 	String classifierName = "weka.classifiers.bayes.NaiveBayes";
 	Classifier cModel = (Classifier) new weka.classifiers.bayes.NaiveBayes();
 
 	boolean wantsNumericClass = true;
 
 	boolean onlyNumericAttributes = false;
 
 	boolean onlyNominalAttributes = false;
 	
 	boolean noMissing = false;
 
 	protected Instances attributes;
 	protected ContentDataSource data;
 	
 	/**Indexes of numerical attributes.*/
 	List<Integer> numerical;
 	/**Indexes of nominal attributes.*/
 	List<Integer> nominal;
 	List<Integer> indexes;
 	Integer targetAttribute;
 
 	ArrayList<Attribute> fvWekaAttributes;
 
 	Instances isTrainingSet;
 
 	public String toString() {
 		return classifierName+(wantsNumericClass?"0":"1");
 	}
 
 	/**
 	 * Creates a Weka Instance from a Rating. We need to transform the attributes, if the method supports only numerical, etc.
 	 * @param rec
 	 * @return
 	 */
 	protected Instance getWekaInstance(Rating rec) {
 		Instance iExample = getInstanceFromIndexes(rec);
 		//iExample = (Instance) rec.getRecord().copy();
 		return iExample;
 	}
 
 
 	/**
 	 * Creates a Weka Instance from a Rating. We need to transform the attributes, if the method supports only numerical, etc.
 	 * @param rec
 	 * @return
 	 */
 	protected Instance getInstanceFromIndexes(Rating rec) {
 		double[] vals = new double[isTrainingSet.numAttributes()];
 		
 		for (int i = 0; i < indexes.size(); i++) {
 			try {
 				if(rec.getRecord().isMissing(indexes.get(i)))
 						continue;
 				 if(numerical.contains(indexes.get(i)) && !wantsNumericClass && targetAttribute == indexes.get(i)){
 						Utils.addStringValue(Double.toString(rec.getRecord().value(indexes.get(i))), vals, fvWekaAttributes.get(i));
 						
 					}
 				 else if ( numerical.contains(indexes.get(i)))
 					 vals[i] = rec.get(indexes.get(i));
 				 //iExample.setValue((Attribute) fvWekaAttributes.elementAt(i),rec.get(indexes.get(i)));				
 				else
 					Utils.addStringValue(rec.getRecord().stringValue(indexes.get(i)), vals, fvWekaAttributes.get(i));
 					/*iExample.setValue(
 							(Attribute) fvWekaAttributes.elementAt(i), 
 							rec.getRecord().stringValue(indexes.get(i)));*/
 			} catch (IllegalArgumentException e) {
 				// e.printStackTrace();
 				if (noMissing) {
 					/*iExample.setValue(
 							(Attribute) fvWekaAttributes.elementAt(i),
 							((Attribute) fvWekaAttributes.elementAt(i))
 									.value(0));*/
 				}
 
 				//System.err.print("" + rec.get(i).toString() + "," + i + "\n");
 			}
 		}
 		Instance iExample = null;
 		iExample = new DenseInstance(isTrainingSet.numAttributes(), vals);
 		iExample.setDataset(isTrainingSet);
 		
 		//Fill the class. Class is always numeric, for the time being.
 		/*try {
 			if (wantsNumericClass)
 				iExample.setValue((Attribute) fvWekaAttributes
 						.elementAt(targetAttribute), rec.get(
 								indexes.get(targetAttribute)));
 			else
 				Utils.addStringValue(rec.getRecord().stringValue(indexes.get(targetAttribute)), iExample, (Attribute)fvWekaAttributes.elementAt(targetAttribute));
 			
 		} catch (IllegalArgumentException e) {
 			// e.printStackTrace();
 			if (noMissing) {
 				iExample.setValue((Attribute) fvWekaAttributes
 						.elementAt(targetAttribute),
 						((Attribute) fvWekaAttributes
 								.elementAt(targetAttribute)).value(0));
 			}
 		}*/
 
 		return iExample;
 	}
 
 	protected void processAttribute(Rating rec, ArrayList<String> vec, int i) {
 		if(rec.getRecord().isMissing(i))
 			return;
 		//It is nominal, use stringValue
 		if(nominal.contains(indexes.get(i))){
 			if (!vec.contains(rec.getRecord().stringValue(indexes.get(i))))
 				vec.add(rec.getRecord().stringValue(indexes.get(i)));			
 		}
 		//It is numeric we want to treat as nominal.
 		else{
 			if (!vec.contains(Double.toString(rec.getRecord().value(indexes.indexOf(i)))))
 				vec.add(Double.toString(rec.getRecord().value(indexes.indexOf(i))));
 			
 		}
 	}
 
 	protected void getAttributes(DataSource ds, Integer user) {
 		ContentDataSource trainingDataset = (ContentDataSource)ds;
 		trainingDataset.setFixedUserId(user);
 		trainingDataset.restart();
 		int size = indexes.size();
 		String[] attributeNames = trainingDataset.getAttributesNames();		
 		@SuppressWarnings("unchecked")
 		ArrayList<String>[] vec = new ArrayList[size];
 		for (int i = 0; i < vec.length; i++) {
 			vec[i] = new ArrayList<String>();
 		}
 
 		Rating rec;
 		if (!onlyNumericAttributes) {
 			while ((rec = (Rating)trainingDataset.next()) != null) {
 				for (int i = 0; i < indexes.size(); i++) {
 					if(( i == targetAttribute && !wantsNumericClass) || nominal.contains(indexes.get(i)))
 						processAttribute(rec, vec[i], i);
 				}
 			}
 		}
 
 		fvWekaAttributes = new ArrayList<Attribute>(size);
 		for (int i = 0; i < size; i++) {
 			// Add a nominal attribute
 			if ((vec[i].size() > 0 && !onlyNumericAttributes) 
 					|| onlyNominalAttributes)
 				fvWekaAttributes.add(new Attribute(
 						ProgolBridge.transform(attributeNames[indexes.get(i)]), vec[i], i));
 			// Add a numerical attribute
 			else if (!onlyNominalAttributes || ( i == targetAttribute && wantsNumericClass))
 				// fvWekaAttributes.add(new Attribute(attributeNames[i],
 				// new ArrayList()));
 				fvWekaAttributes.add(new Attribute(
 						ProgolBridge.transform(attributeNames[indexes.get(i)]), i));
 		}
 	}
 
 	@SuppressWarnings("rawtypes")
 	protected void clear(){
 		nominal = Utils.getList();
 		numerical = Utils.getList();
 		indexes = Utils.getList();
 		fvWekaAttributes = null;
 		isTrainingSet = null;
 		try {
 			Class c = Class.forName(classifierName);
 			Constructor[] a = c.getConstructors();
 			cModel = (Classifier) a[0].newInstance();		
 		}catch (Exception e) {}
 	}
 	
 	/**
 	 * Initializes the attributes indexes. We need to know which are nominal and which are numerical.
 	 * @param ds
 	 */
 	protected void init(DataSource ds) {
 		ContentDataSource trainingDataset = (ContentDataSource)ds;
 		Instances attrs = trainingDataset.getInstances();
 		for (int i = 0; i < attrs.numAttributes(); i++) {
 			Attribute attr = attrs.attribute(i);
 			if (attr.isNumeric())
 				numerical.add(i);
 			else if (attr.isNominal() || attr.isString())
 				nominal.add(i);
 		}
 		if (onlyNumericAttributes)
 			indexes.addAll(numerical);
 		else if (onlyNominalAttributes)
 			indexes.addAll(nominal);
 
 		if (onlyNumericAttributes
 				&& !numerical.contains(trainingDataset.getClassAttributeIndex())) {
 			indexes.add(trainingDataset.getClassAttributeIndex());
 		} else if (onlyNominalAttributes
 				&& !nominal.contains(trainingDataset.getClassAttributeIndex())) {
 			indexes.add(trainingDataset.getClassAttributeIndex());
 		}
 		
 		else {
 			List<Integer> all = Utils.getList();
 			all.addAll(numerical);
 			all.addAll(nominal);
 			indexes = all;
 		}
 		targetAttribute = indexes.indexOf(trainingDataset.getClassAttributeIndex());
 		
 	}
 
 	public int buildModel(DataSource ds, int user) {clear();
 		ContentDataSource trainingDataset = (ContentDataSource)ds;
 		data = trainingDataset;
 		attributes = trainingDataset.getInstances();
 		init(ds);
 		getAttributes(trainingDataset, user);
 		trainingDataset.setFixedUserId(user);
 		trainingDataset.restart();
 		if (!trainingDataset.hasNext())
 			return 0;
 		
 		int count = 0;
 		// Create an empty training set
 		isTrainingSet = new Instances("Rel", fvWekaAttributes, 10);
 		// Set class index
 		isTrainingSet.setClassIndex(targetAttribute);
 		while(trainingDataset.hasNext()){
 			Rating r = (Rating) trainingDataset.next();
 			isTrainingSet.add(getWekaInstance(r));
 
 			count++;
 		}
 		try {
 			cModel.buildClassifier(isTrainingSet);
 		} catch (Exception e) {
			e.printStackTrace();
 			cModel = null;
 		}
 		return count;
 	}
 
 	public Double classifyRecord(UserEval record) {
 		Rating rating=(Rating)record;
 		try {
 			//The model wasn't built, we return null.
 			if(cModel == null)
 				return null;
 			Instance inst = getWekaInstance(rating);
 			//double r = cModel.classifyInstance(inst);
 			double[] fDistribution = cModel.distributionForInstance(inst);
 			double res=0.0;
 			for(int i=0;i<fDistribution.length;i++)
 				if(wantsNumericClass)
 					res+=fDistribution[i];
 				else
 					res+=Utils.objectToDouble(fvWekaAttributes.get(this.targetAttribute).value(i))*fDistribution[i];
 			return res;
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 		return null;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public void configClassifier(XMLConfiguration config, String section) {
 		Configuration methodConf = config.configurationAt(section);
 		try {
 			classifierName = Utils.getFromConfIfNotNull(methodConf, "classifier", classifierName);
 			Class c = Class.forName(classifierName);
 			Constructor[] a = c.getConstructors();
 			cModel = (Classifier) a[0].newInstance();		
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		try {
 		    wantsNumericClass = methodConf.getBoolean("wantsNumericClass", wantsNumericClass);
 		}catch (Exception e) {}
 		try {
 			onlyNominalAttributes = methodConf.getBoolean("onlyNominalAttributes", onlyNominalAttributes);
 		}catch (Exception e) {}
 		try {
 			onlyNumericAttributes = methodConf.getBoolean("onlyNumericAttributes", onlyNumericAttributes);
 		}catch (Exception e) {}
 	}
 
 }
