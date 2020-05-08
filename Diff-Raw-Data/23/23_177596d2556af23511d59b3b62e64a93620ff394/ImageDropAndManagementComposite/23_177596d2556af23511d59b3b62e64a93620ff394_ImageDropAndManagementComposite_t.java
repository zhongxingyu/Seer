 /** -----------------------------------------------------------------
  *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
  *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ** ----------------------------------------------------------------- */
 
 package org.sammelbox.view.image;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.ImageTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Widget;
 import org.sammelbox.controller.GuiController;
 import org.sammelbox.controller.i18n.DictKeys;
 import org.sammelbox.controller.i18n.Translator;
 import org.sammelbox.model.album.AlbumItemPicture;
 import org.sammelbox.view.ApplicationUI;
 import org.sammelbox.view.various.ComponentFactory;
 
 public class ImageDropAndManagementComposite extends Composite implements DropTargetListener{
 	/** A list of images pointing to copies of the original files, located within the corresponding album folder */
 	private LinkedList<AlbumItemPicture> pictures = new LinkedList<AlbumItemPicture>();
 	/** An inner composite presenting the pictures */
 	private Composite imageComposite;
 	/** An inner scrollable composite wrapping the imageComposite */ 
 	private ScrolledComposite imageScrolledComposite;
 
 	/** Creates a new ImageDropAndManagementComposite as a child of the provided parent composite 
 	 * @param parentComposite the parent composite of the ImageDropAndManagementComposite */
 	public ImageDropAndManagementComposite(Composite parentComposite) {
 		super(parentComposite, SWT.NONE);
 		this.initialize();
 	}
 
 	/** Creates a new ImageDropAndManagementComposite as a child of the provided parent composite. 
 	 * Images can be provided for inclusion upon creation.
 	 * @param parentComposite the parent composite of the ImageDropAndManagementComposite 
 	 * @param pictures a list of pictures pointing to images that should be included within the imageComposite */
 	public ImageDropAndManagementComposite(Composite parentComposite, List<AlbumItemPicture> pictures) {	
 		super(parentComposite, SWT.NONE);
 		this.initialize();
 
 		for (AlbumItemPicture picture : pictures) {
 			this.pictures.addLast(picture);
 		}
 
 		this.refreshImageComposite();
 	}
 
 	/** Initializes the ImageDropAndManagementComposite by setting default sizes, labels etc..*/
 	private void initialize() {
 		this.setLayout(new GridLayout(1, false));
 		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		Label dropTextLabel = new Label(this, SWT.BORDER | SWT.CENTER | SWT.VERTICAL);
 		dropTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		dropTextLabel.setText(Translator.get(DictKeys.LABEL_DROP_IMAGE_HERE));
 		addDropSupport(dropTextLabel);
 
 		imageScrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
 		imageScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		imageComposite = new Composite(imageScrolledComposite, SWT.NONE);
 		imageComposite.setLayout(new GridLayout(4, false));
 		imageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		imageScrolledComposite.setContent(imageComposite);
 
 		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gridData.heightHint = 130;
 
 		imageScrolledComposite.setLayoutData(gridData);
 
 		addDropSupport(imageScrolledComposite);
 	}
 
 	/** This method adds drop (drag & drop) support to a given SWT widget 
 	 * @param widget an SWT widget such as a label or a composite */
 	private void addDropSupport(final Widget widget) {
 		int ops = DND.DROP_COPY | DND.DROP_LINK | DND.DROP_DEFAULT;
 		final FileTransfer fTransfer = FileTransfer.getInstance();
 		final ImageTransfer iTransfer = ImageTransfer.getInstance();
 		Transfer[] transfers = new Transfer[] { fTransfer, iTransfer };
 		
 
 		DropTarget target = new DropTarget((Control) widget, ops);
 		target.setTransfer(transfers);
 		target.addDropListener(this);
 	}
 
 	/** This method refreshes the ImageDropAndManagementComposite in a sense that the image composite is completely rebuild. This method
 	 * should be called after the creation of a ImageDropAndManagementComposite when pictures are provided, or in case a new picture 
 	 * has been added to the picture list */
 	public void refreshImageComposite() {
 		for (Control control : imageComposite.getChildren()) {
 			control.dispose();
 		}
 
 		for (final AlbumItemPicture picture : pictures) {			
 			Image originalImage = new Image(Display.getCurrent(), picture.getOriginalPicturePath());	
 			Image scaledImage = new Image(Display.getCurrent(), originalImage.getImageData().scaledTo(100, 100));
 
 			Label pictureLabel = new Label(imageComposite, SWT.NONE);
 			pictureLabel.setImage(scaledImage);
 			Button deleteButton = new Button(imageComposite, SWT.NONE);
 			deleteButton.setText(Translator.get(DictKeys.BUTTON_REMOVE));
 			deleteButton.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					pictures.remove(picture);
 
 					refreshImageComposite();
 				}
 			});
 			
 			InputStream istream = this.getClass().getClassLoader().getResourceAsStream("graphics/arrow-up.png");
 			Image arrowUp = new Image(Display.getCurrent(),istream);
 
 			Button upButton = new Button(imageComposite, SWT.NONE);
 			upButton.setImage(arrowUp);
 			upButton.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					int index = pictures.indexOf(picture);
 
 					if (index > 0) {					
 						AlbumItemPicture tmpPicture = picture;
 
 						pictures.remove(picture);
 						pictures.add(index - 1, tmpPicture);
 
 						refreshImageComposite();
 					}
 				}
 			});
 			
 			istream = this.getClass().getClassLoader().getResourceAsStream("graphics/arrow-down.png");
 			Image arrowDown = new Image(Display.getCurrent(),istream);
 			
 			Button downButton = new Button(imageComposite, SWT.NONE);
 			downButton.setImage(arrowDown);
 			downButton.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					int index = pictures.indexOf(picture);
 
 					if (index < pictures.size() - 1) {
 						AlbumItemPicture tmpPicture = picture;
 
 						pictures.remove(picture);
 						pictures.add(index + 1, tmpPicture);
 
 						refreshImageComposite();
 					}
 				}
 			});
 		}
 
 		imageComposite.pack();
 		imageComposite.layout();
 	}
 
 	/** Return all images 
 	 * @return the images */
 	public List<AlbumItemPicture> getAllPictures() {		
 		return pictures;
 	}
 
 	@Override
 	public void dragEnter(DropTargetEvent event) {
 		// Changing the event detail to drop_copy enables the drop. 
 		event.detail = DND.DROP_COPY;
 	}
 	@Override
 	public void dragLeave(DropTargetEvent arg0) {	
 	}
 
 	@Override
 	public void dragOperationChanged(DropTargetEvent arg0) {		
 	}
 
 	@Override
 	public void dragOver(DropTargetEvent arg0) {
 	}
 
 	@Override
 	public void drop(DropTargetEvent event) {
		if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
 			return;
 		}
 		if (event.data instanceof String[]) {
 			String[] filenames = (String[]) event.data;
 			if (filenames.length > 0){
 				for (String filename : filenames) {
 					AlbumItemPicture picture = ImageManipulator.adaptAndStoreImageForCollector(
 							new File(filename), GuiController.getGuiState().getSelectedAlbum());
 					if (picture == null) {
 						showDroppedUnsupportedFileMessageBox(filename);
 					} else {
 						pictures.add(picture);
 					}
 				}
 				refreshImageComposite();
 			}
 		}
 	}
 
 	@Override
 	public void dropAccept(DropTargetEvent arg0) {	
 	}
 	
 	/** This method displays a message box informing the user of trying to drop the unsupported file.*/
 	public void showDroppedUnsupportedFileMessageBox(String filePathToUnsupportedFilegeBox){
 	    ComponentFactory.getMessageBox(getShell(), 
 	    		Translator.get(DictKeys.DIALOG_TITLE_INVALID_IMAGE_FILE_FORMAT), 
 	    		Translator.get(DictKeys.DIALOG_CONTENT_INVALID_IMAGE_FILE_FORMAT), 
 	    		SWT.ICON_ERROR).open();
 	}
 }
