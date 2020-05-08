 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.armyeditor.dto;
 
 import com.armyeditor.entrys.ItemSelection;
 import com.armyeditor.entrys.SquadPart;
 import com.armyeditor.entrys.SquadronBase;
 import com.armyeditor.entrys.WeaponSelection;
 import java.util.ArrayList;
 
 /**
  *
  * @author dmitry
  */
 public class SquadronBaseDTO {
     private String  id;
     private String name;
     private String description;
     private VenicleBaseDTO venicle;
     private int minSize;
     private int maxSize;
     private ArrayList<ItemSelectionDTO> itemSelection=new ArrayList<ItemSelectionDTO>();
     private ArrayList<WeaponSelectionDTO> weaponSelection=new ArrayList<WeaponSelectionDTO>();
     private String conditions;
     private CodexDTO codex;
 	/**
 	 * 0-hq,1-Elite,2-troops,3-fast,4-heavy,5-fortifications
 	 */
 	private int foqType;
 
     public SquadronBaseDTO() {
     }
     
     public SquadronBaseDTO(SquadronBase squadron) {
         this.id = squadron.getId();
         this.name = squadron.getName();
         this.description = squadron.getDescription();
         this.venicle = new VenicleBaseDTO(squadron.getVenicle());
         this.minSize = squadron.getMinSize();
         this.maxSize = squadron.getMaxSize();
         this.conditions = squadron.getConditions();
         if(squadron.getCodex()!=null)
         this.codex = new CodexDTO(squadron.getCodex());
         for (ItemSelection is : squadron.getItemSelection()) {
             itemSelection.add(new ItemSelectionDTO(is));
         }
         for (WeaponSelection is : squadron.getWeaponSelection()) {
             weaponSelection.add(new WeaponSelectionDTO(is));
         }
     }
     
     public SquadronBase toSquadronBase() {
         SquadronBase sq = new SquadronBase();
         sq.setId(this.getId());
         sq.setName(this.getName());
         sq.setDescription(this.getDescription());
         sq.setVenicle(this.getVenicle().toVenicleBase());
         sq.setMinSize(this.getMinSize());
         sq.setMaxSize(this.getMaxSize());
         sq.setConditions(this.getConditions());
         if(this.getCodex()!=null)
         sq.setCodex(this.getCodex().toCodex());
         for (ItemSelectionDTO is : this.getItemSelection()) {
             sq.getItemSelection().add(is.toItemSelection());
         }
         for (WeaponSelectionDTO is : this.getWeaponSelection()) {
             sq.getWeaponSelection().add(is.toWeaponSelection());
         }
         return sq;
     }
 
     public CodexDTO getCodex() {
         return codex;
     }
 
     public void setCodex(CodexDTO codex) {
         this.codex = codex;
     }
     
     
 
     public String getConditions() {
         return conditions;
     }
 
     public void setConditions(String conditions) {
         this.conditions = conditions;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public int getFoqType() {
         return foqType;
     }
 
     public void setFoqType(int foqType) {
         this.foqType = foqType;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public ArrayList<ItemSelectionDTO> getItemSelection() {
         return itemSelection;
     }
 
     public void setItemSelection(ArrayList<ItemSelectionDTO> itemSelection) {
         this.itemSelection = itemSelection;
     }
 
     public int getMaxSize() {
         return maxSize;
     }
 
     public void setMaxSize(int maxSize) {
         this.maxSize = maxSize;
     }
 
     public int getMinSize() {
         return minSize;
     }
 
     public void setMinSize(int minSize) {
         this.minSize = minSize;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public VenicleBaseDTO getVenicle() {
         return venicle;
     }
 
     public void setVenicle(VenicleBaseDTO venicle) {
         this.venicle = venicle;
     }
 
     public ArrayList<WeaponSelectionDTO> getWeaponSelection() {
         return weaponSelection;
     }
 
     public void setWeaponSelection(ArrayList<WeaponSelectionDTO> weaponSelection) {
         this.weaponSelection = weaponSelection;
     }
     
     
     
     
     
 }
