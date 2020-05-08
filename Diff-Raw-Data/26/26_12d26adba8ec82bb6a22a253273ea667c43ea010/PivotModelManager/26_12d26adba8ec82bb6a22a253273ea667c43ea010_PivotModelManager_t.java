 package com.eyeq.pivot4j.ui.primefaces;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpSession;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.eyeq.pivot4j.PivotModel;
 import com.eyeq.pivot4j.impl.PivotModelImpl;
 
@ManagedBean(name = "pivotModelManager")
 @SessionScoped
 public class PivotModelManager {
 
 	private Logger logger = LoggerFactory.getLogger(getClass());
 
 	private PivotModel model;
 
 	private String sessionId;
 
 	private String initialMdx = "select Union(Union(Crossjoin({[Gender].[All Gender]}, {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]}), Crossjoin({[Gender].[F]}, {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]})), Crossjoin({[Gender].[M]}, {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]})) ON COLUMNS, Hierarchize(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Crossjoin({[Promotion Media].[All Media]}, {([Product].[All Products], [Marital Status].[All Marital Status])}), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Drink], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Baked Goods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Baking Goods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, Union(Crossjoin({[Product].[Food].[Baking Goods].[Baking Goods]}, {[Marital Status].[All Marital Status]}), Crossjoin({[Product].[Food].[Baking Goods].[Baking Goods]}, [Marital Status].[All Marital Status].Children)))), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Baking Goods].[Jams and Jellies], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Breakfast Foods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Canned Foods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Canned Products], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Dairy], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Deli], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Eggs], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Frozen Foods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Meat], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Produce], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Seafood], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Snack Foods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Snacks], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Starchy Foods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Non-Consumable], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Non-Consumable].[Carousel], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Non-Consumable].[Checkout], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Non-Consumable].[Health and Hygiene], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Non-Consumable].[Household], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Non-Consumable].[Periodicals], [Marital Status].[All Marital Status])}))) ON ROWS from [Sales]";
 
 	@ManagedProperty(value = "#{dataSourceManager}")
 	private DataSourceManager dataSourceManager;
 
 	@PostConstruct
 	protected void initialize() {
 		if (logger.isInfoEnabled()) {
 			FacesContext context = FacesContext.getCurrentInstance();
 			HttpSession session = (HttpSession) context.getExternalContext()
 					.getSession(true);
 
 			this.sessionId = session.getId();
 
 			logger.info("Initializing new pivot model for session : "
 					+ session.getId());
 			logger.info("Initial MDX : " + initialMdx);
 		}
 
 		this.model = new PivotModelImpl(dataSourceManager.getDataSource());
 
 		model.setMdx(initialMdx);
 		model.initialize();
 	}
 
 	@PreDestroy
 	protected void destroy() {
 		if (logger.isInfoEnabled() && sessionId != null) {
 			logger.info("Destroying existing pivot model instance  : "
 					+ sessionId);
 
 			this.sessionId = null;
 		}
 
 		model.destroy();
 	}
 
 	/**
 	 * @return the model
 	 */
 	public PivotModel getModel() {
 		return model;
 	}
 
 	/**
 	 * @return the initialMdx
 	 */
 	public String getInitialMdx() {
 		return initialMdx;
 	}
 
 	/**
 	 * @param initialMdx
 	 *            the initialMdx to set
 	 */
 	public void setInitialMdx(String initialMdx) {
 		this.initialMdx = initialMdx;
 	}
 
 	/**
 	 * @return the dataSourceManager
 	 */
 	public DataSourceManager getDataSourceManager() {
 		return dataSourceManager;
 	}
 
 	/**
 	 * @param dataSourceManager
 	 *            the dataSourceManager to set
 	 */
 	public void setDataSourceManager(DataSourceManager dataSourceManager) {
 		this.dataSourceManager = dataSourceManager;
 	}
 }
