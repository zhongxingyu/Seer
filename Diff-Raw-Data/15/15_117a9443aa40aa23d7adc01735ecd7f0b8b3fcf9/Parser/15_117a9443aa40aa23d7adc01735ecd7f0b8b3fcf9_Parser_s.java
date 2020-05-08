 package kkckkc.syntaxpane.parse;
 
 import kkckkc.syntaxpane.model.*;
 import kkckkc.syntaxpane.model.LineManager.Line;
 import kkckkc.syntaxpane.parse.grammar.ContainerContext;
 import kkckkc.syntaxpane.parse.grammar.Context;
 import kkckkc.syntaxpane.parse.grammar.Language;
 import kkckkc.syntaxpane.parse.grammar.MatchableContext;
 import kkckkc.syntaxpane.regex.Matcher;
 import kkckkc.utils.Pair;
 
 import java.util.List;
 
 
 public class Parser {
 	public enum ChangeEvent { ADD, UPDATE, REMOVE }
 
     private static final long MAX_PARSE_TIME = 50 * 1000L * 1000L;
     private static final int SAMPLE_INTERVAL = 100;
 
 	private MutableLineManager lineManager;
 	private Language language;
 	private MutableFoldManager foldManager;
 	
 	public Parser(Language language, MutableLineManager lineManager, MutableFoldManager foldManager) {
 		this.language = language;
 		this.lineManager = lineManager;
 		this.foldManager = foldManager;
 
 		this.foldManager.setFoldEndPattern(language.getFoldEnd());
 	}
 	
 	public Pair<Interval, Interval> parse(int start, int end, ChangeEvent event) {
 		boolean foldChanges = false;
 
 		LineManager.Line line;
 		if (event == ChangeEvent.REMOVE) {
 			Pair<Line, Line> linePair = lineManager.removeInterval(new Interval(start, end));
 			line = linePair.getFirst();
 			
 			foldChanges = foldManager.removeLines(new Interval(linePair.getFirst().getIdx(), linePair.getSecond().getIdx()));
 			
 		} else if (event == ChangeEvent.ADD) {
 			List<Line> lines = lineManager.addInterval(new Interval(start, end));
 
 			if (lines.size() > 1) {
 				foldChanges = foldManager.addLines(new Interval(lines.get(0).getIdx(), lines.get(lines.size() - 1).getIdx()));
 			}
 			
 			line = lineManager.getLineByPosition(start);
 		} else {
 			line = lineManager.getLineByPosition(start);
 		}
 
         long startTimestamp = System.nanoTime();
 
         int i = 0;
         boolean partialParse = false;
 		LineManager.Line previous = lineManager.getPrevious(line);
 		Scope scope = previous == null ? null : previous.getScope();
 		while (line != null) {
 			scope = parseLine(scope, line);
 			
 			Scope origScope = line.getScope();
 			line.setScope(scope);
 
 			if (language.getFoldStart() != null) {
 				if (language.getFoldStart().matcher(line.getCharSequence(false)).matches()) {
 					foldChanges |= foldManager.setFoldableFlag(line.getIdx(), true);
 				} else {
 					foldChanges |= foldManager.setFoldableFlag(line.getIdx(), false);
 				}
 			}
 			
 			line = lineManager.getNext(line);
 
             if ((++i % SAMPLE_INTERVAL) == 0) {
                 if (System.nanoTime() - startTimestamp > MAX_PARSE_TIME) {
                     partialParse = true;
                     break;
                 }
             }
 
 			if (origScope == null) {
 				continue;
 			}
 
 			if (origScope.hasSameSignature(scope)) {
 				break;
 			}
 		}
 
 		if (foldChanges) {
 			foldManager.fireFoldUpdated();
 		}
 
         return new Pair<Interval, Interval>(
                 new Interval(start, line == null ? Integer.MAX_VALUE : line.getStart()),
                 partialParse && line != null ? new Interval(line.getStart(), end < line.getStart() ? line.getStart() + 1 : end) : null);
 	}
 	
 	private Scope parseLine(Scope scope, LineManager.Line line) {
 		CharSequence seq = line.getCharSequence(true);
 		
 		// Apply previous scope to all of this line
 		if (scope != null) {
 			scope = copyScopeOfPreviousLine(scope);
 			
 		} else {
 			scope = new Scope(Integer.MIN_VALUE, Integer.MAX_VALUE, language.getRootContext(), null);
 		}
 
 		int position = 0;
 		boolean newContextToParse;
 		do {
 			newContextToParse = false;
 			
 			MatchableContext def = (MatchableContext) scope.getContext();
 
 			// Resolve contexts 
             MatchableContext[] contexts = findMatchableContexts(def);
 			
 			// Make sure the matchers are in order as follows
 			//  1. extend parent
 			//  2. end matcher for parent
 			//  3. rest
             Matcher[] matchers = new Matcher[contexts.length + 1];
             int rootMatcherIdx = buildMatchers(scope, line, seq, def, contexts, matchers);
 			
 			MatcherCollectionIterator iterator = new MatcherCollectionIterator(matchers);
 			iterator.setPosition(position);
 			
 			while (iterator.hasNext()) {
 				Integer matcherIdx = iterator.next();
 				Matcher matcher = matchers[matcherIdx];
 
 				int contextIdx = matcherIdx < rootMatcherIdx ? matcherIdx : matcherIdx - 1;
 				MatchableContext child = matcherIdx != rootMatcherIdx ? contexts[contextIdx] : null;
 
 				if (matcherIdx == rootMatcherIdx || (child != null && child.isEndParent())) {
 					// End current context, pop context stack and move position
 
                     // If start and end match are at the same place, we will enter an
                     // infinite loop.
                     if (def instanceof ContainerContext && scope.getStart() == matcher.end()) {
                         iterator.ignore();
                         continue;
                     }
                     
 					position = ((ContainerContext) def).close(scope, matcher);
 					scope = scope.getParent();
 					newContextToParse = true;
 					break;
 				} else {
 					if (child.isOnceOnly()) matchers[matcherIdx] = null;
					
 					Scope c = child.createScope(scope, matcher);
 					if (child instanceof ContainerContext) {
 						// Sub context, simulate recursive call by putting context on stack and moving position
 						scope = c;
 						position = matcher.end();
 						newContextToParse = true;
 						break;
					}
 				}
 			}
 		} while (newContextToParse);
 		
 		// Make sure all scopes are open
 		for (Scope s : scope.getAncestors()) {
 			s.makeOpenEnded();
 		}
 		
 		return scope;
 	}
 
     private int buildMatchers(Scope scope, Line line, CharSequence seq, MatchableContext def, MatchableContext[] contexts, Matcher[] matchers) {
         int rootMatcherIdx = 0;
         for (MatchableContext context : contexts) {
             if (context == null) continue;
             if (context.isExtendParent()) {
                 matchers[rootMatcherIdx++] = context.getMatcher(seq);
             }
         }
         if (def instanceof ContainerContext) {
             matchers[rootMatcherIdx] = ((ContainerContext) def).getEndMatcher(seq, scope);
         } else {
             matchers[rootMatcherIdx] = null;
         }
         for (int i = 0; i < contexts.length; i++) {
             if (contexts[i] == null) continue;
             if (contexts[i].isExtendParent()) continue;
 
             if (! (contexts[i].isFirstLineOnly() && line.getIdx() > 0)) {
                 matchers[i + 1] = contexts[i].getMatcher(seq);
             }
         }
         return rootMatcherIdx;
     }
 
     private MatchableContext[] findMatchableContexts(MatchableContext def) {
         int i = 0;
         Context[] children = def.getUnnestedChildren();
         MatchableContext[] contexts = new MatchableContext[children.length];
         for (Context ctx : children) {
             if (ctx instanceof MatchableContext) {
                 contexts[i++] =	(MatchableContext) ctx;
             }
         }
         return contexts;
     }
 
     private Scope copyScopeOfPreviousLine(Scope scope) {
 	    scope = scope.copy(Integer.MIN_VALUE, Integer.MAX_VALUE);
 	    
 	    // Remove all that ends at line end
 	    while (true) {
 	    	Context context = scope.getContext();
 	    	if (context instanceof ContainerContext) {
 	    		ContainerContext acc = (ContainerContext) context;
 	    		if (acc.isEndAtLineEnd()) {
 	    			scope.getParent().getChildren().clear();
 	    			scope = scope.getParent();
 	    			continue;
 	    		}
 	    	}
 	    	
 	    	break;
 	    }
 	    return scope;
     }
 
 	public Language getLanguage() {
 	    return language;
     }
 }
  
