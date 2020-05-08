 package Jordan.bau5.FRC2115.commands;
 
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import Jordan.bau5.FRC2115.OI;
 import Jordan.bau5.FRC2115.subsystems.Chassis;
 
 public abstract class CommandBase extends Command {
 
     // Create a single static instance of all of your subsystems
     public static OI oi;
     public static Chassis chassis  = new Chassis();
 
     public static void init() {
         // This MUST be here. If the OI creates Commands (which it very likely
         // will), constructing it during the construction of CommandBase (from
         // which commands extend), subsystems are not guaranteed to be
         // yet. Thus, their requires() statements may grab null pointers. Bad
         // news. Don't move it.
         oi = new OI();
     }
 
     public CommandBase(String name) {
         super(name);
     }
 
     public CommandBase() {
         super();
     }
 }
