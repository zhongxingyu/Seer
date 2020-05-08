 /*
  * (C) Copyright 2009-2010 Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * Contributors:
  *   Bogdan Stefanescu (bs@nuxeo.com), Nuxeo
  *   Stefane Fermigier (sf@nuxeo.com), Nuxeo
  *   Florent Guillaume (fg@nuxeo.com), Nuxeo
  */
 
 package org.apache.chemistry.shell.cmds.base;
 
 import java.text.SimpleDateFormat;
 
 import org.apache.chemistry.opencmis.client.api.Document;
 import org.apache.chemistry.shell.app.Application;
 import org.apache.chemistry.shell.app.Context;
 import org.apache.chemistry.shell.command.Cmd;
 import org.apache.chemistry.shell.command.Command;
 import org.apache.chemistry.shell.command.CommandException;
 import org.apache.chemistry.shell.command.CommandLine;
 import org.apache.chemistry.shell.command.CommandParameter;
 import org.apache.chemistry.shell.util.Path;
 
 @Cmd(syntax = "ls [-l] [target:item]", synopsis = "List entries in working directory")
 public class Ls extends Command {
 
 	@Override
 	public void run(Application app, CommandLine cmdLine) throws Exception {
 		ensureConnected(app);
 
 		String param = cmdLine.getParameterValue("target");
 		CommandParameter lParam = cmdLine.getParameter("-l");
 		Context ctx;
 		if (param == null) {
 			ctx = app.getContext();
 			ctx.reset();
 			for (String line : ctx.ls()) {
 				println(line);
 			}
 		} else {
 			ctx = app.resolveContext(new Path(param));
 			if (ctx == null) {
 				throw new CommandException("Cannot resolve target: " + param);
 			}
 			Document document = ctx.as(Document.class);
 			// TODO interpret -l arguments correctly
 			if (document != null) {
 				if (lParam != null) {
 					String acls = "?????????";
 					if (document.getAcl() != null) {
 						acls += "+";
 					} else {
 						if (document.getProperty("cmis:isImmutable").equals(
 								Boolean.TRUE)) {
 							acls = "?-??-??-?";
 						} else {
 							acls = "?????????";
 						}
 					}
 					int hardLinks = document.getParents().size();
 					String owner = document.getLastModifiedBy();
 					String group = document.getCreatedBy();
 					long size = document.getContentStreamLength();
					String modDate = new SimpleDateFormat("yyyy MMM dd hh:mm")
 							.format(document.getLastModificationDate()
 									.getTime());
 					String name = document.getName();
 					println(String.format("-%s %d %s %s %d %s %s", acls,
 							hardLinks, owner, group, size, modDate, name));
 				} else {
 					println(document.getName());
 				}
 			} else {
 				for (String line : ctx.ls()) {
 					println(line);
 				}
 			}
 		}
 	}
 }
