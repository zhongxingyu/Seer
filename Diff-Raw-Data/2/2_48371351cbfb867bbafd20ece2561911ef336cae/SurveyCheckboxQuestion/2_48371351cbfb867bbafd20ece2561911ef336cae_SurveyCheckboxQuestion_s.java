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
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.chai.kevin.form.FormElement;
 import org.chai.kevin.location.DataLocationType;
 import org.chai.kevin.util.Utils;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 
 @Entity(name = "SurveyCheckboxQuestion")
 @Table(name = "dhsst_survey_checkbox_question")
 public class SurveyCheckboxQuestion extends SurveyQuestion {
 
 	List<SurveyCheckboxOption> options = new ArrayList<SurveyCheckboxOption>();
 
 	@OneToMany(targetEntity = SurveyCheckboxOption.class, mappedBy = "question")
 	@Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
 	@Fetch(FetchMode.SELECT)
 	@OrderBy("order")
 	public List<SurveyCheckboxOption> getOptions() {
 		return options;
 	}
 
 	public void setOptions(List<SurveyCheckboxOption> options) {
 		this.options = options;
 	}
 
 	public void addOption(SurveyCheckboxOption option) {
 		option.setQuestion(this);
 		options.add(option);
 		Collections.sort(options);
 	}
 
 	@Transient
 	@Override
 	public QuestionType getType() {
 		return QuestionType.CHECKBOX;
 	}
 
 	@Transient
 	@Override
 	public List<SurveyElement> getSurveyElements(DataLocationType type) {
 		List<SurveyElement> dataElements = new ArrayList<SurveyElement>();
 		for (SurveyCheckboxOption option : getOptions(type)) {
 			if (option.getSurveyElement() != null) dataElements.add(option.getSurveyElement());
 		}
 		return dataElements;
 	}
 
 	@Transient
 	@Override
 	public List<SurveyElement> getSurveyElements() {
 		List<SurveyElement> dataElements = new ArrayList<SurveyElement>();
 		for (SurveyCheckboxOption option : getOptions()) {
 			if (option.getSurveyElement() != null) dataElements.add(option.getSurveyElement());
 		}
 		return dataElements;
 	}
 
 	@Transient
 	@Override
 	public void removeSurveyElement(SurveyElement surveyElement) {
 		for (SurveyCheckboxOption option : getOptions()) {
			if (option.getSurveyElement().equals(surveyElement)) option.setSurveyElement(null);
 		}
 	}
 
 	@Transient
 	public List<SurveyCheckboxOption> getOptions(DataLocationType type) {
 		List<SurveyCheckboxOption> result = new ArrayList<SurveyCheckboxOption>();
 		for (SurveyCheckboxOption surveyCheckboxOption : getOptions()) {
 			if (Utils.split(surveyCheckboxOption.getTypeCodeString())
 					.contains(type.getCode()))
 				result.add(surveyCheckboxOption);
 		}
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Transient
 	public Set<String> getTypeApplicable(SurveyElement surveyElement) {
 		Set<String> optionOrgUnitUuIds = new HashSet<String>();
 
 		boolean found = false;
 		for (SurveyCheckboxOption option : this.getOptions()) {
 			if (surveyElement.equals(option.getSurveyElement())) {
 				found = true;
 				optionOrgUnitUuIds.addAll(CollectionUtils.intersection(
 					option.getTypeApplicable(),
 					Utils.split(this.getTypeCodeString()))
 				);
 			}
 		}
 
 		if (!found) {
 			throw new IllegalArgumentException("survey element does not belong to question (options)");
 		}
 		return new HashSet<String>(CollectionUtils.intersection(optionOrgUnitUuIds,
 				this.getSection().getTypeApplicable()));
 	}
 
 	@Override
 	protected SurveyCheckboxQuestion newInstance() {
 		return new SurveyCheckboxQuestion();
 	}
 
 	@Override
 	protected void deepCopy(SurveyQuestion question, SurveyCloner cloner) {
 		SurveyCheckboxQuestion copy = (SurveyCheckboxQuestion)question;
 		super.deepCopy(copy, cloner);
 		for (SurveyCheckboxOption option : getOptions()) {
 			copy.getOptions().add(option.deepCopy(cloner));
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "SurveyCheckboxQuestion[getId()=" + getId() + ", getNames()=" + getNames() + "]";
 	}
 
 }
