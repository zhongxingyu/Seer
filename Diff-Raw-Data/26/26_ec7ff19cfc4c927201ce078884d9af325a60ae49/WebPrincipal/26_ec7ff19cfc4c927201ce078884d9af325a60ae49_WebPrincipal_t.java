 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_web;
 
 import java.security.Principal;
 import java.util.Objects;
 
 /**
  * A basic principal implementation for the Web UI.
  */
 public class WebPrincipal implements Principal {
 
     /** The username of the principal. */
     private final String name;
 
     public WebPrincipal(final String name) {
         this.name = name;
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 67 * hash + Objects.hashCode(this.name);
         return hash;
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final WebPrincipal other = (WebPrincipal) obj;
        return Objects.equals(this.name, other.name);
     }
 
     @Override
     public String toString() {
         return "WebPrincipal{" + "name=" + name + '}';
     }
 
 }
