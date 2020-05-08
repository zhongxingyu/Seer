 package ru.aifgi.recognizer.view.actions;
 /*
  * Copyright 2012 Alexey Ivanov
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import ru.aifgi.recognizer.model.thread_factories.MyThreadFactory;
 import ru.aifgi.recognizer.view.MainWindow;
 import ru.aifgi.recognizer.view.ViewUtil;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  * @author aifgi
  */
 
 public class OpenImageAction extends MyAction {
     private final ExecutorService myService = Executors.newSingleThreadExecutor(new MyThreadFactory("OpenImage"));
 
     public OpenImageAction() {
         super("Open image");
     }
 
     @Override
     protected void performImpl(final AWTEvent event) {
         final JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
         fileChooser.setDialogTitle("Open image");
         final MainWindow mainWindow = ViewUtil.getMainWindow();
         final int returnVal = fileChooser.showOpenDialog(mainWindow);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             myService.execute(new Runnable() {
                 @Override
                 public void run() {
                     final File file = fileChooser.getSelectedFile();
                     try {
                         final BufferedImage bufferedImage = ImageIO.read(file);
                         mainWindow.setImage(bufferedImage);
                     }
                     catch (IOException e) {
                        // TODO:
                        e.printStackTrace();
                         throw new RuntimeException(e);
                     }
                 }
             });
         }
     }
 }
