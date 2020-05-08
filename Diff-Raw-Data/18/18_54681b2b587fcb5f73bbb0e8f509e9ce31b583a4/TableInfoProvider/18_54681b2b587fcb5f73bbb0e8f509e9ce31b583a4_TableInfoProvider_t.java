 /*
  * This is a common dao with basic CRUD operations and is not limited to any
  * persistent layer implementation
  *
  * Copyright (C) 2008  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.dao.impl.hbase.spi;
 
import com.smartitengineering.dao.common.queryparam.QueryParameter;
 import java.util.Map;
 import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
 
 /**
  * An class to provide {@link Result}'s
  * @author imyousuf
  */
 public interface TableInfoProvider {
 
   /**
    * A Generic configuragtion on whether the writes are to be performed in async
    * manner or not.
    * @return True if transactional else false
    */
   boolean isTransactionalDomain();
 
   /**
    * Given an object provide the the classes it converts with their respective
    * table names.
    * @param domainInstance Domain object to check for.
   * @return Domain representaitons mapped by HBase table names
    */
  Map<byte[], Class> getTableNames(Object domainInstance);

  /**
   * Convert query parameters for a particular domain to HBase filter to be used
   * by {@link Scan}.
   * @param parameters Query parameters to pass to {@link Scan}
   * @return Return HBase filter equivalent to parameters
   */
  Filter convertToHBaseFilter(QueryParameter...parameters);
 }
