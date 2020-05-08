 /*
  * The MIT License
  * 
  * Copyright (c) 2011, Harald Wellmann
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
 package com.googlecode.refit.jenkins;
 
 import hudson.model.AbstractBuild;
 import hudson.model.Hudson;
 import hudson.tasks.test.TestResult;
 
 import java.awt.Color;
 
 /**
 * Derived from <code>hudson.tasks.junit.History.ChartLabel</code>, which is not public.
  * @author Harald Wellmann
  *
  */
 public class ChartLabel implements Comparable<ChartLabel> {
     private TestResult result;
     private String url;
 
     public ChartLabel(TestResult o) {
         this.result = o;
         this.url = null;
     }
 
     public String getUrl() {
         if (this.url == null)
             generateUrl();
         return url;
     }
 
     private void generateUrl() {
         AbstractBuild<?, ?> build = result.getOwner();
         String buildLink = build.getUrl();
         String actionUrl = result.getTestResultAction().getUrlName();
         this.url = Hudson.getInstance().getRootUrl() + buildLink + actionUrl + result.getUrl();
     }
 
     public int compareTo(ChartLabel that) {
         return this.result.getOwner().number - that.result.getOwner().number;
     }
 
     @Override
     public boolean equals(Object o) {
         if (!(o instanceof ChartLabel)) {
             return false;
         }
         ChartLabel that = (ChartLabel) o;
         return this.result == that.result;
     }
 
     public Color getColor() {
         return null;
     }
     
     public TestResult getResult() {
         return result;
     }
 
     @Override
     public int hashCode() {
         return result.hashCode();
     }
 
     @Override
     public String toString() {
         String l = result.getOwner().getDisplayName();
         String s = result.getOwner().getBuiltOnStr();
         if (s != null)
             l += ' ' + s;
         return l;
     }
 }
