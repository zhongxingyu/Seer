 package org.terzieva.page.interpreter;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.terzieva.page.collection.ExitList;
 import org.terzieva.page.command.Command;
 import org.terzieva.page.command.CommandFactory;
 import org.terzieva.page.domain.Area;
 import org.terzieva.page.domain.AreaExit;
 import org.terzieva.page.game.Context;
 import org.terzieva.page.template.AreaTemplate;
 
 @Configurable
 public class AreaInterpreter extends AbstractInterpreter implements InitializingBean {
 
 	private Context context;
 	private Map<String, String> aliases;
 	private Map<String, Command> commands;
 	
 	@Autowired private CommandFactory commandFactory;
 	
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		commands = new HashMap<String, Command>();
 		commands.put("NORTH",commandFactory.getCommand("navigationCommand"));
 		commands.put("SOUTH",commandFactory.getCommand("navigationCommand"));
 		commands.put("EAST",commandFactory.getCommand("navigationCommand"));
 		commands.put("WEST",commandFactory.getCommand("navigationCommand"));
 		commands.put("NORTHEAST",commandFactory.getCommand("navigationCommand"));
 		commands.put("NORTHWEST",commandFactory.getCommand("navigationCommand"));
 		commands.put("SOUTHEAST",commandFactory.getCommand("navigationCommand"));
 		commands.put("SOUTHWEST",commandFactory.getCommand("navigationCommand"));
		commands.put("LOOK",commandFactory.getCommand("lookCommand"));
 	}
 	
 	public AreaInterpreter(Context context)
 	{
 		this.context = context;
 		
 		aliases = new HashMap<String, String>();
 		aliases.put("N","NORTH");
 		aliases.put("S","SOUTH");
 		aliases.put("E","EAST");
 		aliases.put("W","WEST");
 		aliases.put("NE","NORTHEAST");
 		aliases.put("NW","NORTHWEST");
 		aliases.put("SE","SOUTHEAST");
 		aliases.put("SW","SOUTHWEST");
 	}
 	
 	@Override
 	public void interpret(String input) 
 	{
 		// get the command
 		String command = input.split(" ")[0].toUpperCase();
 		
 		String aliasResult = aliases.get(command);
 		
 		if(aliasResult != null)
 			command = aliasResult;
 		
 		Command commandObj = commands.get(command);
 		
 		if(commandObj == null)
 			send("Command could not be interpreted.");
 		else
 			send(commandObj.interpret(command, context));
 	}
 	
 	@Override
 	public void start()
 	{
 		send("Area Interpreter accessed. Ready to move you to the world.");
 		
 		if(context.player.getCurrentArea() == null)
 		{
 			context.player.setCurrentArea(Area.findArea(1L));
 			context.player = context.player.merge();
 		}
 			
 		context.exits = new ExitList(AreaExit.findAreaExitsByFromArea(context.player.getCurrentArea()).getResultList());
 		
 		send(AreaTemplate.render(context.player.getCurrentArea(),context.exits));
 	}
 
 }
