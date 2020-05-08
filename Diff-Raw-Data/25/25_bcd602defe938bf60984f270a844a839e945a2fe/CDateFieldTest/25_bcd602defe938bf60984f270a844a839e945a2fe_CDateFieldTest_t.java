 /*
  * Copyright (C) 2011  Christian Roesch
  * 
  * This file is part of crhcomponents.
  * 
  * crhcomponents is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * crhcomponents is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with crhcomponents.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.croesch.components;
 
 import static com.github.croesch.TestUtil.assertDateHasValues;
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.awt.Dimension;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 
 import javax.swing.JFrame;
 import javax.swing.JTextField;
 import javax.swing.text.BadLocationException;
 
 import org.fest.swing.edt.GuiActionRunner;
 import org.fest.swing.edt.GuiQuery;
 import org.fest.swing.edt.GuiTask;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.fixture.JTextComponentFixture;
 import org.fest.swing.junit.testcase.FestSwingJUnitTestCase;
 import org.junit.Test;
 
 import com.github.croesch.util.Util;
 
 /**
  * Provides test methods for {@link CDateField}.
  * 
  * @author croesch
  * @since Date: Jul 3, 2011
  */
 public class CDateFieldTest extends FestSwingJUnitTestCase {
 
   private JTextComponentFixture field;
 
   private CDateField target;
 
   private Locale l;
 
   @Override
   public void onSetUp() {
     this.l = Locale.getDefault();
     Locale.setDefault(Locale.GERMAN);
 
     robot().settings().eventPostingDelay(50);
     robot().settings().delayBetweenEvents(50);
 
     createDateField(Locale.GERMAN);
   }
 
   private final void createDateField(final Locale l) {
     final FrameFixture f = new FrameFixture(robot(), GuiActionRunner.execute(new GuiQuery<JFrame>() {
 
       @Override
       protected JFrame executeInEDT() throws Throwable {
         final JFrame f = new JFrame();
         f.add(new CDateField(l));
         f.setPreferredSize(new Dimension(100, 50));
         return f;
       }
     }));
     f.show();
     this.field = f.textBox();
     this.target = (CDateField) this.field.target;
   }
 
   @Override
   protected void onTearDown() {
     Locale.setDefault(this.l);
     super.onTearDown();
   }
 
   @Test
   public final void testEnterDate1() {
     this.field.enterText("1");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(1);
     this.field.enterText("2");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(2);
     this.field.enterText(".");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(3);
     this.field.enterText("0");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(4);
     this.field.enterText("1");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(5);
     this.field.enterText(".");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(6);
     this.field.enterText("1");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(7);
     this.field.enterText("9");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(8);
     this.field.enterText("7");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(9);
     this.field.enterText("6");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(10);
     this.field.requireText("12.01.1976");
     assertDateHasValues(((CDateField) this.field.target).getDate(), 12, 1, 1976);
   }
 
   @Test
   public final void testEnterDate2() {
     final Calendar cal = new GregorianCalendar();
     cal.set(1976, 0, 12);
     ((CDateField) this.field.target).setDate(cal.getTime());
     this.field.enterText(".");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(3);
     this.field.enterText("0");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(4);
     this.field.enterText("1");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(5);
     this.field.enterText("1");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(7);
     this.field.enterText("9");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(8);
     this.field.enterText("7");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(9);
     this.field.enterText("6");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(10);
     this.field.requireText("12.01.1976");
     assertDateHasValues(((CDateField) this.field.target).getDate(), 12, 1, 1976);
   }
 
   @Test
   public final void testEnterDate3() {
     this.field.enterText("0");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(1);
     this.field.enterText("1");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(2);
     this.field.enterText("0");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(4);
     this.field.enterText("1");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(5);
     this.field.enterText("0");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(7);
     this.field.enterText("0");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(8);
     this.field.requireText("01.01.2000");
     assertDateHasValues(((CDateField) this.field.target).getDate(), 1, 1, 2000);
 
     this.field.deleteText();
 
     this.field.enterText("4");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(2);
     this.field.enterText("4");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(5);
     this.field.enterText("4");
     this.field.requireText("04.04.2004");
     assertDateHasValues(((CDateField) this.field.target).getDate(), 4, 4, 2004);
 
     this.field.deleteText();
 
     this.field.enterText("2");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(1);
     this.field.enterText("9");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(2);
     this.field.enterText("2");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(5);
     this.field.enterText("9");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(7);
     this.field.enterText("0");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(8);
     this.field.enterText("5");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(9);
     this.field.requireText("29.02.1905");
     assertDateHasValues(((CDateField) this.field.target).getDate(), 1, 3, 1905);
   }
 
   @Test
   public final void testPasteText1() {
     final Calendar cal = new GregorianCalendar();
     cal.set(2000, 0, 1);
     ((CDateField) this.field.target).setDateAndDisplay(cal.getTime());
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     this.field.requireText("01.01.2000");
     assertDateHasValues(((CDateField) this.field.target).getDate(), 1, 1, 2000);
 
     GuiActionRunner.execute(new GuiTask() {
 
       @Override
       protected void executeInEDT() throws Throwable {
         final JTextField tf;
         tf = new JTextField("444");
         tf.selectAll();
         tf.copy();
         CDateFieldTest.this.field.target.paste();
       }
 
     });
     this.field.requireText("04.04.2004");
     assertDateHasValues(((CDateField) this.field.target).getDate(), 4, 4, 2004);
   }
 
   @Test
   public final void testPasteText2() {
     final FrameFixture f = new FrameFixture(robot(), GuiActionRunner.execute(new GuiQuery<JFrame>() {
 
       @Override
       protected JFrame executeInEDT() throws Throwable {
         final JFrame f = new JFrame();
         f.add(new CDateField());
         f.setPreferredSize(new Dimension(100, 50));
         return f;
       }
     }));
     f.show();
     this.field = f.textBox();
 
     this.field.enterText("010100");
     this.field.requireText("01.01.2000");
     assertDateHasValues(((CDateField) this.field.target).getDate(), 1, 1, 2000);
 
     this.field.deleteText();
 
     GuiActionRunner.execute(new GuiTask() {
 
       @Override
       protected void executeInEDT() throws Throwable {
         final JTextField tf;
         tf = new JTextField("444");
         tf.selectAll();
         tf.copy();
         CDateFieldTest.this.field.target.paste();
       }
 
     });
     this.field.requireText("04.04.2004");
     assertDateHasValues(((CDateField) this.field.target).getDate(), 4, 4, 2004);
   }
 
   @Test
   public void testInsertString() throws BadLocationException {
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("10.01.1988");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("02355");
     this.field.requireText("02.03.1955");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("28.02.1955");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
 
     this.field.enterText(Util.of("-").toStringMultipliedWith(35));
     this.field.requireText("24.01.1955");
     assertDateHasValues(this.target.getDate(), 24, 1, 1955);
   }
 
   @Test
   public void testInsertString_DMY_Slash() throws BadLocationException {
     createDateField(Locale.CANADA);
 
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("10/01/1988");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("02355");
     this.field.requireText("02/03/1955");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("28/02/1955");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public void testInsertString_DMY_Point() throws BadLocationException {
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("10.01.1988");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("02355");
     this.field.requireText("02.03.1955");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("28.02.1955");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public void testInsertString_YMD_Slash() throws BadLocationException {
     createDateField(new Locale("ga"));
 
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("1988/01/10");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
    this.field.enterText("55/302");
     this.field.requireText("1955/03/02");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("1955/02/28");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public void testInsertString_DMY_Slash2() throws BadLocationException {
     createDateField(new Locale("it"));
 
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("10/01/1988");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("02355");
     this.field.requireText("02/03/1955");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("28/02/1955");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public void testInsertString_MDY() throws BadLocationException {
     createDateField(new Locale("en"));
 
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("01/10/1988");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("30255");
     this.field.requireText("03/02/1955");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("02/28/1955");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public void testInsertString_YMD_Minus() throws BadLocationException {
     createDateField(new Locale("sv"));
 
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("1988-01-10");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("55-302");
     this.field.requireText("1955-03-02");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("1955-02-28");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public void testInsertString_YMD_Point() throws BadLocationException {
     createDateField(new Locale("hu"));
 
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("1988.01.10.");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("55.302");
     this.field.requireText("1955.03.02.");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("1955.02.28.");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public void testInsertString_DMY_Minus() throws BadLocationException {
     createDateField(new Locale("nl"));
 
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("10-01-1988");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("02355");
     this.field.requireText("02-03-1955");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("28-02-1955");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public void testInsertString_DMY_Point2() throws BadLocationException {
     createDateField(new Locale("sr"));
 
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("10.01.1988.");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("02355");
     this.field.requireText("02.03.1955.");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("28.02.1955.");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public void testInsertString_YMD_Point2() throws BadLocationException {
     createDateField(new Locale("lt"));
 
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("1988.01.10");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("55.302");
     this.field.requireText("1955.03.02");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("1955.02.28");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public void testInsertString_YDM_Point() throws BadLocationException {
     createDateField(new Locale("lv"));
 
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("1988.10.01");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("55.023");
     this.field.requireText("1955.02.03");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("1955.28.02");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public void testInsertString_MDY_Minus() throws BadLocationException {
     createDateField(new Locale("es", "NI"));
 
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("01-10-1988");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("30255");
     this.field.requireText("03-02-1955");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("02-28-1955");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public void testInsertString_YMD_Point3() throws BadLocationException {
     createDateField(new Locale("ko"));
 
     final Calendar cal = new GregorianCalendar();
     cal.set(2002, 11, 6);
     this.target.setDate(cal.getTime());
     this.field.requireEmpty();
     assertDateHasValues(this.target.getDate(), 6, 12, 2002);
 
     cal.set(1988, 0, 10);
     this.target.setDateAndDisplay(cal.getTime());
     this.field.requireText("1988. 01. 10");
     assertThat(this.field.target.getCaretPosition()).isEqualTo(0);
     assertDateHasValues(this.target.getDate(), 10, 1, 1988);
 
     this.field.enterText("55. 302");
     this.field.requireText("1955. 03. 02");
     assertDateHasValues(this.target.getDate(), 2, 3, 1955);
 
     this.field.enterText("--");
     this.field.requireText("1955. 02. 28");
     assertDateHasValues(this.target.getDate(), 28, 2, 1955);
   }
 
   @Test
   public final void testGetDate() {
     this.field.enterText("010100");
     assertDateHasValues(((CDateField) this.field.target).getDate(), 1, 1, 2000);
 
     this.field.deleteText();
     assertDateHasValues(((CDateField) this.field.target).getDate(), 1, 1, 2000);
   }
 
   @Test
   public final void testGetDateOrNull() {
     this.field.enterText("010100");
     assertDateHasValues(((CDateField) this.field.target).getDateOrNull(), 1, 1, 2000);
     assertThat(((CDateField) this.field.target).getDateOrNull()).isNotNull();
 
     this.field.deleteText();
     assertThat(((CDateField) this.field.target).getDateOrNull()).isNull();
   }
 
   @Test
   public final void testGetDateWithoutTime() {
     this.field.enterText("010100");
    assertThat(((CDateField) this.field.target).getDateWithoutTime()).isEqualTo(Util.of(((CDateField) this.field.target).getDate())
                                                                                    .getDateWithoutTime());
     final Calendar cal = new GregorianCalendar();
     cal.setTime(((CDateField) this.field.target).getDateWithoutTime());
     assertThat(cal.get(Calendar.HOUR_OF_DAY)).isZero();
     assertThat(cal.get(Calendar.MINUTE)).isZero();
     assertThat(cal.get(Calendar.SECOND)).isZero();
     assertThat(cal.get(Calendar.MILLISECOND)).isZero();
 
     this.field.deleteText();
    assertThat(((CDateField) this.field.target).getDateWithoutTime()).isEqualTo(Util.of(((CDateField) this.field.target).getDate())
                                                                                    .getDateWithoutTime());
   }
 
   @Test
   public final void testGetDateWithoutTimeOrNull() {
     this.field.enterText("010100");
    assertThat(((CDateField) this.field.target).getDateWithoutTimeOrNull()).isEqualTo(Util.of(((CDateField) this.field.target).getDate())
                                                                                          .getDateWithoutTime());
     final Calendar cal = new GregorianCalendar();
     cal.setTime(((CDateField) this.field.target).getDateWithoutTimeOrNull());
     assertThat(cal.get(Calendar.HOUR_OF_DAY)).isZero();
     assertThat(cal.get(Calendar.MINUTE)).isZero();
     assertThat(cal.get(Calendar.SECOND)).isZero();
     assertThat(cal.get(Calendar.MILLISECOND)).isZero();
 
     this.field.deleteText();
     assertThat(((CDateField) this.field.target).getDateWithoutTimeOrNull()).isNull();
   }
 }
