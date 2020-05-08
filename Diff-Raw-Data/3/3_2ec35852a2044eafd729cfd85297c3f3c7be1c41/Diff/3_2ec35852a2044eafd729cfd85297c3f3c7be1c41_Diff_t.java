 package org.andrill.conop.analysis;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Map.Entry;
 
import org.andrill.conop.search.Event;
import org.andrill.conop.search.Run;

 import com.google.common.collect.Maps;
 
 /**
  * Compares two solutions and generates a difference spreadsheet.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class Diff {
 
 	static class Rank {
 		int max;
 		int min;
 		int rank;
 	}
 
 	private static Event find(final Run run, final String col1, final String col2) {
 		String name = col1;
 		if (col1.startsWith("'") || col1.startsWith("\"")) {
 			name = name.substring(1, name.length() - 1);
 		}
 		try {
 			Integer.parseInt(col2);
 		} catch (NumberFormatException e) {
 			String type = col2;
 			if (col2.startsWith("'") || col2.startsWith("\"")) {
 				type = col2.substring(1, col2.length() - 1);
 			}
 			if ("LAD".equals(type) || "FAD".equals(type) || "MID".equals(type)) {
 				name = name + " " + type;
 			}
 		}
 		for (Event e : run.getEvents().asList()) {
 			if (name.equalsIgnoreCase(e.getName())) {
 				return e;
 			}
 		}
 		System.out.println("No match for " + name);
 		return null;
 	}
 
 	/**
 	 * Load a solution from a CSV file.
 	 * 
 	 * @param run
 	 *            the run.
 	 * @param csv
 	 *            the CSV file.
 	 * @return the solution.
 	 */
 	public static Map<Event, Rank> fromCSV(final Run run, final File csv) {
 		String line = null;
 		Map<Event, Rank> map = Maps.newLinkedHashMap();
 		try (BufferedReader reader = new BufferedReader(new FileReader(csv))) {
 			reader.readLine(); // eat header line
 			while (((line = reader.readLine()) != null) && (map.size() < run.getEvents().size())) {
 				String[] split = line.split("\t");
 				Event e = find(run, split[0], split[1]);
 				Rank rank = new Rank();
 				int i = 1;
 				try {
 					rank.rank = Integer.parseInt(split[i++]);
 				} catch (NumberFormatException nfe) {
 					rank.rank = Integer.parseInt(split[i++]);
 				}
 				rank.min = Integer.parseInt(split[i++]);
 				rank.max = Integer.parseInt(split[i]);
 				map.put(e, rank);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return map;
 	}
 
 	/**
 	 * Runs the difference calculator.
 	 * 
 	 * @param args
 	 *            the args.
 	 */
 	public static void main(final String[] args) {
 		// load our run
 		Run run = Run.loadCONOP9Run(new File(args[0]));
 
 		// load our first solution
 		Map<Event, Rank> s1 = fromCSV(run, new File(args[1]));
 
 		// load our second solution
 		Map<Event, Rank> s2 = fromCSV(run, new File(args[2]));
 
 		// write out the
 		try (BufferedWriter writer = new BufferedWriter(new FileWriter(args[3]))) {
 
 			// write out the difference sheet
 			writer.write("Event\tRank1\tMin1\tMax1\tRank2\tMin2\tMax2\tOverlap");
 			for (int i = 0; i < s1.size(); i++) {
 				writer.write("\t" + (s1.size() - i));
 			}
 			writer.write("\n");
 			for (Entry<Event, Rank> e : s1.entrySet()) {
 				Event evt = e.getKey();
 				Rank r1 = e.getValue();
 				int min1 = Math.min(r1.min, r1.max);
 				int max1 = Math.max(r1.min, r1.max);
 				Rank r2 = s2.get(evt);
 				int min2 = Math.min(r2.min, r2.max);
 				int max2 = Math.max(r2.min, r2.max);
 				boolean overlaps = (min1 <= max2) && (min2 <= max1);
 				writer.write("'" + evt.getName() + "'\t" + r1.rank + "\t" + min1 + "\t" + max1 + "\t" + r2.rank + "\t"
 						+ min2 + "\t" + max2 + "\t" + overlaps);
 				for (int i = 0; i < s1.size(); i++) {
 					writer.write("\t");
 					int r = s1.size() - i;
 					if ((r >= min1) && (r <= max1) && (r >= min2) && (r <= max2)) {
 						writer.write("*");
 					} else if ((r >= min1) && (r <= max1)) {
 						writer.write("1");
 					} else if ((r >= min2) && (r <= max2)) {
 						writer.write("2");
 					}
 				}
 				writer.write("\n");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
