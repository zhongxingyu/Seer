 package de.dhbw.wbs;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 public final class Seminarplanung {
 	private static final SimpleDateFormat lectureTimeFormat = new SimpleDateFormat("hh:mm");
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		HashMap<Integer, Lecture> lectures = new HashMap<Integer, Lecture>();
 		HashMap<Integer, Group> groups = new HashMap<Integer, Group>();
 
 		if (args.length != 3) {
 			System.err.println("Expecting three file names as arguments.");
 			System.exit(1);
 		}
 
 		// 1. Parsen
 		try {
 			/*
 			 * Parse 1st file - lectures
 			 *
 			 * Lecture No;subject;lecturer;group number;duration;room
 			 */
 			BufferedReader lectureReader = new BufferedReader(new FileReader(args[0]));
 			// Skip the header line
 			lectureReader.readLine();
 
 			CSVParser lectureParser = new CSVParser(lectureReader);
 
 			for (String[] elems : lectureParser.parse()) {
 				Lecture lecture = new Lecture();
 
 				lecture.setNumber(Integer.parseInt(elems[0]));
 				lecture.setName(elems[1]);
 				lecture.setLecturer(new Lecturer(elems[2]));
 				lecture.setGroup(new Group(Integer.parseInt(elems[3])));
 				lecture.setDuration(Integer.parseInt(elems[4]));
 				lecture.setRoom(new Room(Integer.parseInt(elems[5])));
 
 				lectures.put(new Integer(lecture.getNumber()), lecture);
 			}
 
 			/*
 			 * Parse 2nd file - lecture dependencies
 			 *
 			 * basic lecture;required lecture;;;;
 			 */
 			BufferedReader depReader = new BufferedReader(new FileReader(args[1]));
 			// Skip the header line
 			depReader.readLine();
 
 			CSVParser depParser = new CSVParser(depReader);
 
 			for (String[] elems : depParser.parse()) {
 				Lecture basicLecture = lectures.get(new Integer(elems[0]));
 				Lecture dependentLecture = lectures.get(new Integer(elems[1]));
 
 				dependentLecture.addRequiredLecture(basicLecture);
 			}
 
 			/*
 			 * Parse 3rd file - time information
 			 * group number;lecture number;start time
 			 */
 			BufferedReader timeReader = new BufferedReader(new FileReader(args[2]));
 			// Skip the header line
 			timeReader.readLine();
 
 			CSVParser timeParser = new CSVParser(timeReader);
 
 			for (String[] elems : timeParser.parse()) {
 				Lecture lecture = lectures.get(new Integer(elems[0]));
 				Group group = groups.get(new Integer(elems[1]));
 
 				if (group != lecture.getGroup()) {
 					System.err.println("Error: The group number for lecture " + elems[1] +
 							" as supplied  in file " + args[1] + " does not match the group " +
 							"number from file " + args[0]);
 
 					System.exit(1);
 				}
 
 				Date startTime = null;
 				try {
 					startTime = lectureTimeFormat.parse(elems[2]);
 				} catch (ParseException exc) {
 					System.err.println("Error: Invalid time format " + elems[2] +
 							" in file " + args[2] + ". Expect hh:mm notation.");
 					System.exit(1);
 				}
 
 				Calendar cal = Calendar.getInstance();
 				cal.setTime(startTime);
 				lecture.setStartTime(cal);
 			}
 		}
 		catch (FileNotFoundException e) {
 			System.err.println("File not found: " + e.getMessage());
 			System.exit(1);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(2);
 		}
 
 
 		// 2. Check consistency
 		/*
 		 *  2.1 A lecture may only take place of the group has already heard all lectures that this
 		 *  lecture depends on
 		 */
		for (Lecture dependentLecture : lectures.values()) {
 			for (Lecture requiredLecture : dependentLecture.getRequiredLectures()) {
 					assertTrue(dependentLecture.getGroup() == requiredLecture.getGroup(),
 							"Lecture " + dependentLecture.getName() + "depends on lecture " +
 							requiredLecture.getName() +
 							", but the two lectures are held in different groups.");
 
 					// TODO
 					assertTrue(true, "Lecture " + dependentLecture.getName() + "depends on lecture " +
 							requiredLecture + ", but this lecture is not taught before the other lecture.");
 			}
 		}
 
 		/*
 		 * 2.2 Lectures of the same group or the same lecturer may not overlap.
 		 *     One room can be used for only one lecture at a given point of time.
 		 */
 		// TODO
 
 		/*
 		 * 2.3 There has to be a break between two lectures of length two for both the lecturerers and the
 		 * seminar group.
 		 */
 		// TODO
 	}
 
 	private static void assertTrue(boolean assertion, String errMsg) {
 		if (!assertion) {
 			System.err.println(errMsg);
 			System.exit(1);
 		}
 	}
 
 }
