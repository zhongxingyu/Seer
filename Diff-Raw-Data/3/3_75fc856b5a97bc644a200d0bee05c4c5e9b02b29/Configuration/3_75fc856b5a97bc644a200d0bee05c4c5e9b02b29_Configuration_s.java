 package de.uni_leipzig.simba.saim.core;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.FileOutputStream;
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 import de.konrad.commons.sparql.PrefixHelper;
 import de.uni_leipzig.simba.genetics.util.PropertyMapping;
 import de.uni_leipzig.simba.io.ConfigReader;
 import de.uni_leipzig.simba.io.KBInfo;
 import de.uni_leipzig.simba.saim.core.metric.MetricParser;
 import de.uni_leipzig.simba.saim.core.metric.Output;
 /**Class holds all configuration settings for a linking process. */
 public class Configuration
 {
	private Logger logger = Logger.getLogger("SAIM");
 	protected Output metric = null;
 
 
 //	private static Configuration instance = new Configuration();
 	private PropertyChangeSupport changes = new PropertyChangeSupport( this );
 	public static final String SETTING_CONFIG = "setting from xml";
 	private String id = null;
 	private String name;
 //	private double acceptanceThreshold=0.5d;
 //	private double reviewThreshold=0.4d;
 	int granularity=2;
 	public String sameAsRelation = "owl:sameAs";
 	private ConfigReader cR = new ConfigReader();
 
 	public KBInfo source = null;
 	public KBInfo target = null;
 	public boolean isLocal  = false;
 
 	public volatile PropertyMapping propertyMapping = new PropertyMapping();
 
 	public String getMetricExpression() {
 		if(metric != null)
 			return metric.toString();
 		return null;
 	}
 
 	public void setMetricExpression(String metricExpression) {
 		logger.info("Setting metric expression to "+metricExpression+" using the source.var "+source.var);
 		if(metric != null) {
 			double param1 = 2.0d;
 			double param2 = 2.0d;
 			if(metric.param1 != null)
 				param1 = metric.param1;
 			if(metric.param2 != null)
 				param2 = metric.param2;
 			metric = MetricParser.parse(metricExpression, source.var.replaceAll("\\?", ""));
 			if(param1 <= 1)
 				metric.param1 = param1;
 			if(param2 <= 1)
 				metric.param2 = param2;
 		} else {
 			//donno
 			metric = MetricParser.parse(metricExpression, source.var.replaceAll("\\?", ""));
 		}
 
 		logger.info("Setted metric expression to "+this.metric.toString());
 
 	}
 	public double getAcceptanceThreshold() {
 		if(metric == null || metric.param1==null) {
 			logger.warn("Not able to get threshold");
 			return 0.3d;
 		}
 		return metric.param1;
 	}
 	public void setAcceptanceThreshold(double acceptanceThreshold) {
 		if(metric == null)
 			metric = new Output();
 		metric.param1 = acceptanceThreshold;
 	}
 	public double getVerificationThreshold() {
 		if( metric == null ||metric.param2 == null)
 			return getAcceptanceThreshold()-0.1d;
 		else
 			return metric.param2;
 	}
 	public void setVerificationThreshold(double verificationThreshold) {
 		if(metric == null)
 			metric = new Output();
 		metric.param2 = verificationThreshold;
 	}
 
 	public Configuration() {}
 	public void store() {}
 
 	/** Implements Singleton pattern.*/
 //	public static Configuration getInstance() {return instance;}
 
 	public void setSourceEndpoint(KBInfo source) {	this.source = source;
 	if(source.var == null)
 		source.var = "?src";
 	}
 	public void setTargetEndpoint(KBInfo target) {	this.target = target;
 	if(target.var == null);
 	target.var = "?dest";
 	}
 	public KBInfo getSource() {	return source;}
 	public KBInfo getTarget() {	return target;}
 
 	public void setFromConfigReader(ConfigReader cR)
 	{
 		this.cR = cR;
 		source = cR.sourceInfo;
 		target = cR.targetInfo;
 		this.propertyMapping = new PropertyMapping();
 		metric = MetricParser.parse(cR.metricExpression,cR.sourceInfo.var.replace("?",""));
 		setAcceptanceThreshold(cR.acceptanceThreshold);
 		metric.param2 = cR.verificationThreshold;
 
 		logger.info("Successfully parsed metric from config reader: "+metric);
 		granularity = cR.granularity;
 		if(source.type.equalsIgnoreCase("CSV") || target.type.equalsIgnoreCase("CSV"))
 			this.isLocal = true;
 		changes.firePropertyChange(SETTING_CONFIG, null, this);
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener l)
 	{
 		changes.addPropertyChangeListener(l);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener l)
 	{
 		changes.removePropertyChangeListener(l);
 	}
 
 	//	/**Set default namespaces in both source and target KBInfo  */
 	//	public void setDefaultNameSpaces() {
 	//		source.prefixes = getDefaultNameSpaces();
 	//		target.prefixes = getDefaultNameSpaces();
 	//	}
 	//
 	//	/**
 	//	 * Function returns HashMap of label and uri of often used namespaces.
 	//	 * @return HashMap<label,uri>
 	//	 */
 	//	public HashMap<String, String> getDefaultNameSpaces()
 	//	{
 	//		HashMap<String, String> defs = new HashMap<String, String>();
 	//		//		  defs.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
 	//		//		  defs.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
 	//		//		  defs.put("foaf", "http://xmlns.com/foaf/0.1/");
 	//		//		  defs.put("owl", "http://www.w3.org/2002/07/owl#");
 	//		//		  defs.put("diseasome", "http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/");
 	//		//		  defs.put("dbpedia", "http://dbpedia.org/ontology/");
 	//		//		  defs.put("dbpedia-p", "http://dbpedia.org/property/");
 	//		//		  defs.put("dc", "http://purl.org/dc/terms/");
 	//		//		  defs.put("sider", "http://www4.wiwiss.fu-berlin.de/sider/resource/sider/");
 	//		//		  defs.put("drugbank", "http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/");
 	//		//		  defs.put("dailymed", "http://www4.wiwiss.fu-berlin.de/dailymed/resource/dailymed/");
 	//		Map<String, String> map = PrefixHelper.getPrefixes();
 	//		for(Entry<String, String> e : map.entrySet())
 	//			defs.put(e.getKey(), e.getValue());
 	//		return defs;
 	//	}
 
 	private void fillKBElement(Element element, KBInfo kb)
 	{
 		if(kb == null)
 			return;
 		element.getChild("ID").setText(kb.id);
 		element.getChild("ENDPOINT").setText(kb.endpoint);
 		element.getChild("GRAPH").setText(kb.graph);
 		element.getChild("VAR").setText(String.valueOf(kb.var));
 		element.getChild("PAGESIZE").setText(String.valueOf(kb.pageSize));
 		for(String restriction: kb.restrictions)
 		{
 			Element restrictionElement = new Element("RESTRICTION");
 			element.addContent(restrictionElement);
 			restrictionElement.setText(restriction);
 		}
 		for(String property: kb.properties)
 		{
 			Element restrictionElement = new Element("PROPERTY");
 			element.addContent(restrictionElement);
 			restrictionElement.setText(property);
 		}
 	}
 
 	public void saveToXML(String filename)
 	{
 		try{
 			Document document = null;
 			document = new SAXBuilder().build(getClass().getClassLoader().getResourceAsStream("template.xml.template"));
 			Element rootElement = document.getRootElement();
 			{			// Prefixes
 				Map<String,String> prefixes = new HashMap<String,String>(source.prefixes);
 				prefixes.putAll(target.prefixes);
 				int i = 0;
 				for(String prefix: prefixes.keySet())
 				{
 					Element prefixElement;
 					rootElement.addContent(i++,prefixElement=new Element("PREFIX"));
 					prefixElement.addContent(new Element("NAMESPACE").setText(prefixes.get(prefix)));
 					prefixElement.addContent(new Element("LABEL").setText(prefix));
 				}
 			}
 			Element sourceElement = rootElement.getChild("SOURCE");
 			Element targetElement = rootElement.getChild("TARGET");
 			fillKBElement(sourceElement,source);
 			fillKBElement(targetElement,target);
 
 			if(metric != null) {
 				rootElement.getChild("METRIC").setText(metric.toString());
 				System.out.println(metric.toString());
 			}
 
 			{
 			Element acceptanceElement = rootElement.getChild("ACCEPTANCE");
 			acceptanceElement.getChild("FILE").setText(source.endpoint+'-'+target.endpoint+"-accept");
 			acceptanceElement.getChild("THRESHOLD").setText(Double.toString(getAcceptanceThreshold()));
 			}
 			{
 			Element reviewElement = rootElement.getChild("REVIEW");
 
 			reviewElement.getChild("FILE").setText(source.endpoint+'-'+target.endpoint+"-review");
 			reviewElement.getChild("THRESHOLD").setText(Double.toString(getVerificationThreshold()));
 			}
 			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
 			out.output(document,new FileOutputStream(filename,false));
 
 			//getElementById("/LIMES/SOURCE/VAR")
 		}
 		catch (Exception e){throw new RuntimeException(e);}
 	}
 
 	public String toString() {
 		String output= source.toString()+"\n<br>\n"+target.toString()+"\n<br>\n";
 		if(metric != null) output +=metric.toString();
 		return output;
 	}
 
 	/**
 	 * Method adds a property match, and the properties to the according KBInfos.
 	 * @param sourceProp source property without heading variable of the KBInfo! Eg. <i>rdf:label</i> not <i>x.rdf:label</i>!
 	 * @param targetProp target property without heading variable of the KBInfo! Eg. <i>rdf:label</i> not <i>y.rdf:label</i>!
 	 */
 	public void addPropertiesMatch(String sourceProp, String targetProp, boolean stringProps) {
 		String s_abr=PrefixHelper.abbreviate(sourceProp);
 		String t_abr=PrefixHelper.abbreviate(targetProp);
 		if(!source.properties.contains(s_abr)) {
 			source.properties.add(s_abr);
 			source.prefixes.put(PrefixHelper.getBase(s_abr), PrefixHelper.getURI(PrefixHelper.getBase(s_abr)));
 			source.functions.put(s_abr, new HashMap<String,String>());
 			source.functions.get(s_abr).put(s_abr, "nolang->lowercase");
 		}
 		//		System.out.println("Adding source property: "+s_abr+"::::"+PrefixHelper.getPrefixFromURI(s_abr)+" -- "+PrefixHelper.getURI(PrefixHelper.getPrefixFromURI(s_abr)));
 
 		if(!target.properties.contains(t_abr)) {
 			target.properties.add(t_abr);
 			target.prefixes.put(PrefixHelper.getBase(t_abr), PrefixHelper.getURI(PrefixHelper.getBase(t_abr)));
 			target.functions.put(t_abr, new HashMap<String,String>());
 			target.functions.get(t_abr).put(t_abr, "nolang->lowercase");
 		}
 		if(stringProps) {
 			logger.info("Adding String Property Match: "+s_abr+" - "+t_abr);
 			propertyMapping.addStringPropertyMatch(s_abr, t_abr);
 			if(!source.functions.containsKey(s_abr)) {
 				source.functions.put(s_abr, new HashMap<String,String>());
 				source.functions.get(s_abr).put(s_abr, "nolang->lowercase");
 			}
 
 			if(!target.functions.containsKey(t_abr)) {
 				target.functions.put(t_abr, new HashMap<String,String>());
 				target.functions.get(t_abr).put(t_abr, "nolang->lowercase");
 			}
 
 		} else {
 			logger.info("Adding Number Property Match: "+s_abr+" - "+t_abr);
 			propertyMapping.addNumberPropertyMatch(s_abr, t_abr);
 			source.functions.put(s_abr, new HashMap<String,String>());
 			source.functions.get(s_abr).put(s_abr, "number");
 			target.functions.put(t_abr, new HashMap<String,String>());
 			target.functions.get(t_abr).put(t_abr, "number");
 		}
 	}
 
 	public ConfigReader getLimesConfiReader() {
 		cR = new ConfigReader();
 		if(source.var == null)
 			source.var="?src";
 		if(target.var==null)
 			target.var="?dest";
 		cR.sourceInfo = getSource();
 		cR.targetInfo = getTarget();
 		if(getMetricExpression() == null) {
 			String defMetric = "trigram("+source.var+"."+source.properties.get(0)+","+target.var+"."+target.properties.get(0)+")";
 			defMetric = defMetric.replaceAll("\\?", "");
 			System.out.println("No metricExpression set ... using default: "+defMetric);
 			setMetricExpression(defMetric);
 		}
 		cR.prefixes.putAll(getSource().prefixes);
 		cR.prefixes.putAll(getTarget().prefixes);
 		String base = PrefixHelper.getBase(sameAsRelation);
 		if(base.endsWith(":")) {
 			base = base.substring(0,base.length()-1);
 		}
 		cR.prefixes.put(base, PrefixHelper.getURI(base));
 		if(cR.prefixes.containsKey(null)) {
 			logger.error("Something went wrong with prefixes...");
 		}
 		cR.verificationRelation = sameAsRelation;
 		cR.acceptanceRelation = sameAsRelation;
 		cR.metricExpression = getMetricExpression();
 		cR.acceptanceThreshold = getAcceptanceThreshold();
 		cR.verificationThreshold  = getVerificationThreshold();
 		cR.granularity = granularity;
 		//	cR.
 		return cR;
 	}
 
 	/**
 	 * Tests whether or not the given property is part of either source or target endpoint. Expects property to begin with endpoint variable, if
 	 * not we will just look in both endpoints.
 	 * @param prop
 	 * @return
 	 */
 	public boolean isPropertyDefined(String prop) {
 		if(prop.indexOf(".") == -1) {
 			return source.properties.contains(prop) || target.properties.contains(prop);
 		}else {
 			String var = prop.substring(0, prop.indexOf("."));
 			if(source.var.contains(var))
 				return source.properties.contains(prop.substring(prop.indexOf(".")+1));
 			if(target.var.contains(var))
 				return target.properties.contains(prop.substring(prop.indexOf(".")+1));
 			return false;
 		}
 	}
 
 	@Override
 	public int hashCode()
 	{
 		final int prime = 31;
 		int result = 1;
 		long temp;
 		temp = Double.doubleToLongBits(getAcceptanceThreshold());
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		result = prime * result + granularity;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + (isLocal ? 1231 : 1237);
 		result = prime * result + ((metric == null) ? 0 : metric.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		//		result = prime * result
 		//				+ ((propertyMapping == null) ? 0 : propertyMapping.hashCode());
 		result = prime * result
 				+ ((sameAsRelation == null) ? 0 : sameAsRelation.hashCode());
 		result = prime * result + ((source == null) ? 0 : source.hashCode());
 		result = prime * result + ((target == null) ? 0 : target.hashCode());
 		temp = Double.doubleToLongBits(getVerificationThreshold());
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj)
 	{
 		if (this == obj) return true;
 		if (obj == null) return false;
 		if (getClass() != obj.getClass()) return false;
 		Configuration other = (Configuration) obj;
 		if (Double.doubleToLongBits(getAcceptanceThreshold()) != Double
 				.doubleToLongBits(other.getAcceptanceThreshold())) return false;
 		if (granularity != other.granularity) return false;
 		if (id == null)
 		{
 			if (other.id != null) return false;
 		}
 		else if (!id.equals(other.id)) return false;
 		if (isLocal != other.isLocal) return false;
 		if (metric == null)
 		{
 			if (other.metric != null) return false;
 		}
 		else if (!metric.equals(other.metric)) return false;
 		if (getMetricExpression() == null)
 		{
 			if (other.getMetricExpression() != null) return false;
 		}
 		else if (!getMetricExpression().equals(other.getMetricExpression())) return false;
 		if (name == null)
 		{
 			if (other.name != null) return false;
 		}
 		else if (!name.equals(other.name)) return false;
 		//		if (propertyMapping == null)
 		//		{
 		//			if (other.propertyMapping != null) return false;
 		//		}
 		//		else
 		//		{
 		//			if(other.propertyMapping==null) return false;
 		//			if (!propertyMapping.getStringPropMapping().map.equals(other.propertyMapping.getStringPropMapping().map)) return false;
 		//			if (!propertyMapping.getNumberPropMapping().map.equals(other.propertyMapping.getNumberPropMapping().map)) return false;
 		//
 		//		}
 		if (sameAsRelation == null)
 		{
 			if (other.sameAsRelation != null) return false;
 		}
 		else if (!sameAsRelation.equals(other.sameAsRelation)) return false;
 		if (source == null)
 		{
 			if (other.source != null) return false;
 		}
 		else if (!source.equals(other.source)) return false;
 		if (target == null)
 		{
 			if (other.target != null) return false;
 		}
 		else if (!target.equals(other.target)) return false;
 		if (Double.doubleToLongBits(getVerificationThreshold()) != Double
 				.doubleToLongBits(other.getVerificationThreshold())) return false;
 		return true;
 	}
 }
