 /*
  * Copyright (c) 2010 Henrik Gustafsson <henrik.gustafsson@fnord.se>
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 package se.fnord.rt.client;
 

 public class RTCustomField {
     private final String name;
     private final String description;
     private final String type;
     private final String validation;
 
    public RTCustomField(final String name, final String description, final String type, final String validation) {
         this.name = name;
         this.description = description;
         this.type = type;
         this.validation = validation;
     }
 
     public String getName() {
         return name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public String getType() {
         return type;
     }
 
     public String getValidation() {
         return validation;
     }
 }
