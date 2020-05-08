 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
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
 package ditl.graphs.cli;
 
 import java.io.IOException;
 
 import org.apache.commons.cli.*;
 
 import ditl.Store.*;
 import ditl.WritableStore.AlreadyExistsException;
 import ditl.cli.ConvertApp;
 import ditl.graphs.*;
 
 public class MovementToLinks extends ConvertApp {
 	
 	private GraphOptions graph_options = new GraphOptions(GraphOptions.LINKS, GraphOptions.MOVEMENT);
 	private double range;
 	private Long max_interval = null;
 	
 	public final static String PKG_NAME = "graphs";
 	public final static String CMD_NAME = "movement-to-links";
 	public final static String CMD_ALIAS = "m2l";
 
 
 	@Override
 	protected void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
 		MovementTrace movement = (MovementTrace) orig_store.getTrace(graph_options.get(GraphOptions.MOVEMENT));
 		LinkTrace links = (LinkTrace) dest_store.newTrace(graph_options.get(GraphOptions.LINKS), LinkTrace.type, force);
 		if ( max_interval == null )
 			max_interval = movement.maxTime()-movement.minTime();
 		else
 			max_interval *= movement.ticsPerSecond();
 		new MovementToLinksConverter(links, movement, range, max_interval).convert();
 	}
 	
 	@Override
 	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
 		super.parseArgs(cli, args);
 		graph_options.parse(cli);
 		range = Double.parseDouble(args[1]);
		max_interval = (Long) cli.getParsedOptionValue(intervalOption);
 	}
 
 	@Override
 	protected String getUsageString() {
 		return "[OPTIONS] STORE RANGE";
 	}
 	
 	@Override
 	protected void initOptions(){
 		super.initOptions();
 		graph_options.setOptions(options);
 		options.addOption(null, intervalOption, true, "interval beyond which not to look for new meetings (useful if positions are updated every seconds)");
 	}
 
 }
