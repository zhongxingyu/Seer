 package com.edinarobotics.zed.commands;
 
 import com.edinarobotics.zed.Components;
 import com.edinarobotics.zed.subsystems.Collector;
 import edu.wpi.first.wpilibj.command.Command;
 
 public class SetCollectorCommand extends Command {
     private Collector collector;
     private Collector.CollectorDirection collectorDirection;
     private Collector.CollectorLiftDirection collectorLiftDirection;
     
     public SetCollectorCommand(Collector.CollectorDirection collectorDirection, Collector.CollectorLiftDirection collectorLiftDirection) {
         super("SetCollector");
         this.collector = Components.getInstance().collector;
         this.collectorDirection = collectorDirection;
         this.collectorLiftDirection = collectorLiftDirection;
         requires(collector);
     }
     
     public SetCollectorCommand(Collector.CollectorDirection collectorDirection){
         this(collectorDirection, null);
     }
     
     public SetCollectorCommand(Collector.CollectorLiftDirection collectorLiftDirection){
         this(null, collectorLiftDirection);
     }
 
     protected void initialize() {
         if(collectorDirection != null){
             collector.setCollectorDirection(collectorDirection);
         }
        if(collectorDirection != null){
             collector.setCollectorLiftDirection(collectorLiftDirection);
         }
     }
 
     protected void execute() {
         
     }
 
     protected boolean isFinished() {
         return true;
     }
 
     protected void end() {
         
     }
 
     protected void interrupted() {
         
     }
     
 }
