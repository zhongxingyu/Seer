 /*
  * Created on Aug 11, 2006
  */
 package com.alertscape.browser.ui.swing.panel.collection.summary;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.SwingConstants;
 
 import ca.odell.glazedlists.EventList;
 import ca.odell.glazedlists.FilterList;
 import ca.odell.glazedlists.event.ListEvent;
 import ca.odell.glazedlists.event.ListEventListener;
 import ca.odell.glazedlists.util.concurrent.Lock;
 
 import com.alertscape.browser.localramp.firstparty.preferences.UserPreferencesPanel;
 import com.alertscape.browser.model.AlertFilter;
 import com.alertscape.common.model.Alert;
 import com.alertscape.common.model.AlertCollection;
 import com.alertscape.common.model.BinarySortAlertCollection;
 import com.alertscape.common.model.severity.Severity;
 import com.alertscape.common.model.severity.SeverityFactory;
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * @author josh
  * @version $Version: $
  */
 public class AlertCollectionSummaryPanel extends JPanel implements AlertFilter, UserPreferencesPanel {
   private static final long serialVersionUID = 1L;
   private AlertCollection subCollection;
   private JLabel[] sevLabels;
   private JToggleButton[] sevButtons;
   private JLabel totalLabel;
   private SeverityMatcherEditor severityMatcher = new SeverityMatcherEditor();
   private Map<Severity, Integer> severityCounts;
   private List<Alert> existingEvents = new ArrayList<Alert>(70000);
 
   public AlertCollectionSummaryPanel() {
     severityCounts = new HashMap<Severity, Integer>(SeverityFactory.getInstance().getNumSeverities());
     init();
   }
 
   public AlertCollection setMasterCollection(AlertCollection master) {
     EventList<Alert> masterList = master.getEventList();
     FilterList<Alert> filterList = new FilterList<Alert>(masterList, severityMatcher);
     subCollection = new BinarySortAlertCollection(filterList);
     existingEvents.clear();
     existingEvents.addAll(masterList);
 
     masterList.addListEventListener(new ListEventListener<Alert>() {
       public void listChanged(ListEvent<Alert> listChanges) {
         EventList<Alert> list = listChanges.getSourceList();
         Lock lock = list.getReadWriteLock().readLock();
         lock.lock();
         Alert e;
         Severity s;
         int count;
         while (listChanges.next()) {
           int index = listChanges.getIndex();
           switch (listChanges.getType()) {
           case ListEvent.INSERT:
             e = list.get(index);
             existingEvents.add(index, e);
             s = e.getSeverity();
             count = severityCounts.get(s);
             count++;
             severityCounts.put(s, count);
             break;
           case ListEvent.DELETE:
             e = existingEvents.remove(index);
             s = e.getSeverity();
             count = severityCounts.get(s);
             count--;
             severityCounts.put(s, count);
             break;
           case ListEvent.UPDATE:
             e = existingEvents.get(index);
             Alert newEvent = list.get(index);
             existingEvents.set(index, newEvent);
             if (newEvent.getSeverity() != e.getSeverity()) {
               // Decrement the old severity
               count = severityCounts.get(e.getSeverity());
               count--;
               severityCounts.put(e.getSeverity(), count);
 
               // Increment the new severity
               count = severityCounts.get(newEvent.getSeverity());
               count++;
               severityCounts.put(newEvent.getSeverity(), count);
             }
             break;
           }
 
         }
 
         lock.unlock();
 
         for (Severity sev : severityCounts.keySet()) {
           // sevButtons[sev.getLevel( )].setText(sev.getName( ) + ": " + severityCounts.get(sev));
           sevLabels[sev.getLevel()].setText("" + severityCounts.get(sev));
           sevLabels[sev.getLevel()].setToolTipText(severityCounts.get(sev) + " " + sev.getName() + " alerts");
         }
         totalLabel.setText(list.size() + "");
         totalLabel.setToolTipText(list.size() + " total alerts");
       }
     });
 
     // Initialize the counts to the current counts in the collection
     Lock lock = masterList.getReadWriteLock().readLock();
     lock.lock();
     for (Alert event : masterList) {
       int count = severityCounts.get(event.getSeverity());
       count++;
       severityCounts.put(event.getSeverity(), count);
     }
     lock.unlock();
 
     for (Severity sev : severityCounts.keySet()) {
       // sevButtons[sev.getLevel( )].setText(sev.getName( ) + ":" + severityCounts.get(sev));
       sevLabels[sev.getLevel()].setText(severityCounts.get(sev) + "");
       sevLabels[sev.getLevel()].setToolTipText(severityCounts.get(sev) + " " + sev.getName() + " alerts");
     }
 
     return subCollection;
   }
 
   public AlertCollection getCollection() {
     return subCollection;
   }
 
   protected void init() {
     // setLayout(null);
     JPanel summaryPanel = new JPanel();
     summaryPanel.setLayout(new GridLayout(1,6));
     SeverityFactory fact = SeverityFactory.getInstance();
     int max = fact.getNumSeverities();
     sevLabels = new JLabel[max];
     sevButtons = new JToggleButton[max];
     FormLayout layout = new FormLayout("r:d:g, 3dlu, [22dlu,p]");
     for (int i = 0; i < max; i++) {
       Severity s = fact.getSeverity(i);
       severityCounts.put(s, 0);
 
       JCheckBox sevButton = new JCheckBox();
 
       // sevButton.setForeground(s.getForegroundColor( ));
       // sevButton.setBackground(s.getBackgroundColor( ));
       sevButton.setText(s.getName());
       sevButton.setToolTipText("Show/Hide " + s.getName() + " alerts");
       sevButton.addItemListener(new SeverityItemListener(s));
       sevButton.setSelected(true);
       JLabel sevTotal = new JLabel();
       sevTotal.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
       sevTotal.setForeground(s.getForegroundColor());
       sevTotal.setHorizontalAlignment(SwingConstants.CENTER);
       JPanel totalPanel = new JPanel(new BorderLayout());
       totalPanel.setBackground(s.getBackgroundColor());
       totalPanel.setBorder(BorderFactory.createLineBorder(Color.black));
       totalPanel.add(sevTotal, BorderLayout.CENTER);
       
       DefaultFormBuilder builder = new DefaultFormBuilder(layout);
       builder.append(sevButton, totalPanel);
 
       JPanel wrapper = builder.getPanel();
       summaryPanel.add(wrapper);
       sevLabels[i] = sevTotal;
       sevButtons[i] = sevButton;
     }
     JLabel totalHeader = new JLabel();
     totalHeader.setText("Total");
     totalHeader.setHorizontalAlignment(SwingConstants.CENTER);
     totalHeader.setFont(totalHeader.getFont().deriveFont(Font.BOLD));
     totalLabel = new JLabel();
     totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
     totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
     totalLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
     totalLabel.setText("0");
 
     JPanel totalCountPanel = new JPanel(new BorderLayout());
     totalCountPanel.setBorder(BorderFactory.createLineBorder(Color.black));
     totalCountPanel.add(totalLabel, BorderLayout.CENTER);
 
     DefaultFormBuilder builder = new DefaultFormBuilder(layout);
     builder.append(totalHeader, totalCountPanel);
 
     JPanel totalPanel = builder.getPanel();
     summaryPanel.add(totalPanel);
 
     setLayout(new BorderLayout());
     // add(headerPanel, BorderLayout.NORTH);
     add(summaryPanel, BorderLayout.CENTER);
 
     severityMatcher.addAllSeverities();
   }
 
   private class SeverityItemListener implements ItemListener {
     private Severity s;
 
     public SeverityItemListener(Severity s) {
       this.s = s;
     }
 
     public void itemStateChanged(ItemEvent e) {
       if (e.getStateChange() == ItemEvent.DESELECTED) {
         severityMatcher.removeSeverity(s);
       } else {
         severityMatcher.addSeverity(s);
       }
     }
   }
 
   public Map getUserPreferences() {
     Map vals = new HashMap();
 
     for (int i = 0; i < sevLabels.length; i++) {
       JToggleButton button = sevButtons[i];
       JLabel label = sevLabels[i];
 
       vals.put(label.getText(), new Boolean(button.isSelected()));
     }
 
     return vals;
   }
 
   public void setUserPreferences(Map preferences) {
     for (int i = 0; i < sevLabels.length; i++) {
       JToggleButton button = sevButtons[i];
       JLabel label = sevLabels[i];
 
       Boolean istoggled = (Boolean) preferences.get(label.getText());
       if (istoggled != null) {
         button.setSelected(istoggled.booleanValue());
       }
     }
   }
 }
