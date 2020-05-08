 package com.giftoftheembalmer.gotefarm.client;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 import java.util.List;
 import java.util.Date;
 
 public class JSEventSchedule implements IsSerializable {
     public long esid;
     public long eid;
 
     public Date start_time;
    public Date orig_start_time;
     public int timezone_offset;
     public int duration;
 
     public int display_start;
     public int display_end;
 
     public int signups_start;
     public int signups_end;
 
     public int repeat_size;
     public int repeat_freq;
     public int day_mask;
     public int repeat_by;
 
     public boolean active;
 
     public JSEventSchedule() {
     }
 }
