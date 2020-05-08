 /**
  * Copyright © 2012 Joseph Walton-Rivers <webpigeon@unitycoders.co.uk>
  *
  * This file is part of uc_PircBotX.
  *
  * uc_PircBotX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * uc_PircBotX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with uc_PircBotX.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.co.unitycoders.pircbotx.commandprocessor;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.events.MessageEvent;
 
 /**
  * centrally managed command parsing.
  *
  */
 public class CommandProcessor
 {
 	private final Pattern regex;
 	private final Map<String, Object> commands;
 	private final Map<String, Map<String, Method>> callbacks;
 
 	public CommandProcessor(char trigger)
 	{
 		this.regex = Pattern.compile(trigger+"([a-z0-9]+)(?: ([a-z0-9]+))?(?: (.*))?");
 		this.commands = new HashMap<String, Object>();
 		this.callbacks = new HashMap<String, Map<String, Method>>();
 	}
 
 	public void register(String name, Object target)
 	{
 		Map<String, Method> methods = new HashMap<String, Method>();
 
 		Class<?> clazz = target.getClass();
 		for (Method method : clazz.getMethods())
 		{
 			if (method.isAnnotationPresent(Command.class))
 			{
 				Command c = method.getAnnotation(Command.class);
 				assert !methods.containsKey(c.keyword());
 				methods.put(c.keyword(), method);
 			}
 		}
 
 		commands.put(name, target);
 		callbacks.put(name, methods);
 	}
 
 	public void invoke(MessageEvent<PircBotX> event) throws Exception
 	{
 		Matcher matcher = regex.matcher(event.getMessage());
 
 		//not valid command format
 		if (!matcher.matches())
 			return;
 
 		//XXX lart [thing], thing will be an action
 		String command = matcher.group(1);
 		String action = matcher.group(2);
 		String args = matcher.group(3);
 
 		System.out.println("[DEBUG] Command: "+command);
 		System.out.println("[DEBUG] action: "+action);
 		System.out.println("[DEBUG] args: "+args);
 
 		try
 		{
 			boolean valid;
 			if (action == null)
 				valid = false;
 			else
 				valid = call(command, action, event);
 
 			if(!valid)
 				call(command, "default", event);
		} catch (InvocationTargetException ex)
		{
 			Throwable real = ex.getCause();
 			real.printStackTrace();
 			event.respond("[cmd-error] "+real.getMessage());
 		} catch(Exception ex)
 		{
 			ex.printStackTrace();
 			event.respond("[error] "+ex.getMessage());
 		}
 	}
 
	private boolean call(String type, String cmd, Object... args) throws Exception
	{
 		Object obj = commands.get(type);
 		Map<String, Method> methods = callbacks.get(type);
 
 		System.out.println("Invoking "+type+" : "+cmd+" "+args);
 
 		if (methods == null)
 			return false;
 
 		Method method = methods.get(cmd);
 		if (method == null)
 			return false;
 
 		method.invoke(obj, args);
 		return true;
 	}
 }
