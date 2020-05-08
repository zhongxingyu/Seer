 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: HtmlCommandPanel.java,v 1.4 2003-11-16 17:54:27 shahid.shah Exp $
  */
 
 package com.netspective.sparx.panel;
 
 import java.io.Writer;
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.lang.exception.NestableRuntimeException;
 
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.command.HttpServletCommand;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.commons.command.CommandException;
 import com.netspective.commons.command.Command;
 import com.netspective.commons.command.Commands;
 import com.netspective.commons.value.ValueSource;
 
 public class HtmlCommandPanel extends AbstractPanel
 {
     private static final Log log = LogFactory.getLog(HtmlCommandPanel.class);
     private Command command;
     private ValueSource commandExpr;
 
     public HtmlCommandPanel()
     {
     }
 
     public Command getCommand()
     {
         return command;
     }
 
     public void setCommand(Command command)
     {
         this.command = command;
     }
 
     public ValueSource getCommandExpr()
     {
         return commandExpr;
     }
 
     /**
      * A value source that will be evaluated and the result of the evaluation will be treated as a command
      * specification. This method allows the actual command that will be executed to be dynamic.
      * @param commandExpr
      */
     public void setCommandExpr(ValueSource commandExpr)
     {
         this.commandExpr = commandExpr;
     }
 
     public void render(Writer writer, NavigationContext nc, Theme theme, int flags) throws IOException
     {
         ValueSource commandExpr = getCommandExpr();
         if(commandExpr != null)
         {
             String commandText = commandExpr.getTextValue(nc);
             if(commandText != null)
             {
                 try
                 {
                     HttpServletCommand httpCommand = (HttpServletCommand) Commands.getInstance().getCommand(commandText);
                     httpCommand.handleCommand(writer, nc, false);
                     return;
                 }
                 catch (CommandException e)
                 {
                     log.error("Command error in " + this.getClass().getName(), e);
                     throw new NestableRuntimeException(e);
                 }
             }
         }
 
         // if we get to here, we don't have an expression or the expression returned null so see if we have static
         // command supplied
 
         if(command == null)
         {
             writer.write("No command provided.");
             return;
         }
 
         try
         {
             ((HttpServletCommand) command).handleCommand(writer, nc, false);
         }
         catch (CommandException e)
         {
            log.error("Command error in panel", e);
            writer.write(e.toString());
         }
     }
 
     public void render(Writer writer, DialogContext dc, Theme theme, int flags) throws IOException
     {
         ValueSource commandExpr = getCommandExpr();
         if(commandExpr != null)
         {
             String commandText = commandExpr.getTextValue(dc);
             if(commandText != null)
             {
                 try
                 {
                     HttpServletCommand httpCommand = (HttpServletCommand) Commands.getInstance().getCommand(commandText);
                     httpCommand.handleCommand(writer, dc, false);
                     return;
                 }
                 catch (CommandException e)
                 {
                     log.error("Command error in " + this.getClass().getName(), e);
                     throw new NestableRuntimeException(e);
                 }
             }
         }
 
         if(command == null)
         {
             writer.write("No command provided.");
             return;
         }
 
         try
         {
             ((HttpServletCommand) command).handleCommand(writer, dc, false);
         }
         catch (CommandException e)
         {
            log.error("Command error in panel", e);
            writer.write(e.toString());
         }
     }
 }
