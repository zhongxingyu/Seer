 [+ autogen5 template +]
[+INCLUDE (string-append "licenses/" (get "License") ".tpl") \+]
 /* -*- Mode: C; indent-tabs-mode: t; c-basic-offset: 4; tab-width: 4 -*- */
 /*
  * [+MainClass+].java
 * Copyright (C) [+(shell "date +%Y")+] [+Author+] <[+Email+]>
  * 
[+INVOKE LICENSE-DESCRIPTION PFX=" * " PROGRAM=(get "Name") OWNER=(get "Author") \+]
 */
 
 class [+MainClass+] {
     public static void main(String[] args) {
         System.out.println("Hello World!");
     }
 }
