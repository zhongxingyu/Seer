 /*
  * $Id: $
  *
  * Copyright (C) 2009 by Brockmann Consult (info@brockmann-consult.de)
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation. This program is distributed in the hope it will
  * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.esa.beam.chris.operators;
 
 import junit.framework.TestCase;
 
 public class ConversionsTest extends TestCase {
 
     public void testJulDay() {
         double julDay0 = Conversions.julianDate(1999, 11, 26);
        assertEquals(3097648.0, julDay0, 0.0);
     }
 }
