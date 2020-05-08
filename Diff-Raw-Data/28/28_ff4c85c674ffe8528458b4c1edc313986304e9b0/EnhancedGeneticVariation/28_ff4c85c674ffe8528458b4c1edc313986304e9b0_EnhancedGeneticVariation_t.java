 /*--------------------------------------------------------------------------
  *  Copyright 2010 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // EnhancedGeneticVariation.java
 // Since: 2010/10/20
 //
 //--------------------------------------
 package org.utgenome.util.sv;
 
 import org.utgenome.gwt.utgb.client.bio.AminoAcid;
 
 public class EnhancedGeneticVariation extends GeneticVariation {
 
 	/**
 	 * mutation types (e.g., non-coding, missense, synonymous, non-sense, frame-shift, splice-site mutation, etc.)
 	 * 
 	 * @author leo
 	 * 
 	 */
 	public enum MutationType {
 		NA("N/A"), NC("non coding"), MS("missense"), SN("synonymous"), NS("non-sense mutation"), FS("frame-shift mutation"), SS("splice-site muatation");
 
 		public final String description;
 
 		private MutationType(String description) {
 			this.description = description;
 		}
 	}
 
 	public enum MutationPosition {
 		NA("N/A"), InterGenic("inter-genic"), UTR5("5'-UTR"), UTR3("3'-UTR"), Intron("intron"), NonCodingExon("non-coding exon"), FirstExon("first coding exon"), Exon(
 				"coding exon"), LastExon("last coding exon"), SS5("splice site at 5'-end"), SS3("splice site at 3'-end");
 
 		public final String description;
 
 		private MutationPosition(String description) {
 			this.description = description;
 		}
 	}
 
 	public EnhancedGeneticVariation(GeneticVariation v) {
 		super(v);
 	}
 
 	private static final long serialVersionUID = 1L;
 
 	// additional annotations
 	public MutationPosition mutationPosition;
 
 	public String strand;
 	public String geneName;
 	public AminoAcid refAA;
 	public AminoAcid altAA;
 	public String refCodon;
 	public String altCodon;
 
 	public MutationType getMutationType() {
 		switch (mutationPosition) {
 		case NA:
 		case InterGenic:
 		case UTR5:
 		case UTR3:
 		case Intron:
 		case NonCodingExon:
 			return MutationType.NC;
 		case SS3:
 		case SS5:
 			return MutationType.SS;
 		}
 
		if (refAA == null && altAA == null)
 			return MutationType.NC;
 
		if (variationType != VariationType.Mutation) {
 			return MutationType.FS;
 		}
 
 		if (refAA == altAA)
 			return MutationType.SN;
 
 		if (!refAA.isStopCodon() && altAA.isStopCodon()) {
 			return MutationType.NS;
 		}
 		else
 			return MutationType.MS;
 	}
 
 }
