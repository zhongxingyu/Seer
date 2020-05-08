 /* 
  * Copyright 2011 - Inform8
  * http://www.inform8.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License")
  * http://www.apache.org/licenses/LICENSE-2.0
  */
 package com.eighty8.inform8.db.table;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.List;
 
 import com.eighty8.inform8.db.Settings;
 import com.eighty8.inform8.db.types.FieldType;
 import com.eighty8.inform8.displaysettings.Label;
 import com.eighty8.inform8.util.StringUtils;
 
 public class BaseColumn implements Inform8Column {
 
   /** the name of the field */
   protected final String name;
 
   /** the type of this field */
   protected FieldType type;
 
   /**
    * 
    */
   final Settings settings = new Settings();
 
   /**
    * the fields 'Master' foreign key i.e. the table where we derive the parent data
    */
   public Inform8ForeignKey childForeignKey;
 
   /** the fields foreign key */
   public List<Inform8ForeignKey> masterForeignKeys = new ArrayList<Inform8ForeignKey>();
 
 
   public BaseColumn(String name, int displayIndex) {
     this.name = name;
     setDisplayIndex(displayIndex);
   }
 
 
   public FieldType getType() {
     return type;
   }
 
 
   public void setType(FieldType type) {
     this.type = type;
    this.settings.getProperties().putAll(type.getProperties());
   }
 
 
   public String getName() {
     return name;
   }
 
 
   public String getCaseExplodedName() {
     return StringUtils.getCaseExplodedName(name, true);
   }
 
 
   public int getDisplayIndex() {
     return Integer.parseInt(settings.getProperty(Label.DISPLAY_INDEX, "0"));
   }
 
 
   public void setDisplayIndex(int displayIndex) {
     settings.addProperty(Label.DEFAULT_VALUE, String.valueOf(displayIndex));
   }
 
 
   public void setNotNull(boolean notNull) {
     settings.addProperty(Label.NOT_NULL, String.valueOf(notNull));
   }
 
 
   public void setDefaultValue(String defaultValue) {
     settings.addProperty(Label.DEFAULT_VALUE, defaultValue);
   }
 
 
   public Settings getSettings() {
     return settings;
   }
 
 
   @Override
   public void addMasterForeignKeys(Inform8ForeignKey fk) {
     masterForeignKeys.add(fk);
   }
 
 
   @Override
   public Inform8ForeignKey getChildForeignKey() {
     return childForeignKey;
   }
 
 
   @Override
   public List<Inform8ForeignKey> getMasterForeignKeys() {
     return masterForeignKeys;
   }
 
 
   @Override
   public void setChildForeignKey(Inform8ForeignKey fk) {
     this.childForeignKey = fk;
   }
 
 
   @Override
   public String toString() {
     return "BaseColumn [name=" + name + "]";
   }
 
 }
