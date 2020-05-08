 /**
  *  Copyright 2012-2013 Frederik Hahne, Christoph Stiehm
  *
  * 	This file is part of csv2db.
  *
  *  csv2db is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  csv2db is distributed in the hope that it will be useful,
  * 	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with csv2db.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.peterspan.csv2db.converter;
 
 import java.io.File;
 import java.util.List;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import de.peterspan.csv2db.domain.entities.DataSet;
 import de.peterspan.csv2db.domain.entities.Location;
 import de.peterspan.csv2db.domain.entities.MeasurementValues;
 
 public class Converter extends AbstractConverter {
 
 	public Converter() {
 		super();
 	};
 
 	public Converter(File inputFile) {
 		super(inputFile);
 	
 	}
 
 	public void readLine(String[] line, Session session) {
 		DatasetLine datasetLine = new DatasetLine(line);
 		DataSet dataset = datasetLine.getDataset();
 
 		session.saveOrUpdate(dataset);
 
 		Location loc = datasetLine.getLocation();
 
 		Location sessionLoc = locationDao.getByLocationNumber(session,
 				loc.getLocationNumber());
 
 		if (sessionLoc != null) {
 			dataset.setLocation(sessionLoc);
 			session.saveOrUpdate(dataset);
 		} else {
 			session.saveOrUpdate(loc);
 			dataset.setLocation(loc);
 		}
 
 		MeasurementValues values = datasetLine.getValues();
 
 		session.saveOrUpdate(values);
 
 		dataset.setMeasurementValues(values);
 		session.saveOrUpdate(dataset);
 
 	}
 
 	@Override
 	protected Void doInBackground() throws Exception {
 		Session session = null;
 		Transaction tx = null;
 		try {
 			session = sessionFactory.openSession();
 			tx = session.beginTransaction();
 			List<String[]> allLines = readFile();
 
 			double increment = 100.0 / allLines.size();
 			double progress = 0.0;
 			for (String[] line : allLines) {
 				progress = progress + increment;
 				setProgress((int)Math.round(progress));
 				
				if (line[0].equals("Standort-Nr.")) {
 					continue;
 				}
 				if (line[0].equals("")) {
 					continue;
 				}
 				readLine(line, session);
 			}
 
 			session.flush();
 			tx.commit();
 		} catch (HibernateException he) {
 			if(tx != null){
 				tx.rollback();
 			}
 		}finally{
 			if(session != null){
 				session.close();
 			}
 		}
 		return null;
 	}
 
 }
