 /*
  * Copyright (C) 2012 TomyLobo
  *
  * This file is part of Routes.
  *
  * Routes is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package eu.tomylobo.routes.commands.system;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.bukkit.command.CommandSender;
 
 /**
  * Wraps a method+instance to be invoked by the command system.
  *
  * @author TomyLobo
  *
  */
 public class Invoker {
 	protected Method method;
 	protected CommandContainer instance;
 	protected String[] permissions;
 
 	public Invoker(Method method, CommandContainer instance, String[] permissions) {
 		this.method = method;
 		this.instance = instance;
 		this.permissions = permissions;
 	}
 
 	public void invoke(Context context) throws CommandException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (!hasPermission(context.getSender()))
 			throw new PermissionDeniedException();
 		try {
 			method.invoke(instance, context);
 		}
 		catch (InvocationTargetException e) {
 			if (!(e.getCause() instanceof CommandException))
 				throw e;
 			
 			throw (CommandException) e.getCause();
 		}
 	}
 
 	protected boolean hasPermission(CommandSender sender) {
 		for (String permission : permissions) {
 			if (sender.hasPermission(permission))
 				return true;
 		}
 
 		return false;
 	}
 }
