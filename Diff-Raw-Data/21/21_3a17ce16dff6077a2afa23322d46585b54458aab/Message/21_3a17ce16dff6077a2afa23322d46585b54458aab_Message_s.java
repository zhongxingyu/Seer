 package comm;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.List;
 import java.util.Scanner;
 
 /**
  * A message representing a serializable remote-procedure-call with a name,
  * integer id, and a variable list of values.
  */
 public class Message {
 	private static int computeChecksum(String body) {
 		int res = 0;
 		for (int i = 0; i < body.length(); i++) {
 			res += body.charAt(i);
 		}
 		return res;
 	}
 
 	/**
 	 * Attempts to construct a new Message object from the given InputStream.
 	 * 
 	 * This is a blocking call and will wait on input from the stream.
 	 * 
 	 * Note that calling deserialize() with a stream containing data resulting
 	 * from calling serialize() on a Message must return a Message object which
 	 * is equal to the original.
 	 * 
 	 * @param in
 	 *            An input stream from which to attempt to read a message
 	 * @return A message, if one could be successfully read from the given
 	 *         string. Null otherwise.
 	 */
	public static Message deserialize(InputStream in) {
 		List<String> values = new ArrayList<String>();
 
 		Scanner msgScanner = new Scanner(in);
 		msgScanner.useDelimiter("\\{|\\}");
		String inner = msgScanner.next();
 
 		String[] innerSplit = inner.split("\\|");
 		int checksum = Integer.parseInt(innerSplit[0]);
 		assert (checksum == computeChecksum(innerSplit[1]));
 
 		String[] body = innerSplit[1].split(":");
 		String name = body[0];
 		int id = Integer.parseInt(body[1]);
 
 		String[] v = new String[0];
 		if (body.length >= 3) {
 			v = body[2].split(",");
 			for (int i = 0; i < v.length; i++) {
 				values.add(v[i]);
 			}
 		}
 
 		return new Message(id, name, values);
 	}
 
 	private int id;
 
 	private String name;
 
 	private List<String> values;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param id
 	 *            The integer id of the message
 	 * @param name
 	 *            The name of the message
 	 * @param numParameters
 	 *            The total number of parameters that this message will have
 	 */
 	public Message(int id, String name, int numParameters) {
 		this.id = id;
 		this.name = name;
 		this.values = new ArrayList<String>(numParameters);
 		for (int i = 0; i < numParameters; i++) {
 			this.values.add("");
 		}
 	}
 
 	/**
 	 * Private constructor to create a Message object with known values
 	 * 
 	 * @param id
 	 * @param name
 	 * @param values
 	 */
 	private Message(int id, String name, List<String> values) {
 		this.id = id;
 		this.name = name;
 		this.values = values;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Message other = (Message) obj;
 		if (id != other.id)
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (values == null) {
 			if (other.values != null)
 				return false;
 		} else if (!values.equals(other.values))
 			return false;
 		return true;
 	}
 
 	/**
 	 * Returns the parameter at the specified index
 	 * 
 	 * @param index
 	 *            The index of the parameter to return
 	 * @return The value of the boolean parameter at the specified index. Note
 	 *         that an exception may be thrown if the parameter at this index is
 	 *         not a boolean value
 	 */
 	public boolean getBooleanParameter(int index) {
 		return Boolean.parseBoolean(values.get(index));
 	}
 
 	/**
 	 * Returns the parameter at the specified index
 	 * 
 	 * @param index
 	 *            The index of the parameter to return
 	 * @return The value of the float parameter at the specified index. Note
 	 *         that an exception may be thrown if the parameter at this index is
 	 *         not a float value
 	 */
 	public float getFloatParameter(int index) {
 		return Float.parseFloat(values.get(index));
 	}
 
 	/**
 	 * @return The id number of the message
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * Returns the parameter at the specified index
 	 * 
 	 * @param index
 	 *            The index of the parameter to return
 	 * @return The value of the long parameter at the specified index. Note that
 	 *         an exception may be thrown if the parameter at this index is not
 	 *         a long value
 	 */
 	public long getLongParameter(int index) {
 		return Long.parseLong(values.get(index));
 	}
 
 	/**
 	 * @return The name of the message
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param index
 	 *            The index of the parameter to return
 	 * @return The value of the string parameter at the specified index
 	 */
 	public String getStringParameter(int index) {
 		return values.get(index);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + id;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((values == null) ? 0 : values.hashCode());
 		return result;
 	}
 
 	/**
 	 * Writes the message to the output stream
 	 * 
 	 * @param out
 	 *            The output stream with which to write the message
 	 * @throws IOException
 	 *             If there is an error writing the message to the output stream
 	 */
 	public void serialize(OutputStream out) throws IOException {
 		StringBuilder v = new StringBuilder();
 		for (String s : values) {
 			v.append(s);
 			v.append(",");
 		}
 		String body = String.format("%s:%d:%s", name, id, v.toString());
 		int checksum = computeChecksum(body);
 		String msg = String.format("{%d|%s}", checksum, body);
 		for (int i = 0; i < msg.length(); i++) {
 			byte b = (byte) msg.charAt(i);
 			out.write(b);
 		}
 	}
 
 	/**
 	 * Sets the parameter value at the specified index
 	 * 
 	 * @param index
 	 *            The index of the parameter to modify
 	 * @param value
 	 *            The new value for this parameter
 	 */
 	public void setBoolParameter(int index, boolean value) {
 		values.set(index, Boolean.toString(value));
 	}
 
 	/**
 	 * Sets the parameter value at the specified index
 	 * 
 	 * @param index
 	 *            The index of the parameter to modify
 	 * @param value
 	 *            The new value for this parameter
 	 */
 	public void setFloatParameter(int index, float value) {
 		values.set(index, Float.toString(value));
 	}
 
 	/**
 	 * Sets the parameter value at the specified index
 	 * 
 	 * @param index
 	 *            The index of the parameter to modify
 	 * @param value
 	 *            The new value for this parameter
 	 */
 	public void setLongParameter(int index, long value) {
 		values.set(index, Long.toString(value));
 	}
 
 	/**
 	 * Sets the parameter value at the specified index
 	 * 
 	 * @param index
 	 *            The index of the parameter to modify
 	 * @param value
 	 *            The new value for this parameter
 	 */
 	public void setStringParameter(int index, String value) {
 		values.set(index, value);
 	}
 
 	@Override
 	public String toString() {
 		return "Message [id=" + id + ", name=" + name + ", values=" + values
 				+ "]";
 	}
 }
