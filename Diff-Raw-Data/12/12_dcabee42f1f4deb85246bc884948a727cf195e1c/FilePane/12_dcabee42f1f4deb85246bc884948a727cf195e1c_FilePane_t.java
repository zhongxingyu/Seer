 package nextapp.echo2.app.filetransfer;
 
 import nextapp.echo2.app.Component;
 import nextapp.echo2.app.Pane;
 
 public class FilePane extends Component 
 implements Pane {
     
     public static final String PROVIDER_CHANGED_PROPERTY = "provider";
     
     private DownloadProvider provider;
 
     /**
      * Creates a new <code>FilePane</code> with no download
      * provider.
      */
     public FilePane() {
         this(null);
     }
     
     /**
      * Creates a new <code>Download</code> command with the specified 
      * producer and active state.
      *
      * @param provider The <code>DownloadProvider</code> that will provide the
      *        file download.
      */
     public FilePane(DownloadProvider provider) {
         super();
         this.provider = provider;
     }
 
     /** 
      * Returns the <code>DownloadProvider</code> that will provide the file
      * download.
      *
      * @return The <code>DownloadProvider</code> that will provide the file
      *         download.
      */
     public DownloadProvider getProvider() {
         return provider;
     }
     
     /**    
      * Sets the <code>DownloadProvider</code> that will provide the file
      * download.
      *
      * @param newValue A <code>DownloadProvider</code> that will provide the file
      * download.
      */
     public void setProvider(DownloadProvider newValue) {
         DownloadProvider oldValue = provider;
         provider = newValue;
         firePropertyChange(PROVIDER_CHANGED_PROPERTY, oldValue, newValue);
     }
 }
