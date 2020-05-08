 /*
  * Copyright 2011 Ian D. Bollinger
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.celeria.minecraft.backup;
 
 import javax.annotation.concurrent.Immutable;
 import com.google.inject.*;
 import org.apache.commons.vfs2.*;
 import org.celeria.minecraft.backup.BackUpWorldsTask.BackupFolder;
 import org.joda.time.*;
 import org.slf4j.cal10n.LocLogger;
 
 @Immutable
 class DeleteOldBackupsTask implements Runnable {
     private final LocLogger log;
     private final FileProvider<FileObject> backupFolderProvider;
     private final Duration durationToKeepBackups;
     private final Instant currentTime;
 
     @Inject
     DeleteOldBackupsTask(final LocLogger log,
             @BackupFolder final FileProvider<FileObject> backupFolderProvider,
             final Duration durationToKeepBackups, final Instant currentTime) {
         this.log = log;
         this.backupFolderProvider = backupFolderProvider;
         this.durationToKeepBackups = durationToKeepBackups;
         this.currentTime = currentTime;
     }
 
     @Override
     public void run() {
         try {
             deleteOldBackups();
         } catch (final FileSystemException e) {
             log.error(ErrorMessage.CANNOT_ACCESS_BACKUP, e);
         }
     }
 
     private void deleteOldBackups() throws FileSystemException {
         for (final FileObject backup : getBackupFolderContents()) {
             deleteBackupIfOld(backup);
         }
     }
 
     private FileObject[] getBackupFolderContents() throws FileSystemException {
         return backupFolderProvider.get().getChildren();
     }
 
     private void deleteBackupIfOld(final FileObject backup)
             throws FileSystemException {
         if (backupIsOld(backup)) {
             deleteBackup(backup);
         }
     }
 
     private boolean backupIsOld(final FileObject backup)
             throws FileSystemException {
         final long lastModifiedTime = backup.getContent().getLastModifiedTime();
         final Instant instant = new Instant(lastModifiedTime);
         return currentTime.plus(durationToKeepBackups).isAfter(instant);
     }
 
     private void deleteBackup(final FileObject backup)
             throws FileSystemException {
         // TODO: try to move this further up the application stack.
         backup.delete();
        log.info(LogMessage.DELETED_BACKUP, backup.getName().getBaseName());
     }
 }
