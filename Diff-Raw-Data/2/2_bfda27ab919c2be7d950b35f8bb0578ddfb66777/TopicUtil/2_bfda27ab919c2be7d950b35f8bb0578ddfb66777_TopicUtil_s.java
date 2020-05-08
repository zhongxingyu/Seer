 package no.ovitas.compass2.util;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import no.ovitas.compass2.model.Relation;
 import no.ovitas.compass2.model.Topic;
 import no.ovitas.compass2.model.TopicTreeNode;
 
 public class TopicUtil {
 	private static class TopicNode{
 		Topic parent;
 		Topic topic;
 		double distance;
 		
 		public TopicNode(Topic topic, Topic parent, double distance) {
 			this.topic = topic;
 			this.parent = parent;
 			this.distance = distance;
 		}
 	}
 	
 	private static class TopicLinkNode {
 		Topic topic;
 		double cost;
 		
 		public TopicLinkNode(Topic topic, double distance) {
 			this.topic = topic;
 			this.cost  = distance;
 		}
 	}
 	
 	private static abstract class TopicExpander {
 		List<TopicLinkNode> expand(Topic topic) {
 			List<TopicLinkNode> ret = new ArrayList<TopicLinkNode>();
 			List<Relation> relations = topic.getRelations();
 			if (relations != null) {
 				for (Relation relation : relations) {
 					Topic otherTopic = relation.getSource();
 					if (otherTopic == topic) otherTopic = relation.getTarget();
 					ret.add(new TopicLinkNode(otherTopic, getDistance(relation)));
 				}
 			}
 			return ret;
 		}
 		
 		List<TopicLinkNode> contract(Topic topic) {
 			List<TopicLinkNode> ret = new ArrayList<TopicLinkNode>();
 			List<Relation> relations = topic.getRelations();
 			if (relations != null) {
 				for (Relation relation : relations) {
 					Topic otherTopic = relation.getTarget();
 					if (otherTopic == topic) otherTopic = relation.getSource();
 					ret.add(new TopicLinkNode(otherTopic, getDistance(relation)));
 				}
 			}
 			return ret;
 		}
 		
 		protected abstract double getDistance(Relation relation);
 	}
 	
 	private static class TopicExpanderForMinHopCount extends TopicExpander {
 		protected double getDistance(Relation relation) {
 			return 1;
 		}
 	}
 	
 	private static class TopicExpanderForMaxWeight extends TopicExpander {
 		protected double getDistance(Relation relation) {
 			return -Math.log(relation.getRelationType().getWeight());
 		}
 	}
 	
 	private static final Comparator<TopicNode> comparator = new Comparator<TopicNode>() {
         public int compare(TopicNode left, TopicNode right) {
         	double ret = left.distance - right.distance;
             return ret < 0 ? -1 : ret > 0 ? 1 : 0;
         }
     };
 	
 	public static TopicTreeNode dijkstra(Topic rootTopic, TopicExpander expander, double limit, int maxTopicNumberToExpand) {
 		PriorityQueue<TopicNode> queue = new PriorityQueue<TopicNode>(10, comparator);
 		HashMap<Topic, TopicNode> topicNodes = new HashMap<Topic, TopicNode>();
 		List<TopicNode> finalTopicNodes = new LinkedList<TopicNode>();
 		
 		TopicNode rootTopicNode = new TopicNode(rootTopic, null, 0);
 		topicNodes.put(rootTopicNode.topic, rootTopicNode);
 		queue.add(rootTopicNode);
 		
 		int idx = 0;
 		while (queue.size() > 0 && idx++ < maxTopicNumberToExpand) {
 			TopicNode topicNode = queue.poll();
 			
 			// Get source -> target nodes
			List<TopicLinkNode> topicLinkNodes = expander.contract(topicNode.topic);
 			
 			// Get target -> source nodes and merge into topicLinkNodes
 			List<TopicLinkNode> topicLinkNodesBack = expander.contract(topicNode.topic);
 			for(TopicLinkNode node : topicLinkNodesBack) {
 				if (!topicLinkNodes.contains(node)) {
 					topicLinkNodes.add(node);
 				}
 			}
 			
 			for (TopicLinkNode topicLinkNode : topicLinkNodes) {
 				Topic aTopic = topicLinkNode.topic;
 				TopicNode aTopicNode = topicNodes.get(aTopic);
 				double dist = topicNode.distance + topicLinkNode.cost;
 				if (dist > limit) continue;
 				if (aTopicNode == null) {
 					aTopicNode = new TopicNode(aTopic, topicNode.topic, dist);
 					topicNodes.put(aTopic, aTopicNode);
 					queue.add(aTopicNode);
 				} else if (queue.contains(aTopicNode) && aTopicNode.distance > dist) {
 					queue.remove(aTopicNode);
 					aTopicNode.parent = topicNode.topic;
 					aTopicNode.distance = dist;
 					queue.add(aTopicNode);
 				}
 			}
 			finalTopicNodes.add(topicNode);
 		}
 		
 		Map<Topic, TopicTreeNode> ret = new HashMap<Topic, TopicTreeNode>();
 		for (TopicNode topicNode: finalTopicNodes) {
 			if (topicNode.distance <= limit)
 				ret.put(topicNode.topic, new TopicTreeNode(topicNode.topic));
 		}
 		
 		for (TopicNode topicNode: finalTopicNodes) {
 			if (topicNode.distance <= limit) 
 				ret.get(topicNode.topic).setParent(ret.get(topicNode.parent));
 		}
 		
 		return ret.get(rootTopic);
 	}
 	
 	public static TopicTreeNode expandTopicsForMinHopCount(Topic topic, int maxHopCount, int maxTopicNumberToExpand) {
 		return dijkstra(topic, new TopicExpanderForMinHopCount(), maxHopCount, maxTopicNumberToExpand);
 	}
 
 	public static TopicTreeNode expandTopicsForMaxWeight(Topic topic, double minWeight, int maxTopicNumberToExpand) {
 		return dijkstra(topic, new TopicExpanderForMaxWeight(), -Math.log(minWeight), maxTopicNumberToExpand);
 	}
 }
