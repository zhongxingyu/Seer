 package cl.votainteligente.inspector.server.services;
 
 import cl.votainteligente.inspector.client.services.ParlamentarianService;
 import cl.votainteligente.inspector.model.*;
 
 import org.hibernate.*;
 import org.hibernate.criterion.Disjunction;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.sql.JoinFragment;
 
 import java.util.*;
 
 public class ParlamentarianServiceImpl implements ParlamentarianService {
 	private SessionFactory sessionFactory;
 
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 
 	@Override
 	public List<Parlamentarian> getAllParlamentarians() throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
 			Criteria criteria = hibernate.createCriteria(Parlamentarian.class);
 			criteria.addOrder(Order.asc("lastName"));
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("party", FetchMode.JOIN);
 			List<Parlamentarian> parlamentarians = criteria.list();
 			hibernate.getTransaction().commit();
 			return parlamentarians;
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 
 	@Override
 	public Parlamentarian getParlamentarian(Long parlamentarianId) throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
 			Parlamentarian parlamentarian = (Parlamentarian) hibernate.get(Parlamentarian.class, parlamentarianId);
 
 			for (Society society : parlamentarian.getSocieties().keySet()) {
 				Hibernate.initialize(society);
 				Hibernate.initialize(society.getCategories());
 			}
 
 			Hibernate.initialize(parlamentarian.getParty());
 			Hibernate.initialize(parlamentarian.getParlamentarianType());
 			Hibernate.initialize(parlamentarian.getDistrict());
 			Hibernate.initialize(parlamentarian.getDistrict().getDistrictType());
 			Hibernate.initialize(parlamentarian.getPermanentCommissions());
 			Hibernate.initialize(parlamentarian.getSpecialCommissions());
 			Hibernate.initialize(parlamentarian.getSocieties());
 
 			for (Bill bill : parlamentarian.getAuthoredBills()) {
 				Hibernate.initialize(bill.getCategories());
 			}
 
 			for (Bill bill : parlamentarian.getVotedBills()) {
 				Hibernate.initialize(bill.getCategories());
 			}
 
 			for (Society society : parlamentarian.getSocieties().keySet()) {
 				Hibernate.initialize(society.getCategories());
 			}
 
 			hibernate.getTransaction().commit();
 			return parlamentarian;
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 
 	@Override
 	public Parlamentarian saveParlamentarian(Parlamentarian parlamentarian) throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
 			hibernate.saveOrUpdate(parlamentarian);
 			hibernate.getTransaction().commit();
 			return parlamentarian;
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 
 	@Override
 	public void deleteParlamentarian(Parlamentarian parlamentarian) throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
 			hibernate.delete(parlamentarian);
 			hibernate.getTransaction().commit();
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 
 	@Override
 	public List<Parlamentarian> searchParlamentarian(String keyWord) throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
 			String query = "select distinct p from Parlamentarian p left join fetch p.authoredBills left join fetch p.votedBills left join fetch p.party where ";
 			Query hQuery;
 			String filters = "";
 
 			if (keyWord != null && !keyWord.equals("")) {
 				filters += "(";
 				String[] keyWords = keyWord.split("[ ]");
 
 				for (int i = 0; i < keyWords.length; i++) {
 					keyWords[i]  = keyWords[i].replaceAll("[ÁÀáà]","a");
 					keyWords[i]  = keyWords[i].replaceAll("[ÉÈéè]","e");
 					keyWords[i]  = keyWords[i].replaceAll("[ÍÌíì]","i");
 					keyWords[i]  = keyWords[i].replaceAll("[ÓÒóò]","o");
 					keyWords[i]  = keyWords[i].replaceAll("[ÚÙúù]","u");
 					keyWords[i]  = keyWords[i].replaceAll("[Ññ]", "n");
 					keyWords[i]  = keyWords[i].replaceAll("\\W", "");
 					filters += " lower(TRANSLATE(p.firstName,'ÁáÉéÍíÓóÚúÑñ','AaEeIiOoUuNn')) like lower('%" + keyWords[i] + "%') OR";
 					filters += " lower(TRANSLATE(p.lastName,'ÁáÉéÍíÓóÚúÑñ','AaEeIiOoUuNn')) like lower('%" + keyWords[i] + "%')";
 					if (i + 1 < keyWords.length) {
 						filters += " OR";
 					}
 				}
 				filters += ")";
 			}
 			query += filters;
 
 			hQuery = hibernate.createQuery(query);
 			List<Parlamentarian> parlamentarians = hQuery.list();
 			hibernate.getTransaction().commit();
 			return parlamentarians;
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 
 	@Override
 	public List<Parlamentarian> searchParlamentarian(List<Category> categories) throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
 			List<Parlamentarian> parlamentarians = new ArrayList<Parlamentarian>();
 
 			if (categories.size() > 0) {
 
 				Criteria parlamentarianCriteria = hibernate.createCriteria(Parlamentarian.class);
 				parlamentarianCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 				parlamentarianCriteria.setFetchMode("party", FetchMode.JOIN);
 				parlamentarians = (List<Parlamentarian>)parlamentarianCriteria.list();
 
 				Set<Parlamentarian> billRelatedParlamentarians = new HashSet<Parlamentarian>();
 				Set<Parlamentarian> societyRelatedParlamentarians = new HashSet<Parlamentarian>();
 				Set<Parlamentarian> resultSet = new HashSet<Parlamentarian>();
 
 				forParlamentarian:
 				for (Parlamentarian parlamentarian : parlamentarians) {
 					for (Category category : categories) {
 						for (Society society : parlamentarian.getSocieties().keySet()) {
 							if (society.getCategories().contains(category)) {
 								societyRelatedParlamentarians.add(parlamentarian);
 							}
 						}
 					}
 
 					for (Category category : categories) {
 						for (Bill bill : parlamentarian.getAuthoredBills()) {
 							if (bill.getCategories().contains(category)) {
 								billRelatedParlamentarians.add(parlamentarian);
 								continue forParlamentarian;
 							}
 						}
 					}
 
 					for (Category category : categories) {
 						for (Bill bill : parlamentarian.getVotedBills()) {
 							if (bill.getCategories().contains(category)) {
 								billRelatedParlamentarians.add(parlamentarian);
 								continue forParlamentarian;
 							}
 						}
 					}
 				}
 
 				for (Parlamentarian parlamentarian : billRelatedParlamentarians) {
 					if (societyRelatedParlamentarians.contains(parlamentarian)) {
 						resultSet.add(parlamentarian);
 					}
 				}
 
 				parlamentarians = new ArrayList<Parlamentarian>(resultSet);
 				Collections.sort(parlamentarians, new Comparator<Parlamentarian>() {
 
 					@Override
 					public int compare(Parlamentarian o1, Parlamentarian o2) {
 						return o1.compareTo(o2);
 					}
 				});
 			}
 			hibernate.getTransaction().commit();
 			return parlamentarians;
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 
 	@Override
 	public List<Parlamentarian> getParlamentariansByBill(Bill bill) throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
 
 			bill = (Bill) hibernate.load(Bill.class, bill.getId());
 
 			Criteria criteria = hibernate.createCriteria(Parlamentarian.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("party", FetchMode.JOIN);
 			criteria.setFetchMode("authoredBills", FetchMode.JOIN);
 			criteria.setFetchMode("votedBills", FetchMode.JOIN);
 			criteria.setFetchMode("societies", FetchMode.JOIN);
 
 			// Adds subcriterias used to search in collections
 			criteria.createCriteria("authoredBills", "ab", JoinFragment.LEFT_OUTER_JOIN);
 			criteria.createCriteria("votedBills", "vb", JoinFragment.LEFT_OUTER_JOIN);
 
 			Disjunction disjunction = Restrictions.disjunction();
 			disjunction.add(Restrictions.eq("vb.id", bill.getId()));
 			disjunction.add(Restrictions.eq("ab.id", bill.getId()));
 			criteria.add(disjunction);
 
 			List<Parlamentarian> parlamentarians = criteria.list();
 			Set<Parlamentarian> resultSet = new HashSet<Parlamentarian>();
 			Set<Category> intersection = new HashSet<Category>();
 
 			for (Parlamentarian parlamentarian : parlamentarians) {
 				for (Society society : parlamentarian.getSocieties().keySet()) {
 					intersection = new HashSet<Category>(society.getCategories());
 					intersection.retainAll(bill.getCategories());
 					if (intersection.size() > 0) {
 						resultSet.add(parlamentarian);
 					}
 				}
 			}
 
 			parlamentarians = new ArrayList<Parlamentarian>(resultSet);
 			Collections.sort(parlamentarians, new Comparator<Parlamentarian>() {
 
 				@Override
 				public int compare(Parlamentarian o1, Parlamentarian o2) {
 					return o1.compareTo(o2);
 				}
 			});
 			hibernate.getTransaction().commit();
 			return parlamentarians;
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 
 	@Override
 	public List<Parlamentarian> getBillAuthors(Bill bill) throws Exception {
 		Session hibernate = sessionFactory.getCurrentSession();
 
 		try {
 			hibernate.beginTransaction();
 
 			Criteria criteria = hibernate.createCriteria(Parlamentarian.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("party", FetchMode.JOIN);
 			criteria.setFetchMode("authoredBills", FetchMode.JOIN);
 			criteria.setFetchMode("votedBills", FetchMode.JOIN);
 			criteria.createCriteria("authoredBills", "ab");
 			criteria.add(Restrictions.eq("ab.id", bill.getId()));
 
 			List<Parlamentarian> parlamentarians = criteria.list();
 			hibernate.getTransaction().commit();
 			return parlamentarians;
 		} catch (Exception ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 			}
 
 			throw ex;
 		}
 	}
 }
