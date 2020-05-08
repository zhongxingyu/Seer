 /*******************************************************************************
  * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Matthew Conway - initial API and implementation
  *     IBM Corporation - concepts and ideas taken from Eclipse code
  *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
  *******************************************************************************/
 package net.sourceforge.eclipseccase.ui.preferences;
 
 import net.sourceforge.eclipseccase.ui.ClearcaseImages;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.util.*;
 import net.sourceforge.eclipseccase.ui.*;
 import net.sourceforge.eclipseccase.ui.ClearcaseDecorator.CachedImageDescriptor;
 import net.sourceforge.eclipseccase.ui.viewsupport.OverlayIcon;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jface.preference.*;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.swt.graphics.*;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.team.ui.TeamImages;
 import org.eclipse.ui.*;
 import org.eclipse.ui.ide.IDE.SharedImages;
 
 /**
  * The preference page for the ClearCase label decorator.
  */
 public class DecoratorPreferencePage extends FieldEditorPreferencePageWithCategories implements IWorkbenchPreferencePage {
 
 	/**
 	 * Provides a fixed mock resource tree for showing the preview.
 	 */
 	private final class PreviewContentProvider implements ITreeContentProvider {
 
 		public void dispose() {
 			// empty
 		}
 
 		public Object[] getChildren(Object parentElement) {
 			if (parentElement == ResourcesPlugin.getWorkspace().getRoot())
 				return new String[] { PREVIEW_PROJECT };
 			else if (parentElement.equals(PREVIEW_PROJECT))
				return new String[] { PREVIEW_CHECKEDIN_TXT, PREVIEW_CHECKEDOUT_JAVA, PREVIEW_LINKED_FOLDER, PREVIEW_DERIVED_OBJECT, PREVIEW_IGNORED_TXT, PREVIEW_HIJACKEDARCHIVE_ZIP, PREVIEW_UNKNOWNSTATE_TXT, PREVIEW_VIEWPRIVATE_TXT };
 			else if (parentElement.equals(PREVIEW_FOLDER))
				return new String[] { PREVIEW_CHECKEDIN_TXT, PREVIEW_VIEWPRIVATE_TXT };
 			else
 				return new Object[0];
 		}
 
 		public Object[] getElements(Object inputElement) {
 			return getChildren(inputElement);
 		}
 
 		public Object getParent(Object element) {
 			return null;
 		}
 
 		public boolean hasChildren(Object element) {
 			if (element.equals(PREVIEW_PROJECT) || element.equals(PREVIEW_FOLDER) || element == ResourcesPlugin.getWorkspace().getRoot())
 				return true;
 			else
 				return false;
 		}
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			// empty
 		}
 	}
 
 	/**
 	 * Label provider creates a dummy CVSDecoration using the preferences set in
 	 * this page. The decoration properties are assigned to the different named
 	 * resources in the preview tree so that most of the decoration options are
 	 * shown
 	 */
 	private final class PreviewLabelProvider extends LabelProvider implements IFontProvider, IColorProvider {
 
 		public Color getBackground(Object element) {
 			return null;
 		}
 
 		private ImageDescriptor getElementBackgroundImage() {
 			// if (!customClearCaseElementsBackground.getBooleanValue())
 			return IMG_DESC_ELEMENT_BG;
 
 			// String customImage =
 			// imageClearCaseElementsBackground.getStringValue();
 			// if (null == customImage || customImage.trim().length() == 0)
 			// return ImageDescriptor.getMissingImageDescriptor();
 			//
 			//			if (!(customImage.toLowerCase().endsWith(".gif") //$NON-NLS-1$
 			//					|| customImage.toLowerCase().endsWith(".jpg") //$NON-NLS-1$
 			//			|| customImage.toLowerCase().endsWith(".png"))) //$NON-NLS-1$
 			// return ImageDescriptor.getMissingImageDescriptor();
 			//
 			// // create image
 			// File imageFile = new File(customImage);
 			// if (imageFile.canRead()) {
 			// try {
 			// return ImageDescriptor.createFromURL(imageFile.toURL());
 			// } catch (MalformedURLException e) {
 			// // ignore
 			// }
 			// }
 			// // ensure image is not null
 			// return ImageDescriptor.getMissingImageDescriptor();
 		}
 
 		public Font getFont(Object element) {
 			return null;
 		}
 
 		public Color getForeground(Object element) {
 			return null;
 		}
 
 		@Override
 		public Image getImage(Object element) {
 			Image baseImage;
 			if (element.equals(PREVIEW_PROJECT)) {
 				baseImage = PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);
 			} else if (element.equals(PREVIEW_FOLDER) || element.equals(PREVIEW_LINKED_FOLDER)) {
 				baseImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
 			} else {
 				baseImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
 			}
 
 			ImageDescriptor overlay = null;
 			ImageDescriptor background = null;
 			if (element.equals(PREVIEW_IGNORED_TXT)) {
 				// nothing
 			} else if (element.equals(PREVIEW_PROJECT)) {
 				// checked in
 				// dirty (has checkouts and hijacked)
 				if (dirtyFolders.getBooleanValue()) {
 					overlay = IMG_DESC_DIRTY;
 				} else if (decorateCheckedIn.getBooleanValue()) {
 					overlay = IMG_DESC_CHECKED_IN;
 				}
 				// background
 				if (decorateClearCaseElements.getBooleanValue()) {
 					background = getElementBackgroundImage();
 				}
 			} else if (element.equals(PREVIEW_FOLDER)) {
 				// checked in
 				// dirty (has view private and checked in)
 				if (dirtyFoldersWithViewPrivate.getBooleanValue() && dirtyFolders.getBooleanValue() && decorateViewPrivate.getBooleanValue()) {
 					overlay = IMG_DESC_DIRTY;
 				} else if (decorateCheckedIn.getBooleanValue()) {
 					overlay = IMG_DESC_CHECKED_IN;
 				}
 				// background
 				if (decorateClearCaseElements.getBooleanValue()) {
 					background = getElementBackgroundImage();
 				}
 			} else if (element.equals(PREVIEW_VIEWPRIVATE_TXT)) {
 				// view private
 				if (decorateViewPrivate.getBooleanValue()) {
 					overlay = IMG_DESC_NEW_RESOURCE;
 				}
 			} else if (element.equals(PREVIEW_EDITEDSOMEWHERE_JAVA)) {
 				// edited in another view or branch
 				if (decorateEdited.getBooleanValue()) {
 					overlay = IMG_DESC_EDITED;
 				}
 				// background
 				if (decorateClearCaseElements.getBooleanValue()) {
 					background = getElementBackgroundImage();
 				}
 			} else if (element.equals(PREVIEW_UNKNOWNSTATE_TXT)) {
 				// unknown state
 				if (decorateUnknown.getBooleanValue()) {
 					overlay = IMG_DESC_UNKNOWN_STATE;
 				}
 			} else if (element.equals(PREVIEW_CHECKEDIN_TXT)) {
 				// checked in
 				if (decorateCheckedIn.getBooleanValue()) {
 					overlay = IMG_DESC_CHECKED_IN;
 				}
 				// background
 				if (decorateClearCaseElements.getBooleanValue()) {
 					background = getElementBackgroundImage();
 				}
 			} else if (element.equals(PREVIEW_CHECKEDOUT_JAVA)) {
 				// checked out
 				overlay = IMG_DESC_CHECKED_OUT;
 				// background
 				if (decorateClearCaseElements.getBooleanValue()) {
 					background = getElementBackgroundImage();
 				}
 			} else if (element.equals(PREVIEW_DERIVED_OBJECT)) {
 				if (decorateDerivedObjects.getBooleanValue()) {
 					overlay = IMG_DESC_DERIVED_OBJECT;
 				}
 			} else if (element.equals(PREVIEW_HIJACKEDARCHIVE_ZIP)) {
 				// hijacked
 				if (decorateHijacked.getBooleanValue()) {
 					overlay = IMG_DESC_HIJACKED;
 				}
 				// background
 				if (decorateClearCaseElements.getBooleanValue()) {
 					background = getElementBackgroundImage();
 				}
 			} else if (element.equals(PREVIEW_LINKED_FOLDER)) {
 				// linked
 				overlay = IMG_DESC_LINK;
 				// background
 				if (decorateClearCaseElements.getBooleanValue()) {
 					background = getElementBackgroundImage();
 				}
 			}
 
 			if (null == overlay && null == background)
 				return baseImage;
 
 			// Otherwise
 			ImageDescriptor[] overlays = null != overlay ? new ImageDescriptor[] { overlay } : new ImageDescriptor[0];
 			int[] locations = null != overlay ? new int[] { OverlayIcon.BOTTOM_RIGHT } : new int[0];
 			ImageDescriptor[] backgrounds = null != background ? new ImageDescriptor[] { background } : null;
 			ImageDescriptor resultImageDescriptor = new OverlayIcon(baseImage, overlays, locations, backgrounds, new Point(baseImage.getBounds().width, baseImage.getBounds().height));
 			if (imageCache == null) {
 				imageCache = new HashMap(10);
 			}
 			Image image = (Image) imageCache.get(overlay);
 			if (image == null) {
 				image = resultImageDescriptor.createImage();
 				imageCache.put(resultImageDescriptor, image);
 			}
 			return image;
 		}
 
 		@Override
 		public String getText(Object element) {
 			String prefix = null;
 			String suffix = null;
 			if (element.equals(PREVIEW_IGNORED_TXT)) {
 				// nothing
 			} else if (element.equals(PREVIEW_PROJECT)) {
 				// version info
 				if (addVersionInfo.getBooleanValue()) {
 					suffix = PREVIEW_VERSION_1;
 				}
 				// view name
 				if (addViewInfoToProjects.getBooleanValue()) {
 					if (null == suffix) {
 						suffix = PREVIEW_VIEW_TAG;
 					} else {
 						suffix += PREVIEW_VIEW_TAG;
 					}
 				}
 				// checked in
 				// dirty (has checkouts and hijacked)
 				if (decorateElementStatesWithTextPrefix.getBooleanValue()) {
 					if (dirtyFolders.getBooleanValue()) {
 						prefix = prefixDirtyResources.getStringValue();
 					} else {
 						prefix = prefixCheckedInResources.getStringValue();
 					}
 				}
 			} else if (element.equals(PREVIEW_FOLDER)) {
 				// version info
 				if (addVersionInfo.getBooleanValue()) {
 					suffix = PREVIEW_VERSION_2;
 				}
 				// checked in
 				// dirty (has view private and checked in)
 				if (decorateElementStatesWithTextPrefix.getBooleanValue()) {
 					if (dirtyFoldersWithViewPrivate.getBooleanValue() && dirtyFolders.getBooleanValue()) {
 						prefix = prefixDirtyResources.getStringValue();
 					} else {
 						prefix = prefixCheckedInResources.getStringValue();
 					}
 				}
 			} else if (element.equals(PREVIEW_VIEWPRIVATE_TXT)) {
 				// view private
 				if (decorateElementStatesWithTextPrefix.getBooleanValue()) {
 					prefix = prefixViewPrivateResources.getStringValue();
 				}
 			} else if (element.equals(PREVIEW_EDITEDSOMEWHERE_JAVA)) {
 				// version info
 				if (addVersionInfo.getBooleanValue()) {
 					suffix = PREVIEW_VERSION_3;
 				}
 				// edited in another view or branch
 				if (decorateElementStatesWithTextPrefix.getBooleanValue()) {
 					prefix = prefixResourcesEditedBySomeoneElse.getStringValue();
 				}
 			} else if (element.equals(PREVIEW_UNKNOWNSTATE_TXT)) {
 				// unknown state
 				if (decorateElementStatesWithTextPrefix.getBooleanValue()) {
 					prefix = prefixResourcesWithUnknownState.getStringValue();
 				}
 			} else if (element.equals(PREVIEW_CHECKEDIN_TXT)) {
 				// version info
 				if (addVersionInfo.getBooleanValue()) {
 					suffix = PREVIEW_VERSION_4;
 				}
 				// checked in
 				if (decorateElementStatesWithTextPrefix.getBooleanValue()) {
 					prefix = prefixCheckedInResources.getStringValue();
 				}
 			} else if (element.equals(PREVIEW_CHECKEDOUT_JAVA)) {
 				// checked out
 			} else if (element.equals(PREVIEW_HIJACKEDARCHIVE_ZIP)) {
 				// hijacked
 				if (decorateElementStatesWithTextPrefix.getBooleanValue()) {
 					prefix = prefixHijackedResources.getStringValue();
 				}
 			} else if (element.equals(PREVIEW_LINKED_FOLDER)) {
 				// linked
 				suffix = PREVIEW_LINKE_TARGET;
 			}
 
 			StringBuffer buffer = new StringBuffer();
 			if (prefix != null) {
 				buffer.append(prefix);
 			}
 			buffer.append((String) element);
 			if (suffix != null) {
 				buffer.append(suffix);
 			}
 			return buffer.toString();
 		}
 	}
 
 	/** category 'General' */
 	private static final String CAT_GENERAL = PreferenceMessages.getString("DecoratorPreferencePage.category.general"); //$NON-NLS-1$
 
 	/** category 'Icons' */
 	private static final String CAT_IMAGES = PreferenceMessages.getString("DecoratorPreferencePage.category.images"); //$NON-NLS-1$
 
 	/** category 'Text' */
 	private static final String CAT_TEXT = PreferenceMessages.getString("DecoratorPreferencePage.category.text"); //$NON-NLS-1$
 
 	/** array with all categories */
 	private static final String[] CATEGORIES = new String[] { CAT_TEXT, CAT_IMAGES, CAT_GENERAL };
 
 	/** cached descriptor */
 	static final ImageDescriptor IMG_DESC_CHECKED_IN;
 
 	/** cached descriptor */
 	static final ImageDescriptor IMG_DESC_DERIVED_OBJECT;
 
 	/** cached descriptor */
 	static final ImageDescriptor IMG_DESC_CHECKED_OUT;
 
 	/** cached descriptor */
 	static final ImageDescriptor IMG_DESC_DIRTY;
 
 	/** cached descriptor */
 	static final ImageDescriptor IMG_DESC_EDITED;
 
 	/** cached descriptor */
 	static ImageDescriptor IMG_DESC_ELEMENT_BG;
 
 	/** cached descriptor */
 	static final ImageDescriptor IMG_DESC_HIJACKED;
 
 	/** cached descriptor */
 	static final ImageDescriptor IMG_DESC_LINK;
 
 	/** cached descriptor */
 	static final ImageDescriptor IMG_DESC_LINK_WARNING;
 
 	/** cached descriptor */
 	static final ImageDescriptor IMG_DESC_NEW_RESOURCE;
 
 	/** cached descriptor */
 	static ImageDescriptor IMG_DESC_UNKNOWN_STATE;
 
 	private static final String PREVIEW_CHECKEDIN_TXT = PreferenceMessages.getString("DecoratorPreferencePage.preview.checkedInFile"); //$NON-NLS-1$
 
 	private static final String PREVIEW_CHECKEDOUT_JAVA = PreferenceMessages.getString("DecoratorPreferencePage.preview.checkedOutFile"); //$NON-NLS-1$
 
 	private static final String PREVIEW_EDITEDSOMEWHERE_JAVA = PreferenceMessages.getString("DecoratorPreferencePage.preview.editedBySomeoneElseFile"); //$NON-NLS-1$
 
 	private static final String PREVIEW_FOLDER = PreferenceMessages.getString("DecoratorPreferencePage.preview.folder"); //$NON-NLS-1$
 
 	private static final String PREVIEW_HIJACKEDARCHIVE_ZIP = PreferenceMessages.getString("DecoratorPreferencePage.preview.hijackedFile"); //$NON-NLS-1$
 
 	private static final String PREVIEW_IGNORED_TXT = PreferenceMessages.getString("DecoratorPreferencePage.preview.ignoredFile"); //$NON-NLS-1$
 
 	private static final String PREVIEW_LINKE_TARGET = PreferenceMessages.getString("DecoratorPreferencePage.preview.linkTarget"); //$NON-NLS-1$
 
 	private static final String PREVIEW_LINKED_FOLDER = PreferenceMessages.getString("DecoratorPreferencePage.preview.linkedFolder"); //$NON-NLS-1$
 
 	private static final String PREVIEW_PROJECT = PreferenceMessages.getString("DecoratorPreferencePage.preview.project"); //$NON-NLS-1$
 
 	private static final String PREVIEW_UNKNOWNSTATE_TXT = PreferenceMessages.getString("DecoratorPreferencePage.preview.unknownStateFile"); //$NON-NLS-1$
 
 	private static final String PREVIEW_VERSION_1 = PreferenceMessages.getString("DecoratorPreferencePage.preview.version1"); //$NON-NLS-1$
 
 	private static final String PREVIEW_VERSION_2 = PreferenceMessages.getString("DecoratorPreferencePage.preview.version2"); //$NON-NLS-1$
 
 	private static final String PREVIEW_VERSION_3 = PreferenceMessages.getString("DecoratorPreferencePage.preview.version3"); //$NON-NLS-1$
 
 	private static final String PREVIEW_VERSION_4 = PreferenceMessages.getString("DecoratorPreferencePage.preview.version4"); //$NON-NLS-1$
 
 	private static final String PREVIEW_VIEW_TAG = PreferenceMessages.getString("DecoratorPreferencePage.preview.viewTag"); //$NON-NLS-1$
 
 	private static final String PREVIEW_VIEWPRIVATE_TXT = PreferenceMessages.getString("DecoratorPreferencePage.preview.viewPrivateFile"); //$NON-NLS-1$
 
 	private static final String PREVIEW_DERIVED_OBJECT = PreferenceMessages.getString("DecoratorPreferencePage.preview.derivedObject"); //$NON-NLS-1$
 
 	static {
 		IMG_DESC_DIRTY = new CachedImageDescriptor(TeamImages.getImageDescriptor(org.eclipse.team.ui.ISharedImages.IMG_DIRTY_OVR));
 		IMG_DESC_CHECKED_IN = new CachedImageDescriptor(TeamImages.getImageDescriptor(org.eclipse.team.ui.ISharedImages.IMG_CHECKEDIN_OVR));
 		IMG_DESC_CHECKED_OUT = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_CHECKEDOUT_OVR));
 		IMG_DESC_NEW_RESOURCE = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_QUESTIONABLE_OVR));
 		IMG_DESC_EDITED = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_EDITED_OVR));
 		IMG_DESC_UNKNOWN_STATE = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_UNKNOWN_OVR));
 		IMG_DESC_DERIVED_OBJECT = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_DERIVEDOBJECT_OVR));
 		IMG_DESC_LINK = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_LINK_OVR));
 		IMG_DESC_LINK_WARNING = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_LINK_WARNING_OVR));
 		IMG_DESC_HIJACKED = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_HIJACKED_OVR));
 		IMG_DESC_ELEMENT_BG = new CachedImageDescriptor(ClearcaseImages.getImageDescriptor(ClearcaseImages.IMG_ELEMENT_BG));
 	}
 
 	BooleanFieldEditor addVersionInfo;
 
 	BooleanFieldEditor addViewInfoToProjects;
 
 	// MasterBooleanFieldEditor customClearCaseElementsBackground;
 
 	BooleanFieldEditor decorateCheckedIn;
 
 	BooleanFieldEditor decorateDerivedObjects;
 
 	MasterBooleanFieldEditor decorateClearCaseElements;
 
 	BooleanFieldEditor decorateEdited;
 
 	MasterBooleanFieldEditor decorateElementStatesWithTextPrefix;
 
 	BooleanFieldEditor decorateHijacked;
 
 	BooleanFieldEditor decorateUnknown;
 
 	BooleanFieldEditor decorateViewPrivate;
 
 	MasterBooleanFieldEditor dirtyFolders;
 
 	BooleanFieldEditor dirtyFoldersWithViewPrivate;
 
 	/** Cache for folder images that have been overlayed with sample CVS icons */
 	Map imageCache;
 
 	// FileFieldEditor imageClearCaseElementsBackground;
 
 	StringFieldEditor prefixCheckedInResources;
 
 	StringFieldEditor prefixDirtyResources;
 
 	StringFieldEditor prefixHijackedResources;
 
 	StringFieldEditor prefixResourcesEditedBySomeoneElse;
 
 	StringFieldEditor prefixResourcesWithUnknownState;
 
 	StringFieldEditor prefixViewPrivateResources;
 
 	/** Tree that provides a preview of the decorations */
 	TreeViewer previewTree;
 
 	/**
 	 * Update the preview tree when a theme changes.
 	 */
 	private final IPropertyChangeListener themeListener = new IPropertyChangeListener() {
 
 		public void propertyChange(PropertyChangeEvent event) {
 			previewTree.refresh(true /* update labels */);
 		}
 	};
 
 	/**
 	 * Creates a new instance.
 	 */
 	public DecoratorPreferencePage() {
 		super();
 
 		// Set the preference store for the preference page.
 		setPreferenceStore(ClearcaseUI.getInstance().getPreferenceStore());
 
 		setDescription(PreferenceMessages.getString("DecoratorPreferencePage.description")); //$NON-NLS-1$
 	}
 
 	/**
 	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
 	 */
 	@Override
 	protected void createFieldEditors() {
 		// general
 		dirtyFolders = new MasterBooleanFieldEditor(ClearcaseUIPreferences.DECORATE_FOLDERS_DIRTY, PreferenceMessages.getString("DecoratorPreferencePage.folders.dirty"), //$NON-NLS-1$
 				getFieldEditorParent(CAT_GENERAL));
 		addField(dirtyFolders);
 
 		dirtyFoldersWithViewPrivate = new BooleanFieldEditor(ClearcaseUIPreferences.DECORATE_FOLDERS_CONTAINING_VIEW_PRIVATE_DIRTY, PreferenceMessages.getString("DecoratorPreferencePage.folders.dirty.withViewPrivate"), getFieldEditorParent(CAT_GENERAL));//$NON-NLS-1$
 		addField(dirtyFoldersWithViewPrivate);
 		dirtyFolders.addSlave(dirtyFoldersWithViewPrivate);
 
 		// image decoration
 
 		decorateClearCaseElements = new MasterBooleanFieldEditor(ClearcaseUIPreferences.DECORATE_CLEARCASE_ELEMENTS, PreferenceMessages.getString("DecoratorPreferencePage.clearCaseElements"), getFieldEditorParent(CAT_IMAGES)); //$NON-NLS-1$
 		addField(decorateClearCaseElements);
 
 		//		customClearCaseElementsBackground = new MasterBooleanFieldEditor(ClearcaseUIPreferences.IMAGE_CLEARCASE_ELEMENTS_BACKGROUND_CUSTOM, PreferenceMessages.getString("DecoratorPreferencePage.customClearCaseElementsBackground"), getFieldEditorParent(CAT_IMAGES)); //$NON-NLS-1$
 		// decorateClearCaseElements.addSlave(customClearCaseElementsBackground);
 		// addField(customClearCaseElementsBackground);
 
 		//		imageClearCaseElementsBackground = new FileFieldEditor(ClearcaseUIPreferences.IMAGE_CLEARCASE_ELEMENTS_BACKGROUND, PreferenceMessages.getString("DecoratorPreferencePage.imageClearCaseElementsBackground"), getFieldEditorParent(CAT_IMAGES)); //$NON-NLS-1$
 		//		imageClearCaseElementsBackground.setFileExtensions(new String[] { "*.gif;*.jpg;*.png" }); //$NON-NLS-1$
 		// imageClearCaseElementsBackground.setEmptyStringAllowed(true);
 		// customClearCaseElementsBackground.addSlave(imageClearCaseElementsBackground);
 		// addField(imageClearCaseElementsBackground);
 
 		decorateCheckedIn = new BooleanFieldEditor(ClearcaseUIPreferences.DECORATE_CHECKED_IN_ELEMENTS, PreferenceMessages.getString("DecoratorPreferencePage.checkedIn"), getFieldEditorParent(CAT_IMAGES)); //$NON-NLS-1$
 		addField(decorateCheckedIn);
 
		// decorateEdited = new BooleanFieldEditor(ClearcaseUIPreferences.DECORATE_EDITED_ELEMENTS, PreferenceMessages.getString("DecoratorPreferencePage.edited"), getFieldEditorParent(CAT_IMAGES)); //$NON-NLS-1$
		// addField(decorateEdited);
 
 		decorateHijacked = new BooleanFieldEditor(ClearcaseUIPreferences.DECORATE_HIJACKED_ELEMENTS, PreferenceMessages.getString("DecoratorPreferencePage.hijacked"), //$NON-NLS-1$
 				getFieldEditorParent(CAT_IMAGES));
 		addField(decorateHijacked);
 
 		decorateViewPrivate = new BooleanFieldEditor(ClearcaseUIPreferences.DECORATE_VIEW_PRIVATE_ELEMENTS, PreferenceMessages.getString("DecoratorPreferencePage.viewPrivate"), //$NON-NLS-1$
 				getFieldEditorParent(CAT_IMAGES));
 		addField(decorateViewPrivate);
 
 		decorateDerivedObjects = new BooleanFieldEditor(ClearcaseUIPreferences.DECORATE_DERIVED_OBJECTS, PreferenceMessages.getString("DecoratorPreferencePage.derivedObjects"), //$NON-NLS-1$
 				getFieldEditorParent(CAT_IMAGES));
 		addField(decorateDerivedObjects);
 
 		decorateUnknown = new BooleanFieldEditor(ClearcaseUIPreferences.DECORATE_UNKNOWN_ELEMENTS, PreferenceMessages.getString("DecoratorPreferencePage.unknown"), //$NON-NLS-1$
 				getFieldEditorParent(CAT_IMAGES));
 		addField(decorateUnknown);
 
 		// text decorations
 
 		addViewInfoToProjects = new BooleanFieldEditor(ClearcaseUIPreferences.DECORATE_PROJECTS_WITH_VIEW_INFO, PreferenceMessages.getString("DecoratorPreferencePage.addViewInfoToProjects"), //$NON-NLS-1$
 				getFieldEditorParent(CAT_TEXT));
 		addField(addViewInfoToProjects);
 
 		addVersionInfo = new BooleanFieldEditor(ClearcaseUIPreferences.DECORATE_ELEMENTS_WITH_VERSION_INFO, PreferenceMessages.getString("DecoratorPreferencePage.appendVersionInfoToResources"), //$NON-NLS-1$
 				getFieldEditorParent(CAT_TEXT));
 		addField(addVersionInfo);
 
 		decorateElementStatesWithTextPrefix = new MasterBooleanFieldEditor(ClearcaseUIPreferences.DECORATE_ELEMENT_STATES_WITH_TEXT_PREFIX, PreferenceMessages.getString("DecoratorPreferencePage.prependResourceNamesWithStateInfoFor"), //$NON-NLS-1$
 				getFieldEditorParent(CAT_TEXT));
 		addField(decorateElementStatesWithTextPrefix);
 
 		prefixCheckedInResources = new StringFieldEditor(ClearcaseUIPreferences.TEXT_PREFIX_CHECKED_IN_ELEMENTS, PreferenceMessages.getString("DecoratorPreferencePage.checkedInResources"), 4, getFieldEditorParent(CAT_TEXT));//$NON-NLS-1$
 		addField(prefixCheckedInResources);
 		decorateElementStatesWithTextPrefix.addSlave(prefixCheckedInResources);
 
 		prefixDirtyResources = new StringFieldEditor(ClearcaseUIPreferences.TEXT_PREFIX_DIRTY_ELEMENTS, PreferenceMessages.getString("DecoratorPreferencePage.dirtyResources"), 4, getFieldEditorParent(CAT_TEXT));//$NON-NLS-1$
 		addField(prefixDirtyResources);
 		decorateElementStatesWithTextPrefix.addSlave(prefixDirtyResources);
 
 		prefixHijackedResources = new StringFieldEditor(ClearcaseUIPreferences.TEXT_PREFIX_HIJACKED_ELEMENTS, PreferenceMessages.getString("DecoratorPreferencePage.hijackedResources"), 4, getFieldEditorParent(CAT_TEXT));//$NON-NLS-1$
 		addField(prefixHijackedResources);
 		decorateElementStatesWithTextPrefix.addSlave(prefixHijackedResources);
 
 		prefixViewPrivateResources = new StringFieldEditor(ClearcaseUIPreferences.TEXT_PREFIX_VIEW_PRIVATE_ELEMENTS, PreferenceMessages.getString("DecoratorPreferencePage.viewPrivateResources"), 4, getFieldEditorParent(CAT_TEXT));//$NON-NLS-1$
 		addField(prefixViewPrivateResources);
 		decorateElementStatesWithTextPrefix.addSlave(prefixViewPrivateResources);
 
		//		prefixResourcesEditedBySomeoneElse = new StringFieldEditor(ClearcaseUIPreferences.TEXT_PREFIX_EDITED_ELEMENTS, PreferenceMessages.getString("DecoratorPreferencePage.resourcesEditedBySomeoneElse"), 4, //$NON-NLS-1$
		// getFieldEditorParent(CAT_TEXT));
		// addField(prefixResourcesEditedBySomeoneElse);
		// decorateElementStatesWithTextPrefix.addSlave(prefixResourcesEditedBySomeoneElse);
 
 		prefixResourcesWithUnknownState = new StringFieldEditor(ClearcaseUIPreferences.TEXT_PREFIX_UNKNOWN_ELEMENTS, PreferenceMessages.getString("DecoratorPreferencePage.resourcesWithUnknownState"), 4, //$NON-NLS-1$
 				getFieldEditorParent(CAT_TEXT));
 		addField(prefixResourcesWithUnknownState);
 		decorateElementStatesWithTextPrefix.addSlave(prefixResourcesWithUnknownState);
 	}
 
 	/**
 	 * Creates a preview.
 	 * 
 	 * @see net.sourceforge.eclipseccase.ui.preferences.FieldEditorPreferencePageWithCategories#createInformationAreaContent(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	protected Control createInformationAreaContent(Composite parent) {
 		// Preview Pane
 		previewTree = new TreeViewer(parent);
 		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
 		data.heightHint = 200;
 		previewTree.getTree().setLayoutData(data);
 		previewTree.setContentProvider(new PreviewContentProvider());
 		previewTree.setLabelProvider(new PreviewLabelProvider());
 		previewTree.setInput(ResourcesPlugin.getWorkspace().getRoot());
 		previewTree.expandAll();
 
 		// track theme changes
 		PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(themeListener);
 
 		return previewTree.getControl();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#dispose()
 	 */
 	@Override
 	public void dispose() {
 
 		// remove theme listener
 		PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(themeListener);
 
 		// dispose image cache
 		if (null != imageCache) {
 			Iterator images = imageCache.values().iterator();
 			while (images.hasNext()) {
 				Image image = (Image) images.next();
 				image.dispose();
 			}
 			imageCache = null;
 		}
 
 		// call super
 		super.dispose();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seenet.sourceforge.eclipseccase.ui.preferences.
 	 * FieldEditorPreferencePageWithCategories#getCategories()
 	 */
 	@Override
 	protected String[] getCategories() {
 		return CATEGORIES;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seenet.sourceforge.eclipseccase.ui.preferences.
 	 * FieldEditorPreferencePageWithCategories#getDescription(java.lang.String)
 	 */
 	@Override
 	protected String getDescription(String category) {
 		if (CAT_GENERAL.equals(category))
 			return PreferenceMessages.getString("DecoratorPreferencePage.category.general.description"); //$NON-NLS-1$
 		if (CAT_IMAGES.equals(category))
 			return PreferenceMessages.getString("DecoratorPreferencePage.category.images.description"); //$NON-NLS-1$
 		if (CAT_TEXT.equals(category))
 			return PreferenceMessages.getString("DecoratorPreferencePage.category.text.description"); //$NON-NLS-1$
 
 		return null;
 	}
 
 	public void init(IWorkbench workbench) {
 		// nothing
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
 	 */
 	@Override
 	public boolean performOk() {
 		if (super.performOk()) {
 			// refresh the decorator
 			IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
 			if (manager.getEnabled(ClearcaseDecorator.ID)) {
 				ClearcaseDecorator activeDecorator = (ClearcaseDecorator) manager.getBaseLabelProvider(ClearcaseDecorator.ID);
 				if (activeDecorator != null) {
 					activeDecorator.refresh();
 				}
 			}
 			return true;
 		}
 
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange
 	 * (org.eclipse.jface.util.PropertyChangeEvent)
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 
 		if (FieldEditor.VALUE.equals(event.getProperty())) {
 			previewTree.refresh(true /* update labels */);
 		}
 
 		// call super
 		super.propertyChange(event);
 	}
 
 }
