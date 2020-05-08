 /*
  * This program is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this 
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
  * or from the Free Software Foundation, Inc., 
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright 2011 Pentaho Corporation.  All rights reserved.
  *
  * Created Set 17th, 2012
  * @author Pedro Vale (pedro.vale@webdetails.pt)
  */
 package org.pentaho.telemetry;
 
 import java.util.concurrent.TimeUnit;
 import junit.framework.Assert;
 import org.junit.Test;
 
 
 
 public class TelemetryEventTest {
 
   
   @Test
   public void TestEventOrdering() {
    TelemetryEvent te = new TelemetryEvent("url", 500, TimeUnit.MINUTES);
    TelemetryEvent te2 = new TelemetryEvent("url", 400, TimeUnit.MINUTES);
     
     Assert.assertEquals(-1, te2.compareTo(te));
     
     Assert.assertEquals(0, te2.compareTo(te2));
     
     Assert.assertEquals(1, te.compareTo(te2));
     
     
   }
 }
