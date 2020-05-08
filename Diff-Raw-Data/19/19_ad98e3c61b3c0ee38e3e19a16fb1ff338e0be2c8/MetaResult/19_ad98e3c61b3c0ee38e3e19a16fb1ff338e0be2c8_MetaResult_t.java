 package org.dada.core;
 
 import java.io.Serializable;
 import java.util.Collection;
 
 import clojure.lang.Indexed;
 
 public class MetaResult  implements Serializable, Indexed {
 
 	private final Metadata<?, ?> metadata;
 	private final String prefix;
 	private final Collection<Collection<Object>> pairs;
 	private final Collection<Object> operation;
        private final  Metadata<?, ?> childMetadata;
 	
        public MetaResult(Metadata<?, ?> metadata, String prefix, Collection<Collection<Object>> pairs, Collection<Object> operation, Metadata<?, ?> childMetadata) {
 		super();
 		this.metadata = metadata;
 		this.prefix = prefix;
 		this.pairs = pairs;
 		this.operation = operation;
		this.childMetadata = childMetadata;
 		//System.out.println("METARESULT: "+prefix+", "+pairs+", "+operation);
 	}
 
	public Metadata<?, ?> getMetadata() {
 		return metadata;
 	}
 
 	public String getPrefix() {
 		return prefix;
 	}
 
 	public Collection<Collection<Object>> getPairs() {
 		return pairs;
 	}
 
 	public Collection<Object> getOperation() {
 		return operation;
 	}
 
	public Metadata<?, ?> getChildMetadata() {
		return childMetadata;
	}

 	// Indexed
 	
 	@Override
 	public int count() {
		return 5;
 	}
 
 	@Override
 	public Object nth(int i) {
 		return nth(i, null);
 	}
 
 	@Override
 	public Object nth(int i, Object notFound) {
 		switch (i) {
 		case 0: return metadata;
 		case 1: return prefix;
 		case 2: return pairs;
 		case 3: return operation;
		case 4: return childMetadata;
 		default: return notFound;
 		}
 	}
 
 }
