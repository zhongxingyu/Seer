 /**
  * 
  */
 package siver.context;
 
 import java.awt.geom.Point2D;
 
 import repast.simphony.context.Context;
 import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
 import repast.simphony.dataLoader.ContextBuilder;
 import repast.simphony.engine.environment.RunEnvironment;
 import repast.simphony.engine.schedule.ISchedule;
 import repast.simphony.engine.schedule.ScheduleParameters;
 import repast.simphony.space.continuous.ContinuousSpace;
 import repast.simphony.space.continuous.SimpleCartesianAdder;
 import siver.agents.BoatHouse;
 import siver.river.River;
 import siver.river.RiverFactory;
 
 /**
  * @author hja11
  *
  */
 public class SiverContextCreator implements ContextBuilder<Object> {
 	
 	/* (non-Javadoc)
 	 * @see repast.simphony.dataLoader.ContextBuilder#build(repast.simphony.context.Context)
 	 */
 	@Override
 	public Context build(Context<Object> context) {
 		context.setId("siver");
 		
 		int xdim = 1500;   // The x dimension of the physical space
 		int ydim = 300;   // The y dimension of the physical space
 
 		// Create a new 2D continuous space to model the physical space on which the sheep
 		// and wolves will move.  The inputs to the Space Factory include the space name, 
 		// the context in which to place the space, border specification,
 		// random adder for populating the grid with agents,
 		// and the dimensions of the grid.
 		ContinuousSpace<Object> space = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
 		.createContinuousSpace("Continuous Space", context, new SimpleCartesianAdder<Object>(),
 				new repast.simphony.space.continuous.StrictBorders(), xdim, ydim);
 		
 		River river = RiverFactory.Test(context, space);
 		
 		BoatHouse boatHouse = new BoatHouse(river);
 		context.add(boatHouse);
 		space.moveTo(boatHouse, 0, 30);
 		
 		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
 		//Specify that the action should start at tick 1 and execute every other tick
 		ScheduleParameters params = ScheduleParameters.createOneTime(1);
 
 		//Schedule the boathouse to launch a boat on the first tick only for now
		schedule.schedule(params, boatHouse, "launchBoat");
 		
 		return context;
 	}
 }
