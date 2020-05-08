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
 
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import java.awt.event.ActionEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyVetoException;
 import java.beans.VetoableChangeListener;
 import java.beans.VetoableChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 import org.vesalainen.parsers.sql.FetchResult;
 import org.vesalainen.parsers.sql.dsql.DSQLEngine;
 import org.vesalainen.parsers.sql.dsql.ui.I18n;
 import org.vesalainen.parsers.sql.dsql.ui.ListDialog;
 import org.vesalainen.parsers.sql.dsql.ui.WorkBench;
 
 /**
  * @author Timo Vesalainen
  */
 public class PersistenceHandler
 {
     public static final String OPEN = "open";
     public static final String SAVE = "save";
     
     private VetoableChangeSupport changeSupport;
     private WorkBench workBench;
     private DSQLEngine engine;
     private String storedStatementsKind;
     private FetchResult statements;
     private static StatementDialog dialog;
     private Entity entity;
     private Action[] actions;
     private Action newAction;
     private Action opentAction;
     private Action saveAction;
     private Action saveAsAction;
     private Action removeAction;
     private AutoAction executeAction;
     private List<AutoAction> autoActions = new ArrayList<>();;
 
     public PersistenceHandler(WorkBench workBench, String storedStatementsKind)
     {
         this.workBench = workBench;
         engine = workBench.getEngine();
         changeSupport = new VetoableChangeSupport(this);
         this.storedStatementsKind = storedStatementsKind;
         newAction = new NewStatementAction();
         opentAction = new OpenStatementAction();
         saveAction = new SaveStatementAction();
         saveAsAction = new SaveAsStatementAction();
         removeAction = new RemoveStatementAction();
         actions = new Action[] {
             newAction,
             opentAction,
             saveAction,
             saveAsAction,
             removeAction
         };
     }
 
     public void setExecuteAction(AutoAction executeAction)
     {
         this.executeAction = executeAction;
     }
 
     public void addAutoAction(AutoAction autoAction)
     {
         autoActions.add(autoAction);
     }
     
     public Action[] getActions()
     {
         return actions;
     }
     private boolean confirmInstall()
     {
         if (statements == null)
         {
             statements = engine.execute("select key from "+storedStatementsKind+";");
         }
         if (statements.getRowCount() == 0)
         {
             int confirm = JOptionPane.showConfirmDialog(
                     workBench.getFrame(), 
                     String.format(I18n.get("CREATESTOREDSTATEMENTSKIND"), storedStatementsKind),
                     I18n.get("CONNECTED DATASTORE DOESN'T HAVE STORE FOR SAVED STATEMENTS"), 
                     JOptionPane.OK_CANCEL_OPTION
                     );
             return JOptionPane.YES_OPTION == confirm;
         }
         return true;
     }
     public void open(boolean embed)
     {
         dialog = new StatementDialog(
                 workBench.getFrame(), 
                 storedStatementsKind, 
                 engine, 
                 executeAction, 
                 autoActions
                 );
         dialog.setEmbed(embed);
         Entity ne = dialog.inputEntity();
         if (ne != null)
         {
             try
             {
                 entity = ne;
                 fireVetoableChange(OPEN, null, entity);
             }
             catch (PropertyVetoException ex)
             {
                 JOptionPane.showMessageDialog(workBench.getFrame(), ex.getLocalizedMessage(), I18n.get("REFUSED"), JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
     public void save()
     {
         if (entity != null)
         {
             try
             {
                 fireVetoableChange(SAVE, null, entity);
                 engine.update(entity);
             }
             catch (PropertyVetoException ex)
             {
                 JOptionPane.showMessageDialog(workBench.getFrame(), ex.getLocalizedMessage(), I18n.get("REFUSED"), JOptionPane.ERROR_MESSAGE);
             }
         }
         else
         {
             saveAs();
         }
     }
     public void saveAs()
     {
         if (confirmInstall())
         {
             String name = JOptionPane.showInputDialog(workBench.getFrame(), I18n.get("ENTER NAME FOR STATEMENT"), "");
             if (name != null)
             {
                 try
                 {
                     Key key = engine.createKey(storedStatementsKind, name);
                     Entity ne = new Entity(key);
                     if (entity != null)
                     {
                         ne.setPropertiesFrom(entity);
                     }
                     entity = ne;
                     fireVetoableChange(SAVE, null, entity);
                     engine.update(entity);
                 }
                 catch (PropertyVetoException ex)
                 {
                     JOptionPane.showMessageDialog(workBench.getFrame(), ex.getLocalizedMessage(), I18n.get("REFUSED"), JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }
     private void remove()
     {
         if (entity != null)
         {
             int confirm = JOptionPane.showConfirmDialog(
                     workBench.getFrame(), 
                     entity.getKey().getName(), 
                     I18n.get("CONFIRM REMOVE?"),
                     JOptionPane.OK_CANCEL_OPTION
                     );
            if (JOptionPane.YES_OPTION != confirm)
             {
                 try
                 {
                     fireVetoableChange(SAVE, null, null);
                     engine.delete(entity);
                     entity = null;
                 }
                 catch (PropertyVetoException ex)
                 {
                     JOptionPane.showMessageDialog(workBench.getFrame(), ex.getLocalizedMessage(), I18n.get("REFUSED"), JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }
     private void clear()
     {
         try
         {
             fireVetoableChange(OPEN, null, null);
             entity = null;
         }
         catch (PropertyVetoException ex)
         {
             Logger.getLogger(PersistenceHandler.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     public void addVetoableChangeListener(VetoableChangeListener listener)
     {
         changeSupport.addVetoableChangeListener(listener);
     }
 
     public void removeVetoableChangeListener(VetoableChangeListener listener)
     {
         changeSupport.removeVetoableChangeListener(listener);
     }
 
     public void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException
     {
         changeSupport.fireVetoableChange(propertyName, oldValue, newValue);
     }
     public class NewStatementAction extends AbstractAction
     {
         public NewStatementAction()
         {
             super(I18n.get("NEW"));
             putValue(Action.SHORT_DESCRIPTION, I18n.get("CLEARS THE STATEMENT EDITOR."));
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             clear();
         }
     }
     public class OpenStatementAction extends AbstractAction
     {
         public OpenStatementAction()
         {
             super(I18n.get("OPEN"));
             putValue(Action.SHORT_DESCRIPTION, I18n.get("OPENS A SAVED STATEMENT"));
             putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_DOWN_MASK));
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             open(false);
         }
     }
     public class SaveStatementAction extends AbstractAction
     {
         public SaveStatementAction()
         {
             super(I18n.get("SAVE"));
             putValue(Action.SHORT_DESCRIPTION, I18n.get("SAVES THE STATEMENT AND PLUGINS TOOLTIP"));
             putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             save();
         }
     }
     public class SaveAsStatementAction extends AbstractAction
     {
         public SaveAsStatementAction()
         {
             super(I18n.get("SAVEAS"));
             putValue(Action.SHORT_DESCRIPTION, I18n.get("SAVES THE STATEMENT AND PLUGINS TOOLTIP"));
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             saveAs();
         }
     }
     public class RemoveStatementAction extends AbstractAction
     {
         public RemoveStatementAction()
         {
             super(I18n.get("REMOVE"));
             putValue(Action.SHORT_DESCRIPTION, I18n.get("REMOVES THE CURRENT STATEMENT TOOLTIP"));
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             remove();
         }
     }
 }
