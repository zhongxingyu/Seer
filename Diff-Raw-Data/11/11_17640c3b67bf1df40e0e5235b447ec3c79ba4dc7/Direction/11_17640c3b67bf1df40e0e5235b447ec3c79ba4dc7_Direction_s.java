 package mouse.dbTableModels;
 
 
 public class Direction {
 	
 	private final Antenna in;
 	
 	private final Antenna out;
 	
 	private Directions type;
 
 	public Direction(Antenna in, Antenna out) {
 		super();
 		
 		this.in = in;
 		this.out = out;
 		
 		init();
 	}
 
 	public Antenna getIn() {
 		return in;
 	}
 
 	public Antenna getOut() {
 		return out;
 	}
 	
 	public Directions getType() {
 		return type;
 	}
 
 	@Override
 	public String toString() {
 		return type.toString();
 	}
 	
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((in == null) ? 0 : in.hashCode());
 		result = prime * result + ((out == null) ? 0 : out.hashCode());
 		result = prime * result + ((type == null) ? 0 : type.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Direction other = (Direction) obj;
 		if (in == null) {
 			if (other.in != null)
 				return false;
 		} else if (!in.equals(other.in))
 			return false;
 		if (out == null) {
 			if (other.out != null)
 				return false;
 		} else if (!out.equals(other.out))
 			return false;
 		if (type != other.type)
 			return false;
 		return true;
 	}
 
 	private void init() {
 		int inPos = Integer.parseInt(in.getPosition());
 		int outPos = Integer.parseInt(out.getPosition());
 		if (inPos == outPos) {
 			type = Directions.Undefined;
 		} else {
			type = inPos < outPos ? Directions.In : Directions.Out;
 		}
 	}
 	
 	
 
 }
