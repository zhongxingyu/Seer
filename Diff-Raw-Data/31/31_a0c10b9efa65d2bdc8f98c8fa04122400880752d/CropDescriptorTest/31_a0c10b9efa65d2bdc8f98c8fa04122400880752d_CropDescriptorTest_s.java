 /*
  *    GeoTools - The Open Source Java GIS Toolkit
  *    http://geotools.org
  *
  *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation;
  *    version 2.1 of the License.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  */
 package it.geosolutions.nrl.model;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 /**
  * @author Admin
  *
  */
 public class CropDescriptorTest {
     @Test
     public void seasonSetTest(){
         CropDescriptor cd = new CropDescriptor();
         Season k = Season.KHARIF;
         Season rk = Season.RABI_KHARIF;
         Season r = Season.RABI;
         Season n = Season.NONE;
         
         cd.setSeasons(k);
         assertTrue(cd.getSeasons().length ==1);
         assertEquals(cd.getSeasons()[0], "KHARIF");
         
         cd.setSeasons(rk);
         assertTrue(cd.getSeasons().length==2);
         cd.setSeasons(n);
         assertTrue(cd.getSeasons().length ==0);
         
         String[] sr = r.toArray();
         cd.setSeasons(sr);
        assertEquals(r.toString(), cd.getSeasons()[0]);
         String[] sk = k.toArray();
         cd.setSeasons(sk);
        assertEquals(k.toString(), cd.getSeasons()[0]);
 
     }
 
 }
