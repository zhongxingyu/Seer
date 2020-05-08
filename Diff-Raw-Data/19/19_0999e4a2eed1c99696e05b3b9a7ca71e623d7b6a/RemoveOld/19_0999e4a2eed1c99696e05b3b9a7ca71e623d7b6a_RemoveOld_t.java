 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  John Lawson
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at 
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package org.nof1trial.nof1.server.cron;
 
 import java.io.IOException;
 import java.io.PrintWriter;
import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.nof1trial.nof1.server.Config;
 import org.nof1trial.nof1.server.Data;
 import org.nof1trial.nof1.server.EMF;
 
 /**
  * @author John Lawson
  * 
  */
 public class RemoveOld extends HttpServlet {
 
 	private static final long serialVersionUID = -2368998686028490735L;
 
 	private static final Logger log = Logger.getLogger(RemoveOld.class.getName());
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
 		final PrintWriter writer = resp.getWriter();
 
 		writer.println("Searching datastore for trials older than 2 years");
 		log.info("Searching datastore for trials older than 2 years");
 
 		final List<Config> list = getOldConfigs(2);
 
 		for (Config conf : list) {
 			writer.println("Deleting data for patient: " + conf.getPatientEmail() + " and doctor: " + conf.getDocEmail());
 			log.warning("Deleting data for patient: " + conf.getPatientEmail() + " and doctor: " + conf.getDocEmail());
 
 			deleteDateFromConfig(conf);
 
 			Config.delete(conf);
 
 		}
 
 		writer.println("Cron finished");
 		log.info("Cron finished");
 
 	}
 
 	/** Get a list of all config entities whose trial ended some number of years ago */
 	private List<Config> getOldConfigs(int year) {
 		final EntityManager em = EMF.get().createEntityManager();
 
 		try {
 
 			final Query query = em.createQuery("select x from Config x where x.endDate < :date");
 			final Calendar cal = Calendar.getInstance();
 			cal.add(Calendar.YEAR, -year);
 			query.setParameter("date", cal.getTimeInMillis());
 			// Set max result just to keep data store transactions low each day
 			// Doesn't really matter whether data is deleted exactly on time or a bit late
 			query.setMaxResults(10);
 
 			@SuppressWarnings("unchecked")
 			final List<Config> list = query.getResultList();
 			// Force list to materialise
 			list.size();
 
 			return list;
 
 		} finally {
 			em.close();
 		}
 
 	}
 
 	/** Delete all data from the trial specified by the config entity */
	@SuppressWarnings("unchecked")
 	private void deleteDateFromConfig(Config conf) {
 
 		final EntityManager dataEm = EMF.get().createEntityManager();
		List<Data> dataList = new ArrayList<Data>();

 		try {
 			final Query dataQuery = dataEm
 					.createQuery("select x from Data x where x.time < :time and x.patientEmail = :patient and x.doctorEmail = :doctor");
 			dataQuery.setParameter("time", conf.getEndDate()).setParameter("patient", conf.getPatientEmail())
 					.setParameter("doctor", conf.getDocEmail());
 
			dataList.addAll(dataQuery.getResultList());
 
 			// Force list to materialise
 			dataList.size();
 
 			log.warning("Deleting " + dataList.size() + " number of data");
 		} finally {
 
 			dataEm.close();
 		}

		for (Data data : dataList) {
			data.remove();
		}
 	}
 }
