 package org.eclipse.viatra2.emf.incquery.queryexplorer.content.matcher;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.viatra2.emf.incquery.databinding.runtime.DatabindingAdapterUtil;
 import org.eclipse.viatra2.emf.incquery.queryexplorer.QueryExplorer;
 import org.eclipse.viatra2.emf.incquery.queryexplorer.util.DatabindingUtil;
 import org.eclipse.viatra2.emf.incquery.runtime.api.IPatternMatch;
 import org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher;
 import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.misc.DeltaMonitor;
 import org.eclipse.viatra2.patternlanguage.core.patternLanguage.Annotation;
 import org.eclipse.viatra2.patternlanguage.core.patternLanguage.AnnotationParameter;
 import org.eclipse.viatra2.patternlanguage.core.patternLanguage.impl.StringValueImpl;
 
 /**
  * A PatternMatcher is associated to every IncQueryMatcher which is annotated with PatternUI annotation.
  * These elements will be the children of the top level elements in the treeviewer.
  * 
  * @author Tamas Szabo
  *
  */
 public class ObservablePatternMatcher {
 	
 	private List<ObservablePatternMatch> matches;
 	private IncQueryMatcher<IPatternMatch> matcher;
 	private DeltaMonitor<IPatternMatch> deltaMonitor;
 	private Runnable processMatchesRunnable;
 	private Map<IPatternMatch, ObservablePatternMatch> sigMap;
 	private ObservablePatternMatcherRoot parent;
 	private boolean generated;
 	private String patternFqn;
 	private IPatternMatch filter;
 	private Object[] parameterFilter;
 	private String orderParameter;
 	private boolean descendingOrder;
 	
 	public ObservablePatternMatcher(ObservablePatternMatcherRoot parent, IncQueryMatcher<IPatternMatch> matcher, String patternFqn, boolean generated) {
 		this.parent = parent;
 		this.patternFqn = patternFqn;
 		this.matches = new ArrayList<ObservablePatternMatch>();
 		this.matcher = matcher;
 		this.generated = generated;
 		this.orderParameter = null;
 		
 		if (matcher != null) {
 			Annotation annotation = DatabindingUtil.getAnnotation(matcher.getPattern(), DatabindingUtil.ORDERBY_ANNOTATION);
 			if (annotation != null) {
 				for (AnnotationParameter ap : annotation.getParameters()) {
 					if (ap.getName().matches("key")) {
 						orderParameter = ((StringValueImpl) ap.getValue()).getValue(); 
 					}
 					if (ap.getName().matches("direction")) {
 						String direction = ((StringValueImpl) ap.getValue()).getValue(); 
 						if (direction.matches("desc")) {
 							descendingOrder = true;
 						}
 						else {
 							descendingOrder = false;
 						}
 					}
 				}
 			}
 
 			initFilter();
 			this.sigMap = new HashMap<IPatternMatch, ObservablePatternMatch>();
 			this.deltaMonitor = this.matcher.newFilteredDeltaMonitor(true, filter);
 			this.processMatchesRunnable = new Runnable() {		
 				@Override
 				public void run() {
 					processNewMatches(deltaMonitor.matchFoundEvents);
 					processLostMatches(deltaMonitor.matchLostEvents);
 					deltaMonitor.clear();
 				}
 			};
 			
 			this.matcher.addCallbackAfterUpdates(processMatchesRunnable);
 			this.processMatchesRunnable.run();
 		}
 	}
 	
 	/**
 	 * Call this method to remove the callback handler from the delta monitor of the matcher.
 	 */
 	public void dispose() {
 		if (matcher != null) {
 			for (ObservablePatternMatch pm : matches) {
 				pm.dispose();
 			}
 			this.matcher.removeCallbackAfterUpdates(processMatchesRunnable);
 			processMatchesRunnable = null;
 		}
 	}
 	
 	/**
 	 * Returns the index of the new match in the list based on the ordering set on the matcher.
 	 * 
 	 * @param match the match that will be inserted
 	 * @return -1 if the match should be inserted at the end of the list, else the actual index
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private int placeOfMatch(IPatternMatch match) {
 		if (orderParameter != null) {
 			String[] tokens = orderParameter.split("\\.");
 			String orderParameterClass = tokens[0];
 			String orderParameterAttribute = tokens[1];
 			
 			EObject obj = (EObject) match.get(orderParameterClass);
 			EStructuralFeature feature = DatabindingAdapterUtil.getFeature(obj, orderParameterAttribute);
 			Object value = obj.eGet(feature);
 			if (value instanceof Comparable) {
 				
 				for (int i = 0;i<matches.size();i++) {
 					IPatternMatch compMatch = matches.get(i).getPatternMatch();
 					EObject compObj = (EObject) compMatch.get(orderParameterClass);
 					EStructuralFeature compFeature = DatabindingAdapterUtil.getFeature(compObj, orderParameterAttribute);
 					Comparable compValue = (Comparable) compObj.eGet(compFeature);
 					//descending order, the place is when the new match is greater than the actual element
 					if (descendingOrder) {
 						if (compValue.compareTo(value) <= 0) {
 							return i;
 						}
 					}
 					//ascending order, the place is when the new match is smaller than the actual element
 					else {
 						if (compValue.compareTo(value) >= 0) {
 							return i;
 						}
 					}
 				}
 			}	
 		}
 		return -1;
 	}
 	
 	private void processNewMatches(Collection<IPatternMatch> matches) {
 		for (IPatternMatch s : matches) {
 			addMatch(s);
 		}
 	}
 
 	private void processLostMatches(Collection<IPatternMatch> matches) {
 		for (IPatternMatch s : matches) {
 			removeMatch(s);
 		}
 	}
 	
 	private void addMatch(IPatternMatch match) {
 		ObservablePatternMatch pm = new ObservablePatternMatch(this, match);
 		this.sigMap.put(match, pm);
 		int index = placeOfMatch(match);
 		
 		if (index == -1) {
 			this.matches.add(pm);
 		}
 		else {
 			this.matches.add(index, pm);
 		}
 		
 		QueryExplorer.getInstance().getMatcherTreeViewer().refresh(this);
 	}
 	
 	private void removeMatch(IPatternMatch match) {
		//null checks - eclipse closing - issue 162
 		ObservablePatternMatch observableMatch = this.sigMap.remove(match);
		if (observableMatch != null) {
			this.matches.remove(observableMatch);
			observableMatch.dispose();
		}
		if (QueryExplorer.getInstance() != null) {
			QueryExplorer.getInstance().getMatcherTreeViewer().refresh(this);
		}
 	}
 
 	public ObservablePatternMatcherRoot getParent() {
 		return parent;
 	}
 	
 	public IncQueryMatcher<IPatternMatch> getMatcher() {
 		return matcher;
 	}
 	
 	public String getPatternName() {
 		return patternFqn;
 	}
 	
 	private void initFilter() {
 		if (matcher != null) {
 			parameterFilter = new Object[this.matcher.getParameterNames().length];
 			
 			for (int i = 0;i<this.matcher.getParameterNames().length;i++) {
 				parameterFilter[i] = null;
 			}
 			
 			this.filter = this.matcher.arrayToMatch(parameterFilter);
 		}
 	}
 	
 	public void setFilter(Object[] parameterFilter) {
 		this.parameterFilter = parameterFilter;
 		this.filter = this.matcher.arrayToMatch(parameterFilter);
 		
 		Set<IPatternMatch> tmp = new HashSet<IPatternMatch>(sigMap.keySet());
 		
 		for (IPatternMatch match : tmp) {
 			removeMatch(match);
 		}
 		
 		QueryExplorer.getInstance().getMatcherTreeViewer().refresh(this);
 		this.deltaMonitor = this.matcher.newFilteredDeltaMonitor(true, filter);
 		this.processMatchesRunnable.run();
  	}
 	
 	private boolean isFiltered() {	
 		if (matcher != null) {
 			for (int i = 0;i<this.matcher.getParameterNames().length;i++) {
 				if (parameterFilter[i] != null) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public Object[] getFilter() {
 		return parameterFilter;
 	}
 
 	public String getText() {
 		String isGeneratedString = isGenerated() ? " (Generated)" : " (Runtime)";
 		if (matcher == null) {
 			return String.format("Matcher could not be created for pattern '%s' %s", patternFqn, isGeneratedString);
 		}
 		else {
 			String matchString;
 			switch (matches.size()){
 			case 0: 
 				matchString = "No matches";
 				break;
 			case 1:
 				matchString = "1 match";
 				break;
 			default:
 				matchString = String.format("%d matches", matches.size());
 			}
 			
 			String filtered = isFiltered() ? " - Filtered" : "";
 			
 			//return this.matcher.getPatternName() + (isGeneratedString +" [size of matchset: "+matches.size()+"]");
 			return String.format("%s - %s %s %s", matcher.getPatternName(), matchString, filtered, isGeneratedString);
 		}
 	}
 
 	public static final String MATCHES_ID = "matches";
 	public List<ObservablePatternMatch> getMatches() {
 		return matches;
 	}
 
 	public boolean isGenerated() {
 		return generated;
 	}
 	
 	public boolean isCreated() {
 		return matcher != null;
 	}
 }
