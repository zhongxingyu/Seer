 package uk.co.harcourtprogramming.netcat.docitten;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import uk.co.harcourtprogramming.netcat.ExternalService;
 
 public class MOTDService extends ExternalService
 {
 	private final File f;
 	private final String channel;
 	private int lastId = 0;
 	private long lastModified = 0;
 
 	public class Message
 	{
 		private Integer id = null;
 		private Boolean active = null;
 		private String title = null;
 		private String mshort = null;
 		private String mlong = null;
 		private Long from = null;
 		private Long to = null;
 		private String poster_uid = null;
 		private String poster_name = null;
 
 		private Message()
 		{
 			// Nothing to see here. Move along, citizen!
 		}
 
 		public String toString()
 		{
 			StringBuilder b = new StringBuilder();
 
 			b.append('#');
 			b.append(id);
 			b.append(" *");
 			b.append(poster_name);
 			b.append("* (");
 			b.append(poster_uid);
 			b.append("@doc): ");
 			b.append(title);
 			b.append('\n');
			b.append(mlong.replace("<BR>","\n"));
 
 			return b.toString();
 		}
 	}
 
 	public MOTDService(File f, String channel)
 	{
 		super();
 
 		if (!f.canRead())
 		{
 			throw new RuntimeException(new FileNotFoundException());
 		}
 
 		this.channel = channel;
 		this.f = f;
 		processFile();
 
 		getThread().setDaemon(true);
 	}
 
 	public void run()
 	{
 		while (true)
 		{
 			if (f.lastModified() > lastModified)
 			{
 				processFile();
 			}
 			try
 			{
 				getThread().sleep(60000);
 			}
 			catch (InterruptedException ex)
 			{
 			}
 		}
 	}
 
 	private void processFile()
 	{
 		log(Level.INFO, "Processing MOTD file " + f.getPath());
 		BufferedReader in = null;
 		Message curr = null;
 		final LinkedList<Message> stack = new LinkedList<Message>();
 
 		try
 		{
 			in = new BufferedReader(new FileReader(f));
 
 			while (in.ready())
 			{
 				String line = in.readLine().trim();
 
 				if (line.charAt(0) == '#') continue;
 				if (line.equals("[Message]"))
 				{
					if (curr != null) stack.push(curr);
 					curr = new Message();
 					continue;
 				}
 
 				if (curr == null) continue;
 				int div = line.indexOf('=');
 				if (div == -1) continue;
 
 				String field = line.substring(0,div).toLowerCase();
 				String value = line.substring(div+1);
 
 				if (field.equals("id"))
 				{
 					curr.id = Integer.parseInt(value);
 					continue;
 				}
 				if (field.equals("active"))
 				{
 					curr.active = Boolean.valueOf(value);
 					continue;
 				}
 				if (field.equals("title"))
 				{
 					curr.title = value;
 					continue;
 				}
 				if (field.equals("short"))
 				{
 					curr.mshort = value;
 					continue;
 				}
 				if (field.equals("long"))
 				{
 					curr.mlong = value;
 					continue;
 				}
 				if (field.equals("from"))
 				{
 //					curr.from = new Date(value).getTime();
 					continue;
 				}
 				if (field.equals("to"))
 				{
 //					curr.to = new Date(value).getTime();
 					continue;
 				}
 				if (field.equals("posterid"))
 				{
 					curr.poster_uid = value;
 					continue;
 				}
 				if (field.equals("postername"))
 				{
 					curr.poster_name = value;
 					continue;
 				}
 				if (field.equals("showunix")) continue;
 
 				log(Level.WARNING, "Unknown Field '" + field + "'");
 			}
 		}
 		catch (IOException ex)
 		{
 			log(Level.WARNING, ex);
 		}
 		catch (Throwable ex)
 		{
 			log(Level.SEVERE, ex.getMessage());
 		}
 		finally
 		{
 			try
 			{
 				in.close();
 			}
 			catch (IOException ex)
 			{
 			}
 		}
 
		if (curr != null) stack.push(curr);
 		lastModified = f.lastModified();
 
 		for (Message m : stack)
 		{
 			if (m.id > lastId)
 			{
				log(Level.INFO, "Sending MOTD Notice #" + m.id);
 				message(channel, m.toString());
 				lastId = m.id;
 			}
 		}
 	}
 
 	public void shutdown()
 	{
 		// Nothing to see here. Move along, citizen!
 	}
 }
 
