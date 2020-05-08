 /*
  * Copyright (C) 2011 Secretariat of the Pacific Community
  *
  * This file is part of TUBS.
  *
  * TUBS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TUBS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with TUBS.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.spc.ofp.tubs.domain.purseseine;
 
 import javax.persistence.*;
 import javax.validation.constraints.*;
 
 import org.spc.ofp.tubs.domain.AuditEntry;
 
 /**
  * @author Corey Cole <coreyc@spc.int>
  *
  */
 @Entity
 @Table(name = "[obsv].[s_lfmeas]")
 public class LengthSample implements java.io.Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -373966858497684487L;
 
 	@Id
     @Column(name = "s_lfmeas_id")
     @GeneratedValue(strategy = GenerationType.IDENTITY)
 	private long id;
 	
 	@Column(name = "seq_number")
 	@Min(1)
 	@Max(120)
 	private Integer sampleNumber; // Sample number on the page, between 1 and 120
 	
 	// FIXME We should treat this as a biological sample moment
     // and use an embedded object that holds species data
     @Column(name = "sp_code", length = 3)
     private String speciesCode;
     
     @Column(name = "len_code", length = 2)
     private String lengthCode;
     
     @Column(name = "len")
     @Min(0)
     private Integer length;
     
     @Embedded
     private AuditEntry auditEntry;	
 	
 	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, targetEntity = LengthSamplingHeader.class)
     @JoinColumn(name = "s_lf_id")
     private LengthSamplingHeader header; // Pointer back to containing header
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public Integer getSampleNumber() {
 		return sampleNumber;
 	}
 
 	public void setSampleNumber(Integer sampleNumber) {
 		this.sampleNumber = sampleNumber;
 	}
 
 	public String getSpeciesCode() {
 		return speciesCode;
 	}
 
 	public void setSpeciesCode(String speciesCode) {
		if (null != speciesCode && speciesCode.trim().length() < 3) {
			this.speciesCode = "UNS";
		} else {
			this.speciesCode = speciesCode;
		}
 	}
 
 	public String getLengthCode() {
 		return lengthCode;
 	}
 
 	public void setLengthCode(String lengthCode) {
 		this.lengthCode = lengthCode;
 	}
 
 	public Integer getLength() {
 		return length;
 	}
 
 	public void setLength(Integer length) {
 		this.length = length;
 	}
 
 	public AuditEntry getAuditEntry() {
 		return auditEntry;
 	}
 
 	public void setAuditEntry(AuditEntry auditEntry) {
 		this.auditEntry = auditEntry;
 	}
 
 	public LengthSamplingHeader getHeader() {
 		return header;
 	}
 
 	public void setHeader(LengthSamplingHeader header) {
 		this.header = header;
 	}
 }
