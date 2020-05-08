 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.data.converters;
 
 import org.joda.time.LocalTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.springframework.core.convert.converter.Converter;
 
 /**
  * Created by Tomasz Szuba
  * Date: 04.01.13
  */
 public class LocalTimeToStringConverter implements Converter<LocalTime, String> {
 
    private static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");
 
     @Override
     public String convert(LocalTime source) {
         return source.toString(timeFormatter);
     }
 }
