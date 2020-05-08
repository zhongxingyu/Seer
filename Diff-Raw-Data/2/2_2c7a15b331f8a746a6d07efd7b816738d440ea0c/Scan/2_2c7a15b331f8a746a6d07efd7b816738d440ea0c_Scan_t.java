 package fm.audiobox.sync.task;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import fm.audiobox.sync.util.AsyncTask;
 
 public class Scan extends AsyncTask {
 
 
     private boolean _recursive = false;
     private boolean _hidden = false;
     private File _folder = null;
     private FileFilter _ff = null;
     private List<File> files = null;
 
     private static final List<String> ALLOWED_MEDIA = Arrays.asList(new String[]{ "mp3", "mpeg3"});
 
     public Scan( File folder , boolean recursive){
         this(folder,recursive,false);
     }
 
     public Scan( File folder, boolean recursive , boolean hidden ){
         this._folder = folder;
         this._recursive = recursive;
         this._hidden = hidden;
 
     }
 
     public final void setFilter( FileFilter fileFilter ){
         this._ff = fileFilter;
     }
 
     public FileFilter getFilter(){
         return this._ff;
     }
 
 
     @Override
     protected synchronized void doTask() {
 
         this._startScan(  this._folder );
 
     }
 
     public List<File> scan(){
         this.start();
         this.doTask();
         return this.end();
     }
 
     private void _startScan( File folder ){
         File[] files = folder.listFiles( new FileFilter() {
 
             @Override
             public boolean accept(File pathname) {
 
                 if ( ! pathname.canRead() ) return false;
                 
                 if ( pathname.isHidden() && ! _hidden ) return false;
 
                 if ( pathname.isDirectory() && ! _recursive ) return false;
                 
                 if ( _ff != null ){
                 	return _ff.accept( pathname );
                 } else {
                 	for ( String ext : ALLOWED_MEDIA ) 
                		if ( pathname.getName().endsWith( "." + ext ) ) 
                 			return true;
                 }
 //                /* TODO: check file type */
 //                String[] nameParts = pathname.getName().split("\\.");
 //                
 //                if ( ! ALLOWED_MEDIA.contains( nameParts[ nameParts.length -1 ].toLowerCase() ) && ! pathname.isDirectory() ) return false;
 
                 return false;
 
             }
 
         });
         
         for ( File file : files ){
             
             if ( this.isStopped() ) return;
             
             if ( file.isDirectory() ){
                 this._startScan( file );
                 continue;
             }
             this.files.add( file );
             this.getThreadListener().onProgress(this, 0, 0, 0, file);
             
         }
     }
 
 
     @Override
     protected synchronized List<File> end() {
         return this.files;
     }
 
     @Override
     protected synchronized void start() {
         this.files = new ArrayList<File>();
     }
 
 }
