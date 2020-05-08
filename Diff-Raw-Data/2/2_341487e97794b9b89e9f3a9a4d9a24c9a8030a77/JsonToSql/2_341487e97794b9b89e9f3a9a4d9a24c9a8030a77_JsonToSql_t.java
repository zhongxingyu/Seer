 /*******************************************************************************
  * Copyright (c) 2011-2012 Ethan Hall
  *
  * Permission is hereby granted, free of charge, to any person obtaining a
  * copy of this software and associated documentation files (the "Software"),
  *  to deal in the Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included
  * in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
  * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  ******************************************************************************/
 
 package com.ehdev.chronos.lib;
 
 import android.util.Log;
 import com.ehdev.chronos.lib.enums.Defines;
 import com.ehdev.chronos.lib.enums.PayPeriodDuration;
 import com.ehdev.chronos.lib.types.*;
 import com.google.gson.Gson;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.table.TableUtils;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class JsonToSql {
     private Chronos gChronos;
     private static String TAG = Defines.TAG + " - Json2Sql";
 
     public JsonToSql(Chronos chron){
         gChronos = chron;
     }
 
     public String getJson(){
         List<Job> listOfJobs = gChronos.getAllJobs();
         Gson gson = new Gson();
         List<JsonObj> json = new ArrayList<JsonObj>();
 
         for(Job j : listOfJobs){
             List<Task> tasks = gChronos.getAllTasks(j);
             List<Note> notes = gChronos.getAllNotes(j);
             List<Punch> punches = gChronos.getPunchesByJob(j);
 
             json.add(new JsonObj(j, punches, tasks, notes));
         }
 
         JsonObj[] jsonArray = new JsonObj[json.size()];
         for(int i = 0; i < json.size(); i++){
             jsonArray[i] = json.get(i);
         }
 
         return gson.toJson(jsonArray);
     }
 
     public void putJson(String json){
         Gson gson = new Gson();
         //Log.d(TAG, json);
         JsonObj jsonData[] = gson.fromJson(json, JsonObj[].class);
 
         try{
             TableUtils.dropTable(gChronos.getConnectionSource(), Punch.class, true); //Punch - Drop all
             TableUtils.dropTable(gChronos.getConnectionSource(), Task.class, true); //Task - Drop all
             TableUtils.dropTable(gChronos.getConnectionSource(), Job.class, true); //Job - Drop all
             TableUtils.dropTable(gChronos.getConnectionSource(), Note.class, true); //Note - Drop all
 
             //Recreate DB
             TableUtils.createTable(gChronos.getConnectionSource(), Punch.class); //Punch - Create Table
             TableUtils.createTable(gChronos.getConnectionSource(), Task.class); //Task - Create Table
             TableUtils.createTable(gChronos.getConnectionSource(), Job.class); //Job - Create Table
             TableUtils.createTable(gChronos.getConnectionSource(), Note.class); //Task - Create Table
 
             //recreate entries
             Dao<Task,String> taskDAO = gChronos.getTaskDao();
             Dao<Job,String> jobDAO = gChronos.getJobDao();
             Dao<Punch,String> punchDOA = gChronos.getPunchDao();
             Dao<Note,String> noteDOA = gChronos.getNoteDao();
 
 
             for(JsonObj data : jsonData){
                 Job thisJob = data.getJob();
                 prepareJob(thisJob);
                 jobDAO.create(thisJob);
                 jobDAO.refresh(thisJob);
 
                 List<Task> tasks = data.getTasks();
                 for(Task t: tasks){
                     t.setJob(thisJob);
                     taskDAO.create(t);
                     taskDAO.refresh(t);
                 }
 
                 List<Punch> punches = data.getPunches();
                 Log.d(TAG, "Punches to insert: " + punches.size());
                 for(Punch p: punches){
                     p.setJob(thisJob);
                     p.setTask(tasks.get(0));
                     p.setTime( p.getTime());
 
                     punchDOA.create(p);
                     punchDOA.refresh(p);
                 }
 
                 List<Note> notes = data.getNote();
                 for(Note n: notes){
                     n.setJob(thisJob);
                     noteDOA.create(n);
                     noteDOA.refresh(n);
                 }
 
             }
 
         } catch (SQLException e){
             Log.e(TAG, e.getMessage());
         }
     }
 
     private void prepareJob(Job thisJob) {
         if(null == thisJob.getName())
             thisJob.setName("Default Job");
 
         if( null == thisJob.getDuration())
             thisJob.setDuration(PayPeriodDuration.TWO_WEEKS);
 
        if(null == thisJob.getStartOfPayPeriod() || 2010 > thisJob.getStartOfPayPeriod().getYear())
             thisJob.setStartOfPayPeriod(
                     DateTime.now().withDayOfWeek(7).minusWeeks(1).toDateMidnight().toDateTime().withZone(DateTimeZone.getDefault())
             );
     }
 
 }
