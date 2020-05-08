 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Web Questionnaires 2
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Anton Dmitrijev
  */
 package eionet.webq.dao;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.Collection;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
 import org.springframework.jdbc.support.lob.LobCreator;
 import org.springframework.jdbc.support.lob.LobHandler;
 import org.springframework.stereotype.Repository;
 
 import eionet.webq.dto.UploadedXmlFile;
 
 /**
  * {@link FileStorage} implementation.
  */
 @Repository
 public class FileStorageImpl implements FileStorage {
     /**
      * {@link JdbcTemplate} to perform data access operations.
      */
     @Autowired
     private JdbcTemplate jdbcTemplate;
     /**
      * Large objects handles. Used for storing and retrieving {@link java.sql.Blob} object from database.
      */
     @Autowired
     private LobHandler lobHandler;
 
     @Override
     public void save(final UploadedXmlFile file, final String userId) {
         jdbcTemplate.execute(
                 "INSERT INTO user_xml(user_id, filename, xml_schema, xml, file_size_in_bytes) VALUES(?, ?, ?, ?, ?)",
                 new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                     @Override
                     protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                         ps.setString(1, userId);
                         ps.setString(2, file.getName());
                         ps.setString(3, file.getXmlSchema());
                         lobCreator.setBlobAsBytes(ps, 4, file.getContent());
                         ps.setLong(5, file.getSizeInBytes());
                     }
                 });
     }
 
     @Override
     public UploadedXmlFile getById(int id, String userId) {
         Object[] params = {id, userId};
         return jdbcTemplate.queryForObject("SELECT filename, xml FROM user_xml WHERE id = ? AND user_id = ?", params,
                 new RowMapper<UploadedXmlFile>() {
                     @Override
                     public UploadedXmlFile mapRow(ResultSet rs, int rowNum) throws SQLException {
                         return new UploadedXmlFile().setName(rs.getString(1)).setContent(lobHandler.getBlobAsBytes(rs, 2));
                     }
                 });
     }
 
     @Override
     public Collection<UploadedXmlFile> allUploadedFiles(String userId) {
        return jdbcTemplate.query("SELECT id, filename, file_size_in_bytes, created, updated FROM user_xml WHERE user_id = ? " +
                "ORDER BY updated DESC",
                 new Object[] {userId}, new RowMapper<UploadedXmlFile>() {
                     @Override
                     public UploadedXmlFile mapRow(ResultSet rs, int rowNum) throws SQLException {
                         return new UploadedXmlFile().setId(rs.getInt(1)).setName(rs.getString(2))
                                 .setSizeInBytes(rs.getLong(3)).setCreated(rs.getTimestamp(4)).setUpdated(rs.getTimestamp(5));
                     }
                 });
     }
 
     @Override
     public void updateContent(final UploadedXmlFile file, final String userId) {
         jdbcTemplate.execute("UPDATE user_xml SET xml = ?, file_size_in_bytes = ?, updated = ? WHERE id = ? and user_id= ?",
                 new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                     @Override
                     protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                         lobCreator.setBlobAsBytes(ps, 1, file.getContent());
                         ps.setLong(2, file.getSizeInBytes());
                         ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                         ps.setInt(4, file.getId());
                         ps.setString(5, userId);
                     }
                 });
     }
 }
