 package com.mlj.dms;
 
 import com.mlj.documents.DocumentRegistrationService;
 import com.mlj.documents.RegisterDocumentCommand;
 import com.mlj.documents.SupersedeDocumentCommand;
 import com.mlj.views.DocumentDto;
 import com.mlj.views.DocumentHistoryView;
 import com.mlj.views.DocumentListView;
 import com.mlj.views.DocumentResource;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.List;
 
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 import static org.springframework.web.bind.annotation.RequestMethod.PUT;
 
 @Controller
 @RequestMapping("/dms/docs")
 public class DocumentManagementControl {
 
     private final DocumentRegistrationService documentRegistrationService;
     private final DocumentListView documentListView;
     private final DocumentHistoryView documentHistoryView;
 
     public DocumentManagementControl(DocumentRegistrationService documentRegistrationService,
                                      DocumentListView documentListView,
                                      DocumentHistoryView documentHistoryView) {
         this.documentRegistrationService = documentRegistrationService;
         this.documentListView = documentListView;
         this.documentHistoryView = documentHistoryView;
     }
 
     @RequestMapping(method = GET, produces = "application/json")
     @ResponseBody
     public List<DocumentResource> list() {
         return documentListView.list();
     }
 
     @RequestMapping(method = POST, consumes = "application/json")
     @ResponseStatus(HttpStatus.CREATED)
     public void register(@RequestBody RegisterDocumentCommand command) {
         documentRegistrationService.registerDocument(command);
     }
 
     @RequestMapping(method = PUT, value = "/{documentId}", consumes = "application/json")
     @ResponseStatus(HttpStatus.OK)
    public void supersede(@PathVariable String documentId, @RequestBody SupersedeDocumentCommand command) {
         documentRegistrationService.supersedeDocument(documentId, command);
     }
 
     @RequestMapping(method = GET, value = "/{documentId}/history", produces = "application/json")
     @ResponseBody
     public List<DocumentResource> history(@PathVariable String documentId) {
         return documentHistoryView.history(documentId);
     }
 
 }
