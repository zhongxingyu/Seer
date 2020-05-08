 package de.enwida.web.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 
 import de.enwida.transport.Aspect;
 import de.enwida.transport.DataResolution;
 import de.enwida.web.db.model.CalendarRange;
 import de.enwida.web.db.model.NavigationDefaults;
 import de.enwida.web.model.ProductTree.ProductAttributes;
 import de.enwida.web.utils.NavigationDictionary;
 
 
 public class ChartNavigationData implements Cloneable {
 
 	private NavigationDefaults defaults;
 	private List<ProductTree> productTrees;
 	private List<String> timeRanges;
 	private List<Aspect> aspects;
 
	private int decimals = 2;
 	private boolean isDateScale;
 	private boolean hasTimeSelection;
 	private boolean hasProductSelection;
 	private boolean hasLineSelection;
 	
 	@JsonIgnore
 	private NavigationDictionary dictionary;
 
 	public ChartNavigationData() {
 		this.productTrees = new ArrayList<ProductTree>();
 		this.timeRanges = new ArrayList<String>();
 		this.aspects = new ArrayList<Aspect>();
 		this.dictionary = new NavigationDictionary();
 	}
 	
 	public ChartNavigationData clone() {
 	    final ChartNavigationData result = new ChartNavigationData();
 	    result.setDefaults(defaults.clone());
 	    result.aspects = new ArrayList<Aspect>(aspects);
 	    result.timeRanges = new ArrayList<String>(timeRanges);
 	    result.dictionary = dictionary.clone();
 
	    result.setDecimals(decimals);
 	    result.setIsDateScale(isDateScale);
 	    result.setHasLineSelection(hasLineSelection);
 	    result.setHasProductSelection(hasProductSelection);
 	    result.setHasTimeSelection(hasTimeSelection);
 
 	    for (final ProductTree tree : productTrees) {
 	        result.addProductTree(tree.clone());
 	    }
 	    return result;
 	}
 
 	public NavigationDefaults getDefaults() {
 		return this.defaults;
 	}
 
 	public List<ProductTree> getProductTrees() {
 		return this.productTrees;
 	}
 	
 	public void addProductTree(ProductTree productTree) {
 	    this.productTrees.add(productTree);
 	}
 
 	public List<DataResolution> getAllResolutions() {
 	    final List<DataResolution> result = new ArrayList<DataResolution>();
 	    final Set<DataResolution> set = new HashSet<DataResolution>();
 
 	    for (final ProductTree productTree : productTrees) {
     	    for (final ProductAttributes product : productTree.flatten()) {
     	        set.addAll(product.resolutions);
     	    }
 	    }
 	    result.addAll(set);
 	    return result;
 	}
 
 	public CalendarRange getTimeRangeMax() {
 	    final List<CalendarRange> ranges = new ArrayList<CalendarRange>();
 
 	    for (final ProductTree productTree : productTrees) {
     	    final List<ProductAttributes> products = productTree.flatten();
     	    
     	    for (final ProductAttributes product : products) {
     	        ranges.add(product.timeRange);
     	    }
 	    }
 	    return CalendarRange.getMaximum(ranges);
 	}
 
 	public CalendarRange getTimeRangeMin() {
 	    final List<CalendarRange> ranges = new ArrayList<CalendarRange>();
 
 	    for (final ProductTree productTree : productTrees) {
     	    final List<ProductAttributes> products = productTree.flatten();
     	    
     	    for (final ProductAttributes product : products) {
     	        ranges.add(product.timeRange);
     	    }
 	    }
 	    return CalendarRange.getMinimum(ranges);
 	}
 	
 	public Map<Integer, String> getTsos() {
 		final Map<Integer, String> result = new HashMap<Integer, String>();
 		
 		for (ProductTree tree : productTrees) {
 			final String tsoName = dictionary.getTsos().get(tree.getTso());
 			result.put(tree.getTso(), tsoName);
 		}
 		return result;
 	}
 
 	public String getTitle() {
 		return dictionary.getTitle();
 	}
 
 	public String getxAxisLabel() {
 		return dictionary.getxAxisLabel();
 	}
 
 	public String getyAxisLabel() {
 		return dictionary.getyAxisLabel();
 	}
 
 	public void setDefaults(NavigationDefaults defaults) {
 		this.defaults = defaults;
 	}
 
     public boolean getIsDateScale() {
         return isDateScale;
     }
 
     public void setIsDateScale(boolean dateScale) {
         this.isDateScale = dateScale;
     }
 
     public List<Aspect> getAspects() {
         return aspects;
     }
 
     public List<String> getTimeRanges() {
         return timeRanges;
     }
 
 	public boolean getHasTimeSelection() {
 		return hasTimeSelection;
 	}
 
 	public void setHasTimeSelection(boolean hasTimeSelection) {
 		this.hasTimeSelection = hasTimeSelection;
 	}
 
 	public boolean getHasProductSelection() {
 		return hasProductSelection;
 	}
 
 	public void setHasProductSelection(boolean hasProductSelection) {
 		this.hasProductSelection = hasProductSelection;
 	}
 
 	public boolean getHasLineSelection() {
 		return hasLineSelection;
 	}
 
 	public void setHasLineSelection(boolean hasLineSelection) {
 		this.hasLineSelection = hasLineSelection;
 	}
 	
 	public NavigationDictionary getDictionary() {
 		return dictionary;
 	}
 	
 	public NavigationDictionary getLocalizations() {
 		return dictionary;
 	}

	public int getDecimals() {
		return decimals;
	}

	public void setDecimals(int decimals) {
		this.decimals = decimals;
	}
 	
 }
