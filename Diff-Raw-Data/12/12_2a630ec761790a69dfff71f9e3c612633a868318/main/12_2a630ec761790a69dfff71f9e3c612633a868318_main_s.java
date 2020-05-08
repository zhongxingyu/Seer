 [+ autogen5 template +]
 /* -*- Mode: C; indent-tabs-mode: t; c-basic-offset: 4; tab-width: 4 -*- */
 /*
  * [+MainClass+].java
 * Copyright (C) [+Author+] [+(shell "date +%Y")+] <[+Email+]>
  * 
[+CASE (get "License") +]
[+ == "BSD"  +][+(bsd  (get "Name") (get "Author") " * ")+]
[+ == "LGPL" +][+(lgpl (get "Name") (get "Author") " * ")+]
[+ == "GPL"  +][+(gpl  (get "Name")                " * ")+]
[+ESAC+] */
 
 class [+MainClass+] {
     public static void main(String[] args) {
         System.out.println("Hello World!");
     }
 }
