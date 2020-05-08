 package com.rocketraman.portkeeper;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.spi.SelectorProvider;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Iterator;
 
 /**
  * Keeps open a set of ports specified in a portkeeper.properties file.
  */
 public class PortKeeper implements Runnable {
 
     private final List<ServerSocket> boundSockets = new LinkedList<ServerSocket>();
     private final List<Integer> pendingPorts = new LinkedList<Integer>();
     private Thread keeperThread;
     private volatile boolean running = true;
 
     public static void main(String[] args) throws Exception {
 
         final PortKeeper keeper = new PortKeeper();
 
         Runtime.getRuntime().addShutdownHook(new Thread() {
             public void run() {
                 keeper.stop();
             }
         });
 
         keeper.start();
 
     }
 
     private void start() {
 
         final Thread keeperThread = new Thread(this);
         keeperThread.setDaemon(false);
         keeperThread.start();
 
         this.keeperThread = keeperThread;
 
     }
 
     public void run() {
 
         Properties keeperProperties = new Properties();
 
         try {
 
             keeperProperties.load(PortKeeper.class.getResourceAsStream("/portkeeper.properties"));
 
         } catch (IOException e) {
 
             System.err.println("Cannot load portkeeper.properties from classpath.");
             return;
 
         }
 
         int[] ports = parsePorts(keeperProperties.getProperty("ports"), keeperProperties.getProperty("ports.exclude"));
 
         keep(ports);
 
         while(running) {
 
             retry();
 
             synchronized (this) {
 
                 try {
 
                     wait(1000);
 
                 } catch (InterruptedException e) {
 
                     /* ignore */
 
                 }
 
             }
 
         }
 
         release();
 
     }
 
     private void keep(int[] ports) {
 
         for(int port : ports) {
 
             if(! openPort(port, true)) {
 
                 synchronized (pendingPorts) {
 
                     pendingPorts.add(port);
 
                 }
 
             }
 
         }
 
     }
 
     private void retry() {
 
         synchronized (pendingPorts) {
 
             for(Iterator<Integer> portIterator = pendingPorts.iterator(); portIterator.hasNext();) {
 
                 int port = portIterator.next();
 
                 if(openPort(port, false)) {
 
                     portIterator.remove();
 
                 }
 
             }
 
         }
 
     }
 
     private void release() {
 
         for(ServerSocket serverSocket : boundSockets) {
 
             closePort(serverSocket);
 
         }
 
     }
 
     private boolean openPort(int port, boolean printError) {
 
         try {
 
            ServerSocketChannel socketChannel = SelectorProvider.provider().openServerSocketChannel();
             ServerSocket socket = socketChannel.socket();
             socket.bind(new InetSocketAddress(port));
             boundSockets.add(socket);
             System.out.println("+ " + port);
 
             return true;
 
         } catch (IOException e) {
 
             if(printError) System.err.println("E " + port + " : " + e.getMessage());
             return false;
 
         }
 
     }
 
     private void closePort(ServerSocket socket) {
 
         int port = socket.getLocalPort();
 
         try {
 
             socket.close();
             System.out.println("- " + port);
 
         } catch (IOException e) {
 
             System.err.println("E " + port + " : " + e.getMessage());
 
         }
 
     }
 
     private void stop() {
 
         synchronized (pendingPorts) {
 
             pendingPorts.clear();
 
         }
 
         this.running = false;
 
         synchronized (this) {
 
             notifyAll();
 
         }
 
         try {
 
             this.keeperThread.join();
 
         } catch (InterruptedException e) {
 
             /* ignore */
 
         }
 
     }
 
     private int[] parsePorts(String portsString, String portsExcludeString) {
 
         List<Integer> portList = new LinkedList<Integer>();
         List<Integer> portExcludeList = new LinkedList<Integer>();
 
         String[] portRanges = portsString.split(",");
         String[] portExcludeRanges = portsExcludeString != null ? portsExcludeString.split(",") : new String[0];
 
         addRangesToList(portList, portRanges);
         addRangesToList(portExcludeList, portExcludeRanges);
 
         portList.removeAll(portExcludeList);
 
         int[] ports = new int[portList.size()];
         for(int i = 0; i < portList.size(); i++) ports[i] = portList.get(i);
         return ports;
 
     }
 
     private void addRangesToList(List<Integer> list, String[] ranges) {
 
         for(String range : ranges) {
 
             addRangeToList(list, range);
 
         }
 
     }
 
     private void addRangeToList(List<Integer> list, String range) {
 
         if(range.contains("-")) {
 
             String[] beginEnd = range.split("-");
             for(int port = Integer.valueOf(beginEnd[0]); port <= Integer.valueOf(beginEnd[1]); port++) {
 
                 list.add(port);
 
             }
 
         } else {
 
             list.add(Integer.valueOf(range));
 
         }
 
     }
 
 }
