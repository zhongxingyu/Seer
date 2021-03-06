 package com.sonyericsson.chkbugreport.plugins.logs.event;
 
 import com.sonyericsson.chkbugreport.Report;
 import com.sonyericsson.chkbugreport.Util;
 
 public class ProcStat {
 
     public String proc;
     public int count;
     public long totalTime;
     public long maxTime;
     public int restartCount;
     public long minRestartTime;
     public long totalRestartTime;
     public int bgKillRestartCount;
     public long minBgKillRestartTime;
     public long totalBgKillRestartTime;
     public int errors;
 
     private Report _br;
     private long _firstTs;
     private long _lastTs;
     private long _startTs;
     private long _killTs;
     private long _diedTs;
     private boolean _startGuessed;
 
     public ProcStat(Report br, String component, long firstTs, long lastTs) {
         _br = br;
         proc = component;
         _firstTs = firstTs;
         _lastTs = lastTs;
     }
 
     public void addData(AMData am) {
         long ts = am.getTS();
         switch (am.getAction()) {
         case AMData.PROC_KILL:
             checkProcStart(ts);
             if ("too many background".equals(am.getExtra())) {
                 procKill(ts);
             }
             break;
         case AMData.PROC_DIED:
             checkProcStart(ts);
             procDied(ts);
             break;
         case AMData.PROC_START:
             procStart(ts);
             break;
         }
     }
 
     private void procStart(long ts) {
         if (_startTs != 0) {
             err("am_proc_start without am_died", ts);
             errors++;
         }
         if (_killTs != 0) {
             long time = ts - _killTs;
             if (time < 0) {
                 err("negative restart-after-killed time", ts);
                 errors++;
             }
             bgKillRestartCount++;
             totalBgKillRestartTime += time;
             minBgKillRestartTime = bgKillRestartCount == 1 ? time : Math.min(minBgKillRestartTime, time);
             _killTs = 0;
         }
         if (_diedTs != 0) {
             long time = ts - _diedTs;
             if (time < 0) {
                 err("negative restart time", ts);
                 errors++;
             }
             restartCount++;
             totalRestartTime += time;
             minRestartTime = restartCount == 1 ? time : Math.min(minRestartTime, time);
             _diedTs = 0;
         }
         _startTs = ts;
     }
 
     private void procDied(long ts) {
         count++;
         long time = ts - _startTs;
         if (time < 0) {
             err("negative created time", ts);
             errors++;
         }
         totalTime += time;
         maxTime = Math.max(maxTime, time);
         _diedTs = ts;
         _startTs = 0;
     }
 
     private void procKill(long ts) {
         _killTs = ts;
     }
 
     private void checkProcStart(long ts) {
         if (_startTs == 0) {
             if (_startGuessed) {
                 err("am_proc_start was already guessed, but it's missing again", ts);
                 errors++;
                 _startTs = ts;
             } else {
                 err("am_proc_start was missing", ts);
                 _startTs = _firstTs;
                 _startGuessed = true;
             }
         }
     }
 
     private void err(String msg, long ts) {
        _br.printErr(5, "ProcStat: am_proc_start was already guessed, but it's missing again (" + proc + " @ " + Util.formatLogTS(ts) + ")");
     }
 
     public void finish() {
         if (_startTs != 0) {
             procDied(_lastTs);
         }
     }
 
 }
