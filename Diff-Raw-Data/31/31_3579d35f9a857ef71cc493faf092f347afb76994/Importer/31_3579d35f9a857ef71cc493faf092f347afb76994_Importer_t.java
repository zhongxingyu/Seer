 /*
  * This file is part of SchedVis.
  * 
  * SchedVis is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * SchedVis is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * SchedVis. If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * 
  */
 package cz.muni.fi.spc.SchedVis.background;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import javax.swing.SwingWorker;
 
import org.apache.log4j.Logger;

 import cz.muni.fi.spc.SchedVis.model.entities.Event;
 import cz.muni.fi.spc.SchedVis.model.entities.EventType;
 import cz.muni.fi.spc.SchedVis.model.entities.Machine;
 import cz.muni.fi.spc.SchedVis.parsers.ParseException;
 import cz.muni.fi.spc.SchedVis.parsers.machines.MachineData;
 import cz.muni.fi.spc.SchedVis.parsers.machines.MachinesParser;
 import cz.muni.fi.spc.SchedVis.parsers.schedule.EventHasData;
 import cz.muni.fi.spc.SchedVis.parsers.schedule.EventIsJobRelated;
 import cz.muni.fi.spc.SchedVis.parsers.schedule.EventIsMachineRelated;
 import cz.muni.fi.spc.SchedVis.parsers.schedule.ScheduleEvent;
 import cz.muni.fi.spc.SchedVis.parsers.schedule.ScheduleEventIO;
 import cz.muni.fi.spc.SchedVis.parsers.schedule.ScheduleEventMove;
 import cz.muni.fi.spc.SchedVis.parsers.schedule.ScheduleJobData;
 import cz.muni.fi.spc.SchedVis.parsers.schedule.ScheduleMachineData;
 import cz.muni.fi.spc.SchedVis.parsers.schedule.ScheduleParser;
 import cz.muni.fi.spc.SchedVis.util.Database;
 
 /**
  * A tool to import data from specific files into the SQL database used by this
  * application.
  * 
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  * 
  */
 public final class Importer extends SwingWorker<Void, Void> {
 
 	private final File machinesFile;
 
 	private final Integer machinesLineCount;
 	private final File dataFile;
 	private final Integer dataLineCount;
 	private Integer parsedLines = 0;
 
 	private Integer totalLines = 0;
 	private boolean result = false;
 
 	private final Map<Integer, Event> allJobs = new HashMap<Integer, Event>();
 
 	private final Map<String, String[]> CPUstatus = new HashMap<String, String[]>();
 
 	/**
 	 * A constructor to the class.
 	 * 
 	 * @param machinesFile
 	 *          A file to read the list of available machines from.
 	 * @param dataFile
 	 *          A file to read the list of events from.
 	 */
 	public Importer(final File machinesFile, final File dataFile) {
 		this.machinesFile = machinesFile;
 		this.machinesLineCount = this.countLines(machinesFile);
 		this.dataFile = dataFile;
 		this.dataLineCount = this.countLines(dataFile);
 	}
 
 	/**
 	 * Calculate the number of lines in a given file.
 	 * 
 	 * @param file
 	 *          The file to count the lines in.
 	 * @return Number of lines in a file.
 	 */
 	private Integer countLines(final File file) {
 		try {
 			final LineNumberReader reader = new LineNumberReader(new FileReader(file));
 			Integer count = 0;
 			while (reader.readLine() != null) {
 				count++;
 			}
 			return count;
 		} catch (final FileNotFoundException e) {
 			return 0;
 		} catch (final IOException e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * The method that is executed when this SwingWorker is executed. Handles all
 	 * the reading, parsing and storing in the database of all the required data.
 	 */
 	@Override
 	public Void doInBackground() {
 		if (!this.machinesFile.canRead() || !this.dataFile.canRead()) {
 			return null;
 		}
 		try {
 			this.parseMachines(new BufferedReader(new FileReader(this.machinesFile)));
 			this.parseDataSet(new BufferedReader(new FileReader(this.dataFile)));
 		} catch (final FileNotFoundException e) {
 			return null;
 		} catch (final ParseException e) {
 			return null;
 		}
 		this.result = true;
 		return null;
 	}
 
 	/**
 	 * Whether or not the task finished and succeeded.
 	 * 
 	 * @return True when the task is over and successful, false otherwise.
 	 */
 	public boolean isSuccess() {
 		return this.isDone() && this.result;
 	}
 
 	/**
 	 * Update progress of the overall task when the next line has been parsed.
 	 */
 	public void nextLineParsed() {
 		this.parsedLines++;
 		final Double progress = (this.parsedLines * 100)
 		    / (double) (this.totalLines + 1);
 		if (progress > 100) {
 			this.setProgress(100);
 		} else if (progress < 0) {
 			this.setProgress(0);
 		} else {
 			this.setProgress(progress.intValue());
 		}
 	}
 
 	/**
 	 * Parse the available events and insert the data into database, each line of
 	 * which looks as defined in the grammar.
 	 * 
 	 * @param reader
 	 *          Reader pointing to the data set to parse.
 	 * @throws ParseException
 	 *           When, for some reason, the file cannot be parsed. Might indicate
 	 *           a syntax error or, less possibly, JavaCC bug.
 	 */
 	private void parseDataSet(final BufferedReader reader) throws ParseException {
 		this.setProgress(0);
 		final Map<String, Integer> eventTypes = new HashMap<String, Integer>();
 		eventTypes.put("job-arrival", EventType.EVENT_JOB_ARRIVAL);
 		eventTypes.put("job-execution-start", EventType.EVENT_JOB_EXECUTION_START);
 		eventTypes.put("job-cancel", EventType.EVENT_JOB_CANCEL);
 		eventTypes.put("good-move", EventType.EVENT_JOB_MOVE_GOOD);
 		eventTypes.put("bad-move", EventType.EVENT_JOB_MOVE_BAD);
 		eventTypes.put("machine-failure", EventType.EVENT_MACHINE_FAILURE);
 		eventTypes.put("machine-failure-move-good",
 		    EventType.EVENT_MACHINE_FAILURE_JOB_MOVE_GOOD);
 		eventTypes.put("machine-failure-move-bad",
 		    EventType.EVENT_MACHINE_FAILURE_JOB_MOVE_BAD);
 		eventTypes.put("machine-restart", EventType.EVENT_MACHINE_RESTART);
 		eventTypes.put("machine-restart-move-good",
 		    EventType.EVENT_MACHINE_RESTART_JOB_MOVE_GOOD);
 		eventTypes.put("machine-restart-move-bad",
 		    EventType.EVENT_MACHINE_RESTART_JOB_MOVE_BAD);
 		eventTypes.put("job-completion", EventType.EVENT_JOB_COMPLETION);
 		final Iterator<Map.Entry<String, Integer>> eventTypeIterator = eventTypes
 		    .entrySet().iterator();
 		final List<EventType> etl = new Vector<EventType>();
 		while (eventTypeIterator.hasNext()) {
 			final EventType et = new EventType();
 			final Map.Entry<String, Integer> item = eventTypeIterator.next();
 			et.setId(item.getValue());
 			et.setName(item.getKey());
 			etl.add(et);
 		}
 		Database.persist(etl);
 		try {
 			// parse data set
 			this.parsedLines = 0;
 			this.totalLines = this.dataLineCount;
 			final ScheduleParser parser = new ScheduleParser(reader);
 			parser.setImporter(this);
 			final List<ScheduleEvent> events;
 			try {
 				events = parser.read();
 			} catch (Throwable e) {
 				e.printStackTrace();
 				throw new ParseException();
 			}
 			// fill the event's table
 			final Integer totalEvents = events.size();
 			Integer lineId = 0;
 			Integer eventId = 0;
 			Integer previousClock = -1;
 			Integer virtualClock = 0;
 			Set<Integer> startedJobs = new TreeSet<Integer>();
 			Database.getEntityManager().getTransaction().begin();
 			for (final ScheduleEvent event : events) {
 				if (!previousClock.equals(Integer.valueOf(event.getClock()))) {
 					/*
 					 * here goes a brand new clock. reset any counters, increase virtual
 					 * value
 					 */
 					virtualClock++;
 					previousClock = event.getClock();
 					startedJobs.clear();
 				}
 				lineId++;
 				eventId++;
 				final Event evt = new Event();
 				evt.setType(EventType.get(eventTypes.get(event.getName())));
 				evt.setClock(event.getClock());
 				Integer jobHint = Event.JOB_HINT_NONE;
 				String usedCPUs = null;
 				if (event instanceof EventIsJobRelated) {
 					evt.setJob(((EventIsJobRelated) event).getJob());
 					if (event.getName().equals("job-arrival")) {
 						// if in this clock a new job arrives, remember it
 						startedJobs.add(evt.getJob().intValue());
 						jobHint = Event.JOB_HINT_ARRIVAL;
 					} else if (event.getName().equals("job-execution-start")) {
 						usedCPUs = this.processUsedCPUs((ScheduleEventIO) event);
 						/*
 						 * if in the same clock the newly arrived job is executed, increase
 						 * the virtual clock. this way, we extend the schedule to show this
 						 * rather important change.
 						 */
 						if (startedJobs.contains(evt.getJob().intValue())) {
 							virtualClock++;
 							startedJobs.remove(evt.getJob().intValue());
 						}
 					} else if (event.getName().equals("good-move")
 					    || event.getName().equals("machine-failure-move-good")
 					    || event.getName().equals("machine-restart-move-good")) {
 						jobHint = Event.JOB_HINT_MOVE_OK;
 					} else if (event.getName().equals("bad-move")
 					    || event.getName().equals("machine-failure-move-bad")
 					    || event.getName().equals("machine-restart-move-bad")) {
 						jobHint = Event.JOB_HINT_MOVE_NOK;
 					} else if (event.getName().equals("job-completion")) {
 						usedCPUs = this.processUsedCPUs((ScheduleEventIO) event);
 					}
 				}
 				evt.setVirtualClock(virtualClock);
 				if (event instanceof EventIsMachineRelated) {
 					evt.setSourceMachine(Machine.getWithName(
 					    ((EventIsMachineRelated) event).getMachine(), true));
 					if (event instanceof ScheduleEventMove) {
 						evt.setTargetMachine(Machine.getWithName(
 						    ((ScheduleEventMove) event).getTargetMachine(), true));
 					}
 				}
 				evt.setBringsSchedule(event instanceof EventHasData);
 				Database.persist(evt);
 				if (event instanceof EventHasData) {
 					for (final ScheduleMachineData machine : ((EventHasData) event)
 					    .getData()) {
 						eventId++;
 						/*
 						 * And now post the real schedule.
 						 */
 						for (final ScheduleJobData job : machine.getJobs()) {
 							final Event evt2 = new Event();
 							evt2.setBringsSchedule(true);
 							evt2.setClock(event.getClock());
 							evt2.setVirtualClock(evt.getVirtualClock());
 							evt2.setSourceMachine(Machine.getWithName(machine.getMachineId(),
 							    true));
 							evt2.setNeededCPUs(job.getNeededCPUs());
 							evt2.setAssignedCPUs(job.getAssignedCPUs());
 							evt2.setNeededPlatform(job.getArch());
 							evt2.setNeededRAM(job.getNeededMemory());
 							evt2.setNeededHDD(job.getNeededSpace());
 							evt2.setDeadline(job.getDeadline());
 							evt2.setExpectedStart(job.starts());
 							evt2.setExpectedEnd(job.ends());
 							evt2.setJob(job.getId());
 							evt2.setParent(evt);
 							if (job.getId() == ((EventIsJobRelated) event).getJob()) {
 								evt2.setJobHint(jobHint);
 							}
 							this.allJobs.put(job.getId(), evt2);
 							Database.persist(evt2);
 						}
 						/*
 						 * Create a "dummy" event so that we know that the given machine
 						 * posted some schedule. This is used to tell when the schedule is
 						 * empty. (A "dummy" is found but the real schedule is not.)
 						 */
 						final Event evt3 = new Event();
 						evt3.setClock(event.getClock());
 						evt3.setSourceMachine(Machine.getWithName(machine.getMachineId(),
 						    true));
 						evt3.setVirtualClock(evt.getVirtualClock());
 						evt3.setAssignedCPUs(usedCPUs);
 						evt3.setBringsSchedule(false);
 						evt3.setParent(evt);
 						Database.persist(evt3);
 					}
 				}
 				// update progress
 				final Double progress = (((lineId * 100) / (double) totalEvents) / 2) + 50;
 				this.setProgress(progress.intValue());
 				if (eventId % 1000 == 0) { // persist some items
 					try {
 						Database.getEntityManager().getTransaction().commit();
 						Database.getEntityManager().clear(); // save some memory
 						Database.getEntityManager().getTransaction().begin();
 					} catch (Throwable e) {
 						e.printStackTrace();
 					}
 				}
 				event.clear(); // save some more memory
 			}
 			Database.getEntityManager().getTransaction().commit();
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 	}
 
 	/**
 	 * Parse the machines' input file, each line of which looks as defined in the
 	 * grammar.
 	 * 
 	 * @param reader
 	 *          Reader pointing to the data set to parse.
 	 * @throws ParseException
 	 *           When, for some reason, the file cannot be parsed. Might indicate
 	 *           a syntax error or, less possibly, JavaCC bug.
 	 */
 	private void parseMachines(final BufferedReader reader) throws ParseException {
 		// ready the parser
 		this.parsedLines = 0;
 		this.totalLines = this.machinesLineCount;
 		final MachinesParser parser = new MachinesParser(reader);
 		parser.setImporter(this);
 		List<MachineData> machines = null;
 		try {
 			machines = parser.read();
 		} catch (Throwable e) {
 			e.printStackTrace();
 			throw new ParseException();
 		}
 		// fill the machines' table
 		final Integer totalMachines = machines.size();
 		Integer machineId = 0;
 		final List<Machine> machinesList = new Vector<Machine>();
 		for (final MachineData machine : machines) {
 			machineId++;
 			// persist data
 			final Machine mcn = new Machine();
 			mcn.setName(machine.getName());
 			mcn.setCPUs(machine.getCPUCount());
 			mcn.setSpeed(machine.getSpeed());
 			mcn.setOS(machine.getOperatingSystem());
 			mcn.setPlatform(machine.getArchitecture());
 			mcn.setHDD(machine.getSpace());
 			mcn.setRAM(machine.getMemory());
 			machinesList.add(mcn);
 			// update progress
 			final Double progress = ((machineId * 100) / (double) totalMachines) / 2;
 			this.setProgress(progress.intValue());
 		}
 		Database.persist(machinesList);
 	}
 
 	private String processUsedCPUs(final ScheduleEventIO e) {
 		int jobId = e.getJob();
 		String[] jobCPUs;
 		try {
 			jobCPUs = this.allJobs.get(jobId).getAssignedCPUs().split(",");
 		} catch (Exception ex) {
 			jobCPUs = new String[] {};
 		}
 		String machineId = this.allJobs.get(jobId).getSourceMachine().getName();
 		if (e.getName().equals("job-execution-start")) {
 			// execution starting
			if (!this.CPUstatus.containsKey(machineId)) {
 				this.CPUstatus.put(machineId, jobCPUs);
 			} else {
 				Set<String> old = new HashSet<String>(Arrays.asList(this.CPUstatus
 				    .get(machineId)));
				boolean isChanged = old.addAll(Arrays.asList(jobCPUs));
				if (!isChanged) {
					Logger
					    .getLogger(Importer.class)
					    .warn(
					        "Job execution (#"
					            + jobId
					            + " at "
					            + machineId
					            + ") didn't occupy any unused CPUs. Probably a bug in the data set.");
				}
 				this.CPUstatus.remove(machineId);
 				this.CPUstatus.put(machineId, old.toArray(new String[] {}));
 			}
 		} else {
 			// execution finished
 			Set<String> old = new TreeSet<String>(Arrays.asList(this.CPUstatus
 			    .get(machineId)));
			boolean isChanged = old.removeAll(Arrays.asList(jobCPUs));
			if (!isChanged) {
				Logger
				    .getLogger(Importer.class)
				    .warn(
				        "Job completion (#"
				            + jobId
				            + " at "
				            + machineId
				            + ") didn't free any used CPUs. Probably a bug in the data set.");
			}
 			this.CPUstatus.remove(machineId);
 			this.CPUstatus.put(machineId, old.toArray(new String[] {}));
 			this.allJobs.remove(jobId);
 		}
 		StringBuilder sb = new StringBuilder();
 		String[] CPUs = this.CPUstatus.get(machineId);
 		for (int i = 0; i < CPUs.length; i++) {
 			sb.append(CPUs[i]);
 			if (i < (CPUs.length - 1)) {
 				sb.append(",");
 			}
 		}
 		return sb.toString();
 	}
 }
