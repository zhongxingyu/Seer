 package edu.entitymaster.logic;
 
 import com.google.common.io.Files;
 
 import java.io.*;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Антон
  * Date: 10.11.12
  * Time: 16:13
  * To change this template use File | Settings | File Templates.
  */
 public class ClientService implements ClientRepository {
     private HashMap<Integer, Client> clients;
     private AtomicInteger indexer;
     private File f = new File(System.getProperty("user.dir") + "\\ClientRepositoryLog.csv");
 
     public ClientService() {
         try {
             Files.touch(f);
             clients = readClients();
         } catch (IOException e) {
             e.printStackTrace();
         }
         //creating clients ID counter
         try {
             indexer = new AtomicInteger(Collections.max(clients.keySet()) + 1);
         } catch (NoSuchElementException e) {
             indexer = new AtomicInteger(0);
         }
     }
 
     public void createClient(Client client) {
        client.setId(indexer.get());
         if (!clients.containsKey(client.getId())) {
            clients.put(indexer.getAndIncrement(), client);
             saveToLog(client);
         }
         else
             throw new IllegalArgumentException(client.read() + ": client with such ID already exists");
     }
 
     public void updateClient(Client srcClient, Client destClient) {
         srcClient.setName(destClient.getName()); // set all fields
         saveToLog(srcClient);
     }
 
     public void deleteClient(Client client) {
         clients.remove(client.getId());
         client.setDeleted(true);
         saveToLog(client);
     }
 
     private HashMap<Integer, Client> readClients() {
         clients = new HashMap<Integer, Client>();
 
         try {
             BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
             String line;
             while ((line = bufferedReader.readLine()) != null) {
                 String[] array = line.split(",");
                 int id = Integer.parseInt(array[0]);
                 String name = array[1];
                 Boolean deleted = Boolean.parseBoolean(array[2]);
                 clients.put(id, new Client(id, name, deleted));
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         return clients;
     }
 
     private void saveToLog(Client client) {
 
         try {
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(f, true));
             bufferedWriter.append(client.read());
             bufferedWriter.newLine();
             bufferedWriter.flush();
             bufferedWriter.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void deleteClientRepository(File file) {
         file.delete();
     }
 
     public HashMap<Integer, Client> getClients() {
         return clients;
     }
 }
