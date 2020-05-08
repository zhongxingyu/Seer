 package org.andrill.conop.search;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
 
 /**
  * Represents a CONOP run.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class Run {
 
 	/**
 	 * Load a CONOP9 run from the specified directory. This assumes the standard
 	 * filenames of sections.sct, events.evt, and loadfile.dat.
 	 * 
 	 * @param dir
 	 *            the run directory.
 	 * @return the run.
 	 */
 	public static Run loadCONOP9Run(final File dir) {
 		return loadCONOP9Run(new File(dir, "sections.sct"), new File(dir, "events.evt"), new File(dir, "loadfile.dat"),
 				false);
 	}
 
 	/**
 	 * Load a CONOP9 run from the specified directory. This assumes the standard
 	 * filenames of sections.sct, events.evt, and loadfile.dat.
 	 * 
 	 * @param dir
 	 *            the run directory.
 	 * @param overrideWeights
 	 *            override the specified weights.
 	 * @return the run.
 	 */
 	public static Run loadCONOP9Run(final File dir, final boolean overrideWeights) {
 		return loadCONOP9Run(new File(dir, "sections.sct"), new File(dir, "events.evt"), new File(dir, "loadfile.dat"),
 				overrideWeights);
 	}
 
 	/**
 	 * Load a CONOP9 run from the specified files.
 	 * 
 	 * @param sectionFile
 	 *            the sections file.
 	 * @param eventFile
 	 *            the events file.
 	 * @param loadFile
 	 *            the observations/load file.
 	 * @param overrideWeights
 	 *            override the specified weights.
 	 * @return the run.
 	 */
 	public static Run loadCONOP9Run(final File sectionFile, final File eventFile, final File loadFile,
 			final boolean overrideWeights) {
 
 		// parse section names
 		Map<String, String> sectionNames = Maps.newHashMap();
 		for (List<String> row : parse(sectionFile)) {
 			sectionNames.put(row.get(0), row.get(3));
 		}
 
 		// parse event names
 		Map<String, String> eventNames = Maps.newHashMap();
 		for (List<String> row : parse(eventFile)) {
 			eventNames.put(row.get(0), row.get(2));
 		}
 
 		// parse our load file
 		Map<String, Event> events = Maps.newHashMap();
 		Multimap<String, Observation> observations = HashMultimap.create();
 		for (List<String> row : parse(loadFile)) {
 			String id = row.get(0);
 			String type = row.get(1);
 			String section = row.get(2);
 			BigDecimal level = new BigDecimal(row.get(3));
 			double weightUp = Double.parseDouble(row.get(6));
 			double weightDn = Double.parseDouble(row.get(7));
 			String key = id + "_" + type;
 			Event event = events.get(key);
 			if (event == null) {
 				String name = eventNames.get(id).replaceAll("  ", " ");
 				if (type.equals("1")) {
 					Event lad = Event.createPaired(name + " LAD", name + " FAD");
 					event = lad.getBeforeConstraint();
 					events.put(id + "_2", lad);
 				} else if (type.equals("3")) {
 					event = new Event(name + " MID", events.get(id + "_1"), events.get(id + "_2"));
 				} else if (type.equals("4")) {
 					event = new Event(name + " ASH");
 				} else if (type.equals("5")) {
 					event = new Event(name + " AGE");
 				}
 				events.put(key, event);
 			}
 
 			// override weights
			if (overrideWeights && "1".equals(type)) {
				weightUp = 1000000;
			} else if (overrideWeights && "2".equals(type)) {
				weightDn = 1000000;
			} else if ("4".equals(type) || "5".equals(type)) {
				weightUp = 1000000;
				weightDn = 1000000;
 			}
 
 			// check for uniqueness
 			boolean unique = true;
 			for (Observation o : observations.get(section)) {
 				if (event.equals(o.getEvent())) {
 					System.err.println("Event " + o.getEvent().getName() + " (" + id + ") is not unique in section "
 							+ section);
 					unique = false;
 				}
 			}
 			if (unique) {
 				observations.put(section, new Observation(event, level, weightUp, weightDn));
 			}
 		}
 
 		// create all our sections
 		List<Section> sections = Lists.newArrayList();
 		for (String key : observations.keySet()) {
 			sections.add(new Section(sectionNames.get(key), Lists.newArrayList(observations.get(key))));
 		}
 
 		// create the run
 		return new Run(sections);
 	}
 
 	/**
 	 * Parse the specified CONOP formatted file.
 	 * 
 	 * @param file
 	 *            the file.
 	 * @return the parsed lines.
 	 */
 	protected static List<List<String>> parse(final File file) {
 		if (!file.exists()) {
 			throw new IllegalArgumentException(file.getAbsolutePath() + " does not exist!");
 		}
 		List<List<String>> parsed = Lists.newArrayList();
 		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				List<String> row = parseLine(line.trim());
 				if (row.size() > 0) {
 					parsed.add(row);
 				}
 			}
 		} catch (IOException e) {
 			// do nothing
 		}
 		return parsed;
 	}
 
 	/**
 	 * Parse the specified line.
 	 * 
 	 * @param line
 	 *            the line.
 	 * @return the parsed line.
 	 */
 	protected static List<String> parseLine(final String line) {
 		List<String> list = new ArrayList<String>();
 		boolean inQuote = false;
 		StringBuilder buffer = new StringBuilder();
 		for (int i = 0; i < line.length(); i++) {
 			char c = line.charAt(i);
 			if ((c == '\'') || (c == '"')) {
 				if (inQuote) {
 					list.add(buffer.toString());
 					inQuote = false;
 				} else {
 					inQuote = true;
 				}
 				buffer = new StringBuilder();
 			} else if ((c == ' ') || (c == '\t')) {
 				if (inQuote) {
 					buffer.append(c);
 				} else if (!"".equals(buffer.toString())) {
 					list.add(buffer.toString());
 					buffer = new StringBuilder();
 				} else {
 					buffer = new StringBuilder();
 				}
 			} else {
 				buffer.append(c);
 			}
 		}
 		if (!"".equals(buffer.toString())) {
 			list.add(buffer.toString());
 		}
 		return list;
 	}
 
 	protected final ImmutableSet<Event> events;
 	protected CoexistenceMatrix matrix = null;
 	protected final ImmutableSet<Section> sections;
 
 	/**
 	 * Create a new run from the specified sections.
 	 * 
 	 * @param list
 	 *            the sections.
 	 */
 	public Run(final List<Section> list) {
 		sections = ImmutableSet.copyOf(list);
 
 		// build our immutable set of events
 		Builder<Event> b = ImmutableSet.builder();
 		for (Section s : sections) {
 			b.addAll(s.getEvents());
 		}
 		events = b.build();
 	}
 
 	/**
 	 * Gets the coexistence matrix for this run.
 	 * 
 	 * @return the coexistence matrix.
 	 */
 	public CoexistenceMatrix getCoexistenceMatrix() {
 		if (matrix == null) {
 			matrix = new CoexistenceMatrix(this);
 		}
 		return matrix;
 	}
 
 	/**
 	 * Gets all events in this run.
 	 * 
 	 * @return the events.
 	 */
 	public ImmutableSet<Event> getEvents() {
 		return events;
 	}
 
 	/**
 	 * Gets all sections in this run.
 	 * 
 	 * @return the sections.
 	 */
 	public ImmutableSet<Section> getSections() {
 		return sections;
 	}
 }
