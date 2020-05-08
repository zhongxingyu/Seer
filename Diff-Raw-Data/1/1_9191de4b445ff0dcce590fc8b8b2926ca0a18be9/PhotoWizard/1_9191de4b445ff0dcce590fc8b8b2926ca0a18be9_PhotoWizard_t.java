 package de.hswt.hrm.photo.ui.wizard;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.inject.Inject;
 import org.eclipse.e4.core.contexts.ContextInjectionFactory;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.widgets.TableItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.photo.model.Photo;
 
 public class PhotoWizard extends Wizard {
     private static final Logger LOG = LoggerFactory.getLogger(PhotoWizard.class);
 //
 //    @Inject
 //    private PhotoService photoService;
     
     private PhotoWizardPageOne first;
     
 //    private Optional<List<Photo>> photos;
 
     private Optional<List<Photo>> currentPhotoList;
     
     private TableItem[] tableItems;
     
     private List<Photo> photos;
     
     @Inject
     IEclipseContext context;
 
     
     
     public PhotoWizard(List<Photo> photos) {
         this.photos = photos;
         first = new PhotoWizardPageOne(photos);
         ContextInjectionFactory.inject(first, context);
         
 //        if (photos.isPresent()) {
 //        	setWindowTitle("Edit photos");
 //        } else {
             setWindowTitle("Import photos");
 //        } 
     }
 
     @Override
     public void addPages() {
         addPage(first);
     }
     
     @Override
     public boolean canFinish() {
     	return first.isPageComplete();
     }
 
     @Override
     public boolean performFinish() {
 //    	List<String> oldPhotoNames = new LinkedList<String>();
 //    	for(Photo photo : photos){
 //    		oldPhotoNames.add(photo.getName());    		
 //    	}    	
 //    	for(TableItem item : first.getTableItems()){    		
 //    		if(!oldPhotoNames.contains(((Photo)item.getData()).getName())){
 //    			this.photos.add((Photo)item.getData());
 //    			//photoService.insert((Photo)item.getData());
 //    		}
 //    	}
     	int i = 0;
     	for(TableItem item : first.getTableItems()){
     		item.getText();
     		this.photos.get(i).setLabel(item.getText());
     		i++;
     	}
 //    	for(Photo photo : photos){
 //    		//photoService.update(photo);
 //    		
 //    	}
 //    	oldPhotoNames.clear();
     	
     	int x = 0;
     	for(Photo photo : photos){
     		if(photo.getId() == -1){
 //   TODO 			photos.set(x,photoService.insert(photo)); 
     		} else {
 //    TODO		photoService.update(photo);   			
     		}
     		x++;
     		
     	}
 		return true;
     
 //    	if (photos.isPresent()) {
 //        	for(TableItem item : first.getTableItems()){
 //        		this.photos.get().add((Photo)item.getData());    		
 //        	}
 //            return editExistingPhotos();
 //        } else {
 //        	for(TableItem item : first.getTableItems()){
 //        		this.photos.get().add((Photo)item.getData()); 		
 //        	}
 //            return importNewPhotos();
 //        }
     }
     
     private boolean editExistingPhotos() {
     	TableItem[] tableItems = first.getTableItems();
     	for(TableItem item : tableItems){
     		File f = (File)item.getData();
 			try {					
 				FileInputStream  in = new FileInputStream(f);
 				byte[] data = new byte[in.available()];
 				in.read(data);
 				Photo photo = new Photo(data, f.getName(), item.getText());
 				
 				//TODO Photoservice insert
 	    		
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}    	
 			catch (IOException e) {
 				e.printStackTrace();
 			}			
     	}  	
     	
     	return true;
     }
     
     private boolean importNewPhotos() {
     	TableItem[] tableItems = first.getTableItems();
     	for(TableItem item : tableItems){
     		Photo f = (Photo)item.getData();
     		//photoService.insert(photo);
     	
     	
     	}  	
     	return true;
     }
     
     private void fillPhotoValues(Optional<List<Photo>> photos) {
         List<Photo> newPhotos = new ArrayList<Photo>();
         
         currentPhotoList = Optional.fromNullable(newPhotos);
     }
     
     public Optional<List<Photo>> getPhotos() {
         return currentPhotoList;
     }
     public TableItem[] getTableItems(){
 		return tableItems;    	
     }
 
 }
