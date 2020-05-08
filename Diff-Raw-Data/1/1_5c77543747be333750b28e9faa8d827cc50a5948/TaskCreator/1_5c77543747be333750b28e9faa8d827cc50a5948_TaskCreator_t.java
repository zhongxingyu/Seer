 package com.crowdplatform.util;
 
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.crowdplatform.model.Batch;
 import com.crowdplatform.model.Field;
 import com.crowdplatform.model.Task;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 @Service
 public class TaskCreator {
 
 	/**
 	 * Create a set of tasks and add them to the provided Batch.
 	 * @param batch Batch that will contain the tasks
 	 * @param fields Fields that should be considered for the Task contents
 	 * @param fileContents Data used to create the tasks
 	 */
 	@Transactional
     public void createTasks(Batch batch, List<Field> fields, List<Map<String, String>> fileContents) {
     	for (Map<String, String> line : fileContents) {
     		Map<String, Object> contents = encodeLine(fields, line);
     		
     		Task task = new Task();
     		task.setContents(contents);
     		batch.addTask(task);
     	}
     }
     
     private Map<String, Object> encodeLine(List<Field> fields, Map<String, String> line) {
     	Map<String, Object> contents = Maps.newHashMap();
 		for (Field field : fields) {
 			String value = line.get(field.getName());
 			if (value != null || field.getType().equals(Field.Type.MULTIVALUATE_STRING)) {
 				switch (field.getType()) {
     			case STRING:
     				contents.put(field.getName(), value);
     				break;
     			case INTEGER:
     				if (!value.isEmpty()) {
     					contents.put(field.getName(), Integer.valueOf(value));
     				}
     				break;
     			case DOUBLE:
     				if (!value.isEmpty()) {
     					contents.put(field.getName(), Float.valueOf(value));
     				}
     				break;
     			case MULTIVALUATE_STRING:
     				List<String> array = Lists.newArrayList();
     				for (String column : field.getColumnNames()) {
     					if (line.get(column) != null && !line.get(column).isEmpty())
     						array.add(line.get(column));
     				}
     				contents.put(field.getName(), array);
     				break;
     			case BOOL:
     				break;
     			} 
 			}
 		}
 		return contents;
     }
 }
