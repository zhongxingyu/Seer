 package xdi2.pixel;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import xdi2.core.Graph;
 import xdi2.core.features.linkcontracts.LinkContract;
 import xdi2.core.features.linkcontracts.LinkContracts;
 import xdi2.core.features.linkcontracts.condition.GenericCondition;
 import xdi2.core.features.linkcontracts.condition.IsCondition;
 import xdi2.core.features.linkcontracts.operator.FalseOperator;
 import xdi2.core.features.linkcontracts.operator.GenericOperator;
 import xdi2.core.features.linkcontracts.operator.TrueOperator;
 import xdi2.core.features.linkcontracts.policy.Policy;
 import xdi2.core.impl.memory.MemoryGraphFactory;
 import xdi2.core.xri3.XDI3Segment;
 import xdi2.core.xri3.XDI3Statement;
 
 public class PixelPolicy {
 
 	private HashMap<?, ?> hashMap;
 	private LinkContract linkContract;
 
 	private PixelPolicy(HashMap<?, ?> hashMap, LinkContract linkContract) { 
 
 		this.hashMap = hashMap;
 		this.linkContract = linkContract;
 	}
 
 	static PixelPolicy fromMap(HashMap<?, ?> hashMap) throws PixelParserException {
 
 		LinkContract linkContract = translate(hashMap);
 
 		PixelPolicy pixelPolicy = new PixelPolicy(hashMap, linkContract);
 
 		return pixelPolicy;
 	}
 
 	private static LinkContract translate(HashMap<?, ?> hashMap) throws PixelParserException {
 
 		Graph graph = MemoryGraphFactory.getInstance().openGraph();
 		LinkContract linkContract = LinkContracts.getLinkContract(graph.getRootContextNode(), true);
 
 		linkContract.addPermission(XDI3Segment.create("$do$signal"), XDI3Segment.create("+channel{}+event{}"));
 
 		Policy policyRoot = linkContract.getPolicyRoot(true);
 
 		List<?> policy_stmts = (List<?>) hashMap.get("policy_stmts");
 		if (policy_stmts == null) return linkContract;
 
 		int countDeny = countDenyEffect(policy_stmts);
 		int countAllow = countAllowEffect(policy_stmts);
 
 		for (Object policy_stmt : policy_stmts) {
 
 			// translate effect of current policy statement
 
 			String effect = (String) ((Map<?, ?>) policy_stmt).get("effect");
 
 			Policy policy = policyRoot;
 
 			if (isAllow(effect)) {
 
 				if (countDeny > 0) {
 
 					policy = policy.createAndPolicy(true);
 				}
 
 				if (countAllow > 1) {
 
 					policy = policy.createOrPolicy(true);
 				}
 
 				policy = policy.createAndPolicy(countAllow == 1);
 			} else if (isDeny(effect)) {
 
 				if (countAllow > 0) {
 
 					policy = policy.createAndPolicy(true);
 				}
 
 				policy = policy.createNotPolicy(true);
 
 				if (countDeny > 1) {
 
 					policy = policy.createOrPolicy(true);
 				}
 
 				policy = policy.createAndPolicy(countDeny == 1);
 			}
 
 			// translate details of current policy statement
 
 			translatePolicyStmt((Map<?, ?>) policy_stmt, policy);
 		}
 
 		return linkContract;
 	}
 
 	private static int countDenyEffect(List<?> policy_stmts) {
 
 		int count = 0;
 
 		for (Object policy_stmt : policy_stmts) {
 
 			if (isDeny((String) ((Map<?, ?>) policy_stmt).get("effect"))) count++;
 		}
 
 		return count;
 	}
 
 	private static int countAllowEffect(List<?> policy_stmts) {
 
 		int count = 0;
 
 		for (Object policy_stmt : policy_stmts) {
 
 			if (isAllow((String) ((Map<?, ?>) policy_stmt).get("effect"))) count++;
 		}
 
 		return count;
 	}
 
 	private static boolean isDeny(String effect) {
 
 		return "deny".equals(effect) || "denies".equals(effect);
 	}
 
 	private static boolean isAllow(String effect) {
 
 		return "allow".equals(effect) || "allows".equals(effect);
 	}
 
 	private static void translatePolicyStmt(Map<?, ?> policy_stmt, Policy policyEvent) throws PixelParserException {
 
 		if (policyEvent == null) throw new NullPointerException();
 
 		// 'channel_id'
 
 		String channel_id = (String) policy_stmt.get("channel_id");
 
 		String channelXriString;
 
 		if ("any".equals(channel_id)) {
 
			channelXriString = "+channel{}";
 		} else if (channel_id != null) {
 
 			channelXriString = "+channel" + channel_id;
 		} else {
 
 			throw new PixelParserException("No 'channel_id'.");
 		}
 
 		// 'cloud_id'
 
 		String cloud_id = (String) policy_stmt.get("cloud_id");
 
 		if (cloud_id != null) {
 
 			channelXriString = "(" + cloud_id + ")" + channelXriString;
 		}
 
 		// 'event_filter', 'domain', 'type'
 
 		Map<?, ?> event_filter = (Map<?, ?>) policy_stmt.get("event_filter");
 
 		String event_filter_domain = null;
 		List<?> event_filter_types = null;
 
 		if (event_filter != null) {
 
 			event_filter_domain = (String) event_filter.get("domain");
 			event_filter_types = (List<?>) event_filter.get("types");
 		}
 
 		// 'condition'
 
 		Boolean condition_sense = null;
 		List<?> condition_type_clouds = null;
 		List<?> condition_type_relationships = null;
 
 		Map<?, ?> condition = (Map<?, ?>) policy_stmt.get("condition");
 
 		if (condition != null) {
 
 			condition_sense = (Boolean) condition.get("sense");
 			String condition_type = (String) condition.get("type");
 
 			if ("relationship_list".equals(condition_type)) {
 
 				condition_type_relationships = (List<?>) condition.get("relationship_list");
 			} else if ("relationship_single".equals(condition_type)) {
 
 				condition_type_relationships = Collections.singletonList(condition.get("relationship_id"));
 			} else if ("raised_by_list".equals(condition_type)) {
 
 				condition_type_clouds = (List<?>) condition.get("cloud_list");
 			} else if ("raised_by_single".equals(condition_type)) {
 
 				condition_type_clouds = Collections.singletonList(condition.get("cloud_id"));
 			} else {
 
 				throw new PixelParserException("Invalid 'condition' type: " + condition_type);
 			}
 		}
 
 		// construct policy
 
 		String eventXriString = channelXriString + "+event{1}";
 
 		XDI3Segment eventXri = XDI3Segment.create(eventXriString);
 		XDI3Segment operationXri = XDI3Segment.create("$do$signal");
 
 		if (event_filter_domain != null) {
 
 			if ("all".equals(event_filter_domain))
 				event_filter_domain = "{}";
 			else
 				event_filter_domain = "+" + event_filter_domain;
 
 			XDI3Statement statementXri = XDI3Statement.create("" + eventXri + "/+domain/" + event_filter_domain);
 
 			GenericOperator.createGenericOperator(policyEvent, operationXri, statementXri);
 		}
 
 		if (event_filter_types != null && event_filter_types.size() > 0) {
 
 			Policy policyEventFilterType = event_filter_types.size() > 1 ? policyEvent.createOrPolicy(false) : policyEvent;
 
 			for (Object event_filter_type : event_filter_types) {
 
 				XDI3Statement statementXri = XDI3Statement.create("" + eventXri + "/+type/" + "+" + (String) event_filter_type);
 
 				GenericOperator.createGenericOperator(policyEventFilterType, operationXri, statementXri);
 			}
 		}
 
 		if (condition_type_relationships != null) {
 
 			Policy policyConditionTypeRelationships = condition_type_relationships.size() > 1 ? policyEvent.createOrPolicy(false) : policyEvent;
 
 			for (Object condition_type_relationship : condition_type_relationships) {
 
 				XDI3Statement statementXri = XDI3Statement.create("" + channelXriString + "/" + (String) condition_type_relationship + "/" + "{$from}");
 
 				if (Boolean.TRUE.equals(condition_sense))
 					TrueOperator.createTrueOperator(policyConditionTypeRelationships, GenericCondition.fromStatement(statementXri));
 				else
 					FalseOperator.createFalseOperator(policyConditionTypeRelationships, GenericCondition.fromStatement(statementXri));
 			}
 		}
 
 		if (condition_type_clouds != null) {
 
 			Policy policyConditionTypeClouds = condition_type_clouds.size() > 1 ? policyEvent.createOrPolicy(false) : policyEvent;
 
 			for (Object condition_type_cloud : condition_type_clouds) {
 
 				XDI3Statement statementXri = XDI3Statement.create("" + (String) condition_type_cloud + "/" + "$is" + "/" + "{$from}");
 
 				if (Boolean.TRUE.equals(condition_sense))
 					TrueOperator.createTrueOperator(policyConditionTypeClouds, IsCondition.fromStatement(statementXri));
 				else
 					FalseOperator.createFalseOperator(policyConditionTypeClouds, IsCondition.fromStatement(statementXri));
 			}
 		}
 	}
 
 	public HashMap<?, ?> getHashMap() {
 
 		return this.hashMap;
 	}
 
 	public LinkContract getLinkContract() {
 
 		return this.linkContract;
 	}
 }
