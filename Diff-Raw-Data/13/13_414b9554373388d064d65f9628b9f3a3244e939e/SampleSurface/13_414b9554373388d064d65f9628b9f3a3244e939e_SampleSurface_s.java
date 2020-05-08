 package samplesurface;
 
 
 public class SampleSurface extends DefaultSurface {
 
 	public SampleSurface() {
 		super(true, false);
 	}
 
 	@Override
 	protected void draw() {
 		mGraphics.clear(0);
 		mGraphics2D.drawRect(0,0,1,1);
 	}
 
 }
