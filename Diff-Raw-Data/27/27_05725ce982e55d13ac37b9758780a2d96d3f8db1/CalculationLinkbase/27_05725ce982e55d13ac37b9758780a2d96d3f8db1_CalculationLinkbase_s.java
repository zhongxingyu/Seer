 package xbrlcore.linkbase;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import xbrlcore.constants.GeneralConstants;
 import xbrlcore.exception.XBRLException;
 import xbrlcore.taxonomy.Concept;
 import xbrlcore.taxonomy.DiscoverableTaxonomySet;
 import xbrlcore.xlink.Arc;
 import xbrlcore.xlink.ExtendedLinkElement;
 import xbrlcore.xlink.Locator;
 
 /**
  * This class represents the calculation linkbase of a DTS. This linkbase
  * defines calculation rules between concepts. <br/><br/>
  * 
  * @author Daniel Hamm
  */
 public class CalculationLinkbase extends Linkbase {
 
 	static final long serialVersionUID = -8646600566684543133L;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param dts
 	 *            Discoverable Taxonomy Set this linkbase belongs to.
 	 */
 	public CalculationLinkbase(DiscoverableTaxonomySet dts) {
 		super(dts);
 	}
 
 	/**
 	 * This method returns calculation rules for a certain concept. The key of
 	 * the returned map is the according concept of the calculation, the value
 	 * is the weight attribute determining the calculation rule. If there is no
 	 * rule defined, an empty Map is returned.
 	 * 
 	 * @param concept
 	 *            Concept for which the calculation rules shall be obtained.
 	 * @param extendedLinkRole
 	 *            Extended link role from which calculation rules shall be
 	 *            obtained.
 	 * @return A Map with concepts as keys and their according weight attribute
 	 *         (calc. rule) as values.
 	 */
 	public Map getCalculations(Concept concept, String extendedLinkRole)
 			throws XBRLException {
 		Map returnMap = new HashMap();
 
 		ExtendedLinkElement ex = getExtendedLinkElementFromBaseSet(concept,
 				extendedLinkRole);
 		if (ex == null) {
 			/* The concept is not part of the extended link role */
 			return returnMap;
 		}
 
 		List arcList = getTargetArcsFromExtendedLinkElement(ex,
 				GeneralConstants.XBRL_SUMMATION_ITEM_ARCROLE, extendedLinkRole);
 
 		for (int i = 0; i < arcList.size(); i++) {
 			Arc currArc = (Arc) arcList.get(i);
 			float currWeightAttribute = currArc.getWeightAttribute();
 			Concept currTargetConcept = ((Locator) currArc.getTargetElement())
 					.getConcept();
 			returnMap.put(currTargetConcept, new Float(currWeightAttribute));
 		}
 		return returnMap;
 	}
 
}
