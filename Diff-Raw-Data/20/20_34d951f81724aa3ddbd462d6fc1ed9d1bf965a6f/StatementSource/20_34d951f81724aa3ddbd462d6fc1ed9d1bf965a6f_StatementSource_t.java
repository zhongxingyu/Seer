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
 
 package org.lafayette.server.mapper;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 /**
  * Defines a prepared statement with it's parameters.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 interface StatementSource {
 
     /**
      * Returns the statement SQL string.
      *
      * @return never {@code null}
      */
     String sql();
     /**
      * Returns parameter objects.
      *
      * @return never {@code null}, maybe empty
      */
     Object[] parameters();
 
     /**
      * Prepares a statement.
      *
      * This method get the {@link #sql() SQL} and if the {@link #parameters() parameters}
      * are not empty, sets them on the statement.
      *
      * @param db used to get prepared statement
     * @return never {@code null}
      * @throws SQLException if any SQL syntax error occurs.
      */
     PreparedStatement prepare(Connection db) throws SQLException;
 
 }
