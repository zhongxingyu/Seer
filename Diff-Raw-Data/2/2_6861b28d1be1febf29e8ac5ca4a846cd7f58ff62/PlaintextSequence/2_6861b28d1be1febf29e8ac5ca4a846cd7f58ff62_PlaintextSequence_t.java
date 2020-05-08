 /**
  * Copyright 2013 George Belden
  * 
  * This file is part of ZodiacEngine.
  * 
  * ZodiacEngine is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * ZodiacEngine is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * ZodiacEngine. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ciphertool.zodiacengine.entities;
 
 import org.apache.log4j.Logger;
 import org.springframework.data.annotation.Transient;
 
 import com.ciphertool.genetics.annotations.Dirty;
 import com.ciphertool.genetics.entities.Gene;
 import com.ciphertool.genetics.entities.Sequence;
 
 public class PlaintextSequence implements Sequence {
	private static Logger log = Logger.getLogger(PlaintextSequence.class);
 
 	@Transient
 	private Gene gene;
 
 	protected Integer sequenceId;
 
 	/*
 	 * TODO: Change this to a char or Character instead of String, as it should
 	 * always be only one character for this implementation.
 	 */
 	protected String value;
 
 	protected boolean hasMatch;
 
 	public PlaintextSequence() {
 	}
 
 	public PlaintextSequence(String value, Gene gene) {
 		this.value = value;
 		this.gene = gene;
 	}
 
 	@Override
 	public Gene getGene() {
 		return this.gene;
 	}
 
 	@Override
 	public void setGene(Gene gene) {
 		this.gene = gene;
 	}
 
 	@Override
 	public PlaintextSequence clone() {
 		PlaintextSequence copySequence = null;
 
 		try {
 			copySequence = (PlaintextSequence) super.clone();
 		} catch (CloneNotSupportedException cnse) {
 			log.error(
 					"Caught CloneNotSupportedException while attempting to clone PlaintextSequence.",
 					cnse);
 		}
 		copySequence.setHasMatch(false);
 
 		copySequence.setSequenceId((this.sequenceId != null) ? this.sequenceId.intValue() : null);
 
 		/*
 		 * The Gene must bet set at a higher level.
 		 */
 		copySequence.gene = null;
 
 		return copySequence;
 	}
 
 	/**
 	 * Shifts all the plaintext characters to the right the specified number of
 	 * places.
 	 * 
 	 * @param places
 	 */
 	@Dirty
 	public void shiftRight(int places) {
 		this.sequenceId = this.sequenceId + places;
 	}
 
 	/**
 	 * Shifts all the plaintext characters to the left the specified number of
 	 * places.
 	 * 
 	 * @param places
 	 */
 	@Dirty
 	public void shiftLeft(int places) {
 		this.sequenceId = this.sequenceId - places;
 	}
 
 	@Override
 	public Integer getSequenceId() {
 		return this.sequenceId;
 	}
 
 	/**
 	 * @param sequenceId
 	 *            the sequenceId to set
 	 */
 	public void setSequenceId(Integer sequenceId) {
 		this.sequenceId = sequenceId;
 	}
 
 	@Override
 	public String getValue() {
 		return this.value;
 	}
 
 	@Override
 	@Dirty
 	public void setValue(Object obj) {
 		this.value = (String) obj;
 	}
 
 	public boolean getHasMatch() {
 		return hasMatch;
 	}
 
 	public void setHasMatch(boolean hasMatch) {
 		this.hasMatch = hasMatch;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 * 
 	 * We purposely do not hash the hasMatch property since it is set to false
 	 * for clones.
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((sequenceId == null) ? 0 : sequenceId.hashCode());
 		result = prime * result + ((value == null) ? 0 : value.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 * 
 	 * We purposely do not check the hasMatch property since it is set to false
 	 * for clones.
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		PlaintextSequence other = (PlaintextSequence) obj;
 		if (sequenceId == null) {
 			if (other.sequenceId != null) {
 				return false;
 			}
 		} else if (!sequenceId.equals(other.sequenceId)) {
 			return false;
 		}
 		if (value == null) {
 			if (other.value != null) {
 				return false;
 			}
 		} else if (!value.equals(other.value)) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "PlaintextSequence [sequenceId=" + sequenceId + ", value=" + value + ", hasMatch="
 				+ hasMatch + "]";
 	}
 }
