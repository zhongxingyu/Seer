 package au.org.intersect.samifier;
 
 import java.util.Iterator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 public class GeneInfo
 {
     private String chromosome;
     private int start;
     private String direction;
     private SortedSet<GeneSequence> locations;
 
     public GeneInfo()
     {
         locations = new TreeSet(new GeneSequenceComparator());
     }
 
     public GeneInfo(String chromosome, int start, String direction)
     {
        this(chromosome, start, direction, new TreeSet(new GeneSequenceComparator()));
     }
 
    public GeneInfo(String chromosome, int start, String direction, SortedSet<GeneSequence> locations)
     {
        setChromosome(chromosome);
        setStart(start);
        setDirection(direction);
         this.locations = locations;
     }
 
     public void setChromosome(String chromosome)
     {
         this.chromosome = chromosome;
     }
 
     public String getChromosome()
     {
         return chromosome;
     }
 
     public void setStart(int start)
     {
         this.start = start;
     }
 
     public int getStart()
     {
         return start;
     }
 
     public void setDirection(String direction)
     {
         this.direction = direction;
     }
 
     public String getDirection()
     {
         return direction;
     }
 
     public void addLocation(GeneSequence location)
     {
         locations.add(location);
     }
 
     public Iterator<GeneSequence> getLocationsIterator()
     {
         return locations.iterator();
     }
 
     public String toString()
     {
         return "chromosome: "+chromosome+", start: "+start+", direction: "+direction;
     }
 }
 
