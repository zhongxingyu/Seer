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
 import org.lafayette.server.domain.Role.Names;
 
 /**
  * Domain facade to obtain {@link Role roles} from persistent data storage.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public interface RoleFinder {
 
     /**
      * Find role by it's id.
      *
      * @param id id to find
     * @return {@code null} if no role found
      */
     Role find(final int id);
     /**
      * Find role by it's id.
      *
      * @param id id to find
     * @return {@code null} if no role found
      */
     Role find(final Integer id);
     /**
      * Find all roles belonging to an user id.
      *
      * @param userId of user to find roles for
      * @return never {@code null}, but maybe empty collection
      */
     Collection<Role> findByUserId(int userId);
     /**
      * Find all roles belonging to an user's login name.
      *
      * @param loginName of user to find roles for
      * @return never {@code null}, but maybe empty collection
      */
     Collection<Role> findByName(Names loginName);
     /**
      * Find all roles.
      *
      * @param limit maxim amount of roles
      * @param offset offset of roles
      * @return never {@code null}, but maybe empty collection
      */
     Collection<Role> findAll(int limit, int offset);
     /**
      * Insert {@link Role} into database.
      *
      * @param subject domain object to insert
      * @return primary key id of inserted record set
      */
     int insert(Role subject);
     /**
      * updates {@link Role} from database.
      *
      * @param subject domain object to update
      */
     void update(Role subject);
     /**
      * Deletes {@link Role} from database.
      *
      * @param subject domain object to delete
      */
     void delete(Role subject);
 }
