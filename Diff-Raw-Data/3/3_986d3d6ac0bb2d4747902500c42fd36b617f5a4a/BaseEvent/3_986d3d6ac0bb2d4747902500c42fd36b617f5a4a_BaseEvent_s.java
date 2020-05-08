 /**
  *
  * Copyright (c) 2013, Linagora
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
  *
  */
 package models;
 
 import play.db.jpa.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.PostPersist;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author chamerling
  * 
  */
 @Entity
 public class BaseEvent extends Model {
 
 	public Date date;
 
 	public String message;
 
 	public String type;
 
     /**
      * Emit the event to the client?
      */
     public boolean emit = true;
 
 	public BaseEvent(String message, String type) {
 		this.message = message;
 		this.type = type;
 		this.date = new Date();
 	}
 
 	public static List<BaseEvent> pasts() {
 		return BaseEvent.find("date < ? order by date desc", new Date())
 				.fetch();
 	}
 
 	public static BaseEvent event(String type, String pattern, Object... params) {
 		return new BaseEvent(String.format(pattern, params), type);
 	}
 
     /**
      * Post registration callback. Notifies the user about a new event
      */
     @PostPersist
     public void notifyUser() {
         //ApplicationEvent.live(this.message);
     }
 }
