 package com.metacube.senchacon.demoapp.model.dao;
 
 import java.util.List;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.metacube.senchacon.demoapp.common.util.DAOUtils;
 import com.metacube.senchacon.demoapp.common.util.Utilities;
 import com.metacube.senchacon.demoapp.view.model.DatabaseTableFieldsView;
 import com.metacube.senchacon.demoapp.view.model.DatabaseTableView;
 
 @SuppressWarnings("unchecked")
 public class GaugeChartDAO
 {
 
 	@Autowired
 	private SessionFactory sessionFactory;
 
 	private final static Logger logger = LoggerFactory.getLogger(GaugeChartDAO.class);
 
 	public List<Object> getGaugeChartData(DatabaseTableView database, DatabaseTableFieldsView timeField, String startDate, String endDate,
 			String granularity, DatabaseTableFieldsView dataField,  String fixOrderString,
 			String filterString)
 	{
 		Session session = sessionFactory.getCurrentSession();
 		try
 		{
 			Query query = null;
 			String whereClause = null;
 			String selectClause = null;
 			String tableName = database.getTableName();
 			if (fixOrderString == null)
 			{
 				fixOrderString = "order by " + dataField + " desc";
 			}
 
 			whereClause = "where " + DAOUtils.getTimeWhereClause(timeField, granularity, startDate, endDate, "");
 			selectClause = dataField.getFieldSelection();
 			if (Utilities.verifyString(dataField.getFieldCalculation()))
 			{
 				whereClause = whereClause + " and " + dataField.getFieldCalculation();
 			}
 
 			/* Hard-coded Fix */
 			/*if (dataField.getFieldName().equalsIgnoreCase("data_4"))
 			{
 				selectClause = "count(`" + categoryField.getFieldName() + "`)";
 			}*/
 			/* Hard-coded Fix End */
 
 			if (Utilities.verifyString(filterString))
 			{
 				whereClause = whereClause + " AND " + filterString;
 			}
 
 			String queryString = null;
			queryString = "SELECT `" + "`, " + selectClause + " as "+dataField.getFieldLabel()+" from " + tableName + " " + whereClause ;
 			logger.debug("Final Query in GaugeChartDAO is==" + queryString);
 			query = session.createSQLQuery(queryString);
 			return query.list();
 		}
 		catch (HibernateException e)
 		{
 			logger.debug("Hibernate exception in GaugeChartDAO" + e);
 		}
 		return null;
 	}
 }
