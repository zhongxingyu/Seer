/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

 package de.cosmocode.palava.bridge.command;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Maps;
 import de.cosmocode.palava.bridge.Content;
 import de.cosmocode.palava.bridge.call.Call;
 import de.cosmocode.palava.bridge.content.JsonContent;
 import de.cosmocode.palava.ipc.IpcCommand;
 import de.cosmocode.palava.ipc.IpcCommandExecutionException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Map;
 
 public class IpcCommandCommand implements Command {
     private static final Logger LOG = LoggerFactory.getLogger(IpcCommandCommand.class);
 
     private final IpcCommand ipcCommand;
 
     public IpcCommandCommand(IpcCommand ipcCommand) {
         this.ipcCommand = Preconditions.checkNotNull(ipcCommand, "IpcCommand");
     }
 
     @Override
     public Content execute(Call call) throws CommandException {
         Map<String,Object> result = Maps.newLinkedHashMap();
 
         try {
             ipcCommand.execute(call, result);
         } catch (IpcCommandExecutionException e) {
             throw new CommandException(e);
         }
 
         return new JsonContent(result);
     }
 }
