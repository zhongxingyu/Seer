 /* 
 * Copyright (C) allesklar.com AG
 * All rights reserved.
 *
 * Author: juergi
 * Date: 27.05.12 
 *
 */
 
 
 package com.jmelzer.data.model;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 @Entity
 @Table(name = "issue")
 public class Issue extends ModelBase implements Serializable {
 
 
     private static final long serialVersionUID = -2484476021457815764L;
 
     /** e.g. TST-123 */
     String publicId;
 
     Project project;
 
     IssueType type;
     Date creationDate;
     Date updateDate;
     Priority priority;
     User assignee;
     User reporter;
     String summary;
     String description;
     Date dueDate;
     Long remainingTime;
     Long orgEstimatedTime;
 
 
     private Set<Issue> children = new LinkedHashSet<Issue>();
     private Set<Attachment> attachments = new LinkedHashSet<Attachment>();
     private Set<Label> labels = new LinkedHashSet<Label>();
     private Set<Component> components = new LinkedHashSet<Component>();
     private Set<ProjectVersion> affectedVersions = new LinkedHashSet<ProjectVersion>();
     private Set<ProjectVersion> fixInVersions = new LinkedHashSet<ProjectVersion>();
 
 
     public Issue() {
         creationDate = new Date();
         updateDate = new Date();
     }
 
     @ManyToOne(optional = false)
     @JoinColumn(nullable = false, name = "type_id")
     public IssueType getType() {
         return type;
     }
 
     public void setType(IssueType type) {
         this.type = type;
     }
 
     @OneToMany(cascade = {CascadeType.ALL})
     @OrderBy("creationDate")
     public Set<Issue> getChildren() {
         return children;
     }
 
     public void setChildren(Set<Issue> children) {
         this.children = children;
     }
 
     @OneToMany(cascade = {CascadeType.ALL})
     public Set<Attachment> getAttachments() {
         return attachments;
     }
 
     public void setAttachments(Set<Attachment> attachments) {
         this.attachments = attachments;
     }
 
     public void addChild(Issue child) {
         if (child.getProject() == null) {
             child.setProject(getProject());
         }
         children.add(child);
     }
 
     @Column(nullable = false)
     public Date getCreationDate() {
         return creationDate;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     public Date getUpdateDate() {
         return updateDate;
     }
 
     public void setUpdateDate(Date updateDate) {
         this.updateDate = updateDate;
     }
 
     public void addAttachment(Attachment attachment) {
         attachments.add(attachment);
     }
 
     @OneToMany(cascade = {CascadeType.DETACH}, fetch = FetchType.EAGER)
     public Set<Label> getLabels() {
         return labels;
     }
 
     public void setLabels(Set<Label> labels) {
         this.labels = labels;
     }
 
     public void addLabel(Label label) {
         labels.add(label);
     }
 
     @ManyToOne
     @JoinColumn(name = "priority_id")
     public Priority getPriority() {
         return priority;
     }
 
     public void setPriority(Priority priority) {
         this.priority = priority;
     }
 
     @ManyToOne(targetEntity = User.class)
     @JoinColumn(name = "assignee_id")
     public User getAssignee() {
         return assignee;
     }
 
     @Transient
     public String getAssigneeName() {
         if (assignee != null) {
             return assignee.getName();
         }
         return null;
     }
 
     public void setAssignee(User assignee) {
         this.assignee = assignee;
     }
 
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "reporter_id")
     public User getReporter() {
         return reporter;
     }
 
     @Transient
     public String getReporterName() {
         if (reporter != null) {
             return reporter.getName();
         }
         return null;
     }
 
     public void setReporter(User reporter) {
         this.reporter = reporter;
     }
 
     @Column(name = "public_id", nullable = false, unique = true)
     public String getPublicId() {
         return publicId;
     }
 
     public void setPublicId(String publicId) {
         this.publicId = publicId;
     }
 
     @ManyToOne(optional = false, cascade = CascadeType.REMOVE)
     @JoinColumn(name = "p_id")
     public Project getProject() {
         return project;
     }
 
     public void setProject(Project project) {
         this.project = project;
     }
 
     public String getSummary() {
         return summary;
     }
 
     public void setSummary(String summary) {
         this.summary = summary;
     }
 
     @Column(name = "due")
     public Date getDueDate() {
         return dueDate;
     }
 
     public void setDueDate(Date dueDate) {
         this.dueDate = dueDate;
     }
 
     @OneToMany(cascade = CascadeType.DETACH)
     @JoinColumn(name = "component_id", nullable = true, insertable = false, updatable = false)
     public Set<Component> getComponents() {
         return components;
     }
 
     public void setComponents(Set<Component> components) {
         this.components = components;
     }
 
     public void addComponent(Component component) {
         components.add(component);
     }
 
     @OneToMany(cascade = CascadeType.DETACH)
     @JoinColumn(name = "affected_id", nullable = true, insertable = false, updatable = false)
     public Set<ProjectVersion> getAffectedVersions() {
         return affectedVersions;
     }
 
     public void setAffectedVersions(Set<ProjectVersion> affectedVersions) {
         this.affectedVersions = affectedVersions;
     }
 
     public void addAffectedVersion(ProjectVersion version) {
         affectedVersions.add(version);
     }
 
     @OneToMany(cascade = CascadeType.DETACH)
     @JoinColumn(name = "fixed_id", nullable = true, insertable = false, updatable = false)
     public Set<ProjectVersion> getFixInVersions() {
         return fixInVersions;
     }
 
     public void addFixInVersion(ProjectVersion version) {
         fixInVersions.add(version);
     }
 
     public void setFixInVersions(Set<ProjectVersion> fixInVersions) {
         this.fixInVersions = fixInVersions;
     }
 
     @Column(name = "description", length = 4048)
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     @Column(name = "remaining_time")
     public Long getRemainingTime() {
         return remainingTime;
     }
 
     public void setRemainingTime(Long remainingTime) {
         this.remainingTime = remainingTime;
     }
 
     @Column(name = "org_time")
     public Long getOrgEstimatedTime() {
         return orgEstimatedTime;
     }
 
     public void setOrgEstimatedTime(Long orgEstimatedTime) {
         this.orgEstimatedTime = orgEstimatedTime;
     }
 }
