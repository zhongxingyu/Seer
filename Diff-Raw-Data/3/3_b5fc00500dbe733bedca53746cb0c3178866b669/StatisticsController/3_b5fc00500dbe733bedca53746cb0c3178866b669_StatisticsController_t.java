 package com.sk.stat;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import com.google.common.collect.HashMultiset;
 import com.google.common.collect.Multiset;
 import com.sk.clean.Cleaner;
 import com.sk.util.OrderTable;
 import com.sk.util.PersonalData;
 
 public class StatisticsController {
 
 	private final Cleaner cleaner;
 
 	private AttributeStatistics generateStringStat(double coverage, String... values) {
 		Multiset<String> counts = HashMultiset.create();
 		int totalCount = values.length;
 		Collections.addAll(counts, values);
 		int max = -1, min = totalCount + 1;
 		List<String> maxCont = new ArrayList<>(), minCont = new ArrayList<>();
 		for (Multiset.Entry<String> ent : counts.entrySet()) {
 			if (ent.getCount() > max) {
				max = ent.getCount();
 				maxCont.clear();
 				maxCont.add(ent.getElement());
 			} else if (max == ent.getCount()) {
 				maxCont.add(ent.getElement());
 			}
 			if (ent.getCount() < min) {
				min = ent.getCount();
 				minCont.clear();
 				minCont.add(ent.getElement());
 			} else if (min == ent.getCount()) {
 				minCont.add(ent.getElement());
 			}
 		}
 		final double confidence = max * 1d / totalCount;
 		return new AttributeStatistics(maxCont.toArray(new String[maxCont.size()]), confidence, coverage, counts
 				.elementSet().size(), minCont.toArray(new String[minCont.size()]));
 	}
 
 	private AttributeStatistics generateAttributeStat(String attribute, PersonalData... cleaned) {
 		List<String> values = new ArrayList<>();
 		int containCount = 0;
 		for (PersonalData clean : cleaned) {
 			if (clean.containsKey(attribute)) {
 				containCount++;
 				Collections.addAll(values, clean.getAllValues(attribute));
 			}
 		}
 		return generateStringStat(containCount * 1d / cleaned.length, values.toArray(new String[values.size()]));
 	}
 
 	@SuppressWarnings("unused")
 	public PersonStatistics generateStatClean(String first, String last, PersonalData... cleaned) {
 		PersonStatistics ret = new PersonStatistics(first, last);
 		Set<String> attributeSkip = new HashSet<>();
 		cleanLocation: {
 			OrderTable locationAttributeOrder = new OrderTable();
 			List<String> locAttr = new ArrayList<>();
 			Set<String> allLocAttr = new HashSet<>();
 			for (PersonalData clean : cleaned) {
 				for (String attr : clean.keySet()) {
 					if (attr.startsWith("L"))
 						locAttr.add(attr);
 				}
 				allLocAttr.addAll(locAttr);
 				locationAttributeOrder.addInOrder(locAttr);
 				locAttr.clear();
 			}
 			Set<String> tmp = new TreeSet<>(locationAttributeOrder);
 			tmp.addAll(allLocAttr);
 			attributeSkip.addAll(allLocAttr);
 			allLocAttr = tmp;
 			for (String attribute : allLocAttr) {
 				ret.put("O" + attribute, generateAttributeStat(attribute, cleaned));
 			}
 			// TODO implement smart location cleaning
 		}
 		Set<String> attributes = new LinkedHashSet<>();
 		for (PersonalData data : cleaned) {
 			attributes.addAll(data.keySet());
 		}
 		attributes.removeAll(attributeSkip);
 		for (String attribute : attributes) {
 			ret.put(attribute, generateAttributeStat(attribute, cleaned));
 		}
 		return ret;
 	}
 
 	public PersonStatistics generateStat(String first, String last, PersonalData... dirty) {
 		PersonalData[] clean = new PersonalData[dirty.length];
 		for (int i = 0; i < dirty.length; ++i)
 			clean[i] = cleaner.clean(dirty[i]);
 		return generateStatClean(first, last, clean);
 	}
 
 	private static StatisticsController singleton;
 	private static final Object slock = new Object();
 
 	public static StatisticsController get() {
 		if (singleton == null) {
 			synchronized (slock) {
 				if (singleton == null) {
 					singleton = new StatisticsController();
 				}
 			}
 		}
 		return singleton;
 	}
 
 	private StatisticsController() {
 		cleaner = new Cleaner();
 	}
 
 }
