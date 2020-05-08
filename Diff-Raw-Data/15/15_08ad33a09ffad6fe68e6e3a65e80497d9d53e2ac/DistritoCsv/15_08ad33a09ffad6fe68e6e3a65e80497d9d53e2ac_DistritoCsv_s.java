 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.tamarillo.addressws.csv.model;
 
 import java.io.Serializable;
 
 import org.jsefa.csv.annotation.CsvDataType;
 import org.jsefa.csv.annotation.CsvField;
 
 
 /**
  * The Class Distrito.
  */
 @CsvDataType()
 public class DistritoCsv implements Serializable {
 
 	/** The Constant serialVersionUID. */
 	private static final long serialVersionUID = 4237230490548320019L;
 
 	/** The id. */
	@CsvField(pos = 1, required=true) //, constraints = "pattern=\\d{2}")
 	private String id;
 
 	/** The name. */
	@CsvField(pos = 2, required=true) //, constraints = "pattern=[A-Za-z ÁÂÀÃáâàãÊÈÉêèéÍÎÌíîìÓÔÒÕóôòõÚÛÙúûùÇç)(-]*
 	private String name;
 
 
 	/**
 	 * Gets the id.
 	 *
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * Sets the id.
 	 * 
 	 * @param id
 	 *            the id
 	 */
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	/**
 	 * Gets the name.
 	 * 
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the name.
 	 * 
 	 * @param name
 	 *            the name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!(obj instanceof DistritoCsv)) {
 			return false;
 		}
 		DistritoCsv other = (DistritoCsv) obj;
 		if (id == null) {
 			if (other.id != null) {
 				return false;
 			}
 		} else if (!id.equals(other.id)) {
 			return false;
 		}
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "Distrito [id=" + id + "]";
 	}
 
 }
