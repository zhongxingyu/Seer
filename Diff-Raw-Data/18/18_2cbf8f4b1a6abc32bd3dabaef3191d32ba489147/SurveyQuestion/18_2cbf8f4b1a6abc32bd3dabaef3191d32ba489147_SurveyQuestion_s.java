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
 import java.util.List;
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
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.Orderable;
 import org.chai.kevin.Translation;
 import org.chai.kevin.form.FormElement;
 import org.chai.kevin.location.DataLocationType;
 import org.chai.kevin.util.Utils;
 
 @Entity(name = "SurveyQuestion")
 @Table(name = "dhsst_survey_question")
 @Inheritance(strategy = InheritanceType.JOINED)
 public abstract class SurveyQuestion extends Orderable<Integer> {
 
 	// TODO might be a good idea to get rid of this
 	public enum QuestionType {CHECKBOX("checkboxQuestion"), TABLE("tableQuestion"), SIMPLE("simpleQuestion"), WIZARD("wizardQuestion");
 		private String template;
 	
 		private QuestionType(String template) {
 			this.template = template;
 		}
 		
 		public String getTemplate() {
 			return template;
 		}
 	
 	}
 	
 	private Long id;
 	private Integer order;
 	private SurveySection section;
 	private String typeCodeString;
 	private Translation names = new Translation();
 	private Translation descriptions = new Translation();
 
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
 
 	@ManyToOne(targetEntity = SurveySection.class, fetch=FetchType.LAZY)
 	@JoinColumn(nullable=false)
 	public SurveySection getSection() {
 		return section;
 	}
 
 	public void setSection(SurveySection section) {
 		this.section = section;
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
 	
 	@Embedded
 	@AttributeOverrides({ @AttributeOverride(name = "jsonText", column = @Column(name = "jsonNames", nullable = false)) })
 	public Translation getNames() {
 		return names;
 	}
 
 	public void setNames(Translation names) {
 		this.names = names;
 	}
 
 	@Embedded
 	@AttributeOverrides({ @AttributeOverride(name = "jsonText", column = @Column(name = "jsonDescriptions", nullable = false)) })
 	public Translation getDescriptions() {
 		return descriptions;
 	}
 
 	public void setDescriptions(Translation descriptions) {
 		this.descriptions = descriptions;
 	}
 	
 	@Transient
 	public abstract QuestionType getType();
 
 	@Transient
 	public abstract List<SurveyElement> getSurveyElements(DataLocationType type);
 	
 	@Transient
 	public abstract void removeSurveyElement(SurveyElement surveyElement);
 	
 	@Transient
 	public abstract List<SurveyElement> getSurveyElements();
 	
 	@Transient
 	public abstract Set<String> getTypeApplicable(SurveyElement surveyElement);
 
 	@Transient
 	public Survey getSurvey() {
 		return section.getSurvey();
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof SurveyQuestion))
 			return false;
 		SurveyQuestion other = (SurveyQuestion) obj;
		if (id == null) {
			if (other.id != null)
 				return false;
		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	protected abstract SurveyQuestion newInstance();
 	protected void deepCopy(SurveyQuestion copy, SurveyCloner surveyCloner) {
 		copy.setNames(new Translation(getNames()));
 		copy.setDescriptions(new Translation(getDescriptions()));
 		copy.setTypeCodeString(getTypeCodeString());
 		copy.setOrder(getOrder());
 		copy.setSection(surveyCloner.getSection(getSection()));
 	}
 
 	@Override
 	public abstract String toString();
 	
 }
