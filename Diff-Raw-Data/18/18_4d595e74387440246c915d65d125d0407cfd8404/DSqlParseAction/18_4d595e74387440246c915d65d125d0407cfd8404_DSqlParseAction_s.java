 /*
  * Copyright (C) 2012 Timo Vesalainen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.vesalainen.parsers.sql.dsql.ui.action;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JOptionPane;
 import javax.swing.JTextPane;
 import javax.swing.Timer;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.text.StyledEditorKit;
 import javax.swing.text.StyledEditorKit.ForegroundAction;
 import org.vesalainen.parser.util.InputReader;
 import org.vesalainen.parser.util.OffsetLocatorException;
 import org.vesalainen.parsers.sql.Engine;
 import org.vesalainen.parsers.sql.ErrorReporter;
 import org.vesalainen.parsers.sql.ErrorReporter.Level;
 import org.vesalainen.parsers.sql.SQLLocator;
 import org.vesalainen.parsers.sql.SelectStatement;
 import org.vesalainen.parsers.sql.Statement;
 import org.vesalainen.parsers.sql.dsql.ui.I18n;
 import org.vesalainen.parsers.sql.dsql.ui.WorkBench;
 
 /**
  * @author Timo Vesalainen
  */
 public class DSqlParseAction extends AbstractAction implements DocumentListener, SQLLocator, ErrorReporter, UndoableEditListener
 {
     public static final String PropertyName = "statement";
     private final Timer timer;
     private WorkBench workBench;
     private UndoableEditListener listener;
     private boolean off;
     private JTextPane sqlPane;
     private final ForegroundAction blackAction;
     private final ForegroundAction redAction;
     private final ForegroundAction blueAction;
     private final ForegroundAction grayAction;
     private final ForegroundAction orangeAction;
     private Level errorLevel;
     private InputReader reader;
     private boolean readonly;
     
     public DSqlParseAction(WorkBench workBench, UndoableEditListener listener, boolean readonly)
     {
         super(null);
         this.workBench = workBench;
         this.listener = listener;
         this.readonly = readonly;
         timer = new Timer(500, this);
         timer.stop();
         timer.setRepeats(false);
         timer.setActionCommand("TimerAction");
         
         blackAction = new StyledEditorKit.ForegroundAction("Black", Color.BLACK);
         redAction = new StyledEditorKit.ForegroundAction("Red", Color.red);
         blueAction = new StyledEditorKit.ForegroundAction("Blue", Color.BLUE);
         grayAction = new StyledEditorKit.ForegroundAction("Green", Color.LIGHT_GRAY);
         orangeAction = new StyledEditorKit.ForegroundAction("Orange", Color.ORANGE);
         
     }
 
     @Override
     public void actionPerformed(ActionEvent e)
     {
         if (timer.getActionCommand().equals(e.getActionCommand()))
         {
             firePropertyChange(PropertyName, null, null);
             sqlPane = workBench.getActiveTextPane();
             sqlPane.setToolTipText("");
             color(blackAction, 0, sqlPane.getDocument().getLength());
             String sql = sqlPane.getText();
             if (!sql.isEmpty())
             {
                 if (reader == null)
                 {
                     reader = new InputReader(sql);
                 }
                 else
                 {
                     reader.reuse(sql);
                 }
                 try
                 {
                     errorLevel = ErrorReporter.Level.Ok;
                     Engine engine = workBench.getEngine();
                     engine.check(reader, this);
                     Statement statement = engine.prepare(sql);
                     if (readonly)
                     {
                        if (!(statement instanceof SelectStatement))
                         {
                             JOptionPane.showMessageDialog(workBench.getFrame(), I18n.get("IN READONLY MODE"), I18n.get("REFUSED"), JOptionPane.ERROR_MESSAGE);
                             return;
                         }
                     }
                     statement.check(engine, this);
                     if (errorLevel != Level.Fatal)
                     {
                         firePropertyChange(PropertyName, null, statement);
                     }
                 }
                 catch (OffsetLocatorException ex)
                 {
                     sqlPane.setToolTipText(ex.getLocalizedMessage());
                     color(redAction, ex.getStart(), ex.getEnd());
                 }
             }
         }
     }
 
     private void color(Action action, int start, int end)
     {
         try
         {
             setOff();
             int save = sqlPane.getCaretPosition();
             sqlPane.setCaretPosition(start);
             sqlPane.moveCaretPosition(end);
             action.actionPerformed(null);
             sqlPane.setCaretPosition(save);
             setOn();
         }
         catch (IllegalArgumentException ex)
         {
             ex.printStackTrace();
         }
     }
 
     @Override
     public void locate(int start, int end, SQLLocator.Type type)
     {
         switch (type)
         {
             case COMMENT:
                 color(grayAction, start, end);
                 break;
             case RESERVED_WORD:
                 color(blueAction, start, end);
                 break;
             default:
                 assert false;
         }
     }
 
     @Override
     public void report(String message, ErrorReporter.Level level, String source, int start, int end)
     {
         switch (level)
         {
             case Fatal:
                 color(redAction, start, end);
                 break;
             case Hint:
                 color(orangeAction, start, end);
                 break;
             default:
                 assert false;
         }
         sqlPane.setToolTipText(message);
         if (level.ordinal() > errorLevel.ordinal())
         {
             errorLevel = level;
         }
     }
 
     @Override
     public void replace(String newText, int start, int end)
     {
         setOff();
         int save = sqlPane.getCaretPosition();
         sqlPane.setCaretPosition(start);
         sqlPane.moveCaretPosition(end);
         sqlPane.replaceSelection(newText);
         sqlPane.setCaretPosition(save);
         setOn();
     }
 
     @Override
     public void insertUpdate(DocumentEvent e)
     {
         timer.restart();
     }
 
     @Override
     public void removeUpdate(DocumentEvent e)
     {
         timer.restart();
     }
 
     @Override
     public void changedUpdate(DocumentEvent e)
     {
         if (!off)
         {
             timer.restart();
         }
     }
 
     public void setOn()
     {
         this.off = false;
     }
     
     public void setOff()
     {
         this.off = true;
     }
     
     @Override
     public void undoableEditHappened(UndoableEditEvent e)
     {
         if (!off)
         {
             listener.undoableEditHappened(e);
         }
     }
 
 }
