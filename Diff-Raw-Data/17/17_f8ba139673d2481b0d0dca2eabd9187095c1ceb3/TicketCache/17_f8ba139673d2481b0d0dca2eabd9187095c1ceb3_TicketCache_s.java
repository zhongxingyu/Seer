 package com.artisztikum.ac.cache;
 
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.eclipse.jetty.client.ContentExchange;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import com.artisztikum.ac.Util;
 import com.artisztikum.ac.ac.AbstractCompletableProjectObject;
 import com.artisztikum.ac.ac.AbstractProjectObject;
 import com.artisztikum.ac.ac.Ticket;
 import com.artisztikum.ac.ac.TicketAssignee;
 import com.artisztikum.ac.httpclient.ACHttpClient;
 
 /**
  * Contains the {@link Ticket}s.
  *
  * @author Adam DAJKA (dajka@artisztikum.hu)
  *
  */
 public final class TicketCache
 {
 	/**
 	 * Logger.
 	 */
 	private static final Logger LOG = LoggerFactory.getLogger(TicketCache.class);
 
 	/**
 	 * Singleton instance.
 	 */
 	private static TicketCache singleton;
 
 	/**
 	 * {@link JAXBContext} for unmarshalling {@link Ticket}s.
 	 */
 	private static JAXBContext ticketJC;
 	static {
 		try {
 			ticketJC = JAXBContext.newInstance(Ticket.class, AbstractCompletableProjectObject.class,
 					AbstractProjectObject.class, TicketAssignee.class);
 		} catch (final JAXBException e1) {
 			throw new RuntimeException(e1);
 		}
 	}
 
 	/**
 	 * Ticket cache. Configurable maximum size.
 	 */
 	private final Map<String, Ticket> allTickets = Collections
 			.<String, Ticket> synchronizedMap(new LinkedHashMap<String, Ticket>() {
 				/**
 				 * Serial.
 				 */
 				private static final long serialVersionUID = 1L;
 
 				private static final int MAX_SIZE = 10000;
 
 				@Override
 				protected boolean removeEldestEntry(final Map.Entry<String, Ticket> eldest)
 				{
 					return size() > MAX_SIZE;
 				}
 			});
 
 	/**
 	 * The http client.
 	 */
 	private final ACHttpClient client;
 
 	/**
 	 * @param client
 	 *            Http client to use.
 	 */
 	public TicketCache(final ACHttpClient client)
 	{
 		this.client = client;
 	}
 
 	/**
 	 * Gets all tickets for the given projects.
 	 *
 	 * @param projectId
 	 *            The id of the project
 	 * @return All tickets for the given projects.
 	 */
 	public List<Ticket> getTickets(final String projectId)
 	{
 		// get the fresh list of tickets from the Active Collab API
 		final ContentExchange ex = client.sendGetWait("/projects/%s/tickets/", projectId);
 		LOG.debug("Got ticket xml");
 
 		// parse the response => get the list of tickets
 		final NodeList nl;
 		try {
 			nl = (NodeList) Util.getXpath("//tickets/ticket").evaluate(
 					new InputSource(new StringReader(ex.getResponseContent())), XPathConstants.NODESET);
 		} catch (final XPathExpressionException e) {
 			throw new RuntimeException(e);
 		} catch (final UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 
 		final List<Ticket> result = new ArrayList<Ticket>();
 
 		final Map<String, ContentExchange> reqs = new HashMap<String, ContentExchange>();
 		for (int i = 0; i < nl.getLength(); i++) {
 			final Node n = nl.item(i);
 
 			final String id;
 			final String ticketId;
 			final String ticketVersion;
 			try {
 				id = (String) Util.getXpath("id").evaluate(n, XPathConstants.STRING);
 				ticketId = (String) Util.getXpath("ticket_id").evaluate(n, XPathConstants.STRING);
 				ticketVersion = (String) Util.getXpath("version").evaluate(n, XPathConstants.STRING);
 			} catch (final XPathExpressionException e) {
 				throw new RuntimeException(e);
 			}
 
 			if (!allTickets.containsKey(id) || !allTickets.get(id).getVersion().equals(Integer.parseInt(ticketVersion))) {
				LOG.debug("Ticket {} (version {}) fetching...", ticketId, ticketVersion);
 				reqs.put(id, client.sendGet("/projects/%s/tickets/%s", projectId, ticketId));
 
 			} else {
				LOG.debug("Ticket {} (version {}) found in cache", ticketId, ticketVersion);
 				result.add(allTickets.get(id));
 			}
 		}
 
 		final Map<String, ContentExchange> finished = new HashMap<String, ContentExchange>();
 		while (!reqs.isEmpty()) {
 
 			synchronized (this) {
 				try {
					wait(1000);
 				} catch (final InterruptedException e) {
 					throw new RuntimeException(e);
 				}
 			}
 
 			final Iterator<Entry<String, ContentExchange>> it = reqs.entrySet().iterator();
 
 			while (it.hasNext()) {
 				final Entry<String, ContentExchange> req = it.next();
 				if (req.getValue().isDone()) {
 					finished.put(req.getKey(), req.getValue());
 					it.remove();
 				}
 			}
 
 			LOG.debug("number of pending requests: {}", reqs.size());
 		}
 
 		for (final Entry<String, ContentExchange> entry : finished.entrySet()) {
 			try {
 				final Ticket fetchedTicket = unmarshalTicket(new StreamSource(new StringReader(entry.getValue()
 						.getResponseContent())));
 				result.add(fetchedTicket);
 				allTickets.put(entry.getKey(), fetchedTicket);
 			} catch (final UnsupportedEncodingException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Unmarshals a {@link Source} into a {@link Ticket} with {@link #ticketJC}.
 	 *
 	 * @param src
 	 *            The {@link Source}
 	 * @return The unmarshalled {@link Ticket} instance.
 	 */
 	public static Ticket unmarshalTicket(final Source src)
 	{
 		try {
 			final Unmarshaller u = ticketJC.createUnmarshaller();
 			final JAXBElement<Ticket> doc = u.unmarshal(src, Ticket.class);
 			return doc.getValue();
 		} catch (final JAXBException e) {
 			throw new RuntimeException("Error de-serializing ticket", e);
 		}
 	}
 
 	/**
 	 * Static init of the singleton instance.
 	 *
 	 * @param client
 	 *            the client
 	 */
 	public static void init(final ACHttpClient client)
 	{
 		if (null != singleton) {
 			throw new IllegalStateException("Already initialized");
 		}
 
 		singleton = new TicketCache(client);
 	}
 
 	/**
 	 * @return the singleton instance.
 	 */
 	public static TicketCache get()
 	{
 		if (null == singleton) {
 			throw new IllegalStateException("Not initialized yet");
 		}
 
 		return singleton;
 	}
 
 }
