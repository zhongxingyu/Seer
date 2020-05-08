 /*  Copyright (C) 2012  Nicholas Wright
 
 	part of 'Aid', an imageboard downloader.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package app;
 
 import static config.AppSetting.*;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 public class SettingValidator {
 	private static final Logger logger = Logger.getLogger(SettingValidator.class.getName());
 	private static final String SUBPAGE_REGEX = "([a-zA-Z]+;[0-9]+,)*+[a-zA-Z]+;[0-9]+$";
 	private static final String BASEURL_REGEX = "(?m)http://([0-9a-zA-Z.-]+\\.)+([a-zA-Z])*/$";
 
 	public static boolean validateAppSettings(Properties appSettings){
 		// validate loaded parameters
 		boolean valid = true;
 
 		valid &= validatePageThreads(appSettings);
 		valid &= validateImageThreads(appSettings);
 		valid &= validateWriteBlocked(appSettings);
 		valid &= validateBaseUrl(appSettings);
 		valid &= validateSubPages(appSettings);
 		valid &= validateXpos(appSettings);
 		valid &= validateYpos(appSettings);
 
 		return valid;
 	}
 
 	/**
 	 * @param appSettings
 	 * @return
 	 */
 	protected static boolean validateYpos(Properties appSettings) {
 		return testLessThan(appSettings, ypos.toString(), 0);
 	}
 
 	/**
 	 * @param appSettings
 	 * @return
 	 */
 	protected static boolean validateXpos(Properties appSettings) {
 		// validate window position (x,y)
 		return testLessThan(appSettings, xpos.toString(), 0);
 	}
 
 	/**
 	 * @param appSettings
 	 * @return
 	 */
 	protected static boolean validateSubPages(Properties appSettings) {
 		// validate sub-pages
 		return testRegexMatch(appSettings, sub_pages.toString(), SUBPAGE_REGEX);
 	}
 
 	/**
 	 * @param appSettings
 	 * @return
 	 */
 	protected static boolean validateBaseUrl(Properties appSettings) {
 		// validate base URL
 		return testRegexMatch(appSettings, base_url.toString(), BASEURL_REGEX);
 	}
 
 	/**
 	 * @param appSettings
 	 */
 	protected static boolean validateWriteBlocked(Properties appSettings) {
 		// validate "write blocked" flag
 		String writeBlocked = appSettings.getProperty(write_blocked.toString());
 		if(writeBlocked == null || !(writeBlocked.equals("false") || writeBlocked.equals("true"))){
			String errorMsg = invalidPropertyMessage(write_blocked.toString());
 			logger.warning(errorMsg);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @param appSettings
 	 */
 	protected static boolean validateImageThreads(Properties appSettings) {
 		// validate number of image threads
 		return testLessThan(appSettings, image_threads.toString(), 1);
 	}
 
 	protected static boolean validatePageThreads(Properties appSettings) {
 		// validate number of page threads
 		return testLessThan(appSettings, page_threads.toString(), 1);
 	}
 
 	/**
 	 * Tests if the supplied regex matches the string. Outputs a error message if not.
 	 * @param toTest string to test
 	 * @param errorMsg message to output if string does not match
 	 * @param regex regex to use for test
 	 * @return
 	 */
 	private static boolean testRegexMatch(Properties appSettings, String property, String regex) {
 		String toTest = appSettings.getProperty(property);
 		String errorMsg = invalidPropertyMessage(property);
 		if(toTest == null || (! toTest.matches(regex))){
 			logger.warning(errorMsg);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Test if the string's int value is less than LessThan.
 	 * @param toTest string to test
 	 * @param errorMsg error Message to output in case toTest is equal or grater
 	 * @param lessThan value to test against
 	 * @return true if less, else false
 	 */
 	private static boolean testLessThan(Properties appSettings, String property ,  int lessThan) {
 		String toTest = appSettings.getProperty(property);
 		String errorMsg = invalidPropertyMessage(property);
 		try{
 			if(toTest == null || Integer.parseInt(toTest) < lessThan){
 				logger.warning(errorMsg);
 				return false;
 			}
 		}catch(NumberFormatException nfe){
 			logger.warning(errorMsg);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Generates a error message for properties that are invalid.
 	 * @param property property name in the file
 	 * @param filename the config file where the property is located
 	 * @param defaultValue the default value used for the property
 	 * @return
 	 */
 	private static String invalidPropertyMessage(String property){
 		return "'"+property+"' property is invalid.";
 	}
 }
