 package com.dperry.space;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.dperry.space.genesis.PlanetFactory;
 import com.dperry.space.model.space.Planet;
 import com.dperry.space.model.space.SolarSystem;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 @RequestMapping("/overview")
 public class OverviewController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(OverviewController.class);
 	
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		
 		return "overview";
 	}
 	
 	@RequestMapping("/planettest")
 	public String viewPlanetTest( Model model ) {
 		
 		PlanetFactory planetFactory = new PlanetFactory();
 		planetFactory.setPlanetSizeMin( 10 );
 		planetFactory.setPlanetSizeMax( 50 );
 		planetFactory.setGridHeight( 400 );
 		planetFactory.setGridWidth( 600 );
 		planetFactory.setPlanetTypeGasMin( 35 );
 		planetFactory.setPlanetTypeWaterMin( 20 );
 		planetFactory.setPlanetOreMin( 50 );
 		planetFactory.setPlanetOreMax( 500 );
 		
 		Planet planet = planetFactory.createPlanet();
 		
		planetFactory.generateTerrain( planet );
		
 		model.addAttribute( "planet", planet );
 		
 		return "planet";
 	}
 	
 	@RequestMapping("solarsystemtest")
 	public String viewSolarSystem( Model model ) {
 		
 		PlanetFactory planetFactory = new PlanetFactory();
 		planetFactory.setPlanetSizeMin( 10 );
 		planetFactory.setPlanetSizeMax( 50 );
 		planetFactory.setGridHeight( 400 );
 		planetFactory.setGridWidth( 600 );
 		planetFactory.setPlanetTypeGasMin( 35 );
 		planetFactory.setPlanetTypeWaterMin( 20 );
 		planetFactory.setPlanetOreMin( 50 );
 		planetFactory.setPlanetOreMax( 500 );
 		
 		SolarSystem solarSystem = new SolarSystem();
 		
 		List<Planet> planets = new ArrayList<Planet>();
 		int loop = new Random().nextInt( 10 );
 		for( int i = 0; i < loop; i++ ) {
 			planets.add( planetFactory.createPlanet() );
 		}
 		
 		solarSystem.setPlanets( planets );
 		
 		model.addAttribute( "solarSystem", solarSystem );
 		
 		return "solarsystem";
 	}
 	
 	@RequestMapping("galaxytest")
 	public String viewGalaxy( Model model ) {
 		
 		return "galaxy";
 	}
 	
 }
