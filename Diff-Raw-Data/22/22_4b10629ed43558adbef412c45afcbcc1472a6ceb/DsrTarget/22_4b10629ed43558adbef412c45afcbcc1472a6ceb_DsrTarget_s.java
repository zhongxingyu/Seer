 /** 
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.chai.kevin.dsr;
 /**
  * @author Jean Kahigiso M.
  *
  */
 import java.util.Set;
 
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.data.DataElement;
 import org.chai.kevin.reports.ReportTarget;
 import org.chai.kevin.util.Utils;
 import org.chai.kevin.value.StoredValue;
 
 @Entity(name = "DsrTarget")
 @Table(name = "dhsst_dsr_target")
 public class DsrTarget extends ReportTarget {
 	
 	private Long id;
 	private DataElement<StoredValue> dataElement;
 	private DsrTargetCategory category;
 	private String format;
	private String typeCodeString;  //comma-separated list of location ids
 	
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@ManyToOne(targetEntity=DataElement.class, optional=false)
 	public DataElement<StoredValue> getDataElement() {
 		return dataElement;
 	}
 
 	public void setDataElement(DataElement<StoredValue> dataElement) {
 		this.dataElement = dataElement;
 	}
 
 	@ManyToOne(targetEntity=DsrTargetCategory.class)
 	public DsrTargetCategory getCategory() {
 		return category;
 	}
 
 	public void setCategory(DsrTargetCategory category) {
 		this.category = category;
 	}
 
 	@Basic
 	public String getFormat() {
 		return format;
 	}
 
 	public void setFormat(String format) {
 		this.format = format;
 	}
 
 	@Lob
 	public String getTypeCodeString() {
 		return typeCodeString;
 	}
 
 	public void setTypeCodeString(String typeCodeString) {
 		this.typeCodeString = typeCodeString;
 	}
 	
 	@Transient
 	public Set<String> getTypeCodes() {
 		return Utils.split(typeCodeString);
 	}
 	
 	public void setTypeCodes(Set<String> typeCodes) {
 		this.typeCodeString = Utils.unsplit(typeCodes);
 	}
 	
 }
