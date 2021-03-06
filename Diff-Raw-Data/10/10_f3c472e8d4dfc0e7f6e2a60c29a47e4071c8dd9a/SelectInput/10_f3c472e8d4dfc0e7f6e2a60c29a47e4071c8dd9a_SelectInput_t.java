 /**********************************************************************
  * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/input/SelectInput.java,v $
 * $Revision: 1.36 $
 * $Date: 2008/02/06 11:02:04 $
  * $Author: willuhn $
  * $Locker:  $
  * $State: Exp $
  *
  * Copyright (c) by willuhn.webdesign
  * All rights reserved
  *
  **********************************************************************/
 package de.willuhn.jameica.gui.input;
 
 import java.rmi.RemoteException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.widgets.Control;
 
 import de.willuhn.datasource.BeanUtil;
 import de.willuhn.datasource.GenericIterator;
 import de.willuhn.datasource.GenericObject;
 import de.willuhn.datasource.pseudo.PseudoIterator;
 import de.willuhn.jameica.gui.GUI;
 import de.willuhn.jameica.gui.util.Color;
 import de.willuhn.jameica.system.Application;
 import de.willuhn.jameica.system.OperationCanceledException;
 import de.willuhn.logging.Logger;
 
 /**
  * Ist zustaendig fuer Eingabefelder des Typs "Select" aka "Combo".
  * Wird die Combo-Box mit einer Liste von GenericObjects erzeugt,
  * dann wird dasPrimaer-Attribut eines jeden Objektes angezeigt.
  * @author willuhn
  */
 public class SelectInput extends AbstractInput
 {
   // Fachdaten
   private List list           = null;
   private Object preselected  = null;
   private String attribute    = null;
   
   // SWT-Daten
   private CCombo combo        = null;
   private boolean enabled     = true;
   private boolean editable    = false;
   private String pleaseChoose = null;
 
 
   /**
    * Erzeugt eine neue Combo-Box und schreibt die Werte der uebergebenen Liste rein.
    * Um Jameica von spezifischem Code aus de.willuhn.datasource zu befreien,
    * sollte kuenftig besser der generische Konstruktor <code>List</code>,<code>Object</code>
    * verwendet werden. Damit kann die Anwendung spaeter auch auf ein anderes Persistierungsframework
    * umgestellt werden.
    * @param list Liste von Objekten.
    * @param preselected das Object, welches vorselektiert sein soll. Optional.
    * @throws RemoteException
    */
   public SelectInput(GenericIterator list, GenericObject preselected) throws RemoteException
   {
     this(PseudoIterator.asList(list),preselected);
   }
   
   /**
    * Erzeugt die Combox-Box mit Beans oder Strings.
    * @param list Liste der Objekte.
    * @param preselected das vorausgewaehlte Objekt.
    */
   public SelectInput(List list, Object preselected)
   {
     super();
     this.list        = list;
     this.preselected = preselected;
   }
 
 	/**
    * Erzeugt die Combox-Box mit Beans oder Strings.
    * @param list Liste der Objekte.
    * @param preselected das vorausgewaehlte Objekt.
    */
   public SelectInput(Object[] list, Object preselected)
   {
     this(Arrays.asList(list),preselected);
   }
 
 	/**
 	 * Aendert nachtraeglich das vorausgewaehlte Element.
    * @param preselected neues vorausgewaehltes Element.
    */
   public void setPreselected(Object preselected)
   {
     if (preselected == null)
       return;
     
     this.preselected = preselected;
     
     if (this.combo == null || this.combo.isDisposed())
       return;
 
     int size = this.list.size();
     for (int i=0;i<size;++i)
     {
       Object value = this.combo.getData(Integer.toString(i));
       if (value == null) // Fuer den Fall, dass die equals-Methode von preselected nicht mit null umgehen kann
         continue;
 
 
       try
       {
         if (BeanUtil.equals(preselected,value))
         {
           this.combo.select(i);
           return;
         }
       }
       catch (RemoteException re)
       {
         Logger.error("unable to compare objects",re);
         return;
       }
     }
   }
 	
   /**
    * Optionale Angabe eines Textes, der an Position 1 angezeigt werden soll.
    * Bei Auswahl dieses Elements, wird null zurueckgeliefert.
    * @param choose Anzuzeigender "Bitte whlen..."-Text.
    */
   public void setPleaseChoose(String choose)
   {
     this.pleaseChoose = choose;
   }
   
   /**
    * Legt den Namen des Attributes fest, welches von den Objekten angezeigt werden
    * soll. Bei herkoemmlichen Beans wird also ein Getter mit diesem Namen aufgerufen. 
    * Wird kein Attribut angegeben, wird bei Objekten des Typs <code>GenericObject</code>
    * der Wert des Primaer-Attributes angezeigt, andernfalls der Wert von <code>toString()</code>.
    * @param name Name des anzuzeigenden Attributes (muss im GenericObject
    * via getAttribute(String) abrufbar sein).
    */
   public void setAttribute(String name)
 	{
 		if (name != null)
 			this.attribute = name;
 	}
 
   /**
    * @see de.willuhn.jameica.gui.input.Input#getControl()
    */
   public Control getControl()
   {
 
     this.combo = GUI.getStyleFactory().createCombo(getParent());
    this.combo.setEditable(this.editable); // BUGZILLA 549
 
     int selected             = -1;
     boolean havePleaseChoose = false;
     boolean haveAttribute    = this.attribute != null && this.attribute.length() > 0;
 
     // Haben wir einen "bitte waehlen..."-Text?
     if (this.pleaseChoose != null && this.pleaseChoose.length() > 0)
     {
       this.combo.add(this.pleaseChoose);
       havePleaseChoose = true;
     }
 
     try
     {
       int size = this.list.size();
       for (int i=0;i<size;++i)
       {
         Object object = this.list.get(i);
 
         if (object == null)
           continue;
 
         // Anzuzeigenden Text ermitteln
         if (haveAttribute)
         {
           Object value = BeanUtil.get(object,this.attribute);
           if (value == null)
             continue;
           
           String text = value.toString();
           if (text == null)
             continue;
           this.combo.add(text);
         }
         else
         {
           this.combo.add(BeanUtil.toString(object));
         }
         this.combo.setData(Integer.toString(havePleaseChoose ? i+1 : i),object);
         
         // Wenn unser Objekt dem vorausgewaehlten entspricht, und wir noch
         // keines ausgewaehlt haben merken wir uns dessen Index
         if (selected == -1 && this.preselected != null)
         {
           if (BeanUtil.equals(object,this.preselected))
           {
             selected = i;
             if (havePleaseChoose)
               selected++;
           }
         }
       }
     }
     catch (RemoteException e)
     {
       this.combo.removeAll();
       this.combo.add(Application.getI18n().tr("Fehler beim Laden der Daten..."));
     	Logger.error("unable to create combo box",e);
     }
 
     this.combo.select(selected > -1 ? selected : 0);
     
     
     // Patch von Heiner
     this.combo.setVisibleItemCount(15);
     this.combo.setEnabled(enabled);
    	if (!enabled)
      this.combo.setForeground(Color.COMMENT.getSWTColor());
 
     return this.combo;
   }
 
   /**
    * Liefert das ausgewaehlte GenericObject.
    * Folglich kann der Rueckgabewert direkt nach GenericObject gecastet werden.
    * @see de.willuhn.jameica.gui.input.Input#getValue()
    */
   public Object getValue()
   {
     if (this.combo == null || this.combo.isDisposed())
       return this.preselected;
 
     if (this.editable)
       return this.combo.getText();
     
     int selected = this.combo.getSelectionIndex();
     return this.combo.getData(Integer.toString(selected));
   }
 
 	/**
 	 * Liefert den derzeit angezeigten Text zurueck.
    * @return Text.
    */
   public String getText()
 	{
     if (this.combo == null || this.combo.isDisposed())
       return null;
 		return combo.getText();
 	}
 
   /**
    * @see de.willuhn.jameica.gui.input.Input#focus()
    */
   public void focus()
   {
     if (this.combo == null || this.combo.isDisposed())
       return;
     
     combo.setFocus();
   }
 
 
   /**
    * @see de.willuhn.jameica.gui.input.Input#disable()
    */
   public void disable()
   {
     setEnabled(false);
   }
 
   /**
    * @see de.willuhn.jameica.gui.input.Input#enable()
    */
   public void enable()
   {
     setEnabled(true);
   }
 
   /**
    * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
    */
   public void setEnabled(boolean enabled)
   {
     this.enabled = enabled;
     if (combo != null && !combo.isDisposed())
     {
       combo.setEnabled(enabled);
       if (enabled)
         combo.setForeground(Color.WIDGET_FG.getSWTColor());
       else
         combo.setForeground(Color.COMMENT.getSWTColor());
     }
   }
   
   /**
    * Markiert die Combo-Box als editierbar. Wenn diese
    * Option aktiviert ist, wird jedoch in <code>getValue()</code>
    * generell der angezeigte Text zurueckgeliefert statt des
    * Fachobjektes. Hintergrund: Normalerweise wird die Combo-Box
    * ja mit einer Liste von Fachobjekten/Beans gefuellt.
    * Abhaengig von der Auswahl wird dann das zugehoerige
    * dahinterstehende Objekt zurueckgeliefert. Bei Freitext-Eingabe
    * existiert jedoch kein solches. Daher wird in diesem Fall
    * der eingebene Text zurueckgeliefert.
    * @param editable
    */
   public void setEditable(boolean editable)
   {
     this.editable = editable;
     if (this.combo != null && !this.combo.isDisposed())
       combo.setEditable(this.editable);
   }
 
   /**
    * Die Funktion macht nichts.
    * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
    */
   public void setValue(Object o)
   {
   }
 
   /**
    * @see de.willuhn.jameica.gui.input.Input#isEnabled()
    */
   public boolean isEnabled()
   {
     return enabled;
   }
 
 	/**
 	 * @see de.willuhn.jameica.gui.input.AbstractInput#update()
 	 */
   void update() throws OperationCanceledException
   {
     // Wir machen hier nichts. 
   }
 }
 
 /*********************************************************************
  * $Log: SelectInput.java,v $
 * Revision 1.36  2008/02/06 11:02:04  willuhn
 * @B Bug 549
 *
  * Revision 1.35  2008/02/04 14:06:44  willuhn
  * @N SelectInput#setEditable
  *
  * Revision 1.34  2007/04/10 23:42:56  willuhn
  * @N TablePart Redesign (removed dependencies from GenericIterator/GenericObject)
  *
  * Revision 1.33  2007/04/02 23:29:59  willuhn
  * @R removed debug output
  * @B explicit cast
  *
  * Revision 1.32  2007/04/02 23:23:17  willuhn
  * @C 3. Konstruktor noch generischer
  *
  * Revision 1.31  2007/04/02 23:01:43  willuhn
  * @N SelectInput auf BeanUtil umgestellt
  *
  * Revision 1.30  2007/03/19 12:15:15  willuhn
  * @N Patch von Heiner
  *
  * Revision 1.29  2007/01/23 15:52:10  willuhn
  * @C update() check for recursion
  * @N mandatoryCheck configurable
  *
  * Revision 1.28  2007/01/05 10:36:49  willuhn
  * @C Farbhandling - Jetzt aber!
  *
  * Revision 1.27  2006/11/12 23:29:19  willuhn
  * @B small Bug (thanks to Reinhold)
  *
  * Revision 1.26  2006/10/16 23:04:24  willuhn
  * @B Bug 298
  *
  * Revision 1.25  2006/10/02 16:25:17  willuhn
  * @N Heiners zusaetzlicher Konstruktor
  *
  * Revision 1.24  2006/08/03 22:43:48  willuhn
  * *** empty log message ***
  *
  * Revision 1.23  2006/07/13 22:33:52  willuhn
  * *** empty log message ***
  *
  * Revision 1.22  2006/06/19 10:54:24  willuhn
  * @N neue Methode setEnabled(boolean) in Input
  * @N neue de_willuhn_util lib
  *
  * Revision 1.21  2006/01/02 17:37:49  web0
  * @N moved Velocity to Jameica
  *
  * Revision 1.20  2005/08/25 21:18:24  web0
  * @C changes accoring to findbugs eclipse plugin
  *
  * Revision 1.19  2005/08/22 13:31:52  web0
  * *** empty log message ***
  *
  * Revision 1.18  2004/12/13 22:48:31  willuhn
  * *** empty log message ***
  *
  * Revision 1.17  2004/12/05 17:29:19  willuhn
  * *** empty log message ***
  *
  * Revision 1.16  2004/11/15 00:38:20  willuhn
  * *** empty log message ***
  *
  * Revision 1.15  2004/11/12 18:23:59  willuhn
  * *** empty log message ***
  *
  * Revision 1.14  2004/10/17 16:28:45  willuhn
  * @N Die ersten Dauerauftraege abgerufen ;)
  *
  * Revision 1.13  2004/08/30 15:03:28  willuhn
  * @N neuer Security-Manager
  *
  * Revision 1.12  2004/08/18 23:14:19  willuhn
  * @D Javadoc
  *
  * Revision 1.11  2004/07/23 15:51:20  willuhn
  * @C Rest des Refactorings
  *
  * Revision 1.10  2004/07/21 23:54:53  willuhn
  * @C massive Refactoring ;)
  *
  * Revision 1.9  2004/07/09 00:12:47  willuhn
  * @C Redesign
  *
  * Revision 1.8  2004/06/30 20:58:40  willuhn
  * *** empty log message ***
  *
  * Revision 1.7  2004/06/18 19:47:17  willuhn
  * *** empty log message ***
  *
  * Revision 1.6  2004/06/17 00:05:26  willuhn
  * *** empty log message ***
  *
  * Revision 1.5  2004/05/23 15:30:52  willuhn
  * @N new color/font management
  * @N new styleFactory
  *
  * Revision 1.4  2004/05/04 23:05:16  willuhn
  * *** empty log message ***
  *
  * Revision 1.3  2004/04/27 00:04:44  willuhn
  * @D javadoc
  *
  * Revision 1.2  2004/04/24 19:05:05  willuhn
  * *** empty log message ***
  *
  * Revision 1.1  2004/04/12 19:15:58  willuhn
  * @C refactoring
  * @N forms
  *
  * Revision 1.10  2004/04/05 23:29:26  willuhn
  * *** empty log message ***
  *
  * Revision 1.9  2004/03/24 00:46:03  willuhn
  * @C refactoring
  *
  * Revision 1.8  2004/03/11 08:56:55  willuhn
  * @C some refactoring
  *
  * Revision 1.7  2004/03/06 18:24:23  willuhn
  * @D javadoc
  *
  * Revision 1.6  2004/03/03 22:27:10  willuhn
  * @N help texts
  * @C refactoring
  *
  * Revision 1.5  2004/02/25 23:11:57  willuhn
  * *** empty log message ***
  *
  * Revision 1.4  2004/02/24 22:46:53  willuhn
  * @N GUI refactoring
  *
  * Revision 1.3  2004/02/18 17:14:40  willuhn
  * *** empty log message ***
  *
  * Revision 1.2  2004/02/12 00:49:20  willuhn
  * *** empty log message ***
  *
  * Revision 1.1  2004/01/28 20:51:24  willuhn
  * @C gui.views.parts moved to gui.parts
  * @C gui.views.util moved to gui.util
  *
  * Revision 1.18  2004/01/23 00:29:03  willuhn
  * *** empty log message ***
  *
  * Revision 1.17  2004/01/08 20:50:32  willuhn
  * @N database stuff separated from jameica
  *
  * Revision 1.16  2004/01/06 20:11:21  willuhn
  * *** empty log message ***
  *
  * Revision 1.15  2003/12/29 16:29:47  willuhn
  * @N javadoc
  *
  * Revision 1.14  2003/12/16 02:27:44  willuhn
  * *** empty log message ***
  *
  * Revision 1.13  2003/12/11 21:00:54  willuhn
  * @C refactoring
  *
  * Revision 1.12  2003/12/10 23:51:55  willuhn
  * *** empty log message ***
  *
  * Revision 1.11  2003/12/05 17:12:23  willuhn
  * @C SelectInput
  *
  * Revision 1.10  2003/12/01 21:22:58  willuhn
  * *** empty log message ***
  *
  * Revision 1.9  2003/12/01 20:28:58  willuhn
  * @B filter in DBIteratorImpl
  * @N InputFelder generalisiert
  *
  * Revision 1.8  2003/11/24 23:01:58  willuhn
  * @N added settings
  *
  * Revision 1.7  2003/11/24 17:27:50  willuhn
  * @N Context menu in table
  *
  * Revision 1.6  2003/11/24 14:21:53  willuhn
  * *** empty log message ***
  *
  * Revision 1.5  2003/11/24 11:51:41  willuhn
  * *** empty log message ***
  *
  * Revision 1.4  2003/11/23 19:26:27  willuhn
  * *** empty log message ***
  *
  * Revision 1.3  2003/11/22 20:43:05  willuhn
  * *** empty log message ***
  *
  * Revision 1.2  2003/11/21 02:10:21  willuhn
  * @N prepared Statements in AbstractDBObject
  * @N a lot of new SWT parts
  *
  * Revision 1.1  2003/11/20 03:48:42  willuhn
  * @N first dialogues
  *
  **********************************************************************/
