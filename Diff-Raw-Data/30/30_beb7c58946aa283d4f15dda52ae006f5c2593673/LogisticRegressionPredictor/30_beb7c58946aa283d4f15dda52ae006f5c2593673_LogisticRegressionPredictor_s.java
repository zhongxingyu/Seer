 package mlproject.predictors;
 
 import java.lang.reflect.Field;
 import java.util.Collection;
 import java.util.Enumeration;
 
 import weka.classifiers.functions.Logistic;
 import weka.core.Attribute;
 import weka.core.FastVector;
 import weka.core.Instance;
 import weka.core.Instances;
 
 import mlproject.Utils;
 import mlproject.models.IgnoreField;
 import mlproject.models.Issue;
 //import mlproject.models.TargetField;
 
 public class LogisticRegressionPredictor extends BasePredictor {
 
 	Logistic logistic;
 	final FastVector attributes;
 	
 	public LogisticRegressionPredictor() {
 		Field[] fs = Issue.class.getFields();
 		attributes = new FastVector(fs.length);
 		for(int i = 0; i < fs.length; i++) attributes.addElement(new Attribute(Integer.toString(i)));		
 	}
 	
 	@Override
 	public double Predict(Issue issue) {
 		try {
 			return logistic.classifyInstance(getWekaInstance(issue));
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
	public static void main(String[] args) {		
 		LogisticRegressionPredictor p = new LogisticRegressionPredictor();
 		System.out.println("size: " + p.attributes.size());
 		Instances instances = new Instances("Issue", p.attributes, 300);
 
 		Enumeration<?> enumer = instances.enumerateAttributes();
 		System.out.println("Enums");
 		while(enumer.hasMoreElements()) {
 			System.out.println(enumer.nextElement().toString());
 		}
 	}
 	
 	@Override
 	public void Train(Collection<Issue> issues) {
 		Field[] fs = Issue.class.getFields();
 		FastVector fv = new FastVector(fs.length);
		for(int i = 0; i < fs.length; i++) fv.addElement(new Attribute(fs[i].getName()));
 		Instances instances = new Instances("Issue", fv, 300);
 		
 		for(Issue issue: issues) {
 			Instance instance = getWekaInstance(issue);
 			instances.add(instance);
 			instance.setDataset(instances);
 		}
 		
 		
 		logistic = new Logistic();
 		try {
 			
 			logistic.buildClassifier(instances);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public String name() {
 		return "Logistic Regression Predictor";
 	}
 	
 	public Instance getWekaInstance(Issue issue) {
 		Field[] fs = Issue.class.getFields();
 		Instance instance = new Instance(fs.length);
 		System.out.println("NUMBER OF ATTRIBUTES " + instance.numAttributes());
 		for(int i = 0; i < fs.length; i++) {
 			if (fs[i].isAnnotationPresent(IgnoreField.class)) continue;
 			try {
				instance.setValue(i, Utils.toDouble(fs[i].get(issue)));
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 				throw new RuntimeException(e);
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 				throw new RuntimeException(e);
 			}
 		}
 		
 		return instance;
 	}
 
 }
