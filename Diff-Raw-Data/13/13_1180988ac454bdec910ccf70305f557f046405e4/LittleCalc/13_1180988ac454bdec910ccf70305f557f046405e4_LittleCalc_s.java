 /*
  * This file is part of LittleHelper.
  * Copyright (C) 2009 Elliott Hughes <enh@jessies.org>.
  * 
  * LittleHelper is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import e.gui.*;
 import e.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.text.*;
 import org.jessies.calc.*;
 
 /**
  * A Swing application similar to Mathdroid.
  * 
  * Under GNOME, to run little-calc when F4 (the Dashboard key on Apple keyboards) is pressed:
  * 
  * % gconftool-2 -t string -s /apps/metacity/global_keybindings/run_command_4 F4
  * % gconftool-2 -t string -s /apps/metacity/keybinding_commands/command_4 `which little-calc`
  * 
  * You can also use gconf-editor(1) to edit these from the GUI.
  */
 public class LittleCalc extends JFrame {
     private final Calculator calculator = new Calculator();
     
     private JTextField textField;
     private JTextPane transcript;
     
     private Style inputStyle;
     
     public LittleCalc() {
         super("LittleCalc");
         
         setContentPane(makeUi());
         pack();
         
         setLocationRelativeTo(null);
         
         // FIXME: this is a bit harsh (but effective: MainFrame doesn't seem to work right, and even when it does it's slow).
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         JFrameUtilities.closeOnEsc(this);
         
         GuiUtilities.finishGnomeStartup();
         
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 textField.requestFocus();
             }
         });
     }
     
     private JPanel makeUi() {
         final JPanel ui = new JPanel(new BorderLayout());
         ui.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
         
         this.textField = new JTextField(40);
         textField.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 calculate();
             }
         });
         ui.add(textField, BorderLayout.SOUTH);
         
         this.transcript = new JTextPane();
         transcript.setPreferredSize(new Dimension(400, 400));
         ui.add(new JScrollPane(transcript), BorderLayout.CENTER);
         
         this.inputStyle = transcript.getStyledDocument().addStyle("input", null);
         StyleConstants.setBold(inputStyle, true);
         StyleConstants.setForeground(inputStyle, new Color(0xffcdaa7d));
         
         // There's no point being able to "scroll" a single-line text field, but scrolling a multi-row list is useful.
         ComponentUtilities.divertPageScrollingFromTo(textField, transcript);
         
         return ui;
     }
     
     private void calculate() {
         final String q = textField.getText().trim();
         if (q.length() == 0) {
             return;
         }
         textField.selectAll();
         
         final String a = computeAnswer(q);
         
         if (transcript.getDocument().getLength() > 0) {
             appendToTranscript("\n", null);
         }
         appendToTranscript(q, inputStyle);
         appendToTranscript("\n", null);
         appendToTranscript(" = " + a, null);
     }
     
     private void appendToTranscript(String string, AttributeSet attributes) {
         try {
             final StyledDocument document = transcript.getStyledDocument();
             document.insertString(document.getLength(), string, attributes);
         } catch (BadLocationException ex) {
             // Can't happen. Stupid API.
         }
     }
     
     private String computeAnswer(String query) {
         try {
             String answer = null;
             if (answer == null) {
                 // Convert units.
                 answer = UnitsConverter.convert(query);
             }
             if (answer == null) {
                 // Evaluate mathematical expressions.
                 answer = calculator.evaluate(query);
             }
             if (answer == null) {
                 answer = "Dunno, mate.";
             }
             return answer;
         } catch (CalculatorError ex) {
             return "Error: " + ex.getMessage();
         } catch (Exception ex) {
             ex.printStackTrace();
             return "What do you mean?";
         }
     }
     
     public static void main(String[] args) {
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 GuiUtilities.initLookAndFeel();
                 new LittleCalc().setVisible(true);
             }
         });
     }
 }
