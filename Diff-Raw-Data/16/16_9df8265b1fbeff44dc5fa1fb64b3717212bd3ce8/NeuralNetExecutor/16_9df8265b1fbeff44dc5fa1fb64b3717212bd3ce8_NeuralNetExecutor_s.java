 package balle.strategy.bezierNav.neural;
 
 import java.util.ArrayList;
 
 import org.neuroph.core.NeuralNetwork;
 
 import balle.controller.Controller;
 import balle.main.drawable.Drawable;
 import balle.strategy.bezierNav.WheelSpeedExecutor;
 import balle.world.Snapshot;
 
 public class NeuralNetExecutor implements WheelSpeedExecutor {
 
 	protected NeuralNetwork neural;
 
 	protected CalibrateNeural listener;
 
 	protected int gLeft = 0, gRight = 0;
 	protected double cLeft = 0, cRight = 0;
 
 	public NeuralNetExecutor(NeuralNetwork neural) {
 		this.neural = neural;
 	}
 
 	public void addlistener(CalibrateNeural cn) {
 		this.listener = cn;
 	}
 
 	@Override
 	public boolean isFinished(Snapshot snapshot) {
 		return true;
 	}
 
 	@Override
 	public boolean isPossible(Snapshot snapshot) {
 		return true;
 	}
 
 	@Override
 	public void stop(Controller controller) {
 		controller.stop();
 	}
 
 	@Override
 	public ArrayList<Drawable> getDrawables() {
 		return new ArrayList<Drawable>();
 	}
 
 	@Override
 	public void step(Controller controller, Snapshot snapshot) {
 		int oLeft, oRight;
 
 		oLeft = (int) neural.getOutput()[0];
 		oRight = (int) neural.getOutput()[1];
 
 		controller.setWheelSpeeds(oLeft, oRight);
 		listener.record(gLeft, gRight, cLeft, cRight, oLeft, oRight);
 	}
 
 	@Override
 	public void update(int desLeft, int desRight, double actLeft,
 			double actRight) {
 		neural.setInput(desLeft, actLeft, desRight, actRight);
 		neural.calculate();
 		gLeft = desLeft;
 		gRight = desRight;
 	}
 
 }
