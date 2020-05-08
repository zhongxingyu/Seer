 package me.sinnoh.MasterPromote;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 
 public class MPConfig 
 {
 	public static MasterPromote plugin = MasterPromote.instance;
 	
 	
 	public static void createdefaults()
 	{
 		try
 		{
 			if(!plugin.configFile.exists())
 			{
 				plugin.configFile.getParentFile().mkdirs();
 				copy(plugin.getResource("config.yml"), plugin.configFile);
 			}
 			if(!plugin.messagesFile.exists())
 			{
 				plugin.messagesFile.getParentFile().mkdirs();
 				copy(plugin.getResource("messages.yml"), plugin.messagesFile);
 			}
 			if(!plugin.tokenFile.exists())
 			{
 				plugin.tokenFile.getParentFile().mkdirs();
 				copy(plugin.getResource("token.yml"), plugin.tokenFile);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static void copy(InputStream in, File file)
 	{
 	    try 
 	    {
 	        OutputStream out = new FileOutputStream(file);
 	        byte[] buf = new byte[1024];
 	        int len;
 	        while((len=in.read(buf))>0)
 	        {
 	            out.write(buf,0,len);
 	        }
 	        out.close();
 	        in.close();
 	    } catch (Exception e) 
 	    {
 	        e.printStackTrace();
 	    }
 	}
 	
 	public static void loadYamls() 
 	{
 	    try {
 	        plugin.config.load(plugin.configFile);
 	        plugin.messages.load(plugin.messagesFile);
 	        plugin.token.load(plugin.tokenFile);
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}
 	public static void saveYamls() 
 	{
 	    try 
 	    {
 	        plugin.config.save(plugin.configFile);
 	        plugin.messages.save(plugin.messagesFile);
 	        plugin.token.save(plugin.tokenFile);
 	    } catch (IOException e)
 	    {
 	        e.printStackTrace();
 	    }
 	}
 	
 	public static void updateconfig()
 	{
		if(plugin.config.getString("configversion").equals(plugin.getDescription().getVersion()))
 		{
 			plugin.config.set("configversion", plugin.getDescription().getVersion());
 		}
 		if(plugin.config.getString("Apply.Enabled") == null)
 		{
 			plugin.config.set("Apply.Enabled", true);
 		}
 		if(plugin.config.getString("Apply.Password") == null)
 		{
 			plugin.config.set("Apply.Password", "test");
 		}
 		if(plugin.config.getString("Apply.Defaultgroup") == null)
 		{
 			plugin.config.set("Apply.Defaultgroup", "default");
 		}
 		if(plugin.config.getString("Apply.Group") == null)
 		{
 			plugin.config.set("Apply.Group", "Member");
 		}
 		if(plugin.config.getString("Apply.Freeze") == null)
 		{
 			plugin.config.set("Apply.Freeze", false);
 		}
 		if(plugin.config.getString("Apply.Mute") == null)
 		{
 			plugin.config.set("Apply.Mute", false);
 		}
 		if(plugin.config.getString("Apply.KickWrongPW") == null)
 		{
 			plugin.config.set("Apply.KickWrongPW", true);
 		}
 		
 		if(plugin.config.getString("Time.Enabled") == null)
 		{
 			plugin.config.set("Time.Enabled", false);
 		}
 		if(plugin.config.getString("Time.Group") == null)
 		{
 			plugin.config.set("Time.Group", "Member");
 		}
 		if(plugin.config.getString("Time.Time") == null)
 		{
 			plugin.config.set("Time.Time", 10);
 		}
 		if(plugin.config.getString("Time.CountOffline") == null)
 		{
 			plugin.config.set("Time.CountOffline", false);
 		}
 		
 		if(plugin.config.getString("PromoteSyntax") == null)
 		{
 			plugin.config.set("PromoteSyntax", "none");
 		}
 		
 		if(plugin.config.getString("token") != null)
 		{
 			for(String token : plugin.config.getConfigurationSection("token").getKeys(false))
 			{
 				String usage = plugin.config.getString("token." + token + ".usage"); 
 				String group = plugin.config.getString("token." + token + ".group"); 
 				plugin.token.set("token." + token + ".usage", usage);
 				plugin.token.set("token." + token + ".group", group);
 			}
 			plugin.config.set("token", null);
 		}
 		saveYamls();
 
 	}
 	
 	
 
 }
