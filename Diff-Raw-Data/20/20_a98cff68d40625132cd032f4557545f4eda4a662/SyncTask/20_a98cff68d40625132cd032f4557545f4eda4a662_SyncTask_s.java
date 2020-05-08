 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.com.danielferber.gittocc.sync;
 
 import br.com.danielferber.gittocc.cc.ClearToolCommander;
 import br.com.danielferber.gittocc.git.GitCommander;
 import br.com.danielferber.gittocc.git.GitTreeDiff;
 import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.Meter;
 import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.MeterFactory;
 import com.ibm.icu.text.MessageFormat;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Scanner;
 import java.util.TreeSet;
 import java.util.concurrent.Callable;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 /**
  *
  * @author X7WS
  */
 public class SyncTask implements Callable<Void> {
 
     final GitCommander gitCommander;
     final ClearToolCommander ctCommander;
     final File vobDir;
     final File gitDir;
     final File syncFromCommitFile;
     final File syncCounterFile;
     boolean fetchFromRemote;
     boolean fastForward;
     boolean createActivity;
     String headline = null;
     long syncCounter;
 
     public SyncTask(GitCommander gitCommander, ClearToolCommander ctCommander, File gitDir, File vobDir, File syncFromCommitFile, File syncCounterFile) {
         this.gitCommander = gitCommander;
         this.ctCommander = ctCommander;
         this.gitDir = gitDir;
         this.vobDir = vobDir;
         this.syncCounterFile = syncCounterFile;
         this.syncFromCommitFile = syncFromCommitFile;
     }
 
     public void setFetchFromRemote(boolean fetchFromRemote) {
         this.fetchFromRemote = fetchFromRemote;
     }
 
     public void setFastForward(boolean fastForward) {
         this.fastForward = fastForward;
     }
 
     public void setHeadline(String headline) {
         this.headline = headline;
     }
 
     public void setCreateActivity(boolean createActivity) {
         this.createActivity = createActivity;
     }
 
     public void setSyncCounter(long value) {
         this.syncCounter = value;
     }
 
     public Void call() throws Exception {
 
         Meter m = MeterFactory.getMeter("SyncTask").m("Sincronizar do Git para o ClearCase.").start();
         try {
             String fromCommit = readSyncCommit();
 
             GitTreeDiff diff = buildGitDiff(fromCommit);
 
             if (diff.hasStuff()) {
                 applyDiff(diff);
             }
 
             m.ok();
 
             return null;
         } catch (Exception e) {
             m.fail(e);
             throw e;
         }
     }
 
     private GitTreeDiff buildGitDiff(String fromCommit) throws Exception {
         Meter m = MeterFactory.getMeter("GitDiff").m("Criar histórico Git.").ctx("fromCommit", fromCommit).start();
         Meter m2 = null;
 
         try {
 
             if (fetchFromRemote) {
                 m2 = m.sub("fetch").m("Obter commits remotos.").start();
                 gitCommander.fetch();
                 m2.ok();
             }
 
             if (fastForward) {
                 m2 = m.sub("fastForward").m("Avançar commits.").start();
                 gitCommander.fastForward();
                 m2.ok();
             }
 
             m2 = m.sub("currentCommit").m("Ler commit atual.").start();
             String gitCommit = gitCommander.getCurrentCommit();
             m2.ok("currentCommit", gitCommit);
 
             m2 = m.sub("report").m("Gerar relatório de commits.").ctx("fromCommit", fromCommit).ctx("toCommit", gitCommit).start();
             String report = gitCommander.commitMessagesReport(fromCommit, gitCommit, "%an (%ad):%n%s%n");
             m2.ok();
 
             m2 = m.sub("changeSet").m("Listar modificações.").ctx("fromCommit", fromCommit).ctx("toCommit", gitCommit).start();
             GitTreeDiff diff = gitCommander.treeDif(fromCommit, gitCommit);
             m2.ok();
 
             m.ok();
 
             return diff;
         } catch (Exception e) {
             if (m2 != null) {
                 m2.fail(e);
             }
             m.fail(e);
             throw e;
         }
     }
 
     private static Collection<File> roots(List<File> files) {
         TreeSet<File> roots = new TreeSet<File>();
 
         for (File file : files) {
             File parent = file.getParentFile();
             if (parent == null) {
                 parent = new File(".");
             }
             if (!files.contains(parent)) {
                 roots.add(file);
             }
         }
 
         return roots;
     }
 
     private static TreeSet<File> leafes(List<File> dirs, List<File> files) {
         TreeSet<File> leafes = new TreeSet<File>();
 
         for (File file : files) {
             File parentDir = file.getParentFile();
             if (parentDir == null) {
                 parentDir = new File(".");
             }
             if (!dirs.contains(parentDir)) {
                 leafes.add(file);
             }
         }
         return leafes;
     }
 
     private static Collection<File> parentDirs(Collection<File> files) {
         TreeSet<File> dirs = new TreeSet<File>();
         for (File file : files) {
             File parentFile = file.getParentFile();
             if (parentFile == null) {
                 parentFile = new File(".");
             }
             dirs.add(parentFile);
         }
         return dirs;
     }
 
     private void copyFilesFromGit(final List<File> files) throws IOException {
         for (File file : files) {
             File gitSourceFile = new File(gitDir, file.getPath());
             File ccTargetFile = new File(vobDir, file.getPath());
             FileUtils.copyFile(gitSourceFile, ccTargetFile);
         }
     }
 
     private void applyDiff(GitTreeDiff diff) throws Exception {
         Meter m = MeterFactory.getMeter("VobUpdate").m("Atualizar VOB.").start();
         Meter m2 = null;
 
         try {
             if (createActivity) {
                 HashMap<String, Object> map = new HashMap<String, Object>();
                 map.put("gitCommitTo", diff.toCommit);
                 map.put("gitCommitFrom", diff.fromCommit);
                 map.put("syncDate", new Date());
                 map.put("syncCount", syncCounter);
                 String headlineStr = MessageFormat.format(headline, map);
 
                 m2 = m.sub("createActivity").m("Criar atividade.").ctx("headline", headlineStr).start();
                 ctCommander.createActivity(headlineStr);
                 m2.ok();
             }
 
             m2 = m.sub("checkout.commitFile").m("Bloquear arquivo de controle.").start();
             ctCommander.checkoutFile(syncFromCommitFile);
             ctCommander.checkoutFile(syncCounterFile);
             m2.ok();
 
             if (!diff.dirsAdded.isEmpty()) {
 
                 m2 = m.sub("checkout.roots.dirsAdded").m("Checkout de raizes com diretórios novos.").start();
                 Collection<File> dirs = parentDirs(roots(diff.dirsAdded));
                 ctCommander.checkoutDirs(dirs);
                 m2.ok();
 
                 m2 = m.sub("make.dirsAdded").m("Criar novos diretórios.").start();
                 ctCommander.makeDirs(diff.dirsAdded);
                 m2.ok();
             }
 
             if (!diff.filesAdded.isEmpty()) {
                 m2 = m.sub("checkout.parent.filesAdded").m("Checkout de diretórios com arquivos novos.").start();
                 ctCommander.checkoutDirs(parentDirs(diff.filesAdded));
                 m2.ok();
 
                 m2 = m.sub("make.filesAdded").m("Criar arquivos novos.").start();
                 ctCommander.makeFiles(diff.filesAdded);
                 m2.ok();
 
                m2 = m.sub("write.filesAdded").m("Escrever arquivos novos.").start();
                 copyFilesFromGit(diff.filesAdded);
                 m2.ok();
             }
 
             if (!diff.filesCopiedTo.isEmpty()) {
                 m2 = m.sub("checkout.parent.filesCopiedTo").m("Checkout de diretórios com arquivos copiados.").start();
                 ctCommander.checkoutDirs(parentDirs(diff.filesCopiedTo));
                 m2.ok();
 
                 m2 = m.sub("make.filesCopiedTo").m("Criar arquivos copiados.").start();
                 ctCommander.makeFiles(diff.filesCopiedTo);
                 m2.ok();
 
                m2 = m.sub("write.filesCopiedTo").m("Escrever arquivos copiados.").start();
                copyFilesFromGit(diff.filesCopiedTo);
                m2.ok();
             }
 
             if (!diff.filesMovedFrom.isEmpty()) {
                 m2 = m.sub("checkout.parent.filesMovedFrom").m("Checkout de diretórios origem com arquivos movidos.").start();
                 ctCommander.checkoutDirs(parentDirs(diff.filesMovedFrom));
                 m2.ok();
 
                 m2 = m.sub("checkout.parent.filesMovedTo").m("Checkout de diretórios destino com arquivos movidos.").start();
                 ctCommander.checkoutDirs(parentDirs(diff.filesMovedTo));
                 m2.ok();
 
                m2 = m.sub("move.filesMoved").m("Mover arquivos.").start();
                 Iterator<File> sourceIterator = diff.filesMovedFrom.iterator();
                 Iterator<File> targetIterator = diff.filesMovedTo.iterator();
                 while (sourceIterator.hasNext()) {
                     File source = sourceIterator.next();
                     File target = targetIterator.next();
                     ctCommander.moveFile(source, target);
                 }
 
                 if (!diff.filesMovedModified.isEmpty()) {
                     m2 = m.sub("checkout.filesMovedModified").m("Checkout de arquivos movidos e modificados.").start();
                    ctCommander.checkoutFiles(parentDirs(diff.filesMovedModified));
                     m2.ok();
 
                    m2 = m.sub("write.filesMovedModified").m("Escrever arquivos movidos e modificados.").start();
                     copyFilesFromGit(diff.filesMovedModified);
                     m2.ok();
                 }
             }
 
             if (!diff.filesModified.isEmpty()) {
                 m2 = m.sub("checkout.filesModified").m("Checkout de arquivos modificados.").start();
                 ctCommander.checkoutFiles(diff.filesModified);
                 m2.ok();
 
                 m2 = m.sub("write.filesModified").m("Modificar arquivos.").start();
                 copyFilesFromGit(diff.filesModified);
                 m2.ok();
             }
 
             if (!diff.dirsDeleted.isEmpty()) {
                 final Collection<File> dirsToDelete = roots(diff.dirsDeleted);
 
                 if (!dirsToDelete.isEmpty()) {
                     m2 = m.sub("checkout.roots.dirsDeleted").m("Checkout de raizes de diretórios removidos.").start();
                     ctCommander.checkoutDirs(parentDirs(dirsToDelete));
                     m2.ok();
 
                     m2 = m.sub("delete.dirs").m("Apagar diretórios.").start();
                     ctCommander.removeDirs(dirsToDelete);
                     m2.ok();
                 }
             }
 
             if (!diff.filesDeleted.isEmpty()) {
                 TreeSet<File> filesToDelete = leafes(diff.dirsDeleted, diff.filesDeleted);
 
                 if (!filesToDelete.isEmpty()) {
                     m2 = m.sub("delete.parent.filesDeleted").m("Checkout de diretórios com arquivos removidos.").start();
                     ctCommander.checkoutDirs(parentDirs(filesToDelete));
                     m2.ok();
 
                     m2 = m.sub("delete.files").m("Apagar arquivos.").start();
                     ctCommander.removeFiles(filesToDelete);
                     m2.ok();
                 }
             }
 
 
             m2 = m.sub("write.commitFile").m("Atualizar arquivo de controle.").start();
             writeSyncCommit(diff.toCommit);
             writeSyncCounter(syncCounter);
             m2.ok();
 
             if (ctCommander.requireCheckinDirs()) {
                 m2 = m.sub("checkin.dirs").m("Checkin de diretórios.").start();
                 ctCommander.checkinDirs();
                 m2.ok();
             }
 
             if (ctCommander.requireCheckinFiles()) {
                 m2 = m.sub("checkin.files").m("Checkin de arquivos.").start();
                 ctCommander.checkinFiles();
                 m2.ok();
             }
 
         } catch (Exception e) {
             if (m2 != null) {
                 m2.fail(e);
             }
             throw e;
         }
     }
 
     private String readSyncCommit() throws FileNotFoundException {
         Scanner scanner = null;
         try {
             scanner = new Scanner(this.syncFromCommitFile);
             return scanner.next();
         } finally {
             if (scanner != null) {
                 scanner.close();
             }
         }
     }
 
     private void writeSyncCommit(String commit) throws IOException {
         FileWriter writer = null;
         try {
             writer = new FileWriter(this.syncFromCommitFile);
             writer.write(commit + "\n");
         } finally {
             if (writer != null) {
                 IOUtils.closeQuietly(writer);
             }
         }
     }
 
     private void writeSyncCounter(long value) throws IOException {
         FileWriter writer = null;
         try {
             writer = new FileWriter(this.syncCounterFile);
             writer.write(value + "\n");
         } finally {
             if (writer != null) {
                 IOUtils.closeQuietly(writer);
             }
         }
     }
 }
