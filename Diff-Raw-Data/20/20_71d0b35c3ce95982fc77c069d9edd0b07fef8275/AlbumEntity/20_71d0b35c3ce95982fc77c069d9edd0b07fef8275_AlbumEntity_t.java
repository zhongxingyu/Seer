 package ch.bergturbenthal.image.provider.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 import ch.bergturbenthal.image.provider.Client;
 import ch.bergturbenthal.image.provider.map.CursorField;
 
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.field.ForeignCollectionField;
 import com.j256.ormlite.table.DatabaseTable;
 
 @DatabaseTable(tableName = "albums")
 public class AlbumEntity {
 
   @DatabaseField
   private Date albumCaptureDate;
 
   @DatabaseField(foreign = true, uniqueIndexName = "name_index", canBeNull = false)
   private final ArchiveEntity archive;
 
   @DatabaseField
   private Date autoAddDate;
 
   @ForeignCollectionField(eager = false)
   private final Collection<AlbumEntryEntity> entries = new ArrayList<AlbumEntryEntity>();
 
   @DatabaseField(generatedId = true)
   private int id;
 
   @DatabaseField
   private final String commId;
 
   @ForeignCollectionField(eager = true)
   private final Collection<ClientEntity> interestingClients = new ArrayList<ClientEntity>();
 
   @DatabaseField(canBeNull = false, uniqueIndexName = "name_index")
   private final String name;
 
   @DatabaseField
   private boolean shouldSync = false;
 
   @DatabaseField
   private boolean synced = false;
 
   @DatabaseField
   private boolean syncThumbnails = false;
   @DatabaseField(foreign = true)
   private AlbumEntryEntity thumbnail;
   @DatabaseField
   private Date lastModified;
 
  public AlbumEntity(final ArchiveEntity archive, final String name, final String commId) {
     this.archive = archive;
     this.name = name;
     this.commId = commId;
   }
 
   AlbumEntity() {
     id = -1;
     archive = null;
     name = null;
     commId = null;
   }
 
   @CursorField(Client.Album.NAME)
   public String evalLocalName() {
     if (name == null)
       return null;
     final String[] parts = name.split("/");
     if (parts.length < 1)
       return null;
     return parts[parts.length - 1];
   }
 
   @CursorField(Client.Album.ALBUM_CAPTURE_DATE)
   public Date getAlbumCaptureDate() {
     return albumCaptureDate;
   }
 
   public ArchiveEntity getArchive() {
     return archive;
   }
 
   @CursorField(Client.Album.AUTOADD_DATE)
   public Date getAutoAddDate() {
     return autoAddDate;
   }
 
   public String getCommId() {
     return commId;
   }
 
   public Collection<AlbumEntryEntity> getEntries() {
     return entries;
   }
 
   @CursorField(Client.Album.ID)
   public int getId() {
     return id;
   }
 
   public Collection<ClientEntity> getInterestingClients() {
     return interestingClients;
   }
 
   public Date getLastModified() {
     return lastModified;
   }
 
   @CursorField(Client.Album.FULL_NAME)
   public String getName() {
     return name;
   }
 
   public boolean getSyncThumbnails() {
     return syncThumbnails;
   }
 
   public AlbumEntryEntity getThumbnail() {
     return thumbnail;
   }
 
   @CursorField(Client.Album.SHOULD_SYNC)
   public boolean isShouldSync() {
     return shouldSync;
   }
 
   @CursorField(Client.Album.SYNCED)
   public boolean isSynced() {
     return synced;
   }
 
   public void setAlbumCaptureDate(final Date albumCaptureDate) {
     this.albumCaptureDate = albumCaptureDate;
   }
 
   public void setAutoAddDate(final Date autoAddDate) {
     this.autoAddDate = autoAddDate;
   }
 
   public void setLastModified(final Date lastModified) {
     this.lastModified = lastModified;
   }
 
   public void setShouldSync(final boolean shouldSync) {
     this.shouldSync = shouldSync;
   }
 
   public void setSynced(final boolean isSynced) {
     this.synced = isSynced;
   }
 
   public void setSyncThumbnails(final boolean syncThumbnails) {
     this.syncThumbnails = syncThumbnails;
   }
 
   public void setThumbnail(final AlbumEntryEntity thumbnail) {
     this.thumbnail = thumbnail;
   }
 
 }
