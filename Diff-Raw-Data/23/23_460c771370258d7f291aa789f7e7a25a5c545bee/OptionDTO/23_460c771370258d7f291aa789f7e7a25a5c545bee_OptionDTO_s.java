 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.armyeditor.dto;
 
 import com.armyeditor.entrys.Option;
 import javax.persistence.Id;
 
 /**
  *
  * @author Dmitry
  */
 public class OptionDTO implements java.io.Serializable  {
     private String  id;
     private String name;
     private String description;
     private String action;
     private CodexDTO codex;
 
     public OptionDTO() {
     }
 
 	public OptionDTO(Option option) {
 		this.id = option.getId();
 		this.name =  option.getName();
 		this.description = option.getDescription();
         this.action = option.getAction();
         if(option.getCodex()!=null)
          this.codex = new CodexDTO(option.getCodex());
 	}
 	
 	public Option toOption(){
 		Option option=new Option();
 		option.setId(id);
 		option.setName(name);
 		option.setDescription(description);
         option.setAction(action);
         if(codex!=null)
           option.setCodex(codex.toCodex());
 		return option;
 	}
 
     public CodexDTO getCodex() {
         return codex;
     }
 
     public void setCodex(CodexDTO codex) {
         this.codex = codex;
     }
 
     
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getAction() {
         return action;
     }
 
     public void setAction(String action) {
         this.action = action;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
 	@Override
 	public String toString() {
 		return "Option{" + "id=" + id + ", name=" + name + ", description=" + description + '}';
 	}
 	
 	
 }
