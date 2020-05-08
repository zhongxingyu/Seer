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
 
 import ru.aifgi.recognizer.view.Presentation;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * @author aifgi
  */
 
 public class Separator implements BasicAction {
    // TODO: Better way?
    private static class SeparatorMenuItem extends JMenuItem {
        private SeparatorMenuItem() {
            super();
            setBorder(null);
            setBorderPainted(false);
            setEnabled(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        }

        @Override
        protected void paintComponent(final Graphics g) {
            final int y = getHeight() / 2;
            g.drawLine(1, y, getWidth() - 2, y);
        }
    }

     public static final Separator INSTANCE = new Separator();
 
     private Separator() {
     }
 
     @Override
     public void perform(final AWTEvent event) {
         throw new UnsupportedOperationException("Couldn't perform separator");
     }
 
     @Override
     public boolean update() {
         return false;
     }
 
     @Override
     public boolean isEnabled() {
         return true;
     }
 
     @Override
     public Presentation getPresentation() {
         return null;
     }
 
     @Override
     public JMenuItem createMenuItem() {
        return new SeparatorMenuItem();
     }
 }
