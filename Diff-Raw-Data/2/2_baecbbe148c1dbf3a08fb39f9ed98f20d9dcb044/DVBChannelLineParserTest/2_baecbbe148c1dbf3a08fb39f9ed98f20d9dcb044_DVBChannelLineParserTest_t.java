 /* $Id$
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its 
  *    contributors may be used to endorse or promote products derived from this 
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package org.hampelratte.svdrp.responses.highlevel;
 
 import static junit.framework.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class DVBChannelLineParserTest {
 
     private String channelData = "1 Das Erste:11836:B8C23D12M64T2G32Y0:S19.2E:27500:101:102=deu,103=2ch;106=deu:104:0:28106:1:1101:0";
 
     private ChannelLineParser parser = new DVBChannelLineParser();
 
     private DVBChannel chan;
 
     @Before
     public void parseLine() {
         chan = (DVBChannel) parser.parse(channelData);
     }
 
     @Test
     public void testChannelNumber() {
         assertEquals(1, chan.getChannelNumber());
     }
 
     @Test
     public void testFrequency() {
         assertEquals(11836, chan.getFrequency());
     }
 
     @Test
     public void testParameters() {
         assertEquals(8, chan.getBandwidth());
         assertEquals(23, chan.getCodeRateHP());
         assertEquals(12, chan.getCodeRateLP());
         assertEquals(32, chan.getGuardInterval());
         assertFalse(chan.isHorizontalPolarization());
         assertEquals(64, chan.getModulation());
         assertEquals(2, chan.getTransmissionMode());
         assertFalse(chan.isVerticalPolarization());
         assertEquals(0, chan.getHierarchy());
     }
 
     @Test
     public void testSource() {
         assertEquals("S19.2E", chan.getSource());
     }
 
     @Test
     public void testSymbolRate() {
         assertEquals(27500, chan.getSymbolRate());
     }
 
     @Test
     public void testVPID() {
         assertEquals("101", chan.getVPID());
     }
 
     @Test
     public void testAPID() {
         assertEquals("102=deu,103=2ch;106=deu", chan.getAPID());
     }
 
     @Test
     public void testTPID() {
         assertEquals("104", chan.getTPID());
     }
     
     @Test
     public void testConditionalAccess() {
        assertEquals(new Integer(0), chan.getConditionalAccess().get(0));
     }
 
     @Test
     public void testSID() {
         assertEquals(28106, chan.getSID());
     }
 
     @Test
     public void testNID() {
         assertEquals(1, chan.getNID());
     }
 
     @Test
     public void testTID() {
         assertEquals(1101, chan.getTID());
     }
 
     @Test
     public void testRID() {
         assertEquals(0, chan.getRID());
     }
 }
