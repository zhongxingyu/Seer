 // Tags: GUI JDK1.1
 
 // Copyright (C) 2005 Red Hat
 
 // Mauve is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2, or (at your option)
 // any later version.
 
 // Mauve is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with Mauve; see the file COPYING.  If not, write to
 // the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 // Boston, MA 02110-1301 USA.
 import gnu.testlet.TestHarness;
 import gnu.testlet.Testlet;
 
import javax.swing.JDialog;
 
 public class size implements Testlet {
 
   /**
    * Runs the test using the specified harness.
    * 
    * @param harness  the test harness (<code>null</code> not permitted).
    */
   public void test(TestHarness harness)      
   {
    JDialog jd = new JDialog();
     jd.show();
 
     // jd insets may be larger than preferred size
     Dimension pref = jd.getPreferredSize();
     Dimension size = jd.getSize();
    if (size.width >= pref.width && size.height >= pref.height)
	harness.fail("Dialog should be shown at preferred size when pack is not called before show.");      
   }
 }
