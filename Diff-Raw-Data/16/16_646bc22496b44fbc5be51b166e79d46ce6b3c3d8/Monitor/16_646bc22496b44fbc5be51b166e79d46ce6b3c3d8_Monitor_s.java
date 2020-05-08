 package net.sf.ictalive.monitoring;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import net.sf.ictalive.eventbus.exception.EventBusConnectionException;
 import net.sf.ictalive.monitoring.domain.Proposition;
 import net.sf.ictalive.monitoring.domain.Session;
 import net.sf.ictalive.monitoring.rules.Fact;
 import net.sf.ictalive.monitoring.rules.Rule;
 import net.sf.ictalive.monitoring.rules.drools.DroolsEngine;
 import net.sf.ictalive.monitoring.rules.drools.Opera2Drools;
 import net.sf.ictalive.monitoring.rules.drools.ParsedNorms;
 import net.sf.ictalive.operetta.OM.OperAModel;
 import net.sf.ictalive.runtime.event.Event;
 import net.sf.ictalive.runtime.event.EventFactory;
 import net.sf.ictalive.runtime.fact.FactFactory;
 
 public class Monitor extends Thread implements IMonitor
 {
 	private DroolsEngine				re;
 	private Map<Session,Endpoint>		endpoints;
 	private Map<Session,Set<Fact>>		watched;
 	private Map<Fact,Set<Session>>		subscriptions;
 	private Map<Event,IMonitorListener>	listeners;
 	private EventTransporter			eb;
 	private String						host;
 	private OperAModel					om;
 	
 	public Monitor(String host) throws EventBusConnectionException, IOException
 	{
 		this.host = host;
 		initialise();
 	}
 	
 	public Monitor(String host, OperAModel om) throws EventBusConnectionException, IOException
 	{
 		this.host = host;
 		this.om = om;
 		initialise();
 	}
 	
 	private void initialise() throws EventBusConnectionException, IOException
 	{
 		Opera2Drools			o2d;
 		ParsedNorms				pn;
 		
 		re = new DroolsEngine();
 		endpoints = new TreeMap<Session,Endpoint>();
 		subscriptions = new TreeMap<Fact,Set<Session>>();
 		watched = new TreeMap<Session,Set<Fact>>();
 		
 		if(host == null)
 		{
 			eb = new BusEventTransporter();
 		}
 		else
 		{
 			eb = new BusEventTransporter(host);
 		}
 		
 		listeners = new HashMap<Event, IMonitorListener>();
 		
 		if(om == null)
 		{
 			o2d = new Opera2Drools("Warcraft3ResourceGathering.opera");
 		}
 		else
 		{
 			o2d = new Opera2Drools(om);
 		}
 		o2d.parse();
 		pn = o2d.toDrools();
 		
 		re.addPackage(pn.getRules());
 		re.handleObservation(eb);
 		re.handleObservation(pn.getNorms());
 		re.handleObservation(new Proposition("Context", "Universal"));
 		re.evaluate();
 	}
 
 	public Monitor() throws EventBusConnectionException, IOException
 	{
 		initialise();
 	}
 	
 	public synchronized void subscribe(Event ev, IMonitorListener ml)
 	{
 		listeners.put(ev, ml);
 		re.addEventTemplate(ev);
 	}
 	
 	public void cancelSubscription(Session session)
 	{
 		Set<Fact>		facts;
 		Iterator<Fact>	it;
 		Set<Session>	sessions;
 		
 		if(endpoints.get(session) == null)
 		{
 			// Error
 		}
 		else
 		{
 			facts = watched.get(session);
 			
 			it = facts.iterator();
 			while(it.hasNext())
 			{
 				sessions = subscriptions.get(it.next());
 				if(sessions != null)
 				{
 					sessions.remove(session);
 				}
 			}
 			
 			watched.remove(session);
 			endpoints.remove(session);
 		}
 	}
 
 	public void initialise(Rule[] listOfRules)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void updateRules(Rule[] listOfRules)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	public EventTransporter getEventTransporter()
 	{
 		return eb;
 	}
 
 	public RuleEngine getRuleEngine()
 	{
 		return re;
 	}
 
 	public void run()
 	{
 		Event	ev;
 
 		System.out.println("Waiting for events...");
 		while(true)
 		{
 			try
 			{
 				ev = eb.take();
 				re.handleObservation(ev);
 				re.evaluate();
 //				System.out.println("---------------------------------------");
 //				re.dump();
 //				System.out.println("---------------------------------------");
 //				System.out.println(Runtime.getRuntime().freeMemory());
 			}
 			catch (InterruptedException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void main(String args[]) throws InterruptedException, EventBusConnectionException, IOException
 	{
 		Monitor				m;
 		IMonitorListener	iml;
 		Event				ev;
 		
 		iml = new IMonitorListener() {};
 		ev = EventFactory.eINSTANCE.createEvent();
 		ev.setContent(FactFactory.eINSTANCE.createContent());
 		ev.getContent().setFact(FactFactory.eINSTANCE.createSendAct());
 		
 //		m = new Monitor("147.83.200.115");
 //		m = new Monitor("147.83.200.118");
		m = new Monitor("147.83.200.118");
 		m.subscribe(ev, iml);
 		m.start();
 		
 		m.join();
 	}
 }
