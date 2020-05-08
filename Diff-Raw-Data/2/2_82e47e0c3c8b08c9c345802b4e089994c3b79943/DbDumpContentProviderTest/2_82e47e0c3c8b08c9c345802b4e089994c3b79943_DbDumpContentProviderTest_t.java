 /**
  * Copyright (C) 2012  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.logic.databasebackup.impl;
 
 import javax.sql.DataSource;
 
 import org.jtalks.poulpe.logic.databasebackup.exceptions.FileDownloadException;
 import org.testng.annotations.Test;
 
 /**
  * FileDownloadService.performFileDownload() method during its running should:
  * <ol>
  * <li>Get a content by calling ContentProvider.getContent();</li>
  * <li>Push browser to download prepared content by calling FileDownloader.download().</li>
  * </ol>
  * 
  * @author Evgeny Surovtsev
  */
 public class DbDumpContentProviderTest {
 
    @Test(enabled = false)
     public final void performFileDownloadTest() throws FileDownloadException {
         DataSource dataSource = new org.springframework.jdbc.datasource.DriverManagerDataSource(
                 "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/p_poulpe?characterEncoding=UTF-8", "root", "root");
         DbDumpContentProvider contentProvider = new DbDumpContentProvider(dataSource);
         System.out.print(contentProvider.getContent());
     }
 
 }
