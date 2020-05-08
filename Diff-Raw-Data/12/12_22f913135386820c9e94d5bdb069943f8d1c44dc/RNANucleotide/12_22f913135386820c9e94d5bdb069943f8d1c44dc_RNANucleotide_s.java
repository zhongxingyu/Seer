 package uk.ac.ic.doc.protein_factory;
 
 import java.util.Random;
 
 import uk.ac.ic.doc.protein_factory.Game.State;
 
 public class RNANucleotide extends Nucleotide {
    //private final static String TAG = RNANucleotide.class.getSimpleName();
     private boolean touched = false;
     public RNANucleotide(Game g) {
 		this(g, randomType(g.getGen()));
 	}
     
     public RNANucleotide(Game g, char type)
     {
         super(g, type);
        this.x = g.getGen().nextInt(800);
        this.y = g.getGen().nextInt(480);
         setColour("grey");
     }
     
     protected String partial_bitmap_filename() { return "_"; }
 	
 	public void wobbleLeft() {
 		if(attached) {			Game.State state;
 			x--;
 		}
 		else {
 			x += game.gen.nextInt(3) - 2; // Bias to move left
 			y += game.gen.nextInt(3) - 1;
 		}
 	}
 
 	public boolean touched() { return touched; }
 	public void setTouched(boolean touched) { this.touched = touched; }
 	
 	public void attach(DNANucleotide dna)
     {
 		this.attached = true;
 		dna.attach(this);
 		
 		// If codon isn't complete yet, set our own colours
 		if(!dna.computeCodonValidity()) {
 	        if(dna.matchesPartner()) dna.setState(State.Good);
 	        else dna.setState(State.Bad);
 		}
 
 
 		
 		
 		// Update score
 	}
 	
     private static char randomType(Random gen)
     {
         switch (gen.nextInt(4))
         {
         case 0:
             return 'A';
         case 1:
             return 'C';
         case 2:
             return 'G';
         default:
             return 'U';
         }
     }
 }
