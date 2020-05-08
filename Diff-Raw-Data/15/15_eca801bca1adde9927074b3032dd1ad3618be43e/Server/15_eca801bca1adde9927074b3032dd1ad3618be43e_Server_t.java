 package org.soapdemo.server;
 
 import com.sun.net.httpserver.HttpContext;
 import com.sun.net.httpserver.HttpServer;
 import org.soapdemo.server.dao.AircraftDao;
 import org.soapdemo.server.dao.AirportDao;
 import org.soapdemo.server.dao.jdbc.AircraftDaoJdbcImpl;
 import org.soapdemo.server.dao.jdbc.AirportDaoJdbcImpl;
 
 import javax.xml.ws.Endpoint;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @since 25.06.13
  */
 public class Server {
 
     public static final String JDBC_DRIVER = "org.firebirdsql.jdbc.FBDriver";
 
     private static final Logger logger = Logger.getLogger(Server.class.getName());
 
     public static void main(String[] args) {
         // Разбираем параметры командной строки
         Settings settings = Settings.parseSettings(args);
         if (!settings.isSucceed()) {
             // Если возникла какая-то ошибка при разборе параметров,
             // то печатаем сообщение об ошибке и завершаем работу приложения
             logger.log(Level.SEVERE, settings.getMessage());
             System.exit(-1);
         }
         // Формируем адрес сервера из имени и номера порта,
         // полученных из командной строки
         String address = String.format("http://%s:%d/soap-demo", settings.getAddress(), settings.getPort());
 
         // Загружаем класс драйвера для работы с базой данных
         try {
             Class.forName(JDBC_DRIVER);
         } catch (ClassNotFoundException e) {
             // Если возникла какая-то ошибка при загрузке класса,
             // то печатаем сообщение об ошибке и завершаем работу приложения
             logger.log(Level.SEVERE, e.getMessage(), e);
             System.exit(-2);
         }
 
         Connection connection = null;
         try {
             // Устанавливаем соединение с базой данных
             connection = DriverManager.getConnection(settings.getUrl(), settings.getUsername(), settings.getPassword());
             AircraftDao aircraftDao = new AircraftDaoJdbcImpl(connection);
             AirportDao airportDao = new AirportDaoJdbcImpl(connection);
 
             // Создаем и публикуем разработанную веб-службу
             SoapDemoWebService service = new SoapDemoWebService(aircraftDao, airportDao);
             Endpoint.publish(address, service);
 
             logger.log(Level.INFO, String.format("Listen on address %s and port %d\n", settings.getAddress(), settings.getPort()));
             // Останавливаем основной поток приложения
             // Это необходимо, чтобы приложение не закрывало соединение с базой
            // данных сразу после публикации веб-службы
             while (true) {
                 Thread.sleep(1000);
             }
         } catch (SQLException e) {
             logger.log(Level.SEVERE, e.getMessage(), e);
         } catch (InterruptedException e) {
             logger.log(Level.SEVERE, e.getMessage(), e);
         } finally {
             if (connection != null) {
                 try {
                     // Закрываем соединение с базой данных
                     connection.close();
                 } catch (SQLException e) {
                     logger.log(Level.SEVERE, e.getMessage(), e);
                 }
             }
         }
     }
 }
