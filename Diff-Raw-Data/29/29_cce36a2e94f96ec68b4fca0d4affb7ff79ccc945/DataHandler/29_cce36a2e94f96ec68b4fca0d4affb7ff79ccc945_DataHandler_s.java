 package library;
 
 import static org.junit.Assert.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import library.Section.AdditionalTime;
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * // -------------------------------------------------------------------------
 /**
  *  The DataHandler class is for handling, processing, and parsing the returned
  *  data from the Timetable of classes.
  *
  *  @author cmbuck
  *  @version Apr 4, 2012
  */
 public class DataHandler
 {
     private String data;
     private List<Section> sections;
 
     /**
      * This method sets the preprocessed data; the results from the Timetable
      * HTTP request.
      */
     public void setPreprocessedData(String preprocessed)
     {
         data = preprocessed;
     }
 
     /**
      * Get the processed list of Sections
      * @return the list of Sections
      */
     public List<Section> getSections()
     {
         return sections;
     }
 
     /**
      * Process the returned Timetable HTML data to obtain the list of Sections
      */
     public void processData()
     {
         if (data != null)
         {
             data = cutData(data);              //cut to </HTML>
             data = removeTags(data);           //remove tags
             data = removeGarbage(data);        //remove garbage
 
             String dataArray[];
             dataArray = splitAndRemoveEmptyLines(data);     //remove empty lines
 
             //TODO: make sure it isnt an error
 
             //make list of sections (parse)
             sections = parseData(dataArray);
         }
         else
         {
             //handle null case
         }
 
         //set preprocessed to null once we are done with it
         //to prevent errors if this method is called twice?
         data = null;
     }
 
     /**
      * Cuts the data string to only leave everything after </HTML>
      * @param str String to cut
      * @return The input String after being cut
      */
     public String cutData(String str)
     {
         /*
     	int index, index2;
     	index2=str.indexOf("</html>");
     	index=str.indexOf("</HTML>");
     	if (index2==-1)
     		str=str.substring(index+7,str.length());
     	if (index==-1)
     		str=str.substring(index2+7,str.length());
         return str;
         */
         int index;
         String searchString = "Search Results";
         index = str.indexOf(searchString);
         return str.substring(index + searchString.length());
     }
 
     /**
      * Removes all the (remaining) HTML tags
      * @param str String to remove HTML tags from
      * @return The input String with the HTML tags removed
      */
     public String removeTags(String str)
     {
         //str.replaceAll("<+[^>]+>", "");
         int index = 0;
         String finalString = "";
         while(index < str.length())
         {
             if(str.charAt(index) == '<')
             {
                 while(str.charAt(index) != '>')
                 {
                     index++;
                 }
                 index++;
 
                 finalString += '\n';
             }
             else
             {
                 finalString += str.substring(index, index+1);
                 index++;
             }
         }
 
         return finalString;
         //return str;
     }
 
     /**
      * Removes remaining junk (nbsp, ;, etc)
      * @param str String to remove junk from
      * @return processed String without junk
      */
     public String removeGarbage(String str)
     {
         while (str.contains("&nbsp"))
         {
             str = str.replaceAll("&nbsp;", "");
             str = str.replaceAll("&nbsp", "");
         }
 
         return str;
     }
 
     /**
      * Splits and removes empty lines
      * @param str String to remove empty lines from
      * @return processed String array without empty entries
      */
     public String[] splitAndRemoveEmptyLines(String str)
     {
         String[] data = str.split("\n");
 
         //count the non-empty entries
         int count = 0;
         for (int i=0; i < data.length; i++)
         {
             if (!data[i].equals(""))
                 count++;
         }
 
         String ret[] = new String[count];
         count = 0;
         for (int i=0; i < data.length; i++)
         {
             if (!data[i].equals(""))
                 ret[count++] = data[i];
         }
 
         return ret;
     }
 
     /**
      * Parses the cleaned Timetable data string into a list of Section objects
      * @param cleanedTimetable the cleaned Timetable data string
      * @return the List of Sections
      */
     public List<Section> parseData(String[] cleanedTimetable)
     {
 
         Section currentSection;
         List<Section> sectionList = new LinkedList<Section>();
         int index = 0;
         int nextCRNIndex;
 
         //parse sections
         //while there is another section
         while ((index = nextCRN(cleanedTimetable, index)) != -1)
         {
             currentSection = new Section();
 
             //predict the next CRN index
             nextCRNIndex = nextCRN(cleanedTimetable, index + 1);
 
             //start parsing
             parseCRN(cleanedTimetable, index++, currentSection);
             parseCourse(cleanedTimetable, index++, currentSection);
             parseTitle(cleanedTimetable, index++, currentSection);
             parseType(cleanedTimetable, index++, currentSection);
             parseHrs(cleanedTimetable, index++, currentSection);
             parseCapacity(cleanedTimetable, index++, currentSection);
             parseInstructor(cleanedTimetable, index++, currentSection);
             //check if (ARR)
             if (isARR(cleanedTimetable, index))
             {
                 //dont parse days
                 index++;
                 //dont parse time(s)
                 index++;
                 //parse location normally
                 parseLocation(cleanedTimetable, index++, currentSection);
                 parseExam(cleanedTimetable, index++, currentSection);
             }
             else
             {
                 parseDays(cleanedTimetable, index++, currentSection);
                 parseBegin(cleanedTimetable, index++, currentSection);
                 parseEnd(cleanedTimetable, index++, currentSection);
                 parseLocation(cleanedTimetable, index++, currentSection);
                 parseExam(cleanedTimetable, index++, currentSection);
             }
             //handle Additional Times
             if (isAdditionalTime(cleanedTimetable, index))
             {
                 index++;
                 //parse day, begin, end, location
                 AdditionalTime additional = currentSection.addAdditionalTime();
 
                 parseDays(cleanedTimetable, index++, additional);
                 parseBegin(cleanedTimetable, index++, additional);
                 parseEnd(cleanedTimetable, index++, additional);
                 parseLocation(cleanedTimetable, index++, additional);
             }
 
             sectionList.add(currentSection);
 
             //check for Timetable formatting errors
             if (nextCRNIndex < index && nextCRNIndex != -1)
             {
                 index = nextCRNIndex;
             }
         }
 
         return sectionList;
     }
 
 
     /**
      * Gets the array index of the next CRN, after the specified index.
      * @param cleanedTimetable cleaned data array
      * @param index index to start looking for the next CRN after
      * @return the index of the next CRN, or -1 if not found
      */
     public int nextCRN(String[] cleanedTimetable, int index)
     {
         for (int i = index; i < cleanedTimetable.length; i++)
         {
             String str = cleanedTimetable[i];
             Pattern p = Pattern.compile("[\\d]{5}");
             Matcher m = p.matcher(str);
             if (m.find())
                 return i;
         }
         return -1;
     }
 
     /**
      * Parses the CRN at the specified index and adds it to the current Section
      * @param cleanedTimetable the cleaned Timetable data
      * @param index the element in the array to parse for the CRN
      * @param currentSection reference to the Section currently being processed
      * @post currentSection has its CRN populated
      */
     public void parseCRN(
         String[] cleanedTimetable,
         int index,
         Section currentSection)
     {
         //assumes that a CRN is 5 digits long
         //assumes the CRN field starts with a CRN digit (and not a space)
         String crnString = cleanedTimetable[index];
 
         int crnInt = Integer.parseInt(crnString);
         currentSection.setCrn(crnInt);
 
     }
 
     /**
      * Parses the course that was requested
      * @param cleanedTimetable the cleaned Timetable data
      * @param index the element in the array to parse for the course
      * @param currentSection reference to the Section currently being processed
      * @post currentSection has its course set
      */
     public void parseCourse(
         String[] cleanedTimetable,
         int index,
         Section currentSection)
     {
     	String courseString = cleanedTimetable[index];
     	currentSection.setCourse(courseString);
     }
 
     /**
      * Parses the title of the course
      * @param cleanedTimetable the cleaned Timetable data
      * @param index the element in the array to parse for the title
      * @param currentSection reference to the Section currently being processed
      * @post currentSection has its title set
      */
 
     public void parseTitle(
         String[] cleanedTimetable,
         int index,
         Section currentSection)
     {
         String titleString = cleanedTimetable[index];
         currentSection.setTitle(titleString);
 
     }
 
 
     /**
      * Parses the type of the requested class
      * @param cleanedTimetable the cleaned Timetable data
      * @param index the element in the array to parse for the type
      * @param currentSection reference to the Section currently being processed
      * @post currentSection has its type set
      */
     public void parseType(
         String[] cleanedTimetable,
         int index,
         Section currentSection)
     {
         String typeString = cleanedTimetable[index];
         currentSection.setType(typeString);
 
     }
 
     /**
      * Parses the Hrs at the specified index and adds it to the currentSection
      * @param cleanedTimetable the cleaned Timetable data
      * @param index the element in the array to parse for the Hrs
      * @param currentSection reference to the Section currently being processed
      * @post currentSection has its Hrs populated
      */
     public void parseHrs(
         String[] cleanedTimetable,
         int index,
         Section currentSection)
     {
         String hrsString = cleanedTimetable[index];
         int hrsInt;
 
         try
         {
             hrsInt = Integer.parseInt(hrsString);
         }
         catch (NumberFormatException e)
         {
             hrsInt = -1;
         }
         currentSection.setHrs(hrsInt);
     }
 
     /**
      * Parses the capicity, seats availaible oout of total seats
      * @param cleanedTimetable the cleaned Timetable data
      * @param index the element in the array to parse for the capicity
      * @param currentSection reference to the Section currently being processed
      * @post currentSection has its capcaity set
      */
 
     public void parseCapacity(
         String[] cleanedTimetable,
         int index,
         Section currentSection)
     {
         String capacityString = cleanedTimetable[index];
         currentSection.setCapacity(capacityString);
 
     }
 
     /**
      * Parses the instructor of the section
      * @param cleanedTimetable the cleaned Timetable data
      * @param index the element in the array to parse for the instructor
      * @param currentSection reference to the Section currently being processed
      * @post currentSection has its instructor set
      */
 
     public void parseInstructor(
         String[] cleanedTimetable,
         int index,
         Section currentSection)
     {
         String instructorString = cleanedTimetable[index];
         currentSection.setInstructor(instructorString);
 
     }
 
     /**
      * Parses the days this section meets
      * @param cleanedTimetable the cleaned Timetable data
      * @param index the element in the array to parse for the Days
      * @param currentSection reference to the Section currently being processed
      * @post currentSection has its Days populated
      */
     public void parseDays(
         String[] cleanedTimetable,
         int index,
         Section currentSection)
     {
         //possible Days are M,T,W,R,F
         String days = cleanedTimetable[index];
         if (days.contains("M"))
             currentSection.addDay(Day.MONDAY);
         if (days.contains("T"))
             currentSection.addDay(Day.TUESDAY);
         if (days.contains("W"))
             currentSection.addDay(Day.WEDNESDAY);
         if (days.contains("R"))
             currentSection.addDay(Day.THURSDAY);
         if (days.contains("F"))
             currentSection.addDay(Day.FRIDAY);
 
     }
 
 
     public void parseBegin(
         String[] cleanedTimetable,
         int index,
         Section currentSection)
     {
         String beginString = cleanedTimetable[index];
 
         Time begin = parseTime(beginString);
         currentSection.setBegin(begin);
     }
 
 
     public void parseEnd(
         String[] cleanedTimetable,
         int index,
         Section currentSection)
     {
         String endString = cleanedTimetable[index];
         Time end = parseTime(endString);
         currentSection.setEnd(end);
     }
 
 
     /**
      * Parses a time string into the Time object.
      * @param time the time string (ex. 2:20PM)
      * @return the Time object representing the specified time
      */
     public Time parseTime(String time)
     {
 
         String split[] = time.split("[^0-9]");
         int hour = 0, minute = 0;
         try
         {
             hour = Integer.parseInt(split[0]);
             minute = Integer.parseInt(split[1]);
         }
         catch (Exception e)
         {
             System.out.println("Time parsing failed");
         }
 
         String meridiem = time.substring(time.length()-2);
         if (meridiem.equalsIgnoreCase("PM"))
         {
             hour %= 12; //handles case when hour is between 12:00pm and 12:59pm
             hour += 12;
         }
 
         Time timeObj = new Time(hour, minute);
 
         return timeObj;
     }
 
     /**
      * Parses the location of the class
      * @param cleanedTimetable the cleaned Timetable data
      * @param index the element in the array to parse for the location
      * @param currentSection reference to the Section currently being processed
      * @post currentSection has its location set
      */
 
     public void parseLocation(
         String[] cleanedTimetable,
         int index,
         Section currentSection)
     {
         String locationString = cleanedTimetable[index];
         currentSection.setLocation(locationString);
 
     }
 
     /**
      * Parses the code for the corresponding exam for the class
      * @param cleanedTimetable the cleaned Timetable data
      * @param index the element in the array to parse for the exam code
      * @param currentSection reference to the Section currently being processed
      * @post currentSection has its exam code set
      */
 
     public void parseExam(
         String[] cleanedTimetable,
         int index,
         Section currentSection)
     {
         String examString = cleanedTimetable[index];
         currentSection.setExam(examString);
 
     }
 
 
     /**
      * Determines if this section has an Additional Time
      * @param cleanedTimetable cleaned timetable data array
      * @param index the index of the element to be looked at
      * @return whether the specified index specifies it is an Additional Time
      */
     public boolean isAdditionalTime(String[] cleanedTimetable, int index)
     {
         //assumes that "* Additional Times *" is the only possible marker
         String str = cleanedTimetable[index];
         if (str.equalsIgnoreCase("* Additional Times *"))
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     public boolean isARR(String[] cleanedTimetable, int index)
     {
         String str = cleanedTimetable[index];
         if (str.contains("ARR"))
         {
             return true;
         }
         else
         {
             return false;
         }
 
 
     }
 
 }
