 package ASPseed;
 /**
  * 
  */
 
 /**
  * @author fshelly
  *
  */
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import dcctime.DeltaTime;
 import dcctime.stdtime;
 
 public class ASPseed
 {
 	public static ASPpreferences prefs;
 	/**
 	 * @param args
 	 */
 
 	private static void ShowHelp()
 	{
 		System.out.print(
 				"USAGE: ASPseed get|getnew <host> <port> <station>.[<location>-]<channel>\n" +
 				"                 <start yyyy/mm/dd> <start hh:mm:ss>\n" +
 				"                 <end yyyy/mm/dd> <end hh:mm:ss> <savedir>|<savefile>\n" +
 				"   or  ASPseed dir <host> <port> <station>\n" +
 				"	<host>          Host name or IP address\n" +
 				"	<port>          4003 or other port number\n" +
 				"	<station>       Station name\n" +
 				"	<location>      Location code, missing means blank location\n" +
 				"	                Can use ? and * for wildcarding\n" +
 				"	<channel>       3 Character channel name\n" +
 				"	                Can use ? and * for wildcarding\n" +
 				"	<yyyy/mm/dd>    Year/ month/day of month\n" +
 				"   <hh:mm:ss>      Hour:Minute:Second start/end time\n" +
 				"	<savedir>       Name of directory to save seed data to\n" +
 				" <savefile>      Append all data to this file\n" +
 				"\n" +
 				"First command downloads seed records matching given pattern to <savedir>\n" +
 				"Second command outputs a directory listing of all available channels\n" +
 				"\n" +
 				"File naming convention for get:\n" +
 				"  1) First character is either C for data taken from the continuous buffer\n" +
 				"     or E for data taken from the event buffer.\n" +
 				"  2) Second character is the first letter of the channel(s) requested\n" +
 				"     (taken from BHZ in this example)\n" +
 				"  3) Third and fourth characters contains the last two digits of the year\n" +
 				"     of the requested start time.\n" +
 				"  4) Fifth and sixth characters is the month of the requested start time\n" +
 				"  5) Seventh and eighth characters is the day of the month of start time\n" +
 				"  6) Ninth character (after the period) is the hour + 'A' of start time\n" +
 				"     (i.e. 0=A, 1=B, 2=C etc)\n" +
 				"  7) Tenth and eleventh characters are the minute of start time.\n" +
 				"\n" +
 				"  Example request of 00-BHZ starting at 2008/09/12 1:28\n" +
 				"  Resulting file name = CB080912.B28\n" +
 				"\n" +
 				"File naming convention for getnew:\n" +
 				"  1)  Two digit location if not blank\n" +
 				"  2)  Three digit channel name\n" +
 				"  3)  Last two digits of year of start time\n" +
 				"  4)  Two digit month 1-12 of start time\n" +
 				"  5)  Two digit day of month 1-31 of start time\n" +
 				"  6)  Two digit hour 0-23 of start time\n" +
 				"  7)  Two digit minute 0-59 of start time\n" +
 				"  8)  The extension .seed\n" +
 				"\n" +
 				"  Example request of 00-BHZ starting at 2008/09/12 1:28\n" +
 				"  Resulting file name = 00BHZ0809120128.seed\n"
 			);
 
 	} // ShowHelp()
 
 	public static void main(String[] args)
 	{
 		// No arguments means run the display window
 		if (args.length == 0)
 		{
 			prefs = new ASPpreferences();
 
 			new ASPdisplay();
 
 
 		}
 		else if (args.length == 9)
 		{
 			// Correct number of arguments, see if they parse
 			if (args[0].compareTo("get") != 0 && args[0].compareTo("getnew") != 0)
 			{
 				ShowHelp();
 				System.exit(1);
 			}
 			String hostname = args[1];
 			int port = Integer.parseInt(args[2]);
 			String id = args[3];
 			String station = id.substring(0, id.indexOf('.'));
 			String date = args[4];
 			String time = args[5];
 			String startTime = date + " " + time;
 			String endTime = args[6] + " " + args[7];
 			String savedir = args[8];
 			String chanList[]= null;
 			OutputStream outfile=null;
 			
 			// Get list of channels that have data matching request
 			DirSocket getDirThread = new DirSocket(hostname, port, id, startTime, endTime); 
 			getDirThread.start();
 
 			while (!getDirThread.Done() && getDirThread.isAlive())
 			{
 				try
 				{
 					Thread.sleep(1000);
 				} catch (InterruptedException e)
 				{
 					System.err.println("Sleep failed waiting for DirThread to end");
 					e.printStackTrace();
 				}
 			} // loop until data collection thread says it is done
 
 			chanList = getDirThread.GetChannelList();
 			if (chanList != null)
 			{
 				System.out.println("Downloading " + chanList.length 
 						+ " channel files.");
 				for (int i = 0; i < chanList.length; i++)
 				{
 					String channel;
 					String location;
 					String filename;
 					int duration;
 					int expected;
 					final char hourChar[] = 
 							{'A','B','C','D','E','F','G','H','I','J','K','L',
 							 'M','N','O','P','Q','R','S','T','U','V','W','X'};
 
 
 
 					// Do some syntax checks on return string
 					if (chanList[i].length() < 48) return;
 					if (chanList[i].charAt(2) != '/') return;
 					if (chanList[i].charAt(11) != '/') return;
 					if (chanList[i].charAt(14) != '/') return;
 					if (chanList[i].charAt(20) != ':') return;
 					if (chanList[i].charAt(23) != ':') return;
 					if (chanList[i].charAt(31) != '/') return;
 					if (chanList[i].charAt(34) != '/') return;
 					if (chanList[i].charAt(40) != ':') return;
 					if (chanList[i].charAt(43) != ':') return;
 
 					if (!Character.isDigit(chanList[i].charAt(47))) return;
 					expected = Integer.valueOf(chanList[i].substring(47)).intValue();
 
 					// See if last argument is a file or a directory
 					File saveDirFile = new File(savedir);
 					
 					location = chanList[i].substring(0,2);
 					channel = chanList[i].substring(3, 6);
 					if (location.charAt(0) != ' ')
 						id = station + '.' + location + '-' + channel;
 					else
 						id = station + '.' + channel;
 					
 					if (!saveDirFile.isDirectory())
 					{
 						// User specified a file name so append to that file
 						filename = savedir;
 					} // user specified a file to append to
 					else if (args[0].compareTo("getnew") == 0)
 					{
 						// user wants new filename format
 						if (location.charAt(0) != ' ')
 							filename = savedir + File.separatorChar
 							+ location + channel
 							+ date.substring(2, 4)
 							+ date.substring(5,7)
 							+ date.substring(8,10)
 							+ time.substring(0,2)
 							+ time.substring(3,5)
 							+ '.' + "seed";
 						else
 							filename = savedir + File.separatorChar
 							+ channel
 							+ date.substring(2, 4)
 							+ date.substring(5,7)
 							+ date.substring(8,10)
 							+ time.substring(0,2)
 							+ time.substring(3,5)
 							+ '.' + "seed";
 					} // if getnew argument
 					else
 					{
 						// argument had to be a "get" with a directory specified
 						// user wants old filename format
 						int hour = Integer.valueOf(time.substring(0,2)).intValue();
 						filename = savedir + File.separatorChar
 						+ 'C'
 						+ channel.charAt(0)
 						+ date.substring(2, 4)
 						+ date.substring(5,7)
 						+ date.substring(8,10)
 						+ '.'
 						+ hourChar[hour]
 						+ time.substring(3,5);
 
 					} // get with directory
 
 					System.out.println(chanList[i] + ": append to " + filename);
 					System.out.flush();
 					try
 					{
 						outfile = new FileOutputStream(filename, true);
 					} catch (FileNotFoundException e)
 					{
 						System.err.println("Failed to open file " + savedir);
 						e.printStackTrace();
 						System.exit(1);
 					}
 					
 					stdtime tFinish = new stdtime(args[6], args[7]);
 					stdtime tStart = new stdtime(date, time);
 
 					DeltaTime tDelta = new DeltaTime(tFinish, tStart);
 					if (tDelta.ExceedsMaxSeconds())
 					{
 						System.out.println("Duration exceeds limit of " + DeltaTime.MaxDeltaInt
 								+ " seconds for " + location + '/' + channel);
 						duration = DeltaTime.MaxDeltaInt;
 						System.out.flush();
 					}
 					else
 						duration = tDelta.toSeconds();
 					if (duration < 1) duration = 1;
 
 					// Get data for this channel and time period
 					SeedSocket getSeedThread = new SeedSocket(hostname, port, id, date,
 							time, duration, outfile); 
 					getSeedThread.start();
 
 					while (!getSeedThread.Done() && getSeedThread.isAlive())
 					{
 						try
 						{
 							Thread.sleep(1000);
 						} catch (InterruptedException e)
 						{
 							System.err.println("Sleep failed waiting for SeedThread");
 							e.printStackTrace();
 						}
 					} // loop until data collection thread says it is done
 
 					if (expected != getSeedThread.GetRecordCount())
 					{
 						System.out.println("Only collected " + getSeedThread.GetRecordCount()
 								+ " of " + expected + " available records");
 						System.out.flush();
 					}
 
 					try
 					{
 						outfile.close();
 					} catch (IOException e)
 					{
 						System.out.println("Error closing seed data file.");
 						e.printStackTrace();
 					}
 					if (getSeedThread.GetRecordCount() < 1)
 					{
 						// No data so delete output file
 						System.out.println("Warning, no data added to file " + filename);
 					}
 				} // loop through each channel returned
 				System.out.println("Done transferring seed data.");
 			} // channels were returned
 			else
 			{
 				System.out.println("No channels have data matching request.");
 			}
 
 		} // running data request from the command line
 		else if (args.length == 4)
 		{
 			// Get directory listing
 			String hostname = args[1];
 			int port = Integer.parseInt(args[2]);
 			String station = args[3];
 			String id = station +".*-*";
 			String chanList[]= null;
 
 			// verify arguments
 			if (args[0].compareTo("dir") != 0)
 			{
 				System.err.println("Invalid command, expected 'dir'");
 				ShowHelp();
 				System.exit(1);
 			}
 			
 			// Get data
 			DirSocket getDirThread = new DirSocket(hostname, port, id, "*", "*"); 
 			getDirThread.start();
 
 			while (!getDirThread.Done() && getDirThread.isAlive())
 			{
 				try
 				{
 					Thread.sleep(1000);
 				} catch (InterruptedException e)
 				{
 					System.err.println("Sleep failed waiting for DirThread to end");
 					e.printStackTrace();
 				}
 			} // loop until data collection thread says it is done
 
 			chanList = getDirThread.GetChannelList();
			if (chanList == null)
			{
				System.out.println("No response to directory request");
			}
			else
			{
  			for (int i = 0; i < chanList.length; i++)
        {
  	      System.out.println(chanList[i]);
        }
  			System.out.println("Done listing " + getDirThread.GetChannelCount()
  					+ " available channels");
			}
 
 		} // running dir command from the command line
 		else // argument count is wrong, show help
 		{
 			ShowHelp();
 		}
 
 	} // main()
 
 } // class ASPseed
 
