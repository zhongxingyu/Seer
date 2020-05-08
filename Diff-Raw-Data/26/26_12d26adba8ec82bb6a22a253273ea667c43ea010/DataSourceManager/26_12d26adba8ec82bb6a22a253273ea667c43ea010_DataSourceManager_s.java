 package com.eyeq.pivot4j.ui.primefaces;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ApplicationScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.FacesContext;
 import javax.servlet.ServletContext;
 
 import org.olap4j.OlapDataSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.eyeq.pivot4j.datasource.SimpleOlapDataSource;
 
@ManagedBean(eager = true)
 @ApplicationScoped
 public class DataSourceManager {
 
 	private Logger logger = LoggerFactory.getLogger(getClass());
 
 	private OlapDataSource dataSource;
 
 	@PostConstruct
 	protected void initialize() {
 		FacesContext facesContext = FacesContext.getCurrentInstance();
 		ServletContext servletContext = (ServletContext) facesContext
 				.getExternalContext().getContext();
 
 		String database = servletContext.getRealPath("/WEB-INF/foodmart");
 		String schema = servletContext.getRealPath("/WEB-INF/FoodMart.xml");
 
 		if (logger.isInfoEnabled()) {
 			logger.info("Starting Pivot4J JSF Sample application.");
 			logger.info("	- database path : " + database);
 			logger.info("	- schema path : " + schema);
 		}
 
 		String driverName = "mondrian.olap4j.MondrianOlap4jDriver";
 		try {
 			Class.forName(driverName);
 		} catch (ClassNotFoundException e) {
 			String msg = "Failed to load JDBC driver : " + driverName;
 			throw new RuntimeException(msg, e);
 		}
 
 		StringBuilder builder = new StringBuilder();
 		builder.append("jdbc:mondrian:");
 		builder.append("Jdbc=jdbc:derby:");
 		builder.append(database);
 		builder.append(";");
 		builder.append("JdbcDrivers=org.apache.derby.jdbc.EmbeddedDriver;");
 		builder.append("JdbcUser=sa;");
 		builder.append("Catalog=file:");
 		builder.append(schema);
 		builder.append(";");
 
 		String url = builder.toString();
 
 		SimpleOlapDataSource dataSource = new SimpleOlapDataSource();
 		dataSource.setConnectionString(url);
 
 		this.dataSource = dataSource;
 
 		if (logger.isInfoEnabled()) {
 			logger.info("Pivot4J JSF Sample has been initialized successfully.");
 		}
 	}
 
 	/**
 	 * @return the dataSource
 	 */
 	public OlapDataSource getDataSource() {
 		return dataSource;
 	}
 }
