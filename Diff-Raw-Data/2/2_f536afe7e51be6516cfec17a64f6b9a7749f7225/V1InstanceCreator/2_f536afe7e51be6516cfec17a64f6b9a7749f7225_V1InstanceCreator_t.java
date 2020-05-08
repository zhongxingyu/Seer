 /*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
 package com.versionone.om;
 
 import com.versionone.DB.DateTime;
 import com.versionone.Duration;
 import com.versionone.apiclient.MimeType;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * Create Assets.
  */
 public class V1InstanceCreator {
     private final V1Instance instance;
 
     V1InstanceCreator(V1Instance instance) {
         this.instance = instance;
     }
 
     /**
      * Create a new project entity with a name, parent project, begin date, and
      * optional schedule.
      *
      * @param name          name of project.
      * @param parentProject parent project for created project.
      * @param beginDate     start date of created project.
      * @param schedule      Schedule that defines how this project's iterations are
      *                      spaced.
      * @return A newly minted Project that exists in the VersionOne system.
      */
     public Project project(String name, Project parentProject,
                            DateTime beginDate, Schedule schedule) {
         return project(name, parentProject, beginDate, schedule, null);
     }
 
     /**
      * Create a new project entity with a name, parent project, begin date, and
      * optional schedule with specified attributes.
      *
      * @param name          name of project.
      * @param parentProject parent project for created project.
      * @param beginDate     start date of created project.
      * @param schedule      Schedule that defines how this project's iterations are
      *                      spaced.
      * @param attributes    additional attributes for inicialization project.
      * @return A newly minted Project that exists in the VersionOne system.
      */
     public Project project(String name, Project parentProject,
                            DateTime beginDate, Schedule schedule, Map<String, Object> attributes) {
         Project project = new Project(instance);
         project.setName(name);
         project.setParentProject(parentProject);
         project.setBeginDate(beginDate);
         project.setSchedule(schedule);
         addAttributes(project, attributes);
         project.save();
         return project;
     }
 
     /**
      * Create a new project entity with a name, parent project, begin date, and
      * optional schedule.
      *
      * @param name            name of project.
      * @param parentProjectID id of parent project for created project.
      * @param beginDate       start date of created project.
      * @param schedule        Schedule that defines how this project's iterations are
      *                        spaced.
      * @return A newly minted Project that exists in the VersionOne system.
      */
     public Project project(String name, AssetID parentProjectID,
                            DateTime beginDate, Schedule schedule) {
         return project(name, new Project(parentProjectID, instance),
                        beginDate, schedule, null);
     }
 
 
     /**
      * Create a new project entity with a name, parent project, begin date, and
      * optional schedule.
      *
      * @param name            name of project.
      * @param parentProjectID id of parent project for created project.
      * @param beginDate       start date of created project.
      * @param schedule        Schedule that defines how this project's iterations are
      *                        spaced.
      * @param attributes      additional attributes for the Project.
      * @return A newly minted Project that exists in the VersionOne system.
      */
     public Project project(String name, AssetID parentProjectID,
                            DateTime beginDate, Schedule schedule, Map<String, Object> attributes) {
         return project(name, new Project(parentProjectID, instance),
                        beginDate, schedule, attributes);
     }
 
     /**
      * Create a new member entity with a name, short name, and default role.
      *
      * @param name        The full name of the user.
      * @param shortName   An alias or nickname used throughout the VersionOne user
      *                    interface.
      * @param defaultRole The new user's default role on projects.
      * @return A newly minted Member that exists in the VersionOne system.
      */
     public Member member(String name, String shortName, Role defaultRole) {
         return member(name, shortName, defaultRole, null);
     }
 
     /**
      * Create a new member entity with a name, short name, and default role.
      *
      * @param name        The full name of the user.
      * @param shortName   An alias or nickname used throughout the VersionOne user
      *                    interface.
      * @param defaultRole The new user's default role on projects.
      * @param attributes  additional attributes for the Member.
      * @return A newly minted Member that exists in the VersionOne system.
      */
     public Member member(String name, String shortName, Role defaultRole, Map<String, Object> attributes) {
         Member member = new Member(instance);
 
         member.setName(name);
         member.setShortName(shortName);
         member.setDefaultRole(defaultRole);
         addAttributes(member, attributes);
         member.save();
         return member;
     }
 
     /**
      * Create a new member entity with a name and short name.
      *
      * @param name      The full name of the user.
      * @param shortName An alias or nickname used throughout the VersionOne user
      *                  interface.
      * @return A newly minted Member that exists in the VersionOne system.
      */
     public Member member(String name, String shortName) {
         return member(name, shortName, Role.TEAM_MEMBER);
     }
 
     /**
      * Create a new member entity with a name and short name.
      *
      * @param name       The full name of the user.
      * @param shortName  An alias or nickname used throughout the VersionOne user
      *                   interface.
      * @param attributes additional attributes for the member.
      * @return A newly minted Member that exists in the VersionOne system.
      */
     public Member member(String name, String shortName, Map<String, Object> attributes) {
         return member(name, shortName, Role.TEAM_MEMBER, attributes);
     }
 
     /**
      * Create a new team entity with a name.
      *
      * @param name name of team.
      * @return A newly minted Team that exists in the VersionOne system.
      */
     public Team team(String name) {
         return team(name, null);
     }
 
     /**
      * Create a new team entity with a name.
      *
      * @param name       name of team.
      * @param attributes additional attributes for the Team.
      * @return A newly minted Team that exists in the VersionOne system.
      */
     public Team team(String name, Map<String, Object> attributes) {
         Team team = new Team(instance);
 
         team.setName(name);
         addAttributes(team, attributes);
         team.save();
         return team;
     }
 
     /**
      * Create a new Story with a name.
      *
      * @param name    The initial name of the entity.
      * @param project The Project this Story will be in.
      * @return A newly minted Story that exists in the VersionOne system.
      */
     public Story story(String name, Project project) {
         return story(name, project, null);
     }
 
 
     /**
      * Create a new Story with a name.
      *
      * @param name       The initial name of the entity.
      * @param project    The Project this Story will be in.
      * @param attributes additional attributes for the Story.
      * @return A newly minted Story that exists in the VersionOne system.
      */
     public Story story(String name, Project project, Map<String, Object> attributes) {
         Story story = new Story(instance);
 
         story.setName(name);
         story.setProject(project);
         addAttributes(story, attributes);
         story.save();
         return story;
     }
 
 
     /**
      * Create a new story with a name. Set the story's IdentifiedIn to the given
      * retrospective and the project to the retrospective's project.
      *
      * @param name          The initial name of the story.
      * @param retrospective The retrospective this story was identified in.
      * @return A newly minted Story that exists in the VersionOne system.
      */
     public Story story(String name, Retrospective retrospective) {
         return story(name, retrospective, null);
     }
 
     /**
      * Create a new story with a name. Set the story's IdentifiedIn to the given
      * retrospective and the project to the retrospective's project.
      *
      * @param name          The initial name of the story.
      * @param retrospective The retrospective this story was identified in.
      * @param attributes    additional attributes for the Story.
      * @return A newly minted Story that exists in the VersionOne system.
      */
     public Story story(String name, Retrospective retrospective, Map<String, Object> attributes) {
         Story story = new Story(instance);
 
         story.setName(name);
         story.setIdentifiedIn(retrospective);
         story.setProject(retrospective.getProject());
         addAttributes(story, attributes);
         story.save();
         return story;
     }
 
     /**
      * Create a new Defect with a name.
      *
      * @param name    The initial name of the entity.
      * @param project The Project this Defect will be in.
      * @return A newly minted Defect that exists in the VersionOne system.
      */
     public Defect defect(String name, Project project) {
         return defect(name, project, null);
     }
 
     /**
      * Create a new Defect with a name.
      *
      * @param name       The initial name of the entity.
      * @param project    The Project this Defect will be in.
      * @param attributes additional attributes for the Defect.
      * @return A newly minted Defect that exists in the VersionOne system.
      */
     public Defect defect(String name, Project project, Map<String, Object> attributes) {
         Defect defect = new Defect(instance);
 
         defect.setName(name);
         defect.setProject(project);
         addAttributes(defect, attributes);
         defect.save();
         return defect;
     }
 
     /**
      * Create a new Task with a name. Assign it to the given primary workitem.
      *
      * @param name            The initial name of the task.
      * @param primaryWorkitem The PrimaryWorkitem this Task will belong to.
      * @return A newly minted Task that exists in the VersionOne system.
      */
     public Task task(String name, PrimaryWorkitem primaryWorkitem) {
         return task(name, primaryWorkitem, null);
     }
 
     /**
      * Create a new Task with a name. Assign it to the given primary workitem.
      *
      * @param name            The initial name of the task.
      * @param primaryWorkitem The PrimaryWorkitem this Task will belong to.
      * @param attributes      additional attributes for the Task.
      * @return A newly minted Task that exists in the VersionOne system.
      */
     public Task task(String name, PrimaryWorkitem primaryWorkitem, Map<String, Object> attributes) {
         Task task = new Task(instance);
 
         task.setName(name);
         task.setParent(primaryWorkitem);
         addAttributes(task, attributes);
         task.save();
         return task;
     }
 
     /**
      * Create a new Test with a name. Assign it to the given primary workitem.
      *
      * @param name            The initial name of the test.
      * @param workitem        The Workitem(Epic, Story, Defect) this Test will belong to.
      * @return A newly minted Test that exists in the VersionOne system.
      */
     public Test test(String name, Workitem workitem) {
         return test(name, workitem, null);
     }
 
     /**
      * Create a new Test with a name. Assign it to the given primary workitem.
      *
      * @param name            The initial name of the test.
      * @param workitem        The Workitem(Epic, Story, Defect) this Test will belong to.
      * @param attributes      additional attributes for the Test.
      * @return A newly minted Test that exists in the VersionOne system.
      */
     public Test test(String name, Workitem workitem, Map<String, Object> attributes) {
         Test test = new Test(instance);
 
         test.setName(name);
         test.setParent(workitem);
         addAttributes(test, attributes);
         test.save();
         return test;
     }
 
     /**
      * Create a new Theme with a name.
      *
      * @param name    The initial name of the entity.
      * @param project The Project this Theme will be in.
      * @return A newly minted Theme that exists in the VersionOne system.
      */
     public Theme theme(String name, Project project) {
         return theme(name, project, null);
     }
 
     /**
      * Create a new Theme with a name.
      *
      * @param name       The initial name of the entity.
      * @param project    The Project this Theme will be in.
      * @param attributes additional attributes for the Theme.
      * @return A newly minted Theme that exists in the VersionOne system.
      */
     public Theme theme(String name, Project project, Map<String, Object> attributes) {
         Theme theme = new Theme(instance);
 
         theme.setName(name);
         theme.setProject(project);
         addAttributes(theme, attributes);
         theme.save();
         return theme;
     }
 
     /**
      * Create a new Goal with a name.
      *
      * @param name    The initial name of the entity.
      * @param project The Project this Goal will be in.
      * @return A newly minted Goal that exists in the VersionOne system.
      */
     public Goal goal(String name, Project project) {
         return goal(name, project, null);
     }
 
     /**
      * Create a new Goal with a name.
      *
      * @param name       The initial name of the entity.
      * @param project    The Project this Goal will be in.
      * @param attributes additional attributes for the Goal.
      * @return A newly minted Goal that exists in the VersionOne system.
      */
     public Goal goal(String name, Project project, Map<String, Object> attributes) {
         Goal goal = new Goal(instance);
 
         goal.setName(name);
         goal.setProject(project);
         addAttributes(goal, attributes);
         goal.save();
         return goal;
     }
 
     /**
      * Create a new Issue with a name.
      *
      * @param name    The initial name of the entity.
      * @param project The Project this Issue will be in.
      * @return A newly minted Issue that exists in the VersionOne system.
      */
     public Issue issue(String name, Project project) {
         return issue(name, project, null);
     }
 
     /**
      * Create a new Issue with a name.
      *
      * @param name       The initial name of the entity.
      * @param project    The Project this Issue will be in.
      * @param attributes additional attributes for the Issue.
      * @return A newly minted Issue that exists in the VersionOne system.
      */
     public Issue issue(String name, Project project, Map<String, Object> attributes) {
         Issue issue = new Issue(instance);
 
         issue.setName(name);
         issue.setProject(project);
         addAttributes(issue, attributes);
         issue.save();
         return issue;
     }
 
     /**
      * Creates an Issue related to a Retrospective.
      *
      * @param name          The name of the Issue.
      * @param retrospective The Retrospective to relate the Issue to.
      * @return The newly saved Issue.
      */
     public Issue issue(String name, Retrospective retrospective) {
         return issue(name, retrospective, null);
     }
 
     /**
      * Creates an Issue related to a Retrospective.
      *
      * @param name          The name of the Issue.
      * @param retrospective The Retrospective to relate the Issue to.
      * @param attributes    additional attributes for the Issue.
      * @return The newly saved Issue.
      */
     public Issue issue(String name, Retrospective retrospective, Map<String, Object> attributes) {
         Issue issue = new Issue(instance);
 
         issue.setName(name);
         issue.setProject(retrospective.getProject());
         addAttributes(issue, attributes);
         issue.save();
         issue.getRetrospectives().add(retrospective);
         return issue;
     }
 
     /**
      * Create a new Request with a name.
      *
      * @param name    The initial name of the entity.
      * @param project The Project this Request will be in.
      * @return A newly minted Request that exists in the VersionOne system.
      */
     public Request request(String name, Project project) {
         return request(name, project, null);
     }
 
     /**
      * Create a new Request with a name.
      *
      * @param name       The initial name of the entity.
      * @param project    The Project this Request will be in.
      * @param attributes additional attributes for the Request.
      * @return A newly minted Request that exists in the VersionOne system.
      */
     public Request request(String name, Project project, Map<String, Object> attributes) {
         Request request = new Request(instance);
 
         request.setName(name);
         request.setProject(project);
         addAttributes(request, attributes);
         request.save();
         return request;
     }
 
     /**
      * Create a new Epic with a name.
      *
      * @param name    The initial name of the entity.
      * @param project The Project this Epic will be in.
      * @return A newly minted Epic that exists in the VersionOne system.
      */
     public Epic epic(String name, Project project) {
         return epic(name, project, null);
     }
 
     /**
      * Create a new Epic with a name.
      *
      * @param name       The initial name of the entity.
      * @param project    The Project this Epic will be in.
      * @param attributes additional attributes for the Epic.
      * @return A newly minted Epic that exists in the VersionOne system.
      */
     public Epic epic(String name, Project project, Map<String, Object> attributes) {
     	Epic epic = new Epic(instance);
 
         epic.setName(name);
         epic.setProject(project);
         addAttributes(epic, attributes);
         epic.save();
 
         return epic;
     }
 
     /**
      * Create a new link with a name.
      *
      * @param name   The initial name of the link.
      * @param asset  The asset this link belongs to.
      * @param url    The URL of the link.
      * @param onMenu True to show on the asset's detail page menu.
      * @return A newly minted Link that exists in the VersionOne system.
      */
     public Link link(String name, BaseAsset asset, String url,
                      boolean onMenu) {
         return link(name, asset, url, onMenu, null);
     }
 
     /**
      * Create a new link with a name.
      *
      * @param name       The initial name of the link.
      * @param asset      The asset this link belongs to.
      * @param url        The URL of the link.
      * @param onMenu     True to show on the asset's detail page menu.
      * @param attributes additional attributes for the Link.
      * @return A newly minted Link that exists in the VersionOne system.
      */
     public Link link(String name, BaseAsset asset, String url,
                      boolean onMenu, Map<String, Object> attributes) {
         Link link = new Link(instance);
 
         link.setAsset(asset);
         link.setName(name);
         link.setURL(url);
         link.setOnMenu(onMenu);
         addAttributes(link, attributes);
         link.save();
         return link;
     }
 
     /**
      * Create a new note with a name, asset, content, and 'personal' flag.
      *
      * @param name     The initial name of the note.
      * @param asset    The asset this note belongs to.
      * @param content  The content of the note.
      * @param personal True if this note is only visible to.
      * @return A newly minted Note that exists in the VersionOne system.
      */
     public Note note(String name, BaseAsset asset, String content,
                      boolean personal) {
 
         return note(name, asset, content, personal, null);
     }
 
     /**
      * Create a new note with a name, asset, content, and 'personal' flag.
      *
      * @param name       The initial name of the note.
      * @param asset      The asset this note belongs to.
      * @param content    The content of the note.
      * @param personal   True if this note is only visible to.
      * @param attributes additional attributes for the Note.
      * @return A newly minted Note that exists in the VersionOne system.
      */
     public Note note(String name, BaseAsset asset, String content,
                      boolean personal, Map<String, Object> attributes) {
         Note note = new Note(instance);
 
         note.setAsset(asset);
         note.setName(name);
         note.setContent(content);
         note.setPersonal(personal);
         addAttributes(note, attributes);
         note.save();
         return note;
     }
 
     /**
      * Create a new TestSuite with a name.
      *
      * @param name      The initial name of the TestSuite.
      * @param reference A free text field used for reference (perhaps to an
      *                  external system).
      * @return A newly minted TestSuite that exists in the VersionOne system.
      */
     public TestSuite testSuite(String name, String reference) {
         return testSuite(name, reference, null);
     }
 
     /**
      * Create a new TestSuite with a name.
      *
      * @param name       The initial name of the TestSuite.
      * @param reference  A free text field used for reference (perhaps to an
      *                   external system).
      * @param attributes additional attributes for the TestSuite.
      * @return A newly minted TestSuite that exists in the VersionOne system.
      */
     public TestSuite testSuite(String name, String reference, Map<String, Object> attributes) {
         TestSuite testSuite = new TestSuite(instance);
 
         testSuite.setName(name);
         testSuite.setReference(reference);
         addAttributes(testSuite, attributes);
         testSuite.save();
         return testSuite;
     }
 
     /**
      * Create a new attachment with a name.
      *
      * @param name     The name of the attachment.
      * @param asset    The asset this attachment belongs to.
      * @param filename The name of the original attachment file.
      * @param stream   The read-enabled stream that contains the attachment
      *                 content to upload.
      * @return A newly minted Attachment that exists in the VersionOne system.
      * @throws AttachmentLengthExceededException
      *          if attachment is too long.
      * @throws ApplicationUnavailableException
      *          if appears any problem with
      *          connection to service.
      */
     public Attachment attachment(String name, BaseAsset asset,
                                  String filename, InputStream stream)
             throws AttachmentLengthExceededException,
             ApplicationUnavailableException {
         return attachment(name, asset, filename, stream, null);
     }
 
     /**
      * Create a new attachment with a name.
      *
      * @param name       The name of the attachment.
      * @param asset      The asset this attachment belongs to.
      * @param filename   The name of the original attachment file.
      * @param stream     The read-enabled stream that contains the attachment
      *                   content to upload.
      * @param attributes additional attributes for the Attachment.
      * @return A newly minted Attachment that exists in the VersionOne system.
      * @throws AttachmentLengthExceededException
      *          if attachment is too long.
      * @throws ApplicationUnavailableException
      *          if appears any problem with
      *          connection to service.
      */
     public Attachment attachment(String name, BaseAsset asset,
                                  String filename, InputStream stream, Map<String, Object> attributes)
             throws AttachmentLengthExceededException,
             ApplicationUnavailableException {
         Attachment attachment = new Attachment(instance);
 
         attachment.setAsset(asset);
         attachment.setName(name);
         attachment.setFilename(filename);
         attachment.setContentType(MimeType.resolve(filename));
         addAttributes(attachment, attributes);
         attachment.save();
 
         if (stream != null) {
             attachment.readFrom(stream);
         }
 
         return attachment;
     }
 
     /**
      * Create a new retrospective with a name.
      *
      * @param name    The name of the retrospective.
      * @param project The project this retrospective belongs to.
      * @return A newly minted Retrospective that exists in the VersionOne
      *         system.
      */
     public Retrospective retrospective(String name, Project project) {
         return retrospective(name, project, null);
     }
 
     /**
      * Create a new retrospective with a name.
      *
      * @param name       The name of the retrospective.
      * @param project    The project this retrospective belongs to.
      * @param attributes additional attributes for the Retrospective.
      * @return A newly minted Retrospective that exists in the VersionOne
      *         system.
      */
     public Retrospective retrospective(String name, Project project, Map<String, Object> attributes) {
         Retrospective retro = new Retrospective(instance);
 
         retro.setProject(project);
         retro.setName(name);
         addAttributes(retro, attributes);
         retro.save();
         return retro;
     }
 
     /**
      * Create a new iteration using suggested system values.
      *
      * @param project The project is used to determine the schedule this iteration belongs to.
      * @return A newly minted Iteration that exists in the VersionOne system.
      */
     public Iteration iteration(Project project) {
         return iteration(project, null);
     }
 
     /**
      * Create a new iteration using suggested system values.
      *
      * @param project    The project is used to determine the schedule this iteration belongs to.
      * @param attributes additional attributes for the Iteration.
      * @return A newly minted Iteration that exists in the VersionOne system.
      */
     public Iteration iteration(Project project, Map<String, Object> attributes) {
         Iteration iteration = instance.createNew(Iteration.class, project);
 
         addAttributes(iteration, attributes);
         iteration.save();
         //Fix bug on the backend. (C# comment)
         iteration.makeFuture();
         return iteration;
     }
 
     /**
      * Create a new iteration with a name, begin date, and end date.
      *
      * @param name      The name of the iteration.
      * @param schedule  The schedule this iteration belongs to.
      * @param beginDate The begin date or start date of this iteration.
      * @param endDate   The end date of this iteration.
      * @return A newly minted Iteration that exists in the VersionOne system.
      */
     public Iteration iteration(String name, Schedule schedule,
                                DateTime beginDate, DateTime endDate) {
         return iteration(name, schedule, beginDate, endDate, null);
     }
 
     /**
      * Create a new iteration with a name, begin date, and end date.
      *
      * @param name       The name of the iteration.
      * @param schedule   The schedule this iteration belongs to.
      * @param beginDate  The begin date or start date of this iteration.
      * @param endDate    The end date of this iteration.
      * @param attributes additional attributes for the Iteration.
      * @return A newly minted Iteration that exists in the VersionOne system.
      */
     public Iteration iteration(String name, Schedule schedule,
                                DateTime beginDate, DateTime endDate, Map<String, Object> attributes) {
         Iteration iteration = new Iteration(instance);
 
         iteration.setName(name);
         iteration.setSchedule(schedule);
         iteration.setBeginDate(beginDate);
         iteration.setEndDate(endDate);
         addAttributes(iteration, attributes);
         iteration.save();
         return iteration;
     }
 
     /**
      * Create a new effort record with a value and date, assigned to the given
      * workitem and member.
      *
      * @param value  the value of the effort record.
      * @param item   the workitem to assign the effort record to.
      * @param member the member to assign the effort record to. If is null then
      *               member not set.
      * @param date   the date to log the effort record against. If is null then
      *               date not set.
      * @return A newly minted Effort Record that exists in the VersionOne
      *         system.
      * @throws IllegalStateException if Effort tracking is not enabled.
      */
     public Effort effort(double value, Workitem item, Member member,
                          DateTime date) throws IllegalStateException {
         return effort(value, item, member, date, null);
     }
 
     /**
      * Create a new effort record with a value and date, assigned to the given
      * workitem and member.
      *
      * @param value      the value of the effort record.
      * @param item       the workitem to assign the effort record to.
      * @param member     the member to assign the effort record to. If is null then
      *                   member not set.
      * @param date       the date to log the effort record against. If is null then
      *                   date not set.
      * @param attributes additional attributes for the Effort.
      * @return A newly minted Effort Record that exists in the VersionOne
      *         system.
      * @throws IllegalStateException if Effort tracking is not enabled.
      */
     public Effort effort(double value, Workitem item, Member member,
                          DateTime date, Map<String, Object> attributes) throws IllegalStateException {
         if (!instance.getConfiguration().effortTrackingEnabled) {
             throw new IllegalStateException("Effort Tracking is disabled.");
         }
 
         instance.preventTrackingLevelAbuse(item);
 
         Effort actual = instance.createNew(Effort.class, item);
         actual.setValue(value);
 
         if (member != null) {
             actual.setMember(member);
         }
 
         if (date != null) {
             actual.setDate(date);
         }
         addAttributes(actual, attributes);
         actual.save();
         return actual;
     }
 
     /**
      * Create a new effort record for the currently logged in member.
      *
      * @param value The value of the effort record.
      * @param item  The workitem to assign the effort record to.
      * @return A newly minted Effort Record that exists in the VersionOne
      *         system.
      * @throws IllegalStateException if Effort tracking is not enabled.
      */
     public Effort effort(double value, Workitem item)
             throws IllegalStateException {
         return effort(value, item, null, null);
     }
 
     /**
      * Create a new effort record for the currently logged in member.
      *
      * @param value      The value of the effort record.
      * @param item       The workitem to assign the effort record to.
      * @param attributes additional attributes for the Effort.
      * @return A newly minted Effort Record that exists in the VersionOne
      *         system.
      * @throws IllegalStateException if Effort tracking is not enabled.
      */
     public Effort effort(double value, Workitem item, Map<String, Object> attributes)
             throws IllegalStateException {
         return effort(value, item, null, null, attributes);
     }
 
     /**
      * Create a new Build Project with a name and reference.
      *
      * @param name      Initial name.
      * @param reference Reference value.
      * @return A newly minted Build Project that exists in the VersionOne
      *         system.
      */
     public BuildProject buildProject(String name, String reference) {
         return buildProject(name, reference, null);
     }
 
     /**
      * Create a new Build Project with a name and reference.
      *
      * @param name       Initial name.
      * @param reference  Reference value.
      * @param attributes additional attributes for the BuildProject.
      * @return A newly minted Build Project that exists in the VersionOne
      *         system.
      */
     public BuildProject buildProject(String name, String reference, Map<String, Object> attributes) {
         BuildProject buildProject = new BuildProject(instance);
 
         buildProject.setName(name);
         buildProject.setReference(reference);
         addAttributes(buildProject, attributes);
         buildProject.save();
         return buildProject;
     }
 
     /**
      * Create a new Build Run in the given Build Project with a name and date.
      *
      * @param buildProject The Build Project this Build Run belongs to.
      * @param name         Name of the build project.
      * @param date         The Date this Build Run ran.
      * @return A newly minted Build Run that exists in the VersionOne system.
      */
     public BuildRun buildRun(BuildProject buildProject, String name,
                              DateTime date) {
         return buildRun(buildProject, name, date, null);
     }
 
     /**
      * Create a new Build Run in the given Build Project with a name and date.
      *
      * @param buildProject The Build Project this Build Run belongs to.
      * @param name         Name of the build project.
      * @param date         The Date this Build Run ran.
      * @param attributes   additional attributes for the BuildRun.
      * @return A newly minted Build Run that exists in the VersionOne system.
      */
     public BuildRun buildRun(BuildProject buildProject, String name,
                              DateTime date, Map<String, Object> attributes) {
         BuildRun buildRun = new BuildRun(instance);
 
         buildRun.setName(name);
         buildRun.setBuildProject(buildProject);
         buildRun.setDate(date);
         addAttributes(buildRun, attributes);
         buildRun.save();
         return buildRun;
     }
 
     /**
      * Create a new ChangeSet with a name and reference.
      *
      * @param name      Initial name.
      * @param reference Reference value.
      * @return A newly minted ChangeSet that exists in the VersionOne system.
      */
     public ChangeSet changeSet(String name, String reference) {
         return changeSet(name, reference, null);
     }
 
     /**
      * Create a new ChangeSet with a name and reference.
      *
      * @param name       Initial name.
      * @param reference  Reference value.
      * @param attributes additional attributes for the ChangeSet.
      * @return A newly minted ChangeSet that exists in the VersionOne system.
      */
     public ChangeSet changeSet(String name, String reference, Map<String, Object> attributes) {
         ChangeSet changeSet = new ChangeSet(instance);
 
         changeSet.setName(name);
         changeSet.setReference(reference);
         addAttributes(changeSet, attributes);
         changeSet.save();
         return changeSet;
     }
 
     /**
      * Create a new schedule entity with a name, iteration length, and iteration gap
      *
      * @param name            Name of the new schedule
      * @param iterationLength The duration an iteration will last in this schedule
      * @param iterationGap    The duration between iterations in this schedule.
      * @return A newly minted Schedule that exists in the VersionOne system.
      */
     public Schedule schedule(String name, Duration iterationLength, Duration iterationGap) {
         return schedule(name, iterationLength, iterationGap, null);
     }
 
     /**
      * Create a new schedule entity with a name, iteration length, and iteration gap
      *
      * @param name            Name of the new schedule
      * @param iterationLength The duration an iteration will last in this schedule
      * @param iterationGap    The duration between iterations in this schedule.
      * @param attributes      additional attributes for the Schedule.
      * @return A newly minted Schedule that exists in the VersionOne system.
      */
     public Schedule schedule(String name, Duration iterationLength, Duration iterationGap,
                              Map<String, Object> attributes) {
         Schedule schedule = new Schedule(instance);
         schedule.setName(name);
         schedule.setIterationLength(iterationLength);
         schedule.setIterationGap(iterationGap);
         addAttributes(schedule, attributes);
         schedule.save();
         return schedule;
     }
 
     /**
      * Create a new response to an existing note with a name, content, and 'personal' flag
      *
      * @param responseTo The Note to respond to
      * @param name       The initial name of the note
      * @param content    The content of the note
      * @param personal   True if this note is only visible to
      * @return A newly minted Note in response to the original one that exists in the VersionOne system.
      */
     public Note note(Note responseTo, String name, String content, boolean personal) {
         return note(responseTo, name, content, personal, null);
     }
 
     /**
      * Create a new response to an existing note with a name, content, and 'personal' flag
      *
      * @param responseTo The Note to respond to.
      * @param name       The initial name of the note.
      * @param content    The content of the note.
      * @param personal   True if this note is only visible to.
      * @param attributes additional attributes for the note.
      * @return A newly minted Note in response to the original one that exists in the VersionOne system.
      */
     public Note note(Note responseTo, String name, String content, boolean personal, Map<String, Object> attributes) {
         Note note = new Note(responseTo, instance);
         note.setName(name);
         note.setContent(content);
         note.setPersonal(personal);
         addAttributes(note, attributes);
         note.save();
         return note;
     }
 
 
     /**
      * Create a new Message with a subject and recipient.
      *
      * @param subject     Message subject.
      * @param messageBody Message body.
      * @return A newly minted Message that exists in the VersionOne system.
      */
     public Message message(String subject, String messageBody) {
         Collection<Member> recipients = new ArrayList<Member>();
         return message(subject, messageBody, recipients);
     }
 
     /**
      * Create a new Message with a subject and recipient.
      *
      * @param subject     Message subject.
      * @param messageBody Message body.
      * @param attributes  additional attributes for the Message.
      * @return A newly minted Message that exists in the VersionOne system.
      */
     public Message message(String subject, String messageBody, Map<String, Object> attributes) {
         Collection<Member> recipients = new ArrayList<Member>();
         return message(subject, messageBody, recipients, attributes);
     }
 
     /**
      * Create a new Message with a subject and recipient.
      *
      * @param subject     Message subject.
      * @param messageBody Message body.
      * @param recipient   Who this message will go to.
      * @return A newly minted Message that exists in the VersionOne system.
      */
     public Message message(String subject, String messageBody, Member recipient) {
         Collection<Member> recipients = new ArrayList<Member>();
         recipients.add(recipient);
         return message(subject, messageBody, recipients);
     }
 
     /**
      * Create a new Message with a subject and recipient.
      *
      * @param subject     Message subject.
      * @param messageBody Message body.
      * @param recipient   Who this message will go to.
      * @param attributes  additional attributes for the Message.
      * @return A newly minted Message that exists in the VersionOne system.
      */
     public Message message(String subject, String messageBody, Member recipient, Map<String, Object> attributes) {
         Collection<Member> recipients = new ArrayList<Member>();
         recipients.add(recipient);
         return message(subject, messageBody, recipients, attributes);
     }
 
     /**
      * Create a new Message with a subject and recipient.
      *
      * @param subject     Message subject.
      * @param messageBody Message body.
      * @param recipients  Who this message will go to. May be null.
      * @return A newly minted Message that exists in the VersionOne system.
      */
     public Message message(String subject, String messageBody, Collection<Member> recipients) {
         return message(subject, messageBody, recipients, null);
     }
 
     /**
      * Create a new Message with a subject and recipient.
      *
      * @param subject     Message subject.
      * @param messageBody Message body.
      * @param recipients  Who this message will go to. May be null.
      * @param attributes  additional attributes for the Message.
      * @return A newly minted Message that exists in the VersionOne system.
      */
     public Message message(String subject, String messageBody, Collection<Member> recipients, Map<String, Object> attributes) {
         Message message = new Message(instance);
         message.setName(subject);
         message.setDescription(messageBody);
         if (recipients != null)
             for (Member recipient : recipients) {
                 message.getRecipients().add(recipient);
             }
         addAttributes(message, attributes);
         message.save();
         return message;
     }
 
     /**
      * Creates a new Regression Suite with title assigned with this Regression Plan.
      *
      * @param name           Title of the suite.
      * @param regressionPlan Regression Plan to assign.
      * @return A newly minted Regression Suite that exists in the VersionOne system.
      */
     public RegressionSuite regressionSuite(String name, RegressionPlan regressionPlan) {
         return regressionSuite(name, regressionPlan, null);
     }
 
     /**
      * Creates a new Regression Suite with title assigned with this Regression Plan.
      *
      * @param name           Title of the suite.
      * @param regressionPlan Regression Plan to assign.
      * @param attributes     additional attributes for the RegressionSuite.
      * @return A newly minted Regression Suite that exists in the VersionOne system.
      */
     public RegressionSuite regressionSuite(String name, RegressionPlan regressionPlan, Map<String, Object> attributes) {
         RegressionSuite regressionSuite = new RegressionSuite(instance);
 
         regressionSuite.setName(name);
         regressionSuite.setRegressionPlan(regressionPlan);
         addAttributes(regressionSuite, attributes);
         regressionSuite.save();
 
         return regressionSuite;
     }
 
     /**
      * Creates a new Regression Plan with title and project.
      *
      * @param name    Title of the plan.
      * @param project Project to assign.
      * @return A newly minted Regression Plan that exists in the VersionOne system.
      */
     public RegressionPlan regressionPlan(String name, Project project) {
         return regressionPlan(name, project, null);
     }
 
     /**
      * Creates a new Regression Plan with title and project.
      *
      * @param name       Title of the plan.
      * @param project    Project to assign.
      * @param attributes Additional attributes for initialization Regression Plan.
      * @return A newly minted Regression Plan that exists in the VersionOne system.
      */
     public RegressionPlan regressionPlan(String name, Project project, Map<String, Object> attributes) {
         RegressionPlan regressionPlan = new RegressionPlan(instance);
 
         regressionPlan.setName(name);
         regressionPlan.setProject(project);
         addAttributes(regressionPlan, attributes);
         regressionPlan.save();
 
         return regressionPlan;
     }
 
     /**
      * Creates a new TestSet with title and project.
      *
      * @param name    Title of the testSet.
      * @param suite   Parent RegressionSuite.
      * @param project Project to assign.
      * @return Newly created Test Set.
      */
     public TestSet testSet(String name, RegressionSuite suite, Project project) {
         return testSet(name, suite, project, null);
     }
 
     /**
      * Creates a new TestSet with title and project.
      *
      * @param name       Title of the testSet.
      * @param suite      Parent RegressionSuite.
      * @param project    Project to assign.
      * @param attributes Additional attributes for initialization TestSet.
      * @return Newly created Test Set.
      */
     public TestSet testSet(String name, RegressionSuite suite, Project project, Map<String, Object> attributes) {
         // TODO invent consistent solution, possibly remove ability to create Test Sets from project
         if (!suite.getRegressionPlan().getProject().equals(project)) {
             throw new UnsupportedOperationException("Suite should belong to the project passed in parameters");
         }
         TestSet testSet = new TestSet(instance);
 
         testSet.setName(name);
         testSet.setRegressionSuite(suite);
         testSet.setProject(project);
         addAttributes(testSet, attributes);
         testSet.save();
 
         return testSet;
     }
 
     /**
      * Creates new Environment.
      *
      * @param name    Environment name.
      * @param project Parent Project.
      * @return Newly created Environment.
      */
     public Environment environment(String name, Project project) {
         return environment(name, project, null);
     }
 
     /**
      * Creates new Environment.
      *
      * @param name       Environment name.
      * @param project    Parent Project.
      * @param attributes Additional attributes that should be set for Environment.
      * @return Newly created Environment.
      */
     public Environment environment(String name, Project project, Map<String, Object> attributes) {
         Environment environment = new Environment(instance);
 
         environment.setName(name);
         environment.setProject(project);
         addAttributes(environment, attributes);
         environment.save();
 
         return environment;
     }
 
     /**
      * Creates new Regression Test.
      *
      * @param name    Regression Test name
      * @param project Regression Test project.
      * @return Newly created Regression Test.
      */
     public RegressionTest regressionTest(String name, Project project) {
         return regressionTest(name, project, null);
     }
 
 
     /**
      * Creates new Regression Test.
      *
      * @param name       Regression Test name
      * @param project    Regression Test project.
      * @param attributes Additional attributes that should be set for RegressionTest.
      * @return Newly created Regression Test.
      */
     public RegressionTest regressionTest(String name, Project project, Map<String, Object> attributes) {
         RegressionTest regressionTest = new RegressionTest(instance);
 
         regressionTest.setName(name);
         regressionTest.setProject(project);
         addAttributes(regressionTest, attributes);
         regressionTest.save();
 
         return regressionTest;
     }
 
 
     /**
      * Creates new Regression Test based on Test
      *
      * @param test    Test which we will be used to create Regression Test.
      * @return Newly created Regression Test.
      */
     public RegressionTest regressionTest(Test test) {
         RegressionTest regressionTest = new RegressionTest(instance);
 
         regressionTest.setDescription(test.getDescription());
         regressionTest.setGeneratedFrom(test);
         regressionTest.setName(test.getName());
         regressionTest.setProject(test.getProject());
         regressionTest.getType().setCurrentValue(test.getType().getCurrentValue());
 
         for (Member member : test.getOwners()) {
             regressionTest.getOwners().add(member);
         }
 
         regressionTest.save();
 
         return regressionTest;
     }
 
     /**
      * Adds new Conversation with expression with author as current logged user.
      *
      * @param content Content of Expression in Conversation.
      * @return Newly created Conversation.
      */
     public Conversation conversation(String content) {
         return conversation(instance.getLoggedInMember(), content, null);
     }
 
     /**
      * Adds new Conversation.
      *
      * @param author Author of Expression in Conversation.
      * @param content Content of Expression in Conversation.
      * @return Newly created Conversation.
      */
     public Conversation conversation(Member author, String content) {
         return conversation(author, content, null);
     }
 
     /**
      * Adds new Conversation.
      * @param author Author of Expression in Conversation.
      * @param content Content of Expression in Conversation.
      * @param attributes Additional attributes that should be set for Conversation Expression.
      * @return Newly created Conversation
      */
     public Conversation conversation(Member author, String content, Map<String, Object> attributes) {
         Conversation conversation = new Conversation(instance);
         Expression expression = new Expression(instance);
         DateTime utcNow = new DateTime(DateTime.convertLocalToUtc(new Date()));
         expression.setAuthor(author);
         expression.setAuthoredAt(utcNow);
         expression.setContent(content);
        expression.setBelongsTo(conversation);
         addAttributes(expression, attributes);
         conversation.save();
 
         return conversation;
     }
 
     /**
      * Fill attributes for specified entity.
      *
      * @param entity     entity to fill.
      * @param attributes Map of attributes on filling to entity.
      */
     protected void addAttributes(Entity entity, Map<String, Object> attributes) {
         if (attributes != null) {
             for (Entry<String, Object> pairs : attributes.entrySet()) {
                 entity.set(pairs.getKey(), pairs.getValue());
             }
         }
     }
 
     /**
      * Adds new Expression in Reply To an existing Expression.
      * @param author Author of Expression.
      * @param content Content of Expression.
      * @param inReplyTo Expression being replied to.
      * @return Newly created Conversation
      */
     public Expression expression(Member author, String content, Expression inReplyTo) {
         Expression expression = new Expression(instance);
         expression.setAuthor(author);
         expression.setContent(content);
         expression.setInReplyTo(inReplyTo);
         expression.save();
         return expression;
     }
 }
