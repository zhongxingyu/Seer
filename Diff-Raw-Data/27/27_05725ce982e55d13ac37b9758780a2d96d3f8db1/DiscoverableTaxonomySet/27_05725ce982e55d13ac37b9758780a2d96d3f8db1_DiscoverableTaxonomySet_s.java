 package xbrlcore.taxonomy;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.jdom.Element;
 
 import xbrlcore.dimensions.Hypercube;
 import xbrlcore.exception.XBRLCoreException;
 import xbrlcore.linkbase.CalculationLinkbase;
 import xbrlcore.linkbase.DefinitionLinkbase;
 import xbrlcore.linkbase.LabelLinkbase;
 import xbrlcore.linkbase.PresentationLinkbase;
 
 /**
  * This class represents a discoverable taxonomy set (DTS).<br/><br/> A DTS
 * consists of mulitple taxonomies, which themselves consist of a (taxonomy)
  * schema file and different linkbases. For DTS rules of discovery, see XBRL
  * Spec. section 3. <br/><br/>
  * 
  * @author Daniel Hamm
  * 
  */
 public class DiscoverableTaxonomySet implements Serializable {
 
 	static final long serialVersionUID = 8343887320619608137L;
 
 	private TaxonomySchema topTaxonomy; /* name of the taxonomy */
 
 	private LabelLinkbase labelLinkbase;
 
 	private PresentationLinkbase presentationLinkbase;
 
 	private DefinitionLinkbase definitionLinkbase;
 
 	private CalculationLinkbase calculationLinkbase;
 
 	private Map taxonomyMap; /* all taxonomies from the DTS */
 
 	/**
 	 * Constructor.
 	 * 
 	 */
 	public DiscoverableTaxonomySet() {
 		taxonomyMap = new HashMap();
 	}
 
 	/**
 	 * This method adds a new (taxonomy) schema to the DTS.
 	 * 
 	 * @param taxonomySchema
 	 *            The new (taxonomy) schema.
 	 */
 	public void addTaxonomy(TaxonomySchema taxonomySchema) {
 		taxonomyMap.put(taxonomySchema.getName(), taxonomySchema);
 	}
 
 	/**
 	 * Returns a Concept object to a specific ID from the DTS. All the
 	 * taxonomies belonging to this DTS are scanned for the element.
 	 * 
 	 * @param id
 	 *            The ID for which the element shall be obtained.
 	 * @return The according Concept object from the DTS. If the concept is not
 	 *         found in any taxonomy of the DTS, null is returned.
 	 */
 	public Concept getConceptByID(String id) {
 		Iterator dtsIterator = taxonomyMap.keySet().iterator();
 		while (dtsIterator.hasNext()) {
 			String currTaxonomyName = (String) dtsIterator.next();
 			TaxonomySchema tmpTaxonomy = (TaxonomySchema) taxonomyMap
 					.get(currTaxonomyName);
 			Concept tmpElement = tmpTaxonomy.getConceptByID(id);
 			if (tmpElement != null) {
 				return tmpElement;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns a Concept object to a specific name from the DTS. All the
 	 * taxonomies belonging to this DTS are scanned for the element.
 	 * 
 	 * @param name
 	 *            The name for which the element shall be obtained.
 	 * @return The according Concept object from the DTS. If the concept is not
 	 *         found in any taxonomy of the DTS, null is returned.
 	 */
 	public Concept getConceptByName(String name) {
 		Iterator dtsIterator = taxonomyMap.keySet().iterator();
 		while (dtsIterator.hasNext()) {
 			String currTaxonomyName = (String) dtsIterator.next();
 			TaxonomySchema tmpTaxonomy = (TaxonomySchema) taxonomyMap
 					.get(currTaxonomyName);
 			Concept tmpElement = tmpTaxonomy.getConceptByName(name);
 			if (tmpElement != null) {
 				return tmpElement;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns a list of all the concepts belonging to this DTS (Set of Concept
 	 * objects).
 	 * 
 	 * @return Set with all the concepts of all taxonomies belonging to this
 	 *         DTS.
 	 */
 	public Set getConcepts() {
 		Set returnSet = new HashSet();
 		Iterator dtsIterator = taxonomyMap.keySet().iterator();
 		while (dtsIterator.hasNext()) {
 			String currTaxonomyName = (String) dtsIterator.next();
 			TaxonomySchema tmpTaxonomy = (TaxonomySchema) taxonomyMap
 					.get(currTaxonomyName);
 			returnSet.addAll(tmpTaxonomy.getConcepts());
 		}
 		return returnSet;
 	}
 
 	/**
 	 * Returns a set of all Concept objects belonging to a specific substitution
 	 * group of this DTS.
 	 * 
 	 * @param substitutionGroup
 	 *            The substitution group all returned concepts shall belong to.
 	 * @return Set of Concept objects of a certain substitution group.
 	 */
 	public Set getConceptBySubstitutionGroup(String substitutionGroup) {
 		Set returnSet = new HashSet();
 		Iterator dtsIterator = taxonomyMap.keySet().iterator();
 		while (dtsIterator.hasNext()) {
 			String currTaxonomyName = (String) dtsIterator.next();
 			TaxonomySchema tmpTaxonomy = (TaxonomySchema) taxonomyMap
 					.get(currTaxonomyName);
 			returnSet.addAll(tmpTaxonomy
 					.getConceptBySubstitutionGroup(substitutionGroup));
 		}
 		return returnSet;
 	}
 
 	/**
 	 * Returns the schema element which the member of a typed dimension can be
 	 * validated against.
 	 * 
 	 * @param typedDimensionElement
 	 *            The typed dimension element for which the according schema
 	 *            element is desired.
 	 * @return The schema element validating members of the given typed
 	 *         dimension.
 	 */
 	public Element getElementForTypedDimension(Concept typedDimensionElement)
 			throws XBRLCoreException {
 		if (typedDimensionElement == null)
 			return null;
 		if (!typedDimensionElement.isTypedDimension()) {
 			throw new XBRLCoreException("Cannot get schema element for "
 					+ typedDimensionElement.getName()
 					+ " since this is no typed dimension");
 		}
 
 		String typedDomainRefAttribute = typedDimensionElement
 				.getTypedDomainRef();
 		/* typedDomainRefAttribute is of form <tax.name># <id> */
 		String taxonomyName = null;
 		String elementID = null;
 		if (typedDomainRefAttribute.indexOf("#") > 0) {
 			taxonomyName = typedDomainRefAttribute.substring(0,
 					typedDomainRefAttribute.indexOf("#"));
 			elementID = typedDomainRefAttribute.substring(
 					typedDomainRefAttribute.indexOf("#") + 1,
 					typedDomainRefAttribute.length());
 		} else {
 			taxonomyName = typedDimensionElement.getTaxonomySchemaName();
 			elementID = typedDomainRefAttribute.substring(1,
 					typedDomainRefAttribute.length());
 		}
 
 		TaxonomySchema taxSchema = getTaxonomySchema(taxonomyName);
 		if (taxSchema == null) {
 			throw new XBRLCoreException("Cannot find schema element for "
 					+ typedDimensionElement.getName() + ": Unknown taxonomy "
 					+ taxonomyName);
 		}
 		Concept cElementForTypedDimension = taxSchema.getConceptByID(elementID);
 		if (cElementForTypedDimension == null) {
 			throw new XBRLCoreException("Cannot find schema element for "
 					+ typedDimensionElement.getName() + ": Unknown element "
 					+ elementID);
 		}
 		Element elementForTypedDimension = new Element(
 				cElementForTypedDimension.getName());
 		elementForTypedDimension.setNamespace(taxSchema.getNamespace());
 
 		return elementForTypedDimension;
 	}
 
 	/**
 	 * Returns all the concepts (Set of Concept objects) of this DTS
 	 * representing dimensional elements.
 	 * 
 	 * @return Set of Concept objects of substitution group
 	 *         xbrldt:dimensionItem.
 	 */
 	public Set getDimensionConceptSet() {
 		if (definitionLinkbase == null) {
 			return null;
 		} else {
 			return definitionLinkbase.getDimensionConceptSet();
 		}
 	}
 
 	/**
 	 * Returns the hypercube to a specific concept.
 	 * 
 	 * @param concept
 	 *            Concept to which the hypercube shall be obtained.
 	 * @return The hypercube matching to the given element.
 	 */
 	public Hypercube getHypercube(Concept concept) {
 		if (definitionLinkbase == null) {
 			return null;
 		}
 		return definitionLinkbase.getHypercube(concept);
 	}
 
 	/**
 	 * Returns a specific (taxonomy) schema object.
 	 * 
 	 * @param name
 	 *            Name of the (taxonomy) schema object.
 	 * @return According TaxonomySchema object.
 	 */
 	public TaxonomySchema getTaxonomySchema(String name) {
 		return (TaxonomySchema) taxonomyMap.get(name);
 	}
 
 	/**
 	 * @return Returns the label linkbase of the DTS.
 	 */
 	public LabelLinkbase getLabelLinkbase() {
 		return labelLinkbase;
 	}
 
 	/**
 	 * @param linkbase
 	 *            Label linkbase of the DTS.
 	 */
 	public void setLabelLinkbase(LabelLinkbase linkbase) {
 		labelLinkbase = linkbase;
 	}
 
 	/**
 	 * @return Returns presentation linkbase of the DTS.
 	 */
 	public PresentationLinkbase getPresentationLinkbase() {
 		return presentationLinkbase;
 	}
 
 	/**
 	 * @param linkbase
 	 *            Presentation linkbase of the DTS.
 	 */
 	public void setPresentationLinkbase(PresentationLinkbase linkbase) {
 		presentationLinkbase = linkbase;
 	}
 
 	/**
 	 * @return Returns the definition linkbase of the DTS.
 	 */
 	public DefinitionLinkbase getDefinitionLinkbase() {
 		return definitionLinkbase;
 	}
 
 	/**
 	 * @param linkbase
 	 *            Definition linkbase of the DTS.
 	 */
 	public void setDefinitionLinkbase(DefinitionLinkbase linkbase) {
 		definitionLinkbase = linkbase;
 	}
 
 	/**
 	 * @return (Taxonomy) schema which has been the beginning of the DTS
 	 *         discovery process of this DTS. For details on this discovery
 	 *         process, see Spec. section 3.
 	 */
 	public TaxonomySchema getTopTaxonomy() {
 		return topTaxonomy;
 	}
 
 	/**
 	 * @param schema
 	 *            (Taxonomy) schema which has been the beginning of the DTS
 	 *            discovery process of this DTS. For details on this discovery
 	 *            process, see Spec. section 3.
 	 */
 	public void setTopTaxonomy(TaxonomySchema schema) {
 		topTaxonomy = schema;
 	}
 
 	/**
 	 * @return Returns map with all the taxonomy schema objects belonging to
 	 *         this DTS. The key is the name, the value is the according
 	 *         TaxonomySchema object.
 	 */
 	public Map getTaxonomyMap() {
 		return taxonomyMap;
 	}
 
 	/**
 	 * @return Returns the calculationLinkbase.
 	 */
 	public CalculationLinkbase getCalculationLinkbase() {
 		return calculationLinkbase;
 	}
 
 	/**
 	 * @param calcLinkbase
 	 *            The calculationLinkbase to set.
 	 */
 	public void setCalculationLinkbase(CalculationLinkbase calcLinkbase) {
 		this.calculationLinkbase = calcLinkbase;
 	}
 }
