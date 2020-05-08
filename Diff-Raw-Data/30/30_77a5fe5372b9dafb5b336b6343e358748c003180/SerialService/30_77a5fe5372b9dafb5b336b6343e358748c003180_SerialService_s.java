 package net.ant.rc.serial;
 
 import net.ant.rc.serial.exception.CommPortException;
 
 import java.util.concurrent.PriorityBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ant
  * Date: 04.02.13
  * Time: 23:34
  * To change this template use File | Settings | File Templates.
  */
 public class SerialService implements Runnable {
 
     private final long MAX_QUEUE_SIZE = 20;
     private final long POLL_WAIT_TIMEOUT = 3000;
     private final SerialDriver serialDriver;
     private final PriorityBlockingQueue<Command> commandQueue;
    private Command STOP = TractorCommand.STOP();
     private Command lastCommand = STOP;
     private boolean serviceStopped = false;
     private int queueSize;
 
     @Override
     public void run() {
 
         while(!this.serviceStopped){
             try {
                 Command command = this.commandQueue.poll(POLL_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
 
                 //If timeout was expired
                 if (command == null) {
                     //if last command was STOP then continue waiting, else go to send STOP
                     if (lastCommand.equals(STOP)) continue;
                    command = TractorCommand.STOP();
                 }
                 queueSize = this.commandQueue.size();
 
                 VectorCommand vectorCommand = null;
                 TractorCommand tractorCommand = null;
                 String valueForLog = "";
                 if (command instanceof VectorCommand) {
                    STOP = VectorCommand.STOP();
                     vectorCommand = (VectorCommand) command;
                     valueForLog = vectorCommand.x + "," + vectorCommand.y;
                 }
                 if (command instanceof TractorCommand) {
                    STOP = TractorCommand.STOP();
                     tractorCommand = (TractorCommand) command;
                     valueForLog = tractorCommand.left + "," + tractorCommand.right;
                 }
 
                 if (CheckBypass1(command, valueForLog))continue;
                 if (CheckBypass2(command, valueForLog))continue;
                 if (CheckBypass3(command, valueForLog))continue;
 
                 if (command.commandType.equals("Digital")){
                     if (command instanceof VectorCommand) {
                         System.out.println(serialDriver.sendVectorCommand(vectorCommand.x, vectorCommand.y));
                     }
                     if (command instanceof TractorCommand) {
                         System.out.println(serialDriver.sendTractorCommand(tractorCommand.left, tractorCommand.right));
                     }
                     lastCommand = command;
                 }
             } catch (InterruptedException | CommPortException e) {
                 e.printStackTrace();
             }
         }
    }
 
     //Bypass the entries older then last sended
     private boolean CheckBypass1(Command command, String valueForLog){
         boolean result = false;
         if (command.timeMillis < lastCommand.timeMillis){
             System.out.println("Bypass1 value of [" + valueForLog + "] for " + command.timeMillis + " < " + lastCommand.timeMillis);
             result = true;
         }
         return result;
     }
 
     //Bypass the same command
     private boolean CheckBypass2(Command command, String valueForLog){
         boolean result = false;
         if (command.equals(lastCommand)){
             System.out.println("Bypass2 value of [" + valueForLog + "] already sent");
             result = true;
         }
         return result;
     }
 
     //Bypass entries if queue is too long
     private boolean CheckBypass3(Command command, String valueForLog){
         boolean result = false;
         if (queueSize > MAX_QUEUE_SIZE){
             System.out.println("Bypass3 value of [" + valueForLog + "] for " + queueSize + ">" + MAX_QUEUE_SIZE);
             result = true;
         }
         return result;
     }
 
     public SerialService(SerialDriver serialDriver, PriorityBlockingQueue<Command> commandQueue) {
         this.serialDriver = serialDriver;
         this.commandQueue = commandQueue;
     }
 
     public void stop(){
         this.serviceStopped = true;
         this.serialDriver.disconnect();
     }
 }
