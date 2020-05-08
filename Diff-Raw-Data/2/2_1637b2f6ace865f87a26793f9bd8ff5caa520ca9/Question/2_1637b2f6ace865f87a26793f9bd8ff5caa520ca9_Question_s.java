 /*
  * Copyright (C) 2013 headissue GmbH (www.headissue.com)
  *
  * Source repository: https://github.com/headissue/pigeon
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This patch is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this patch.  If not, see <http://www.gnu.org/licenses/agpl.txt/>.
  */
 package com.headissue.pigeon.survey;
 
 import com.headissue.pigeon.service.Adapter;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 import java.util.ArrayList;
 import java.util.List;
 
 @Entity
 @Table(name = "pigeon_question")
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(propOrder = {"id", "orderBy", "title", "text", "type", "answers"})
 public class Question {
 
   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pigeon_sequence")
   @Column(name = "question_id")
   private int id;
 
   @Column(name = "order_by", nullable = false)
   private int orderBy;
 
   @Column(name = "title", nullable = false, columnDefinition = "VARCHAR")
   private String title;
 
   @Column(name = "text", nullable = true, columnDefinition = "VARCHAR")
   private String text;
 
   @Column(name = "type", columnDefinition = "VARCHAR(20)")
   @XmlJavaTypeAdapter(Adapter.QuestionTypeAdapter.class)
   private String type;
 
   @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy = "question")
   @OrderBy("orderBy ASC")
   private List<QuestionText> answers = new ArrayList<QuestionText>();
 
   @ManyToOne(cascade = {CascadeType.ALL})
  @JoinColumn(name = "survery_id", nullable = false)
   @XmlTransient
   private Survey survey;
 
   public Question() {
   }
 
   public int getId() {
     return id;
   }
 
   public void setId(int id) {
     this.id = id;
   }
 
   public int getOrderBy() {
     return orderBy;
   }
 
   public void setOrderBy(int orderBy) {
     this.orderBy = orderBy;
   }
 
   public String getTitle() {
     return title;
   }
 
   public void setTitle(String title) {
     this.title = title;
   }
 
   public String getText() {
     return text;
   }
 
   public void setText(String text) {
     this.text = text;
   }
 
   public String getType() {
     return type;
   }
 
   public void setType(String type) {
     this.type = type;
   }
 
   public List<QuestionText> getAnswers() {
     return answers;
   }
 
   public void setAnswers(List<QuestionText> answers) {
     this.answers = answers;
   }
 
   public Survey getSurvey() {
     return survey;
   }
 
   public void setSurvey(Survey survey) {
     this.survey = survey;
   }
 
   public void addAnswer(String text, int orderBy) {
     QuestionText answer = new QuestionText();
     answer.setText(text);
     answer.setOrderBy(orderBy);
     addAnswer(answer);
   }
 
   public void addAnswer(QuestionText answer) {
     if (answer.getOrderBy() <= 0) {
       int orderBy = answers.size() + 1;
       answer.setOrderBy(orderBy);
     }
     answers.add(answer);
     answer.setQuestion(this);
   }
 
   @Override
   public String toString() {
     final StringBuilder sb = new StringBuilder();
     sb.append("Question");
     sb.append("{id=").append(id);
     sb.append(", orderBy=").append(orderBy);
     sb.append(", title='").append(title).append('\'');
     sb.append(", text='").append(text).append('\'');
     sb.append(", type=").append(type);
     sb.append(", answers=[").append(answers != null ? answers.size() : -1).append("]");
     sb.append(", survey=[").append(survey != null ? survey.getId() : -1).append("]");
     sb.append('}');
     return sb.toString();
   }
 }
