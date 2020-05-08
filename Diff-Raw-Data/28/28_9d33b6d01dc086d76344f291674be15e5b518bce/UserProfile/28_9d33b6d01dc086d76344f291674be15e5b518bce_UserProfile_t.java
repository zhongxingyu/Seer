 package core;
 import java.rmi.activation.ActivationException;
 import java.net.InetSocketAddress;
 import data.Constants;
 import java.net.SocketAddress;
 import gui.SwingGUI;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Scanner;
 import static data.Constants.*;
 
 /**
  *
  * @author Will
  */
 public class UserProfile
 {
 	private File profilePath;
 
 	private SocketAddress address;
 
 	private String hostName;
 	private String nickName;
 	private String realName;
 	private String userInfo;
 	private Integer port;
 
 	public UserProfile()
 	{
 		// defaults
 		nickName = "Nullname" + (int)(Math.random() * 8999 + 1000);
 		realName = "Anonymous";
 		userInfo = "No user info entered.";
 		hostName = "chat.freenode.net";
 		port = 6667;
 
 		if (initialize())
 		{
 			address = new InetSocketAddress(hostName, port);
 		}
 		else
 		{
 			throw new IllegalArgumentException();
 		}
 	}
 
 	/**
 	 *
 	 * @return true if successful, false otherwise
 	 */
 	private boolean initialize()
 	{
 		if (askProfile()) // using file profile
 		{
			// TODO create if does not exist
 			return readProfile();
 		}
		else if (askProfileComponents()) // using temp profile or making a new one
 		{
			if (isPersistent())
 			{
				writeProfile();
 			}

			return true;
 		}
			
		return false;  // could not gather enough data for a profile
 	}
 
 	/**
 	 *
 	 * @return true if successful, false otherwise
 	 */
 	private boolean readProfile()
 	{
 		try
 		{
 			Scanner in = new Scanner(profilePath);
 
 			nickName = in.nextLine().trim();
 			realName = in.nextLine().trim();
 			hostName = in.nextLine().trim();
 			port = in.nextInt();
 
 			in.close();
 
 			return true;
 		}
 		catch (IOException ex)
 		{
 			return false;
 		}
 	}
 
 	/**
 	 *
 	 * @return true if successful, false otherwise
 	 */
 	private boolean askProfileComponents()
 	{
 		return askNick(true) && askName(true) && askHost(true) && askPort(true);
 	}
 
 	/**
 	 *
 	 * @return true if successful, false otherwise
 	 */
 	private boolean askProfile()
 	{
 		if (PROFILE_DIRS_EXIST)
 		{
 			String answer = SwingGUI.askQuestion("Enter the new or existing profile to use:", "default");
 
 			if (answer != null)
 				profilePath = new File(PROFILE_PATH + SLASH + PROFILE_DIRS[0] + SLASH + answer.toLowerCase());
 			else
 				profilePath = PROFILE_PATH_DNE;
 
 			return isProfileWritable();
 		}
 
 		return false;
 	}
 
 	/**
 	 *
 	 * @return true if successful, false otherwise
 	 */
 	private boolean askHost(boolean first)
 	{
 		String answer;
 
 		if (first)
 			answer = SwingGUI.askQuestion("Enter the host-name:", hostName);
 		else
 			answer = SwingGUI.askQuestion("Invalid Host. Re-enter the host-name:", hostName);
 
 		if (answer != null)
 		{
 			hostName = answer;
 		}
 
 		return answer != null;
 	}
 
 	/**
 	 *
 	 * @return true if successful, false otherwise
 	 */
 	private boolean askPort(boolean first)
 	{
 		String answer;
 		Integer parsedPort = null;
 
 		if (first)
 			answer = SwingGUI.askQuestion("Enter the port:", port.toString());
 		else
			answer = SwingGUI.askQuestion("Invalid port. Re-enter the port:", port.toString());
 
 		if (answer != null)
 		{
 			try
 			{
 				parsedPort = Integer.valueOf(answer);
 			}
 			catch (NumberFormatException ex)
 			{
 				parsedPort = null;
 			}
 		}
 
 		if (parsedPort != null)
 		{
 			port = parsedPort;
 		}
 
 		return parsedPort != null;
 	}
 
 	/**
 	 *
 	 * @return true if successful, false otherwise
 	 */
 	public boolean askNick(boolean first)
 	{
 		String answer;
 
 		if (first)
 			answer = SwingGUI.askQuestion("Enter your nick-name:", nickName);
 		else
 			answer = SwingGUI.askQuestion("Invalid nick. Re-enter the nick-name:", nickName);
 
 		if (answer != null)
 		{
 			nickName = answer;
 		}
 
 		return answer != null;
 	}
 
 	/**
 	 *
 	 * @return true if successful, false otherwise
 	 */
 	public boolean askName(boolean first)
 	{
 		String answer;
 
 		if (first)
 			answer = SwingGUI.askQuestion("Enter your user-name:", realName);
 		else
 			answer = SwingGUI.askQuestion("Invalid user. Re-enter the user-name:", realName);
 
 		if (answer != null)
 		{
 			realName = answer;
 		}
 
 		return answer != null;
 	}
 
 	public String getHost()
 	{
 		return hostName;
 	}
 
 	public Integer getPort()
 	{
 		return port;
 	}
 
 	public String getNick()
 	{
 		return nickName;
 	}
 
 	public String getName()
 	{
 		return realName;
 	}
 
 	public SocketAddress getAddress()
 	{
 		return address;
 	}
 
 	public void setNick(String newNick)
 	{
 		nickName = newNick;
 	}
 
 	public boolean writeProfile()
 	{
 		if (!isProfileWritable())
 			throw new IllegalStateException("Profile is not writable");
 
 		try
 		{
 			BufferedOutputStream out =
 				new BufferedOutputStream(new FileOutputStream(profilePath));
 
 			out.write((nickName + "\n").getBytes());
 			out.write((realName + "\n").getBytes());
 			out.write((hostName + "\n").getBytes());
 			out.write((port + "\n").getBytes());
 
 			out.close();
 
 			return true;
 		}
 		catch (IOException ex)
 		{
 			System.err.println("Could not write profile.");
 
 			return false;
 		}
 	}
 
 	public boolean isPersistent()
 	{
 		return profilePath != null;
 	}
 
 	/**
 	 *
 	 * @return true if the profilePath is write safe
 	 */
 	public boolean isProfileWritable()
 	{
 		try
 		{
 			return PROFILE_DIRS_EXIST && profilePath != null &&
 				(profilePath.isFile() || profilePath.createNewFile());
 		}
 		catch (Exception ex)
 		{
 			return false;
 		}
 	}
 
 
 
 	public static final String[] PROFILE_DIRS = {"profiles", "scripts", "plugins"};
 	public static final File PROFILE_PATH = new File(System.getProperty("user.home") + File.separator + ".wIRC");
 	public static final File PROFILE_PATH_DNE = null;
 	public static boolean PROFILE_DIRS_EXIST;
 	
 	static
 	{
 		PROFILE_DIRS_EXIST = true;
 
 		if (!PROFILE_PATH.isDirectory() && !PROFILE_PATH.mkdir())
 			PROFILE_DIRS_EXIST = false;
 
 		File tempFile;
 
 		for (int i = 0; i < PROFILE_DIRS.length; ++i)
 		{
 			tempFile = new File(PROFILE_PATH + SLASH + PROFILE_DIRS[i]);
 
 			if (!tempFile.isDirectory() && !tempFile.mkdir())
 			{
 				PROFILE_DIRS_EXIST = false;
 			}
 		}
 	}
 }
