 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.web.converters;
 
 import org.joda.time.LocalTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.springframework.core.convert.converter.Converter;
 
 /**
  * Created by Tomasz Szuba
  * Date: 04.01.13
  */
 public class StringToLocalTimeConverter implements Converter<String, LocalTime> {
 
    private static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("'T'HH:mm:ss");
 
     @Override
     public LocalTime convert(String source) {
         return LocalTime.parse(source, timeFormatter);
     }
 }
