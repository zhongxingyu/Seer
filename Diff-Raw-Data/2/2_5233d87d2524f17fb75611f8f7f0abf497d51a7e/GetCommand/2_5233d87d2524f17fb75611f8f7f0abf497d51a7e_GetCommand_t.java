 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.console.commands.http;
 
 import static org.oobium.client.Client.client;
 
 import java.util.Map;
 
 import org.oobium.build.console.commands.HttpCommand;
 import org.oobium.client.ClientResponse;
 
 public class GetCommand extends HttpCommand {
 
 	@Override
 	public void configure() {
 		optionsRequired = true;
 	}
 	
 	@Override
 	protected ClientResponse executeRequest(String host, int port, String path, Map<String, String> parameters) {
		return client(host, port).get(path, parameters);
 	}
 	
 }
