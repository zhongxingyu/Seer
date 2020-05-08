 package restaurante;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import javax.imageio.ImageIO;
 import javax.swing.JDesktopPane;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author Alan
  */
 public class Utils {
     
     public static void showInfoMessage(Component parent, String text) {
         JOptionPane.showMessageDialog(parent, text, Main.PROGRAM_NAME, JOptionPane.INFORMATION_MESSAGE);
     }
     
     public static void showErrorMessage(Component parent, String text) {
         JOptionPane.showMessageDialog(parent, text, Main.PROGRAM_NAME, JOptionPane.ERROR_MESSAGE);
     }
     
     public static boolean showQuestionMessage(Component parent, String text) {
         return JOptionPane.showConfirmDialog(parent, text, Main.PROGRAM_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0;
     }
 
     public static void centerScreen(JFrame frame) {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension jFrameSize = frame.getSize();
         frame.setLocation((screenSize.width - jFrameSize.width) / 2, (screenSize.height - jFrameSize.height) / 2);
     }
     
     public static void centerScreen(JDialog frame) {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension jFrameSize = frame.getSize();
         frame.setLocation((screenSize.width - jFrameSize.width) / 2, (screenSize.height - jFrameSize.height) / 2);
     }
     
     public static void centerScreen(JInternalFrame frame) {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension jFrameSize = frame.getSize();
         frame.setLocation((screenSize.width - jFrameSize.width) / 2, (screenSize.height - jFrameSize.height) / 2);
     }
     
     public static void centerScreen(JDesktopPane desktop, JInternalFrame frame) {
         Dimension screenSize = desktop.getSize();
         Dimension jFrameSize = frame.getSize();
         frame.setLocation((screenSize.width - jFrameSize.width) / 2, (screenSize.height - jFrameSize.height) / 2);
     }
     
     public static String[] getAbbreviationStates() {
         String[][] tempUf = getStates();
         String[] uf = new String[tempUf.length];
         for(int i = 0; i < tempUf.length; i++) {
             uf[i] = tempUf[i][0];
         }
         Arrays.sort(uf);
         return uf;
     }
     
     public static String[][] getStates() {
         String[][] uf =
                 {{"AC","Acre"},
                 {"AL","Alagoas"},
                 {"AP","Amapá"},
                 {"AM","Amazonas"},
                 {"BA","Bahia"},
                 {"CE","Ceará"},
                 {"DF","Distrito Federal"},
                 {"ES","Espirito Santo"},
                 {"GO","Goiás"},
                 {"MA","Maranhão"},
                 {"MT","Mato Grosso"},
                 {"MS","Mato Grosso do Sul"},
                 {"MG","Minas Gerais"},
                 {"PA","Pará"},
                 {"PB","Paraiba"},
                 {"PE","Pernambuco"},
                 {"PI","Piauí"},
                 {"PR","Paraná"},
                 {"RJ","Rio de Janeiro"},
                 {"RN","Rio Grande do Norte"},
                 {"RS","Rio Grande do Sul"},
                 {"RO","Rondônia"},
                 {"RR","Roraima"},
                 {"SC","Santa Catarina"},
                 {"SE","Sergipe"},
                 {"SP","São Paulo"},
                 {"TO","Tocantins"}};
         return uf;
     }
         
     public static <T> ArrayList<T> arrayToArrayList(Class<T> clazz, T[] array) {
         ArrayList<T> arrayList = new ArrayList<T>();
         for(T object : array) {
             arrayList.add(object);
         }
         return arrayList;
     }
     
     public static Date stringToDate(String dateString) throws ParseException {
         String format = null;
         if(dateString.length() == 10) {
             format = "dd/MM/yyyy";
         } else { 
             format = "dd/MM/yyyy HH:mm";
         }
         SimpleDateFormat formatter = new SimpleDateFormat(format);
         return formatter.parse(dateString);
     }
     
     public static String dateToString(Date date) throws ParseException {
         if(date == null) { return ""; }
         
         SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
         String ret = formatter.format(date);
         return ret;
     }
 
     public static String hourToString(Date date) throws ParseException {
         SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
         return formatter.format(date);
     }
     
     public static String doubleToCurrencyString(double value) {
         return String.format("R$ %.2f", value);
     }
     
     public static double currencyStringToDouble(String value) {
         value = value.replace("R$ ", "");
         value = value.replace(",", ".");
         return Double.valueOf(value);
     }
 
     public static void copyFile(File sourceFile, String pathDestination) throws IOException {
         File destFile = new File(pathDestination);
 	if (!sourceFile.exists()) {
             return;
 	}
 	if (!destFile.exists()) {
             destFile.createNewFile();
 	}
 	FileChannel source = null;
 	FileChannel destination = null;
 	source = new FileInputStream(sourceFile).getChannel();
 	destination = new FileOutputStream(destFile).getChannel();
 	if (destination != null && source != null) {
             destination.transferFrom(source, 0, source.size());
 	}
 	if (source != null) {
             source.close();
 	}
 	if (destination != null) {
             destination.close();
 	}
     }
     
     public static BufferedImage readImageFromFile(String path) throws IOException {
         File file = new File(path);
         BufferedImage image = ImageIO.read(file);
         return image;
     }
     
     public static boolean renameFile(String path, String newName) throws IOException {
         File file = new File(path);
         return file.renameTo(new File(newName));
     }
 }
