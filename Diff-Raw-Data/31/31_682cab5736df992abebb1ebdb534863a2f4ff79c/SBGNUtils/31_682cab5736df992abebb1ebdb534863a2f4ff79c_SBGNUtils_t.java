 package com.miravtech.checksbgn;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.graphdrawing.graphml.xmlns.graphml.Data;
 import org.graphdrawing.graphml.xmlns.graphml.Edge;
 import org.graphdrawing.graphml.xmlns.graphml.Graph;
 import org.graphdrawing.graphml.xmlns.graphml.GraphEdgedefaultType;
 import org.graphdrawing.graphml.xmlns.graphml.Graphml;
 import org.graphdrawing.graphml.xmlns.graphml.Key;
 import org.graphdrawing.graphml.xmlns.graphml.KeyForType;
 import org.graphdrawing.graphml.xmlns.graphml.Node;
 
 import com.miravtech.sbgn.AndNodeType;
 import com.miravtech.sbgn.ArcType;
 import com.miravtech.sbgn.AssociationType;
 import com.miravtech.sbgn.CatalysisArcType;
 import com.miravtech.sbgn.CompartmentType;
 import com.miravtech.sbgn.ComplexType;
 import com.miravtech.sbgn.ConsumptionArcType;
 import com.miravtech.sbgn.DissociationType;
 import com.miravtech.sbgn.EntityPoolNodeType;
 import com.miravtech.sbgn.InhibitionArcType;
 import com.miravtech.sbgn.LogicalOperatorNodeType;
 import com.miravtech.sbgn.MacromoleculeType;
 import com.miravtech.sbgn.ModulationArcType;
 import com.miravtech.sbgn.NotNodeType;
 import com.miravtech.sbgn.NucleicAcidFeatureType;
 import com.miravtech.sbgn.OmittedProcessType;
 import com.miravtech.sbgn.OrNodeType;
 import com.miravtech.sbgn.PhenotypeType;
 import com.miravtech.sbgn.ProcessType;
 import com.miravtech.sbgn.ProductionArcType;
 import com.miravtech.sbgn.SBGNGlyphType;
 import com.miravtech.sbgn.SBGNNodeType;
 import com.miravtech.sbgn.SBGNPDl1;
 import com.miravtech.sbgn.SimpleChemicalType;
 import com.miravtech.sbgn.SinkType;
 import com.miravtech.sbgn.SourceType;
 import com.miravtech.sbgn.StateVariableType;
 import com.miravtech.sbgn.UncertainProcessType;
 import com.miravtech.sbgn.UnspecifiedEntityType;
 import com.yworks.xml.graphml.ArrowTypeType;
 import com.yworks.xml.graphml.GroupNode;
 import com.yworks.xml.graphml.NodeLabelType;
 import com.yworks.xml.graphml.PolyLineEdge;
 import com.yworks.xml.graphml.ProxyShapeNode;
 import com.yworks.xml.graphml.ShapeNode;
 import com.yworks.xml.graphml.ShapeTypeType;
 import com.yworks.xml.graphml.EdgeType.Arrows;
 import com.yworks.xml.graphml.GroupNode.State;
 import com.yworks.xml.graphml.ProxyShapeNodeType.Realizers;
 import com.yworks.xml.graphml.ShapeNode.Shape;
 
 public class SBGNUtils {
 
 	final static Map<String, SBGNNodeType> nodes = new HashMap<String, SBGNNodeType>();
 
 	final static Set<String> idSet = new HashSet<String>();
 
 	public static String getPossibleID(String begin) {
 		String root = begin;
 		// remove xml unfriendly chars
 		root = root.replace('\"', '_');
 		root = root.replace('>', '_');
 		root = root.replace('\'', '_');
 		root = root.replace('<', '_');
 		if (!idSet.contains(root)) {
 			idSet.add(root);
 			return root;
 		}
 		int i = 1;
 		while (true) {
 			String id = root + "_" + i++;
 			if (!idSet.contains(id)) {
 				idSet.add(id);
 				return id;
 			}
 
 		}
 	}
 
 	public static void setIDs(SBGNPDl1 in) throws JAXBException {
 
 		// set empty labels, if ID is provided
 		new SBGNIterator() {
 			@Override
 			public void iterateNode(SBGNNodeType n) {
 				if (n.getLabel() == null && n.getID() != null)
 					n.setLabel(n.getID());
 			}
 		}.run(in);
 
 		// save the ids in a map
 		new SBGNIterator() {
 			public void iterateNode(SBGNNodeType n) {
 				if (n.getID() != null) // save the entry in the map
 					nodes.put(n.getID(), n);
 				if (n.getID() != null)
 					idSet.add(n.getID());
 			};
 		}.run(in);
 
 		// set the IDs
 		new SBGNIterator() {
 			@Override
 			public void iterateGlyph(SBGNGlyphType n) {
 
 				if (n.getID() != null)
 					return;
 
 				// determine the beginning of the possible id
 				String root = null;
 				if (n instanceof SBGNNodeType) {
 					SBGNNodeType node = (SBGNNodeType) n;
 					if (node.getLabel() != null)
 						root = node.getLabel();
 				}
 
 				if (root == null)
 					root = n.getClass().getSimpleName();
 				n.setID(getPossibleID(root));
 			}
 
 		}.run(in);
 
 		// dump
 		JAXBContext jaxbContext = JAXBContext
 				.newInstance("com.miravtech.sbgn:com.miravtech.sbgn_graphics"); //
 		Marshaller marshaller = jaxbContext.createMarshaller();
 		marshaller.marshal(in, new File("SBGNDEBUG.xml"));
 		// solve the clones
 
 		while (true) {
 			final boolean[] found = new boolean[1];
 			new SBGNIterator() {
 				public void iterateNode(SBGNNodeType n) {
 					if (n.getCloneref() != null) {
 						SBGNNodeType cloned = nodes.get(n.getCloneref());
 						if (cloned.getCloneref() == null) {
 							Clone(n, cloned);
 							found[0] = true;
 						}
 					}
 				};
 			}.run(in);
 			if (!found[0])
 				break; // no further clone resolved
 		}
 	}
 
 	// private static int count = 0;
 
 	public static Graphml asGraphML(SBGNPDl1 in) {
 		final Graphml graphml = new Graphml();
 		Key k;
 
 		k = new Key();
 		k.setFor(KeyForType.NODE);
 		k.setId("d3");
 		k.setYfilesType("nodegraphics");
 		graphml.getKeies().add(k);
 
 		k = new Key();
 		k.setFor(KeyForType.EDGE);
 		k.setId("d6");
 		k.setYfilesType("edgegraphics");
 		graphml.getKeies().add(k);
 
 		final Graph main = new Graph();
 		graphml.getGraphsAndDatas().add(main);
 		main.setEdgedefault(GraphEdgedefaultType.DIRECTED);
 		main.setId("MainGraph");
 
 		final Map<SBGNNodeType, Graph> mapping = new HashMap<SBGNNodeType, Graph>();
 		mapping.put(null, main);
 		new SBGNIterator() {
 			public void iterateNode(SBGNNodeType n) {
 				Graph loc;
 				if (stack.size() == 0) // base
 					loc = main;
 				else
 					loc = mapping.get(getLastNode());
 
 				Node theNode = new Node();
 				Data dt = new Data();
 				dt.setKey("d3");
 				theNode.getDatasAndPorts().add(dt);
 
 				NodeLabelType nlt = new NodeLabelType();
 				String label = n.getLabel();
 				if (n instanceof OmittedProcessType)
 					label = "\\\\";
 				if (n instanceof UncertainProcessType)
 					label = "?";
 				if (n instanceof DissociationType)
 					label = "O";
 				if (n instanceof AndNodeType)
 					label = "AND";
 				if (n instanceof OrNodeType)
 					label = "OR";
 				if (n instanceof NotNodeType)
 					label = "NOT";
 				nlt.setValue(label);
 
 				if (n instanceof ComplexType || n instanceof CompartmentType) {
 					ProxyShapeNode p = new ProxyShapeNode();
 					p.setRealizers(new Realizers());
 					p.getRealizers().setActive(new BigInteger("0"));
 					GroupNode gnt = new GroupNode();
 					gnt.getNodeLabels().add(nlt);
 					State s = new State();
 					s.setClosed(false);
 					s.setInnerGraphDisplayEnabled(true);
 					p.getRealizers().getShapeNodesAndImageNodesAndGroupNodes()
 							.add(gnt);
 					gnt.setState(s);
 					gnt = new GroupNode();
 					NodeLabelType nlt1 = new NodeLabelType();
 					nlt1.setValue(nlt.getValue());
 					gnt.getNodeLabels().add(nlt1);
 					s = new State();
 					s.setClosed(true);
 					s.setInnerGraphDisplayEnabled(true);
 					gnt.setState(s);
					p.getRealizers().getShapeNodesAndImageNodesAndGroupNodes()
							.add(gnt);
 
 					dt.getContent().add(p);
 					Graph inner = new Graph();
 					inner.setEdgedefault(GraphEdgedefaultType.DIRECTED);
 
 					theNode.setGraph(inner);
 					mapping.put(n, inner);
 				} else {
 					if (n instanceof ProcessType
 							|| n instanceof EntityPoolNodeType
 							|| n instanceof LogicalOperatorNodeType) {
 						// simple node
 						ShapeNode s = new ShapeNode();
 						Shape sh = new Shape();
 						sh.setType(ShapeTypeType.RECTANGLE);
 						if (n instanceof UnspecifiedEntityType)
 							sh.setType(ShapeTypeType.ELLIPSE);
 						if (n instanceof SimpleChemicalType)
 							sh.setType(ShapeTypeType.ELLIPSE);
 						if (n instanceof MacromoleculeType)
 							sh.setType(ShapeTypeType.ROUNDRECTANGLE);
 						if (n instanceof PhenotypeType)
 							sh.setType(ShapeTypeType.DIAMOND);
 						if (n instanceof NucleicAcidFeatureType)
 							sh.setType(ShapeTypeType.ROUNDRECTANGLE);
 						if (n instanceof AssociationType)
 							sh.setType(ShapeTypeType.ELLIPSE);
 						if (n instanceof DissociationType)
 							sh.setType(ShapeTypeType.ELLIPSE);
 						if (n instanceof SinkType)
 							sh.setType(ShapeTypeType.ELLIPSE);
 						if (n instanceof SourceType)
 							sh.setType(ShapeTypeType.ELLIPSE);
 						if (n instanceof LogicalOperatorNodeType)
 							sh.setType(ShapeTypeType.ELLIPSE);
 						s.setShape(sh);
 						s.getNodeLabels().add(nlt);
 						dt.getContent().add(s);
 					} else
 						return; // uninteresting node type
 				}
 				theNode.setId(n.getID());
 				loc.getDatasAndNodesAndEdges().add(theNode);
 			};
 		}.run(in);
 
 		new SBGNIterator() {
 			public void iterateArc(ArcType n) {
 				SBGNNodeType n1 = nodes.get(n.getRefNode());
 				SBGNNodeType n2 = getCurrentNode();
 
 				if (n1 instanceof ProcessType) { // any arc goes to a process,
 					// except production
 					if (!(n instanceof ProductionArcType)) {
 						SBGNNodeType tmp = n1;
 						n1 = n2;
 						n2 = tmp;
 					}
 				}
 				if (n2 instanceof ProcessType) { // production - process to EPN
 					if (n instanceof ProductionArcType) {
 						SBGNNodeType tmp = n1;
 						n1 = n2;
 						n2 = tmp;
 					}
 				}
 
 				if (n2 instanceof EntityPoolNodeType) { // any arc goes from EPN
 					// to process
 					if (!(n instanceof ProductionArcType)) { // except
 						// production
 						SBGNNodeType tmp = n1;
 						n1 = n2;
 						n2 = tmp;
 					}
 				}
 				if (n1 instanceof EntityPoolNodeType) { // production goes from
 					// process to epn
 					if (n instanceof ProductionArcType) {
 						SBGNNodeType tmp = n1;
 						n1 = n2;
 						n2 = tmp;
 					}
 				}
 				if (n1 instanceof SinkType) { // Sink is always the target
 					if (n instanceof ConsumptionArcType) {
 						SBGNNodeType tmp = n1;
 						n1 = n2;
 						n2 = tmp;
 					}
 				}
 
 				if (n2 instanceof SourceType) { // Source is always the source
 					if (n instanceof ConsumptionArcType) {
 						SBGNNodeType tmp = n1;
 						n1 = n2;
 						n2 = tmp;
 					}
 				}
 
 				Edge e = new Edge();
 				e.setSource(n1.getID());
 				e.setTarget(n2.getID());
 				Data dt = new Data();
 				dt.setKey("d6");
 				e.getDatas().add(dt);
 				PolyLineEdge plet = new PolyLineEdge();
 				dt.getContent().add(plet);
 				Arrows a = new Arrows();
 				a.setSource(ArrowTypeType.NONE);
 				a.setTarget(ArrowTypeType.NONE);
 				// determine the arrow ending type / start
 				if (n instanceof ProductionArcType)
 					a.setTarget(ArrowTypeType.DELTA);
 				if (n instanceof ModulationArcType)
 					a.setTarget(ArrowTypeType.WHITE_DIAMOND);
 				if (n instanceof CatalysisArcType)
 					a.setTarget(ArrowTypeType.TRANSPARENT_CIRCLE);
 				if (n instanceof InhibitionArcType)
 					a.setTarget(ArrowTypeType.T_SHAPE);
 				plet.setArrows(a);
 
 				main.getDatasAndNodesAndEdges().add(e);
 			};
 		}.run(in);
 
 		return graphml;
 
 	}
 
 	/**
 	 * Copies the content of model to n1.
 	 * 
 	 * @param n1
 	 * @param model
 	 */
 	static void CloneBeans(SBGNNodeType n1, SBGNNodeType model) {
 		// TODO iterate all the getters!!
 
 		// if lists, append from model to n1
 
 		// if bean objects, not null call recursively
 
 		// if simple object, clone and copy if the n1 side is null
 
 	}
 
 	static public SBGNNodeType cloneNode(SBGNNodeType n1) {
 		try {
 
 			SBGNNodeType ret = (SBGNNodeType) BeanUtils.cloneBean(n1);
 			ret.getInnerNodes().clear();
 			for (SBGNNodeType n2 : n1.getInnerNodes()) {
 				ret.getInnerNodes().add(cloneNode(n2));
 			}
 			if (ret.getID() != null)
 				ret.setID(getPossibleID(ret.getID()));
 			return ret;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Copy all node data from model to n1
 	 * 
 	 * @param n1
 	 * @param model
 	 */
 	static void Clone(SBGNNodeType n1, SBGNNodeType model) {
 		// variables of n1
 		Map<String, String> vars = getNodeVariables(n1);
 
 		// TODO remove when CloneBeans is ready
 		if (n1.getLabel() == null)
 			n1.setLabel(model.getLabel());
 		// IDs have to be copied and changed to unique ones, since on
 		// copying they become non-unique
 		SBGNNodeType c = cloneNode(model);
 		n1.getInnerNodes().addAll(c.getInnerNodes());
 
 		// copy the data
 		CloneBeans(n1, model);
 
 		// remove all variables of n1
 		Set<SBGNNodeType> toRemove = new HashSet<SBGNNodeType>();
 		for (SBGNNodeType node : n1.getInnerNodes())
 			if (node instanceof StateVariableType)
 				toRemove.add(node);
 		n1.getInnerNodes().removeAll(toRemove);
 		// merge the lists of variable
 		Map<String, String> vars_model = getNodeVariables(model);
 		for (Map.Entry<String, String> e : vars.entrySet())
 			vars_model.put(e.getKey(), e.getValue());
 		// add the merged list
 		for (Map.Entry<String, String> e : vars_model.entrySet()) {
 			StateVariableType st = new StateVariableType();
 			st.setVariable(e.getKey());
 			st.setLabel(e.getValue());
 			n1.getInnerNodes().add(st);
 		}
 		n1.setCloneref(null); // remove the clone attribute.
 	}
 
 	static Map<String, String> getNodeVariables(SBGNNodeType n) {
 		Map<String, String> ret = new HashMap<String, String>();
 		for (SBGNNodeType n1 : n.getInnerNodes()) {
 			if (n1 instanceof StateVariableType) {
 				StateVariableType var = (StateVariableType) n1;
 				ret.put(var.getVariable(), var.getLabel());
 			}
 		}
 		return ret;
 	}
 
 }
