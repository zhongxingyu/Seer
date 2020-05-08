 package de.dhbw.wbs;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
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
 		ArrayList<Lecturer> lecturers = new ArrayList<Lecturer>();
 		ArrayList<Room> rooms = new ArrayList<Room>();
 
 		if (args.length != 3) {
 			System.err.println("Expecting three file names as arguments.");
 			System.exit(1);
 		}
 
 		String lectureFileName = args[0];
 		String depFileName = args[1];
 		String timeFileName = args[2];
 
 		// 1. Parsen
 		try {
 			/*
 			 * Parse 1st file - lectures
 			 *
 			 * Lecture No;subject;lecturer;group number;duration;room
 			 */
 			BufferedReader lectureReader = new BufferedReader(new FileReader(lectureFileName));
 			// Skip the header line
 			lectureReader.readLine();
 
 			CSVParser lectureParser = new CSVParser(lectureReader);
 
 			for (String[] elems : lectureParser.parse()) {
 				Lecture lecture = new Lecture();
 
 				Integer groupNumber = new Integer(elems[3]);
 				Group group = groups.get(groupNumber);
 				if (group == null) {
 					group = new Group(groupNumber);
 					groups.put(groupNumber, group);
 				}
 				lecture.setGroup(group);
 
 				Lecturer lecturer = new Lecturer(elems[2]);
 				lecture.setLecturer(lecturer);
 				lecturers.add(lecturer);
 
 				Room room = new Room(Integer.parseInt(elems[5]));
 				lecture.setRoom(room);
 				rooms.add(room);
 
 				lecture.setNumber(Integer.parseInt(elems[0]));
 				lecture.setName(elems[1]);
 				lecture.setDuration(Integer.parseInt(elems[4]));
 
 				lectures.put(new Integer(lecture.getNumber()), lecture);
 			}
 
 			/*
 			 * Parse 2nd file - lecture dependencies
 			 *
 			 * basic lecture;required lecture;;;;
 			 */
 			BufferedReader depReader = new BufferedReader(new FileReader(depFileName));
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
 			BufferedReader timeReader = new BufferedReader(new FileReader(timeFileName));
 			// Skip the header line
 			timeReader.readLine();
 
 			CSVParser timeParser = new CSVParser(timeReader);
 
 			for (String[] elems : timeParser.parse()) {
 				Lecture lecture = lectures.get(new Integer(elems[1]));
 				Group group = groups.get(new Integer(elems[0]));
 
 
 				if (group != lecture.getGroup()) {
 					System.err.println("Error: The group number for lecture " + elems[1] +
 							"(" + lecture.getName() + ") as supplied  in file " + timeFileName + " does not match the group " +
 							"number from file " + lectureFileName);
 
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
 			if (dependentLecture.isTakingPlace()) {
 				for (Lecture requiredLecture : dependentLecture.getRequiredLectures()) {
 					assertTrue(dependentLecture.getGroup() == requiredLecture.getGroup(),
 							"Lecture " + dependentLecture.getName() + "depends on lecture " +
 							requiredLecture.getName() +
 							", but the two lectures are held in different groups.");
 
 					if (!requiredLecture.isTakingPlace()) {
 						abort("Lecture " + dependentLecture + " depends on lecture " +
 											requiredLecture + ", but this lecture is not taught at all.");
 					}
 					else {
 						AllenRelation allenRelation = AllenRelation.getAllenRelation(requiredLecture.getTimeSpan(),
 								dependentLecture.getTimeSpan());
 						switch (allenRelation) {
 						case BEFORE:
 						case MEETS:
 							break;
 						default:
 							abort("Lecture " + dependentLecture + " depends on lecture " +
 									requiredLecture + ", but this lecture is not taught before the other lecture\n" +
 									"(Allen relation between required and dependent lecture is " + allenRelation.name() + ")");
 						}
 					}
 				}
 			}
 		}
 
 		/*
 		 * 2.2 Lectures of the same group or the same lecturer may not overlap.
 		 *     One room can be used for only one lecture at a given point of time.
 		 */
 
 		for (Lecturer lecturer : lecturers) {
 			final Lecturer lecturerCopy = lecturer;
 			Predicate<Lecture> lecturerPredicate = new Predicate<Lecture>() {
 				@Override
 				public boolean matches(Lecture aLecture) {
 					return aLecture.getLecturer().equals(lecturerCopy);
 				}
 			};
 
			checkForOverlap(lecturerPredicate.filter(lectures.values()));
 		}
 
 		for (Group group : groups.values()) {
 			final Group groupCopy = group;
 			Predicate<Lecture> groupPredicate = new Predicate<Lecture>() {
 				@Override
 				public boolean matches(Lecture aLecture) {
 					return aLecture.getGroup().equals(groupCopy);
 				}
 			};
 
			checkForOverlap(groupPredicate.filter(lectures.values()));
 		}
 
 		for (Room room : rooms) {
 			final Room roomCopy = room;
 			Predicate<Lecture> roomPredicate = new Predicate<Lecture>() {
 				@Override
 				public boolean matches(Lecture aLecture) {
 					return aLecture.getRoom().equals(roomCopy);
 				}
 			};
 
			checkForOverlap(roomPredicate.filter(lectures.values()));
 		}
 
 		/*
 		 * 2.3 There has to be a break between two lectures of length two for both the lecturerers and the
 		 * seminar group.
 		 */
 		// TODO
 
 		System.out.println("All checks passed. File appears to be valid.");
 		System.exit(0);
 	}
 
	private static void checkForOverlap(ArrayList<Lecture> lectures) {
 		for (Lecture lecture1 : lectures) {
 			for (Lecture lecture2 : lectures) {
 				if (lecture1 == lecture2)
 					continue;
 
 				if (lecture1.overlapsWith(lecture2))
					abort("Lecture " + lecture1.getName() + " overlaps with lecture " + lecture2.getName() + "\n");
 			}
 		}
 	}
 
 	private static void abort(String errMsg) {
 		System.err.println(errMsg);
 		System.err.println("Abort.");
 		System.exit(1);
 	}
 
 	private static void assertTrue(boolean assertion, String errMsg) {
 		if (!assertion)
 			abort(errMsg);
 	}
 
 }
