 package jp.gr.java_conf.neko_daisuki.simplemediascanner;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import android.util.SparseArray;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.annotations.SerializedName;
 import com.google.gson.reflect.TypeToken;
 
 public class Database {
 
     public static class Task extends Row {
 
         @SerializedName("path")
         private String mPath;
 
         private Task(int id, String path) {
             super(id);
             mPath = path;
         }
 
         public String getPath() {
             return mPath;
         }
 
         public String toString() {
             String fmt = "Task(id=%d, path=%s)";
             return String.format(Locale.ROOT, fmt, getId(), mPath);
         }
     }
 
     public static class Schedule extends Row {
 
         public static final int HOUR_WILDCARD = -1;
 
         @SerializedName("hour")
         private int mHour;
 
         @SerializedName("minute")
         private int mMinute;
 
         public Schedule(int id, int hour, int minute) {
             super(id);
             mHour = hour;
             mMinute = minute;
         }
 
         public int getHour() {
             return mHour;
         }
 
         public int getMinute() {
             return mMinute;
         }
 
         public boolean isDaily() {
             return mHour != HOUR_WILDCARD;
         }
     }
 
     public static class TaskSchedule {
 
         @SerializedName("task_id")
         private int mTaskId;
 
         @SerializedName("schedule_id")
         private int mScheduleId;
 
         public TaskSchedule(int taskId, int scheduleId) {
             mTaskId = taskId;
             mScheduleId = scheduleId;
         }
 
         public int getTaskId() {
             return mTaskId;
         }
 
         public int getScheduleId() {
             return mScheduleId;
         }
 
         public String toString() {
             String fmt = "TaskSchedule(task_id=%d, schedule_id=%d)";
             return String.format(Locale.ROOT, fmt, mTaskId, mScheduleId);
         }
 
         @Override
         public boolean equals(Object o) {
             TaskSchedule datum;
             try {
                 datum = (TaskSchedule)o;
             }
             catch (ClassCastException e) {
                 return false;
             }
             int taskId = datum.getTaskId();
             int scheduleId = datum.getScheduleId();
             return (taskId == mTaskId) && (scheduleId == mScheduleId);
         }
 
         @Override
         public int hashCode() {
             int n = Integer.valueOf(mTaskId).hashCode();
             int m = Integer.valueOf(mScheduleId).hashCode();
             return n + m;
         }
     }
 
     private static class Row {
 
         @SerializedName("id")
         private int mId;
 
         public Row(int id) {
             mId = id;
         }
 
         public final int getId() {
             return mId;
         }
 
         @Override
         public int hashCode() {
             return Integer.valueOf(mId).hashCode();
         }
 
         @Override
         public boolean equals(Object o) {
             Row row;
             try {
                 row = getClass().cast(o);
             }
             catch (ClassCastException e) {
                 return false;
             }
             return mId == row.getId();
         }
     }
 
     private static final Type TASK_COLLECTION_TYPE = new TypeToken<Collection<Task>>() {}.getType();
     private static final Type SCHEDULE_COLLECTION_TYPE = new TypeToken<Collection<Schedule>>() {}.getType();
     private static final Type TASK_SCHEDULE_COLLECTION_TYPE = new TypeToken<Set<TaskSchedule>>() {}.getType();
 
     // documents
     private SparseArray<Task> mTasks;
     private SparseArray<Schedule> mSchedules;
     private Set<TaskSchedule> mTaskSchedule;
 
     public int addTask(String path) {
         int id = getNewId(mTasks);
         editTask(id, path);
         return id;
     }
 
     public void editTask(int id, String path) {
         mTasks.put(id, new Task(id, path));
     }
 
     public Task getTask(int id) {
         return mTasks.get(id);
     }
 
     public Schedule[] getSchedules() {
         int size = mSchedules.size();
         Schedule[] schedules = new Schedule[size];
         for (int i = 0; i < size; i++) {
             schedules[i] = mSchedules.valueAt(i);
         }
         return schedules;
     }
 
     public Task[] getTasks() {
         int size = mTasks.size();
         Task[] tasks = new Task[size];
         for (int i = 0; i < size; i++) {
             tasks[i] = mTasks.valueAt(i);
         }
         return tasks;
     }
 
     public void removeTask(int id) {
         mTasks.remove(id);
     }
 
     public int addSchedule(int hour, int minute) {
         int id = getNewId(mSchedules);
         mSchedules.put(id, new Schedule(id, hour, minute));
         return id;
     }
 
     public void removeScheduleFromTask(int taskId, int scheduleId) {
         for (TaskSchedule e: mTaskSchedule) {
             int id = e.getTaskId();
             if ((id == taskId) && (e.getScheduleId() == scheduleId)) {
                 mTaskSchedule.remove(e);
                 return;
             }
         }
     }
 
     public void addScheduleToTask(int taskId, int scheduleId) {
         mTaskSchedule.add(new TaskSchedule(taskId, scheduleId));
     }
 
     public int[] getScheduleIdsOfTask(int taskId) {
         List<Integer> l = new LinkedList<Integer>();
         for (TaskSchedule datum: mTaskSchedule) {
             if (datum.getTaskId() == taskId) {
                 l.add(Integer.valueOf(datum.getScheduleId()));
             }
         }
         int size = l.size();
         int[] ids = new int[size];
         for (int i = 0; i < size; i++) {
             ids[i] = l.get(i).intValue();
         }
         return ids;
     }
 
     public void read(File directory) throws IOException {
         Gson gson = makeGson();
         String path = directory.getAbsolutePath();
         mTasks = readTasks(gson, path);
         mSchedules = readSchedules(gson, path);
         mTaskSchedule = readTaskSchedule(gson, path);
     }
 
     public void write(File directory) throws IOException {
         Gson gson = makeGson();
         String path = directory.getAbsolutePath();
         writeTasks(gson, path);
         writeSchedules(gson, path);
         writeTaskSchedule(gson, path);
     }
 
     public void initializeEmptyDatabase() {
         mTasks = new SparseArray<Task>();
         mSchedules = new SparseArray<Schedule>();
         mTaskSchedule = new HashSet<TaskSchedule>();
     }
 
     private Gson makeGson() {
         GsonBuilder builder = new GsonBuilder();
         builder.setPrettyPrinting();
         return builder.create();
     }
 
     private String getSchedulePath(String directory) {
         return String.format("%s/schedules.json", directory);
     }
 
     private String getTaskSchedulePath(String directory) {
         return String.format("%s/task_schedules.json", directory);
     }
 
     private String getTasksPath(String directory) {
         return String.format("%s/tasks.json", directory);
     }
 
     private <E extends Row> SparseArray<E> makeArray(Collection<E> c) {
         SparseArray<E> a = new SparseArray<E>();
         for (E e: c) {
             a.put(e.getId(), e);
         }
         return a;
     }
 
     private SparseArray<Schedule> readSchedules(Gson gson, String directory) throws IOException {
         Collection<Schedule> schedules;
         Reader reader = new FileReader(getSchedulePath(directory));
         try {
             schedules = gson.fromJson(reader, SCHEDULE_COLLECTION_TYPE);
         }
         finally {
             reader.close();
         }
         return makeArray(schedules);
     }
 
     private Set<TaskSchedule> readTaskSchedule(Gson gson, String directory) throws IOException {
         Reader reader = new FileReader(getTaskSchedulePath(directory));
         try {
             return gson.fromJson(reader, TASK_SCHEDULE_COLLECTION_TYPE);
         }
         finally {
             reader.close();
         }
     }
 
     private SparseArray<Task> readTasks(Gson gson, String directory) throws IOException {
         /*
          * Reading/writing SparseArrays with Gson is difficult for me. So I
          * converts arrays to usual collections.
          */
         Collection<Task> tasks;
         Reader reader = new FileReader(getTasksPath(directory));
         try {
             tasks = gson.fromJson(reader, TASK_COLLECTION_TYPE);
         }
         finally {
             reader.close();
         }
         return makeArray(tasks);
     }
 
     private void writeSchedules(Gson gson, String directory) throws IOException {
         Writer writer = new FileWriter(getSchedulePath(directory));
         try {
             gson.toJson(makeCollection(mSchedules), writer);
         }
         finally {
             writer.close();
         }
     }
 
     private void writeTaskSchedule(Gson gson, String directory) throws IOException {
         Writer writer = new FileWriter(getTaskSchedulePath(directory));
         try {
             gson.toJson(mTaskSchedule, writer);
         }
         finally {
             writer.close();
         }
     }
 
     private void writeTasks(Gson gson, String directory) throws IOException {
         Writer writer = new FileWriter(getTasksPath(directory));
         try {
             gson.toJson(makeCollection(mTasks), writer);
         }
         finally {
             writer.close();
         }
     }
 
     private <E> Collection<E> makeCollection(SparseArray<E> a) {
         int size = a.size();
         Collection<E> set = new HashSet<E>(size);
         for (int i = 0; i < size; i++) {
             set.add(a.valueAt(i));
         }
         return set;
     }
 
     private int getNewId(SparseArray<?> a) {
         int size = a.size();
         return 0 < size ? a.keyAt(size - 1) + 1 : 0;
     }
 }
