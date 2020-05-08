 /* vim: set ts=2: */
 /**
  * Copyright (c) 2006 The Regents of the University of California.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *   1. Redistributions of source code must retain the above copyright
  *      notice, this list of conditions, and the following disclaimer.
  *   2. Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions, and the following
  *      disclaimer in the documentation and/or other materials provided
  *      with the distribution.
  *   3. Redistributions must acknowledge that this software was
  *      originally developed by the UCSF Computer Graphics Laboratory
  *      under support by the NIH National Center for Research Resources,
  *      grant P41-RR01081.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 package edu.ucsf.rbvi.structureViz2.internal.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.TreeMap;
 
 /**
  * This class provides the implementation for the ChimeraChain object
  * 
  * @author scooter
  * 
  */
 
 public class ChimeraChain implements ChimeraStructuralObject {
 
 	/**
 	 * The model/subModel number this chain is a part of
 	 */
 	private int modelNumber;
 	private int subModelNumber;
 
 	/**
 	 * A pointer to the model this chain is a part of
 	 */
 	private ChimeraModel chimeraModel;
 
 	/**
 	 * The chainID (from the PDB record)
 	 */
 	private String chainId;
 
 	/**
 	 * The residues that are part of this chain
 	 */
 	private TreeMap<String, ChimeraResidue> residueMap;
 
 	/**
 	 * userData to associate with this chain
 	 */
 	private Object userData;
 
 	/**
 	 * Flag to indicate the selection state
 	 */
 	private boolean selected = false;
 
 	/**
 	 * Constructor to create a new ChimeraChain
 	 * 
 	 * @param model
 	 *          the model number this chain is part of
 	 * @param subModel
 	 *          the subModel number this chain is part of
 	 * @param chainId
 	 *          the chain ID for this chain
 	 */
 	public ChimeraChain(int model, int subModel, String chainId) {
 		this.modelNumber = model;
 		this.subModelNumber = subModel;
 		this.chainId = chainId;
 		residueMap = new TreeMap<String, ChimeraResidue>();
 	}
 
 	/**
 	 * set the selected state of this chain
 	 * 
 	 * @param selected
 	 *          a boolean to set the selected state to
 	 */
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 
 	/**
 	 * return the selected state of this chain
 	 * 
 	 * @return the selected state
 	 */
 	public boolean isSelected() {
 		return selected;
 	}
 
 	public boolean hasSelectedChildren() {
 		if (selected) {
 			return true;
 		} else {
 			for (ChimeraResidue residue : getResidues()) {
 				if (residue.isSelected())
 					return true;
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
 		if (selected) {
 			residueList.addAll(getResidues());
 		} else {
 			for (ChimeraResidue residue : getResidues()) {
 				if (residue.isSelected())
 					residueList.add(residue);
 			}
 		}
 		return residueList;
 	}
 
 	/**
 	 * Add a residue to the chain.
 	 * 
 	 * @param residue
 	 *          the ChimeraResidue to add to the chain.
 	 */
 	public void addResidue(ChimeraResidue residue) {
 		String index = residue.getIndex();
 		// Put it in our map so that we can return it in order
 		residueMap.put(index, residue);
 	}
 
 	/**
 	 * Return the list of residues in this chain in pdb residue order
 	 * 
 	 * @return a Collection of residues in residue order
 	 */
 	public Collection<ChimeraResidue> getResidues() {
 		return residueMap.values();
 	}
 
 	/**
 	 * Return the list of residues in this chain as a list
 	 * 
 	 * @return List of residues
 	 */
 	public List<ChimeraStructuralObject> getChildren() {
 		return new ArrayList<ChimeraStructuralObject>(residueMap.values());
 	}
 
 	/**
 	 * Get a specific residue
 	 * 
 	 * @param residueIndex
 	 *          String representation of the residue index
 	 * @return the ChimeraResidue represented by the residueIndex
 	 */
 	public ChimeraResidue getResidue(String index) {
 		// Integer index = new Integer(residueIndex);
 		if (residueMap.containsKey(index))
 			return residueMap.get(index);
 		return null;
 	}
 
 	/**
 	 * Get a list of residues as a residue range
 	 * 
 	 * @param residueRange
 	 *          String representation of the residue range
 	 * @return the List of ChimeraResidues represented by the range
 	 */
 	public List<ChimeraResidue> getResidueRange(String residueRange) {
 		String[] range = residueRange.split("-", 2);
 		if (range[1] == null || range[1].length() == 0) {
 			range[1] = range[0];
 		}
 		List<ChimeraResidue> resultRange = new ArrayList<ChimeraResidue>();
 		int start = Integer.parseInt(range[0]);
 		int end = Integer.parseInt(range[1]);
		for (int i = start; i < end; i++) {
 			String index = String.valueOf(i);
 			if (residueMap.containsKey(index))
 				resultRange.add(residueMap.get(index));
 		}
 		return resultRange;
 	}
 
 	/**
 	 * Get the ID for this chain
 	 * 
 	 * @return String value of the chainId
 	 */
 	public String getChainId() {
 		return chainId;
 	}
 
 	/**
 	 * Get the model number for this chain
 	 * 
 	 * @return the model number
 	 */
 	public int getModelNumber() {
 		return modelNumber;
 	}
 
 	/**
 	 * Get the sub-model number for this chain
 	 * 
 	 * @return the sub-model number
 	 */
 	public int getSubModelNumber() {
 		return subModelNumber;
 	}
 
 	/**
 	 * Return a string representation of this chain as follows: Chain <i>chainId</i>
 	 * (<i>residue_count</i> residues)
 	 * 
 	 * @return String representation of chain
 	 */
 	public String displayName() {
 		if (chainId.equals("_")) {
 			return ("Chain (no ID) (" + getResidueCount() + " residues)");
 		} else {
 			return ("Chain " + chainId + " (" + getResidueCount() + " residues)");
 		}
 	}
 
 	/**
 	 * Return a string representation of this chain as follows: Node xxx [Model yyyy Chain
 	 * <i>chainId</i>]
 	 * 
 	 * @return String representation of chain
 	 */
 	public String toString() {
 		String displayName = chimeraModel.getModelName();
 		if (displayName.length() > 14)
 			displayName = displayName.substring(0, 13) + "...";
 		if (chainId.equals("_")) {
 			return (displayName + " Chain (no ID) (" + getResidueCount() + " residues)");
 		} else {
 			return (displayName + " Chain " + chainId + " (" + getResidueCount() + " residues)");
 		}
 	}
 
 	/**
 	 * Return the Chimera specification for this chain
 	 * 
 	 * @return Chimera specification
 	 */
 	public String toSpec() {
 		if (chainId.equals("_")) {
 			return ("#" + modelNumber + "." + subModelNumber + ":.");
 		} else {
 			return ("#" + modelNumber + "." + subModelNumber + ":." + chainId);
 		}
 	}
 
 	/**
 	 * Return the number of residues in this chain
 	 * 
 	 * @return integer number of residues
 	 */
 	public int getResidueCount() {
 		return residueMap.size();
 	}
 
 	/**
 	 * Set the ChimeraModel for this chain
 	 * 
 	 * @param model
 	 *          ChimeraModel to associate with this chain
 	 */
 	public void setChimeraModel(ChimeraModel model) {
 		this.chimeraModel = model;
 	}
 
 	/**
 	 * Get the ChimeraModel for this chain
 	 * 
 	 * @return ChimeraModel associated with this chain
 	 */
 	public ChimeraModel getChimeraModel() {
 		return chimeraModel;
 	}
 
 	/**
 	 * Get the user data for this Chain
 	 * 
 	 * @return user data
 	 */
 	public Object getUserData() {
 		return userData;
 	}
 
 	/**
 	 * Set the user data for this Chain
 	 * 
 	 * @param data
 	 *          the user data to associate with this chain
 	 */
 	public void setUserData(Object data) {
 		this.userData = data;
 	}
 }
