 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package InsightLT;
 import edu.wpi.first.wpilibj.I2C;
 import edu.wpi.first.wpilibj.DigitalModule;
 import edu.wpi.first.wpilibj.Timer;
 import java.util.Vector;
 
 
 
 /**
  *
  * @author Bit Built Tech
  */
 public class InsightLT implements Runnable {
     
     public final static int FOUR_ZONES = 0;
     public final static int ONE_TWO_LINE_ZONE = 1;
     public final static int TWO_ONE_LINE_ZONES = 2;
     
     public final static int LINE_1 = 0;
     public final static int LINE_2 = 1;
     
     public InsightLT()
     {
         module = DigitalModule.getInstance(1);
         setupHelper();        
         config(ONE_TWO_LINE_ZONE);        
     }
     
     public InsightLT(int option)
     {
         
         module = DigitalModule.getInstance(1);  
         setupHelper();      
         config(option);
     }
     
     public InsightLT(int option, char moduleNumber)
     {       
         module = DigitalModule.getInstance(moduleNumber);
         setupHelper();        
         config(option);
     }
     	
     private void setupHelper()
     {
         m_stopDisplayThread = false;
         m_twiComm = module.getI2C(0x78);
         m_displayConnected = false;
         m_zones = new Vector();
     }
     public Zone getZone(int zoneNumber)
     {
         if(zoneNumber < 1 || zoneNumber > m_zones.size())
         {
             return null;
         }
         else
         {
             return (Zone)m_zones.elementAt(zoneNumber - 1);
         }
     }
     
     public void startDisplay()
     {
         thread = new Thread(this);
         m_stopDisplayThread = false;
         thread.start();
     }    
     
     public void stopDisplay()
     {
         m_stopDisplayThread = true;
     }
     
     public void writeMessage(String message, int line, int position)
     {
         byte[] buffer = new byte[6];
 	
 	buffer[0] = 0x00;
 	buffer[1] = (byte)(((line * 0x40) + position) | 0x80);	
 	m_twiComm.transaction(buffer, 2, buffer, 0);
 	
 	boolean end = false;
 	buffer[0] = 0x40;
 	for(int x = 0; x < message.length();)
 	{
             for(int y = 1; y < 6; y++)
             {
                 buffer[y] = (byte)message.charAt(x);
                 x++;
                 if(x >= message.length())
                 {
                     m_twiComm.transaction(buffer, y+1, buffer, 0);
                     end = true;
                     break;
                 }
             }
             if(!end)
             {
                 m_twiComm.transaction(buffer, 6, buffer, 0);
             }
 	}
     }
     
     public boolean registerData(DisplayData dataItem, int zoneNumber)
     {
         if(zoneNumber < 1 || zoneNumber > m_zones.size())
         {
             return false;
         }        
         ((Zone)m_zones.elementAt(zoneNumber - 1)).registerData(dataItem);
         return true;
     }
     
     public void setZoneScrollTime(int zoneNumber, int time)
     {
         if(zoneNumber > 0 && zoneNumber <= m_zones.size())
         {
             ((Zone)m_zones.elementAt(zoneNumber - 1)).setTime(time);
         }
     }
     
    public void manualScroll(int zoneNumber, char direction)
     {
         if(zoneNumber > 0 && zoneNumber <= m_zones.size())
         {
             if(direction > 0)
             {
                 ((Zone)m_zones.elementAt(zoneNumber - 1)).advanceZoneUp();
             }
             else
             {
                 ((Zone)m_zones.elementAt(zoneNumber - 1)).advanceZoneDown();
             }
         }
     }
     
     public void clearScreen()
     {
        byte[] buffer = new byte[2];
        buffer[0] = 0x00;
        buffer[1] = 0x01;
        m_twiComm.transaction(buffer, 2, buffer, 0);
     }
     
     private boolean getConnectionStatus()
     {
         return m_displayConnected;   
     }
     private void setConnectionStatus(boolean status)
     {
         m_displayConnected = status;
     }
     
     public void run()	
     {      
 	while(!m_stopDisplayThread)
 	{
             if(!getConnectionStatus())
             {
                 System.out.println("Connecting Display...");
                 while(m_twiComm.addressOnly())
                 {
                     Timer.delay(.5);
                 }
                 initializeDisplay();
                 clearScreen();
                 welcomeMessage();
                 setConnectionStatus(true);
                 Timer.delay(2);
                 clearScreen();
             }	
             else
             {
                 while(!m_stopDisplayThread)
                 {
                     if(m_twiComm.addressOnly())
                     {
                         setConnectionStatus(false);
                         break;
                     }
                     for(int x = 1; x < 5; x++)
                     {					
                         Zone tmpZone = getZone(x);
                         
                         if(tmpZone != null)
                         {
                             tmpZone.update();
                             writeMessage(tmpZone.getLineOne(), tmpZone.getLine(), tmpZone.getPosition());                           
                             if(tmpZone.isTwoLines())
                             {
                                 writeMessage(tmpZone.getLineTwo(), tmpZone.getLine() + 1, tmpZone.getPosition());
                             }
                         }
                     }	
                     Timer.delay(.05);
                 }                
             }
         }
         setConnectionStatus(false);
     }
     
     private void initializeDisplay()
     {
         	
 	byte[] buffer = new byte[6];
 	byte[] emptyArray = new byte[0];
         
 	buffer[0] = 0x00;		
 	buffer[1] = 0x38;		
 	buffer[2] = 0x39;				
 	buffer[3] = 0x14;
 	buffer[4] = 0x78;
 	buffer[5] = 0x5E;
 	m_twiComm.transaction(buffer, 6, emptyArray, 0);
 	
 	buffer[0] = 0x00;
 	buffer[1] = 0x6D;	
 	m_twiComm.transaction(buffer, 2, emptyArray, 0);
 	Timer.delay(.01);			
 	
 	buffer[0] = 0x00;
 	buffer[1] = 0x0C;
 	buffer[2] = 0x01;
 	buffer[3] = 0x06;
 	m_twiComm.transaction(buffer, 4, emptyArray, 0);			
 	Timer.delay(.01);
     }
     
     private void welcomeMessage()
     {
         writeMessage("Bit Built Tech", LINE_1, 0);
         writeMessage("Robot Diagnostics", LINE_2, 3);
     }
     
     private void config(int option)
     {        	
 	switch(option)
 	{
 	case FOUR_ZONES:
 		m_zones.addElement(new Zone(LINE_1, 0, 10, 1, 1500));
 		m_zones.addElement(new Zone(LINE_1, 10, 10, 1, 1500));
 		m_zones.addElement(new Zone(LINE_2, 0, 10, 1, 1500));
 		m_zones.addElement(new Zone(LINE_2, 10, 10, 1, 1500));
 	case TWO_ONE_LINE_ZONES:
 		m_zones.addElement(new Zone(LINE_1, 0, 20, 1, 1500));
 		m_zones.addElement(new Zone(LINE_2, 0, 20, 1, 1500));
 		break;
 	case ONE_TWO_LINE_ZONE:
 	default:
 		m_zones.addElement(new Zone(LINE_1, 0, 20, 2, 1500));
 		break;
 	}
     }
 
     private DigitalModule module;
     private Thread thread;
     private I2C m_twiComm;
     private boolean m_wasPaused;
     private boolean m_displayConnected;
     private boolean m_started;
     private boolean m_stopDisplayThread;
     private Vector m_zones;
 }
