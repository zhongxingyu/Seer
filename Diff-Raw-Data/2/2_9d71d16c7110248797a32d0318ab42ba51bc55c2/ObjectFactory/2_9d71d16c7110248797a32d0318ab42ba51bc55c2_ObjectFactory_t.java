 package mdettlaff.javagit.core;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import mdettlaff.javagit.core.GitObject.Type;
 import mdettlaff.javagit.core.Tree.Node;
 import mdettlaff.javagit.core.Tree.Node.Mode;
 
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 
 class ObjectFactory {
 
 	public GitObject create(byte[] rawObject) {
 		int firstSpaceIndex = ArrayUtils.indexOf(rawObject, (byte) ' ');
 		int firstNullByteIndex = ArrayUtils.indexOf(rawObject, (byte) 0);
 		String typeLiteral = new String(rawObject, 0, firstSpaceIndex);
 		int sizeLength = firstNullByteIndex - (firstSpaceIndex + 1);
 		int size = Integer.valueOf(new String(rawObject, firstSpaceIndex + 1, sizeLength));
 		byte[] content = Arrays.copyOfRange(rawObject, firstNullByteIndex + 1, rawObject.length);
 		verifySize(content, size);
 		Type type = Type.getByLiteral(typeLiteral);
 		ObjectContent objectContent = createContent(type, content);
 		return new GitObject(type, size, objectContent);
 	}
 
 	private ObjectContent createContent(Type type, byte[] content) {
 		switch (type) {
 		case BLOB:
 			return createBlob(content);
 		case TREE:
 			return createTree(content);
 		case COMMIT:
 			return createCommit(content);
 		case TAG:
 			return createTag(content);
 		default:
 			throw new IllegalArgumentException("Unknown object type: " + type);
 		}
 	}
 
 	private Blob createBlob(byte[] content) {
 		return new Blob(content);
 	}
 
 	private Tree createTree(byte[] content) {
 		List<Node> nodes = new ArrayList<>();
 		int startIndex = 0;
 		for (int i = 0; i < content.length; i++) {
 			if (content[i] == (byte) 0) {
 				int endIndex = i + 21;
 				i = endIndex;
 				byte[] subarray = ArrayUtils.subarray(content, startIndex, endIndex);
 				Node node = createNode(subarray);
 				nodes.add(node);
 				startIndex = endIndex;
 			}
 		}
 		return new Tree(ImmutableList.copyOf(nodes));
 	}
 
 	private Node createNode(byte[] content) {
 		int firstSpaceIndex = ArrayUtils.indexOf(content, (byte) ' ');
 		int firstNullByteIndex = ArrayUtils.indexOf(content, (byte) 0);
 		String fileModeLiteral = new String(content, 0, firstSpaceIndex);
 		Mode mode = Mode.getByLiteral(fileModeLiteral);
 		int pathLength = firstNullByteIndex - (firstSpaceIndex + 1);
 		String path = new String(content, firstSpaceIndex + 1, pathLength);
 		byte[] id = Arrays.copyOfRange(content, firstNullByteIndex + 1, content.length);
 		ObjectId value = new ObjectId(id);
 		return new Node(mode, value, path);
 	}
 
 	private Commit createCommit(byte[] content) {
 		CommitBuilder commit = new CommitBuilder();
 		String[] lines = new String(content, ByteArrayBuilder.ENCODING).split("\n");
 		for (int i = 0; i < lines.length; i++) {
 			String line = lines[i];
 			if (line.startsWith("tree")) {
 				commit.tree(new ObjectId(line.substring(5)));
 			}
 			if (line.startsWith("parent")) {
 				commit.addParent(new ObjectId(line.substring(7)));
 			}
 			if (line.startsWith("author")) {
 				commit.author(createCreator(line.substring(7)));
 			}
 			if (line.startsWith("committer")) {
 				commit.committer(createCreator(line.substring(10)));
 			}
 			if (line.isEmpty()) {
 				String[] messageLines = ArrayUtils.subarray(lines, i + 1, lines.length);
				commit.message(StringUtils.join(messageLines, '\n'));
 				break;
 			}
 		}
 		return commit.build();
 	}
 
 	private Tag createTag(byte[] content) {
 		TagBuilder tag = new TagBuilder();
 		String[] lines = new String(content, ByteArrayBuilder.ENCODING).split("\n");
 		for (int i = 0; i < lines.length; i++) {
 			String line = lines[i];
 			if (line.startsWith("object")) {
 				tag.object(new ObjectId(line.substring(7)));
 			}
 			if (line.startsWith("type")) {
 				tag.type(Type.getByLiteral(line.substring(5)));
 			}
 			if (line.startsWith("tag ")) {
 				tag.tag(line.substring(4));
 			}
 			if (line.startsWith("tagger")) {
 				tag.tagger(createCreator(line.substring(7)));
 			}
 			if (line.isEmpty()) {
 				String[] messageLines = ArrayUtils.subarray(lines, i + 1, lines.length);
 				tag.message(StringUtils.join(messageLines));
 				break;
 			}
 		}
 		return tag.build();
 	}
 
 	private Creator createCreator(String content) {
 		Pattern pattern = Pattern.compile("(.*) <(.*)> ([0-9]+) ([+-]?[0-9]+)");
 		Matcher matcher = pattern.matcher(content);
 		Preconditions.checkArgument(matcher.matches(), "Invalid creator content: " + content);
 		String name = matcher.group(1);
 		String email = matcher.group(2);
 		long instant = Long.valueOf(matcher.group(3)).longValue() * 1000;
 		String timezone = matcher.group(4);
 		DateTime date = new DateTime(instant).withZone(DateTimeZone.forID(timezone));
 		return new Creator(name, email, date, timezone);
 	}
 
 	private void verifySize(byte[] content, int size) {
 		Preconditions.checkState(content.length == size, "Invalid content size: " + size);
 	}
 
 	private static class CommitBuilder {
 
 		private ObjectId tree;
 		private List<ObjectId> parents;
 		private Creator author;
 		private Creator committer;
 		private String message;
 
 		public CommitBuilder() {
 			parents = new ArrayList<>();
 		}
 
 		public void tree(ObjectId tree) {
 			this.tree = tree;
 		}
 
 		public void addParent(ObjectId parent) {
 			parents.add(parent);
 		}
 
 		public void author(Creator author) {
 			this.author = author;
 		}
 
 		public void committer(Creator committer) {
 			this.committer = committer;
 		}
 
 		public void message(String message) {
 			this.message = message;
 		}
 
 		public Commit build() {
 			return new Commit(tree, ImmutableList.copyOf(parents), author, committer, message);
 		}
 	}
 
 	private static class TagBuilder {
 
 		private ObjectId object;
 		private Type type;
 		private String tag;
 		private Creator tagger;
 		private String message;
 
 		public void object(ObjectId object) {
 			this.object = object;
 		}
 
 		public void type(Type type) {
 			this.type = type;
 		}
 
 		public void tag(String tag) {
 			this.tag = tag;
 		}
 
 		public void tagger(Creator tagger) {
 			this.tagger = tagger;
 		}
 
 		public void message(String message) {
 			this.message = message;
 		}
 
 		public Tag build() {
 			return new Tag(object, type, tag, tagger, message);
 		}
 	}
 }
