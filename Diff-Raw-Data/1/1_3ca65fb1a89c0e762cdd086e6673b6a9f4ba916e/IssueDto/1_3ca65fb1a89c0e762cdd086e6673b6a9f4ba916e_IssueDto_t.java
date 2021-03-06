 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2008-2012 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.core.issue;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.sonar.api.rule.RuleKey;
 import org.sonar.api.utils.KeyValueFormat;
 
 import javax.annotation.Nullable;
 
 import java.util.Date;
 
 /**
  * @since 3.6
  */
 public final class IssueDto {
 
   private Long id;
   private String uuid;
   private Integer resourceId;
   private Integer ruleId;
   private String severity;
   private boolean manualSeverity;
   private boolean manualIssue;
   private String title;
   private String description;
   private Integer line;
   private Double cost;
   private String status;
   private String resolution;
   private String checksum;
   private String userLogin;
   private String assignee;
   private Long personId;
   private String data;
   private Date createdAt;
   private Date updatedAt;
   private Date closedAt;
 
   // joins
   private String rule;
 
   // This fields are not persited in db
   private transient String ruleRepo;
   private transient String componentKey;
 
   public Long getId() {
     return id;
   }
 
   public IssueDto setId(@Nullable Long id) {
     this.id = id;
     return this;
   }
 
   public String getUuid() {
     return uuid;
   }
 
   public IssueDto setUuid(String uuid) {
     this.uuid = uuid;
     return this;
   }
 
   public Integer getResourceId() {
     return resourceId;
   }
 
   public IssueDto setResourceId(Integer resourceId) {
     this.resourceId = resourceId;
     return this;
   }
 
   public Integer getRuleId() {
     return ruleId;
   }
 
   public IssueDto setRuleId(Integer ruleId) {
     this.ruleId = ruleId;
     return this;
   }
 
   public String getSeverity() {
     return severity;
   }
 
   public IssueDto setSeverity(@Nullable String severity) {
     this.severity = severity;
     return this;
   }
 
   public boolean isManualSeverity() {
     return manualSeverity;
   }
 
   public IssueDto setManualSeverity(boolean manualSeverity) {
     this.manualSeverity = manualSeverity;
     return this;
   }
 
   public boolean isManualIssue() {
     return manualIssue;
   }
 
   public IssueDto setManualIssue(boolean manualIssue) {
     this.manualIssue = manualIssue;
     return this;
   }
 
   public String getTitle() {
     return title;
   }
 
   public IssueDto setTitle(String title) {
     this.title = title;
     return this;
   }
 
   public String getDescription() {
     return description;
   }
 
   public IssueDto setDescription(String s) {
     this.description = s;
     return this;
   }
 
   public Integer getLine() {
     return line;
   }
 
   public IssueDto setLine(@Nullable Integer line) {
     this.line = line;
     return this;
   }
 
   public Double getCost() {
     return cost;
   }
 
   public IssueDto setCost(Double cost) {
     this.cost = cost;
     return this;
   }
 
   public String getStatus() {
     return status;
   }
 
   public IssueDto setStatus(@Nullable String status) {
     this.status = status;
     return this;
   }
 
   public String getResolution() {
     return resolution;
   }
 
   public IssueDto setResolution(@Nullable String resolution) {
     this.resolution = resolution;
     return this;
   }
 
   public String getChecksum() {
     return checksum;
   }
 
   public IssueDto setChecksum(String checksum) {
     this.checksum = checksum;
     return this;
   }
 
   public String getUserLogin() {
     return userLogin;
   }
 
   public IssueDto setUserLogin(@Nullable String userLogin) {
     this.userLogin = userLogin;
     return this;
   }
 
   public String getAssignee() {
     return assignee;
   }
 
   public IssueDto setAssignee(@Nullable String s) {
     this.assignee = s;
     return this;
   }
 
   public Long getPersonId() {
     return personId;
   }
 
   public IssueDto setPersonId(@Nullable Long personId) {
     this.personId = personId;
     return this;
   }
 
   public String getData() {
     return data;
   }
 
   public IssueDto setData(@Nullable String s) {
     Preconditions.checkArgument(s == null || s.length() <= 1000,
       "Issue data must not exceed 1000 characters: " + s);
     this.data = s;
     return this;
   }
 
   public Date getCreatedAt() {
     return createdAt;
   }
 
   public IssueDto setCreatedAt(Date createdAt) {
     this.createdAt = createdAt;
     return this;
   }
 
   public Date getUpdatedAt() {
     return updatedAt;
   }
 
   public IssueDto setUpdatedAt(@Nullable Date updatedAt) {
     this.updatedAt = updatedAt;
     return this;
   }
 
   public Date getClosedAt() {
     return closedAt;
   }
 
   public IssueDto setClosedAt(@Nullable Date closedAt) {
     this.closedAt = closedAt;
     return this;
   }
 
   public String getRule() {
     return rule;
   }
 
   public String getRuleRepo() {
     return ruleRepo;
   }
 
   public String getComponentKey() {
     return componentKey;
   }
 
   /**
    * Only for unit tests
    */
   public IssueDto setRuleKey_unit_test_only(String repo, String rule) {
     this.ruleRepo = repo;
     this.rule = rule;
     return this;
   }
 
   /**
    * Only for unit tests
    */
   public IssueDto setComponentKey_unit_test_only(String componentKey) {
     this.componentKey = componentKey;
     return this;
   }
 
   @Override
   public String toString() {
     return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
   }
 
   @Override
   public boolean equals(Object o) {
     if (this == o) {
       return true;
     }
     if (o == null || getClass() != o.getClass()) {
       return false;
     }
 
     IssueDto issueDto = (IssueDto) o;
     return !(id != null ? !id.equals(issueDto.id) : issueDto.id != null);
   }
 
   @Override
   public int hashCode() {
     return id != null ? id.hashCode() : 0;
   }
 
 
   public static IssueDto toDto(DefaultIssue issue, Integer componentId, Integer ruleId) {
     return new IssueDto()
       .setUuid(issue.key())
       .setLine(issue.line())
       .setTitle(issue.title())
       .setDescription(issue.description())
       .setCost(issue.cost())
       .setResolution(issue.resolution())
       .setStatus(issue.status())
       .setSeverity(issue.severity())
       .setChecksum(issue.getChecksum())
       .setManualIssue(issue.isManual())
       .setManualSeverity(issue.isManualSeverity())
       .setUserLogin(issue.userLogin())
       .setAssignee(issue.assignee())
       .setCreatedAt(issue.createdAt())
       .setUpdatedAt(issue.updatedAt())
       .setClosedAt(issue.closedAt())
       .setRuleId(ruleId)
       .setResourceId(componentId)
       .setData(issue.attributes() != null ? KeyValueFormat.format(issue.attributes()) : "")
       // TODO
 //        .setPersonId()
       ;
   }
 
   public DefaultIssue toDefaultIssue() {
     DefaultIssue issue = new DefaultIssue();
     issue.setKey(uuid);
     issue.setStatus(status);
     issue.setResolution(resolution);
     issue.setDescription(description);
     issue.setTitle(title);
     issue.setCost(cost);
     issue.setLine(line);
     issue.setSeverity(severity);
     issue.setUserLogin(userLogin);
     issue.setAssignee(assignee);
     issue.setCreatedAt(createdAt);
     issue.setUpdatedAt(updatedAt);
     issue.setClosedAt(closedAt);
     issue.setAttributes(KeyValueFormat.parse(Objects.firstNonNull(data, "")));
     issue.setComponentKey(componentKey);
     issue.setManual(manualIssue);
     issue.setManualSeverity(manualSeverity);
     issue.setRuleKey(RuleKey.of(ruleRepo, rule));
    issue.setNew(false);
     // TODO personId
     return issue;
   }
 }
