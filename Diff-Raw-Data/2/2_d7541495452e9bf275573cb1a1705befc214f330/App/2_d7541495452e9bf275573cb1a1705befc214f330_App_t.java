 package com.smartitengineering.jgit.usage;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.Properties;
 import java.util.Random;
 import java.util.Set;
 import name.fraser.neil.plaintext.diff_match_patch;
 import name.fraser.neil.plaintext.diff_match_patch.Diff;
 import org.spearce.jgit.lib.Commit;
 import org.spearce.jgit.lib.Constants;
 import org.spearce.jgit.lib.FileTreeEntry;
 import org.spearce.jgit.lib.GitIndex;
 import org.spearce.jgit.lib.ObjectId;
 import org.spearce.jgit.lib.ObjectLoader;
 import org.spearce.jgit.lib.ObjectWriter;
 import org.spearce.jgit.lib.PersonIdent;
 import org.spearce.jgit.lib.RefUpdate;
 import org.spearce.jgit.lib.Repository;
 import org.spearce.jgit.lib.Tree;
 import org.spearce.jgit.lib.TreeEntry;
 import org.spearce.jgit.revwalk.RevCommit;
 import org.spearce.jgit.revwalk.RevWalk;
 import org.spearce.jgit.treewalk.TreeWalk;
 import org.spearce.jgit.treewalk.filter.TreeFilter;
 
 /**
  * Hello world!
  *
  */
 public class App {
 
     private static final String INIT_ID = "23";
     private static final int OBJECT_COUNT = 100;
 
     public static void main(String[] args) {
         long startTime = System.currentTimeMillis();
         performVersioningExperiment(false, false, false);
         System.out.println("Duration: " + (System.currentTimeMillis() -
             startTime) + "ms");
     }
 
     private static void commitSingleBook(Tree head,
                                          String isbn,
                                          final String xmlData,
                                          ObjectWriter objectWriter,
                                          GitIndex gitIndex,
                                          Repository repository)
         throws IOException {
         FileTreeEntry treeEntry;
         byte[] dataBytes;
         if (head.existsBlob(isbn)) {
             treeEntry = (FileTreeEntry) head.findBlobMember(isbn);
             String modifiedData =
                 xmlData.replace("<name>\n\t\tTest" + "\n\t</name>",
                 "<name>\n\t\tTest Updated @ " + new Date() + "\n\t</name>");
             modifiedData = modifiedData.replace("$ID$", isbn);
             dataBytes = modifiedData.getBytes();
         }
         else {
             treeEntry = head.addFile(isbn);
             String modifiedData = xmlData.replace("$ID$", isbn);
             dataBytes = modifiedData.getBytes();
         }
         treeEntry.setExecutable(false);
         ObjectId objectId = objectWriter.writeBlob(dataBytes);
         treeEntry.setId(objectId);
         gitIndex.readTree(head);
         gitIndex.write();
         ObjectId treeId = gitIndex.writeTree();
         head.setId(treeId);
         ObjectId currentHeadId =
             repository.resolve(Constants.HEAD);
         boolean commitAvailable = true;
         if (currentHeadId != null) {
             Commit headCommit = repository.mapCommit(currentHeadId);
             if (headCommit != null) {
                 Tree headTree = headCommit.getTree();
                 if (headTree != null && head.getId().equals(headTree.getId())) {
                     commitAvailable = false;
                 }
             }
         }
         if (commitAvailable) {
             System.out.println("Commit.....");
             ObjectId[] parentIds;
             if (currentHeadId != null) {
                 parentIds = new ObjectId[]{currentHeadId};
             }
             else {
                 parentIds = new ObjectId[0];
             }
             Commit commit =
                 new Commit(repository, parentIds);
             commit.setTree(head);
             commit.setTreeId(head.getId());
             PersonIdent person = new PersonIdent(repository);
             commit.setAuthor(person);
             commit.setCommitter(person);
             commit.setMessage(isbn + " Commit message");
             ObjectId newCommitId = objectWriter.writeCommit(commit);
             commit.setCommitId(newCommitId);
             System.out.println("Commit: " + commit);
             RefUpdate refUpdate =
                 repository.updateRef(Constants.HEAD);
             refUpdate.setNewObjectId(commit.getCommitId());
             refUpdate.setRefLogMessage(commit.getMessage(), false);
             refUpdate.forceUpdate();
         }
     }
 
     private static Tree getHeadTree(Repository repository)
         throws IOException {
         Tree head = repository.mapTree(Constants.HEAD);
         if (head == null) {
             head = new Tree(repository);
         }
         return head;
     }
 
     private static void performVersioningExperiment(
         final boolean performCommit,
         final boolean performCommitWalk,
         final boolean performObjectWalk) {
 
         final String xmlData =
             "<bookElement>\n\t<name>\n\t\tTest\n\t</name>\n\t" +
             "<isbn>\n\t\t$ID$\n\t</isbn>\n\t<summary>\n\t\tSummary for test." +
             "\n\t</summary>\n</bookElement>\n";
         Repository repository = null;
 
         try {
             final File repoLocation =
                 new File(
                 "/home/imyousuf/projects/git-projs/test-repo/jgit-test/.git");
             repository = new Repository(repoLocation);
             if (!repoLocation.exists()) {
                 repository.create();
             }
             Tree head = getHeadTree(repository);
             if (performCommit) {
                 //WRITE to REPOSITORY
                 GitIndex gitIndex = repository.getIndex();
                 ObjectWriter objectWriter =
                     new ObjectWriter(repository);
                 for (int i = 0;
                     i < App.OBJECT_COUNT;
                     ++i) {
                     String isbn =
                         String.valueOf(Integer.parseInt(App.INIT_ID) + i);
                     commitSingleBook(head, isbn, xmlData, objectWriter, gitIndex,
                         repository);
                     head = getHeadTree(repository);
                 }
             }
 
             //READ from REPOSITORY
             Repository repo = repository;
             if (performCommitWalk) {
                 ObjectId commitId = repo.resolve(Constants.HEAD);
                 traverseCommit(commitId, repo);
             }
 
             //REV Walk
             if (performObjectWalk) {
                 head = getHeadTree(repository);
                 printRevisionsForObject(repo);
             }
             int rand = Math.abs(new Random().nextInt()) % App.OBJECT_COUNT;
             long startTime = System.currentTimeMillis();
             String testIsbn = String.valueOf(Integer.parseInt(INIT_ID) + rand);
             readRevisionsForObjectPath(repo, testIsbn);
             System.out.println("Duration for single object revisions for ISBN " +
                 testIsbn + ": " + (System.currentTimeMillis() - startTime) +
                 "ms");
         }
         catch (Exception ex) {
             ex.printStackTrace();
         }
         finally {
             if (repository != null) {
                 repository.close();
             }
         }
     }
 
     private static void printRevisionsForObject(Repository repo)
         throws NumberFormatException,
                IOException {
         for (int i = 0; i < App.OBJECT_COUNT;
             ++i) {
             System.out.println("INDEX: " + i);
             String isbn =
                 String.valueOf(Integer.parseInt(App.INIT_ID) + i);
             System.out.println("ISBN: " + isbn);
             readRevisionsForObjectPath(repo, isbn);
         }
     }
 
     private static void readRevisionsForObjectPath(Repository repo,
                                                    String isbn)
         throws IOException {
         final Set<ObjectId> versions = new LinkedHashSet<ObjectId>();
         final RevWalk rw = new RevWalk(repo);
         final TreeWalk tw = new TreeWalk(repo);
         rw.markStart(rw.parseCommit(repo.resolve(Constants.HEAD)));
         tw.setFilter(TreeFilter.ANY_DIFF);
         RevCommit c;
         while ((c = rw.next()) != null) {
             final ObjectId[] p = new ObjectId[c.getParentCount() + 1];
             for (int i = 0; i < c.getParentCount(); i++) {
                 rw.parse(c.getParent(i));
                 p[i] = c.getParent(i).getTree();
             }
             final int me = p.length - 1;
             p[me] = c.getTree();
             tw.reset(p);
             while (tw.next()) {
                 if (tw.getFileMode(me).getObjectType() == Constants.OBJ_BLOB) {
                     // This path was modified relative to the ancestor(s)
                     String s = tw.getPathString();
                     if (s != null && s.equals(isbn)) {
                         Set<ObjectId> i = versions;
                         i.add(tw.getObjectId(me));
                     }
                 }
                 if (tw.isSubtree()) {
                     // make sure we recurse into modified directories
                     tw.enterSubtree();
                 }
             }
         }
         System.out.println("Revisions (" + versions.size() + "): " + versions);
         ArrayList<ObjectId> versionList = new ArrayList(versions);
         Collections.reverse(versionList);
         Iterator<ObjectId> versionIterator = versionList.iterator();
         ObjectId first = versionIterator.next();
         for(int i = 0; i < versions.size() - 1; ++i) {
             final ObjectId second = versionIterator.next();
             ObjectLoader objectLoader = repo.openObject(first);
             String firstStr = new String(objectLoader.getBytes());
             objectLoader = repo.openObject(second);
             String secondStr = new String(objectLoader.getBytes());
             diff_match_patch diff = new diff_match_patch();
             LinkedList<Diff> diffs = diff.diff_main(firstStr, secondStr);
             System.out.println("-----------------------");
             final String htmlDiff = diff.diff_prettyHtml(diffs);
             Properties properties = new Properties();
             properties.load(App.class.getClassLoader().getResourceAsStream("props.properties"));
             String outputDir = properties.getProperty("write_dir");
            if(outputDir != null && !outputDir.isEmpty() && !outputDir.startsWith("${")) {
                 OutputStreamWriter writer = new FileWriter(outputDir + i + ".html");
                 writer.write(htmlDiff, 0, htmlDiff.length());
                 writer.close();
             }
             System.out.println(diff.patch_toText(diff.patch_make(diffs)));
             System.out.println("-----------------------");
             first = second;
         }
         
     }
 
     private static void traverseCommit(ObjectId commitId,
                                        Repository repo)
         throws IOException,
                IOException {
         System.out.println("Commit: " + commitId.toString());
         Commit currentCommit = repo.mapCommit(commitId);
         Tree tree = repo.mapTree(currentCommit.getTreeId());
         traverseTree(tree);
         ObjectId[] parents = currentCommit.getParentIds();
         for (ObjectId commitObjectId : parents) {
             traverseCommit(commitObjectId, repo);
         }
     }
 
     private static void traverseTree(Tree tree)
         throws IOException {
         System.out.println("Tree: " + tree.getId().toString());
         for (TreeEntry entry : tree.members()) {
             System.out.println(entry.getFullName() + " " +
                 entry.getId().toString());
             if (entry instanceof FileTreeEntry) {
                 FileTreeEntry fileTreeEntry =
                     (FileTreeEntry) entry;
                 ObjectLoader loader = fileTreeEntry.openReader();
                 System.out.println(new String(loader.getBytes()));
             }
         }
     }
 }
