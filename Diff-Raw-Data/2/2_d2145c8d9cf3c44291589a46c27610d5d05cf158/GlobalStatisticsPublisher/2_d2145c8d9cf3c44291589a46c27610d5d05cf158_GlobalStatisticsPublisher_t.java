 package pt.com.gcs.messaging;
 
 import java.util.Date;
 
 import org.caudexorigo.text.DateUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.types.NetBrokerMessage;
 import pt.com.broker.types.NetPublish;
 import pt.com.broker.types.NetAction.DestinationType;
 import pt.com.broker.types.stats.ChannelStats;
 import pt.com.broker.types.stats.EncodingStats;
 import pt.com.broker.types.stats.MiscStats;
 import pt.com.gcs.conf.GcsInfo;
 
 public class GlobalStatisticsPublisher implements Runnable
 {
 	private static Logger log = LoggerFactory.getLogger(GlobalStatisticsPublisher.class);
 
 	private static Date date = new Date();
 
 	@Override
 	public void run()
 	{
 		Date oldDate = date;
 		date = new Date();
 
 		long difSeconds = (date.getTime() - oldDate.getTime()) / 1000;
 
 		String currentDateStr = DateUtil.formatISODate(date);
 
 		publishQueueInfo(currentDateStr, difSeconds);
 
 		publishTopicInfo(currentDateStr, difSeconds);
 
 		publishChannelInfo(currentDateStr, difSeconds);
 
 		publishEncodingInfo(currentDateStr, difSeconds);
 
 		publishMiscInformation(currentDateStr, difSeconds);
 	}
 
 	private void publishQueueInfo(String date, long seconds)
 	{
 		double dSeconds = (double) seconds;
 
 		StringBuilder sb = new StringBuilder();
 
 		sb.append(String.format("<mqinfo date='%s' agent-name='%s'>", date, GcsInfo.getAgentName()));
 
 		double rate;
 		long value;
 		for (QueueProcessor qp : QueueProcessorList.values())
 		{
 			StringBuilder qSb = new StringBuilder();
 			int infoCount = 0;
 
 			value = qp.getQueueStatistics().getQueueMessagesReceivedAndReset();
 			if (value != -1)
 			{
 				rate = ((double) value / dSeconds);
 				qSb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"input-rate\" value=\"%s\" />", qp.getQueueName(), rate));
 				++infoCount;
 			}
 
 			value = qp.getQueueStatistics().getQueueMessagesDeliveredAndReset();
 			if (value != -1)
 			{
 				rate = ((double) value / dSeconds);
 				qSb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"output-rate\" value=\"%s\" />", qp.getQueueName(), rate));
 				++infoCount;
 			}
 
 			value = qp.getQueueStatistics().getQueueMessagesFailedAndReset();
 			if (value != -1)
 			{
 				rate = ((double) value / dSeconds);
 				qSb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"failed-rate\" value=\"%s\" />", qp.getQueueName(), rate));
 				++infoCount;
 			}
 
 			value = qp.getQueueStatistics().getQueueMessagesExpiredAndReset();
 			if (value != -1)
 			{
 				rate = ((double) value / dSeconds);
 				qSb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"expired-rate\" value=\"%s\" />", qp.getQueueName(), rate));
 				++infoCount;
 			}
 
 			value = qp.getQueueStatistics().getQueueMessagesRedeliveredAndReset();
 			if (value != -1)
 			{
 				rate = ((double) value / dSeconds);
 				qSb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"redelivered-rate\" value=\"%s\" />", qp.getQueueName(), rate));
 				++infoCount;
 			}
 
 			if (infoCount != 0)
 			{
 				sb.append(qSb.toString()); // Add queue information only when something is different from zero
 			}
 		}
 
 		sb.append("\n</mqinfo>");
 
 		String result = sb.toString();
 
 		final String sys_topic = String.format("/system/stats/queues/#%s#", GcsInfo.getAgentName());
 
 		NetPublish np = new NetPublish(sys_topic, DestinationType.TOPIC, new NetBrokerMessage(result));
 
 		Gcs.publish(np);
 	}
 
 	private void publishTopicInfo(String date, long seconds)
 	{
 		double dSeconds = (double) seconds;
 
 		StringBuilder sb = new StringBuilder();
 
 		sb.append(String.format("<mqinfo date='%s' agent-name='%s'>", date, GcsInfo.getAgentName()));
 
 		double rate;
 		long value;
 
 		rate = ((double) TopicProcessorList.getTopicMessagesReceivedAndReset() / dSeconds);
 
 		sb.append(String.format("\n\t<item subject='topic://.*' predicate='input-rate' value='%s' />", rate));
 		for (TopicProcessor tp : TopicProcessorList.values())
 		{
 			StringBuilder tSb = new StringBuilder();
 			int infoCount = 0;
 
 			value = tp.getTopicStatistics().getTopicMessagesDeliveredAndReset();
 			if (value != -1)
 			{
 				rate = ((double) value / dSeconds);
 				tSb.append(String.format("\n	<item subject=\"topic://%s\" predicate=\"output-rate\" value=\"%s\" />", tp.getSubscriptionName(), rate));
 				++infoCount;
 			}
 
 			value = tp.getTopicStatistics().getTopicMessagesDiscardedAndReset();
 			if (value != -1)
 			{
 				rate = ((double) value / dSeconds);
 				tSb.append(String.format("\n	<item subject=\"topic://%s\" predicate=\"discarded-rate\" value=\"%s\" />", tp.getSubscriptionName(), rate));
 				++infoCount;
 			}
 
 			value = tp.getTopicStatistics().getTopicMessagesDispatchedToQueueAndReset();
 			if (value != -1)
 			{
 				rate = ((double) value / dSeconds);
 				tSb.append(String.format("\n	<item subject=\"topic://%s\" predicate=\"dispatched-to-queue-rate\" value=\"%s\" />", tp.getSubscriptionName(), rate));
 				++infoCount;
 			}
 			if (infoCount != 0)
 			{
 				sb.append(tSb.toString()); // Add queue information only when something is different from zero
 			}
 		}
 
 		sb.append("\n</mqinfo>");
 
 		String result = sb.toString();
 
 		final String sys_topic = String.format("/system/stats/topics/#%s#", GcsInfo.getAgentName());
 
 		NetPublish np = new NetPublish(sys_topic, DestinationType.TOPIC, new NetBrokerMessage(result));
 
 		Gcs.publish(np);
 	}
 
 	private void publishChannelInfo(String date, long seconds)
 	{
 		double dSeconds = (double) seconds;
 
 		StringBuilder sb = new StringBuilder();
 
 		sb.append(String.format("<mqinfo date='%s' agent-name='%s'>", date, GcsInfo.getAgentName()));
 
 		double rate;
 
 		rate = ((double) ChannelStats.getDropboxReceivedMessagesAndReset() / dSeconds);
 		sb.append(String.format("\n\t<item subject='dropbox' predicate='input-rate' value='%s' />", rate));
 		rate = ((double) ChannelStats.getHttpReceivedMessagesAndReset() / dSeconds);
 		sb.append(String.format("\n\t<item subject='http' predicate='input-rate' value='%s' />", rate));
 
 		sb.append("\n</mqinfo>");
 		String result = sb.toString();
 
 		final String sys_topic = String.format("/system/stats/channels/#%s#", GcsInfo.getAgentName());
 		NetPublish np = new NetPublish(sys_topic, DestinationType.TOPIC, new NetBrokerMessage(result));
 
 		Gcs.publish(np);
 	}
 
 	private void publishEncodingInfo(String date, long seconds)
 	{
 		double dSeconds = (double) seconds;
 
 		StringBuilder sb = new StringBuilder();
 
 		sb.append(String.format("<mqinfo date='%s' agent-name='%s'>", date, GcsInfo.getAgentName()));
 
 		double rate;
 
 		rate = ((double) EncodingStats.getSoapDecodedMessageAndReset() / dSeconds);
 		sb.append(String.format("\n\t<item subject='xml' predicate='input-rate' value='%s' />", rate));
 		rate = ((double) EncodingStats.getSoapEncodedMessageAndReset() / dSeconds);
 		sb.append(String.format("\n\t<item subject='xml' predicate='output-rate' value='%s' />", rate));
 
 		rate = ((double) EncodingStats.getProtoDecodedMessageAndReset() / dSeconds);
 		sb.append(String.format("\n\t<item subject='protobuf' predicate='input-rate' value='%s' />", rate));
 		rate = ((double) EncodingStats.getProtoEncodedMessageAndReset() / dSeconds);
 		sb.append(String.format("\n\t<item subject='protobuf' predicate='output-rate' value='%s' />", rate));
 
 		rate = ((double) EncodingStats.getThriftDecodedMessageAndReset() / dSeconds);
 		sb.append(String.format("\n\t<item subject='thrift' predicate='input-rate' value='%s' />", rate));
 		rate = ((double) EncodingStats.getThriftEncodedMessageAndReset() / dSeconds);
 		sb.append(String.format("\n\t<item subject='thrift' predicate='output-rate' value='%s' />", rate));
 
 		sb.append("\n</mqinfo>");
 
 		String result = sb.toString();
 
 		final String sys_topic = String.format("/system/stats/encoding/#%s#", GcsInfo.getAgentName());
 		NetPublish np = new NetPublish(sys_topic, DestinationType.TOPIC, new NetBrokerMessage(result));
 
 		Gcs.publish(np);
 	}
 
 	private void publishMiscInformation(String date, long seconds)
 	{
 		double dSeconds = (double) seconds;
 
 		StringBuilder sb = new StringBuilder();
 		sb.append(String.format("<mqinfo date='%s' agent-name='%s'>", date, GcsInfo.getAgentName()));
 
 		double rate;
 		// invalid messages
 		rate = ((double) MiscStats.getInvalidMessagesAndReset() / dSeconds);
 		sb.append(String.format("\n\t<item subject='invalid-messages' predicate='input-rate' value='%s' />", rate));
 
 		// access denied
 		rate = ((double) MiscStats.getAccessesDeniedAndReset() / dSeconds);
 		sb.append(String.format("\n\t<item subject='access' predicate='denied' value='%s' />", rate));
 
 		// tcp, tcp-legacy, ssl
 		sb.append(String.format("\n\t<item subject='tcp' predicate='connections' value='%s' />", MiscStats.getTcpConnections()));
 		sb.append(String.format("\n\t<item subject='tcp-legacy' predicate='connections' value='%s' />", MiscStats.getTcpLegacyConnections()));
 		sb.append(String.format("\n\t<item subject='ssl' predicate='connections' value='%s' />", MiscStats.getSslConnections()));
 
 		long f_sys_msgs = MiscStats.getSystemMessagesFailuresAndReset();
 
 		// System messages - failed delivery (count)
 		sb.append(String.format("\n\t<item subject='system-message' predicate='failed-delivery' value='%s' />", f_sys_msgs));
 
 		// faults (rate)
 		rate = ((double) MiscStats.getFaultsAndReset() / dSeconds);
 		sb.append(String.format("\n\t<item subject='faults' predicate='rate' value='%s' />", rate));
 
 		sb.append("\n</mqinfo>");
 
 		String result = sb.toString();
 
 		final String sys_topic = String.format("/system/stats/misc/#%s#", GcsInfo.getAgentName());
 		NetPublish np = new NetPublish(sys_topic, DestinationType.TOPIC, new NetBrokerMessage(result));
 
 		Gcs.publish(np);
 
		log.info("Failed system messages: '{}'.", f_sys_msgs);
 	}
 }
