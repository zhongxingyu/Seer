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
 package org.chai.kevin.survey;
 
 /**
  * @author JeanKahigiso
  *
  */
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.MapKeyJoinColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.Orderable;
 import org.chai.kevin.Translation;
 import org.chai.kevin.util.Utils;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 @Entity(name = "SurveyTableRow")
 @Table(name = "dhsst_survey_table_row")
 public class SurveyTableRow extends Orderable<Integer> {
 
 	private Long id;
 	private Integer order;
 	private String typeCodeString;
 	private SurveyTableQuestion question;
 	private Map<SurveyTableColumn, SurveyElement> surveyElements = new LinkedHashMap<SurveyTableColumn, SurveyElement>();
 	private Translation names = new Translation();
 
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@Basic
 	@Column(name="ordering")
 	public Integer getOrder() {
 		return order;
 	}
 	
 	public void setOrder(Integer order) {
 		this.order = order;
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
 
 	@ManyToOne(targetEntity=SurveyTableQuestion.class, fetch=FetchType.LAZY)
 	@JoinColumn(nullable=false)
 	public SurveyTableQuestion getQuestion() {
 		return question;
 	}
 
 	public void setQuestion(SurveyTableQuestion question) {
 		this.question = question;
 	}
 
 	@OneToMany(targetEntity=SurveyElement.class)
 	@JoinTable(name="dhsst_survey_table_row_elements", joinColumns=@JoinColumn(nullable=false))
 	@MapKeyJoinColumn(nullable=false, name="survey_table_column")
 	@Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
 	public Map<SurveyTableColumn, SurveyElement> getSurveyElements() {
 		return surveyElements;
 	}
 
 	public void setSurveyElements(
 			Map<SurveyTableColumn, SurveyElement> surveyElements) {
 		this.surveyElements = surveyElements;
 	}
 	
 	@Embedded
 	@AttributeOverrides({ @AttributeOverride(name = "jsonText", column = @Column(name = "jsonNames", nullable = false)) })
 	public Translation getNames() {
 		return names;
 	}
 
 	public void setNames(Translation names) {
 		this.names = names;
 	}
 
 	@Transient
 	public Set<String> getTypeApplicable() {
 		return Utils.split(this.typeCodeString);
 	}
 	
     @Transient
 	protected SurveyTableRow deepCopy(SurveyCloner cloner, Map<Long, SurveyTableColumn> columns) {
     	SurveyTableRow copy = new SurveyTableRow();
     	copy.setNames(new Translation(getNames()));
     	copy.setTypeCodeString(getTypeCodeString());
     	copy.setOrder(getOrder());
     	copy.setQuestion((SurveyTableQuestion)cloner.getQuestion(getQuestion()));
     	for (Entry<SurveyTableColumn, SurveyElement> entry : getSurveyElements().entrySet()) {
 			copy.getSurveyElements().put(columns.get(entry.getKey().getId()), cloner.getElement(entry.getValue()));
 		}
     	return copy;
 	}
     
 	@Override
	public String toString() {
		return "SurveyTableRow[getId()=" + getId() + ", getNames()=" + getNames() + "]";
	}

	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((names == null) ? 0 : names.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		SurveyTableRow other = (SurveyTableRow) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (names == null) {
 			if (other.names != null)
 				return false;
 		} else if (!names.equals(other.names))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
		return "SurveyTableRow [id=" + id + ", order=" + order
				+ ", typeCodeString=" + typeCodeString + ", question="
				+ question + ", surveyElements=" + surveyElements + ", names="
				+ names + "]";
	}
    
 
 }
