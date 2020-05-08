 package org.chai.kevin.value;
 
 /* 
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
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import org.chai.kevin.data.DataElement;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.NaturalId;
 import org.hisp.dhis.organisationunit.OrganisationUnit;
 import org.hisp.dhis.period.Period;
 
 @Entity(name="DataValue")
 @Table(name="datavalue",
 		uniqueConstraints=@UniqueConstraint(columnNames={"dataElement", "period", "organisationUnit"})
 )
 @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
 public class DataValue extends AbstractValue {
 	
 //	private enum Status {COLLECTED, AGGREGATED, AGGREGATED_MISSING, NOT_AGGREGATABLE}
 	
 	private Long id;
 	
 	private DataElement dataElement;
 	
 	public DataValue() {}
 
 	public DataValue(DataElement dataElement, OrganisationUnit organisationUnit, 
 			Period period, String value) {
 		this.dataElement = dataElement;
 		this.organisationUnit = organisationUnit;
 		this.period = period;
 		this.value = value;
 	}
 	
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 
 	@NaturalId
 	@ManyToOne(targetEntity=DataElement.class, optional=false)
 	@JoinColumn(nullable=false)
 	public DataElement getDataElement() {
 		return dataElement;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	public void setDataElement(DataElement dataElement) {
 		this.dataElement = dataElement;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result
 				+ ((dataElement == null) ? 0 : dataElement.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		DataValue other = (DataValue) obj;
 		if (dataElement == null) {
 			if (other.dataElement != null)
 				return false;
 		} else if (!dataElement.equals(other.dataElement))
 			return false;
 		return true;
 	}

	@Override
	public String toString() {
		return "DataValue [dataElement=" + dataElement + ", organisationUnit="
				+ organisationUnit + ", period=" + period + ", value=" + value
				+ "]";
	}
 	
 }
