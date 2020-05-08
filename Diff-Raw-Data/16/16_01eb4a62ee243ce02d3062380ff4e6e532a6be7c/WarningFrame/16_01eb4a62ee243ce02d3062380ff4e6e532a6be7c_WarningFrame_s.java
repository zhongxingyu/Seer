 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package irigui;
 
import java.applet.AudioClip;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.net.URL;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.*;
 
 /**
  *
  * @author user
  */
 public class WarningFrame implements Runnable, ActionListener {
 
     JFrame WarningFrame;
     JPanel WarningPanel;
     JButton OkButton;
     Clip clip;
 
     public WarningFrame(String text, float value) {
         WarningFrame = new JFrame();
         WarningPanel = new JPanel(new BorderLayout());
         JLabel WarningText = new JLabel();
         OkButton = new JButton("Take Action!");
         OkButton.addActionListener(this);
 
 
         WarningText.setText(text);

        WarningPanel.add(WarningText, BorderLayout.CENTER);
         WarningPanel.add(OkButton, BorderLayout.PAGE_END);
         WarningFrame.add(WarningPanel);
         WarningFrame.setSize(500, 300);
         WarningFrame.setLocationRelativeTo(null);
         Thread flasher = new Thread(this);
         flasher.start();
         WarningFrame.setVisible(true);
     }
 
     @Override
     public void run() {
         PlaySound();
         while (true) {
             WarningPanel.setBackground(Color.red);
             try {
                 Thread.sleep(500);
             } catch (Exception e) {
                 System.out.println("SLEEP FAIL!");
             }
             WarningPanel.setBackground(Color.YELLOW);
             try {
                 Thread.sleep(500);
             } catch (Exception e) {
                 System.out.println("SLEEP FAIL!");
             }
         }
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (e.getSource().equals(OkButton)) {
             WarningFrame.dispose();
             StopSound();
         }
     }
 
     public void PlaySound() {
         try {
          URL url = this.getClass().getClassLoader().getResource("resources\\ALARM.wav");
          AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
          clip = AudioSystem.getClip();
         
          clip.open(audioIn);
          clip.start();
       } catch (UnsupportedAudioFileException e) {
          e.printStackTrace();
       } catch (IOException e) {
          e.printStackTrace();
       } catch (LineUnavailableException e) {
          e.printStackTrace();
       }
     }
     private void StopSound(){
         clip.stop();
     }
 }
