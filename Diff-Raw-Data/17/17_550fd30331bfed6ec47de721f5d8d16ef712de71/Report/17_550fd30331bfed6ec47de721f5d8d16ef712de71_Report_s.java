 package org.pescuma.buildhealth.core;
 
 import static java.util.Arrays.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Strings;
 
 public class Report {
 	
 	private final BuildStatus status;
 	private final String name;
 	private final String value;
 	private final String description;
 	private final List<Report> children;
 	
 	public Report(BuildStatus status, String name, String value, Report... children) {
 		this(status, name, value, null, asList(children));
 	}
 	
 	public Report(BuildStatus status, String name, String value, String description, Report... children) {
 		this(status, name, value, description, asList(children));
 	}
 	
 	public Report(BuildStatus status, String name, String value, String description, List<Report> children) {
 		if (status == null)
 			throw new IllegalArgumentException();
 		if (Strings.isNullOrEmpty(name))
 			throw new IllegalArgumentException();
 		if (Strings.isNullOrEmpty(value))
 			throw new IllegalArgumentException();
 		
 		this.status = status;
 		this.name = name;
 		this.value = value;
 		this.description = Objects.firstNonNull(description, "");
 		
 		List<Report> list = new ArrayList<Report>();
 		if (children != null)
 			list.addAll(children);
 		this.children = Collections.unmodifiableList(list);
 	}
 	
 	public BuildStatus getStatus() {
 		return status;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getValue() {
 		return value;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	public List<Report> getChildren() {
 		return children;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + children.hashCode();
 		result = prime * result + description.hashCode();
 		result = prime * result + name.hashCode();
 		result = prime * result + status.hashCode();
 		result = prime * result + value.hashCode();
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
 		Report other = (Report) obj;
 		if (!children.equals(other.children))
 			return false;
 		if (!description.equals(other.description))
 			return false;
 		if (!name.equals(other.name))
 			return false;
 		if (status != other.status)
 			return false;
 		if (!value.equals(other.value))
 			return false;
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		return "Report [" + status + ", " + name + "=" + value + ", desc=" + description
 				+ (children.isEmpty() ? "" : ", " + children) + "]";
 	}
 	
 	public static BuildStatus mergeBuildStatus(List<Report> reports) {
		BuildStatus status = null;
 		for (Report report : reports)
 			status = BuildStatus.merge(status, report.getStatus());
 		return status;
 	}
 }
