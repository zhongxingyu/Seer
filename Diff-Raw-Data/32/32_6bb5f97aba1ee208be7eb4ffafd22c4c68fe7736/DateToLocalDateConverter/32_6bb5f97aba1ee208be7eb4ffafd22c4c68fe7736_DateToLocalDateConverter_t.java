 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.data.converters;
 
 import org.joda.time.LocalDate;
 import org.springframework.core.convert.converter.Converter;
 
 import java.util.Date;
 
 /**
 * Converter used to Converter between standard java date and Joda's LocalTime
 * <p/>
  * Created by Tomasz Szuba
  * Date: 03.01.13
  */
 public class DateToLocalDateConverter implements Converter<Date, LocalDate> {
    /**
     * {@inheritDoc}
     */
     @Override
     public LocalDate convert(Date source) {
         return new LocalDate(source);
     }
 }
