 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 15th November 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.bookings.impl.slotsengine;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 
 import au.edu.uts.eng.remotelabs.schedserver.bookings.BookingEngineService;
 import au.edu.uts.eng.remotelabs.schedserver.bookings.impl.BookingEngine;
 import au.edu.uts.eng.remotelabs.schedserver.bookings.impl.BookingNotification;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Bookings;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RequestCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ResourcePermission;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigOfflineSchedule;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigType;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.User;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 
 /**
  * The slot booking engine. This is an in-memory booking engine with uses 
  * aligned booking time.
  */
 public class SlotBookingEngine implements BookingEngine, BookingEngineService
 {
     /** The length of each booking slot in seconds. */
     public static final int TIME_QUANTUM = 15 * 60;
     
     /** The number of slots in a day. */
     public static final int NUM_SLOTS = 24 * 60 * 60 / SlotBookingEngine.TIME_QUANTUM;
     
     /** The last slot number in a day. */
     public static final int END_SLOT = SlotBookingEngine.NUM_SLOTS - 1;
     
     /** The loaded of day bookings. */
     private final Map<String, DayBookings> days;
     
     /** The amount of hits the day has had. */
     private final Map<String, Integer> dayHitCounts;
     
     /** Redeemer which redeems and cleans bookings. */
     private Redeemer redeemer;
     
     /** Logger. */
     private Logger logger;
     
     public SlotBookingEngine()
     {
         this.logger = LoggerActivator.getLogger();
         this.days = new TreeMap<String, DayBookings>();
         this.dayHitCounts = new HashMap<String, Integer>();
     }
     
     @Override
     public synchronized BookingInit init()
     {
         this.logger.debug("Initalising the slot booking engine...");
         
         Calendar today = Calendar.getInstance();
         
         /* Cancel all bookings in the past. */
         Session db = DataAccessActivator.getNewSession();
         @SuppressWarnings("unchecked")
         List<Bookings> bookings = db.createCriteria(Bookings.class)
             .add(Restrictions.eq("active", Boolean.TRUE))
             .add(Restrictions.lt("endTime", today.getTime()))
             .list();
         for (Bookings bk : bookings)
         {
             this.logger.warn("Cancelling booking (" + bk.getId() + ") which expired when the Scheduling Server " +
            		"wasn't running for user " +   bk.getUser().qName() + " which expired on " + bk.getEndTime() + '.');
             bk.setActive(false);
             bk.setCancelReason("Expired when Scheduling Server wasn't running.");
             
             new BookingNotification(bk).notifyCancel();
         }
         if (bookings.size() > 0)
         {
             db.beginTransaction();
             db.flush();
             db.getTransaction().commit();
         }
         
         /* Load up the current day bookings. */
         DayBookings day = this.getDayBookings(TimeUtil.getDayKey(today));
         day.fullLoad(db);
         
         /* Initalise the management tasks. */
         BookingInit init = new BookingInit();
         this.redeemer = new Redeemer(this, day);
         init.addTask(this.redeemer);
         init.addListener(this.redeemer);
         init.addListener(new RigRegisteredListener());
         init.addTask(new DayCleaner());
         
         return init;
     }
 
     @Override
     public BookingCreation createBooking(User user, ResourcePermission perm, TimePeriod tp, Session ses)
     {
         Calendar start = tp.getStartTime();
         Calendar end = tp.getEndTime();
         
         BookingCreation response = new BookingCreation();
         
         Bookings bk = new Bookings();
         bk.setActive(true);
         bk.setResourcePermission(perm);
         
         /* Timing information. */
         bk.setStartTime(start.getTime());
         bk.setEndTime(end.getTime());
         bk.setDuration((int) (end.getTimeInMillis() - start.getTimeInMillis()) / 1000);
         
         /* User information. */
         bk.setUser(user);
         bk.setUserNamespace(user.getNamespace());
         bk.setUserName(user.getName());
         
         DayBookings db;
         List<String> dayKeys = TimeUtil.getDayKeys(start.getTime(), end.getTime());
         if (dayKeys.size() == 1)
         {
             /* Single day booking so we can proceed normally creating the booking. */
             String day = dayKeys.get(0);
             
             bk.setResourceType(perm.getType());
             bk.setRig(perm.getRig());
             bk.setRigType(perm.getRigType());
             bk.setRequestCapabilities(perm.getRequestCapabilities());
             
             MBooking mb = new MBooking(bk, day);
             synchronized (db = this.getDayBookings(day))
             {
                 if (db.createBooking(mb, ses))
                 {
                     response.setWasCreated(true);
                     try
                     {
                         /* Save the booking to the database. */
                         ses.beginTransaction();
                         ses.save(bk);
                         ses.getTransaction().commit();
                         response.setWasCreated(true);
                         response.setBooking(bk);
                         
                         String info = "Successfully created booking for " + user.getNamespace() + ':' + user.getName() + 
                                 " on ";
                         switch (mb.getType())
                         {
                             case RIG:        info += "rig " + mb.getRig().getName(); break;
                             case TYPE:       info += "rig type " + mb.getRigType().getName(); break;
                             case CAPABILITY: info += "capabilities " + mb.getRequestCapabilities().getCapabilities(); break;
                         }
                         info += " to start at " + bk.getStartTime() + " and finish " + bk.getEndTime() + '.';
                         this.logger.info(info);
                     }
                     catch (HibernateException ex)
                     {
                         this.logger.error("Failed to persist a booking to the databse. Error: " + ex.getMessage() + 
                                 ". Rolling back and removing the booking.");
                         ses.getTransaction().rollback();
                         response.setWasCreated(false);
                         db.removeBooking(mb);
                     }
                 }
                 else
                 {
                     response.setWasCreated(false);
                     
                     long rs;
                     for (MRange range : db.findBestFits(mb, ses))
                     {
                         if ((rs = range.getStart().getTimeInMillis()) < perm.getStartTime().getTime() ||
                              rs < System.currentTimeMillis())
                         {
                             this.logger.info("Excluding best fit option which was to start at " + range.getStart().getTime() +
                                     " because it would be before the permission start or is in the past .");
                         }
                         else if (range.getEnd().getTimeInMillis() > perm.getExpiryTime().getTime())
                         {
                             this.logger.info("Excluding best fit option which was to end at " + range.getEnd().getTime() +
                                     " because it would finish after the permission end time.");
                         }
                         else
                         {                       
                             response.addBestFit(new TimePeriod(range.getStart(), range.getEnd()));
                         }
                     }
                 }
             }
         }
         else
         {
             /* Multi-day booking so we will need to do some fiddling. */
             
             /* 1) If the booking is less than an hour each way of a hour divide,
              * give a best fit solution and not allow the booking to be created. */
             // TODO multi-day
         }
         
         return response;
     }
 
     @Override
     public List<TimePeriod> getFreeTimes(Rig rig, TimePeriod period, int minDuration, Session ses)
     {
         /* Work out the slots that the minimum duration requires. */
         int minSlots = minDuration / TIME_QUANTUM;
         
         /* Which days this falls across. */
         List<String> dayKeys = TimeUtil.getDayKeys(period);
         List<MRange> free = new ArrayList<MRange>();
         
         DayBookings db;
         for (String dayKey : dayKeys)
         {
             synchronized (db = this.getDayBookings(dayKey))
             {
                 free.addAll(db.getFreeSlots(rig, 
                         TimeUtil.getDaySlotIndex(period.getStartTime(), dayKey), 
                         TimeUtil.getDaySlotIndex(period.getEndTime(), dayKey), 
                         minSlots, ses));
             }
         }
         
         return this.periodRangeCheck(MRange.rangeToTimePeriod(free), period);
     }
 
     @Override
     public List<TimePeriod> getFreeTimes(RigType rigType, TimePeriod period, int minDuration, Session ses)
     {
         int minSlots = minDuration / TIME_QUANTUM;
         List<String> dayKeys = TimeUtil.getDayKeys(period);
         List<MRange> free = new ArrayList<MRange>();
         
         DayBookings db;
         for (String dayKey : dayKeys)
         {
             synchronized (db = this.getDayBookings(dayKey))
             {
                 free.addAll(db.getFreeSlots(rigType, 
                         TimeUtil.getDaySlotIndex(period.getStartTime(), dayKey), 
                         TimeUtil.getDaySlotIndex(period.getEndTime(), dayKey), 
                         minSlots, ses));
             }
         }
         
         return this.periodRangeCheck(MRange.rangeToTimePeriod(free), period);
     }
 
     @Override
     public List<TimePeriod> getFreeTimes(RequestCapabilities caps, TimePeriod period, int minDuration, Session ses)
     {
         int minSlots = minDuration / TIME_QUANTUM;
         List<String> dayKeys = TimeUtil.getDayKeys(period);
         List<MRange> free = new ArrayList<MRange>();
         
         DayBookings db;
         for (String dayKey : dayKeys)
         {
             synchronized (db = this.getDayBookings(dayKey))
             {
                 free.addAll(db.getFreeSlots(caps, 
                         TimeUtil.getDaySlotIndex(period.getStartTime(), dayKey), 
                         TimeUtil.getDaySlotIndex(period.getEndTime(), dayKey), 
                         minSlots, ses));
             }
         }
         
         return this.periodRangeCheck(MRange.rangeToTimePeriod(free), period);
     }
     
     @Override
     public boolean cancelBooking(Bookings booking, String reason, Session ses)
     {
         boolean response = true;
         
         /* Remove the booking from the memory allocations. */
         DayBookings db;
         for (String dayKey : TimeUtil.getDayKeys(booking.getStartTime(), booking.getEndTime()))
         {
             synchronized (this.days)
             {
                 if (this.days.containsKey(dayKey))
                 {
                     synchronized (db = this.days.get(dayKey))
                     {
                         if (!db.removeBooking(new MBooking(booking, dayKey))) response = false; 
                     }
                 }
             }
         }
         
         /* Cancel the booking. */ 
         booking.setActive(false);
         booking.setCancelReason(reason);
         ses.beginTransaction();
         ses.flush();
         ses.getTransaction().commit();
         
         return response;
     }
     
     @Override
     public boolean isFreeFor(Rig rig, int duration, org.hibernate.Session db)
     {
         Calendar now = Calendar.getInstance();
         Calendar end = Calendar.getInstance();
         end.add(Calendar.SECOND, duration);
 
         for (String day : TimeUtil.getDayKeys(now.getTime(), end.getTime()))
         {
                if (!this.getDayBookings(day).isRigFree(rig, TimeUtil.getDaySlotIndex(now, day), 
                     TimeUtil.getDaySlotIndex(end, day), db)) return false;
         }
 
         return true;
     }
     
     @Override
     public boolean putQueuedSession(Rig rig, au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session ses,
            Session db)
     {   
         Calendar now = Calendar.getInstance();
         String dayKey = TimeUtil.getDayKey(now);
         MBooking mb = new MBooking(ses, rig, now, dayKey), nb = mb;
         boolean success = false;
         
         DayBookings dayb;
         if (mb.isMultiDay())
         {
             Calendar end = Calendar.getInstance();
             end.add(Calendar.SECOND, ses.getDuration());
             
             Map<String, MBooking> allocs = new HashMap<String, MBooking>();
             for (String day : TimeUtil.getDayKeys(now.getTime(), end.getTime()))
             {
                 if (!dayKey.equals(day)) nb = new MBooking(ses, rig, now, day);
                 synchronized (dayb = this.getDayBookings(day))
                 {
                     if ((success = dayb.createBooking(nb, db))) allocs.put(day, nb);
                     else break;
                 }
             }
             
             /* If one of the days failed to be assigned. We need to roll back
              * the current allocations. */
             if (!success && allocs.size() > 0)
             {
                 for (Entry<String, MBooking> e : allocs.entrySet())
                 {
                     synchronized (dayb = this.getDayBookings(e.getKey()))
                     {
                         dayb.removeBooking(e.getValue());
                     }
                 }
             }
         }
         else
         {
             /* The booking is only on a single day so we don't need to go 
              * across days. */
             synchronized (dayb = this.getDayBookings(dayKey))
             {
                 success = dayb.createBooking(mb, db);
             }
         }
         
         if (success)
         {
             this.redeemer.putRunningSession(rig, mb);
         }
         return success;
     }
 
     @Override
     public boolean extendQueuedSession(Rig rig, au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session ses,
             int duration, Session db)
     {
         MBooking mb = this.redeemer.getRunningSession(rig);
         if (mb == null)
         {
             this.logger.error("Trying to extend a session that doesn't exist. Failing...");
             return false;
         }
         
         /* Work out the new end time and where this falls. */
         Calendar end = mb.getEnd();
         end.add(Calendar.SECOND, duration);
         String extDay = TimeUtil.getDayKey(end);
         int extEnd = TimeUtil.getSlotIndex(end);
         
         DayBookings dayb;
         if (mb.getDay().equals(extDay))
         {
             /* If we are still in the same slot as before, allow the time extension. */
             if (mb.getEndSlot() == extEnd) return mb.extendBooking(duration);
             
             /* Otherwise, attempt to extend the existing mbooking on the day. */
             dayb = this.getDayBookings(extDay);
             if (dayb.isRigFree(rig, mb.getEndSlot() + 1, extEnd, db))
             {
                 /* Check whether the booking can be extended. */
                 synchronized (dayb)
                 {
                     return dayb.isRigFree(ses.getRig(), mb.getEndSlot() + 1, extEnd, db) && 
                        mb.extendBooking(duration) && dayb.extendBooking(rig, mb, db); // Extend and commit the extension
                 }
             }
         }
         else
         {
             MBooking extDayMB = this.getDayBookings(extDay).getBookingOnSlot(rig, 0);
             if (extDayMB == null)
             {
                 /* ---- Some preconditions must hold before extending bookings:
                  *  1) If the booking on the start day doesn't finish at the end of the day
                  *     it must be extendable to the end day, provided the day hasn't elapsed. */
                 if (mb.getEndSlot() != END_SLOT && this.days.containsKey(mb.getDay()) &&
                     this.getDayBookings(mb.getDay()).isRigFree(rig, mb.getEndSlot() + 1, END_SLOT, db))
                 {
                     /* The current day cannot be extended, so the time extension can't be allowed. */
                     return false;
                 }
                 
                 /*  2) Any days between the extension end and the current end must be extendable.
                  *     This for the exceptional case that the total extension duration is greater
                  *     than a day. */
                 Calendar dayScroll = Calendar.getInstance();
                 dayScroll.setTimeInMillis(end.getTimeInMillis());
                 dayScroll.add(Calendar.DAY_OF_MONTH, -1);
                 String scrollDay;
                 MBooking scrollmb;
                 
                 while (!mb.getDay().equals(scrollDay = TimeUtil.getDayKey(dayScroll)))
                 {
                     dayScroll.add(Calendar.DAY_OF_MONTH, -1);
                     
                     /* Day unloaded so in the past therefore we can ignore it. */
                     if (!this.days.containsKey(scrollDay)) continue;
                     
                     dayb = this.getDayBookings(scrollDay);
                     if ((scrollmb = dayb.getBookingOnSlot(rig, 0)) == null)
                     {
                         if (!dayb.isRigFree(rig, 0, END_SLOT, db)) return false;
                     }
                     else
                     {
                         if (scrollmb.getEndSlot() != END_SLOT && 
                                 !dayb.isRigFree(rig, scrollmb.getEndSlot() + 1, END_SLOT, db)) return false;
                     }
                 }
                 
                 /*  3) The extension day must be free for the extension slots. */
                 if (!this.getDayBookings(extDay).isRigFree(rig, 0, extEnd, db)) return false;
 
 
                 /*---- Preconditions are satisfied so we can commit the extension.
                  *  1) Extend the current day booking provided it is free. */
                 if (mb.getEndSlot() != END_SLOT && this.days.containsKey(mb.getDay()))
                 {
                     synchronized (dayb = this.getDayBookings(mb.getDay()))
                     {
                         /* Double check to prevent a race condition. */
                         if (!(dayb.isRigFree(rig, mb.getEndSlot() + 1, END_SLOT, db) && 
                                 mb.extendBooking(duration) && dayb.extendBooking(rig, mb, db))) return false;
                     }
                 }
                 else mb.extendBooking(duration);
                 
                 /*  2) Commit the intermediary day bookings. */
                 dayScroll.setTimeInMillis(end.getTimeInMillis());
                 dayScroll.add(Calendar.DAY_OF_MONTH, -1);
                 while (!mb.getDay().equals(scrollDay = TimeUtil.getDayKey(dayScroll)))
                 {
                     dayScroll.add(Calendar.DAY_OF_MONTH, -1);
                     
                     /* Day unloaded so in the past therefore we can ignore it. */
                     if (!this.days.containsKey(scrollDay)) continue;
                     
                     synchronized (dayb = this.getDayBookings(scrollDay))
                     {
                         if ((scrollmb = dayb.getBookingOnSlot(rig, 0)) == null)
                         {
                             scrollmb = new MBooking(mb.getSession(), rig, mb.getStart(), scrollDay);
                             scrollmb.extendBooking(mb.getDuration() - scrollmb.getDuration());
                             if (!(dayb.isRigFree(rig, 0, NUM_SLOTS - 1, db) && dayb.createBooking(scrollmb, db))) return false;
                         }
                         else
                         {
                             if (!(dayb.isRigFree(rig, scrollmb.getEndSlot() + 1, NUM_SLOTS - 1, db) && 
                                     scrollmb.extendBooking(duration) && dayb.removeBooking(scrollmb))) return false;
                         }
                     }
                 }
                 
                 /*  3) Commit the booking on the extension end day. */
                 synchronized (dayb = this.getDayBookings(extDay))
                 {
                     MBooking extmb = new MBooking(mb.getSession(), rig, mb.getStart(), extDay);
                     extmb.extendBooking(mb.getDuration() - extmb.getDuration());
                     return dayb.isRigFree(rig, 0, extEnd, db) && dayb.createBooking(extmb, db);
                 }
             }
             else
             {
                 if (extDayMB.getSession() == null || !extDayMB.getSession().getId().equals(mb.getSession().getId()))
                 {
                     /* The booking on the slot is not the session getting extended, 
                      * so we cannot extend the session because another booking is
                      * going to start on it. */
                     return false;
                 }
                 
                 
                 if (extDayMB.getEndSlot() == extEnd)
                 {
                     /* The extension is still in same slot. */
                     return mb.extendBooking(duration) && extDayMB.extendBooking(duration);
                 }
                 
                 dayb = this.getDayBookings(extDay);
                 if (dayb.isRigFree(rig, extDayMB.getEndSlot() + 1, extEnd, db))
                 {
                     /* Check whether the booking can be extended. */
                     synchronized (dayb)
                     {
                         return dayb.isRigFree(ses.getRig(), extDayMB.getEndSlot() + 1, extEnd, db) && 
                            mb.extendBooking(duration) && extDayMB.extendBooking(duration) && 
                            dayb.extendBooking(rig, extDayMB, db); // Extend and commit the extension
                     }
                 }
             }
         }
 
         return false;
     }
 
     /**
      * Checks the range of the time period such that the first time period starts 
      * later or equal to the specified period start and ends earlier or equal to
      * specified period end.
      * 
      * @param fp list of time periods
      * @param period specified period
      * @return fp parameter
      */
     private List<TimePeriod> periodRangeCheck(List<TimePeriod> fp, TimePeriod period)
     {
         if (fp.size() > 0)
         {   
             /* Ranging check. Make sure the free start and end times are not 
              * earlier or later than the specified start or end periods 
              * respectively. */
             if (fp.get(0).getStartTime().before(period.getStartTime()))
             {
                 fp.set(0, new TimePeriod(period.getStartTime(), fp.get(0).getEndTime()));
             }
             
             if (fp.get(fp.size() - 1).getEndTime().after(period.getEndTime()))
             {
                 fp.set(fp.size() - 1, new TimePeriod(fp.get(fp.size() - 1).getStartTime(), period.getEndTime()));
             }
         }
 
         return fp;
     }
     
     /**
      * A rig has been registered. So notify each day to update
      * the resource loop mappings.
      * 
      * @param rig rig that was registered
      * @param db database session
      */
     public void rigRegistered(Rig rig, Session db)
     {
         this.logger.debug("Received rig " + rig.getName() + " registered event.");
         synchronized (this.days)
         {
             for (DayBookings day : this.days.values())
             {
                 synchronized (day)
                 {
                     day.rigRegistered(rig, db);
                 }
             }
         }
     }
     
     @Override
     public void putRigOffline(RigOfflineSchedule period, Session db)
     {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void clearRigOffline(RigOfflineSchedule period, Session db)
     {
         // TODO Auto-generated method stub
         
     }
     
     /**
      * Returns the bookings for that day. If that day isn't loaded, it is 
      * loaded.
      * 
      * @param dayKey day key
      * @return bookings
      */
     public DayBookings getDayBookings(String dayKey)
     {
         return this.getDayBookings(dayKey, true);
     }
     
     /**
      * Returns the bookings for that day. The load parameter specifies whether
      * the day should be loaded if it is currently unloaded.
      * 
      * @param dayKey day key
      * @param load 
      * @return bookings
      */
     public DayBookings getDayBookings(String dayKey, boolean load)
     {
         if (!this.days.containsKey(dayKey) && load)
         {
             synchronized (this.days)
             {
                 if (!this.days.containsKey(dayKey))
                 {
                     this.days.put(dayKey, new DayBookings(dayKey));
                     this.dayHitCounts.put(dayKey, 1);
                 }
             }
         }
         else if (load)
         {
             this.dayHitCounts.put(dayKey, this.dayHitCounts.get(dayKey) + 1);
         }
         
         return this.days.get(dayKey);
     }
     
     
     
     /**
      * Removes the day from the day listing. 
      * 
      * @param dayKey day bookings
      */
     public void removeDay(String dayKey)
     {
         synchronized (this.days)
         {
             this.days.remove(dayKey);
             this.dayHitCounts.remove(dayKey);
         }
     }
     
     /** The number of days to not unload from the current days. */
     public static final int HOT_DAYS = 10;
     
     /** The maximum number of days to keep in memory. */
     public static final int MAX_DAYS = 90;
     
     /**
      * Remove stale days from the current days loaded list. Stale days are
      * those which:
      * <ul>
      *  <li>Days in the past.</li>
      *  <li>Days in the <em>not</em> near future that haven't been used 
      *  much.</li>
      * </ul>
      * NOTE: This should run infrequently as it requires an exclusive lock
      * on the whole booking engine.
      */
     public void cleanStaleDays()
     {
         Calendar cal = Calendar.getInstance();
         String currentDay = TimeUtil.getDayKey(cal);
         cal.add(Calendar.DAY_OF_MONTH, HOT_DAYS);
         String hotDayThres = TimeUtil.getDayKey(cal);
         cal.add(Calendar.DAY_OF_MONTH, MAX_DAYS - HOT_DAYS);
         String maxDayThres = TimeUtil.getDayKey(cal);
         
         this.logger.info("Running booking engine stale day clean. Clean thresholds are: current day=" + currentDay +
         		", hot days=" + hotDayThres + ", max days=" + maxDayThres);
         
         synchronized (this.days)
         {
             Iterator<String> daysIt = this.days.keySet().iterator();
             while (daysIt.hasNext())
             {
                 String day = daysIt.next();
                 if  (day.compareTo(currentDay) < 0)
                 {
                     /* Day in the past. */
                     daysIt.remove();
                     this.dayHitCounts.remove(day);
                 }
                 else if (day.compareTo(hotDayThres) <= 0) continue; // Day in hot range.
                 else if (day.compareTo(maxDayThres) <= 0)
                 {
                     /* In this time region only remove days that are infrequently 
                      * used. */
                     int dc = this.dayHitCounts.get(day);
                     if (dc < 3)
                     {
                         daysIt.remove();
                         this.dayHitCounts.remove(day);
                     }
                     else this.dayHitCounts.put(day, dc / 2);
                 }
                 else
                 {
                     /* Day in future past max days. */
                     daysIt.remove();
                     this.dayHitCounts.remove(day);
                 }
                 
             }
         }
     }
     
     @Override
     public void cleanUp()
     {
         this.logger.debug("Cleaning up the slot booking engine by clearing all days.");
         this.days.clear();
     }
 }
