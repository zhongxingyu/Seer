 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.data.converters;
 
 import org.joda.time.LocalDate;
 import org.springframework.core.convert.converter.Converter;
 
 import java.util.Date;
 
 /**
  * Created by Tomasz Szuba
  * Date: 03.01.13
  */
 public class DateToLocalDateConverter implements Converter<Date, LocalDate> {
     @Override
     public LocalDate convert(Date source) {
         return new LocalDate(source);
     }
 }
