 package com.zerodes.exchangesync.sync;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.zerodes.exchangesync.Pair;
 import com.zerodes.exchangesync.StatisticsCollector;
 import com.zerodes.exchangesync.calendarsource.CalendarSource;
 import com.zerodes.exchangesync.dto.AppointmentDto;
 
 public class SyncCalendarsImpl {
 	
 	private CalendarSource exchangeSource;
 	private CalendarSource otherSource;
 
 	public SyncCalendarsImpl(CalendarSource exchangeSource, CalendarSource otherSource) {
 		this.exchangeSource = exchangeSource;
 		this.otherSource = otherSource;
 	}
 
 	protected Set<Pair<AppointmentDto, AppointmentDto>> generatePairs() {
 		Set<Pair<AppointmentDto, AppointmentDto>> results = new HashSet<Pair<AppointmentDto, AppointmentDto>>();
 		Collection<AppointmentDto> otherAppointments = otherSource.getAllAppointments();
 		Collection<AppointmentDto> exchangeAppointments = exchangeSource.getAllAppointments();
 		Map<String, AppointmentDto> otherAppointmentsMap = generateExchangeIdMap(otherAppointments);
 		Map<String, AppointmentDto> exchangeAppointmentsMap = generateExchangeIdMap(exchangeAppointments);
 		for (AppointmentDto exchangeAppointment : exchangeAppointments) {
 			AppointmentDto otherAppointment = otherAppointmentsMap.get(exchangeAppointment.getExchangeId());
 			results.add(new Pair<AppointmentDto, AppointmentDto>(exchangeAppointment, otherAppointment));
 		}
 		for (AppointmentDto otherAppointment : otherAppointments) {
 			AppointmentDto exchangeAppointment = exchangeAppointmentsMap.get(otherAppointment.getExchangeId());
 			results.add(new Pair<AppointmentDto, AppointmentDto>(exchangeAppointment, otherAppointment));
 		}
 		return results;
 	}
 
 	/**
 	 * Take a matching exchange CalendarEntry and other CalendarEntry and determine what needs to be done to sync them.
 	 *
 	 * @param exchangeCalendarEntry Exchange CalendarEntry (or null if no matching CalendarEntry exists)
 	 * @param otherCalendarEntry CalendarEntry from "other" data source (or null if no matching CalendarEntry exists)
 	 */
 	public void sync(final AppointmentDto exchangeCalendarEntry, final AppointmentDto otherCalendarEntry, final StatisticsCollector stats) {
 		if (exchangeCalendarEntry != null && otherCalendarEntry == null) {
 			otherSource.addAppointment(exchangeCalendarEntry);
 			stats.appointmentAdded();
 		} else if (exchangeCalendarEntry == null && otherCalendarEntry != null && otherCalendarEntry.getExchangeId() != null) {
 			otherSource.deleteAppointment(otherCalendarEntry);
 			stats.appointmentDeleted();
 		} else if (exchangeCalendarEntry != null && otherCalendarEntry != null) {
 			if (exchangeCalendarEntry.getLastModified().after(otherCalendarEntry.getLastModified())) {
 				// Exchange CalendarEntry has a more recent modified date, so modify other CalendarEntry
				exchangeCalendarEntry.copyTo(otherCalendarEntry);
				otherSource.updateAppointment(otherCalendarEntry);
 				stats.appointmentUpdated();
 			} else {
 				// Other CalendarEntry has a more recent modified date, so modify Exchange
 			}
 		}
 	}
 
 	public void syncAll(final StatisticsCollector stats) {
 		System.out.println("Synchronizing calendars...");
 
 		// Generate matching pairs of CalendarEntrys
 		Set<Pair<AppointmentDto, AppointmentDto>> pairs = generatePairs();
 
 		// Create/complete/delete as required
 		for (Pair<AppointmentDto, AppointmentDto> pair : pairs) {
 			sync(pair.getLeft(), pair.getRight(), stats);
 		}
 	}
 
 	public Map<String, AppointmentDto> generateExchangeIdMap(Collection<AppointmentDto> CalendarEntrys) {
 		Map<String, AppointmentDto> results = new HashMap<String, AppointmentDto>();
 		for (AppointmentDto CalendarEntry : CalendarEntrys) {
 			results.put(CalendarEntry.getExchangeId(), CalendarEntry);
 		}
 		return results;
 	}
 }
