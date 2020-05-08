 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 
 package org.lafayette.server.config;
 
 import java.io.File;
 
 /**
  * Defines a strategy to find the server configuration file.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 interface LoaderStrategie {
 
     /**
      * Whether the loader has found a configuration file or not.
      *
      * Call this method after {@link #findConfig()} to check if there is a file available.
      *
      * @return
      */
    boolean hasFoundConfig();
     /**
      * Get the found configuration file.
      *
      * @return may return {@code null}
      */
    File getFoundConfig();
     /**
      * Starts the search strategy to find a configuration file.
      */
    void findConfig();
 
 }
