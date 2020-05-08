 /*
  * Copyright (C) 2013 Michael Koppen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.fhb.mi.paperfly.dto;
 
import java.util.Set;
 
 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;
 import lombok.ToString;
 
 /**
  * @author Michael Koppen <michael.koppen@googlemail.com>
  */
 @Getter
 @Setter
 @ToString
 @EqualsAndHashCode(callSuper = false)
 @NoArgsConstructor
 public class AccountDTO extends BaseDTO {
 
     private String email;
     private String username;
     private String lastName;
     private String firstName;
     private Status status;
    private Set<String> friendListUsernames;
 
     public AccountDTO(final AccountDTO account) {
         this.setEmail(account.getEmail());
         this.setUsername(account.getUsername());
         this.setLastName(account.getLastName());
         this.setFirstName(account.getFirstName());
         this.setFriendListUsernames(account.getFriendListUsernames());
         this.setCreated(account.getCreated());
         this.setLastModified(account.getLastModified());
         this.setStatus(account.getStatus());
         this.setEnabled(account.isEnabled());
     }
 }
