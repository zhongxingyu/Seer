 package pt.com.gcs.messaging;
 
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 class TopicMatcher
 {
 	private static Logger log = LoggerFactory.getLogger(TopicMatcher.class);
 
 	public static boolean match(String subscriptionName, String topicName)
 	{
 		if (StringUtils.countMatches(subscriptionName, "/") < 2)
 		{
 			return false;
 		}
 
 		if (recursiveMatch(subscriptionName, topicName))
 		{
 			return true;
 		}
 
 		if (wildcardMatch(subscriptionName, topicName))
 		{
 			return true;
 		}
 
 		return false;
 	}
 
 	private static boolean wildcardMatch(String subscriptionName, String topicName)
 	{
 		try
 		{
 			if (!StringUtils.contains(subscriptionName, '#'))
 			{
 				return false;
 			}
 
 			String[] sub_parts = subscriptionName.split("/");
 			String[] topic_parts = topicName.split("/");
 
 			if (sub_parts.length != topic_parts.length)
 				return false;
 
 			for (int i = 0; i < sub_parts.length; i++)
 			{
 				if (sub_parts[i].equals("#"))
 				{
 					topic_parts[i] = "#";
 				}
 			}
 
 			StringBuilder sb = new StringBuilder();
 			for (int i = 1; i < topic_parts.length; i++)
 			{
 				sb.append("/");
 				sb.append(topic_parts[i]);
 			}
 
 			if (sb.toString().equals(subscriptionName))
 			{
 				return true;
 			}
 
 			return false;
 		}
		catch (Throwable t)
 		{
			String message = String.format("wildcardMatch-> subscriptionName: '%s'; topicName: '%s'", subscriptionName, topicName);
			log.error(message, t);
			return false;
 		}
 	}
 
 	private static boolean recursiveMatch(String subscriptionName, String topicName)
 	{
 		try
 		{
 			if (!subscriptionName.endsWith("/>"))
 			{
 				return false;
 			}
 
 			String[] sub_parts = subscriptionName.split("/");
 			String[] topic_parts = topicName.split("/");

 			if (topic_parts.length < sub_parts.length)
 			{
 				return false;
 			}
 
 			int pos = -1;
 			for (int i = 0; i < sub_parts.length; i++)
 			{
 				if (sub_parts[i].equals(">"))
 				{
 					pos = i;
 				}
 			}
 
 			StringBuilder sb = new StringBuilder();
 			for (int i = 1; i < pos; i++)
 			{
 				sb.append("/");
 				sb.append(topic_parts[i]);
 			}
 			sb.append("/>");
 
 			if (sb.toString().equals(subscriptionName))
 			{
 				return true;
 			}
 
 			return false;
 		}
		catch (Throwable t)
 		{
			String message = String.format("recursiveMatch-> subscriptionName: '%s'; topicName: '%s'", subscriptionName, topicName);
			log.error(message, t);
			return false;
 		}
 	}
 }
