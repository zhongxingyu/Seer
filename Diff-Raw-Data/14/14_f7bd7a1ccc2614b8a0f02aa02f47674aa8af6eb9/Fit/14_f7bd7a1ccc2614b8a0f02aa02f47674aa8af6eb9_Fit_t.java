 /*
  * Copyright: Copyright 2010 Topic Maps Lab, University of Leipzig. http://www.topicmapslab.de/
  * License:   Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0.html
  */
 
 package de.topicmapslab.schemafit;
 
 import java.math.BigInteger;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.tmapi.core.Topic;
 import org.tmapi.core.TopicMap;
 
 import de.topicmapslab.majortom.model.namespace.Namespaces;
 import de.topicmapslab.tmql4j.components.processor.results.model.IResult;
 
 /**
  * @author <a href="mailto:arnim.bleier+fit@gmail.com">Arnim Bleier</a>
  */
 
 @SuppressWarnings({ "rawtypes", "unchecked" })
 public class Fit {
 
 	private ITMQLRunner tmql;
 
 	public Fit(ITMQLRunner tmql) {
 		this.tmql = tmql;
 
 	}
 
 	private Iterator<?> execute(TopicMap tm, String q) {
 		return tmql.run(tm, q).iterator();
 	}
 
 	/**
 	 * Infer all the constraints as described in
 	 * http://www.isotopicmaps.org/tmcl/tmcl.html 6.2 - 6.7 & 7.2 - 7.14 & 7.16
 	 * 
 	 * @param tmData
 	 *            The topic map with the instance data
 	 * @param tmSchema
 	 *            The evt. empty map in which the constraints are to be written
 	 */
 	public void run(TopicMap tmData, TopicMap tmSchema) {
 		doTopicType(tmData, tmSchema);
 		doNameType(tmData, tmSchema);
 		doOccurrenceType(tmData, tmSchema);
 		doAssociationType(tmData, tmSchema);
 		doRoleType(tmData, tmSchema);
 		doOverlapTypes(tmData, tmSchema);
 
 		doIndicatorConstraint(tmData, tmSchema,
 				AS_SUBJECTIDENTIFEIER_CONSTRAINT);
 		doIndicatorConstraint(tmData, tmSchema, AS_SUBJECTLOCATOR_CONSTRAINT);
 		doIndicatorConstraint(tmData, tmSchema, AS_ITEMIDENTIFEIER_CONSTRAINT);
 		doTopicCharacteristicsConstraint(tmData, tmSchema,
 				Namespaces.TMCL.TOPIC_NAME_CONSTRAINT);
 		doTopicCharacteristicsConstraint(tmData, tmSchema,
 				Namespaces.TMCL.TOPIC_OCCURRENCE_CONSTRAINT);
 		doTopicVariantNameConstraint(tmData, tmSchema);
 		doTopicRoleConstraint(tmData, tmSchema);
 //		doScopeConstraint(tmData, tmSchema);
 		doReifierConstraint(tmData, tmSchema);
 //		doAssociationRoleConstraintt(tmData, tmSchema);
 		doOccurrenceDataTypeConstraint(tmData, tmSchema);
 
 	}
 
 	/**
 	 * 6.2 Topic Type
 	 * 
 	 * @param tmData
 	 * @param tmSchema
 	 */
 	void doTopicType(TopicMap tmData, TopicMap tmSchema) {
 		addTypes(execute(tmData, QueryString.GETTOPICSTYPES),
 				tmSchema.createTopicBySubjectIdentifier(tmSchema
 						.createLocator(Namespaces.TMCL.TOPIC_TYPE)), tmSchema);
 
 	}
 
 	/**
 	 * 6.3 Name Type
 	 * 
 	 * @param tmData
 	 * @param tmSchema
 	 */
 	void doNameType(TopicMap tmData, TopicMap tmSchema) {
 		addTypes(execute(tmData, QueryString.GETNAMETYPES),
 				tmSchema.createTopicBySubjectIdentifier(tmSchema
 						.createLocator(Namespaces.TMCL.NAME_TYPE)), tmSchema);
 	}
 
 	/**
 	 * 6.4 Occurrence Type
 	 * 
 	 * @param tmData
 	 * @param tmSchema
 	 */
 	void doOccurrenceType(TopicMap tmData, TopicMap tmSchema) {
 		addTypes(execute(tmData, QueryString.GETOCCURENCETYPES),
 				tmSchema.createTopicBySubjectIdentifier(tmSchema
 						.createLocator(Namespaces.TMCL.OCCURRENCE_TYPE)),
 				tmSchema);
 	}
 
 	/**
 	 * 6.5 Association Type
 	 * 
 	 * @param tmData
 	 * @param tmSchema
 	 */
 	void doAssociationType(TopicMap tmData, TopicMap tmSchema) {
 		addTypes(execute(tmData, QueryString.GETASSOCIATIONTYPES),
 				tmSchema.createTopicBySubjectIdentifier(tmSchema
 						.createLocator(Namespaces.TMCL.ASSOCIATION_TYPE)),
 				tmSchema);
 	}
 
 	/**
 	 * 6.6 Association Type
 	 * 
 	 * @param tmData
 	 * @param tmSchema
 	 */
 	void doRoleType(TopicMap tmData, TopicMap tmSchema) {
 		addTypes(execute(tmData, QueryString.GETROLETYPES),
 				tmSchema.createTopicBySubjectIdentifier(tmSchema
 						.createLocator(Namespaces.TMCL.ROLE_TYPE)), tmSchema);
 	}
 
 	/**
 	 * 6.7 Overlap Declaration
 	 * 
 	 * @param tmData
 	 * @param tmSchema
 	 * 
 	 * TODO there seems to be a case where the second element of of a IResult is NULL
 	 * currently not reproducible
 	 * 
 	 */
 	void doOverlapTypes(TopicMap tmData, TopicMap tmSchema) {
 		Iterator<?> r = execute(tmData, QueryString.OVERLAPTYPES);
 		while (r.hasNext()) {
 			IResult n = (IResult) r.next();		
 //			System.out.println(n);
 			Set<String> all = new HashSet<String>();
 			Iterator<Object> resultIter = null;
 			try {
 				resultIter = ((List) ((IResult) n)
 						.get(1)).iterator();
 			} catch (Exception e) {
 				// TODO: In which cases is the second element of a IResult null?
 			} 
 			if (resultIter != null) {
 			while (resultIter.hasNext()) {
 				String s = (String) resultIter.next();
 				if (!all.contains(s)) {
 					Iterator<String> ii = all.iterator();
 					while (ii.hasNext()) {
 						String string = (String) ii.next();
						String call = " overlaps( <" + string + ">, <" + s
 								+ ">)";
 						 insert(tmSchema, TEMPLATE.OVERLAPTYPES + call);
 					}
 				}
 				all.add(s);	
 			}
 			}
 		}
 	}
 
 	public final static String AS_SUBJECTIDENTIFEIER_CONSTRAINT = "has-subject-identifier";
 	public final static String AS_SUBJECTLOCATOR_CONSTRAINT = "has-subject-locator";
 	public final static String AS_ITEMIDENTIFEIER_CONSTRAINT = "has-item-identifier";
 
 	/**
 	 * 7.3 Subject Identifier Constraint & 7.4 Subject Locator Constraint & 7.5
 	 * Item Identifier Constraint
 	 * 
 	 * (NO regexp, a regular expression the subject identifier shall match)
 	 * 
 	 * @param tmData
 	 * @param tmSchema
 	 */
 	void doIndicatorConstraint(TopicMap tmData, TopicMap tmSchema, String as) {
 		Iterator<?> r;
 		String template;
 		if (as == AS_SUBJECTIDENTIFEIER_CONSTRAINT) {
 			r = execute(tmData, QueryString.SUBJECTIDENTIFIER);
 			template = TEMPLATE.SUBJECTIDENTIFIERCONSTRAINT;
 		} else if (as == AS_SUBJECTLOCATOR_CONSTRAINT) {
 			r = execute(tmData, QueryString.SUBJECTLOCATOR);
 			template = TEMPLATE.SUBJECTLOCATORCONSTRAINT;
 		} else if (as == AS_ITEMIDENTIFEIER_CONSTRAINT) {
 			r = execute(tmData, QueryString.ITEMIDENTIFIER);
 			template = TEMPLATE.ITEMIDENTIFIERCONSTRAINT;
 		} else {
 			throw new RuntimeException("No propper Selection String (si|sl|ii)");
 		}
 		HashMap<String, List<Integer>> map = new HashMap<String, List<Integer>>();
 		String key;
 		while (r.hasNext()) {
 			IResult n = (IResult) r.next();
 			int times = ((BigInteger) ((List) n.get(2)).get(0)).intValue();
 			Iterator keyList = ((List) n.get(1)).iterator();
 			while (keyList.hasNext()) {
 				key = (String) keyList.next();
 				if (map.containsKey(key)) {
 					map.get(key).add(times);
 				} else {
 					LinkedList<Integer> timesList = new LinkedList<Integer>();
 					timesList.add(times);
 					map.put(key, timesList);
 				}
 			}
 		}
 		while (!map.isEmpty()) {
 			key = map.keySet().iterator().next();
 			int max = Collections.max(map.get(key));
 			String call;
 			if (max < 1) {
				call = " " + as + "( <" + key + "> , 0, 0, \".*\")";
 			} else if (max == 1) {
				call = " " + as + "( <" + key + "> , 0, 1, \".*\")";
 			} else {
				call = " " + as + "( <" + key + "> , 0, *, \".*\")";
 			}
 			insert(tmSchema, template + call);
 			map.remove(key);
 		}
 	}
 	
 	
 	
 	
 
 	/**
 	 * 7.6 Topic Name Constraint
 	 * 
 	 * @param tmData
 	 * @param tmSchema
 	 * @param constraindtType
 	 */
 	void doTopicCharacteristicsConstraint(TopicMap tmData, TopicMap tmSchema,
 			String constraintType) {
 		Iterator<?> resultIterator;
 		String template, qs;
 		if (constraintType == Namespaces.TMCL.TOPIC_NAME_CONSTRAINT) {
 			constraintType = "has-name";
 			qs = QueryString.TOPICNAME;
 			template = TEMPLATE.TOPICNAMECONSTRAINT;
 		} else if (constraintType == Namespaces.TMCL.TOPIC_OCCURRENCE_CONSTRAINT) {
 			constraintType = "has-occurrence";
 			qs = QueryString.TOPICOCCURRENCE;
 			template = TEMPLATE.TOPICOCCURENCECONSTRAINT;
 		} else
 			throw new RuntimeException(
 					"No propper Selection String (TOPIC_NAME_CONSTRAINT || TOPIC_NAME_CONSTRAINT)");
 		resultIterator = execute(tmData, "FOR $t IN tm:subject >> instances RETURN  {FOR $tt in $t >> types RETURN fn:best-identifier($tt , false), fn:best-identifier($t , false) }");
 		Map<String, Map<String, List<Integer>>> map = new HashMap<String, Map<String, List<Integer>>>();
 		Map<String, Set<String>> typesInstances = new HashMap<String, Set<String>>();
 		Map<String, List<String>> instanceNameTypes = new HashMap<String, List<String>>();
 		while (resultIterator.hasNext()) {
 			IResult sr = (IResult) resultIterator.next();
 			if (!typesInstances.containsKey(sr.get(0)))
 				typesInstances.put((String) sr.get(0), new HashSet<String>());
 			typesInstances.get(sr.get(0)).add((String) sr.get(1));
 		}
 		Iterator<String> ii = typesInstances.keySet().iterator();
 		HashSet<String> instanceSet = new HashSet<String>();
 		while (ii.hasNext()) 
 			instanceSet.addAll(typesInstances.get(ii.next()));
 		ii = instanceSet.iterator();
 		while (ii.hasNext()) {
 			String inst = ii.next();
			resultIterator = execute(tmData, "FOR $n IN <" + inst + "> >> characteristics " + qs +" RETURN fn:best-identifier( $n >> types, false)");
 			if (!instanceNameTypes.containsKey(inst))
 				instanceNameTypes.put(inst, new LinkedList<String>());
 			while (resultIterator.hasNext()) 
 				instanceNameTypes.get(inst).add((String) ((IResult) resultIterator.next()).first());
 		}
 		Iterator<Entry<String, Set<String>>> typeInstanceIterator = typesInstances.entrySet().iterator();
 		while (typeInstanceIterator.hasNext()) {
 			Entry<String, Set<String>> typeInstnces = typeInstanceIterator.next();
 			Iterator<String> instancesIterator = typeInstnces.getValue().iterator();
 			String topicType = typeInstnces.getKey();
 			while (instancesIterator.hasNext()) {
 				List<String> nameTypesList = instanceNameTypes.get(instancesIterator.next());
 				if (!map.containsKey(topicType))
 					map.put(topicType, new HashMap<String, List<Integer>>());
 				Map<String, List<Integer>> typeNameTypeCounts = map.get(topicType);
 				Iterator<String> nameTypesIterator = nameTypesList.iterator();
 				while (nameTypesIterator.hasNext()) {
 					String nnt = nameTypesIterator.next();
 					if (!typeNameTypeCounts.containsKey(nnt)){
 						typeNameTypeCounts.put(nnt, new LinkedList<Integer>());
 					}
 				} // ensure that each nt has an entry 
 			}
 			instancesIterator = typeInstnces.getValue().iterator();
 			while (instancesIterator.hasNext()) {
 				List<String> nameTypesList = instanceNameTypes.get(instancesIterator.next());
 				if (!map.containsKey(topicType))
 					map.put(topicType, new HashMap<String, List<Integer>>());
 				Map<String, List<Integer>> typeNameTypeCounts = map.get(topicType);
 				Iterator<String> ntmi = typeNameTypeCounts.keySet().iterator();
 				while (ntmi.hasNext()) {
 					String s =  ntmi.next();
 					typeNameTypeCounts.get(s).add(Collections.frequency(nameTypesList, s));
 				}
 			}
 			Iterator<Entry<String, List<Integer>>> nteI = map.get(topicType).entrySet().iterator();
 			while (nteI.hasNext()) {
 				Map.Entry<java.lang.String, java.util.List<java.lang.Integer>> entry = (Map.Entry<java.lang.String, java.util.List<java.lang.Integer>>) nteI
 						.next();
 				String call = constraintType + "(" + topicType + ", " + entry.getKey() +  ", " + Collections.min(entry.getValue()) +", " + Collections.max(entry.getValue()) +")";
 				insert(tmSchema, template + " " + call);
 			}
 		}
 	}
 
 	/**
 	 * 7.7 Variant Name Constraint
 	 * 
 	 * @param _tm
 	 * @param _tmSchema
 	 */
 	public void doTopicVariantNameConstraint(TopicMap tm, TopicMap tmSchema) {
 		Iterator<?> resultIterator = execute(tm, QueryString.TOPICNAMEVARIANTS);
 		Map<String, Map<String, Map<String, List<Integer>>>> map = new HashMap<String, Map<String, Map<String, List<Integer>>>>();
 		while (resultIterator.hasNext()) {
 			IResult resultTopic = ((IResult) resultIterator.next());
 			Map<String, Map<String, List<Integer>>> TTNTmap;
 			Iterator tt = ((List) resultTopic.get(1)).iterator();
 			List ntList = (List) resultTopic.get(2);
 			List timesList = (List) resultTopic.get(4);
 			List scopesList = (List) resultTopic.get(3);
 			while (tt.hasNext()) {
 				String TTKey = (String) tt.next();
 				if (!map.containsKey(TTKey))
 					TTNTmap = map.put(TTKey,
 							new HashMap<String, Map<String, List<Integer>>>());
 				TTNTmap = map.get(TTKey);
 				for (int i = 0; i < ntList.size(); i++) {
 					Map<String, List<Integer>> mapForNT;
 					String NTKey = (String) ntList.get(i);
 					if (!TTNTmap.containsKey(NTKey))
 						TTNTmap.put(NTKey, new HashMap<String, List<Integer>>());
 					mapForNT = TTNTmap.get(NTKey);
 					for (int j = 0; j < scopesList.size(); j++) {
 						String scopeKey = (String) scopesList.get(j);
 						List listForScope;
 						if (!mapForNT.containsKey(scopeKey))
 							mapForNT.put(scopeKey, new LinkedList<Integer>());
 						listForScope = mapForNT.get(scopeKey);
 						listForScope.add(((BigInteger) timesList.get(i))
 								.intValue());
 					}
 				}
 			}
 		}
 		while (!map.isEmpty()) {
 			String tt = map.keySet().iterator().next();
 			Map<String, Map<String, List<Integer>>> TTNTmap = map.get(tt);
 			while (!TTNTmap.isEmpty()) {
 				String nt = TTNTmap.keySet().iterator().next();
 				Map<String, List<Integer>> ScopeMap = TTNTmap.get(nt);
 				while (!ScopeMap.isEmpty()) {
 					String scope = ScopeMap.keySet().iterator().next();
 					int max = Collections.max(ScopeMap.get(scope));
 					String call;
 					if (max < 2)
 						call = " has-variant(" + tt + ", " + nt + " ," + scope
 								+ ", 0, 1)";
 					else
 						call = " has-variant(" + tt + ", " + nt + ", " + scope
 								+ ", 0, *)";
 					insert(tmSchema, TEMPLATE.VARIANTNAMECONSTRAINT + call);
 					ScopeMap.remove(scope);
 				}
 				TTNTmap.remove(nt);
 			}
 			map.remove(tt);
 		}
 	}
 
 	/**
 	 * 7.9 Topic Role Constraint
 	 * 
 	 * @param _tm
 	 * @param _tmSchema
 	 */
 	public void doTopicRoleConstraint(TopicMap tm, TopicMap tmSchema) {
 		Iterator<?> resultIterator = execute(tm, QueryString.TOPICROLE);
 		while (resultIterator.hasNext()) {
 			IResult r = (IResult) resultIterator.next();
 			Iterator atIterator = ((List) r.get(2)).iterator();
 			Iterator bigTimes = ((List) r.get(3)).iterator();
 			List times = new LinkedList<Integer>();
 			while (bigTimes.hasNext())
 				times.add(((BigInteger) bigTimes.next()).intValue());
 			String call;
 			while (atIterator.hasNext()) {
 				call = " plays-role(" + r.get(0) + ", " + r.get(1) + ", "
 						+ atIterator.next() + ", " + Collections.min(times)
 						+ ", " + Collections.max(times) + ")";
 				// System.out.println(call); // TODO evtl. to change to the 0.1.* schme
 				insert(tmSchema, TEMPLATE.TOPICROLECONSTRAINT + call);
 			}
 		}
 	}
 
 //	public void doScopeConstraint(TopicMap tm, TopicMap tmSchema) {
 //		Iterator<?> resultIterator = execute(tm, QueryString.SCOPE);
 //		while (resultIterator.hasNext()) {
 //			IResult r = (IResult) resultIterator.next();
 //			// System.out.println(r);
 //
 //		}
 //	}
 
 	/**
 	 * 7.12 Reifier Constraint & 7.13 Topic Reifies Constraint
 	 */
 	void doReifierConstraint(TopicMap tm, TopicMap tmSchema) {
 		Iterator<?> resultIterator = execute(tm, QueryString.REIFIER);
 		Map<String, Map<String, List<String>>> characteristicsMap = new HashMap<String, Map<String, List<String>>>();
 		Map<String, List<String>> rMap;
 		String id, ct;
 		List<String> rtList, idLIst;
 		while (resultIterator.hasNext()) {
 			IResult r = (IResult) resultIterator.next();
 			id = r.get(0);
 			ct = r.get(1);
 			rtList = (List) r.get(2);
 
 			if (!characteristicsMap.containsKey(ct))
 				characteristicsMap.put(ct, new HashMap<String, List<String>>());
 			rMap = characteristicsMap.get(ct);
 
 			if (!rMap.containsKey(id))
 				rMap.put(id, new LinkedList<String>());
 			idLIst = rMap.get(id);
 			if (rtList != null)
 				idLIst.addAll(rtList);
 			else
 				idLIst.add(null);
 		}
 		Set<String> insertSet = new HashSet<String>();
 		while (!characteristicsMap.isEmpty()) {
 			ct = characteristicsMap.keySet().iterator().next();
 			rMap = characteristicsMap.get(ct);
 			rtList = null;
 			while (!rMap.isEmpty()) {
 				id = rMap.keySet().iterator().next();
 				Set rtSet = new HashSet(rMap.get(id));
 				if (rtSet.contains(null)) {
 //					insertSet.add(" cannot-have-reifier( " + ct + " ) ");
 					// TODO what about cannot-have-reifier? Meaningfull?
 					rMap.clear();
 				} else {
 					if (rtList == null)
 						rtList = new LinkedList<String>(rtSet);
 					else
 						rtList.retainAll(rtSet);
 				}
 				rMap.remove(id);
 			}
 			if (rtList != null) {
 				Iterator<String> rtIter = rtList.iterator();
 				while (rtIter.hasNext()) {
 					insertSet.add(" may-have-reifier( " + ct + ", "
 							+ rtIter.next() + " ) ");
 				}
 			}
 			characteristicsMap.remove(ct);
 		}
 		Iterator<String> issIter = insertSet.iterator();
 		while (issIter.hasNext()) {
 			String call = issIter.next();
 //			System.err.println(call);
 			insert(tmSchema, TEMPLATE.REIFIERCONSTRAINT + call);
 		}
 
 	}
 
 	/**
 	 * 7.14 Association Role Constraint
 	 */
 	void doAssociationRoleConstraintt(TopicMap tm, TopicMap tmSchema) {
 		Iterator<?> resultIterator = execute(tm, QueryString.ASSOCIATIONROLE);
 		Map<String, Map<String, List<String>>> assoMap = new HashMap<String, Map<String, List<String>>>();
 		String assoID, assoType, roleType;
 		Map<String, List<String>> roleMap;
 		while (resultIterator.hasNext()) {
 			IResult r = (IResult) resultIterator.next();
 			assoType = (String) r.get(1);
 			roleType = (String) r.get(2);
 			assoID = (String) r.get(0);
 			if (!assoMap.containsKey(assoType))
 				assoMap.put(assoType, new HashMap<String, List<String>>());
 			roleMap = assoMap.get(assoType);
 			if (!roleMap.containsKey(roleType))
 				roleMap.put(roleType, new LinkedList<String>());
 			roleMap.get(roleType).add(assoID);
 		}
 		String call = "";
 		Iterator<String> idIter;
 		List times = new LinkedList<Integer>();
 		while (!assoMap.isEmpty()) {
 			assoType = assoMap.keySet().iterator().next();
 			roleMap = assoMap.get(assoType);
 			while (!roleMap.isEmpty()) {
 				roleType = roleMap.keySet().iterator().next();
 				idIter = new HashSet<String>(roleMap.get(roleType)).iterator();
 				while (idIter.hasNext())
 					times.add(Collections.frequency(roleMap.get(roleType),
 							idIter.next()));
 				call = " has-role(" + assoType + ", " + roleType + ", "
 						+ Collections.min(times) + ", "
 						+ Collections.max(times) + ") ";
 				roleMap.remove(roleType);
 				// System.out.println(call);
 				insert(tmSchema, TEMPLATE.ASSOCIATIONROLECONSTRAINT + call);
 			}
 			assoMap.remove(assoType);
 		}
 	}
 
 	/**
 	 * 7.16 Occurrence Data Type Constraint
 	 * 
 	 */
 	void doOccurrenceDataTypeConstraint(TopicMap tm, TopicMap tmSchema) {
 		Map<String, String> occurenceTypeMap = new HashMap<String, String>();
 		Iterator<?> resultIterator = execute(tm, QueryString.OCCURENCEDATATYPE);
 
 		while (resultIterator.hasNext()) {
 			IResult r = (IResult) resultIterator.next();
 			if (occurenceTypeMap.containsKey(r.get(0))
 					&& !occurenceTypeMap.get(r.get(0)).equals(r.get(1)))
 				occurenceTypeMap.put((String) r.get(0),
 						"http://www.w3.org/2001/XMLSchema#anyType");
 			else
 				occurenceTypeMap.put((String) r.get(0), (String) r.get(1));
 		}
 		while (!occurenceTypeMap.isEmpty()) {
 			String ot = occurenceTypeMap.keySet().iterator().next();
 			String dt = occurenceTypeMap.get(ot);
 			String call = "has-datatype(" + ot + ", " + dt + ")";
 			// System.out.println(call);
 			insert(tmSchema, TEMPLATE.OCCURENCEDATATYPECONSTRAINT + call);
 			occurenceTypeMap.remove(ot);
 		}
 	}
 
 	private void insert(TopicMap tm, String ctm) {
 		execute(tm,
 				"INSERT ''' %prefix tmcl http://psi.topicmaps.org/tmcl/  %prefix tmdm http://psi.topicmaps.org/iso13250/model "
 						+ ctm + " '''");
 	}
 
 	private void addTypes(Iterator<?> s, Topic t, TopicMap tm) {
 		// TODO using TMQL UPDATE
 		while (s.hasNext())
 			tm.createTopicBySubjectIdentifier(
 					tm.createLocator(((IResult) s.next()).first()
 							.toString())).addType(t);
 	}
 
 }
