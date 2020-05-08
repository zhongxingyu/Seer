 package com.livegameengine.web;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.jdo.PersistenceManager;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentType;
 import org.w3c.dom.Node;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import com.google.appengine.api.channel.ChannelService;
 import com.google.appengine.api.channel.ChannelServiceFactory;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.appengine.api.utils.SystemProperty;
 import com.livegameengine.config.Config;
 import com.livegameengine.error.GameLoadException;
 import com.livegameengine.model.ClientMessage;
 import com.livegameengine.model.Game;
 import com.livegameengine.model.GameState;
 import com.livegameengine.model.GameType;
 import com.livegameengine.model.GameURIResolver;
 import com.livegameengine.model.GameUser;
 import com.livegameengine.model.Player;
 import com.livegameengine.model.Watcher;
 import com.livegameengine.persist.PMF;
 import com.livegameengine.util.AddListenersParser;
 import com.livegameengine.util.Util;
 import com.sun.org.apache.xerces.internal.dom.DocumentTypeImpl;
 import com.sun.xml.internal.fastinfoset.stax.util.StAXParserWrapper;
 
 public class GameServlet extends HttpServlet {
 	Log log = LogFactory.getLog(GameServlet.class);
 	Config config = Config.getInstance();
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		handleRequest(req, resp);
 	}
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		handleRequest(req, resp);
 	}
 
 	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		UserService userService = UserServiceFactory.getUserService();
 		User u = userService.getCurrentUser();
 		GameUser gu = GameUser.findOrCreateGameUserByUser(u);		
 		
 		Pattern p = Pattern.compile("/-/([^/]+)/(meta|data|raw_view|join|start|addListeners|message|(event/([^/]+)))?");
 		
 		final Matcher m = p.matcher(req.getPathInfo());
 		
 		//log.info("path info: " + req.getPathInfo());
 		
 		if(m != null && m.matches()) {
 			Game g;
 			try {
 				g = Game.findGameByKey(KeyFactory.stringToKey(m.group(1)));
 			} catch (GameLoadException e1) {
 				g = null;
 			}
 			
 			if(g == null) {
 				resp.setStatus(404);
 				resp.setContentType("text/plain");
 				resp.getWriter().write("game not found: " + m.group(1));
 				return;
 			}
 			
 			GameState gs = g.getMostRecentState();
 			
 			if(gs == null) {
 				resp.setStatus(404);
 				resp.setContentType("text/plain");
 				resp.getWriter().write("game has no recent state: " + m.group(1));
 				return;				
 			}
 			
 			Document doc = null;
 			Document doc1 = null;
 			
 			doc = config.newXmlDocument();
 			doc1 = config.newXmlDocument();
 			
 			OutputStream responseStream = resp.getOutputStream();
 			StreamResult responseResult = new StreamResult(responseStream);
 						
 			
 			if(m.group(2) == null) {
 				if(!req.getMethod().equals("GET")) {
 					resp.setStatus(405);
 					return;
 				}
 				
 				resp.setContentType("application/xhtml+xml");
 				
 				GameSource s = new GameSource(gs);					
 				Transformer gametrans = new GameTransformer(gu);
 
 				try {
 					gametrans.transform(s, responseResult);
 				} catch (TransformerException e) {
 					throw new IOException(e);
 				}
 			}
 			else if(m.group(2).equals("raw_view")) {
 				if(req.getMethod().equals("GET")) {
 					resp.setContentType("text/xml");
 					
 					GameSource s = new GameSource(gs);
 					Transformer gametrans = new GameTransformer(gu);
 					
 					gametrans.setOutputProperty(GameTransformer.PROPERTY_OUTPUT_TYPE, "Raw");
 
 					try {
 						gametrans.transform(s, responseResult);
 					} catch (TransformerException e) {
 						throw new IOException(e);
 					}
 				}
 				else {
 					resp.setStatus(405);
 				}
 			}
 			else if(m.group(2).equals("data")) {
 				if(req.getMethod().equals("GET")) {
 					resp.setContentType("text/xml");
 					
 					GameSource s = new GameSource(gs);
 					Transformer gametrans = new GameTransformer(gu);
 					
 					gametrans.setOutputProperty(GameTransformer.PROPERTY_OUTPUT_TYPE, "Data");
 					
 					try {
 						gametrans.transform(s, responseResult);
 					} catch (TransformerException e) {
 						throw new IOException(e);
 					}
 				}
 				else {
 					resp.setStatus(405);
 				}
 			}
 			else if(m.group(2).equals("meta")) {
 				if(req.getMethod().equals("GET")) {
 					resp.setContentType("text/xml");			
 					
 					
 					XMLOutputFactory factory = XMLOutputFactory.newFactory();
 					XMLStreamWriter writer = null;
 					try {
 						writer = factory.createXMLStreamWriter(responseStream, config.getEncoding());
 						writer.setDefaultNamespace(config.getGameEngineNamespace());
 						
 						
 						writer.writeStartDocument();
 						
 						g.serializeToXml("game", writer);
 						writer.writeEndDocument();
 						writer.flush();
 						
 					} catch (XMLStreamException e) {
 						resp.setStatus(500);
 						e.printStackTrace();
 					}						
 				}
 				else {
 					resp.setStatus(405);
 				}
 			}
 			else if(m.group(2).equals("addListeners")) {
 				/*
 				Document d = config.newXmlDocument();
 				Transformer t = null;
 				try {
 					t = config.newTransformer();
 					t.transform(new StreamSource(req.getInputStream()), new DOMResult(d));
 				} catch (TransformerConfigurationException e) {
 					resp.setStatus(500);
 					e.printStackTrace();
 				} catch (TransformerException e) {
 					// invalid xml input
 					resp.setStatus(400);
 					e.printStackTrace();
 				}
 				*/
 				
 				Set<String> listeners = parseAddListenersRequest(req);
 				
 				Watcher w = Watcher.findWatcherByGameAndGameUser(g, gu);
 				
 				if(w != null) {
 					w.addListners(listeners);
 				}
 			}
 			else if(m.group(2).equals("message")) {
 				if(!req.getMethod().equals("GET")) {
 					resp.setStatus(405);
 					return;
 				}
 				
 				String since = req.getParameter("since");
 				
 				if(since == null) {
 					resp.setStatus(400);
 					return;
 				}
 				
 				Date s;
 				try {
 					s = config.getDateFormat().parse(since);
 				} catch (ParseException e) {
 					resp.setStatus(400);
 					return;
 				}
 				
 
 				List<ClientMessage> messages = ClientMessage.findClientMessagesSince(g, s);
 				
 				if(messages == null || messages.size() == 0) {
 					resp.setStatus(204);
 					return;
 				}
 				
 				ClientMessageSource so = new ClientMessageSource(messages);
 				Transformer gametrans = new GameTransformer(gu);
 				
 				gametrans.setOutputProperty(GameTransformer.PROPERTY_OUTPUT_TYPE, "View");
 				
 				
 				try {
 					gametrans.transform(so, responseResult);
 				} catch (TransformerException e) {
 					throw new IOException(e);
 				}
 				
 			}
 			else if(m.group(2).equals("start") || m.group(2).equals("join") || m.group(2).startsWith("event/")) {
 				if(!req.getMethod().equals("POST")) {
 					resp.setStatus(405);
 					resp.addHeader("Allow", "POST");
 					return;
 				}
 				
 				if(u == null) {
 					resp.setStatus(403);
 					return;
 				}
 				
 				boolean success = true;
 				
 				if(m.group(2).equals("start")) {
 					success = g.sendStartGameRequest(u);
 				}
 				else if(m.group(2).equals("join")) {
 					success = g.sendPlayerJoinRequest(u);
 				}
 				else if(m.group(2).startsWith("event/")) {
 					String eventName = "board." + m.group(4);
 					
 					Node data = doc.createElementNS(config.getGameEngineNamespace(), "data");
 					
 					Node player = doc.createElementNS(config.getGameEngineNamespace(), "player");
 					player.appendChild(doc.createTextNode(gu.getHashedUserId()));
 					
 					data.appendChild(player);
 					doc.appendChild(data);
 					
 					DOMResult dr = new DOMResult(data);
 									
 					if(req.getContentLength() > 0) {
 						try {
 							Transformer trans = config.newTransformer();
 							
 							trans.transform(new StreamSource(req.getInputStream()), dr);
 						} catch (TransformerException e) {
 							// ignore error
 						}
 					}
 					
 					success = g.triggerEvent(eventName, data);
 				}
 					
 				if(!success) {
 					String errorMessage = "";
 					if(!g.isError()) {
 						// there was no official error, the event is simply not valid
 						errorMessage = "Action not valid in the current game state";
 					}
 					else {
 						errorMessage = g.getErrorMessage();
 					}
 					
 					Node error = doc1.createElementNS(config.getGameEngineNamespace(), "error");
 					error.appendChild(doc1.createTextNode(errorMessage));
 					doc1.appendChild(error);
 											
 					resp.setContentType("application/xml");
 					resp.setStatus(400);
 					Transformer trans;
 					try {
 						trans = config.newTransformer();
 						trans.transform(new DOMSource(doc1), responseResult);
 					} catch (TransformerConfigurationException e) {
 						resp.setStatus(500);
 						e.printStackTrace();
 					} catch (TransformerException e) {
 						resp.setStatus(500);
 						e.printStackTrace();
 					}
 					
 					g.clearError();
 					
 				}
 				else {
 					resp.setStatus(200);
 				}
 			}
 			else {
 				resp.setStatus(404);
 				resp.setContentType("text/plain");
 				//resp.getWriter().write("unknown url");
 			}
 		}
 		else {
 			resp.setStatus(404);
 			resp.getWriter().write("game not found.");
 		}
 	}
 	
 	private Set<String> parseAddListenersRequest(HttpServletRequest req) throws IOException {
 		AddListenersParser alp;
 		try {
 			alp = new AddListenersParser(req);
 		} catch (XMLStreamException e) {
 			throw new IOException("parser error.");
 		}
 		
 		return alp.getEvents();
 		
 	}
 }
