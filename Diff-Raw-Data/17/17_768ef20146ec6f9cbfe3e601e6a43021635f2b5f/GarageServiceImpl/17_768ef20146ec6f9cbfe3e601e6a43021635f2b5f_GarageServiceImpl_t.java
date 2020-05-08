 package ie.tech.talk.service;
 
 import ie.tech.talk.domain.Car;
 import ie.tech.talk.domain.Engine;
 import ie.tech.talk.domain.SparkPlug;
 import ie.tech.talk.utils.GarageUtils;
 
 import java.util.List;
 
 public class GarageServiceImpl
 {
 	/**
 	 * Tuning the car check
 	 * 
 	 * @param car
 	 */
 	public void tuneEngine(Car car)
 	{
 		Engine engine = car.getEngine();
 		engine.stopEngine();
 		List<SparkPlug> sparkPlugs = engine.inspectSparkPlugs();

		if (!GarageUtils.checkSparkPlugs(sparkPlugs))
 		{
			List<SparkPlug> newSparkPlugs = GarageUtils.getNewSparkPlugs();
 		}
 
 	}
 }
