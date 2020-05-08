 package org.usfirst.frc1318;
 
 import org.usfirst.frc1318.autonomous.AutoRunner;
 import org.usfirst.frc1318.minimike.calculators.MMCalculator;
 import org.usfirst.frc1318.minimike.components.MMGamePadReader;
 import org.usfirst.frc1318.minimike.components.MMLimitSwitchReader;
 import org.usfirst.frc1318.minimike.components.MMNetworkTableRunner;
 import org.usfirst.frc1318.minimike.components.MMTurretRunner;
 import org.usfirst.frc1318.testbed.calculators.TBKickerCalculator;
 import org.usfirst.frc1318.testbed.components.TBGamePadReader;
 import org.usfirst.frc1318.testbed.components.TBHelloWorldRunner;
 import org.usfirst.frc1318.testbed.components.TBSolenoidRunner;
 import org.usfirst.frc1318.testbed.components.TBThroughBeamReader;
 import org.usfirst.frc1318.FRC2013.calculators.DriveTrain1JoystickCalculator;
 import org.usfirst.frc1318.FRC2013.calculators.DriveTrainPIDCalculator;
 import org.usfirst.frc1318.FRC2013.calculators.LifterCalculator;
 import org.usfirst.frc1318.FRC2013.calculators.ShooterCalculator;
 import org.usfirst.frc1318.FRC2013.controllers.*;
 import org.usfirst.frc1318.FRC2013.runners.CompressorRunner;
 import org.usfirst.frc1318.FRC2013.runners.DriveTrainRunner;
 import org.usfirst.frc1318.FRC2013.runners.LifterRunner;
 import org.usfirst.frc1318.FRC2013.runners.NetworkTableRunner;
 import org.usfirst.frc1318.FRC2013.runners.ShooterAngleRunner;
 import org.usfirst.frc1318.FRC2013.runners.ShooterFireRunner;
 import org.usfirst.frc1318.FRC2013.runners.ShooterMotorRunner;
 import org.usfirst.frc1318.FRC2013.runners.ShooterPIDRunner;
 import org.usfirst.frc1318.FRC2013.runners.SolenoidTestRunner;
 
 public class MainIterativeComponentRobot extends IterativeComponentRobot {
 
 	public BotVector currentRobotComponents() {
 		return compressorTest();
 	}
 	
 	protected static BotVector gamePadTest()
 	{
 		BotVector botVector = new BotVector();
 		botVector.add(new GamePadReader());
 		botVector.add(new NetworkTableRunner());
 		return botVector;
 	}
 	
 	protected static BotVector CompressorTestBot(){
 		BotVector botVector = new BotVector();
 		botVector.add(new CompressorRunner());
 		return botVector;
 	}
 	
 	protected static BotVector competitionBot(){
 		BotVector botVector = new BotVector();
 		
 		botVector.add(new DriveTrainEncoderReader());
 		botVector.add(new GamePadReader());
 		botVector.add(new JoystickReader());
 		botVector.add(new LifterLimitSwitchReader());
 		//botVector.add(new ShooterEncoderReader());
 		
 		//Autonomous Here
 		
 		botVector.add(new LifterCalculator());
 		botVector.add(new DriveTrain1JoystickCalculator());
 		botVector.add(new ShooterCalculator());
 		
 		botVector.add(new DriveTrainPIDCalculator());
 		botVector.add(new ShooterPIDRunner());
 		botVector.add(new ShooterPIDRunner());
 		
 		botVector.add(new CompressorRunner());
 		botVector.add(new DriveTrainRunner());
 		botVector.add(new LifterRunner());
 		botVector.add(new ShooterAngleRunner());
 		botVector.add(new ShooterFireRunner());
 		botVector.add(new ShooterMotorRunner());
 		
 		botVector.add(new NetworkTableRunner());
 		
 		return botVector;
 		
 	}
 	
 	protected static BotVector motorTest() {
 		BotVector botVector = new BotVector();
 		botVector.add(new GamePadReader());
 		botVector.add(new NetworkTableRunner());
 		botVector.add(new ShooterMotorRunner());
 		return botVector;
 	}
 	
 	
 	protected static BotVector compressorTest(){
 		BotVector botVector = new BotVector();
 		
 		botVector.add(new CompressorRunner());
 	
 		return botVector;
 	}
 
 
 	protected static BotVector solenoidTest(){
 		BotVector botVector = new BotVector();
 		
 		botVector.add(new CompressorRunner());
 		botVector.add(new SolenoidTestRunner());
 		
 		return botVector;
 	}
 
 	protected static BotVector helloBot() {
 		BotVector botVector = new BotVector();
 		return botVector;
 	}
 	
 	protected static BotVector pneumaticTestBed() {
 		BotVector botVector = new BotVector();
 		botVector.add(new TBGamePadReader());
 		botVector.add(new TBKickerCalculator());
 		botVector.add(new TBSolenoidRunner());
 		botVector.add(new TBThroughBeamReader());
 		
 		return botVector;
 	}
 	
 	protected static BotVector helloWorld(){
 		BotVector botVector = new BotVector();
 		botVector.add(new TBHelloWorldRunner());
 		return botVector;
 	}
 	
 	protected static BotVector MiniMikeVector()
 	{
 		BotVector bv = new BotVector();
 		bv.add(new TBGamePadReader());
 		
 		//TODO add more components
 		
 		return bv;
 	}
 	
 	protected static BotVector MiniMikeNetworkTableTest() {
 		BotVector botVector = new BotVector();
 		botVector.add(new org.usfirst.frc1318.minimike.networktable.NetworkTableTest());
 		return botVector;
 	}
 	
 	protected static BotVector MiniMikeVector2(){
 		BotVector botVector = new BotVector();
 		botVector.add(new MMGamePadReader());
 		botVector.add(new MMLimitSwitchReader());
 		botVector.add(new AutoRunner());
 		botVector.add(new MMCalculator());
         botVector.add(new MMTurretRunner());
 		botVector.add(new MMNetworkTableRunner());
 		return botVector;
 	}
 }
