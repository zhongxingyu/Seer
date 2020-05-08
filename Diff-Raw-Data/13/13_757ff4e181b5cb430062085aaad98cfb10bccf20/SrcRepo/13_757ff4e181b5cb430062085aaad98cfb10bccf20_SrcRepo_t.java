 package com.tms.threed.threedFramework.repo.server;
 
 import com.google.common.base.Preconditions;
 import com.google.common.io.InputSupplier;
 import com.tms.threed.threedFramework.repo.shared.CommitId;
 import com.tms.threed.threedFramework.repo.shared.FullSha;
 import com.tms.threed.threedFramework.repo.shared.RevisionParameter;
 import com.tms.threed.threedFramework.repo.shared.RootTreeId;
 import com.tms.threed.threedFramework.repo.shared.TagCommit;
 import com.tms.threed.threedFramework.threedCore.shared.SeriesKey;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.jgit.JGitText;
 import org.eclipse.jgit.api.AddCommand;
 import org.eclipse.jgit.api.CommitCommand;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.TagCommand;
 import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
 import org.eclipse.jgit.api.errors.JGitInternalException;
 import org.eclipse.jgit.api.errors.NoHeadException;
 import org.eclipse.jgit.lib.AnyObjectId;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.ObjectLoader;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.lib.RefUpdate;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevObject;
 import org.eclipse.jgit.revwalk.RevTag;
 import org.eclipse.jgit.revwalk.RevTree;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.eclipse.jgit.storage.file.FileBasedConfig;
 import org.eclipse.jgit.storage.file.FileRepository;
 
 import javax.annotation.Nonnull;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
 public class SrcRepo {
 
     private final File srcRepoDir;
     private final SeriesKey seriesKey;
 
     private final FileRepository repo;
 
     public SrcRepo(File srcRepoDir, SeriesKey seriesKey) {
 
         assert srcRepoDir != null;
         assert seriesKey != null;
 
         this.srcRepoDir = srcRepoDir;
         this.seriesKey = seriesKey;
 
 
         try {
             repo = new FileRepository(this.srcRepoDir);
 
         } catch (Exception e) {
             throw new RepoException("Problems reading srcRepoDir [" + srcRepoDir + "]", e);
         }
 
         if (!srcRepoDir.exists()) {
             try {
                 repo.create();
             } catch (IOException e) {
                 throw new RepoException("Problems initializing new srcRepoDir [" + srcRepoDir + "]", e);
             }
         }
 
         initConfig();
 
 
     }
 
     private void initConfig() {
         FileBasedConfig config = repo.getConfig();
         config.setInt("core", null, "compression", 0);
         try {
             config.save();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
 
     public String getTagFromCommitId(CommitId commitId) {
         ObjectId objectId = ObjectId.fromString(commitId.getName());
         return getTagFromCommitId(objectId);
     }
 
     public String getTagFromCommitId(ObjectId commitId) {
         Map<String, Ref> tags = getTags();
         for (Map.Entry<String, Ref> e : tags.entrySet()) {
             String shortTagName = e.getKey();
             Ref ref = e.getValue();
             ObjectId objectId = ref.getObjectId();
 
             if (objectId.equals(commitId)) {
                 return shortTagName;
             }
 
         }
 
         return null;
     }
 
     private ObjectId resolveGitObjectId(RevisionParameter revisionParameter) throws UnableToResolveRevisionParameterException {
         ObjectId oid;
         try {
             oid = repo.resolve(revisionParameter.stringValue());
         } catch (Exception e) {
             throw new UnableToResolveRevisionParameterException("Error resolving revisionParameter to an ObjectId", e);
         }
 
         if (oid == null)
             throw new UnableToResolveRevisionParameterException("Unable to resolve revisionParameter, [" + revisionParameter + "], to an ObjectId", null);
 
         return oid;
     }
 
     public ObjectId resolve(RevisionParameter revisionParameter) throws UnableToResolveRevisionParameterException {
         return resolveGitObjectId(revisionParameter);
     }
 
     public ObjectId resolveCommitHead() throws UnableToResolveRevisionParameterException {
         return resolve(RevisionParameter.HEAD_REVISION_PARAMETER);
     }
 
     public RootTreeId resolveHeadRootTreeId() {
         ObjectId commitId = resolveCommitHead();
 
         RevCommit revCommit = getRevCommitEager(commitId);
 
         RevTree tree = revCommit.getTree();
         return new RootTreeId(tree.getName());
     }
 
     @Nonnull
     public ObjectLoader getRepoObject(ObjectId objectId) throws UnableToResolveRevisionParameterException {
 
         try {
             ObjectLoader loader = repo.open(objectId);
             if (loader == null) throw new UnableToResolveRevisionParameterException("ObjectLoader was null", null);
             return loader;
         } catch (Exception e) {
             throw new UnableToResolveRevisionParameterException("Error opening ObjectLoader", e);
         }
     }
 
     public byte[] getRepoObjectAsBytes(ObjectId objectId) throws UnableToResolveRevisionParameterException {
         ObjectLoader loader = getRepoObject(objectId);
         byte[] bytes = loader.getBytes();
         return bytes;
     }
 
     public void close() {
         repo.close();
     }
 
     /**
      * @return null nothing if nothing has been committed
      */
     public List<TagCommit> getTagCommits() {
         ArrayList<TagCommit> a = new ArrayList<TagCommit>();
 
         Map<String, Ref> tags = getTags();
 
         boolean isHeadTagged = false;
 
         ObjectId headId;
         try {
             headId = repo.resolve(Constants.HEAD);
             if (headId == null) return null;
         } catch (IOException e) {
             log.error("Problem resolving HEAD commit for repo[" + seriesKey + "]");
             throw new RuntimeException(e);
         }
 
         for (String shortTagName : tags.keySet()) {
             Ref ref = tags.get(shortTagName);
             Ref leaf = ref.getLeaf();
             ObjectId commitId = leaf.getObjectId();
             boolean head = headId.equals(commitId);
             if (head) {
                 isHeadTagged = true;
             }
 
             try {
                 int type = repo.open(commitId).getType();
                 if(type != Constants.OBJ_COMMIT) continue;
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
 
 
 
             RevCommit revCommit = getRevCommitEager(commitId);
             RevTree revTree = revCommit.getTree();
 
             String treeId = revTree.getName();
 
             TagCommit tagCommit = new TagCommit(shortTagName, commitId.name(), treeId, head);
             a.add(tagCommit);
         }
 
         if (!isHeadTagged) {
             RevCommit revCommit = getRevCommitEager(headId);
             RevTree revTree = revCommit.getTree();
             TagCommit h = new TagCommit("HEAD", headId.name(), revTree.getName(), true);
             a.add(h);
         }
 
 
         Collections.sort(a, new Comparator<TagCommit>() {
             @Override
             public int compare(TagCommit t1, TagCommit t2) {
 
                 String n1 = t1.getTagShortName();
                 String n2 = t2.getTagShortName();
 
                 if (n1.equalsIgnoreCase("HEAD")) n1 = "__HEAD";
                 if (n2.equalsIgnoreCase("HEAD")) n2 = "__HEAD";
 
                 return n1.compareTo(n2);
             }
         });
 
 
         return a;
 
     }
 
 
     public Map<String, Ref> getTags() {
         return repo.getTags();
     }
 
     public File getDir() {
         return srcRepoDir;
     }
 
     public FileRepository getGitRepo() {
         return repo;
     }
 
 //    private RevCommit getRevCommit(CommitId commitId) {
 //        ObjectLoader loader = this.getRepoObject(commitId);
 //        return RevCommit.parse(loader.getBytes());
 //    }
 
 //    public RevCommit getRevCommitFromCommitId(CommitId commitId) {
 //        ObjectId oid = RevCommit.fromString(commitId.stringValue());
 //        ObjectLoader loader;
 //        try {
 //            loader = repo.open(oid);
 //        } catch (IOException e) {
 //            throw new RuntimeException(e);
 //        }
 //
 //        return RevCommit.parse(loader.getBytes());
 //    }
 
 //    public RevCommit getRevCommit(ObjectId commitId) {
 //        RevWalk revWalk = new RevWalk(repo);
 //        return revWalk.lookupCommit(commitId);
 //    }
 
     public RevCommit getRevCommit(AnyObjectId commitId) {
         RevWalk revWalk = new RevWalk(repo);
 
         RevCommit revCommit = revWalk.lookupCommit(commitId);
 
         revWalk.release();
 
         return revCommit;
     }
 
     public RevCommit getRevCommitEager(AnyObjectId commitId) {
         try {
             RevWalk walk = new RevWalk(repo);
 
 
             walk.markStart(walk.lookupCommit(commitId));
 
             RevCommit revCommit = walk.next();
 
             walk.release();
 
             return revCommit;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
 
     }
 
 
 //    public RevCommit getRevCommitFromCommitId(ObjectId commitId) {
 //        ObjectLoader loader;
 //        try {
 //            loader = repo.open(commitId);
 //        } catch (IOException e) {
 //            throw new RuntimeException(e);
 //        }
 //
 //        System.out.println("AA commitId = " + commitId);
 //        byte[] bytes = loader.getBytes();
 //        return RevCommit.parse(bytes);
 //    }
 
 
 
     public Ref getRef(String name) {
         try {
             return repo.getRef(Constants.MASTER);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static void validateFullSha(String fullSha) {
         Preconditions.checkNotNull(fullSha, "Invalid fullSha[" + fullSha + "]. fullSha must be non-null");
         Preconditions.checkArgument(fullSha.length() == 40, "Invalid fullSha[" + fullSha + "]. fullSha must be 40 characters long");
         Preconditions.checkArgument(RevCommit.isId(fullSha));
     }
 
 //    public void tagCurrentVersion(String tagName) throws NoHeadException, ConcurrentRefUpdateException, InvalidTagNameException {
 //        Git git = new Git(repo);
 //
 //
 //        TagCommand tag = git.tag();
 //        TagCommand tagCommand = tag.setName(tagName);
 //        tagCommand.call();
 //    }
 
     public void tagCurrentVersion(String tagShortName) {
         RevWalk revWalk = null;
         try {
 
             //grab commitId of HEAD
             ObjectId objectId = repo.resolve(Constants.HEAD + "^{commit}");
             if (objectId == null) {
                 throw new NoHeadException(JGitText.get().tagOnRepoWithoutHEADCurrentlyNotSupported);
             }
 
 
             revWalk = new RevWalk(repo);
 
             String refName = Constants.R_TAGS + tagShortName;
             RefUpdate tagRef = repo.updateRef(refName);
             tagRef.setNewObjectId(objectId);
             tagRef.setForceUpdate(false);
             tagRef.setRefLogMessage("tagged " + tagShortName, false);
             RefUpdate.Result updateResult = tagRef.update(revWalk);
             switch (updateResult) {
                 case NEW:
                 case FORCED:
                     return;
                 case LOCK_FAILURE:
                     throw new ConcurrentRefUpdateException(
                             JGitText.get().couldNotLockHEAD,
                             tagRef.getRef(), updateResult);
                 default:
                     throw new JGitInternalException(MessageFormat.format(
                             JGitText.get().updatingRefFailed, refName,
                             tagShortName, updateResult));
             }
 
         } catch (Exception e) {
             throw new RuntimeException(e);
         } finally {
             if (revWalk != null) {
                 revWalk.release();
             }
         }
 
 
     }
 
 
 //    class SeriesCommitBuilder {
 //
 //        private final RevCommit revCommit;
 //        private final Map<String, Ref> tags;
 //
 //        public SeriesCommitBuilder(RevCommit revCommit) {
 //            this.revCommit = revCommit;
 //            tags = getTags();
 //        }
 //
 //        public SeriesCommit buildSeriesCommit() {
 //            SeriesId seriesId = new SeriesId(seriesKey, revCommit.getId().getName());
 //            String shortMessage = getShortMessage();
 //            String fullMessage = getFullMessage();
 //            SeriesCommit.Committer author = getAuthor();
 //
 //            String commitId = seriesId.getCommitId();
 //
 //            Ref ref = tags.get(commitId);
 //
 //
 //            SeriesCommit seriesCommit = new SeriesCommit(seriesId, shortMessage, fullMessage, author, revCommit.getCommitTime(), getParentCommits());
 //            return seriesCommit;
 //        }
 //
 //        private String getShortMessage() {
 //            try {
 //                return revCommit.getShortMessage();
 //            } catch (NullPointerException e) {
 //                return null;
 //            }
 //        }
 //
 //
 //        private String getFullMessage() {
 //            try {
 //                return revCommit.getFullMessage();
 //            } catch (NullPointerException e) {
 //                return null;
 //            }
 //        }
 //
 //        private SeriesCommit.Committer getAuthor() {
 //            try {
 //                PersonIdent authorIdent = revCommit.getAuthorIdent();
 //                return new SeriesCommit.Committer(authorIdent.getEmailAddress());
 //            } catch (NullPointerException e) {
 //                return null;
 //            }
 //        }
 //
 //        private CommitId[] getParentCommits() {
 //            RevCommit[] rParents = revCommit.getParents();
 //            CommitId[] parentCommits;
 //            if (rParents == null || rParents.length == 0) {
 //                parentCommits = new CommitId[0];
 //            } else {
 //                parentCommits = new CommitId[rParents.length];
 //                for (int i = 0; i < rParents.length; i++) {
 //                    RevCommit rParent = revCommit.getParent(i);
 //                    assert rParent != null;
 //                    parentCommits[i] = new CommitId(revCommit);
 //                }
 //            }
 //            return parentCommits;
 //        }
 //
 //    }
 
 //    public SeriesCommit getHeadSeriesCommit() {
 //        CommitId commitId = resolveCommitHead();
 //        return getSeriesCommit(commitId);
 //    }
 //
 //    public SeriesCommit getSeriesCommit(ObjectId commitId) {
 //        RevCommit revCommit = getRevCommit(commitId);
 //        return getCommitFromRevCommit(revCommit);
 //    }
 //
 //    public SeriesCommit getSeriesCommit(CommitId commitId) {
 //        RevCommit revCommit = getRevCommit(commitId);
 //        return getCommitFromRevCommit(revCommit);
 //    }
 
 //    public SeriesCommit getCommitFromRevCommit(RevCommit revCommit) {
 //        Preconditions.checkNotNull(revCommit);
 //        SeriesCommitBuilder builder = new SeriesCommitBuilder(revCommit);
 //        return builder.buildSeriesCommit();
 //    }
 
 
 //    public TagCommit getTagCommit(ObjectId commitId) {
 //        String tag = getTagFromCommitId(commitId);
 //        if (tag == null) throw new IllegalArgumentException("This commitId is not tagged[" + commitId + "]");
 //        boolean h = isHead(commitId);
 //        return new TagCommit(tag, commitId.name(), h);
 //    }
 //
 //    public void test1() throws Exception {
 //
 //    }
 
     private boolean isHead(ObjectId commitId) {
         if (commitId == null) return false;
         ObjectId objectId = resolveCommitHead();
         if (objectId == null) return false;
         return commitId.equals(objectId);
     }
 
     public void addAll() throws Exception {
         log.info("Adding files to the repository");
         Git git = new Git(repo);
         AddCommand addCommand = git.add().addFilepattern(".");
         addCommand.call();
         log.info("Add complete");
     }
 
    public RevCommit commitAll(String commitMessage) throws Exception {
         log.info("Committing to the repository");
         Git git = new Git(repo);

         CommitCommand commit = git.commit()
                .setMessage(commitMessage)
                .setAll(true);

         RevCommit revCommit = commit.call();
         log.info("Commit complete");
         return revCommit;
     }
 
     public void tag(String newTagName, RevObject commitId) throws Exception {
         log.info("Adding files to the repository");
         Git git = new Git(repo);
 
 
         log.info("tagging with edition number");
         TagCommand tag = git.tag()
                 .setName(newTagName)
                 .setObjectId(commitId)
                 .setMessage("Created tag for " + newTagName + ". head version is " + commitId.name());
 
         RevTag tagCall = tag.call();
         log.info("Tag added: " + tagCall.getFullMessage());
 
 
     }
 
     public RevCommit addAllAndCommit(String commitMessage) throws Exception {
         addAll();
        return commitAll(commitMessage);
     }
 
     public RevCommit addAllCommitAndTag(String commitMessage, String newTagName) throws Exception {
         Preconditions.checkNotNull(newTagName);
         RevCommit revCommit = addAllAndCommit(commitMessage);
         tag(newTagName, revCommit);
         return revCommit;
     }
 
 
     private static Log log = LogFactory.getLog(SrcRepo.class);
 
     public ObjectId toGitObjectId(FullSha objectId) {
         return ObjectId.fromString(objectId.getName());
     }
 
     public ObjectId toGitObjectId(String fullSha) {
         return ObjectId.fromString(fullSha);
     }
 
 
     public InputSupplier<? extends InputStream> getInputSupplier(final ObjectId objectId) {
         return new InputSupplier<InputStream>() {
             @Override public InputStream getInput() throws IOException {
                 ObjectLoader loader = getRepoObject(objectId);
                 return loader.openStream();
             }
         };
 
 
     }
 }
