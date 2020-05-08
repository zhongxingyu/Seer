 package de.twenty11.skysail.server.ext.notes.resources;
 
 import org.restlet.data.Form;
 import org.restlet.resource.Delete;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.ResourceException;
 
 import de.twenty11.skysail.common.responses.EntityDetailsResponse;
 import de.twenty11.skysail.common.responses.FormResponse;
 import de.twenty11.skysail.common.responses.SkysailResponse;
 import de.twenty11.skysail.common.responses.SuccessResponse;
 import de.twenty11.skysail.server.core.restlet.EntityServerResource;
 import de.twenty11.skysail.server.ext.notes.NotesApplication;
 import de.twenty11.skysail.server.ext.notes.domain.Note;
 
 /**
  * takes care of ".../notes/" and ".../notes/{id}" requests.
  * 
  */
 public class NoteResource extends EntityServerResource<Note> {
 
     private Long noteId;
     private NotesApplication app;
 
     public NoteResource() {
         app = (NotesApplication) getApplication();
     }
 
     @Override
     protected void doInit() throws ResourceException {
         if (getRequest().getAttributes().get("id") != null) {
             noteId = new Long((String) getRequest().getAttributes().get("id"));
         }
     }
 
     @Override
     public SkysailResponse<Note> getEntity() {
         return super.getEntity("Note #" + noteId);
     }
 
     @Override
     public Note getData() {
        return app.getNotesRepository().getById(new Long(noteId));
     }
 
     @Override
     public Note getData(Form form) {
         Note note = new Note(null, form.getFirstValue("title"), form.getFirstValue("content"));
         note.setOwner(app.getCurrentUser());
         return note;
     }
 
     @Get("htmlform")
     public FormResponse<Note> createForm() {
         FormResponse<Note> formResponse = new FormResponse<Note>(new Note(null, "", ""),
                 NotesApplication.getPostNewNotePath());
         formResponse.setMessage("Add a new folder");
         return formResponse;
     }
 
     @Delete
     public void deleteNote() {
         app.getNotesRepository().delete(noteId);
     }
 
     // @Override
    // TODO check addNoteResource
     @Post("x-www-form-urlencoded:html")
     public EntityDetailsResponse addEntity2(Form form) {
 
         Note entity = getData(form);
         // Set<ConstraintViolation<T>> violations = validate(entity);
         // if (violations.size() > 0) {
         // return new ConstraintViolationsResponse(entity, violations);
         // }
         // return addEntity(entity);
 
        NotesApplication app = (NotesApplication) getApplication();
         app.getNotesRepository().add(entity);
         // return new FoldersResource().getEntities();
         EntityDetailsResponse response = new EntityDetailsResponse(entity, "note");
         response.setMessage("note entity details");
         return response;
     }
 
     // @Put("x-www-form-urlencoded:html")
     // public EntityDetailsResponse put(Form form) {
     //
     // Note entity = getData(form);
     // entity.setPid(noteId);
     // // Set<ConstraintViolation<T>> violations = validate(entity);
     // // if (violations.size() > 0) {
     // // return new ConstraintViolationsResponse(entity, violations);
     // // }
     // // return addEntity(entity);
     //
     // NotesApplication app = (NotesApplication) getApplication();
     // app.getNotesRepository().update(entity);
     // // return new FoldersResource().getEntities();
     // EntityDetailsResponse response = new EntityDetailsResponse(entity, "note");
     // response.setMessage("note entity details");
     // return response;
     // }
 
     @Override
     public SkysailResponse<?> addEntity(Note entity) {
         app.getNotesRepository().add(entity);
         return new SuccessResponse<Note>(entity);
     }
 
     @Override
     public String getMessage(String key) {
         // TODO Auto-generated method stub
         return null;
     }
 
 }
