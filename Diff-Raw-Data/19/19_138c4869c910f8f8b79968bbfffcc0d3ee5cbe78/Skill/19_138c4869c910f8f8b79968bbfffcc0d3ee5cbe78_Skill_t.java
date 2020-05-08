 package org.timadorus.webapp.client.character;
 
 import java.io.Serializable;
 
import javax.jdo.annotations.Column;
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 
 // This class represents a Skill of a Character-Object
 @PersistenceCapable
 public class Skill implements Serializable, Cloneable {
 
   private static final long serialVersionUID = -5634375002626095838L;
 
   @Persistent
   private String defLabel = "";
 
   @Persistent
   private String lvlBonusCat = "";
 
   @Persistent
   private String stat1 = "";
 
   @Persistent
   private String stat2 = "";
 
   @Persistent
   private String actionType = "";
 
   @Persistent
   private String calcType = "";
 
   @Persistent
   private String localeDescLabel = "";
 
   @Persistent
   private String localeDescLanguage = "";
 
   @Persistent
   private boolean localeDescDefault = false;
 
   @Persistent
   private String name = "";
 
  @NotPersistent
  private final int descriptionLength = 1024;
  
   @Persistent
  @Column(length = descriptionLength)
   private String description = "";
 
   @Persistent
   private int cost = 0;
 
   @Persistent
   private int rank = 0;
 
   @Persistent
   private int rkBn = 0;
 
   @Persistent
   private int statBn = 0;
 
   @Persistent
   private int levelBn = 0;
 
   @Persistent
   private int item = 0;
 
   @Persistent
   private int total = 0;
 
 //  @Serialized
 //  @Persistent
   @NotPersistent
   private String[] overallInformation;
 
 
 
 
   public Skill() {
     // TODO Auto-generated constructor stub
   }
 
   public Skill(final String defLabelIn,
                 final String lvlBonusCatIn,
                 final String stat1In,
                 final String stat2In,
                 final String actionTypeIn,
                 final String calcTypeIn,
                 final String localeDescLabelIn,
                 final String localeDescLanguageIn,
                 final boolean localeDescDefaultIn,
                 final String descriptionIn,
                 final int costIn,
                 final int rankIn,
                 final int rkBnIn,
                 final int statBnIn,
                 final int levelBnIn,
                 final int itemIn) {
 
     this.actionType = actionTypeIn;
     this.calcType = calcTypeIn;
     this.defLabel = defLabelIn;
     this.localeDescDefault = localeDescDefaultIn;
     this.localeDescLabel = localeDescLabelIn;
     this.localeDescLanguage = localeDescLanguageIn;
     this.lvlBonusCat = lvlBonusCatIn;
     this.stat1 = stat1In;
     this.stat2 = stat2In;
     this.name = this.localeDescLabel;
     this.description = descriptionIn;
     this.cost = costIn;
     this.rank = rankIn;
     this.rkBn = rkBnIn;
     this.statBn = statBnIn;
     this.levelBn = levelBnIn;
     this.item = itemIn;
 
     setTotal("start");
   }
 
   public Skill(final Skill skillIn) {
     this.actionType = skillIn.actionType;
     this.calcType = skillIn.calcType;
     this.defLabel = skillIn.defLabel;
     this.localeDescDefault = skillIn.localeDescDefault;
     this.localeDescLabel = skillIn.localeDescLabel;
     this.localeDescLanguage = skillIn.localeDescLanguage;
     this.lvlBonusCat = skillIn.lvlBonusCat;
     this.stat1 = skillIn.stat1;
     this.stat2 = skillIn.stat2;
     this.name = skillIn.localeDescLabel;
     this.description = skillIn.description;
     this.cost = skillIn.cost;
     this.rank = skillIn.rank;
     this.rkBn = skillIn.rkBn;
     this.statBn = skillIn.statBn;
     this.levelBn = skillIn.levelBn;
     this.item = skillIn.item;
 
     setTotal("start");
 }
 
   public final String getDefLabel() {
     return defLabel;
   }
 
   public final void setDefLabel(final String defLabelIn) {
     this.defLabel = defLabelIn;
   }
 
   public final String getLvlBonusCat() {
     return lvlBonusCat;
   }
 
   public final void setLvlBonusCat(final String lvlBonusCatIn) {
     this.lvlBonusCat = lvlBonusCatIn;
   }
 
   public final String getStat1() {
     return stat1;
   }
 
   public final void setStat1(final String stat1In) {
     this.stat1 = stat1In;
   }
 
   public final String getStat2() {
     return stat2;
   }
 
   public final void setStat2(final String stat2In) {
     this.stat2 = stat2In;
   }
 
   public final String getActionType() {
     return actionType;
   }
 
   public final void setActionType(final String actionTypeIn) {
     this.actionType = actionTypeIn;
   }
 
   public final String getCalcType() {
     return calcType;
   }
 
   public final void setCalcType(final String calcTypeIn) {
     this.calcType = calcTypeIn;
   }
 
   public final String getLocaleDescLabel() {
     return localeDescLabel;
   }
 
   public final void setLocaleDescLabel(final String localeDescLabelIn) {
     this.localeDescLabel = localeDescLabelIn;
   }
 
   public final String getLocaleDescLanguage() {
     return localeDescLanguage;
   }
 
   public final void setLocaleDescLanguage(final String localeDescLanguageIn) {
     this.localeDescLanguage = localeDescLanguageIn;
   }
 
   public final boolean isLocaleDescDefault() {
     return localeDescDefault;
   }
 
   public final void setLocaleDescDefault(final boolean localeDescDefaultIn) {
     this.localeDescDefault = localeDescDefaultIn;
   }
 
   public final String getName() {
     return name;
   }
 
   public final void setName(String nameIn) {
     this.name = nameIn;
   }
 
   public final String getDescription() {
     return description;
   }
 
   public final void setDescription(String descriptionIn) {
     this.description = descriptionIn;
   }
 
   public final int getCost() {
     return cost;
   }
 
   public final void setCost(final int costIn) {
     this.cost = costIn;
   }
 
   public final int getRank() {
     return rank;
   }
 
   public final void setRank(final int rankIn) {
     this.rank = rankIn;
   }
 
   public final int getRkBn() {
     return rkBn;
   }
 
   public final void setRk_Bn(final int rkBnIn) {
     this.rkBn = rkBnIn;
   }
 
   public final int getStatBn() {
     return statBn;
   }
 
   public final void setStat_Bn(final int statBnIn) {
     this.statBn = statBnIn;
   }
 
   public final int getLevelBn() {
     return levelBn;
   }
 
   public final void setLevel_Bn(final int levelBnIn) {
     this.levelBn = levelBnIn;
   }
 
   public final int getItem() {
     return item;
   }
 
   public final void setItem(final int itemIn) {
     this.item = itemIn;
   }
 
   public final int getTotal() {
     return total;
   }
 
   /**
    * TODO! evaluate the update parameter??
    *
    * @param update ??
    */
   public final void setTotal(final String update) {
     this.total = getRkBn() + getStatBn();
   }
 
   public final String[] getGesamtInfo() {
     int i = 0;
     final int arraySize = 8;
     overallInformation = new String[arraySize];
     overallInformation[i++] = this.name;
     overallInformation[i++] = "" + this.cost;
     overallInformation[i++] = "" + this.rank;
     overallInformation[i++] = "" + this.rkBn;
     overallInformation[i++] = "" + this.statBn;
     overallInformation[i++] = "" + this.levelBn;
     overallInformation[i++] = "" + this.item;
     overallInformation[i++] = "" + getTotal();
     return overallInformation;
   }
 
   public final void setGesamtInfo(final String[] gesamtInfoIn) {
     this.overallInformation = gesamtInfoIn;
   }
 
 //  @Override
 //  public Skill clone(){
 //
 //      try {
 //        return (Skill)clone();
 //    } catch (Exception e) {
 //        // TODO Auto-generated catch block
 //        e.printStackTrace();
 //        throw new InternalError();
 //    }
 ////  return null;
 //  }
 
   @Override
   public final String toString() {
     String s = "<br>Skill-Name: " + name + "<br>Level-Bonus-Cat: "
                + lvlBonusCat + "<br>Stat1: " + stat1 + "<br>Stat2: " + stat2
                + "<br>Action-Type: " + actionType + "<br>Calc-Type: " + calcType
                + "<br>Local-Desc-Language: " + localeDescLanguage
                + "<br>Local-Desc-Default: " + localeDescDefault;
 
     return s;
 
   }
 }
