 package edu.ucsf.rbvi.structureViz2.internal.model;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyIdentifiable;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNode;
 
 import edu.ucsf.rbvi.structureViz2.internal.model.StructureManager.ModelType;
 
 /**
  * This class provides the implementation for the ChimeraModel, ChimeraChain, and ChimeraResidue
  * objects
  * 
  * @author scooter
  * 
  */
 public class ChimeraModel implements ChimeraStructuralObject {
 
 	private String name; // The name of this model
 	private ModelType type; // The type of the model
 	private int modelNumber; // The model number
 	private int subModelNumber; // The sub-model number
 
 	private Color modelColor = null; // The color of this model (from Chimera)
 	private Object userData = null; // User data associated with this model
 	private boolean selected = false; // The selected state of this model
 
 	private TreeMap<String, ChimeraChain> chainMap; // The list of chains
 	// private TreeMap<String, ChimeraResidue> residueMap; // The list of residues
 	private Map<CyIdentifiable, CyNetwork> cyObjects; // The list of Cytoscape objects
 	private HashSet<ChimeraResidue> funcResidues; // List of functional residues
 
 	/**
 	 * Constructor to create a model
 	 * 
 	 * @param name
 	 *            the name of this model
 	 * @param color
 	 *            the model Color
 	 * @param modelNumber
 	 *            the model number
 	 * @param subModelNumber
 	 *            the sub-model number
 	 */
 	public ChimeraModel(String name, ModelType type, int modelNumber, int subModelNumber) {
 		this.name = name;
 		this.type = type;
 		this.modelNumber = modelNumber;
 		this.subModelNumber = subModelNumber;
 
 		this.chainMap = new TreeMap<String, ChimeraChain>();
 		this.cyObjects = new HashMap<CyIdentifiable, CyNetwork>();
 		this.funcResidues = new HashSet<ChimeraResidue>();
 	}
 
 	/**
 	 * Constructor to create a model from the Chimera input line
 	 * 
 	 * @param inputLine
 	 *            Chimera input line from which to construct this model
 	 */
 	// invoked when listing models: listm type molecule; lists level molecule
 	// line = model id #0 type Molecule name 1ert
 	public ChimeraModel(String inputLine) {
 		this.name = ChimUtils.parseModelName(inputLine);
 		// TODO: [Optional] Write a separate method to get model type
 		if (name.startsWith("smiles")) {
 			this.type = ModelType.SMILES;
 		} else {
 			this.type = ModelType.PDB_MODEL;
 		}
 		this.modelNumber = ChimUtils.parseModelNumber(inputLine)[0];
 		this.subModelNumber = ChimUtils.parseModelNumber(inputLine)[1];
 
 		this.chainMap = new TreeMap<String, ChimeraChain>();
 		this.cyObjects = new HashMap<CyIdentifiable, CyNetwork>();
 		this.funcResidues = new HashSet<ChimeraResidue>();
 	}
 
 	/**
 	 * Add a residue to this model
 	 * 
 	 * @param residue
 	 *            to add to the model
 	 */
 	public void addResidue(ChimeraResidue residue) {
 		residue.setChimeraModel(this);
 		// residueMap.put(residue.getIndex(), residue);
 		String chainId = residue.getChainId();
 		if (chainId != null) {
 			addResidue(chainId, residue);
 		} else {
 			addResidue("_", residue);
 		}
 		// Put it in our map so that we can return it in order
 		// residueMap.put(residue.getIndex(), residue);
 	}
 
 	/**
 	 * Add a residue to a chain in this model. If the chain associated with chainId doesn't exist,
 	 * it will be created.
 	 * 
 	 * @param chainId
 	 *            to add the residue to
 	 * @param residue
 	 *            to add to the chain
 	 */
 	public void addResidue(String chainId, ChimeraResidue residue) {
 		ChimeraChain chain = null;
 		if (!chainMap.containsKey(chainId)) {
 			chain = new ChimeraChain(this.modelNumber, this.subModelNumber, chainId);
 			chain.setChimeraModel(this);
 			chainMap.put(chainId, chain);
 		} else {
 			chain = chainMap.get(chainId);
 		}
 		chain.addResidue(residue);
 	}
 
 	/**
 	 * Get the ChimeraModel (required for ChimeraStructuralObject interface)
 	 * 
 	 * @return ChimeraModel
 	 */
 	public ChimeraModel getChimeraModel() {
 		return this;
 	}
 
 	/**
 	 * Get the model color of this model
 	 * 
 	 * @return model color of this model
 	 */
 	public Color getModelColor() {
 		return this.modelColor;
 	}
 
 	/**
 	 * Set the color of this model
 	 * 
 	 * @param color
 	 *            Color of this model
 	 */
 	public void setModelColor(Color color) {
 		this.modelColor = color;
 	}
 
 	/**
 	 * Return the name of this model
 	 * 
 	 * @return model name
 	 */
 	public String getModelName() {
 		return name;
 	}
 
 	/**
 	 * Set the name of this model
 	 * 
 	 * @param name
 	 *            model name
 	 */
 	public void setModelName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Get the model number of this model
 	 * 
 	 * @return integer model number
 	 */
 	public int getModelNumber() {
 		return modelNumber;
 	}
 
 	/**
 	 * Set the model number of this model
 	 * 
 	 * @param modelNumber
 	 *            integer model number
 	 */
 	public void setModelNumber(int modelNumber) {
 		this.modelNumber = modelNumber;
 	}
 
 	/**
 	 * Get the sub-model number of this model
 	 * 
 	 * @return integer sub-model number
 	 */
 	public int getSubModelNumber() {
 		return subModelNumber;
 	}
 
 	/**
 	 * Set the sub-model number of this model
 	 * 
 	 * @param subModelNumber
 	 *            integer model number
 	 */
 	public void setSubModelNumber(int subModelNumber) {
 		this.subModelNumber = subModelNumber;
 	}
 
 	public ModelType getModelType() {
 		return type;
 	}
 
 	public void setModelType(ModelType type) {
 		this.type = type;
 	}
 
 	public HashSet<ChimeraResidue> getFuncResidues() {
 		return funcResidues;
 	}
 
 	public void setFuncResidues(List<String> residues) {
 		for (String residue : residues) {
 			for (ChimeraChain chain : getChains()) {
				if (residue.indexOf("-") > 0) {
					funcResidues.addAll(chain.getResidueRange(residue));
				} else {
					funcResidues.add(chain.getResidue(residue));
				}
 			}
 		}
 	}
 
 	/**
 	 * Get the user data for this model
 	 * 
 	 * @return user data
 	 */
 	public Object getUserData() {
 		return userData;
 	}
 
 	/**
 	 * Set the user data for this model
 	 * 
 	 * @param data
 	 *            user data to associate with this model
 	 */
 	public void setUserData(Object data) {
 		this.userData = data;
 	}
 
 	/**
 	 * Return the selected state of this model
 	 * 
 	 * @return the selected state
 	 */
 	public boolean isSelected() {
 		return selected;
 	}
 
 	/**
 	 * Set the selected state of this model
 	 * 
 	 * @param selected
 	 *            a boolean to set the selected state to
 	 */
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 
 	/**
 	 * Return the chains in this model as a List
 	 * 
 	 * @return the chains in this model as a list
 	 */
 	public List<ChimeraStructuralObject> getChildren() {
 		return new ArrayList<ChimeraStructuralObject>(chainMap.values());
 	}
 
 	/**
 	 * Return the chains in this model as a colleciton
 	 * 
 	 * @return the chains in this model
 	 */
 	public Collection<ChimeraChain> getChains() {
 		return chainMap.values();
 	}
 
 	/**
 	 * Get the number of chains in this model
 	 * 
 	 * @return integer chain count
 	 */
 	public int getChainCount() {
 		return chainMap.size();
 	}
 
 	/**
 	 * Get the list of chain names associated with this model
 	 * 
 	 * @return return the list of chain names for this model
 	 */
 	public Collection<String> getChainNames() {
 		return chainMap.keySet();
 	}
 
 	/**
 	 * Get the residues associated with this model
 	 * 
 	 * @return the list of residues in this model
 	 */
 	public Collection<ChimeraResidue> getResidues() {
 		Collection<ChimeraResidue> residues = new ArrayList<ChimeraResidue>();
 		for (ChimeraChain chain : getChains()) {
 			residues.addAll(chain.getResidues());
 		}
 		return residues;
 	}
 
 	/**
 	 * Get the number of residues in this model
 	 * 
 	 * @return integer residues count
 	 */
 	public int getResidueCount() {
 		int count = 0;
 		for (ChimeraChain chain : getChains()) {
 			count += chain.getResidueCount();
 		}
 		return count;
 	}
 
 	/**
 	 * Get a specific chain from the model
 	 * 
 	 * @param chain
 	 *            the ID of the chain to return
 	 * @return ChimeraChain associated with the chain
 	 */
 	public ChimeraChain getChain(String chain) {
 		if (chainMap.containsKey(chain)) {
 			return chainMap.get(chain);
 		}
 		return null;
 	}
 
 	/**
 	 * Return a specific residue based on its index
 	 * 
 	 * @param index
 	 *            of the residue to return
 	 * @return the residue associated with that index
 	 */
 	public ChimeraResidue getResidue(String chainId, String index) {
 		if (chainMap.containsKey(chainId)) {
 			return chainMap.get(chainId).getResidue(index);
 		}
 		return null;
 	}
 
 	/**
 	 * Checks if this model has selected children.
 	 */
 	public boolean hasSelectedChildren() {
 		if (selected) {
 			return true;
 		} else {
 			for (ChimeraChain chain : getChains()) {
 				if (chain.hasSelectedChildren()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Return the list of selected residues
 	 * 
 	 * @return all selected residues
 	 */
 	public List<ChimeraResidue> getSelectedResidues() {
 		List<ChimeraResidue> residueList = new ArrayList<ChimeraResidue>();
 		for (ChimeraChain chain : getChains()) {
 			if (selected) {
 				residueList.addAll(chain.getSelectedResidues());
 			} else {
 				residueList.addAll(getResidues());
 			}
 		}
 		return residueList;
 	}
 
 	/**
 	 * Returns the map of {@link CyIdentifiable} associated with this model.
 	 * 
 	 * @return Map of {@link CyIdentifiable} and their respective network.
 	 */
 	public Map<CyIdentifiable, CyNetwork> getCyObjects() {
 		return cyObjects;
 	}
 
 	/**
 	 * Checks if this model has any {@link CyIdentifiable} associated with it.
 	 */
 	public boolean hasCyObjects() {
 		if (cyObjects.size() > 0) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Add a new Cytoscape object associated with this model and the network it belongs to.
 	 * 
 	 * @param newObj
 	 *            {@link CyIdentifiable} associated with this model.
 	 * @param network
 	 *            Network the {@link CyIdentifiable} belongs to.
 	 */
 	public void addCyObject(CyIdentifiable newObj, CyNetwork network) {
 		if (newObj != null && network != null) {
 			cyObjects.put(newObj, network);
 		}
 	}
 
 	/**
 	 * Remove Cytoscape object associated with this model.
 	 * 
 	 * @param cyObj
 	 *            {@link CyIdentifiable} associated with this model.
 	 */
 	public void removeCyObjectName(CyIdentifiable cyObj) {
 		if (cyObjects.containsKey(cyObj)) {
 			cyObjects.remove(cyObj);
 		}
 	}
 
 	/**
 	 * Return the Chimera specification for this model.
 	 */
 	public String toSpec() {
 		if (subModelNumber == 0)
 			return ("#" + modelNumber);
 		return ("#" + modelNumber + "." + subModelNumber);
 	}
 
 	/**
 	 * Return a string representation for the model.
 	 */
 	// TODO: [Bug] Throws a NullPointerException null after network is deleted...
 	public String toString() {
 		String nodeName = "Cytoscape";
 		try {
 			if (cyObjects.size() == 1) {
 				CyIdentifiable cyObj = cyObjects.keySet().iterator().next();
 				CyNetwork network = cyObjects.get(cyObj);
 				if (network != null
 						&& (cyObj instanceof CyNetwork
 								|| (cyObj instanceof CyNode && network.containsNode((CyNode) cyObj)) || (cyObj instanceof CyEdge && network
 								.containsEdge((CyEdge) cyObj)))) {
 					nodeName += " " + network.getRow(cyObj).get(CyNetwork.NAME, String.class);
 				}
 			} else if (cyObjects.size() > 1) {
 				nodeName += " {";
 				for (CyIdentifiable cyObj : cyObjects.keySet()) {
 					CyNetwork network = cyObjects.get(cyObj);
 					if (network != null
 							&& (cyObj instanceof CyNetwork
 									|| (cyObj instanceof CyNode && network
 											.containsNode((CyNode) cyObj)) || (cyObj instanceof CyEdge && network
 									.containsEdge((CyEdge) cyObj)))) {
 						nodeName += network.getRow(cyObj).get(CyNetwork.NAME, String.class) + ",";
 					}
 				}
 				nodeName = nodeName.substring(0, nodeName.length() - 1) + "}";
 			}
 		} catch (Exception ex) {
 			nodeName = null;
 			// ignore
 		}
 		if (nodeName == null) {
 			nodeName = "Cytoscape {none}";
 		}
 
 		String displayName = name;
		// TODO: [Optional] Show model name first and then Cytoscape objects associated with it
 		// TODO: [Optional] Shorten model names in the MND
 		// if (name.length() > 14)
 		// displayName = name.substring(0, 13) + "...";
 		if (getChainCount() > 0) {
 			return (nodeName + "[Model " + toSpec() + " " + displayName + " (" + getChainCount()
 					+ " chains, " + getResidueCount() + " residues)]");
 		} else if (getResidueCount() > 0) {
 			return (nodeName + "[Model " + toSpec() + " " + displayName + " (" + getResidueCount() + " residues)]");
 		} else {
 			return (nodeName + "[Model " + toSpec() + " " + displayName + "]");
 		}
 
 	}
 }
