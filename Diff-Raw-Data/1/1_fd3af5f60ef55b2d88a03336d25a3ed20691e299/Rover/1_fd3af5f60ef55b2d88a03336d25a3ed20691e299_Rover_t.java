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
 
 import gridrover.PhysicalObject;
 import gridrover.MapSquare;
 
 public class Rover implements PhysicalObject
 {
 	private String name;
 	private double mass, bulk;
 	private MapSquare location;
 
 	public Rover(String name, double mass, double bulk, MapSquare location)
 	{
 		this.name = name;
 		this.mass = mass;
 		this.bulk = bulk;
 		this.location = location;
 		location.getInventory().add(this);
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 	
 	public double getMass()
 	{
 		return mass;
 	}
 
 	public double getBulk()
 	{
 		return bulk;
 	}
 	
 	public MapSquare getLocation()
 	{
 		return location;
 	}
 	
 	public boolean go(String direction)
 	{
 		MapSquare nextLocation;
 		try
 		{
 			nextLocation = location.getSquareDirFrom(direction);
 		}
 		catch (OutOfBoundsException e)
 		{
 			return false;
 		}
 		location.getInventory().remove(this);
 		nextLocation.getInventory().add(this);
 		location = nextLocation;
 		return true;
 	}
 }
