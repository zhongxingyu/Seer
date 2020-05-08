 package eu32k.vJoy.curveEditor;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.audio.AudioDevice;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 
 import eu32k.vJoy.VJoyMain;
 import eu32k.vJoy.curveEditor.audio.AudioTrack;
 import eu32k.vJoy.curveEditor.misc.KeyPressEvent;
 import eu32k.vJoy.curveEditor.misc.Range;
 import eu32k.vJoy.curveEditor.spectrum.PixmapWidget;
 import eu32k.vJoy.curveEditor.spectrum.SpectrumPixmap;
 import eu32k.vJoy.curveEditor.spectrum.WaveformPixmap;
 
 public class CurveEditorStage extends Stage {
 
    private AudioDevice device;
    private AudioTrack track;
 
    private WaveformPixmap waveform;
    private SpectrumPixmap spectrum;
    // private SpectrumPixmap pixmapChannel2;
 
    private MusicPlayer player;
 
    private Slider zoomSliderX;
    private Slider zoomSliderY;
 
    private float zoomX = 0.1f;
    private float zoomY = 0.1f;
    private Rectangle zoomRange = new Rectangle(0.003f, 0.03f, 1.0f, 1.0f);
 
    public CurveEditorStage(String filePath) {
 
       track = new AudioTrack(Gdx.files.absolute(filePath));
 
       device = Gdx.audio.newAudioDevice(track.getRate(), track.isMono());
       player = new MusicPlayer(device, track);
      waveform = new WaveformPixmap(1920 * 2, 1080, track);
      spectrum = new SpectrumPixmap(1920 * 2, 1080, track);
 
       Table table = new Table();
       table.setFillParent(true);
 
       zoomSliderX = new Slider(zoomRange.x, zoomRange.width, 0.0001f, false, VJoyMain.SKIN);
       zoomSliderX.setValue(zoomX);
       zoomSliderX.addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
             setZoomX(zoomSliderX.getValue());
          }
       });
 
       zoomSliderY = new Slider(zoomRange.y, zoomRange.height, 0.0001f, false, VJoyMain.SKIN);
       zoomSliderY.setValue(zoomY);
       zoomSliderY.addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
             setZoomY(zoomSliderY.getValue());
          }
       });
 
       table.add(player).colspan(2).fillX().expandX().pad(5);
       table.row();
       table.add(new PixmapWidget(waveform)).colspan(2).fill().height(200).pad(5);
       table.row();
       table.add(new PixmapWidget(spectrum)).colspan(2).fill().expand().pad(5);
       // table.row();
       // table.add(new
       // PixmapWidget(pixmapChannel2)).colspan(2).fill().expand().pad(5);
       table.row();
       table.add(new Label("Zoom X:", VJoyMain.SKIN)).left().pad(5);
       table.add(zoomSliderX).fillX().expandX().pad(5);
       table.row();
       table.add(new Label("Zoom Y:", VJoyMain.SKIN)).left().pad(5);
       table.add(zoomSliderY).fillX().expandX().pad(5);
 
       addActor(table);
 
       Range range = new Range(0.0f, zoomX, 0.0f, zoomY);
 
       waveform.updatePixmap(range, 0);
       spectrum.updatePixmap(range, 0);
       // pixmapChannel2.updatePixmap(new Rectangle(0.0f, 0.0f, zoomX, zoomY));
 
       new KeyPressEvent(Input.Keys.SPACE) {
          @Override
          public void onPress() {
             if (player.isPlaying()) {
                player.pause();
             } else {
                player.play();
             }
          }
       };
    }
 
    @Override
    public boolean scrolled(int x) {
       setZoomX(zoomX + x * 0.01f);
       zoomSliderX.setValue(zoomX);
       return true;
    }
 
    private void setZoomX(float zoomX) {
       this.zoomX = MathUtils.clamp(zoomX, zoomRange.x, zoomRange.width);
    }
 
    private void setZoomY(float zoomY) {
       this.zoomY = MathUtils.clamp(zoomY, zoomRange.y, zoomRange.height);
    }
 
    boolean updated = false;
    private float lastPosition = -1;
    private float lastZoomX = -1;
    private float lastZoomY = -1;
 
    @Override
    public void draw() {
       KeyPressEvent.update();
 
       float np = (float) player.getNormalizedPosition();
       if (lastPosition != np || lastZoomX != zoomX || lastZoomY != zoomY) {
          lastPosition = np;
          lastZoomX = zoomX;
          lastZoomY = zoomY;
 
          float low = np - zoomX / 2.0f;
          float high = np + zoomX / 2.0f;
          if (low < 0) {
             low = 0;
             high = zoomX;
          } else if (high > 1) {
             high = 1;
             low = 1 - zoomX;
          }
 
          Range range = new Range(low, high, 0, zoomY);
 
          waveform.updatePixmap(range, np);
          spectrum.updatePixmap(range, np);
       }
 
       player.update();
       super.draw();
    }
 
    @Override
    public void dispose() {
       player.shutdown();
       device.dispose();
    }
 }
