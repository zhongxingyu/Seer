 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl.transfers.cli;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.ParseException;
 
 import ditl.cli.App;
 import ditl.cli.Command;
 import ditl.transfers.viz.TransferPlayer;
 
@Command(pkg = "transfers", cmd = "play", alias = "p")
 public class Player extends App {
 
     private File[] files;
 
     @Override
     protected void parseArgs(CommandLine cli, String[] args)
             throws ParseException, ArrayIndexOutOfBoundsException,
             HelpException {
         if (args.length > 0) {
             files = new File[args.length];
             for (int i = 0; i < args.length; ++i)
                 files[i] = new File(args[i]);
         }
     }
 
     @Override
     protected void run() throws IOException {
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 final TransferPlayer player = new TransferPlayer();
                 if (files != null)
                     player.load(files);
             }
         });
     }
 
     @Override
     protected String getUsageString() {
         return "STORE1 [STORE2...]";
     }
 
 }
