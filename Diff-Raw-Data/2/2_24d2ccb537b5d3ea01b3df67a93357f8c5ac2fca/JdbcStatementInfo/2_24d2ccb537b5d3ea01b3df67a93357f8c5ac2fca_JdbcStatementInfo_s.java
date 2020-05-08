 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.woozoo73.ht.jdbc;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import com.github.woozoo73.ht.ObjectInfo;
 
 @XmlRootElement(name = "statement")
 @XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "sql", "parameters" })
 public class JdbcStatementInfo implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@XmlElement(name = "sql")
 	private String sql;
 
 	private Long start;
 
 	private Long end;
 
 	@XmlAttribute
 	private Long durationNanoTime;
 
 	@XmlElementWrapper(name = "parameters")
 	@XmlElement(name = "parameter")
 	private List<ObjectInfo> parameters;
 
 	private Map<Integer, ObjectInfo> parameterMap;
 
 	@XmlElement(name = "t")
 	private ObjectInfo throwableInfo;
 
 	public void setParameter(Integer index, Object value) {
 		if (parameterMap == null) {
 			parameterMap = new TreeMap<Integer, ObjectInfo>();
 		}
 
 		parameterMap.put(index, new ObjectInfo(value));
 	}
 
 	public void fixData() {
 		if (parameterMap == null) {
 			return;
 		}
 
 		Collection<ObjectInfo> params = parameterMap.values();
 		parameters = new ArrayList<ObjectInfo>();
 		parameters.addAll(params);
 	}
 
 	public Double getDurationMiliTime() {
 		if (durationNanoTime == null) {
 			return null;
 		}
 
 		return durationNanoTime.doubleValue() / (1000 * 1000);
 	}
 
 	public void calculateDuration() {
 		if (start == null || end == null) {
 			return;
 		}
 
 		durationNanoTime = end - start;
 	}
 
 	public String getSql() {
 		return sql;
 	}
 
 	public void setSql(String sql) {
 		this.sql = sql;
 	}
 
 	public Long getStart() {
 		return start;
 	}
 
 	public void setStart(Long start) {
 		this.start = start;
 	}
 
 	public Long getEnd() {
 		return end;
 	}
 
 	public void setEnd(Long end) {
 		this.end = end;
 	}
 
 	public Long getDurationNanoTime() {
 		return durationNanoTime;
 	}
 
 	public void setDurationNanoTime(Long durationNanoTime) {
 		this.durationNanoTime = durationNanoTime;
 	}
 
 	public List<ObjectInfo> getParameters() {
 		return parameters;
 	}
 
 	public ObjectInfo getThrowableInfo() {
 		return throwableInfo;
 	}
 
 	public void setThrowableInfo(ObjectInfo throwableInfo) {
 		this.throwableInfo = throwableInfo;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("JdbcStatementInfo [sql=");
 		builder.append(sql);
 		builder.append(", start=");
 		builder.append(start);
 		builder.append(", end=");
 		builder.append(end);
 		builder.append(", durationNanoTime=");
 		builder.append(durationNanoTime);
 		builder.append(", parameters=");
 		builder.append(parameters);
 		builder.append(", parameterMap=");
 		builder.append(parameterMap);
 		builder.append(", throwableInfo=");
 		builder.append(throwableInfo);
 		builder.append("]");
 		return builder.toString();
 	}
 
 }
