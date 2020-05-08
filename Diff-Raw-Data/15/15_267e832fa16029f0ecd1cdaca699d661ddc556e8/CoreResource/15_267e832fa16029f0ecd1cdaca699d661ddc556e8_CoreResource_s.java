 package io.trygvis.esper.testing.web.resource;
 
 import fj.data.*;
 import io.trygvis.esper.testing.*;
 import io.trygvis.esper.testing.core.badge.*;
 import io.trygvis.esper.testing.core.db.*;
 import io.trygvis.esper.testing.util.sql.*;
 import io.trygvis.esper.testing.web.*;
 import org.joda.time.*;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.*;
 import java.sql.*;
 import java.util.*;
 import java.util.List;
 
 import static fj.data.Option.fromNull;
 
 @Path("/resource/core")
 @Produces(MediaType.APPLICATION_JSON)
 public class CoreResource extends AbstractResource {
 
     private final BadgeService badgeService;
 
     public CoreResource(DatabaseAccess da, BadgeService badgeService) {
         super(da);
         this.badgeService = badgeService;
     }
 
     // -----------------------------------------------------------------------
     // Person
     // -----------------------------------------------------------------------
 
     @GET
     @Path("/person")
     public List<PersonDetailJson> getPersons(@MagicParam final PageRequest pageRequest,
                                              @QueryParam("query") final String query) throws Exception {
         return da.inTransaction(new CoreDaosCallback<List<PersonDetailJson>>() {
             protected List<PersonDetailJson> run() throws SQLException {
                 List<PersonDetailJson> list = new ArrayList<>();
                 for (PersonDto person : daos.personDao.selectPersons(pageRequest, fromNull(query))) {
                     list.add(super.getPersonDetailJson.apply(person));
                 }
                 return list;
             }
         });
     }
 
     @GET
     @Path("/person/{uuid}")
     public PersonDetailJson getPerson(@MagicParam final Uuid uuid) throws Exception {
         return sql(new CoreDaosCallback<SqlOption<PersonDetailJson>>() {
             protected SqlOption<PersonDetailJson> run() throws SQLException {
                 return daos.personDao.selectPerson(uuid).map(super.getPersonDetailJson);
             }
         });
     }
 
     // -----------------------------------------------------------------------
     // Build
     // -----------------------------------------------------------------------
 
     @GET
     @Path("/build")
     public List<Object> getBuilds(@MagicParam final PageRequest page,
                                   @MagicParam(query = "person") final Uuid person,
                                   @QueryParam("fields") final List<String> fields) throws Exception {
         return da.inTransaction(new CoreDaosCallback<List<Object>>() {
             public List<Object> run() throws SQLException {
                 List<BuildDto> buildDtos;
 
                 boolean detailed = fields.contains("detailed");
 
                 if (person != null) {
                     buildDtos = daos.buildDao.selectBuildsByPerson(person, page);
                 } else {
                     buildDtos = daos.buildDao.selectBuilds(page);
                 }
 
                 List<Object> list = new ArrayList<>();
 
                 SqlF<BuildDto, ?> buildDtoSqlF = detailed ? getBuildDetailJson : getBuildJson;
 
                 for (BuildDto build : buildDtos) {
                     list.add(buildDtoSqlF.apply(build));
                 }
                 return list;
             }
         });
     }
 
     @GET
     @Path("/build-participant/{uuid}")
     public List<PersonDetailJson> getBuildParticipants(@MagicParam final UUID build) throws Exception {
         return da.inTransaction(new CoreDaosCallback<List<PersonDetailJson>>() {
             protected List<PersonDetailJson> run() throws SQLException {
                 List<PersonDetailJson> list = new ArrayList<>();
                 for (PersonDto person : daos.buildDao.selectPersonsFromBuildParticipant(build)) {
                     list.add(super.getPersonDetailJson.apply(person));
                 }
                 return list;
             }
         });
     }
 
     @GET
     @Path("/build/{uuid}")
     public BuildDetailJson getBuild(@MagicParam final UUID uuid) throws Exception {
         return sql(new CoreDaosCallback<SqlOption<BuildDetailJson>>() {
             public SqlOption<BuildDetailJson> run() throws SQLException {
                 return daos.buildDao.selectBuild(uuid).map(getBuildDetailJson);
             }
         });
     }
 
     // -----------------------------------------------------------------------
     // Badge
     // -----------------------------------------------------------------------
 
     /**
      * This shouldn't return a detailed list by default.
      */
     @GET
     @Path("/badge")
     public List<BadgeDetailJson> getBadges(@MagicParam final PageRequest page, @MagicParam(query = "person") final Uuid person) throws Exception {
         return da.inTransaction(new CoreDaosCallback<List<BadgeDetailJson>>() {
             protected List<BadgeDetailJson> run() throws SQLException {
                 List<PersonalBadgeDto> badgeDtos = daos.personDao.selectBadges(fromNull(person), Option.<PersonalBadgeDto.BadgeType>none(), Option.<Integer>none(), page);
 
                 List<BadgeDetailJson> list = new ArrayList<>();
                 for (PersonalBadgeDto badge : badgeDtos) {
                     list.add(getBadgeDetailJson.apply(badge));
                 }
                 return list;
             }
         });
     }
 
     @GET
     @Path("/badge/{uuid}")
     public BadgeDetailJson getBadges(@MagicParam final Uuid uuid) throws Exception {
         return sql(new CoreDaosCallback<SqlOption<BadgeDetailJson>>() {
             protected SqlOption<BadgeDetailJson> run() throws SQLException {
                 return daos.personDao.selectBadge(uuid).map(getBadgeDetailJson);
             }
         });
     }
 
     SqlF<PersonalBadgeDto, PersonalBadge> badge = new SqlF<PersonalBadgeDto, PersonalBadge>() {
         public PersonalBadge apply(PersonalBadgeDto dto) throws SQLException {
             return badgeService.badge(dto);
         }
     };
 
     abstract class CoreDaosCallback<T> implements DatabaseAccess.DaosCallback<T> {
         protected Daos daos;
 
         protected abstract T run() throws SQLException;
 
         public T run(Daos daos) throws SQLException {
             this.daos = daos;
             return run();
         }
 
         // -----------------------------------------------------------------------
         // Person
         // -----------------------------------------------------------------------
 
         protected final SqlF<PersonDto, PersonJson> getPersonJson = new SqlF<PersonDto, PersonJson>() {
             public PersonJson apply(PersonDto person) throws SQLException {
                 return new PersonJson(person.uuid, person.name, person.mail);
             }
         };
 
         protected final SqlF<PersonDto, PersonDetailJson> getPersonDetailJson = new SqlF<PersonDto, PersonDetailJson>() {
             public PersonDetailJson apply(PersonDto person) throws SQLException {
                 List<BadgeJson> badges = new ArrayList<>();
                 for (PersonalBadgeDto badge : daos.personDao.selectBadges(person.uuid)) {
                     badges.add(getBadgeJson.apply(badge));
                 }
 
                 List<BadgeJson> badgesInProgress = new ArrayList<>();
                 for (PersonBadgeProgressDto badgeProgressDto : daos.personDao.selectBadgeProgresses(person.uuid)) {
                     BadgeProgress progress = badgeService.badgeProgress(badgeProgressDto);
                     badgesInProgress.add(getBadge(progress));
                 }
 
                 List<UUID> jenkinsUserUuid = daos.personDao.selectJenkinsUserUuidsByPerson(person.uuid);
 
                 return new PersonDetailJson(
                         getPersonJson.apply(person),
                         badges,
                         badgesInProgress,
                         jenkinsUserUuid);
             }
         };
 
         // -----------------------------------------------------------------------
         // Build
         // -----------------------------------------------------------------------
 
         protected final SqlF<BuildDto, BuildJson> getBuildJson = new SqlF<BuildDto, BuildJson>() {
             public BuildJson apply(BuildDto dto) throws SQLException {
                return new BuildJson(dto.uuid, dto.timestamp, dto.success);
             }
         };
 
         protected final SqlF<BuildDto, BuildDetailJson> getBuildDetailJson = new SqlF<BuildDto, BuildDetailJson>() {
             public BuildDetailJson apply(BuildDto build) throws SQLException {
                 List<PersonJson> list = new ArrayList<>();
                 for (PersonDto person : daos.buildDao.selectPersonsFromBuildParticipant(build.uuid)) {
                     list.add(getPersonJson.apply(person));
                 }
 
                 return new BuildDetailJson(getBuildJson.apply(build),
                         list);
             }
         };
 
         // -----------------------------------------------------------------------
         // Badge
         // -----------------------------------------------------------------------
 
         protected SqlF<PersonalBadgeDto, BadgeJson> getBadgeJson = new SqlF<PersonalBadgeDto, BadgeJson>() {
             public BadgeJson apply(PersonalBadgeDto badge) throws SQLException {
                 return new BadgeJson(badge.uuid, badge.createdDate, badge.type.name(), badge.level);
             }
         };
 
         private BadgeJson getBadge(BadgeProgress progress) {
             return new BadgeJson(progress.type.name(), progress.progressingAgainstLevel(), progress.progression(), progress.goal());
         }
 
         protected final SqlF<PersonalBadgeDto, BadgeDetailJson> getBadgeDetailJson = new SqlF<PersonalBadgeDto, BadgeDetailJson>() {
             public BadgeDetailJson apply(PersonalBadgeDto badgeDto) throws SQLException {
 
                 return new BadgeDetailJson(getBadgeJson.apply(badgeDto),
                         badge.apply(badgeDto),
                         daos.personDao.selectPerson(badgeDto.person).map(getPersonJson).get());
             }
         };
     }
 }
 
 class BuildJson {
     public final UUID uuid;
     public final DateTime createdDate;
     public final boolean success;
 
    public BuildJson(UUID uuid, DateTime createdDate, boolean success) {
         this.uuid = uuid;
         this.createdDate = createdDate;
         this.success = success;
     }
 }
 
 class BuildDetailJson {
     public final BuildJson build;
     public final List<PersonJson> participants;
 
     BuildDetailJson(BuildJson build, List<PersonJson> participants) {
         this.build = build;
         this.participants = participants;
     }
 }
