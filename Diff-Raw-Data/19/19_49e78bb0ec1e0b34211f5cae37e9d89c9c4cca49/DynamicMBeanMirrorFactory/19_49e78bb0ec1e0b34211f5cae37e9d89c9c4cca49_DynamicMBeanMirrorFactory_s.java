 package mbean;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import javax.management.Attribute;
 import javax.management.AttributeList;
 import javax.management.AttributeNotFoundException;
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.InstanceNotFoundException;
 import javax.management.IntrospectionException;
 import javax.management.InvalidAttributeValueException;
 import javax.management.ListenerNotFoundException;
 import javax.management.MBeanException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MBeanServerConnection;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.Notification;
 import javax.management.NotificationListener;
 import javax.management.ObjectInstance;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 import javax.management.monitor.Monitor;
 import javax.management.remote.JMXConnectionNotification;
 import javax.management.remote.JMXConnector;
 
 import org.bson.types.ObjectId;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 import model.MongoDBConnection;
 import model.MyCounterMonitor;
 import model.MyDynamicMBeanMirror;
 import model.MyGaugeMonitor;
 import model.MyMonitor;
 //import model.MyThresholdMonitor;
 //import model.MyStringMonitor;
 import mbean.RemoteMessageListener;
 
 public class DynamicMBeanMirrorFactory implements NotificationListener{
 	
 	public static MBeanServer masterMbeanServer = null;
 	private static RemoteMessageListener attlist = new RemoteMessageListener();
     public static RemoteMonitorListener monlist = new RemoteMonitorListener();
     private static List<MyMonitor> mymonitors = new ArrayList<MyMonitor>();
 
     public static MyDynamicMBeanMirror newMBeanMirror( MBeanServerConnection mbsc, ObjectName objectName) throws IOException, InstanceNotFoundException, IntrospectionException {
     	MyDynamicMBeanMirror mirror = new MyDynamicMBeanMirror(mbsc, objectName);
         return mirror;
     }
     
     public static void setMBeanMasterServer(MBeanServer mbServer) {
     	masterMbeanServer=mbServer;
     }
     
 	public static String register(String dirip, String port, String domain, String type){
 		String retorno="failure";
 		MBSAConnection connection=MBSAConnections.searchConnection(dirip, port);
 		if(connection==null){
 			connection = new MBSAConnection(dirip, port, domain, type);
 			connection.connect();
 			if(connection.getConn()!=null){
 				connection.getConn().addConnectionNotificationListener(new DynamicMBeanMirrorFactory(), null, null);
 				MBSAConnections.add(connection);
 				importAll(connection);
 				retorno="success";
 			}
 		}else{
 			System.out.println("Ya existe una conexión en la dirección "+dirip+":"+port);
 		}
 		return retorno;
 	}
 	
 	private static void importAll(MBSAConnection connection){
 		
 		if(connection.getConn()!=null){
 			Set<DBObject> mcratrs = new HashSet<DBObject>();
 			DBObject obj = null,obj1;
 			MongoDBConnection mdbc = null;
 
 			try {
 				mdbc = MongoDBConnection.getInstance();
 			} catch (UnknownHostException e1) {
 				e1.printStackTrace();
 			}
 			DB db = mdbc.getDb();
 			DBCollection coll;
 			BasicDBObject query1,query2;
 			DBCursor cursor1,cursor2;
 
 			coll = db.getCollection("man_rscs");
 			query1 = new BasicDBObject("name", connection.getType()).append("domain", connection.getDomain());
 			cursor1 = coll.find(query1);
 
 			try {
 			   while(cursor1.hasNext()) {
 				   obj=cursor1.next();
 				   coll = db.getCollection("mcr_atrs");
 				   query2 = new BasicDBObject("man_rsc_id", obj.get("_id"));
 				   cursor2 = coll.find(query2);
 				   try {
 					   while(cursor2.hasNext()) {
 						   obj1=cursor2.next();
 						   mcratrs.add(obj1);
 					   }
 					} finally {
 					   cursor2.close();
 					}
 			   }
 			} finally {
 			   cursor1.close();
 			}
 			Set<ObjectName> mbeanNames = new HashSet<ObjectName>();
 			ObjectName mirrorName = null;
 			ObjectName name = null;
 	        for (DBObject objma : mcratrs) {
 	            try {
 	            	name = new ObjectName(connection.getDomain()+":type="+connection.getType()+",name="+objma.get("name"));
 	            	mbeanNames.add(name);
 	                mirrorName = name;
 	                MyDynamicMBeanMirror mirror = DynamicMBeanMirrorFactory.newMBeanMirror(connection.getAgentMbeanServer(), name);
 	                masterMbeanServer.registerMBean(mirror, mirrorName);
 	                mirror.addNotificationListener(attlist, null, "{mrid:"+obj.get("_id")+", maid:"+objma.get("_id")+"}");
 	            	System.out.println("MBean "+mirrorName+" registrado.");
 	            	mdbc.setColl("man_rscs");
 	    			BasicDBObject doca = new BasicDBObject().append("_id",obj.get("_id"));
 	    			BasicDBObject docb = new BasicDBObject();
 	    			docb.append("$set", new BasicDBObject().append("on", true));
 	    			mdbc.update_doc(doca, docb);
	    			if(obj.get("alrtbl").equals("true"))
 	    				loadMonitors(connection, objma);
 	            } catch (IllegalArgumentException e) {
 	            	System.out.println("El MBeanServerAgent \""+mirrorName+"\" no presenta interfaz de notificaciones");
 	            } catch (InstanceAlreadyExistsException e) {
 					System.out.println("El MBean "+mirrorName+" ya se encuentra registrado.");
 				} catch (MBeanRegistrationException e) {
 					e.printStackTrace();
 				} catch (NotCompliantMBeanException e) {
 					e.printStackTrace();
 				} catch (InstanceNotFoundException e) {
 					e.printStackTrace();
 				} catch (IntrospectionException e) {
 					e.printStackTrace();
 				} catch (MalformedObjectNameException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 	        }
 	        connection.setMbeanNames(mbeanNames);
 		}
     }
 
 	public static void removeAll(String ip, String port){
 		MBSAConnection connection=MBSAConnections.searchConnection(ip, port);
 		System.out.println(connection.getDomain());
 		System.out.println(connection.getType());
 		Set<ObjectName> names = connection.getMbeanNames();
         for (ObjectName name : names) {
             try {
         		unloadMonitors(connection);
         		masterMbeanServer.unregisterMBean(name);
         		setMROff(connection.getDomain(), connection.getType());
             } catch (IllegalArgumentException e) {
             	System.out.println("El MBean \""+name+"\" no presenta interfaz de notificaciones");
             } catch (MBeanRegistrationException e) {
 				e.printStackTrace();
 			} catch (InstanceNotFoundException e) {
 				e.printStackTrace();
 			}
         }
 		MBSAConnections.removeConnection(connection);
 		connection.setConn(null);
 	}
 	
 	public static void removeAll(MBSAConnection connection){
 		Set<ObjectName> names = connection.getMbeanNames();
         for (ObjectName name : names) {
             try {
             	if(!name.toString().equals("JMImplementation:type=MBeanServerDelegate"))
             		unloadMonitors(connection);
             		masterMbeanServer.unregisterMBean(name);
             		setMROff(connection.getDomain(), connection.getType());
             } catch (IllegalArgumentException e) {
             	System.out.println("El MBean \""+name+"\" no presenta interfaz de notificaciones");
             } catch (MBeanRegistrationException e) {
 				e.printStackTrace();
 			} catch (InstanceNotFoundException e) {
 				e.printStackTrace();
 			}
         }
 		MBSAConnections.removeConnection(connection);
 		connection.setConn(null);
 	}
 	
 	private static void setMROff(String domain, String type){
 		DBObject obj = null;
 		MongoDBConnection mdbc = null;
 
 		try {
 			mdbc = MongoDBConnection.getInstance();
 		} catch (UnknownHostException e1) {
 			e1.printStackTrace();
 		}
 		DB db = mdbc.getDb();
 		DBCollection coll;
 		BasicDBObject query1;
 		DBCursor cursor1;
 
 		coll = db.getCollection("man_rscs");
 		query1 = new BasicDBObject("name", type).append("domain", domain);
 		cursor1 = coll.find(query1);
 
 		try {
 		   while(cursor1.hasNext()) {
 			   obj=cursor1.next();
 		   }
 		} finally {
 		   cursor1.close();
 		}
 		mdbc.setColl("man_rscs");
 		BasicDBObject doca = new BasicDBObject().append("_id",obj.get("_id"));
 		BasicDBObject docb = new BasicDBObject();
 		docb.append("$set", new BasicDBObject().append("on", false));
 		mdbc.update_doc(doca, docb);
 	}
 
 	@Override
 	public void handleNotification(Notification notification, Object arg1) {
 		JMXConnectionNotification notif = (JMXConnectionNotification)notification;
 		JMXConnector conn = (JMXConnector)notif.getSource();
 		if(notif.getType().equals("jmx.remote.connection.closed")){
 			try {
 				conn.removeConnectionNotificationListener(this);
 			} catch (ListenerNotFoundException e) {
 				e.printStackTrace();
 			}
 			System.out.println("La conexión RMI se cayó");
 			MBSAConnection connection=MBSAConnections.searchConnection(conn);
 			System.out.println("eliminando conexion "+connection.getDomain()+"/"+connection.getType());
 			removeAll(connection);
 		}
 	}
 
 	public static String setAttribute(String domain, String name, String type, String attribute, String value){
 		String retorno="OK";
 		Attribute attr = new Attribute(attribute, value);
 		if(masterMbeanServer!=null){
 			try {
 				masterMbeanServer.setAttribute(new ObjectName(domain+":type="+type+",name="+name), attr);
 			} catch (InstanceNotFoundException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			} catch (InvalidAttributeValueException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			} catch (AttributeNotFoundException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			} catch (MalformedObjectNameException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			} catch (ReflectionException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			} catch (MBeanException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			}
 		}
 		return retorno;
 	}
 
 	public static String setAttributes(String domain, String name, String type, HashMap<String, String> attributes){
 		
 		String retorno="OK";
 		if(masterMbeanServer!=null){
 			AttributeList listattr = new AttributeList();
 			for (Entry<String, String> attribute : attributes.entrySet()) {
 				listattr.add(new Attribute(attribute.getKey(), attribute.getValue()));			
 			}
 			try {
 				masterMbeanServer.setAttributes(new ObjectName(domain+":type="+type+",name="+name), listattr);
 			} catch (InstanceNotFoundException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			} catch (MalformedObjectNameException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			} catch (ReflectionException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			}
 		}
 		return retorno;
 	}
 	
 	public static String getMRInfo(String domain, String type){
 		String retorno="Error";
 		return retorno;
 	}
 	
 	public static String getMAInfo(String domain, String type, String name){
 		String retorno="Error";
 		
 		return retorno;
 	}
 	
 	public static String setMonitor(String domain, String type, String name, String attribute, String monitor, String value){
 		String retorno="Error";
 		Set<?> dynamicData = null;
 		String mon="";
 
 		if(monitor.equals("qos"))
 			mon="QoSMonitors";
 		else
 			mon="AlrMonitors";
 		
 		DBObject obj = null,obj1;		
 		try {
 			MongoDBConnection mdbc = MongoDBConnection.getInstance();
 			DB db = mdbc.getDb();
 			DBCollection coll;
 			BasicDBObject query1;
 			DBCursor cursor1;
 			
 			coll = db.getCollection("atrs");
 			query1 = new BasicDBObject("_id", new ObjectId(attribute));
 			cursor1 = coll.find(query1);
 
 			try {
 			   while(cursor1.hasNext()) {
 				   obj=cursor1.next();
 			   }
 			} finally {
 			   cursor1.close();
 			}
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			dynamicData = masterMbeanServer.queryMBeans(new ObjectName(mon+":type="+domain+",resource="+type+",macroatr="+name+",attribute="+obj.get("name")), null);
 		} catch (MalformedObjectNameException e1) {
 			e1.printStackTrace();
 		} catch (NullPointerException e1) {
 			e1.printStackTrace();
 		}
 		
 		System.out.println(value);
 		System.out.println(monitor);
 		System.out.println(dynamicData.size());
 		
 		if(value.equals("on") && dynamicData.size()==0){
 			retorno="OK";
 			try {
 				MyMonitor mm;
 				if(monitor.equals("qos"))
 					obj1=(DBObject) obj.get("qos_mon");
 				else
 					obj1=(DBObject) obj.get("alr_mon");
 				
 				if(obj1.get("_type").equals("AlrMntrCntr"))
 					mm = createCounterMonitor((String)obj.get("name"), ((Double)obj1.get("value")).intValue());
 				else
 					mm = createGaugeMonitor((String)obj.get("name"), (Double)obj1.get("value_up"), (Double)obj1.get("value_down"));
 
 				createMonitor(monitor, mm, domain, type, name, new ObjectId(attribute));
 			} catch (NullPointerException e) {
 				e.printStackTrace();
 			}
 		}else if(value.equals("off") && dynamicData.size()==1){
 			for (Iterator<?> it = dynamicData.iterator(); it.hasNext();) {
 				ObjectInstance oi = (ObjectInstance) it.next();
 				ObjectName oName = oi.getObjectName();
 				MyMonitor m = null;
 				for (int i=0;i<mymonitors.size(); i++) {
 					if(((MyMonitor)mymonitors.get(i)).getName().equals(""+oName)){
 						m=(MyMonitor)mymonitors.get(i);
 						break;
 					}
 				}
 				m.getMonitor().stop();
 				mymonitors.remove(m);
 				try {
 					masterMbeanServer.unregisterMBean(oName);
 				} catch (MBeanRegistrationException e) {
 					e.printStackTrace();
 				} catch (InstanceNotFoundException e) {
 					e.printStackTrace();
 				}
 				System.out.println("off monitor "+oName.toString());
 			}
 		}
 		return retorno;
 	}
 	
 	public static String getAttribute(String domain, String name, String type, String attribute){
 		String retorno="Error";
 		if(masterMbeanServer!=null){
 			try {
 				retorno = (String) masterMbeanServer.getAttribute(new ObjectName(domain+":type="+type+",name="+name), attribute);
 			} catch (InstanceNotFoundException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			} catch (AttributeNotFoundException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			} catch (MalformedObjectNameException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			} catch (ReflectionException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			} catch (MBeanException e) {
 				e.printStackTrace();
 				retorno=e.toString();
 			}
 		}
 		return retorno;
 	}
 	
 	public static void loadMonitors(MBSAConnection connection, DBObject objma){
 		/*MyThresholdMonitor cm1=new MyThresholdMonitor();
 		cm1.setAttribute("perfil");
 		cm1.setOffset((double) 0);
 		cm1.setThreshold((double) 5);
 		cm1.setPeriod(1000L);*/
 		//ms.thresholdMonitors.add(cm1);		
 
 		try {
 			MongoDBConnection mdbc = MongoDBConnection.getInstance();
 			DB db = mdbc.getDb();
 			DBCollection coll;
 			BasicDBObject query1;
 			DBCursor cursor1;
 			DBObject obj1,obj2;
 			
 		   coll = db.getCollection("atrs");
 		   query1 = new BasicDBObject("mcr_atr_id", objma.get("_id"));
 		   cursor1 = coll.find(query1);
 		   try {
 			   while(cursor1.hasNext()) {
 				   obj1=cursor1.next();
 				   try {
 						obj2=(DBObject) obj1.get("qos_mon");
 						if(obj2 != null && obj2.get("state").equals("act")){
 							MyMonitor mm;
 							if(obj2.get("_type").equals("AlrMntrCntr"))
 								mm = createCounterMonitor((String)obj1.get("name"), ((Double)obj2.get("value")).intValue());
 							else
 								mm = createGaugeMonitor((String)obj1.get("name"), (Double)obj2.get("value_up"), (Double)obj2.get("value_down"));
 							createMonitor("qos", mm, connection.getDomain(), connection.getType(), (String)objma.get("name"), (ObjectId)obj1.get("_id"));
 						}
 						obj2=(DBObject) obj1.get("alr_mon");
 						if(obj2 != null && obj2.get("state").equals("act")){
 							MyMonitor mm;
 							if(obj2.get("_type").equals("AlrMntrCntr"))
 								mm = createCounterMonitor((String)obj1.get("name"), ((Double)obj2.get("value")).intValue());
 							else
 								mm = createGaugeMonitor((String)obj1.get("name"), (Double)obj2.get("value_up"), (Double)obj2.get("value_down"));
 							createMonitor("alr", mm, connection.getDomain(), connection.getType(), (String)objma.get("name"), (ObjectId)obj1.get("_id"));
 						}
 					} catch (NullPointerException e) {
 						e.printStackTrace();
 					}
 			   }
 		   	} finally {
 			   cursor1.close();
 			}
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}		   
 	}
 	
 	private static void createMonitor(String tipo, MyMonitor mm, String domain, String type, String mcrName, ObjectId atrid){
 		String mon="";
 		if(tipo.equals("qos"))
 			mon="QoSMonitors";
 		else
 			mon="AlrMonitors";
 		
 		Monitor m = mm.getMonitor();
 		String moname = mon+":type="+domain+",resource="+type+",macroatr="+mcrName+",attribute="+m.getObservedAttribute();
 		mm.setName(moname);
 		try {
 			m.addObservedObject(new ObjectName(domain+":type="+type+",name="+mcrName));
 			masterMbeanServer.registerMBean(m, new ObjectName(moname));
 		} catch (InstanceAlreadyExistsException e) {
 			e.printStackTrace();
 		} catch (MBeanRegistrationException e) {
 			e.printStackTrace();
 		} catch (NotCompliantMBeanException e) {
 			e.printStackTrace();
 		} catch (MalformedObjectNameException e) {
 			e.printStackTrace();
 		}
         try {
         	m.addNotificationListener(monlist, null, "{montype:"+tipo+", atrid:"+atrid+"}");
         } catch (Exception e) {
             e.printStackTrace();
         }
 		mymonitors.add(mm);
 		m.start();
 		System.out.println("on monitor"+moname);
 	}
 	
 	private static MyMonitor createCounterMonitor(String atr, Integer threshold){
 		MyCounterMonitor cm1=new MyCounterMonitor();
 		cm1.setAttribute(atr);
 		cm1.setOffset(0);
 		cm1.setThreshold(threshold);
 		cm1.setPeriod(1000L);
 		return cm1;
 	}
 	
 	private static MyMonitor createGaugeMonitor(String atr, Double umbralH, Double umbralL){
 		MyGaugeMonitor gm1= new MyGaugeMonitor();
 		gm1.setAttribute(atr);
 		gm1.setNotifyHigh(true);
 		gm1.setNotifyLow(true);
 		gm1.setPeriod(1000L);
 		gm1.setThresholdHigh(umbralH);
 		gm1.setThresholdLow(umbralL);
 		gm1.setDifference(false);
 		return gm1;
 	}
 	
 	public static void unloadMonitors(MBSAConnection connection){
 		Set<?> dynamicData;
 		try {
 			dynamicData = masterMbeanServer.queryMBeans(new ObjectName("QoSMonitors:type="+connection.getDomain()+",resource="+connection.getType()+",*"), null);
 			for (Iterator<?> it = dynamicData.iterator(); it.hasNext();) {
 				ObjectInstance oi = (ObjectInstance) it.next();
 				ObjectName oName = oi.getObjectName();
 				MyMonitor m = null;
 				for (int i=0;i<mymonitors.size(); i++) {
 					if(((MyMonitor)mymonitors.get(i)).getName().equals(""+oName)){
 						m=(MyMonitor)mymonitors.get(i);
 						break;
 					}
 				}
 				m.getMonitor().stop();
 				System.out.println("removido monitor "+m.getName());
 				mymonitors.remove(m);
 				try {
 					masterMbeanServer.unregisterMBean(oName);
 				} catch (MBeanRegistrationException e) {
 					e.printStackTrace();
 				} catch (InstanceNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 			dynamicData = masterMbeanServer.queryMBeans(new ObjectName("AlrMonitors:type="+connection.getDomain()+",resource="+connection.getType()+",*"), null);
 			for (Iterator<?> it = dynamicData.iterator(); it.hasNext();) {
 				ObjectInstance oi = (ObjectInstance) it.next();
 				ObjectName oName = oi.getObjectName();
 				MyMonitor m = null;
 				for (int i=0;i<mymonitors.size(); i++) {
 					if(((MyMonitor)mymonitors.get(i)).getName().equals(""+oName)){
 						m=(MyMonitor)mymonitors.get(i);
 						break;
 					}
 				}
 				m.getMonitor().stop();
 				System.out.println("removido monitor "+m.getName());
 				mymonitors.remove(m);
 				try {
 					masterMbeanServer.unregisterMBean(oName);
 				} catch (MBeanRegistrationException e) {
 					e.printStackTrace();
 				} catch (InstanceNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 
 		} catch (MalformedObjectNameException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static String setAlertable(String domain, String type, String value) {
 		String retorno="";
 		MBSAConnection connection=MBSAConnections.searchConnection2(domain, type);
 		if(value.equals("act")){
 			if(connection.getConn()!=null){
 				Set<DBObject> mcratrs = new HashSet<DBObject>();
 				DBObject obj = null,obj1;
 				MongoDBConnection mdbc = null;
 	
 				try {
 					mdbc = MongoDBConnection.getInstance();
 				} catch (UnknownHostException e1) {
 					e1.printStackTrace();
 				}
 				DB db = mdbc.getDb();
 				DBCollection coll;
 				BasicDBObject query1,query2;
 				DBCursor cursor1,cursor2;
 	
 				coll = db.getCollection("man_rscs");
 				query1 = new BasicDBObject("name", connection.getType()).append("domain", connection.getDomain());
 				cursor1 = coll.find(query1);
 	
 				try {
 				   while(cursor1.hasNext()) {
 					   obj=cursor1.next();
 					   coll = db.getCollection("mcr_atrs");
 					   query2 = new BasicDBObject("man_rsc_id", obj.get("_id"));
 					   cursor2 = coll.find(query2);
 					   try {
 						   while(cursor2.hasNext()) {
 							   obj1=cursor2.next();
 							   mcratrs.add(obj1);
 						   }
 						} finally {
 						   cursor2.close();
 						}
 				   }
 				} finally {
 				   cursor1.close();
 				}
 				
 		        for (DBObject objma : mcratrs) {
 		            try {
		    			if(obj.get("alrtbl").equals("true"))
 		    				loadMonitors(connection, objma);
 		            } catch (IllegalArgumentException e) {}
 		        }
 			}
 			retorno="act";
 		}else{
 			unloadMonitors(connection);
 			retorno="inact";
 		}
 		return retorno;
 	}
 }
 
 
