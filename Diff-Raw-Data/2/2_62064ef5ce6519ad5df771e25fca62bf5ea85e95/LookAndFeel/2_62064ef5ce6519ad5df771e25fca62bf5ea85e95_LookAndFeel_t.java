 // Copyright (C) 2004, 2005 Philip Aston
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
 
 package net.grinder.console.swingui;
 
 import java.awt.Component;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.EventListener;
 import java.util.Iterator;
 import java.util.List;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import net.grinder.console.model.ConsoleProperties;
 
 
 /**
  * Manage Look and Feel.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 final class LookAndFeel {
 
   private List m_listeners = new ArrayList();
 
   private final UIManager.LookAndFeelInfo[] m_installedLookAndFeels =
     UIManager.getInstalledLookAndFeels();
 
   public LookAndFeel(ConsoleProperties properties) {
 
     properties.addPropertyChangeListener(
       ConsoleProperties.LOOK_AND_FEEL_PROPERTY,
       new SwingDispatchedPropertyChangeListener(
         new PropertyChangeListener() {
           public void propertyChange(PropertyChangeEvent event) {
             setLookAndFeel((String)event.getNewValue());
           }
         }));
 
     setLookAndFeel(properties.getLookAndFeel());
 
     // The LAF in the properties may differ from the installed one.
     // Perhaps the user is sharing the properties file across
     // architectures (or perhaps across JVMs). We leave the default
     // alone unless the user specifically changes it.
   }
 
   public UIManager.LookAndFeelInfo[] getInstalledLookAndFeels() {
     return m_installedLookAndFeels;
   }
 
   private void setLookAndFeel(String className) {
 
     try {
       if (className != null) {
         UIManager.setLookAndFeel(className);
       }
       else {
         UIManager.setLookAndFeel(
           UIManager.getCrossPlatformLookAndFeelClassName());
       }
     }
     catch (Exception e) {
       throw new AssertionError(e);
     }
 
     synchronized (m_listeners) {
       final Iterator iterator = m_listeners.iterator();
 
       while (iterator.hasNext()) {
         final Listener listener = (Listener)iterator.next();
         listener.lookAndFeelChanged();
       }
     }
   }
 
   public void addListener(Listener window) {
     synchronized (m_listeners) {
       m_listeners.add(window);
     }
   }
 
   /**
    * Listeners implement this to learn about LAF changes.
    */
   interface Listener extends EventListener {
     /**
     * LookAndFeel dispatches notification in Swing thread.
      */
     void lookAndFeelChanged();
   }
 
   /**
    * {@link LookAndFeel.Listener} that does the basic post-LAF change
    * refreshing of a <code>Component</code>.
    */
   static class ComponentListener implements Listener {
     private final Component m_component;
 
     public ComponentListener(Component component) {
       m_component = component;
     }
 
     public void lookAndFeelChanged() {
       SwingUtilities.updateComponentTreeUI(m_component);
     }
   }
 }
