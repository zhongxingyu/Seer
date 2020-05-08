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
  * @author Jean Kahigiso M.
  *
  */
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.chai.kevin.Organisation;
 import org.chai.kevin.ValueService;
import org.chai.kevin.data.DataElement;
 import org.chai.kevin.value.DataValue;
 import org.springframework.transaction.annotation.Transactional;
 
 public class SurveyService {
 	//private Log log = LogFactory.getLog(SurveyService.class);
 	private ValueService valueService;
 
 	@Transactional(readOnly = true)
 	public SurveyPage getSurvey(Survey currentSurvey,
 			Organisation currentOrganisation, SurveySubStrategicObjective currentSubObjective) {
 		
 		Map<SurveyElement, DataValue> values = new LinkedHashMap<SurveyElement, DataValue>();
 		if (currentSubObjective != null) {
 			Collections.sort(currentSubObjective.getQuestions(), new SurveyQuestionSorter());
 			for (SurveyQuestion question : currentSubObjective.getQuestions()) {
 				for (SurveyElement surveyElement : question.getSurveyElements()) {
 					DataValue value = valueService.getValue(surveyElement.getDataElement(), currentOrganisation.getOrganisationUnit(), currentSurvey.getPeriod());
 					if (value == null) value = new DataValue(surveyElement.getDataElement(), currentOrganisation.getOrganisationUnit(), currentSurvey.getPeriod(), null);
 					values.put(surveyElement, value);
 				}
 			}
 		}
 		SurveyPage surveyPage = new SurveyPage(currentOrganisation.getOrganisationUnit(), currentSubObjective);
 		Map<Long, SurveyElementValue> surveyElementValues = new HashMap<Long, SurveyElementValue>();
 		for (Entry<SurveyElement, DataValue> entry : values.entrySet()) {
 			SurveyElementValue surveyElementValue = new SurveyElementValue(surveyPage, entry.getKey(), entry.getValue());
 			surveyElementValues.put(entry.getKey().getId(), surveyElementValue);
 		}
 		surveyPage.setSurveyElements(surveyElementValues);
 		return surveyPage;
 	}
 
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 
 }
