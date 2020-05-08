 // Copyright (C) 2004 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.testutility;
 
 import java.io.File;
 import java.io.IOException;
import java.util.Arrays;
 
 import junit.framework.Assert;
 
 import net.grinder.common.GrinderException;
 
 
 /**
 * File utilities missing from Java.
  *
  * @author    Philip Aston
  */
 public class FileUtilities extends Assert {
 
   public static void setCanAccess(File file, boolean canAccess)
     throws Exception {
     //    exec(new String[] {
     //           "chmod",
     //           canRead ? "ugo+r" : "ugo-r",
     //           file.getAbsolutePath(),
     //         });
 
    // Strewth: getCanonicalPath doesn't quote spaces correctly for cacls.
    String path = file.getCanonicalPath();
    path = path.replaceAll("%20", " ");

     // Sadly cygwin ntsec support doesn't allow us to ignore inherited
     // attributes. Do this instead:
     exec(new String[] {
            "cacls",
           path,
            "/E",
            "/P",
            System.getProperty("user.name") + ":" + (canAccess ? "F" : "N"),
          });
   }
 
   /*
   public static void setCanWrite(File file, boolean canWrite)
     throws Exception {
 
     exec(new String[] {
            "chmod",
            canWrite ? "ugo+w" : "ugo-w",
            file.getAbsolutePath(),
          });
   }
   */
 
   private static void exec(String[] command)
     throws GrinderException, InterruptedException {
 
     final Process process;
 
     try {
       process = Runtime.getRuntime().exec(command);
     }
     catch (IOException e) {
       throw new GrinderException(
         "Couldn't chmod: perhaps you should patch this" +
         "test for your platform?",
         e);
     }

     process.waitFor();
 
    assertEquals("exec of " + Arrays.asList(command) +
      " succeeded", 0, process.exitValue());
   }
 }
