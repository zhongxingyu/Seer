 /* ElementStructure8.java -- 
    Copyright (C) 2006 Red Hat
 This file is part of Mauve.
 
 Mauve is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.
 
 Mauve is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Mauve; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.
 
 */
 
// Tags: FIXME
 
 package gnu.testlet.javax.swing.text.DefaultStyledDocument.ElementBuffer;
 
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Element;
 
 import gnu.testlet.TestHarness;
 import gnu.testlet.Testlet;
 
 public class ElementStructure8 implements Testlet
 {
   /**
    * Starts the test run.
    *
    * @param harness the test harness to use
    */
   public void test(TestHarness harness)
   {
     try
       {
         DefaultStyledDocument doc = new DefaultStyledDocument();
         Element root = doc.getDefaultRootElement();
         doc.insertString(0, "first line of text. \n", null);
         harness.check(root.getElementCount() == 2);
         harness.check(root.getElement(0).getStartOffset() == 0);
         harness.check(root.getElement(0).getEndOffset() == 21);
         harness.check(root.getElement(1).getStartOffset() == 21);
         harness.check(root.getElement(1).getEndOffset() == 22);
         doc.insertString(21, "second line of text. \n third line of text. \n", null);
         harness.check(root.getElementCount() == 4);
         harness.check(root.getElement(0).getElementCount() == 1);
         harness.check(root.getElement(1).getElementCount() == 1);
         harness.check(root.getElement(2).getElementCount() == 1);
 
         Element first = root.getElement(0).getElement(0);
         harness.check(first.getStartOffset() == 0);
         harness.check(first.getEndOffset() == 21);
 
         Element second = root.getElement(1).getElement(0);
         harness.check(second.getStartOffset() == 21);
         harness.check(second.getEndOffset() == 43);
 
         Element third = root.getElement(2).getElement(0);
         harness.check(third.getStartOffset() == 43);
         harness.check(third.getEndOffset() == 65);
 
         Element fourth = root.getElement(3).getElement(0);
         harness.check(fourth.getStartOffset() == 65);
         harness.check(fourth.getEndOffset() == 66);
         
         
         doc = new DefaultStyledDocument();
         root = doc.getDefaultRootElement();
         doc.insertString(0, "first line of text. \n", null);
         harness.check(root.getElementCount() == 2);
         harness.check(root.getElement(0).getStartOffset() == 0);
         harness.check(root.getElement(0).getEndOffset() == 21);
         harness.check(root.getElement(1).getStartOffset() == 21);
         harness.check(root.getElement(1).getEndOffset() == 22);
         doc.insertString(21, "\n second line of text. \n third line of text. \n", null);
         harness.check(root.getElementCount() == 5);
         harness.check(root.getElement(0).getElementCount() == 1);
         harness.check(root.getElement(1).getElementCount() == 1);
         harness.check(root.getElement(2).getElementCount() == 1);
         harness.check(root.getElement(3).getElementCount() == 1);
 
         first = root.getElement(0).getElement(0);
         harness.check(first.getStartOffset() == 0);
         harness.check(first.getEndOffset() == 21);
 
         second = root.getElement(1).getElement(0);
         harness.check(second.getStartOffset() == 21);
         harness.check(second.getEndOffset() == 22);
 
         third = root.getElement(2).getElement(0);
         harness.check(third.getStartOffset() == 22);
         harness.check(third.getEndOffset() == 45);
 
         fourth = root.getElement(3).getElement(0);
         harness.check(fourth.getStartOffset() == 45);
         harness.check(fourth.getEndOffset() == 67);
         
         Element fifth = root.getElement(4).getElement(0);
         harness.check(fifth.getStartOffset() == 67);
         harness.check(fifth.getEndOffset() == 68);
         
         
         doc = new DefaultStyledDocument();
         root = doc.getDefaultRootElement();
         doc.insertString(0, "\n second line of text. \n third line of text. \n", null);
         harness.check(root.getElementCount() == 4);
         harness.check(root.getElement(0).getElementCount() == 1);
         harness.check(root.getElement(1).getElementCount() == 1);
         harness.check(root.getElement(2).getElementCount() == 1);
         harness.check(root.getElement(3).getElementCount() == 1);
 
         first = root.getElement(0).getElement(0);
         harness.check(first.getStartOffset() == 0);
         harness.check(first.getEndOffset() == 1);
 
         second = root.getElement(1).getElement(0);
         harness.check(second.getStartOffset() == 1);
         harness.check(second.getEndOffset() == 24);
 
         third = root.getElement(2).getElement(0);
         harness.check(third.getStartOffset() == 24);
         harness.check(third.getEndOffset() == 46);
 
         fourth = root.getElement(3).getElement(0);
         harness.check(fourth.getStartOffset() == 46);
         harness.check(fourth.getEndOffset() == 47);
       }
     catch (Throwable t)
       {
         harness.fail(t.getCause() + " Exception caught!");
       }
   }
 }
