 package minespy.nbt;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
import nbt.Tag;

 public class TagCompound extends Tag {
 
 	public static final byte TAG_ID = 10;
 	public static final String TYPENAME = "TAG_Compound";
 
 	public static class Parser extends Tag.Parser {
 
 		public Parser() {
 			super(TAG_ID, TYPENAME);
 		}
 
 		@Override
 		protected Tag parsePayload(String name, DataInput in) throws IOException {
 			TagCompound ret = new TagCompound(name);
 			Tag t = null;
 			while ((t = Tag.parse(in)) != null) {
 				ret.add(t);
 			}
 			return ret;
 		}
 
 		@Override
 		public Class<?> payloadClass() {
 			return null;
 		}
 
 	}
 
 	private Map<String, Tag> m_data;
 
 	public TagCompound(String name_) {
 		super(TAG_ID, name_);
 		m_data = new HashMap<String, Tag>();
 	}
 
 	@Override
 	public int size() {
 		return m_data.size();
 	}
 
 	@Override
 	public Tag child(String tagname) {
 		Tag tag = m_data.get(tagname);
 		if (tag == null) throw new NoSuchElementException("Bad tagname: " + tagname);
 		return tag;
 	}
 
 	@Override
 	public <T> T get() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public <T> void set(T t) {
 		if (t == null) throw new NullPointerException();
 		try {
 			Iterable<?> l = (Iterable<?>) t;
 			// check tag types
 			for (Object o : l) {
 				Tag.class.cast(o);
 			}
 			// copy tags
 			m_data.clear();
 			for (Object o : l) {
 				Tag tag = (Tag) o;
 				m_data.put(tag.name(), tag);
 			}
 		} catch (ClassCastException e) {
 			throw new IllegalArgumentException(e);
 		}
 	}
 
 	@Override
 	public <T> void set(String tagname, T t) {
 		if (tagname == null) throw new NullPointerException();
 		try {
 			Tag tag = child(tagname);
 			tag.set(t);
 		} catch (NoSuchElementException e) {
 			// child by that name doesn't exist
 			add(Tag.wrap(tagname, t));
 		}
 	}
 
 	@Override
 	public void add(Tag tag) {
 		if (tag == null) throw new NullPointerException();
 		// don't allow overwriting already-existing tag of same name
 		if (m_data.containsKey(tag.name())) throw new IllegalArgumentException();
 		m_data.put(tag.name(), tag);
 	}
 
 	@Override
 	public Tag remove(String tagname) {
 		if (tagname == null) throw new NullPointerException();
 		return m_data.remove(tagname);
 	}
 	
 	@Override
 	public Tag replace(String tagname, Tag tag) {
 		if (tagname == null || tag == null) throw new NullPointerException();
 		if (tagname.equals(tag.name())) {
 			// replace tag of same name or just add
 			return m_data.put(tag.name(), tag);
 		} else if (m_data.containsKey(tag.name())) {
 			// cant add, tag by that name already exists
 			throw new IllegalArgumentException();
 		} else {
 			m_data.put(tag.name(), tag);
 			return m_data.remove(tagname);
 		}
 	}
 
 	@Override
 	public void clear() {
 		m_data.clear();
 	}
 
 	@Override
 	public Iterator<Tag> iterator() {
 		return m_data.values().iterator();
 	}
 
 	@Override
 	protected void writePayload(DataOutput out) throws IOException {
 		for (Tag t : m_data.values()) {
 			t.write(out);
 		}
 		out.writeByte(0);
 	}
 
 	@Override
 	public String toString() {
 		return super.toString() + " size=" + m_data.size();
 	}
 
 	@Override
 	public TagCompound clone() {
 		TagCompound t = (TagCompound) super.clone();
 		t.m_data = new HashMap<String, Tag>();
 		for (Tag tag : this.m_data.values()) {
 			t.m_data.put(tag.name(), tag.clone());
 		}
 		return t;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((m_data == null) ? 0 : m_data.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) return true;
 		if (!super.equals(obj)) return false;
 		if (getClass() != obj.getClass()) return false;
 		TagCompound other = (TagCompound) obj;
 		if (m_data == null) {
 			if (other.m_data != null) return false;
 		} else if (!m_data.equals(other.m_data)) return false;
 		return true;
 	}
 
 }
