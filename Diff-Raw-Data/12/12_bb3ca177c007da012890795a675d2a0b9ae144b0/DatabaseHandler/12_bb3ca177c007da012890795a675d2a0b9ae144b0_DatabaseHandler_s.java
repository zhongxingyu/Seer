 package cz.cvut.felk.via.datalayer;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.jdo.Query;
 import javax.jdo.JDOHelper;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 
 import cz.cvut.felk.via.data.CommentInfo;
 import cz.cvut.felk.via.data.Event;
 
 public class DatabaseHandler {
 	
     private static final Logger logger = Logger.getLogger(DatabaseHandler.class.getName());
 	private static final PersistenceManagerFactory pmFactory = JDOHelper.getPersistenceManagerFactory("transactions-optional");
 	
 	public boolean addEvent(EventJDO e)	{
 		logger.log(Level.INFO,"addEvent called");
 		PersistenceManager pm = null;
 		try {
 			pm = pmFactory.getPersistenceManager();
 			pm.makePersistent(e);
 		} catch (Exception ex)	{
 			logger.log(Level.SEVERE, "addEvent exception: "+ex.getMessage());
 			return false;
 		} finally {
 			pm.close();
 		}
 		
 		return true;
 	}
 	
 	public boolean addEvents(ArrayList<EventJDO> events)	{
 		logger.log(Level.INFO,"addEvents called");
 		PersistenceManager pm = null;
 		try {
 			pm = pmFactory.getPersistenceManager();
 			for (EventJDO e : events)	{
 				pm.makePersistent(e);
 			}
 		} catch (Exception ex)	{
 			logger.log(Level.SEVERE, "addEvents exception: "+ex.getMessage());
 			return false;
 		} finally {
 			pm.close();
 		}
 		
 		return true;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public ArrayList<Event> searchEvents(Map<String, String> queryParameters)	{
 		logger.log(Level.INFO,"searchEvents called");
 		PersistenceManager pm = null;
 		ArrayList<Event> foundEvents = new ArrayList<Event>();
 		try {
 			pm = pmFactory.getPersistenceManager();
 			Query query = pm.newQuery(EventJDO.class);
 			
 			int counter = 0;
 			StringBuilder stringQuery = new StringBuilder();
 			StringBuilder declaredParameters = new StringBuilder();
 			ArrayList<Object> parameters = new ArrayList<Object>();
 			
 			Set<String> keys = queryParameters.keySet();
 			for (String key : keys)	{
 				if (key.equals("startEvent") || key.equals("stopEvent"))		{
 					addParameter(stringQuery, declaredParameters, key, "Date", counter++);
 					parameters.add(new SimpleDateFormat("dd.MM.yyyy").parse(queryParameters.get(key)));					
 				}
 			}
 			if (stringQuery.length() != 0)	{
 				query.setFilter(stringQuery.toString());
 				query.declareParameters(declaredParameters.toString());
 			}
 			query.declareImports("import com.google.appengine.api.datastore.Text; import java.util.Date;");	
 			List<EventJDO> result = (List<EventJDO>) query.executeWithArray(parameters.toArray());
 	
 			boolean checkEventOrganiser = queryParameters.containsKey("eventOrganiser");
 			boolean checkCategory = queryParameters.containsKey("category");
 			boolean checkDescription = queryParameters.containsKey("description");
 			
 			for (EventJDO e : result)	{
 				boolean addEvent = true;
 				if (checkEventOrganiser)	{
 					if (!(e.getEventOrganiser() != null && e.getEventOrganiser().contains(queryParameters.get("eventOrganiser"))))	{
 						addEvent = false;
 					}
 				}
 				if (checkCategory)	{
 					if (!(e.getCategory() != null && e.getCategory().contains(queryParameters.get("category"))))	{
 						addEvent = false;
 					}
 				}
 				if (checkDescription)	{
 					boolean shortOk = true;
 					boolean longOk = true;
 					if (!(e.getShortDescription() != null && e.getShortDescription().getValue().contains(queryParameters.get("description"))))	{
 						shortOk = false;
 					}
 					if (!(e.getLongDescription() != null && e.getLongDescription().getValue().contains(queryParameters.get("description"))))	{
 						longOk = false;
 					}
 					if (!shortOk && !longOk)	{
 						addEvent = false;
 					}
 				}
 				
 				if (addEvent == true)	{
 					Event newEvent = new Event();
 					newEvent.setId(e.getId());
 					newEvent.setEventOrganiser(e.getEventOrganiser());
 					newEvent.setCategory(e.getCategory());
 					newEvent.setShortDescription(e.getShortDescription().getValue());
 					newEvent.setLongDescription(e.getLongDescription().getValue());
 					newEvent.setStartEvent(e.getStartEvent());
 					newEvent.setStopEvent(e.getStopEvent());
 					newEvent.setLatitude(e.getLatitude());
 					newEvent.setLongitude(e.getLongitude());
 					if (e.getComments() != null)	{
 						ArrayList<CommentInfo> comments = new ArrayList<CommentInfo>();
 						for (CommentInfoJDO c : e.getComments())	{
 							CommentInfo ci = new CommentInfo();
 							ci.setUserNick(c.getUserNick());
 							ci.setTimeStamp(c.getTimeStamp());
 							ci.setComment(c.getComment().getValue());
 							comments.add(ci);
 						}
 						newEvent.setComments(comments);
 					}
 					foundEvents.add(newEvent);
 				}
 			}
 
 		} catch (Exception ex)	{
 			logger.log(Level.SEVERE, "searchEvents exception: "+ex.getMessage());
 			return null;
 		} finally {
 			pm.close();
 		}
 			
 		return foundEvents;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public boolean isCvutEventRecorded(EventJDO e) 	{
 		logger.log(Level.INFO,"isCvutEventRecorded called");
 		PersistenceManager pm = null;
 		try {
 			pm = pmFactory.getPersistenceManager();
 			Query query = pm.newQuery(EventJDO.class);
 			query.setFilter("eventOrganiser == p1 && category == p2 && startEvent == p3");
 			query.declareParameters("String p1, String p2, Date p3");
 			query.declareImports("import com.google.appengine.api.datastore.Text; import java.util.Date;");
 			Object[] parameters = { e.getEventOrganiser(), e.getCategory(), e.getStartEvent() };
 			List<EventJDO> result = (List<EventJDO>) query.executeWithArray(parameters);
 			for (EventJDO fe : result)	{
 				if (fe.getShortDescription().getValue().contains(e.getShortDescription().getValue()))	{
 					return true;
 				}
 			}
 			return false;
 		} catch (Exception ex)	{
 			logger.log(Level.SEVERE, "isCvutEventRecorded exception: "+ex.getMessage());
 			return true;
 		} finally {
 			pm.close();
 		}
 	}
 	
 	public boolean updateEvent(EventJDO e)	{
 		// TODO implement
 		return false;
 	}
 	
 	public boolean deleteEvent(EventJDO e)	{
 		// TODO implement
 		return false;
 	}
 	
 	private void addParameter(StringBuilder stringQuery, StringBuilder declaredParameters, String param, String type, int counter)	{
 		stringQuery.append(stringQuery.length() == 0 ? "" : " && ");
 		stringQuery.append("startEvent"+" "+(param.equals("startEvent") ? ">=" : "<=")+" p"+counter);			
 		declaredParameters.append(declaredParameters.length() == 0 ? "" : ", ");
 		declaredParameters.append(type+" p"+counter);
 	}
 	
 }
