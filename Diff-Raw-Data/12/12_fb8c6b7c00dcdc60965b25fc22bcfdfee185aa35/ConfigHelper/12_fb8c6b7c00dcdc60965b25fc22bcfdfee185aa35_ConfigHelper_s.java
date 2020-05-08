 package com.caramelcode.mcstudent.forge;
 
 import java.io.File;
 
 import net.minecraftforge.common.ConfigCategory;
 import net.minecraftforge.common.Configuration;
 
 public class ConfigHelper {
 	public static final String STUDENT_CATEGORY = "student_category";
	public static final int DEFAULT_QUESTION_MINUTES = 1;
 	public static final int ONE_MINUTE_TICKS = 1200;
 	public static final int DEFAULT_STUDENT_GRADE = 1;
 	public static final boolean DEBUG_MODE = false;
	public static final int DEBUG_TICK_SPACING = 200;
 	private static Configuration config = null;
 
 	public static void init(File configFile) {
 		if (config == null) {
 			config = new Configuration(configFile);
 
 			config.load();
 
 			// TODO: make a GUI manager for config variables
 
 			// Initialize the category
 			ConfigCategory category = config.getCategory(STUDENT_CATEGORY);
 			category.setComment("I:question_minutes - Number of minutes between questions.\n"
 					+ "I:student_grade - Student's grade level. Possible values 1-4 currently. "
 					+ "More grade levels coming soon.");
 
 			// Initialize default configuration variables
 			config.get(STUDENT_CATEGORY, "question_minutes", DEFAULT_QUESTION_MINUTES).getInt();
 			config.get(STUDENT_CATEGORY, "student_grade", DEFAULT_STUDENT_GRADE).getInt();
 
 			config.save();
 		}
 	}
 
 	public static Configuration getConfig() {
 		return config;
 	}
 
 	public static int getQuestionMinutes() {
 		int questionMinutes = DEFAULT_QUESTION_MINUTES;
 
 		if (config != null) {
 			questionMinutes = config.get(STUDENT_CATEGORY, "question_minutes",
 					DEFAULT_QUESTION_MINUTES).getInt();
 		}
 
 		return questionMinutes;
 	}
 
 	public static int getStudentGrade() {
 		int studentGrade = DEFAULT_STUDENT_GRADE;
 
 		if (config != null) {
 			studentGrade = config.get(STUDENT_CATEGORY, "student_grade", DEFAULT_STUDENT_GRADE)
 					.getInt();
 		}
 
 		return studentGrade;
 	}
 
 	public static boolean isDebug() {
 		boolean debugMode = DEBUG_MODE;
 
 		if (config != null) {
 			debugMode = config.get(STUDENT_CATEGORY, "debug", DEBUG_MODE).getBoolean(DEBUG_MODE);
 		}
 
 		return debugMode;
 	}
 }
