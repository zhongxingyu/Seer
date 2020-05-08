 /**
  * *****************************************************************************
  * Copyright 2012-2013 Trento Rise (www.trentorise.eu/)
  *
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the GNU Lesser General Public License (LGPL)
  * version 2.1 which accompanies this distribution, and is available at
  *
  * http://www.gnu.org/licenses/lgpl-2.1.html
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  *******************************************************************************
  */
 
 package eu.trentorise.opendata.ckanalyze.utility;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import eu.trentorise.opendata.ckanalyze.jpa.Catalog;
 import eu.trentorise.opendata.ckanalyze.jpa.Configuration;
 import eu.trentorise.opendata.ckanalyze.jpa.Datatype;
 import eu.trentorise.opendata.ckanalyze.jpa.Resource;
 import eu.trentorise.opendata.ckanalyze.managers.PersistencyManager;
 import eu.trentorise.opendata.ckanalyze.model.catalog.CatalogueDatatypeCount;
 
 /**
  * This is a centralized class which contains all queries to the DB.
  * @author Alberto Zanella <a.zanella@trentorise.eu>
  * Last modified by azanella On 31/lug/2013
  */
 @SuppressWarnings("unchecked")
 public final class QueryBuilder {
 	
 	
 	private QueryBuilder() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	private static Session ss;
 	public static void closeSession() {
 		if (ss == null) {
 			ss = PersistencyManager.getSessionFactory().openSession();
 		}
 		ss.close();
 	}
 
 	private static Session openSession() {
 		if ((ss == null) || (!ss.isOpen())) {
 			ss = PersistencyManager.getSessionFactory().openSession();
 		}
 		return ss;
 	}
 
 	public static Resource getResourceByCkanId(String resckanid, Catalog cat) {
 		
 		Query q = openSession()
 				.createQuery(
 						"SELECT r FROM Resource r JOIN r.catalog c WHERE r.ckanId = :ckanid AND r.catalog = :catalog");
 		q.setParameter("catalog", cat);
 		q.setParameter("ckanid", resckanid);
 		List<Resource> toCheck = q.list();
 		if (!toCheck.isEmpty()) {
 			return toCheck.get(0);
 		}
 		return null;
 	}
 
 	public static Catalog getCatalogByName(String name) {
 		Query q = openSession().createQuery(
 				"FROM Catalog c WHERE c.url = :name");
 		q.setParameter("name", name);
 		List<Catalog> toCheck = q.list();
 		if (!toCheck.isEmpty()) {
 			return toCheck.get(0);
 		}
 		return null;
 	}
 
 	public static Set<Datatype> getAllDataTypes(Catalog catalog) {
 		Set<Datatype> retval = new HashSet<Datatype>();
 		String hql = "SELECT distinct d FROM Datatype d JOIN d.colsDataTypes dt JOIN dt.resource r WHERE dt.freq > 0 AND r.catalog = :cat";
 		org.hibernate.Query q = openSession().createQuery(hql);
 		q.setParameter("cat", catalog);
		return new HashSet<Datatype>(q.list());
 	}
 
 
 	public static List<CatalogueDatatypeCount> getAllColsAvgTypes(
 			Catalog catalog) {
 		String hql = "SELECT new eu.trentorise.opendata.ckanalyze.model.catalog.CatalogueDatatypeCount(d.name, avg(dtc.freq)) FROM ResourceDatatypesCount dtc JOIN dtc.resource r JOIN dtc.datatype d WHERE r.catalog = :cat GROUP BY d.name";
 		org.hibernate.Query q = openSession().createQuery(hql);
 		q.setParameter("cat", catalog);
		return q.list();
 	}
 
 	public static Double getAvgFileSize(Catalog catalog) {
 		String hql = "SELECT avg(fileSize) from Resource r WHERE catalog = :cat";
 		org.hibernate.Query q = openSession().createQuery(hql);
 		q.setParameter("cat", catalog);
 		List<Double> retval = q.list();
 		if (!retval.isEmpty()) {
 			return retval.get(0);
 		}
 		return null;
 	}
 
 
 	public static List<Configuration> getScheduledCatalog(String catalogName) {
 		String hql = "FROM Configuration WHERE catalogHostName = :cname";
 		org.hibernate.Query q = openSession().createQuery(hql);
 		q.setParameter("cname", catalogName);
		List<Configuration> retval = q.list();
 		closeSession();
 		return retval;
 	}
 
 	public static boolean isScheduled(String catalogName) {
 		return !getScheduledCatalog(catalogName).isEmpty();
 	}
 
 	public static void scheduleCatalog(Configuration conf) {
		ss = PersistencyManager.getSessionFactory().openSession();
 		ss.beginTransaction();
 		ss.saveOrUpdate(conf);
 		ss.getTransaction().commit();
 		ss.close();
 	}
 	
 
 }
