 package de.vilandgr.cssc;
 
 import java.util.ArrayList;
 
 import de.vilandgr.cssc.node.ARuleset;
 import de.vilandgr.cssc.node.ARulesetSelectorAp;
 import de.vilandgr.cssc.node.ASelector;
 import de.vilandgr.cssc.node.PRulesetSelectorAp;
 
 class SelectorPrinter {
 	private ARuleset set;
 	
 	private SelectorPrinter parent;
 	
 	private ArrayList<ASelector> selectors;
 	
 	private final String SPACE = " ";
 
 	private final String SEP = ",";
 
 	public SelectorPrinter(SelectorPrinter parent, ARuleset set) {
 		super();
 		this.set = set;
 		this.parent = parent;
 		this.selectors = new ArrayList<ASelector>();
 		
 		// add all selectors to list
 		selectors.add((ASelector)this.set.getSelector());
 		for (PRulesetSelectorAp selector : this.set.getRulesetSelectorAp()) {
 			ARulesetSelectorAp aselector = (ARulesetSelectorAp)selector;
 			selectors.add((ASelector)aselector.getSelector());
 		}
 	}
 	
 	public SelectorPrinter getParent() {
 		return parent;
 	}
 	
 	public StringBuilder buildSelectorPath(StringBuilder path, ASelector selector) {		
 		// add parent path
 		if (this.parent != null) {
 			for (ASelector sel : selectors) {
 				parent.buildSelectorPath(path, sel);
 				if (selector != null) {
 					path.append(SPACE);
 					path.append(getCompressed(selector));
 				}
 			}
 		} else {
 			for (ASelector sel : selectors) {
				path.append(SEP);
 				path.append(SPACE);
 				path.append(getCompressed(sel));
 				if (selector != null) {
 					path.append(SPACE);
 					path.append(getCompressed(selector));
 				}
 			}
 		}
 		
 		return path;
 	}
 	
 	public String getCompressed(ASelector selector) {
 		StringCompressorAdapter sca = new StringCompressorAdapter();
 		selector.apply(sca);
 		return sca.toString();
 	}
 	
 	public String toString() {
 		String build = buildSelectorPath(new StringBuilder(), null).toString();
 		String cleaned = build.replaceAll(SPACE + "[&]|^" + SEP + SPACE, "");
 		return cleaned;
 	}
 }
