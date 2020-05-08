 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 package org.melati.poem.test;
 
 import org.melati.poem.Group;
 import org.melati.poem.Table;
 import org.melati.poem.CachedSelection;
 
 /**
  * Test the behaviour of CachedSelections in a multithreaded setup.
  * 
  * @see org.melati.poem.CachedSelection
  */
 public class CachedSelectionTest extends PoemTestCase {
 
   /**
    * Constructor.
    */
   public CachedSelectionTest() {
     super();
   }
 
   /**
    * Constructor.
    * @param name
    */
   public CachedSelectionTest(String name) {
     super(name);
   }
   /**
    * {@inheritDoc}
    * @see org.melati.poem.test.PoemTestCase#setUp()
    */
   protected void setUp()
       throws Exception {
     super.setUp();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.test.PoemTestCase#tearDown()
    */
   protected void tearDown()
       throws Exception {
     super.tearDown();
   }
 
   /**
    * @see org.melati.poem.CachedSelection#firstObject()
    */
   public void testFirstObject() {
     CachedSelection cachedSelection = new CachedSelection(getDb().getTableInfoTable(), null, null, null);
     if (!getDb().getDbms().canDropColumns()) {
       return;
     }
     assertEquals("tableinfo/0", cachedSelection.firstObject().toString());
   }
 
   /**
    * @see org.melati.poem.CachedSelection#nth()
    */
   public void testNth() {
     CachedSelection cachedSelection = new CachedSelection(getDb().getTableInfoTable(), null, null, null);
     if (!getDb().getDbms().canDropColumns()) {
       return;
     }
     assertEquals("tableinfo/0", cachedSelection.nth(0).toString());
     assertEquals("tableinfo/7", cachedSelection.nth(6).toString());
     assertNull(cachedSelection.nth(999));
   }
 
   /**
    * Test multi-table selection.
    */
   public void testMultiTableSelection() {
     getDb().uncache();
     Table[] others = new Table[] {getDb().getGroupMembershipTable(),
                                   getDb().getGroupTable()};
     String query =  
     getDb().getUserTable().troidColumn().fullQuotedName() +
     // user.id
     " = 1 AND " +
     getDb().getGroupMembershipTable().getUserColumn().fullQuotedName() +
     //groupmembership.user 
     " = " +
     // user.id 
     getDb().getUserTable().troidColumn().fullQuotedName()   +
     " AND " +
     getDb().getGroupMembershipTable().quotedName()  + "." +
     getDb().getGroupMembershipTable().getGroupColumn().quotedName()
     //groupmembership.group 
     + " = " +  
     //group.id
     getDb().getGroupTable().troidColumn().fullQuotedName() + 
     " AND " + 
     getDb().getGroupTable().troidColumn().fullQuotedName()  +
     // group.id
     " = 0";
    
   // System.err.println("IN test:" + query);
     int count = getDb().getQueryCount();
    // FIXME counts differ between Maven and Eclipse, as cache is persistent 
     
     
     CachedSelection cachedSelection = new CachedSelection(
         getDb().getUserTable(), query, null, others);
     assertEquals(count + 4, getDb().getQueryCount());    
     //getDb().setLogSQL(true);
     assertEquals("_administrator_", cachedSelection.nth(0).toString());
     assertEquals(count + 6, getDb().getQueryCount());    
     assertEquals("_administrator_", cachedSelection.nth(0).toString());
     assertEquals(count + 6, getDb().getQueryCount());
     String currentName = getDb().guestUser().getName();
     String lastQuery = getDb().getLastQuery(); 
     assertEquals(count + 8, getDb().getQueryCount());
     assertEquals(lastQuery, getDb().getLastQuery());
     getDb().guestUser().setName(currentName);
     lastQuery = getDb().getLastQuery();
     assertEquals("_administrator_", cachedSelection.nth(0).toString());
     assertEquals("_administrator_", cachedSelection.nth(0).toString());
     Group g = getDb().getGroupTable().getGroupObject(0);
     g.setName(g.getName());
     assertEquals("_administrator_", cachedSelection.nth(0).toString());
     assertEquals("_administrator_", cachedSelection.nth(0).toString());
     assertEquals("org.melati.poem.CachedSelection " + 
             "SELECT " + getDb().getDbms().getQuotedName("user") + "." + getDb().getDbms().getQuotedName("id") + 
             " FROM " + getDb().getDbms().getQuotedName("user") + ", " + 
             getDb().getDbms().getQuotedName("groupmembership") + ", " + 
             getDb().getDbms().getQuotedName("group") + " WHERE " + 
             "(" + getDb().getDbms().getQuotedName("user") + "." + getDb().getDbms().getQuotedName("id") + 
             " = 1 AND " + getDb().getDbms().getQuotedName("groupmembership") + "." + 
             getDb().getDbms().getQuotedName("user") + " = " + 
             getDb().getDbms().getQuotedName("user") + "." + getDb().getDbms().getQuotedName("id") + " AND " + 
             getDb().getDbms().getQuotedName("groupmembership")  + "." + getDb().getDbms().getQuotedName("group") + 
             " = " + getDb().getDbms().getQuotedName("group") + "." + getDb().getDbms().getQuotedName("id") + 
             " AND " + 
             getDb().getDbms().getQuotedName("group") + "." + getDb().getDbms().getQuotedName("id") + 
             " = 0) ORDER BY " + getDb().getDbms().getQuotedName("user") + "." + getDb().getDbms().getQuotedName("name"), 
             cachedSelection.toString());
     getDb().setLogSQL(false);
   }
   
   /**
    * Test toString. 
    */
   public void testToString() {
     
   }
 }
