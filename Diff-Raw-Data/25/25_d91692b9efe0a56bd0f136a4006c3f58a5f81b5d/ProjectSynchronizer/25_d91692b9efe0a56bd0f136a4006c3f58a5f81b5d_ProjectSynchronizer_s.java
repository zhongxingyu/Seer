 package org.dodgybits.android.shuffle.server.tracks;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.ContextWrapper;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.util.Xml;
 import org.dodgybits.android.shuffle.R;
 import org.dodgybits.android.shuffle.model.Context;
 import org.dodgybits.android.shuffle.model.Project;
 import org.dodgybits.android.shuffle.provider.Shuffle;
 import org.dodgybits.android.shuffle.util.BindingUtils;
 import org.dodgybits.android.shuffle.util.ModelUtils;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlSerializer;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Morten Nielsen
  */
 public final class ProjectSynchronizer extends Synchronizer<Project> {
     private final String tracksUrl;
 
     public ProjectSynchronizer(ContentResolver contentResolver, Resources resources, WebClient client, ContextWrapper activity, TracksSynchronizer tracksSynchronizer, String tracksUrl, int basePercent) {
         super(contentResolver, tracksSynchronizer, client, resources, activity, basePercent);
 
         this.tracksUrl = tracksUrl;
     }
 
 
     @Override
     protected String readingRemoteText() {
        return resources.getString(R.string.readingLocalProjects);
     }
 
     @Override
     protected String processingText() {
         return resources.getString(R.string.processingProjects);
     }
 
     @Override
     protected String readingLocalText() {
         return resources.getString(R.string.readingLocalProjects);
     }
 
     @Override
     protected String stageFinishedText() {
         return resources.getString(R.string.doneWithProjects);
     }
 
     @Override
     protected void saveLocalEntityFromRemote(Project project) {
         ModelUtils.insertProject(activity, project);
     }
 
     protected Project createMergedLocalEntity(Project localProject, Project newContext) {
         return new Project(localProject.id, newContext.name,
                 newContext.defaultContextId, localProject.archived,
                 newContext.tracksId, newContext.modified);
     }
 
     protected String createDocumentForEntity(Project project) {
         XmlSerializer serializer = Xml.newSerializer();
         StringWriter writer = new StringWriter();
 
         try {
             serializer.setOutput(writer);
 
             serializer.startTag("", "project");
             Date date = new Date();
             serializer.startTag("", "created-at").attribute("", "type", "datetime").text(simpleDateFormat.format(date)).endTag("", "created-at");
             String contextId = findContextRemoteId(project);
             if(contextId != null)            serializer.startTag("", "default-context-id").attribute("", "type", "integer").text(contextId).endTag("", "default-context-id");
             serializer.startTag("", "name").text(project.name).endTag("", "name");
             serializer.startTag("", "state").text(project.archived ? "hidden": "active").endTag("", "state");
             serializer.startTag("", "updated-at").attribute("", "type", "datetime").text(simpleDateFormat.format(date)).endTag("", "updated-at");
             serializer.endTag("", "project");
 
             serializer.flush();
         } catch (IOException ignored) {
 
         }
 
 
         return writer.toString();
     }
 
     private String findContextRemoteId(Project project) {
         Context context = BindingUtils.fetchContextById(activity, project.defaultContextId);
         if(context != null && context.tracksId != null)
             return context.tracksId.toString();
         else
             return null;
     }
 
 
     protected Project parseSingleEntity(XmlPullParser parser) throws ParseException {
         try {
             int eventType = parser.getEventType();
             String projectName = null;
             Long trackId = null;
             Long trackModifiedDate = null;
                         boolean activeState = false;
 
                 Long defaultContextId=null;
             boolean done = false;
 
             SimpleDateFormat format = simpleDateFormat;
             while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                 String name = parser.getName();
 
                 switch (eventType) {
                     case XmlPullParser.START_DOCUMENT:
 
                         break;
                     case XmlPullParser.START_TAG:
 
 
                         if (name.equalsIgnoreCase("name")) {
                             projectName = parser.nextText();
                         } else if (name.equalsIgnoreCase("id")) {
                             trackId = Long.parseLong(parser.nextText());
                         } else if (name.equalsIgnoreCase("updated-at")) {
                             trackModifiedDate = format.parse(parser.nextText()).getTime();
                         } else if (name.equalsIgnoreCase("default-context-trackId")) {
                             defaultContextId = findContextByRemoteId( Long.parseLong(parser.nextText())).id;
                         }else if (name.equalsIgnoreCase("state")) {
                             activeState = !parser.nextText().equalsIgnoreCase("active");
                         }
 
 
 
 
                         break;
                     case XmlPullParser.END_TAG:
                         if (name.equalsIgnoreCase("project") && projectName != null) {
 
                             return new Project(null, projectName, defaultContextId, activeState, trackId, trackModifiedDate);
                         }
                         break;
                 }
                 eventType = parser.next();
             }
         } catch (IOException e) {
             throw new ParseException("Unable to parse context", 0);
         } catch (XmlPullParserException e) {
             throw new ParseException("Unable to parse context", 0);
         }
         return null;
     }
 
     private Context findContextByRemoteId(long tracksId) {
        		Context context = null;
 
 			Cursor contextCursor = contentResolver.query(
 					Shuffle.Contexts.CONTENT_URI, Shuffle.Contexts.cFullProjection, 
 					"tracks_id = "+tracksId, null, null);
 			if (contextCursor.moveToFirst()) {
 				context = BindingUtils.readContext(contextCursor, activity.getResources());
 			}
         contextCursor.close();
 		return context;
     }
 
     @Override
     protected String createEntityUrl(Project project) {
         return tracksUrl+ "/projects/" + project.tracksId + ".xml";
     }
 
 
     @Override
     protected String endIndexTag() {
         return "projects";
     }
 
     @Override
     protected String entityIndexUrl() {
         return tracksUrl+ "/projects.xml";
     }
 
     protected boolean removeLocalEntity(Project project) {
         return 1 == contentResolver.delete(
                 Shuffle.Projects.CONTENT_URI, Shuffle.Projects._ID + " = " + project.id,
                 null);
     }
 
     protected void saveLocalEntity(Project project) {
         ContentValues values = new ContentValues();
         BindingUtils.writeProject(values, project);
         contentResolver.update(Shuffle.Projects.CONTENT_URI, values, 
                 Shuffle.Projects._ID + "=?", new String[]{String.valueOf(project.id)});
     }
 
     protected Map<String, Project> getShuffleEntities(ContentResolver contentResolver, Resources resources) {
 
 
         Cursor cursor = contentResolver.query(
                 Shuffle.Projects.CONTENT_URI, Shuffle.Projects.cFullProjection,
                 null, null, null);
 
 
         Map<String, Project> list = new HashMap<String, Project>();
 
         while (cursor.moveToNext()) {
             Project context = BindingUtils.readProject(cursor);
 
             list.put(context.name, context);
 
 
         }
         cursor.close();
         return list;
     }
 }
