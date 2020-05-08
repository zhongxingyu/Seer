 package org.gwaspi.dao.jpa;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 import org.gwaspi.dao.MatrixService;
 import org.gwaspi.dao.sql.MatrixServiceImpl;
 import org.gwaspi.global.Config;
 import org.gwaspi.model.Matrix;
 import org.gwaspi.model.MatrixMetadata;
 import org.gwaspi.model.Operation;
 import org.gwaspi.model.OperationsList;
 import org.gwaspi.model.ReportsList;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * JPA implementation of a matrix service.
  * Uses abstracted DB access to store data,
  * see persistence.xml for DB settings.
  */
 public class JPAMatrixService implements MatrixService {
 
 	private static final Logger LOG
 			= LoggerFactory.getLogger(JPAMatrixService.class);
 
 	private final EntityManagerFactory emf;
 
 
 	public JPAMatrixService(EntityManagerFactory emf) {
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
 	public Matrix getById(int matrixId) throws IOException {
 
 		Matrix matrix = null;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("matrix_fetchById");
 			query.setParameter("id", matrixId);
 			matrix = (Matrix) query.getSingleResult();
 		} catch (NoResultException ex) {
 			LOG.trace("Failed fetching a matrix by id: " + matrixId
 					+ " (id not found)", ex);
 		} catch (Exception ex) {
 			LOG.trace("Failed fetching a matrix by id: " + matrixId, ex);
 		} finally {
 			close(em);
 		}
 
 		return matrix;
 	}
 
 	@Override
 	public List<Matrix> getMatrixList() throws IOException {
 
 		List<Matrix> matrices = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			matrices = em.createNamedQuery("matrix_list").getResultList();
 		} catch (Exception ex) {
 			LOG.error("Failed fetching all matrices", ex);
 		} finally {
 			close(em);
 		}
 
 		return matrices;
 	}
 
 	@Override
 	public List<Matrix> getMatrixList(int studyId) throws IOException {
 
 		List<Matrix> matrices = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("matrix_listByStudyId");
 			query.setParameter("studyId", studyId);
 			matrices = query.getResultList();
 		} catch (Exception ex) {
 			LOG.error("Failed fetching all matrices", ex);
 		} finally {
 			close(em);
 		}
 
 		return matrices;
 	}
 
 	@Override
 	public List<MatrixMetadata> getMatricesTable(int studyId) throws IOException {
 
 		List<MatrixMetadata> matricesMetadata = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("matrixMetadata_listByStudyId");
 			query.setParameter("studyId", studyId);
 			matricesMetadata = query.getResultList();
			for (int i = 0; i < matricesMetadata.size(); i++) {
				matricesMetadata.set(i, completeMatricesTable(matricesMetadata.get(i)));
			}
 		} catch (Exception ex) {
 			LOG.error("Failed fetching all matrices-metadata", ex);
 		} finally {
 			close(em);
 		}
 
 		return matricesMetadata;
 	}
 
 	@Override
 	public String createMatricesTable() {
 //		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 		return "1";
 	}
 
 	@Override
 	public void insertMatrixMetadata(MatrixMetadata matrixMetadata) throws IOException {
 
 		EntityManager em = null;
 		try {
 			em = open();
 			begin(em);
 			em.persist(matrixMetadata);
 			commit(em);
 		} catch (Exception ex) {
 			LOG.error("Failed adding a matrix-metadata", ex);
 			rollback(em);
 		} finally {
 			close(em);
 		}
 	}
 
 	@Override
 	public void deleteMatrix(int matrixId, boolean deleteReports) {
 		try {
 			Matrix matrix = getById(matrixId);
 			MatrixMetadata matrixMetadata = getMatrixMetadataById(matrixId);
 
 			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
 			genotypesFolder += "/STUDY_" + matrixMetadata.getStudyId() + "/";
 
 			// DELETE OPERATION netCDFs FROM THIS MATRIX
 			List<Operation> operations = OperationsList.getOperationsList(matrixId);
 			for (Operation op : operations) {
 				File opFile = new File(genotypesFolder + op.getNetCDFName() + ".nc");
 				org.gwaspi.global.Utils.tryToDeleteFile(opFile);
 			}
 
 			ReportsList.deleteReportByMatrixId(matrixId);
 
 			// DELETE MATRIX NETCDF FILE
 			File matrixFile = new File(genotypesFolder + matrixMetadata.getMatrixNetCDFName() + ".nc");
 			org.gwaspi.global.Utils.tryToDeleteFile(matrixFile);
 
 			// DELETE METADATA INFO FROM DB
 			boolean removed = false;
 			EntityManager em = null;
 			try {
 				em = open();
 				begin(em);
 				em.remove(matrix);
 				em.remove(matrixMetadata);
 				commit(em);
 				removed = true;
 			} catch (Exception ex) {
 				LOG.error("Failed removing a matrix", ex);
 				rollback(em);
 			} finally {
 				close(em);
 			}
 		} catch (Exception ex) {
 			LOG.error("Failed deleting matrix", ex);
 		}
 	}
 
 	@Override
 	public void saveMatrixDescription(int matrixId, String description) throws IOException {
 
 		MatrixMetadata matrixMetadata = getMatrixMetadataById(matrixId);
 		matrixMetadata.setDescription(description);
 
 		EntityManager em = null;
 		try {
 			em = open();
 			begin(em);
 			em.merge(matrixMetadata);
 			commit(em);
 		} catch (Exception ex) {
 			LOG.error("Failed adding a matrix-metadata", ex);
 			rollback(em);
 		} finally {
 			close(em);
 		}
 	}
 
 	@Override
 	public MatrixMetadata getLatestMatrixId() throws IOException {
 
 		MatrixMetadata matrixMetadata = null;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("matrixMetadata_listIds");
 			List<Integer> matrixIds = query.getResultList();
 
 			query = em.createNamedQuery("matrixMetadata_fetchById");
 			query.setParameter("id", matrixIds.get(matrixIds.size() - 1));
 			matrixMetadata = (MatrixMetadata) query.getSingleResult();
			matrixMetadata = completeMatricesTable(matrixMetadata);
 		} catch (Exception ex) {
 			LOG.trace("Failed fetching latest-matrix-metadata", ex);
 		} finally {
 			close(em);
 		}
 
 		return matrixMetadata;
 	}
 
 	@Override
 	public MatrixMetadata getMatrixMetadataById(int matrixId) throws IOException {
 
 		MatrixMetadata matrixMetadata = null;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("matrixMetadata_fetchById");
 			query.setParameter("id", matrixId);
 			matrixMetadata = (MatrixMetadata) query.getSingleResult();
			matrixMetadata = completeMatricesTable(matrixMetadata);
 		} catch (NoResultException ex) {
 			LOG.trace("Failed fetching matrix-metadata by id: " + matrixId
 					+ " (id not found)", ex);
 		} catch (Exception ex) {
 			LOG.trace("Failed fetching matrix-metadata by id: " + matrixId, ex);
 		} finally {
 			close(em);
 		}
 
 		return matrixMetadata;
 	}
 
 	@Override
 	public MatrixMetadata getMatrixMetadataByNetCDFname(String netCDFName) throws IOException {
 
 		MatrixMetadata matrixMetadata = null;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("matrixMetadata_fetchByNetCDFName");
 			query.setParameter("netCDFName", netCDFName);
 			matrixMetadata = (MatrixMetadata) query.getSingleResult();
			matrixMetadata = completeMatricesTable(matrixMetadata);
 		} catch (NoResultException ex) {
 			LOG.trace("Failed fetching matrix-metadata by netCDFname: " + netCDFName
 					+ " (id not found)", ex);
 		} catch (Exception ex) {
 			LOG.trace("Failed fetching matrix-metadata by netCDFname: " + netCDFName, ex);
 		} finally {
 			close(em);
 		}
 
 		return matrixMetadata;
 	}
 
 	@Override
 	public MatrixMetadata getMatrixMetadata(String netCDFpath, int studyId, String newMatrixName) throws IOException {
 
 		int matrixId = Integer.MIN_VALUE;
 		String matrixFriendlyName = newMatrixName;
 		String matrixNetCDFName = org.gwaspi.database.Utils.generateMatrixNetCDFNameByDate();
 		String description = "";
 		String matrixType = "";
 		Date creationDate = null;
 
 		String pathToMatrix = netCDFpath;
 		return MatrixServiceImpl.loadMatrixMetadataFromFile(matrixId, matrixFriendlyName, matrixNetCDFName, studyId, pathToMatrix, description, matrixType, creationDate);
 	}
 
	private MatrixMetadata completeMatricesTable(MatrixMetadata toCompleteMatrixMetadata) throws IOException {
		String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");
		String pathToStudy = genotypesFolder + "/STUDY_" + toCompleteMatrixMetadata.getStudyId() + "/";
		String pathToMatrix = pathToStudy + toCompleteMatrixMetadata.getMatrixNetCDFName() + ".nc";
		return MatrixServiceImpl.loadMatrixMetadataFromFile(
				toCompleteMatrixMetadata.getMatrixId(),
				toCompleteMatrixMetadata.getMatrixFriendlyName(),
				toCompleteMatrixMetadata.getMatrixNetCDFName(),
				toCompleteMatrixMetadata.getStudyId(),
				pathToMatrix,
				toCompleteMatrixMetadata.getDescription(),
				toCompleteMatrixMetadata.getMatrixType(),
				toCompleteMatrixMetadata.getCreationDate());
	}

 	@Override
 	public void shutdownBackend() throws IOException {}
 }
