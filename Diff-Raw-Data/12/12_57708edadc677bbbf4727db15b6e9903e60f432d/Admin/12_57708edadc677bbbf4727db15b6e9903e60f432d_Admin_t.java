 package com.giftoftheembalmer.gotefarm.client;
 
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
 import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.StackPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.NamedNodeMap;
 import com.google.gwt.xml.client.Node;
 import com.google.gwt.xml.client.NodeList;
 import com.google.gwt.xml.client.XMLParser;
 
 import java.util.Date;
 import java.util.List;
 import java.util.ArrayList;
 
 public class Admin extends Composite {
 
     Widget centerWidget = null;
     DockPanel dpanel = new DockPanel();
 
     Events events = new Events();
     Schedules schedules = new Schedules();
 
     public void eventAdded() {
         events.update();
         schedules.update();
     }
 
     public class EventEditor extends Composite implements ChangeListener {
         JSEventTemplate et;
 
         VerticalPanel vpanel = new VerticalPanel();
         TextBox name = new TextBox();
         TextBox size = new TextBox();
         TextBox minlevel = new TextBox();
         ListBox instances = new ListBox();
         TextBox newinst = new TextBox();
         ListBox bosses = new ListBox();
         TextBox newboss = new TextBox();
         ListBox roles = new ListBox();
         TextBox newrole = new TextBox();
         FlexTable roleft = new FlexTable();
         ListBox badges = new ListBox();
         TextBox newbadge = new TextBox();
         FlexTable badgeft = new FlexTable();
 
         final String NEW_EVENT = "New Event";
         final String NEW_INSTANCE = "New Instance";
         final String NEW_BOSS = "New Boss";
         final String NEW_ROLE = "New Role";
         final String NEW_BADGE = "New Badge";
         final String SELECT_A_ROLE = "Select a role";
         final String SELECT_A_BADGE = "Select a badge";
         final String ALL_ROLES = "All roles";
 
         public EventEditor(JSEventTemplate et) {
             this.et = et;
 
             vpanel.setWidth("100%");
             vpanel.setHeight("100%");
 
             FlexTable grid = new FlexTable();
             grid.setWidth("100%");
 
             CellFormatter cf = grid.getCellFormatter();
             // right align field lables
             cf.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
             cf.setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
             cf.setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);
             cf.setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
             cf.setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_RIGHT);
             cf.setHorizontalAlignment(5, 0, HasHorizontalAlignment.ALIGN_RIGHT);
             cf.setHorizontalAlignment(7, 0, HasHorizontalAlignment.ALIGN_RIGHT);
 
             grid.setText(0, 0, "Event Name:");
             grid.setText(1, 0, "Raid Size:");
             grid.setText(2, 0, "Minimum Level:");
             grid.setText(3, 0, "Instance:");
             grid.setText(4, 0, "Bosses:");
             grid.setText(5, 0, "Roles:");
             grid.setText(7, 0, "Badges:");
 
             size.setVisibleLength(2);
             minlevel.setVisibleLength(2);
 
             size.addChangeListener(new ChangeListener() {
                 public void onChange(Widget sender) {
                     try {
                         int sizen = Integer.parseInt(size.getText());
                         if (sizen < 0) {
                             size.setText("0");
                         }
                     }
                     catch (NumberFormatException e) {
                         size.setText("25");
                     }
 
                     updateRoleTotals();
                 }
             });
 
             minlevel.addChangeListener(new ChangeListener() {
                 public void onChange(Widget sender) {
                     try {
                         int minleveln = Integer.parseInt(minlevel.getText());
                         if (minleveln < 0) {
                             size.setText("0");
                         }
                     }
                     catch (NumberFormatException e) {
                         size.setText("70");
                     }
                 }
             });
 
             instances.setWidth("100%");
             instances.setVisibleItemCount(1);
 
             roles.setWidth("100%");
             roles.setVisibleItemCount(1);
 
             badges.setWidth("100%");
             badges.setVisibleItemCount(1);
 
             grid.setWidget(0, 1, name);
             grid.setWidget(1, 1, size);
             grid.setWidget(2, 1, minlevel);
 
             grid.setWidget(3, 1, instances);
 
             GoteFarm.testService.getInstances(new AsyncCallback<List<String>>() {
                 public void onSuccess(List<String> results) {
                     int sel = 0;
 
                     for (String i : results) {
                         instances.addItem(i);
                         if (EventEditor.this.et != null && i.equals(EventEditor.this.et.instance)) {
                             sel = instances.getItemCount() - 1;
                         }
                     }
 
                     instances.setSelectedIndex(sel);
 
                     updateBosses();
                 }
 
                 public void onFailure(Throwable caught) {
                 }
             });
 
             instances.addChangeListener(this);
             roles.addChangeListener(this);
             badges.addChangeListener(this);
 
             grid.setWidget(3, 2, newinst);
 
             newinst.setText(NEW_INSTANCE);
 
             newinst.addKeyboardListener(new KeyboardListenerAdapter() {
                 public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                     if (keyCode == (char)KEY_ENTER) {
                         String inst = newinst.getText();
 
                         boolean found = false;
 
                         for (int i = 0; i < instances.getItemCount(); ++i) {
                             if (instances.getItemText(i).equals(inst)) {
                                 found = true;
                                 break;
                             }
                         }
 
                         if (!found) {
                             instances.addItem(inst);
                             instances.setSelectedIndex(instances.getItemCount()-1);
                             bosses.clear();
 
                             newboss.setFocus(true);
                             newboss.setText(NEW_BOSS);
                             newboss.setSelectionRange(0, NEW_BOSS.length());
 
                             GoteFarm.testService.addInstance(GoteFarm.sessionID, inst, new AsyncCallback<Boolean>() {
                                 public void onSuccess(Boolean result) {
                                 }
 
                                 public void onFailure(Throwable caught) {
                                 }
                             });
                         }
                     }
                 }
             });
 
             grid.setWidget(4, 1, bosses);
             grid.setWidget(4, 2, newboss);
 
             bosses.setWidth("100%");
             bosses.setVisibleItemCount(10);
             bosses.setMultipleSelect(true);
 
             newboss.setText(NEW_BOSS);
 
             newboss.addKeyboardListener(new KeyboardListenerAdapter() {
                 public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                     if (keyCode == (char)KEY_ENTER) {
                         int selinst = instances.getSelectedIndex();
                         if (selinst == -1) {
                             // TODO: display error
                             return;
                         }
 
                         String boss = newboss.getText();
 
                         boolean found = false;
 
                         for (int i = 0; i < bosses.getItemCount(); ++i) {
                             if (bosses.getItemText(i).equals(boss)) {
                                 found = true;
                                 break;
                             }
                         }
 
                         if (!found) {
                             bosses.addItem(boss);
                             bosses.setItemSelected(bosses.getItemCount()-1, true);
 
                             GoteFarm.testService.addBoss(
                                 GoteFarm.sessionID,
                                 instances.getItemText(selinst),
                                 boss,
                                 new AsyncCallback<Boolean>() {
 
                                 public void onSuccess(Boolean result) {
                                 }
 
                                 public void onFailure(Throwable caught) {
                                 }
                             });
                         }
 
                         newboss.setText(NEW_BOSS);
                         newboss.setSelectionRange(0, NEW_BOSS.length());
                     }
                 }
             });
 
             grid.setWidget(5, 1, roles);
 
             roles.addItem(SELECT_A_ROLE);
 
             GoteFarm.testService.getRoles(new AsyncCallback<List<JSRole>>() {
                 public void onSuccess(List<JSRole> results) {
                     for (JSRole i : results) {
                         roles.addItem(i.name);
                     }
                 }
 
                 public void onFailure(Throwable caught) {
                 }
             });
 
             grid.setWidget(5, 2, newrole);
 
             newrole.setText(NEW_ROLE);
 
             newrole.addKeyboardListener(new KeyboardListenerAdapter() {
                 public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                     if (keyCode == (char)KEY_ENTER) {
                         String role = newrole.getText();
 
                         boolean found = false;
 
                         for (int i = 0; i < roles.getItemCount(); ++i) {
                             if (roles.getItemText(i).equals(role)) {
                                 found = true;
                                 break;
                             }
                         }
 
                         if (!found) {
                             roles.addItem(role);
                             roles.setSelectedIndex(roles.getItemCount()-1);
 
                             newrole.setFocus(true);
                             newrole.setText(NEW_ROLE);
                             newrole.setSelectionRange(0, NEW_ROLE.length());
 
                             addRole(role);
 
                             GoteFarm.testService.addRole(GoteFarm.sessionID, role, true, new AsyncCallback<Boolean>() {
                                 public void onSuccess(Boolean result) {
                                 }
 
                                 public void onFailure(Throwable caught) {
                                 }
                             });
                         }
                     }
                 }
             });
 
             roleft.setWidth("100%");
             roleft.setCellSpacing(0);
             roleft.setCellPadding(5);
             roleft.setText(0, 0, "Role");
             roleft.setText(0, 1, "Min");
             roleft.setText(0, 2, "Max");
             roleft.setText(1, 0, "Totals");
 
             FlexTable.FlexCellFormatter rcf = roleft.getFlexCellFormatter();
             rcf.addStyleName(0, 0, "header");
             rcf.addStyleName(0, 1, "header");
             rcf.addStyleName(0, 2, "header");
             rcf.addStyleName(0, 3, "header");
             rcf.addStyleName(1, 0, "footer");
             rcf.addStyleName(1, 1, "footer");
             rcf.addStyleName(1, 2, "footer");
             rcf.addStyleName(1, 3, "footer");
 
             FlexTable.FlexCellFormatter gcf = grid.getFlexCellFormatter();
 
             grid.setWidget(6, 0, roleft);
             gcf.setColSpan(6, 0, 3);
 
             grid.setWidget(7, 1, badges);
 
             badges.addItem(SELECT_A_BADGE);
 
             GoteFarm.testService.getBadges(new AsyncCallback<List<String>>() {
                 public void onSuccess(List<String> results) {
                     for (String i : results) {
                         badges.addItem(i);
                     }
                 }
 
                 public void onFailure(Throwable caught) {
                 }
             });
 
             grid.setWidget(7, 2, newbadge);
 
             newbadge.setText(NEW_BADGE);
 
             newbadge.addKeyboardListener(new KeyboardListenerAdapter() {
                 public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                     if (keyCode == (char)KEY_ENTER) {
                         String badge = newbadge.getText();
 
                         boolean found = false;
 
                         for (int i = 0; i < badges.getItemCount(); ++i) {
                             if (badges.getItemText(i).equals(badge)) {
                                 found = true;
                                 break;
                             }
                         }
 
                         if (!found) {
                             badges.addItem(badge);
                             badges.setSelectedIndex(badges.getItemCount()-1);
 
                             newbadge.setFocus(true);
                             newbadge.setText(NEW_BADGE);
                             newbadge.setSelectionRange(0, NEW_BADGE.length());
 
                             addBadge(badge);
 
                             GoteFarm.testService.addBadge(GoteFarm.sessionID, badge, 0, new AsyncCallback<Boolean>() {
                                 public void onSuccess(Boolean result) {
                                 }
 
                                 public void onFailure(Throwable caught) {
                                 }
                             });
                         }
                     }
                 }
             });
 
             badgeft.setWidth("100%");
             badgeft.setCellSpacing(0);
             badgeft.setCellPadding(5);
             badgeft.setText(0, 0, "Badge");
             badgeft.setText(0, 1, "Require For Sign Up");
             badgeft.setText(0, 2, "Apply To Role");
             badgeft.setText(0, 3, "Num Role Slots");
             badgeft.setText(0, 4, "Early Signup (Hours)");
 
             FlexTable.FlexCellFormatter bcf = badgeft.getFlexCellFormatter();
             bcf.addStyleName(0, 0, "header");
             bcf.addStyleName(0, 1, "header");
             bcf.addStyleName(0, 2, "header");
             bcf.addStyleName(0, 3, "header");
             bcf.addStyleName(0, 4, "header");
             bcf.addStyleName(0, 5, "header");
 
             grid.setWidget(8, 0, badgeft);
             gcf.setColSpan(8, 0, 3);
 
             vpanel.add(grid);
 
             HorizontalPanel hpanel = new HorizontalPanel();
             hpanel.setWidth("100%");
 
            final Label errmsg = new Label();
            errmsg.addStyleName(errmsg.getStylePrimaryName() + "-error");
            errmsg.addStyleName(errmsg.getStylePrimaryName() + "-bottom");

             Button save = new Button("Save", new ClickListener() {
                 public void onClick(Widget sender) {
                    // clear error message
                    errmsg.setText("");

                     JSEventTemplate t = new JSEventTemplate();
 
                     if (EventEditor.this.et != null) {
                         t.eid = EventEditor.this.et.eid;
                     }
                     else {
                         t.eid = -1;
                     }
                     t.name = name.getText();
                     t.size = getRaidSize();
                     t.minimumLevel = Integer.parseInt(minlevel.getText());
                     t.instance = instances.getItemText(instances.getSelectedIndex());
 
                     t.bosses = new ArrayList<String>();
                     for (int i = 0; i < bosses.getItemCount(); ++i) {
                         if (bosses.isItemSelected(i)) {
                             t.bosses.add(bosses.getItemText(i));
                         }
                     }
 
                     t.roles = new ArrayList<JSEventRole>();
                     for (int i = 0; i < roleft.getRowCount() - 2; ++i) {
                         JSEventRole er = new JSEventRole();
                         er.name = roleft.getText(i + 1, 0);
                         TextBox minmax;
                         minmax = (TextBox)roleft.getWidget(i + 1, 1);
                         er.min = Integer.parseInt(minmax.getText());
                         minmax = (TextBox)roleft.getWidget(i + 1, 2);
                         er.max = Integer.parseInt(minmax.getText());
                         t.roles.add(er);
                     }
 
                     t.badges = new ArrayList<JSEventBadge>();
                     for (int i = 0; i < badgeft.getRowCount() - 1; ++i) {
                         JSEventBadge eb = new JSEventBadge();
                         eb.name = badgeft.getText(i + 1, 0);
                         CheckBox cb = (CheckBox)badgeft.getWidget(i + 1, 1);
                         eb.requireForSignup = cb.isChecked();
                         ListBox lb = (ListBox)badgeft.getWidget(i + 1, 2);
                         String role = lb.getItemText(lb.getSelectedIndex());
                         if (!role.equals(ALL_ROLES)) {
                             eb.applyToRole = role;
                         }
                         TextBox tb;
                         tb = (TextBox)badgeft.getWidget(i + 1, 3);
                         eb.numSlots = Integer.parseInt(tb.getText());
                         tb = (TextBox)badgeft.getWidget(i + 1, 4);
                         eb.earlySignup = Integer.parseInt(tb.getText());
                         t.badges.add(eb);
                     }
 
                     GoteFarm.testService.saveEventTemplate(GoteFarm.sessionID, t, new AsyncCallback<Boolean>() {
                         public void onSuccess(Boolean result) {
                             eventAdded();
                             setCenterWidget(null);
                         }
 
                         public void onFailure(Throwable caught) {
                            errmsg.setText(caught.getMessage());
                         }
                     });
                 }
             });
 
             save.addStyleName(save.getStylePrimaryName() + "-bottom");
             save.addStyleName(save.getStylePrimaryName() + "-left");
 
             Button cancel = new Button("Cancel", new ClickListener() {
                 public void onClick(Widget sender) {
                     setCenterWidget(null);
                 }
             });
 
             cancel.addStyleName(cancel.getStylePrimaryName() + "-bottom");
             cancel.addStyleName(cancel.getStylePrimaryName() + "-right");
 
             hpanel.add(save);
            hpanel.add(errmsg);
             hpanel.add(cancel);
 
             vpanel.add(hpanel);
 
             if (et != null) {
                 for (JSEventRole ev : et.roles) {
                     addRole(ev.name, ev.min, ev.max);
                 }
 
                 for (JSEventBadge eb : et.badges) {
                     addBadge(eb.name, eb.requireForSignup, eb.applyToRole, eb.numSlots, eb.earlySignup);
                 }
             }
 
             if (et == null) {
                 name.setText(NEW_EVENT);
 
                 DeferredCommand dc = new DeferredCommand();
                 dc.addCommand(new Command() {
                     public void execute() {
                         name.setFocus(true);
                         name.setSelectionRange(0, NEW_EVENT.length());
                     }
                 });
 
                 size.setText("25");
                 minlevel.setText("70");
             }
             else {
                 name.setText(et.name);
                 size.setText("" + et.size);
                 minlevel.setText("" + et.minimumLevel);
             }
 
             updateRoleTotals();
 
             initWidget(vpanel);
 
             setStyleName("Admin-EventEditor");
         }
 
         public EventEditor() {
             this(null);
         }
 
         public void updateBosses() {
             final String inst = instances.getItemText(instances.getSelectedIndex());
 
             GoteFarm.testService.getInstanceBosses(inst, new AsyncCallback<List<String>>() {
                 public void onSuccess(List<String> results) {
                     bosses.clear();
 
                     for (String ib : results) {
                         bosses.addItem(ib);
                         if (et != null && inst.equals(et.instance)) {
                             for (String eb : et.bosses) {
                                 if (ib.equals(eb)) {
                                     bosses.setItemSelected(bosses.getItemCount() - 1, true);
                                     break;
                                 }
                             }
                         }
                     }
                 }
 
                 public void onFailure(Throwable caught) {
                 }
             });
         }
 
         public void addRole(String role) {
             addRole(role, 0, 0);
         }
 
         public void addRole(String role, int min_value, int max_value) {
             if (role.equals(SELECT_A_ROLE)) {
                 return;
             }
 
             final int rows = roleft.getRowCount() - 2;
 
             // check role doesn't already exist
             for (int row = 0; row < rows; ++row) {
                 if (roleft.getText(row + 1, 0).equals(role)) {
                     return;
                 }
             }
 
             roleft.insertRow(rows + 1);
             roleft.setText(rows + 1, 0, role);
 
             final TextBox min = new TextBox();
             final TextBox max = new TextBox();
 
             min.setText("" + min_value);
             max.setText("" + max_value);
 
             min.setVisibleLength(2);
             max.setVisibleLength(2);
 
             ChangeListener minmax = new ChangeListener() {
                 public void onChange(Widget sender) {
                     try {
                         Integer minv = Integer.parseInt(min.getText());
                         Integer maxv = Integer.parseInt(max.getText());
 
                         if (sender == min) {
                             if (minv > maxv) {
                                 max.setText(min.getText());
                             }
                         }
                         else if (sender == max) {
                             if (maxv < minv) {
                                 min.setText(max.getText());
                             }
                         }
                     }
                     catch (NumberFormatException e) {
                         if (sender == min) {
                             min.setText("0");
                         }
                         else if (sender == max) {
                             max.setText("0");
                         }
                     }
 
                     updateRoleTotals();
                 }
             };
 
             min.addChangeListener(minmax);
             max.addChangeListener(minmax);
 
             roleft.setWidget(rows + 1, 1, min);
             roleft.setWidget(rows + 1, 2, max);
             roleft.setWidget(rows + 1, 3, new Button("Remove", new ClickListener() {
                 public void onClick(Widget sender) {
                     final int rows = roleft.getRowCount() - 2;
                     for (int i = 0; i < rows; ++i) {
                         if (roleft.getWidget(i + 1, 3) == sender) {
                             roleft.removeRow(i + 1);
                             updateBadgeRoles();
                             break;
                         }
                     }
                 }
             }));
 
             updateRoleTotals();
         }
 
         public void addBadge(String badge) {
             addBadge(badge, false, null, 0, 0);
         }
 
         public void addBadge(String badge, boolean requireForSignup, String applyToRole, int numSlots, int earlySignup) {
             if (badge.equals(SELECT_A_BADGE)) {
                 return;
             }
 
             final int rows = badgeft.getRowCount() - 1;
 
             badgeft.setText(rows + 1, 0, badge);
 
             final CheckBox cb = new CheckBox();
             cb.setChecked(requireForSignup);
             badgeft.setWidget(rows + 1, 1, cb);
 
             final ListBox roles = new ListBox();
             roles.setVisibleItemCount(1);
 
             roles.addItem(ALL_ROLES);
 
             int sel_index = 0;
 
             for (int i = 0; i < roleft.getRowCount() - 2; ++i) {
                 String role = roleft.getText(i + 1, 0);
                 roles.addItem(role);
                 if (applyToRole != null && role.equals(applyToRole)) {
                     sel_index = roles.getItemCount() - 1;
                 }
             }
 
             roles.setSelectedIndex(sel_index);
             badgeft.setWidget(rows + 1, 2, roles);
 
             final TextBox slots = new TextBox();
             slots.setVisibleLength(2);
             if (requireForSignup && applyToRole != null) {
                 slots.setText("" + numSlots);
             }
             else {
                 slots.setEnabled(false);
                 slots.setText("0");
             }
             badgeft.setWidget(rows + 1, 3, slots);
             slots.addChangeListener(new ChangeListener() {
                 public void onChange(Widget sender) {
                     try {
                         int num = Integer.parseInt(slots.getText());
                         if (num < 0) {
                             slots.setText("0");
                         }
                     }
                     catch (NumberFormatException e) {
                         slots.setText("0");
                     }
                 }
             });
 
             cb.addClickListener(new ClickListener() {
                 public void onClick(Widget sender) {
                     if (cb.isChecked() && roles.getSelectedIndex() > 0) {
                         slots.setEnabled(true);
                     }
                     else {
                         slots.setEnabled(false);
                         slots.setText("0");
                     }
                 }
             });
 
             roles.addChangeListener(new ChangeListener() {
                 public void onChange(Widget sender) {
                     if (cb.isChecked() && roles.getSelectedIndex() > 0) {
                         slots.setEnabled(true);
                     }
                     else {
                         slots.setEnabled(false);
                         slots.setText("0");
                     }
                 }
             });
 
             final TextBox early = new TextBox();
             early.setVisibleLength(2);
             early.setText("" + earlySignup);
             badgeft.setWidget(rows + 1, 4, early);
             early.addChangeListener(new ChangeListener() {
                 public void onChange(Widget sender) {
                     try {
                         int num = Integer.parseInt(early.getText());
                         if (num < 0) {
                             early.setText("0");
                         }
                     }
                     catch (NumberFormatException e) {
                         early.setText("0");
                     }
                 }
             });
 
             badgeft.setWidget(rows + 1, 5, new Button("Remove", new ClickListener() {
                 public void onClick(Widget sender) {
                     final int rows = badgeft.getRowCount() - 1;
                     for (int i = 0; i < rows; ++i) {
                         if (badgeft.getWidget(i + 1, 5) == sender) {
                             badgeft.removeRow(i + 1);
                             break;
                         }
                     }
                 }
             }));
         }
 
         public int getRaidSize() {
             try {
                 return Integer.parseInt(size.getText());
             }
             catch (NumberFormatException e) {
                 return 0;
             }
         }
 
         public void updateRoleTotals() {
             int mint = 0;
             int maxt = 0;
 
             final int rows = roleft.getRowCount() - 2;
 
             for (int row = 0; row < rows; ++row) {
                 TextBox minw = (TextBox)roleft.getWidget(row + 1, 1);
                 TextBox maxw = (TextBox)roleft.getWidget(row + 1, 2);
 
                 mint += Integer.parseInt(minw.getText());
                 maxt += Integer.parseInt(maxw.getText());
             }
 
             roleft.setText(rows + 1, 1, "" + mint);
             roleft.setText(rows + 1, 2, "" + maxt);
 
             int size = getRaidSize();
 
             FlexTable.FlexCellFormatter fcf = roleft.getFlexCellFormatter();
 
             if (mint < size) {
                 fcf.addStyleName(rows + 1, 1, "error");
             }
             else {
                 fcf.removeStyleName(rows + 1, 1, "error");
             }
 
             if (maxt < size) {
                 fcf.addStyleName(rows + 1, 2, "error");
             }
             else {
                 fcf.removeStyleName(rows + 1, 2, "error");
             }
 
             updateBadgeRoles();
         }
 
         public void updateBadgeRoles() {
             final int role_rows = roleft.getRowCount() - 2;
             final int badge_rows = badgeft.getRowCount() - 1;
 
             for (int i = 0; i < badge_rows; ++i) {
                 ListBox broles = (ListBox)badgeft.getWidget(i + 1, 2);
                 String selected = broles.getItemText(broles.getSelectedIndex());
 
                 broles.clear();
 
                 broles.addItem(ALL_ROLES);
 
                 int selected_index = 0;
 
                 for (int j = 0; j < role_rows; ++j) {
                     String role = roleft.getText(j + 1, 0);
                     broles.addItem(role);
                     if (role.equals(selected)) {
                         selected_index = j + 1;
                     }
                 }
 
                 broles.setSelectedIndex(selected_index);
 
                 // should num slots be enabled?
                 boolean enable = false;
 
                 if (selected_index > 0) {
                     CheckBox cb = (CheckBox)badgeft.getWidget(i + 1, 1);
                     if (cb.isChecked()) {
                         enable = true;
                     }
                 }
 
                 TextBox tb = (TextBox)badgeft.getWidget(i + 1, 3);
                 tb.setEnabled(enable);
                 if (!enable) {
                     tb.setText("0");
                 }
             }
         }
 
         public void onChange(Widget sender) {
             if (sender == instances) {
                 updateBosses();
             }
             else if (sender == roles) {
                 addRole(roles.getItemText(roles.getSelectedIndex()));
             }
             else if (sender == badges) {
                 addBadge(badges.getItemText(badges.getSelectedIndex()));
             }
         }
     }
 
     public class Events extends Composite {
         VerticalPanel vpanel = new VerticalPanel();
         ListBox eventlb = new ListBox();
 
         public Events() {
             eventlb.setWidth("100%");
             eventlb.setVisibleItemCount(20);
 
             eventlb.addChangeListener(new ChangeListener() {
                 public void onChange(Widget sender) {
                     int sel = eventlb.getSelectedIndex();
                     if (sel < 0) return;
 
                     String name = eventlb.getItemText(sel);
 
                     GoteFarm.testService.getEventTemplate(GoteFarm.sessionID, name, new AsyncCallback<JSEventTemplate>() {
                         public void onSuccess(JSEventTemplate result) {
                             setCenterWidget(new EventEditor(result));
                         }
 
                         public void onFailure(Throwable caught) {
                         }
                     });
                 }
             });
 
             vpanel.add(eventlb);
 
             vpanel.add(new Button("New Event", new ClickListener() {
                 public void onClick(Widget sender) {
                     eventlb.setSelectedIndex(-1);
                     setCenterWidget(new EventEditor());
                 }
             }));
 
             update();
 
             initWidget(vpanel);
 
             setStyleName("Admin-Events");
         }
 
         public void update() {
             eventlb.clear();
             GoteFarm.testService.getEventTemplates(GoteFarm.sessionID, new AsyncCallback<List<String>>() {
                 public void onSuccess(List<String> results) {
                     for (String t : results) {
                         eventlb.addItem(t);
                     }
                 }
 
                 public void onFailure(Throwable caught) {
                 }
             });
         }
     }
 
     public class ScheduleEditor extends Composite {
         class Schedule extends Composite implements ChangeListener, ClickListener {
             JSEventSchedule sched = null;
             VerticalPanel vpanel = new VerticalPanel();
             final DatePickerWrapper dp;
             final TimePicker tp;
             final DurationPicker display_start = new DurationPicker();
             final DurationPicker display_end = new DurationPicker();
             final DurationPicker signups_start = new DurationPicker();
             final DurationPicker signups_end = new DurationPicker();
             final Label display_start_time = new Label();
             final Label display_end_time = new Label();
             final Label signups_start_time = new Label();
             final Label signups_end_time = new Label();
             final RadioButton rptnever = new RadioButton("repeatGroup", "Never");
             final RadioButton rptdaily = new RadioButton("repeatGroup", "Daily");
             final RadioButton rptweekly = new RadioButton("repeatGroup", "Weekly");
             final RadioButton rptmonthly = new RadioButton("repeatGroup", "Monthly");
             final VerticalPanel dailyrptpanel = new VerticalPanel();
             final VerticalPanel weeklyrptpanel = new VerticalPanel();
             final VerticalPanel monthlyrptpanel = new VerticalPanel();
             final ListBox dailyrptdays = new ListBox();
             final Label dailyrptdaylabel = new Label("day");
             final ListBox weeklyrptweeks = new ListBox();
             final Label weeklyrptweeklabel = new Label("week");
             final CheckBox[] weeklydays = {
                 new CheckBox("Sun"),
                 new CheckBox("Mon"),
                 new CheckBox("Tue"),
                 new CheckBox("Wed"),
                 new CheckBox("Thu"),
                 new CheckBox("Fri"),
                 new CheckBox("Sat")
             };
             final ListBox monthlyrptmonth = new ListBox();
             final Label monthlyrptmonthlabel = new Label("month");
             final RadioButton rptdayofmonth = new RadioButton("dayofGroup", "day of the month");
             final RadioButton rptdayofweek = new RadioButton("dayofGroup", "day of the week");
 
             public Schedule(JSEventSchedule sched) {
                 this.sched = sched;
                 if (this.sched == null) {
                     this.sched = sched = new JSEventSchedule();
 
                     sched.esid = -1;
                     sched.eid = -1;
 
                     sched.start_time = new Date();
                     sched.start_time.setTime(
                         sched.start_time.getTime() -
                         (sched.start_time.getTime() % 3600000)
                     );
 
                     sched.duration = 120 * 60;
 
                     sched.display_start = 7 * 86400;
                     sched.display_end = 3600;
 
                     sched.signups_start = 2 * 86400;
                     sched.signups_end = 1800;
 
                     sched.repeat_size = 0;
                     sched.repeat_freq = 1;
                     sched.day_mask = 1 << sched.start_time.getDay();
                     sched.repeat_by = 0;
                 }
 
                 vpanel.setWidth("100%");
 
                 dp = new DatePickerWrapper(sched.start_time);
                 dp.setYoungestDate(new Date());
                 dp.addChangeListener(this);
                 tp = new TimePicker(sched.start_time);
                 tp.addChangeListener(this);
 
                 {
                     HorizontalPanel hpanel = new HorizontalPanel();
                     hpanel.setVerticalAlignment(hpanel.ALIGN_MIDDLE);
                     hpanel.add(new Label("Start Date:"));
                     hpanel.add(dp);
                     hpanel.add(new HTML("&nbsp;&nbsp;"));
                     hpanel.add(new Label("Start Time:"));
                     hpanel.add(tp);
 
                     vpanel.add(hpanel);
                 }
 
                 display_start.addChangeListener(this);
                 display_start.setVisibleLength(6);
                 display_start.setDuration(sched.display_start);
 
                 {
                     HorizontalPanel hpanel = new HorizontalPanel();
                     hpanel.setVerticalAlignment(hpanel.ALIGN_MIDDLE);
                     hpanel.setSpacing(5);
                     hpanel.add(new Label("Display event "));
                     hpanel.add(display_start);
                     hpanel.add(new Label(" before start time: "));
                     hpanel.add(display_start_time);
 
                     vpanel.add(hpanel);
                 }
 
                 display_end.addChangeListener(this);
                 display_end.setVisibleLength(6);
                 display_end.setDuration(sched.display_end);
 
                 {
                     HorizontalPanel hpanel = new HorizontalPanel();
                     hpanel.setVerticalAlignment(hpanel.ALIGN_MIDDLE);
                     hpanel.setSpacing(5);
                     hpanel.add(new Label("Remove event "));
                     hpanel.add(display_end);
                     hpanel.add(new Label(" after start time: "));
                     hpanel.add(display_end_time);
 
                     vpanel.add(hpanel);
                 }
 
                 signups_start.addChangeListener(this);
                 signups_start.setVisibleLength(6);
                 signups_start.setDuration(sched.signups_start);
 
                 {
                     HorizontalPanel hpanel = new HorizontalPanel();
                     hpanel.setVerticalAlignment(hpanel.ALIGN_MIDDLE);
                     hpanel.setSpacing(5);
                     hpanel.add(new Label("Signups open "));
                     hpanel.add(signups_start);
                     hpanel.add(new Label(" before start time: "));
                     hpanel.add(signups_start_time);
 
                     vpanel.add(hpanel);
                 }
 
                 signups_end.addChangeListener(this);
                 signups_end.setVisibleLength(6);
                 signups_end.setDuration(sched.signups_end);
 
                 {
                     HorizontalPanel hpanel = new HorizontalPanel();
                     hpanel.setVerticalAlignment(hpanel.ALIGN_MIDDLE);
                     hpanel.setSpacing(5);
                     hpanel.add(new Label("Signups end "));
                     hpanel.add(signups_end);
                     hpanel.add(new Label(" before start time: "));
                     hpanel.add(signups_end_time);
 
                     vpanel.add(hpanel);
                 }
 
                 {
                     HorizontalPanel rptpanel = new HorizontalPanel();
 
                     rptpanel.add(new Label("Repeat event"));
                     rptpanel.add(rptnever);
                     rptpanel.add(rptdaily);
                     rptpanel.add(rptweekly);
                     rptpanel.add(rptmonthly);
 
                     rptnever.addClickListener(this);
                     rptdaily.addClickListener(this);
                     rptweekly.addClickListener(this);
                     rptmonthly.addClickListener(this);
 
                     vpanel.add(rptpanel);
                 }
 
                 dailyrptpanel.setVisible(false);
                 weeklyrptpanel.setVisible(false);
                 monthlyrptpanel.setVisible(false);
 
                 {
                     HorizontalPanel hpanel = new HorizontalPanel();
                     hpanel.add(new Label("Repeat every"));
 
                     for (int i = 1; i < 31; ++i) {
                         dailyrptdays.addItem("" + i);
                     }
 
                     dailyrptdays.addChangeListener(new ChangeListener() {
                         public void onChange(Widget sender) {
                             if (dailyrptdays.getSelectedIndex() > 0) {
                                 dailyrptdaylabel.setText("days");
                             }
                             else {
                                 dailyrptdaylabel.setText("day");
                             }
                         }
                     });
 
                     dailyrptdays.setSelectedIndex(sched.repeat_freq - 1);
 
                     hpanel.add(dailyrptdays);
                     hpanel.add(dailyrptdaylabel);
 
                     dailyrptpanel.add(hpanel);
                     vpanel.add(dailyrptpanel);
                 }
 
                 {
                     HorizontalPanel hpanel = new HorizontalPanel();
                     hpanel.add(new Label("Repeat every"));
 
                     for (int i = 1; i < 31; ++i) {
                         weeklyrptweeks.addItem("" + i);
                     }
 
                     weeklyrptweeks.addChangeListener(new ChangeListener() {
                         public void onChange(Widget sender) {
                             if (weeklyrptweeks.getSelectedIndex() > 0) {
                                 weeklyrptweeklabel.setText("weeks");
                             }
                             else {
                                 weeklyrptweeklabel.setText("week");
                             }
                         }
                     });
 
                     weeklyrptweeks.setSelectedIndex(sched.repeat_freq - 1);
 
                     hpanel.add(weeklyrptweeks);
                     hpanel.add(weeklyrptweeklabel);
 
                     weeklyrptpanel.add(hpanel);
                 }
 
                 {
                     HorizontalPanel hpanel = new HorizontalPanel();
                     hpanel.add(new Label("Repeat on"));
 
                     for (int i = 0; i < 7; ++i ) {
                         weeklydays[i].addStyleName("padleft");
                         if ((sched.day_mask & (1 << i)) > 0) {
                             weeklydays[i].setChecked(true);
                         }
                         hpanel.add(weeklydays[i]);
                     }
 
                     weeklyrptpanel.add(hpanel);
                     vpanel.add(weeklyrptpanel);
                 }
 
                 {
                     HorizontalPanel hpanel = new HorizontalPanel();
                     hpanel.add(new Label("Repeat every"));
 
                     for (int i = 1; i < 31; ++i) {
                         monthlyrptmonth.addItem("" + i);
                     }
 
                     monthlyrptmonth.addChangeListener(new ChangeListener() {
                         public void onChange(Widget sender) {
                             if (monthlyrptmonth.getSelectedIndex() > 0) {
                                 monthlyrptmonthlabel.setText("months");
                             }
                             else {
                                 monthlyrptmonthlabel.setText("month");
                             }
                         }
                     });
 
                     monthlyrptmonth.setSelectedIndex(sched.repeat_freq - 1);
 
                     hpanel.add(monthlyrptmonth);
                     hpanel.add(monthlyrptmonthlabel);
 
                     monthlyrptpanel.add(hpanel);
                 }
 
                 {
                     HorizontalPanel hpanel = new HorizontalPanel();
                     hpanel.add(new Label("Repeat by"));
 
                     if (sched.repeat_by == 0) {
                         rptdayofmonth.setChecked(true);
                     }
                     else {
                         rptdayofweek.setChecked(true);
                     }
 
                     hpanel.add(rptdayofmonth);
                     hpanel.add(rptdayofweek);
 
                     monthlyrptpanel.add(hpanel);
                     vpanel.add(monthlyrptpanel);
                 }
 
                 updateTimes();
 
                 switch (sched.repeat_size) {
                     case 0:
                         rptnever.setChecked(true);
                         break;
                     case 1:
                         rptdaily.setChecked(true);
                         onClick(rptdaily);
                         break;
                     case 2:
                         rptweekly.setChecked(true);
                         onClick(rptweekly);
                         break;
                     case 3:
                         rptmonthly.setChecked(true);
                         onClick(rptmonthly);
                         break;
                 }
 
                 initWidget(vpanel);
 
                 setStyleName("Admin-Schedule");
             }
 
             public Schedule() {
                 this(null);
             }
 
             public void onChange(Widget sender) {
                 if (sched == null) return;
 
                 if (sender == dp) {
                     Date d = dp.getSelectedDate();
                     sched.start_time.setYear(d.getYear());
                     sched.start_time.setMonth(d.getMonth());
                     sched.start_time.setDate(d.getDate());
                 }
                 else if (sender == tp) {
                     Date d = tp.getSelectedDate();
                     sched.start_time.setHours(d.getHours());
                     sched.start_time.setMinutes(d.getMinutes());
                     sched.start_time.setSeconds(d.getSeconds());
                 }
                 else if (sender == display_start) {
                     sched.display_start = display_start.getDuration();
                 }
                 else if (sender == display_end) {
                     sched.display_end = display_end.getDuration();
                 }
                 else if (sender == signups_start) {
                     sched.signups_start = signups_start.getDuration();
                 }
                 else if (sender == signups_end) {
                     sched.signups_end = signups_end.getDuration();
                 }
 
                 updateTimes();
             }
 
             private void updateTimes() {
                 Date d = new Date();
 
                 d.setTime(sched.start_time.getTime() - display_start.getDuration() * 1000);
                 display_start_time.setText(d.toString());
 
                 d.setTime(sched.start_time.getTime() + display_end.getDuration() * 1000);
                 display_end_time.setText(d.toString());
 
                 d.setTime(sched.start_time.getTime() - signups_start.getDuration() * 1000);
                 signups_start_time.setText(d.toString());
 
                 d.setTime(sched.start_time.getTime() - signups_end.getDuration() * 1000);
                 signups_end_time.setText(d.toString());
             }
 
             public void onClick(Widget sender) {
                 Widget toshow = null;
 
                 if (sender == rptdaily)        { toshow = dailyrptpanel; }
                 else if (sender == rptweekly)  { toshow = weeklyrptpanel; }
                 else if (sender == rptmonthly) { toshow = monthlyrptpanel; }
 
                 dailyrptpanel.setVisible(toshow == dailyrptpanel);
                 weeklyrptpanel.setVisible(toshow == weeklyrptpanel);
                 monthlyrptpanel.setVisible(toshow == monthlyrptpanel);
             }
         }
 
         VerticalPanel vpanel = new VerticalPanel();
 
         public ScheduleEditor(List<JSEventSchedule> schedules) {
             vpanel.setWidth("100%");
             vpanel.setHeight("100%");
 
             for (JSEventSchedule s : schedules) {
                 vpanel.add(new Schedule(s));
             }
 
             vpanel.add(new Schedule());
 
             initWidget(vpanel);
 
             setStyleName("Admin-ScheduleEditor");
         }
     }
 
     public class Schedules extends Composite {
         VerticalPanel vpanel = new VerticalPanel();
         ListBox eventlb = new ListBox();
 
         public Schedules() {
             eventlb.setWidth("100%");
             eventlb.setVisibleItemCount(20);
 
             eventlb.addChangeListener(new ChangeListener() {
                 public void onChange(Widget sender) {
                     int sel = eventlb.getSelectedIndex();
                     if (sel < 0) return;
 
                     String name = eventlb.getItemText(sel);
 
                     GoteFarm.testService.getEventSchedules(GoteFarm.sessionID, name, new AsyncCallback<List<JSEventSchedule>>() {
                         public void onSuccess(List<JSEventSchedule> results) {
                             setCenterWidget(new ScheduleEditor(results));
                         }
 
                         public void onFailure(Throwable caught) {
                         }
                     });
                 }
             });
 
             vpanel.add(eventlb);
 
             update();
 
             initWidget(vpanel);
 
             setStyleName("Admin-Schedules");
         }
 
         public void update() {
             eventlb.clear();
             GoteFarm.testService.getEventTemplates(GoteFarm.sessionID, new AsyncCallback<List<String>>() {
                 public void onSuccess(List<String> results) {
                     for (String t : results) {
                         eventlb.addItem(t);
                     }
                 }
 
                 public void onFailure(Throwable caught) {
                 }
             });
         }
     }
 
     public Admin() {
         dpanel.setWidth("100%");
 
         StackPanel sp = new StackPanel();
 
         sp.add(events, "Events");
         sp.add(schedules, "Schedules");
         sp.add(new Label("Accounts"), "Accounts");
         sp.add(new Label("Instances"), "Instances");
         sp.add(new Label("Bosses"), "Bosses");
         sp.add(new Label("Roles"), "Roles");
         sp.add(new Label("Badges"), "Badges");
 
         dpanel.add(sp, dpanel.WEST);
 
         initWidget(dpanel);
 
         setStyleName("Admin");
     }
 
     public void setCenterWidget(Widget widget) {
         if (centerWidget != null) {
             dpanel.remove(centerWidget);
         }
 
         if (widget != null) {
             dpanel.add(widget, dpanel.CENTER);
         }
 
         centerWidget = widget;
     }
 
     public void refresh() {
         events.update();
         schedules.update();
     }
 }
