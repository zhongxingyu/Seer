 package visualiser.provider;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.contribution.visualiser.VisualiserPlugin;
 import org.eclipse.contribution.visualiser.core.ProviderManager;
 import org.eclipse.contribution.visualiser.core.Stripe;
 import org.eclipse.contribution.visualiser.interfaces.IMarkupKind;
 import org.eclipse.contribution.visualiser.interfaces.IMarkupProvider;
 import org.eclipse.contribution.visualiser.simpleImpl.SimpleMarkupKind;
 import org.eclipse.contribution.visualiser.simpleImpl.SimpleMarkupProvider;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.internal.core.JavaProject;
 
 import visualiser.convertxml.ConvertXMLtoMVIS;
 
 public class PatchMarkupProvider extends SimpleMarkupProvider implements
 		ISelectionListener {
 
 	private static String WORKSPACE_ROOT = ResourcesPlugin.getWorkspace().getRoot().getLocation().toPortableString();
 	private final static boolean debugLoading = false;
 
 	private Map kinds;
 	private IProject lastProj;
 
 	/**
 	 * Initialise the provider - loads markup information from a file
 	 */
 	public void initialise() {
 		kinds = new HashMap();
 		
 		if (VisualiserPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() != null) {
 			VisualiserPlugin.getDefault().getWorkbench()
 					.getActiveWorkbenchWindow().getSelectionService()
 					.addSelectionListener(this);
 		}
 	}
 
 	/**
 	 * Load the markup information from given input stream
 	 * 
 	 * @param in
 	 */
 	public void loadMarkups(InputStream in) {
 		int scount = 0; // How many stripes added altogether
 
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String line = br.readLine();
 
 			// Go through the file until we hit the end
 			// Each line has a format like:
 			// Stripe:ABC.A Kind:S1 Offset:5 Depth:1
 			while (line != null && line.length() != 0) {
 
 				// Process lines starting Stripe:
 				if (line.startsWith("Stripe:")) { //$NON-NLS-1$
 					String membername = null;
 					String kindStr = null;
 					int offset = 0;
 					int depth = 1;
 
 					// Retrieve the fully qualified membername, e.g. ABC.A
 					membername = retrieveKeyValue("Stripe:", line); //$NON-NLS-1$
 
 					// Retrieve the Kind:, e.g. S1
 					kindStr = retrieveKeyValue("Kind:", line); //$NON-NLS-1$
 					IMarkupKind kind;
 					if (kinds.get(kindStr) instanceof IMarkupKind) {
 						kind = (IMarkupKind) kinds.get(kindStr);
 					} else {
 						kind = new SimpleMarkupKind(kindStr);
 						kinds.put(kindStr, kind);
 					}
 
 					try {
 						super.addMarkupKind(kind);
 					} catch (NullPointerException npe) {
 						// ignore for plugin initialization
 					}
 
 					// Retrieve the Offset:, e.g. 42
 					offset = Integer.parseInt(retrieveKeyValue("Offset:", line)); //$NON-NLS-1$
 
 					// Retrieve the Depth:, e.g. 30
 					depth = Integer.parseInt(retrieveKeyValue("Depth:", line)); //$NON-NLS-1$
 
 					// Create a new stripe and add it as a markup
 					Stripe newstripe = new Stripe(kind, offset, depth);
 					addMarkup(membername, newstripe);
 					scount++;
 
 					if (debugLoading)
 						System.err.println("Loading new stripe: Adding " + newstripe + " for " + membername); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 				line = br.readLine();
 			}
 		} catch (FileNotFoundException e) {
 			System.err.println("Problem loading markup data"); //$NON-NLS-1$
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.err.println("Problem loading markup data"); //$NON-NLS-1$
 			e.printStackTrace();
 		}
 		processMarkups();
 	}
 
 	/**
 	 * Given a 'key' it looks for the key in a supplied string and returns the
 	 * value after the key. For example, looking for "Fred:" in the string
 	 * "Barney:40 Fred:45 Betty:40" would return "45". If values need to have
 	 * spaces in then _ characters can be used, this method will translate those
 	 * to spaces before it returns.
 	 * 
 	 * @param what
 	 *            The key to look for
 	 * @param where
 	 *            The string to locate the key in
 	 * @return the value after the key (whitespace is the value delimiter)
 	 */
 	private String retrieveKeyValue(String what, String where) {
 		if (debugLoading)
 			System.err.println("looking for '" + what + "' in '" + where + "'");
 		if (where.indexOf(what) == -1)
 			return null;
 		String postWhat = where.substring(where.indexOf(what) + what.length());
 		String result = postWhat;
 		if (result.indexOf(" ") != -1)
 			result = postWhat.substring(0, postWhat.indexOf(" "));
 		result = result.replace('_', ' ');
 		if (debugLoading)
 			System.err.println("Returning '" + result + "'");
 		return result;
 	}
 
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		IProject proj = null;
 		
 		if (selection instanceof IStructuredSelection) {
 			Object selected = ((IStructuredSelection) selection).getFirstElement();
 			
 			if (selected instanceof IResource) {
 				IResource resource = (IResource) selected;
 				proj = resource.getProject();
 			} else if (selected instanceof IJavaProject) {
 				proj = ((IJavaProject) selected).getProject();
 			} else if (selected instanceof IResource) {
 				IResource resource = (IResource) selected;
 				proj = resource.getProject();
 			} else if (selected instanceof ICompilationUnit) {
 				ICompilationUnit cu = (ICompilationUnit) selected;
 				proj = cu.getJavaProject().getProject();
 			} else {
				try{
					IFile file = (IFile) part.getSite().getWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput().getAdapter(IFile.class);
					proj = file.getProject();
				} catch (NullPointerException npe){
					//do nothing
				}
 			}
 		} else if (selection instanceof TextSelection){
 			IFile file = (IFile) part.getSite().getWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput().getAdapter(IFile.class);
 			proj = file.getProject();
 		}
 		
 		if (proj != lastProj) {
 
 			resetMarkupsAndKinds();
 			lastProj = proj;
 
 			ConvertXMLtoMVIS.convertContentVis(proj);
 			ConvertXMLtoMVIS.convertMarkupVis(proj);
 
 			try {
 				File fileURL = new File(proj.getLocation() + File.separator + "Markup.mvis");
 				InputStream in = new FileInputStream(fileURL);
 				loadMarkups(in);
 				in.close();
 			} catch (IOException ioe) {
 				VisualiserPlugin.logException(ioe);
 			}
 
 			IMarkupProvider markupP = ProviderManager.getMarkupProvider();
 			if (markupP instanceof SimpleMarkupProvider) {
 				((SimpleMarkupProvider) markupP).resetColours();
 			}
 
 			// if the Visualiser is showing, update to use the new settings
 			if (VisualiserPlugin.visualiser != null) {
 				if (VisualiserPlugin.menu != null) {
 					VisualiserPlugin.menu.setVisMarkupProvider(markupP);
 				}
 				VisualiserPlugin.visualiser.updateDisplay(true);
 			}
 		}
 	}
 }
