 package histaroach.util;
 
 import histaroach.model.DiffFile;
 import histaroach.model.HistoryGraph;
 import histaroach.model.Revision;
 import histaroach.model.Revision.Compilable;
 import histaroach.model.TestResult;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 
 /**
  * HistoryGraphXMLReader reads an XML file representing a HistoryGraph 
  * and reconstructs the HistoryGraph instance.
  */
 public class HistoryGraphXMLReader extends XMLReader<HistoryGraph> {
 	
 	private final Map<String, Revision> revisions;
 	
 	public HistoryGraphXMLReader(File xmlFile) throws ParserConfigurationException, 
 			SAXException, IOException {
 		super(xmlFile);
 		revisions = new HashMap<String, Revision>();
 	}
 	
 	/**
 	 * Reconstructs the HistoryGraph instance from the XML file.
 	 * 
 	 * @return the HistoryGraph.
 	 */
 	@Override
 	public HistoryGraph read() {
 		HistoryGraph hGraph = new HistoryGraph();
 		
 		List<Element> revisionElements = traverseContainedElements(rootElement);
 		
 		for (Element revisionElement : revisionElements) { // <Revision>
 			Revision revision = parseRevisionElement(revisionElement);
 			hGraph.addRevision(revision);
 			
 			revisions.put(revision.getCommitID(), revision);
 		}
 		
 		return hGraph;
 	}
 	
 	public Revision parseRevisionElement(Element revisionElement) { // <Revision>
 		Iterator<Element> iter = traverseContainedElements(revisionElement).iterator();
 		
 		Element commitIDElement = iter.next();		// <commitID>
 		Element compilableElement = iter.next();	// <Compilable>
 		Element testAbortedElement = iter.next();	// <testAborted>
 		Element testResultElement = iter.next();	// <TestResult>
 		Element parentsElement = iter.next();		// <Parents>
 		
 		String commitID = getString(commitIDElement);
 		Compilable compilable = parseCompilableElement(compilableElement);
 		boolean testAborted = parseTestAbortedElement(testAbortedElement);
 		TestResult testResult = null;
 		
		if (compilable == Compilable.YES) {
 			testResult = parseTestResultElement(testResultElement);
 		}
 		
 		Map<Revision, Set<DiffFile>> parentToDiffFiles = parseParentsElement(parentsElement);
 		
 		Revision revision = new Revision(commitID, parentToDiffFiles, compilable, 
 				testAborted, testResult);
 		
 		return revision;
 	}
 	
 	public Map<Revision, Set<DiffFile>> parseParentsElement(Element parentsElement) { // <Parents>
 		Map<Revision, Set<DiffFile>> parentToDiffFiles = new HashMap<Revision, Set<DiffFile>>();
 		
 		List<Element> parentElements = traverseContainedElements(parentsElement);
 		
 		for (Element parentElement : parentElements) {
 			Pair<Revision, Set<DiffFile>> pair = parseParentElement(parentElement);
 			Revision parent = pair.getFirst();
 			Set<DiffFile> diffFiles = pair.getSecond();
 			
 			parentToDiffFiles.put(parent, diffFiles);
 		}
 		
 		return parentToDiffFiles;
 	}
 	
 	public Pair<Revision, Set<DiffFile>> parseParentElement(Element parentElement) { // <Parent>
 		Iterator<Element> iter = traverseContainedElements(parentElement).iterator();
 		
 		Element commitIDElement = iter.next();	// <commitID>
 		Element diffFilesElement = iter.next();	// <DiffFiles>
 		
 		String parentCommitID = getString(commitIDElement);
 		Set<DiffFile> diffFiles = parseDiffFilesElement(diffFilesElement);
 		
 		// because of topological ordering of Revisions in HistoryGraph
 		assert revisions.containsKey(parentCommitID);
 		
 		Revision parent = revisions.get(parentCommitID);
 		
 		Pair<Revision, Set<DiffFile>> pair = new Pair<Revision, Set<DiffFile>>(parent, diffFiles);
 		
 		return pair;
 	}
 }
