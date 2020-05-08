 /*
  * Copyright (C) 2013 by Netcetera AG.
  * All rights reserved.
  *
  * The copyright to the computer program(s) herein is the property of Netcetera AG, Switzerland.
  * The program(s) may be used and/or copied only with the written permission of Netcetera AG or
  * in accordance with the terms and conditions stipulated in the agreement/contract under which
  * the program(s) have been supplied.
  */
 package com.github.marschall.punch.jdbc;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 
 import com.github.marschall.punch.core.RecoveryService;
 import com.github.marschall.punch.core.TaskPath;
 
 public class DatabaseRecoveryService implements RecoveryService {
 
  private static final String SELECT_TASK_PATH = "SELECT task_path FROM t_task_state WHERE task_state != 'FINISHED'";
   private static final String CREATE_NEW_TASK_GROUP = "SELECT seq_task_state.nextval FROM DUAL";
 
   private final JdbcTemplate jdbcTemplate;
 
  private final AtomicReference<Set<TaskPath>> unfinishedTasks;
 
   public DatabaseRecoveryService(JdbcTemplate jdbcTemplate) {
     this.jdbcTemplate = jdbcTemplate;
    this.unfinishedTasks = new AtomicReference<>();
   }
 
   private Set<TaskPath> getFinishedTasks() {
    Set<TaskPath> set = this.unfinishedTasks.get();
     if (set != null) {
       return set;
     }
     List<TaskPath> list = this.jdbcTemplate.query(SELECT_TASK_PATH, TaskPathRowMapper.INSTANCE);
     set = new HashSet<>(list);
    boolean success = this.unfinishedTasks.compareAndSet(null, set);
     if (success) {
       return set;
     } else {
      return this.unfinishedTasks.get();
     }
   }
 
   @Override
   public int newTaskGroup() {
     // Need a transaction?
     return this.jdbcTemplate.queryForInt(CREATE_NEW_TASK_GROUP);
   }
 
   @Override
   public boolean isFinished(TaskPath path) {
    return !this.getFinishedTasks().contains(path);
   }
 
 
   static enum TaskPathRowMapper implements RowMapper<TaskPath> {
 
     INSTANCE;
 
     @Override
     public TaskPath mapRow(ResultSet rs, int rowNum) throws SQLException {
       return TaskPath.fromString(rs.getString("TASK_PATH"));
     }
 
   }
 
 }
