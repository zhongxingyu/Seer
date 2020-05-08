 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.sdk.features;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.ltk.core.refactoring.Change;
 import org.eclipse.ltk.core.refactoring.CompositeChange;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
 import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;
 import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
 import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
 import org.nuxeo.ide.sdk.model.ManifestWriter;
 
 ;
 
 /**
  * 
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class DeleteFeatureParticipant extends DeleteParticipant {
 
     protected FeatureType type;
 
     public DeleteFeatureParticipant() {
     }
 
     @Override
     protected boolean initialize(Object element) {
         type = FeatureType.fromElement(element);
         if (type != null && type.file.exists()) {
             return true;
         }
         return false;
     }
 
     @Override
     public String getName() {
         return "Delete Feature Synchronizer";
     }
 
     @Override
     public RefactoringStatus checkConditions(IProgressMonitor pm,
             CheckConditionsContext context) throws OperationCanceledException {
         if (type == null) {
             return new RefactoringStatus();
         }
         RefactoringStatus status = new RefactoringStatus();
         ResourceChangeChecker checker = (ResourceChangeChecker) context.getChecker(ResourceChangeChecker.class);
         IResourceChangeDescriptionFactory deltaFactory = checker.getDeltaFactory();
        deltaFactory.delete(type.file);
         IFile mf = type.getProject().getFile(ManifestWriter.PATH);
         deltaFactory.change(mf);
         status.addInfo("Deleting extension file: " + type.file.getName());
         // check for resource bundles impact
         checkI18NConditions(status, deltaFactory);
         checkResourcesConditions(status, deltaFactory);
         return status;
     }
 
     protected void checkI18NConditions(RefactoringStatus status,
             IResourceChangeDescriptionFactory deltaFactory) {
         IFolder i18nFolder = type.getProject().getFolder(
                 "src/main/i18n/web/nuxeo.war/WEB-INF/classes");
         if (!i18nFolder.exists()) {
             return;
         }
         try {
             for (IResource m : i18nFolder.members()) {
                 deltaFactory.change((IFile) m);
             }
         } catch (Exception e) {
             status.addError("Cannot list i18n resource bundles");
         }
     }
 
     protected void checkResourcesConditions(final RefactoringStatus status,
             final IResourceChangeDescriptionFactory deltaFactory) {
         final String fqn = type.type.getFullyQualifiedName();
         final IFolder resources = type.getProject().getFolder(
                 "src/main/resources");
         try {
             resources.accept(new ResourceVisitor(fqn) {
 
                 @Override
                 public void visitResource(IFile file, String suffix,
                         @SuppressWarnings("hiding") ContentType type) {
                     deltaFactory.delete(file);
                 }
 
             });
         } catch (CoreException e) {
             status.addError("Cannot visit binary resources");
         }
     }
 
     @Override
     public Change createChange(IProgressMonitor pm) throws CoreException,
             OperationCanceledException {
         CompositeChange result = new CompositeChange("Synchronizing extensions");
        result.add(new DeleteResourceChange(type.file.getFullPath(), true));
         IFile mf = type.getProject().getFile(ManifestWriter.PATH);
         if (mf.exists()) {
             ManifestChange change = new ManifestChange(mf);
             change.remove("Nuxeo-Component", type.getRuntimeExtensionPath());
             result.add(change);
         }
         create18NChange(result);
         createResourcesChange(result);
         return result;
     }
 
     protected void create18NChange(CompositeChange result) throws CoreException {
         final String fqn = type.type.getFullyQualifiedName();
         IFolder i18nFolder = type.getProject().getFolder(
                 "src/main/i18n/web/nuxeo.war/WEB-INF/classes");
         if (!i18nFolder.exists()) {
             return;
         }
         for (IResource m : i18nFolder.members()) {
             final IFile file = (IFile) m;
             result.add(new RemoveMatchingLinesChange(file, fqn));
         }
     }
 
     protected void createResourcesChange(final CompositeChange result)
             throws CoreException {
         final String fqn = type.type.getFullyQualifiedName();
         final IFolder resources = type.getProject().getFolder(
                 "src/main/resources");
 
         resources.accept(new ResourceVisitor(fqn) {
 
             @Override
             public void visitResource(IFile file, String suffix,
                     @SuppressWarnings("hiding") ContentType type) {
                 result.add(new DeleteResourceChange(file.getFullPath(), false));
             }
 
         });
     }
 }
