 /*
  * Copyright 2013 Dr Daniel R Naylor.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package uk.co.drnaylor.moneysigns;
 
 public abstract class Util {
     
    /**
    * Converts a timestamp delta to a readable duration
     * 
     * @param timestamp UNIX Timestamp delta to convert
     * @return String with the parsed duration
     */
     public static String toDuration(long timestamp) {
         long t = timestamp;
         long seconds = t % 60;
         t = t / 60;
         long minutes = t % 60;
         t = t / 60;
         long hours = t % 24;
         long days = t / 24;
         
         StringBuilder s = new StringBuilder();
         
         s.append(days);
         s.append(" days, ");
         s.append(hours);
         s.append(" hours, ");
         s.append(minutes);
         s.append(" minutes, ");
         s.append(seconds);
         s.append(" seconds");
         
         return s.toString();
     }
 }
