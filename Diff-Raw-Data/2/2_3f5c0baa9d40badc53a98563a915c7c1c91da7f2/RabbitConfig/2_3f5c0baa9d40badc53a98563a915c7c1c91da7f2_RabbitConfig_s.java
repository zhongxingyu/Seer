 package edu.uc.labs.heartbeat.config;
 
 
 import com.typesafe.config.Config;
 import edu.uc.labs.heartbeat.domain.Machine;
 import edu.uc.labs.heartbeat.service.HeartbeatService;
 import edu.uc.labs.heartbeat.tasks.MessageListener;
 import org.springframework.amqp.core.*;
 import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
 import org.springframework.amqp.rabbit.connection.ConnectionFactory;
 import org.springframework.amqp.rabbit.core.RabbitAdmin;
 import org.springframework.amqp.rabbit.core.RabbitTemplate;
 import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
 import org.springframework.amqp.support.converter.DefaultClassMapper;
 import org.springframework.amqp.support.converter.JsonMessageConverter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 @Configuration
 public class RabbitConfig {
 
     @Bean
     public ConnectionFactory connectionFactory() {
         CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
         connectionFactory.setHost(config.getString("rabbit.host"));
         connectionFactory.setVirtualHost(config.getString("rabbit.vhost"));
         connectionFactory.setUsername(config.getString("rabbit.username"));
         connectionFactory.setPassword(config.getString("rabbit.password"));
         return connectionFactory;
     }
 
     @Bean
     public Queue machineQueue() {
         Queue q = new Queue(heartbeatService.getUUID());
         return q;
     }
 
 
     @Bean
     public DirectExchange heartbeatExchange() {
 
         DirectExchange ex = new DirectExchange("machine.status", true, false);
         return ex;
     }
 
     @Bean
     public Binding heartbeatBinding() {
         return BindingBuilder.bind(
                 machineQueue()).to(heartbeatExchange()).with(heartbeatService.getUUID());
     }
 
     @Bean
     public AmqpAdmin rabbitAdmin() {
         RabbitAdmin admin = new RabbitAdmin(connectionFactory());
         admin.declareQueue(machineQueue());
         admin.declareExchange(heartbeatExchange());
         admin.declareBinding(heartbeatBinding());
         admin.setAutoStartup(true);
         return admin;
     }
 
     @Bean
     public AmqpTemplate rabbitTemplate() {
         RabbitTemplate r = new RabbitTemplate(connectionFactory());
         r.setMessageConverter(messageConverter());
         return r;
     }
 
     @Bean
     public JsonMessageConverter messageConverter() {
         JsonMessageConverter converter = new JsonMessageConverter();
         DefaultClassMapper mapper = new DefaultClassMapper();
         mapper.setDefaultType(Machine.class);
         converter.setClassMapper(mapper);
         return converter;
     }
 
     @Bean
     public SimpleMessageListenerContainer messageListenerContainer() {
         SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
         container.setConnectionFactory(connectionFactory());
         container.setQueues(machineQueue());
        container.setMessageListener(new MessageListener());
         container.setAutoStartup(true);
         container.setConcurrentConsumers(1);
         return container;
     }
 
     @Autowired
     Config config;
 
     @Autowired
     HeartbeatService heartbeatService;
 
 }
