 /**
  * Copyright (c) 2012, Sini
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy 
  * of this software and associated documentation files (the "Software"), to deal 
  * in the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in 
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
  * THE SOFTWARE.
  */
 
 package net.votefucker.nkvoter;
 
 
 public final class Version {
     
     /**
      * The major version.
      */
     private final int major;
     
     /**
      * The minor version.
      */
     private final int minor;
     
     /**
      * The sub-minor version.
      */
     private final int subMinor;
     
     /**
      * Constructs a new {@link Version};
      * 
      * @param major     The major version.
      * @param minor     The minor version.
      * @param subMinor  The sub-minor version.
      */
     public Version(int major, int minor, int subMinor) {
         this.major = major;
         this.minor = minor;
         this.subMinor = subMinor;
     }
     
     /**
      * Gets the major version.
      * 
      * @return  The major version.
      */
     public int getMajor() {
         return major;
     }
     
     /**
      * Gets the minor version.
      * 
      * @return  The minor version.
      */
     public int getMinor() {
         return minor;
     }
 
     /**
      * Gets the sub-minor version.
      * 
      * @return  The sub-minor version.
      */
     public int getSubMinor() {
         return subMinor;
     }
     
     @Override
     public String toString() {
        return "Version " + major + "." + minor + "." + subMinor;
     }
 }
