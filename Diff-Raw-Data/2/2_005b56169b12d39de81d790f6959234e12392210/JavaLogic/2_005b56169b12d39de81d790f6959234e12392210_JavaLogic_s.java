 /**
  * A Package containing the main flow of the program
  */
 package logic;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 
 import com.martiansoftware.jsap.FlaggedOption;
 import com.martiansoftware.jsap.JSAP;
 import com.martiansoftware.jsap.JSAPException;
 import com.martiansoftware.jsap.JSAPResult;
 import com.martiansoftware.jsap.Switch;
 import com.martiansoftware.jsap.UnflaggedOption;
 
 import account.Account;
 
 /**
  * @author erik heinisch 
  * 
  */
 public class JavaLogic 
 {
 	
 	private  String saveFile = "SaveFile";
 	private  ArrayList<Account> accounts = new ArrayList<Account>();
 	JSAP jsap = new JSAP();
 	private JSAPResult command;
 	
 	public JavaLogic(String[] args) throws JSAPException
 	{
 			// Account
 				FlaggedOption opt_name = new FlaggedOption("name").setStringParser(JSAP.STRING_PARSER).setDefault(JSAP.NO_DEFAULT).setRequired(false).setShortFlag('n').setLongFlag("name");
 				jsap.registerParameter(opt_name);
 				
 				FlaggedOption opt_inboxserver = new FlaggedOption("inboxserver").setStringParser(JSAP.STRING_PARSER).setDefault(JSAP.NO_DEFAULT).setRequired(false).setShortFlag(JSAP.NO_SHORTFLAG).setLongFlag("inboxserver");
 				jsap.registerParameter(opt_inboxserver);
 				FlaggedOption opt_inboxserverport = new FlaggedOption("inboxserverport").setStringParser(JSAP.INTEGER_PARSER).setDefault(JSAP.NO_DEFAULT).setRequired(false).setShortFlag(JSAP.NO_SHORTFLAG).setLongFlag("inboxserverport");
 				jsap.registerParameter(opt_inboxserverport);
 				
 				FlaggedOption opt_outboxserver = new FlaggedOption("outboxserver").setStringParser(JSAP.STRING_PARSER).setDefault(JSAP.NO_DEFAULT).setRequired(false).setShortFlag(JSAP.NO_SHORTFLAG).setLongFlag("outboxserver");
 				jsap.registerParameter(opt_outboxserver);
 				FlaggedOption opt_outboxserverport = new FlaggedOption("outboxserverport").setStringParser(JSAP.INTEGER_PARSER).setDefault(JSAP.NO_DEFAULT).setRequired(false).setShortFlag(JSAP.NO_SHORTFLAG).setLongFlag("outboxserverport");
 				jsap.registerParameter(opt_outboxserverport);
 			// Account
 			
 			// Mail 
 				FlaggedOption opt_mail_id = new FlaggedOption("mail_id").setStringParser(JSAP.INTEGER_PARSER).setDefault(JSAP.NO_DEFAULT).setRequired(false).setShortFlag(JSAP.NO_SHORTFLAG).setLongFlag("mail_id");
 				jsap.registerParameter(opt_mail_id);
 			// Mail
 			
 			// Contact
 				FlaggedOption opt_contact_id = new FlaggedOption("contact_id").setStringParser(JSAP.INTEGER_PARSER).setDefault(JSAP.NO_DEFAULT).setRequired(false).setShortFlag(JSAP.NO_SHORTFLAG).setLongFlag("contact_id");
 				jsap.registerParameter(opt_contact_id);
 			// Contact
 				
 			// Contact
 				Switch sw_help = new Switch("help").setShortFlag('h').setLongFlag("help");
 				jsap.registerParameter(sw_help);
 			// Contact
 			
 			FlaggedOption opt_type = new FlaggedOption("type").setStringParser(JSAP.STRING_PARSER).setDefault(JSAP.NO_DEFAULT).setRequired(true).setShortFlag('t').setLongFlag("type");
 			jsap.registerParameter(opt_type);
 			
 			UnflaggedOption ufopt_setting = new UnflaggedOption("setting").setStringParser(JSAP.STRING_PARSER).setDefault(JSAP.NO_DEFAULT).setRequired(false);
 			jsap.registerParameter(ufopt_setting);
 			
 			UnflaggedOption ufopt_value = new UnflaggedOption("value").setDefault(JSAP.NO_DEFAULT).setRequired(false).setGreedy(true);
 			jsap.registerParameter(ufopt_value);	
 				
 			command = jsap.parse(args);   // contains one command
 			
 		
 		deserialize(saveFile); 
 			if(command.getBoolean("help"))
 			{	
 				printHelp();
 			}
 			if(args.length>0 && (accounts.size()>0 || command.contains("type") && command.getString("type").equals("mkAccount") || command.contains("type") && command.getString("type").equals("help")))
 			{
 					switch (command.getString("type"))
 					{
 					case "mkAccount":	 mkAccount();	break;
 					case "rmAccount":	 rmAccount();	break;
 					case "modAccount":	 modAccount();	break;
 					case "getAccount":	 getAccount();	break;
 					
 					case "mkMail":		 mkMail();		break;
 					case "rmMail":		 rmMail();		break;
 					case "modMail":	 	 modMail();		break;
 					case "getMail":	 	 getMail();		break;
 					
 					case "mkContact":	 mkContact();	break;
 					case "rmContact":	 rmContact(); 	break;
 					case "modContact":	 modContact();	break;
 					case "getContact":	 getContact();	break;
 					
 					case "help":		 printHelp(); 		break;
 					}
 			}
 			else if (args.length>0)
 			{
 				noAccounts();
 			}
 			else 
 			{
 				printHelp();
 			}
 		serialize(saveFile);
 		
 	}
 
 	//// Begin : Account
 		private void mkAccount() 
 		{
 			//Account := accountName inboxServer inboxServerPort outboxServer outboxServerPort
 			if(command.contains("name") && command.contains("inboxserver") && command.contains("inboxserverport") && command.contains("outboxserver") && command.contains("outboxserverport"))
 			{
 				accounts.add(new Account(command.getString("name"), command.getString("inboxserver"), command.getInt("inboxserverport"), command.getString("outboxserver"), command.getInt("outboxserverport")));
 			}
 			else
 			{
 				System.err.println("Missing arguments usage : -t mkAccount --name 'accountname' --inboxserver 'pop3.servername.com' --inboxserverport '22' --outboxserver 'pop3.servername.com' --outboxserverport '255'");
 			}
 		}
 		private void rmAccount() 
 		{
 			//Account := accountName
 			if(command.contains("name"))
 			{
 				int i = 0;
 				while(i<accounts.size())
 				{
 					if(accounts.get(i).get("name").equals(command.getString("name")))
 					{
 						accounts.remove(i); // no i++ because if I remove an element the array will shrink and I would leave out an element. 
 					}
 					else
 					{
 						i++;
 					}
 				}
 			}
 			else
 			{
 				System.err.println("Missing arguments usage : -t rmAccount --name 'accountname'");
 			}
 		}
 		private void getAccount()
 		{
 			//Account := accountName attributToGet
 			if(command.contains("name") && command.contains("setting"))
 			{
 				for(Account a : accounts)
 				{
 					if(a.get("name").equals(command.getString("name")))
 					{
 						System.out.println(a.get(command.getString("setting")));
 					}
 				}
 			}
 			else
 			{
 				for(Account a : accounts)
 				{
 					System.out.println(a.get("name"));
 				}
 			}
 		}
 		private void modAccount()
 		{
 			//Account := accountName attributToSet Value
 			if(command.contains("name") && command.contains("setting") && command.contains("value"))
 			{
 				for(Account a : accounts)
 				{
 					if(a.get("name").equals(command.getString("name")))
 					{
 						a.set(command.getString("setting"),command.getString("value"));
 					}
 				}
 			}
 		}
 	//// End : Account
 	////Begin : Mail
 		private void mkMail() 
 		{
 			//Mail := accountName mail_id
 			if(command.contains("name") && command.contains("mail_id"))
 			{
 				for(Account account : accounts)
 				{
 					if(account.get("name").equals(command.getString("name")))
 					{
 						account.mkMail(command.getInt("mail_id"));
 					}
 				}
 			}
 		}
 		private void rmMail() 
 		{
 			//Mail := accountname mail_id
 			if(command.contains("name") && command.contains("mail_id"))
 			{
 				for(Account account : accounts)
 				{
 					if(account.get("name").equals(command.getString("name")))
 					{
 						account.rmMail(command.getInt("mail_id"));
 					}
 				}	
 			}
 		}
 		private void getMail() 
 		{
 			//Mail := accountname mail_id attributToGet
 			if(command.contains("name"))
 			{
 				if(command.contains("mail_id") && command.contains("setting"))
 				{
 					for(Account account : accounts)
 					{
 						if(account.get("name").equals(command.getString("name")))
 						{
 							account.getMail(command.getInt("mail_id"),command.getString("setting"));
 						}
 					}
 				}
 				else
 				{
 					for(Account account : accounts)
 					{
 						if(account.get("name").equals(command.getString("name")))
 						{
 							account.getMail();
 						}
 					}
 				}
 			}
 		}
 		private void modMail() 
 		{
 			//Mail := accountname mail_id setting value
 			if(command.contains("name") && command.contains("mail_id") && command.contains("setting") && command.contains("value"))
 			{
 				for(Account account : accounts)
 				{
 					if(account.get("name").equals(command.getString("name")))
 					{
 						account.modMail(command.getInt("mail_id"),command.getString("setting"),command.getStringArray("value"));
 					}
 				}
 			}
 		}
 	//// End : Mail
 	////Begin : Contact
 		private void mkContact()  
 		{
 			//Contact := accountName contact_id
 			if(command.contains("name") && command.contains("contact_id"))
 			{
 				for(Account account : accounts)
 				{
 					if(account.get("name").equals(command.getString("name")))
 					{
						account.mkMail(command.getInt("mail_id"));
 					}
 				}
 			}
 		}
 		private void rmContact() 
 		{
 			//Contact := accountname contact_id
 			if(command.contains("name") && command.contains("contact_id"))
 			{
 				for(Account account : accounts)
 				{
 					if(account.get("name").equals(command.getString("name")))
 					{
 						account.rmContact(command.getInt("contact_id"));  
 					}
 				}	
 			}
 		}
 		private void getContact()
 		{
 			//Contact := accountname contact_id attributToGet
 			if(command.contains("name"))
 			{
 				if(command.contains("contact_id") && command.contains("setting"))
 				{
 					for(Account account : accounts)
 					{
 						if(account.get("name").equals(command.getString("name")))
 						{
 							account.getContact(command.getInt("contact_id"),command.getString("setting"));
 						}
 					}
 				}
 				else
 				{
 					for(Account account : accounts)
 					{
 						if(account.get("name").equals(command.getString("name")))
 						{
 							account.getContact();
 						}
 					}
 				}
 			}
 		}
 		private void modContact()
 		{
 			//Contact := accountname contact_id
 			if(command.contains("name") && command.contains("contact_id") && command.contains("setting") && command.contains("value"))
 			{
 				for(Account account : accounts)
 				{
 					if(account.get("name").equals(command.getString("name")))
 					{
 						account.modContact(command.getInt("contact_id"),command.getString("setting"),command.getString("value"));
 					}
 				}
 			}
 		}
 	//// End : Contact
 
 	private void printHelp() 
 	{
 		try 
 		{
 			BufferedReader br = new BufferedReader(new FileReader("help.txt"));
 			for(String line = br.readLine(); line!=null; line=br.readLine())
 			{
 				System.out.println(line);
 			}
 	    	br.close();
 		} 
 		catch (IOException e) {e.printStackTrace();}
 	}
 
 //// Begin : Serialize and Deserialize ////
 	public void serialize(String p_saveFilePath)
 	{
 		  OutputStream fos = null;
 
 		  try
 		  {
 			// Begin : saveDirectory //
 				File saveDirectory = new File(p_saveFilePath); // root directory
 				deleteFolder(saveDirectory);
 		    	if(!saveDirectory.exists())
 		    	{
 		    		saveDirectory.mkdir();
 		    	}   
 		    // End : saveDirectory //
 		    for(Account acc : accounts)
 		    {
 		    	fos = new FileOutputStream(p_saveFilePath+File.separator+acc.get("name"), false);
 		    	ObjectOutputStream o = new ObjectOutputStream( fos );
 		    	o.writeObject(acc);
 		    	fos.close();
 		    }
 		    
 		  }
 		  catch ( IOException e ) { System.err.println( e ); }
 		
 	}
 	// Begin : Help method to clean up folder in serialize
 	public void deleteFolder(File p_saveFolder) 
 	{
 		File[] accountFiles = p_saveFolder.listFiles();
 		if(accountFiles!=null) //test for empty dirs
 		{ 
 			for(File accfile: accountFiles) 
 			{
 				if(accfile.isDirectory()) 
 				{
 					deleteFolder(accfile);
 				} else 
 				{
 					accfile.delete();
 				}
 			}
 		}
 		
 	}
 	// End : Help method to clean up folder in serialize
 	public void deserialize(String p_readFilePath)
 	{
 		// Begin : Preparation //
 			accounts.clear(); // In case this method is called more than one time the entries won't be doubled.
 		// End : Preparation //
 		try
     	{
 			File saveFolder = new File(p_readFilePath);
 			
 			if (saveFolder.list().length != 0) 
 			{
 				for (File fileEntry : saveFolder.listFiles()) 
 				{
 					if (!fileEntry.isDirectory()) 
 					{
 						InputStream fis = new FileInputStream(
 								fileEntry.getAbsoluteFile());
 						ObjectInputStream o = new ObjectInputStream(fis);
 
 						accounts.add((Account) o.readObject());
 						fis.close();
 					}
 				}
 			}
     	}
     	catch ( IOException|ClassNotFoundException e ) { System.err.println( e ); }
 	}
 //// End : Serialize and Deserialize ////
 	
 	private void noAccounts() 
 	{
 		System.err.println("Es gibt keine Accounts im dafür vorgesehenen ordner");
 		System.err.println("Also hier --> "+new File(saveFile).getAbsolutePath());
 	}
 }
