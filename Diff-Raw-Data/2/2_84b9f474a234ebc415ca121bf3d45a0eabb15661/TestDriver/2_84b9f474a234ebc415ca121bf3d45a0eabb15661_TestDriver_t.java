 /*
  * The MIT License (MIT)
  * Copyright © 2012 Remo Koch, http://rko.mit-license.org
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
  * associated documentation files (the “Software”), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge, publish, distribute,
  * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial
  * portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
  * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package io.rko.uzh.zt2.facade.cli;
 
 import io.rko.uzh.zt2.base.Fan;
 import io.rko.uzh.zt2.model.Celebrity;
 import io.rko.uzh.zt2.model.CoWorker;
 import io.rko.uzh.zt2.model.FamilyMember;
 import io.rko.uzh.zt2.version.PackageVersion;
 
 
 public class TestDriver {
 
     public static void main(String[] pArgs) {
         System.out.println(String.format("%s started successfully.", PackageVersion.getPackageVersion()));
         System.out.println();
 
         Fan luisa = new FamilyMember("Luisa", "0791234567", 18);
         Fan axel = new CoWorker("Axel", "0441234567");
 
         Celebrity jobs = new Celebrity("Steve Jobs");
         jobs.subscribe(luisa);
         jobs.subscribe(axel);
 
         for (int i = 0; i < 100; i++) {
             jobs.printStatus((i + 1) + ": I have no idea what I am doing!");
         }
 
         System.out.println();
        System.out.println("All done, shutting down."); // Just for you Yanik! :D
         System.exit(0);
     }
 
 }
