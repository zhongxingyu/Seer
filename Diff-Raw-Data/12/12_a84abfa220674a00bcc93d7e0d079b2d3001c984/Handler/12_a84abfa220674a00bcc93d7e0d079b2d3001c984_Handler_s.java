 package ru.spbstu.telematics.lab_4.chat.server;
 
 import java.io.IOException;
 import java.io.StreamCorruptedException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Set;
 import ru.spbstu.telematics.lab_4.chat.common.ChatMessage;
 
 /**
  * Обработчик
  * @author simonenko
  */
 public class Handler extends Thread implements Runnable {
    
    private int id;                                       // Идентификатор обработчика.
 	private volatile Selector clientSelector;             // Селектор для выбора активных соединений.
 	private volatile Object clientSelectorMutex;          // Мьютекс для селектора.
 	private volatile HashMap<Integer, Client> clientsMap; // Список пользователей. 
 	private volatile Object clientsMapMutex;              // Мьютекс для списка.
 	private volatile Queue<ChatMessage> messageQueue;     // Очередь сообщений обработчика.
 	private volatile Object messageQueueMutex;            // Мьютекс для очереди.
 	private Controller controller;                        // Ссылка на контроллер.
 	
 	/**
 	 * Конструктор класса.
 	 */
 	public Handler(int id, Controller controller) {
 		super();	
 		this.id = id;
 		try {
          clientSelector = Selector.open();
       } catch (IOException e) {
          System.err.println(selfName() + ".E: " + e.getMessage());
       }
 		clientSelectorMutex = new Object();
 		clientsMap = 
 		      new HashMap<Integer, Client>(ClientRegistrator.getHandlerUserAmount());
 		clientsMapMutex = new Object();
 		messageQueue = new LinkedList<ChatMessage>();
 		messageQueueMutex = new Object();
 		this.controller = controller;
 	}
 	
 	/**
 	 * Основной цикл работы обработчика.
 	 * Слушает сокеты, произвдит чтиение и запись из них,
 	 * передает данные клиентам.
 	 */
 	@Override
 	public void run() {
 		try {
 		   if(clientSelector == null)
 		      throw new Exception("Client Selector is empty");
 		   while(true) {
 		      try {
 		         // Работа с сокетом, передача активных сокетов к клиентам.
 		         synchronized(clientSelectorMutex) {
 		            if(clientSelector.selectNow() > 0) {
 		               Set<SelectionKey> selectedKeys = clientSelector.selectedKeys();
 		               Iterator<SelectionKey> keysIterator = selectedKeys.iterator();
 		               while(keysIterator.hasNext()) {
 		                  SelectionKey key = (SelectionKey)keysIterator.next();
 		                  SocketChannel socketChannel = (SocketChannel)key.channel();
 		                  if(key.isReadable()) {
 		                     Client client = (Client)key.attachment();
 		                     if(client != null) {
 		                        try {
 		                           client.inputMessage(socketChannel);
 		                        } catch (StreamCorruptedException sce) {
 		                           client.deactivate();
 		                           key.cancel();
 		                        }
 		                     }
 		                  }
 		                  else if(key.isWritable()) {
 		                     Client client = (Client)key.attachment();
 		                     if(client != null) {
 		                        client.outputMessage(socketChannel);
 		                        if(client.isRegistrated() && key.isValid()) {
 		                           client.deactivate();
 		                           key.cancel();
 		                        }
 		                     }
 		                  }
 		                  keysIterator.remove();
 		               }
 		            }
 		            Set<SelectionKey> keys = clientSelector.keys();
 		            Iterator<SelectionKey> it = keys.iterator();
 		            while(it.hasNext()) {
 		               SelectionKey key = it.next();
 		               ((Client)key.attachment()).processMessages();
 		            }
 		         }
 		         // Передача трансферных сообщений клинетам.
 		         synchronized (messageQueueMutex) {
 		            while(!messageQueue.isEmpty()) {
 		               ChatMessage message = messageQueue.poll();
 		               System.out.println(selfName() + ".T: " + message);
 		               if(message != null) {
 		                  Client toClient = null;
 		                  boolean isToAll = false;
 		                  synchronized (clientsMapMutex) {
 		                     if(message.getTo() != null) {
 		                        int id = message.getTo().getId();
 		                        if(id != -1)
 		                           toClient = clientsMap.get(id);
 		                        else
 		                           isToAll = true;
 		                     }
 		                  }
 		                  if(toClient != null) {
 		                     toClient.inputMessage(message);
 		                  }
 		                  else if(isToAll) {
 		                     Iterator<Client> clientIt = clientsMap.values().iterator();
 		                     while(clientIt.hasNext()) {
 		                        toClient = clientIt.next();
 		                        if(toClient.isActive())
 		                           toClient.inputMessage(message);
 		                     }
 		                  }
 		                  break;
 		               }
 		            }							
 		         }
    			} catch (Exception e) {
    			   System.err.println(selfName() + ".E: " + e.getMessage());
    			   e.printStackTrace();
    			}
 		   }
    	} catch (Exception e) {
    	   System.err.println(selfName() + ".E: " + e.getMessage());
    	}
 	}
 
 	/**
 	 * Метод регистрации клиента.
 	 * @param socketChannel - канал, к которому подключился новый клиент.
 	 */
    public boolean registrateClient(SocketChannel socketChannel) {
 		int operationMask = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
 		try {   
 		   if(socketChannel == null)
 		      throw new Exception("socketChannel is empty");
 		   socketChannel.configureBlocking(false);
 		   synchronized(clientSelectorMutex) {
 		      int clientId = ClientRegistrator.reserveIndex(id);
 		      if(clientId == -1) {
 		         System.out.println(selfName() + ".R: can't register new connection");
 		         return false;
 		      }
 		      System.out.println(selfName() + ".R: " + clientId);
 		      SelectionKey key = socketChannel.register(clientSelector, operationMask);
 		      synchronized (clientsMapMutex) {
 		         Client client = clientsMap.get(clientId);
 		         if(client == null) {
 		            client = new Client(clientId, this);
 		            clientsMap.put(clientId, client);
 		         }
 		         key.attach(client);
 		      }
 		   }
 		} catch (Exception e) {
 			System.err.println(selfName() + ".E: At registrateClient: " + e.getMessage());
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
    
    /**
     * Метод предачи трансферного сообщения.
     * @param message - само сообщение.
     * @param toController - флаг,
     * указывающий предавать в контроллер или в обработчик.
     */
	public synchronized void message(ChatMessage message, boolean toController) {
 	   if(toController)
 	      controller.tranferMessage(message);
 	   else {
 	      synchronized (messageQueueMutex) {
             messageQueue.add(message);
          }
 	   } 
 	}
 	
 	/**
 	 * Метод для получения идентификатора обработчика.
 	 */
	public synchronized int getHandlerId() {
 	   return id;
 	}
 	
 	private String selfName() {
 	   return "H." + id;
 	}
 }
