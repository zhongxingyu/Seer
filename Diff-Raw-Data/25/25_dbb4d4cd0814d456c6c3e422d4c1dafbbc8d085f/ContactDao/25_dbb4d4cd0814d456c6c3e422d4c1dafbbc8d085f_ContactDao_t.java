 package com.odessa.discmanagement.server.dao.impl;
 
 import com.odessa.discmanagement.server.dao.Dao;
 import com.odessa.discmanagement.server.dao.model.Contact;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
 import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
 import org.springframework.jdbc.core.namedparam.SqlParameterSource;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: ToRTiK
  * Date: 12.01.13
  */
 @Component
 public class ContactDao implements Dao<Contact> {
 
     private static final String DELETE_FROM_CONTACT_WHERE_ID_ID = "delete from contact where id=:id";
 
     NamedParameterJdbcTemplate jdbcTemplate;
 
     @Autowired
     @Resource(name="jdbcTemplate")
     public void setJdbcTemplate(NamedParameterJdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 
     @Override
     public Contact create(Contact contact) {
         String query = "insert into contact " +
                "values (:id, :firstName, :lastName, :address, :telephone, :email, :statusId )";
 
         int id = jdbcTemplate.queryForInt("select next value for cmstr_seq from dual", Collections.EMPTY_MAP);
         contact.setId(id);
 
         jdbcTemplate.update(query, new BeanPropertySqlParameterSource(contact));
 
         return contact;
     }
 
     @Override
     public Contact update(Contact contact) {
         jdbcTemplate.update("update contact set firstName=:firstName, lastName=:lastName, address=:address, " +
                             "telephone=:telephone, email=:email, statusId=:statusId where id=:id",
                             new BeanPropertySqlParameterSource(contact));
         return contact;
     }
 
     @Override
     public Contact find(Contact contact) {
        return jdbcTemplate.queryForObject("select id, firstName, lastName, address, telephone, email, statusId " +
                "from contact where id=:id", new BeanPropertySqlParameterSource(contact),
                new BeanPropertyRowMapper<Contact>(Contact.class));
     }
 
     @Override
     public Contact find(int id) {
        return jdbcTemplate.queryForObject("select id, firstName, lastName, address, telephone, email, statusId " +
                "from contact where id=:id", Collections.singletonMap("id", id),
                new BeanPropertyRowMapper<Contact>(Contact.class));
     }
 
     @Override
     public List<Contact> getAll() {
        return jdbcTemplate.query("select id, firstName, lastName, address, telephone, email, statusId " +
                "from contact", Collections.EMPTY_MAP, new BeanPropertyRowMapper(Contact.class));
     }
 
     @Override
     public void delete(Contact contact) {
         jdbcTemplate.update(DELETE_FROM_CONTACT_WHERE_ID_ID, new BeanPropertySqlParameterSource(contact));
     }
 
     @Override
     public void delete(int id) {
         jdbcTemplate.update(DELETE_FROM_CONTACT_WHERE_ID_ID,  Collections.singletonMap("id", id));
     }
 }
