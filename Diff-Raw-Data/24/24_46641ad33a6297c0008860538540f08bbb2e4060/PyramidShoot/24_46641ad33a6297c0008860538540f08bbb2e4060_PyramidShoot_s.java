 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package storm2013.commands.autonomous;
 
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.command.CommandGroup;
 import edu.wpi.first.wpilibj.command.WaitForChildren;
 import storm2013.Robot;
 import storm2013.commands.DoNothing;
 import storm2013.commands.EncoderDrive;
 import storm2013.commands.LowerTilter;
 import storm2013.commands.Shoot;
 import storm2013.commands.SpinDown;
 import storm2013.commands.TargetPIDTilt;
 import storm2013.commands.TargetPIDTurn;
 import storm2013.subsystems.Vision;
 import storm2013.utilities.Target;
 
 /**
  *
  * @author Joe
  */
 public class PyramidShoot extends CommandGroup {
     
     public PyramidShoot() {
         addParallel(new Command() {
             {
                 requires(Robot.vision);
             }
             protected void initialize() {
                 Robot.vision.setDistance(Vision.Distance.CENTER);
             }
             protected void execute() {}
             protected boolean isFinished() {
                 return true;
             }
             protected void end() {}
             protected void interrupted() {}
         });
         addSequential(new EncoderDrive(-1606.0, 0.6));
         addSequential(new LowerTilter(),1.5);
         addParallel(new TargetPIDTilt(Target.ThreePT, 1.0, false));
         addParallel(new TargetPIDTurn(Target.ThreePT,1.0,false));
         addSequential(new WaitForChildren());
         addSequential(new Shoot());
         addSequential(new Shoot());
         addSequential(new Shoot());
         addSequential(new Shoot());
         addSequential(new SpinDown());
     }
 }
