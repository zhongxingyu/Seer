 /*
  * Copyright (C) 2013 Universitat Pompeu Fabra
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.gwaspi.model;
 
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.IdClass;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import org.gwaspi.constants.cNetCDF.Defaults.OPType;
 
 @Entity
 @Table(name = "operationMetadata")
 @IdClass(OperationKey.class)
 @NamedQueries({
 	@NamedQuery(
 		name = "operationMetadata_fetchById",
 		query = "SELECT om FROM OperationMetadata om WHERE om.id = :id"),
 	@NamedQuery(
 		name = "operationMetadata_fetchByNetCDFName",
 		query = "SELECT om FROM OperationMetadata om WHERE om.matrixCDFName = :netCDFName"),
 	@NamedQuery(
 		name = "operationMetadata_listByParentMatrixId",
 		query = "SELECT om FROM OperationMetadata om WHERE om.parentMatrixId = :parentMatrixId"),
 	@NamedQuery(
 		name = "operationMetadata_listByParentMatrixIdOperationId",
 		query = "SELECT om FROM OperationMetadata om WHERE om.parentMatrixId = :parentMatrixId AND om.id = :operationId"),
 	@NamedQuery(
 		name = "operationMetadata_listByParentMatrixIdParentOperationId",
 		query = "SELECT om FROM OperationMetadata om WHERE om.parentMatrixId = :parentMatrixId AND om.parentOperationId = :parentOperationId"),
 	@NamedQuery(
 		name = "operationMetadata_listByParentMatrixIdParentOperationIdOperationType",
 		query = "SELECT om FROM OperationMetadata om WHERE om.parentMatrixId = :parentMatrixId AND om.parentOperationId = :parentOperationId AND om.genotypeCode = :operationType"),
 })
 public class OperationMetadata implements Serializable {
 
 	private OperationKey key;
 	private String opName; // == Operation.friendlyName
 	private String netCDFName;
 	private OPType gtCode; // == Operation.type
 	private int parentOperationId;
 	private String description;
 	private String pathToMatrix;
 	private int opSetSize;
 	private int implicitSetSize;
 	private Date creationDate;
 
 	protected OperationMetadata() {
 
		this.key = new OperationKey();
 		this.parentOperationId = Integer.MIN_VALUE;
 		this.opName = "";
 		this.netCDFName = "";
 		this.description = "";
 		this.pathToMatrix = "";
 		this.gtCode = null;
 		this.opSetSize = Integer.MIN_VALUE;
 		this.implicitSetSize = Integer.MIN_VALUE;
 		this.creationDate = new Date();
 	}
 
 	public OperationMetadata(
 			int id,
 			MatrixKey parentMatrixKey,
 			int parentOperationId,
 			String opName,
 			String netCDFName,
 			String description,
 			String pathToMatrix,
 			OPType gtCode,
 			int opSetSize,
 			int implicitSetSize,
 			Date creationDate
 			)
 	{
 		this.key = new OperationKey(parentMatrixKey, id);
 		this.parentOperationId = parentOperationId;
 		this.opName = opName;
 		this.netCDFName = netCDFName;
 		this.description = description;
 		this.pathToMatrix = pathToMatrix;
 		this.gtCode = gtCode;
 		this.opSetSize = opSetSize;
 		this.implicitSetSize = implicitSetSize;
 		this.creationDate = (creationDate == null)
 				? null : (Date) creationDate.clone();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		final OperationMetadata other = (OperationMetadata) obj;
 		if (this.getStudyId() != other.getStudyId()) {
 			return false;
 		}
 		if (this.getParentMatrixId() != other.getParentMatrixId()) {
 			return false;
 		}
 		return (this.getOPId() == other.getOPId());
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = 7;
 		hash = 29 * hash + this.getStudyId();
 		hash = 29 * hash + this.getParentMatrixId();
 		hash = 29 * hash + this.getOPId();
 		return hash;
 	}
 
 	@Id
 	@SequenceGenerator(name = "seqOperationId")
 	@GeneratedValue(strategy = javax.persistence.GenerationType.TABLE, generator = "seqOperationId")
 	@Column(
 		name       = "id",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getId() {
 		return key.getId();
 	}
 
 	protected void setId(int id) {
 		this.key = new OperationKey(key.getParentMatrixKey(), id);
 	}
 
 	@Transient
 	public int getOPId() {
 		return getId();
 	}
 
 	@Id
 	@Column(
 		name       = "parentMatrixId",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getParentMatrixId() {
 		return key.getParentMatrixKey().getMatrixId();
 	}
 
 	protected void setParentMatrixId(int parentMatrixId) {
 		this.key = new OperationKey(new MatrixKey(
 				key.getParentMatrixKey().getStudyKey(),
 				parentMatrixId),
 				getId());
 	}
 
 	@Transient
 	public MatrixKey getParentMatrixKey() {
 		return key.getParentMatrixKey();
 	}
 
 	@Id
 	@Column(
 		name       = "studyId",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getStudyId() {
 		return key.getParentMatrixKey().getStudyKey().getId();
 	}
 
 	protected void setStudyId(int studyId) {
 		this.key = new OperationKey(new MatrixKey(
 				new StudyKey(studyId),
 				key.getParentMatrixKey().getMatrixId()),
 				getId());
 	}
 
 	@Transient
 	public StudyKey getStudyKey() {
 		return key.getParentMatrixKey().getStudyKey();
 	}
 
 	@Column(
 		name       = "opName",
 		length     = 127,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getOPName() {
 		return opName;
 	}
 
 	protected void setOPName(String opName) {
 		this.opName = opName;
 	}
 
 	@Transient
 	public String getFriendlyName() {
 		return getOPName();
 	}
 
 	@Column(
 		name       = "netCDFName",
 		length     = 255,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getMatrixCDFName() {
 		return netCDFName;
 	}
 
 	protected void setMatrixCDFName(String netCDFName) {
 		this.netCDFName = netCDFName;
 	}
 
 	@Transient
 	public String getNetCDFName() {
 		return getMatrixCDFName();
 	}
 
 	@Column(
 		name       = "gtCode",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public OPType getGenotypeCode() {
 		return gtCode;
 	}
 
 	protected void setGenotypeCode(OPType gtCode) {
 		this.gtCode = gtCode;
 	}
 
 	@Transient
 	public OPType getOperationType() {
 		return getGenotypeCode();
 	}
 
 	@Transient
 	public int getOpSetSize() {
 		return opSetSize;
 	}
 
 	public void setOpSetSize(int opSetSize) {
 		this.opSetSize = opSetSize;
 	}
 
 	@Transient
 	public String getPathToMatrix() {
 		return pathToMatrix;
 	}
 
 	public void setPathToMatrix(String pathToMatrix) {
 		this.pathToMatrix = pathToMatrix;
 	}
 
 	@Column(
 		name       = "description",
 		length     = 511,
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public String getDescription() {
 		return description;
 	}
 
 	protected void setDescription(String description) {
 		this.description = description;
 	}
 
 	@Id
 	@Column(
 		name       = "parentOperationId",
 		unique     = false,
 		nullable   = false,
 		insertable = true,
 		updatable  = false
 		)
 	public int getParentOperationId() {
 		return parentOperationId;
 	}
 
 	protected void setParentOperationId(int parentOperationId) {
 		this.parentOperationId = parentOperationId;
 	}
 
 	@Transient
 	public OperationKey getParentOperationKey() {
 		return new OperationKey(getParentMatrixKey(), getParentOperationId());
 	}
 
 	@Transient
 	public int getImplicitSetSize() {
 		return implicitSetSize;
 	}
 
 	public void setImplicitSetSize(int implicitSetSize) {
 		this.implicitSetSize = implicitSetSize;
 	}
 
 	@Temporal(TemporalType.DATE)
 	@Column(
 		name       = "creationDate",
 		unique     = false,
 		nullable   = true,
 		insertable = true,
 		updatable  = false
 		)
 	public Date getCreationDate() {
 		return (creationDate == null) ? null : (Date) creationDate.clone();
 	}
 
 	protected void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	@Transient
 	public OPType getType() {
 		return getGenotypeCode();
 	}
 }
