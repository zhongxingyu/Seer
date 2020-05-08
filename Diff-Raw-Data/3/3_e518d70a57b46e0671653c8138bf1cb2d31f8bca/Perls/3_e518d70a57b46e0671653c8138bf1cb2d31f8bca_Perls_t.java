 package Perls_Package;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.prefs.Preferences;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.border.EmptyBorder;
 
 public class Perls {
 
     static JFrame frame;
     static JDialog dialog; // Диалог добавления перла
     static String author; // Пользователь приложения
     static TrayManager trayMng; // Менеджер трея
     static ManagerDB mngDB; // Менеджер БД
     // Список имён для выделения
     static String[] names = {"\\[Feuer Herz\\]", "\\[Вы\\]",
         "\\[Виктор Хомяк\\]", "\\[Кирилл Тестин\\]",
         "Кирилл", "Виктор",
         "Нютка", "Feuer"
     };
 
     public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException {
         System.setProperty("file.encoding", "UTF-8");
         // Устанавливаем пользователя (подробнее в описании метода)
         author = setAuthor();
 
         // Автивируем трей (и обработчики событий)
         trayMng = new TrayManager();
 
 
         //System.setOut(new PrintStream(System.out, true, "cp866"));
 //        frame = new JFrame("Сингулярность перлов");
 //        JTextField tf = new JTextField("");
 //
 //        tf.setText("Разгон Адронного коллайдера, запуск 7990, активация 8350...");
 //        frame.add(tf);
 //
 //        //Отображение окна.
 //        frame.pack();
 //        frame.setLocationRelativeTo(null);
 //        frame.setVisible(true);
     }
 
     /**
      * Пытается из реестра получить имя пользователя
      * В случает отсутствия конфига выдает запрос на ввод имени, записывает его в реестр и возвращает
      * @return String author
      */
     static public String setAuthor() {
         Preferences userPrefs = Preferences.userRoot().node("perlsconf");
         String ath = userPrefs.get("author", null); // Пытаемся получить значение "author"
         if(ath == null){
             String showInputDialog = JOptionPane.showInputDialog("Введи своё имя, бро!");
             if(showInputDialog != null) {
                 showInputDialog = showInputDialog.replace(" ", ""); // Удаляем пробелы
                 if(showInputDialog.equals("")) {
                     JOptionPane.showMessageDialog(null, "Не ввел имя, значит будешь Уасей!");
                     showInputDialog = "Уася";
                 } else if(showInputDialog.length()>50) {
                     JOptionPane.showMessageDialog(null, "Сильно длинное имя, будешь Уасей!");
                     showInputDialog = "Уася";
                 }
             } else {
                 JOptionPane.showMessageDialog(null, "Не ввел имя, значит будешь Уасей!");
                 showInputDialog = "Уася";
             }
             userPrefs.put("author", showInputDialog);
             return showInputDialog;
         } else {
             return ath;
         }
     }
 
     /**
      * Обработчик "Добавить перл" (Используется в TrayManager)
      */
     static ActionListener addPerlListener = new ActionListener() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
 
             // Модальный диалог
             dialog = new JDialog(frame, "Добавь перл, сука!", true);
 
             // Панель
             JPanel p = new JPanel();
             p.setLayout(new GridBagLayout());
             p.setBorder(new EmptyBorder(10, 10, 10, 10));
             p.add(new JLabel("Добавь свой божественный перл сюда ↓"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
                 GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0), 0);
             // TextArea
             final JTextArea ta = new JTextArea(20, 25);
             ta.setLineWrap(true);
             ta.setBorder(BorderFactory.createLineBorder(Color.BLACK));
             JScrollPane scroll = new JScrollPane(ta);
 
             p.add(scroll, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST,
                 GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0), 0);
             JButton buttonAdd = new JButton("Жги!");
 
             // Обработчик нажатия кнопки
             buttonAdd.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if(!ta.getText().isEmpty()) {
 
                         // Заменяем имена для выделения
                         String perl = ta.getText();
                         for (String word : names) {
                             perl = perl.replaceAll(word, "<b>" + word + "</b>");
                         }
                         ta.setText(perl);
                         // Добавляем в базу запись
                         mngDB = new ManagerDB();
                         String result = mngDB.setDB(ta.getText(), author);
 
                         // Обработка результата
                         switch(result) {
                             case "The request is successful!":
                                 trayMng.trayMessage("Перл успешно добавлен!");
                                 break;
                             case "There is no option PERL!":
                                 trayMng.trayMessage("Отсутствует параметр PERL");
                                 break;
                             case "There is no option AUTHOR!":
                                 trayMng.trayMessage("Отсутствует параметр AUTHOR");
                                 break;
                             case "The request failed":
                                 trayMng.trayMessage("Ошибка запроса о_О");
                                 break;
                             case "Нет соединения с сервером, не добавлено!":
                                 trayMng.trayMessage("Нет соединения с сервером, не добавлено!");
                                 break;
                            case "DB Error":
                                trayMng.trayMessage("Ошибка базы данных, не добавлено!");
                                break;
                             case "Ошибка преобразования строки о_О":
                                 trayMng.trayMessage("Ошибка преобразования строки о_О");
                                 break;
                             default:
                                 trayMng.trayMessage(result);
                                 break;
                         }
                         dialog.setVisible(false);
                     } else {
                         JOptionPane.showMessageDialog(null, "Ты не написал ничего =(", "Ащипка!", JOptionPane.INFORMATION_MESSAGE);
                     }
                 }
             });
             p.add(buttonAdd, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST,
                 GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0), 0);
 
             // Переопределяем обработчик закртия окна
             dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
             dialog.addWindowListener(new WindowAdapter() {
                 @Override
                 public void windowClosing(WindowEvent we) {
                     JOptionPane.showMessageDialog(null, "<html>Ты что с ума сошел? Дорогой друг издалека прилетает на минуточку — а у вас нет <s>торта</s> шутки!?</html>",
                             "Ну... Ц!", JOptionPane.INFORMATION_MESSAGE);
                     dialog.setVisible(false);
                 }
             });
             // Отображение окна
             dialog.setContentPane(p);
             dialog.pack();
             dialog.setLocationRelativeTo(null);
             dialog.setVisible(true);
         }
     };
 }
