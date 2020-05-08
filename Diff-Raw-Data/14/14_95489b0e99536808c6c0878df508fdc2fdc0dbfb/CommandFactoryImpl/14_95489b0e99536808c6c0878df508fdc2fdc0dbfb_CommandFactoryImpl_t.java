 /**
  * Copyright 2013 Simon Curd <simoncurd@gmail.com>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.hula.lang.factory;
 
 import java.io.IOException;
import java.io.InputStream;
 import java.util.Properties;
 
 import com.hula.lang.factory.exception.CommandFactoryException;
 import com.hula.lang.factory.exception.ErrorCreatingCommandException;
 import com.hula.lang.factory.exception.UnknownCommandException;
 import com.hula.lang.model.Command;
 import com.hula.lang.util.FileUtil;
 
 /**
  * Implementation of the {@link CommandFactory}
  */
 public class CommandFactoryImpl implements CommandFactory
 {
 	private Properties commands = new Properties();
 
 	@Override
 	public void loadCommands(String filename) throws IOException
 	{
		InputStream in = null;
		try
		{
			in = FileUtil.getFileInputStream(filename);
			commands.load(in);
		}
		finally
		{
			in.close();
		}
		
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Command getCommandInstance(String id) throws CommandFactoryException
 	{
 		if (commands.containsKey(id))
 		{
 			String value = commands.getProperty(id);
 			Command command = null;
 
 			try
 			{
 				Class<Command> commandClass = (Class<Command>) Class.forName(value);
 				command = (Command) commandClass.newInstance();
 				return command;
 			}
 			catch (Throwable t)
 			{
 				throw new ErrorCreatingCommandException("Cannot create command from commandId [" + id + "]", t);
 			}
 		}
 		else
 		{
 			throw new UnknownCommandException("no command defined for commandId [" + id + "]");
 		}
 	}
 
 
 
 
 }
