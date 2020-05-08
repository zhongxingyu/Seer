 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.exercise.submitting;
 
 import edu.gatech.statics.application.StaticsApplet;
 import edu.gatech.statics.exercise.Exercise;
 import java.util.Map;
 import java.util.logging.Handler;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 /**
  *
  * @author lccstudent
  */
 public class DatabaseLogHandler extends Handler {
 
     private PostLogger poster = new PostLogger();
 
     @Override
     public void publish(LogRecord record) {
         Map<String, String> postMap = poster.getNewPostMap();

        String message = record.getMessage();
        if(record.getThrown() != null) {
            Throwable throwable = record.getThrown();
            
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                sb.append(stackTraceElement.toString()+"\n");
            }

            message += sb.toString();
        }

         postMap.put("problem_id", "" + Exercise.getExercise().getProblemID());
         postMap.put("user_id", "" + Exercise.getExercise().getState().getUserID());
         postMap.put("session_id", "" + Exercise.getExercise().getSessionID());
         postMap.put("java_class", record.getSourceClassName());
         postMap.put("java_method", record.getSourceMethodName());
        postMap.put("level", record.getLevel().toString());
        postMap.put("message", message);
         postMap.put("timestamp", "" + record.getMillis());
 
         poster.post(postMap);
     }
 
     @Override
     public void flush() {
     }
 
     @Override
     public void close() throws SecurityException {
     }
 }
