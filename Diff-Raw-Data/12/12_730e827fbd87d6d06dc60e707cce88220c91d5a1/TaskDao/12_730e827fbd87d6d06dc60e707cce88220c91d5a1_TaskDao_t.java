 package com.github.cwilper.fcrepo.cloudsync.service.impl;
 
 import com.github.cwilper.fcrepo.cloudsync.api.Task;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 import java.util.ArrayList;
 import java.util.List;
 
 class TaskDao {
 
     private final JdbcTemplate db;
 
     public TaskDao(JdbcTemplate db) {
         this.db = db;
     }
 
     public Task createTask(Task task) {
         return task;
     }
 
     public List<Task> listTasks() {
         List<Task> list = new ArrayList<Task>();
        /*
         Task item = new Task();
         item.id = "1";
         list.add(item);
        Task item2 = new Task();
        item2.id = "2";
        list.add(item2);
        Task item3 = new Task();
        item3.id = "3";
        list.add(item3);
        Task item4 = new Task();
        item4.id = "4";
        list.add(item4);
        */
         return list;
     }
 
     public Task getTask(String id) {
         Task item = new Task();
         item.id = id;
         return item;
     }
 
     public Task updateTask(String id, Task task) {
         return task;
     }
 
     public void deleteTask(String id) {
     }
 
 }
