 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.armyeditor.entrys;
 
 import com.armyeditor.dto.ItemSelectionDTO;
 import com.armyeditor.dto.VenicleBaseDTO;
 import com.armyeditor.dto.WeaponSelectionDTO;
 import java.util.ArrayList;
 import javax.persistence.*;
 
 /**
  *
  * @author dmitry
  */
 @Entity
 public class SquadronBase {
     private String  id;
     private String name;
     private String description;
     private VenicleBase venicle;
     private int minSize;
     private int maxSize;
    private ArrayList<ItemSelection> itemSelection=new ArrayList<ItemSelection>();
    private ArrayList<WeaponSelection> weaponSelection=new ArrayList<WeaponSelection>();
     private String conditions;
     private Codex codex;
 	/**
 	 * 0-hq,1-Elite,2-troops,3-fast,4-heavy,5-fortifications
 	 */
 	private int foqType;
 
     @ManyToOne( cascade = {CascadeType.MERGE, CascadeType.REFRESH} )
     @JoinColumn(name="Codex_fk")
     public Codex getCodex() {
         return codex;
     }
 
     public void setCodex(Codex codex) {
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
     @ManyToMany(cascade = CascadeType.REFRESH)
    public ArrayList<ItemSelection> getItemSelection() {
         return itemSelection;
     }
     
    public void setItemSelection(ArrayList<ItemSelection> itemSelection) {
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
 
     @ManyToOne( cascade = {CascadeType.REFRESH} )
     @JoinColumn(name="VenicletBase_fk")
     public VenicleBase getVenicle() {
         return venicle;
     }
 
     public void setVenicle(VenicleBase venicle) {
         this.venicle = venicle;
     }
     @ManyToMany(cascade = CascadeType.REFRESH)
    public ArrayList<WeaponSelection> getWeaponSelection() {
         return weaponSelection;
     }
 
    public void setWeaponSelection(ArrayList<WeaponSelection> weaponSelection) {
         this.weaponSelection = weaponSelection;
     }
     
     
 }
