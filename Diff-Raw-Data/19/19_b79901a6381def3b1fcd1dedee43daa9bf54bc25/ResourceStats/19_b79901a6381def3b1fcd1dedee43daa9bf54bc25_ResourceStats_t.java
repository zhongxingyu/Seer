 /**
 * *****************************************************************************
 * Copyright 2012-2013 Trento Rise (www.trentorise.eu/)
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License (LGPL)
 * version 2.1 which accompanies this distribution, and is available at
 *
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *******************************************************************************
 */
 
 package eu.trentorise.opendata.ckanalyze.model.resources;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import eu.trentorise.opendata.ckanalyze.model.StringDistribution;
 import eu.trentorise.opendata.ckanalyze.model.Types;
 
 /**
  * Object containing resources statistics 
  * @author Alberto Zanella <a.zanella@trentorise.eu>
  *
  */
 @XmlRootElement
 public class ResourceStats {
 	   private String resourceId;
 	   private int rowCount;
 	   private int columnCount;
 	   private double stringLengthAvg;
 	   private String fileName;
 	   private String fileFormat;
 	   private long fileSize;
 	   private String url;
 	   private List<ResourceDatatypeCount> colsPerType;
 	   private List<StringDistribution> stringLengthsDistribution;
 	   
 	   
 	public String getResourceId() {
 		return resourceId;
 	}
 	public void setResourceId(String resourceId) {
 		this.resourceId = resourceId;
 	}
 	public int getRowCount() {
 		return rowCount;
 	}
 	public void setRowCount(int rowCount) {
 		this.rowCount = rowCount;
 	}
 	public int getColumnCount() {
 		return columnCount;
 	}
 	public void setColumnCount(int columnCount) {
 		this.columnCount = columnCount;
 	}
 	public double getStringLengthAvg() {
 		return stringLengthAvg;
 	}
 	public void setStringLengthAvg(double stringLengthAvg) {
 		this.stringLengthAvg = stringLengthAvg;
 	}
 	public String getFileName() {
 		return fileName;
 	}
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 	public String getFileFormat() {
 		return fileFormat;
 	}
 	public void setFileFormat(String fileFormat) {
 		this.fileFormat = fileFormat;
 	}
 	public String getUrl() {
 		return url;
 	}
 	public void setUrl(String url) {
 		this.url = url;
 	}
 	
 	@XmlElements({@XmlElement(name="datatype")})
 	@XmlElementWrapper
 	public List<ResourceDatatypeCount> getColsPerType() {
 		return colsPerType;
 	}
 	public void setColsPerType(List<ResourceDatatypeCount> colsPerType) {
 		this.colsPerType = colsPerType;
 	}
 	/**
 	 * This methods returns the same content of getAvgColsPerType but organized in a map instead of an array of tuples. 
 	 * @return a map with TypeName Strings as keys and frequency as values
 	 */
 	@XmlTransient
 	public Map<Types, Integer> getColsPerTypeMap()
 	{
 		Map<Types, Integer> retval = new HashMap<Types,Integer>();
 		for (ResourceDatatypeCount cdc : colsPerType) {
			retval.put(Types.valueOf(cdc.getTypeName()), cdc.getCount());
 		}
 		return retval;
 	}
 	
 	/**
 	 * 
 	 * @return the a list of StringDistribution objects. Length with frequence 0 are omitted.
 	 */
 	@XmlElements({@XmlElement(name="distribution")})
 	@XmlElementWrapper
 	public List<StringDistribution> getStringLengthsDistribution() {
 		return stringLengthsDistribution;
 	}
 	public void setStringLengthsDistribution(
 			List<StringDistribution> stringLengthsDistribution) {
 		this.stringLengthsDistribution = stringLengthsDistribution;
 	}
 	
 	/**
 	 *
 	 * @return the file size expressed in Byte
 	 */
 	public long getFileSize() {
 		return fileSize;
 	}
 	public void setFileSize(long fileSize) {
 		this.fileSize = fileSize;
 	}
 	
 	   
 
 }
