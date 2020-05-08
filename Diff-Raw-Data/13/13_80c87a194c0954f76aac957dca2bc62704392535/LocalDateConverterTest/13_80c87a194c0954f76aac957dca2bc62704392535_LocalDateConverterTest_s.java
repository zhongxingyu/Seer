 /*
  * Copyright 2011, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.peralta.mycellar.interfaces.web.converters;
 
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
 
 import java.util.Locale;
 
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 
 import fr.peralta.mycellar.interfaces.client.web.converters.LocalDateConverter;
 
 /**
  * @author speralta
  */
 public class LocalDateConverterTest {
 
     private LocalDateConverter localDateConverter;
 
     @Before
     public void setUp() {
         localDateConverter = new LocalDateConverter();
     }
 
     /**
      * Test method for
      * {@link fr.peralta.mycellar.interfaces.client.web.converters.LocalDateConverter#convertToObject(java.lang.String, java.util.Locale)}
      * .
      */
     @Test
     public void testConvertToObject() {
         assertThat(localDateConverter.convertToObject("08/05/2010", Locale.FRENCH),
                 equalTo(new LocalDate(2010, 5, 8)));
        assertThat(localDateConverter.convertToObject("05/08/2010", Locale.ENGLISH),
                 equalTo(new LocalDate(2010, 5, 8)));
     }
 
     /**
      * Test method for
      * {@link org.apache.wicket.util.convert.converter.AbstractConverter#convertToString(java.lang.Object, java.util.Locale)}
      * .
      */
     @Test
     public void testConvertToString() {
         assertThat(localDateConverter.convertToString(new LocalDate(2010, 5, 8), Locale.FRENCH),
                equalTo("08/05/10"));
         assertThat(localDateConverter.convertToString(new LocalDate(2010, 5, 8), Locale.ENGLISH),
                equalTo("5/8/10"));
     }
 
 }
