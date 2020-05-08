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
 
 import ditl.Store.LoadTraceException;
 import ditl.WritableStore.AlreadyExistsException;
 import ditl.cli.ImportApp;
 import ditl.graphs.*;
 
 
 public class ImportMovement extends ImportApp {
 	
 	private ExternalFormat ext_fmt = new ExternalFormat(ExternalFormat.NS2, ExternalFormat.ONE);
 	private Long maxTime;
 	private long ticsPerSecond;
 	private Double timeMul;
 	private GraphOptions graph_options = new GraphOptions(GraphOptions.MOVEMENT);
 	private long interval;
 	private long offset;
 	private boolean fix_pause_times;
 	
 	private String fixPauseTimesOption = "fix-pause-times";
 	
 	public final static String PKG_NAME = "graphs";
 	public final static String CMD_NAME = "import-movement";
 	public final static String CMD_ALIAS = "im";
 
 	@Override
 	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
 		super.parseArgs(cli, args);
 		graph_options.parse(cli);
 		ext_fmt.parse(cli);
		maxTime = (Long) cli.getParsedOptionValue(maxTimeOption);
 		ticsPerSecond = getTicsPerSecond(cli.getOptionValue(destTimeUnitOption,"ms"));
 		Long otps = getTicsPerSecond(cli.getOptionValue(origTimeUnitOption,"s"));
 		timeMul = getTimeMul(otps,ticsPerSecond);
 		if ( timeMul == null )
 			throw new HelpException();
 		offset = Long.parseLong(cli.getOptionValue(offsetOption,"0")) * ticsPerSecond;	
 		interval = Long.parseLong(cli.getOptionValue(snapIntervalOption, "60")) * ticsPerSecond; // by default, snap every minute
 		fix_pause_times = cli.hasOption(fixPauseTimesOption);
 	}
 	
 	@Override
 	protected void run() throws IOException, AlreadyExistsException, LoadTraceException {
 		MovementTrace movement = (MovementTrace) _store.newTrace(graph_options.get(GraphOptions.MOVEMENT), MovementTrace.type, force);
 		if ( ext_fmt.is(ExternalFormat.NS2) )
 			NS2Movement.fromNS2(movement, _in, maxTime, timeMul, ticsPerSecond, offset, interval, fix_pause_times);
 		else
 			ONEMovement.fromONE(movement, _in, maxTime, timeMul, ticsPerSecond, offset, interval);
 	}
 
 
 	@Override
 	protected void initOptions() {
 		super.initOptions();
 		graph_options.setOptions(options);
 		ext_fmt.setOptions(options);
		options.addOption(null, maxTimeOption, true, "maximum movement time");
 		options.addOption(null, origTimeUnitOption, true, "time unit of original trace [s, ms, us, ns] (default: s)");
 		options.addOption(null, destTimeUnitOption, true, "time unit of destination trace [s, ms, us, ns] (default: ms)");
 		options.addOption(null, snapIntervalOption, true, "snapshot interval in seconds (default 60)");
 		options.addOption(null, offsetOption, true, "offset to add to all times in seconds (default 0)");
 		options.addOption(null, fixPauseTimesOption, false, "fix missing pause times in NS2");
 	}
 }
