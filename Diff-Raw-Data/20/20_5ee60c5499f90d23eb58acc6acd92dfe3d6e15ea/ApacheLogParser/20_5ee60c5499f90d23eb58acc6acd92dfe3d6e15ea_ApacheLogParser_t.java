 package ru.develbureau.mrtesting.parser;
 
 import ru.develbureau.mrtesting.model.LoggedRequest;
 
 import java.text.SimpleDateFormat;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: sergey.sheypak
  * Date: 23.03.13
  * Time: 13:41
  *
  * Sample input: 199.72.81.55 - - [01/Jul/1995:00:00:01 -0400] "GET /history/apollo/ HTTP/1.0" 200 6245
  */
 public class ApacheLogParser {
 
     private static final int DATE_GROUP = 1;
     private static final int REQUEST_GROUP = 2;
     private static final int CODE_GROUP = 3;
     private static final int TIMESTAMP_NUM = 0;
     private static final int REQ_NUM = 1;
     private static final int CODE_NUM = 2;
     private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH);
     private final Pattern pattern = Pattern.compile(".+\\s-\\s-\\s\\[(.+)\\s.+\\]\\s\"\\w+\\s(.+)\"\\s(\\d+)\\s\\d+");
 
     public LoggedRequest parse(String inputLine) throws ParserException{
         try{
             String[] capturedGroups = splitByGroups(inputLine);
             return new LoggedRequest(parseTimestamp(capturedGroups[TIMESTAMP_NUM]),
                                                     parseRequest(capturedGroups[REQ_NUM]),
                                                     Integer.valueOf(capturedGroups[CODE_NUM]));
         }catch (Exception e){
             throw new ParserException("Error while parsing apache log line", e);
         }
     }
 
     public long parseTimestamp(String timeAsString) throws ParserException {
         try{
             return simpleDateFormat.parse(timeAsString).getTime();
         }catch (Exception e){
             throw new ParserException(String.format("Error while trying to get timestamp from [%s] using pattern [%s]",
                                                      timeAsString, simpleDateFormat.toPattern()), e);
         }
     }
 
     protected String parseRequest(String dirtyRequest){
         int indexOfSpace =dirtyRequest.lastIndexOf(" ");
         return dirtyRequest.substring(0, indexOfSpace);
     }
 
     protected String[] splitByGroups(String inputLine) throws ParserException {
         Matcher matcher = pattern.matcher(inputLine);
         if(matcher.matches()){
             return new String[]{matcher.group(DATE_GROUP), matcher.group(REQUEST_GROUP), matcher.group(CODE_GROUP)};
         }
        throw new ParserException(String.format("Input line [%s] doesn't match provided regex [%s]", inputLine, pattern.pattern()));
     }
 }
