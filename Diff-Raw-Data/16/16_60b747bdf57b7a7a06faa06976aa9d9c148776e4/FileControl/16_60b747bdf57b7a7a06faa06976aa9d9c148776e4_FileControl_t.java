 /**
  * This file is part of FileControl application (check README).
  * Copyright (C) 2013  Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package filecontrol;
 
 /**
  * FileControl main class;
  * @author Stanislav Nepochatov
  */
 public class FileControl {
     
     /**
      * Main window object;
      */
     public static ControlFrame MainWindow = new ControlFrame();
     
     /**
      * Main configuration properties;
      */
     public static java.util.Properties MainProperties = new java.util.Properties();
     
     /**
      * Date format
      */
     private static java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
     
     /**
      * Config path to files
      */
     public static String configPath = System.getProperty("user.home") + "/.FileControl";
     
     /**
      * Log file object;
      */
     private static java.io.File logFile;
     
     /**
      * User notify flag;
      */
     public static Boolean notifyFlag = false;
     
     /**
      * System tray icon object
      */
     public static java.awt.TrayIcon controlIcon;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         new java.io.File(configPath).mkdirs();
         initLogFile();
         initProperties();
         ControlQuene.init();
         initSystemTray();
        MainWindow.postInit();
         MainWindow.setVisible(true);
     }
     
     /**
      * Initiate log file;
      */
     private static void initLogFile() {
         logFile = new java.io.File(configPath + "/Control.log");
         if (!logFile.exists()) {
             try {
                 logFile.createNewFile();
             } catch (java.io.IOException ex) {
                 logFile = null;
                 log(0, "Невозможно создать файл журнала!");
             }
         } else {
             if (logFile.length() > (200 * 1024)) {
                 try {
                     java.nio.file.Files.delete(logFile.toPath());
                 } catch (java.io.IOException ex) {
                     logFile = null;
                     log(0, "Невозможно отчистить файл журнала!");
                 }
                 logFile = null;
             }
         }
     }
     
     /**
      * Init main application properties or create new one;
      */
     private static void initProperties() {
         java.io.File mainProps = new java.io.File(configPath + "/" + "FileControl.properties");
         if (!mainProps.exists()) {
             try {
                 log(2, "Файл конфигурации не найден. Создание...");
                 mainProps.createNewFile();
                 java.util.Properties tempProps = new java.util.Properties();
                 tempProps.load(FileControl.class.getResourceAsStream("FileControl_sample.properties"));
                 tempProps.store(new java.io.FileWriter(mainProps), null);
             } catch (java.io.IOException ex) {
                 log(0, "Невозможно создать файл конфигурации!");
             }
         }
         try {
             MainProperties.load(new java.io.FileInputStream(mainProps));
         } catch (java.io.IOException ex) {
             log(0, "Невозможно прочитать файл конфигурации!");
         }
     }
     
     /**
      * Init system tray object;
      */
     private static void initSystemTray() {
         java.awt.PopupMenu controlMenu = new java.awt.PopupMenu();
         java.awt.MenuItem showItem = new java.awt.MenuItem("Показать");
         java.awt.MenuItem exitItem = new java.awt.MenuItem("Выход");
         controlMenu.add(showItem);
         controlMenu.addSeparator();
         controlMenu.add(exitItem);
         controlIcon = new java.awt.TrayIcon(MainWindow.appIconNormal);
         controlIcon.setPopupMenu(controlMenu);
         showItem.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 FileControl.MainWindow.setVisible(true);
             }
         });
         exitItem.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 Object[] options = {"Да", "Нет"};
                 Integer result = javax.swing.JOptionPane.showOptionDialog(new javax.swing.JPanel(),
                     "Закрыть программу?",
                     "Контроль файлов",
                     javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
                     javax.swing.JOptionPane.QUESTION_MESSAGE,
                     null,
                     options,
                     options[1]
                 );
                 if (result == 0) {
                     FileControl.controlExit();
                 }
             }
         });
         controlIcon.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 FileControl.MainWindow.setVisible(true);
             }
         });
         try {
             java.awt.SystemTray.getSystemTray().add(controlIcon);
         } catch (java.awt.AWTException ex) {
             log(1, "Невозможно инициализировать поддержку системного лотка!");
             ex.printStackTrace();
         }
     }
     
     /**
      * Store properties to config file;
      */
     public static void storeProperties() {
         try {
             MainProperties.store(new java.io.FileWriter(new java.io.File(configPath + "/" + "FileControl.properties")), null);
         } catch (java.io.IOException ex) {
             FileControl.log(1, "Невозможно сохранить файл конфигурации!");
         }
     }
     
     /**
      * Get current date with default date format
      * @return current date in string;
      */
     private static String getCurrentDate() {
         java.util.Date now = new java.util.Date();
         String strDate = dateFormat.format(now);
         return strDate;
     }
     
     /**
      * Write message to log
      * @param level level of message;
      * @param message message itself;
      */
     public static synchronized void log(Integer level, String message) {
         String typeStr = "";
         switch (level) {
             case 0:
                 typeStr = "КРИТИЧЕСКАЯ ОШИБКА";
                 break;
             case 1:
                 typeStr = "ОШИБКА";
                 break;
             case 2:
                 typeStr = "предупреждение";
                 break;
             case 3:
                 typeStr = "сообщение";
                 break;
         }
         String compiledMessage = getCurrentDate() + " " + typeStr + ": '" + message + "';";
         System.out.println(compiledMessage);
         MainWindow.addToLog(compiledMessage);
         if (logFile != null) {
             try (java.io.FileWriter logWriter = new java.io.FileWriter(logFile, true)) {
                 logWriter.write(compiledMessage + "\r\n");
             } catch (Exception ex) {
                 logFile = null;
                 log(0, "невозможно записать файл журнала!");
             }
         }
         if (level < 2) {
             final javax.swing.JPanel panel = new javax.swing.JPanel();
             javax.swing.JOptionPane.showMessageDialog(panel, message, "Ошибка!", javax.swing.JOptionPane.ERROR_MESSAGE);
             if (level == 0) {
                 System.exit(-1);
             }
         }
     }
     
     /**
      * Handle signal from control entry's;
      * @param openFileManager open file manager;
      * @param givenEntry entry which called this method;
      */
     public synchronized static void conditionHandle(Boolean openFileManager, ControlEntry givenEntry) {
         if (openFileManager && (MainProperties.getProperty("err_open_filemanager")).equals("1")) {
             try {
                 Runtime.getRuntime().exec(MainProperties.getProperty("err_file_handler").replace("$PATH", givenEntry.Path));
             } catch (java.io.IOException ex) {
                 log(1, "Невозможно создать под-процесс!");
             }
         }
         notifyFlag = true;
     }
     
     /**
      * Exit FileControl application;
      */
     private static void controlExit() {
         log(2, "Завершение работы программы.");
         MainProperties.setProperty("window_pos_x", String.valueOf(MainWindow.getX()));
         MainProperties.setProperty("window_pos_y", String.valueOf(MainWindow.getY()));
         MainProperties.setProperty("scan_timer_index", String.valueOf(MainWindow.timerBox.getSelectedIndex()));
         storeProperties();
         System.exit(0);
     }
 }
