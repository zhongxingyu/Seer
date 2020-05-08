 package org.geworkbench.bison.datastructure.bioobjects.markers.goterms;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import org.apache.commons.collections15.map.ListOrderedMap;
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.cookie.CookiePolicy;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.builtin.projects.OboSourcePreference;
 
 /**
  * Represents the Gene Ontology Tree and provides methods to access it.
  * 
  * @author John Watkinson
  * @author Xiaoqing Zhang
  * @version $Id$
  */
 public class GeneOntologyTree {
 	
 	private static Log log = LogFactory.getLog(GeneOntologyTree.class);
 	
 	private static GeneOntologyTree instance = null;
 	
 	private String dataVersion;
 	private String date;
 
 	// at class loading, start creating an instance but do not hold
 	static {
 		OboSourcePreference pref = OboSourcePreference.getInstance();
 		if(pref.getSourceType()== OboSourcePreference.Source.REMOTE) {
 			new Thread() {
 				@Override
 				public void run() {
 					instance = new GeneOntologyTree();
 				}
 			}.start();
 			System.out.println(new java.util.Date());
 		} else {
 			instance = new GeneOntologyTree(OboSourcePreference.getInstance().getSourceLocation()); //OboSourcePreference.DEFAULT_OBO_FILE);
 		}
 	}
 	
 	// this method may block for up to 200 seconds, but always return non null unless timed out
 	public static GeneOntologyTree getInstanceUntilAvailable() {
 		final long ONE_SECOND = 1000;
 		final long LIMIT = 200;
 		long count = 0;
 		while(instance==null && count<LIMIT) {
 			try {
 				Thread.sleep(ONE_SECOND);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			count++;
 		}
 		return instance;
 	}
 
 	// this method return immediately but may return null
 	public static GeneOntologyTree getInstance() {
 		return instance;
 	}
 
 	// Interim object for building up go term data
 	private static class Term {
 		int id;
 		List<Integer> parents;
 		String name;
 		String def;
 		boolean isRoot = false;
 
 		public Term() {
 		}
 
 		public boolean isRoot() {
 			return isRoot;
 		}
 
 		public void setRoot(boolean root) {
 			isRoot = root;
 		}
 
 		public int getId() {
 			return id;
 		}
 
 		public void setId(int id) {
 			this.id = id;
 		}
 
 		public List<Integer> getParents() {
 			return parents;
 		}
 
 		public void setParents(List<Integer> parentList) {
 			if (parentList == null) {
 				parents = new ArrayList<Integer>();
 			} else {
 				parents = parentList;
 			}
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public String getDef() {
 			return def;
 		}
 
 		public void setDef(String def) {
 			this.def = def;
 		}
 
 		public boolean equals(Object o) {
 			if (this == o)
 				return true;
 			if (o == null || getClass() != o.getClass())
 				return false;
 
 			final Term term = (Term) o;
 
 			if (id != term.id)
 				return false;
 
 			return true;
 		}
 
 		public int hashCode() {
 			return id;
 		}
 	}
 
 	private static int parseID(String id) {
 		int colon = id.indexOf(':');
 		int i = -1;
 		if (colon != -1) {
 			try {
 				i = Integer.parseInt(id.substring(colon + 1).trim());
 			} catch (NumberFormatException nfe) {
 				// Ignore -- result will be -1
 			}
 		}
 		return i;
 	}
 
 	private static final String FILE_HEADER1_0 = "format-version: 1.0";
 	private static final String FILE_HEADER1_2 = "format-version: 1.2";
 
 	private static final String TERM_START = "[Term]";
 
 	private static final String KEY_ID = "id";
 	private static final String KEY_NAME = "name";
 	private static final String KEY_DEF = "def";
 	private static final String KEY_IS_A = "is_a";
 	private static final String KEY_RELATIONSHIP = "relationship";
 	private static final String KEY_NAMESPACE = "namespace";
 
 	private ListOrderedMap<String, GOTerm> roots;
 	private HashMap<Integer, GOTerm> terms;
 
 	private GeneOntologyTree() {
 		roots = new ListOrderedMap<String, GOTerm>();
 		terms = new HashMap<Integer, GOTerm>();
 
 		readFromUrl(OboSourcePreference.DEFAULT_REMOTE_LOCATION);
 	}
 	
 	private GeneOntologyTree(String oboFileName) {
 		roots = new ListOrderedMap<String, GOTerm>();
 		terms = new HashMap<Integer, GOTerm>();
 		
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(oboFileName));
 			parseOBOFile(in);
 		} catch (FileNotFoundException e) {
 			JOptionPane
 					.showMessageDialog(
 							null,
 							oboFileName
 									+ ", which is part of geworkbench installation, is missing.\nGene Ontology related functionality will not work correctly unless you choose to use remote obo source.",
 							".obo file missing", JOptionPane.ERROR_MESSAGE);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void parseOBOFile(BufferedReader in) throws IOException {
 		String header = in.readLine();
 		if (!FILE_HEADER1_0.equals(header) && !FILE_HEADER1_2.equals(header)) {
 			throw new IOException("This is not a version 1.0 or 1.2 OBO file.");
 		}
 		String line = in.readLine();
 		if(line.startsWith("data-version:")) {
 			dataVersion = line.substring(line.indexOf(" ")+1);
 		}
 		line = in.readLine();
 		if(line.startsWith("date:")) {
 			date = line.substring(line.indexOf(" ")+1);
 		}
 		log.info("GeneOntologyTree: reading from " + in.toString()+" "+dataVersion+" "+date);
 		line = in.readLine();
 		HashMap<Integer, Term> termMap = new HashMap<Integer, Term>();
 		while (line != null) {
 			while ((line != null) && (!line.equals(TERM_START))) {
 				line = in.readLine();
 			}
 			// We are now at the beginning of a term
 			Term term = new Term();
 			String namespace = null;
 			List<Integer> parents = new ArrayList<Integer>();
 			line = in.readLine();
 			while ((line != null) && (line.trim().length() > 0)) {
 				int splitPoint = line.indexOf(':');
 				if (splitPoint == -1) {
 					System.out
 							.println("Warning: improperly formatted term data: "
 									+ line);
 				} else {
 					String key = line.substring(0, splitPoint);
 					String value = line.substring(splitPoint + 1).trim();
 					if (KEY_ID.equals(key)) {
 						int id = parseID(value);
 						term.setId(id);
 					} else if (KEY_NAME.equals(key)) {
 						term.setName(value);
 					} else if (KEY_DEF.equals(key)) {
 						term.setDef(value);
 					} else if (KEY_IS_A.equals(key)) {
 						int cutoff = value.indexOf('!');
 						if (cutoff != -1) {
 							value = value.substring(0, cutoff).trim();
 						}
 						int id = parseID(value);
 						parents.add(id);
 					} else if (KEY_RELATIONSHIP.equals(key)) {
 						int cutoff = value.indexOf('!');
 						if (cutoff != -1) {
 							value = value.substring(0, cutoff).trim();
 						}
 						int id = parseID(value);
 						parents.add(id);
 					} else if (KEY_NAMESPACE.equals(key)) {
 						namespace = value;
 					}
 				}
 				line = in.readLine();
 			}
 			term.setParents(parents);
 			if (term.getName() != null) {
 				if (term.getName().equals(namespace)) {
 					term.setRoot(true);
 				}
 				termMap.put(term.getId(), term);
 			}
 		}
 		// All terms are now loaded, so do reverse mappings from terms to
 		// children
 		// Build up mappings, and also find roots
 		HashMap<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();
 		Set<Integer> termIDs = termMap.keySet();
 		for (Integer id : termIDs) {
 			Term term = termMap.get(id);
 			GOTerm goterm = new GOTerm(id);
 			goterm.setDefinition(term.getDef());
 			goterm.setName(term.getName());
 			terms.put(id, goterm);
 			List<Integer> parents = term.getParents();
 			if (term.isRoot()) {
 				roots.put(term.getName(), goterm);
 			}
 			for (int j = 0; j < parents.size(); j++) {
 				Integer parent = parents.get(j);
 				ArrayList<Integer> children = map.get(parent);
 				if (children == null) {
 					children = new ArrayList<Integer>();
 					map.put(parent, children);
 				}
 				children.add(term.getId());
 			}
 		}
 		// Set children in each term, populate GOTerms
 		for (Integer id : termIDs) {
 			Term term = termMap.get(id);
 			GOTerm goterm = terms.get(id);
 			ArrayList<Integer> children = map.get(term.getId());
 			List<Integer> parents = term.getParents();
 			GOTerm[] goParents = new GOTerm[parents.size()];
 			for (int i = 0; i < goParents.length; i++) {
 				goParents[i] = terms.get(parents.get(i));
 			}
 			GOTerm[] goChildren;
 			if (children != null) {
 				goChildren = new GOTerm[children.size()];
 				for (int i = 0; i < children.size(); i++) {
 					goChildren[i] = terms.get(children.get(i));
 				}
 			} else {
 				goChildren = new GOTerm[0];
 			}
 			goterm.setParents(goParents);
 			goterm.setChildren(goChildren);
 		}
 	}
 
 	public int getNumberOfRoots() {
 		return roots.size();
 	}
 
 	public GOTerm getRoot(int index) {
 		return roots.get(roots.get(index));
 	}
 
 	public GOTerm getRoot(String rootName) {
 		return roots.get(rootName);
 	}
 
 	public GOTerm getTerm(int id) {
 		return terms.get(id);
 	}
 
 	public Collection<GOTerm> getAllTerms() {
 		return terms.values();
 	}
 
 	/**
 	 * Returns the depth of the term for the given ID. The depth is defined as
 	 * the minimum distance from this term to a root term.
 	 */
 	public int getDepth(int id) {
 		GOTerm term = getTerm(id);
 		return getDepthHelper(term);
 	}
 
 	private int getDepthHelper(GOTerm term) {
 		GOTerm[] parents = term.getParents();
 		if (parents.length == 0) {
 			return 0;
 		} else {
 			int min = Integer.MAX_VALUE;
 			for (GOTerm parent : parents) {
 				int depth = getDepthHelper(parent);
 				if (depth < min) {
 					min = depth;
 				}
 			}
 			return min + 1;
 		}
 	}
 
 	/**
 	 * Gets all the ancestor terms for the term with the given ID. By
 	 * definition, a term is an ancestor of itself.
 	 */
 	public Set<GOTerm> getAncestors(int id) {
 		HashSet<GOTerm> set = new HashSet<GOTerm>();
 		getAncestorsHelper(getTerm(id), set);
 		return set;
 	}
 
 	private void getAncestorsHelper(GOTerm term, Set<GOTerm> set) {
 		if (term != null) {
 			set.add(term);
 			GOTerm[] parents = term.getParents();
 			for (GOTerm parent : parents) {
 				getAncestorsHelper(parent, set);
 			}
 		}else{
 			//System.out.println("EMPTY GOTERM ID:" + term);
 		}
 	}
 
 	/**
 	 * Gets all the children terms for the term with the given ID. By
 	 * definition, a term is a child of itself.
 	 */
 	public Set<GOTerm> getChildren(int id) {
 		HashSet<GOTerm> set = new HashSet<GOTerm>();
 		getChildrenHelper(getTerm(id), set);
 		return set;
 	}
 
 	private void getChildrenHelper(GOTerm term, Set<GOTerm> set) {
 		set.add(term);
 		GOTerm[] children = term.getChildren();
 		for (GOTerm child : children) {
 			getChildrenHelper(child, set);
 		}
 	}
 	
 	// file for the updated version retrieved remotely
 	public final static String OBO_FILENAME = "gene_ontology.1_2.obo";
 
 	/**
 	 * Read ontology file from remote URL
 	 */
 	private void readFromUrl(String url) {
 		long time0 = System.currentTimeMillis();
 		HttpClient client = new HttpClient();
 		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(
 				2, true);
 		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
 				retryhandler);
 		client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
 
 		GetMethod method = new GetMethod(url);
 
 		try {
 			int statusCode = client.executeMethod(method);
 
 			if (statusCode == HttpStatus.SC_OK) {
 				InputStream stream = method.getResponseBodyAsStream();
 				BufferedReader in = new BufferedReader(new InputStreamReader(
 						stream));
 				// this file is put under default current directory meant to be user transparent
 				PrintWriter pw = new PrintWriter(new FileWriter(OBO_FILENAME));
 				String line = in.readLine();
 				while(line!=null) {
 					pw.println(line);
 					line = in.readLine();
 				}
 				pw.close();
 				BufferedReader in2 = new BufferedReader(new FileReader(OBO_FILENAME));
 				parseOBOFile(in2);
 				actualUsedOboSource = null;
 			} else {
 				log.warn("error in reading remote obo from "+url);
 			}
 		} catch (HttpException e) {
 			//e.printStackTrace();
 			handleRemoteException(e);
 		} catch (IOException e) {
 			//e.printStackTrace();
 			handleRemoteException(e);
 		} finally {
 			method.releaseConnection();
 		}
 		long time1 = System.currentTimeMillis();
 		log.info("Time taken to update OBO file: "+(time1-time0)+" milliseconds");
 	}
 	
 	private void handleRemoteException(final Exception e) {
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 
 				public void run() {
 					JOptionPane.showMessageDialog(null,
 						    "Remote obo file was not loaded succesfully due to connection problem:\n>> "+e.getMessage()
						    +"\nAttempting to open local copy.",
 						    "Obo file warning",
 						    JOptionPane.WARNING_MESSAGE);
 				}
 				
 			});
 			actualUsedOboSource = OboSourcePreference.DEFAULT_OBO_FILE;
 			BufferedReader in = new BufferedReader(new FileReader(actualUsedOboSource));
 			parseOBOFile(in);
 		} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		} catch (InvocationTargetException e1) {
 			e1.printStackTrace();
 		} catch (FileNotFoundException e1) {
 			e.printStackTrace();
 		} catch (IOException e1) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getVersion() {
 		return dataVersion;
 	}
 
 	public String getDate() {
 		return date;
 	}
 
 
 	private String actualUsedOboSource = null;
 	public String getActualSource() {
 		OboSourcePreference pref = OboSourcePreference.getInstance();
 		if (pref.getSourceType() == OboSourcePreference.Source.REMOTE
 				&& actualUsedOboSource!=null) {
 			return actualUsedOboSource;
 		} else {
 			return pref.getSourceLocation();
 		}
 	}
 
 	public String getActualFile() {
 		OboSourcePreference pref = OboSourcePreference.getInstance();
 		if (pref.getSourceType() == OboSourcePreference.Source.REMOTE) {
 			if(actualUsedOboSource==null) {
 				return GeneOntologyTree.OBO_FILENAME;
 			} else {
 				return actualUsedOboSource;
 			}
 		} else {
 			return pref.getSourceLocation();
 		}
 	}
 }
