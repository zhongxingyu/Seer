 package com.wp.carlos4web.cdi.components.messages;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Singleton;
 
 import org.apache.log4j.Logger;
 
 @Singleton
 public class MessageComponent implements IMessageComponent{
 
 	private static final Logger logger = Logger.getLogger(MessageComponent.class);
 	
 	private Collection<Message> messages;
 	
 	public MessageComponent(){
 		
 	}
 	
 	@SuppressWarnings("unused")
 	@PostConstruct
 	private void init(){
 		if(this.messages == null){
 			logger.info("Configurando o componente de mensagens.");
			this.messages = new ArrayList<Message>(0);
 		}
 	}
 	
 	@Override
 	public void addMessage(Message message) {
 		this.messages.add(message);
 	}
 
 	@Override
 	public void debugMessages() {
 		for (Message m : this.messages) {
 			logger.info("Descriçao: " + m.getDescricao());
 		}
 	}
 }
