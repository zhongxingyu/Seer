 package ch.unibe.scg.cc.mappers;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.errors.MissingObjectException;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.ObjectLoader;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevTree;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.eclipse.jgit.storage.dfs.DfsObjDatabase;
 import org.eclipse.jgit.storage.dfs.DfsRepositoryDescription;
 import org.eclipse.jgit.storage.dfs.InMemoryRepository;
 import org.eclipse.jgit.transport.PackParser;
 import org.eclipse.jgit.treewalk.TreeWalk;
 
 import ch.unibe.scg.cc.Frontend;
 import ch.unibe.scg.cc.Java;
 import ch.unibe.scg.cc.activerecord.CodeFile;
 import ch.unibe.scg.cc.activerecord.Project;
 import ch.unibe.scg.cc.activerecord.RealProjectFactory;
 import ch.unibe.scg.cc.activerecord.RealVersionFactory;
 import ch.unibe.scg.cc.activerecord.Version;
 import ch.unibe.scg.cc.git.PackedRef;
 import ch.unibe.scg.cc.git.PackedRefParser;
 import ch.unibe.scg.cc.mappers.TablePopulator.CharsetDetector;
 import ch.unibe.scg.cc.mappers.inputformats.GitInputFormat;
 import ch.unibe.scg.cc.util.WrappedRuntimeException;
 
 public class GitTablePopulator implements Runnable {
 
 	final HbaseWrapper hbaseWrapper;
 	
 	@Inject
 	GitTablePopulator(HbaseWrapper hbaseWrapper) {
 		this.hbaseWrapper = hbaseWrapper;
 	}
 	
 	public void run() {
 		try {
 			Job job = hbaseWrapper.createMapJob("gitPopulate", GitTablePopulator.class, "GitTablePopulatorMapper", Text.class, IntWritable.class);
			job.setInputFormatClass(GitInputFormat.class);
 			job.setOutputFormatClass(NullOutputFormat.class);
			FileInputFormat.addInputPath(job, new Path("/project-clone-detector/projects"));
 //			job.getConfiguration().setInt("mapred.map.tasks", 1); //XXX
 //			job.getConfiguration().setInt("mapreduce.job.maps", 1); //XXX
 			System.out.println("yyy wait for completion");
 			job.waitForCompletion(true);
 		} catch (IOException e) {
 			throw new WrappedRuntimeException(e);
 		} catch (InterruptedException e) {
 			throw new WrappedRuntimeException(e);
 		} catch (ClassNotFoundException e) {
 			throw new WrappedRuntimeException(e);
 		}
 	}
 	
 	public static class GitTablePopulatorMapper extends GuiceMapper<Text, BytesWritable, Text, IntWritable> {
 		@Inject
 		GitTablePopulatorMapper(@Java Frontend javaFrontend, @Named("versions") HTable versions,
 				@Named("files") HTable files,  @Named("functions") HTable functions, @Named("facts") HTable facts,
 				@Named("strings") HTable strings, @Named("hashfactContent") HTable hashfactContent,  
 				RealProjectFactory projectFactory,
 				RealVersionFactory versionFactory,
 				CharsetDetector charsetDetector) {
 			super();
 			this.javaFrontend = javaFrontend;
 			this.versions = versions;
 			this.files = files;
 			this.functions = functions;
 			this.facts = facts;
 			this.strings = strings;
 			this.hashfactContent = hashfactContent;
 			this.projectFactory = projectFactory;
 			this.versionFactory = versionFactory;
 			this.charsetDetector = charsetDetector;
 		}
 
 		final Frontend javaFrontend;
 		final HTable versions, files, functions, facts, strings, hashfactContent;
 		final RealProjectFactory projectFactory;
 		final RealVersionFactory versionFactory;
 		final CharsetDetector charsetDetector;
 		
 		public void map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException {
 			String packFilePath = key.toString();
 			System.out.println("yyy RECEIVED: " + packFilePath);
 			InputStream packFileStream = new ByteArrayInputStream(value.getBytes());
 			DfsRepositoryDescription desc = new DfsRepositoryDescription(packFilePath);
 			InMemoryRepository r = new InMemoryRepository(desc);
 
 			Configuration conf = new Configuration();
 			conf.addResource(new Path("/etc/hadoop/conf/core-site.xml"));
 			FileSystem fileSystem = FileSystem.get(conf);
 			
 			PackParser pp = r.newObjectInserter().newPackParser(packFileStream);
 			pp.parse(null);
 
 			Git git = Git.wrap(r);
 			DfsObjDatabase db = r.getObjectDatabase();
 
 			RevWalk revWalk = new RevWalk(r);
 			
 			PackedRefParser prp = new PackedRefParser();
 			Pattern pattern = Pattern.compile("(.+)objects/pack/pack-[a-f0-9]{40}.pack");
 			Matcher matcher = pattern.matcher(key.toString());
 			if(!matcher.matches())
 				throw new RuntimeException("Something seems to be wrong with this input path: " + key.toString());
 			String gitDirPath = matcher.group(1);
 			String packedRefsPath = gitDirPath + Constants.PACKED_REFS;
 			
 			FSDataInputStream ins = fileSystem.open(new Path(packedRefsPath));
 			List<PackedRef> pr = prp.parse(ins);
 			
 			String projectName = packFilePath; // XXX
 			System.out.println("PROCESSING: " + packFilePath);
 			for(PackedRef paref : pr) {
 				String tag = paref.getName();
 				System.out.println("WALK TAG: " + tag);
 
 				revWalk.dispose();
 				RevCommit commit = revWalk.parseCommit(paref.getKey());
 				RevTree tree = commit.getTree();
 				TreeWalk treeWalk = new TreeWalk(r);
 				treeWalk.addTree(tree);
 				treeWalk.setRecursive(true);
 				if (!treeWalk.next()) {
 					return;
 				}
 				while (treeWalk.next()) {
 					ObjectId objectId = treeWalk.getObjectId(0);
 					try {
 //						ObjectLoader loader = r.open(objectId);
 //						System.out.println("twn " + Constants.typeString(loader.getType())
 //							+ ": " + objectId + " " + treeWalk.getPathString());
 						String content = getContent(r, objectId);
 						String filePath = treeWalk.getPathString();
 						if(!filePath.endsWith(".java"))
 							continue;
 //						System.out.println("twn filepath: " + filePath);
 						String fileName = filePath.lastIndexOf('/') == -1 ? filePath : filePath.substring(filePath.lastIndexOf('/')+1);
						CodeFile codeFile = register(content, fileName);
 //						System.out.println("svd contents: " + fileName);
						Version version = register(filePath, codeFile);
 //						System.out.println("svd codefile: " + filePath);
						register(projectName, version, tag);
 //						System.out.println("svd version: " + tag);
 					} catch (MissingObjectException moe) {
 						System.out.println("twn MissingObjectException: " + moe);
 					}
 				}
 			}
 			System.out.println("svd FINISHED PROCESSING " + packFilePath);
 		}
 		
 		private String getContent(Repository repository, ObjectId objectId) throws MissingObjectException, IOException {
 			ObjectLoader loader = repository.open(objectId);
 			InputStream inputStream = loader.openStream();
 			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
 			StringBuilder stringBuilder = new StringBuilder();
 			String line = null;
 			while ((line = bufferedReader.readLine()) != null) {
 				stringBuilder.append(line + "\n");
 			}
 			bufferedReader.close();
 			return stringBuilder.toString();
 		}
 		
 		private CodeFile register(String content, String fileName) throws IOException {
 			CodeFile codeFile = javaFrontend.register(content, fileName);
 	        functions.flushCommits();
 	        facts.flushCommits();
 	        hashfactContent.flushCommits();
 	        return codeFile;
 		}
 
 		private Version register(String filePath, CodeFile codeFile) throws IOException {
 			Version version = versionFactory.create(filePath, codeFile);
 			javaFrontend.register(version);
 	        files.flushCommits();
 	        return version;
 		}
 
 		private void register(String projectName, Version version, String tag) throws IOException {
 			Project proj = projectFactory.create(projectName, version, tag);
 			javaFrontend.register(proj);
 	        versions.flushCommits();
 	        strings.flushCommits();
 		}
 	}
 }
