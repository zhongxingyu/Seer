 package maro.example.soccer;
 
 import maro.core.IntelligentEnvironment;
 import jason.asSyntax.ASSyntax;
 
 public class Environment extends IntelligentEnvironment
 {
 	@Override
 	protected void stepStarted(int step) {
 		//clearAllPercepts();
 		getLogger().info("Started step " + step);
 		addPercept(ASSyntax.createLiteral("step",
 					ASSyntax.createNumber(step)));
 		super.stepStarted(step);
 	}
 
     @Override
     protected void stepFinished(int step, long elapsedTime, boolean byTimeout) {
         super.stepFinished(step, elapsedTime, byTimeout);
         clearAllPercepts();
     }
 }
