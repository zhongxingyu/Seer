 /**
  * Copyright ekupeng,Inc. 2012-2013
  * @Title: QueueBindingUtil.java
  *
  */
 package com.ekupeng.top.comet.client.queue;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.amqp.rabbit.connection.Connection;
 import org.springframework.amqp.rabbit.connection.ConnectionFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 import org.springframework.util.StringUtils;
 
 import com.rabbitmq.client.Channel;
 
 /**
  * @Description: 队列及路由规则绑定工具
  * @ClassName: QueueBindingUtil
  * @author emerson <emsn1026@gmail.com>
  * @date 2013-6-5 下午11:16:56
  * @version V1.0
  */
 @Component("queueBindingUtil")
 public class QueueBindingUtil implements InitializingBean {
 
 	@Value("${appID}")
 	private String appID;
 
 	/*
 	 * routing-key
 	 */
 	/*
 	 * 交易相关
 	 */
 	@Value("${trade.created.pattern}")
 	private String tradeCreatedPattern;
 
 	@Value("${trade.paid.pattern}")
 	private String tradePaidPattern;
 
 	@Value("${trade.success.pattern}")
 	private String tradeSuccessPattern;
 
 	@Value("${trade.closed.pattern}")
 	private String tradeClosedPattern;
 
 	@Value("${trade.sent.pattern}")
 	private String tradeSentPattern;
 
 	/*
 	 * 任务相关
 	 */
 	@Value("${task.complete.pattern}")
 	private String taskCompletePattern;
 
 	/*
 	 * routing-binding
 	 */
 	/*
 	 * 交易相关
 	 */
 	@Value("${trade.created.bind}")
 	private String tradeCreatedBind;
 
 	@Value("${trade.paid.bind}")
 	private String tradePaidBind;
 
 	@Value("${trade.success.bind}")
 	private String tradeSuccessBind;
 
 	@Value("${trade.closed.bind}")
 	private String tradeClosedBind;
 
 	@Value("${trade.sent.bind}")
 	private String tradeSentBind;
 	/*
 	 * 任务相关
 	 */
 	@Value("${task.complete.bind}")
 	private String taskCompleteBind;
 
 	/*
 	 * exchange
 	 */
 	@Value("${exchange.name}")
 	private String exchangeName;
 
 	@Autowired
 	private ConnectionFactory connectionFactory;
 
 	/**
 	 * 创建队列及其绑定
 	 * 
 	 * @throws IOException
 	 */
 	public void declareQueueAndBind() throws IOException {
 		if (connectionFactory == null)
 			throw new RuntimeException("必须先初始化connectionFactory");
 
 		// 初始化rabbitmq连接和通道
 		Connection connection = connectionFactory.createConnection();
 		Channel channel = connection.createChannel(false);
 
 		Map<String, List<String>> bindings = extractQueueAndBinding();
 
 		// 创建所有不存在的队列，若某队列存在则不做任何变更
 		// 同时对已存在的队列进行绑定
 		for (String binding : bindings.keySet()) {
 			List<String> queues = bindings.get(binding);
 			if (queues == null)
 				continue;
 			for (String queue : queues) {
 				// 创建队列
				queue = binding.replace("*", queue);
 				channel.queueDeclare(queue, true, false, false, null);
 				// 绑定队列到路由
 				channel.queueBind(queue, exchangeName, binding);
 			}
 		}
 	}
 
 	/**
 	 * 从配置config.properties中抽取队列及绑定数据
 	 * 
 	 * @return
 	 */
 	private Map<String, List<String>> extractQueueAndBinding() {
 		Map<String, List<String>> binding = new HashMap<String, List<String>>();
 		// 交易相关
 		if (StringUtils.hasText(tradeCreatedBind)) {
 			binding.put(tradeCreatedPattern,
 					Arrays.asList(tradeCreatedBind.split("#")));
 		}
 		if (StringUtils.hasText(tradePaidBind)) {
 			binding.put(tradePaidPattern,
 					Arrays.asList(tradePaidBind.split("#")));
 		}
 		if (StringUtils.hasText(tradeSentBind)) {
 			binding.put(tradeSentPattern,
 					Arrays.asList(tradeSentBind.split("#")));
 		}
 		if (StringUtils.hasText(tradeSuccessBind)) {
 			binding.put(tradeSuccessPattern,
 					Arrays.asList(tradeSuccessBind.split("#")));
 		}
 		if (StringUtils.hasText(tradeClosedBind)) {
 			binding.put(tradeClosedPattern,
 					Arrays.asList(tradeClosedBind.split("#")));
 		}
 		// 任务相关
 		if (StringUtils.hasText(taskCompleteBind)) {
 			binding.put(taskCompletePattern,
 					Arrays.asList(taskCompleteBind.split("#")));
 		}
 		// 退款相关
 		// TODO
 		// 商品相关
 		// TODO
 		return binding;
 	}
 
 	public void setAppID(String appID) {
 		this.appID = appID;
 	}
 
 	public void setTradeCreatedPattern(String tradeCreatedPattern) {
 		this.tradeCreatedPattern = tradeCreatedPattern;
 	}
 
 	public void setTradePaidPattern(String tradePaidPattern) {
 		this.tradePaidPattern = tradePaidPattern;
 	}
 
 	public void setTradeSuccessPattern(String tradeSuccessPattern) {
 		this.tradeSuccessPattern = tradeSuccessPattern;
 	}
 
 	public void setTradeClosedPattern(String tradeClosedPattern) {
 		this.tradeClosedPattern = tradeClosedPattern;
 	}
 
 	public void setTradeSentPattern(String tradeSentPattern) {
 		this.tradeSentPattern = tradeSentPattern;
 	}
 
 	public void setTradeCreatedBind(String tradeCreatedBind) {
 		this.tradeCreatedBind = tradeCreatedBind;
 	}
 
 	public void setTradePaidBind(String tradePaidBind) {
 		this.tradePaidBind = tradePaidBind;
 	}
 
 	public void setTradeSuccessBind(String tradeSuccessBind) {
 		this.tradeSuccessBind = tradeSuccessBind;
 	}
 
 	public void setTradeClosedBind(String tradeClosedBind) {
 		this.tradeClosedBind = tradeClosedBind;
 	}
 
 	public void setTradeSentBind(String tradeSentBind) {
 		this.tradeSentBind = tradeSentBind;
 	}
 
 	public void setExchangeName(String exchangeName) {
 		this.exchangeName = exchangeName;
 	}
 
 	public void setConnectionFactory(ConnectionFactory connectionFactory) {
 		this.connectionFactory = connectionFactory;
 	}
 
 	public void setTaskCompletePattern(String taskCompletePattern) {
 		this.taskCompletePattern = taskCompletePattern;
 	}
 
 	public void setTaskCompleteBind(String taskCompleteBind) {
 		this.taskCompleteBind = taskCompleteBind;
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		appID = appID.toUpperCase();
 		tradeCreatedPattern = appID + "." + tradeCreatedPattern.toUpperCase();
 		tradePaidPattern = appID + "." + tradePaidPattern.toUpperCase();
 		tradeSuccessPattern = appID + "." + tradeSuccessPattern.toUpperCase();
 		tradeClosedPattern = appID + "." + tradeClosedPattern.toUpperCase();
 		tradeSentPattern = appID + "." + tradeSentPattern.toUpperCase();
 		taskCompletePattern = appID + "." + taskCompletePattern.toUpperCase();
 		exchangeName = appID + "." + exchangeName.toUpperCase();
 	}
 
 }
