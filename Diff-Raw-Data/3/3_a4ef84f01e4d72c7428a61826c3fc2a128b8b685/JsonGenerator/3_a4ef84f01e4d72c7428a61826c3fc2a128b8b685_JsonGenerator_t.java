 package eu.stratosphere.sopremo.io;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.IdentityHashMap;
 import java.util.Map;
 
import eu.stratosphere.sopremo.type.IArrayNode;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.sopremo.type.IObjectNode;
 import eu.stratosphere.sopremo.type.IStreamNode;
 import eu.stratosphere.sopremo.type.TextNode;
 
 /**
  * Writes the string-representation of {@link IJsonNode}s to a specified sink.
  */
 public class JsonGenerator {
 
 	BufferedWriter writer;
 
 	boolean isFirst = true;
 
 	/**
 	 * Initializes a JsonGenerator which uses the given {@link OutputStream} as
 	 * a sink.
 	 * 
 	 * @param stream
 	 *        the stream that should be used as a sink
 	 */
 	public JsonGenerator(final OutputStream stream) {
 		this.writer = new BufferedWriter(new OutputStreamWriter(stream));
 	}
 
 	/**
 	 * Initializes a JsonGenerator which uses the given {@link Writer} as a
 	 * sink.
 	 * 
 	 * @param writer
 	 *        the writer that should be used as a sink
 	 */
 	public JsonGenerator(final Writer writer) {
 		this.writer = new BufferedWriter(writer);
 	}
 
 	/**
 	 * Initializes a JsonGenerator which uses the given {@link File} as a sink.
 	 * 
 	 * @param file
 	 *        the file that should be used as a sink
 	 * @throws IOException
 	 */
 	public JsonGenerator(final File file) throws IOException {
 		this.writer = new BufferedWriter(new FileWriter(file));
 	}
 
 	/**
 	 * Closes the connection to the specified sink.
 	 * 
 	 * @throws IOException
 	 */
 	public void close() throws IOException {
 		this.writer.close();
 	}
 
 	/**
 	 * Writes the given {@link IJsonNode} to the specified sink. The
 	 * string-representations of multiple invocations are separated by a comma.
 	 * 
 	 * @param iJsonNode
 	 *        the node that should be written to the sink
 	 * @throws IOException
 	 */
 	public void writeTree(final IJsonNode iJsonNode) throws IOException {
 		if (iJsonNode != null) {
 			if (!this.isFirst) {
 				this.writer.write(",\n");
 			}
 			JsonTypeWriter<IJsonNode> typeWriter = JsonTypeWriterPool.getJsonTypeWriterFor(iJsonNode);
 			typeWriter.write(iJsonNode, this.writer);
 			this.isFirst = false;
 		}
 	}
 
 	/**
 	 * Delegetes the flush operation to the underlying writer
 	 * 
 	 * @throws IOException
 	 */
 	public void flush() throws IOException {
 		this.writer.flush();
 	}
 
 	/**
 	 * Writes the end-array-token to the specified sink. The token is specified
 	 * in {@link JsonToken#END_ARRAY}.
 	 * 
 	 * @throws IOException
 	 */
 	public void writeEndArray() throws IOException {
 		JsonToken.END_ARRAY.write(this.writer);
 		this.writer.flush();
 
 	}
 
 	/**
 	 * Writes the start-array-token to the specified sink. The token is
 	 * specified in {@link JsonToken#START_ARRAY}.
 	 * 
 	 * @throws IOException
 	 */
 	public void writeStartArray() throws IOException {
 		JsonToken.START_ARRAY.write(this.writer);
 		this.writer.flush();
 	}
 
 	/**
 	 * This interface describes the general behavior of JsonTypeWriters.
 	 * 
 	 * @param <T>
 	 *        A JsonTypeWriter should only be used with types implementing
 	 *        IJsonNode
 	 */
 	private static interface JsonTypeWriter<T extends IJsonNode> {
 		/**
 		 * This method takes a IJsonNode and a writer and let's the writer write the node in a type-specific way.
 		 * 
 		 * @param node
 		 *        The node you want to write.
 		 * @param writer
 		 *        The writer you want to write in.
 		 * @throws IOException
 		 */
 		public void write(T node, Writer writer) throws IOException;
 	}
 
 	/**
 	 * This class implements the JSON-Serialization for TextNodes
 	 * 
 	 * @param <T>
 	 */
 	private static class TextNodeTypeWriter implements JsonTypeWriter<TextNode> {
 
 		private static TextNodeTypeWriter Instance = new TextNodeTypeWriter();
 
 		@Override
 		public void write(TextNode node, Writer writer) throws IOException {
 			writer.append('\"');
 			final CharSequence textValue = node;
 			for (int index = 0, count = textValue.length(); index < count; index++) {
 				final char ch = textValue.charAt(index);
 				if (ch == '"')
 					writer.append("\\\"");
 				else
 					writer.append(ch);
 			}
 			writer.append('\"');
 		}
 	}
 
 	/**
 	 * This class implements the JSON-Serialization for TextNodes
 	 * 
 	 * @param <T>
 	 */
 	private static class ArrayNodeTypeWriter implements JsonTypeWriter<IStreamNode<?>> {
 
 		private static ArrayNodeTypeWriter Instance = new ArrayNodeTypeWriter();
 
 		@Override
 		public void write(IStreamNode<?> node, Writer writer) throws IOException {
 			writer.append('[');
 
 			boolean first = true;
 			for (IJsonNode elem : node) {
 				if (first)
 					first = false;
 				else
 					writer.append(',');
 
 				JsonTypeWriterPool.getJsonTypeWriterFor(elem).write(elem, writer);
 			}
 
 			writer.append(']');
 		}
 	}
 
 	/**
 	 * This class implements the JSON-Serialization for ObjectNodes
 	 * 
 	 * @param <T>
 	 */
 	private static class ObjectNodeTypeWriter implements JsonTypeWriter<IObjectNode> {
 
 		private static ObjectNodeTypeWriter Instance = new ObjectNodeTypeWriter();
 
 		@Override
 		public void write(IObjectNode node, Writer writer) throws IOException {
 			writer.append('{');
 
 			boolean first = true;
 			for (final Map.Entry<String, IJsonNode> en : node) {
 				if (first)
 					first = false;
 				else
 					writer.append(',');
 
 				writer.append('\"').append(en.getKey()).append("\":");
 				JsonTypeWriterPool.getJsonTypeWriterFor(en.getValue()).write(en.getValue(), writer);
 			}
 
 			writer.append('}');
 		}
 	}
 
 	/**
 	 * This class implements the JSON-Serialization for all IJsonNodes without
 	 * an explicit TypeWriter
 	 * 
 	 * @param <T>
 	 */
 	private static class GenericNodeTypeWriter<T extends IJsonNode> implements JsonTypeWriter<IJsonNode> {
 
 		private static GenericNodeTypeWriter<IJsonNode> Instance = new GenericNodeTypeWriter<IJsonNode>();
 
 		@Override
 		public void write(IJsonNode node, Writer writer) throws IOException {
 			node.appendAsString(writer);
 		}
 	}
 
 	/**
 	 * This class holds an Enum-Map with the JSON-node-types and their
 	 * corresponding TypeWriters. It therefore provides TypeWriters for concrete
 	 * IJsonNodes, and you can ask it to return one.
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private static class JsonTypeWriterPool {
 		private static Map<Class<? extends IJsonNode>, JsonTypeWriter<IJsonNode>> writerMap;
 
 		static {
 			writerMap = new IdentityHashMap<Class<? extends IJsonNode>, JsonGenerator.JsonTypeWriter<IJsonNode>>();
 			writerMap.put(TextNode.class, (JsonTypeWriter) TextNodeTypeWriter.Instance);
 			writerMap.put(IObjectNode.class, (JsonTypeWriter) ObjectNodeTypeWriter.Instance);
 			writerMap.put(IStreamNode.class, (JsonTypeWriter) ArrayNodeTypeWriter.Instance);
			writerMap.put(IArrayNode.class, (JsonTypeWriter) ArrayNodeTypeWriter.Instance);
 		}
 
 		/**
 		 * @param aJsonNode
 		 *        The JSON-node you want to have a writer for
 		 * @return The desired TypeWriter for your IJsonNode. A {@link GenericNodeTypeWriter} is return for all types
 		 *         without
 		 *         a specifically defined writer.
 		 */
 		public static JsonTypeWriter<IJsonNode> getJsonTypeWriterFor(IJsonNode aJsonNode) {
 			JsonTypeWriter<IJsonNode> writerToReturn = writerMap.get(aJsonNode.getType());
 			if (writerToReturn == null)
 				writerToReturn = GenericNodeTypeWriter.Instance;
 			return writerToReturn;
 		}
 	}
 }
