 package uk.ac.manchester.cs.ore.output;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.semanticweb.owlapi.apibinding.OWLManager;
 import org.semanticweb.owlapi.model.OWLAxiom;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 
 import uk.ac.manchester.cs.diff.axiom.LogicalDiff;
 import uk.ac.manchester.cs.diff.axiom.StructuralDiff;
 import uk.ac.manchester.cs.diff.axiom.changeset.ChangeSet;
 import uk.ac.manchester.cs.diff.axiom.changeset.LogicalChangeSet;
 import uk.ac.manchester.cs.diff.axiom.changeset.StructuralChangeSet;
 import uk.ac.manchester.cs.diff.output.XMLReport;
 
 /**
  * @author Rafael S. Goncalves <br/>
  * Information Management Group (IMG) <br/>
  * School of Computer Science <br/>
  * University of Manchester <br/>
  */
 public class ResultComparator {
 	private List<File> files;
 	private List<String> reasonerList;
 	private BufferedWriter log;
 	private Map<String,String> map;
 	private String ontName, opName, outputFolder, conceptList;
 	
 	/**
 	 * Constructor
 	 * @param files	Set of results files
 	 * @param conceptList	Concept list file if applicable, empty string otherwise
 	 * @param opName	Operation name
 	 * @param outputFolder	Output folder
 	 * @param ontName	Ontology filename
 	 */
 	public ResultComparator(List<File> files, String conceptList, String opName, String outputFolder, String ontName) {
 		this.files = files;
 		this.conceptList = conceptList;
 		this.opName = opName;
 		this.outputFolder = outputFolder;
 		this.ontName = ontName;
 		map = new HashMap<String,String>();
 		reasonerList = getReasonerList();
 		log = initWriter(outputFolder, "log.txt", true);
 	}
 	
 	
 	/**
 	 * Determines whether all given files or individual sat results are equivalent
 	 * @return true if all files are equivalent, false otherwise
 	 * @throws IOException
 	 */
 	public boolean areResultsEquivalent() throws IOException {
 		boolean allEquiv = true;
 		verifyFiles();
 		if(opName.equalsIgnoreCase("sat")) {
 			File f = new File(conceptList);
 			if(f.exists() && f.length()>0) {
 				BufferedReader br = new BufferedReader(new FileReader(conceptList));
 				String cName = br.readLine();
 				while(cName != null) {
 					cName = cName.trim();
 					boolean equiv = areResultsEquivalent(cName);
 					if(!equiv) allEquiv = false;
 					cName = br.readLine();
 				}
 				br.close();
 			}
 			else
 				System.err.println("! Inexistent or empty concept name list");
 		}
 		else  
 			allEquiv = areResultsEquivalent("");
 		log.close();
 		return allEquiv;
 	}
 	
 	
 	/**
 	 * Determines whether all given files or individual sat results are equivalent
 	 * @param o1	1st SAT result if applicable, otherwise ignored
 	 * @param o2	2nd SAT result if applicable, otherwise ignored
 	 * @param cName	Concept name of SAT test if applicable, otherwise ignored
 	 * @return true if all files are equivalent, false otherwise
 	 * @throws IOException 
 	 */
 	public boolean areResultsEquivalent(String cName) throws IOException {
 		boolean allEquiv = true;
 		String equivalent = "   Equivalent", sep = "----------------------------------------------------";
 		List<Set<File>> clusters = new ArrayList<Set<File>>();
 		Set<File> clustered = new HashSet<File>();
 		printIntro(cName, sep);
 		if(!files.isEmpty()) {
 			System.out.print("\nComparing results files");
 			if(!cName.equals("")) System.out.print(" for concept " + cName);
 			System.out.println("...\n");
 			LinkedList<File> list = new LinkedList<File>(files);
 			while(!list.isEmpty()) {
 				File f1 = list.pop(); clustered.add(f1);
 				Object o1 = loadFile(f1, cName);
 				if(o1 != null) {
 					Set<File> f1Cluster = new HashSet<File>(Collections.singleton(f1));
 					for(File f2 : files) {
 						if(f1 != f2 && !clustered.contains(f2)) {
 							Object o2 = loadFile(f2, cName);
 							if(o2 != null) {
 								printComparisonStatement(sep, f1, f2);
 								if(equals(o1, o2, f1, f2, cName)) {
 									System.out.println(equivalent); log.write(equivalent);
 									f1Cluster.add(f2);
 									list.remove(f2);
 									clustered.add(f2);
 								}
 								else
 									allEquiv = false;
 								System.out.println(sep); log.write("\n" + sep);
 								cleanUp(o2);
 							}
 							else
 								map.put(getReasonerName(f2),"nofile");
 						}
 					}
 					clusters.add(f1Cluster);
 					cleanUp(o1);
 				}
 				else
 					map.put(getReasonerName(f1),"nofile");
 			}
 		}
 		produceOutput(clusters, cName);
 		System.out.println("\nDone");
 		log.write("\nDone\n");
 		return allEquiv;
 	}
 	
 		
 	/**
 	 * Checks whether the given set of files exists (in the file system),
 	 * if not remove them from the results file set 
 	 */
 	public void verifyFiles() {
 		Set<File> toRemove = new HashSet<File>();
 		for(File f : files) {
 			if(!f.exists()) {
 				map.put(getReasonerName(f),"nofile");
 				toRemove.add(f);
 			}
 		}
 		files.removeAll(toRemove);
 	}
 	
 	
 	/**
 	 * Loads a given file (ontology or csv) and returns the object
 	 * @param f	File to load
 	 * @return Object representing the given file
 	 * @throws IOException
 	 */
 	public Object loadFile(File f, String cName) throws IOException {
 		Object result = null;
 		if(opName.equals("classification")) {
 			result = loadOntology(f);
 			if(result == null)
 				map.put(getReasonerName(f), "unparseable");
 			else if(!(((OWLOntology)result).getLogicalAxiomCount()>0))
 				map.put(getReasonerName(f), "empty");
 		}
 		else if(!(f.length() > 0))
 			map.put(getReasonerName(f), "empty");
 		else if(opName.equalsIgnoreCase("sat")) {
 			result = loadSatResult(f, cName);
 		}
 		else 
 			result = ""; // or return a dummy result for non-nullness' sake
 		return result;
 	}
 	
 	
 	/**
 	 * Checks whether the given pair of files is equivalent
 	 * @param o1	Object 1
 	 * @param o2	Object 2
 	 * @param f1	File 1
 	 * @param f2	File 2
 	 * @return true if files are equivalent, false otherwise
 	 * @throws IOException
 	 */
 	public boolean equals(Object o1, Object o2, File f1, File f2, String cName) throws IOException {
 		boolean equals = true;
 		if(opName.equalsIgnoreCase("classification"))
 			equals = equivalentEntailmentSets(o1, o2, f1, f2);
 		else if(opName.equalsIgnoreCase("sat")) 
 			equals = equalResult((String)o1, (String)o2, f1, f2, cName);
 		else if(opName.equalsIgnoreCase("consistency"))
 			equals = equalConsistencyResults(f1, f2);
 		return equals;
 	}
 	
 	
 	/**
 	 * Checks whether two given entailment sets are equivalent
 	 * @param o1	Ontology 1
 	 * @param o2	Ontology 2
 	 * @param f1	File 1
 	 * @param f2	File 2
 	 * @return true if entailment sets are equivalent, false otherwise
 	 * @throws IOException
 	 */
 	public boolean equivalentEntailmentSets(Object o1, Object o2, File f1, File f2) throws IOException {
 		boolean equiv = true;
 		ChangeSet cs = getDiff((OWLOntology)o1, (OWLOntology)o2, f1, f2);
 		if(!cs.isEmpty()) {
 			Set<OWLAxiom> adds = getAdditions(cs), rems = getRemovals(cs);
 			if(rems.isEmpty() && adds.isEmpty())
 				equiv = true;
 			else {
 				equiv = false;
 				logChanges(f1, f2, adds, rems);
 			}
 		}
 		else equiv = true;
 		return equiv;
 	}
 	
 	
 	/**
 	 * Get the result in the given file for the specifed concept name  
 	 * @param f	File
 	 * @param cName	Concept name
 	 * @return true if both files report the same results for all sat tests
 	 */
 	private String loadSatResult(File f, String cName) {
 		String out = "";
 		try {
 			BufferedReader br1 = new BufferedReader(new FileReader(f));
 			String line1 = br1.readLine();
 			while(line1 != null) {
 				StringTokenizer tokenizer1 = new StringTokenizer(line1, ",");
 				String cName1 = tokenizer1.nextToken();
 				String result1 = tokenizer1.nextToken();
 				if(cName1.equals(cName)) {
 					out = result1;
 					break;
 				}
 				line1 = br1.readLine();
 			}
 			br1.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if(out.equals("")) {
 			System.err.println("! Reasoner " + getReasonerName(f) + " has no result for concept name " + cName);
 			return null;
 		}
 		return out;
 	}
 	
 	
 	/**
 	 * Compare consistency results files
 	 * @param f1	File 1
 	 * @param f2	File 2
 	 * @return true if result is the same, false otherwise
 	 */
 	private boolean equalConsistencyResults(File f1, File f2) {
 		boolean equal = true;
 		try {
 			BufferedReader br1 = new BufferedReader(new FileReader(f1)), br2 = new BufferedReader(new FileReader(f2));
 			String line1 = br1.readLine(), line2 = br2.readLine();
 			while(line1 != null && line2 != null) {
 				equal = equalResult(line1, line2, f1, f2, "");
 				line1 = br1.readLine();
 				line2 = br2.readLine();
 			}
 			br1.close();
 			br2.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return equal;
 	}
 	
 	
 	/**
 	 * Checks whether two strings representing reasoner results are equal
 	 * @param s1	1st string
 	 * @param s2	2nd string
 	 * @param f1	File 1
 	 * @param f2	File 2
 	 * @return true if string are the same, false otherwise
 	 * @throws IOException
 	 */
 	private boolean equalResult(String s1, String s2, File f1, File f2, String conceptName) throws IOException {
 		boolean equals = true;
		s1 = s1.trim(); s2 = s2.trim();
 		if(!s1.equalsIgnoreCase(s2)) {
 			equals = false;
 			if(!conceptName.equals(""))
 				System.out.println("   Concept: " + conceptName);
 			String outSt = "     " + getReasonerName(f1) + " reports " + s1 + " while " + getReasonerName(f2) + " reports " + s2;
 			System.out.println(outSt); log.write(outSt);
 		}
 		return equals;
 	}
 	
 	
 	/**
 	 * Log differences between the two files
 	 * @param f1	File 1
 	 * @param f2	File 2
 	 * @param rems	Set of removals
 	 * @param adds	Set of additions
 	 * @throws IOException
 	 */
 	private void logChanges(File f1, File f2, Set<OWLAxiom> rems, Set<OWLAxiom> adds) throws IOException {
 		if(!rems.isEmpty()) {
 			String s = "  " + getReasonerName(f1) + " outputs " + rems.size() + " extra entailment(s)";
 			System.out.println(s);
 			log.write("\n" + s + "\n");
 			for(OWLAxiom ax : rems)
 				log.write("\n\t" + ax);
 		}
 		if(!adds.isEmpty()) {
 			String s = "  " + getReasonerName(f2) + " outputs " + adds.size() + " extra entailment(s)";
 			System.out.println("\n" + s);
 			log.write("\n" + s + "\n");
 			for(OWLAxiom ax : adds)
 				log.write("\n\t" + ax);
 		}
 	}
 	
 	
 	/**
 	 * Get the set of added axioms from the given change set
 	 * @param cs	Change set
 	 * @return Set of added axioms
 	 */
 	public Set<OWLAxiom> getAdditions(ChangeSet cs) {
 		Set<OWLAxiom> out = null;
 		if(cs instanceof LogicalChangeSet)
 			out = ((LogicalChangeSet)cs).getEffectualAdditionAxioms();
 		else if(cs instanceof StructuralChangeSet)
 			out = ((StructuralChangeSet)cs).getAddedAxioms();
 		return out;
 	}
 	
 	
 	/**
 	 * Get the set of removed axioms from the given change set
 	 * @param cs	Change set
 	 * @return Set of removed axioms
 	 */
 	public Set<OWLAxiom> getRemovals(ChangeSet cs) {
 		Set<OWLAxiom> out = null;
 		if(cs instanceof LogicalChangeSet)
 			out = ((LogicalChangeSet)cs).getEffectualRemovalAxioms();
 		else if(cs instanceof StructuralChangeSet)
 			out = ((StructuralChangeSet)cs).getRemovedAxioms();
 		return out;
 	}
 	
 	
 	/**
 	 * Produce output results file and cluster info file
 	 * @param clusters	List of file clusters
 	 * @throws IOException
 	 */
 	private void produceOutput(List<Set<File>> clusters, String cName) throws IOException {
 		if(!clusters.isEmpty())
 			analyseClusters(clusters);
 		else
 			System.out.println("No reasoner produced a (valid) result file");
 		
 		serializeClusterInfo(clusters, cName);
 		serialize(generateCSV(cName), outputFolder, "results.csv", true);
 	}
 	
 	
 	/**
 	 * Checks all clusters to determine which contains the highest number of files (i.e., most agreed-upon result)
 	 * @param clusters	List of file clusters
 	 * @throws IOException
 	 */
 	public void analyseClusters(List<Set<File>> clusters) throws IOException {
 		int max = 0, index = 0;
 		System.out.println("\nResults clusters:");
 		log.write("\n\nResults clusters:");
 		for(int i = 0; i<clusters.size(); i++) {
 			Set<File> fileset = clusters.get(i);
 			if(fileset.size() > max) {
 				max = fileset.size();
 				index = i;
 			}
 			String cluster = "Cluster " + (i+1) + " (size " + fileset.size() + "): ";
 			for(File f : fileset) {
 				String r = getReasonerName(f) + " ";
 				cluster += r;
 			}
 			System.out.print("  " + cluster); log.write("\n  " + cluster);
 			System.out.println();
 		}
 		log.write("\n");
 		Set<File> correct = clusters.get(index);
 		Set<File> incorrect = new HashSet<File>();
 		for(int i = 0; i<clusters.size(); i++) {
 			if(i!=index) {
 				for(File f : clusters.get(i))
 					incorrect.add(f);
 			}
 		}
 		updateMap(correct, "true");
 		updateMap(incorrect, "false");
 		if(!incorrect.isEmpty()) {
 			System.out.println("\nSummary:");
 			log.write("\nSummary:");
 			printSummary("Equivalent (majority)", correct);
 			printSummary("Non Equivalent", incorrect);
 		}
 	}
 	
 	
 	/**
 	 * Update results map
 	 * @param files	Set of files
 	 * @param value	Map value for all files 
 	 */
 	private void updateMap(Set<File> files, String value) {
 		for(File f : files)
 			map.put(getReasonerName(f), value);
 	}
 	
 	
 	/**
 	 * Generate a comma-separated row with the results in the map
 	 * @return Comma-separated string with results 
 	 */
 	private String generateCSV(String cName) {
 		String out = ontName + "," + opName + ",";
 		if(!cName.equals(""))
 			out += cName + ",";
 		for(String r : reasonerList) {
 			out += map.get(r) + ",";
 		}
 		return out;
 	}
 	
 	
 	/**
 	 * Get the set of reasoner names for all files
 	 * @param files	Set of files
 	 * @return Set containing the reasoner names corresponding to each file
 	 */
 	public Set<String> getReasonerNames(Set<File> files) {
 		Set<String> out = new HashSet<String>();
 		for(File f : files)
 			out.add(getReasonerName(f));
 		return out;
 	}
 	
 	
 	/**
 	 * Get the reasoner name assuming standard folder structure
 	 * @param f	File
 	 * @return Reasoner name
 	 */
 	public String getReasonerName(File f) {
 		return f.getParentFile().getParentFile().getParentFile().getName();
 	}
 	
 	
 	/**
 	 * Verify whether two ontology files are equivalent, first w.r.t. structural diff and,
 	 * subsequently, because of the equivalent vs dual subsumptions issue, logical diff
 	 * @param ont1	Ontology 1
 	 * @param ont2	Ontology 2
 	 * @return Change set between the two given ontologies
 	 */
 	public ChangeSet getDiff(OWLOntology ont1, OWLOntology ont2, File f1, File f2) {
 		ChangeSet changeSet = null;
 		XMLReport report = null;
 		
 		StructuralDiff sdiff = new StructuralDiff(ont1, ont2, false);
 		changeSet = sdiff.getDiff();
 		report = sdiff.getXMLReport();
 		
 		boolean structEquiv = sdiff.isEquivalent();
 		if(!structEquiv) {
 			LogicalDiff ldiff = new LogicalDiff(ont1, ont2, false);
 			changeSet = ldiff.getDiff();
 			report = ldiff.getXMLReport();
 		}
 		if(!changeSet.isEmpty())
 			serializeDiff(report, f1, f2);
 		return changeSet;
 	}	
 	
 	
 	/**
 	 * Serialize diff xml report
 	 * @param report	XMLReport object
 	 * @param f1	File 1
 	 * @param f2	File 2
 	 */
 	public void serializeDiff(XMLReport report, File f1, File f2) {
 		try {
 			String rep = report.getReportAsString(report.getXMLDocumentReport());
 			if(!outputFolder.endsWith(File.separator)) outputFolder+=File.separator;
 			String filename = getReasonerName(f1) + "_vs_" + getReasonerName(f2) + ".xml";
 			String folder = outputFolder + "diff_reports" + File.separator + ontName;
 			serialize(rep, folder, filename, false);
 		} catch (Exception e) {
 			System.err.println("! Unable to serialize diff report\n");
 		}
 	}
 	
 	
 	/**
 	 * Load ontology file
 	 * @param f	File
 	 * @return OWLOntology
 	 * @throws IOException 
 	 */
 	private OWLOntology loadOntology(File f) throws IOException {
 		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
 		OWLOntology ont = null;
 		if(getReasonerName(f).equals("elephant")) f = new File(fixFile(f));
 		try {
 			ont = man.loadOntologyFromOntologyDocument(f);
 		} catch (Exception e) {
 			System.err.println("! Unable to parse result file of: " + getReasonerName(f) + " (" + f.getAbsolutePath() + ")\n");
 		}
 		return ont;
 	}
 	
 	
 	
 	/**
 	 * Fix an ontology file that is missing (in functional syntax) the Ontology(..) statement
 	 * @param f	File to fix
 	 * @throws IOException
 	 */
 	private String fixFile(File f) throws IOException {
 		// Prepare fixed file's output
 		String folder = f.getParentFile().getAbsolutePath();
 		if(!folder.endsWith(File.separator)) folder += File.separator;
 		String filename = f.getName() + "_fixed.owl";
 		BufferedWriter writer = initWriter(folder, filename, false);
 		writer.write("Ontology(\n");
 		
 		// Read in original file
 		BufferedReader br = new BufferedReader(new FileReader(f));
 		String line;
 		while((line = br.readLine()) != null)
 			writer.write(line + "\n");
 		br.close();
 		
 		// Finalize fixed file
 		writer.write(")");
 		writer.close();
 		return folder+filename;
 	}
 	
 	
 	/**
 	 * List the given set of files
 	 * @param desc	Description, i.e., equivalent or non-equivalent
 	 * @param files	Set of files
 	 * @throws IOException 
 	 */
 	private void printSummary(String desc, Set<File> files) throws IOException {
 		System.out.println(desc + ":"); log.write("\n  " + desc + ":" + "\n");
 		System.out.print("\t"); log.write("\t");
 		for(File f : files) {
 			System.out.print(getReasonerName(f) + " ");
 			log.write(getReasonerName(f) + " ");
 		}
 		System.out.println();
 	}
 	
 	
 	/**
 	 * Get file comparison statement
 	 * @param f1	File 1
 	 * @param f2	File 2
 	 * @return String with the comparison statement
 	 */
 	private String getComparisonStatement(File f1, File f2) {
 		return getReasonerName(f1) + " vs " + getReasonerName(f2);
 	}
 	
 	
 	/**
 	 * Print file comparison statement
 	 * @param sep	Separator string
 	 * @param f1	File 1
 	 * @param f2	File 2
 	 * @throws IOException
 	 */
 	private void printComparisonStatement(String sep, File f1, File f2) throws IOException {
 		String st = getComparisonStatement(f1, f2);
 		System.out.println(st); log.write("\n" + st + "\n");
 	}
 	
 	
 	/**
 	 * Generate the full list of reasoner names for ORE-2013
 	 * @return List of reasoner names
 	 */
 	public List<String> getReasonerList() {
 		String reasoners[] = {"basevisor","chainsaw","elephant","elk-loading-counted","elk-loading-not-counted","fact","hermit",
 				"jcel","jfact","konclude","more-hermit","more-pellet","snorocket","treasoner","trowl","wsclassifier"};
 		return new ArrayList<String>(Arrays.asList(reasoners));
 	}
 	
 	
 	/**
 	 * Append a given string to the specified output file
 	 * @param out	String to be flushed
 	 */
 	public void serialize(String out, String folder, String filename, boolean append) {
 		BufferedWriter br = initWriter(folder, filename, append);
 		try {
 			br.write(out + "\n");
 			br.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Serialize a comma-separated file with the cluster information
 	 * @param clusters	List of file clusters
 	 */
 	public void serializeClusterInfo(List<Set<File>> clusters, String cName) {
 		String out = ontName + "," + opName;
 		if(!cName.equals(""))
 			out += "," + cName;
 		for(Set<File> set : clusters) {
 			out += "," + set.size() + ",";
 			for(String r : getReasonerNames(set))
 				out += r + " ";
 		}
 		serialize(out, outputFolder, "clusters.csv", true);
 	}
 	
 	
 	/**
 	 * Initialize a buffered writer
 	 * @param filename	Desired filename
 	 * @return Buffered file writer
 	 */
 	private BufferedWriter initWriter(String folder, String filename, boolean append) {
 		BufferedWriter out = null;
 		if(!folder.endsWith(File.separator)) folder += File.separator;
 		try {
 			File file = new File(folder + filename);
 			file.getParentFile().mkdirs();
 			out = new BufferedWriter(new FileWriter(file, append));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return out;
 	}
 	
 	
 	/**
 	 * Clean up given object
 	 * @param o	Object
 	 */
 	public void cleanUp(Object o) {
 		if(o instanceof OWLOntology) ((OWLOntology)o).getOWLOntologyManager().removeOntology((OWLOntology)o);
 		o=null;
 	}
 	
 	
 	/**
 	 * Print introductory messages
 	 * @param cName	Concept name
 	 * @param sep	Separator
 	 * @throws IOException
 	 */
 	private void printIntro(String cName, String sep) throws IOException {
 		log.write("\n" + sep + "\nOntology: " + ontName);
 		System.out.println("\n" + sep + "\nOntology: " + ontName);
 		if(!cName.equals("")) {
 			String out = "Concept: " + cName;
 			log.write("\n" + out + "\n" + sep); System.out.println(out+"\n"+sep);
 		}
 	}
 	
 	
 	/**
 	 * Main
 	 * @param 0	Operation name
 	 * @param 1	Output folder
 	 * @param 2 Concept list for SAT tests
 	 * @param 3..n	Results files
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		String opName = args[0];
 		String outputFile = args[1];
 		String satFile = args[2];
 		
 		List<File> files = new ArrayList<File>();
 		for(int i = 3; i < args.length; i++)
 			files.add(new File(args[i]));
 
 		File f1 = files.get(0);
 		String ontName = f1.getParentFile().getName();
 		
 		String ops[] = {"sat","classification","consistency"};
 		List<String> opList = new ArrayList<String>(Arrays.asList(ops));
 		if(opList.contains(opName)) {
 			ResultComparator comp = new ResultComparator(files, satFile, opName, outputFile, ontName);
 			boolean allEquiv = comp.areResultsEquivalent();
 			if(allEquiv)
 				System.out.println("\nAll results files are equivalent");
 			else
 				System.out.println("\nNot all results files are equivalent");
 		}
 		else
 			System.err.println("! Unrecognized operation name: " + opName);
 	}
 }
