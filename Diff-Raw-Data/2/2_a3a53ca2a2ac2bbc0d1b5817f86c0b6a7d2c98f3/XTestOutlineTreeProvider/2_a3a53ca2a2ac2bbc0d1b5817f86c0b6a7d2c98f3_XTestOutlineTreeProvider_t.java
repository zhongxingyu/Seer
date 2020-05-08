 package org.xtest.ui.outline;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.xtext.diagnostics.Severity;
 import org.eclipse.xtext.nodemodel.ICompositeNode;
 import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
 import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
 import org.eclipse.xtext.ui.editor.outline.impl.DefaultOutlineTreeProvider;
 import org.eclipse.xtext.ui.editor.outline.impl.EObjectNode;
 import org.eclipse.xtext.ui.resource.IStorage2UriMapper;
 import org.eclipse.xtext.ui.util.IssueUtil;
 import org.eclipse.xtext.util.ITextRegion;
 import org.eclipse.xtext.util.Pair;
 import org.eclipse.xtext.util.TextRegion;
 import org.eclipse.xtext.validation.Issue;
 import org.xtest.preferences.PerFilePreferenceProvider;
 import org.xtest.preferences.RuntimePref;
 import org.xtest.results.XTestResult;
 import org.xtest.results.XTestState;
 import org.xtest.runner.external.ContinuousTestRunner;
 import org.xtest.ui.internal.XtestPluginImages;
 import org.xtest.ui.mediator.XtestResultsCache;
 import org.xtest.xTest.Body;
 import org.xtest.xTest.impl.BodyImplCustom;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 
 /**
  * Customize the tree outline to display the the results of running the tests graphically
  * 
  * @author Michael Barry
  */
 public class XTestOutlineTreeProvider extends DefaultOutlineTreeProvider {
     @Inject
     private XtestPluginImages images;
     @Inject
     private IssueUtil issueUtil;
     @Inject
     private IStorage2UriMapper mapper;
     @Inject
     private XtestResultsCache mediator;
     private IAnnotationModel model;
     @Inject
     private PerFilePreferenceProvider prefs;
 
     @Override
     public void createChildren(IOutlineNode parentNode, EObject body) {
         if (body instanceof BodyImplCustom) {
             String fileName = ((BodyImplCustom) body).getFileName();
             List<Issue> issues = getIssues((Body) body);
             URI uri = body.eResource().getURI();
             XTestResult last = mediator.getLast(uri);
             if (last != null) {
                 Object text = parentNode.getText();
                 if (!fileName.equals(text)) {
                     createNode(parentNode, last, fileName, issues);
                 }
             } else {
                 scheduleValidation(body);
             }
         }
     }
 
     /**
      * Sets the annotation model to get errors and warnings from
      * 
      * @param annotationModel
      *            The annotation model
      */
     public void setAnnotationModel(IAnnotationModel annotationModel) {
         this.model = annotationModel;
     }
 
     @Override
     protected EObjectNode createEObjectNode(IOutlineNode parentNode, EObject modelElement,
             Image image, Object text, boolean isLeaf) {
         EObjectNode eObjectNode = new XTestEObjectNode(modelElement, parentNode, image, text,
                 isLeaf);
         ITextRegion region = getRegion(parentNode, modelElement);
         if (region != null) {
             eObjectNode.setShortTextRegion(region);
         }
         return eObjectNode;
     }
 
     /**
      * Creates a new node for the test result, setting the name and icon appropriately given the
      * pass/fail/not run state of the test
      * 
      * @param parentNode
      *            The parent node
      * @param result
      *            The test result
      * @param suggestedName
      *            The suggested name to use
      * @return The new tree node
      */
     private EObjectNode createEObjectNode(IOutlineNode parentNode, XTestResult result,
             String suggestedName, List<Issue> issues) {
         EObject eObject = result.getEObject();
         String name = result.getName();
         if (name == null) {
             name = suggestedName;
         }
         Image image;
         Severity severity = getSeverity(result, issues);
         boolean isLeaf = result.getSubTests().isEmpty();
         if (isLeaf && parentNode.getParent() != null) {
             image = severity == null ? images.getTestImage() : images.getTestImage(severity);
         } else {
             image = severity == null ? images.getSuiteImage() : images.getSuiteImage(severity);
         }
 
         EObjectNode createEObjectNode = createEObjectNode(parentNode, eObject, image, name, isLeaf);
 
         if (severity == Severity.ERROR) {
             ((XTestEObjectNode) createEObjectNode).setFailed();
         }
         return createEObjectNode;
     }
 
     /**
      * Creates a node for the test and sub-tests
      * 
      * @param parentNode
      *            The parent tree node
      * @param test
      *            The sub test
      * @param suggestedName
      *            Name to use for the node
      */
     private void createNode(IOutlineNode parentNode, XTestResult test, String suggestedName,
             List<Issue> issues) {
         EObjectNode thisNode = createEObjectNode(parentNode, test, suggestedName, issues);
         for (XTestResult subTest : test.getSubTests()) {
             createNode(thisNode, subTest, null, issues);
         }
     }
 
     private IFile getFile(Body body) {
         URI uri = body.eResource().getURI();
         Iterable<Pair<IStorage, IProject>> storages = mapper.getStorages(uri);
         IFile first = (IFile) storages.iterator().next().getFirst();
         return first;
     }
 
     private List<Issue> getIssues(Body body) {
         List<Issue> result = Lists.newArrayList();
         if (prefs.get(body, RuntimePref.RUN_WHILE_EDITING) && model != null) {
             // use annotation model
             Iterator<?> iterator;
             iterator = model.getAnnotationIterator();
             while (iterator.hasNext()) {
                 final Annotation a = (Annotation) iterator.next();
                 if (!a.isMarkedDeleted()) {
                     Issue issue = issueUtil.getIssueFromAnnotation(a);
                     if (issue != null) {
                         result.add(issue);
                     }
                 }
             }
         } else {
             // use file markers
             IFile first = getFile(body);
             try {
                 IMarker[] findMarkers = first.findMarkers(null, true, IResource.DEPTH_ZERO);
                 for (IMarker marker : findMarkers) {
                     Issue createIssue = issueUtil.createIssue(marker);
                     if (createIssue != null && createIssue.getOffset() >= 0
                             && createIssue.getLength() >= 0) {
                         result.add(createIssue);
                     }
                 }
             } catch (CoreException e) {
             }
         }
         return result;
     }
 
     private ITextRegion getRegion(IOutlineNode parentNode, EObject modelElement) {
         ITextRegion region = null;
         ICompositeNode parserNode = NodeModelUtils.getNode(modelElement);
         if (parserNode != null) {
             region = new TextRegion(parserNode.getOffset(), parserNode.getLength());
         }
         if (isLocalElement(parentNode, modelElement)) {
             region = locationInFileProvider.getSignificantTextRegion(modelElement);
         }
         return region;
     }
 
     private Severity getSeverity(List<Issue> issues, final int offset, final int length) {
         boolean hasWarnings = false;
         for (Issue issue : issues) {
             Position position = new Position(issue.getOffset(), issue.getLength());
             if (position.overlapsWith(offset, length)) {
                 if (issue.getSeverity() == Severity.ERROR) {
                     return Severity.ERROR;
                 } else if (issue.getSeverity() == Severity.WARNING) {
                     hasWarnings = true;
                 }
             }
         }
         if (hasWarnings) {
             return Severity.WARNING;
         } else {
             return Severity.INFO;
         }
     }
 
     /**
      * Returns the severity for the EObject derived from its test result status and any issues on
      * contained {@link EObject}s
      * 
      * @param result
      *            The test result
      * @return The {@link Severity} of the node, or null if no issues and no tests have run
      */
     private Severity getSeverity(XTestResult result, List<Issue> issues) {
         Severity severity = null;
         XTestState state = result.getState();
         if (state == XTestState.FAIL) {
             severity = Severity.ERROR;
         } else {
             EObject eObject = result.getEObject();
             ICompositeNode node = NodeModelUtils.findActualNodeFor(eObject);
            severity = getSeverity(issues, node.getOffset(), node.getTotalLength());
         }
 
         return severity;
     }
 
     private void scheduleValidation(EObject body) {
         Iterable<Pair<IStorage, IProject>> storages = mapper.getStorages(body.eResource().getURI());
         for (Pair<IStorage, IProject> pair : storages) {
             IStorage first = pair.getFirst();
             if (first instanceof IFile) {
                 ContinuousTestRunner.schedule((IFile) first);
             }
         }
     }
 }
