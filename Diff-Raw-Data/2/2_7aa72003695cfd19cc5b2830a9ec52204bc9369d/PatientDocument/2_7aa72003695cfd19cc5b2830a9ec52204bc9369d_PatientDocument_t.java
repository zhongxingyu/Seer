 package com.osdiab.patient_organizer;
 
import org.apache.commons.lang3.StringUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.regex.Pattern;
 
 /**
  * Created by osdiab on 7/29/14.
  */
 public abstract class PatientDocument {
     public static final Pattern dateRegex;
 
     static {
         String[] months = {
                 "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
                 "Aug", "Sep", "Oct", "Nov", "Dec"};
 
         StringBuilder regexBuilder = new StringBuilder();
         regexBuilder.append("((?:");
         regexBuilder.append(StringUtils.join(Arrays.asList(months), "|"));
         regexBuilder.append(")[a-z]* *[0-9]{1,2} *, *[0-9]{4})");
         dateRegex = Pattern.compile(regexBuilder.toString());
     }
 
     public abstract void replaceAppointmentDate(Date date) throws DateNotFoundException;
     public abstract void save(File dest) throws IOException;
 }
