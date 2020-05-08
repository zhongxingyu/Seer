 package com.gmail.altakey.mint;
 
 import android.database.Cursor;
 
 import com.google.gson.TypeAdapter;
 import com.google.gson.stream.JsonReader;
 import com.google.gson.stream.JsonWriter;
 
 import java.io.IOException;
 import java.util.Date;
 
 public class Task {
     public long _id;
     public String _cookie;
     public long id;
     public String title;
     public String note;
     public long modified;
     public long completed;
     public long folder;
     public long context;
     public long priority;
     public long star;
     public long duedate;
     public long duetime;
     public String status;
     public Resolved resolved = new Resolved();
     public boolean grayedout;
 
     public class Resolved {
         TaskFolder folder;
         TaskContext context;
     };
 
     public void markAsDone() {
         markAsDone(new Date().getTime());
     }
 
     public void markAsDone(long at) {
         completed = at / 1000;
     }
 
     public void addCookie() {
         note = String.format("%s\n(mint:%s)", note, DB.nextCookie());
     }
 
     public void removeCookie() {
         note = note.replaceAll("\n?(mint:[0-9a-f]{32,})", "");
     }
 
    public void isReplica() {
         return note.contains(String.format("(mint:%s)", _cookie));
     }
 
     public static Task fromCursor(Cursor c, int offset) {
         final Task task = new Task();
         task._id = c.getLong(0 + offset);
         task._cookie = c.getString(1 + offset);
         task.id = c.getLong(2 + offset);
         task.title = c.getString(3 + offset);
         task.note = c.getString(4 + offset);
         task.modified = c.getLong(5 + offset);
         task.completed = c.getLong(6 + offset);
         task.folder = c.getLong(7 + offset);
         task.context = c.getLong(8 + offset);
         task.priority = c.getLong(9 + offset);
         task.star = c.getLong(10 + offset);
         task.duedate = c.getLong(11 + offset);
         task.duetime = c.getLong(12 + offset);
         task.status = c.getString(13 + offset);
         return task;
     }
 
     public static class JsonAdapter extends TypeAdapter<Task> {
         @Override
         public Task read(JsonReader reader) throws IOException {
             final Task task = new Task();
             reader.beginObject();
             while (reader.hasNext()) {
                 final String name = reader.nextName();
                 final String value = reader.nextString();
                 if ("id".equals(name)) {
                     task.id = Long.valueOf(value);
                 } else if ("title".equals(name)) {
                     task.title = value;
                 } else if ("note".equals(name)) {
                     task.note = value;
                 } else if ("modified".equals(name)) {
                     task.modified = Long.valueOf(value);
                 } else if ("completed".equals(name)) {
                     task.completed = Long.valueOf(value);
                 } else if ("folder".equals(name)) {
                     task.folder = Long.valueOf(value);
                 } else if ("context".equals(name)) {
                     task.context = Long.valueOf(value);
                 } else if ("priority".equals(name)) {
                     task.priority = Long.valueOf(value);
                 } else if ("star".equals(name)) {
                     task.star = Long.valueOf(value);
                 } else if ("duedate".equals(name)) {
                     task.duedate = Long.valueOf(value);
                 } else if ("duetime".equals(name)) {
                     task.duetime = Long.valueOf(value);
                 } else if ("status".equals(name)) {
                     task.status = value;
                 }
             }
             reader.endObject();
             return task;
         }
 
         @Override
         public void write(JsonWriter writer, Task value) throws IOException {
             final Task task = value;
             writer
                 .beginObject()
                 .name("id").value(task.id)
                 .name("title").value(task.title)
                 .name("note").value(task.note)
                 .name("modified").value(task.modified)
                 .name("completed").value(task.completed)
                 .name("folder").value(task.folder)
                 .name("context").value(task.context)
                 .name("priority").value(task.priority)
                 .name("star").value(task.star)
                 .name("duedate").value(task.duedate)
                 .name("duetime").value(task.duetime)
                 .name("status").value(task.status)
                 .endObject();
         }
     }
 }
