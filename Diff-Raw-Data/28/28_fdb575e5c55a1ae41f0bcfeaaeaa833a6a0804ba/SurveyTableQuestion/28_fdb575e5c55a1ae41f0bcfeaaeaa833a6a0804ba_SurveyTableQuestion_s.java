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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.chai.kevin.Translation;
 import org.chai.kevin.form.FormElement;
 import org.chai.kevin.location.DataLocationType;
 import org.chai.kevin.util.Utils;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 
 @Entity(name = "SurveyTableQuestion")
 @Table(name = "dhsst_survey_table_question")
 public class SurveyTableQuestion extends SurveyQuestion {
 
 	private Translation tableNames = new Translation();
 	private List<SurveyTableColumn> columns = new ArrayList<SurveyTableColumn>();
 	private List<SurveyTableRow> rows = new ArrayList<SurveyTableRow>();
 
 	@Embedded
 	@AttributeOverrides({ @AttributeOverride(name = "jsonText", column = @Column(name = "jsonTableNames", nullable = false)) })
 	public Translation getTableNames() {
 		return tableNames;
 	}
 
 	public void setTableNames(Translation tableNames) {
 		this.tableNames = tableNames;
 	}
 
 	@OneToMany(targetEntity = SurveyTableColumn.class, mappedBy = "question", orphanRemoval=true)
 	@Cascade({ CascadeType.ALL })
 	@Fetch(FetchMode.SELECT)
 	@OrderBy("order")
 	public List<SurveyTableColumn> getColumns() {
 		return columns;
 	}
 
 	public void setColumns(List<SurveyTableColumn> columns) {
 		this.columns = columns;
 	}
 
 	@OneToMany(targetEntity = SurveyTableRow.class, mappedBy = "question", orphanRemoval=true)
 	@Cascade({ CascadeType.ALL })
 	@Fetch(FetchMode.SELECT)
 	@OrderBy("order")
 	public List<SurveyTableRow> getRows() {
 		return rows;
 	}
 
 	public void setRows(List<SurveyTableRow> rows) {
 		this.rows = rows;
 	}
 
 	@Transient
 	@Override
 	public QuestionType getType() {
 		return QuestionType.TABLE;
 	}
 
 	public void addColumn(SurveyTableColumn column) {
 		column.setQuestion(this);
 		columns.add(column);
 		Collections.sort(columns);
 	}
 
 	public void addRow(SurveyTableRow row) {
 		row.setQuestion(this);
 		rows.add(row);
 		Collections.sort(rows);
 	}
 
 	@Transient
 	@Override
 	public List<SurveyElement> getSurveyElements() {
 		List<SurveyElement> dataElements = new ArrayList<SurveyElement>();
 		for (SurveyTableRow row : getRows()) {
 			for (SurveyTableColumn column : getColumns()) {
 				if (row.getSurveyElements().get(column) != null) dataElements.add(row.getSurveyElements().get(column));
 			}
 		}
 		return dataElements;
 	}
 
 	@Transient
 	@Override
 	public List<SurveyElement> getSurveyElements(DataLocationType type) {
 		List<SurveyElement> dataElements = new ArrayList<SurveyElement>();
 		for (SurveyTableRow row : getRows(type)) {
 			for (SurveyTableColumn column : getColumns(type)) {
 				if (row.getSurveyElements().get(column) != null) dataElements.add(row.getSurveyElements().get(column));
 			}
 		}
 		return dataElements;
 	}
 
 	@Override
 	@Transient
 	public void removeSurveyElement(SurveyElement surveyElement) {
 		for (SurveyTableRow row : getRows()) {
 			for (SurveyTableColumn column : getColumns()) {
 				if (row.getSurveyElements().get(column).equals(surveyElement)) {
 					row.getSurveyElements().remove(column);
 				}
 			}
 		}
 	}
 	
 	@Transient
 	public List<SurveyTableRow> getRows(DataLocationType type) {
 		List<SurveyTableRow> result = new ArrayList<SurveyTableRow>();
 		for (SurveyTableRow surveyTableRow : getRows()) {
 			if (Utils.split(surveyTableRow.getTypeCodeString()).contains(type.getCode()))
 				result.add(surveyTableRow);
 		}
 		return result;
 	}
 
 	@Transient
 	public List<SurveyTableColumn> getColumns(DataLocationType type) {
 		List<SurveyTableColumn> result = new ArrayList<SurveyTableColumn>();
 		for (SurveyTableColumn surveyTableColumn : getColumns()) {
 			if (Utils.split(surveyTableColumn.getTypeCodeString()).contains(type.getCode()))
 				result.add(surveyTableColumn);
 		}
 		return result;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Transient
 	public Set<String> getTypeApplicable(SurveyElement surveyElement) {
 		Set<String> columnRowOrgUnitUuIDs = new HashSet<String>();
 		
 		boolean found = false;
 		for (SurveyTableRow row : this.rows) {
 			if (row.getSurveyElements().values().contains(surveyElement)) {
 				Iterator it = row.getSurveyElements().entrySet().iterator();
 				while (it.hasNext()) {
 					Map.Entry pairs = (Map.Entry) it.next();
 					if (surveyElement.equals(pairs.getValue())) {
 						found = true;
 						columnRowOrgUnitUuIDs.addAll(
 							CollectionUtils.intersection(
 								row.getTypeApplicable(),
 								((SurveyTableColumn) pairs.getKey()).getTypeApplicable()
 							)
 						);
 					}
 				}
 			}
 		}
 
 		if (!found) throw new IllegalArgumentException("survey element does not belong to question (row-column)");
 		return new HashSet<String>(
 			CollectionUtils.intersection(
 				CollectionUtils.intersection(
 					columnRowOrgUnitUuIDs, 
 					Utils.split(this.getTypeCodeString())
 				),
 				getSection().getTypeApplicable()	
 			)
 		);
 	}
 
 	@Override
 	@Transient
 	protected SurveyTableQuestion newInstance() {
 		return new SurveyTableQuestion();
 	}
 
 	@Transient
 	protected void deepCopy(SurveyQuestion question, SurveyCloner cloner) {
 		SurveyTableQuestion copy = (SurveyTableQuestion)question;
 		super.deepCopy(copy, cloner);
 		copy.setTableNames(getTableNames());
 		Map<Long, SurveyTableColumn> columns = new HashMap<Long, SurveyTableColumn>();
 		for (SurveyTableColumn tableColumn : getColumns()) {
 			SurveyTableColumn columnCopy = tableColumn.deepCopy(cloner);
 			columns.put(tableColumn.getId(), columnCopy);
 			copy.getColumns().add(columnCopy);
 		}
 		for (SurveyTableRow tableRow : getRows()) {
 			copy.getRows().add(tableRow.deepCopy(cloner, columns));
 		}
 	}
 
 	@Override
 	public String toString() {
		return "SurveyTableQuestion [getId()=" + getId() + ", getNames()=" + getNames() + "]";
 	}
 
 
 }
