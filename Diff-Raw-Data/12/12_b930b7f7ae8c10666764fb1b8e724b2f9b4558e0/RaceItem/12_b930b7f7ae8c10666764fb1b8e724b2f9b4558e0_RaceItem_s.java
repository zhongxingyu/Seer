 package com.pathfindersdk.books.items;
 
 import java.util.Collections;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import com.pathfindersdk.books.BookItem;
 import com.pathfindersdk.books.Index;
 import com.pathfindersdk.creatures.CreatureType;
 import com.pathfindersdk.creatures.RacialTrait;
 import com.pathfindersdk.enums.AbilityType;
 import com.pathfindersdk.enums.BookSectionType;
 import com.pathfindersdk.enums.LanguageType;
 import com.pathfindersdk.enums.SizeType;
 import com.pathfindersdk.enums.SpeedType;
 import com.pathfindersdk.enums.VisionType;
 import com.pathfindersdk.stats.Stat;
 
 public class RaceItem extends BookItem
 {
   protected CreatureType type;
   protected SortedMap<AbilityType, Integer> racialModifiers;
   protected SizeType size;
   protected VisionType vision;
   protected SortedSet<RacialTrait> racialTraits;
   protected SortedSet<AlternateRacialTraitItem> alternateRacialTraits;
   protected SortedSet<LanguageType> baseLanguages;
   protected SortedSet<LanguageType> optionalLanguages;
   protected SortedMap<SpeedType, Stat> speeds;
   // Weapon familiarity...
     
   public RaceItem(String name)
   {
     super(name);
   }
   
   public CreatureType getType()
   {
     return type;
   }
 
   public void setType(CreatureType type)
   {
     this.type = type;
   }
   
   public SortedMap<AbilityType, Integer> getRacialModifiers()
   {
     return Collections.unmodifiableSortedMap(racialModifiers);
   }
 
  public void addRacialModifier(AbilityType ability, Integer score)
   {
     if(ability != null && score != null)
     {
       if(racialModifiers == null)
         racialModifiers = new TreeMap<AbilityType, Integer>();
       
       racialModifiers.put(ability, score);
     }
   }
 
   public SizeType getSize()
   {
     return size;
   }
 
   public void setSize(SizeType size)
   {
     this.size = size;
   }
 
   public VisionType getVision()
   {
     return vision;
   }
 
   public void setVision(VisionType vision)
   {
     this.vision = vision;
   }
 
   public SortedSet<RacialTrait> getRacialTraits()
   {
     return Collections.unmodifiableSortedSet(racialTraits);
   }
 
   public void addRacialTrait(RacialTrait trait)
   {
     if(trait != null)
     {
       if(racialTraits == null)
         racialTraits = new TreeSet<RacialTrait>();
       
       racialTraits.add(trait);
     }
   }
 
   public SortedSet<AlternateRacialTraitItem> getAlternateRacialTraits()
   {
     return Collections.unmodifiableSortedSet(alternateRacialTraits);
   }
 
   public void addAlternateRacialTrait(AlternateRacialTraitItem trait)
   {
     if(trait != null)
     {
       if(alternateRacialTraits == null)
         alternateRacialTraits = new TreeSet<AlternateRacialTraitItem>();
       
       alternateRacialTraits.add(trait);
     }
   }
 
   public SortedSet<LanguageType> getBaseLanguages()
   {
     return Collections.unmodifiableSortedSet(baseLanguages);
   }
 
   public void addBaseLanguage(LanguageType language)
   {
     if(language != null)
     {
       if(baseLanguages == null)
         baseLanguages = new TreeSet<LanguageType>();
       
       baseLanguages.add(language);
     }
   }
 
   public SortedSet<LanguageType> getOptionalLanguages()
   {
     return Collections.unmodifiableSortedSet(optionalLanguages);
   }
 
   public void addOptionalLanguage(LanguageType language)
   {
     if(language != null)
     {
       if(optionalLanguages == null)
         optionalLanguages = new TreeSet<LanguageType>();
       
       optionalLanguages.add(language);
     }
   }
 
   public SortedMap<SpeedType, Stat> getSpeeds()
   {
     return Collections.unmodifiableSortedMap(speeds);
   }
 
   public void addSpeed(SpeedType type, Stat speed)
   {
     if(type != null && speed != null)
     {
       if(speeds == null)
         speeds = new TreeMap<SpeedType, Stat>();
       
       speeds.put(type, speed);
     }
   }
   
   @Override
   protected void index()
   {
     Index.getInstance().getIndex(BookSectionType.RACES).addItem(this);
   }
 }
