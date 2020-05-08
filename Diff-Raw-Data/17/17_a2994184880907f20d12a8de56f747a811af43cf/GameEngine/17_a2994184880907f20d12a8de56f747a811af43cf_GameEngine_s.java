 /*
     GridRover -- A game to teach programming skills
     Copyright (C) 2008  Lucas Adam M. Paul
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
 
 package gridrover;
 
 public class GameEngine
 {
 	private RoverControlInterface controlInterface;
 	private MapGrid missionMap;
 	private Rover rover;
 	//private Lander lander; // Not yet used
 
 	public GameEngine(RoverControlInterface controlInterface, int width, int length)
 	{
 		this.controlInterface = controlInterface;
 		missionMap = new MapGrid(width, length);
 		MapSquare startsquare;
 		try
 		{
 			startsquare = missionMap.getSquare(width/2, length/2);
 		}
 		catch (OutOfBoundsException e)
 		{
 			throw new Error("Something funky happened!  Really funky!", e);
 		}
 		/*lander = */new Lander("Lander", 348.0, 11.236, startsquare); // Mass 348.0 kg, 2.65 meters diameter by 1.6 meters tall
 		rover = new Rover("Rover", 185.0, 5.52, startsquare); // Mass 185.0 kg, 1.5 meters tall by 2.3 meters wide by 1.6 meters long
 	}
 	
 	public GameEngine(RoverControlInterface controlInterface, int width, int length, double maxElevation, int precision)
 	{
 		this.controlInterface = controlInterface;
 		missionMap = new MapGrid(width, length, maxElevation, precision);
 		MapSquare startsquare;
 		try
 		{
 			startsquare = missionMap.getSquare(width/2, length/2);
 		}
 		catch (OutOfBoundsException e)
 		{
 			throw new Error("Something funky happened!  Really funky!", e);
 		}
 		/*lander = */new Lander("Lander", 348.0, 11.236, startsquare); // Mass 348.0 kg, 2.65 meters diameter by 1.6 meters tall
 		rover = new Rover("Rover", 185.0, 5.52, startsquare); // Mass 185.0 kg, 1.5 meters tall by 2.3 meters wide by 1.6 meters long
 	}
 
 	public void roverLoop()
 	{
 		boolean running = true;
 		Command command;
 		while (running)
 		{
 			command = controlInterface.getNextCommand();
 			switch (command.getCommandWord())
 			{
 			case QUIT:
 				running = false; break;
 			case GO:
 				go(command); break;
 			case LOOK:
 				look(); break;
 			case WAIT:
 				break;
 			default:
 				controlInterface.commandUnknown(command);
 			}
 		}
 	}
 
 	public void look()
 	{
 		MapSquare location = rover.getLocation();
 		controlInterface.describeLocation(location);
 	}
 
 	public void go(Command command)
 	{
 		Debug.debug("Attempting to go " + command.getArgs()[0]);
 		if (rover.go(command.getArgs()[0]))
 			controlInterface.commandSucceeded(command);
 		else
 			controlInterface.commandFailed(command);
 	}
 }
