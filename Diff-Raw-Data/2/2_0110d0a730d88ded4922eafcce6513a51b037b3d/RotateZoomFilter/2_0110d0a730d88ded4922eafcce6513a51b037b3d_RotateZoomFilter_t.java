 package eu32k.vJoy.common.workset.atomic.image;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL20;
 
 import eu32k.vJoy.common.AdvancedShader;
 import eu32k.vJoy.common.Tools;
 import eu32k.vJoy.common.workset.ImagePort;
 import eu32k.vJoy.common.workset.NumberPort;
 import eu32k.vJoy.common.workset.atomic.ImageInstance;
 import eu32k.vJoy.common.workset.atomic.ImageType;
 
 public class RotateZoomFilter extends ImageType {
    private static final long serialVersionUID = 4203549682307964834L;
 
    private ImagePort image = addPort(new ImagePort("Input"));
    private NumberPort rot = addPort(new NumberPort("Rotation"));
    private NumberPort zoom = addPort(new NumberPort("Zoom"));
 
    public RotateZoomFilter() {
      super("Rotate / Zoom Filter");
    }
 
    @Override
    public ImageInstance instanciate(float x, float y) {
       return new ImageInstance(this, x, y) {
          private static final long serialVersionUID = 2339019018930598026L;
 
          private AdvancedShader shader = Tools.getShader("rotateZoom");
 
          @Override
          public void renderInternally() {
             renderPort(image);
 
             Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
             getPortValue(image).bind();
 
             shader.begin();
 
             shader.setUniformi("uTexture", 0);
             shader.setUniformf("uRot", getPortValue(rot));
             shader.setUniformf("uZoom", getPortValue(zoom));
 
             shader.renderToQuad(frameBuffer);
          }
       };
    }
 }
