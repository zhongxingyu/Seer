 package ru.spbstu.telematics.lab_4.chat.server;
 
 import java.io.*;
 import java.net.*;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Set;
 import ru.spbstu.telematics.lab_4.chat.common.ChatMessage;
 
 /**
  * Контроллер, который занимается облуживанием новых подключений,
  * созданием обработчиков, регистрацией в них новых клиентов и
  * трансфером сообщений между обработчиками.
  * @author simonenko
  */
 public class Controller {
    
 	private HandlerSelector handlerSelector;                   // Селектор для выбора обработчика.
 	private ArrayList<Handler> activeHandlers;                 // Список активных обработчиков.
 	private ServerSocketChannel serverSocketChannel;           // Серверный сокет.
 	private volatile Queue<ChatMessage> transferMessageQueue;  // Очередь для трансфера сообщений.
 	private volatile Object transferMessageQueueMutex;         // Мьютекс на очередь, для синхронизации доступа.
 	
 	public Controller(ServerConfig config) throws Exception {
 		handlerSelector = new HandlerSelector(config.getHandlersAmount(), this);
 		activeHandlers = new ArrayList<Handler>();
 		serverSocketChannel = ServerSocketChannel.open();
 		serverSocketChannel.socket().bind(new InetSocketAddress(config.getServerPort()));
 		serverSocketChannel.configureBlocking(false);
 		transferMessageQueue = new LinkedList<ChatMessage>();
 		transferMessageQueueMutex = new Object();
 		ClientRegistrator.init(config.getHandlersAmount(),
 		      config.getHandlerMaxConnections());
 	}
 	
 	/**
 	 * Основной цикл работы контроллера.
 	 */
 	public void run() {
 		try {
 		   System.out.println("Server started");
 			Selector selector = Selector.open();
 			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
 			while(true) {
 			   // Оперировние новыми подключениями.
 				if(selector.selectNow() > 0) {
 					Set<SelectionKey> selectedKeys = selector.selectedKeys();
 					Iterator<SelectionKey> keysIterator = selectedKeys.iterator();
 					while(keysIterator.hasNext()) {
 						SelectionKey key = (SelectionKey)keysIterator.next();
 						if(key.isAcceptable()) {
 							SocketChannel socketChannel = ((ServerSocketChannel)key.channel()).accept();
 							Handler handler = handlerSelector.getHandler();
 							if(handler.registrateClient(socketChannel)) {
 							   if(!activeHandlers.contains(handler))
 							      activeHandlers.add(handler);
 							}
 							keysIterator.remove();
 						}
 					}
 				}
 				// Трансфер сообщений
 				synchronized (transferMessageQueueMutex) {
                while(!transferMessageQueue.isEmpty()) {
                   ChatMessage message = transferMessageQueue.poll();
                   Iterator<Handler> handlerIt = activeHandlers.iterator();
                   while(handlerIt.hasNext())
                      (handlerIt.next()).message(message, false);       
                }
             }
 			}
 		} catch (Exception e) {
 			System.err.println("Error! " + e.getMessage());
 		} finally {
 			try {
 				serverSocketChannel.close();
 			} catch (IOException e) {
 				System.err.println("Error! " + e.getMessage());
 			}
 		}
 	}
 	
 	/**
 	 * Метод трансфера сообщения.
 	 */
	public synchronized void tranferMessage(ChatMessage message) {
 	   synchronized (transferMessageQueueMutex) {
 	      transferMessageQueue.add(message);
       }
 	}
 }
