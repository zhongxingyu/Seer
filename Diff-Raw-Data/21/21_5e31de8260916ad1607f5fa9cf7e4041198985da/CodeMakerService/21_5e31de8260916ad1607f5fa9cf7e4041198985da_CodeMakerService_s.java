 package com.adrienbrault.jastermind.server;
 
 import com.adrienbrault.jastermind.model.CodePeg;
 import com.adrienbrault.jastermind.model.KeyPeg;
 import com.adrienbrault.jastermind.model.Peg;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.Arrays;
 import java.util.Random;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @Author: adrienbrault
  * @Date: 04/06/11 12:26
  */
 public class CodeMakerService implements Runnable {
 
     protected Socket socket;
     protected ObjectOutputStream objectOutputStream;
     protected ObjectInputStream objectInputStream;
 
     protected CodePeg[] secretCode = new CodePeg[Peg.LINE_SIZE];
 
     public CodeMakerService(Socket socket) {
         this.socket = socket;
 
         this.generateSecretCode();
     }
 
     @Override
     public String toString() {
         return super.toString() + " " + this.secretCode;
     }
     
 
 
     public void run() {
         System.out.println("Service start: " + this);
 
         try {
             this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
             this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
 
             while (this.objectInputStream != null) {
                 CodePeg[] codePegs = (CodePeg[])this.objectInputStream.readObject();
 
                 KeyPeg[] keyPegs = this.getAnswer(codePegs);
 
                 this.objectOutputStream.writeObject(keyPegs);
             }
         } catch (IOException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } finally {
             this.closeStreams();
 
             System.out.println("Service end: " + this);
         }
     }
 
     protected void closeStreams() {
         if (this.objectInputStream != null) {
             try {
                 this.objectInputStream.close();
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 this.objectInputStream = null;
             }
         }
 
         if (this.objectOutputStream != null) {
             try {
                 this.objectOutputStream.close();
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 this.objectOutputStream = null;
             }
         }
         
         if (this.socket != null) {
             try {
                 this.socket.close();
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 this.socket = null;
             }
         }
     }
 
     
 
     protected void generateSecretCode() {
         Random randomGenerator = new Random();
 
         System.out.print("[");
         for (int i=0; i< Peg.LINE_SIZE; i++) {
             int randomPegIndex = randomGenerator.nextInt(CodePeg.values().length);
             this.secretCode[i] = CodePeg.values()[randomPegIndex];
             System.out.print(this.secretCode[i] + " ,");
         }
         System.out.print("]\n");
     }
 
     protected KeyPeg[] getAnswer(CodePeg[] codePegs) {
         KeyPeg[] keyPegs = new KeyPeg[Peg.LINE_SIZE];
         Arrays.fill(keyPegs, KeyPeg.WRONG);
         int keyPegsIndex = 0;
 
         // Check positions.
        boolean[] correctPositionsUsed = new boolean[Peg.LINE_SIZE];
        Arrays.fill(correctPositionsUsed, false);
 
         for (int i=0; i<Peg.LINE_SIZE; i++) {
             if (codePegs[i] == this.secretCode[i]) {
                correctPositionsUsed[i] = true;
                 keyPegs[keyPegsIndex++] = KeyPeg.CORRECT;
             }
         }
        
 
         // Checking colors.
        boolean[] correctColorUsed = new boolean[Peg.LINE_SIZE];
        Arrays.fill(correctColorUsed, false);

         for (int i=0; i<Peg.LINE_SIZE; i++) {
            if (!correctPositionsUsed[i]) {
                 for (int j=0; j<Peg.LINE_SIZE; j++) {
                    if (codePegs[i] == this.secretCode[j] && !correctColorUsed[j]) {
                        correctColorUsed[j] = true;
                         keyPegs[keyPegsIndex++] = KeyPeg.COLOR;
                     }
                 }
             }
         }
 
         return keyPegs;
     }
 
 }
