 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.db.migration;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.IncorrectResultSizeDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowCallbackHandler;
 import org.springframework.util.StringUtils;
 
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Optional;
 import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;
 
 public class V1_9_0_2__LogEventDomainIds implements SpringJdbcMigration {
 
   private Logger logger = LoggerFactory.getLogger(V1_9_0_2__LogEventDomainIds.class);
 
   @Override
   public void migrate(final JdbcTemplate jdbcTemplate) throws Exception {
     final Pattern bodPortIdPattern = Pattern.compile("bodPortId=(.+?),");
     final Pattern vprlUuidPattern = Pattern.compile("^VirtualPortRequestLink \\[uuid=(.+?),");
     final Pattern vpManagerLabelPattern = Pattern.compile("^VirtualPort \\[managerLabel=(.+?),");
     final Pattern reservationCreationTimePattern = Pattern.compile("creationDateTime=(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{2,3})");
 
     jdbcTemplate.query("select * from log_event where domain_object_id is null and domain_object_class is not null", new RowCallbackHandler() {
 
       @Override
       public void processRow(ResultSet rs) throws SQLException {
         String serializedObject = rs.getString("serialized_object");
         String objectClass = rs.getString("domain_object_class");
         Long eventLogId = rs.getLong("id");
 
         Pattern idPattern = Pattern.compile("^"+objectClass+" \\[id=(\\d+),");
         Matcher matcher = idPattern.matcher(serializedObject);
 
         Optional<Long> id;
         if (matcher.find()) {
           id = Optional.of(Long.valueOf(matcher.group(1)));
        } else {
           switch (objectClass) {
           case "PhysicalPort":
             id = findId(serializedObject, bodPortIdPattern, "select id from physical_port where bod_port_id = ?", jdbcTemplate, Functions.<String>identity());
             break;
           case "VirtualPortRequestLink":
             id = findId(serializedObject, vprlUuidPattern, "select id from virtual_port_request_link where uuid = ?", jdbcTemplate, Functions.<String>identity());
             break;
           case "VirtualPort":
             id = findId(serializedObject, vpManagerLabelPattern, "select id from virtual_port where manager_label = ?", jdbcTemplate, Functions.<String>identity());
             break;
           case "Reservation":
             id = findId(serializedObject, reservationCreationTimePattern, "select id from reservation where cast(creation_date_time as TEXT) like ?", jdbcTemplate,
                 new Function<String, String>() {
                   @Override
                   public String apply(String input) {
                      String result = input.replace('T', ' ');
                      result = StringUtils.trimTrailingCharacter(result, '0');
 
                      return result.concat("%");
                   }
                 });
             break;
           case "ReservationArchive":
             id = Optional.absent();
             break;
           default:
             logger.error("Could not find patter: {}", serializedObject);
             throw new AssertionError("Could not find pattern");
           }
         }
 
         if (id.isPresent()) {
           jdbcTemplate.update("update log_event set domain_object_id = ? where id = ?", id.get(), eventLogId);
         }
       }
     });
   }
 
   private Optional<Long> findId(String serializedObject, Pattern pattern, String query, JdbcTemplate jdbcTemplate, Function<String, String> function) {
     Matcher matcher = pattern.matcher(serializedObject);
     if (matcher.find()) {
       String arg = function.apply(matcher.group(1));
       try {
        return Optional.of(jdbcTemplate.queryForObject(query, Long.class, arg));
       } catch (IncorrectResultSizeDataAccessException e) {
         if (e.getActualSize() == 0) {
           logger.error("Object deleted for arg '{}' in {}", arg, serializedObject);
           return Optional.absent();
        } else {
           logger.error("Multiple results for arg '{}' in {}", arg, serializedObject);
           return Optional.absent();
         }
       }
     } else {
       throw new AssertionError("Could not find pattern " + pattern.pattern() + " in " + serializedObject);
     }
   }
 
 }
