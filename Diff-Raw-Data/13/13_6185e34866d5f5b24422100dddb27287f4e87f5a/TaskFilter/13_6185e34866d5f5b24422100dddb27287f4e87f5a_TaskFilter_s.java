 package com.psddev.dari.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** Cleans up after any resources opened by {@link Task}. */
 public class TaskFilter extends AbstractFilter {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(TaskFilter.class);
 
     private final List<Task> tasks = new ArrayList<Task>();
 
     @Override
     protected void doInit() {
         for (Class<? extends RepeatingTask> taskClass : ObjectUtils.findClasses(RepeatingTask.class)) {
             try {
                 RepeatingTask task = taskClass.newInstance();
 
                 task.scheduleWithFixedDelay(1.0, 1.0);
                 tasks.add(task);
 
             } catch (IllegalAccessException error) {
                LOGGER.warn(String.format("Can't create [%s] task!"), error);
 
             } catch (InstantiationException error) {
                LOGGER.warn(String.format("Can't create [%s] task!"), error.getCause());
             }
         }
     }
 
     @Override
     protected void doDestroy() {
         for (Task task : tasks) {
             task.stop();
         }
 
         tasks.clear();
 
         for (TaskExecutor executor : TaskExecutor.Static.getAll()) {
             executor.shutdownNow();
         }
     }
 }
