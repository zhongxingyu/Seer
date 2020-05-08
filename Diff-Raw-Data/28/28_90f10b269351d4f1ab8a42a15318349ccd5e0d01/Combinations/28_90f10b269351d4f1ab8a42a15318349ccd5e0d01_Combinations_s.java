 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 public class Combinations {
     static public void main(String[] args) {
 	// testCombinationGeneration();
 	String configFile = args[0];
 	int replicationDegree = Integer.parseInt(args[1]);
 	int dataSize = Integer.parseInt(args[2]);
 	processWorkload(configFile, replicationDegree, dataSize);
     }
 
     private static void processWorkload(String file, int K, int O) {
 	List<String> attributes = new ArrayList<String>(parseWorkload(file));
 	List<Configuration> configs = genAllCombinations(attributes);
 	Configuration[] ranking = rankConfigurations(configs, K, O);
 	int rank = 1;
 	for (Configuration config : ranking) {
 	    System.out.println(rank + " \t- " + config + " | " + round1(config.getEstimatedThroughput()));
 	    rank++;
 	}
     }
 
     protected static void testCombinationGeneration() {
 	List<String> attributes = new ArrayList<String>();
 	List<Configuration> res = null;
 	attributes.add("A");
 	res = genCombinations(attributes);
 	System.out.println("1 (" + res.size() + ") " + res);
 	res = genAllCombinations(attributes);
 	System.out.println("1_all (" + res.size() + ") " + res);
 	attributes.add("B");
 	res = genCombinations(attributes);
 	System.out.println("2 (" + res.size() + ") " + res);
 	res = genAllCombinations(attributes);
 	System.out.println("2_all (" + res.size() + ") " + res);
 	attributes.add("C");
 	res = genCombinations(attributes);
 	System.out.println("3 (" + res.size() + ") " + res);
 	res = genAllCombinations(attributes);
 	System.out.println("3_all (" + res.size() + ") " + res);
 	attributes.add("D");
 	res = genCombinations(attributes);
 	System.out.println("4 (" + res.size() + ") " + res);
 	res = genAllCombinations(attributes);
 	System.out.println("4_all (" + res.size() + ") " + res);
 	attributes.add("E");
 	res = genCombinations(attributes);
 	System.out.println("5 (" + res.size() + ") " + res);
 	res = genAllCombinations(attributes);
 	System.out.println("5_all (" + res.size() + ") " + res);
 	attributes.add("F");
 	res = genCombinations(attributes);
 	System.out.println("6 (" + res.size() + ") " + res);
 	res = genAllCombinations(attributes);
 	System.out.println("6_all (" + res.size() + ") " + res);
     }
 
     /**
      * Generates all possible combinations of configurations for a given
      * attribute list.
      */
     public static List<Configuration> genAllCombinations(List<String> attributes) {
 	List<List<String>> powerset = powerset(attributes);
 	powerset.remove(new ArrayList<String>());
 	System.out.println(powerset);
 	ArrayList<Configuration> retVal = new ArrayList<Combinations.Configuration>();
 	for (List<String> i : powerset) {
 	    List<Configuration> comb = genCombinations(i);
 	    retVal.addAll(comb);
 	}
 	return retVal;
     }
 
     /**
      * powerset algorithm
      * 
      * @param list
      * @return
      */
     public static <T> List<List<T>> powerset(Collection<T> list) {
 	List<List<T>> ps = new ArrayList<List<T>>();
 	ps.add(new ArrayList<T>()); // add the empty set
 
 	// for every item in the original list
 	for (T item : list) {
 	    List<List<T>> newPs = new ArrayList<List<T>>();
 
 	    for (List<T> subset : ps) {
 		// copy all of the current powerset's subsets
 		newPs.add(subset);
 
 		// plus the subsets appended with the current item
 		List<T> newSubset = new ArrayList<T>(subset);
 		newSubset.add(item);
 		newPs.add(newSubset);
 	    }
 
 	    // powerset is now powerset of list.subList(0, list.indexOf(item)+1)
 	    ps = newPs;
 	}
 	return ps;
     }
 
     /**
      * Generates all possible combinations using all attributes in the argument
      * list.
      * 
      * @param attributes
      * @return
      */
     private static List<Configuration> genCombinations(List<String> attributes) {
 	ArrayList<String> attributesClone = new ArrayList<String>(attributes);
 	if (attributesClone.size() == 1) { // simple case
 	    // one subspace with one attribute
 	    Subspace s = new Subspace(attributesClone.get(0));
 	    // one configuration with one subspace
 	    Configuration c = new Configuration(s);
 
 	    List<Configuration> retVal = new ArrayList<Combinations.Configuration>();
 	    retVal.add(c);
 	    return retVal;
 	}
 	String last = attributesClone.remove(attributesClone.size() - 1);
 	return combineWith(last, genCombinations(new ArrayList<String>(attributesClone)));
     }
 
     /**
      * Adds an element to a list of configurations. Returns a new list of new
      * configurations
      * 
      * @param el
      * @param list
      *            list of configurations to add <code>el</code>. Will not be
      *            modified.
      * @return
      */
     private static List<Configuration> combineWith(String el, List<Configuration> list) {
 	List<Configuration> retVal = new ArrayList<Combinations.Configuration>();
 	// add as a new subspace of each configuration
 	for (Configuration c : list) {
 	    // one subspace with one attribute
 	    Subspace s = new Subspace(el);
 	    Configuration cClone = c.add(s);
 	    retVal.add(cClone);
 	}
 
 	// add as extra attribute inside each subspace
 	for (Configuration c : list) {
 	    for (Subspace s : c) {
 		Subspace sClone = s.add(el);
 		Configuration cClone = c.replace(s, sClone);
 		retVal.add(cClone);
 	    }
 	}
 	return retVal;
     }
 
     public static final List<QueryTemplate> workloadQueries = new ArrayList<QueryTemplate>();
 
     /**
      * Sorts the list of configurations according to the estimated throughput. This is calculated given the workload template queries.
      * @param configurations The possible configurations.
      * @return A sorted array of the configurations. 
      */
     private static Configuration[] rankConfigurations(List<Configuration> configurations, int K, int O) {
 	Configuration[] ranking = new Configuration[configurations.size()];
 	int i = 0;
 	for (Configuration config : configurations) {
 	    double estimatedThroughput = 0.0;
 	    for (QueryTemplate query : workloadQueries) {
		estimatedThroughput += query.calculateCost(config, K, O);
 	    }
 	    config.setEstimatedThroughput(estimatedThroughput);
 	    ranking[i] = config;
 	    i++;
 	}
 	Arrays.sort(ranking);
 	return ranking;
     }
 
     /**
      * Parses the workload template queries to produce the list of queries containing the attributes accessed and likelihood of the query.
      * @param configFile The file with the workload specification.
      * @return The unique set of attributes in the workload to be used as the seed for configuration generation.
      */
     private static Set<String> parseWorkload(String configFile) {
 	List<String> configLines = readFileContent(configFile);
 	if (configLines.size() <= 1) {
 	    exitError("Must have at least a line for percentages of operations and one query example");
 	}
 
 	String[] percentagesConfig = configLines.get(0).split(" ");
 	if (percentagesConfig.length != 3) {
 	    exitError("Must configure 3 percentages for: <PercInsert> <PercModify> <PercSearch>");
 	}
 
 	double percInsert = Double.parseDouble(percentagesConfig[0]);
 	double percModify = Double.parseDouble(percentagesConfig[1]);
 	double percSearch = Double.parseDouble(percentagesConfig[2]);
 	if ((percInsert + percModify + percSearch) != 100.0) {
 	    exitError("The sum of percentages of each type of operation must be 100.0");
 	}
 
 	Set<String> uniqueAttrs = new HashSet<String>();
 	configLines.remove(0);
 	for (String line : configLines) {
 	    String[] template = line.split(" ");
 	    if (template.length == 0) {
 		continue;
 	    }
 	    if (template.length < 3) {
 		exitError("Incorrect query template " + line);
 	    }
 
 	    String type = template[0];
 	    double percentage = Double.parseDouble(template[1]);
 	    List<String> attrs = new ArrayList<String>();
 	    for (int i = 2; i < template.length; i++) {
 		String attributeName = template[i];
 		uniqueAttrs.add(attributeName);
 		attrs.add(attributeName);
 	    }
 
 	    if (type.equals("M")) {
		workloadQueries.add(new ModifyTemplate(percentage * percModify, attrs));
 	    } else if (type.equals("S")) {
		workloadQueries.add(new SearchTemplate(percentage * percSearch, attrs));
 	    } else {
 		exitError("Type " + type + " is not yet supported");
 	    }
 	}
 
 	return uniqueAttrs;
     }
 
     private static List<String> readFileContent(String filename) {
 	List<String> lines = new ArrayList<String>();
 	try {
 	    FileInputStream is = new FileInputStream(filename);
 	    DataInputStream in = new DataInputStream(is);
 	    BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
 	    String strLine;
 	    while ((strLine = br.readLine()) != null) {
 		if (strLine.equals("")) {
 		    continue;
 		}
 		lines.add(strLine);
 	    }
 	    br.close();
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    System.exit(1);
 	}
 	return lines;
     }
 
     private static void exitError(String error) {
 	System.err.println(error);
 	System.exit(1);
     }
 
     static abstract class QueryTemplate implements Iterable<String> {
 
 	private final double percentage;
 	private final List<String> attributes;
 
 	public QueryTemplate(double percentage, List<String> attributes) {
 	    this.percentage = percentage;
 	    this.attributes = attributes;
 	}
 
 	public abstract double calculateCost(Configuration configuration, int K, int O);
 
 	@Override
 	public Iterator<String> iterator() {
 	    return this.attributes.iterator();
 	}
 
     }
 
     static class SearchTemplate extends QueryTemplate {
 
 	// TODO we should actually calculate Ri, no?
 	private static final int REGIONS_DEFAULT = 256;
 	private static final double BETA = 3.31 * Math.pow(10, -7);
 
 	public SearchTemplate(double percentage, List<String> attributes) {
 	    super(percentage, attributes);
 	}
 
 	@Override
 	public double calculateCost(Configuration configuration, int K, int O) {
 	    int CR = configuration.regionsContacted(this, REGIONS_DEFAULT);
 	    double estT = 1.0 / (CR * O / REGIONS_DEFAULT * BETA);
	    return super.percentage * estT;
 	}
 
     }
 
     static class ModifyTemplate extends QueryTemplate {
 
	private static final double ALPHA = 5.33;
 	private static final double MAX_T = 98000;
 
 	public ModifyTemplate(double percentage, List<String> attributes) {
 	    super(percentage, attributes);
 	}
 
 	@Override
 	public double calculateCost(Configuration configuration, int K, int O) {
	    int M = configuration.getM(this);
	    int N = configuration.getM(this);
 	    double estT = (MAX_T) / (1 + K * (N + ALPHA * M));
	    return super.percentage * estT;
 	}
 
     }
 
     static class Subspace implements Iterable<String> {
 	private final List<String> attributes;
 
 	public Subspace(String seed) {
 	    this.attributes = new ArrayList<String>();
 	    this.attributes.add(seed);
 	}
 
 	public Subspace(Collection<String> attributes) {
 	    this.attributes = new ArrayList<String>();
 	    for (String a : attributes) {
 		this.attributes.add(a);
 	    }
 	}
 
 	/**
 	 * adds attribute to subspace
 	 * 
 	 * @param s
 	 *            attribute to add
 	 * @return copy of subspace with added attribute
 	 */
 	public Subspace add(String a) {
 	    Subspace retVal = new Subspace(attributes);
 	    retVal.attributes.add(a);
 	    return retVal;
 	}
 
 	public String toString() {
 	    String retval = "SUB<";
 	    String delimiter = "";
 	    for (String a : attributes) {
 		retval += delimiter + a;
 		delimiter = ",";
 	    }
 	    retval += ">";
 	    return retval;
 	}
 
 	@Override
 	public Iterator<String> iterator() {
 	    return this.attributes.iterator();
 	}
 
 	public boolean contains(String a) {
 	    return this.attributes.contains(a);
 	}
 
 	public int size() {
 	    return this.attributes.size();
 	}
     }
 
     static class Configuration implements Iterable<Subspace>, Comparable<Configuration> {
 	private final List<Subspace> subspaces;
 	private double throughput = 0.0;	// computed later on
 
 	public Configuration(Subspace seed) {
 	    this.subspaces = new ArrayList<Combinations.Subspace>();
 	    this.subspaces.add(seed);
 	}
 
 	public Configuration(Collection<Subspace> subspaces) {
 	    this.subspaces = new ArrayList<Combinations.Subspace>();
 	    for (Subspace s : subspaces) {
 		this.subspaces.add(s);
 	    }
 	}
 
 	public double getEstimatedThroughput() {
 	    return this.throughput;
 	}
 
 	public void setEstimatedThroughput(double throughput) {
 	    this.throughput = throughput;
 	}
 
 	/**
 	 * adds subspace to configuration
 	 * 
 	 * @param s
 	 *            subspace to add
 	 * @return copy of configuration with added subspace
 	 */
 	public Configuration add(Subspace s) {
 	    Configuration retVal = new Configuration(subspaces);
 	    retVal.subspaces.add(s);
 	    return retVal;
 	}
 
 	/**
 	 * returns a copy of this configuration, with a subspace replaced by
 	 * another
 	 * 
 	 * @param oldS
 	 *            subspace to remove
 	 * @param newS
 	 *            subspace to add
 	 * @return copy of configuration with subspaces replaced
 	 */
 	public Configuration replace(Subspace oldS, Subspace newS) {
 	    Configuration retVal = new Configuration(subspaces);
 	    retVal.subspaces.remove(oldS);
 	    retVal.subspaces.add(newS);
 	    return retVal;
 	}
 
 	public String toString() {
 	    String retval = "CONFIG{";
 	    String delimiter = "";
 	    for (Subspace s : subspaces) {
 		retval += delimiter + s;
 		delimiter = ",";
 	    }
 	    retval += "}";
 	    return retval;
 	}
 
 	// FIXME: test!
 	public int getM(ModifyTemplate m) {
 	    int retVal = 0;
 	    for (Subspace i : subspaces) {
 		for (String j : m) {
 		    if (i.contains(j)) {
 			retVal++;
 			break; // must break, or I may count this subspace twice
 		    }
 		}
 	    }
 	    return retVal;
 	}
 
 	// FIXME: test!
 	public int getN(ModifyTemplate m) {
	    return subspaces.size() - getM(m);
 	}
 
 	// FIXME: test!
 	public int regionsContacted(SearchTemplate s, int r) {
 	    int min = Integer.MAX_VALUE;
 	    for (Subspace sub : subspaces) {
 		int res = calcRegions(s, sub, r);
 		if (res < min) {
 		    min = res;
 		}
 	    }
 	    return min;
 	}
 
 	// FIXME: test!
 	private int calcRegions(SearchTemplate s, Subspace sub, int r) {
 	    double S = sub.size();
 	    double E = S;
 	    for (String it : s) {
 		if (sub.contains(it)) {
 		    E--;
 		}
 	    }
 	    // FIXME: is round a good idea? is floor better?
 	    return (int) Math.round(Math.pow(Math.pow(r, 1 / S), E));
 	}
 
 	@Override
 	public Iterator<Subspace> iterator() {
 	    return this.subspaces.iterator();
 	}
 
 	@Override
 	public int compareTo(Configuration other) {
 	    if (this.throughput > other.throughput) {
 		return -1;
 	    } else if (this.throughput == other.throughput) {
 		return 0;
 	    } else {
 		return 1;
 	    }
 	}
     }
 
     public static String round1(double value) {
 	DecimalFormat dtime = new DecimalFormat("#.#");
 	return dtime.format(value);
     }
 }
