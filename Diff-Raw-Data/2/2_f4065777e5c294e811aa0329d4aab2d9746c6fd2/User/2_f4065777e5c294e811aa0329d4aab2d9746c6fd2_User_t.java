 /*
  *
  * Copyright (C) 2011 SW 11 Inc.
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package models;
 
 import play.*;
 import play.db.jpa.*;
 
 import java.util.*;
 import javax.persistence.*;
 
 @Entity
 public class User extends Model
 {
 
     public String email_;
     public String password_;
     public String firstname_;
     public String lastname_;
     public String question_;
     public String answer_;
     public boolean admin_;
 
     public User(String email_, String password_, String firstname_, String lastname_, String question_, String answer_)
     {
         this.email_ = email_;
         this.password_ = password_;
         this.firstname_ = firstname_;
         this.lastname_ = lastname_;
         this.question_ = question_;
         this.answer_ = answer_;
 
         if (User.count() == 0)
         {
             this.admin_ = true;
         }
         else
         {
            this.admin_ = false;
         }
     }
 
     public User()
     {
     }
 
     public void register(String email, String password, String firstname, String lastname, String question, String answer)
     {
         this.email_ = email;
         this.password_ = password;
         this.firstname_ = firstname;
         this.lastname_ = lastname;
         this.question_ = question;
         this.answer_ = answer;
         this.admin_ = false;
     }
 
     public static User connect(String email, String password)
     {
         return find("byEmail_AndPassword_", email, password).first();
 
     }
 }
