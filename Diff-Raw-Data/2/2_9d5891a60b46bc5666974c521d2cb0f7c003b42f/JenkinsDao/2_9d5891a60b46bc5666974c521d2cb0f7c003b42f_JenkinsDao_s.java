 package io.trygvis.esper.testing.jenkins;
 
 import fj.data.*;
 import io.trygvis.esper.testing.jenkins.xml.*;
 import io.trygvis.esper.testing.util.sql.*;
 import org.joda.time.*;
 
 import java.net.*;
 import java.sql.*;
 import java.util.List;
 import java.util.*;
 
 import static fj.data.Option.fromNull;
 import static io.trygvis.esper.testing.Util.*;
 import static io.trygvis.esper.testing.util.sql.ResultSetF.*;
 import static io.trygvis.esper.testing.util.sql.SqlOption.*;
 import static java.lang.System.*;
 
 public class JenkinsDao {
 
     private final Connection c;
 
     public static final String JENKINS_SERVER = "uuid, created_date, name, url, enabled";
 
     public static final String JENKINS_USER = "uuid, created_date, server, absolute_url";
 
     public JenkinsDao(Connection c) {
         this.c = c;
     }
 
     private SqlF<ResultSet,JenkinsServerDto> jenkinsServer = new SqlF<ResultSet, JenkinsServerDto>() {
         public JenkinsServerDto apply(ResultSet rs) throws SQLException {
             int i = 1;
             return new JenkinsServerDto(
                     UUID.fromString(rs.getString(i++)),
                     new DateTime(rs.getTimestamp(i++).getTime()),
                     rs.getString(i++),
                     URI.create(rs.getString(i++)),
                     rs.getBoolean(i));
         }
     };
 
     public static final String JENKINS_JOB = "uuid, created_date, server, file, url, job_type, display_name";
 
     public static final SqlF<ResultSet, JenkinsJobDto> jenkinsJob = new SqlF<ResultSet, JenkinsJobDto>() {
         public JenkinsJobDto apply(ResultSet rs) throws SQLException {
             int i = 1;
             return new JenkinsJobDto(
                     UUID.fromString(rs.getString(i++)),
                     new DateTime(rs.getTimestamp(i++).getTime()),
                     UUID.fromString(rs.getString(i++)),
                     UUID.fromString(rs.getString(i++)),
                     URI.create(rs.getString(i++)),
                     rs.getString(i++),
                     fromNull(rs.getString(i)));
         }
     };
 
     public static final String JENKINS_BUILD = "uuid, created_date, job, file, entry_id, url, users";
 
     public static final SqlF<ResultSet, JenkinsBuildDto> jenkinsBuild = new SqlF<ResultSet, JenkinsBuildDto>() {
         public JenkinsBuildDto apply(ResultSet rs) throws SQLException {
             int i = 1;
             return new JenkinsBuildDto(
                     UUID.fromString(rs.getString(i++)),
                     new DateTime(rs.getTimestamp(i++).getTime()),
                     UUID.fromString(rs.getString(i++)),
                     UUID.fromString(rs.getString(i++)),
                     rs.getString(i++),
                     URI.create(rs.getString(i++)),
                     toUuidArray(rs, i));
         }
     };
 
     public static final SqlF<ResultSet, JenkinsUserDto> jenkinsUser = new SqlF<ResultSet, JenkinsUserDto>() {
         public JenkinsUserDto apply(ResultSet rs) throws SQLException {
             int i = 1;
             return new JenkinsUserDto(
                     UUID.fromString(rs.getString(i++)),
                     new DateTime(rs.getTimestamp(i++).getTime()),
                     UUID.fromString(rs.getString(i++)),
                     rs.getString(i));
         }
     };
 
     // -----------------------------------------------------------------------
     // Server
     // -----------------------------------------------------------------------
 
     public List<JenkinsServerDto> selectServers(boolean enabledOnly) throws SQLException {
         String sql = "SELECT " + JENKINS_SERVER + " FROM jenkins_server";
 
         if (enabledOnly) {
             sql += " WHERE enabled=true";
         }
 
         sql += " ORDER BY url";
 
         try (PreparedStatement s = c.prepareStatement(sql)) {
             return toList(s, jenkinsServer);
         }
     }
 
     public SqlOption<JenkinsServerDto> selectServer(UUID uuid) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("SELECT " + JENKINS_SERVER + " FROM jenkins_server WHERE uuid=?")) {
             s.setString(1, uuid.toString());
             return fromRs(s.executeQuery()).map(jenkinsServer);
         }
     }
 
     // -----------------------------------------------------------------------
     // Job
     // -----------------------------------------------------------------------
 
     public SqlOption<JenkinsJobDto> selectJob(UUID uuid) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("SELECT " + JENKINS_JOB + " FROM jenkins_job WHERE uuid=?")) {
             s.setString(1, uuid.toString());
             return fromRs(s.executeQuery()).map(jenkinsJob);
         }
     }
 
     public List<JenkinsJobDto> selectJobsByServer(UUID server, PageRequest page) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("SELECT " + JENKINS_JOB + " FROM jenkins_job WHERE server=? ORDER BY created_date DESC LIMIT ? OFFSET ?")) {
             int i = 1;
             s.setString(i++, server.toString());
             s.setInt(i++, page.count.orSome(10));
             s.setInt(i, page.startIndex.orSome(0));
             return toList(s, jenkinsJob);
         }
     }
 
     public SqlOption<JenkinsJobDto> selectJobByUrl(URI url) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("SELECT " + JENKINS_JOB + " FROM jenkins_job WHERE url=?")) {
             s.setString(1, url.toASCIIString());
             return fromRs(s.executeQuery()).map(jenkinsJob);
         }
     }
 
     public int selectJobCountForServer(UUID uuid) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("SELECT count(1) FROM jenkins_job WHERE server=?")) {
             s.setString(1, uuid.toString());
             return fromRs(s.executeQuery()).map(getInt).get();
         }
     }
 
     public UUID insertJob(UUID server, UUID file, URI url, JenkinsJobXml.JenkinsJobType type, Option<String> displayName) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("INSERT INTO jenkins_job(" + JENKINS_JOB + ") VALUES(?, ?, ?, ?, ?, ?, ?)")) {
             UUID uuid = UUID.randomUUID();
             int i = 1;
             s.setString(i++, uuid.toString());
             s.setTimestamp(i++, new Timestamp(currentTimeMillis()));
             s.setString(i++, server.toString());
             s.setString(i++, file.toString());
             s.setString(i++, url.toASCIIString());
             s.setString(i++, type.name());
             s.setString(i, displayName.toNull());
             s.executeUpdate();
 
             return uuid;
         }
     }
 
     // -----------------------------------------------------------------------
     // Build
     // -----------------------------------------------------------------------
 
     public SqlOption<JenkinsBuildDto> selectBuildByEntryId(String id) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("SELECT " + JENKINS_BUILD + " FROM jenkins_build WHERE entry_id=?")) {
             int i = 1;
             s.setString(i, id);
             return fromRs(s.executeQuery()).map(jenkinsBuild);
         }
     }
 
     public List<JenkinsBuildDto> selectBuildByJob(UUID job, PageRequest page) throws SQLException {
         String sql = "SELECT " + JENKINS_BUILD + " FROM jenkins_build WHERE job=?";
        sql += orderBy(ifEmpty(page.orderBy, "created_date-"), "created_date", "timestamp");
         sql += " LIMIT ? OFFSET ?";
 
         try (PreparedStatement s = c.prepareStatement(sql)) {
             int i = 1;
             s.setString(i++, job.toString());
             s.setInt(i++, page.count.orSome(10));
             s.setInt(i, page.startIndex.orSome(0));
             return toList(s, jenkinsBuild);
         }
     }
 
     public SqlOption<JenkinsBuildDto> selectBuild(UUID uuid) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("SELECT " + JENKINS_BUILD + " FROM jenkins_build WHERE uuid=?")) {
             int i = 1;
             s.setString(i, uuid.toString());
             return fromRs(s.executeQuery()).map(jenkinsBuild);
         }
     }
 
     public int selectBuildCountByJob(UUID job) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("SELECT count(1) FROM jenkins_build WHERE job=?")) {
             int i = 1;
             s.setString(i, job.toString());
             return fromRs(s.executeQuery()).map(getInt).get();
         }
     }
 
     public UUID insertBuild(UUID job, UUID file, String entryId, URI url, UUID[] users) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("INSERT INTO jenkins_build(" + JENKINS_BUILD + ") VALUES(?, ?, ?, ?, ?, ?, ?)")) {
             UUID uuid = UUID.randomUUID();
             int i = 1;
             s.setString(i++, uuid.toString());
             s.setTimestamp(i++, new Timestamp(currentTimeMillis()));
             s.setString(i++, job.toString());
             s.setString(i++, file.toString());
             s.setString(i++, entryId);
             s.setString(i++, url.toASCIIString());
             s.setArray(i, c.createArrayOf("varchar", users));
             s.executeUpdate();
 
             return uuid;
         }
     }
 
     // -----------------------------------------------------------------------
     // User
     // -----------------------------------------------------------------------
 
     public UUID insertUser(UUID server, String absoluteUrl) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("INSERT INTO jenkins_user(" + JENKINS_USER + ") VALUES(?, ?, ?, ?)")) {
             UUID uuid = UUID.randomUUID();
             int i = 1;
             s.setString(i++, uuid.toString());
             s.setTimestamp(i++, new Timestamp(currentTimeMillis()));
             s.setString(i++, server.toString());
             s.setString(i, absoluteUrl);
             s.executeUpdate();
 
             return uuid;
         }
     }
 
     public SqlOption<JenkinsUserDto> selectUser(UUID uuid) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("SELECT " + JENKINS_USER + " FROM jenkins_user WHERE uuid=?")) {
             int i = 1;
             s.setString(i, uuid.toString());
             return fromRs(s.executeQuery()).map(jenkinsUser);
         }
     }
 
     public SqlOption<JenkinsUserDto> selectUserByAbsoluteUrl(UUID server, String absoluteUrl) throws SQLException {
         try (PreparedStatement s = c.prepareStatement("SELECT " + JENKINS_USER + " FROM jenkins_user WHERE server=? AND absolute_url=?")) {
             int i = 1;
             s.setString(i++, server.toString());
             s.setString(i, absoluteUrl);
             return fromRs(s.executeQuery()).map(jenkinsUser);
         }
     }
 }
