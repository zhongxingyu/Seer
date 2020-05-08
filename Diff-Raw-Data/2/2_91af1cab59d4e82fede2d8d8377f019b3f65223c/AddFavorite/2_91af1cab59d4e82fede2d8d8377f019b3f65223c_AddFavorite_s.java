 /*
 Copyright (c) 2005, Lucas Holt
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
   Redistributions of source code must retain the above copyright notice, this list of
   conditions and the following disclaimer.
 
   Redistributions in binary form must reproduce the above copyright notice, this
   list of conditions and the following disclaimer in the documentation and/or other
   materials provided with the distribution.
 
   Neither the name of the Just Journal nor the names of its contributors
   may be used to endorse or promote products derived from this software without
   specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 package com.justjournal.ctl;
 
 import com.justjournal.SQLHelper;
 import org.apache.log4j.Category;
 
 /**
  * Adds a favorite journal entry to the users list.
  * @author Lucas Holt
  * @version 1.0
  * @since 1.0
  *
  * Created:
  * Date: Dec 10, 2005
  * Time: 8:44:39 PM
  */
 public class AddFavorite extends Protected {
     private static Category log = Category.getInstance(AddFavorite.class.getName());
     protected int entryId;
 
     /**
      * Retrieves the current logged in user.
      * @return username as string
      */
     public String getMyLogin() {
         return this.currentLoginName();
     }
 
     /**
      * Retrieves the current entry id we are adding as a favorite
      * @return favorite entry id
      */
     public int getEntryId() {
         return entryId;
     }
 
     /**
      * Sets the entry id we wish to make a favorite.
      * @param entryId id to add to our favorites list
      */
     public void setEntryId(int entryId) {
         this.entryId = entryId;
     }
 
     /**
      * Performs the action to create a favorite in our data store
      * @return the view to display upon completion (SUCCESS OR ERROR)
      * @throws Exception
      */
     protected String insidePerform() throws Exception {
 
         if (log.isDebugEnabled())
             log.debug("insidePerform(): Attempting to add favorite");
 
          if (this.currentLoginId() < 1)
             addError("login", "The login timed out or is invalid.");
 
         if (!this.hasErrors()) {
             try {
                String sql = "addfavorite " + entryId + "," + this.currentLoginId() + ";";
                 int result = SQLHelper.executeNonQuery(sql);
 
                 if (result != 1)
                     addError("Add Favorite", "Error adding your favorite.  Perhaps its already a favorite?");
             }
             catch (Exception e) {
                 addError("Add Favorite", "Could not add the favorite.");
                 if (log.isDebugEnabled())
                     log.debug("insidePerform(): " + e.getMessage());
             }
         }
 
         if (this.hasErrors())
             return ERROR;
         else
             return SUCCESS;
     }
 
 }
