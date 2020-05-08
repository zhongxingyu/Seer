 package edu.ucsf.rbvi.structureViz2.internal.model;
 
 import java.awt.Color;
 import java.awt.Paint;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyEdge.Type;
 import org.cytoscape.model.CyIdentifiable;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNetworkFactory;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.view.model.CyNetworkView;
 import org.cytoscape.view.model.CyNetworkViewManager;
 import org.cytoscape.view.model.View;
 import org.cytoscape.view.presentation.property.BasicVisualLexicon;
 import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
 import org.cytoscape.view.vizmap.VisualMappingManager;
 import org.cytoscape.view.vizmap.VisualStyle;
 import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
 
 // TODO: [Optional] No dist edges between ligands and others since we only consider distance between C_alphas
 public class RINManager {
 
 	private StructureManager structureManager;
 	private ChimeraManager chimeraManager;
 
 	// Edge types
 	private static final String HBONDEDGE = "hbond";
 	private static final String CONTACTEDGE = "contact";
 	private static final String CLASHEDGE = "clash";
 	private static final String COMBIEDGE = "combi";
 	private static final String DISTEDGE = "distance";
 	private static final String BBEDGE = "backbone";
 	// private static final String SUBTYPEDELIM1 = "_";
 	private static final String SUBTYPEDELIM = "_";
 
 	// Edge attributes
 	private static final String DISTANCE_ATTR = "Distance";
 	private static final String OVERLAP_ATTR = "Overlap";
 	private static final String INTSUBTYPE_ATTR = "InteractionSubtype";
 	private static final String INTATOMS_ATTR = "InteractingAtoms";
 	private static final String NUMINT_ATTR = "NumberInteractions";
 	// Node attributes
 	private static final String SMILES_ATTR = "SMILES";
 	private static final String SEED_ATTR = "SeedResidues";
 	private static final String CHAIN_ATTR = "ResChain";
 	private static final String TYPE_ATTR = "ResType";
 	private static final String RESINDEX_ATTR = "ResIndex";
 
 	public static final Map<String, String> residueAttrCommandMap = new HashMap<String, String>();
 
 	static {
 		residueAttrCommandMap.put("SecondaryStructure", "");
 		residueAttrCommandMap.put("Coordinates", "");
 	}
 
 	public RINManager(StructureManager structureManager) {
 		this.structureManager = structureManager;
 		this.chimeraManager = structureManager.getChimeraManager();
 	}
 
 	public void includeContacts(CyNetwork rin, Map<String, CyNode> nodeMap,
 			int includeInteractions, boolean ignoreWater, boolean removeRedContacts,
 			double overlapCutoff, double hbondAllowance, int bondSep) {
 		// System.out.println("Getting contacts");
 		List<String> replyList = chimeraManager.sendChimeraCommand(
 				getContactCommand(includeInteractions, overlapCutoff, hbondAllowance, bondSep),
 				true);
 		if (replyList != null) {
 			parseContactReplies(replyList, rin, nodeMap, ignoreWater, removeRedContacts,
 					CONTACTEDGE);
 		}
 	}
 
 	public void includeClashes(CyNetwork rin, Map<String, CyNode> nodeMap, int includeInteractions,
 			boolean ignoreWater, boolean removeRedContacts, double overlapCutoff,
 			double hbondAllowance, int bondSep) {
 		// System.out.println("Getting clashes");
 		List<String> replyList = chimeraManager.sendChimeraCommand(
 				getContactCommand(includeInteractions, overlapCutoff, hbondAllowance, bondSep),
 				true);
 		if (replyList != null) {
 			parseContactReplies(replyList, rin, nodeMap, ignoreWater, removeRedContacts, CLASHEDGE);
 		}
 	}
 
 	public void includeHBonds(CyNetwork rin, Map<String, CyNode> nodeMap, int includeInteractions,
 			boolean ignoreWater, boolean removeRedContacts, boolean addHydrogens,
 			boolean relaxHBonds, double angleSlop, double distSlop) {
 		// System.out.println("Getting hydrogen bonds");
 		List<String> replyList = chimeraManager.sendChimeraCommand(
 				getHBondCommand(includeInteractions, relaxHBonds, angleSlop, distSlop), true);
 		if (replyList != null) {
 			parseHBondReplies(replyList, rin, nodeMap, ignoreWater, removeRedContacts, addHydrogens);
 		}
 	}
 
 	public void includeConnectivity(CyNetwork rin) {
 		// System.out.println("Getting connectivity");
 		List<String> replyList = chimeraManager.sendChimeraCommand("list physicalchains", true);
 		if (replyList != null) {
 			parseConnectivityReplies(replyList, rin);
 		}
 	}
 
 	public void includeDistances(CyNetwork rin, Map<String, CyNode> nodeMap,
 			int includeInteractions, boolean ignoreWater, boolean removeRedContacts,
 			double distCutoff) {
 		// System.out.println("Getting distances");
 		List<String> replyList = chimeraManager.sendChimeraCommand(
 				getDistanceCommand(includeInteractions), true);
 		if (replyList != null) {
 			parseDistanceReplies(replyList, rin, nodeMap, ignoreWater, removeRedContacts,
 					distCutoff, includeInteractions);
 		}
 	}
 
 	public void addCombinedEdges(CyNetwork rin) {
 		// System.out.println("Getting combined edges");
 		if (rin == null || rin.getEdgeCount() == 0) {
 			return;
 		}
 		List<CyNode> nodes = rin.getNodeList();
 		for (int i = 0; i < rin.getNodeCount(); i++) {
 			CyNode source = nodes.get(i);
 			for (int j = i + 1; j < rin.getNodeCount(); j++) {
 				CyNode target = nodes.get(j);
 				List<CyEdge> edges = rin.getConnectingEdgeList(source, target, Type.ANY);
 				if (edges.size() > 0) {
 					CyEdge edge = rin.addEdge(source, target, true);
 					rin.getRow(edge).set(
 							CyNetwork.NAME,
 							rin.getRow(source).get(CyNetwork.NAME, String.class) + " (" + COMBIEDGE
 									+ ") " + rin.getRow(target).get(CyNetwork.NAME, String.class));
 					rin.getRow(edge).set(CyEdge.INTERACTION, COMBIEDGE);
 					rin.getRow(edge)
 							.set(INTSUBTYPE_ATTR, COMBIEDGE + " all" + SUBTYPEDELIM + "all");
 					rin.getRow(edge).set(NUMINT_ATTR, edges.size());
 					// rin.getRow(edge).set(INTATOMS_ATTR, "");
 
 				}
 			}
 		}
 	}
 
 	private String getContactCommand(int includeInteractions, double overlapCutoff,
 			double hbondAllowance, int bondSep) {
 		String atomspec1 = "";
 		String atomspec2 = "";
 		// "Within selection"
 		if (includeInteractions == 0) {
 			// among the specified atoms
 			atomspec1 = "sel";
 			atomspec2 = "test self";
 		}
 		// "Between selection and all other atoms"
 		else if (includeInteractions == 1) {
 			// between the specified atoms and all other atoms
 			atomspec1 = "sel";
 			atomspec2 = "test other";
 		}
 		// "All of the above"
 		else if (includeInteractions == 2) {
 			// intra-model interactions between the specified atoms and all
 			// other atoms
 			atomspec1 = "sel";
 			atomspec2 = "test model";
 		}
 		// "Between models"
 		// else if (includeInteracions.getSelectedValue() ==
 		// interactionArray[3]) {
 		// // between the specified atoms and all other atoms
 		// atomspec1 = "#" + chimeraManager.getChimeraModel().getModelNumber();
 		// atomspec2 = "test other";
 		// }
 		// // Between and within all models
 		// else {
 		// atomspec1 = "#*";
 		// atomspec2 = "test self";
 		// }
 		// Create the command
 		String command = "findclash " + atomspec1
 				+ " makePseudobonds false log true namingStyle command overlapCutoff "
 				+ overlapCutoff + " hbondAllowance " + hbondAllowance + " bondSeparation "
 				+ bondSep + " " + atomspec2;
 		return command;
 	}
 
 	private String getHBondCommand(int includeInteractions, boolean relaxHBonds, double angleSlop,
 			double distSlop) {
 		// for which atoms to find hydrogen bonds
 		String atomspec = "";
 		// intermodel: whether to look for H-bonds between models
 		// intramodel: whether to look for H-bonds within models.
 		String modelrestr = "";
 		// "Within selection"
 		if (includeInteractions == 0) {
 			// Limit H-bond detection to H-bonds with both atoms selected
 			atomspec = "selRestrict both";
 			modelrestr = "intramodel true intermodel true";
 		}
 		// "Between selection and all atoms"
 		else if (includeInteractions == 1) {
 			// Limit H-bond detection to H-bonds with at least one atom selected
 			atomspec = "selRestrict any";
 			modelrestr = "intramodel false intermodel true";
 		}
 		// "Within selection and all atoms"
 		else if (includeInteractions == 2) {
 			// Limit H-bond detection to H-bonds with at least one atom selected
 			atomspec = "selRestrict any";
 			modelrestr = "intramodel true intermodel true";
 		}
 		// "Between models"
 		// else if (includeInteracions.getSelectedValue() ==
 		// interactionArray[3]) {
 		// // Restrict H-bond detection to the specified model
 		// atomspec = "spec #*";
 		// modelrestr = "intramodel false intermodel true";
 		// }
 		// // Between and within models
 		// else {
 		// atomspec = "spec #*";
 		// modelrestr = "intramodel true intermodel true";
 		// }
 		String command = "findhbond " + atomspec + " " + modelrestr
 				+ " makePseudobonds false log true namingStyle command";
 		if (relaxHBonds) {
 			command += " relax true distSlop " + distSlop + " angleSlop " + angleSlop;
 		}
 		return command;
 	}
 
 	private String getDistanceCommand(int includeInteractions) {
 		String atomspec = "";
 		// "Within selection"
 		if (includeInteractions == 0) {
 			// among the specified atoms
 			atomspec = "@CA&sel";
 		}
 		// "Between selection and all other atoms" or "All of the above"
 		else if (includeInteractions == 1 || includeInteractions == 2) {
 			// between the specified atoms and all other atoms
 			atomspec = "@CA";
 		}
 
 		// Create the command
 		String command = "list distmat " + atomspec;
 		return command;
 	}
 
 	/**
 	 * Clash replies look like: *preamble* *header line* *clash lines* where preamble is: Allowed
 	 * overlap: -0.4 H-bond overlap reduction: 0 Ignore contacts between atoms separated by 4 bonds
 	 * or less Ignore intra-residue contacts 44 contacts and the header line is: atom1 atom2 overlap
 	 * distance and the clash lines look like: :2470.A@N :323.A@OD2 -0.394 3.454
 	 */
 	private List<CyEdge> parseContactReplies(List<String> replyLog, CyNetwork rin,
 			Map<String, CyNode> nodeMap, boolean ignoreWater, boolean removeRedContacts,
 			String edgeType) {
 		// Scan for our header line
 		boolean foundHeader = false;
 		int index = 0;
 		for (index = 0; index < replyLog.size(); index++) {
 			String str = replyLog.get(index);
 
 			if (str.trim().startsWith("atom1")) {
 				foundHeader = true;
 				break;
 			}
 		}
 		if (!foundHeader)
 			return null;
 
 		Map<CyEdge, Double> distanceMap = new HashMap<CyEdge, Double>();
 		Map<CyEdge, Double> overlapMap = new HashMap<CyEdge, Double>();
 		for (++index; index < replyLog.size(); index++) {
 			// System.out.println(replyLog.get(index));
 			String[] line = replyLog.get(index).trim().split("\\s+");
 			if (line.length != 4)
 				continue;
 
 			CyEdge edge = createEdge(rin, nodeMap, ignoreWater, removeRedContacts, line[0],
 					line[1], edgeType);
 			if (edge == null) {
 				continue;
 			}
 
 			// We want the smallest distance
 			updateMap(distanceMap, edge, line[3], -1);
 			// We want the largest overlap
 			updateMap(overlapMap, edge, line[2], 1);
 		}
 
 		// OK, now update the edge attributes we want
 		for (CyEdge edge : distanceMap.keySet()) {
 			rin.getRow(edge).set(DISTANCE_ATTR, distanceMap.get(edge));
 			rin.getRow(edge).set(OVERLAP_ATTR, overlapMap.get(edge));
 		}
 
 		return new ArrayList<CyEdge>(distanceMap.keySet());
 	}
 
 	// H-bonds (donor, acceptor, hydrogen, D..A dist, D-H..A dist):
 	/**
 	 * Finding acceptors in model '1tkk' Building search tree of acceptor atoms Finding donors in
 	 * model '1tkk' Matching donors in model '1tkk' to acceptors Finding intermodel H-bonds Finding
 	 * intramodel H-bonds Constraints relaxed by 0.4 angstroms and 20 degrees Models used: #0 1tkk
 	 * H-bonds (donor, acceptor, hydrogen, D..A dist, D-H..A dist): ARG 24.A NH1 GLU 2471.A OE1 no
 	 * hydrogen 3.536 N/A LYS 160.A NZ GLU 2471.A O no hydrogen 2.680 N/A LYS 162.A NZ ALA 2470.A O
 	 * no hydrogen 3.022 N/A LYS 268.A NZ GLU 2471.A O no hydrogen 3.550 N/A ILE 298.A N GLU 2471.A
 	 * OE2 no hydrogen 3.141 N/A ALA 2470.A N THR 135.A OG1 no hydrogen 2.814 N/A ALA 2470.A N ASP
 	 * 321.A OD1 no hydrogen 2.860 N/A ALA 2470.A N ASP 321.A OD2 no hydrogen 3.091 N/A ALA 2470.A N
 	 * ASP 323.A OD1 no hydrogen 2.596 N/A ALA 2470.A N ASP 323.A OD2 no hydrogen 3.454 N/A GLU
 	 * 2471.A N SER 296.A O no hydrogen 2.698 N/A HOH 2541.A O GLU 2471.A OE1 no hydrogen 2.746 N/A
 	 * HOH 2577.A O GLU 2471.A O no hydrogen 2.989 N/A
 	 */
 	private List<CyEdge> parseHBondReplies(List<String> replyLog, CyNetwork rin,
 			Map<String, CyNode> nodeMap, boolean ignoreWater, boolean removeRedContacts,
 			boolean addHydrogens) {
 		// Scan for our header line
 		boolean foundHeader = false;
 		int index = 0;
 		for (index = 0; index < replyLog.size(); index++) {
 			String str = replyLog.get(index);
 			if (str.trim().startsWith("H-bonds")) {
 				foundHeader = true;
 				break;
 			}
 		}
 		if (!foundHeader) {
 			return null;
 		}
 
 		Map<CyEdge, Double> distanceMap = new HashMap<CyEdge, Double>();
 		for (++index; index < replyLog.size(); index++) {
 			String[] line = replyLog.get(index).trim().split("\\s+");
 			if (line.length != 5 && line.length != 6)
 				continue;
 
 			CyEdge edge = createEdge(rin, nodeMap, ignoreWater, removeRedContacts, line[0],
 					line[1], HBONDEDGE);
 			if (edge == null) {
 				continue;
 			}
 			String distance = line[3];
 			if ((line[2].equals("no") && line[3].equals("hydrogen")) || addHydrogens) {
 				distance = line[4];
 			}
 			updateMap(distanceMap, edge, distance, -1); // We want the smallest
 														// distance
 		}
 
 		// OK, now update the edge attributes we want
 		for (CyEdge edge : distanceMap.keySet()) {
 			rin.getRow(edge).set(DISTANCE_ATTR, distanceMap.get(edge));
 		}
 
 		return new ArrayList<CyEdge>(distanceMap.keySet());
 	}
 
 	/**
 	 * Parse the connectivity information from Chimera. The data is of the form: physical chain
 	 * #0:283.A #0:710.A physical chain #0:283.B #0:710.B physical chain #0:283.C #0:710.C
 	 * 
 	 * We don't use this data to create new nodes -- only new edges. If two nodes are within the
 	 * same physical chain, we connect them with a "backbone/connected" edge
 	 */
 	private List<CyEdge> parseConnectivityReplies(List<String> replyLog, CyNetwork rin) {
 		List<CyEdge> edgeList = new ArrayList<CyEdge>();
 		List<ChimeraResidue[]> rangeList = new ArrayList<ChimeraResidue[]>();
 		for (String line : replyLog) {
 			String[] tokens = line.split(" ");
 			if (tokens.length != 4)
 				continue;
 			String start = tokens[2];
 			String end = tokens[3];
 
 			ChimeraResidue[] range = new ChimeraResidue[2];
 
 			// Get the residues from the reside spec
 			range[0] = ChimUtils.getResidue(start, chimeraManager);
 			range[1] = ChimUtils.getResidue(end, chimeraManager);
 			if (range[0] != null && range[1] != null) {
 				rangeList.add(range);
 			}
 		}
 
 		// For each node pair, figure out if the pair is connected
 		List<CyNode> nodes = rin.getNodeList();
 		for (int i = 0; i < nodes.size(); i++) {
 			CyNode node1 = nodes.get(i);
 			ChimeraResidue[] range = getRange(rangeList, node1, rin);
 			if (range == null) {
 				continue;
 			}
 			for (int j = i + 1; j < nodes.size(); j++) {
 				CyNode node2 = nodes.get(j);
 				if (inRange2(range, node1, node2, rin)) {
 					// These two nodes are connected
 					edgeList.add(createConnectivityEdge(rin, node1, node2));
 				}
 			}
 		}
 
 		// Now, make the edges based on whether any pair of nodes are in the
 		// same range
 		return edgeList;
 	}
 
 	/**
 	 * 
 	 * distmat #0:36.A@CA #0:37.A@CA 3.777 distmat #0:36.A@CA #0:38.A@CA 6.663
 	 * 
 	 * @param replyLog
 	 * @param rin
 	 * @param nodeMap
 	 * @return
 	 */
 	private List<CyEdge> parseDistanceReplies(List<String> replyLog, CyNetwork rin,
 			Map<String, CyNode> nodeMap, boolean ignoreWater, boolean removeRedContacts,
 			double distCutoff, int includeInteractions) {
 		List<String> selectedResidues = chimeraManager.getSelectedResidueSpecs();
 		List<CyEdge> distEdges = new ArrayList<CyEdge>();
 		for (int index = 0; index < replyLog.size(); index++) {
 			// System.out.println(replyLog.get(index));
 			String[] line = replyLog.get(index).trim().split("\\s+");
 			if (line.length != 4)
 				continue;
 
 			String distance = line[3];
 			// try to read distance and create an edge if distance between atoms smaller than cutoff
 			// special case of cutoff = 0: create all edges
 			try {
 				Double distNum = Double.parseDouble(distance);
 				String res1 = line[1].substring(0, line[1].indexOf("@"));
 				String res2 = line[2].substring(0, line[2].indexOf("@"));
 				// continue
 				// if distance is below cutoff or if cutoff is not set, i.e. equal to 0 (to retrieve
 				// all distance) and
 				// 1) if retrieve only for selected residues
 				// 2) if retrieve for selected and neighbors and the first residue is selected and
 				// the second is not
 				// 3) if retrieve for both selected and neighbors and the first residue is selected
 				if ((distCutoff == 0.0 || distNum <= distCutoff)
 						&& (includeInteractions == 0
 								|| (includeInteractions == 1
 										&& ((selectedResidues.contains(res1) && !selectedResidues
 												.contains(res2))) || (selectedResidues
 										.contains(res2) && !selectedResidues.contains(res1))) || (includeInteractions == 2 && (selectedResidues
 								.contains(res1) || selectedResidues.contains(res2))))) {
 					CyEdge edge = createEdge(rin, nodeMap, ignoreWater, removeRedContacts, line[1],
 							line[2], DISTEDGE);
 					if (edge == null) {
 						continue;
 					}
 					distEdges.add(edge);
 					rin.getRow(edge).set(DISTANCE_ATTR, distNum);
 				}
 			} catch (Exception ex) {
 				// ignore
 			}
 		}
 		return distEdges;
 	}
 
 	private CyEdge createEdge(CyNetwork rin, Map<String, CyNode> nodeMap, boolean ignoreWater,
 			boolean removeRedContacts, String sourceAlias, String targetAlias, String type) {
 		// Create our two nodes. Note that makeResidueNode also adds three
 		// attributes:
 		// 1) FunctionalResidues; 2) Seed; 3) SideChainOnly
 		CyNode source = createResidueNode(rin, nodeMap, ignoreWater, sourceAlias);
 		CyNode target = createResidueNode(rin, nodeMap, ignoreWater, targetAlias);
 		if (source == null || target == null) {
 			return null;
 		}
 		String interactingAtoms = sourceAlias + "," + targetAlias;
 		String sourceAtom = ChimUtils.getAtomName(sourceAlias);
 		String targetAtom = ChimUtils.getAtomName(targetAlias);
 		List<String> subtype = new ArrayList<String>();
 		subtype.add(ChimUtils.getIntSubtype(rin.getRow(source).get(CyNetwork.NAME, String.class),
 				sourceAtom));
 		subtype.add(ChimUtils.getIntSubtype(rin.getRow(target).get(CyNetwork.NAME, String.class),
 				targetAtom));
 		Collections.sort(subtype);
 		String interactionSubtype = type + " " + subtype.get(0) + SUBTYPEDELIM + subtype.get(1);
 
 		// Create our edge
 		CyEdge edge = null;
 		if (removeRedContacts && type.equals(HBONDEDGE)) {
 			List<CyEdge> existingEdges = rin.getConnectingEdgeList(source, target, Type.ANY);
 			if (existingEdges.size() > 0) {
 				for (CyEdge exEdge : existingEdges) {
 					if (rin.getRow(exEdge).get(CyEdge.INTERACTION, String.class)
 							.equals(CONTACTEDGE)
 							&& rin.getRow(exEdge).get(INTATOMS_ATTR, String.class)
 									.equals(interactingAtoms)) {
 						edge = exEdge;
 						rin.getRow(edge).set(OVERLAP_ATTR, null);
 						break;
 					}
 				}
 			}
 		}
 		if (edge == null) {
 			edge = rin.addEdge(source, target, true);
 		}
 		String edgeName = rin.getRow(source).get(CyNetwork.NAME, String.class) + " (" + type + ") "
 				+ rin.getRow(target).get(CyNetwork.NAME, String.class);
 		rin.getRow(edge).set(CyNetwork.NAME, edgeName);
 		rin.getRow(edge).set(CyEdge.INTERACTION, type);
 		rin.getRow(edge).set(INTATOMS_ATTR, interactingAtoms);
 		rin.getRow(edge).set(INTSUBTYPE_ATTR, interactionSubtype);
 		return edge;
 	}
 
 	private CyEdge createConnectivityEdge(CyNetwork rin, CyNode node1, CyNode node2) {
 		CyEdge edge = rin.addEdge(node1, node2, true);
 		String edgeName = rin.getRow(node1).get(CyNetwork.NAME, String.class) + " (" + BBEDGE
 				+ ") " + rin.getRow(node2).get(CyNetwork.NAME, String.class);
 		rin.getRow(edge).set(CyNetwork.NAME, edgeName);
 		rin.getRow(edge).set(CyEdge.INTERACTION, BBEDGE);
 		rin.getRow(edge).set(INTSUBTYPE_ATTR, BBEDGE + " mc" + SUBTYPEDELIM + "mc");
 		return edge;
 	}
 
 	private CyNode createResidueNode(CyNetwork rin, Map<String, CyNode> nodeMap,
 			boolean ignoreWater, String alias) {
 		// alias is a atomSpec of the form [#model]:residueNumber@atom
 		// We want to convert that to a node identifier of [pdbid#]ABC nnn
 		// and add FunctionalResidues and BackboneOnly attributes
 		// boolean singleModel = false;
 		ChimeraModel model = ChimUtils.getModel(alias, chimeraManager);
 		if (model == null) {
 			model = chimeraManager.getChimeraModel();
 			// singleModel = true;
 		}
 		ChimeraResidue residue = ChimUtils.getResidue(alias, model);
 		if (ignoreWater && residue.getType().equals("HOH")) {
 			return null;
 		}
 		// boolean backbone = ChimUtils.isBackbone(alias);
 
 		int displayType = ChimeraResidue.getDisplayType();
 		ChimeraResidue.setDisplayType(ChimeraResidue.THREE_LETTER);
 		// OK, now we have everything we need, create the node
 		String nodeName = residue.toString().trim();
 		if (residue.getChainId() != "_") {
 			nodeName += "." + residue.getChainId();
 		}
 		ChimeraResidue.setDisplayType(displayType);
 
 		// if (!singleModel)
 		nodeName = model.getModelName() + "#" + nodeName;
 
 		// Create the node if it does not already exist in the network
 		CyNode node = null;
 		if (!nodeMap.containsKey(nodeName)) {
 			node = rin.addNode();
 			rin.getRow(node).set(CyNetwork.NAME, nodeName);
 			nodeMap.put(nodeName, node);
 
 			// Add simple attributes such as name, type, index and association with the chimera
 			// model it was created from
 			String chimRes = model.getModelName() + "#" + residue.getIndex();
 			if (residue.getChainId() != "_") {
 				chimRes += "." + residue.getChainId();
 			}
 			rin.getRow(node).set(ChimUtils.DEFAULT_STRUCTURE_KEY, chimRes);
 			rin.getRow(node).set(
 					ChimUtils.RINALYZER_ATTR,
 					model.getModelName() + ":" + residue.getChainId() + ":" + residue.getIndex()
 							+ ":_:" + residue.getType());
 			rin.getRow(node).set(SEED_ATTR, Boolean.valueOf(residue.isSelected()));
 			rin.getRow(node).set(CHAIN_ATTR, residue.getChainId());
 			rin.getRow(node).set(TYPE_ATTR, residue.getType());
			rin.getRow(node).set(RESINDEX_ATTR, residue.getIndex());
 
 			// Add structureViz attributes
 			String smiles = ChimUtils.toSMILES(residue.getType());
 			if (smiles != null) {
 				rin.getRow(node).set(SMILES_ATTR, smiles);
 			}
 		} else {
 			node = nodeMap.get(nodeName);
 		}
 		return node;
 	}
 
 	private void updateMap(Map<CyEdge, Double> map, CyEdge edge, String value, int comparison) {
 		// Save the minimum distance between atoms
 		Double v = Double.valueOf(value);
 		if (map.containsKey(edge)) {
 			if (comparison < 0 && map.get(edge).compareTo(v) > 0)
 				map.put(edge, v);
 			else if (comparison > 0 && map.get(edge).compareTo(v) < 0)
 				map.put(edge, v);
 		} else {
 			map.put(edge, v);
 		}
 	}
 
 	private ChimeraResidue[] getRange(List<ChimeraResidue[]> rangeList, CyNode node, CyNetwork rin) {
 		for (ChimeraResidue[] range : rangeList) {
 			if (inRange(range, node, rin))
 				return range;
 		}
 		return null;
 	}
 
 	private boolean inRange(ChimeraResidue[] range, CyNode node, CyNetwork rin) {
 		String residueAttr = rin.getRow(node).get(ChimUtils.DEFAULT_STRUCTURE_KEY, String.class);
 		ChimeraStructuralObject cso = ChimUtils.fromAttribute(residueAttr, chimeraManager);
 		// Models can't be in a range...
 		if (cso == null || cso instanceof ChimeraModel)
 			return false;
 
 		// A chain might be in a range -- check this
 		if (cso instanceof ChimeraChain) {
 			String chainID = ((ChimeraChain) cso).getChainId();
 			return inChainRange(range, chainID);
 		}
 
 		// OK, we have a residue, but we need to be careful to make
 		// sure that the chains match
 		ChimeraResidue residue = (ChimeraResidue) cso;
 		if (!inChainRange(range, residue.getChainId())) {
 			return false;
 		}
 
 		int startIndex = Integer.parseInt(range[0].getIndex());
 		int endIndex = Integer.parseInt(range[1].getIndex());
 		int residueIndex = Integer.parseInt(residue.getIndex());
 
 		if (endIndex < startIndex) {
 			if (endIndex <= residueIndex && residueIndex <= startIndex)
 				return true;
 		} else {
 			if (startIndex <= residueIndex && residueIndex <= endIndex)
 				return true;
 		}
 
 		return false;
 	}
 
 	private boolean inRange2(ChimeraResidue[] range, CyNode node1, CyNode node2, CyNetwork rin) {
 		ChimeraStructuralObject cso1 = ChimUtils.fromAttribute(
 				rin.getRow(node1).get(ChimUtils.DEFAULT_STRUCTURE_KEY, String.class),
 				chimeraManager);
 		ChimeraStructuralObject cso2 = ChimUtils.fromAttribute(
 				rin.getRow(node2).get(ChimUtils.DEFAULT_STRUCTURE_KEY, String.class),
 				chimeraManager);
 		// Models can't be in a range...
 		if (cso1 == null || cso1 instanceof ChimeraModel || cso1 instanceof ChimeraChain
 				|| cso2 == null || cso2 instanceof ChimeraModel || cso2 instanceof ChimeraChain)
 			return false;
 
 		// OK, we have a residue, but we need to be careful to make
 		// sure that the chains match
 		ChimeraResidue residue1 = (ChimeraResidue) cso1;
 		ChimeraResidue residue2 = (ChimeraResidue) cso2;
 
 		if (!inChainRange(range, residue1.getChainId())) {
 			return false;
 		} else if (!inChainRange(range, residue2.getChainId())) {
 			return false;
 		}
 
 		int startIndex = Integer.parseInt(range[0].getIndex());
 		int endIndex = Integer.parseInt(range[1].getIndex());
 		int residueIndex1 = Integer.parseInt(residue1.getIndex());
 		int residueIndex2 = Integer.parseInt(residue2.getIndex());
 		int diff = Math.abs(residueIndex1 - residueIndex2);
 
 		if (endIndex < startIndex) {
 			if (diff == 1 && endIndex <= residueIndex1 && residueIndex1 <= startIndex
 					&& endIndex <= residueIndex2 && residueIndex2 <= startIndex)
 				return true;
 		} else {
 			if (diff == 1 && startIndex <= residueIndex1 && residueIndex1 <= endIndex
 					&& startIndex <= residueIndex2 && residueIndex2 <= endIndex)
 				return true;
 		}
 		return false;
 	}
 
 	private boolean inChainRange(ChimeraResidue[] range, String chainID) {
 		String start = range[0].getChainId();
 		String end = range[1].getChainId();
 		// range should contain residues from the same chain
 		if (!start.equals(end)) {
 			return false;
 		}
 		// change positions if necessary
 		if (start.compareTo(end) > 0) {
 			end = range[0].getChainId();
 			start = range[1].getChainId();
 		}
 		// chainID should be in the chain
 		if (start.compareTo(chainID) <= 0 && chainID.compareTo(end) <= 0) {
 			return true;
 		}
 		return false;
 	}
 
 	public CyNetwork createRIN(Map<String, CyNode> nodeMap, String networkName,
 			boolean ignoreWater, boolean includeCombiEdges) {
 		// get factories, etc.
 		CyNetworkFactory cyNetworkFactory = (CyNetworkFactory) structureManager
 				.getService(CyNetworkFactory.class);
 
 		// Create the network
 		CyNetwork rin = cyNetworkFactory.createNetwork();
 		rin.getRow(rin).set(CyNetwork.NAME, networkName);
 
 		// Create new attributes
 		// rin.getDefaultNodeTable().createColumn(ChimUtils.RESIDUE_ATTR, String.class, false);
 		rin.getDefaultNodeTable().createColumn(ChimUtils.RINALYZER_ATTR, String.class, false);
 		rin.getDefaultNodeTable().createColumn(SMILES_ATTR, String.class, false);
 		rin.getDefaultNodeTable()
 				.createColumn(ChimUtils.DEFAULT_STRUCTURE_KEY, String.class, false);
 		rin.getDefaultNodeTable().createColumn(SEED_ATTR, Boolean.class, false);
 		rin.getDefaultNodeTable().createColumn(CHAIN_ATTR, String.class, false);
 		rin.getDefaultNodeTable().createColumn(TYPE_ATTR, String.class, false);
 		rin.getDefaultNodeTable().createColumn(RESINDEX_ATTR, Integer.class, false);
 
 		rin.getDefaultEdgeTable().createColumn(DISTANCE_ATTR, Double.class, false);
 		rin.getDefaultEdgeTable().createColumn(OVERLAP_ATTR, Double.class, false);
 		rin.getDefaultEdgeTable().createColumn(INTSUBTYPE_ATTR, String.class, false);
 		rin.getDefaultEdgeTable().createColumn(INTATOMS_ATTR, String.class, false);
 		if (includeCombiEdges) {
 			rin.getDefaultEdgeTable().createColumn(NUMINT_ATTR, Integer.class, false);
 		}
 
 		// add all selected nodes
 		List<String> residues = chimeraManager.getSelectedResidueSpecs();
 		for (String res : residues) {
 			// System.out.println("get selected residue");
 			createResidueNode(rin, nodeMap, ignoreWater, res);
 		}
 
 		// return network
 		return rin;
 	}
 
 	public String getAttrCommand(String resAttr) {
 		return residueAttrCommandMap.get(resAttr);
 	}
 
 	public Collection<String> getResAttrs() {
 		return residueAttrCommandMap.keySet();
 	}
 
 	public void annotate(CyNetwork network, String resAttr, String command) {
 		// get models
 		Set<ChimeraStructuralObject> chimObjs = structureManager.getAssociatedChimObjs(network);
 		if (chimObjs == null) {
 			return;
 		}
 		// System.out.println("Annotate from " + chimObjs.size() + " chimera objects.");
 		// TODO: [Optional] What to do if there are two open models associated with the same
 		// network?
 		// Now, attributes are just overwritten
 		for (ChimeraStructuralObject chimObj : chimObjs) {
 			if (chimObj instanceof ChimeraModel) {
 				// get attribute values
 				Map<ChimeraResidue, Object> resValues = chimeraManager.getAttrValues(command,
 						chimObj.getChimeraModel());
 				if (resValues.size() == 0) {
 					continue;
 				}
 				Object testObj = resValues.values().iterator().next();
 				if (testObj == null) {
 					continue;
 				}
 				// create attribute
 				if (network.getDefaultNodeTable().getColumn(resAttr) != null
 						&& network.getDefaultNodeTable().getColumn(resAttr).getType() != testObj
 								.getClass()) {
 					network.getDefaultNodeTable().deleteColumn(resAttr);
 				} else if (network.getDefaultNodeTable().getColumn(resAttr) == null) {
 					network.getDefaultNodeTable().createColumn(resAttr, testObj.getClass(), false);
 				}
 				// save all the values
 				for (ChimeraResidue res : resValues.keySet()) {
 					Set<CyIdentifiable> cyObjs = structureManager.getAssociatedCyObjs(res);
 					if (cyObjs == null) {
 						continue;
 					}
 					for (CyIdentifiable cyId : cyObjs) {
 						if (cyId instanceof CyNode && network.containsNode((CyNode) cyId)) {
 							network.getRow(cyId).set(resAttr, resValues.get(res));
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void annotateSS(CyNetwork network) {
 		// get models
 		final String ssColumn = "SS";
 		Set<ChimeraStructuralObject> chimObjs = structureManager.getAssociatedChimObjs(network);
 		if (chimObjs == null) {
 			return;
 		}
 		if (network.getDefaultNodeTable().getColumn(ssColumn) == null) {
 			network.getDefaultNodeTable().createColumn(ssColumn, String.class, false, "");
 		}
 		for (ChimeraStructuralObject chimObj : chimObjs) {
 			if (chimObj instanceof ChimeraModel) {
 				chimeraManager.sendChimeraCommand("ksdssp", false);
 				Map<ChimeraResidue, Object> hResidues = chimeraManager.getAttrValues("isHelix",
 						chimObj.getChimeraModel());
 				Map<ChimeraResidue, Object> sResidues = chimeraManager.getAttrValues("isSheet",
 						chimObj.getChimeraModel());
 				for (ChimeraResidue res : hResidues.keySet()) {
 					Set<CyIdentifiable> cyObjs = structureManager.getAssociatedCyObjs(res);
 					if (cyObjs == null) {
 						continue;
 					}
 					for (CyIdentifiable cyId : cyObjs) {
 						if (cyId instanceof CyNode && network.containsNode((CyNode) cyId)) {
 							if (hResidues.get(res).equals(Boolean.TRUE)) {
 								network.getRow(cyId).set(ssColumn, "Helix");
 							} else if (sResidues.containsKey(res)
 									&& sResidues.get(res).equals(Boolean.TRUE)) {
 								network.getRow(cyId).set(ssColumn, "Sheet");
 							} else {
 								network.getRow(cyId).set(ssColumn, "Loop");
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void annotateCoord(CyNetwork network, String resAttr) {
 		Set<ChimeraStructuralObject> chimObjs = structureManager.getAssociatedChimObjs(network);
 		if (chimObjs == null) {
 			return;
 		}
 		if (network.getDefaultNodeTable().getColumn(resAttr + ".x") == null) {
 			network.getDefaultNodeTable().createColumn(resAttr + ".x", Double.class, false);
 		}
 		if (network.getDefaultNodeTable().getColumn(resAttr + ".y") == null) {
 			network.getDefaultNodeTable().createColumn(resAttr + ".y", Double.class, false);
 		}
 		if (network.getDefaultNodeTable().getColumn(resAttr + ".z") == null) {
 			network.getDefaultNodeTable().createColumn(resAttr + ".z", Double.class, false);
 		}
 		// get coordinates
 		Map<ChimeraResidue, Double[]> resCoords = new HashMap<ChimeraResidue, Double[]>();
 		for (ChimeraStructuralObject model : chimObjs) {
 			if (model instanceof ChimeraModel) {
 				List<String> reply = chimeraManager.sendChimeraCommand("getcrd xf "
 						+ model.getChimeraModel().toSpec(), true);
 				if (reply == null) {
 					continue;
 				}
 				String[] lineParts = null;
 				for (String inputLine : reply) {
 					// response from chimera should look like this:
 					// Atom #0:355.A@C 36.598 78.221 2.056
 					// Atom #0:355.A@CA 35.276 77.803 1.543
 					lineParts = inputLine.split("\\s+");
 					if (lineParts.length != 5) {
 						continue;
 					}
 					ChimeraResidue residue = ChimUtils.getResidue(lineParts[1],
 							model.getChimeraModel());
 					String atom = ChimUtils.getAtomName(lineParts[1]);
 					if (residue == null) {
 						continue;
 					}
 					Double[] coord = null;
 					try {
 						coord = new Double[3];
 						for (int i = 0; i < 3; i++) {
 							coord[i] = new Double(lineParts[i + 2]);
 						}
 						if (!resCoords.containsKey(residue) || atom.equals("CA")) {
 							resCoords.put(residue, coord);
 						}
 					} catch (NumberFormatException ex) {
 						// no coordinates for this node, ignore
 						// ex.printStackTrace();
 					}
 				}
 			}
 		}
 		// save coordinates as attributes
 		for (CyNode node : network.getNodeList()) {
 			Set<ChimeraStructuralObject> nodeChimObjs = structureManager
 					.getAssociatedChimObjs(node);
 			if (nodeChimObjs == null) {
 				continue;
 			}
 			for (ChimeraStructuralObject chimObj : nodeChimObjs) {
 				if (resCoords.containsKey(chimObj)) {
 					final Double[] coord = resCoords.get(chimObj);
 					network.getRow(node).set(resAttr + ".x", coord[0]);
 					network.getRow(node).set(resAttr + ".y", coord[1]);
 					network.getRow(node).set(resAttr + ".z", coord[2]);
 				}
 			}
 		}
 	}
 
 	public void syncColors() {
 		Map<Integer, ChimeraModel> models = chimeraManager.getSelectedModels();
 		for (ChimeraModel selModel : models.values()) {
 			ChimeraModel model = chimeraManager.getChimeraModel(selModel.getModelNumber(),
 					selModel.getSubModelNumber());
 			if (model != null) {
 				for (CyIdentifiable obj : model.getCyObjects().keySet()) {
 					if (obj instanceof CyNetwork) {
 						CyNetworkViewManager manager = (CyNetworkViewManager) structureManager
 								.getService(CyNetworkViewManager.class);
 						for (CyNetworkView view : manager.getNetworkViews((CyNetwork) obj)) {
 							syncChimToCyColors(view);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void syncChimToCyColors(CyNetworkView networkView) {
 		// get models
 		CyNetwork network = networkView.getModel();
 		// if (network.getDefaultNodeTable().getColumn("chimeraColor") == null) {
 		// network.getDefaultNodeTable().createColumn("chimeraColor", String.class, false);
 		// }
 		Set<ChimeraStructuralObject> chimObjs = structureManager.getAssociatedChimObjs(network);
 		if (chimObjs == null) {
 			return;
 		}
 		Map<Long, Paint> nodeToColorMapping = new HashMap<Long, Paint>();
 		for (ChimeraStructuralObject chimObj : chimObjs) {
 			if (chimObj instanceof ChimeraModel) {
 				// get attribute values
 				Map<ChimeraResidue, Object> resValues = chimeraManager.getAttrValues("ribbonColor",
 						chimObj.getChimeraModel());
 				if (resValues.size() == 0) {
 					continue;
 				}
 				// save all the values
 				for (ChimeraResidue res : resValues.keySet()) {
 					Set<CyIdentifiable> cyObjs = structureManager.getAssociatedCyObjs(res);
 					if (cyObjs == null) {
 						continue;
 					}
 					for (CyIdentifiable cyId : cyObjs) {
 						if (cyId instanceof CyNode && network.containsNode((CyNode) cyId)) {
 							String[] rgb = ((String) resValues.get(res)).split(",");
 							if (rgb.length == 3) {
 								try {
 									Color resColor = new Color(Float.valueOf(rgb[0]),
 											Float.valueOf(rgb[1]), Float.valueOf(rgb[2]));
 									nodeToColorMapping.put(cyId.getSUID(), resColor);
 									// network.getRow(cyId).set("chimeraColor",
 									// resColor.toString());
 									// networkView.getNodeView((CyNode) cyId).clearValueLock(
 									// BasicVisualLexicon.NODE_FILL_COLOR);
 									// networkView.getNodeView((CyNode) cyId).setVisualProperty(
 									// BasicVisualLexicon.NODE_FILL_COLOR, resColor);
 								} catch (NumberFormatException ex) {
 									// ignore
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		// TODO: [Optional] Use passthrough mapping if working
 		// VisualMappingFunctionFactory vmfFactoryP = (VisualMappingFunctionFactory)
 		// structureManager
 		// .getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
 		// PassthroughMapping colorMapping = (PassthroughMapping) vmfFactoryP
 		// .createVisualMappingFunction("chimeraColor", String.class,
 		// BasicVisualLexicon.NODE_FILL_COLOR);
 		VisualMappingFunctionFactory vmfFactoryD = (VisualMappingFunctionFactory) structureManager
 				.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
 		DiscreteMapping<Long, Paint> nodeColorMapping = (DiscreteMapping<Long, Paint>) vmfFactoryD
 				.createVisualMappingFunction(CyIdentifiable.SUID, Long.class, BasicVisualLexicon.NODE_FILL_COLOR);
 		nodeColorMapping.putAll(nodeToColorMapping);
 		VisualMappingManager manager = (VisualMappingManager) structureManager
 				.getService(VisualMappingManager.class);
 		VisualStyle vs = manager.getCurrentVisualStyle();
 		vs.addVisualMappingFunction(nodeColorMapping);
 		vs.apply(networkView);
 		networkView.updateView();
 	}
 
 	public void syncCyToChimColors(CyNetworkView networkView) {
 		final Map<Color, String> color2res = new HashMap<Color, String>();
 		for (final View<CyNode> nodeView : networkView.getNodeViews()) {
 			final CyNode node = nodeView.getModel();
 			final Color color = (Color) nodeView
 					.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR);
 			final Set<ChimeraStructuralObject> chimObjs = structureManager
 					.getAssociatedChimObjs(node);
 			if (color != null && chimObjs != null) {
 				for (ChimeraStructuralObject chimObj : chimObjs) {
 					if (!color2res.containsKey(color)) {
 						color2res.put(color, chimObj.toSpec());
 					} else {
 						color2res.put(color, color2res.get(color) + chimObj.toSpec());
 					}
 				}
 			}
 		}
 		for (final Color color : color2res.keySet()) {
 			String colorDef = "";
 			try {
 				float[] rgbColorCodes = color.getRGBColorComponents(null);
 				for (int i = 0; i < rgbColorCodes.length; i++) {
 					colorDef += rgbColorCodes[i] + ",";
 				}
 			} catch (Exception e) {
 				continue;
 			}
 			colorDef += "r,a"; // ribbons and atoms
 			chimeraManager.sendChimeraCommand("color " + colorDef + " " + color2res.get(color),
 					false);
 		}
 	}
 
 }
