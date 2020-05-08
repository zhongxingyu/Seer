 package kkckkc.jsourcepad.model;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import kkckkc.jsourcepad.model.bundle.BundleManager;
 import kkckkc.jsourcepad.model.bundle.PrefKeys;
 import kkckkc.jsourcepad.util.AutoSuggestUtils;
 import kkckkc.jsourcepad.util.SedUtils;
 import kkckkc.syntaxpane.model.LineManager;
 import kkckkc.syntaxpane.model.LineManager.Line;
 import kkckkc.syntaxpane.model.Scope;
import kkckkc.utils.CharSequenceUtils;
 import kkckkc.utils.Pair;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 public class SymbolList {
 	private Buffer buffer;
     private BundleManager bundleManager;
 
     public SymbolList(Buffer buffer) {
 		this.buffer = buffer;
         this.bundleManager = Application.get().getBundleManager();
 	}
 	
 	public List<Pair<String, Integer>> getSymbols() {
 		List<Pair<String, Integer>> symbols = Lists.newArrayList();
 
 		LineManager lm = buffer.getLineManager();
 		Line line = lm.getLineByPosition(0);
 		while (line != null) {
 			Scope root = line.getScope().getRoot();
 
 			visit(line, root, root, symbols);
 			
 			line = lm.getNext(line);
 		}
 
 		return symbols;
 	}
 
 	private void visit(Line line, Scope scope, Scope root, List<Pair<String, Integer>> symbolList) {
         Object o = null;
 
         LinkedList<Scope> queue = new LinkedList<Scope>();
         queue.add(scope);
 
         while (! queue.isEmpty()) {
             scope = queue.removeFirst();
 
             // Skip multiline scopes
             if (scope.getStart() != Integer.MIN_VALUE || scope.getEnd() != Integer.MAX_VALUE)
                 o = bundleManager.getPreference(PrefKeys.SYMBOL_SHOW_IN_LIST, scope);
 
             if (root != scope && o != null) {
                String symbol = CharSequenceUtils.safeSubsequence(line.getCharSequence(false), scope.getStart(), scope.getEnd()).toString();
 
                 String transformation = (String) bundleManager.getPreference(PrefKeys.SYMBOL_TRANSFORMATION, scope);
                 if (transformation != null) {
                     symbol = SedUtils.applySedExpressions(symbol, transformation);
                 }
                 symbolList.add(new Pair<String, Integer>(symbol, line.getStart() + scope.getStart()));
             } else if (scope.hasChildren()) {
                 List<Scope> children = scope.getChildren();
                 int size = children.size();
                 for (int i = 0; i < size; i++) {
                     queue.addFirst(children.get(i));
                 }
             }
         }
 
     }
 
     public List<Pair<String, Integer>> getSymbols(final String query) {
         List<Pair<String, Integer>> all = getSymbols();
         List<Pair<String, Integer>> dest = Lists.newArrayList();
 
         Predicate<String> predicate = AutoSuggestUtils.makePredicate(query);
 
         for (Pair<String, Integer> p : all) {
             if (predicate.apply(p.getFirst())) dest.add(p);
         }
 
         Ordering<Pair<String, Integer>> scoringOrdering = Ordering.natural().onResultOf(
                 new Function<Pair<String, Integer>, Integer>() {
                     public Integer apply(Pair<String, Integer> p) {
                         int score = 0;
 
                         // Score by matching characters, matches late in string decreases score
                         score -= AutoSuggestUtils.getScorePenalty(p.getFirst(), query);
 
                         return score;
                     }
                 }).reverse();
 
         Collections.sort(dest, scoringOrdering);
 
         return dest;
     }
 }
