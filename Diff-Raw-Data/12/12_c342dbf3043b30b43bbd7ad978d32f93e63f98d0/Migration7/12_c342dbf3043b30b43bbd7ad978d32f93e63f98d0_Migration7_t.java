 package de.deepamehta.plugins.eduzen.migrations;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import de.deepamehta.core.Topic;
 import de.deepamehta.core.TopicType;
 import de.deepamehta.core.AssociationType;
 import de.deepamehta.core.RelatedTopic;
 import de.deepamehta.core.ResultSet;
 import de.deepamehta.core.model.CompositeValue;
 import de.deepamehta.core.model.AssociationDefinitionModel;
 import de.deepamehta.core.model.AssociationTypeModel;
 import de.deepamehta.core.model.AssociationModel;
 import de.deepamehta.core.model.TopicRoleModel;
 import de.deepamehta.core.model.TopicModel;
 import de.deepamehta.core.service.Migration;
 
 
 
 public class Migration7 extends Migration {
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     private long WORKSPACE_ID = 9676;
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
     @Override
     public void run() {
 
         // set URI for default Workspace
         Topic defaultWorkspace = dms.getTopic(WORKSPACE_ID, false, null);
         defaultWorkspace.setUri("tub.eduzen.workspace_editors");
 
         // create new "EduZEN"-Workspace for users
         dms.createTopic(new TopicModel("tub.eduzen.workspace_users", "dm4.workspaces.workspace", 
             new CompositeValue().put("dm4.workspaces.name", "EduZEN Users")), null);
 
         // assign all eduzen topic types to "EduZEN Editors"-Workspace
         TopicType studyPathName = dms.getTopicType("tub.eduzen.studypath_name", null);
         assignWorkspace(studyPathName);
         TopicType moduleName = dms.getTopicType("tub.eduzen.module_name", null);
         assignWorkspace(moduleName);
         TopicType topicalareaName = dms.getTopicType("tub.eduzen.topicalarea_name", null);
         assignWorkspace(topicalareaName);
         TopicType topicalareaAlias = dms.getTopicType("tub.eduzen.topicalarea_alias", null);
         assignWorkspace(topicalareaAlias);
         TopicType courseName = dms.getTopicType("tub.eduzen.course_name", null);
         assignWorkspace(courseName);
         TopicType courseType = dms.getTopicType("tub.eduzen.course_type", null);
         assignWorkspace(courseType);
         TopicType lectureType = dms.getTopicType("tub.eduzen.lecture_type", null);
         assignWorkspace(lectureType);
         TopicType approachCorrectness = dms.getTopicType("tub.eduzen.approach_correctness", null);
         assignWorkspace(approachCorrectness);
         TopicType lecturePlace = dms.getTopicType("tub.eduzen.lecture_place", null);
         assignWorkspace(lecturePlace);
         TopicType resourceName = dms.getTopicType("tub.eduzen.resource_name", null);
         assignWorkspace(resourceName);
         TopicType resourceContent = dms.getTopicType("tub.eduzen.resource_content", null);
         assignWorkspace(resourceContent);
         TopicType excerciseObject = dms.getTopicType("tub.eduzen.excercise_object", null);
         assignWorkspace(excerciseObject);
         TopicType excerciseDifficulcy = dms.getTopicType("tub.eduzen.excercise_difficulcy", null);
         assignWorkspace(excerciseDifficulcy);
         TopicType timeframeNote = dms.getTopicType("tub.eduzen.timeframe_note", null);
         assignWorkspace(timeframeNote);
         TopicType timeframeStart = dms.getTopicType("tub.eduzen.timeframe_start", null);
         assignWorkspace(timeframeStart);
         TopicType timeframeEnd = dms.getTopicType("tub.eduzen.timeframe_end", null);
         assignWorkspace(timeframeEnd);
         TopicType practicesheetDue = dms.getTopicType("tub.eduzen.practicesheet_due", null);
         assignWorkspace(practicesheetDue);
         TopicType practicesheetName = dms.getTopicType("tub.eduzen.practicesheet_name", null);
         assignWorkspace(practicesheetName);
         TopicType practicesheetGiven = dms.getTopicType("tub.eduzen.practicesheet_given", null);
         assignWorkspace(practicesheetGiven);
         TopicType excerciseName = dms.getTopicType("tub.eduzen.excercise_name", null);
         assignWorkspace(excerciseName);
         TopicType excerciseFrozen = dms.getTopicType("tub.eduzen.excercise_frozen", null);
         assignWorkspace(excerciseFrozen);
         TopicType excerciseArchived = dms.getTopicType("tub.eduzen.excercise_archived", null);
         assignWorkspace(excerciseArchived);
         TopicType approachContent = dms.getTopicType("tub.eduzen.approach_content", null);
         assignWorkspace(approachContent);
         TopicType approachSample = dms.getTopicType("tub.eduzen.approach_sample", null);
         assignWorkspace(approachSample);
         TopicType commentCorrect = dms.getTopicType("tub.eduzen.comment_correct", null);
         assignWorkspace(commentCorrect);
         TopicType commentExplanation = dms.getTopicType("tub.eduzen.comment_explanation", null);
         assignWorkspace(commentExplanation);
         TopicType identity = dms.getTopicType("tub.eduzen.identity", null);
         assignWorkspace(identity);
         TopicType topicalarea = dms.getTopicType("tub.eduzen.topicalarea", null);
         assignWorkspace(topicalarea);
         TopicType lecture = dms.getTopicType("tub.eduzen.lecture", null);
         assignWorkspace(lecture);
         TopicType resource = dms.getTopicType("tub.eduzen.resource", null);
         assignWorkspace(resource);
         TopicType comment = dms.getTopicType("tub.eduzen.comment", null);
         assignWorkspace(comment);
         TopicType approach = dms.getTopicType("tub.eduzen.approach", null);
         assignWorkspace(approach);
         TopicType excerciseText = dms.getTopicType("tub.eduzen.excercise_text", null);
         assignWorkspace(excerciseText);
         TopicType excercise = dms.getTopicType("tub.eduzen.excercise", null);
         assignWorkspace(excercise);
         TopicType course = dms.getTopicType("tub.eduzen.course", null);
         assignWorkspace(course);
         TopicType module = dms.getTopicType("tub.eduzen.module", null);
         assignWorkspace(module);
         TopicType studypath = dms.getTopicType("tub.eduzen.studypath", null);
         assignWorkspace(studypath);
         TopicType practicesheet = dms.getTopicType("tub.eduzen.practicesheet", null);
         assignWorkspace(practicesheet);
         // assocTypes
         AssociationType contentItem = dms.getAssociationType("tub.eduzen.content_item", null);
         assignWorkspace(contentItem);
         AssociationType originItem = dms.getAssociationType("tub.eduzen.origin_item", null);
         assignWorkspace(originItem);
         AssociationType participant = dms.getAssociationType("tub.eduzen.participant", null);
         assignWorkspace(participant);
         AssociationType lecturer = dms.getAssociationType("tub.eduzen.lecturer", null);
         assignWorkspace(lecturer);
         AssociationType submitter = dms.getAssociationType("tub.eduzen.submitter", null);
         assignWorkspace(submitter);
         AssociationType author = dms.getAssociationType("tub.eduzen.author", null);
         assignWorkspace(author);
         AssociationType hint = dms.getAssociationType("tub.eduzen.hint", null);
         assignWorkspace(hint);
         AssociationType corrector = dms.getAssociationType("tub.eduzen.corrector", null);
         assignWorkspace(corrector);
         AssociationType requirement = dms.getAssociationType("tub.eduzen.requirement", null);
         assignWorkspace(requirement);
         AssociationType compatible = dms.getAssociationType("tub.eduzen.compatible", null);
         assignWorkspace(compatible);
         AssociationType identical = dms.getAssociationType("tub.eduzen.identical", null);
         assignWorkspace(identical);
        // maybe we need to add, some dm4.types to the EduZEN Editors-Workspace too? 
        // this was just tested with "Web Resource", admin can create and edit instances of such, postponing issue.
 
         // fixme: cannot create new AssocDefModel via this call with a new ViewConfig (editable=false)
         // fixme: cannot be edited, does this new assoc also needs to be associated with a workspace, if so, how?
         identity.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
             "tub.eduzen.identity", "dm4.accesscontrol.username", "dm4.core.one", "dm4.core.one"));
 
         // create LV AssocType manually
         dms.createAssociationType(new AssociationTypeModel("tub.eduzen.lecture_content", 
           "Lecture Content", "dm4.core.text"), null);
        // assign new assoctype to workspace
         AssociationType lectureContent = dms.getAssociationType("tub.eduzen.lecture_content", null);
        assignWorkspace(lectureContent);
        lectureContent.getViewConfig().addSetting("dm4.webclient.view_config", "dm4.webclient.color", "#1072c8");
     }
 
 
     // === Workspace ===
 
     private void assignWorkspace(Topic topic) {
         if (hasWorkspace(topic)) {
             return;
         }
         dms.createAssociation(new AssociationModel("dm4.core.aggregation",
             new TopicRoleModel(topic.getId(), "dm4.core.whole"),
             new TopicRoleModel(WORKSPACE_ID, "dm4.core.part")
         ), null);
     }
 
     private boolean hasWorkspace(Topic topic) {
         return topic.getRelatedTopics("dm4.core.aggregation", "dm4.core.whole", "dm4.core.part",
             "dm4.workspaces.workspace", false, false, 0, null).getSize() > 0;
     }
 
 }
