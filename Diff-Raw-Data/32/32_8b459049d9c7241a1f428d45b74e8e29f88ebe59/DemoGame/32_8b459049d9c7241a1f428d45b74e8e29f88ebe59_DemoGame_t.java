 package demo.OGL2Samples;
 
 import TEEngine.Core.TEEngine;
 import TEEngine.Core.TEGame;
 import TEEngine.Core.TEGameObject;
 import TEEngine.Util.TEColor4;
 import TEEngine.Util.TEUtilPoint;
 import TEEngine.Util.TEUtilSize;
 import android.util.Log;
 
 public class DemoGame extends TEGame {
 
 	public void start() {
		//imageDemo();
 		//polyDemo();
		renderToTextureDemo();
 		Log.v("DemoGame", "start");
 	}
 	
 	public void imageDemo() {
 		TEEngine engine = TEEngine.sharedEngine();
 		TEGameObject go;
 		TEUtilSize size;
 		RenderImage ri;
 	    go = new TEGameObject();
 	    size = TEUtilSize.make(160, 160);
 	    ri = new RenderImage(R.raw.diamond_ace, TEUtilPoint.make(0, 0), size);
 	    ri.setGrayscale();
 	    /*
 	    float kernel[] = new float[9];
 	    kernel[0] = 1.0f/9.0f;
 	    kernel[1] = 1.0f/9.0f;
 	    kernel[2] = 1.0f/9.0f;
 	    kernel[3] = 1.0f/9.0f;
 	    kernel[4] = 1.0f/9.0f;
 	    kernel[5] = 1.0f/9.0f;
 	    kernel[6] = 1.0f/9.0f;
 	    kernel[7] = 1.0f/9.0f;
 	    kernel[8] = 1.0f/9.0f;
 	    ri.setKernel(kernel);
 		*/
 	    
 	    go.position.x = -80.0f;
 	    go.position.y = 0.0f;
 
 	    go.addComponent(ri);
 	    engine.addGameObject(go);		
 
 	    go = new TEGameObject();
 	    size = TEUtilSize.make(160, 160);
 	    ri = new RenderImage(R.raw.spade_ace, TEUtilPoint.make(0, 0), size);
 
 	    go.position.x = 80.0f;
 	    go.position.y = 0.0f;
 
 	    go.addComponent(ri);
 	    engine.addGameObject(go);		
 	}
 	
 	public void kernelDemo() {
 		TEEngine engine = TEEngine.sharedEngine();
 		TEGameObject go;
 		TEUtilSize size;
 	    go = new TEGameObject();
 	    size = TEUtilSize.make(160, 160);
 	    RenderImage ri = new RenderImage(R.raw.club_ace, TEUtilPoint.make(0, 0), size);
 
 	    go.position.x = -80.0f;
 	    go.position.y = 0.0f;
 
 	    go.addComponent(ri);
 	    engine.addGameObject(go);		
 	}
 	
 	public void polyDemo() {
 		TEEngine engine = TEEngine.sharedEngine();
 		TEGameObject go;
 		TEColor4 color;
 		TEUtilSize size;
 		RenderPolygon rp;
 		int radius;
 	    go = new TEGameObject();
 	    color = TEColor4.make(0.0f, 0.0f, 0.0f, 1.0f);
 	    size = TEUtilSize.make(164, 164);
 	    radius = 5;
 	    
 	    rp = RenderPolygonFactory.roundedRect(size, color, (float)radius, radius);
 	    //rp->setRenderTarget(rtt->getTargetFrameBuffer());
 	    go.addComponent(rp);
 	    
 	    size.width -= 4;
 	    size.height -= 4;
 	    color.r = 1.0f;
 	    color.g = 0.0f;
 	    color.b = 0.0f;
 	    color.a = 1.0f;
 	    rp = RenderPolygonFactory.roundedRect(size, color, radius, radius);
 	    //rp->setRenderTarget(rtt->getTargetFrameBuffer());
 	    go.addComponent(rp);
 	    
 	    go.position.x = 82.0f;
 	    go.position.y = 0.0f;
 	    
 	    engine.addGameObject(go);
 	}
 	
 	public void renderToTextureDemo() {
 		TEEngine engine = TEEngine.sharedEngine();
 	    TEGameObject go;
 	    TEColor4 color;
 	    RenderPolygon rp;
 	    TEUtilSize size;
 	    float radius;
 	    RenderToTexture rtt;
 	    RenderToTexture rtt2;
 
 	    go = new TEGameObject();
 	    go.position.x = 0.0f;
 	    go.position.y = 0.0f;
 	    
 	    rtt  = new RenderToTexture(256);
	    /*
 	    float kernel[] = new float[9];
 	    kernel[0] = 1.0f/9.0f;
 	    kernel[1] = 1.0f/9.0f;
 	    kernel[2] = 1.0f/9.0f;
 	    kernel[3] = 1.0f/9.0f;
 	    kernel[4] = 1.0f/9.0f;
 	    kernel[5] = 1.0f/9.0f;
 	    kernel[6] = 1.0f/9.0f;
 	    kernel[7] = 1.0f/9.0f;
 	    kernel[8] = 1.0f/9.0f;
 	    rtt.setKernel(kernel);
		*/
	    
 	    go.addComponent(rtt);
 	    engine.addGameObject(go);
 	    
 	    go = new TEGameObject();
	    color = TEColor4.make(0.0f, 0.0f, 0.0f, 1.0f);
	    size = TEUtilSize.make(164, 164);
	    radius = 5;
 	    
 	    rp = RenderPolygonFactory.roundedRect(size, color, radius, (int)radius);
 	    rp.setRenderTarget(rtt.getTargetFrameBuffer());
 	    go.addComponent(rp);
 	    
	    size.width -= 4;
	    size.height -= 4;
	    color.r = 1.0f;
 	    color.g = 0.0f;
 	    color.b = 0.0f;
 	    color.a = 1.0f;
 	    rp = RenderPolygonFactory.roundedRect(size, color, radius, (int)radius);
 	    rp.setRenderTarget(rtt.getTargetFrameBuffer());
 	    go.addComponent(rp);
 	    
	    go.position.x = 82.0f;
 	    go.position.y = 0.0f;
 	    
 	    engine.addGameObject(go);
	    /*
 	    
 	    go = new TEGameObject();
 	    go.position.x = 0.0f;
 	    go.position.y = 0.0f;
 
 	    rtt2  = new RenderToTexture(256);
 	    color.r = 0;
 	    color.g = 0;
 	    color.b = 0;
 	    rtt2.setTransparentColor(color);
 
 	    go.addComponent(rtt2);
 	    engine.addGameObject(go);
 
 	    rtt.setRenderTarget(rtt2.getTargetFrameBuffer());
	   */
 	}
 }
