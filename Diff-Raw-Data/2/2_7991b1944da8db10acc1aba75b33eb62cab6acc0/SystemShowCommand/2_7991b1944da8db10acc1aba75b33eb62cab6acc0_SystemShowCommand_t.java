 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  *  $Id$
  *
  */
 package org.ow2.sirocco.cimi.tools;
 
 import java.util.List;
 
 import org.nocrala.tools.texttablefmt.Table;
 import org.ow2.sirocco.cimi.sdk.CimiClient;
 import org.ow2.sirocco.cimi.sdk.CimiClientException;
 import org.ow2.sirocco.cimi.sdk.System;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.beust.jcommander.ParametersDelegate;
 
 @Parameters(commandDescription = "show system")
 public class SystemShowCommand implements Command {
     @Parameter(description = "<system id>", required = true)
     private List<String> systemIds;
 
     @ParametersDelegate
     private ResourceSelectExpandParams showParams = new ResourceSelectExpandParams();
 
     @Override
     public String getName() {
         return "system-show";
     }
 
     @Override
     public void execute(final CimiClient cimiClient) throws CimiClientException {
         System system = System.getSystemByReference(cimiClient, this.systemIds.get(0), this.showParams.getQueryParams());
         SystemShowCommand.printSystem(system, this.showParams);
     }
 
     public static void printSystem(final System system, final ResourceSelectExpandParams showParams) throws CimiClientException {
         Table table = CommandHelper.createResourceShowTable(system, showParams);
 
        if (showParams.isSelected("state") && system.getState() != null) {
             table.addCell("state");
             table.addCell(system.getState().toString());
         }
 
         java.lang.System.out.println(table.render());
     }
 }
