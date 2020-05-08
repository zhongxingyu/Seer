 /***************************************************************************
 *                                                                          *
 *  Organization: Lawrence Livermore National Lab (LLNL)                    *
 *   Directorate: Computation                                               *
 *    Department: Computing Applications and Research                       *
 *      Division: S&T Global Security                                       *
 *        Matrix: Atmospheric, Earth and Energy Division                    *
 *       Program: PCMDI                                                     *
 *       Project: Earth Systems Grid (ESG) Data Node Software Stack         *
 *  First Author: Gavin M. Bell (gavin@llnl.gov)                            *
 *                                                                          *
 ****************************************************************************
 *                                                                          *
 *   Copyright (c) 2009, Lawrence Livermore National Security, LLC.         *
 *   Produced at the Lawrence Livermore National Laboratory                 *
 *   Written by: Gavin M. Bell (gavin@llnl.gov)                             *
 *   LLNL-CODE-420962                                                       *
 *                                                                          *
 *   All rights reserved. This file is part of the:                         *
 *   Earth System Grid (ESG) Data Node Software Stack, Version 1.0          *
 *                                                                          *
 *   For details, see http://esgf.org/esg-node/                    *
 *   Please also read this link                                             *
 *    http://esgf.org/LICENSE                                      *
 *                                                                          *
 *   * Redistribution and use in source and binary forms, with or           *
 *   without modification, are permitted provided that the following        *
 *   conditions are met:                                                    *
 *                                                                          *
 *   * Redistributions of source code must retain the above copyright       *
 *   notice, this list of conditions and the disclaimer below.              *
 *                                                                          *
 *   * Redistributions in binary form must reproduce the above copyright    *
 *   notice, this list of conditions and the disclaimer (as noted below)    *
 *   in the documentation and/or other materials provided with the          *
 *   distribution.                                                          *
 *                                                                          *
 *   Neither the name of the LLNS/LLNL nor the names of its contributors    *
 *   may be used to endorse or promote products derived from this           *
 *   software without specific prior written permission.                    *
 *                                                                          *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS    *
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT      *
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS      *
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL LAWRENCE    *
 *   LIVERMORE NATIONAL SECURITY, LLC, THE U.S. DEPARTMENT OF ENERGY OR     *
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,           *
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT       *
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF       *
 *   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND    *
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,     *
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT     *
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF     *
 *   SUCH DAMAGE.                                                           *
 *                                                                          *
 ***************************************************************************/
 
 /**
    Description:
    Perform sql query to log visitors' access to data files
    
 **/
 package esg.node.filters;
 
 import java.io.Serializable;
 import java.util.Properties;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.sql.DataSource;
 
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.ResultSetHandler;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 public class AccessLoggingDAO implements Serializable {
 
     //TODO figure out what these queries should be!
     private static final String accessLoggingIngressQuery = 
         "insert into access_logging (id, user_id, email, url, file_id, remote_addr, user_agent, service_type, batch_update_time, date_fetched, success) "+
         "values ( nextval('seq_access_logging'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
     private static final String accessLoggingEgressQuery = 
         "update access_logging set success = ?, duration = ? "+
         "where user_id = ?, url = ?, file_id = ?, remote_addr = ?, date_fetched = ?";
     
     private static final Log log = LogFactory.getLog(AccessLoggingDAO.class);
     
     private DataSource dataSource = null;
     private QueryRunner queryRunner = null;
     
     public AccessLoggingDAO(DataSource dataSource) {
         this.setDataSource(dataSource);
     }
     
     //Not preferred constructor but here for serialization requirement.
     public AccessLoggingDAO() { this(null); }
 
     //Initialize result set handlers...
     public void init() { }
     
     public void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
         this.queryRunner = new QueryRunner(dataSource);
     }
     
     //TODO: put in args and setup query!!!
     public int logIngressInfo(String userID,  
                               String email, 
                               String url, 
                               String fileID, 
                               String remoteAddress, 
                               String userAgent, 
                               String serviceType, 
                               long batchUpdateTime,
                               long dateFetched) {
         int ret = -1;
         try{
             //TODO: Perhaps the url can be used to resolve the dataset???
             //That is the bit of information we really want to also have.
             //What we really need is an absolute id for a file!!!
             ret = queryRunner.update(accessLoggingIngressQuery,
                                      userID,email,url,fileID,remoteAddress,userAgent,serviceType,batchUpdateTime,dateFetched,false);
         }catch(SQLException ex) {
             log.error(ex);
         }
         return ret;
     }
     
     //Upon egress update the ingress record with additional
     //information that can only be obtained on egress of the filter
     //this information is duration (though I am skeptical that it
     //means what Nate things it means) and success.  the identifying
     //tuple of information is the same as the ingress log fields.
     //Once the record is uniquely identified then the egress
     //information (the last pair) can be updated.
     public int logEgressInfo(String userID, 
                              String url, 
                              String fileID, 
                              String remoteAddress, 
                              long dateFetched, 
                              boolean success, long duration) {
         int ret = -1;
         try {
             ret = queryRunner.update(accessLoggingEgressQuery,
                                      success,duration,
                                      userID,url,remoteAddress,fileID,dateFetched);
         }catch(SQLException ex) {
             log.error(ex);
         }
         return ret;
     }
     
     public String toString() {
         return "DAO:(1)["+this.getClass().getName()+"] - [Q:"+accessLoggingIngressQuery+"] "+((dataSource == null) ? "[OK]\n" : "[INVALID]\n")+
             "DAO:(2)["+this.getClass().getName()+"] - [Q:"+accessLoggingEgressQuery+"]  "+((dataSource == null) ? "[OK]\n" : "[INVALID]\n");
     }
 
}
