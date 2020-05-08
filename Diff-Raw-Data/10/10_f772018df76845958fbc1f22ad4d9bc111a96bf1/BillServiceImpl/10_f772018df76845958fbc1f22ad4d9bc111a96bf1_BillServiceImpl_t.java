 package cl.votainteligente.inspector.server.services;
 
 import cl.votainteligente.inspector.client.services.BillService;
 import cl.votainteligente.inspector.model.Bill;
 import cl.votainteligente.inspector.model.Category;
 import cl.votainteligente.inspector.model.Parlamentarian;
 
 import org.hibernate.*;
 import org.hibernate.criterion.*;
 
 import java.util.*;
 
 public class BillServiceImpl implements BillService {
 	private SessionFactory sessionFactory;
 
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 
 	@Override
 	public List<Bill> getAllBills() throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
 			Criteria criteria = hibernate.createCriteria(Bill.class);
 			List<Bill> bills = criteria.list();
 			hibernate.getTransaction().commit();
 			return bills;
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 
 	@Override
 	public Bill getBill(Long billId) throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
			Criteria criteria = hibernate.createCriteria(Bill.class);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.setFetchMode("initiativeType", FetchMode.JOIN);
			criteria.setFetchMode("billType", FetchMode.JOIN);
			criteria.setFetchMode("originChamber", FetchMode.JOIN);
			criteria.setFetchMode("urgency", FetchMode.JOIN);
			criteria.setFetchMode("stage", FetchMode.JOIN);
			criteria.add(Restrictions.eq("id", billId));
			Bill bill = (Bill) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 			return bill;
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 
 	@Override
 	public Bill saveBill(Bill bill) throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
 			hibernate.saveOrUpdate(bill);
 			hibernate.getTransaction().commit();
 			return bill;
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 
 	@Override
 	public void deleteBill(Bill bill) throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
 			hibernate.delete(bill);
 			hibernate.getTransaction().commit();
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 
 	@Override
 	public List<Bill> searchBills(Parlamentarian parlamentarian, Category category) throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
 			// TODO: optimize method
 			parlamentarian = (Parlamentarian) hibernate.load(Parlamentarian.class, parlamentarian.getId());
 
 			Set<Long> billIds = new HashSet<Long>();
 			List<Bill> filteredBills = new ArrayList<Bill>();
 
 			for (Bill bill : parlamentarian.getAuthoredBills()) {
 				billIds.add(bill.getId());
 			}
 
 			for (Bill bill : parlamentarian.getVotedBills()) {
 				billIds.add(bill.getId());
 			}
 
 			if (billIds.size() > 0) {
 				List<Bill> bills = new ArrayList<Bill>();
 				Criteria criteria = hibernate.createCriteria(Bill.class);
 				criteria.add(Restrictions.in("id", billIds.toArray()));
 				bills = criteria.list();
 
 				for (Bill bill : bills) {
 					for (Category billCategory : bill.getCategories())
 					{
 						if (billCategory.getId().equals(category.getId())) {
 							filteredBills.add(bill);
 						}
 					}
 				}
 			}
 			hibernate.getTransaction().commit();
 			return filteredBills;
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 }
