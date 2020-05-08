 /* 
  * Copyright 2011 - Inform8
  * http://www.inform8.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License")
  * http://www.apache.org/licenses/LICENSE-2.0
  */
 package com.eighty8.inform8.db.types;
 
 import java.util.Arrays;
 
 import com.eighty8.inform8.displaysettings.Property;
 
 public class EnumType extends BaseFieldType {
 
   private final String[] options;
 
 
   public EnumType(String[] options) {
     this.options = options;
    this.properties.put(Property.ENUM_LIST.name(), Arrays.asList(options).toString().replace("[", "").replace("]", ""));
   }
 
 
   @Override
   public String getName() {
     return "enum";
   }
 
 
   public String[] getOptions() {
     return options;
   }
 
 }
