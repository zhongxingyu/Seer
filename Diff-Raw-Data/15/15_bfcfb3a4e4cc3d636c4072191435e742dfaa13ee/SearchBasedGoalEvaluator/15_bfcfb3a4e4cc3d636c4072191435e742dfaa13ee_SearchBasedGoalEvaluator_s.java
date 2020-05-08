 package org.eclipse.dltk.ti.goals;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.search.FieldReferenceMatch;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.MethodReferenceMatch;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchMatch;
 import org.eclipse.dltk.core.search.SearchParticipant;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.SearchRequestor;
import org.eclipse.dltk.ti.BasicContext;
 import org.eclipse.dltk.ti.GoalState;
 
 public abstract class SearchBasedGoalEvaluator extends GoalEvaluator {
 	
 	private List possiblePositionsGoals = new ArrayList();
 	private List references = new ArrayList();
 		
 	private SearchRequestor requestor = new SearchRequestor() {
 
 		public void acceptSearchMatch(SearchMatch match) throws CoreException {
 			ASTNode node = null;
 			if (match instanceof FieldReferenceMatch) {
 				FieldReferenceMatch match2 = (FieldReferenceMatch) match;
 				node = match2.getNode();
 			}						
 			if (match instanceof MethodReferenceMatch) {
 				MethodReferenceMatch match2 = (MethodReferenceMatch) match;
 				node = match2.getNode();
 			}
 			PossiblePosition pos = new PossiblePosition(match.getResource(),
 					match.getOffset(), match.getLength(), node);
 			possiblePositionsGoals.add(createVerificationGoal(pos));			
 		}
 		
 	};
 		
 	public SearchBasedGoalEvaluator(IGoal goal) {
 		super(goal);
 	}
 
 
 	public IGoal[] init() {	
 		IGoal goal = getGoal();
		BasicContext basicContext = (BasicContext) goal.getContext();
		IDLTKProject project = basicContext.getSourceModule().getScriptProject();
 		IDLTKSearchScope scope = SearchEngine.createSearchScope(new IModelElement[] {project});
 		SearchPattern pattern = createSearchPattern();
 		SearchEngine engine = new SearchEngine();
 	
 		try {
 			engine.search(pattern, new SearchParticipant[] { SearchEngine
 					.getDefaultSearchParticipant() }, scope, requestor, null);
 		} catch (CoreException e) {
 			e.printStackTrace();
 			return IGoal.NO_GOALS;
 		}
 
 		return (IGoal[]) possiblePositionsGoals.toArray(new IGoal[possiblePositionsGoals.size()]);
 	}
 
 	public IGoal[] subGoalDone(IGoal subgoal, Object result, GoalState state) {
 		if (result != null && result instanceof ItemReference) {
 			references.add(result);
 		}
 		return IGoal.NO_GOALS;
 	}
 
 	public Object produceResult() {
 		return references.toArray(new ItemReference[references.size()]);
 	}
 	
 	protected abstract SearchPattern createSearchPattern ();
 	protected abstract IGoal createVerificationGoal (PossiblePosition pos);
 
 }
