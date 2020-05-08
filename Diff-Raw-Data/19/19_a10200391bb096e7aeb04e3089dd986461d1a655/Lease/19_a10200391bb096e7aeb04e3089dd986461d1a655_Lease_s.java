 package uk.ac.nott.mrl.homework.server.model;
 
 import java.util.Date;
 
 public class Lease
 {
 	public enum Action
 	{
 		add, del, old, upd
 	}
 
 	// private static final Logger logger = Logger.getLogger(Lease.class.getName());
 
 	public static void parseResultSet(final String results, final Model model)
 	{
 		final String[] lines = results.split("\n");
 		if (!lines[0].endsWith("<|>0<|>0<|>"))
 		{
			System.out.println("Leases	: " + lines[0]);
 		}
 		for (int index = 2; index < lines.length; index++)
 		{
 			try
 			{
 				final String[] columns = lines[index].split("<\\|>");
 				final Lease lease = new Lease();
 				final String time = columns[0].substring(1, columns[0].length() - 1);
 				final long timeLong = Long.parseLong(time, 16);
 				lease.timestamp = new Date(timeLong / 1000000).getTime();
 				lease.action = Action.valueOf(columns[1].toLowerCase());
 				lease.macAddress = columns[2].toLowerCase();
 				lease.ipAddress = columns[3];
 				lease.hostName = columns[4];
				if (lease.hostName.equals("NULL"))
 				{
 					lease.hostName = null;
 				}
 
 				model.add(lease);
 			}
 			catch (final Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private Action action;
 	private String hostName;
 	private String ipAddress;
 	private String macAddress;
 	private long timestamp;
 	private transient Action nameAction;
 
 	public void clearIPAddress()
 	{
 		ipAddress = null;
 	}
 
 	public Action getAction()
 	{
 		return action;
 	}
 
 	public String getHostName()
 	{
 		return hostName;
 	}
 
 	public String getIpAddress()
 	{
 		return ipAddress;
 	}
 
 	public String getMacAddress()
 	{
 		return macAddress;
 	}
 
 	public long getTimestamp()
 	{
 		return timestamp;
 	}
 
 	public void update(final Lease lease)
 	{
 		if (lease.getAction() == Action.del)
 		{
 			clearIPAddress();
 		}
 		else
 		{
 			if (lease.getIpAddress() != null)
 			{
 				ipAddress = lease.getIpAddress();
 			}
 
 			if (nameAction != Action.upd || lease.getAction() == Action.upd)
 			{
 				hostName = lease.getHostName();
 				nameAction = lease.getAction();
 			}
 		}
 
 		timestamp = lease.getTimestamp();
 	}
 }
