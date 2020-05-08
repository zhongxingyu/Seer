 package nz.ac.victoria.ecs.kpsmart.reporting.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import nz.ac.victoria.ecs.kpsmart.InjectOnCall;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Location;
 import nz.ac.victoria.ecs.kpsmart.entities.state.MailDelivery;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Price;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Priority;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Route;
 import nz.ac.victoria.ecs.kpsmart.logging.ReadOnlyLog;
 import nz.ac.victoria.ecs.kpsmart.reporting.Report;
 import nz.ac.victoria.ecs.kpsmart.state.ReadOnlyState;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 
 /**
  * A default implementation of a report.
  * 
  * @author hodderdani
  *
  */
 public class DefaultReport implements Report {
 	@Inject
 	private ReadOnlyState state;
 	
 	@Inject 
 	private ReadOnlyLog log;
 	
 	@InjectOnCall
 	public DefaultReport() {}
 	public DefaultReport(ReadOnlyState state, ReadOnlyLog log) {
 		this.state = state;
 		this.log = log;
 	}
 
 	@Override
 	public Collection<AmountOfMail> getAmountsOfMailForAllRoutes() {
 		Collection<Location> startPoints = this.state.getAllLocations();
 		Collection<Location> endPoints = this.state.getAllLocations();
 		Collection<MailDelivery> mailDeliveries = this.state.getAllMailDeliveries();
 		Collection<AmountOfMail> result = new HashSet<AmountOfMail>();
 		
 		for (Location start : startPoints){
 			for (Location end : endPoints) {
 				if (start.equals(end))
 					continue;
 				
 				int count = 0;
 				double weight = 0;
 				double volume = 0;
 				
 				for (MailDelivery m : mailDeliveries) {
 					if (
 							!m.getRoute().get(0).getStartPoint().equals(start) || 
 							!m.getRoute().get(m.getRoute().size()-1).getEndPoint().equals(end))
 						continue;
 					
 					count++;
 					weight += m.getWeight();
 					volume += m.getVolume();
 				}
 				
 				result.add(new AmountOfMail(start, end, count, weight, volume));
 			}
 		}
 		
 		return result;
 	}
 
 	@Override
 	public Collection<DeliveryRevenueExpediture> getAllRevenueExpenditure() {
 		Collection<Location> allLocations = this.state.getAllLocations();
 		Collection<MailDelivery> mailDeliveries = this.state.getAllMailDeliveries();
 		Collection<DeliveryRevenueExpediture> result = new HashSet<DeliveryRevenueExpediture>();
 		
 		for (Location start : allLocations) {
 			for (Location end : allLocations) {
 				if ((start.isInternational() && end.isInternational()) || start.equals(end))
 						continue;
 				
 				for (Priority p : Priority.values()) {
 					double revinue = 0;
 					double expenditure = 0;
 					double totalDeliveryTime = 0;
 					long mailCount = 0;
 					
 					for (MailDelivery m : mailDeliveries) {
 						if (
 								!m.getPriority().equals(p) ||
 								!m.getRoute().get(0).getStartPoint().equals(start) || 
 								!m.getRoute().get(m.getRoute().size()-1).getEndPoint().equals(end)
 						)
 							continue;
 						
 						revinue += m.getPrice();
 						expenditure += m.getCost();
 						totalDeliveryTime += m.getShippingDuration()/(double)msinhour;
 						mailCount++;
 					}
 					
 					result.add(new DeliveryRevenueExpediture(start, end, p, revinue, expenditure, totalDeliveryTime/(double)mailCount));
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	@Override
 	public int getNumberOfEvents() {
 		return this.log.getNumberOfEvents();
 	}
 	
 	@Override
 	public double getTotalExpenditure() {
 		double sum = 0;
 		for (MailDelivery m : this.state.getAllMailDeliveries())
 			sum+= m.getCost();
 		
 		return sum;
 	}
 
 	@Override
 	public double getTotalRevenue() {
 		double sum = 0;
 		for (MailDelivery m : this.state.getAllMailDeliveries())
 			sum += m.getPrice();
 		
 		return sum;
 	}
 	
 	@Override
 	public List<RevenueExpenditure> getRevenueExpenditureOverTime() {
 		ArrayList<RevenueExpenditure> list = new ArrayList<RevenueExpenditure>();
 		
 		Collection<MailDelivery> mailDeliveries = this.state.getAllMailDeliveries();
 		
 		double revenue = 0;
 		double expenditure = 0;
 		
 		for(MailDelivery m : mailDeliveries) {
 			revenue += m.getPrice();
 			expenditure += m.getCost();
 			list.add(new RevenueExpenditure(revenue, expenditure, m.getSubmissionDate(), this.log.getEvent(m.getRelateEventID().getId()).getUid().getId()));
 		}
 		
 		return list;
 	}
 	
 	private static final long msinhour = 60*60*1000;
 	
 	public static final class Module extends AbstractModule {
 		@Override
 		protected void configure() {
 			bind(Report.class).to(DefaultReport.class);
 		}
 		
 	}
 
 	@Override
 	public Report getAtEventID(long eventID) {
 		return new DefaultReport(state.getAtEventID(eventID), log.getAtEventID(eventID));
 	}
 	@Override
 	public List<GraphSummary> getRevenueByDomesticInternational() {
 		List<GraphSummary> result = new ArrayList<GraphSummary>();
 		
 		Collection<MailDelivery> deliveries = this.state.getAllMailDeliveries();
 		
 		double totalRevenue = 0;
 		double totalRevenueInternational = 0;
 		double totalRevenueDomestic = 0;
 		
 		for(MailDelivery m : deliveries) {
 			double price = m.getPrice();
 			totalRevenue += price;
 			if(m.isInternational()) {
 				totalRevenueInternational += price;
 			}
 			else {
 				totalRevenueDomestic += price;
 			}
 		}
 		
 		result.add(new GraphSummary("International", totalRevenueInternational, totalRevenueInternational/totalRevenue));
 		result.add(new GraphSummary("Domestic", totalRevenueDomestic, totalRevenueDomestic/totalRevenue));
 
 		return result;
 	}
 	@Override
 	public List<GraphSummary> getExpenditureByDomesticInternational() {
 		List<GraphSummary> result = new ArrayList<GraphSummary>();
 		
 		Collection<MailDelivery> deliveries = this.state.getAllMailDeliveries();
 		
 		double totalExpenditure = 0;
 		double totalExpenditureInternational = 0;
 		double totalExpenditureDomestic = 0;
 		
 		for(MailDelivery m : deliveries) {
 			double cost = m.getCost();
 			totalExpenditure += cost;
 			if(m.isInternational()) {
 				totalExpenditureInternational += cost;
 			}
 			else {
 				totalExpenditureDomestic += cost;
 			}
 		}
 		
 		result.add(new GraphSummary("International", totalExpenditureInternational, totalExpenditureInternational/totalExpenditure));
 		result.add(new GraphSummary("Domestic", totalExpenditureDomestic, totalExpenditureDomestic/totalExpenditure));
 
 		return result;
 	}
 	@Override
 	public List<GraphSummary> getRevenueByRoute() {
 		List<GraphSummary> result = new ArrayList<GraphSummary>();
 		List<GraphSummary> domestic = new ArrayList<GraphSummary>();
 		List<GraphSummary> international = new ArrayList<GraphSummary>();
 		
 		Collection<DeliveryRevenueExpediture> revExp = getAllRevenueExpenditure();
 		
 		Collection<MailDelivery> deliveries = this.state.getAllMailDeliveries();
 		
 		double totalRevenue = 0;
 		
 		for(MailDelivery m : deliveries) {
 			double price = m.getPrice();
 			totalRevenue += price;
 		}
 		
 		for(DeliveryRevenueExpediture d : revExp) {
 			GraphSummary slice = new GraphSummary(d.getStartPoint().getName()+
 													" -> "+
 													d.getEndPoint().getName()+
													" ("+d.getPriority().getFormattedName()+")"
 												  , d.getRevenue(), d.getRevenue()/totalRevenue);
 			if(d.getEndPoint().isInternational()) {
 				international.add(slice);
 			}
 			else {
 				domestic.add(slice);
 			}
 		}
 		
 		result.addAll(international);
 		result.addAll(domestic);
 		
 		return result;
 	}
 	@Override
 	public List<GraphSummary> getExpenditureByRoute() {
 		List<GraphSummary> result = new ArrayList<GraphSummary>();
 		List<GraphSummary> domestic = new ArrayList<GraphSummary>();
 		List<GraphSummary> international = new ArrayList<GraphSummary>();
 		
 		Collection<DeliveryRevenueExpediture> revExp = getAllRevenueExpenditure();
 		
 		Collection<MailDelivery> deliveries = this.state.getAllMailDeliveries();
 		
 		double totalExpenditure = 0;
 		
 		for(MailDelivery m : deliveries) {
 			double cost = m.getCost();
 			totalExpenditure += cost;
 		}
 		
 		for(DeliveryRevenueExpediture d : revExp) {
 			GraphSummary slice = new GraphSummary(d.getStartPoint().getName()+
 													" -> "+
 													d.getEndPoint().getName()+
 													" ("+d.getPriority().getFormattedName()+")"
 												  , d.getExpenditure(), d.getExpenditure()/totalExpenditure);
 			if(d.getEndPoint().isInternational()) {
 				international.add(slice);
 			}
 			else {
 				domestic.add(slice);
 			}
 		}
 		
 		result.addAll(international);
 		result.addAll(domestic);
 		
 		return result;
 	}
 	@Override
 	public List<RevenueExpenditure> getLastRevenueExpenditureOverTime(int lastN) {
 		List<RevenueExpenditure> revexp = getRevenueExpenditureOverTime();
 		
 		
 		
 		return revexp.subList(Math.max(revexp.size()-lastN, 0), revexp.size());
 	}
 }
