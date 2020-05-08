 package App.factory;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import App.model.Planet;
 import App.model.PoliticalSystem;
 import App.model.ResourceType;
 import App.model.TechnologyLevel;
 import App.service.Randomizer;
 
 public class PlanetFactory {
 
     private static Map<String, Planet> planets;
 
 	public static Planet getPlanet(String name)
 	{
         return planets.get(name);
 	}
     public static Map<String, Planet> getPlanets(){
         return planets;
     }
 
     /**
      * Fills the factory with planets.
      *
      * @param names List of the Planet's possible names
      * @param maxXDim Length of the game's x-axis
      * @param maxYDim Length of the game's y-axis
      * @param numPlanets how many planets for this game
      */
     public static void createPlanets(List<String> names, int maxXDim, int maxYDim, int numPlanets){

         planets = new HashMap<String, Planet>();
         List<String> namesHolder = new ArrayList<String>(names);
         List<Integer[]> coordinateList=Randomizer.generateDimensions(numPlanets, maxXDim, maxYDim);
 
         int index = 0;
         while (index < numPlanets && !namesHolder.isEmpty()){
            Planet planet = new Planet();
             int planetNameIndex = Randomizer.nextInt(namesHolder.size());
             String planetName=namesHolder.get(planetNameIndex);
             namesHolder.remove(planetNameIndex);
             planet.setName(planetName);
             planet.setX(coordinateList.get(index)[0]);
             planet.setY(coordinateList.get(index)[1]);
             planet.setTechnologyLevel((TechnologyLevel) Randomizer.randEnum((TechnologyLevel.class)));
             planet.setResourceType((ResourceType) Randomizer.randEnum((ResourceType.class)));
             planet.setPoliticalSystem((PoliticalSystem) Randomizer.randEnum((PoliticalSystem.class)));
             planets.put(planet.getName(), planet);
             index++;
         }
 
     }
 
 }
