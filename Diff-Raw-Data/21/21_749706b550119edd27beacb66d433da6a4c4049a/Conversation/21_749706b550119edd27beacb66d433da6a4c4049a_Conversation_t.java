 package edu.rit.asksg.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OrderBy;
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.annotations.Type;
 import org.joda.time.LocalDateTime;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooJpaEntity
 @RooJson(deepSerialize = true)
 public class Conversation {
 
 	@OrderBy("created")
 	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
 	private List<Message> messages = new ArrayList<Message>();
 
 	@NotNull
 	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
 	@DateTimeFormat(style = "M-")
 	private LocalDateTime created = new LocalDateTime();
 
 	@NotNull
 	@DateTimeFormat(style = "M-")
 	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
 	private LocalDateTime modified = new LocalDateTime();
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	private Service service;
 
 	private String externalId;
 
 	private Boolean hidden = Boolean.FALSE;
 
 	private Boolean isRead = Boolean.FALSE;
 
 	private String subject;
 
	private Boolean privateConversation = Boolean.FALSE;

 	public Conversation() {
 	}
 
 	public Conversation(Message m) {
 		this.messages = new ArrayList<Message>();
 		this.messages.add(m);
 		m.setConversation(this);
 	}
 
 	public String getRecipient() {
 		if (!messages.isEmpty()) {
 			return messages.get(0).getAuthor();
 		}
 		throw new IllegalStateException("Conversation object was not constructed properly, there must be at least one message");
 	}
 }
