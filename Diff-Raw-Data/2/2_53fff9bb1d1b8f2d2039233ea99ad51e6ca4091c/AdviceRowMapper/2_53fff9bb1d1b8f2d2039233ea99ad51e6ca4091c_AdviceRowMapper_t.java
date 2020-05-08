 package com.forum.repository;
 
 import com.forum.domain.Advice;
 import com.forum.domain.User;
 import com.forum.util.BooleanToInteger;
 import org.springframework.jdbc.core.RowMapper;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 
 public class AdviceRowMapper implements RowMapper {
     BooleanToInteger converter=new BooleanToInteger();
     @Override
     public Advice mapRow(ResultSet resultSet, int i) throws SQLException {
         User user = new User(
                 resultSet.getString("USERNAME"),
                 resultSet.getString("PASSWORD"),
                 resultSet.getString("NAME"),
                 resultSet.getString("EMAIL_ADDRESS"),
                 resultSet.getString("PHONE_NUMBER"),
                 resultSet.getString("COUNTRY"),
                 resultSet.getString("GENDER"),
                 resultSet.getInt("AGE_RANGE"),
                 converter.toBoolean(resultSet.getInt("PRIVACY")));
                 user.setId(resultSet.getInt("USER_ID"));
         Advice advice = new Advice(resultSet.getInt("QUESTION_ID"),user,resultSet.getString("DESCRIPTION"));
        advice.setCreatedAt(resultSet.getTimestamp("CREATED_AT"));
         advice.setId(resultSet.getInt("ID"));
       return advice;
     }
 }
