 package org.gwaspi.dao.jpa;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 import org.gwaspi.dao.ReportService;
 import org.gwaspi.dao.sql.ReportServiceImpl;
 import org.gwaspi.model.Operation;
 import org.gwaspi.model.Report;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * JPA implementation of a report service.
  * Uses abstracted DB access to store data,
  * see persistence.xml for DB settings.
  */
 public class JPAReportService implements ReportService {
 
 	private static final Logger LOG
 			= LoggerFactory.getLogger(JPAReportService.class);
 
 	private final EntityManagerFactory emf;
 
 
 	public JPAReportService(EntityManagerFactory emf) {
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
 	public Report getById(int reportId) throws IOException {
 
 		Report report = null;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("report_fetchById");
 			query.setParameter("id", reportId);
 			report = (Report) query.getSingleResult();
 		} catch (NoResultException ex) {
 			LOG.error("Failed fetching a report by id: " + reportId
 					+ " (id not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed fetching a report by id: " + reportId, ex);
 		} finally {
 			close(em);
 		}
 
 		return report;
 	}
 
 	@Override
 	public List<Report> getReportsList(int parentOperationId, int parentMatrixId) throws IOException {
 
 		List<Report> reports = Collections.EMPTY_LIST;
 
 		EntityManager em = null;
 		try {
 			em = open();
			Query query = em.createNamedQuery(
					"report_fetchByParentOperationIdParentMatrixId");
			query.setParameter("parentOperationId", parentOperationId);
			query.setParameter("parentMatrixId", parentMatrixId);
 			reports = (List<Report>) query.getResultList();
 		} catch (NoResultException ex) {
 			LOG.error("Failed fetching a report by"
 					+ ": parent-operation-id: " + parentOperationId
 					+ ", parent-matrix-id: " + parentMatrixId
 					+ "; (not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed fetching report", ex);
 		} finally {
 			close(em);
 		}
 
 		return reports;
 	}
 
 	@Override
 	public String getReportNamePrefix(Operation op) {
 		return ReportServiceImpl.extractReportNamePrefix(op);
 	}
 
 	@Override
 	public String createReportsMetadataTable() {
 //		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 		return "1";
 	}
 
 	@Override
 	public void insertRPMetadata(Report report) {
 
 		EntityManager em = null;
 		try {
 			em = open();
 			begin(em);
 			em.persist(report);
 			commit(em);
 		} catch (Exception ex) {
 			LOG.error("Failed adding a report", ex);
 			rollback(em);
 		} finally {
 			close(em);
 		}
 	}
 
 	@Override
 	public void deleteReportByMatrixId(int parentMatrixId) throws IOException {
 
 		int deleted = 0;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("report_deleteByParentMatrixId");
 			query.setParameter("parentMatrixId", parentMatrixId);
 			deleted = query.executeUpdate();
 		} catch (NoResultException ex) {
 			LOG.error("Failed deleting reports by"
 					+ ": parent-matrix-id: " + parentMatrixId
 					+ "; (not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed deleting reports", ex);
 		} finally {
 			close(em);
 		}
 	}
 
 	@Override
 	public void deleteReportByOperationId(int parentOperationId) throws IOException {
 
 		int deleted = 0;
 
 		EntityManager em = null;
 		try {
 			em = open();
 			Query query = em.createNamedQuery("report_deleteByParentOperationId");
 			query.setParameter("parentOperationId", parentOperationId);
 			deleted = query.executeUpdate();
 		} catch (NoResultException ex) {
 			LOG.error("Failed deleting reports by"
 					+ ": parent-operation-id: " + parentOperationId
 					+ "; (not found)", ex);
 		} catch (Exception ex) {
 			LOG.error("Failed deleting reports", ex);
 		} finally {
 			close(em);
 		}
 	}
 }
