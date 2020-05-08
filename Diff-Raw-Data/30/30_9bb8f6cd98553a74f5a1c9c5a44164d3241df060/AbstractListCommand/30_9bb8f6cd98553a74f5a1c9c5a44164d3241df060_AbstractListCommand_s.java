 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.command.base;
 
 import java.util.List;
 
 import org.apache.felix.gogo.commands.Option;
 
 import com.redhat.rhevm.api.model.BaseResource;
 
 import com.redhat.rhevm.api.command.base.VerboseDisplay;
 
 import static com.redhat.rhevm.api.command.base.BaseClient.pad;
 import static com.redhat.rhevm.api.command.base.BaseClient.value;
 
 /**
  * Displays the resources
  */
 public abstract class AbstractListCommand<T extends BaseResource> extends AbstractCommand {
 
     @Option(name = "-c", aliases = { "--concise" }, description = "Limit display to entity names", required = false, multiValued = false)
     private boolean concise;
 
     @Option(name = "-l", aliases = {"--verbose"}, description="Display entities in verbose format", required = false, multiValued = false)
     protected boolean verbose;
 
     protected VerboseDisplay<T> verboseDisplay;
 
     protected Object doList(List<T> collection, int limit) throws Exception {
         int i = 0, widestName = "Name".length(), widestId = "ID".length(), widestDescription = 0;
         for (T model : collection) {
             if (model.isSetName() && model.getName().length() > widestName) {
                 widestName = model.getName().length();
             }
             if (model.isSetId() && model.getId().length() > widestId) {
                 widestId = model.getId().length();
             }
             if (model.isSetDescription() && model.getDescription().length() > widestDescription) {
                 widestDescription = model.getDescription().length();
             }
         }
         for (T model : collection) {
             if (++i > limit) {
                 break;
             }
             if (concise) {
                 System.out.print(model.getName() + " ");
             } else {
                 if (i == 1) {
                     System.out.println(pad("Name", widestName, false)
                             + pad("ID", widestId, false)
                             + (widestDescription > 0 ? " Description" : ""));
                 }
                 System.out.println(pad(model.getName(), widestName)
                                    + pad(model.getId(), widestId)
                                    + value(model.getDescription()));
                 verbose(model);
             }
         }
         if (concise) {
             System.out.println();
         }
         return null;
     }
 
     protected void verbose(T model) {
        if (verboseDisplay != null) {
             verboseDisplay.expand(model);
         }
     }
 
     @SuppressWarnings("unchecked")
     public void setVerboseDisplay(VerboseDisplay verboseDisplay) {
         this.verboseDisplay = (VerboseDisplay<T>)verboseDisplay;
     }
 }
