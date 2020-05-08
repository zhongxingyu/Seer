 package maro.example.soccer;
 
 import maro.core.IntelligentEnvironment;
import maro.core.ActionLoader;
 import jason.asSyntax.ASSyntax;
 
 public class Environment extends IntelligentEnvironment
 {
	public Environment () {
		super();
		ActionLoader.getInstance().loadAllActions("maro.example.soccer.ea");
	}

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
