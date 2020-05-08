 /*
     Copyright (c) 2013 Alessandro Coppo
     All rights reserved.
 
     Redistribution and use in source and binary forms, with or without
     modification, are permitted provided that the following conditions
     are met:
     1. Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
     2. Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
     3. The name of the author may not be used to endorse or promote products
        derived from this software without specific prior written permission.
 
     THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
     IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
     OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
     IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
     INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
     NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
     THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package it.webalice.alexcoppo.lrxl.expando;
 
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portlet.expando.model.ExpandoColumn;
 import com.liferay.portlet.expando.model.ExpandoTable;
 import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
 
 /**
  *
  */
 public class ExpandoColumnUtils {
     /**
      * 
      * @param et the expando table
      * @param columnName
      * @return
      * @throws SystemException 
      */
     public static boolean exists(ExpandoTable et, String columnName) throws SystemException {
         return ExpandoColumnLocalServiceUtil.getColumn(et.getCompanyId(), et.getClassName(), et.getName(), columnName) != null;
     }
     
     /**
      * 
      * @param et the expando table
      * @param columnName
      * @return
      * @throws SystemException 
      */
     public static ExpandoColumn get(ExpandoTable et, String columnName) throws SystemException {
         return ExpandoColumnLocalServiceUtil.getColumn(et.getCompanyId(), et.getClassName(), et.getName(), columnName);
     }
 
     /**
      * Create a column.
      *
      * @param et the expando table
      * @param columnName the name of the column
      * @param columnType the type of the column from ExpandoColumnConstants
      * @throws PortalException
      * @throws SystemException 
      */
    public static ExpandoColumn create(ExpandoTable et, String columnName, int columnType) throws SystemException, PortalException {
        return ExpandoColumnLocalServiceUtil.addColumn(et.getTableId(), columnName, columnType);
     }
     
     /**
      * Create a colum, if it already exists, drop it first.
      * 
      * @param et the expando table
      * @param columnName
      * @param columnType
      * @throws SystemException
      * @throws PortalException 
      */
    public static ExpandoColumn createForced(ExpandoTable et, String columnName, int columnType) throws SystemException, PortalException {
         if (exists(et, columnName))
             drop(et, columnName);
        return create(et, columnName, columnType);
     }
 
     /**
      * Create a column only if missing.
      * 
      * @param et the expando table
      * @param columnName the name of the column
      * @param columnType the type of the column from ExpandoColumnConstants
      * @throws SystemException
      * @throws PortalException 
      */
    public static ExpandoColumn createIfMissing(ExpandoTable et, String columnName, int columnType) throws SystemException, PortalException {
        ExpandoColumn ec = get(et, columnName);
        
        if (ec == null)
            ec = create(et, columnName, columnType);
        
        return ec;
     }
 
     /**
      * Drop a column.
      * 
      * @param et the expando table
      * @param columnName the name of the column
      * @throws SystemException 
      */
    public static void drop(ExpandoTable et, String columnName) throws SystemException {
         ExpandoColumnLocalServiceUtil.deleteColumn(et.getTableId(), columnName);
     }
 
     /**
      * Drop a column if exists.
      * 
      * @param et
      * @param columnName
      * @throws SystemException 
      */
    public static void dropSafe(ExpandoTable et, String columnName) throws SystemException {
         if (exists(et, columnName))
             ExpandoColumnLocalServiceUtil.deleteColumn(et.getTableId(), columnName);
     }
 }
