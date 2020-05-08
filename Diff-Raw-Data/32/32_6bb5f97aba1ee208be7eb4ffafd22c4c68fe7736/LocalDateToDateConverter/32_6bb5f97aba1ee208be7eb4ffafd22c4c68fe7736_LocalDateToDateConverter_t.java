 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.data.converters;
 
 import org.joda.time.LocalDate;
 import org.springframework.core.convert.converter.Converter;
 import org.springframework.data.convert.WritingConverter;
 
 import java.util.Date;
 
 /**
 * Converter used to convert between joda's LocalDate and standard JDK Date
 * <p/>
  * Created by Tomasz Szuba
  * Date: 03.01.13
  */
 @WritingConverter
 public class LocalDateToDateConverter implements Converter<LocalDate, Date> {
    /**
     * {@inheritDoc}
     */
     @Override
     public Date convert(LocalDate source) {
         return source.toDate();
     }
 }
