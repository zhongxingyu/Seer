 package es.uah.cc.ie.metadatastatistics;
 
 import java.util.Iterator;
 import java.util.List;
 
 class ResourceIterator implements Iterator<Resource> {
     
     private List source;
     private MetadataParser parser;
     private int position = 0;
     
     public ResourceIterator(List source, MetadataParser parser) {
         this.source = source;
         this.parser = parser;
         this.position = 0;
     }
 
     public boolean hasNext() {
        return this.position < (this.source.size());
     }
 
     public Resource next() {
         int pos = this.position;
         this.position ++;
         return parser.parse(source.get(pos));
     }
 
     public void remove() { }
     
 }
