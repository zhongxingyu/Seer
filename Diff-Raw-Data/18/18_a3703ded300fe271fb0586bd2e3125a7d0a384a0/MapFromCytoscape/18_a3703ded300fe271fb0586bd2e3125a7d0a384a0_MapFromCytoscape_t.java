 /*
   Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
   The Cytoscape Consortium is:
   - Institute for Systems Biology
   - University of California San Diego
   - Memorial Sloan-Kettering Cancer Center
   - Institut Pasteur
   - Agilent Technologies
 
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
 
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package org.cytoscape.coreplugin.psi_mi.cyto_mapper;
 
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 
import cytoscape.data.AttributeValueVisitor;
 import cytoscape.data.CyAttributes;
import cytoscape.data.CyAttributesUtils;
import cytoscape.data.attr.CountedIterator;
import cytoscape.data.attr.MultiHashMap;
import cytoscape.data.attr.MultiHashMapDefinition;
 
 import org.cytoscape.coreplugin.psi_mi.data_mapper.Mapper;
 import org.cytoscape.coreplugin.psi_mi.model.AttributeBag;
 import org.cytoscape.coreplugin.psi_mi.model.ExternalReference;
 import org.cytoscape.coreplugin.psi_mi.model.Interaction;
 import org.cytoscape.coreplugin.psi_mi.model.Interactor;
 import org.cytoscape.coreplugin.psi_mi.model.vocab.CommonVocab;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 
 /**
  * Maps Cytoscape Graph Objects to Data Service Interaction objects.
  * This class performs the inverse mapping of the MapToCytoscape
  * class.
  *
  * @author Ethan Cerami
  */
 public class MapFromCytoscape implements Mapper {
 	private CyNetwork cyNetwork;
 	private CyAttributes nodeAttributes;
 	private CyAttributes edgeAttributes;
 
 	/**
 	 * All new interactions.
 	 */
 	private ArrayList interactions;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param cyNetwork CyNetwork Object.
 	 */
 	public MapFromCytoscape(CyNetwork cyNetwork) {
 		this.cyNetwork = cyNetwork;
 		this.nodeAttributes = Cytoscape.getNodeAttributes();
 		this.edgeAttributes = Cytoscape.getEdgeAttributes();
 	}
 
 	/**
 	 * Perform Mapping.
 	 */
 	public void doMapping() {
 		interactions = new ArrayList();
 
 		Iterator edgesIterator = cyNetwork.edgesIterator();
 
 		while (edgesIterator.hasNext()) {
 			Interaction interaction = new Interaction();
 			CyEdge edge = (CyEdge) edgesIterator.next();
 
 			if (edge != null) {
 				CyNode sourceNode = (CyNode) edge.getSource();
 				CyNode targetNode = (CyNode) edge.getTarget();
 				Interactor sourceInteractor = new Interactor();
 				Interactor targetInteractor = new Interactor();
 				transferNodeAttributes(sourceNode, sourceInteractor);
 				transferNodeAttributes(targetNode, targetInteractor);
 
 				ArrayList interactors = new ArrayList();
 				interactors.add(sourceInteractor);
 				interactors.add(targetInteractor);
 				interaction.setInteractors(interactors);
 				transferEdgeAttributes(edge, interaction);
 				interactions.add(interaction);
 			}
 		}
 	}
 
 	/**
 	 * Gets an ArrayList of Interaction objects.
 	 *
 	 * @return ArrayList of Interaction objects.
 	 */
 	public ArrayList getInteractions() {
 		return this.interactions;
 	}
 
 	/**
 	 * Transfers all Edge Attributes from Cytoscape to Data Service Objects.
 	 *
 	 * @param edge        Cytoscape Edge.
 	 * @param interaction Data Service Interaction Object.
 	 */
 	private void transferEdgeAttributes(CyEdge edge, Interaction interaction) {
 		String[] attributeNames = edgeAttributes.getAttributeNames();
 		transferAllAttributes(attributeNames, edgeAttributes, edge.getIdentifier(), interaction);
 	}
 
 	/**
 	 * Transfers all Node Attributes from Cytoscape to Data Service objects.
 	 *
 	 * @param node       Cytoscape Node.
 	 * @param interactor Data Service Interactor object.
 	 */
 	private void transferNodeAttributes(CyNode node, Interactor interactor) {
 		interactor.setName(node.getIdentifier());
 
 		String[] attributeNames = nodeAttributes.getAttributeNames();
 		transferAllAttributes(attributeNames, nodeAttributes, node.getIdentifier(), interactor);
 	}
 
 	/**
 	 * Transfers all Node / Edge Attributes.
 	 */
 	private void transferAllAttributes(String[] attributeNames, CyAttributes attributes,
	                                   String nodeName, final AttributeBag bag) {
 		List dbNames = null;
 		List dbIds = null;
 
 		for (int i = 0; i < attributeNames.length; i++) {
 			String attributeName = attributeNames[i];
 
 			if (attributeName.equals(CommonVocab.XREF_DB_NAME)) {
 				dbNames = attributes.getListAttribute(nodeName, attributeName);
 			} else if (attributeName.equals(CommonVocab.XREF_DB_ID)) {
 				dbIds = attributes.getListAttribute(nodeName, attributeName);
 			} else {
				CyAttributesUtils.traverseAttributeValues(nodeName, attributeName, attributes, new AttributeValueVisitor() {
					@Override
					public void visitingAttributeValue(String objTraverseID, String attrName, CyAttributes attrs, Object[] keySpace, Object visitedValue) {
						bag.addAttribute(attrName, visitedValue);
					}
				});
 			}
 		}
 
 		addExternalReferences(dbNames, dbIds, bag);
 	}
 
 	/**
 	 * Adds External References.
 	 */
 	private void addExternalReferences(List dbNames, List dbIds, AttributeBag bag) {
 		if ((dbNames != null) && (dbIds != null)) {
 			ExternalReference[] refs = new ExternalReference[dbNames.size()];
 
 			for (int i = 0; i < dbNames.size(); i++) {
 				String dbName = (String) dbNames.get(i);
 				String dbId = (String) dbIds.get(i);
 				refs[i] = new ExternalReference(dbName, dbId);
 			}
 
 			bag.setExternalRefs(refs);
 		}
 	}
 }
