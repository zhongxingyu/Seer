 package com.crowdgame.model;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 
 public class TaskInput {
 
	private Integer taskId;
 	
 	private String contents;
 	
 	private TaskInputDecoded decoded;
 
	public Integer getTaskId() {
		return taskId;
 	}
 
	public void setTaskId(Integer id) {
		this.taskId = id;
 	}
 
 	public String getContents() {
 		return contents;
 	}
 
 	public void setContents(String contents) {
 		this.contents = contents;
 		decodeContents();
 	}
 	
 	public void decodeContents() {
 		ObjectMapper mapper = new ObjectMapper();
 		try {
 			decoded = mapper.readValue(contents, TaskInputDecoded.class);
 		} catch (JsonParseException e) {
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getType() {
 		return decoded.getType();
 	}
 
 	public String getWord() {
 		return decoded.getWord();
 	}
 
	public Integer getId() {
 		return decoded.getId();
 	}
 	
 	public Integer getLevel() {
 		return decoded.getLevel();
 	}
 	
 	public String getLanguage() {
 		return decoded.getLanguage();
 	}
 	
 	public String getDisplayText() {
 		return decoded.getDisplay();
 	}
 
 	public List<String> getAnswers() {
 		return decoded.getAnswers();
 	}
 	
 	public static class TaskInputDecoded {
 		
 		private String type;
 		
 		private String word;
 		
 		private Integer id;
 		
 		private Integer level;
 		
 		private String language;
 		
 		private String display;
 		
 		private List<String> answers;
 
 		public String getType() {
 			return type;
 		}
 
 		public void setType(String type) {
 			this.type = type;
 		}
 
 		public String getWord() {
 			return word;
 		}
 
 		public void setWord(String word) {
 			this.word = word;
 		}
 
 		public Integer getId() {
 			return id;
 		}
 
 		public void setId(Integer id) {
 			this.id = id;
 		}
 
 		public List<String> getAnswers() {
 			return answers;
 		}
 
 		public void setAnswers(List<String> answers) {
 			this.answers = answers;
 		}
 
 		public Integer getLevel() {
 			return level;
 		}
 
 		public void setLevel(Integer level) {
 			this.level = level;
 		}
 
 		public String getLanguage() {
 			return language;
 		}
 
 		public void setLanguage(String language) {
 			this.language = language;
 		}
 
 		public String getDisplay() {
 			return display;
 		}
 
 		public void setDisplay(String displayText) {
 			this.display = displayText;
 		}
 		
 		
 	}
 }
