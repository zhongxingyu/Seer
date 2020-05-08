 package genotype;
 
public class Genome {
 
 	final static public byte	X = 1,
 								Y = 2,
 								UNDEF = -1;
 	enum GenomeName{
 		R, L;
 	}
 	
 	byte gender;
 	GenomeName name;
 	
 	public Genome(byte gender, GenomeName name){
 		this.gender = gender;
 		this.name = name;
 	}
 
 	public byte getGender() {
 		return gender;
 	}
 
 	public GenomeName getName() {
 		return name;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == this)
 			return true;
 		if (getClass() != obj.getClass())
 			return false;
 		return gender == ((Genome)obj).gender
 			&& name == ((Genome)obj).name;
 	}
 }
