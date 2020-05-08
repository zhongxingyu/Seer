 package net.kevxu.purdueassist.ui;
 
 import java.io.IOException;
 import java.util.Formatter;
 
 import net.kevxu.purdueassist.course.ScheduleDetail;
 import net.kevxu.purdueassist.course.ScheduleDetail.OnScheduleDetailFinishedListener;
 import net.kevxu.purdueassist.course.ScheduleDetail.ScheduleDetailEntry;
 import net.kevxu.purdueassist.course.shared.CourseNotFoundException;
 import net.kevxu.purdueassist.course.shared.HttpParseException;
 import net.kevxu.purdueassist.course.shared.Predefined.Term;
 
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 public class CommandLine {
 
 	public static final String VERSION = "0.1.2";
 
 	public static void main(String[] args) {
 		Options options = new Options();
 		options.addOption("s", "slient", false, "Do not print anything.");
 		options.addOption("t", "term", true, "Specify school term.");
 
 		CommandLineParser parser = new GnuParser();
 		org.apache.commons.cli.CommandLine cmd;
 		try {
 			cmd = parser.parse(options, args);
 
			String termString = cmd.getOptionValue("t");
			final Term term = parseTerm(termString);
 			final boolean silent = cmd.hasOption("s");
 			final String[] crns = cmd.getArgs();
 
 			for (final String crnString : crns) {
 				final int crn = Integer.valueOf(crnString);
 				ScheduleDetail detail = new ScheduleDetail(term, crn,
 						new OnScheduleDetailFinishedListener() {
 
 							@Override
 							public void onScheduleDetailFinished(
 									CourseNotFoundException e) {
 								if (!silent) {
 									System.out.println("INPUT: " + crnString
 											+ " " + term);
 									System.out.println("Course Not Found!");
 									System.out.println();
 								}
 							}
 
 							@Override
 							public void onScheduleDetailFinished(
 									HttpParseException e) {
 								if (!silent) {
 									System.out.println("INPUT: " + crnString
 											+ " " + term);
 									System.out.println("Parse Error!");
 									System.out.println();
 								}
 							}
 
 							@Override
 							public void onScheduleDetailFinished(IOException e) {
 								if (!silent) {
 									System.out.println("INPUT: " + crnString
 											+ " " + term);
 									System.out.println("IO Error!");
 									System.out.println();
 								}
 							}
 
 							@Override
 							public void onScheduleDetailFinished(
 									ScheduleDetailEntry entry) {
 								if (!silent) {
 									System.out.println("INPUT: " + crnString
 											+ " " + term);
 									System.out.println(entry);
 									System.out.println();
 								}
 							}
 						});
 				detail.getResult();
 			}
 
 		} catch (ParseException e) {
 			System.err.println("Command line arguments parsing failed. Reason: " + e.getMessage());
 		}
 	}
 
 	private static void printHelp() {
 		final String help = "Purdue Course Parser %s\n"
 				+ "Usage: java RemainSeats.jar [term] [crn1, crn2, ...]\n"
 				+ "term - full name (without space) for school term. i.e. fall2012\n"
 				+ "crn - crn number of class\n";
 		Formatter formatter = new Formatter();
 		System.err.print(formatter.format(help, VERSION).toString());
 		formatter.close();
 	}
 
 	private static Term parseTerm(String termString) {
 		return Term.valueOf(termString.toUpperCase());
 	}
 
 }
