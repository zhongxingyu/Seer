 package handlers.output;
 
 import handlers.OutputHandler;
 import core.Facade;
 import data.ServerChannel;
 import gui.TextColor;
 
 /**
  *
  * @author Will
  */
 public class PrivMsgHandler extends OutputHandler
 {
 	private static final String[] HOOKS = {"PRIVMSG", "MSG"};
 
 	public PrivMsgHandler(Facade mgr)
 	{
 		super(mgr);
 	}
 
 	@Override
 	public String[] getHooks()
 	{
 		return HOOKS;
 	}
 
 	@Override
 	public void process(String msg, ServerChannel dest)
 	{
 		Facade mgr = getManager();
 		
		mgr.sendData(msg, dest.server);
 
 		mgr.println("<" + mgr.profile.getNick() + "> ", dest, TextColor.BLUE_BOLD);
 		mgr.print(msg.substring(msg.indexOf(" :") + 2), dest, TextColor.BLACK);
 	}
 }
