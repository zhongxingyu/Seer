 package ch.unibe.scg.cc;
 
 import static com.google.common.io.BaseEncoding.base16;
 import static java.lang.annotation.ElementType.FIELD;
 import static java.lang.annotation.ElementType.METHOD;
 import static java.lang.annotation.ElementType.PARAMETER;
 import static java.lang.annotation.RetentionPolicy.RUNTIME;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 
 import java.io.IOException;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import javax.inject.Qualifier;
 
 import org.apache.hadoop.hbase.util.Bytes;
 import org.junit.Assert;
 import org.junit.Test;
 
 import ch.unibe.scg.cc.Annotations.Populator;
 import ch.unibe.scg.cc.Protos.CloneType;
 import ch.unibe.scg.cc.Protos.CodeFile;
 import ch.unibe.scg.cc.Protos.Function;
 import ch.unibe.scg.cc.Protos.GitRepo;
 import ch.unibe.scg.cc.Protos.Project;
 import ch.unibe.scg.cc.Protos.Snippet;
 import ch.unibe.scg.cc.Protos.Version;
 import ch.unibe.scg.cc.javaFrontend.JavaModule;
 import ch.unibe.scg.cells.CellsModule;
 import ch.unibe.scg.cells.InMemoryStorage;
 import ch.unibe.scg.cells.Sink;
 import ch.unibe.scg.cells.Source;
 import ch.unibe.scg.cells.TableModule.DefaultTableModule;
 
 import com.google.common.collect.Iterables;
 import com.google.common.io.ByteStreams;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.TypeLiteral;
 import com.google.protobuf.ByteString;
 
 @SuppressWarnings("javadoc")
 public final class GitPopulatorTest {
 	private static final String TESTREPO = "testrepo.zip";
 
 	@Test
 	public void testProjnameRegex() throws IOException {
 		try(GitPopulator gitWalker = new GitPopulator(null, null)) {
 			String fullPathString = "har://hdfs-haddock.unibe.ch/projects/testdata.har"
 					+ "/apfel/.git/objects/pack/pack-b017c4f4e226868d8ccf4782b53dd56b5187738f.pack";
 			String projName = gitWalker.extractProjectName(fullPathString);
 			assertThat(projName, is("apfel"));
 
 			fullPathString = "har://hdfs-haddock.unibe.ch/projects/dataset.har/dataset/sensei/objects/pack/pack-a33a3daca1573e82c6fbbc95846a47be4690bbe4.pack";
 			projName = gitWalker.extractProjectName(fullPathString);
 			assertThat(projName, is("sensei"));
 		}
 	}
 
 	@Test
 	public void testPopulate() throws IOException, InterruptedException {
 		Injector i = walkRepo(parseZippedGit(TESTREPO));
 
 		try(Source<Project> projectPartitions = i.getInstance(
 				Key.get(new TypeLiteral<Source<Project>>() {}, Populator.class));
 		Source<Str<Function>> functionStringPartitions	=
 				i.getInstance(Key.get(new TypeLiteral<Source<Str<Function>>>() {}, Populator.class));
 		Source<Snippet> snippet2FuncsPartitions =
 				i.getInstance(Key.get(new TypeLiteral<Source<Snippet>>() {}, TestSink.class));
 		Source<Version> versionPartitions = i.getInstance(
 				Key.get(new TypeLiteral<Source<Version>>() {}, Populator.class));
 		Source<CodeFile> filePartitions = i.getInstance(
 				Key.get(new TypeLiteral<Source<CodeFile>>() {}, Populator.class));
 		Source<Function> functionPartitions = i.getInstance(
 				Key.get(new TypeLiteral<Source<Function>>() {}, Populator.class));
 		Source<Snippet> snippetPartitions = i.getInstance(
 				Key.get(new TypeLiteral<Source<Snippet>>() {}, Populator.class))) {
 			assertThat(Iterables.size(projectPartitions), is(1));
 
 			Iterable<Project> projects = Iterables.getOnlyElement(projectPartitions);
 			assertThat(Iterables.size(projects), is(1));
 
 			Project p = Iterables.getOnlyElement(projects);
 			assertThat(p.getName(), is("testrepo.zip"));
 
 			assertThat(Iterables.size(versionPartitions), is(1));
 
 			Iterable<Version> versions = Iterables.getOnlyElement(versionPartitions);
 			assertThat(Iterables.size(versions), is(1));
 			Version v = Iterables.getOnlyElement(versions);
 			assertThat(v.getName(), is("master"));
 			assertThat(v.getProject(), is(p.getHash()));
 
 			assertThat(Iterables.size(filePartitions), is(1));
 
 			Iterable<CodeFile> files = Iterables.getOnlyElement(filePartitions);
 			assertThat(Iterables.size(files), is(1));
 			CodeFile cf = Iterables.getOnlyElement(files);
 			assertThat(cf.getPath(), is("GitTablePopulatorTest.java"));
 			assertThat(cf.getVersion(), is(v.getHash()));
 
 			assertThat(cf.getContents().indexOf("package ch.unibe.scg.cc.mappers;"), is(0));
 
 			assertThat(Iterables.size(functionPartitions), is(1));
 
 			Iterable<Function> functions = Iterables.getOnlyElement(functionPartitions);
 			assertThat(Iterables.size(functions), is(1));
 
 			Function fn = Iterables.getOnlyElement(functions);
 			assertThat(fn.getCodeFile(), is(cf.getHash()));
 			assertThat(fn.getBaseLine(), is(10));
 			assertThat(fn.getContents().indexOf("public void testProjname"), is(1));
 
 			assertThat(Iterables.size(snippetPartitions), is(1)); // means we have only one function
 
 			Iterable<Snippet> snippets = Iterables.getOnlyElement(snippetPartitions);
 			assertThat(Iterables.size(snippets), is(24));
 			Snippet s0 = Iterables.get(snippets, 0);
 			Snippet s1 = Iterables.get(snippets, 1);
 			Snippet s7 = Iterables.get(snippets, 7);
 
 			assertThat(s0.getFunction(), is(fn.getHash()));
 			assertThat(s0.getLength(), is(5));
 			assertThat(s0.getPosition(), is(0));
 			assertThat(s1.getPosition(), is(1));
 			assertThat(s1.getFunction(), is(fn.getHash()));
 			assertThat(s7.getPosition(), is(7));
 			assertThat(s7.getFunction(), is(fn.getHash()));
 
 			// Check FunctionString
 			Iterable<Str<Function>> functionStringRow = Iterables.getOnlyElement(functionStringPartitions);
 			Str<Function> functionString = Iterables.getOnlyElement(functionStringRow);
 			assertThat(functionString.contents.indexOf("public void testProjnameRegex"), is(1));
 

 			assertThat(Iterables.size(snippet2FuncsPartitions), is(24 - 2)); // 2 collisions
 
 			Iterable<Snippet> snippets2Funcs = Iterables.get(snippet2FuncsPartitions, 0);
 			assertThat(Iterables.size(snippets2Funcs), is(1));
 		}
 	}
 
 	@Test
 	public void testPaperExampleFile2Function() throws IOException, InterruptedException {
 		Injector i = walkRepo(GitPopulatorTest.parseZippedGit("paperExample.zip"));
 
 		try(Source<Function> decodedPartitions =
 				i.getInstance(Key.get(new TypeLiteral<Source<Function>>() {}, Populator.class))) {
 			Iterable<Function> funs = Iterables.concat(decodedPartitions);
 			assertThat(Iterables.size(funs), is(15));
 
 			Set<ByteString> funHashes = new HashSet<>();
 			for (Function fun : funs) {
 				funHashes.add(fun.getHash());
 			}
 			assertThat(funHashes.size(), is(9));
 
 			Set<String> fileHashes = new HashSet<>();
 			for (Iterable<Function> partition : decodedPartitions) {
 				String cur = base16().encode(Iterables.get(partition, 0).getCodeFile().toByteArray());
 				Assert.assertFalse(cur + fileHashes, fileHashes.contains(cur));
 				fileHashes.add(cur);
 				// Check that every partition shares the same file.
 				for (Function f : partition) {
 					assertThat(base16().encode(f.getCodeFile().toByteArray()), is(cur));
 				}
 			}
 			assertThat(fileHashes.size(), is(3));
 		}
 	}
 
 	@Test
 	public void testPaperExampleSnippet2Functions() throws IOException, InterruptedException {
 		Injector i = walkRepo(GitPopulatorTest.parseZippedGit("paperExample.zip"));
 		try(Source<Snippet> snippet2Function = i.getInstance(Key.get(new TypeLiteral<Source<Snippet>>() {}, TestSink.class))) {
 			assertThat(Iterables.size(snippet2Function), is(145));
 
 			// Numbers are taken from paper. See table II.
 			ByteString row03D8 = ByteString.copyFrom(new byte[] {0x03, (byte) 0xd8});
 			Iterable<Snippet> partition03D8 = null;
 			for(Iterable<Snippet> s2fPartition : snippet2Function) {
 				if(Bytes.startsWith(Iterables.get(s2fPartition, 0).getHash().toByteArray(), row03D8.toByteArray())) {
 					partition03D8 = s2fPartition;
 					break;
 				}
 			}
 			Assert.assertNotNull(partition03D8);
 			assertThat(Iterables.size(partition03D8), is(2));
 
 			int actualDistance = Math.abs(Iterables.get(partition03D8, 0).getPosition()
 					- Iterables.get(partition03D8, 1).getPosition());
 			assertThat(actualDistance, is(3));
 		}
 	}
 
 	@Test
 	public void testPaperExampleFunction2Snippets() throws IOException, InterruptedException {
 		Injector i = walkRepo(GitPopulatorTest.parseZippedGit("paperExample.zip"));
 
 		try(Source<Snippet> function2snippetsPartitions = i.getInstance(
 				Key.get(new TypeLiteral<Source<Snippet>>() {}, Populator.class))) {
 
 			// Num partitions is the number of functions. As per populator test, that's 9.
 			assertThat(Iterables.size(function2snippetsPartitions), is(9));
 
 			// We'll examine this row further in Function2RoughClones
 			Iterable<Snippet> aaa0 = null;
 			for (Iterable<Snippet> row : function2snippetsPartitions) {
 				if (base16().encode(Iterables.get(row, 0).getFunction().toByteArray()).startsWith("AAA0")) {
 					aaa0 = row;
 					break;
 				}
 			}
 			assert aaa0 != null; // Null analysis insists ...
 			assertNotNull(aaa0);
 
 			List<String> snippetHashes = new ArrayList<>();
 			for (Snippet s : aaa0) {
 				if (s.getCloneType() == CloneType.GAPPED) {
 					snippetHashes.add(base16().encode(s.getHash().toByteArray()));
 				}
 			}
 
 			// These snippets were only partially checked.
 			assertThat(snippetHashes, is(d618SnippetHashes()));
 		}
 	}
 
 	static Collection<String> d618SnippetHashes() {
 		return Arrays.asList("58BA4690385740B2C9F8FCBF890A1ECF3BDC17C4", "0FA256C80C3AF5E1AC1FE54F5F0AF85D8752F474",
 				"0FA256C80C3AF5E1AC1FE54F5F0AF85D8752F474", "A9BBB1B13ECC261749436CAF9DC5DC20E9C2F68B",
 				"98DB4D210584D3033A1E26785A6721C609B54D14", "BB3E14556ABA10796131584A07979C50470B4DBA",
 				"38C303121D329190DA79CE955F0E790569D168D3", "598571B72AE83C72E299F3747B9C025848C45014",
 				"301729FB42E326C3CE1130994C16BD4C9DF14A79", "A4A8B82E4ABE99EBF67D12A1FF190B61FF6E6520",
 				"5F72E12E161EF9991A85572864F5FBE6C3DF72EB", "9C628251AE1C7A39F3265D1AACA3630B69DA3655",
 				"1474378C2B8FDE56C8A835AFD8F7DFB46F1E59DC", "20A4E2A38A590E7D29978E7A3FF308EE15B6DC63",
 				"6F4533745BDB2D84648440CE9D2826DECAEA72EE", "278629562C3404A50795A832EE4E81722319D775",
 				"48C31A2277EF29216311E8FC7366A7ACE9F3A59B", "48C31A2277EF29216311E8FC7366A7ACE9F3A59B",
 				"24D6FB97266FFFCC409DD4F57CDC938EE6423C5F");
 	}
 
 	private static Injector walkRepo(GitRepo repo) throws IOException, InterruptedException {
 		Injector i = Guice.createInjector(new CCModule(new InMemoryStorage()), new JavaModule(), new CellsModule() {
 			@Override protected void configure() {
 				installTable(TestSink.class, new TypeLiteral<Snippet>() {},
 						Snippet2FunctionsCodec.class, new InMemoryStorage(), new DefaultTableModule("rofl", ByteString.EMPTY));
 			}
 		});
 
 		try (GitPopulator gitPopulator = i.getInstance(GitPopulator.class);
 				Sink<Snippet> snippetSink = i.getInstance(Key.get(new TypeLiteral<Sink<Snippet>>() {}, TestSink.class))) {
 			gitPopulator.map(repo, Arrays.asList(repo), snippetSink);
 		}
 		return i;
 	}
 
 	@Qualifier
 	@Target({ FIELD, PARAMETER, METHOD })
 	@Retention(RUNTIME)
 	private static @interface TestSink {}
 
 	static GitRepo parseZippedGit(String pathToZip) throws IOException {
 		try(ZipInputStream packFile = new ZipInputStream(GitPopulatorTest.class.getResourceAsStream(pathToZip));
 				ZipInputStream packedRefs = new ZipInputStream(GitPopulatorTest.class.getResourceAsStream(pathToZip))) {
 			for (ZipEntry entry; (entry = packFile.getNextEntry()) != null;) {
 				if (entry.getName().endsWith(".pack")) {
 					break;
 				}
 			}
 
 			for (ZipEntry entry; (entry = packedRefs.getNextEntry()) != null;) {
 				if (entry.getName().endsWith("packed-refs")) {
 					break;
 				}
 			}
 			return GitRepo.newBuilder()
 					.setProjectName(pathToZip)
 					.setPackFile(ByteString.copyFrom(ByteStreams.toByteArray(packFile)))
 					.setPackRefs(ByteString.copyFrom(ByteStreams.toByteArray(packedRefs)))
 					.build();
 		}
 	}
 }
