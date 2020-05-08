 /*******************************************************************************
  * Copyright 2012 Brendan Gordon
  * 	
  * 	This file is part of Jira2Db.
  * 	
  * 	Jira2Db is free software: you can redistribute it and/or modify
  * 	it under the terms of the GNU General Public License as published by
  * 	the Free Software Foundation, either version 3 of the License, or
  * 	(at your option) any later version.
  * 	
  * 	Jira2Db is distributed in the hope that it will be useful,
  * 	but WITHOUT ANY WARRANTY; without even the implied warranty of
  * 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * 	GNU General Public License for more details.
  * 	
  * 	You should have received a copy of the GNU General Public License
  * 	along with Jira2Db.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package com.gordcorp.jira2db.persistence.dto;
 
 import java.util.Date;
 
 public class JiraIssueDto extends GenericDto
 {
 	Integer id;
 	String assignee;
 	Date creationDate;
 	String description;
 	String jiraUri;
 	String jiraKey;
 	String priority;
 	String project;
 	String reporter;
 	String status;
 	String summary;
 
 	Date updateDate;
 
 	public Integer getId()
 	{
 		return id;
 	}
 
 	public void setId(Integer id)
 	{
 		this.id = id;
 	}
 
 	public String getAssignee()
 	{
 		return assignee;
 	}
 
 	public void setAssignee(String assignee)
 	{
 		this.assignee = assignee;
 	}
 
 	public Date getCreationDate()
 	{
 		return creationDate;
 	}
 
 	public void setCreationDate(Date creationDate)
 	{
 		this.creationDate = creationDate;
 	}
 
 	public String getDescription()
 	{
 		return description;
 	}
 
 	public void setDescription(String description)
 	{
 		this.description = description;
 	}
 
 	public String getJiraUri()
 	{
 		return jiraUri;
 	}
 
 	public void setJiraUri(String jiraUri)
 	{
 		this.jiraUri = jiraUri;
 	}
 
 	public String getJiraKey()
 	{
 		return jiraKey;
 	}
 
 	public void setJiraKey(String jiraKey)
 	{
 		this.jiraKey = jiraKey;
 	}
 
 	public String getPriority()
 	{
 		return priority;
 	}
 
 	public void setPriority(String priority)
 	{
 		this.priority = priority;
 	}
 
 	public String getProject()
 	{
 		return project;
 	}
 
 	public void setProject(String project)
 	{
 		this.project = project;
 	}
 
 	public String getReporter()
 	{
 		return reporter;
 	}
 
 	public void setReporter(String reporter)
 	{
 		this.reporter = reporter;
 	}
 
 	public String getStatus()
 	{
		return status;
 	}
 
 	public void setStatus(String status)
 	{
 		this.status = status;
 	}
 
 	public String getSummary()
 	{
 		return summary;
 	}
 
 	public void setSummary(String summary)
 	{
 		this.summary = summary;
 	}
 
 	public Date getUpdateDate()
 	{
 		return updateDate;
 	}
 
 	public void setUpdateDate(Date updateDate)
 	{
 		this.updateDate = updateDate;
 	}
 
 	public int hashCode()
 	{
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((assignee == null) ? 0 : assignee.hashCode());
 		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
 		result = prime * result + ((description == null) ? 0 : description.hashCode());
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((jiraKey == null) ? 0 : jiraKey.hashCode());
 		result = prime * result + ((jiraUri == null) ? 0 : jiraUri.hashCode());
 		result = prime * result + ((priority == null) ? 0 : priority.hashCode());
 		result = prime * result + ((project == null) ? 0 : project.hashCode());
 		result = prime * result + ((reporter == null) ? 0 : reporter.hashCode());
 		result = prime * result + ((status == null) ? 0 : status.hashCode());
 		result = prime * result + ((summary == null) ? 0 : summary.hashCode());
 		result = prime * result + ((updateDate == null) ? 0 : updateDate.hashCode());
 		return result;
 	}
 
 	public boolean equals(Object obj)
 	{
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		JiraIssueDto other = (JiraIssueDto) obj;
 		if (assignee == null)
 		{
 			if (other.assignee != null)
 				return false;
 		}
 		else if (!assignee.equals(other.assignee))
 			return false;
 		if (creationDate == null)
 		{
 			if (other.creationDate != null)
 				return false;
 		}
 		else if (!creationDate.equals(other.creationDate))
 			return false;
 		if (description == null)
 		{
 			if (other.description != null)
 				return false;
 		}
 		else if (!description.equals(other.description))
 			return false;
 		if (id == null)
 		{
 			if (other.id != null)
 				return false;
 		}
 		else if (!id.equals(other.id))
 			return false;
 		if (jiraKey == null)
 		{
 			if (other.jiraKey != null)
 				return false;
 		}
 		else if (!jiraKey.equals(other.jiraKey))
 			return false;
 		if (jiraUri == null)
 		{
 			if (other.jiraUri != null)
 				return false;
 		}
 		else if (!id.equals(other.id))
 			return false;
 		if (priority == null)
 		{
 			if (other.priority != null)
 				return false;
 		}
 		else if (!priority.equals(other.priority))
 			return false;
 		if (project == null)
 		{
 			if (other.project != null)
 				return false;
 		}
 		else if (!project.equals(other.project))
 			return false;
 		if (reporter == null)
 		{
 			if (other.reporter != null)
 				return false;
 		}
 		else if (!reporter.equals(other.reporter))
 			return false;
 		if (status == null)
 		{
 			if (other.status != null)
 				return false;
 		}
 		else if (!status.equals(other.status))
 			return false;
 		if (status == null)
 		{
 			if (other.status != null)
 				return false;
 		}
 		else if (!status.equals(other.status))
 			return false;
 		if (summary == null)
 		{
 			if (other.summary != null)
 				return false;
 		}
 		else if (!summary.equals(other.summary))
 			return false;
 		if (updateDate == null)
 		{
 			if (other.updateDate != null)
 				return false;
 		}
 		else if (!updateDate.equals(other.updateDate))
 			return false;
 		return true;
 	}
 
 }
