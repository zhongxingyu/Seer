 package pl.psnc.dl.wf4ever.portal.pages.ro;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.tree.TreeModel;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.RestartResponseException;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.upload.FileUpload;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.purl.wf4ever.checklist.client.ChecklistEvaluationService;
 import org.purl.wf4ever.checklist.client.EvaluationResult;
 import org.purl.wf4ever.rosrs.client.Annotation;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 import org.purl.wf4ever.rosrs.client.Statement;
 import org.purl.wf4ever.rosrs.client.Thing;
 import org.purl.wf4ever.rosrs.client.evo.JobStatus;
 import org.purl.wf4ever.rosrs.client.exception.ROException;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 
 import pl.psnc.dl.wf4ever.portal.MySession;
 import pl.psnc.dl.wf4ever.portal.PortalApplication;
 import pl.psnc.dl.wf4ever.portal.pages.ErrorPage;
 import pl.psnc.dl.wf4ever.portal.pages.base.Base;
 import pl.psnc.dl.wf4ever.portal.pages.ro.behaviours.JobStatusUpdatingBehaviour;
 import pl.psnc.dl.wf4ever.portal.pages.ro.behaviours.OnDomReadyAjaxBehaviour;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.ROExplorer;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.behaviours.IAjaxLinkListener;
 import pl.psnc.dl.wf4ever.portal.pages.util.MyFeedbackPanel;
 import pl.psnc.dl.wf4ever.portal.services.OAuthException;
 import pl.psnc.dl.wf4ever.portal.ui.components.LoadingCircle;
 import pl.psnc.dl.wf4ever.portal.utils.RDFFormat;
 
 import com.sun.jersey.api.client.ClientResponse;
 
 /**
  * The Research Object page.
  * 
  * @author piotrekhol
  * 
  */
 public class RoPage extends Base {
 
     /** id. */
     private static final long serialVersionUID = 1L;
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(RoPage.class);
 
     /** Can the user edit the RO. */
     boolean canEdit = false;
 
     /** The part showing the RO annotations. */
     final AnnotatingBox annotatingBox;
 
     /** The modal window for editing annotations. */
     final StatementEditModal stmtEditForm;
 
     /** The modal window for editing relations. */
     final RelationEditModal relEditForm;
 
     /** The modal window for importing annotations. */
     private final ImportAnnotationModal importAnnotationModal;
 
     /** The modal window for adding resources. */
     private UploadResourceModal uploadResourceModal;
 
     /** The feedback panel. */
     MyFeedbackPanel feedbackPanel;
 
     protected ResearchObject researchObject;
 
     protected EvaluationResult qualityEvaluation;
 
     /** Regex pattern for parsing Link HTTP headers. */
     private static final Pattern LINK_HEADER = Pattern.compile("<(.+)>; rel=(.+)");
 
     /** Template for HTML Link Headers. */
     private static final String HTML_LINK_TEMPLATE = "<link rel=\"%s\" href=\"%s\"/>";
 
     private ROExplorer foldersViewer;
     private WebMarkupContainer roExplorerParent;
     RoEvoBox roevoBox;
     private LoadingCircle loadingEvoCircle;
 
     /** Loading image. */
     private LoadingCircle loadingCircle;
 
     /** Selected resource model. */
     private CompoundPropertyModel<Thing> itemModel;
 
     /** Loading information. */
     private static final String LOADING_OBJECT = "Loading Research Object metadata.<br />Please wait...";
     private static final String LOADING_EVO_OBJECT = "Loading ROEVO history.<br />Please wait...";
 
 
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
             URI roURI = new URI(parameters.get("ro").toString());
             researchObject = new ResearchObject(roURI, MySession.get().getRosrs());
         } else {
             throw new RestartResponseException(ErrorPage.class, new PageParameters().add("message",
                 "The RO URI is missing"));
         }
 
         feedbackPanel = new MyFeedbackPanel("feedbackPanel");
         feedbackPanel.setOutputMarkupId(true);
         add(feedbackPanel);
         if (MySession.get().isSignedIn()) {
             //            List<URI> uris = ROSRService.getROList(rodlURI, MySession.get().getdLibraAccessToken());
             //            canEdit = uris.contains(roURI);
             canEdit = true;
         }
         add(new Label("title", researchObject.getUri().toString()));
 
         itemModel = new CompoundPropertyModel<Thing>((Thing) null);
 
         loadingCircle = new LoadingCircle("folders-viewer", LOADING_OBJECT);
         loadingEvoCircle = new LoadingCircle("ro-evo-box", LOADING_EVO_OBJECT);
         loadingEvoCircle.setOutputMarkupId(true);
         roExplorerParent = new WebMarkupContainer("ro-explorer-parent");
         add(roExplorerParent);
         roExplorerParent.setOutputMarkupId(true);
         roExplorerParent.add(loadingCircle);
         add(new OnDomReadyAjaxBehaviour(feedbackPanel) {
 
             @Override
             protected void respond(AjaxRequestTarget target) {
                 if (!researchObject.isLoaded()) {
                     try {
                         researchObject.load();
                         onRoLoaded(target);
                    } catch (ROSRSException | ROException e) {
                         error("Research object cannot be loaded: " + e.getMessage());
                         LOG.error("Research object cannot be loaded", e);
                     }
                 }
                 super.respond(target);
             }
         });
         add(new OnDomReadyAjaxBehaviour(feedbackPanel) {
 
             @Override
             protected void respond(AjaxRequestTarget target) {
                if (!researchObject.isEvolutionInformationLoaded()) {
                    researchObject.loadEvolutionInformation();
                    onRoEvoLoaded(target);
                 }
                 super.respond(target);
             }
         });
         add(new OnDomReadyAjaxBehaviour(feedbackPanel) {
 
             @Override
             protected void respond(AjaxRequestTarget target) {
                ChecklistEvaluationService service = ((PortalApplication) getApplication()).getChecklistService();
                qualityEvaluation = service.evaluate(researchObject.getUri(), "ready-to-release");
                foldersViewer.onQualityEvaluated(target);
                 super.respond(target);
             }
         });
 
         foldersViewer = new ROExplorer("folders-viewer", researchObject, itemModel,
                 new PropertyModel<EvaluationResult>(this, "qualityEvaluation"));
         foldersViewer.setOutputMarkupId(true);
         foldersViewer.getSnapshotButton().addLinkListener(new IAjaxLinkListener() {
 
             @Override
             public void onAjaxLinkClicked(AjaxRequestTarget target) {
                 createSnapshot(target);
             }
         });
         foldersViewer.getReleaseButton().addLinkListener(new IAjaxLinkListener() {
 
             @Override
             public void onAjaxLinkClicked(AjaxRequestTarget target) {
                 createArchive(target);
             }
         });
         annotatingBox = new AnnotatingBox(this, itemModel);
         foldersViewer.appendOnNodeClickTarget(annotatingBox);
         /*****************************************************************************/
         add(annotatingBox);
         annotatingBox.selectedStatements.clear();
         foldersViewer.addLinkListener(annotatingBox);
         add(new DownloadMetadataModal("downloadMetadataModal", this));
         uploadResourceModal = new UploadResourceModal("uploadResourceModal", this);
         add(uploadResourceModal);
         stmtEditForm = new StatementEditModal("statementEditModal", RoPage.this, new CompoundPropertyModel<Statement>(
                 (Statement) null));
         add(stmtEditForm);
         relEditForm = new RelationEditModal("relationEditModal", RoPage.this, new CompoundPropertyModel<Statement>(
                 (Statement) null), "loadingROFragment");
         add(relEditForm);
         importAnnotationModal = new ImportAnnotationModal("importAnnotationModal", this, itemModel);
         add(importAnnotationModal);
         add(loadingEvoCircle);
     }
 
 
     @SuppressWarnings("serial")
     protected void createSnapshot(AjaxRequestTarget target) {
         final JobStatus status = researchObject.snapshot(researchObject.getName().substring(0,
             researchObject.getName().length() - 1)
                 + "-snapshot");
         feedbackPanel.add(new JobStatusUpdatingBehaviour(feedbackPanel, status, "snapshot") {
 
             @Override
             public void onSuccess(AjaxRequestTarget target) {
                 researchObject.loadEvolutionInformation();
                 RoEvoBox newRoevoBox = new RoEvoBox("ro-evo-box", researchObject);
                 roevoBox.replaceWith(newRoevoBox);
                 roevoBox = newRoevoBox;
                 target.add(roevoBox);
             }
         });
 
         target.add(feedbackPanel);
     }
 
 
     @SuppressWarnings("serial")
     protected void createArchive(AjaxRequestTarget target) {
         final JobStatus status = researchObject.archive(researchObject.getName().substring(0,
             researchObject.getName().length() - 1)
                 + "-release");
         feedbackPanel.add(new JobStatusUpdatingBehaviour(feedbackPanel, status, "release") {
 
             @Override
             public void onSuccess(AjaxRequestTarget target) {
                 researchObject.loadEvolutionInformation();
                 RoEvoBox newRoevoBox = new RoEvoBox("ro-evo-box", researchObject);
                 roevoBox.replaceWith(newRoevoBox);
                 roevoBox = newRoevoBox;
                 target.add(roevoBox);
             }
         });
 
         target.add(feedbackPanel);
     }
 
 
     @Override
     public void renderHead(IHeaderResponse response) {
         super.renderHead(response);
         try {
             ClientResponse head = MySession.get().getRosrs()
                     .getResourceHead(researchObject.getUri().resolve(".ro/manifest.rdf"));
             List<String> links = head.getHeaders().get("Link");
             if (links != null) {
                 for (String link : links) {
                     Matcher m = LINK_HEADER.matcher(link);
                     if (m.matches()) {
                         response.renderString(String.format(HTML_LINK_TEMPLATE, m.group(2), m.group(1)));
                     }
                 }
             }
             head.close();
         } catch (ROSRSException e) {
             LOG.error("Unexpected response when getting RO head", e);
         }
     }
 
 
     public TreeModel getPhysicalResourcesTree() {
         return null;
     }
 
 
     public EvaluationResult getQualityEvaluation() {
         return qualityEvaluation;
     }
 
 
     public void setQualityEvaluation(EvaluationResult qualityEvaluation) {
         this.qualityEvaluation = qualityEvaluation;
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
      * @throws ROSRSException
      *             unexpected response code
      * @throws IOException
      *             can't get the uploaded file
      * @throws ROException
      */
     void onResourceAdd(AjaxRequestTarget target, final FileUpload uploadedFile)
             throws ROSRSException, IOException, ROException {
         researchObject.aggregate(uploadedFile.getClientFileName(), uploadedFile.getInputStream(),
             uploadedFile.getContentType());
         target.add(foldersViewer);
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
      * @throws ROSRSException
      *             deleting the resource caused an unexpected response
      */
     public void onResourceDelete(org.purl.wf4ever.rosrs.client.Resource resource, AjaxRequestTarget target)
             throws IOException, ROSRSException {
         resource.delete();
         target.add(foldersViewer);
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
      * @throws ROSRSException
      *             aggregating the resource caused unexpected response
      * @throws ROException
      */
     public void onRemoteResourceAdded(AjaxRequestTarget target, URI resourceURI)
             throws ROSRSException, ROException {
         URI absoluteResourceURI = researchObject.getUri().resolve(resourceURI);
         researchObject.aggregate(absoluteResourceURI);
         target.add(foldersViewer);
 
     }
 
 
     /**
      * Called when the user wants to create a new annotation.
      * 
      * @param statements
      *            a list of statements to be put in the annotation
      * @throws URISyntaxException
      *             URIs retrieved from RODL are incorrect
      * @throws ROSRSException
      *             requests to RODL returned incorrect responses
      * @throws ROException
      * @throws IOException
      */
     void onStatementAdd(List<Statement> statements)
             throws URISyntaxException, ROSRSException, ROException {
         InputStream in = Annotation.wrapAnnotationBody(statements);
         researchObject.annotate(".ro/" + UUID.randomUUID().toString(), in, RDFFormat.RDFXML.getDefaultMIMEType());
     }
 
 
     /**
      * User has edited a statement of an annotation.
      * 
      * @param statement
      *            the statement
      * @throws ROSRSException
      *             requests to RODL returned incorrect responses
      */
     void onStatementEdit(Statement statement)
             throws ROSRSException {
         statement.getAnnotation().update();
     }
 
 
     /**
      * Called after an annotation has been created or edited.
      * 
      * @param target
      *            request target.
      */
     void onStatementAddedEdited(AjaxRequestTarget target) {
         //FIXME isn't it the same as the next one?
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
         //        this.annotatingBox.getModelObject().setAnnotations(
         //            this.annotatingBox.getModelObject().RoFactory.createAnnotations(rodlURI, roURI, this.annotatingBox
         //                    .getModelObject().getUri(), MySession.get().getUsernames()));
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
      *             can't get the uploaded file
      * @throws URISyntaxException
      *             problems with creating the annotation
      * @throws ROSRSException
      *             uploading the annotation body returned an unexpected response
      */
     public void onAnnotationImport(AjaxRequestTarget target, FileUpload uploadedFile, Thing aggregatedResource)
             throws IOException, URISyntaxException, ROSRSException {
         String contentType = RDFFormat.forFileName(uploadedFile.getClientFileName(), RDFFormat.RDFXML)
                 .getDefaultMIMEType();
         MySession
                 .get()
                 .getRosrs()
                 .addAnnotation(researchObject.getUri(), new HashSet<>(Arrays.asList(aggregatedResource.getUri())),
                     uploadedFile.getClientFileName(), uploadedFile.getInputStream(), contentType);
         setResponsePage(RoPage.class, getPageParameters());
     }
 
 
     public MyFeedbackPanel getFeedbackPanel() {
         return feedbackPanel;
     }
 
 
     public String getROZipLink() {
         return researchObject.getUri().toString().replaceFirst("/ROs/", "/zippedROs/");
     }
 
 
     /**
      * Return a link to a format-specific version of the manifest.
      * 
      * @param format
      *            RDF format
      * @return a URI as string of the resource
      */
     public String getROMetadataLink(RDFFormat format) {
         return researchObject.getUri()
                 .resolve(".ro/manifest." + format.getDefaultFileExtension() + "?original=manifest.rdf").toString();
     }
 
 
     public void onRoLoaded(AjaxRequestTarget target) {
         foldersViewer.onRoLoaded(target);
         loadingCircle.replaceWith(foldersViewer);
         target.add(roExplorerParent);
     }
 
 
     public void onRoEvoLoaded(AjaxRequestTarget target) {
         roevoBox = new RoEvoBox("ro-evo-box", researchObject);
         loadingEvoCircle.replaceWith(roevoBox);
         target.add(roevoBox);
     }
 
 }
