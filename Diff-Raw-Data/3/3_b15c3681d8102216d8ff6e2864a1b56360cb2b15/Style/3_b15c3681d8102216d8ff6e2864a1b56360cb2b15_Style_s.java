 package html2windows.css;
 
 import java.util.Comparator;
 import java.util.TreeSet;
 
 import html2windows.dom.Element;
 
 /**Style
  * style simulate CSS's style. It contains a set of CSSRuleSets.
  * You can add CSSRuleSet to Style or set property to CSSRuleSet in Style.
  * You can also get properties from CSSRuleSet in Style in the order of priority.
  * 
  * @author Jason Kuo
  * 
  */
 public class Style {
 
 	/*
 	 *	MAX_PRIORITY 	define CSSRuleSet's max priority as 5
 	 *	element 		element that own this style
 	 * 	comparator		comparator that compare CSSRuleSet with priority
 	 *	set				TreeSet of CSSRuleSet 
 	 */
 	private static final int MAX_PRIORITY=5;
 	private Element element;
 	private Comparator<CSSRuleSet> comparator = new CssRuleSetComparator();
 	private TreeSet <CSSRuleSet> set = new TreeSet<CSSRuleSet>(comparator);
 	
 	/**constructor of style
 	 * create style and add to CSSRuleSet
 	 * @param element 		parent node
 	 */
 	public Style(Element element){
 		this.element=element;
 		CSSRuleSet ruleSet=new CSSRuleSet(MAX_PRIORITY);
 		set.add(ruleSet);
 	}
 	
     /**set CssRuleSet's property
      * set CssRuleSet's inline property
      * @param propertyName 	inserted property name
      * @param value		 	inserted property value
      */
     public void setProperty(String propertyName, String value){
         set.first().setProperty(propertyName,value);
     }
     
     /**get property's value
      * get property value according to the order of treeSet(property)
      * @param propertyName		property name
      * @return					property's value
      */
     public String getProperty(String propertyName){
     	String value=null;
     	for(CSSRuleSet ruleSet : set){
     		value=ruleSet.getProperty(propertyName);
     		if(value!=null)
     			return value;
     	}
         return null;
     }
   
     /**add CSSRuleSet
      * add new CSSRuleSet to Style
      * @param cssRuleSet		CSSRuleSet to be added
      */
     public void addCSSRuleSet(CSSRuleSet cssRuleSet){
     	set.add(cssRuleSet);
     }
     
    /**get element that own this style
      * @return			owner of the Style 
      */
     public Element getElement(){
         return element;
     }
     
     /**CSSRuleSet's comparator
      * compare CSSRuleSet with its priority
      */
     public class CssRuleSetComparator implements Comparator<CSSRuleSet>{
 		@Override
 		public int compare(CSSRuleSet o1, CSSRuleSet o2) {
 			return (o1.getPriority()>o2.getPriority())?1:-1;
 		}
 	}
 }
