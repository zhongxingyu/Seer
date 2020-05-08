 package erwin.client;
 
 import java.util.HashMap;
 import java.util.Map;
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import erwin.shared.Complex;
 import erwin.shared.Const;
 import erwin.shared.enums.Operator;
 import erwin.shared.enums.Resolution;
 import erwin.shared.enums.TimeMode;
 import erwin.shared.utils.ColorUtil;
 import erwin.shared.utils.WaveUtil;
 
 public class Erwin implements EntryPoint {
     private static final int WIDTH = 400;
     private static final int HEIGHT = WIDTH;
     private static final int FPS_AIM = 250;
     private static final int MS_PER_S = 1000;
     private static final int TIME_DIVISOR = 100;
 
     private static final String LABEL_STYLE = "lab";
 
     private int currentResolution;
     private int bufferWidth;
     private int bufferHeight;
 
     private final VerticalPanel pan = new VerticalPanel();
     private final Label resolutionLabel = new Label("Resolution");
     private final Map<Resolution, RadioButton> resolutionBoxes = new HashMap<Resolution, RadioButton>();
     private final Label operatorLabel = new Label("Operator");
     private final RadioButton addRb = new RadioButton(Operator.GROUP_NAME, Operator.ADD.toString());
     private final RadioButton mulRb = new RadioButton(Operator.GROUP_NAME, Operator.MULTIPLY.toString());
     private final Label timeModeLabel = new Label("Time Mode");
     private final RadioButton relRb = new RadioButton(TimeMode.GROUP_NAME, TimeMode.RELATIVE.toString());
     private final RadioButton absRb = new RadioButton(TimeMode.GROUP_NAME, TimeMode.ABSOLUTE.toString());
     private final Button startButton = new Button("Start");
     private final Button stopButton = new Button("Stop");
     private final Button resetButton = new Button("Reset");
     private Canvas can;
     private Canvas buffer;
     private Context2d context;
     private Context2d bufferContext;
     private final Label fpsLabel = new Label();
     private final Label statusLabel = new Label();
 
     private long zeroFrame = 0L;
     private long lastFrame = 0L;
     private long frameCount = 0L;
 
     public Erwin() {
         can = Canvas.createIfSupported();
         buffer = Canvas.createIfSupported();
         if (can == null) {
             statusLabel.setText("Your browser doesn't support canvas.");
             return;
         }
         initAllCanvas(Resolution.getDefault());
     }
 
     public final void onModuleLoad() {
         final Timer timer = new Timer() {
             @Override public void run() {
                 final long currentFrame = System.currentTimeMillis();
                 final int diff = Long.valueOf(currentFrame - lastFrame).intValue();
                 final int fps = diff > 0 ? MS_PER_S / diff : FPS_AIM;
                 fpsLabel.setText("FPS: " + fps);
                 frameCount++;
                 if (absRb.getValue()) {
                     drawWaves(currentFrame - zeroFrame);
                 } else {
                     drawWaves(frameCount);
                 }
                 context.drawImage(bufferContext.getCanvas(), 0D, 0D, WIDTH, HEIGHT);
                 lastFrame = currentFrame;
             }
         };
 
         startButton.addClickHandler(new ClickHandler() {
             @Override public void onClick(final ClickEvent event) {
                 zeroFrame = System.currentTimeMillis();
                 frameCount = 0L;
                 timer.scheduleRepeating(MS_PER_S / FPS_AIM);
             }
         });
         stopButton.addClickHandler(new ClickHandler() {
             @Override public void onClick(final ClickEvent event) {
                 timer.cancel();
             }
         });
         resetButton.addClickHandler(new ClickHandler() {
             @Override public void onClick(final ClickEvent event) {
                 reset();
             }
         });
 
         for (final Resolution resolution : Resolution.values()) {
             final RadioButton rb = new RadioButton(Resolution.GROUP_NAME, resolution.toString());
             rb.setValue(resolution.isDefault());
             rb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                 @Override public void onValueChange(final ValueChangeEvent<Boolean> event) {
                     final int res = resolution.getResolution();
                     if (res != 1 || Window.confirm("This may be very slow!")) {
                         initAllCanvas(res);
                     } else {
                         resolutionBoxes.get(Resolution.valueOf(currentResolution)).setValue(true);
                     }
                 }
             });
             resolutionBoxes.put(resolution, rb);
         }
 
         reset();
 
         final HorizontalPanel radioPan = new HorizontalPanel();
         resolutionLabel.setStyleName(LABEL_STYLE);
         radioPan.add(resolutionLabel);
         for (final Resolution resolution : Resolution.values()) {
             radioPan.add(resolutionBoxes.get(resolution));
         }
         final HorizontalPanel operatorPan = new HorizontalPanel();
         operatorLabel.setStyleName(LABEL_STYLE);
         operatorPan.add(operatorLabel);
         operatorPan.add(addRb);
         operatorPan.add(mulRb);
         final HorizontalPanel timeModePan = new HorizontalPanel();
         timeModeLabel.setStyleName(LABEL_STYLE);
         timeModePan.add(timeModeLabel);
         timeModePan.add(relRb);
         timeModePan.add(absRb);
         final HorizontalPanel buttonPan = new HorizontalPanel();
         buttonPan.add(startButton);
         buttonPan.add(stopButton);
         buttonPan.add(resetButton);
 
         pan.add(radioPan);
         pan.add(operatorPan);
         pan.add(timeModePan);
         pan.add(buttonPan);
         pan.add(can);
         pan.add(fpsLabel);
         pan.add(statusLabel);
         RootPanel.get("mainContainer").add(pan);
 
         startButton.click();
     }
 
     private void initAllCanvas(final int resolution) {
         currentResolution = resolution;
         bufferWidth = WIDTH / currentResolution;
         bufferHeight = HEIGHT / currentResolution;
         initCanvas(can, false);
         initCanvas(buffer, true);
         context = can.getContext2d();
         bufferContext = buffer.getContext2d();
     }
 
     private void initCanvas(final Canvas c, final boolean isBuffer) {
         final int width = isBuffer ? bufferWidth : WIDTH;
         final int height = isBuffer ? bufferHeight : HEIGHT;
         c.setWidth(width + "px");
         c.setHeight(height + "px");
         c.setCoordinateSpaceWidth(width);
         c.setCoordinateSpaceHeight(height);
     }
 
     private void clearCanvas() {
         bufferContext.beginPath();
         bufferContext.setFillStyle(Const.BLACK);
         bufferContext.fillRect(0D, 0D, bufferWidth, bufferHeight);
         bufferContext.closePath();
         context.drawImage(bufferContext.getCanvas(), 0D, 0D, WIDTH, HEIGHT);
     }
 
     private void reset() {
         final String wlDefault = String.valueOf(Const.DEFAULT_WAVELENGTH);
         RootPanel.get("wavelength").getElement().setInnerHTML(wlDefault);
         final String sliderDefault = String.valueOf(Const.DEFAULT_WAVELENGTH * 100);
         RootPanel.get("wlSlider").getElement().setPropertyString("value", sliderDefault);
        currentResolution = Resolution.getDefault();
         resolutionBoxes.get(Resolution.valueOf(currentResolution)).setValue(true);
         addRb.setValue(true);
         relRb.setValue(true);
         frameCount = 0L;
         statusLabel.setText("");
         fpsLabel.setText("");
         clearCanvas();
     }
 
     private void drawWaves(final long t) {
         final double wavelength = Double.valueOf(RootPanel.get("wavelength").getElement().getInnerHTML());
         final int x0 = bufferWidth / 2;
         final int y0 = bufferHeight / 2;
         final long tt = absRb.getValue() ? Double.valueOf(Double.valueOf(t) / TIME_DIVISOR).longValue() : t;
         for (int x = 0; x < bufferWidth; x++) {
             for (int y = 0; y < bufferHeight; y++) {
                 final Operator op = mulRb.getValue() ? Operator.MULTIPLY : Operator.ADD;
                 final Complex c = WaveUtil.calculateWave(x - x0, y - y0, tt, wavelength, op);
                 bufferContext.setFillStyle(ColorUtil.getColor(c));
                 bufferContext.fillRect(x, y, 1D, 1D);
             }
         }
         bufferContext.setFillStyle(Const.WHITE);
         bufferContext.fillRect(x0, y0, 1D, 1D);
     }
 }
