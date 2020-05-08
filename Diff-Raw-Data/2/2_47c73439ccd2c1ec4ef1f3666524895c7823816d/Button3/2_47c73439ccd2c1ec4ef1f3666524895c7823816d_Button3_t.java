 import Button2.NetworkRequest;
 import Button2.Sender;
 import Util.Messages;
 import Util.Popup;
 import Util.Strings;
 import javax.microedition.lcdui.Display;
 import javax.microedition.midlet.MIDlet;
 
 /*******************************************************************************
 * CLASS: Button3
  *
  * FUNCTIONS: void startApp()
  *            void pauseApp()
  *            void destroyApp(boolean unconditional)
  *
  * DATE: 2013-01-18
  *
  * REVISIONS: 2013-05-18
  *            Steve Lo
  *            John Payment
  *
  * DESIGNER: Team Cirno
  *
  * PROGRAMMER: Team Cirno
  *
  * NOTES: This is a Java Mobile App intended to connect to the validation server
  *        with pre-stored credentials. It then reports that the device should
  *        no longer be monitored by the server.
  *******************************************************************************/
 public class Button3 extends MIDlet
 {
 	/**
 	 * Constant string representing the connection being OK.
 	 */
 	private static final String OK = "OK"; 
 
 	/*************************************************************************** 
 	 * FUNCTION: startApp
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * REVISIONS: 2013-05-10
 	 *            Steve Lo
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: void startApp()
 	 * 
 	 * NOTES: Called when the app is launched.
 	 ***************************************************************************/
 	public void startApp()
 	{
 		String response;
 		Popup p;
 		Display disp;
 		disp = Display.getDisplay(this);
 		try
 		{
 			String[] str = Strings.getAllMessages();
 			p = new Popup(this, str[Messages.SENDING], false, null);
 			disp.setCurrent(p);
 			response = Sender.send(null, NetworkRequest.END_MONITORING_MESSAGE);
 			p = new Popup(this, (response.equals(OK)) ? str[Messages.RECEIVED] : response, true, null);
 			disp.setCurrent(p);
 		} catch(Exception e)
 		{
 			//most likely to happen if there is an error in Strings.getAllMessages()
 			Popup pop = new Popup(this, "Please login using the Configurator first", true, null);
 			disp.setCurrent(pop);
 		}
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: pauseApp
 	 * 
 	 * DESIGNER: N/A
 	 * 
 	 * PROGRAMMER: N/A
 	 * 
 	 * INTERFACE: void pauseApp()
 	 * 
 	 * NOTES: Default function inherited by the parent class.
 	 ***************************************************************************/
 	public void pauseApp()
 	{
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: destroyApp
 	 * 
 	 * DESIGNER: N/A
 	 * 
 	 * PROGRAMMER: N/A
 	 * 
 	 * INTERFACE: void destroyApp(boolean unconditional)
 	 * 
 	 * NOTES: Default function inherited by the parent class.
 	 ***************************************************************************/
 	public void destroyApp(boolean unconditional)
 	{
 	}
 }
