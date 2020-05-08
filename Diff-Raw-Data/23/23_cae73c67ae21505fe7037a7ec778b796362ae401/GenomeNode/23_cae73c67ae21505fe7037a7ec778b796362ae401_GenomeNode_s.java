 package extended;
 
 import com.sun.jna.*;
 import core.Range;
 
 abstract class GenomeNode
 {
  protected Pointer genome_node_ptr;
 
   public interface GT extends Library
   {
     GT INSTANCE = (GT) Native.loadLibrary("genometools", GT.class);
 
     int gt_genome_node_accept(Pointer gn, Pointer gv, Pointer err);
     Pointer gt_genome_node_ref(Pointer gn);
     NativeLong gt_genome_node_get_start(Pointer gn);
     NativeLong gt_genome_node_get_end(Pointer gn);
     String gt_genome_node_get_filename(Pointer gn);
     void gt_genome_node_delete(Pointer gn);
   }
 
   public GenomeNode() {
   }
   
   public GenomeNode(Pointer node_ptr) 
   {
     genome_node_ptr = GT.INSTANCE.gt_genome_node_ref(node_ptr);
   }
   
   protected void finalize()
   {
     GT.INSTANCE.gt_genome_node_delete(this.genome_node_ptr);
   }
   
   public Range get_range()
   {
     NativeLong nstart = GT.INSTANCE
         .gt_genome_node_get_start(this.genome_node_ptr);
     NativeLong nend = GT.INSTANCE.gt_genome_node_get_end(this.genome_node_ptr);
     Range ran = new Range((int)nstart.longValue(), (int)nend.longValue());
     return ran;
   }
   
   String get_filename()
   {
     return GT.INSTANCE.gt_genome_node_get_filename(this.genome_node_ptr);
   }
   
  /* public void accept(Pointer visitor) throws GTerror
   {
     GTerror err = new GTerror();
     int rval = GT.INSTANCE.gt_genome_node_accept(this.genome_node_ptr, visitor,
         err.to_ptr());
     if (rval != 0) {
       throw new GTerror(err.get_err(), err.to_ptr());
     }
   } */
   
   public int hashCode() {
     return this.genome_node_ptr.hashCode();    
   }
   
   public boolean equals(Object obj) {
     if(this == obj)
       return true;
     if((obj == null) || (obj.getClass() != this.getClass()))
       return false;
     GenomeNode gn = (GenomeNode) obj;
     return (this.to_ptr().equals(gn.to_ptr()));
   }
   
   public Pointer to_ptr() {
     return genome_node_ptr;
   }  
 }
