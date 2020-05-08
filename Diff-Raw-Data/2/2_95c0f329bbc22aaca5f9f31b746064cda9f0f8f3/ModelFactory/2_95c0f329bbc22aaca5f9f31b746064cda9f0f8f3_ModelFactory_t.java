 package uk.ac.ed.inf.Metabolic.paxexport;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.biopax.paxtools.impl.level3.CellularLocationVocabularyImpl;
 import org.biopax.paxtools.impl.level3.Level3FactoryImpl;
 import org.biopax.paxtools.impl.level3.SmallMoleculeImpl;
 import org.biopax.paxtools.model.BioPAXLevel;
 import org.biopax.paxtools.model.Model;
 import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
 import org.biopax.paxtools.model.level3.BiochemicalReaction;
 import org.biopax.paxtools.model.level3.Catalysis;
 import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
 import org.biopax.paxtools.model.level3.ChemicalStructure;
 import org.biopax.paxtools.model.level3.Complex;
 import org.biopax.paxtools.model.level3.Control;
 import org.biopax.paxtools.model.level3.ControlType;
 import org.biopax.paxtools.model.level3.Direction;
 import org.biopax.paxtools.model.level3.Entity;
 import org.biopax.paxtools.model.level3.Level3Element;
 import org.biopax.paxtools.model.level3.Level3Factory;
 import org.biopax.paxtools.model.level3.Named;
 import org.biopax.paxtools.model.level3.PhysicalEntity;
 import org.biopax.paxtools.model.level3.Protein;
 import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
 import org.biopax.paxtools.model.level3.RelationshipXref;
 import org.biopax.paxtools.model.level3.StepDirection;
 import org.biopax.paxtools.model.level3.Stoichiometry;
 import org.biopax.paxtools.model.level3.UnificationXref;
 import org.biopax.paxtools.model.level3.XReferrable;
 import org.biopax.paxtools.model.level3.Xref;
 import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
 import org.pathwayeditor.contextadapter.toolkit.ndom.INdomModel;
 
 import uk.ac.ed.inf.Metabolic.ndomAPI.ICompartment;
 import uk.ac.ed.inf.Metabolic.ndomAPI.ICompound;
 import uk.ac.ed.inf.Metabolic.ndomAPI.IMacromolecule;
 import uk.ac.ed.inf.Metabolic.ndomAPI.IModel;
 import uk.ac.ed.inf.Metabolic.ndomAPI.IMolecule;
 import uk.ac.ed.inf.Metabolic.ndomAPI.IReaction;
 import uk.ac.ed.inf.Metabolic.ndomAPI.IRelation;
 
 /**
  * @author Anatoly Sorokin
  * 
  */
 /**
  * @author Anatoly Sorokin
  *
  */
 public class ModelFactory {
 
 	public static final String GO_MOL_FUNC_ID = "0003674";
 	public static final String GO_DB = "GO";
 	private static final String PUBCHEM_DB = "PubChem";
 	private static final String CHEBI_DB = "ChEBI";
 	public static final String INCHI_CHEMICAL_FORMAT = "InChI";
 	public static final String SMILES_CHEMICAL_FORMAT = "SMILES";
 	private static final String CID_DB = "KEGG";
 	private Level3Factory factory;
 	private IModel model;
 	private RelationshipTypeVocabulary goFunction;
 	private Model paxModel;
 	private Map<INdomModel, Entity> model2pax = new HashMap<INdomModel, Entity>();
 
 	public ModelFactory() {
 		factory = new Level3FactoryImpl();
 	}
 
 	public Model createPaxModel(IModel model)throws IllegalBioPAXArgumentException {
 		this.model = model;
 		paxModel=factory.createModel();
 		generateStaticContent();
 		generatePathway();
 		return paxModel;
 	}
 
 	/**
 	 * Create BioPax description of compartment.
 	 * 
 	 * @param c
 	 */
 	void addCompartment(ICompartment c) {
 		// TODO add compartment properties
 		CellularLocationVocabulary clv = new CellularLocationVocabularyImpl();
 		annotateEntity(c, clv);
 		clv.addTerm(c.getASCIIName());
 //		model2pax.put(c, clv);
 		for (ICompartment ch : c.getChildCompartments()) {
 			addCompartment(ch);
 		}
 		for (ICompound co : c.getCompoundList()) {
 			addCompound(co,clv);
 		}
 		for (IMacromolecule m : c.getMacromoleculeList()) {
 			addMacromolecule(m,clv);
 		}
 	}
 
 	void annotateEntity(INdomModel m, Level3Element elem) {
 		elem.setRDFId(m.getId());
 		elem.addComment(m.getASCIIName());
 		elem.addComment(m.getName());
 		elem.addComment(m.getDescription());
 		elem.addComment(m.getDetailedDescription());
 		getPaxModel().add(elem);
 	}
 
 	void nameEntity(INdomModel m, Named elem) {
 		elem.setStandardName(m.getASCIIName());
 		elem.setDisplayName(m.getName());
 	}
 	
 	/**
 	 * Create macromolecule annotation in BioPAX.
 	 * All macromolecules in Metabolic context are suppose to be Proteins and their complexes.
 	 * This is not true for rybozymes, for example, but at current stage we will create protein
 	 * entities for each macromolecule on the diagram.
 	 * @param m macromolecule NDOM element
 	 */
 	void addMacromolecule(IMacromolecule m,CellularLocationVocabulary clv) {
 		PhysicalEntity pe=null;
 		if(m.getSubunitList().size()+m.getCompoundList().size()>0){
 			pe=factory.createComplex();
 			Complex c=(Complex)pe;
 			annotateEntity(m, c);
 			nameEntity(m, c);
 			for(IMacromolecule su:m.getSubunitList()){
 				addSubunit(c,su);
 			}
 			for(ICompound csu:m.getCompoundList()){
 				addSubunit(c,csu);
 			}
 		}else{
 			pe=factory.createProtein();
 			Protein p=(Protein)pe;
 			annotateEntity(m, p);
 			nameEntity(m, p);
 			addUnificationXRef(p, "UniProt", m.getUniProt());
 		}
 		addRelationshipXRef(pe, GO_DB, m.getGOTerm(), goFunction);
 		pe.setCellularLocation(clv);
 		model2pax.put(m, pe);
 	}
 
 	/**
 	 * Add macromolecular subunit to complex. There is no way to represent nested complex structure in BioPAX so all annotation related to subcomplexes will be lost.
 	 * Macromolecule will inherit Subcellular localisation from parent complex.
 	 * @param parent topmost parent complex
 	 * @param su macromolecular subunit
 	 */
 	void addSubunit(Complex parent, IMacromolecule su) {
 		if(su.getSubunitList().size()+su.getCompoundList().size()>0){
 			for(IMacromolecule ssu:su.getSubunitList()){
				addSubunit(parent,ssu);
 			}
 			for(ICompound csu:su.getCompoundList()){
 				addSubunit(parent,csu);
 			}
 		}else{
 			PhysicalEntity pe=factory.createProtein();
 			Protein p=(Protein)pe;
 			annotateEntity(su, p);
 			nameEntity(su, p);
 			addUnificationXRef(p, "UniProt", su.getUniProt());
 			addRelationshipXRef(pe, GO_DB, su.getGOTerm(), goFunction);
 			pe.setCellularLocation(parent.getCellularLocation());
 			//all activation should point to top-most complex	
 			model2pax.put(su, parent);
 		}
 	}
 
 	/**
 	 * Add simple molecule component to complex. Simple molecule will inherit Subcellular localisation from parent complex.
 	 * @param parent
 	 * @param csu
 	 */
 	void addSubunit(Complex parent, ICompound csu) {
 		SmallMoleculeImpl sm = createCompound(csu);
 		sm.setCellularLocation(parent.getCellularLocation());
 		model2pax.put(csu, parent);
 		
 	}
 
 	void addCompound(ICompound co,CellularLocationVocabulary clv) {
 		SmallMoleculeImpl sm = createCompound(co);
 		sm.setCellularLocation(clv);
 		model2pax.put(co, sm);
 	}
 
 	SmallMoleculeImpl createCompound(ICompound co) {
 		SmallMoleculeImpl sm=(SmallMoleculeImpl) factory.createSmallMolecule();
 		annotateEntity(co, sm);
 		nameEntity(co,sm);
 		addStructure(sm, co.getSmiles(), SMILES_CHEMICAL_FORMAT);
 		addStructure(sm, co.getInChI(),INCHI_CHEMICAL_FORMAT);
 		addUnificationXRef(sm, CHEBI_DB, co.getChEBIId());
 		addUnificationXRef(sm, PUBCHEM_DB, co.getPubChemId());
 		addUnificationXRef(sm, CID_DB, co.getCID());
 		return sm;
 	}
 
 	void addUnificationXRef(XReferrable sm, String db, String id) {
 		if(id!=null && id.trim().length()>0){
 			UnificationXref ux=factory.createUnificationXref();
 			ux.setRDFId(sm.getRDFId()+"_"+db);
 			ux.setDb(db);
 			ux.setId(id);
 			sm.addXref(ux);
 			getPaxModel().add(ux);
 		}
 	}
 	
 	void addRelationshipXRef(XReferrable sm, String db, String id,RelationshipTypeVocabulary rtv) {
 		if(id!=null && id.trim().length()>0){
 			RelationshipXref rx=factory.createRelationshipXref();
 			rx.setRDFId(sm.getRDFId()+"_"+db);
 			rx.setDb(db);
 			rx.setId(id);
 			rx.setRelationshipType(rtv);
 			sm.addXref(rx);
 			getPaxModel().add(rx);
 		}
 	}
 
 	void addStructure(SmallMoleculeImpl sm, String structure, String format) {
 		if(structure!=null && structure.trim().length()>0){
 			ChemicalStructure cs=factory.createChemicalStructure();
 			cs.setRDFId(sm.getRDFId()+"_"+format);
 			cs.setStructureFormat(format);
 			cs.setStructureData(structure);
 			getPaxModel().add(cs);
 			sm.addStructure(cs);
 		}
 	}
 
 	/**
 	 * Create Pathway element in BioPax model. It also create and add all
 	 * dependent elements to the paxModel.
 	 */
 	void generatePathway() {
 		addPathwayProps(model);
 		for (ICompartment c : model.getCompartmentList()) {
 			addCompartment(c);
 		}
 		for (IReaction r : model.getReactionList()) {
 			addReaction(r);
 		}
 
 	}
 
 	void addReaction(IReaction r) {
 		BiochemicalReaction br = factory.createBiochemicalReaction();
 		annotateEntity(r, br);
 		if (r.getECNumber() != null && r.getECNumber().trim().length() > 0) {
 			br.addECNumber(r.getECNumber());
 		}
 		for (IRelation subs : r.getSubstrateList()) {
 			PhysicalEntity pe = getRelationPE(subs);
 			br.addLeft(pe);
 			// if(subs.getStoichiometry()>1){
 			addStoich(br, subs, pe);
 			// }
 		}
 		for (IRelation prod : r.getProductList()) {
 			PhysicalEntity pe = getRelationPE(prod);
 			br.addRight(pe);
 			addStoich(br, prod, pe);
 		}
 		for (IRelation cat : r.getCatalystList()) {
 			PhysicalEntity pe = getRelationPE(cat);
 			Catalysis catE = factory.createCatalysis();
 			annotateEntity(cat, catE);
 			catE.addControlled(br);
 			catE.addController(pe);
 			if (r.isReversible()) {
 				catE.setDirection(Direction.REVERSIBLE);
 			} else {
 				catE.setDirection(Direction.IRREVERSIBLE_LEFT_TO_RIGHT);
 			}
 		}
 
 		for (IRelation act : r.getActivatorList()) {
 			ControlType ct = ControlType.ACTIVATION;
 			PhysicalEntity pe = getRelationPE(act);
 			createControl(br, act, ct, pe);
 		}
 		for (IRelation inh : r.getInhibitorList()) {
 			ControlType ct = ControlType.INHIBITION;
 			PhysicalEntity pe = getRelationPE(inh);
 			createControl(br, inh, ct, pe);
 
 		}
 		// TODO export parameters and kinetic law
 		br.addComment(r.getKineticLaw());
 		br.addComment(r.getParameters());
 		//Add reversibility information
 		if (!r.isReversible()) {
 			BiochemicalPathwayStep bps = factory.createBiochemicalPathwayStep();
 			bps.setRDFId(r.getId() + "_step");
 			bps.setStepConversion(br);
 			bps.setStepDirection(StepDirection.LEFT_TO_RIGHT);
 			getPaxModel().add(bps);
 		}
 	}
 
 	void createControl(BiochemicalReaction br, IRelation act, ControlType ct,
 			PhysicalEntity pe) {
 		Control cont = factory.createControl();
 		annotateEntity(act, cont);
 		cont.addControlled(br);
 		cont.addController(pe);
 		cont.setControlType(ct);
 	}
 
 	private PhysicalEntity getRelationPE(IRelation subs) {
 		IMolecule mol = subs.getMolecule();
 		PhysicalEntity pe = (PhysicalEntity) model2pax.get(mol);
 		return pe;
 	}
 
 	void addStoich(BiochemicalReaction br, IRelation subs, PhysicalEntity pe) {
 		Stoichiometry stoich = factory.createStoichiometry();
 		annotateEntity(subs, stoich);
 		stoich.setStoichiometricCoefficient(subs.getStoichiometry());
 		stoich.setPhysicalEntity(pe);
 		stoich.addComment(subs.getRole());
 		br.addParticipantStoichiometry(stoich);
 	}
 
 	void addPathwayProps(IModel m) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * generates elements of static content like Provenance etc.
 	 */
 	void generateStaticContent() {
 		goFunction=factory.createRelationshipTypeVocabulary();
 		goFunction.setRDFId("GO_FUNCTION");
 		goFunction.addTerm("GO Molecular function");
 		Xref xref=factory.createUnificationXref();
 		xref.setRDFId("GO_FUNCTION_ID");
 		xref.setDb(GO_DB);
 		xref.setId(GO_MOL_FUNC_ID);
 		getPaxModel().add(xref);
 		goFunction.addXref(xref);
 		getPaxModel().add(goFunction);
 		
 	}
 
 	public IModel getModel() {
 		return model;
 	}
 
 	public Model getPaxModel() {
 		return paxModel;
 	}
 
 	public BioPAXLevel getLevel() {
 		return factory.getLevel();
 	}
 
 }
