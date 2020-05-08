 package driver;
 
 import ga.GeneticParams;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import learning.LearningAgentParams;
 import learning.LearningParams;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import temperature.TemperatureParams;
 import cwcore.complexParams.ComplexAgentParams;
 import cwcore.complexParams.ComplexEnvironmentParams;
 import cwcore.complexParams.ComplexFoodParams;
 import disease.DiseaseParams;
 
 public class SimulationConfig {
 	private static void removeIgnorableWSNodes(Element parent) {
 		Node nextNode = parent.getFirstChild();
 		for (Node child = parent.getFirstChild();
 		nextNode != null;) {
 			child = nextNode;
 			nextNode = child.getNextSibling();
 			if (child.getNodeType() == Node.TEXT_NODE) {
 				// Checks if the text node is ignorable
 				if (child.getTextContent().matches("^\\s*$")) {
 					parent.removeChild(child);
 				}
 			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
 				removeIgnorableWSNodes((Element )child);
 			}
 		}
 	}
 
 	private String fileName = null;
 
 	/**
 	 * The genetic sequence. Initialize them to a certain sequence for the four agents.
 	 */
 
 	private ComplexEnvironmentParams envParams;
 
 	private GeneticParams geneticParams;
 
 	private ComplexAgentParams[] agentParams;
 
 	//xxxprivate LearningAgentParams[] learningAgentParams;
 
 	private LearningParams learningParams;
 
 	private ComplexFoodParams[] foodParams;
 
 	private DiseaseParams[] diseaseParams;
 
 	private TemperatureParams tempParams;
 
 	public SimulationConfig() {
 		envParams = new ComplexEnvironmentParams();
 
 		agentParams = new ComplexAgentParams[envParams.getAgentTypes()];
 		for (int i = 0; i < envParams.getAgentTypes(); i++) {
 			agentParams[i] = new ComplexAgentParams(envParams);
 			agentParams[i].type = i;
 		}
 
 		foodParams = new ComplexFoodParams[envParams.getFoodTypes()];
 		for (int i = 0; i < envParams.getFoodTypes(); i++) {
 			foodParams[i] = new ComplexFoodParams();
 			foodParams[i].type = i;
 		}
 
 		geneticParams = new GeneticParams(envParams);
 
 		diseaseParams = new DiseaseParams[envParams.getAgentTypes()];
 		for (int i = 0; i < envParams.getAgentTypes(); i++) {
 			diseaseParams[i] = new DiseaseParams(envParams);
 			diseaseParams[i].type = i;
 		}
 
 		tempParams = new TemperatureParams(envParams);
 
 		learningParams = new LearningParams(envParams);
 
 		//xxxlearningAgentParams = learningParams.getLearningParams();
 	}
 
 	public SimulationConfig(InputStream file) {
 		this();
 		this.fileName = ":STREAM:" + file.toString() + ":";
 		loadFile(file);
 	}
 	public SimulationConfig(String fileName) throws FileNotFoundException {
 		this();
 		this.fileName = fileName;
 		loadFile(new FileInputStream(fileName));
 	}
 
 	public ComplexAgentParams[] getAgentParams() {
 		return agentParams;
 	}
 
 	/*xxxpublic LearningAgentParams[] getLearningAgentParams() {
 		return learningAgentParams;
 	}*/
 
 	public DiseaseParams[] getDiseaseParams() {
 		return diseaseParams;
 	}
 
 	public ComplexEnvironmentParams getEnvParams() {
 		return envParams;
 	}
 
 	public String getFilename() {
 		return fileName;
 	}
 
 	public ComplexFoodParams[] getFoodParams() {
 		return foodParams;
 	}
 
 	public GeneticParams getGeneticParams() {
 		return geneticParams;
 	}
 
 
 	public TemperatureParams getTempParams() {
 		return tempParams;
 	}
 
 	public LearningParams getLearningParams() {
 		return learningParams;
 	}
 
 
 	private void loadFile(InputStream file) throws IllegalArgumentException {
 
 		// read these variables from the xml file
 
 		// DOM initialization
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setIgnoringElementContentWhitespace(true);
 		factory.setIgnoringComments(true);
 		// factory.setValidating(true);
 
 		Document document;
 		try {
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			document = builder.parse(file);
 		} catch (SAXException ex) {
 			throw new IllegalArgumentException("Can't open config file", ex);
 		} catch (ParserConfigurationException ex) {
 			throw new IllegalArgumentException("Can't open config file", ex);
 		} catch (IOException ex) {
 			throw new IllegalArgumentException("Can't open config file", ex);
 		}
 
 
 		Node root = document.getFirstChild();
 		removeIgnorableWSNodes((Element) root);
 
 		envParams = new ComplexEnvironmentParams();
 
 		envParams.loadConfig(root);
 
 		agentParams = new ComplexAgentParams[envParams.getAgentTypes()];
 
 		foodParams = new ComplexFoodParams[envParams.getFoodTypes()];
 
 		diseaseParams = new DiseaseParams[envParams.getAgentTypes()];
 
 		for (int i = 0; i < envParams.getAgentTypes(); i++)
 			diseaseParams[i] = new DiseaseParams(envParams);
 
 		tempParams = new TemperatureParams(envParams);
 
 		geneticParams = new GeneticParams(envParams);
 
 		NodeList nodes = root.getChildNodes();
 		int agent = 0;
 		int food = 0;
 		for (int j = 0; j < nodes.getLength(); j++) {
 			Node node = nodes.item(j);
 			String nodeName = node.getNodeName();
 
 			if (nodeName.equals("ga")) {
 				geneticParams.loadConfig(node);
 
 			} else if (nodeName.equals("agent")) {
 				ComplexAgentParams p = new ComplexAgentParams(envParams);
 				LearningAgentParams lp = new LearningAgentParams();
 				p.loadConfig(node);
 				if (p.type < 0)
 					p.type = agent++;
 				if (p.type >= envParams.getAgentTypes())
 					continue;
 				agentParams[p.type] = p;
 				//xxxlearningAgentParams[p.type] = lp;
 
 			} else if (nodeName.equals("food")) {
 				ComplexFoodParams p = new ComplexFoodParams();
 				p.loadConfig(node);
 				if (p.type < 0)
 					p.type = food++;
 
 				if (p.type >= envParams.getFoodTypes())
 					continue;
 
 				foodParams[p.type] = p;
 			} else if (nodeName.equals("disease")) {
 				parseDiseaseParams(node);
 			} else if (nodeName.equals("Temperature")) {
 				tempParams.loadConfig(node);
 			} else if (nodeName.equals("Learning")) {
 				learningParams.loadConfig(node);
 			}
 		}
 		for (int i = 0; i < agentParams.length; i++) {
 			if (agentParams[i] == null) {
 				agentParams[i] = new ComplexAgentParams(envParams);
 				agentParams[i].type = i;
 			}
 		}
 		for (int i = 0; i < foodParams.length; i++) {
 			if (foodParams[i] == null) {
 				foodParams[i] = new ComplexFoodParams();
 				foodParams[i].type = i;
 			}
 		}
 		/*xxx
 		for (int i = 0; i < learningAgentParams.length; i++) {
 			if (learningAgentParams[i] == null) {
 				learningAgentParams[i] = new LearningAgentParams();
 				learningAgentParams[i].type = i;
 			}
 		}*/
 
 	}
 
 	private void parseDiseaseParams(Node root) {
 		NodeList nodes = root.getChildNodes();
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node n = nodes.item(i);
 			if (i >= envParams.getAgentTypes())
 				break;
 			DiseaseParams dp = new DiseaseParams(envParams);
 			dp.loadConfig(n);
 			diseaseParams[i] = dp;
 		}
 		for (int i = 0; i < diseaseParams.length; i++) {
 			if (diseaseParams[i] == null)
 				diseaseParams[i] = new DiseaseParams(envParams);
 		}
 	}
 
 	/**
 	 * Writes the information stored in this tree to an XML file, conforming to the rules of our spec.
 	 *
 	 */
 	public void write(OutputStream stream) {
 		Document d;
 		try {
 			d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		} catch (ParserConfigurationException ex) {
 			throw new RuntimeException(ex);
 		}
 		Node root = d.createElement("inputData");
 
 		envParams.saveConfig(root, d);
 		for (int i = 0; i < envParams.getAgentTypes(); i++) {
 			Node node = d.createElement("agent");
 			agentParams[i].saveConfig(node, d);
 			root.appendChild(node);
 		}
 
 		for (int i = 0; i < envParams.getFoodTypes(); i++) {
 			Node node = d.createElement("food");
 			foodParams[i].saveConfig(node, d);
 			root.appendChild(node);
 		}
 
 		Node ga = d.createElement("ga");
 		geneticParams.saveConfig(ga, d);
 
 		root.appendChild(ga);
 
 		Node disease = d.createElement("disease");
 		for (DiseaseParams diseaseParam : diseaseParams) {
 			Node node = d.createElement("agent");
 			diseaseParam.saveConfig(node, d);
 			disease.appendChild(node);
 		}
 		root.appendChild(disease);
 
 		Node temp = d.createElement("Temperature");
 		tempParams.saveConfig(temp, d);
 		root.appendChild(temp);
 
		Node learn = d.createElement("Learning");
 		learningParams.saveConfig(learn, d);
 		root.appendChild(learn);
 
 		Node version = d.createComment("Generated by COBWEB2 version " + Versionator.getVersion() );
 		root.appendChild(version);
 
 		d.appendChild(root);
 
 		Source s = new DOMSource(d);
 
 		Transformer t;
 		TransformerFactory tf = TransformerFactory.newInstance();
 		try {
 			t = tf.newTransformer();
 
 		} catch (TransformerConfigurationException ex) {
 			throw new RuntimeException(ex);
 		}
 		t.setOutputProperty(OutputKeys.INDENT, "yes");
 		t.setParameter(OutputKeys.STANDALONE, "yes");
 		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 
 		Result r = new StreamResult(stream);
 		try {
 			t.transform(s, r);
 		} catch (TransformerException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public void SetAgentTypeCount(int count) {
 		this.envParams.agentTypeCount = count;
 		this.envParams.foodTypeCount = count;
 
 		{ 
 			ComplexAgentParams[] n = Arrays.copyOf(this.agentParams, count);
 			for (int i = 0; i < this.agentParams.length && i < count; i++) {
 				n[i].resizeFoodweb(envParams);
 			}
 			for (int i = this.agentParams.length; i < count; i++) {
 				n[i] = new ComplexAgentParams(envParams);
 			}
 			this.agentParams = n;
 		}
 
 		/*xxx
 		{
 			LearningAgentParams[] n = Arrays.copyOf(this.learningAgentParams, count);
 			for (int i = 0; i < this.learningAgentParams.length && i < count; i++) {
 				n[i] = new LearningAgentParams();
 			}
 			this.learningAgentParams = n;
 		}*/
 
 		{
 			ComplexFoodParams[] n = Arrays.copyOf(this.foodParams, count);
 			for (int i = this.foodParams.length; i < count; i++) {
 				n[i] = new ComplexFoodParams();
 			}
 			this.foodParams = n;
 		}
 
 		{
 			DiseaseParams[] n = Arrays.copyOf(diseaseParams, count);
 
 			for (int i = 0; i < this.diseaseParams.length && i < count; i++) {
 				n[i].resize(envParams);
 			}
 
 			for (int i = this.diseaseParams.length; i < count; i++) {
 				n[i] = new DiseaseParams(envParams);
 			}
 
 			this.diseaseParams = n;
 		}
 		{
 			this.geneticParams.resize(envParams);
 		}
 		{
 			this.tempParams.resize(envParams);
 		}
 		{
 			this.learningParams.resize(envParams);
 		}
 
 	}
 
 } // Parser
