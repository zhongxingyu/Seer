 package ar.edu.itba.pdc.filters;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import ar.edu.itba.pdc.jabber.JabberElement;
 import ar.edu.itba.pdc.jabber.Message;
 import ar.edu.itba.pdc.jabber.Presence;
 import ar.edu.itba.pdc.stanzas.Stanza;
 
 public class StatisticsFilter implements Filter{
 	
 	private static final int DEFAULT_INTERVAL = 120000; // 2 minutos en milisegundos
 	private static final int TRANSFER_UNIT = 1024;
 	private static final int ACCESS_UNIT = 1;
 	private static int interval = DEFAULT_INTERVAL;
 	
 	private boolean statisticsEnabled = false;
 	private long initialStatisticsTime = -1;
 	
 	private Map<String, PersonalStatistic> usersStatistics = null;
 
 	public StatisticsFilter() {
 		if (usersStatistics == null) {
 			usersStatistics = new HashMap<String, PersonalStatistic>();
			initialStatisticsTime = System.currentTimeMillis();
 //			setInterval(AdminParser.getInterval()) // desde el archivo conf
 		}
 	}
	
 	public void getStatistics() {
 		
 		int currInterval = getCurrentInterval();
 		int globalTotalAccesses = 0, globalTotalByteTransfers = 0;
 		int[] globalAccessByInterval = new int[currInterval], byteTransferByInterval = new int[currInterval];
 		Date date = new Date(System.currentTimeMillis());
		String slash = System.getProperty("path.separator");
		String home = System.getProperty("user.home");
 		try {
			FileWriter fstream = new FileWriter(home + slash + "ProxyStatistics" + slash + date + ".txt");
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write("Estadistica del proxy - " + date + "\n\n");
 			for (PersonalStatistic ps : usersStatistics.values()) {
 				
 				out.write("Estadistica del Usuario: " + ps.jid + "\n");
 				
 				int userTotalAccesses = 0, userTotalBytesTransfered = 0;
 				int[] userAccessByInterval = new int[currInterval], userByteTransferByInterval = new int[currInterval];
 				
 				for (Entry<Integer,Integer> access : ps.accessBetweenIntervals.entrySet()) {
 					globalAccessByInterval[access.getKey()] += access.getValue();
 					userTotalAccesses += access.getValue();
 				}
 				globalTotalAccesses += userTotalAccesses;
 				
 				for (Entry<Integer,Integer> bytesTransfered : ps.bytesBetweenIntervals.entrySet()) {
 					byteTransferByInterval[bytesTransfered.getKey()] += bytesTransfered.getValue();
 					userTotalBytesTransfered += bytesTransfered.getValue();
 				}
 				globalTotalByteTransfers += userTotalBytesTransfered;
 				
 				out.write("Accesos totales del usuario:    "+ ps.jid + userTotalAccesses + "\n");
 				out.write("Bytes transferidos del usuario: "+ ps.jid + userTotalBytesTransfered + "\n");
 				out.write("Histograma de accesos del usuario: " + ps.jid + "\n");
 				printHistogram(userAccessByInterval, currInterval, ACCESS_UNIT);
 				out.write("Histograma de transferencia del usuario: " + ps.jid + "\n");
 				printHistogram(userByteTransferByInterval, currInterval, TRANSFER_UNIT);
 			}
 			out.write("Estadistica General\n");
 			out.write("Accesos totales al sistema: " + globalTotalAccesses);
 			out.write("Bytes transferidos del sistema: " + globalTotalByteTransfers);
 			out.write("Histograma de accesos totales: \n");
 			printHistogram(globalAccessByInterval, currInterval, ACCESS_UNIT);
 			out.write("Histograma de transferencias totales: \n");
 			printHistogram(byteTransferByInterval, currInterval, TRANSFER_UNIT);
 			
 			out.close();
 		} catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 	
 
 	public void setInterval(int minutes) {
 		interval = minutes*60*1000;
 	}
 	
 //	public void beginStatistics() {
 //		initialStatisticsTime = System.currentTimeMillis(); 
 //	}
 	
 	public void enableStatistics() {
 //		if (initialStatisticsTime == -1)
 //			beginStatistics();
 		statisticsEnabled = true;
 	}
 	
 	public void disableStatistics() {
 		statisticsEnabled = false;
 	}
 	
 	
 	private int getCurrentInterval() {
 		return (int)((System.currentTimeMillis()-initialStatisticsTime)/interval);
 	}
 	
 	private String printHistogram(int[] array, int interval, int unit) {
 		String out = "";
 		for (int i=0; i < interval ; i++) {
 			out += i + ": ";
 			int aux = 0;
 			while(aux < array[i]/unit)
 				out += "*";
 			out += "\n";
 		}
 		return out + "\n";
 	}
 	
 	/* inicio clase interna */
 	
 	private class PersonalStatistic {
 		
 		Map<Integer,Integer> accessBetweenIntervals = new HashMap<Integer,Integer>();
 		Map<Integer,Integer> bytesBetweenIntervals = new HashMap<Integer,Integer>();
 		String jid = null;
 
 		PersonalStatistic(String jid) {
 			this.jid = jid;
 		}
 		
 		private void applyFilter(Message m) {
 			int position = StatisticsFilter.this.getCurrentInterval();
 			if (!bytesBetweenIntervals.containsKey(position))
 				bytesBetweenIntervals.put(position, m.getMessage().length());
 			else
 				bytesBetweenIntervals.put(position, bytesBetweenIntervals.get(position) + m.getMessage().length());
 		}
 
 		/* Si el to esta en null es un mensaje broadcast y por lo tanto se esta conectando*/
 		/* Pensar que pasa cuando retransmite este mensaje (bastante posible) */
 		private void applyFilter(Presence p) {
 			if (p.getTo() == null) {
 				int position = StatisticsFilter.this.getCurrentInterval();
 				if (!bytesBetweenIntervals.containsKey(position))
 					accessBetweenIntervals.put(position, 1);
 				else
 					accessBetweenIntervals.put(position, accessBetweenIntervals.get(position) + 1);
 			}
 		}
 	}
 	
 	/* fin clase interna */
 	
 	public void apply(Stanza stanza) {
 		String from;
 		JabberElement je;
 		if (stanza != null && (je = stanza.getElement()) != null && (from = je.getFrom()) != null) {
 				if (!usersStatistics.containsKey(from)) {
 					usersStatistics.put(from, new PersonalStatistic(from));
 				}
 				if (stanza.isMessage()) {
 					usersStatistics.get(from).applyFilter((Message) je);
 				} else if (stanza.isPresence()) {
 					usersStatistics.get(from).applyFilter((Presence) je);
 				}
 		}
 	}
 }
