 
 package com.zanoccio.axirassa.services.sentinel;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hyperic.sigar.Sigar;
 import org.hyperic.sigar.SigarException;
 
 import com.zanoccio.axirassa.services.Service;
 import com.zanoccio.axirassa.util.HibernateTools;
 import com.zanoccio.axirassa.util.SigarLoader;
 
 public class SentinelService implements Service {
 
 	private final Session session;
 	private final int machineid;
 
 	private Sigar sigar;
 
 	private final ArrayList<Class<? extends SentinelAgent>> agentclasses = new ArrayList<Class<? extends SentinelAgent>>();
 	private final ArrayList<SentinelAgent> agents = new ArrayList<SentinelAgent>();
 
 	private ArrayList<NetworkStatistic> networkstat;
 
 
 	public static void main(String[] param) throws Exception {
 		Session session = HibernateTools.getSession();
 		SentinelService service = new SentinelService(session, 1);
 
 		service.addAgent(CPUSentinelAgent.class);
 		service.addAgent(MemorySentinelAgent.class);
 		service.addAgent(DiskUsageSentinelAgent.class);
 		service.addAgent(NetworkSentinelAgent.class);
 
 		while (true) {
 			service.execute();
			Thread.sleep(60 * 1000);
 		}
 	}
 
 
 	public SentinelService(Session session, int machineid) {
 		this.session = session;
 		this.machineid = machineid;
 	}
 
 
 	public void addAgent(Class<? extends SentinelAgent> agentclass) {
 		agentclasses.add(agentclass);
 	}
 
 
	@Override
	public void execute() throws Exception {
 		setupSigar();

 		setupAgents();
 
 		retrieveStatistics();
 		insertData();
 	}
 
 
 	private void setupSigar() {
 		SigarLoader.require();
 		if (sigar == null)
 			sigar = new Sigar();
 	}
 
 
 	private void setupAgents() {
 		try {
 			setupAgentsWithExceptions();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	private void setupAgentsWithExceptions() throws InstantiationException, IllegalAccessException {
 		for (Class<? extends SentinelAgent> agentclass : agentclasses) {
 			SentinelAgent agent = agentclass.newInstance();
 			agent.setSigar(sigar);
 			agents.add(agent);
 		}
 	}
 
 
 	private void retrieveStatistics() throws SigarException {
 		SigarLoader.require();
 		if (sigar == null)
 			sigar = new Sigar();
 
 		Date date = new Date();
 		System.out.println("Retrieving: " + date);
 
 		for (SentinelAgent agent : agents) {
 			agent.setMachineID(machineid);
 			agent.setDate(date);
 			agent.execute();
 		}
 	}
 
 
 	private void insertData() {
 		Transaction transaction = session.beginTransaction();
 
 		for (SentinelAgent agent : agents)
 			agent.save(session);
 
 		transaction.commit();
 	}
 }
