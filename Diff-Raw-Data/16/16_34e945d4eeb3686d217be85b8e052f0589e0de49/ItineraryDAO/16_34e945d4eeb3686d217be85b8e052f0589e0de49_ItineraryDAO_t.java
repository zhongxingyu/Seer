 package com.tda.persistence.dao;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import com.tda.model.itinerary.Itinerary;
 import com.tda.model.itinerary.Place;
 import com.tda.model.patient.Patient;
 import com.tda.model.report.ItineraryForReport;
 
 public class ItineraryDAO extends GenericDAOImpl<Itinerary> {
 
 	@Override
 	protected Class<Itinerary> getDomainClass() {
 		return Itinerary.class;
 	}
 
 	public List<Itinerary> findItineraryForDate(Date date) {
 		DetachedCriteria crit = DetachedCriteria.forClass(Itinerary.class);
 		crit.add(Restrictions.and(Restrictions.le("beginningDate", date),
 				Restrictions.ge("endDate", date)));
 		return getHibernateTemplate().findByCriteria(crit);
 	}
 
 	public Itinerary findNextItinerary() {
 		Date currentDate = new Date();
 		List<Itinerary> currentItineraries = findItineraryForDate(currentDate);
 		if (currentItineraries.size() > 0) {
 			return currentItineraries.get(0);
 		}
 
 		DetachedCriteria crit = DetachedCriteria.forClass(Itinerary.class);
 		crit.add(Restrictions.ge("beginningDate", currentDate));
 		crit.addOrder(Order.asc("beginningDate"));
 		List<Itinerary> nextItineraries = getHibernateTemplate()
 				.findByCriteria(crit);
 		if (nextItineraries.size() > 0) {
 			return nextItineraries.get(0);
 		}
 
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Collection<ItineraryForReport> reportCollection() {
 		Collection<Itinerary> allIts = this.findAll();
 		Collection<ItineraryForReport> allItsRep = new ArrayList<ItineraryForReport>();
 
 		String DATE_FORMAT = "yyyy/MM/dd";
 		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
 
 		for (Itinerary itinerary : allIts) {
 			ItineraryForReport anIt = new ItineraryForReport();
 			anIt.setAdditionalInfo(itinerary.getAdditionalInfo());
 			anIt.setBeginningDate(itinerary.getBeginningDate());
 			anIt.setDescription(itinerary.getDescription());
 			anIt.setEndDate(itinerary.getEndDate());
 			anIt.setId(itinerary.getId());
 			anIt.setPlaces(itinerary.getPlaces());
 
 			anIt.setPlacesStr("");
 			for (Place place : itinerary.getPlaces()) {
 				anIt.setPlacesStr(anIt.getPlacesStr() + place.getCity() + ", ");
 			}
 
 			List<Patient> list = getHibernateTemplate()
 					.find("from Patient WHERE id in (select patient.id from SocialWorkerForm where fillingDate > '"
 							+ sdf.format(itinerary.getBeginningDate())
 							+ "' and fillingDate < '"
 							+ sdf.format(itinerary.getEndDate())
 							+ "') or id in ("
 							+ "select patient.id from PediatricianForm where fillingDate > '"
 							+ sdf.format(itinerary.getBeginningDate())
 							+ "' and fillingDate < '"
 							+ sdf.format(itinerary.getEndDate())
 							+ "') or id in ("
 							+ "select patient.id from NurseForm where fillingDate > '"
 							+ sdf.format(itinerary.getBeginningDate())
 							+ "' and fillingDate < '"
 							+ sdf.format(itinerary.getEndDate())
 							+ "') or id in ("
 							+ "select patient.id from DentistForm where fillingDate > '"
 							+ sdf.format(itinerary.getBeginningDate())
 							+ "' and fillingDate < '"
 							+ sdf.format(itinerary.getEndDate()) + "')");
 
 			anIt.setAttendedPatients(list.size());
 
 			allItsRep.add(anIt);
 		}
 
 		return allItsRep;
 	}
 }
