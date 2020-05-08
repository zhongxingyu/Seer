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
 
 package org.gwaspi.dao.jpa;
 
 import java.util.Collections;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.constants.cNetCDF.Defaults.OPType;
 import org.gwaspi.dao.OperationService;
 import org.gwaspi.global.Config;
 import org.gwaspi.model.MatrixOperationSpec;
import org.gwaspi.model.OperationKey;
 import org.gwaspi.model.OperationMetadata;
 import org.gwaspi.model.ReportsList;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFile;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * JPA implementation of a operation service.
  * Uses abstracted DB access to store data,
  * see persistence.xml for DB settings.
  */
 public class JPAOperationService implements OperationService {
 
 	private static final Logger LOG
 			= LoggerFactory.getLogger(JPAOperationService.class);
 
 	private final EntityManagerFactory emf;
 
 
 	public JPAOperationService(EntityManagerFactory emf) {
 		this.emf = emf;
 	}
 
 	private EntityManager open() {
 
 		EntityManager em = emf.createEntityManager();
 		return em;
 	}
 	private void begin(EntityManager em) {
 		em.getTransaction().begin();
 	}
 	private void commit(EntityManager em) {
 		em.getTransaction().commit();
 	}
 	private void rollback(EntityManager em) {
 
 		if (em == null) {
 			LOG.error("Failed to create an entity manager");
 		} else {
 			try {
 				if (em.isOpen() && em.getTransaction().isActive()) {
 					em.getTransaction().rollback();
 				} else {
 					LOG.error("Failed to rollback a transaction: no active"
 							+ " connection or transaction");
 				}
 			} catch (PersistenceException ex) {
 				LOG.error("Failed to rollback a transaction", ex);
 			}
 		}
 	}
 	private void close(EntityManager em) {
 
 		if (em == null) {
 			LOG.error("Failed to create an entity manager");
 		} else {
 			try {
 				if (em.isOpen()) {
 					em.close();
 				}
 			} catch (IllegalStateException ex) {
 				LOG.error("Failed to close an entity manager", ex);
 			}
 		}
 	}
 
 	@Override
 	public OperationMetadata getById(int operationId) throws IOException {
 
 		OperationMetadata operation = null;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("operationMetadata_fetchById");
 			query.setParameter("id", operationId);
 			operation = (OperationMetadata) query.getSingleResult();
 		} catch (NoResultException ex) {
 			LOG.error("Failed fetching a operation by id: " + operationId
 					+ " (id not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed fetching a operation by id: " + operationId, ex);
 		} finally {
 			close(em);
 		}
 
 		return operation;
 	}
 
 	@Override
 	public List<OperationMetadata> getOperationsList(int parentMatrixId, int parentOpId) throws IOException {
 
 		List<OperationMetadata> operations = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"operationMetadata_listByParentMatrixIdParentOperationId");
 			query.setParameter("parentMatrixId", parentMatrixId);
 			query.setParameter("parentOperationId", parentOpId);
 			operations = query.getResultList();
 		} catch (Exception ex) {
 			LOG.error("Failed fetching operations", ex);
 		} finally {
 			close(em);
 		}
 
 		return operations;
 	}
 
 	@Override
 	public List<OperationMetadata> getOperationsList(int parentMatrixId, int parentOpId, OPType opType) throws IOException {
 
 		List<OperationMetadata> operations = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"operationMetadata_listByParentMatrixIdParentOperationIdOperationType");
 			query.setParameter("parentMatrixId", parentMatrixId);
 			query.setParameter("parentOperationId", parentOpId);
 			query.setParameter("operationType", opType);
 			operations = query.getResultList();
 		} catch (Exception ex) {
 			LOG.error("Failed fetching operations", ex);
 		} finally {
 			close(em);
 		}
 
 		return operations;
 	}
 
 	@Override
 	public List<OperationMetadata> getOperationsTable(int parentMatrixId) throws IOException {
 
 		List<OperationMetadata> operationsMetadata = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"operationMetadata_listByParentMatrixId");
 			query.setParameter("parentMatrixId", parentMatrixId);
 			operationsMetadata = query.getResultList();
 
 			for (int i = 0; i < operationsMetadata.size(); i++) {
 				operationsMetadata.set(i, completeOperationMetadata(operationsMetadata.get(i)));
 			}
 		} catch (Exception ex) {
 			LOG.error("Failed fetching operation-metadata", ex);
 		} finally {
 			close(em);
 		}
 
 		return operationsMetadata;
 	}
 
 	@Override
 	public List<OperationMetadata> getOperationsTable(int parentMatrixId, int operationId) throws IOException {
 
 		List<OperationMetadata> operationsMetadata = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"operationMetadata_listByParentMatrixIdOperationId");
 			query.setParameter("parentMatrixId", parentMatrixId);
 			query.setParameter("operationId", operationId);
 			operationsMetadata = query.getResultList();
 
 			query = em.createNamedQuery(
 					"operationMetadata_listByParentMatrixIdParentOperationId");
 			query.setParameter("parentMatrixId", parentMatrixId);
 			query.setParameter("parentOperationId", operationId);
 			operationsMetadata.addAll(query.getResultList());
 
 			for (int i = 0; i < operationsMetadata.size(); i++) {
 				operationsMetadata.set(i, completeOperationMetadata(operationsMetadata.get(i)));
 			}
 		} catch (Exception ex) {
 			LOG.error("Failed fetching operation-metadata", ex);
 		} finally {
 			close(em);
 		}
 
 		return operationsMetadata;
 	}
 
 	@Override
 	public String createOperationsMetadataTable() {
 //		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 		return "1";
 	}
 
 	@Override
 	public void insertOPMetadata(OperationMetadata operationMetadata) throws IOException {
 
 		EntityManager em = null;
 		try {
 			em = open();
 			begin(em);
 			em.persist(operationMetadata);
 			commit(em);
 		} catch (Exception ex) {
 			LOG.error("Failed persisting operation-metadata", ex);
 			rollback(em);
 		} finally {
 			close(em);
 		}
 	}
 
 	@Override
 	public List<MatrixOperationSpec> getMatrixOperations(int matrixId) throws IOException {
 
 		List<MatrixOperationSpec> matrixOperationSpecs = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"operationMetadata_listByParentMatrixId");
 			query.setParameter("parentMatrixId", matrixId);
 			matrixOperationSpecs = query.getResultList();
 		} catch (Exception ex) {
 			LOG.error("Failed fetching matrix-operation-specs", ex);
 		} finally {
 			close(em);
 		}
 
 		return matrixOperationSpecs;
 	}
 
 	@Override
 	public void deleteOperationBranch(int studyId, int opId, boolean deleteReports) throws IOException {
 
 		try {
 			OperationMetadata op = getById(opId);
 			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
 
 			List<OperationMetadata> operations = getOperationsList(op.getParentMatrixId(), opId);
 			if (!operations.isEmpty()) {
 				operations.add(op);
 				for (int i = 0; i < operations.size(); i++) {
 					File matrixOPFile = new File(genotypesFolder + "/STUDY_" + studyId + "/" + operations.get(i).getNetCDFName() + ".nc");
 					org.gwaspi.global.Utils.tryToDeleteFile(matrixOPFile);
 					if (deleteReports) {
 						ReportsList.deleteReportByOperationId(operations.get(i).getId());
 					}
 
 					EntityManager em = null;
 					try {
 						em = open();
 						begin(em);
						final int operationId = operations.get(i).getId();
						final OperationKey operationKey = new OperationKey(studyId, op.getParentMatrixId(), operationId);
						OperationMetadata operation = em.find(OperationMetadata.class, operationKey);
 						if (operation == null) {
							throw new IllegalArgumentException("No operation found with this ID: " + operationKey.getId());
 						}
 						em.remove(operation);
 						commit(em);
 					} catch (Exception ex) {
 						LOG.error("Failed removing a matrix", ex);
 						rollback(em);
 					} finally {
 						close(em);
 					}
 				}
 			} else {
 				File matrixOPFile = new File(genotypesFolder + "/STUDY_" + studyId + "/" + op.getNetCDFName() + ".nc");
 				org.gwaspi.global.Utils.tryToDeleteFile(matrixOPFile);
 				if (deleteReports) {
 					ReportsList.deleteReportByOperationId(opId);
 				}
 
 				EntityManager em = null;
 				try {
 					em = open();
 					begin(em);
 					em.remove(getById(opId));
 					commit(em);
 				} catch (Exception ex) {
 					LOG.error("Failed removing a matrix", ex);
 					rollback(em);
 				} finally {
 					close(em);
 				}
 			}
 		} catch (IOException ex) {
 			LOG.warn(null, ex);
 			// PURGE INEXISTING OPERATIONS FROM DB
 			EntityManager em = null;
 			try {
 				em = open();
 				begin(em);
 				em.remove(getById(opId));
 				commit(em);
 			} catch (Exception exi) {
 				LOG.error("Failed removing a matrix", ex);
 				rollback(em);
 			} finally {
 				close(em);
 			}
 		} catch (IllegalArgumentException ex) {
 			LOG.warn(null, ex);
 			// PURGE INEXISTING OPERATIONS FROM DB
 			EntityManager em = null;
 			try {
 				em = open();
 				begin(em);
 				em.remove(getById(opId));
 				commit(em);
 			} catch (Exception exi) {
 				LOG.error("Failed removing a matrix", ex);
 				rollback(em);
 			} finally {
 				close(em);
 			}
 		}
 	}
 
 	@Override
 	public OperationMetadata getOperationMetadata(int operationId) throws IOException {
 
 		OperationMetadata operationMetadata = null;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("operationMetadata_fetchById");
 			query.setParameter("id", operationId);
 			operationMetadata = (OperationMetadata) query.getSingleResult();
 			operationMetadata = completeOperationMetadata(operationMetadata);
 		} catch (NoResultException ex) {
 			LOG.error("Failed fetching a operation-metadata by id: " + operationId
 					+ " (id not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed fetching a operation-metadata by id: " + operationId, ex);
 		} finally {
 			close(em);
 		}
 
 		return operationMetadata;
 	}
 
 	@Override
 	public OperationMetadata getOperationMetadata(String netCDFName) throws IOException {
 
 		OperationMetadata operationMetadata = null;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery(
 					"operationMetadata_fetchByNetCDFName");
 			query.setParameter("netCDFName", netCDFName);
 			operationMetadata = (OperationMetadata) query.getSingleResult();
 			operationMetadata = completeOperationMetadata(operationMetadata);
 		} catch (NoResultException ex) {
 			LOG.error("Failed fetching a operation-metadata by netCDF-name: " + netCDFName
 					+ " (id not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed fetching a operation-metadata by netCDF-name: " + netCDFName, ex);
 		} finally {
 			close(em);
 		}
 
 		return operationMetadata;
 	}
 
 	public static OperationMetadata completeOperationMetadata(OperationMetadata toComplete) throws IOException {
 
 		int opSetSize = Integer.MIN_VALUE;
 		int implicitSetSize = Integer.MIN_VALUE;
 
 		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
 		String pathToStudy = genotypesFolder + "/STUDY_" + toComplete.getStudyId() + "/";
 		String pathToMatrix = pathToStudy + toComplete.getMatrixCDFName() + ".nc";
 		if (new File(pathToMatrix).exists()) {
 			NetcdfFile ncfile = null;
 			try {
 				ncfile = NetcdfFile.open(pathToMatrix);
 //				gtCode = ncfile.findGlobalAttribute(cNetCDF.Attributes.GLOB_GTCODE).getStringValue();
 
 				Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_OPSET);
 				opSetSize = markerSetDim.getLength();
 
 				Dimension implicitDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_IMPLICITSET);
 				implicitSetSize = implicitDim.getLength();
 			} catch (IOException ex) {
 				LOG.error("Cannot open file: " + pathToMatrix, ex);
 			} finally {
 				if (null != ncfile) {
 					try {
 						ncfile.close();
 					} catch (IOException ex) {
 						LOG.warn("Cannot close file: " + ncfile.getLocation(), ex);
 					}
 				}
 			}
 		}
 
 		toComplete.setPathToMatrix(pathToMatrix);
 		toComplete.setOpSetSize(opSetSize);
 		toComplete.setImplicitSetSize(implicitSetSize);
 
 		return toComplete;
 	}
 }
