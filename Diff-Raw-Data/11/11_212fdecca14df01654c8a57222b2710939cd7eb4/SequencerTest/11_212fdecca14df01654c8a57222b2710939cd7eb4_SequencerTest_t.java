 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded;
 
 import com.flexive.core.storage.StorageManager;
 import com.flexive.shared.CustomSequencer;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxSystemSequencer;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLogoutFailedException;
 import com.flexive.shared.interfaces.SequencerEngine;
 import org.apache.commons.lang.RandomStringUtils;
 import org.testng.Assert;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.util.List;
 
 import static com.flexive.tests.embedded.FxTestUtils.login;
 import static com.flexive.tests.embedded.FxTestUtils.logout;
 
 /**
  * Sequencer tests
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"ejb", "structure", "sequencer"})
 public class SequencerTest {
 
     private SequencerEngine id;
 
     @BeforeClass
     public void beforeClass() throws Exception {
         id = EJBLookup.getSequencerEngine();
         login(TestUsers.SUPERVISOR);
     }
 
     @AfterClass
     public void afterClass() throws FxLogoutFailedException {
         logout();
     }
 
     @Test
     public void systemSequencer() throws Exception {
         long next = id.getId(FxSystemSequencer.MANDATOR);
         long start = next - 1;
         //curr >= next and not curr == next due to possible caching from the database (eg postgres)
         long curr = id.getCurrentId(FxSystemSequencer.MANDATOR);
         Assert.assertTrue(curr >= next, curr + " is expected to be >= " + next);
         StorageManager.getSequencerStorage().setSequencerId(FxSystemSequencer.MANDATOR.getSequencerName(), start);
         long reset = id.getCurrentId(FxSystemSequencer.MANDATOR);
         Assert.assertTrue(start == reset || start == (reset - 1), "Expected start to be reset or reset-1. start=" + start + ", reset=" + reset);
     }
 
     @Test
     public void customSequencer() throws Exception {
         List<CustomSequencer> startList = id.getCustomSequencers();
         String seq1 = "A" + RandomStringUtils.randomAlphanumeric(16).toUpperCase();
         String seq2 = "B" + RandomStringUtils.randomAlphanumeric(16).toUpperCase();
         String seq3 = "C" + RandomStringUtils.randomAlphanumeric(16).toUpperCase();
         id.createSequencer(seq1, true, 0);
         id.createSequencer(seq2, true, id.getMaxId());
         id.createSequencer(seq3, false, id.getMaxId());
         Assert.assertTrue(id.sequencerExists(seq1), "Expected sequencer " + seq1 + " to exist!");
         Assert.assertTrue(id.sequencerExists(seq2), "Expected sequencer " + seq2 + " to exist!");
         Assert.assertTrue(id.sequencerExists(seq3), "Expected sequencer " + seq3 + " to exist!");
         long i1 = id.getId(seq1);
        Assert.assertEquals(i1, 1);
         long i2 = id.getId(seq1);
         Assert.assertTrue(i2 > i1, "Expected a higher id after 2nd getId()!");
         i1 = id.getId(seq2); //call should cause the sequencer to roll over
         Assert.assertEquals(i1, 0, "Expected: " + 0 + ", got: " + i1);
         i1 = id.getId(seq2); //should be 1 after rollover
         Assert.assertEquals(i1, 1, "Expected: " + 1 + ", got: " + i1);
         Assert.assertTrue(i1 <= id.getCurrentId(seq2), "Expected " + i1 + " of " + seq2 + " to be <= " + id.getCurrentId(seq2));
         try {
             id.getId(seq3);
             Assert.fail("Expected an exception since seq3 should be exhausted!");
         } catch (FxApplicationException e) {
             //expected
         }
 
         List<CustomSequencer> g2 = id.getCustomSequencers();
         g2.removeAll(startList);
         Assert.assertTrue(g2.size() == 3, "Expected a size of 3, but got " + g2.size());
         Assert.assertTrue(g2.get(0).getName().equals(seq1), "Expected " + seq1 + " got " + g2.get(0).getName());
         Assert.assertTrue(g2.get(1).getName().equals(seq2), "Expected " + seq2 + " got " + g2.get(1).getName());
         Assert.assertTrue(g2.get(2).getName().equals(seq3), "Expected " + seq3 + " got " + g2.get(2).getName());
         Assert.assertTrue(g2.get(0).isAllowRollover());
         Assert.assertTrue(g2.get(1).isAllowRollover());
         Assert.assertFalse(g2.get(2).isAllowRollover());
         id.removeSequencer(seq1);
         id.removeSequencer(seq2);
         id.removeSequencer(seq3);
         Assert.assertTrue(id.getCustomSequencers().size() == startList.size());
     }
 }
