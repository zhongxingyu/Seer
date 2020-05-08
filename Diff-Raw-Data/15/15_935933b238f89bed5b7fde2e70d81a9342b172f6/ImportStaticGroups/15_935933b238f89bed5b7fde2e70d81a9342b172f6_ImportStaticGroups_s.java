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
 import java.util.*;
 
 import org.apache.commons.cli.*;
 
 import ditl.*;
 import ditl.Store.*;
 import ditl.WritableStore.AlreadyExistsException;
 import ditl.cli.WriteApp;
 import ditl.graphs.*;
 
 
 public class ImportStaticGroups extends WriteApp {
 
 	private static String labelsOption = "labels";
 	
 	private boolean useLabels;
 	private GraphOptions graph_options = new GraphOptions(GraphOptions.PRESENCE, GraphOptions.GROUPS);
 	private String[] group_specs;
 	
 	public final static String PKG_NAME = "graphs";
 	public final static String CMD_NAME = "import-groups";
 	public final static String CMD_ALIAS = "ig";
 	
 	@Override
 	protected String getUsageString(){
 		return"[OPTIONS] STORE GROUP [GROUP..]";
 	}
 
 	@Override
 	protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
 		super.parseArgs(cli, args);
 		graph_options.parse(cli);
 		group_specs = Arrays.copyOfRange(args,1,args.length);
 		useLabels = cli.hasOption(labelsOption);
 	}
 
 	@Override
 	protected void initOptions() {
 		super.initOptions();
 		graph_options.setOptions(options);
 		options.addOption(null, labelsOption, false, "consider first element of group specification as the group's label");
 	}
 	
 	@Override
 	public void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
 		Trace<?> presence = _store.getTrace(graph_options.get(GraphOptions.PRESENCE));
 		GroupTrace groups = (GroupTrace) _store.newTrace(graph_options.get(GraphOptions.GROUPS), GroupTrace.type, force);
 		StatefulWriter<GroupEvent,Group> groupWriter = groups.getWriter(presence.snapshotInterval()); 
 		Set<Group> initState = new HashSet<Group>();
 		List<String> labels = new LinkedList<String>();
 		int i=0;
 		for ( String spec : group_specs ){
 			Group group = new Group(i);
			String[] ranges = spec.split(":");
 			int j = 0;
 			if ( useLabels ){
 				labels.add(ranges[j]);
 				++j;
 			}
 			while ( j < ranges.length ){
				String[] bounds = ranges[j].split("-");
 				Integer n;
 				if ( bounds.length == 1 ) {
 					n = Integer.parseInt(bounds[0]);
 					group.handleEvent(new GroupEvent(group.gid(), GroupEvent.JOIN, new Integer[]{n}));
 				} else {
 					for ( n = Integer.parseInt(bounds[0]); n<=Integer.parseInt(bounds[1]); ++n){
 						group.handleEvent(new GroupEvent(group.gid(), GroupEvent.JOIN, new Integer[]{n}));
 					}
 				}
 				++j;
 			}
 			initState.add(group);
 			++i;
 		}
 		
 		groupWriter.setInitState(presence.minTime(), initState);
 		groupWriter.setProperty(Trace.maxTimeKey, presence.maxTime());
 		groupWriter.setProperty(Trace.ticsPerSecondKey, presence.ticsPerSecond());
 		
 		if ( useLabels ){
 			StringBuffer buffer = new StringBuffer();
 			Iterator<String> si = labels.iterator();
 			while ( si.hasNext() ){
 				buffer.append(si.next());
 				if ( si.hasNext() )
 					buffer.append(",");
 			}
 			groupWriter.setProperty(GroupTrace.labelsKey, buffer.toString());
 		}
 		
 		groupWriter.close();		
 	}
 }
