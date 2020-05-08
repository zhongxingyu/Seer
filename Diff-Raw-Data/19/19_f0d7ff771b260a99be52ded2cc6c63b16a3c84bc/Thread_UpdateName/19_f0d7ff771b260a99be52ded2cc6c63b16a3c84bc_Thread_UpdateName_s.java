 package thread;
 /* This program is licensed under the terms of the GPL V3 or newer*/
 /* Written by Johannes Putzke*/
 /* eMail: die_eule@gmx.net*/ 
 
 
 import gui.Gui_TablePanel;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ResourceBundle;
 
 import control.SRSOutput;
 
 import misc.Stream;
 
 
 
 /**
  * This Thread reads streams from the process of an streamripper process
  * and updates names and error in the right cell on StreamRipStars mainwindow.
  * @author Johannes Putzke
  *
  */
 public class Thread_UpdateName extends Thread
 {
 	private String i = "";
 	private Stream stream;
 	private int row;
 	private Gui_TablePanel tablePanel;
 	private Boolean isDead = false;
 	private ResourceBundle trans = ResourceBundle.getBundle("translations.StreamRipStar");
 	private Process streamProcess;
 	private BufferedReader b,c;
 	
 	/**
 	 * To create with this constructor you need the stream, you want to record, the table
 	 * where the new track names will updates in and the row in the table. Make sure there
 	 * really exist. There are no tests for valid parameters.
 	 * @param astream
 	 * @param arow
 	 * @param tablePanel
 	 */
 	public Thread_UpdateName(Stream astream, int arow, Gui_TablePanel tablePanel)
 	{
 		this.stream = astream;
 		this.row = arow;
 		this.tablePanel = tablePanel;
 	}
 	
 	/**
 	 * set an variable to kill this thread
 	 */
 	public void killMe()
 	{
 		SRSOutput.getInstance().log("Kill update names in cells for stream: "+ stream.name);
 		isDead = true;
 	}
 	
 	/**
 	 * Start the thread running and with it read from the input streams
 	 */
 	public void run()
 	{	
 		streamProcess = stream.getProcess();
 		if(isDead == false && streamProcess != null )
 		{	
 			try
 			{
 				boolean streamG = false;
 				boolean serverG = false;
 				boolean bitrateG = false;
 				boolean metaG = false;
 				String[] metaData = {"-","-","-","-"};
 				
 				tablePanel.setSelectedCurrentNameCellAndTitle(trans.getString("receivMeta"),row,true);
 				b = new BufferedReader(new InputStreamReader(streamProcess.getInputStream()));
 				c = new BufferedReader(new InputStreamReader(streamProcess.getErrorStream()));
 				
 				//read metaData as long as:
 				//- it comes no Killsignal to stop this Thread (isDead)
 				//- all data is read correctly (*G)
 				//- and the streamProcess exist (streamProcess != null?
 				while(!isDead && !(streamG && serverG && bitrateG && metaG)&& streamProcess != null)
 				{
 					//Test if byte are in the cache,
 					testErrorstream();
 					
 					String tmpo = b.readLine();
 					
 					if(tmpo == null )
 					{
 						isDead = true;
 					} else {
 						tmpo = tmpo.toLowerCase();
 						//if you can't connect, streamRipStar print out the reason
 						// for 8 seconds
 						if(tmpo.startsWith("error") && !isDead) 
 						{
 							tablePanel.setSelectedCurrentNameCellAndTitle(tmpo,row,true);
							Thread.sleep(8000);
 							isDead = true;
 						}
 						
 						if(tmpo.startsWith("connecting") && !isDead) 
 						{
 							tablePanel.setSelectedCurrentNameCellAndTitle(
 									trans.getString("connecting"),row,true);
 						}
 						
 						if(tmpo.startsWith("stream:")&& !isDead) 
 						{
 							metaData[0] = tmpo.substring(7).trim();
 							streamG=true;
 							tablePanel.setSelectedCurrentNameCellAndTitle(
 									trans.getString("streamReceived"),row,true);
 						}
 						
 						if(tmpo.startsWith("server name")&& !isDead) 
 						{
 							metaData[1] = tmpo.substring(12).trim();
 							serverG=true;
 							tablePanel.setSelectedCurrentNameCellAndTitle(
 									trans.getString("serverNameReceived"),row,true);
 						}
 						
 						//need to look for the bitrate in an hole string, because the word bitrate
 						//is changed its position in every version of streamripper
 						if(tmpo.contains("bitrate:") && !tmpo.startsWith("server name") && !isDead)
 						{
 							metaData[2] = tmpo.substring(8).replace("bitrate", "").replace(":", "").trim() + " kbit/s";
 							bitrateG=true;
 							tablePanel.setSelectedCurrentNameCellAndTitle(
 									trans.getString("bitrateReceived"),row,true);
 						}
 						
 						if(tmpo.startsWith("meta interval:")&& !isDead)
 						{
 							metaData[3] = tmpo.substring(14).trim();
 							metaG=true;
 							tablePanel.setSelectedCurrentNameCellAndTitle(
 									trans.getString("metaIntervallReceived"),row,true);
 						}
 						
 						// this is needed for streamripper > 1.63, because of
 						// new name update issues
 						// this skip fetching serverinfo for streamRipStar 
 						if(tmpo.startsWith("[skipping")&& !isDead) {
 							metaG=true;
 							streamG=true;
 							bitrateG=true;
 							serverG=true;
 							tablePanel.setSelectedCurrentNameCellAndTitle(
 									trans.getString("skipBefore"),row,true);
 						}
 						
 						//failsave: if you are here, not all data where collected
 						//but the server sends already the title. In this case,
 						//break searching for metadata
 						if(tmpo.startsWith("[ripping")&& !isDead) {
 							metaG=true;
 							streamG=true;
 							bitrateG=true;
 							serverG=true;
 						}
 					}
 				}
 				//write metaData in stream
 				stream.setMetaData(metaData);
 
 		// From here StreamRipStar using it usually 
 				
 				//Read as long as stream is alive or a kill signal comes
 				while(!isDead && stream.getProcess()!= null && (i = b.readLine()) != null) {
 					//first test, if an error appears  
 					testErrorstream();
 					
 					//looks, that it write in the right cell
 					if(!(stream.name.equals(tablePanel.getNameFromRow(row)))) {
 						int row2 = tablePanel.getNewRowForNameForUpdate(stream.name);
 						tablePanel.setSelectedCurrentNameCellAndTitle("",row,false);
 						row = row2;
 					}
 					
 					if(i.startsWith("Time to stop"))
 						tablePanel.setSelectedCurrentNameCellAndTitle(i,row,true);
 					else if(i.startsWith("[ripping...")) {
 						if(i.length()>= 17)
 							tablePanel.setSelectedCurrentNameCellAndTitle(i.substring(17),row,true);
 					}
 					else if(i.startsWith("[buffering")) {
 						tablePanel.setSelectedCurrentNameCellAndTitle(trans.getString("buffering"),row,true);
 					}
 					else if(i.startsWith("[skipping")) {
 						if(i.length()>= 16)
 							tablePanel.setSelectedCurrentNameCellAndTitle(i.substring(16),row,true);
 					}
 					//Thread.sleep(200);
 				}
 			} catch (IOException e) {
 				SRSOutput.getInstance().logE("Controlled IOException: "+e.getMessage());
 				stream.setStatus(false);
 			} catch (InterruptedException f) {
 				SRSOutput.getInstance().logE("Controlled InterruptedException: "+f.getMessage());
 				stream.setStatus(false);
 			}
 		}
 		if(stream.getStatus())
 			stream.setStop(false);
 		tablePanel.setSelectedCurrentNameCellAndTitle("",row,false);
 	}
 	
 	public void testErrorstream() {
 		try {
 			if(streamProcess.getErrorStream().available() != 0) {
 				String tmpe;
 				//read as long as the message "error" comes
 				while((tmpe = c.readLine()) != null && !isDead) {
 					//look for the error message
 					//then print it in the right cell for 8 seconds
 					//and kill the thread
 					if((tmpe != null) && !isDead && tmpe.toLowerCase().startsWith("error")) {
 						tablePanel.setSelectedCurrentNameCellAndTitle(tmpe,row,true);
						Thread.sleep(8000);
 						isDead = true;
 					}
 				}
 			}
 		}
 		catch (InterruptedException f) {
 			SRSOutput.getInstance().logE(f.getMessage());
 			stream.setStatus(false);
 		} catch (IOException e) {
 			SRSOutput.getInstance().logE(e.getMessage());
 			stream.setStatus(false);
 		}
 	}
 }
 
