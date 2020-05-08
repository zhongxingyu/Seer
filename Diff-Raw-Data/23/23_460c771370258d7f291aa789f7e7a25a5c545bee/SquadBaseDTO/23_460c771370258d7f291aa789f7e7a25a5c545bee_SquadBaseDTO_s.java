 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.armyeditor.dto;
 
 import com.armyeditor.entrys.Option;
 import com.armyeditor.entrys.SquadBase;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util. ArrayList;
 import java.util.Map;
 import javax.persistence.CascadeType;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 /**
  *
  * @author Dmitry
  */
 public class SquadBaseDTO implements java.io.Serializable {
     private String  id;
     private SquadPartBaseDTO squadPartBase;
     private ArrayList<OptionDTO> options=new ArrayList<OptionDTO>();
     private String name;
     private String description;
     private CodexDTO codex;
 	/**
 	 * 0-hq,1-Elite,2-troops,3-fast,4-heavy,5-fortifications
 	 */
 	private int foqType;
 
     public SquadBaseDTO() {
     }
 
     public SquadBaseDTO(SquadBase squadBase){
         this.id = squadBase.getId();
         this.squadPartBase = new SquadPartBaseDTO(squadBase.getSquadPartBase(),null);
         for (Option o:squadBase.getOptions()){
             options.add(new OptionDTO(o));
         }
         this.name = squadBase.getName();
         this.description = squadBase.getDescription();
         if(squadBase.getCodex()!=null)
           this.codex = new CodexDTO(squadBase.getCodex());
     }
 
     public SquadBase toSquadBase(){
         SquadBase squadBase = new SquadBase();
         squadBase.setId(id);
         squadBase.setSquadPartBase(squadPartBase.toSquadPartBase());
         for (OptionDTO o:options){
             squadBase.getOptions().add(o.toOption());
         }
         squadBase.setName(name);
         squadBase.setDescription(description);
         if(codex!=null)
          squadBase.setCodex(codex.toCodex());
         return squadBase;
     }
 
     public CodexDTO getCodex() {
         return codex;
     }
 
     public void setCodex(CodexDTO codex) {
         this.codex = codex;
     }
     
 
 	public int getFoqType() {
 		return foqType;
 	}
 
 	public void setFoqType(int foqType) {
 		this.foqType = foqType;
 	}
 	
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
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
     
     public  ArrayList<OptionDTO> getOptions() {
         return options;
     }
 
     public void setOptions( ArrayList<OptionDTO> options) {
         this.options = options;
     }
 
     public SquadPartBaseDTO getSquadPartBase() {
         return squadPartBase;
     }
 
     public void setSquadPartBase(SquadPartBaseDTO squadPartBase) {
         this.squadPartBase = squadPartBase;
     }
 
 }
