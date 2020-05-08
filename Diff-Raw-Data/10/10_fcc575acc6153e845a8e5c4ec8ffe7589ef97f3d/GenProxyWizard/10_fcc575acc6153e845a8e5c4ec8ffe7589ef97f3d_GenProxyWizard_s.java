 package com.github.generategwtrfproxy.wizards;
 
 import com.github.generategwtrfproxy.Activator;
 import com.github.generategwtrfproxy.beans.Method;
 import com.github.generategwtrfproxy.util.Utils;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.jdt.core.IBuffer;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
 import org.eclipse.jdt.core.formatter.CodeFormatter;
 import org.eclipse.jdt.ui.CodeStyleConfiguration;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.util.Policy;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class GenProxyWizard extends Wizard implements INewWizard {
 
   private static String lineDelimiter = System.getProperty(
       "line.separator", "\n"); //$NON-NLS-N$
 
   private GenProxyWizardPage initialPage;
 
   private List<GenProxyWizardPage> pages = new ArrayList<GenProxyWizardPage>();
 
   private Map<IType, GenProxyWizardPage> mapTypeToPage = new HashMap<IType, GenProxyWizardPage>();
 
   private IType initialProxyFor;
 
   private boolean isDone;
 
   public GenProxyWizard() {
     setNeedsProgressMonitor(true);
     setForcePreviousAndNextButtons(true);
     setWindowTitle("New Proxy");
   }
 
   @Override
   public boolean canFinish() {
     // Default implementation is to check if all pages are complete.
     for (int i = 0; i < pages.size(); i++) {
       if (!pages.get(i).isPageComplete()) {
         return false;
       }
     }
     return true;
   }
 
   @Override
   public void createPageControls(Composite pageContainer) {
     // the default behavior is to create all the pages controls
     for (int i = 0; i < pages.size(); i++) {
       IWizardPage page = (IWizardPage) pages.get(i);
       page.createControl(pageContainer);
       // page is responsible for ensuring the created control is
       // accessable
       // via getControl.
       Assert.isNotNull(page.getControl());
     }
   }
 
   @Override
   public void dispose() {
     // notify pages
     disposePage(initialPage);
     for (GenProxyWizardPage page : mapTypeToPage.values()) {
       disposePage(page);
     }
     super.dispose();
   }
 
   @Override
   public IWizardPage getNextPage(IWizardPage wizardPage) {
     List<GenProxyWizardPage> newList = new ArrayList<GenProxyWizardPage>();
     for (GenProxyWizardPage page : pages) {
       newList.add(page);
       if (page == wizardPage) {
         break;
       }
     }
     pages = newList;
 
     if (null != initialProxyFor) {
       mapTypeToPage.remove(initialProxyFor);
     }
     initialProxyFor = initialPage.getProxyFor();
     if (null != initialProxyFor) {
       mapTypeToPage.put(initialProxyFor, initialPage);
     }
 
     GenProxyWizardPage nextPage = null;
 
     for (GenProxyWizardPage page : pages) {
 
       List<Method> methods = page.getSelectedMethods();
       if (null == methods || methods.isEmpty()) {
         continue;
       }
 
       for (Method method : methods) {
        nextPage = null;
         IType type = getTypeToProxy(method.getParamOrReturnTypeBinding());
         if (null != type) {
           nextPage = mapTypeToPage.get(type);
           if (null == nextPage) {
             nextPage = new GenProxyWizardPage();
             nextPage.setPackageFragmentRoot(
                 initialPage.getPackageFragmentRoot(), true);
             nextPage.setPackageFragment(initialPage.getPackageFragment(), true);
             nextPage.setProxyFor(type);
             mapTypeToPage.put(type, nextPage);
             break;
           } else {
            boolean typeNotAlreadyDefined = true;
             for (GenProxyWizardPage p : pages) {
               // if a page has already been completed for this type
               if (p == nextPage) {
                typeNotAlreadyDefined = false;
               }
             }
            if (typeNotAlreadyDefined) {
               break;
             }
           }
         }
       }
 
       if (null != nextPage) {
         break;
       }
     }
 
     if (null != nextPage) {
       pages.add(nextPage);
       nextPage.setWizard(this);
       return nextPage;
     }
 
     return null;
   }
 
   @Override
   public IWizardPage[] getPages() {
     return pages.toArray(new IWizardPage[pages.size()]);
   }
 
   @Override
   public IWizardPage getPreviousPage(IWizardPage page) {
     int index = pages.indexOf(page);
     if (index == 0 || index == -1) {
       // first page or page not found
       return null;
     }
     return (IWizardPage) pages.get(index - 1);
   }
 
   @Override
   public IWizardPage getStartingPage() {
     return initialPage;
   }
 
   public void init(IWorkbench workbench, IStructuredSelection selection) {
     initialPage = new GenProxyWizardPage();
     pages.add(initialPage);
     initialPage.setWizard(this);
 
     Object element = selection.getFirstElement();
 
     if (element instanceof ICompilationUnit) {
 
       initialPage.setProxyFor(((ICompilationUnit) selection.getFirstElement()).findPrimaryType());
 
     } else if (element instanceof IPackageFragmentRoot) {
 
       initialPage.setPackageFragmentRoot((IPackageFragmentRoot) element, true);
 
     } else if (element instanceof IPackageFragment) {
 
       IPackageFragment pkg = (IPackageFragment) element;
       IPackageFragmentRoot root = null;
       IJavaElement parent = pkg.getParent();
       while (null != parent) {
         if (parent instanceof IPackageFragmentRoot) {
           root = (IPackageFragmentRoot) parent;
           break;
         }
         parent = parent.getParent();
       }
       initialPage.setPackageFragmentRoot(root, true);
       initialPage.setPackageFragment(pkg, true);
 
     }
   }
 
   @Override
   public boolean performFinish() {
     try {
       super.getContainer().run(false, false, new IRunnableWithProgress() {
         @Override
         public void run(IProgressMonitor monitor)
             throws InvocationTargetException, InterruptedException {
           isDone = finish(monitor);
         }
       });
     } catch (Exception e) {
       e.printStackTrace();
       return false;
     }
     return isDone;
   }
 
   private void disposePage(GenProxyWizardPage page) {
     try {
       page.dispose();
     } catch (Exception e) {
       Status status = new Status(IStatus.ERROR, Policy.JFACE, IStatus.ERROR,
           e.getMessage(), e);
       Policy.getLog().log(status);
     }
   }
 
   private boolean finish(IProgressMonitor desiredMonitor) {
     IProgressMonitor monitor = desiredMonitor;
     if (monitor == null) {
       monitor = new NullProgressMonitor();
     }
 
     try {
       for (GenProxyWizardPage page : pages) {
         writeProxy(monitor, page);
       }
     } catch (Exception e) {
       monitor.done();
       return false;
     }
 
     monitor.done();
     return true;
   }
 
   private IType getTypeToProxy(ITypeBinding type) {
     if (Utils.isValueType(type)) {
       return null;
     }
     // List or Set
     if (type.isParameterizedType()) {
       ITypeBinding genericParameter = type.getTypeArguments()[0];
       if (Utils.isValueType(genericParameter)) {
         return null;
       } else {
         return (IType) genericParameter.getJavaElement();
       }
     }
 
     return (IType) type.getJavaElement();
   }
 
   private void writeMethod(Method method, ImportRewrite imports, IBuffer buffer) {
 
     // add import including List and Set and their generic type
     imports.addImport(method.getParamOrReturnTypeBinding());
 
     String paramOrReturnName = method.getParamOrReturnTypeBinding().getName();
     IType type = getTypeToProxy(method.getParamOrReturnTypeBinding());
     if (null != type) {
       GenProxyWizardPage page = mapTypeToPage.get(type);
 
       // replace the type name by the proxy name to keep the List and Set in the
       // name
       paramOrReturnName = paramOrReturnName.replaceAll(type.getElementName(),
           page.getTypeName());
 
       // retrieving package for import
       String paramOrReturnFullyQualifiedName = page.getPackageFragment().getElementName();
       if (null == paramOrReturnFullyQualifiedName
           || paramOrReturnFullyQualifiedName.length() == 0) {
         paramOrReturnFullyQualifiedName = page.getTypeName();
       } else {
         paramOrReturnFullyQualifiedName += "." + page.getTypeName();
       }
       imports.addImport(paramOrReturnFullyQualifiedName);
       // remove the import added from first addImport call. Little trick to keep
       // the List and Set import
       imports.removeImport(type.getFullyQualifiedName());
     }
 
     if (method.isGetter()) {
       buffer.append(paramOrReturnName);
     } else {
       buffer.append("void");
     }
 
     buffer.append(" ");
     buffer.append(method.getMethodName());
     buffer.append("(");
     if (!method.isGetter()) {
       buffer.append(paramOrReturnName);
       buffer.append(" ");
       buffer.append(method.getParameterName());
     }
     buffer.append(");");
     buffer.append(lineDelimiter);
   }
 
   private void writeProxy(IProgressMonitor monitor, GenProxyWizardPage page)
       throws Exception {
 
     IType proxyFor = page.getProxyFor();
     IPackageFragment pkg = page.getPackageFragment();
     String proxyName = page.getTypeName();
     boolean isAnnotProxyFor = page.isAnnotProxyFor();
     String locator = page.getLocator();
     boolean entityProxy = page.isEntityProxy();
     List<Method> selectedMethods = page.getSelectedMethods();
 
     ICompilationUnit cu = null;
     try {
       monitor.subTask("Creating proxy");
 
       cu = pkg.createCompilationUnit(proxyName + ".java", "", false,
           new SubProgressMonitor(monitor, 1));
 
       cu.becomeWorkingCopy(new SubProgressMonitor(monitor, 1));
 
       ImportRewrite imports = CodeStyleConfiguration.createImportRewrite(cu,
           false);
       imports.addImport(proxyFor.getFullyQualifiedName());
       if (isAnnotProxyFor) {
         imports.addImport("com.google.web.bindery.requestfactory.shared.ProxyFor");
       } else {
         imports.addImport("com.google.web.bindery.requestfactory.shared.ProxyForName");
       }
       if (null != locator && locator.length() > 0 && isAnnotProxyFor) {
         imports.addImport(locator);
       }
       if (entityProxy) {
         imports.addImport("com.google.web.bindery.requestfactory.shared.EntityProxy");
       } else {
         imports.addImport("com.google.web.bindery.requestfactory.shared.ValueProxy");
       }
 
       cu.createPackageDeclaration(pkg.getElementName(), new SubProgressMonitor(
           monitor, 1));
 
       IBuffer buffer = cu.getBuffer();
       if (isAnnotProxyFor) {
         buffer.append("@ProxyFor(value=");
         buffer.append(proxyFor.getElementName());
         buffer.append(".class");
         if (null != locator && locator.length() > 0) {
           buffer.append(", locator=");
           int index = locator.lastIndexOf(".");
           if (index == -1) {
             buffer.append(locator);
           } else {
             buffer.append(locator.substring(index + 1));
           }
           buffer.append(".class");
         }
         buffer.append(")");
       } else {
         buffer.append("@ProxyForName(value=\"");
         buffer.append(proxyFor.getFullyQualifiedName());
         buffer.append("\"");
         if (null != locator && locator.length() > 0) {
           buffer.append(", locator=\"");
           buffer.append(locator);
           buffer.append("\"");
         }
         buffer.append(")");
       }
       buffer.append(lineDelimiter);
       buffer.append("public interface ");
       buffer.append(proxyName);
       buffer.append(" extends ");
       if (entityProxy) {
         buffer.append("EntityProxy");
       } else {
         buffer.append("ValueProxy");
       }
       buffer.append(" {");
       buffer.append(lineDelimiter);
 
       for (Method method : selectedMethods) {
         writeMethod(method, imports, buffer);
       }
 
       buffer.append("}");
 
       cu.applyTextEdit(
           imports.rewriteImports(new SubProgressMonitor(monitor, 1)),
           new SubProgressMonitor(monitor, 1));
 
       String formattedContent = Utils.format(buffer.getContents(),
           CodeFormatter.K_COMPILATION_UNIT);
       buffer.setContents(formattedContent);
 
       cu.commitWorkingCopy(false, new SubProgressMonitor(monitor, 1));
 
       monitor.worked(1);
     } catch (Exception e) {
       e.printStackTrace();
 
       IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
           "An unexpected error has happened. Close the wizard and retry.", e);
 
       ErrorDialog.openError(getShell(), null, null, status);
 
       throw e;
     } finally {
       if (null != cu) {
         try {
           cu.discardWorkingCopy();
         } catch (JavaModelException e) {
           e.printStackTrace();
         }
       }
     }
   }
 }
