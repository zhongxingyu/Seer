 /*
  *  Copyright (C) 2011 Carl Green
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package info.carlwithak.mpxg2.model;
 
 import java.beans.IntrospectionException;
 import org.junit.Before;
 import org.junit.Test;
 
 import static info.carlwithak.mpxg2.model.Util.testBean;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 /**
  * Test Patch using bean tester where possible. Hard code other tests.
  *
  * @author Carl Green
  */
 public class PatchTest {
 
     Patch patch;
 
     @Before
     public void setup() {
         patch = new Patch();
     }
 
     @Test
     public void testPatch() throws IntrospectionException {
         testBean(Patch.class);
     }
 
     @Test
     public void testGetAndSetSource() {
         assertEquals(0, patch.getSourceIndex());
         patch.setSource(157);
         assertEquals(157, patch.getSourceIndex());
     }
 
     @Test
     public void testGetSourceName() {
         assertNull(patch.getSourceName());
         patch.setSource(Patch.PatchSource.MIDI_CC19.ordinal());
        assertEquals("MIDI CC19", patch.getSourceName());
         patch.setSource(Patch.PatchSource.CTLS_AB.ordinal());
         assertEquals("Ctls A/B", patch.getSourceName());
     }
 
     @Test
     public void testGetAndSetDestinationEffect() {
         assertNull(patch.getDestinationEffectIndex());
         patch.setDestinationEffect(4);
         assertEquals(4, patch.getDestinationEffectIndex().intValue());
         patch.setDestinationEffect(null);
         assertNull(patch.getDestinationEffectIndex());
     }
 
     @Test
     public void testGetDestinationEffectName() {
         assertNull(patch.getDestinationEffectName());
         patch.setDestinationEffect(Patch.PatchDestination.EFFECT_1.ordinal());
         assertEquals("Effect 1", patch.getDestinationEffectName());
     }
 
 }
