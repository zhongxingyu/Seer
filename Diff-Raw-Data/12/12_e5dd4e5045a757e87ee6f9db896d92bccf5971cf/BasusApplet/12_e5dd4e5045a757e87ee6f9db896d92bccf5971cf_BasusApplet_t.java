 package no.shhsoft.basus.ui;
 
 import java.applet.Applet;
 import java.awt.GridLayout;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import no.shhsoft.basus.language.eval.runtime.BasusRunner;
 import no.shhsoft.swing.SwingUtils;
 import no.shhsoft.utils.IoUtils;
 
 /**
  * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
  */
 @SuppressWarnings("synthetic-access")
 public final class BasusApplet
 extends Applet {
 
     private static final long serialVersionUID = 1L;
     private OutputCanvas outputCanvas;
     private BasusRunner runner;
     private String program;
     private URL programUrl;
 
     private URL getUrlParameter(final String name) {
         final String param = getParameter(name);
         if (param == null) {
             throw new RuntimeException("missing `" + name + "' parameter");
         }
         try {
             if (param.indexOf("://") >= 0) {
                 return new URL(param);
             }
             return new URL(getDocumentBase(), param);
         } catch (final MalformedURLException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void createGui() {
         if (outputCanvas != null) {
             return;
         }
         outputCanvas = new OutputCanvas();
         setLayout(new GridLayout(1, 0));
         add(outputCanvas);
         runner = new BasusRunner();
         programUrl = getUrlParameter("program");
         try {
             program = new String(IoUtils.read(programUrl.openStream()), "UTF-8");
         } catch (final IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void init() {
         super.init();
    }

    @Override
    public void start() {
        super.start();
         SwingUtils.invokeAndWait(new Runnable() {
             @Override
             public void run() {
                 createGui();
             }
         });
        outputCanvas.waitUntilInitiated();
         runner.runProgram(program, outputCanvas, outputCanvas, null, programUrl);
         outputCanvas.requestFocusInWindow();
     }
 
     @Override
     public void stop() {
         runner.stopProgram();
         super.stop();
     }
 
 }
