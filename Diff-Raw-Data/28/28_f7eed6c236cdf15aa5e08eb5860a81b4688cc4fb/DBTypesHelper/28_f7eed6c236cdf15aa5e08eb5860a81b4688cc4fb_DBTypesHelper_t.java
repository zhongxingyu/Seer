 /* SVN FILE: $Id: DBTypesHelper.java 5340 2012-09-27 14:48:52Z jvuccolo $ */
 package edu.psu.iam.cpr.core.database.helpers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import edu.psu.iam.cpr.core.database.SessionFactoryUtil;
 import edu.psu.iam.cpr.core.database.beans.IdentifierType;
 
 /**
  * DBTypesHelper is a singleton class that is used to load database types
  * and store them in a map.
  * 
  * Copyright 2012 The Pennsylvania State University
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * @package edu.psu.iam.cpr.core.database.helpers
  * @author $Author: jvuccolo $
  * @version $Rev: 5340 $
  * @lastrevision $Date $
  */
 public enum DBTypesHelper {
 	
 	INSTANCE;
 	
 	/** Contains the array of database tables for the various types */
 	private final String[] typeTableNames = { "IdentifierType" };
 		
 	/** Contains the index values that correspond to the table names from above. */
 	public static final int IDENTIFIER_TYPE = 0;
 	
 	/** Contains the array of hash maps of database types */
 	private final ArrayList<HashMap<String,Object>> typeMaps = new ArrayList<HashMap<String,Object>>();
 	
 	/**
 	 * This method will return a particular hashmap.
 	 * @param index contains the index of the map to be returned.
 	 * @return will contain the returned hash map.
 	 */
 	public HashMap<String,Object> getTypeMaps(int index) {
 		return typeMaps.get(index);
 	}
 	
 	/**
 	 * Constructor 
 	 * Used to initialize the Enum.
 	 */
 	private DBTypesHelper() {
 		
 		Session session = null;
 		
 		try {
 			session = SessionFactoryUtil.getSessionFactory().openSession();
 			session.getTransaction().begin();
 
 			for (int i = 0; i < typeTableNames.length; ++i) {
 
 				HashMap<String,Object> map = new HashMap<String,Object>();
 				final String sqlQuery = "from " + typeTableNames[i];
 				final Query query = session.createQuery(sqlQuery);
 
 				switch (i) {
 				case IDENTIFIER_TYPE:
 					for (final Iterator<?> it = query.list().iterator(); it.hasNext(); ) {
 						Object obj = it.next();
 						map.put(((IdentifierType) obj).getTypeName(), obj);
 					}
 					break;
 				}
 				typeMaps.add(map);
 			}
 
 
 			session.getTransaction().commit();
 		}
 		catch (HibernateException e) {
 			session.getTransaction().rollback();
 			throw new HibernateException(e);
 		}
 	}	
 }
