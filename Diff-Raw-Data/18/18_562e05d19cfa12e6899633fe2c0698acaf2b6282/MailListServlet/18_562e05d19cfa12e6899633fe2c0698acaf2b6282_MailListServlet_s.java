 package zinger.secsan.server;
 
 import com.google.common.base.*;
 import com.google.common.collect.*;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.util.logging.*;
 import java.util.regex.*;
 
 import javax.mail.*;
 import javax.mail.internet.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import zinger.secsan.client.*;
 import zinger.secsan.db.*;
 
 public class MailListServlet extends HttpServlet
 {
 	public static final Function<String, InternetAddress> STRING_TO_ADDRESS = new Function<String, InternetAddress>()
 	{
 		public InternetAddress apply(final String email)
 		{
 			try
 			{
 				return new InternetAddress(email);
 			}
 			catch(final AddressException ex)
 			{
 				ex.printStackTrace();
 				return null;
 			}
 		}
 	};
 	
 	public static final Function<Address, String> ADDRESS_TO_STRING = new Function<Address, String>()
 	{
 		public String apply(final Address address)
 		{
 			return address instanceof InternetAddress ? ((InternetAddress)address).getAddress() : address.toString();
 		}
 	};
 	
 	protected final Logger log = Logger.getLogger(getClass().getName());
 	
 	public static final String LIST_EMAIL_PREFIX = "list-";
 	public static final String DOMAIN_SUFFIX = ".appspotmail.com";
 	
 	protected String appId;
 	protected String emailDomainSuffix;
 	protected String sysEmail;
 	
 	protected final StateManager stateManager = StateManagerFactory.INSTANCE.getStateManager();
 	
 	public void init(final ServletConfig config)
 	{
 		appId = config.getInitParameter("appId");
 		if(appId == null)
 			appId = config.getServletContext().getInitParameter("appId");
 		emailDomainSuffix = appId + DOMAIN_SUFFIX;
 		sysEmail = "system@" + emailDomainSuffix;
 		
 		log.info("email domain suffix: " + emailDomainSuffix);
 		log.info("list email prefix: " + LIST_EMAIL_PREFIX);
 	}
 	
 	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
 	{
 		final Session mailSession = Session.getDefaultInstance(new Properties(), null);
 		
 		try
 		{
 			final MimeMessage incomingMessage = new MimeMessage(mailSession, request.getInputStream());
 			
 			Address sender = incomingMessage.getSender();
 			if(sender == null)
 				sender = incomingMessage.getFrom()[0];
 			
 			// Note: in the following passage, we'll mark rejected addresses under BCC routing list
 			final SetMultimap<Message.RecipientType, Address> routing = HashMultimap.create();
 			for(final Message.RecipientType recipientType : Arrays.asList(Message.RecipientType.TO, Message.RecipientType.CC))
 			{
 				final Pair<? extends Iterable<Address>, ? extends Iterable<Address>> recipientTypeRouting = routeAddresses(incomingMessage.getRecipients(recipientType), sender);
 				routing.putAll(recipientType, recipientTypeRouting.head);
 				routing.putAll(Message.RecipientType.BCC, recipientTypeRouting.tail);
 			}
 			log.info("Routing: " + routing);
 			
 			if(routing.containsKey(Message.RecipientType.TO) || routing.containsKey(Message.RecipientType.CC))
 				routeMessage(mailSession, incomingMessage, routing.get(Message.RecipientType.TO), routing.get(Message.RecipientType.CC));
 			
 			if(routing.containsKey(Message.RecipientType.BCC))
 				rejectMessage(mailSession, incomingMessage, routing.get(Message.RecipientType.BCC));
 		}
 		catch(final MessagingException ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 	
 	protected Pair<? extends Iterable<Address>, ? extends Iterable<Address>> routeAddresses(final Iterable<Address> recipients, final Address sender) throws MessagingException, NoSuchElementException
 	{
 		final Set<Address> processed = new HashSet<Address>();
 		final Set<Address> rejected = new HashSet<Address>();
 		
 		final Iterable<Address> toProcess = Iterables.filter(recipients, new Predicate<Address>()
 		{
 			public boolean apply(final Address recipient)
 			{
 				return recipient instanceof InternetAddress && ((InternetAddress)recipient).getAddress().toLowerCase().endsWith(emailDomainSuffix);
 			}
 		});
 		
 		final Iterable<Address> listRecipients = Iterables.filter(toProcess, new Predicate<Address>()
 		{
 			public boolean apply(final Address recipient)
 			{
 				return ((InternetAddress)recipient).getAddress().toLowerCase().startsWith(LIST_EMAIL_PREFIX);
 			}
 		});
 		
 		for(final Address listAddress : listRecipients)
 		{
 			final String email = ((InternetAddress)listAddress).getAddress();
 			final String list = email.substring(LIST_EMAIL_PREFIX.length(), email.indexOf("@"));
 			final Set<String> usersInPool = stateManager.getUsersInPool(list);
 			if(usersInPool.contains(((InternetAddress)sender).getAddress()))
 			{
 				for(final InternetAddress address : Iterables.transform(usersInPool, STRING_TO_ADDRESS))
 					processed.add(address);
 			}
 			else
 				for(final InternetAddress address : Iterables.transform(usersInPool, STRING_TO_ADDRESS))
 					rejected.add(address);
 		}
 		
 		return Pair.of(processed, rejected);
 	}
 	
 	protected Pair<? extends Iterable<Address>, ? extends Iterable<Address>> routeAddresses(final Address[] recipients, final Address sender) throws MessagingException, NoSuchElementException
 	{
 		return recipients == null ?
 			routeAddresses((Set<Address>)Collections.EMPTY_SET, sender) :
 			routeAddresses(Arrays.asList(recipients), sender);
 	}
 	
 	protected void routeMessage(
 		final Session mailSession, 
 		final MimeMessage incomingMessage, 
 		final Collection<Address> to, 
 		final Collection<Address> cc
 	) throws MessagingException, IOException
 	{
 		final MimeMessage outgoingMessage = new MimeMessage(mailSession);
 		final Address sender = 
 			!to.isEmpty() ?
 				to.iterator().next() :
 				!cc.isEmpty() ?
 					cc.iterator().next() :
 					new InternetAddress(sysEmail);
 		outgoingMessage.setFrom(sender);
 		outgoingMessage.setSubject(incomingMessage.getSubject());
 		
 		final Object originalContent = incomingMessage.getContent();
 		if(originalContent instanceof Multipart)
 			outgoingMessage.setContent((Multipart)originalContent);
 		else if(originalContent instanceof String)
 			outgoingMessage.setText((String)originalContent);
 		else if(originalContent instanceof InputStream)
 		{
 			final BodyPart body = new MimeBodyPart((InputStream)originalContent);
 			final Multipart multipart = new MimeMultipart();
 			multipart.addBodyPart(body);
 			outgoingMessage.setContent(multipart);
 		}
 		else
 			throw new IllegalArgumentException(String.format("Could not process content type %s", originalContent.getClass().getName()));
 		
 		Transport.send(outgoingMessage);
 	}
 	
 	protected void rejectMessage(
 		final Session mailSession, 
 		final MimeMessage incomingMessage, 
 		final Collection<Address> rejected
 	) throws MessagingException
 	{
 		throw new UnsupportedOperationException();
 	}
 }
