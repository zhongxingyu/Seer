 package org.soapdemo.client;
 
 import org.soapdemo.client.generated.Aircraft;
 import org.soapdemo.client.generated.Airport;
 import org.soapdemo.client.generated.SoapDemoWebService;
 import org.soapdemo.client.generated.SoapDemoWebServiceService;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.handler.Handler;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Pavel Karpukhin
  * @since 25.06.13
  */
 public class Client {
 
     private static final Logger logger = Logger.getLogger(Client.class.getName());
 
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
         String address = String.format("http://%s:%d/soap-demo", settings.getHost(), settings.getPort());
         URL url = null;
         QName qName = new QName("http://server.soapdemo.org/", "SoapDemoWebServiceService");
         try {
             url = new URL(address + "?wsdl");
         } catch (MalformedURLException e) {
             // Если возникла какая-то ошибка при формировании адреса,
             // то печатаем сообщение об ошибке и завершаем работу приложения
             logger.log(Level.SEVERE, "Malformed URL: " + address);
             System.exit(-2);
         }
         logger.log(Level.INFO, "Connecting to " + address);
         SoapDemoWebServiceService service = new SoapDemoWebServiceService(url, qName);
 
         SoapDemoWebService port = service.getSoapDemoWebServicePort();
 
        // Добавляем специальный "логгер", который бедт записывать все SOAP сообщения
         addLogger(port);
 
         int k = -1;
         Scanner scanner = new Scanner(System.in);
         do {
             System.out.println();
             System.out.println("1. Print list of aircrafts.");
             System.out.println("2. Print list of airports.");
             System.out.println("0. Exit.");
             String line = scanner.nextLine();
             try {
                 k = Integer.parseInt(line);
             } catch (NumberFormatException e) {
                 System.out.println("you must enter a number");
                 continue;
             }
             switch (k) {
                 case 1:
                    // Зарпашиваем с сервера список воздушных суден
                     // и выводим его на экран
                     List<Aircraft> aircrafts = port.getAllAircrafts();
                     System.out.printf("%4s %15s %9s %4s\n", "id", "name", "price", "places");
                     for (Aircraft aircraft : aircrafts) {
                         System.out.printf("%4d %15s %9.2f %4d\n", aircraft.getId(), aircraft.getName(), aircraft.getPriceHourPlace(), aircraft.getNumberOfPlaces());
                     }
                     break;
                 case 2:
                    // Зарпашиваем с сервера список аэропортов
                     // и выводим его на экран
                     List<Airport> airports = port.getAllAirports();
                     System.out.printf("%4s %15s %4s\n", "id", "name", "city");
                     for (Airport airport : airports) {
                         System.out.printf("%4d %15s %4d\n", airport.getId(), airport.getName(), airport.getCityId());
                     }
                     break;
             }
         } while (k != 0);
     }
 
     public static void addLogger(SoapDemoWebService service) {
         BindingProvider bp = (BindingProvider)service;
         List<Handler> handlers = bp.getBinding().getHandlerChain();
         if (handlers == null) {
             handlers = new ArrayList<Handler>();
         }
         handlers.add(new LoggingHandler(System.out));
         bp.getBinding().setHandlerChain(handlers);
     }
 }
