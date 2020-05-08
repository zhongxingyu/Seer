 // Copyright (C) 2000 Paco Gomez
 // Copyright (C) 2000, 2001, 2002 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.tools.tcpsniffer;
 
 import junit.framework.TestCase;
 import junit.swingui.TestRunner;
 //import junit.textui.TestRunner;
 
 import net.grinder.common.GrinderException;
 
 
 /**
  * Unit test case for <code>ConnectionDetails</code>.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestConnectionDetails extends TestCase
 {
     public static void main(String[] args)
     {
 	TestRunner.run(TestConnectionDetails.class);
     }
 
     public TestConnectionDetails(String name)
     {
 	super(name);
     }
 
     protected void setUp() throws Exception
     {
     }
 
     public void testGetDescription() throws Exception
     {
 	final ConnectionDetails connectionDetails =
 	    new ConnectionDetails("one", 55, "two", 121, true);
 
 	assertEquals("one:55->two:121", connectionDetails.getDescription());
     }
 
     public void testGetURLBase() throws Exception
     {
 	final ConnectionDetails connectionDetails =
 	    new ConnectionDetails("one", 55, "two", 121, true);
 
 	assertEquals("https://two:121", connectionDetails.getURLBase("http"));
     }
 
     public void testEquality() throws Exception
     {
 	final ConnectionDetails[] connectionDetails = {
 	    new ConnectionDetails("A", 55, "B", 80, false),
 	    new ConnectionDetails("a", 55, "B", 80, false),
 	    new ConnectionDetails("c", 55, "B", 80, false),
 	    new ConnectionDetails("a", 55, "B", 80, true),
 	    new ConnectionDetails("a", 56, "B", 80, false),
 	};
 
 	assertEquals(connectionDetails[0], connectionDetails[0]);
 	assertEquals(connectionDetails[0], connectionDetails[1]);
 	assertEquals(connectionDetails[1], connectionDetails[0]);
 	assertTrue(!connectionDetails[0].equals(connectionDetails[2]));
 	assertTrue(!connectionDetails[1].equals(connectionDetails[3]));
 	assertTrue(!connectionDetails[1].equals(connectionDetails[4]));
     }
 }
