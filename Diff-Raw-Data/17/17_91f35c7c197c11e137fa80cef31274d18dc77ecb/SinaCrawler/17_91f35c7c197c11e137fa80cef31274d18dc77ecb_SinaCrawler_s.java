 package swarm;
 /**
  * 
  * @author swarm
  *
  */
 import java.io.IOException;
 
 import org.apache.commons.httpclient.HttpException;
 
 import weibo4j.Weibo;
 import weibo4j.examples.Log;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import javax.swing.*;
 
 import java.awt.Toolkit;
 import java.awt.Dimension;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Timer;
 
 public class SinaCrawler  implements ActionListener 
 { 
 	  public SinaCrawler()
 	    {
 		  	printStream = new PrintStream(new MyOutputStream());
 		  	
 			JFrame crawlerFrame = new JFrame();
 			crawlerFrame.setTitle("The NEW Sina Weibo Crawler!"); 
 			 
 			Toolkit kit = Toolkit.getDefaultToolkit();    // 定义工具包
 			Dimension screenSize = kit.getScreenSize();   // 获取屏幕的尺寸
 			int screenWidth = screenSize.width/2;         // 获取屏幕的宽
 			int screenHeight = screenSize.height/2;       // 获取屏幕的高
 			int height = crawlerFrame.getHeight();
 			int width = crawlerFrame.getWidth();
 	    
 			crawlerFrame.setLocation(screenWidth-width/2-300, screenHeight-height/2-322);
 	 
 	        setUpUIComponent(crawlerFrame);
 			crawlerFrame.setVisible(true);
 			crawlerFrame.addWindowListener(
 		              new WindowAdapter(){
 		                  public void windowClosing(WindowEvent e){
 		                     System.exit(0);	
 		                  }	
 		              }	
 		         );
 			
			System.setOut(printStream);
			System.setErr(printStream);
 	    }
 
 	    private void setUpUIComponent(final JFrame f)
 	    {
 	        f.setSize(400, 400);   
 	        f.setResizable(false);
 	        Container container = f.getContentPane();
 	        controlPanel = new JPanel();
 			controlPanel.setBackground(new Color(232,232,232));
 	        statusPanel = new JPanel();
 	        Box baseBox=Box.createVerticalBox();
 			controlPanel.setLayout(new BorderLayout());
 			controlPanelLabel = new JLabel("                      The new Sina Weibo Crawler based on API V2!!!                ");
 			controlPanelLabel.setBackground(new Color(0,0,0));
 			controlPanelLabel.setMaximumSize(new Dimension(400,66));
 	        controlPanel.add(controlPanelLabel,BorderLayout.NORTH);
 	        statusPanelLabel = new JLabel("  ");
 	        
 		
 			statusPanel.setLayout(new BorderLayout());
 			statusPanel.add(statusPanelLabel,BorderLayout.NORTH);
 			statusArea = new JTextArea();
 			statusArea.setBorder(BorderFactory.createTitledBorder("status of the crawler"));	
 			statusArea.setEditable(false);
 			statusArea.setAutoscrolls(true); 
 			String statusStringBefore = "\n The Crawler has not started~\n\n Just choose your own account \n and the crawler that swarm demands, then press start!! ";
 			statusArea.setText(statusStringBefore);
             
 			baseBox.add(controlPanel);			
 			baseBox.add(statusPanelLabel);
 			baseBox.add(statusArea);
 			
 			container.add(baseBox);
  
 			Box controlBox = Box.createHorizontalBox(); 
 			Box controlOneVBox = Box.createVerticalBox();
 			Box controlTwoVBox = Box.createVerticalBox();
 			String[] account = {"1  Wang Heng's Account ","2  Longwei Yu's Account","3  Ge Zhenghan's Account", "4  Zhang Zhaochen's  Account","5  Zhang Erchen's Account"};
 			String[] crawlerString = {" Only User Crawler"," Only Relationship Crawler"," Only Status Crawler"," Only Comments Crawler"};
 			chooseAccountBox = new JComboBox<String>(account);
 			chooseAccountBox.addActionListener(this);
 			chooseAccountBox.setMaximumSize(new Dimension(320,50));
 			chooseAccountBox.setBorder(BorderFactory.createTitledBorder("select which one's account to use "));
 			chooseCrawlerBox = new JComboBox<String>(crawlerString);
 			chooseCrawlerBox.addActionListener(this);
 			chooseCrawlerBox.setMaximumSize(new Dimension(320,50));
 			chooseCrawlerBox.setBorder(BorderFactory.createTitledBorder(" select which crawler to start "));
 
 			paddingLabel = new JLabel("         ");
 
 			controlOneVBox.add(chooseAccountBox);
 			controlOneVBox.add(paddingLabel);
 			paddingLabel = new JLabel("         ");
 			controlOneVBox.add(paddingLabel);
 			paddingLabel = new JLabel("         ");
 			controlOneVBox.add(paddingLabel);
 			controlOneVBox.add(chooseCrawlerBox);
 			startButton = new JButton("Start");
 			startButton.setMaximumSize(new Dimension(80,30));
 			aboutButton = new JButton("About");
 			aboutButton.setMaximumSize(new Dimension(80,30));   
 			exitButton = new JButton("Exit");
 			exitButton.setMaximumSize(new Dimension(80,30)); 
 			
 			startButton.addActionListener(this);			
 			aboutButton.addActionListener(
 					new ActionListener()
 					{
 						public void actionPerformed(ActionEvent e)
 						{
 							String text = "Email:   iswangheng@gmail.com\n"+"        " +
 									"@author   swarm"+"\n            " +
 									"version: beta";							
 						     JOptionPane.showMessageDialog(f, text, "About this crawler    ", 1);
 						}
 					});			
 			exitButton.addActionListener(this);
 			
 			controlTwoVBox.add(startButton);
 			controlTwoVBox.add(paddingLabel);  
 			controlTwoVBox.add(aboutButton);
 			paddingLabel = new JLabel("         ");
 			controlTwoVBox.add(paddingLabel);
 			controlTwoVBox.add(exitButton);
 			controlBox.add(controlOneVBox);
 			controlBox.add(controlTwoVBox);
 			controlPanel.add(controlBox,BorderLayout.CENTER);
 			 
 	    } 
 	    
 	    public class MyOutputStream extends OutputStream
 	    {
 	        public void write(int arg0) throws IOException
 	        {
 	          // 写入指定的字节，忽略
 	        }    
 	        
 	        public void write(byte data[]) throws IOException
 	        {
 	          // 追加一行字符串
 	          //statusArea.append(new String(data));
 	          //statusArea.setText(new String(data));
 	        }
 	        
 	        public void write(byte data[], int off, int len) throws IOException 
 	        {
 	          // 追加一行字符串中指定的部分，这个最重要
 	        	//statusArea.setText(new String(data, off, len));
 	          // 移动TextArea的光标到最后，实现自动滚动
 	        	//statusArea.setCaretPosition(statusArea.getText().length());
 	        	final String message = new String(data, off, len);
                 sb.append(message); 
 	            SwingUtilities.invokeLater(new Runnable(){
 	                public void run(){
 	                   if(sb.length()>131)
 	                	   {
 	                	   		sb.delete(0,sb.length()-90);// 控制 sb 的大小确保不会占用太大内存。  
 	                	   }  
 	                    //sb.append("-------sb:"+sb.capacity()+"------- "); 
 	                   currentTime = System.currentTimeMillis(); 
 	                   
 	                   int runningTime = (int)(currentTime - startTime);
 	                   runningTime = runningTime/1000;
 	                   int hour = 0;
 		           	   int minute = 0;
 		           	   int second = 0;
 		           	   int temp = runningTime%3600;
 		           	   if(runningTime>3600)
 		           	   {
 		           	    	   hour= runningTime/3600;
 		           	              if(temp!=0)
 		           	              {
 		           	            	  if(temp>60)
 		           	            	  {
 		           	            		  minute = temp/60;
 		           	            		  if(temp%60!=0)
 		           	            		  {
 		           	            			  	second = temp%60;
 		           	            		  }
 		           	            	  }
 		           	            	  else
 		           	            	  {
 		           	            		  second = temp;
 		           	            	  }
 		           	              }
 		           	    }
 		           	   else
 		           	   {
 		           	    	minute = runningTime/60;
 		           	    	if(runningTime%60!=0)
 		           	    	{
 		           	    	 second = runningTime%60;
 		           	    	}
 		           	   }
 	                    String statusString = statusStringCrawler+"\n\n Crawler has run for "+hour+" hours "+minute+" minutes "+second+" seconds !!"+" \n\n Attention: the running time is not updated in real time~\n\n";
 	                    statusString += sb.toString();
 	                    statusArea.setText(statusString); 
 	                }
 	            });
 	        }
 	    }
 	    
 	    public void actionPerformed(ActionEvent e)
 	    {
 	    	if(e.getSource() == chooseAccountBox)
 	    	{
 	    		 index = chooseAccountBox.getSelectedIndex();
 		 	}
 	    	if(e.getSource() == chooseCrawlerBox)
 	    	{
 	    		crawlerIndex = chooseCrawlerBox.getSelectedIndex();
 	    	}
 	    	if(e.getSource() == startButton)
 	    	{
 	    		isStart++;
 				String crawlerName = "";
 				crawlerName = SetAndGetCrawlerName();
 	    		//首次start则运行，否则不运行
 	    		if(isStart == 1)
 	    		{
 					setAccess(index);	  
 					System.out.println("Index: "+index);
 					startTime = System.currentTimeMillis();
 					statusStringCrawler = new String("\n The "+crawlerName+" crawler is now running!!!"); 
 					statusArea.setText(statusStringCrawler);
 					//statusArea.paintImmediately(statusArea.getBounds());
 					statusArea.repaint();
 
 					weibo = new Weibo();
 					weibo.setToken(PublicMethods.accessToken); 
 					Thread toRun = SetAndGetThread();
 					toRun.start();
 	    		}
 	    		else
 	    		{
 	    			statusStringCrawler = new String("\n The "+crawlerName+" crawler is ALREADY running!!!");
 	    			statusArea.setText(statusStringCrawler);;
 	    		}
 		 	} 
 	    	if(e.getSource() == exitButton)
 	    	{
 	    		System.exit(0);	
 		 	}
 	    }
 	    
 	    
 	    public static void setAccess(int i)
 	    {					
 			GetAccessToken getAccessToken = null; 			
 	    	 switch (i)
 	    	 {
 		    	 case 0:	// WH
 		    		 getAccessToken = new GetAccessToken(Access.WH.client_ID, Access.WH.client_SECRET, Access.WH.userId, Access.WH.passwd);
 		 			 getAccessToken.setToken();
 		    		 break;
 		    	 case 1:	// LWY
 		    		 getAccessToken = new GetAccessToken(Access.LWY.client_ID, Access.LWY.client_SECRET, Access.LWY.userId, Access.LWY.passwd);
 		 			 getAccessToken.setToken();
 		    		 break;
 		    	 case 2:    // GZH
 		    		 getAccessToken = new GetAccessToken(Access.GZH.client_ID, Access.GZH.client_SECRET, Access.GZH.userId, Access.GZH.passwd);
 		 			 getAccessToken.setToken();
 		    		 break;
 		    	 case 3:	//ZZC
 		    		 getAccessToken = new GetAccessToken(Access.ZZC.client_ID, Access.ZZC.client_SECRET, Access.ZZC.userId, Access.GZH.passwd);
 		 			 getAccessToken.setToken();
 		    		 break; 
 		    	 case 4:		//ZZC-OLD
 		    		 getAccessToken = new GetAccessToken(Access.ZEC.client_ID, Access.ZEC.client_SECRET, Access.ZEC.userId, Access.ZEC.passwd);
 		 			 getAccessToken.setToken();
 		    		 break; 
 		    	default: 
 		    		 getAccessToken = new GetAccessToken();
 		 			 getAccessToken.setToken(); 
 	    	 }
 			PublicMethods.accessToken = getAccessToken.getToken();
 			//Log.logInfo("accessToken: "+PublicMethods.accessToken);
 			System.out.println("accessToken: " + PublicMethods.accessToken);
 	    }
 	    
 	    public Thread SetAndGetThread()
 	    {
 	    	Thread toRun = new Thread();
 			switch(crawlerIndex)
 			{
 				case 0: 
 					toRun = new Thread(new GetUsers());
 					break;
 				case 1: 
 					toRun = new Thread(new GetRelationship());
 					break;
 				case 2: 
 					toRun = new Thread(new GetUserStatus());
 					break;
 				case 3: 
 					toRun = new Thread(new GetComments());
 					break;
 				default: 
 					toRun = new Thread(new GetUserStatus());
 					break;
 			}
 			return toRun;
 	    }
 	    
 	    public String SetAndGetCrawlerName()
 	    {
 	    	String crawlerName = "";
 			switch(crawlerIndex)
 			{
 				case 0:
 					crawlerName = "User";
 					break;
 				case 1:
 					crawlerName = "Relationship";
 					break;
 				case 2:
 					crawlerName = "Status";
 					break;
 				case 3:
 					crawlerName = "Comments";
 					break;
 				default: 
 					break;
 			}
 			return crawlerName;
 	    }
 	    
 		public static void main(String[] args) throws HttpException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
 		{ 	
 			
 			//Thread getUsersThread = new Thread(new GetUsersThread());  
 			//Thread getRelationshipThread = new Thread(new GetRelationshipThread()); 
 			//Thread getUserStatusThread = new Thread(new GetUserStatusThread());
 			//Thread getCommentsThread = new Thread(new GetCommentsThread());
 			
 			//getUserStatusThread.start();		
 			//getUsersThread.start();
 			//System.out.println("consumer key: "+Weibo.CONSUMER_KEY+" secret: "+Weibo.CONSUMER_SECRET);
 			//System.out.println("accessToken: "+accessToken+" accessTokenSecret: "+accessTokenSecret);
 			 
 			new SinaCrawler();
 		}
 		
 	    private JLabel controlPanelLabel;
 	    private JLabel statusPanelLabel;
 	    private JPanel controlPanel; 
 	    private JPanel statusPanel;
 	    
 	    private JTextArea statusArea;
 	    private PrintStream printStream;
 	    
 	    private JComboBox<String> chooseAccountBox;
 	    private JComboBox<String> chooseCrawlerBox;
 	    
 	    private JButton startButton; 
 	    private JButton exitButton;
 	    private JButton aboutButton;
 	    private JLabel paddingLabel;
 	     
 	  	private static long startTime = (long)0;
 	  	private static long currentTime = (long)0;
 	  	
 	    private StringBuffer sb = new StringBuffer();
 	    
 	    private static String statusStringCrawler = "";
 	    
 	    public static int isStart = 0;
 	    public static int index = 0;
 	    public static int crawlerIndex = 0;
 	    public static String consumerKey ="";
 	    public static String consumerSecret ="";
 	    public static String accessToken ="";
 	    public static String accessTokenSecret ="";
 	    
 	    private Weibo weibo;
 
 }
