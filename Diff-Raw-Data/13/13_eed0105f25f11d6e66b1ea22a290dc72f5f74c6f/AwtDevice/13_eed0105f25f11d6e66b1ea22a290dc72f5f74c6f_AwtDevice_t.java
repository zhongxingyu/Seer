 package net.baremodels.awt;
 
 import net.baremodels.device.GenericDevice;
 import net.baremodels.intent.Intent;
 import net.baremodels.model.Model;
 import net.baremodels.runner.SimpleComponentListener;
 import net.baremodels.runner.SimpleComponentTranslator;
 import net.baremodels.ui.UIComponent;
 
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 
 /**
  * A GenericDevice that uses AWT for widgets.
  */
 final class AwtDevice
     implements GenericDevice
 {
 
     private final Frame frame;
     private final SimpleComponentListener listener;
     private final SimpleComponentTranslator translator;
 
     private AwtDevice(Frame frame) {
         this(frame,new SimpleComponentTranslator(new AwtWidgetSupplier()), new SimpleComponentListener());
     }
 
     AwtDevice(Frame frame, SimpleComponentTranslator translator, SimpleComponentListener listener) {
         this.frame = frame;
         this.translator = translator;
         this.listener = listener;
     }
 
     public static AwtDevice newInstance() {
         FutureTask<Frame> task = new FutureTask(new FrameMaker());
         try {
             EventQueue.invokeAndWait(task);
             return new AwtDevice(task.get());
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         } catch (ExecutionException | InvocationTargetException e) {
             throw new RuntimeException(e.getCause());
         }
     }
 
     @Override
     public Model display(UIComponent ui) {
         frame.removeAll();
         frame.add((Component) translator.translate(ui,listener));
        frame.setSize(1600,980);
        frame.validate();
         Model selected = listener.waitForSelectionChange();
         return selected;
     }
 
     private static void exitOnWindowClose(Frame frame) {
         frame.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 System.exit(0);
             }
         });
     }
 
     @Override
     public void onIntent(Intent intent) {
 
     }
 
     private static class FrameMaker implements Callable<Frame>{
         @Override
         public Frame call() throws Exception {
             Frame frame = new Frame();
             frame.setVisible(true);
             frame.setSize(500,500);
             exitOnWindowClose(frame);
             return frame;
         }
     }
 
 }
