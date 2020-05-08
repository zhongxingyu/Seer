 package com.luka.chat.data;
 
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 import java.util.Date;
 
 /**
  * Please do not use maliciously but for educational purposes
  * Created with IntelliJ IDEA.
  * User: Luka
  * Date: 03/09/13
  * Time: 19:50
  * To change this template use File | Settings | File Templates.
  */
 public abstract class Data<T> implements Opcodes {
 
     private final T value;
    private final Date date;
     private final byte opcode;
 
     protected Data(final byte opcode, final T value) {
           this.value  = value;
          this.date = new Date();
           this.opcode = opcode;
     }
 
     protected T getValue() {
         return this.value;
     }
 
     protected byte getOpcode() {
         return this.opcode;
     }
 
     public abstract ByteBuffer getBuffer();
 
 
 
 }
