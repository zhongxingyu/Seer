 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.data.converters;
 
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.springframework.core.convert.converter.Converter;
 
 /**
  * Created by Tomasz Szuba
  * Date: 04.01.13
  */
 public class LocalDateToStringConverter implements Converter<LocalDate, String> {
 
     private static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
 
     @Override
     public String convert(LocalDate source) {
         return source.toString(dateFormat);
     }
 }
