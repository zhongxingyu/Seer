 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

 import java.io.File;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.UnknownHostException;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.ws.rs.core.UriBuilder;
 
 import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
 import org.fife.ui.rtextarea.RTextScrollPane;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 /**
  * Project Home: http://fifesoft.com/rsyntaxtextarea<br>
  * Downloads: https://sourceforge.net/projects/rsyntaxtextarea
  */
 public class JDecaf extends JFrame
 {
    private static final long serialVersionUID = 1L;
    private static final int FRAME_WIDTH = 900;
    private static final int FRAME_HEIGHT = 800;
    private static final String TITLE = "JDecaf Editor";
    private TextArea textArea=null;
    
 	//private static final String URL = "http://localhost:8080/dataUpdateService";
 	//private static final String URL = "http://193.61.44.50:8888/dataUpdateService";
 	private static final String URL = "http://talos:8888/dataUpdateService";
 
 	// constants
 	private static final int UPDATE_TIME_INTERVAL=5000;	//constant - an update is initiated after a 5 second pause
 	private static final int CHECK_TIME_INTERVAL=1000;	//constant - the daemon checks every second for user activity
 
 	// variables (* means synchronized)
 	private static int updateTimeInterval=UPDATE_TIME_INTERVAL;
 	private static int checkTimeInterval=CHECK_TIME_INTERVAL;
 	private static Timestamp startTime=null;			//timestamp to store the start of the session
 	private static Timestamp timeStamp=null;			//timestamp to store the last time a key was pressed (*)
 	//private static boolean initComplete=false;			//flag that ensures that initilisation is executed only once
 	//private static long totalCharsInserted=0;			//the total number of keys pressed since the session started (*)
 	private static long charsInserted=0;				//the number of keys pressed since the last update (*)
 	private static Timer timer = null; 					//timer used to scheduled checks for user in-activity
 
 	private boolean updateInactivity(Timestamp newTimestamp)
 	{
 		//update the total number of chars inserted so far
 		//totalCharsInserted+=charsInserted;
 		
 		//compute the time difference since the beginning of the session
 		//long timedifference=newTimestamp.getTime()-startTime.getTime();
 		
 		//compute chars/second ratio for the whole lot inserted so far
 		//double tratio=totalCharsInserted/(timedifference/1000);
 		
 		//compute the time difference since the last update
 		//timedifference=newTimestamp.getTime()-timeStamp.getTime();
 
 		//compute chars/second ratio for the characters inserted since the last update
 		//double ratio=charsInserted/(timedifference/1000);
 		
 		//reset the counter for the next measurement
 		charsInserted=0;
 		
 		//get username
 		String userName = System.getProperty("user.name");
 		
 		//get machine name
 		String machineName="";
 		
 		try
 		{
 			machineName = InetAddress.getLocalHost().getHostName();
 		}
 		catch (UnknownHostException e)
 		{
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 			System.out.println(e.getMessage());
 			machineName="unknown host";
 			return false;
 		}
 		
 		//update the database with the new values
 		//System.out.println(userName + "@" + machineName + " total ratio: "+tratio+" current ratio: "+ratio);
 		return updateDatabase(userName + "@" + machineName + "@inactive");
 	}
 
 	private boolean updateActivity(Timestamp newTimestamp)
 	{	
 		//get username
 		String userName = System.getProperty("user.name");
 		
 		//get machine name
 		String machineName="";
 		
 		try
 		{
 			machineName = InetAddress.getLocalHost().getHostName();
 		}
 		catch (UnknownHostException e)
 		{
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 			System.out.println(e.getMessage());
 			machineName="unknown host";
 			return false;
 		}
 		
 		//update the database with the new values
 		//System.out.println(userName + "@" + machineName + " active");
 		return updateDatabase(userName + "@" + machineName + "@active");
 	}
 	
 	private boolean updateDatabase(String userData)
 	{
 		Scanner stream=null;
 		
 		try
 		{
 			ClientConfig configuration=new DefaultClientConfig();
 			Client client=Client.create(configuration);
 			WebResource service=client.resource(getBaseURI());
 			ClientResponse response=(ClientResponse)service.path("rest").path("lkl").accept(new String[]{"text/plain"}).put(ClientResponse.class,userData);
 			System.out.println(response.toString());
 			stream=new Scanner(response.getEntityInputStream());
 			
 			while(stream.hasNext())
 			{
 				System.out.println(stream.nextLine());
 			}
 			
 		}
 		catch(Exception e)
 		{
 			System.out.println(e.getMessage());
 			return false;
 		}
 		finally
 		{
             if (stream != null)
             {
                 stream.close();
             }
         }
 		
 		return true;
 	}
 	
 	private static URI getBaseURI()
 	{
 		return UriBuilder.fromUri(URL).build((Object)null);
 	}	
 	
 	
    public JDecaf()
    {
       //create the editor component and parameterise it
       textArea = new TextArea(40, 100);
       textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
       textArea.setCodeFoldingEnabled(true);
       textArea.setAntiAliasingEnabled(true);
       
       textArea.getDocument().addDocumentListener(new DocumentListener()
       {
     	  public void changedUpdate(DocumentEvent e)
     	  {
     		  //markChange();
     	  }
 
     	  public void removeUpdate(DocumentEvent e)
     	  {
     		  markChange();
     		  handleActivity();
     	  }
 
     	  public void insertUpdate(DocumentEvent e)
     	  {
     		  markChange();
     		  handleActivity();
     	  }
 
 		  public void markChange()
 		  {
 			  textArea.setContentChanged(true);
 			  JDecaf.this.setAppTitle("*");
 		  } 
 		  
 		  public void handleActivity()
 		  {
 				Timestamp newTimestamp = getTimestamp();		
 
 				synchronized(JDecaf.this)
 				{
 					//initialise the start time if applicable
 					if(startTime==null)
 					{
 						startTime=newTimestamp;
 					}
 					
 					boolean update=false;
 
 					if(charsInserted==0)
 					{
 						update=updateActivity(newTimestamp);
 
 						if(!update)
 						{
 							System.out.println("update activity - not successful");						
 						}
 					}
 					
 					//update the counter
 					charsInserted++;		
 
 					//update the timestamp
 					timeStamp=newTimestamp;
 					
 					if(update)
 					{
 						System.out.println(timeStamp.toString());
 					}
 				}		
 			}
       });
       
 	  timer=new Timer();
 	  timer.schedule(new Task("(task) user activity tracker"), 0, checkTimeInterval);
       
       RTextScrollPane editorPane = new RTextScrollPane(textArea);
       editorPane.setFoldIndicatorEnabled(true);
       
       ActionListener listener = new ControlEventHandler(this,textArea);
       
       //create the buttons
       JButton newDocument = new JButton("New");
       newDocument.addActionListener(listener);
 
       JButton open = new JButton("Open");
       open.addActionListener(listener);
       
       JButton save = new JButton("Save");
       save.addActionListener(listener);
 
       JButton terminate = new JButton("Exit");
       terminate.addActionListener(listener);
       
       //create the main panel
 	  JPanel panel = new JPanel(new GridBagLayout());
 	  GridBagConstraints constraints = new GridBagConstraints();
 	  
 	  //add editor pane
 	  constraints.insets = new Insets(10,10,10,0);
 	  constraints.fill = GridBagConstraints.BOTH;
 	  constraints.gridheight = 4;	//occupy 4 cells
 	  constraints.gridx = 0;
 	  constraints.gridy = 0;
 	  panel.add(editorPane,constraints);
 	  
 	  //add buttons
 	  constraints.insets = new Insets(10,10,10,10);
 	  constraints.fill = GridBagConstraints.HORIZONTAL;
 	  constraints.gridheight = 1;	//occupy 1 cells
 	  constraints.gridx = 1;
 	  constraints.gridy = 0;
 	  panel.add(newDocument,constraints);
 
 	  constraints.gridx = 1;
 	  constraints.gridy = 1;
 	  panel.add(open,constraints);
 
 	  constraints.gridx = 1;
 	  constraints.gridy = 2;
 	  panel.add(save,constraints);
 
 	  constraints.anchor = GridBagConstraints.PAGE_START;
 	  constraints.gridx = 1;
 	  constraints.gridy = 3;
 	  panel.add(terminate,constraints);
 
       setContentPane(panel);
        
       this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
       this.setResizable(false);
       this.setAppTitle("");
       
      this.addWindowListener(new WindowAdapter()
  	  {
			public void windowClosing(WindowEvent e)
			{
				Timestamp timestamp = getTimestamp();
				updateActivity(timestamp);
				System.exit(0);
			}
	  });
	  
 //    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       this.pack();
       this.setLocationRelativeTo(null);
       
       System.out.println(getSize());
       System.out.println(textArea.getSize());
    }
       
    public void setAppTitle(String text)
    {
 	   File file=textArea.getFile();
 	   String fileName=file==null?"":file.getAbsolutePath();
 	   setTitle(TITLE+" "+fileName+text);
    }
 
    public static void main(String[] args)
    {
       // Start all Swing applications on the EDT.
       SwingUtilities.invokeLater(new Runnable()
       {
          public void run()
          {
             new JDecaf().setVisible(true);
          }
       });
    }
    
 	private Timestamp getTimestamp()
 	{
 		Date date= new Date();
 		return new Timestamp(date.getTime());
 	}
    
    class Task extends TimerTask
    {
 
 	   	private String name;                 // A string to output
 	
 	   	/**
 	   	* Constructs the object, sets the string to be output in function run()
 	   	* @param str
 	   	*/
 	   	Task(String name)
 	   	{
 	   		this.name = name;
 	   	}
 	
 	   	/**
 	   	* When the timer executes, this code is run.
 	   	*/
 	   	public void run()
 	   	{
 	   		//get the new timestamp
 	   		Timestamp newTimestamp = getTimestamp();
 	   				
 	   		//if there is previous timestamp, compute the difference and update the database
 	   		synchronized(JDecaf.this)
 	   		{
 	   			if(timeStamp!=null)
 	   			{
 	   				long timedifference=newTimestamp.getTime()-timeStamp.getTime();
 	   			
 	   				if(timedifference>=updateTimeInterval&&charsInserted!=0)
 	   				{
 	   					if(updateInactivity(newTimestamp))
 	   					{
 	   						System.out.println(name + ": updated database successfully (inactivity)");
 	   					}
 						else
 						{
 							System.out.println("update inactivity - not successful");						
 						}
 	   				}
 	   			}
 	   		}
 	   	}
    }
 }
 
