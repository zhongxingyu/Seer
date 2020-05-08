 package amd.tsino.launcher.ui;
 
 import amd.tsino.launcher.LauncherConstants;
 import amd.tsino.launcher.LauncherUtils;
 import net.minecraft.launcher.Launcher;
 
 import javax.swing.*;
 import java.awt.*;
 import java.io.IOException;
 
 public class LauncherFrame {
     private final JFrame frame;
     private MainPanel mainPanel;
 
     public LauncherFrame(JFrame frame) throws IOException {
         this.frame = frame;
 
         SetSystemLookAndFeel();
 
         mainPanel = new MainPanel(Launcher.getInstance().getStyle()
                 .getMainPanelStyle());
         mainPanel.setVisible(true);
 
         frame.setVisible(false);
         frame.setTitle(LauncherConstants.LAUNCHER_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         String text = ((JTextArea) ((JScrollPane) frame.getContentPane().getComponent(0)).getViewport().getComponent(0)).getText();
         Launcher.getInstance().getLog().setBootstrapLog(text);
         frame.getContentPane().removeAll();
         frame.setBackground(Color.DARK_GRAY);
         frame.add(mainPanel);
         frame.setResizable(false);
         frame.pack();
         frame.setLocationRelativeTo(null);
         frame.getRootPane().setDefaultButton(mainPanel.getAuth().getEnter());
         frame.setVisible(true);
     }
 
     private static void SetSystemLookAndFeel() {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             Launcher.getInstance().getLog().error(e);
         }
     }
 
     public void hide() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 frame.setVisible(false);
             }
         });
     }
 
     public void showLoginError() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 mainPanel.getAuth().showLoginError();
             }
         });
     }
 
     public void showOutdatedNotice() {
         String error = "Извините, у Вас старая версия лаунчера.\n"
                 + "Пожалуйста, скачайте новый лаунчер.";
 
         int result = JOptionPane.showOptionDialog(frame, error,
                 "Outdated launcher", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
                 LauncherConstants.UPDATE_BUTTONS,
                 LauncherConstants.UPDATE_BUTTONS[1]);
 
         if (result < LauncherConstants.UPDATE_URLS.length - 1) {
             LauncherUtils.openLink(LauncherConstants.UPDATE_URLS[result]);
         }
     }
 
     public int showOfflineNotice() {
         String message = "Извините, не удалось подключиться к серверу.\n" +
                 "Проверьте Ваше интернет-соединение.\n\n" +
                 "Попытаться запустить игру в оффлайн режиме?";
 
         return JOptionPane.showConfirmDialog(frame, message,
                 "Offline", JOptionPane.YES_NO_OPTION);
     }
 
     public void showDownloadFailedNotice() {
         String message = "<html>Извините, не удалось скачать все необходимые файлы." +
                 "<br>Проверьте Ваше интернет-соединение." +
                 "<br><br>Журнал событий:</html>";
         JOptionPane.showMessageDialog(frame, new ErrorPanel(message),
                 "Download failed", JOptionPane.WARNING_MESSAGE);
     }
 
     public void showLaunchFailedNotice() {
         String message = "<html>Извините, произошла ошибка." +
                 "<br>Пожалуйста, перезапустите лаунчер." +
                 "<br><br>Журнал событий:</html>";
         JOptionPane.showMessageDialog(frame, new ErrorPanel(message),
                 "Launch failed", JOptionPane.WARNING_MESSAGE);
     }
 }
