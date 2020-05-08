 package com.movix.AdminScheduling.model.dao;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 import com.movix.AdminScheduling.comparators.SchedulingEntryProComparator;
 import com.movix.AdminScheduling.model.dto.EventDTO;
 import com.movix.shared.Operador;
 import com.movixla.service.scheduling.client.SchedulingClient;
 import com.movixla.service.scheduling.common.SchedulingEntry;
 import com.movixla.service.scheduling.common.SchedulingEntryPro;
 import com.movixla.service.scheduling.common.SchedulingEntryPro.Type;
 
 public class EventDAOImpl implements EventDAO {
 	private Integer eventId = 1;
 	private String[] daysPatterns = {"LU", "MA", "MI", "JU", "VI", "SA", "DO"};
 	private static final Logger logger = LoggerFactory.getLogger(EventDAOImpl.class.getName());
 
 	@Override
 	public void upsert(EventDTO event) {
 		SchedulingClient schedulingClient = SchedulingClient.getInstance();
 
 		List<SchedulingEntryPro> schedulingEntryPros = event.getEntries();
 		List<SchedulingEntryPro> newSchedulingEntryPros = new ArrayList<SchedulingEntryPro>();
 
 		for(int i = 0; i < daysPatterns.length; i++){
 			for(String key : event.getDias().keySet()){
 				String[] days = key.split(",");
 				for(int j = 0; j < days.length; j++){
 					if(daysPatterns[i].equals(days[j])){
 						String schedule = event.getDias().get(key);
 						String[] daysSchedule = schedule.split("\\|");
 						int h = 0;
 						for(int k = 0; k < daysSchedule.length; k++){
 							String[] rangesByDay = daysSchedule[k].trim().split(",");
 							String lastKey = "-1";
 							for(int l = 0; l < rangesByDay.length; l++){
 								SchedulingEntryPro newEntry = new SchedulingEntryPro();
 								String dayPattern = l == 0 && k == 0 ? daysPatterns[i] : "+" + k + "d";
 								String startHour = rangesByDay[l].trim().split("-")[0]; 
 								String endHour = rangesByDay[l].trim().split("-")[1];
 								String spForKey = event.getSp() == null ? "_" : event.getSp();
 								String entryKey = event.getProducto() + ":" + event.getOperador().getIdBD() + ":" + spForKey + ":_:" + 
 													"d" + i + "-h" + h + "-" + event.getTipo();
 								newEntry.setActive(true);
 								newEntry.setDayPattern(dayPattern);
 								newEntry.setEndHour(endHour.trim());
 								newEntry.setKey(entryKey);
 								newEntry.setOperator(event.getOperador().getIdBD());
 								newEntry.setParentKey(lastKey);
 								newEntry.setProduct(event.getProducto());
 								newEntry.setServicePrice(event.getSp());
 								newEntry.setStartHour(startHour.trim());
 								newEntry.setType(Type.valueOf(event.getTipo()));
 								newEntry.setActive(event.isActive());
 								newSchedulingEntryPros.add(newEntry);
 								lastKey = entryKey;
 								h++;
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		
 		System.out.println("=====NUEVO=====");
 		for(SchedulingEntryPro newEntry : newSchedulingEntryPros){
 			System.out.println(newEntry);
 		}
 		if(schedulingEntryPros != null){
 			System.out.println("=====VIEJO=====");
 			for(SchedulingEntryPro oldEntry : schedulingEntryPros){
 				System.out.println(oldEntry);
 			}
 		}
 		
		if(schedulingEntryPros != null){
			for(int i = 0; i < Math.max(newSchedulingEntryPros.size(), schedulingEntryPros.size()); i++){
				SchedulingEntryPro toSave = new SchedulingEntryPro();
				if(i < newSchedulingEntryPros.size()){
					toSave = newSchedulingEntryPros.get(i);
					if(i < schedulingEntryPros.size()){
						toSave.setId(schedulingEntryPros.get(i).getId());					
					}
				} else {
					toSave = schedulingEntryPros.get(i); 
					toSave.setActive(false);
 				}
				schedulingClient.upsert(toSave);
			}
		} else {
			for(int i = 0; i < newSchedulingEntryPros.size(); i++){
				schedulingClient.upsert(newSchedulingEntryPros.get(i));
 			}
 		}
 //		for(SchedulingEntryPro newEntry : newSchedulingEntryPros){
 //			if(schedulingEntryPros != null){		
 //				for(SchedulingEntryPro oldEntry : schedulingEntryPros){
 //					if(newEntry.getKey().equals(oldEntry.getKey())){
 //						newEntry.setId(oldEntry.getId());
 //						break;
 //					}
 //				}
 //			}
 //			schedulingClient.upsert(newEntry);
 //		}
 //		if(schedulingEntryPros != null){
 //			for(SchedulingEntryPro oldEntry : schedulingEntryPros){
 //				boolean exists = false;
 //				for(SchedulingEntryPro newEntry : newSchedulingEntryPros){
 //					if(newEntry.getKey().equals(oldEntry.getKey())){
 //						exists = true;
 //						break;
 //					}
 //				}
 //				if(!exists){
 //					oldEntry.setActive(false);
 //					schedulingClient.upsert(oldEntry);				
 //				}
 //			}
 //		}
 	}
 	
 	@Override
 	public List<EventDTO> findAll(){
 		List<EventDTO> events = new ArrayList<EventDTO>();
 		SchedulingClient schedulingClient = SchedulingClient.getInstance();
 		List<SchedulingEntryPro> schedulingEntries = schedulingClient.getEntries();
 //		boolean found = false;
 //		for(SchedulingEntryPro entry : schedulingEntries){
 //			String sp = entry.getServicePrice() == null ? "" : entry.getServicePrice();
 //			if(sp.equals("nuevo")){
 //				found = true;
 //				break;
 //			}
 //		}
 //		if(found) System.out.println("Encontrado nuevo");			
 		Collections.sort(schedulingEntries, new SchedulingEntryProComparator());
 		SchedulingEntryPro last = new SchedulingEntryPro();
 		String hourSchedule = "", lastDay = "", lastServicePrice = "";
 		BiMap<String, String> daysMap = HashBiMap.create(7);
 		List<SchedulingEntryPro> eventEntries = new ArrayList<SchedulingEntryPro>();
 		boolean first = true;
 		for(SchedulingEntryPro entry : schedulingEntries){
 			lastServicePrice = last.getServicePrice() == null ? "" : last.getServicePrice();
 			if(first){
 				last = entry;
 				lastDay = entry.getDayPattern();
 				first = false;
 			}
 			eventEntries.add(last);
 			if(mustCreateEvent(last.getKey(), entry.getKey())){
 				putScheduleInMap(hourSchedule, lastDay, daysMap);
 				addNewEvent(events, last, lastServicePrice, daysMap, eventEntries);
 				eventEntries = new ArrayList<SchedulingEntryPro>();
 				lastDay = entry.getDayPattern();
 				hourSchedule = getSchedule(entry);
 				daysMap = HashBiMap.create(7);
 			} else {
 				String dayPattern = entry.getDayPattern();
 				if(Arrays.asList(daysPatterns).contains(dayPattern)){
 					if(dayPattern.equals(lastDay)){
 						hourSchedule += getSchedule(entry);
 					} else {
 						putScheduleInMap(hourSchedule, lastDay, daysMap);
 						hourSchedule = getSchedule(entry);
 					}
 					lastDay = dayPattern;
 				} else {
 					int daysOff = Integer.parseInt(dayPattern.substring(1,2));
 					int daysPassed = hourSchedule.length() - hourSchedule.replace("|", "").length();
 					String schedule = getSchedule(entry);
 					boolean newDay = false;
 					if(daysOff > daysPassed){
 						int daysDiff = daysOff - daysPassed;
 						String separator = " ";
 						for(int i = 0; i < daysDiff; i++){
 							separator += "| ";
 						}
 						schedule = separator + schedule;
 						newDay = true;
 					}
 					hourSchedule += ( newDay ? "" : ", " ) + schedule;
 				}
 			}
 			last = entry;
 		}
 		lastDay = last.getDayPattern();
 		putScheduleInMap(hourSchedule, lastDay, daysMap);
 		addNewEvent(events, last, lastServicePrice, daysMap, eventEntries);
 		return events;
 	}
 	
 //	private List<SchedulingEntryPro> eventToSchedulingEntryPros(EventDTO event){
 //		return null;
 //	}
 	
 //	@Override
 //	public List<EventDTO> findAll(){
 //		List<EventDTO> events = new ArrayList<EventDTO>();
 //		SchedulingClient schedulingClient = SchedulingClient.getInstance();
 //		List<SchedulingEntryPro> schedulingEntries = schedulingClient.getEntries();
 //		Map<String, List<SchedulingEntryPro>> groupedEntries = new HashMap<String, List<SchedulingEntryPro>>();
 //
 //		for(SchedulingEntryPro entry : schedulingEntries){
 //			String eKey = entry.getServicePrice() + "|" + entry.getType();
 //			groupedEntries.get(eKey);
 //			if(groupedEntries.get(eKey) == null){
 //				List<SchedulingEntryPro> newList = new ArrayList<SchedulingEntryPro>();
 //				newList.add(entry);
 //				groupedEntries.put(eKey, newList);
 //			} else {
 //				groupedEntries.get(eKey).add(entry);
 //			}
 //		}
 //		
 //		for(String key : groupedEntries.keySet()){
 //			System.out.println("=====" + key + "=====");
 //			for(SchedulingEntryPro ent : groupedEntries.get(key)){
 //				System.out.println("\t" + ent.getKey() + "=>" + ent.getStart() + "-" + ent.getEnd());	
 //			}
 //		}
 //
 //		for(List<SchedulingEntryPro> entries : groupedEntries.values()){
 //			EventDTO newEvent = new EventDTO();
 //			BiMap<String, String> daysMap = HashBiMap.create(7);
 //			Operador operator = Operador.getOperadorPorIdBD(entries.get(0).getOperator());;
 //			String product = entries.get(0).getProduct();;
 //			String sp = entries.get(0).getServicePrice();
 //			Type type = entries.get(0).getType();
 //			String lastPattern = "";
 //			for(int i = 0; i < entries.size(); i++){
 //				int d = 0, h = 0;
 //				SchedulingEntryPro nextEntry = new SchedulingEntryPro();
 //				boolean found = false;
 //				for(SchedulingEntryPro entry : entries){
 //					String key = product + ":"+ operator.getIdBD() + ":" + sp + ":_:d" + d + "-h" + h + "-" + type;
 //					String nextDayKey = product + ":"+ operator.getIdBD() + ":" + sp + ":_:d" + d + "-h0" + "-" + type;
 //					String nextHourKey = product + ":"+ operator.getIdBD() + ":" + sp + ":_:d" + d + "-h" + (h + 1) + "-" + type;
 //					if(entry.getKey().equals(key) && !found){
 //						nextEntry = entry;
 //						found = true;
 //					}
 //					if(entry.getKey().equals(nextHourKey)){
 //						h++;
 //					} else if(entry.getKey().equals(nextDayKey)){
 //						d++;
 //					}
 //				}
 //				String range = nextEntry.getStart() + " - " + nextEntry.getEnd();
 //				String dayPattern = nextEntry.getDayPattern();
 //				System.out.println(dayPattern + " || " + (product + ":"+ operator.getIdBD() + ":" + sp + ":_:d" + d + "-h" + h + "-" + type));
 //				
 //				if(Arrays.asList(daysPatterns).contains(dayPattern)){
 //					daysMap.put(dayPattern, range);
 //					lastPattern = dayPattern;
 //				} else {
 //					String schedule = daysMap.get(lastPattern);
 //					int daysOff = Integer.parseInt(dayPattern.substring(1,2));
 //					int daysPassed = schedule.length() - schedule.replace("|", "").length();
 //					String separator = ", ";
 //					if(daysOff > daysPassed){
 //						int daysDiff = daysOff - daysPassed;
 //						separator = " ";
 //						for(int k = 0; k < daysDiff; k++){
 //							separator += "| ";
 //						}
 //						schedule = separator + range;
 //					}
 //					daysMap.put(lastPattern, separator + range);
 //				}
 //			}
 //			newEvent.setDias(dayMerge(daysMap));
 //			newEvent.setEntries(entries);
 //			newEvent.setId(eventId);
 //			newEvent.setOperador(operator);
 //			newEvent.setProducto(product);
 //			newEvent.setSp(sp);
 //			newEvent.setTipo(type.toString());
 //		}
 //
 //		return events;
 //	}
 	
 	private BiMap<String, String> dayMerge(BiMap<String, String> daysMap){
 		BiMap<String, String> ret = HashBiMap.create(7);
 		for(String key : daysMap.keySet()){
 			for(String secondKey : daysMap.keySet()){
 				if(daysMap.get(key).equals(daysMap.get(secondKey))){
 					ret.put(key + "," + secondKey, daysMap.get(key));
 				}
 			}
 		}
 		return ret;
 	}
 	
 	private String getSchedule(SchedulingEntryPro entry) {
 		if(entry.getStart().equals("*") || entry.getEnd().equals("*")){
 			return "* - *";
 		}
 //		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
 //		Date startDate = new Date();
 //		Date endDate = new Date();
 //		Operador operador = Operador.getOperadorPorIdBD(entry.getOperator());
 //		String start = entry.getStart(), end = entry.getEnd();
 //		try {
 //			startDate = formatter.parse(entry.getStart());
 //			endDate = formatter.parse(entry.getEnd());
 //		} catch (ParseException e) {
 //			e.printStackTrace();
 //		}
 //		if(operador != null){
 //			start = DateUtil.formatearHora(startDate, operador.getPais());
 //			end = DateUtil.formatearHora(endDate, operador.getPais());			
 //		}
 		return  entry.getStart() + " - " + entry.getEnd();
 	}
 
 	private void putScheduleInMap(String hourSchedule, String lastDay,
 			BiMap<String, String> daysMap) {
 		if(!daysMap.containsValue(hourSchedule)){
 			daysMap.put(lastDay, hourSchedule);
 		} else {
 			updateKeyAndPut(hourSchedule, lastDay, daysMap);
 		}
 	}
 
 	private void addNewEvent(List<EventDTO> events, SchedulingEntryPro last,
 			String lastServicePrice, BiMap<String, String> daysMap,
 			List<SchedulingEntryPro> eventEntries) {
 		EventDTO newEvent = new EventDTO();
 		String key = last.getProduct() + ":" + last.getOperator() + ":" + lastServicePrice + ":" + last.getKey().substring(last.getKey().lastIndexOf("-") + 1);
 		newEvent.setId(eventId);
 		newEvent.setDias(daysMap);
 		newEvent.setKey(key);
 		newEvent.setOperador(Operador.getOperadorPorIdBD(last.getOperator()));
 		newEvent.setProducto(last.getProduct());
 		newEvent.setSp(lastServicePrice);
 		newEvent.setTipo(last.getKey().substring(last.getKey().lastIndexOf("-") + 1));
 		newEvent.setEntries(eventEntries);
 		events.add(newEvent);
 		eventId++;
 	}
 
 	private void updateKeyAndPut(String hourSchedule, String lastDay,
 			BiMap<String, String> daysMap) {
 		String oldKey = daysMap.inverse().get(hourSchedule);
 		String newKey = oldKey + "," + lastDay;
 		daysMap.remove(oldKey);
 		daysMap.put(newKey, hourSchedule);
 	}
 
 	private BiMap<String, String> getEventShortDays(BiMap<String, String> daysMap){
 		BiMap<String, String> auxMap = HashBiMap.create(7);;
 		for(String key : daysMap.keySet()){
 			String shortKey = getShortDays(key);
 			auxMap.put(shortKey, daysMap.get(key));
 		}
 		return auxMap;
 	}
 	
 	private String getShortDays(String days){
 		if(days.length() == 0){
 			return "";
 		}
 		String shortDays = "";
 		int i = 0, initial = -1;
 		String[] daysTokens = days.split(",");
 		for(i = 0; i < daysPatterns.length; i++){
 			boolean exists = false;
 			for(int j = 0; j < daysTokens.length; j++){
 				if(daysTokens[j].equals(daysPatterns[i])){
 					exists = true;
 					break;
 				}
 			}
 			if(exists && initial == -1){
 				initial = i;
 			}
 			if(initial >= 0 && !exists){
 				shortDays = concatShortDays(shortDays, i, initial);
 				initial = -1;
 			}
 		}
 		if(initial >= 0){
 			shortDays = concatShortDays(shortDays, i, initial);
 		}
 		return shortDays.substring(0, shortDays.length() - 2);
 	}
 
 	private String concatShortDays(String shortDays, int i, int initial) {
 		if(i - initial == 1){
 			shortDays += daysPatterns[i - 1];
 		} else if(i - initial == 2){
 			shortDays += daysPatterns[i - 2] + "," + daysPatterns[i - 1]; 
 		} else {
 			shortDays += daysPatterns[initial] + " a " + daysPatterns[i - 1];
 		}
 		shortDays += ", ";
 		return shortDays;
 	}
 	
 	private boolean mustCreateEvent(String lastKey, String entryKey){
 		boolean different = false;
 		String[] lastKeyTokens = lastKey.split(":");
 		String[] entryKeyTokens = entryKey.split(":");
 		for(int i = 0; i < lastKeyTokens.length; i++){
 			if(i == lastKeyTokens.length - 1){
 				lastKeyTokens[i] = lastKeyTokens[i].replaceAll("[0-9]","");
 				entryKeyTokens[i] = entryKeyTokens[i].replaceAll("[0-9]","");
 			}
 			if(!lastKeyTokens[i].equals(entryKeyTokens[i])){
 				different = true;
 			}
 		}
 		return different;
 	}
 	
 	public enum Product {
 		MP, SUS
 	}
 	
 	public void test(){
 		SchedulingClient schedulingClient = SchedulingClient.getInstance();
 		List<SchedulingEntryPro> schedulingEntries = schedulingClient.getEntries();
 		for(SchedulingEntryPro entry : schedulingEntries){
 			entry.setActive(true);
 			schedulingClient.upsert(entry);
 		}
 	}
 }
