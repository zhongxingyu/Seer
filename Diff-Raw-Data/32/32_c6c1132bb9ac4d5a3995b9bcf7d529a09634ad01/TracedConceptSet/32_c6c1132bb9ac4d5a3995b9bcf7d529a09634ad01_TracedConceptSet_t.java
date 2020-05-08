 package edu.iastate.pdlreasoner.tableau;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import edu.iastate.pdlreasoner.model.Concept;
 import edu.iastate.pdlreasoner.tableau.branch.BranchPoint;
 import edu.iastate.pdlreasoner.util.CollectionUtil;
 
 public class TracedConceptSet {
 
 	private Map<Concept, TracedConcept> m_Open;
 	private Map<Concept, TracedConcept> m_Expanded;
 	
 	public TracedConceptSet() {
 		m_Open = CollectionUtil.makeMap();
 		m_Expanded = CollectionUtil.makeMap();
 	}
 	
 	public boolean add(TracedConcept tc) {
 		Concept c = tc.getConcept();
 		if (m_Expanded.containsKey(c)) return false;
 		return m_Open.put(c, tc) == null;
 	}
 	
 	public Set<TracedConcept> flush() {
 		if (m_Open.isEmpty()) return Collections.emptySet();
 		Set<TracedConcept> openCopy = CollectionUtil.copy(m_Open.values());
 		m_Expanded.putAll(m_Open);
 		m_Open.clear();
 		return openCopy;
 	}
 	
 	public TracedConcept getTracedConceptWith(Concept c) {
 		TracedConcept tc = m_Expanded.get(c);
 		if (tc != null) return tc;
 		return m_Open.get(c);
 	}
 	
 	public boolean isComplete() {
 		return m_Open.isEmpty();
 	}
 	
 	public Collection<TracedConcept> getExpanded() {
 		return m_Expanded.values();
 	}
 	
 	public boolean containsAllConcepts(TracedConceptSet o) {
 		return containsAllConcepts(o.m_Open.keySet()) && containsAllConcepts(o.m_Expanded.keySet());
 	}
 	
 	private boolean containsAllConcepts(Set<Concept> cs) {
 		for (Concept c : cs) {
 			if (getTracedConceptWith(c) == null) return false;
 		}
 		return true;
 	}
 
 	public boolean prune(BranchPoint restoreTarget) {
 		boolean hasChanged = prune(restoreTarget, m_Open);
 		hasChanged |= prune(restoreTarget, m_Expanded);
 		return hasChanged;
 	}
 	
 	public void reopen(TracedConcept tc) {
 		m_Expanded.remove(tc.getConcept());
 		m_Open.put(tc.getConcept(), tc);
 	}
 
 	public void reopenAll() {
		if (m_Expanded.isEmpty()) return;
		
 		m_Open.putAll(m_Expanded);
 		m_Expanded.clear();
 	}
 	
 	private boolean prune(BranchPoint restoreTarget, Map<Concept, TracedConcept> map) {
 		boolean hasChanged = false;
 		for (Iterator<TracedConcept> it = map.values().iterator(); it.hasNext(); ) {
 			TracedConcept tc = it.next();
 			if (tc.getDependency().hasSameOrAfter(restoreTarget)) {
 				it.remove();
 				hasChanged = true;
 			}
 		}
 		return hasChanged;
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Open:\n");
 		for (Concept key : m_Open.keySet()) {
 			builder.append(key).append("\n");
 		}
 		
 		builder.append("\nExpanded:\n");
 		for (Concept key : m_Expanded.keySet()) {
 			builder.append(key).append("\n");
 		}
 		return builder.toString();
 	}
 }
