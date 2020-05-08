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
 
 package org.lafayette.server.domain;
 
 import java.util.Collection;
 
 /**
 * Domain facade to obtain {@link USer users} from persistent data storage.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public interface UserFinder {
 
     /**
      * Find user by it's id.
      *
      * @param id id to find
     * @return {@cde null} if no user found
      */
     User find(final int id);
     /**
      * Find user by it's id.
      *
      * @param id id to find
     * @return {@cde null} if no user found
      */
     User find(final Integer id);
     /**
      * Find a user by the login name.
      *
      * @param loginName name to find
     * @return {@cde null} if no user found
      */
     User findByLoginName(String loginName);
     /**
      * Find all users.
      *
      * @param limit maxim amount of users
      * @param offset offset of roles
      * @return never {@code null}, but maybe empty collection
      */
     Collection<User> findAll(int limit, int offset);
     /**
      * Insert {@link User} into database.
      *
      * @param subject domain object to insert
      * @return primary key id of inserted record set
      */
     int insert(User subject);
     /**
      * updates {@link User} from database.
      *
      * @param subject domain object to update
      */
     void update(User subject);
     /**
      * Deletes {@link User} from database.
      *
      * @param subject domain object to delete
      */
     void delete(User subject);
 
 }
