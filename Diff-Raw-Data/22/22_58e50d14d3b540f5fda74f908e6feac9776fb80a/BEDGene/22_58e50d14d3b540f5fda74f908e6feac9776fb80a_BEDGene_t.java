 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
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
 // BEDGene.java
 // Since: 2010/09/02
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.bio;
 
 import java.io.Serializable;
 
 /**
  * Representing each gene line of BED format
  * 
  * @author yoshimura
  * 
  */
 public class BEDGene extends Gene implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	public String coordinate;
 	public int score = 0;
 
 	public BEDGene() {
 	}
 
 	public BEDGene(BEDGene other) {
 		super(other);
 		this.coordinate = other.coordinate;
 		this.score = other.score;
 	}
 
	public CDS cdsRange() {
 		return getCDS().get(0);
 	}
 
 	@Override
 	public void accept(OnGenomeDataVisitor visitor) {
 		visitor.visitBEDGene(this);
 	}
 }
