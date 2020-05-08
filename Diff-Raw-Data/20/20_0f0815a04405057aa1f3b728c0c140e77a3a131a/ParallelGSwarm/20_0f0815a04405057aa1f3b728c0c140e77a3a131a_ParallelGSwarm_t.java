 package gswarm;
 
 import gui.Window;
 import pso.*;
 import utils.FileUtils;
 
 public class ParallelGSwarm implements Runnable {
 
 	private GSwarm[] swarms = new GSwarm[3];
 	
 	public ParallelGSwarm(){
 		init();
 	}
 	
 	private void init(){
 		swarms[0] = new GSwarm("ahead");
 		swarms[1] = new GSwarm("gun");
 		swarms[2] = new GSwarm("turn");
 	}
 	
 	public void run(){
 		prepareTemplates();
 		
 		GSwarm swarm;
 		
 		for (int i = 0; i < 3; i++){
 			swarms[i].generateInitialSwarm();
 		}
 		
 		for (int i = 0; i < Setup.ITERATIONS; i++){			
 			for (int j = 0; j < 3; j++){
 				swarm = swarms[j];
 				
 				swarm.iteration = i;
 				swarm.generateRobots();
 				swarm.testRobots();
 				swarm.calculateFitness();
 				swarm.updateGlobalFitness();
 			}
 			
 			updateTemplates();
 			
 			for (int j = 0; j < 3; j++){
 				swarm = swarms[j];
 				
 				swarm.updateVelocities(i);
 				swarm.logIteration();
 				swarm.updateLocations();
 			}
 		}
 	}
 	
 	private void prepareTemplates(){
 		String botTemplate = FileUtils.convertFileToString("templates/bot_module.tmpl");
 		
 		String aheadTemplate = botTemplate.replace("#ahead#", "#gswarm-simple.grm").replace("#gun#", "0").replace("#turn#", "0");
 		String gunTemplate = botTemplate.replace("#ahead#", "0").replace("#gun#", "#gswarm-simple.grm").replace("#turn#", "0");
 		String turnTemplate = botTemplate.replace("#ahead#", "0").replace("#gun#", "0").replace("#turn#", "#gswarm-simple.grm");
 		
 		FileUtils.saveStringToFile("templates/ahead_bot.tmpl", aheadTemplate);
 		FileUtils.saveStringToFile("templates/gun_bot.tmpl", gunTemplate);
 		FileUtils.saveStringToFile("templates/turn_bot.tmpl", turnTemplate);
 		
 		swarms[0].loadTemplate("templates/ahead_bot.tmpl");
 		swarms[1].loadTemplate("templates/gun_bot.tmpl");
 		swarms[2].loadTemplate("templates/turn_bot.tmpl");
 	}
 	
 	private void updateTemplates(){
 		Particle particle;
 		
 		String botTemplate = FileUtils.convertFileToString("templates/bot_module.tmpl");
 		
 		particle = swarms[0].getSwarm().getGlobalBestParticle();
		String aheadProgram = swarms[0].robotGen.translateGlobalBestParticleToProgram(particle);
		
		System.out.println("aheadProgram");
		System.out.println(aheadProgram);
 		
 		particle = swarms[1].getSwarm().getGlobalBestParticle();
		String gunProgram = swarms[1].robotGen.translateGlobalBestParticleToProgram(particle);
		
		System.out.println("gunProgram");
		System.out.println(gunProgram);
 		
 		particle = swarms[2].getSwarm().getGlobalBestParticle();
		String turnProgram = swarms[2].robotGen.translateGlobalBestParticleToProgram(particle);
		
		System.out.println("turnProgram");
		System.out.println(turnProgram);
 		
 		String aheadTemplate = botTemplate.replace("#ahead#", "#gswarm-simple.grm").replace("#gun#", gunProgram).replace("#turn#", turnProgram);
 		String gunTemplate = botTemplate.replace("#ahead#", aheadProgram).replace("#gun#", "#gswarm-simple.grm").replace("#turn#", turnProgram);
 		String turnTemplate = botTemplate.replace("#ahead#", aheadProgram).replace("#gun#", gunProgram).replace("#turn#", "#gswarm-simple.grm");
 		
 		FileUtils.saveStringToFile("templates/ahead_bot.tmpl", aheadTemplate);
 		FileUtils.saveStringToFile("templates/gun_bot.tmpl", gunTemplate);
 		FileUtils.saveStringToFile("templates/turn_bot.tmpl", turnTemplate);
 		
 		swarms[0].loadTemplate("templates/ahead_bot.tmpl");
 		swarms[1].loadTemplate("templates/gun_bot.tmpl");
 		swarms[2].loadTemplate("templates/turn_bot.tmpl");
 	}
 	
 }
