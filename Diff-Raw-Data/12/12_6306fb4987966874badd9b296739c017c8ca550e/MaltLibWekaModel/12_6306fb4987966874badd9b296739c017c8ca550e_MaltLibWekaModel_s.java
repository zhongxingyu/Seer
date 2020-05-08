 package maltlibweka;
 
 import java.io.Serializable;
 import java.util.Map;
 
 import org.maltparser.ml.lib.MaltFeatureNode;
 import org.maltparser.ml.lib.MaltLibModel;
 
 import weka.classifiers.Classifier;
 import weka.classifiers.functions.Logistic;
 import weka.core.Attribute;
 import weka.core.FastVector;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.filters.Filter;
 import weka.filters.supervised.attribute.NominalToBinary;
 
 public class MaltLibWekaModel implements Serializable, MaltLibModel {
     private static final long serialVersionUID = 3423414687296070741L;
     private Classifier classifier;
     private Map<String, FastVector> nominalMap;
 
     // remember the Instances object that we're using so we don't
     // have to construct it over and over again.
     private Instances save_instances = null;
 
     private int classUpperBound;
     // attributes for weka instances, before feature binarization.
     private FastVector attInfoBeforeHacks;
     // attributes for weka instances, what was actually used to train the
     // classifier.
     private FastVector attInfoPostHacks;
 
     /**
      * Construct a MaltLibWekaModel with the specified weka classifier and
      * maximum class number. (classification might return any number up to the
      * upperbound - 1)
      * 
      * @param classifier
      * @param attinfo
      * @param attinfoPostHacks
      * @param nomMap
      */
     public MaltLibWekaModel(Classifier classifier,
 	    FastVector attinfoBeforeHacks, FastVector attinfoPostHacks,
 	    Map<String, FastVector> nomMap, int classUpperBound) {
 	this.classifier = classifier;
 	this.nominalMap = nomMap;
 	this.attInfoBeforeHacks = attinfoBeforeHacks;
 	this.attInfoPostHacks = attinfoPostHacks;
 	this.classUpperBound = classUpperBound;
     }
 
     /**
      * We're given an array of MaltFeatureNode, and we have to come up with a
      * classification. In order to do that, produce a weka instance.
      */
     @Override
     public int[] predict(MaltFeatureNode[] x) {
 	double prediction = 0;
 	try {
 	    Instances instances = getInstances(x);
 	    Instance instance = featuresToInstance(x, attInfoBeforeHacks);
 	    // We may need to mangle the attributes to match the attributes in
 	    // the classifier.
 	    instances = classifierSpecificHacks(instances);
 	    prediction = classifier.classifyInstance(instances.firstInstance());
 	    instances.delete();
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    System.exit(1);
 	}
 	int[] out = new int[classUpperBound];
 	for (int i = 0; i < classUpperBound; i++) {
 	    out[i] = (int) prediction;
 	}
 	return out;
     }
 
     /**
      * Create the weka Instances object that we'll use to hold our weka
      * instances, then classify them. Has to know about the appropriate
      * attributes. We may mangle these.
      * 
      * @param x
      * @return
      */
     private Instances getInstances(MaltFeatureNode[] x) {
 	if (save_instances != null) {
 	    return save_instances;
 	}
 	save_instances = new Instances("MaltFeatures", attInfoBeforeHacks, 0);
 	save_instances.setClass(save_instances.attribute(save_instances
 		.numAttributes() - 1));
 	return this.save_instances;
     }
 
     private Instances classifierSpecificHacks(Instances instances)
 	    throws Exception {
 	if (classifier instanceof Logistic) {
 	    return binarizeAndFilter(instances);
 	}
 	return instances;
     }
 
     /**
      * Build a weka Instance out of MaltFeatureNodes.
      * 
      * @param features
      *            Array of MaltFeatureNode.
      * @param instances
      * @param attinfo
      * @return
      */
     private Instance featuresToInstance(MaltFeatureNode[] features,
 	    FastVector attinfo) {
 	Instance out = new Instance(features.length + 1);
 	Instances instances = getInstances(features);
 	out.setDataset(instances);
 	for (int i = 0; i < features.length; i++) {
 	    MaltFeatureNode mfn = features[i];
 	    Attribute att = (Attribute) attinfo.elementAt(i);
 	    String val = "" + Math.round(mfn.getValue());
 	    FastVector vec = nominalMap.get(att.name());
 	    if (!vec.contains(val)) {
 		val = "OOV";
 	    }
 	    out.setValue(att, val);
 	}
 	out.setClassValue((String) nominalMap.get("CLASS").elementAt(0));
 	instances.add(out);
 	return out;
     }
 
     /**
      * Change the nominal attributes into binary ones, then filter out the
      * attributes that weren't used to train the classifier.
      * 
      * @param instances
      * @return
      * @throws Exception
      */
     private Instances binarizeAndFilter(Instances nominals) throws Exception {
 	// XXX(alexr): The thing to do here is parse the names of the
 	// attributes, isn't it? We're going to go through the attInfoPostHacks
 	// and compute each one separately, yeahhh!!
 
 	Instances out = new Instances("MaltFeatures", attInfoPostHacks, 0);
 	out.setClassIndex(out.numAttributes() - 1);
 	Instance nominal = nominals.firstInstance();
 	Instance binarized = new Instance(attInfoPostHacks.size());
 
 	for (int i = 0; i < attInfoPostHacks.size(); i++) {
 	    Attribute binAttribute = (Attribute) attInfoPostHacks.elementAt(i);
	    System.out.println(binAttribute.name());
	    String[] splitted = binAttribute.name().split("=");
	    if (splitted[0].equals("CLASS"))
 		continue;
 	    // names of attributes start at 1.
 	    int field = Integer.parseInt(splitted[0]) - 1;
 	    String val = splitted[1];
	    binarized.setValue(i, (nominal.stringValue(field) == val) ? 1 : 0);
 	}
 	binarized.setDataset(out);
 	out.add(binarized);
 	binarized.setClassValue((String) nominalMap.get("CLASS").elementAt(0));
 	return out;
     }
 }
