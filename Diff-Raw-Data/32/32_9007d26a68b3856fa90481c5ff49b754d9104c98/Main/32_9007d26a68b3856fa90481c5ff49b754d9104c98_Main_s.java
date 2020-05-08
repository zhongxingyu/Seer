 /*
  * Copyright (c) 2013, Sam Malone. All rights reserved.
  * 
  * Redistribution and use of this software in source and binary forms, with or
  * without modification, are permitted provided that the following conditions
  * are met:
  * 
  *  - Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  - Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  - Neither the name of Sam Malone nor the names of its contributors may be
  *    used to endorse or promote products derived from this software without
  *    specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package swapf;
 
 import java.io.IOException;
 
 /**
  *
  * @author Sam Malone
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Args arguments = Args.parse(args);
         if(arguments == null) {
             Display.printHelp();
             System.exit(0);
         }
        if(!Args.validate(arguments)) {
            System.out.println("The input files are not valid");
             System.exit(1);
         }
         System.out.print(Display.formattedFileList(arguments.getFileList()));
         System.out.println("\nEnter the ID of the filename to swap (blank to ignore):\n");
         Swapper s = new Swapper(arguments.getFileList());
         s.promptIds();
         System.out.println();
         try {
             s.validateSwaps();
         } catch(IOException e) {
             System.err.println(e.getMessage());
             System.exit(1);
         }
         System.out.println("Preview Swaps:\n");
         System.out.println(s.preview());
         System.out.print("Swap Files? y/[n]: ");
         if(!s.confirmSwaps()) {
             System.exit(0);
         }
         try {
             s.swap();
         } catch(IOException e) {
             System.err.println(e.getMessage());
         }
     }
     
 }
