 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2008 Tim Pizey
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
  *     Tim Pizey <timp At paneris.org>
  *     http://paneris.org/~timp
  */
 
 package org.melati.test.test;
 
 import org.melati.JettyWebTestCase;
 
 /**
  * @author timp
  * @since  7 Mar 2008
  *
  */
 public class EmailTemplateServletTestTest extends JettyWebTestCase {
 
   /**
    * Constructor.
    * @param name
    */
   public EmailTemplateServletTestTest(String name) {
     super(name);
   }
 
   /**
    * {@inheritDoc}
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
   }
 
   /**
    * {@inheritDoc}
    * @see junit.framework.TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
     super.tearDown();
   }
   /**
    * Will only work on a machine allowed to relay.
    * @throws Exception
    */
   public void testInvoke() throws Exception {
     beginAt("/org.melati.test.EmailTemplateServletTest");
     assertTextPresent("Smtp Server");
     submit();
     assertTextPresent("Check your email");
    
   }
 
  public void testError() throws Exception { 
    // Do it with a db this time for coverage
    beginAt("/org.melati.test.EmailTemplateServletTest/melatitest");
    assertTextPresent("Smtp Server");
    setTextField("to", "timp");
    submit();
    assertTextPresent("Check your email");
    
  }
  
 }
