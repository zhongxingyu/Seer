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
 
 package eu.trentorise.opendata.ckanalyze.model.catalog;
 
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
 
 
 
 @XmlRootElement
 /**
  * This class contains all statistics of a catalog 
  * @author Alberto Zanella <a.zanella@trentorise.eu>
  *Last modified by azanella On 25/lug/2013
  */
 public class CatalogStats {
 	private String catalogName;
 	private int totalDatasetsCount;
 	private int totalResourcesCount;
 	private long totalColsCount;
 	private double avgStringLength;
 	private long totalFileSizeCount;
 	private double avgColumnCount;
 	private double avgRowCount;
 	private List<CatalogDatatypeCount> colsPerType;
 	private double avgResourcesFileSize;
 	private List<StringDistribution> stringLengthsDistribution;
 	
 	
 	
 	public long getTotalColsCount() {
 		return totalColsCount;
 	}
 	public void setTotalColsCount(long totalColsCount) {
 		this.totalColsCount = totalColsCount;
 	}
 	public String getCatalogName() {
 		return catalogName;
 	}
 	public void setCatalogName(String catalogName) {
 		this.catalogName = catalogName;
 	}
 	public int getTotalDatasetsCount() {
 		return totalDatasetsCount;
 	}
 	public void setTotalDatasetsCount(int totalDatasetsCount) {
 		this.totalDatasetsCount = totalDatasetsCount;
 	}
 	public int getTotalResourcesCount() {
 		return totalResourcesCount;
 	}
 	public void setTotalResourcesCount(int totalResourcesCount) {
 		this.totalResourcesCount = totalResourcesCount;
 	}
 	public double getAvgStringLength() {
 		return avgStringLength;
 	}
 	public void setAvgStringLength(double avgStringLength) {
 		this.avgStringLength = avgStringLength;
 	}
 	public long getTotalFileSizeCount() {
 		return totalFileSizeCount;
 	}
 	public void setTotalFileSizeCount(long totalFileSizeCount) {
 		this.totalFileSizeCount = totalFileSizeCount;
 	}
 	public double getAvgColumnCount() {
 		return avgColumnCount;
 	}
 	public void setAvgColumnCount(double avgColumnCount) {
 		this.avgColumnCount = avgColumnCount;
 	}
 	public double getAvgRowCount() {
 		return avgRowCount;
 	}
 	public void setAvgRowCount(double avgRowCount) {
 		this.avgRowCount = avgRowCount;
 	}
 	
 	public List<CatalogDatatypeCount> getColsPerType() {
 		return colsPerType;
 	}
 	
 	@XmlElements({@XmlElement(name="datatype")})
 	@XmlElementWrapper
 	public void setColsPerType(List<CatalogDatatypeCount> colsPerType) {
 		this.colsPerType = colsPerType;
 	}
 	
 	/**
 	 * This methods returns the same content of getAvgColsPerType but organized in a map instead of an array of tuples. 
 	 * @return a map with TypeName Strings as keys and AVG of frequency  as values
 	 */
 	@XmlTransient
 	public Map<Types, Long> getColsPerTypeMap()
 	{
 		Map<Types, Long> retval = new HashMap<Types,Long>();
 		for (CatalogDatatypeCount cdc : colsPerType) {
			for (Types t : Types.values()) {
				if(t.toString().equals(cdc.getTypeName()))
				{
					retval.put(t, cdc.getCount());
				}
			}
 		}
 		return retval;
 	}
 	
 	public double getAvgResourcesFileSize() {
 		return avgResourcesFileSize;
 	}
 	public void setAvgResourcesFileSize(double avgResourcesFileSize) {
 		this.avgResourcesFileSize = avgResourcesFileSize;
 	}
 
 	/**
 	 * 
 	 * 	 * @return the a list of StringDistribution objects. Length with frequence 0 are omitted.
 	 */
 	@XmlElements({@XmlElement(name="distribution")})
 	@XmlElementWrapper
 	public List<StringDistribution> getStringLengthsDistribution() {
 		return stringLengthsDistribution;
 	}
 	public void setStringLengthsDistribution(
 			List<StringDistribution> stringLengthsDistrib) {
 		this.stringLengthsDistribution = stringLengthsDistrib;
 	}
 	
 }
