 package kkckkc.jsourcepad.model;
 
 import java.util.List;
 
 import com.google.common.collect.Lists;
 
import kkckkc.jsourcepad.model.bundle.BundleManager;
 import kkckkc.jsourcepad.model.bundle.PrefKeys;
 import kkckkc.jsourcepad.util.SedUtils;
 import kkckkc.syntaxpane.model.LineManager;
 import kkckkc.syntaxpane.model.Scope;
 import kkckkc.syntaxpane.model.SourceDocument;
 import kkckkc.syntaxpane.model.LineManager.Line;
 import kkckkc.syntaxpane.util.Pair;
 
 public class SymbolList {
 	private Buffer buffer;
 
 	public SymbolList(Buffer buffer) {
 		this.buffer = buffer;
 	}
 	
 	public List<Pair<String, Integer>> getSymbols() {
 		List<Pair<String, Integer>> symbols = Lists.newArrayList();
 
 		SourceDocument sd = buffer.getSourceDocument();
 		LineManager lm = sd.getLineManager();
 		Line l = lm.getLineByPosition(0);
 		while (l != null) {
 			Scope root = l.getScope().getRoot();
 			visit(l, root, symbols);
 			
 			l = lm.getNext(l);
 		}	
 		
 		return symbols;
 	}
 	
 
 	private void visit(Line l, Scope s, List<Pair<String, Integer>> symbolList) {
		BundleManager bundleManager = Application.get().getBundleManager();
		
		Object o = bundleManager.getPreference(PrefKeys.SYMBOL_SHOW_IN_LIST, s);
 		if (s.getRoot() != s && o != null) {
 			String symbol = l.getCharSequence().subSequence(s.getStart(), s.getEnd()).toString();
 			
			String transformation = (String) bundleManager.getPreference(PrefKeys.SYMBOL_TRANSFORMATION, s);
 			if (transformation != null) {
 				symbol = SedUtils.applySedExpressions(symbol, transformation);
 			}
 			symbolList.add(new Pair<String, Integer>(symbol, l.getStart() + s.getStart()));
 		} else if (s.hasChildren()) {
         	for (Scope sc : s.getChildren()) {
         		visit(l, sc, symbolList);
         	}
 		}
     }	
 }
