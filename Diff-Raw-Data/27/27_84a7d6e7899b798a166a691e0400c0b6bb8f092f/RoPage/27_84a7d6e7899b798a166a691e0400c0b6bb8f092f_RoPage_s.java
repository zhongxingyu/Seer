 package pl.psnc.dl.wf4ever.portal.pages.ro;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.swing.tree.TreeModel;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.RestartResponseException;
 import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.upload.FileUpload;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.UrlDecoder;
 import org.apache.wicket.request.UrlEncoder;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.purl.wf4ever.rosrs.client.common.ROSRService;
 import org.purl.wf4ever.rosrs.client.common.ROService;
 import org.purl.wf4ever.rosrs.client.common.Vocab;
 import org.scribe.model.Token;
 
 import pl.psnc.dl.wf4ever.portal.MySession;
 import pl.psnc.dl.wf4ever.portal.PortalApplication;
 import pl.psnc.dl.wf4ever.portal.model.AggregatedResource;
 import pl.psnc.dl.wf4ever.portal.model.Annotation;
 import pl.psnc.dl.wf4ever.portal.model.Creator;
 import pl.psnc.dl.wf4ever.portal.model.ResourceGroup;
 import pl.psnc.dl.wf4ever.portal.model.RoTreeModel;
 import pl.psnc.dl.wf4ever.portal.model.Statement;
 import pl.psnc.dl.wf4ever.portal.pages.ErrorPage;
 import pl.psnc.dl.wf4ever.portal.pages.TemplatePage;
 import pl.psnc.dl.wf4ever.portal.pages.util.MyFeedbackPanel;
 import pl.psnc.dl.wf4ever.portal.services.OAuthException;
 import pl.psnc.dl.wf4ever.portal.services.RoFactory;
 import pl.psnc.dl.wf4ever.portal.utils.RDFFormat;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.sun.jersey.api.client.ClientResponse;
 
 /**
  * The Research Object page.
  * 
  * @author piotrekhol
  * 
  */
 public class RoPage extends TemplatePage {
 
     /** id. */
     private static final long serialVersionUID = 1L;
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(RoPage.class);
 
     /** RO URI. */
     URI roURI;
 
     /** Resources aggregated by the RO. */
     Map<URI, AggregatedResource> resources;
 
     /** Can the user edit the RO. */
     boolean canEdit = false;
 
     /** The part showing the RO structure. */
     final RoViewerBox roViewerBox;
 
     /** The part showing the RO annotations. */
     final AnnotatingBox annotatingBox;
 
     /** The conceptual model of RO resource. */
     private RoTreeModel conceptualResourcesTree;
 
     /** The physical model of RO resource. */
     private RoTreeModel physicalResourcesTree;
 
     /** The modal window for editing annotations. */
     final StatementEditModal stmtEditForm;
 
     /** The modal window for editing relations. */
     final RelationEditModal relEditForm;
 
     /** The modal window for importing annotations. */
     private final ImportAnnotationModal importAnnotationModal;
 
     /** The modal window for adding resources. */
     private UploadResourceModal uploadResourceModal;
 
     /** The feedback panel. */
     private MyFeedbackPanel feedbackPanel;
 
 
     /**
      * Constructor.
      * 
      * @param parameters
      *            page parameters
      * @throws URISyntaxException
      *             if URIs returned by the RODL are incorrect
      * @throws OAuthException
      *             if it cannot connect to RODL
      * @throws IOException
      *             if it cannot connect to RODL
      */
     @SuppressWarnings("serial")
     public RoPage(final PageParameters parameters)
             throws URISyntaxException, OAuthException, IOException {
         super(parameters);
         if (!parameters.get("ro").isEmpty()) {
             roURI = new URI(UrlDecoder.QUERY_INSTANCE.decode(parameters.get("ro").toString(), "UTF-8"));
         } else {
             throw new RestartResponseException(ErrorPage.class, new PageParameters().add("message",
                 "The RO URI is missing"));
         }
 
         feedbackPanel = new MyFeedbackPanel("feedbackPanel");
         feedbackPanel.setOutputMarkupId(true);
         add(feedbackPanel);
 
         if (MySession.get().isSignedIn()) {
             List<URI> uris = ROSRService.getROList(rodlURI, MySession.get().getdLibraAccessToken());
             canEdit = uris.contains(roURI);
         }
         add(new Label("title", roURI.toString()));
 
         final CompoundPropertyModel<AggregatedResource> itemModel = new CompoundPropertyModel<AggregatedResource>(
                 (AggregatedResource) null);
         roViewerBox = new RoViewerBox(this, itemModel, new PropertyModel<TreeModel>(this, "conceptualResourcesTree"),
                 new PropertyModel<TreeModel>(this, "physicalResourcesTree"), "loadingROFragment");
         add(roViewerBox);
         annotatingBox = new AnnotatingBox(this, itemModel);
         add(annotatingBox);
         annotatingBox.selectedStatements.clear();
         add(new DownloadMetadataModal("downloadMetadataModal", this));
         uploadResourceModal = new UploadResourceModal("uploadResourceModal", this,
                 ((PortalApplication) getApplication()).getResourceGroups());
         add(uploadResourceModal);
         stmtEditForm = new StatementEditModal("statementEditModal", RoPage.this, new CompoundPropertyModel<Statement>(
                 (Statement) null));
         add(stmtEditForm);
         relEditForm = new RelationEditModal("relationEditModal", RoPage.this, new CompoundPropertyModel<Statement>(
                 (Statement) null), "loadingROFragment");
         add(relEditForm);
         importAnnotationModal = new ImportAnnotationModal("importAnnotationModal", this, itemModel);
         add(importAnnotationModal);
 
         add(new RoEvoBox("roEvoBox", ((PortalApplication) getApplication()).getSparqlEndpointURI(), roURI));
 
         add(new AbstractDefaultAjaxBehavior() {
 
             @Override
             protected void respond(AjaxRequestTarget target) {
                 try {
                     if (getConceptualResourcesTree() == null || getPhysicalResourcesTree() == null) {
                         PortalApplication app = ((PortalApplication) getApplication());
                         Map<URI, Creator> usernames = MySession.get().getUsernames();
                         OntModel model = ROSRService.createManifestAndAnnotationsModel(roURI);
                         resources = RoFactory.getAggregatedResources(model, rodlURI, roURI, usernames);
                         RoFactory.assignResourceGroupsToResources(model, roURI, app.getResourceGroups(), resources);
                         setConceptualResourcesTree(RoFactory.createConceptualResourcesTree(roURI, resources));
                         setPhysicalResourcesTree(RoFactory.createPhysicalResourcesTree(roURI, resources));
 
                         RoFactory.createRelations(model, roURI, resources);
                         // FIXME this has been turned off because it takes too much time and is generally a hack
                         //						RoFactory.createStabilities(model, roURI, resources);
 
                         roViewerBox.onRoTreeLoaded();
                         relEditForm.onRoTreeLoaded();
                         target.add(roViewerBox);
                     }
                 } catch (URISyntaxException e) {
                     LOG.error(e);
                     error(e);
                 }
                 target.add(feedbackPanel);
             }
 
 
             @Override
             public void renderHead(final Component component, final IHeaderResponse response) {
                 super.renderHead(component, response);
                 response.renderOnDomReadyJavaScript(getCallbackScript().toString());
             }
 
         });
     }
 
 
     public RoTreeModel getConceptualResourcesTree() {
         return conceptualResourcesTree;
     }
 
 
     public void setConceptualResourcesTree(RoTreeModel conceptualResourcesTree) {
         this.conceptualResourcesTree = conceptualResourcesTree;
     }
 
 
     public RoTreeModel getPhysicalResourcesTree() {
         return physicalResourcesTree;
     }
 
 
     public void setPhysicalResourcesTree(RoTreeModel physicalResourcesTree) {
         this.physicalResourcesTree = physicalResourcesTree;
     }
 
 
     /**
      * A utility class for creating an external link to a property of a statement.
      * 
      * @author piotrekhol
      * 
      */
     @SuppressWarnings("serial")
     class ExternalLinkFragment extends Fragment {
 
         /**
          * Constructor.
          * 
          * @param id
          *            wicket id
          * @param markupId
          *            fragment wicket id
          * @param markupProvider
          *            which component defines the fragment
          * @param model
          *            statement model
          * @param property
          *            property of a statement
          */
         public ExternalLinkFragment(String id, String markupId, MarkupContainer markupProvider,
                 CompoundPropertyModel<Statement> model, String property) {
             super(id, markupId, markupProvider, model);
             add(new ExternalLink("link", model.<String> bind(property), model.<String> bind(property)));
         }
     }
 
 
     /**
      * A utility class for creating links to resources inside the RO.
      * 
      * @author piotrekhol
      * 
      */
     @SuppressWarnings("serial")
     class InternalLinkFragment extends Fragment {
 
         /**
          * Constructor.
          * 
          * @param id
          *            wicket id
          * @param markupId
          *            fragment wicket id
          * @param markupProvider
          *            which component defines the fragment
          * @param statement
          *            the statement for which the link is created
          */
         public InternalLinkFragment(String id, String markupId, MarkupContainer markupProvider, Statement statement) {
             super(id, markupId, markupProvider);
             String internalName = "./" + roURI.relativize(statement.getSubjectURI()).toString();
             add(new AjaxLink<String>("link", new Model<String>(internalName)) {
 
                 @Override
                 public void onClick(AjaxRequestTarget target) {
                     // TODO Auto-generated method stub
 
                 }
             }.add(new Label("name", internalName.toString())));
         }
     }
 
 
     /**
      * A utility class for creating a link for editing a statement.
      * 
      * @author piotrekhol
      * 
      */
     @SuppressWarnings("serial")
     class EditLinkFragment extends Fragment {
 
         /**
          * Constructor.
          * 
          * @param id
          *            wicket id
          * @param markupId
          *            fragment wicket id
          * @param markupProvider
          *            which component defines the fragment
          * @param link
          *            link defining the action upon click
          */
         public EditLinkFragment(String id, String markupId, MarkupContainer markupProvider,
                 AjaxFallbackLink<String> link) {
             super(id, markupId, markupProvider);
             add(link);
         }
     }
 
 
     /**
      * Called when the user wants to add a local resource.
      * 
      * @param target
      *            response target
      * @param uploadedFile
      *            the uploaded file
      * @param selectedResourceGroups
      *            resource groups of the file
      * @throws IOException
      *             error connecting to RODL
      */
     void onResourceAdd(AjaxRequestTarget target, final FileUpload uploadedFile,
             Set<ResourceGroup> selectedResourceGroups)
             throws IOException {
         URI resourceURI = roURI.resolve(UrlEncoder.PATH_INSTANCE.encode(uploadedFile.getClientFileName(), "UTF-8"));
         ClientResponse response = ROSRService.uploadResource(resourceURI, uploadedFile.getInputStream(),
             uploadedFile.getContentType(), MySession.get().getdLibraAccessToken());
         if (response.getStatus() != HttpServletResponse.SC_OK && response.getStatus() != HttpServletResponse.SC_CREATED) {
             throw new IOException(response.getClientResponseStatus().getReasonPhrase());
         }
         OntModel manifestModel = ROSRService.createManifestModel(roURI);
         Individual individual = manifestModel.createResource(resourceURI.toString()).as(Individual.class);
         for (ResourceGroup resourceGroup : selectedResourceGroups) {
             individual.addRDFType(manifestModel.createResource(resourceGroup.getRdfClasses().iterator().next()
                     .toString()));
         }
         response = ROSRService.uploadManifestModel(roURI, manifestModel, MySession.get().getdLibraAccessToken());
         if (response.getStatus() != HttpServletResponse.SC_OK) {
             throw new IOException(response.getClientResponseStatus().getReasonPhrase());
         }
 
         AggregatedResource resource = RoFactory.createResource(rodlURI, roURI, resourceURI, true, MySession.get()
                 .getUsernames());
         resource.getMatchingGroups().addAll(selectedResourceGroups);
         getConceptualResourcesTree().addAggregatedResource(resource, true);
         getPhysicalResourcesTree().addAggregatedResource(resource, false);
         target.add(roViewerBox);
 
         resources.put(resourceURI, resource);
         RoFactory.addRelation(resources.get(roURI), Vocab.ORE_AGGREGATES, resource);
 
         roViewerBox.renderJSComponents(resources, target);
     }
 
 
     /**
      * Called when a user wants to a delete an aggregated resource.
      * 
      * @param resource
      *            remote or local resource
      * @param target
      *            request target
      * @throws IOException
      *             when cannot connect to RODL
      */
     public void onResourceDelete(AggregatedResource resource, AjaxRequestTarget target)
             throws IOException {
         ClientResponse response = ROSRService.deleteResource(resource.getURI(), MySession.get().getdLibraAccessToken());
         if (response.getStatus() != HttpServletResponse.SC_NO_CONTENT) {
             if (response.getStatus() == HttpServletResponse.SC_NOT_FOUND) {
                 onRemoteResourceDelete(resource, target);
             } else {
                 throw new IOException(response.getClientResponseStatus().getReasonPhrase());
             }
         }
         getConceptualResourcesTree().removeAggregatedResource(resource);
         getPhysicalResourcesTree().removeAggregatedResource(resource);
 
         resources.remove(resource.getURI());
         for (Entry<String, AggregatedResource> entry : resource.getInverseRelations().entries()) {
             entry.getValue().getRelations().remove(entry.getKey(), resource);
         }
         roViewerBox.renderJSComponents(resources, target);
         target.add(roViewerBox);
     }
 
 
     /**
      * Called when a user wants to delete a remote resource.
      * 
      * @param resource
      *            a remote resource
      * @param target
      *            request target
      * @throws IOException
      *             can't connect to RODL
      */
     private void onRemoteResourceDelete(AggregatedResource resource, AjaxRequestTarget target)
             throws IOException {
         OntModel manifestModel = ROSRService.createManifestModel(roURI);
         // HACK this shouldn't be deleted by portal but rather by RODL
         Resource ro = manifestModel.createResource(roURI.toString());
         URI absoluteResourceURI = roURI.resolve(resource.getURI());
         Individual individual = manifestModel.createResource(absoluteResourceURI.toString()).as(Individual.class);
         if (!ro.hasProperty(Vocab.ORE_AGGREGATES, individual)) {
             throw new IOException("Not found");
         }
 
         manifestModel.remove(ro, Vocab.ORE_AGGREGATES, individual);
 
         ResIterator it2 = manifestModel.listSubjectsWithProperty(Vocab.ORE_ANNOTATES_AGGREGATED_RESOURCE, individual);
         while (it2.hasNext()) {
             Resource ann = it2.next();
             manifestModel.remove(ann, Vocab.ORE_ANNOTATES_AGGREGATED_RESOURCE, individual);
             if (!ann.hasProperty(Vocab.ORE_ANNOTATES_AGGREGATED_RESOURCE)) {
                 Resource annBody = ann.getPropertyResourceValue(Vocab.AO_BODY);
                 if (annBody != null && annBody.isURIResource()) {
                     URI annBodyURI = URI.create(annBody.getURI());
                     ROSRService.deleteResource(annBodyURI, ((MySession) getSession()).getdLibraAccessToken());
                 }
                 manifestModel.removeAll(ann, null, null);
                 manifestModel.removeAll(null, null, ann);
             }
         }
 
         ClientResponse response = ROSRService.uploadManifestModel(roURI, manifestModel, MySession.get()
                 .getdLibraAccessToken());
         if (response.getStatus() != HttpServletResponse.SC_OK) {
             throw new IOException(response.getClientResponseStatus().getReasonPhrase());
         }
     }
 
 
     /**
      * Called when the currently selected aggregated resource has changed.
      * 
      * @param target
      *            request target
      */
     public void onResourceSelected(AjaxRequestTarget target) {
         annotatingBox.selectedStatements.clear();
         target.add(annotatingBox);
     }
 
 
     /**
      * Called when a remote resource is added.
      * 
      * @param target
      *            request target
      * @param resourceURI
      *            remote resource URI
      * @param downloadURI
      *            remote resource downloadURI (unused)
      * @param selectedTypes
      *            resource groups that this resource belongs to
      * @throws IOException
      *             can't connect to RODL
      */
     public void onRemoteResourceAdded(AjaxRequestTarget target, URI resourceURI, URI downloadURI,
             Set<ResourceGroup> selectedTypes)
             throws IOException {
         URI absoluteResourceURI = roURI.resolve(resourceURI);
         //		URI absoluteDownloadURI = (downloadURI != null ? roURI.resolve(downloadURI) : null);
         OntModel manifestModel = ROSRService.createManifestModel(roURI);
         // HACK this shouldn't be added by portal but rather by RODL
         Resource ro = manifestModel.createResource(roURI.toString());
         Individual individual = manifestModel.createResource(absoluteResourceURI.toString()).as(Individual.class);
         ro.addProperty(Vocab.ORE_AGGREGATES, individual);
         individual.addProperty(DCTerms.creator, manifestModel.createResource(MySession.get().getUserURI().toString()));
         individual.addRDFType(Vocab.RO_RESOURCE);
         for (ResourceGroup resourceGroup : selectedTypes) {
             individual.addRDFType(manifestModel.createResource(resourceGroup.getRdfClasses().iterator().next()
                     .toString()));
         }
         ClientResponse response = ROSRService.uploadManifestModel(roURI, manifestModel, MySession.get()
                 .getdLibraAccessToken());
         if (response.getStatus() != HttpServletResponse.SC_OK) {
             throw new IOException(response.getClientResponseStatus().getReasonPhrase());
         }
 
         AggregatedResource resource = RoFactory.createResource(rodlURI, roURI, absoluteResourceURI, true, MySession
                 .get().getUsernames());
         resource.getMatchingGroups().addAll(selectedTypes);
         getConceptualResourcesTree().addAggregatedResource(resource, true);
         getPhysicalResourcesTree().addAggregatedResource(resource, false);
         target.add(roViewerBox);
 
         resources.put(absoluteResourceURI, resource);
         RoFactory.addRelation(resources.get(roURI), Vocab.ORE_AGGREGATES, resource);
 
         roViewerBox.renderJSComponents(resources, target);
     }
 
 
     /**
      * Called when the user wants to create a new annotation.
      * 
      * @param statements
      *            a list of statements to be put in the annotation
      * @throws URISyntaxException
      *             URIs retrieved from RODL are incorrect
      * @throws IOException
      *             requests to RODL returned incorrect responses
      */
     void onStatementAdd(List<Statement> statements)
             throws URISyntaxException, IOException {
         Token dLibraToken = MySession.get().getdLibraAccessToken();
         URI annURI = ROService.createAnnotationURI(null, roURI);
         URI bodyURI = ROService.createAnnotationBodyURI(roURI, statements.get(0).getSubjectURI());
         ClientResponse res = ROSRService.addAnnotation(rodlURI, roURI, annURI, statements.get(0).getSubjectURI(),
             bodyURI, MySession.get().getUserURI(), dLibraToken);
         if (res.getStatus() != HttpServletResponse.SC_OK) {
             throw new IOException("Error when adding annotation: " + res.getClientResponseStatus());
         }
         List<com.hp.hpl.jena.rdf.model.Statement> jenaStatements = new ArrayList<>();
         for (Statement s : statements) {
             jenaStatements.add(s.createJenaStatement());
         }
         res = ROSRService.uploadResource(bodyURI, jenaStatements, dLibraToken);
         if (res.getStatus() != HttpServletResponse.SC_CREATED) {
             ROSRService.deleteAnnotationAndBody(roURI, annURI, dLibraToken);
             throw new IOException("Error when adding annotation: " + res.getClientResponseStatus());
         }
     }
 
 
     /**
      * User has edited a statement of an annotation.
      * 
      * @param statement
      *            the statement
      * @throws IOException
      *             requests to RODL returned incorrect responses
      */
     void onStatementEdit(Statement statement)
             throws IOException {
         Token dLibraToken = MySession.get().getdLibraAccessToken();
         Annotation ann = statement.getAnnotation();
         ClientResponse res = ROSRService.uploadResource(ann.getBodyURI(), RoFactory.wrapAnnotationBody(ann.getBody()),
             "application/rdf+xml", dLibraToken);
         if (res.getStatus() != HttpServletResponse.SC_OK) {
             throw new IOException("Error when adding statement: " + res.getClientResponseStatus());
         }
     }
 
 
     /**
      * Called after an annotation has been created or edited.
      * 
      * @param target
      *            request target.
      */
     void onStatementAddedEdited(AjaxRequestTarget target) {
         //FIXME isn't it the same as the next one?
         this.annotatingBox.getModelObject().setAnnotations(
             RoFactory.createAnnotations(rodlURI, roURI, this.annotatingBox.getModelObject().getURI(), MySession.get()
                     .getUsernames()));
         target.add(roViewerBox.infoPanel);
         target.add(annotatingBox);
     }
 
 
     /**
      * Called when a relation is added or edited.
      * 
      * @param statement
      *            the relation
      * @param target
      *            request target
      */
     void onRelationAddedEdited(Statement statement, AjaxRequestTarget target) {
         this.annotatingBox.getModelObject().setAnnotations(
             RoFactory.createAnnotations(rodlURI, roURI, this.annotatingBox.getModelObject().getURI(), MySession.get()
                     .getUsernames()));
         AggregatedResource subjectAR = resources.get(statement.getSubjectURI());
         AggregatedResource objectAR = resources.get(statement.getObjectURI());
         RoFactory.addRelation(subjectAR, statement.getPropertyURI(), objectAR);
         target.add(roViewerBox.infoPanel);
         target.add(annotatingBox);
     }
 
 
     /**
      * Called when an annotation body has been uploaded.
      * 
      * @param target
      *            request target
      * @param uploadedFile
      *            uploaded file
      * @param aggregatedResource
      *            resource annotated
      * @throws IOException
      *             problems with connecting to RODL
      * @throws URISyntaxException
      *             problems with creating the annotation
      */
     public void onAnnotationImport(AjaxRequestTarget target, FileUpload uploadedFile,
             AggregatedResource aggregatedResource)
             throws IOException, URISyntaxException {
         MySession session = (MySession) getSession();
         URI annURI = ROService.createAnnotationURI(null, roURI);
         URI bodyURI = ROService.createAnnotationBodyURI(roURI, aggregatedResource.getURI());
         ClientResponse response = ROSRService.addAnnotation(rodlURI, roURI, annURI, aggregatedResource.getURI(),
             bodyURI, session.getUserURI(), session.getdLibraAccessToken());
         if (response.getStatus() != HttpServletResponse.SC_OK) {
             LOG.error(response.getEntity(String.class));
             throw new IOException(response.getClientResponseStatus().getReasonPhrase());
         }
 
         String contentType = RDFFormat.forFileName(uploadedFile.getClientFileName(), RDFFormat.RDFXML)
                 .getDefaultMIMEType();
         response = ROSRService.uploadResource(bodyURI, uploadedFile.getInputStream(), contentType, MySession.get()
                 .getdLibraAccessToken());
         if (response.getStatus() != HttpServletResponse.SC_CREATED) {
             ROSRService.deleteAnnotationAndBody(roURI, annURI, session.getdLibraAccessToken());
             LOG.error(response.getEntity(String.class));
             throw new IOException(response.getClientResponseStatus().getReasonPhrase());
         }
 
         setResponsePage(RoPage.class, getPageParameters());
     }
 
 
     public MyFeedbackPanel getFeedbackPanel() {
         return feedbackPanel;
     }
 
 
     public String getROZipLink() {
         return roURI.toString().replaceFirst("/ROs/", "/zippedROs/");
     }
 
 
     /**
      * Return a link to a format-specific version of the manifest.
      * 
      * @param format
      *            RDF format
      * @return a URI as string of the resource
      */
     public String getROMetadataLink(RDFFormat format) {
         return roURI.resolve(".ro/manifest." + format.getDefaultFileExtension() + "?original=manifest.rdf").toString();
     }
 }
