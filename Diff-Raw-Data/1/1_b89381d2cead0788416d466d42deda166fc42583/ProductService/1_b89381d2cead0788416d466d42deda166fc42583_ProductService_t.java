 package com.megalogika.sv.service;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.DirectFieldAccessor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 import org.springframework.webflow.execution.RequestContext;
 
 import com.megalogika.sv.model.Confirmation;
 import com.megalogika.sv.model.E;
 import com.megalogika.sv.model.Product;
 import com.megalogika.sv.model.ProductChange;
 import com.megalogika.sv.model.Report;
 import com.megalogika.sv.model.TimedEvent;
 import com.megalogika.sv.model.User;
 import com.megalogika.sv.service.filter.ApprovedFilter;
 
 @Service("productService")
 @Repository
 public class ProductService {
 
 	static final transient Logger logger = Logger
 			.getLogger(ProductService.class);
 
 	@PersistenceContext
 	private EntityManager em;
 
 	@Autowired
 	private EService eService;
 	@Autowired
 	private UserService userService;
 
 	public void setEntityManager(EntityManager em) {
 		this.em = em;
 	}
 
 	public Product createProduct(RequestContext ctx, User user, String barcode) {
 		return createProduct((HttpServletRequest) ctx.getExternalContext()
 				.getNativeRequest(), user, barcode);
 	}
 
 	public Product createProduct(HttpServletRequest request, User user,
 			String barcode) {
 		Product p = createProduct(request, user);
 		if (StringUtils.hasText(barcode)) {
 			p.setBarcode(Long.parseLong(barcode));
 		}
 		return p;
 	}
 
 	public Product createProduct(RequestContext ctx, User user) {
 		return createProduct((HttpServletRequest) ctx.getExternalContext()
 				.getNativeRequest(), user);
 	}
 
 	public Product createProduct(HttpServletRequest request, User user) {
 		Product product = new Product();
 
 		logger.debug("ASSIGNING USER TO PRODUCT: " + user);
 
 		product.setUser(user);
 		product.setEnteredByIp(request.getRemoteAddr() + " ("
 				+ request.getRemoteHost() + ")");
 
 		return product;
 	}
 
 	public boolean isActionPerformedByAdmin() {
 		try {
 			return User.ROLE_ADMIN.equals(userService.getCurrentUser()
 					.getRole());
 		} catch (NullPointerException e) {
 			return false;
 		}
 	}
 
 //	@Transactional
 //	public Product save(Product p) {
 //		
 //		logger.debug("---- Man atrodo manes niekas nekviecia..");
 //
 //		// if ((!userService.getCurrentUser().equals(p.getUser()) || p
 //		// .isApproved()) && !isActionPerformedByAdmin()) {
 //		// logger.error("NEGALIMA redaguoti svetimų produktų! Produktas: "
 //		// + p.getName() + " vartotojas: "
 //		// + userService.getCurrentUser().getEmail());
 //		// throw new IllegalArgumentException(
 //		// "NEGALIMA redaguoti svetimų produktų!");
 //		// }
 //
 //		// Px, galima. Paeditinu, pakonfirminu ir norma.
 //
 //		removeConfirmations(p);
 //		updateConservants(p);
 //		User u = userService.getCurrentUser();
 //		Confirmation c = new Confirmation(p, u);
 //		confirm(p, c);
 //
 //		Product ret = em.merge(p);
 //		return ret;
 //	}
 
 	@Transactional
 	public Product saveNew(Product p) {
 		Product ret = em.merge(p);
 		return ret;
 	}
 
 	@Transactional(readOnly = true)
 	public Product load(long id) {
 		return em.find(Product.class, id);
 	}
 
 	@Transactional
 	public void delete(Product p) {
 		if (!isActionPerformedByAdmin()) {
 			logger.error("NEGALIMA trinti svetimų produktų! Produktas: "
 					+ p.getName() + " vartotojas: "
 					+ userService.getCurrentUser().getEmail());
 			throw new IllegalArgumentException(
 					"NEGALIMA trinti svetimų produktų!");
 		}
 
 		em.remove(p);
 	}
 
 	@Transactional(readOnly = true)
 	public List<Product> getList(ProductSearchCriteria criteria,
 			boolean addRandomUnapprovedProduct) {
 		String qString = "select p from Product p " + criteria.getWhereClause()
 				+ "order by " + criteria.getOrderByClause();
 
 		criteria.setTotalItems(((Long) criteria.setParameters(
 				em.createQuery("select count(*) from Product p "
 						+ criteria.getWhereClause())).getSingleResult())
 				.longValue());
 
 		Product randomUnapprovedProduct = null;
 		if (addRandomUnapprovedProduct) {
 			randomUnapprovedProduct = this.getRandomUnapprovedProduct();
 		}
 
 		int itemsPerPage = (addRandomUnapprovedProduct && randomUnapprovedProduct != null) ? -1
 				: 0;
 
 		List<Product> pList = criteria
 				.setParameters(em.createQuery(qString))
 				.setFirstResult(criteria.getPage() * criteria.getItemsPerPage())
 				.setMaxResults(criteria.getItemsPerPage() + itemsPerPage)
 				.getResultList();
 
 		if (addRandomUnapprovedProduct && randomUnapprovedProduct != null) {
 			pList.add(this.getRandomUnapprovedProduct());
 			Collections.sort(pList);
 		}
 
 		return pList;
 	}
 
 	@Transactional(readOnly = true)
 	public Product getRandomUnapprovedProduct() {
 		String qString = "select p from Product p where p.approved = false order by p.entryDate "
 				+ ((Math.random() * 1) > 0.5 ? "asc" : "desc");
 		List<Product> pList = em.createQuery(qString).getResultList();
 		return (pList.size() > 0) ? pList.get((int) (Math.random() * pList
 				.size())) : null;
 	}
 
 	public List<Product> getListContainingE(ProductSearchCriteria c, List<E> l) {
 		for (E e : l) {
 			c.addEFilter(e.getId(), e.getName());
 		}
 		return getList(c, false);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Transactional(readOnly = true)
 	public List<Product> getProductList(ProductSearchCriteria criteria) {
 		Assert.notNull(criteria);
 		Assert.notNull(criteria.getRankingFilter());
 
 		String tables = " product p LEFT JOIN productcategory pc ON (p.category_id = pc.id) ";
 
 		String qString = "SELECT * "
 				+ criteria.getRankingFilter().getRankClause() + " FROM "
 				+ tables + criteria.getWhereClause() + " order by "
 				+ criteria.getRankingFilter().getRankColumn()
 				+ criteria.getOrderByClause();
 		criteria.setTotalItems(((BigInteger) criteria.setParameters(
 				em.createNativeQuery("select count(*) FROM " + tables
 						+ criteria.getWhereClause())).getSingleResult())
 				.longValue());
 
 		int itemsPerPage = criteria.getItemsPerPage();
 		if (0 == itemsPerPage) {
 			itemsPerPage = new Long(criteria.getTotalItems()).intValue();
 		}
 
 		logger.debug("items per page: " + itemsPerPage);
 		logger.debug("page: " + criteria.getPage());
 		logger.debug("first: " + criteria.getPage()
 				* criteria.getItemsPerPage());
 
 		return criteria
 				.setParameters(em.createNativeQuery(qString, Product.class))
 				.setFirstResult(criteria.getPage() * criteria.getItemsPerPage())
 				.setMaxResults(itemsPerPage).getResultList();
 
 	}
 
 	@Transactional
 	public void replace(long from, long to) {
 		E fromE = eService.load(from);
 		E toE = eService.load(to);
 
 		logger.debug("REPLACING " + fromE + " WITH " + toE);
 
 		ArrayList<Product> tmp = new ArrayList<Product>(fromE.getProducts());
 		for (Product p : tmp) {
 			p.getConservants().remove(fromE);
 			p.getConservants().add(toE);
 			fromE.getProducts().remove(p);
 			toE.getProducts().add(p);
 		}
 		fromE.getProducts().clear();
 	}
 
 	public EService getEService() {
 		return eService;
 	}
 
 	public void setEService(EService service) {
 		eService = service;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Collection<String> findCompanies(String q) {
 		return em
 				.createQuery(
 						"SELECT DISTINCT p.company FROM Product p WHERE p.approved=true AND (LOWER(p.company) LIKE :qstart OR LOWER(p.company) LIKE :qbothends)")
 				.setParameter("qstart", q + "%")
 				.setParameter("qbothends", "%" + q + "%").getResultList();
 	}
 
 	// public List<Product> getShoppingBasket(User user, SearchCriteria
 	// criteria) {
 	// List<Product> shoppingBasket = user.getShoppingBasket().getProducts();
 	// criteria.setTotalItems(shoppingBasket.size());
 	//
 	// int fromIndex = criteria.getPage() * criteria.getItemsPerPage();
 	// int toIndex = fromIndex + criteria.getItemsPerPage();
 	//
 	// return new ArrayList<Product>(shoppingBasket.subList(fromIndex, toIndex >
 	// shoppingBasket.size() ? shoppingBasket.size() : toIndex));
 	// }
 
 	public void setUserService(UserService userService) {
 		this.userService = userService;
 	}
 
 	public UserService getUserService() {
 		return userService;
 	}
 
 	public Product merge(Product p) {
 		return em.merge(p);
 	}
 
 	public void refresh(Product p) {
 		em.refresh(p);
 	}
 
 	public Product loadProduct(String id) {
 		Long productId;
 		try {
 			productId = Long.parseLong(id);
 		} catch (NumberFormatException e) {
 			logger.error("Bad product id: '" + id + "': ", e);
 			return null;
 		}
 
 		return load(productId);
 	}
 
 	@Transactional
 	public Product addView(Product p) {
 		p.setViewCount(p.getViewCount() + 1);
 
 		return p;
 	}
 
 	public Product getOneByBarcode(long barcode) {
 		List<Product> ret = getByBarcode(barcode, !isActionPerformedByAdmin());
 		if (null != ret && ret.size() > 0) {
 			return ret.get(0);
 		} else {
 			return null;
 		}
 	}
 
 	public Product getOneByBarcode(String barcode) {
 		return getOneByBarcode(Long.parseLong(barcode));
 	}
 
 	public List<Product> getByBarcode(String barcode) {
 		return getByBarcode(Long.parseLong(barcode),
 				!isActionPerformedByAdmin());
 	}
 
 	public List<Product> getByBarcode(long barcode) {
 		return getByBarcode(barcode, !isActionPerformedByAdmin());
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Product> getByBarcode(long barcode, boolean approvedOnly) {
 		try {
 			String approvedClause = "";
 			if (approvedOnly) {
 				approvedClause = new ApprovedFilter(false).getFilterClause();
 			}
 			return (List<Product>) em
 					.createQuery(
 							"select p from Product p where p.barcode = :barcode"
 									+ approvedClause)
 					.setParameter("barcode", barcode).getResultList();
 		} catch (Exception e) {
 			logger.error("getByBarcode: RETURNING NULL", e);
 			return null;
 		}
 	}
 
 	@Transactional
 	public Product updateConservants(Product p) {
 		// p.setConservants(eService.detectConservants(p.getConservantsParsed()));
 		logger.debug("----- darysim p.setConservantsText");
 		p.setConservantsText(eService.clearDuplicates(p.getConservantsText()));
 		logger.debug("----- darysim p.setConservants");
 		p.setConservants(eService.detectConservants(p.getConservantsText()));
 		logger.debug("----- darysim p.calculateHazard");
 		p.calculateHazard();
 		logger.debug("--- grazinsim produkta");
 		logger.debug("--- grazinsim produkta, su tokiais navarotais: "
 				+ p.getHazard());
 		return p;
 	}
 
 	private void removeEvents(List<? extends TimedEvent> listOfEvents) {
 		for (Iterator<? extends TimedEvent> i = listOfEvents.iterator(); i
 				.hasNext();) {
 			TimedEvent c = i.next();
 			em.remove(c);
 		}
 	}
 
 	@Transactional
 	public void confirm(Product p, Confirmation c) {
 		Assert.notNull(p);
 		Assert.notNull(c);
 
 		p.confirm(c);
 
 //		removeEvents(p.getReports());
 		p.setReports(new ArrayList<Report>());
 	}
 
 	@Transactional
 	public void report(Product p, Report r) {
 		Assert.notNull(p);
 		Assert.notNull(r);
 
 		p.addReport(r);
 		
 		logger.warn("BLOGU NUOTRAUKU REPORTAS CIA TURI SIUSTIS MEILU ADMINUI!!");
 
 //		if (p.getReports().size() >= p.getReportsRequired())
 //			removeConfirmations(p);
 	}
 
 	@Transactional
 	public void removeConfirmations(Product p) {
 		removeEvents(p.getConfirmations());
 		p.setConfirmations(new ArrayList<Confirmation>());
 		p.setConfirmationCount(0);
 	}
 
 	@Transactional
 	public void removeAdditive(Product p, E e) {
 		p.setConservantsText(p.getConservantsText().replaceAll(
 				e.getNumber() + ",? ?", ""));
 		p.getConservants().remove(e);
 	}
 
 	@Transactional
 	public void updateField(Product p, String field, Object value)
 			throws Exception {
 		DirectFieldAccessor accessor = new DirectFieldAccessor(p);
 		accessor.setPropertyValue(field, value);
 	}
 
 	@Transactional
 	public void addChange(Product p, ProductChange c) {
 		p.addChange(c);
 		logger.debug("ADDED CHANGE " + c + " TO PRODUCT " + p);
 	}
 
 	public Product loadProductByField(String field, String value) {
 		try {
 			return (Product) em
 					.createQuery(
 							"select p from Product p where p." + field
 									+ "  = :value")
 					.setParameter("value", value).setMaxResults(1)
 					.getSingleResult();
 		} catch (NoResultException nre) {
 			return null;
 		}
 	}
 
 	public Product loadProductByField(String field, long value) {
 		try {
 			return (Product) em
 					.createQuery(
 							"select p from Product p where p." + field
 									+ "  = :value")
 					.setParameter("value", value).setMaxResults(1)
 					.getSingleResult();
 		} catch (NoResultException nre) {
 			return null;
 		}
 	}
 
 	public long getMod10CheckDigit(String barcode) {
 		long total = 0;
 		for (int i = barcode.length() - 3; i >= 0; i = i - 2) {
 			total += Integer.parseInt(barcode.substring(i, i + 1)) * 1;
 		}
 		for (int i = barcode.length() - 2; i >= 0; i = i - 2) {
 			total += Integer.parseInt(barcode.substring(i, i + 1)) * 3;
 		}
 
 		long nextTen = Math.round(Math.ceil(((double) total) / 10) * 10);
 		return nextTen - total;
 	}
 
 	public boolean isValidBarcode(String barcode) {
 		if (!StringUtils.hasText(barcode)) {
 			return false;
 		}
 
 		int lastDigit = Integer
 				.parseInt(barcode.substring(barcode.length() - 1));
 		return lastDigit == getMod10CheckDigit(barcode);
 	}
 
 	@Transactional
 	public void updateProduct(Product p, String field, Object value,
 			User currentUser) throws Exception {
 		updateField(p, field, value);
 		addChange(p, new ProductChange(p, currentUser));
		updateConservants(p);
 		removeConfirmations(p);
 		confirm(p, new Confirmation(currentUser));
 	}
 
 }
