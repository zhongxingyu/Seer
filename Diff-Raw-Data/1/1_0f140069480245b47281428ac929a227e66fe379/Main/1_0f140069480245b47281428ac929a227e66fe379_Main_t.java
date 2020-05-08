 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.aperigeek.jlad.trainer.wikipedia;
 
 import com.aperigeek.jlad.trainer.TrainerException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Properties;
 
 /**
  * Main class responsible for running the Wikipedia trainer.
  * 
  * @author Vivien Barousse
  */
 public class Main {
     
     /**
      * Runs the application.
      * 
      * Currently, the application takes the input file on the standard input
      * and outputs the n-grams repartitions on the standard output, without
      * any specific order.
      * 
      * @param args Command line arguments
      */
     public static void main(String[] args) {
         Properties commandArgs;
         try {
             commandArgs = parseArguments(args);
         } catch (CommandLineArgsParseException ex) {
             System.err.println(ex.getMessage());
             return;
         }
         
         WikipediaTrainer trainer = new WikipediaTrainer(System.in);
         try {
             trainer.setLimit(Integer.parseInt(commandArgs.getProperty("-n", "0")));
             trainer.train();
             trainer.dump((OutputStream) System.out);
         } catch (TrainerException ex) {
             System.err.println("Unexpected error while training.");
             ex.printStackTrace(System.err);
         } catch (IOException ex) {
             System.out.println("Error while writing results.");
             ex.printStackTrace(System.err);
         }
     }
     
     private static Properties parseArguments(String... args) throws CommandLineArgsParseException {
         Properties p = new Properties();
         
         int i = 0;
         try {
             while (i < args.length) {
                 if (args[i].equals("-n")) {
                     i++;
                     // Checks integer validity
                     int n = Integer.parseInt(args[i]);
                     p.put("-n", Integer.toString(n));
                    i++;
                 } else {
                     throw new CommandLineArgsParseException("Unknown option " + args[i]);
                 }
             }
         } catch (ArrayIndexOutOfBoundsException ex) {
             throw new CommandLineArgsParseException(
                     "Missing parameter after" + args[i - 1], 
                     ex);
         } catch (NumberFormatException ex) {
             throw new CommandLineArgsParseException(
                     "Invalid number for option " + args[i - 1],
                     ex);
         }
         
         return p;
     }
 
     private static class CommandLineArgsParseException extends Exception {
 
         public CommandLineArgsParseException(Throwable cause) {
             super(cause);
         }
 
         public CommandLineArgsParseException(String message, Throwable cause) {
             super(message, cause);
         }
 
         public CommandLineArgsParseException(String message) {
             super(message);
         }
 
         public CommandLineArgsParseException() {
         }
     }
     
 }
