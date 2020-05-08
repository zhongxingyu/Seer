 package com.giftoftheembalmer.gotefarm.client;
 
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import java.util.Date;
 import java.util.List;
 
 public class ScheduleEditor extends Composite {
     static int last_group_num = 0;
 
     class Schedule extends Composite implements ChangeListener, ClickListener {
         JSEventSchedule sched;
 
         // append a distinct number to radio button group names, per schedule,
         // so multiple schedules' radio buttons don't end up in the same group
         final int radio_group_num = last_group_num++;
         final String REPEAT_GROUP = "repeatGroup_" + radio_group_num;
         final String DAYOF_GROUP = "dayofGroup_" + radio_group_num;
 
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
         final DateTimeFormat time_formatter = DateTimeFormat.getShortDateTimeFormat();
         final RadioButton rptnever = new RadioButton(REPEAT_GROUP, "Never");
         final RadioButton rptdaily = new RadioButton(REPEAT_GROUP, "Daily");
         final RadioButton rptweekly = new RadioButton(REPEAT_GROUP, "Weekly");
         final RadioButton rptmonthly = new RadioButton(REPEAT_GROUP, "Monthly");
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
         final RadioButton rptdayofmonth = new RadioButton(DAYOF_GROUP, "day of the month");
         final RadioButton rptdayofweek = new RadioButton(DAYOF_GROUP, "day of the week");
 
         public Schedule(long eid, JSEventSchedule sched) {
             this.sched = sched;
 
             if (this.sched == null) {
                 this.sched = sched = new JSEventSchedule();
 
                 sched.esid = -1;
                 sched.eid = eid;
 
                 sched.start_time = new Date();
                 sched.start_time.setTime(
                     sched.start_time.getTime() -
                     (sched.start_time.getTime() % 3600000)
                 );
                sched.orig_start_time = sched.start_time;
 
                 // FIXME: this doesn't handle daylight saving time properly
                 sched.timezone_offset = sched.start_time.getTimezoneOffset();
 
                 sched.duration = 120 * 60;
 
                 sched.display_start = 7 * 86400;
                 sched.display_end = 3600;
 
                 sched.signups_start = 2 * 86400;
                 sched.signups_end = 1800;
 
                 sched.repeat_size = 0;
                 sched.repeat_freq = 1;
                 sched.day_mask = 1 << sched.start_time.getDay();
                 sched.repeat_by = 0;
 
                 sched.active = true;
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
                         final int index = dailyrptdays.getSelectedIndex();
 
                         Schedule.this.sched.repeat_freq = index + 1;
 
                         if (index > 0) {
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
                         final int index = weeklyrptweeks.getSelectedIndex();
 
                         Schedule.this.sched.repeat_freq = index + 1;
 
                         if (index > 0) {
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
 
                 final ClickListener daychanged = new ClickListener() {
                     public void onClick(Widget sender) {
                         final boolean checked = ((CheckBox)sender).isChecked();
 
                         for (int i = 0; i < 7; ++i ) {
                             if (sender == weeklydays[i]) {
                                 if (checked) {
                                     Schedule.this.sched.day_mask |= (1 << i);
                                 }
                                 else {
                                     Schedule.this.sched.day_mask &= ~(1 << i);
                                 }
                                 break;
                             }
                         }
                     }
                 };
 
                 for (int i = 0; i < 7; ++i ) {
                     weeklydays[i].addStyleName("padleft");
 
                     if ((sched.day_mask & (1 << i)) > 0) {
                         weeklydays[i].setChecked(true);
                     }
 
                     weeklydays[i].addClickListener(daychanged);
 
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
                         final int index = monthlyrptmonth.getSelectedIndex();
 
                         Schedule.this.sched.repeat_freq = index + 1;
 
                         if (index > 0) {
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
 
                 final ClickListener rptdayofchanged = new ClickListener() {
                     public void onClick(Widget sender) {
                         if (sender == rptdayofmonth) {
                             Schedule.this.sched.repeat_by = 0;
                         }
                         else {
                             Schedule.this.sched.repeat_by = 1;
                         }
                     }
                 };
 
                 if (sched.repeat_by == 0) {
                     rptdayofmonth.setChecked(true);
                 }
                 else {
                     rptdayofweek.setChecked(true);
                 }
 
                 rptdayofmonth.addClickListener(rptdayofchanged);
                 rptdayofweek.addClickListener(rptdayofchanged);
 
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
 
             HorizontalPanel hpanel = new HorizontalPanel();
             hpanel.setWidth("100%");
 
             final Label errmsg = new Label();
             errmsg.addStyleName(errmsg.getStylePrimaryName() + "-bottom");
 
             Button save = new Button("Save", new ClickListener() {
                 public void onClick(Widget sender) {
                     // clear error message
                     errmsg.setText("");
 
                     // update timezone_offset when saving
                     Schedule.this.sched.timezone_offset = Schedule.this.sched.start_time.getTimezoneOffset();
 
                    // clone start_time into orig_start_time
                    Schedule.this.sched.orig_start_time = Schedule.this.sched.start_time;

                     GoteFarm.goteService.saveEventSchedule(GoteFarm.sessionID, Schedule.this.sched, new AsyncCallback<Boolean>() {
                         public void onSuccess(Boolean result) {
                             errmsg.removeStyleName(errmsg.getStylePrimaryName() + "-error");
                             errmsg.setText("Schedule saved successfully.");
                         }
 
                         public void onFailure(Throwable caught) {
                             errmsg.addStyleName(errmsg.getStylePrimaryName() + "-error");
                             errmsg.setText(caught.getMessage());
                         }
                     });
                 }
             });
 
             save.addStyleName(save.getStylePrimaryName() + "-bottom");
             save.addStyleName(save.getStylePrimaryName() + "-left");
 
             hpanel.add(save);
             hpanel.add(errmsg);
 
             vpanel.add(hpanel);
 
             initWidget(vpanel);
 
             setStyleName("Admin-Schedule");
         }
 
         public Schedule(long eid) {
             this(eid, null);
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
             display_start_time.setText(time_formatter.format(d));
 
             d.setTime(sched.start_time.getTime() + display_end.getDuration() * 1000);
             display_end_time.setText(time_formatter.format(d));
 
             d.setTime(sched.start_time.getTime() - signups_start.getDuration() * 1000);
             signups_start_time.setText(time_formatter.format(d));
 
             d.setTime(sched.start_time.getTime() - signups_end.getDuration() * 1000);
             signups_end_time.setText(time_formatter.format(d));
         }
 
         public void onClick(Widget sender) {
             Widget toshow = null;
 
             int repeat_size = 0;
 
             if      (sender == rptdaily)   { toshow = dailyrptpanel;   repeat_size = 1; }
             else if (sender == rptweekly)  { toshow = weeklyrptpanel;  repeat_size = 2; }
             else if (sender == rptmonthly) { toshow = monthlyrptpanel; repeat_size = 3; }
 
             dailyrptpanel.setVisible(toshow == dailyrptpanel);
             weeklyrptpanel.setVisible(toshow == weeklyrptpanel);
             monthlyrptpanel.setVisible(toshow == monthlyrptpanel);
 
             sched.repeat_size = repeat_size;
         }
     }
 
     VerticalPanel vpanel = new VerticalPanel();
 
     public ScheduleEditor(long eid, List<JSEventSchedule> schedules) {
         vpanel.setWidth("100%");
         vpanel.setHeight("100%");
         vpanel.setSpacing(40);
 
         for (JSEventSchedule s : schedules) {
             vpanel.add(new Schedule(eid, s));
         }
 
         vpanel.add(new Schedule(eid));
 
         initWidget(vpanel);
 
         setStyleName("Admin-ScheduleEditor");
     }
 }
