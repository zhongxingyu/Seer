 package com.cs2340.spacetrader;
 
 //planet
 public class Ship {
 	private ShipInventory inventory;
 	private String planetName;
 	private int fuel;
 	private int fuelCapacity;
 	
 
 	public Ship(int gold, int nSlots)
 	{
 		this.inventory = new ShipInventory(gold, nSlots);
 		this.planetName = GameSetup.theMap.getPlanetArray()[0].getName();
 		//TODO think of a centralized place to put all these kinds of numbers.
 		this.fuel = 100;
 		this.fuelCapacity = 100;
 	}
 
 	public ShipInventory getInventory()
 	{
 		return inventory;
 	}
 	
 	public String getPlanetName()
 	{
 		return planetName;
 	}
 	
 	public int getFuel()
 	{
 		return fuel;
 	}
 
 	public int getFuelCapacity()
 	{
 		return fuelCapacity;
 	}
 	
 	public void moveToPlanet(Planet newPlanet)
 	{
 		//These must be ordered in this way. deltaFuel depends on being on the planet moved from
 		deltaFuel(fuelCost(newPlanet));
 		planetName = newPlanet.getName();
 	}
 	
 	public int fuelCost(Planet newPlanet)
 	{
 		int[] destCoords = newPlanet.getCoordinate();
 		int[] currentCoords =  GameSetup.theMap.getPlanet(this.planetName).getCoordinate();
 		return fuelMetric((destCoords[0]-currentCoords[0]),(destCoords[1]-currentCoords[1]));
 	}
 
 	public void deltaFuel(int fuelAmount)
 	{
		int newFuel = this.fuel-fuelAmount;
 		if (newFuel < 0)
 			this.fuel=0;
 		else if (newFuel > this.fuelCapacity)
 			this.fuel = this.fuelCapacity;
 		else
 			this.fuel = newFuel;
 	}
 	
 	private int fuelMetric(int deltaX, int deltaY)
 	{
 		int squaredCost = (int) (Math.pow((int)deltaX,2)+ Math.pow((int)deltaY, 2));
 		return (int) Math.round(Math.sqrt(squaredCost));
 	}
 }
