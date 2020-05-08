 /*
  * Copyright (c) 2013 Lucas Holt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 
 package com.justjournal.ctl.api;
 
 import com.justjournal.core.Statistics;
 import com.justjournal.db.CommentDao;
 import com.justjournal.db.EntryDAO;
 import com.sun.istack.internal.Nullable;
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Generate Global and per user statistics on blogging entries and comments.
  * @author Lucas Holt
  */
 @Controller
 @RequestMapping("/api/statistics")
 final public class StatisticsController {
     private static final Logger log = Logger.getLogger(StatisticsController.class);
 
     /**
      * Get Site statistics
      * @return statistics
      */
    @RequestMapping(method = RequestMethod.GET, headers="Accept=*/*", produces = "application/json")
     public
     @ResponseBody
     Statistics get() {
         return new Statistics();
     }
 
     /**
      * Get statistics for a specific user
      * @param id username
      * @param response Servlet response
      * @return stats
      */
     @RequestMapping("/api/statistics/{id}")
     @ResponseBody
     @Nullable
     public Stats getById(@PathVariable String id, HttpServletResponse response) {
         try {
             return new Stats(id);
         } catch (Exception e) {
             log.warn("Statistics User not found " + id);
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
             return null;
         }
     }
 
     /**
      * Individual statistics for user
      */
     class Stats {
         private String username;
 
         /**
          * Create Stats
          * @param username user's username
          */
         public Stats(String username) {
             this.username = username;
         }
 
         /**
          * Total number of entries
          * @return entry count
          * @throws Exception
          */
         public int getEntryCount() throws Exception {
             return EntryDAO.entryCount(username);
         }
 
         /**
          * Total number of comments
          * @return comment count
          * @throws Exception
          */
         public int getCommentCount() throws Exception {
             return CommentDao.commentCount(username);
         }
     }
 }
