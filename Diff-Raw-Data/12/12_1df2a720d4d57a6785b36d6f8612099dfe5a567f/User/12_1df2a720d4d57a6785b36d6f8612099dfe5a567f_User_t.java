 import java.io.*;
 import java.net.Socket;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Serv
  * Date: 27.06.13
  * Time: 14:51
  * To change this template use File | Settings | File Templates.
  */
 public class User  extends Thread {
     FileSystemController fileSystemController = new FileSystemController();
     Server server = null;
 
     // Сокет пользователя
     private Socket socket = null;
     // Входной канал
     public BufferedReader bufferedReader = null;
     // Выходной канал
     public PrintStream printStream = null;
     // Имя пользователя
     public String userName = "";
     // IP пользователя
     public String userIp = "";
 
 
     // Конструктор
     User(Server chatServer, Socket socket) throws IOException {
         super("User Thread");
         this.server = chatServer;
         this.socket = socket;
 
         start();
     }
 
     // Обработка потока
     @Override
     public void run() {
         try {

            if(!socket.isConnected()) return;   //just for tests !!

             bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             printStream = new PrintStream(socket.getOutputStream(), true, "UTF-8");
 
             // Предлагаем пользователю ввести свое имя
 
             // IP пользователя
 
             userIp = socket.getInetAddress().getHostAddress();
             // Получаем логин пользователя
             userName = bufferedReader.readLine(); // блокируется пока не получит строки!
 
             server.GUI.printOnServer("New user " + userName + " try connect to Server");
             synchronized (server)   {
                 for (User user : server.userList){
                     if (user.userName.equals(userName)) {
                         printStream.println("This UserName already engaged");
                         closeSocket();
                         return;
 
                     }
                 }
             }
 
             // Нотификация о подключении нового пользователя
             server.onUserConnected(this);
             // Помещаем пользователя в список пользователей
             // server.userList.add(this);
             // Отправляем всем сообщение
             // server.sendChatMessage(null, "Подключен пользователь: "+userName);
             printStream.println("Hello " + userName + "!");
             // основной цикл обработки
             while(true) {
                 try {
                     printStream.println("Type new command");
                     // Читаем новое сообщение от пользователя
                     String messageReceived = bufferedReader.readLine(); // блокируется пока не получит строки или null!
                     if(messageReceived==null) {
                         // Невозможно прочитать данные, пользователь отключился от сервера
                         closeSocket();
                         // Останавливаем бесконечный цикл
                         break;
                     }
                     else if(!messageReceived.isEmpty()) {
                         if (messageReceived.equalsIgnoreCase("quit")) {
                             closeSocket();
                             break;
                         }
                         List<String> answer = fileSystemController.doCommand(messageReceived, this);
                         for(String line : answer){
                             printStream.println(line);
 
                         }
 
                         // Нотификация: получено сообщение
                         server.onMessageReceived(this, messageReceived);
                         // Отправляем всем сообщение
                         // server.sendChatMessage(this, messageReceived);
                     }
                 } catch (IOException e1) {
                     e1.printStackTrace();
                     closeSocket();
                     break;//To change body of catch statement use File | Settings | File Templates.
                 }
 
 
             }
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
 
 
 
     // Удаляет пользователя из списка онлайн пользователей и освобождает ресурсы сокета
     private void closeSocket() {
         try {
             // Нотификация: пользователь отключился
             printStream.println("quit");
             server.onUserDisconnected(this);
             // Отправляем всем сообщение
             //   server.sendChatMessage(null, "Отключен пользователь: "+userName);
             // Удаляем пользователя со списка онлайн
             //    server.userList.remove(this);
             // Закрываем потоки
 
             bufferedReader.close();
             printStream.close();
             socket.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public void iMakeChanges(String changes) {
         server.userMakeChanges(changes, this);
     }
 }
 
