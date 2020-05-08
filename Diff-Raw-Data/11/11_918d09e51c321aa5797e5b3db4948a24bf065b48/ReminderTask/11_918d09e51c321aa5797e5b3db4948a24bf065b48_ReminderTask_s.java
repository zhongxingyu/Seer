 package net.robbytu.banjoserver.bungee.directsupport;
 
 import net.robbytu.banjoserver.bungee.Main;
 
 import java.util.concurrent.TimeUnit;
 
 public class ReminderTask implements Runnable {
     public void run() {
         Tickets.remindAdminsOfOpenTickets();
        Main.instance.getProxy().getScheduler().schedule(Main.instance, new ReminderTask(), 300, TimeUnit.SECONDS);
     }
 }
