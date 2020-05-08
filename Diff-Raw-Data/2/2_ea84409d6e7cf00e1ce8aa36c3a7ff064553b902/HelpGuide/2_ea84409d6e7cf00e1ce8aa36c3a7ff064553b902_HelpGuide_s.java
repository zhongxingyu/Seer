 /*
  * This file is part of SmartStreets.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ru.jcorp.smartstreets.guide;
 
 import javax.swing.*;
 import java.awt.*;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /**
  * <p>$Id$</p>
  *
  * @author Artamonov Yuriy
  */
 public class HelpGuide extends JDialog {
 
     public HelpGuide() {
         this.setLocationByPlatform(true);
         this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
         this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         this.setSize(new Dimension(640, 480));
         this.setResizable(false);
 
        setLayout(new BorderLayout());
         try {
             JEditorPane jEditorPane = new JEditorPane();
 
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(getClass().getResourceAsStream("/docs/guide.htm"), "UTF-8"));
             StringBuilder builder = new StringBuilder();
 
             String line;
             while ((line = reader.readLine()) != null)
                 builder.append(line).append("\n");
 
             jEditorPane.setContentType("text/html; charset=UTF-8");
             jEditorPane.setText(builder.toString());
             JScrollPane scrollPane = new JScrollPane(jEditorPane);
             jEditorPane.setCaretPosition(0);
             jEditorPane.setEditable(false);
             add(scrollPane, BorderLayout.CENTER);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 }
